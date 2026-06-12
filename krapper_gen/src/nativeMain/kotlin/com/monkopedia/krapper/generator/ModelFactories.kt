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

import clang.CXAvailabilityKind
import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_ClassDecl
import clang.CXCursorKind.CXCursor_ClassTemplate
import clang.CXCursorKind.CXCursor_StructDecl
import clang.CXCursorKind.CXCursor_TypeAliasDecl
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXType
import clang.CXTypeKind.CXType_Pointer
import clang.CX_CXXAccessSpecifier
import com.monkopedia.krapper.generator.model.ElementIdentity
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.RangeReturn
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.WrappedTypedef
import com.monkopedia.krapper.generator.model.forEachIdentityPair
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents

// FRONT-END SEAM (issue #44): everything in this file CONSTRUCTS the pure parse-output
// model (model/ + model/type/) from libclang-C cursors. The model files themselves carry
// no clang/cinterop dependency, so an alternative front-end (e.g. one built on generated
// libclang-cpp bindings) can build the same model without this file.

private val elementLookup = mutableMapOf<String, WrappedElement>()

// USRs of declarations the diagnostic policy chose to drop at parse time (issue
// #9: an `error:` diagnostic attributable to a single declaration skips THAT
// declaration and lets the rest of the header bind). A dropped USR is excised here
// in [map] — returning null removes the declaration AND, via the
// `map(parentCursor) ?: return@forEachRecursive` guard in [mapAll], all of its
// members — so the broken declaration is never carried into resolution.
// Parse-scoped: [setDroppedUsrs] is called with each TU's set right before its
// [mapAll]. Mirrors the process-scoped [elementLookup] memo above.
private var droppedUsrs: Set<String> = emptySet()

/** Register the USRs of declarations dropped by the diagnostic policy for the next
 * [mapAll]. */
fun WrappedElement.Companion.setDroppedUsrs(usrs: Set<String>) {
    droppedUsrs = usrs
}

/**
 * Clear the process-global parse memo before a fresh generation run (#11 hermeticity).
 *
 * [elementLookup] interns parsed elements by USR-tag so a declaration re-encountered
 * across the base parse and every forcing re-parse WITHIN one run collapses onto a single
 * instance — resolution then mutates that instance in place (metadata, synthetic children,
 * dedup). That identity must NOT survive across runs: in service mode the v2 plugin's
 * kplusplusSync reuses one krapper_gen process, so a later run re-parsing the same header
 * would otherwise find the prior run's already-RESOLVED, mutated elements and build on
 * them, diverging from a fresh-process run. [droppedUsrs] is overwritten before each
 * [mapAll], but is reset here too so a fresh run never inherits a stale dropped set.
 * Called from `IndexedServiceImpl.init`, next to [DropLedger.reset]/[GenerationContext.reset].
 */
fun WrappedElement.Companion.resetElementMemo() {
    elementLookup.clear()
    droppedUsrs = emptySet()
}

/**
 * Round-trip oracle support (#45 brick 1): after `--roundTripModel` replaces a parsed
 * tree with its deserialized copy, point every [elementLookup] memo entry that referenced
 * an element of the ORIGINAL tree at its RESTORED counterpart. The memo is what carries
 * element identity ACROSS parses (a class re-encountered in a later forcing header is the
 * same instance, and resolution mutates it in place — metadata, synthetic children,
 * dedup), so without the remap the post-round-trip parses would keep building on the
 * abandoned originals and the run would diverge from a non-flag run for reasons that have
 * nothing to do with serialization fidelity.
 */
fun WrappedElement.Companion.remapMemoized(original: WrappedElement, restored: WrappedElement) {
    val replacements = HashMap<ElementIdentity, WrappedElement>()
    forEachIdentityPair(original, restored) { old, new ->
        replacements[ElementIdentity(old)] = new
    }
    for (entry in elementLookup.entries) {
        replacements[ElementIdentity(entry.value)]?.let { entry.setValue(it) }
    }
}

