/*
 * Copyright 2022 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.AndFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.FilterableTypes
import com.monkopedia.krapper.FilterableTypes.CLASS
import com.monkopedia.krapper.FilterableTypes.FIELD
import com.monkopedia.krapper.FilterableTypes.METHOD
import com.monkopedia.krapper.FilterableTypes.TYPE
import com.monkopedia.krapper.HierarchyFilter
import com.monkopedia.krapper.HierarchyTarget.ALL_CHILDREN
import com.monkopedia.krapper.HierarchyTarget.ANY_CHILD
import com.monkopedia.krapper.HierarchyTarget.BASE
import com.monkopedia.krapper.HierarchyTarget.PARENT
import com.monkopedia.krapper.NotFilter
import com.monkopedia.krapper.OrFilter
import com.monkopedia.krapper.StringFilter
import com.monkopedia.krapper.StringMatcher
import com.monkopedia.krapper.StringMatcherType.CONTAINS
import com.monkopedia.krapper.StringMatcherType.ENDS_WITH
import com.monkopedia.krapper.StringMatcherType.EQUALS
import com.monkopedia.krapper.StringMatcherType.REGEX
import com.monkopedia.krapper.StringMatcherType.STARTS_WITH
import com.monkopedia.krapper.StringSelector
import com.monkopedia.krapper.StringSelector.CLASS_NAME
import com.monkopedia.krapper.StringSelector.CLASS_QUALIFIED
import com.monkopedia.krapper.StringSelector.METHOD_NAME
import com.monkopedia.krapper.StringSelector.METHOD_RETURN_TYPE
import com.monkopedia.krapper.StringSelector.METHOD_TYPE
import com.monkopedia.krapper.StringSelector.NAMESPACE
import com.monkopedia.krapper.StringSelector.STRINGIFY
import com.monkopedia.krapper.TypeFilter
import com.monkopedia.krapper.filter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.ModelIo
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.baseParent
import com.monkopedia.krapper.generator.model.cloneRecursive
import com.monkopedia.krapper.generator.model.filterRecursive
import com.monkopedia.krapper.generator.model.forEachRecursive
import com.monkopedia.krapper.generator.model.freeFunctionQualifiedName
import com.monkopedia.krapper.generator.model.parentClass
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.resolvedmodel.MethodType.STATIC
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedNamespace
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType
import kotlin.math.min

typealias ElementFilter = WrappedElement.() -> Boolean

// THE FRONT-END (#45 brick 2; flip B5), set by KrapperGen --parsedModel <path>.
// krapper_gen has a SINGLE front-end: it loads its parse-output WrappedTU from this path —
// the full-fidelity ModelIo JSON the krapper_parse binary emitted from the Clang C++ AST (on
// kplusplus-generated bindings). The loaded tree enters the pipeline at the post-parse
// point: the pre-resolution rewrites below run on it, then ParsedResolver wraps it. The
// in-tree libclang-C reducer was DELETED in B5 (#47) — there is no in-process parse path.
// CLI-scoped global so the ksrpc service schema (:slice) stays untouched.
var cppParsedModelPath: String? = null

// Instantiation forcing (#45 brick 3), set by KrapperGen --forcingModel <spec>=<path>.
// Maps each normalized instantiation target (e.g. "std::vector<Item*>") to the ModelIo JSON
// of its FORCING-PARSE WrappedTU — the tree the krapper_parse binary produces by synthesizing
// a `KrapperForce_*` header and parsing it. requestInstantiation loads the tree, and the
// whole downstream flow (scoping + resolveForcing's 3-pass dance + cumulative keys + dedup)
// runs over it. CLI-scoped global, same rationale as [cppParsedModelPath].
var cppForcingModelPaths: Map<String, String> = emptyMap()

/**
 * Load a ModelIo WrappedTU from [path] and prepare it for resolution exactly the way a
 * libclang parse is prepared: run the pre-resolution rewrites, then wrap in a
 * ParsedResolver. Shared by the --frontend=cpp base-model hook in [parseHeader] and the
 * per-instantiation forcing-model load in IndexedServiceImpl.requestInstantiation. No
 * cursor->element memo exists on this path (nothing was parsed in-process); identity
 * across the separately-loaded trees is structural (type-string keys), which is all the
 * forcing flow consults (alreadyBoundKeys / tracker keys / dedup are string-keyed).
 */
internal suspend fun loadParsedModel(path: String): ParsedResolver {
    val restored = ModelIo.decodeFromString(File(path).readText())
    rewriteViewReturns(restored)
    rewriteUniquePtrReturns(restored)
    return ParsedResolver(restored)
}

// The base-model tree loaded by [parseHeader]'s --parsedModel hook, kept so each
// forcing-model load can mirror libclang's first-seen ordering against it (O1, #46).
internal var cppBaseModelTu: WrappedTU? = null

/**
 * Load a FORCING model (#46 O1): [loadParsedModel], then reorder its namespace-level
 * children into the BASE model's first-seen order. On the libclang path the base parse
 * and every forcing re-parse share one Index, so ModelFactories' USR-keyed elementLookup
 * memo collapses each namespace onto the element the base parse already populated —
 * re-encountered decls keep their base position and only NEW decls append. The forcing
 * TU's own lexical order DIFFERS from the base TU's whenever ForcingHeader prepends a
 * std include (e.g. `#include <vector>` pulls <new> in front of the user header's
 * <cwchar>), so a per-payload tree loaded as-is would emit that round's std functions
 * in a SPEC-DEPENDENT order. First-seen base order is the spec-invariant choice.
 */
internal suspend fun loadForcingModel(path: String): ParsedResolver {
    val resolver = loadParsedModel(path)
    cppBaseModelTu?.let { base -> reorderToFirstSeen(resolver.tu, base) }
    return resolver
}

// The structural identity used ONLY for the first-seen ordering above — the same
// name/signature granularity the forcing flow's string-keyed merge consults
// (alreadyBoundKeys / forcingIdentity / last-wins dedup).
private fun WrappedElement.firstSeenKey(): String? = when (this) {
    is WrappedNamespace -> "n:$namespace"
    is WrappedTemplate -> "T:$name"
    is WrappedClass -> "c:$name"
    is WrappedTypedef -> "t:$name"
    is WrappedMethod -> "m:$name(${args.joinToString(",") { it.type.toString() }})"
    else -> null
}

// Stable-sort [forcing]'s children by their first occurrence in [base]'s children
// (children with no base counterpart — the KrapperForce_* struct, new decls — keep
// their own relative order AFTER every base-seen child, exactly where the libclang
// memo would APPEND them), then recurse into namespaces. Class members are untouched:
// they attach from the defining redeclaration on both paths.
private fun reorderToFirstSeen(forcing: WrappedElement, base: WrappedElement) {
    val baseOrder = mutableMapOf<String, Int>()
    val baseByKey = mutableMapOf<String, WrappedElement>()
    base.children.forEachIndexed { index, child ->
        val key = child.firstSeenKey() ?: return@forEachIndexed
        if (key !in baseOrder) {
            baseOrder[key] = index
            baseByKey[key] = child
        }
    }
    val reordered = forcing.children
        .sortedBy { baseOrder[it.firstSeenKey()] ?: Int.MAX_VALUE }
    forcing.clearChildren()
    forcing.addAllChildren(reordered)
    for (child in reordered) {
        if (child !is WrappedNamespace) continue
        (baseByKey[child.firstSeenKey()] as? WrappedNamespace)
            ?.let { reorderToFirstSeen(child, it) }
    }
}

fun FilterDefinition.wrapperFilter(): (WrappedElement) -> Boolean {
    return when (this) {
        is AndFilter -> {
            val each = this.elements.map { it.wrapperFilter() }
            return { element ->
                each.all { it(element) }
            }
        }

        is OrFilter -> {
            val each = this.elements.map { it.wrapperFilter() }
            return { element ->
                each.any { it(element) }
            }
        }

        is NotFilter -> {
            val base = this.base.wrapperFilter()
            return { element ->
                !base(element)
            }
        }

        DefaultFilter -> {
            return defaultFilter().wrapperFilter()
        }

        is AllowListFilter -> {
            val allow = this.qualifiedNames.toSet()
            return { element ->
                (element is WrappedClass && element.type.toString() in allow) ||
                    // T1.0c: a free function is a STATIC WrappedMethod with no parent
                    // class — it can never match a class allowlist entry by `type`. Match
                    // it by its fully-qualified spelling `<namespace>::<name>` (where
                    // `qualified` yields only the enclosing namespace path, the function
                    // name not being a named segment), so `--only foo::bar` selects it.
                    (
                        element is WrappedMethod && element.methodType == STATIC &&
                            element.parentClass == null &&
                            element.freeFunctionQualifiedName in allow
                        )
            }
        }

        is HierarchyFilter -> {
            val base = this.filter.wrapperFilter()
            return when (this.target) {
                PARENT -> { element ->
                    element.parent?.let { base(it) } ?: false
                }

                BASE -> { element ->
                    base(element.baseParent)
                }

                ANY_CHILD -> { element ->
                    element.children.any(base)
                }

                ALL_CHILDREN -> { element ->
                    element.children.all(base)
                }
            }
        }

        is StringFilter -> {
            return { element ->
                val str = this.selector.select(element)
                this.matcher.matches(str)
            }
        }

        is TypeFilter -> {
            return { element ->
                types.any {
                    when (it) {
                        CLASS -> element is WrappedClass
                        METHOD -> element is WrappedMethod
                        FIELD -> element is WrappedField
                        TYPE -> element is WrappedType
                        FilterableTypes.NAMESPACE -> element is WrappedNamespace
                    }
                }
            }
        }
    }
}