fun WrappedElement.Companion.mapAll(
    value: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder
): WrappedElement? {
    val element = map(value, null, null, resolverBuilder) ?: return null
    value.forEachRecursive { childCursor, parentCursor ->
        val parentUsr = parentCursor.usr.toKString()

        val parent =
            map(parentCursor, null, null, resolverBuilder) ?: return@forEachRecursive
        val child =
            map(childCursor, parent, parentUsr, resolverBuilder) ?: return@forEachRecursive
        if (child is WrappedTemplate) {
            child.templateArgCounter = 0
        }
        if (child == element) return@forEachRecursive
        if (child.parent == parent) return@forEachRecursive
        if (parent.children.contains(child)) {
            if (child is WrappedTemplateParam ||
                parent is WrappedTemplateParam ||
                child is WrappedTemplateRef ||
                child is WrappedNamespace ||
                child == WrappedType.UNRESOLVABLE
            ) {
                return@forEachRecursive
            }
//                    throw IllegalArgumentException(
//                        "Parent ($parent) already contains child ($child)"
//                    )
            return@forEachRecursive
        }
        if (child is WrappedMethod && parent is WrappedNamespace) {
            // Don't add a method to a namespace when its already been added to a class.
            if (child.parent is WrappedClass) {
                return@forEachRecursive
            }
            val parentKind = childCursor.semanticParent.kind
            if (parentKind == CXCursor_ClassDecl ||
                parentKind == CXCursor_ClassTemplate ||
                parentKind == CXCursor_StructDecl
            ) {
                return@forEachRecursive
            }
        }
        parent.addChild(child)
    }
    return element
}