fun FilterDefinition.resolveFilter(): (ResolvedElement) -> Boolean {
    return when (this) {
        is AndFilter -> {
            val each = this.elements.map { it.resolveFilter() }
            return { element ->
                each.all { it(element) }
            }
        }

        is OrFilter -> {
            val each = this.elements.map { it.resolveFilter() }
            return { element ->
                each.any { it(element) }
            }
        }

        is NotFilter -> {
            val base = this.base.resolveFilter()
            return { element ->
                !base(element)
            }
        }

        DefaultFilter -> {
            return defaultFilter().resolveFilter()
        }

        is AllowListFilter -> {
            val allow = this.qualifiedNames.toSet()
            return { element ->
                (element is ResolvedClass && element.type.type in allow) ||
                    // T1.0c (resolved side, mirror of wrapperFilter): match a free
                    // function (STATIC method, no parent class) by `<qualified>::<name>`.
                    // `qualified` carries only the enclosing namespace, so append `name`.
                    (
                        element is ResolvedMethod && element.methodType == STATIC &&
                            element.parentClass == null &&
                            element.qualified.let {
                                if (it.isEmpty()) element.name else "$it::${element.name}"
                            } in allow
                        )
            }
        }

        is HierarchyFilter -> {
            val base = this.filter.resolveFilter()
            return when (this.target) {
                PARENT -> { element ->
                    element.parent?.let { base(it) } ?: false
                }

                BASE -> { element ->
                    base(element.baseParent)
                }

                ANY_CHILD -> { element ->
                    element.children.any(base)
                }

                ALL_CHILDREN -> { element ->
                    element.children.all(base)
                }
            }
        }

        is StringFilter -> {
            return { element ->
                val str = this.selector.select(element)
                this.matcher.matches(str)
            }
        }

        is TypeFilter -> {
            return { element ->
                types.any {
                    when (it) {
                        CLASS -> element is ResolvedClass
                        METHOD -> element is ResolvedMethod
                        FIELD -> element is ResolvedField
                        TYPE -> element is ResolvedType
                        FilterableTypes.NAMESPACE -> element is ResolvedNamespace
                    }
                }
            }
        }
    }
}

private fun StringMatcher.matches(input: String?): Boolean {
    val target = input ?: return false
    return when (this.type) {
        STARTS_WITH -> {
            target.startsWith(this.str)
        }

        CONTAINS -> {
            target.contains(this.str)
        }

        EQUALS -> {
            target == this.str
        }

        ENDS_WITH -> {
            target.endsWith(this.str)
        }

        REGEX -> {
            Regex(this.str).matches(target)
        }
    }
}

private fun StringSelector.select(element: ResolvedElement): String? = when (this) {
    STRINGIFY -> element.toString()
    CLASS_NAME -> (element as? ResolvedClass)?.name
    CLASS_QUALIFIED -> (element as? ResolvedClass)?.type?.type
    METHOD_NAME -> (element as? ResolvedMethod)?.name
    METHOD_TYPE -> (element as? ResolvedMethod)?.methodType?.toString()
    METHOD_RETURN_TYPE -> (element as? ResolvedMethod)?.returnType?.type
    NAMESPACE -> (element as? ResolvedNamespace)?.namespace
}

private fun StringSelector.select(element: WrappedElement): String? = when (this) {
    STRINGIFY -> element.toString()
    CLASS_NAME -> (element as? WrappedClass)?.name
    CLASS_QUALIFIED -> (element as? WrappedClass)?.type?.toString()
    METHOD_NAME -> (element as? WrappedMethod)?.name
    METHOD_TYPE -> (element as? WrappedMethod)?.methodType?.toString()
    METHOD_RETURN_TYPE -> (element as? WrappedMethod)?.returnType?.toString()
    NAMESPACE -> (element as? WrappedNamespace)?.namespace
}

fun defaultFilter(): FilterDefinition = filter {
    (
        (thiz isType ResolvedClass) and
            (!(qualified startsWith "std::")) and
            (!(qualified.startsWith("__")))
        ) or (
        (thiz isType ResolvedMethod) and
            (methodType eq STATIC.name) and
            !(parent isType ResolvedClass) and
            base(!(stringified startsWith "std")) and
            parent(!((thiz isType ResolvedNamespace) and (namespace startsWith "_")))
        )
}

fun WrappedElement.defaultFilter(): Boolean {
    if (this is WrappedClass) {
        return !type.toString().startsWith("std::") && !type.toString().startsWith("__")
    }
    if (this is WrappedMethod && this.methodType == STATIC) {
        return this.parentClass == null && !this.baseParent.toString()
            .startsWith("std") && !this.name.startsWith("_") &&
            (this.parent as? WrappedNamespace)?.namespace?.startsWith("_") != true
    }
    return false
}

class ParsedResolver(val tu: WrappedTU) : Resolver {
    private val classMap = mutableMapOf<String, Pair<ResolvedClass, WrappedClass>?>()
    private val templateMap = mutableMapOf<String, WrappedTemplate>()

    // ---- Type-string index over the TU (issue #10 / PR #160 base-bind hotspot) ----
    //
    // `resolve`/`resolveTemplate` used to locate a class/template by re-walking the ENTIRE
    // ~73k-node TU with `filterRecursive` on every distinct type — O(distinct-types × tree-size),
    // measured at ~1.27 B node visits / ~309 s by the #160 profiler. These indices replace that
    // with an O(1) map lookup keyed by the SAME strings `filterRecursive` compared against:
    //   * classIndex:    `WrappedClass.type.toString()` -> the WrappedClass node(s) with that type
    //   * templateIndex: `WrappedTemplate.qualified`     -> the WrappedTemplate node(s) with it
    //
    // BYTE-IDENTICAL contract — the index must return EXACTLY what `filterRecursive{…}.singleOrNull()`
    // returned. Two invariants make that hold:
    //
    //  1. Match semantics preserved AT QUERY TIME. `filterRecursive` returned ALL matches and the
    //     callers took `singleOrNull()` (the node iff exactly one match; null for 0 or 2+). So the
    //     index stores a LIST per key (grouped, pre-order — same order filterRecursive produced) and
    //     the query applies `singleOrNull()` itself. Crucially the `isNotEmpty()` predicate the class
    //     scan ANDs in is evaluated at QUERY time on the (small) candidate list, NOT frozen at build
    //     time: `isNotEmpty()` reflects the class's live children, and although the resolve loop can
    //     mutate a class's children (modifyMethodsIfNeeded adds a default ctor + sizeOf/alignOf;
    //     dropConstOverloadDuplicates / hasHiddenDelete remove members), evaluating it lazily makes
    //     the index observe the exact same value the live `filterRecursive` scan would have. (In
    //     practice the synthetic additions are excluded by `calculateNotEmpty`, so emptiness is
    //     stable, but querying live keeps us correct without relying on that.)
    //
    //  2. The indexed node SET is FIXED for the resolver's lifetime, so a one-time build is complete.
    //     No WrappedClass or WrappedTemplate is ever ADDED to `tu` during resolution: INCLUDE_MISSING
    //     materializations (`typedAs`) build a DETACHED WrappedClass stored only in classMap/tracker
    //     (never `tu.addChild`-ed), and the only in-tree mutations are non-Class/non-Template members
    //     (ctor/sizeOf/alignOf) and the pre-resolution rewrites that all run BEFORE the first query.
    //     The key strings (`type`/`qualified`) derive from the parent chain, which is likewise stable
    //     once resolution starts (the forcing first-seen reorder runs during loadForcingModel, before
    //     resolve). So building once (lazily, on first query) captures every node any scan could match.
    //
    // Built lazily on first use (after findClasses / the pre-resolution rewrites have run) in ONE
    // `forEachRecursive` pass: ~1.27 B node visits collapse to a single ~73k-node walk.
    private var indexBuilt = false
    private val classIndex = mutableMapOf<String, MutableList<WrappedClass>>()
    private val templateIndex = mutableMapOf<String, MutableList<WrappedTemplate>>()

    private fun ensureIndex() {
        if (indexBuilt) return
        var walkedNodes = 0
        tu.forEachRecursive { element ->
            if (BaseBindProfiler.enabled) walkedNodes++
            when (element) {
                is WrappedClass ->
                    classIndex.getOrPut(element.type.toString()) { mutableListOf() }.add(element)

                is WrappedTemplate ->
                    templateIndex.getOrPut(element.qualified) { mutableListOf() }.add(element)

                else -> {}
            }
        }
        // One build pass instead of one whole-TU walk per distinct type — the whole point of #10.
        BaseBindProfiler.recordTreeWalk(walkedNodes)
        indexBuilt = true
    }

    override fun resolveTemplate(type: WrappedType, context: ResolveContext): WrappedTemplate =
        templateMap.getOrPut(type.toString()) {
            ensureIndex()
            // Mirror `filterRecursive { qualified == key }.singleOrNull()`: the single template
            // with this qualified name, or fail (0 or 2+ candidates -> singleOrNull() == null).
            templateIndex[type.toString()]?.singleOrNull()
                ?: error("Can't resolve template $type (${type::class.simpleName})")
        }

    override suspend fun resolve(
        type: WrappedType,
        context: ResolveContext
    ): Pair<ResolvedClass, WrappedClass>? {
        val key = type.toString()
        // When the profiler is OFF this is exactly the original `classMap.getOrPut(key) { ... }`.
        // When ON, a memo HIT returns early (no timing — no work done); only a genuine MISS is
        // timed + recorded, so the rate reflects real per-type resolve work, not memo lookups.
        if (!BaseBindProfiler.enabled) {
            return classMap.getOrPut(key) { computeResolve(type, key, context) }
        }
        classMap[key]?.let { return it }
        val resolveMark = kotlin.time.TimeSource.Monotonic.markNow()
        val result = classMap.getOrPut(key) { computeResolve(type, key, context) }
        BaseBindProfiler.recordResolved(key, resolveMark.elapsedNow().inWholeMicroseconds / 1000.0)
        return result
    }