private fun WrappedElement.Companion.map(
    value: CValue<CXCursor>,
    parent: WrappedElement?,
    parentUsr: String?,
    resolverBuilder: ResolverBuilder
): WrappedElement? {
    val usr = value.usr.toKString().orEmpty()
    // Excise a declaration the diagnostic policy dropped at parse time (issue #9).
    // Its members are excised transitively: when this cursor is a member's parent,
    // mapAll's `map(parentCursor) ?: return@forEachRecursive` guard skips the member.
    if (usr.isNotEmpty() && usr in droppedUsrs) {
        return null
    }
    val strTag = usr.ifEmpty { "$parentUsr:${value.spelling.toKString()}" }

    elementLookup[strTag]?.let { return it }

    if (value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPrivate ||
        value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXProtected ||
        value.availability == CXAvailabilityKind.CXAvailability_NotAvailable
    ) {
        if (value.kind == CXCursorKind.CXCursor_Constructor) {
            (parent as? WrappedClass)?.metadata?.hasConstructor = true
            (parent as? WrappedTemplate)?.metadata?.hasConstructor = true
            // A `= delete`d or non-public COPY constructor is filtered out here, but
            // its absence makes the type non-copyable for the C wrapper's by-value
            // Holder placement-new. Record it so canCopyConstruct can drop by-value
            // returns/fields of this type (T-skip).
            if (value.isCopyConstructor) {
                (parent as? WrappedClass)?.metadata?.hasDeletedCopyConstructor = true
                (parent as? WrappedTemplate)?.metadata?.hasDeletedCopyConstructor = true
            }
        }
        if (value.kind == CXCursorKind.CXCursor_CXXMethod) {
            val opName = value.referenced.spelling.toKString()
            if (opName == "operator new") {
                (parent as? WrappedClass)?.metadata?.hasHiddenNew = true
                (parent as? WrappedTemplate)?.metadata?.hasHiddenNew = true
            } else if (opName == "operator delete") {
                (parent as? WrappedClass)?.metadata?.hasHiddenDelete = true
                (parent as? WrappedTemplate)?.metadata?.hasHiddenDelete = true
            } else if (opName == "operator=" &&
                value.isCopyAssignmentOf(parent, resolverBuilder)
            ) {
                // A `= delete`d/non-public COPY ASSIGNMENT is filtered out here; record
                // it so a field of this type drops its `field = *value` setter (T-skip).
                (parent as? WrappedClass)?.metadata?.hasDeletedCopyAssignment = true
                (parent as? WrappedTemplate)?.metadata?.hasDeletedCopyAssignment = true
            }
        }
        if (value.kind == CXCursorKind.CXCursor_FieldDecl) {
            if (WrappedType(value.type, resolverBuilder).isConst) {
                (parent as? WrappedClass)?.metadata?.hasPrivateConstField = true
                (parent as? WrappedTemplate)?.metadata?.hasPrivateConstField = true
            }
        }
        return null
    }
    val element = try {
        when (value.kind) {
            CXCursorKind.CXCursor_StructDecl,
            CXCursorKind.CXCursor_ClassDecl -> {
                // An anonymous record (the unnamed `struct { ... }` in
                // `struct { int a; } anon;`) has no addressable name — libclang
                // spells it `(unnamed struct at <file>:<line>)`, which is not a
                // valid C++ type to wrap. Binding it emits broken C
                // (`G2_(unnamed struct at ...)_new`). Skip-not-crash: drop the
                // anonymous record; its enclosing class still binds (the anon
                // member itself is skipped by the FieldDecl blank-spelling guard).
                if (value.isAnonymous) {
                    return null
                }
                // A class/struct lexically NESTED inside a class TEMPLATE (e.g.
                // `template<class T> struct W { struct Inner { ... }; };`) has a
                // qualified name that can't be spelled standalone — naming
                // `W<...>::Inner` needs the enclosing template's arguments, which the
                // generator doesn't have. krapper flattens it to a bare
                // `clang::Inner` and emits `sizeof`/`reinterpret_cast`/field accessors
                // for a type the compiler can't find ("no type named 'Inner'"). Such a
                // type is unbindable; skip-not-crash: drop the whole class (and log).
                // A nested class inside a NON-template class (`Outer::Inner`) IS
                // nameable and is NOT dropped.
                if (value.isNestedInClassTemplate) {
                    DropLedger.record(
                        value.spelling.toKString() ?: "<unnamed class>",
                        "Class nested inside a class template " +
                            "(unnameable without template args)",
                        DropPhase.PARSE
                    )
                    println(
                        "WARN skip-not-crash: dropping class " +
                            "'${value.spelling.toKString()}' nested inside a class " +
                            "template (unnameable without template args)"
                    )
                    return null
                }
                WrappedClass(value, resolverBuilder)
            }

            CXCursorKind.CXCursor_FieldDecl -> {
                // Anonymous data members (anon bitfield padding `int :3;`, anon
                // union/struct members) have a blank spelling. They have no name to
                // address, and emitting an accessor for one produces broken C
                // (`thiz_cast->;` + an empty accessor name). Skip-not-crash: drop the
                // member; the rest of the class still binds.
                if (value.referenced.spelling.toKString().isNullOrBlank()) {
                    return null
                }
                // A (public) data member that is a REFERENCE or CONST makes the
                // enclosing class's implicit copy ASSIGNMENT deleted (you can't
                // rebind a reference or reassign a const), and a reference member
                // additionally deletes the implicit DEFAULT constructor (a reference
                // must be initialized). These special members are never emitted as
                // cursors (they're implicit), so the existing parse-time signal —
                // which only fires when an EXPLICIT `= delete`/non-public member is
                // filtered out — misses them. Record the structural facts here so a
                // field setter (`field = *value`) of this type is dropped (canAssign)
                // and no bogus default-construct (`new T()`) is synthesized for it
                // (T-skip residuals: implicitly-deleted copy-assign / default ctor).
                run {
                    val fieldType = WrappedType(value.type, resolverBuilder)
                    if (fieldType.isReference || fieldType.isConst) {
                        (parent as? WrappedClass)?.metadata
                            ?.hasDeletedCopyAssignment = true
                        (parent as? WrappedTemplate)?.metadata
                            ?.hasDeletedCopyAssignment = true
                    }
                    if (fieldType.isReference) {
                        (parent as? WrappedClass)?.metadata
                            ?.hasDeletedDefaultConstructor = true
                        (parent as? WrappedTemplate)?.metadata
                            ?.hasDeletedDefaultConstructor = true
                    }
                }
                WrappedField(value, resolverBuilder)
            }

            CXCursorKind.CXCursor_ParmDecl -> return null

            // WrappedArgument(value, resolverBuilder)
            CXCursorKind.CXCursor_TypedefDecl ->
                try {
                    WrappedTypedef(value, resolverBuilder)
                } catch (t: IllegalArgumentException) {
                    // Don't mind when parsing everything, if this reference is needed,
                    // it'll come up in resolution
                    return null
                }

            // A user-defined conversion operator (`operator double() const`,
            // `operator bool()`, ...) is, at the C ABI, a zero-arg method whose
            // return is the conversion target. clang exposes it as its own cursor
            // kind; route it through the same WrappedMethod path as a plain method
            // (its `referenced.spelling` is `operator double`, `type.result` is the
            // target type). Operator.from then recognizes it via ConversionOperator
            // and CppWriter/KotlinWriter emit the `(target)*self` cast + a `toX()`.
            CXCursorKind.CXCursor_ConversionFunction,
            CXCursorKind.CXCursor_FunctionDecl,
            CXCursorKind.CXCursor_CXXMethod -> {
                if (value.referenced.spelling.toKString() in listOf(
                        "operator new",
                        "operator new[]",
                        "operator delete",
                        "operator delete[]"
                    )
                ) {
                    return null
                }
                WrappedMethod(value, resolverBuilder).also {
                    for (i in 0 until value.numArguments) {
                        it.addChild(
                            WrappedArgument(
                                value.getArgument(i.toUInt()),
                                resolverBuilder,
                                i
                            )
                        )
                    }
                }
            }

            CXCursorKind.CXCursor_Namespace -> {
                val name = value.spelling.toKString() ?: error("Namespace without name")
                // Anonymous namespaces have an empty spelling. Their members have
                // TU-internal linkage and cannot be referenced across the C ABI, so
                // they aren't bindable. Skip them (and, transitively, everything they
                // contain) rather than emitting a binding with an empty package name.
                if (name.isEmpty()) return null
                WrappedNamespace(name)
            }

            CXCursorKind.CXCursor_Constructor ->
                WrappedConstructor(
                    value.spelling.toKString() ?: "constructor",
                    WrappedType.VOID,
                    value.isCopyConstructor,
                    value.isDefaultConstructor
                ).also {
                    for (i in 0 until value.numArguments) {
                        it.addChild(
                            WrappedArgument(
                                value.getArgument(i.toUInt()),
                                resolverBuilder,
                                i
                            )
                        )
                    }
                }

            CXCursorKind.CXCursor_Destructor ->
                WrappedDestructor(
                    value.spelling.toKString() ?: "destructor",
                    WrappedType.VOID
                ).also {
                    // Thread destructor virtuality (same plumbing as method
                    // isVirtual) so the non-virtual-destructor diagnostic can fire.
                    it.isVirtual = value.isVirtual
                    for (i in 0 until value.numArguments) {
                        it.addChild(
                            WrappedArgument(
                                value.getArgument(i.toUInt()),
                                resolverBuilder,
                                i
                            )
                        )
                    }
                }

            CXCursorKind.CXCursor_TemplateTypeParameter -> WrappedTemplateParam(
                value,
                resolverBuilder
            )

            CXCursorKind.CXCursor_ClassTemplate -> WrappedTemplate(value, resolverBuilder)

            CXCursorKind.CXCursor_TypeRef -> WrappedTemplateRef(
                value.spelling.toKString() ?: error("TypeRef without a name")
            )

            CXCursorKind.CXCursor_CXXBaseSpecifier -> WrappedBase(
                try {
                    WrappedType(value.type, resolverBuilder)
                } catch (t: IllegalArgumentException) {
                    // Don't mind when parsing everything, if this reference is needed,
                    // it'll come up in resolution
                    return null
                },
                isPublic = value.accessSpecifier == CX_CXXAccessSpecifier.CX_CXXPublic,
                isVirtualBase = value.isVirtualBase
            )

            CXCursorKind.CXCursor_TemplateRef ->
                try {
                    WrappedType(value.type, resolverBuilder)
                } catch (t: IllegalArgumentException) {
                    // Don't mind when parsing everything, if this reference is needed,
                    // it'll come up in resolution
                    return null
                }

            CXCursorKind.CXCursor_TranslationUnit -> WrappedTU()

            else -> return null
        }
    } catch (t: RuntimeException) {
        // Skip-not-crash: building an element reaches several error() calls
        // (IllegalStateException) — a missing spelling/usr/name — that the
        // narrower per-branch catches (IllegalArgumentException only) let
        // escape as a fatal crash. When parsing everything, a single
        // unmodelable cursor must drop-and-log: if this reference is genuinely
        // needed it resurfaces during resolution, where it fails gracefully.
        DropLedger.record(
            value.spelling.toKString()?.takeIf { it.isNotEmpty() }
                ?: value.kind.toString(),
            "Unmodelable ${value.kind} cursor (${t.message})",
            DropPhase.PARSE
        )
        println(
            "WARN skip-not-crash: dropping ${value.kind} cursor " +
                "'${value.spelling.toKString()}' (${t.message})"
        )
        return null
    }
    elementLookup[strTag] = element
    return element
}