    private suspend fun computeResolve(
        type: WrappedType,
        key: String,
        context: ResolveContext
    ): Pair<ResolvedClass, WrappedClass>? {
        ensureIndex()
        // Mirror `filterRecursive { type.toString() == key && it.isNotEmpty() }.singleOrNull()`:
        // among the classes indexed under this type-string, keep the non-empty ones (evaluated
        // LIVE, not at index-build time — see the invariant note above) and take singleOrNull().
        val existingClass = classIndex[key]
            ?.filter { it.isNotEmpty() }
            ?.singleOrNull()
        existingClass?.let { cls ->
            return cls.resolve(context)?.let { it to cls }
        }
        return when (type) {
            is WrappedTemplateType -> {
                val template = resolveTemplate(type.baseType, context)
                template.typedAs(type, context)
            }

            is WrappedTemplateRef -> {
                throw IllegalArgumentException("Can't resolve $type since it is templated")
            }

            else -> {
                error("Can't resolve $type (${type::class.simpleName})")
            }
        }
    }

    override suspend fun findClasses(filter: ElementFilter): List<WrappedElement> {
        Log.i("Finding classes")
        return mutableListOf<WrappedElement>().also { ret ->
            tu.forEachRecursive {
                if (it.filter() == true) {
                    ret.add(it)
                }
            }
        }.also {
            Log.i("Found ${it.size} classes")
        }
    }
}

/**
 * krapper_gen's SINGLE front-end (flip B5, #47): load the parse-output WrappedTU from the
 * ModelIo JSON the krapper_parse binary produced (--parsedModel) and prepare it for
 * resolution. The in-tree libclang-C reducer was deleted — there is NO in-process parse
 * path. [cppParsedModelPath] must be set (the CLI requires --parsedModel); calling this
 * with it unset is a misconfiguration and fails loudly.
 *
 * The loaded tree enters the pipeline at the post-parse point: the pre-resolution rewrites
 * (rewriteViewReturns/rewriteUniquePtrReturns) run on it, then ParsedResolver wraps it. The
 * base tree is stashed in [cppBaseModelTu] for the forcing loads' first-seen reorder
 * (O1, #46 — loadForcingModel).
 */
suspend fun parseHeader(): Resolver {
    val path = cppParsedModelPath
        ?: error(
            "krapper_gen has no in-process parse front-end (the libclang-C reducer was " +
                "removed in flip B5, #47). A parse-output model is required: pass " +
                "--parsedModel <ModelIo JSON> (the krapper_parse binary produces it)."
        )
    Log.i("cpp front-end: loading parsed model from $path")
    return loadParsedModel(path).also { cppBaseModelTu = it.tu }
}

suspend fun WrappedTemplate.typedAs(
    templateSpec: WrappedTemplateType,
    baseContext: ResolveContext
): Pair<ResolvedClass, WrappedClass>? {
    val fullyQualified = templateSpec.baseType.toString()
    val templates =
        filterRecursive { it is WrappedTemplateParam }.filterIsInstance<WrappedTemplateParam>()
    val mapping = mutableMapOf<String, WrappedType?>()
    for (i in 0 until min(templates.size, templateSpec.templateArgs.size)) {
        val mappedType = baseContext.map(templateSpec.templateArgs[i])
            ?: throw IllegalArgumentException(
                "Can't resolve ${templateSpec.templateArgs[i]} in $templateSpec"
            )
        mapping[templates[i].name] = mappedType
        mapping[templates[i].usr] = mappedType
        for (extra in templates[i].otherParams) {
            mapping[extra.name] = mappedType
            mapping[extra.usr] = mappedType
        }
    }
    val mapper: TypeMapping = { type, context ->
        if (type.toString() == fullyQualified) {
            ReplaceWith(templateSpec)
        } else {
            type.operateOn { type ->
                if (type is WrappedTemplateRef) {
                    val mapping = mapping[type.target]
                    if (mapping != null) {
                        val result = baseContext.typeMapping(mapping, context)
                        return@operateOn if (result == ElementUnchanged) {
                            ReplaceWith(mapping)
                        } else {
                            result
                        }
                    }
                }
                if (type.toString() == fullyQualified) {
                    val result = baseContext.typeMapping(templateSpec, context)
                    if (result == ElementUnchanged) {
                        ReplaceWith(templateSpec)
                    } else {
                        result
                    }
                } else {
                    baseContext.typeMapping(type, context)
                }
            }
        }
    }
    val localContext = baseContext.copy(typeMapping = mapper, mappingCache = mutableMapOf())
    for (i in min(templates.size, templateSpec.templateArgs.size) until templates.size) {
        val defaultType = templates[i].defaultType ?: continue
        val mappedType = mapper(defaultType, localContext)
        if (mappedType is RemoveElement) continue
        val type = if (mappedType is ReplaceWith) mappedType.replacement else defaultType
        mapping[templates[i].name] = type
        mapping[templates[i].usr] = type
    }
    val outputClass = WrappedClass(name, false, templateSpec)
    outputClass.metadata = metadata.copy()
    outputClass.parent = parent
    outputClass.addAllChildren(children.map { it.cloneRecursive() })
    removeDuplicateMethods(outputClass)
    dropMistypedInitializerListMembers(outputClass, fullyQualified)
    rewritePairSecondReturns(outputClass, fullyQualified)
    rewriteViewReturns(outputClass)
    dropValueEqualityIfElementLacksEquals(outputClass, templateSpec, baseContext, fullyQualified)
    return outputClass.resolve(localContext)?.let { it to outputClass }
}