// True when [this] class/struct cursor is lexically nested inside a class TEMPLATE: any
// of its enclosing semantic-parent contexts is a `CXCursor_ClassTemplate` (or a partial
// specialization of one). Walks up the semantic-parent chain until it reaches the
// translation unit (or stops making progress). The enclosing template makes the nested
// type's qualified name unspellable without template arguments, so it can't be bound.
private val CValue<CXCursor>.isNestedInClassTemplate: Boolean
    get() {
        var current = semanticParent
        while (true) {
            val kind = current.kind
            if (kind == CXCursorKind.CXCursor_ClassTemplate ||
                kind == CXCursorKind.CXCursor_ClassTemplatePartialSpecialization
            ) {
                return true
            }
            if (kind == CXCursorKind.CXCursor_TranslationUnit ||
                kind == CXCursorKind.CXCursor_InvalidFile ||
                kind == CXCursorKind.CXCursor_NoDeclFound
            ) {
                return false
            }
            val next = current.semanticParent
            if (next.equals(current)) return false
            current = next
        }
    }

// True when [this] `operator=` cursor is a COPY assignment of [parent]'s class — i.e. it
// takes a single (const) LVALUE-reference parameter to the same class. Distinguishes copy
// assignment (whose deletion blocks the generated `field = *value` setter) from a deleted
// MOVE assignment (`operator=(T&&)`), which leaves copy assignment available, so the field
// is still assignable.
private fun CValue<CXCursor>.isCopyAssignmentOf(
    parent: WrappedElement?,
    resolverBuilder: ResolverBuilder
): Boolean {
    if (numArguments != 1) return false
    val argType = WrappedType(getArgument(0u).type, resolverBuilder)
    if (!argType.isReference) return false
    // Exclude move assignment (`operator=(T&&)`): a deleted move leaves copy available.
    if (argType.toString().contains("&&")) return false
    val className = when (parent) {
        is WrappedClass -> parent.type.toString()
        is WrappedTemplate -> parent.name
        else -> return false
    }
    val unref = argType.unreferenced
    val bare = if (unref.isConst) unref.unconst else unref
    return bare.toString() == className
}