/**
 * Drop methods/constructors taking a `std::initializer_list<E>` whose element type
 * `E` libclang mis-resolved to a bare template parameter on a key→value map.
 *
 * For `std::unordered_map<K, V>`, libstdc++ defines `value_type` through a chain
 * of `_Hashtable`/`__detail` trait expressions that libclang collapses to the
 * bare mapped parameter `_Tp` instead of `std::pair<const _Key, _Tp>`. Every
 * member taking `initializer_list<value_type>` (the `initializer_list` constructor,
 * `insert`, `operator=`) then inherits that wrong element, so the generated wrapper
 * calls e.g. `unordered_map(initializer_list<int>, ...)` / `insert(initializer_list<int>)`,
 * which fail to compile (the real signatures want `initializer_list<pair<const int,int>>`).
 * `std::map` doesn't hit this — libclang reports ITS value_type correctly as the
 * pair, so the same members drop on their own during resolution.
 *
 * Reconstructing the correct pair element would require modelling the dependent
 * trait chain; instead we drop just these unconstructable members (the Kotlin facade
 * can't build a C++ initializer_list anyway). For `std::map` this is invisible —
 * libclang resolves its value_type correctly, so these members already dropped on
 * their own. For `std::unordered_map` the at/operator[]/count accessors are
 * ADDITIONALLY dropped by the resolver (the _Hashtable base-class trait chain) — a
 * separate follow-up; this fix only stops the init-list ctor from crashing the sync.
 *
 * Scoped to key→value maps (classes exposing both `key_type` and `mapped_type`
 * typedefs) so sequence/set containers — where `value_type` genuinely IS the bare
 * element (`std::vector<T>`, `std::unordered_set<T>`) — keep their valid
 * initializer_list members.
 */
private suspend fun dropMistypedInitializerListMembers(
    element: WrappedElement,
    fullyQualified: String
) = rewriteMethods(element) { owner, method ->
    if (owner !is WrappedClass) return@rewriteMethods method
    val typedefNames = owner.children
        .filterIsInstance<WrappedTypedef>()
        .map { it.name }
        .toSet()
    val isKeyValueMap = "key_type" in typedefNames && "mapped_type" in typedefNames
    if (!isKeyValueMap) return@rewriteMethods method
    val hasBareTemplateInitializerList = method.args.any { arg ->
        val type = arg.type.maybeUnconst.maybeUnreferenced
        type is WrappedTemplateType &&
            type.baseType.toString() == "std::initializer_list" &&
            type.templateArgs.singleOrNull() is WrappedTemplateRef
    }
    if (!hasBareTemplateInitializerList) return@rewriteMethods method
    Log.i(
        "Dropping mistyped initializer_list member ${method.name} on " +
            "$fullyQualified (libclang collapsed value_type to a bare " +
            "template parameter)"
    )
    null
}

/**
 * Shared method-rewrite walker for the structurally-identical recursive passes below
 * ([dropMistypedInitializerListMembers], [rewritePairSecondReturns], [rewriteViewReturns],
 * [rewriteUniquePtrReturns]). Recurses [element]'s subtree; for every [WrappedMethod] child
 * of every element it calls [transform], which returns:
 *  - the same method instance -> leave it in place (no change),
 *  - a different method        -> replace it (removeChild + addChild),
 *  - `null`                    -> drop it (removeChild).
 * This owns the remove/add bookkeeping and the recursion tail the passes used to copy-paste.
 * Each pass guards on the element type inside [transform] (returning the method unchanged) so
 * the exact set of rewritten/dropped methods - and the traversal order - is preserved.
 */
private suspend fun rewriteMethods(
    element: WrappedElement,
    transform: suspend (owner: WrappedElement, method: WrappedMethod) -> WrappedMethod?
) {
    for (method in element.children.filterIsInstance<WrappedMethod>()) {
        val result = transform(element, method)
        if (result === method) continue
        element.removeChild(method)
        if (result != null) element.addChild(result)
    }
    for (child in element.children) {
        rewriteMethods(child, transform)
    }
}