fun WrappedClass(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedClass =
    WrappedClass(
        wrapName(value, value.spelling.toKString() ?: error("Missing name")),
        value.isAbstract
    )

private fun wrapName(value: CValue<CXCursor>, name: String): String {
    val type = value.type.spelling.toKString() ?: return name
    val index = type.indexOf(name)
    if (index < 0) return name
    return type.substring(index)
}

fun WrappedField(field: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedField =
    WrappedField(
        field.referenced.spelling.toKString() ?: error("Can't find name for $field"),
        WrappedType(field.type, resolverBuilder)
    )

private val clsParents = listOf(
    CXCursorKind.CXCursor_ClassTemplate,
    CXCursorKind.CXCursor_ClassDecl,
    CXCursorKind.CXCursor_StructDecl
)

fun WrappedMethod(method: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedMethod =
    WrappedMethod(
        method.referenced.spelling.toKString() ?: error("Can't find name of $method"),
        rangeVectorReturn(method) ?: WrappedType(method.type.result, resolverBuilder).let {
            // A `const` method says nothing about its RETURN type's constness. Const on
            // a by-value (non-pointer/reference) return is meaningless for the returned
            // temporary (G8) and actively breaks the by-value Holder path — placement-new
            // into a `const T*` is ill-formed. Only carry method-derived const for a
            // pointer/reference return, where const-of-pointee can matter; strip it on a
            // by-value return.
            if (method.isConst && (it.isPointer || it.isReference)) const(it) else it
        },
        if (method.isStatic ||
            (method.semanticParent.kind !in clsParents && method.lexicalParent.kind !in clsParents)
        ) {
            MethodType.STATIC
        } else {
            MethodType.METHOD
        }
    ).also {
        it.isVirtual = method.isVirtual
        it.isConst = method.isConst
        // T1.3: an `iterator_range<It>` return is materialized into the
        // `std::vector<Elem*>` produced above; record Elem so CppWriter emits the loop.
        it.rangeElementType = extractRangeReturn(method.type.result)?.elementSpelling
    }

// T1.3: if [method] returns `llvm::iterator_range<It>`, build the materialized
// `std::vector<Elem*>` return type (a real template type, so it resolves through
// the existing vector-instantiation/CppVector path — yielding a `for`-iterable on
// the Kotlin side). Returns null for any non-range return, so the caller falls
// back to the normal return-type construction.
private fun rangeVectorReturn(method: CValue<CXCursor>): WrappedType? {
    val range = extractRangeReturn(method.type.result) ?: return null
    return WrappedTemplateType(
        WrappedType("std::vector"),
        listOf(pointerTo(WrappedType(range.elementSpelling)))
    )
}

fun WrappedArgument(
    arg: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder,
    index: Int = 0
): WrappedArgument = WrappedArgument(
    arg.spelling.toKString()?.takeIf { it.isNotBlank() } ?: "_arg_$index",
    WrappedType(arg.type, resolverBuilder),
    arg.usr.toKString() ?: "",
    hasDefault(arg),
    defaultValue(arg)
)

private val nonDefaultChildKinds = listOf(
    CXCursorKind.CXCursor_TypeRef,
    CXCursorKind.CXCursor_TemplateRef,
    CXCursorKind.CXCursor_NamespaceRef
)

private fun defaultExpr(value: CValue<CXCursor>): CValue<CXCursor>? {
    val last = value.children.lastOrNull() ?: return null
    return last.takeIf { it.kind !in nonDefaultChildKinds }
}

private fun hasDefault(value: CValue<CXCursor>): Boolean = defaultExpr(value) != null

// Recovers the C++ source text of the parameter's default-value expression by
// tokenizing the default sub-expression cursor's extent (libclang exposes no
// direct accessor). Returns null when there is no default.
private fun defaultValue(value: CValue<CXCursor>): String? {
    val expr = defaultExpr(value) ?: return null
    return expr.tokenSpellings().joinToString("").takeIf { it.isNotBlank() }
}

fun WrappedTemplate(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedTemplate =
    WrappedTemplate(value.spelling.toKString() ?: error("Missing name"))

fun WrappedTemplateParam(
    value: CValue<CXCursor>,
    resolverBuilder: ResolverBuilder
): WrappedTemplateParam = WrappedTemplateParam(
    value.spelling.toKString() ?: error("Template param without name $value"),
    value.usr.toKString() ?: error("Template param without USR $value")
)

fun WrappedTypedef(value: CValue<CXCursor>, resolverBuilder: ResolverBuilder): WrappedTypedef =
    WrappedTypedef(
        value.spelling.toKString() ?: error("Template param without name $value"),
        WrappedType(value.type, resolverBuilder)
    )

/**
 * Detect a method return type of `llvm::iterator_range<It>` and recover the element class
 * `Elem` the range yields, so the method can be materialized into a bound
 * `std::vector<Elem*>` (T1.3). Returns null for any non-range return (the method keeps its
 * original return handling).
 *
 * Works off the CANONICAL return type, so the `using X_range = iterator_range<...>` member
 * aliases Clang sprinkles everywhere (`decl_range`, `method_range`, `base_class_range`, …)
 * are all seen through to their `llvm::iterator_range<It>` form before matching.
 *
 * Element extraction mirrors what `*it` dereferences to, normalized to the underlying
 * class (the vector holds `Elem*`):
 *  - `It` is a pointer (`Decl**`, `CXXBaseSpecifier*`): the iterated element is the
 *    pointee; if the pointee is itself a pointer (`Decl**` → `Decl*`) strip that level too,
 *    so a pointer-of-pointer iterator still yields the leaf class `Decl`.
 *  - `It` is an iterator class exposing a `value_type` typedef (`decl_iterator`'s
 *    `value_type = Decl*`): strip any trailing `*` to get the class.
 *  - `It` is a template specialization with no usable `value_type`
 *    (`specific_decl_iterator<CXXMethodDecl>`, whose `operator*`/`value_type` are dependent
 *    and unreadable): take the iterator's own first template argument as the element.
 *
 * All three cases were validated against the real Clang AST for `decls()`/`methods()`/
 * `fields()`/`bases()`; the resulting C++ `kpp_to_elem_ptr<Elem>(*it)` push compiles
 * against libclang-cpp for every shape.
 */
private fun extractRangeReturn(resultType: CValue<CXType>): RangeReturn? {
    val canonical = resultType.canonicalType
    val canonicalSpelling = canonical.spelling.toKString() ?: return null
    if (!canonicalSpelling.contains("iterator_range<")) return null
    if (canonical.numTemplateArguments < 1) return null
    val iterator = canonical.getTemplateArgumentType(0u)
    val element = elementOfIterator(iterator) ?: return null
    return RangeReturn(element)
}

private fun elementOfIterator(iterator: CValue<CXType>): String? {
    val canon = iterator.canonicalType
    if (canon.useContents { kind } == CXType_Pointer) {
        val pointee = canon.pointeeType.canonicalType
        // `Decl**` → element is `Decl` (peel the second pointer); `CXXBaseSpecifier*` →
        // element is `CXXBaseSpecifier` (peel the lone pointer).
        val element =
            if (pointee.useContents { kind } == CXType_Pointer) {
                pointee.pointeeType.canonicalType
            } else {
                pointee
            }
        return stripDecoration(element.spelling.toKString())
    }
    // A class iterator. Prefer its `value_type` typedef (e.g. decl_iterator's `Decl*`).
    val decl = canon.typeDeclaration
    val valueType = decl.children.firstOrNull {
        (it.kind == CXCursor_TypedefDecl || it.kind == CXCursor_TypeAliasDecl) &&
            it.spelling.toKString() == "value_type"
    }
    if (valueType != null) {
        val underlying = valueType.typedefDeclUnderlyingType.canonicalType
        return stripDecoration(underlying.spelling.toKString())
    }
    // No readable value_type (e.g. specific_decl_iterator<X>, whose members are dependent):
    // the iterator's own first template argument is the element class.
    if (canon.numTemplateArguments >= 1) {
        return stripDecoration(canon.getTemplateArgumentType(0u).spelling.toKString())
    }
    // Recognized an `iterator_range<It>` but couldn't recover the element from `It` (no
    // pointer/`value_type`/template-arg shape matched). The range method is dropped — keep
    // that DECISION, but make it discoverable: a silent null here was an unanswerable "did
    // the generator drop it?". PARSE phase: this is a cursor/CXType-level pass with no
    // ResolveContext yet. (DropLedger.record is non-suspend, so it's callable here.)
    DropLedger.record(
        symbol = iterator.spelling.toKString() ?: "<unknown iterator_range element>",
        reason = "iterator_range element type not extractable from iterator " +
            "(no pointer/value_type/template-argument shape matched)",
        phase = DropPhase.PARSE
    )
    return null
}

// Reduce a type spelling to its bare class name: drop a leading `const`, then any
// trailing `*`/`&`/`const`/whitespace. `clang::Decl *const *` → `clang::Decl`,
// `const clang::CXXBaseSpecifier` → `clang::CXXBaseSpecifier`.
private fun stripDecoration(spelling: String?): String? {
    var s = (spelling ?: return null).trim()
    if (s.isEmpty()) return null
    while (true) {
        val before = s
        if (s.startsWith("const ")) s = s.substring("const ".length).trim()
        if (s.endsWith("*") || s.endsWith("&")) s = s.dropLast(1).trim()
        if (s.endsWith("const")) s = s.dropLast("const".length).trim()
        if (s == before) break
    }
    return s.ifEmpty { null }
}