/**
 * Rewrite methods returning `std::pair<UNRESOLVABLE_ITERATOR, bool>` (notably
 * `std::set`/`std::multiset`/`std::unordered_set` `insert(const value_type&)`) to plain
 * `bool`. (Map `insert` is also rewritten but still drops on its `pair` value_type arg.)
 *
 * `insert` returns `std::pair<iterator, bool>` where the `bool` reports whether the
 * element was newly inserted and the iterator points at it. The iterator half is a real
 * nested class (`std::_Rb_tree_iterator<...>`) that we don't wrap, so the whole method
 * would otherwise fail return-type resolution and silently drop — taking the useful
 * was-inserted bool with it.
 *
 * We rewrite the return type to bare `bool` and set [WrappedMethod.returnsPairSecond];
 * CppWriter then emits `(thiz->insert(x)).second` so the wrapper hands back just the
 * bool. Scoped narrowly: the return must be a bare `std::pair<_, bool>` template type
 * (no const/ref) whose second arg is `bool` — hint/range/initializer_list `insert`
 * overloads (which return an iterator or void) are untouched and stay dropped.
 */
private suspend fun rewritePairSecondReturns(element: WrappedElement, fullyQualified: String) =
    rewriteMethods(element) { owner, method ->
        if (owner !is WrappedClass) return@rewriteMethods method
        val ret = method.returnType
        val isPairBool = ret is WrappedTemplateType &&
            ret.baseType.toString() == "std::pair" &&
            ret.templateArgs.size == 2 &&
            ret.templateArgs[1].toString() == "bool"
        if (!isPairBool) return@rewriteMethods method
        val rewritten = method.copy(returnType = WrappedType("bool")).also {
            it.returnsPairSecond = true
        }
        Log.i(
            "Rewriting ${method.name} on $fullyQualified: " +
                "$ret -> bool (returning the pair's .second; iterator half unwrappable)"
        )
        rewritten
    }

/**
 * T1.10 view-type marshalling. A method returning a non-owning string view
 * (`llvm::StringRef` — a `(const char*, size_t)` borrow) can't hand the view across
 * the boundary safely (the pointee may outlive nothing). Rewrite the return to an
 * owned `std::string` and set [WrappedMethod.returnViaMemberCall] = "str"; CppWriter
 * then emits `(thiz->getName()).str()`, materializing an owned copy that flows through
 * the existing std::string return machinery (STRING style → Kotlin `String`).
 *
 * Matched by qualified type name (with/without a leading const, by value or const&),
 * so it applies whether the view is returned bare or as `const StringRef&`. Generic in
 * that ANY recognized view name routes the same way; today only `llvm::StringRef` is
 * listed (it's the view the Clang AST surface forces). Add sibling views here as needed.
 */
internal suspend fun rewriteViewReturns(element: WrappedElement) =
    rewriteMethods(element) { owner, method ->
        if (owner !is WrappedClass) return@rewriteMethods method
        val ret = method.returnType
        // The view's underlying class name, with any const/reference peeled off.
        val baseName = ret.let { if (it.isReference) it.unreferenced else it }
            .let { if (it.isConst) it.unconst else it }
            .toString()
        if (baseName !in STRING_VIEW_TYPES) return@rewriteMethods method
        val rewritten = method.copy(returnType = WrappedType("std::string")).also {
            it.returnViaMemberCall = "str"
        }
        Log.i(
            "Rewriting ${method.name} on ${owner.type}: " +
                "$ret -> std::string (materializing the view via .str())"
        )
        rewritten
    }

// Non-owning string-view types. As a RETURN they are materialized to an owned std::string
// via `.str()` (T1.10, rewriteViewReturns); as a PARAM they are constructed from an inbound
// `const char*` at the C boundary (T1.10p, determineArgumentCastMode -> STRING_VIEW).
internal val STRING_VIEW_TYPES = setOf("llvm::StringRef")

// #10 GATE 1 (value-equality). The container member names whose BODY instantiates
// `element == element`: llvm::ArrayRef<T>::equals delegates to std::equal, and the
// std::vector<T> free `operator==`/`operator!=` compare element-wise. When the concrete
// element type `T` is a RECORD that declares no `operator==` anywhere in scope, these
// members fail to instantiate deep in <stl_algobase.h> — the exact invalid-operands tail
// (#10). Drop them here, at template materialization, when the element is PROVEN to lack
// `==` (its parse-time metadata flag is false); keep-on-doubt otherwise.
private val VALUE_EQUALITY_MEMBERS = setOf("equals", "operator==", "operator!=")

/**
 * #10 GATE 1 consumer. For the just-materialized specialization [outputClass], drop each
 * [VALUE_EQUALITY_MEMBERS] member iff the container's element type is a record PROVEN to have
 * no `operator==` — the parse-time [ClassMetadata.hasEqualityOperator] fact carried on the
 * element's [WrappedClass] in [baseContext]'s tracker.
 *
 * Keep-on-doubt is load-bearing (a false DROP loses a valid binding — forbidden):
 *  - a non-record element (scalar / pointer / enum — which have a builtin `==`) is never in
 *    the class tracker, so it is left untouched;
 *  - a record we can't find in the tracker (unbound / out-of-scope) is left untouched;
 *  - ONLY an element record whose metadata says `hasEqualityOperator == false` gates.
 */
private fun dropValueEqualityIfElementLacksEquals(
    outputClass: WrappedClass,
    templateSpec: WrappedTemplateType,
    baseContext: ResolveContext,
    fullyQualified: String
) {
    val element = templateSpec.templateArgs.singleOrNull()
        ?.let { if (it.isConst) it.unconst else it }
        ?: return
    // Only a bare record element can lack `==`; a pointer/reference element (std::vector<T*>)
    // compares pointers with the builtin `==` and must never be gated.
    if (element.isPointer || element.isReference) return
    val elementClass = baseContext.tracker.classes[element.toString()] ?: return
    if (elementClass.metadata.hasEqualityOperator) return
    for (method in outputClass.children.filterIsInstance<WrappedMethod>()) {
        if (method.name !in VALUE_EQUALITY_MEMBERS) continue
        DropLedger.record(
            "$fullyQualified::${method.name}",
            "Element type $element has no operator== (container value-equality would " +
                "fail to instantiate) [#10 gate 1]",
            DropPhase.RESOLVE
        )
        outputClass.removeChild(method)
    }
}

/**
 * T1.7e unique_ptr-return marshalling. A by-value `std::unique_ptr<T>` return is
 * move-only, so the by-value Holder placement-new path can't copy-construct it and the
 * method silently DROPS. Rewrite the return to a raw `T*` and set
 * [WrappedMethod.returnViaMemberCall] = "release"; CppWriter then emits
 * `(thiz->factory()).release()`, transferring ownership of the heap object to the caller
 * as a raw pointer. The pointer flows through the normal pointer-return (VOIDP) machinery,
 * so the Kotlin side gets the usual non-owning wrapper with `.owned()`/`dispose()` — the
 * caller decides lifetime (the pointee must have an accessible destructor).
 *
 * Generic over any `std::unique_ptr<T>` whose single template arg `T` is a concrete type
 * (so `T*` resolves to a bound wrapper). The custom-deleter form
 * `unique_ptr<T, Deleter>` is left untouched — `.release()` still hands back `T*`, but the
 * caller-side dispose path can't honor a non-default deleter, so we don't pretend to.
 */
// Apply to a class's methods AND to namespace-level FREE FUNCTIONS — a free function
// returning std::unique_ptr<T> (e.g. clang::tooling::buildASTFromCode -> unique_ptr<ASTUnit>,
// the AST-walk entry point) needs the same release()-rewrite or its move-only return drops.
internal suspend fun rewriteUniquePtrReturns(element: WrappedElement) =
    rewriteMethods(element) { owner, method ->
        if (owner !is WrappedClass && owner !is WrappedNamespace) return@rewriteMethods method
        val container = (owner as? WrappedClass)?.type?.toString()
            ?: (owner as? WrappedNamespace)?.namespace ?: "?"
        val ret = method.returnType
        // Match a bare by-value `std::unique_ptr<T>` (no const/reference, single arg).
        if (ret !is WrappedTemplateType) return@rewriteMethods method
        if (ret.baseType.toString() != "std::unique_ptr") return@rewriteMethods method
        val elementType = ret.templateArgs.singleOrNull() ?: return@rewriteMethods method
        val rewritten = method.copy(returnType = pointerTo(elementType)).also {
            it.returnViaMemberCall = "release"
        }
        Log.i(
            "Rewriting ${method.name} on $container: " +
                "$ret -> ${pointerTo(elementType)} (transferring ownership via .release())"
        )
        rewritten
    }

fun removeDuplicateMethods(element: WrappedElement) {
    if (element is WrappedClass) {
        val signaturesSeen = mutableSetOf<String>()
        for (child in element.children.filterIsInstance<WrappedMethod>()) {
            val signature = child.generateSignatureString()
            if (!signaturesSeen.add(signature)) {
                element.removeChild(child)
            }
        }
    }
    for (child in element.children) {
        removeDuplicateMethods(child)
    }
}

private fun WrappedMethod.generateSignatureString(): String = buildString {
    append(methodType.ordinal)
    append('#')
    append(name)
    append(',')
    for (argument in args) {
        append(argument.type.maybeUnconst.maybeUnreferenced.toString())
        append(',')
    }
}

private val WrappedType.maybeUnconst: WrappedType
    get() = if (isConst) unconst else this
private val WrappedType.maybeUnreferenced: WrappedType
    get() = if (isReference) unreferenced else this
