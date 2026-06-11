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

import clang.CXCursor
import clang.CXCursorKind
import clang.CXCursorKind.CXCursor_TypedefDecl
import clang.CXDiagnosticSeverity
import clang.CXIndex
import clang.CXTranslationUnit
import clang.CXType
import clang.clang_defaultDiagnosticDisplayOptions
import clang.clang_disposeDiagnostic
import clang.clang_disposeString
import clang.clang_formatDiagnostic
import clang.clang_getDiagnostic
import clang.clang_getDiagnosticLocation
import clang.clang_getDiagnosticSeverity
import clang.clang_getNumDiagnostics
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
import com.monkopedia.krapper.generator.canonicalType
import com.monkopedia.krapper.generator.codegen.File
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
import com.monkopedia.krapper.generator.model.serialized
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
import kotlin.system.exitProcess
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.DeferScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.EOF
import platform.posix.close
import platform.posix.getenv
import platform.posix.read
import platform.posix.system
import platform.posix.write

typealias ElementFilter = WrappedElement.() -> Boolean

// Golden-test dump path (#44 brick 7), set by KrapperGen --dumpParsedModel before the run.
// When non-null, [parseHeader] writes the parsed (pre-resolution, pre-rewrite) WrappedTU
// as canonical SerializedElement JSON to this path and exits the process — parse-only
// mode, used by :cppfrontend:goldenCompare. A CLI-scoped global rather than a KrapperConfig
// field so the ksrpc service schema (:slice) is untouched by a debug-only flag.
var dumpParsedModelPath: String? = null

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

/**
 *
#include <...> search starts here:
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0/x86_64-pc-linux-gnu
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/../../../../include/c++/11.1.0/backward
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/include
/usr/local/include
/usr/lib/gcc/x86_64-pc-linux-gnu/11.1.0/include-fixed
/usr/include
End of search list.
 */

fun generateIncludes(compiler: String) = memScoped {
    // Per-run unique scratch dir (pid + counter) so concurrent generator runs never
    // collide on a fixed /tmp path; removed before returning so it never accumulates.
    val tmpDir = File.createTempDir("krapper_includes")
    defer { tmpDir.rmR() }
    val emptyFile = File(tmpDir, "clang_includes.c")
    emptyFile.writeText("")
    val process = Process {
        system("$compiler -E -x c++ -v ${emptyFile.path}")
    }
    process.start()
    defer {
        process.kill()
    }
    val buffer = alloc<ByteVar> {
        EOF
    }
    write(process.stdIn(), buffer.ptr, 1.toULong())
    close(process.stdIn())
    process.wait()
    val readBuffer = allocArray<ByteVar>(256)
    var fullString = StringBuilder()
    var amount = read(process.stdOut(), readBuffer, 255.toULong())
    while (amount > 0) {
        readBuffer[amount.toInt()] = 0.toByte()
        fullString.append(readBuffer.toKStringFromUtf8())
        amount = read(process.stdOut(), readBuffer, 255.toULong())
    }
    val lines = fullString.split("\n")
    val start = lines.indexOf("#include <...> search starts here:")
    val end = lines.indexOf("End of search list.")
    if (start < 0 || end < 0) {
        throw IllegalStateException("Can't find includes for:\n$fullString")
    }
    return@memScoped (
        lines.subList(start + 1, end).toList().map { it.trim() } + "."
        ).toTypedArray()
}

fun find(s: String): String? {
    val paths = getenv("PATH")?.toKStringFromUtf8().orEmpty().split(":")
    for (path in paths) {
        val parent = File(path)
        val file = File(parent, s)
        if (file.exists()) {
            return file.path
        }
    }
    return error("Can't find $s in $paths")
}

// Obtained from 'g++ -E -x c++ - -v < /dev/null'

class ParsedResolver(val tu: WrappedTU) : Resolver {
    private val classMap = mutableMapOf<String, Pair<ResolvedClass, WrappedClass>?>()
    private val templateMap = mutableMapOf<String, WrappedTemplate>()

    override fun resolveTemplate(type: WrappedType, context: ResolveContext): WrappedTemplate =
        templateMap.getOrPut(type.toString()) {
            val templateCandidates = tu.filterRecursive {
                ((it as? WrappedTemplate)?.qualified == type.toString())
            }
            templateCandidates.singleOrNull() as? WrappedTemplate
                ?: error("Can't resolve template $type (${type::class.simpleName})")
        }

    override suspend fun resolve(
        type: WrappedType,
        context: ResolveContext
    ): Pair<ResolvedClass, WrappedClass>? {
        return classMap.getOrPut(type.toString()) {
            val existingClass = tu.filterRecursive {
                (it as? WrappedClass)?.type?.toString() == type.toString() &&
                    it.isNotEmpty()
            }.singleOrNull() as? WrappedClass
            existingClass?.let { cls ->
                return@getOrPut cls.resolve(context)?.let { it to cls }
            }
            when (type) {
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

private class ResolverBuilderImpl : ResolverBuilder {
    private val seenNames = mutableMapOf<String, CValue<CXType>>()
    val classes = mutableListOf<WrappedClass>()
    val desiredTemplates = mutableListOf<CValue<CXType>>()

    override fun visit(type: CValue<CXType>): CValue<CXType> {
        // Use canonical type spelling as cache key to avoid collisions between
        // inner classes with the same short name (e.g. Serializer::Delegate vs
        // Deserializer::Delegate both spelling as just "Delegate").
        // However, template type parameters have positional canonical forms like
        // "type-parameter-0-0" that collide across different templates (e.g.,
        // _Tp and _Alloc from different templates both become type-parameter-0-0).
        // For those, use the original spelling which preserves the parameter name.
        val strType = run {
            val canonical = type.canonicalType.spelling.toKString()?.trim()
            if (canonical != null && !canonical.contains("type-parameter")) {
                canonical
            } else {
                type.spelling.toKString()?.trim()
            }
        } ?: return type
        return seenNames.getOrPut(strType) {
            val declaration = type.typeDeclaration
            when (declaration.kind) {
                CXCursorKind.CXCursor_ClassDecl,
                CXCursorKind.CXCursor_StructDecl -> {
                    seenNames[strType] = type
                    if (type.numTemplateArguments <= 0) {
                        classes.add(WrappedClass(declaration, this))
                    } else {
                        desiredTemplates.add(type)
                    }
                    type
                }

                CXCursor_TypedefDecl -> {
                    visit(type.typeDeclaration.typedefDeclUnderlyingType)
                }

                else -> {
                    type
                }
            }
        }
    }
}

suspend fun DeferScope.parseHeader(
    index: CXIndex,
    file: List<String>,
    includePaths: Array<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + includePaths.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false,
    // When true, any `error:` diagnostic aborts the whole run (historical behavior).
    // When false (default), an error attributable to a single declaration drops that
    // symbol into the drop ledger and binding continues; only fatal / unattributable
    // diagnostics abort. See [handleDiagnostics].
    strictDiagnostics: Boolean = false
): Resolver {
    val builder = ResolverBuilderImpl()
    val tu = file.map {
        parseHeader(index, it, builder, includePaths, args, debug, strictDiagnostics)
    }
        .reduceRight { tu1, tu2 ->
            tu1.also {
                it.addAllChildren(
                    tu2.children.map {
                        it.also { it.parent = tu1 }
                    }
                )
            }
        }
    Log.i("Reduced ${tu.children.size}")
    // Golden-test dump (#44 brick 7, armed by krapper_gen --dumpParsedModel): project the
    // PARSED tree through the canonical SerializedElement DTO and exit (parse-only mode).
    // The hook sits HERE — after the multi-file reduce completes the tree, but BEFORE the
    // pre-resolution rewrites below (rewriteViewReturns/rewriteUniquePtrReturns are
    // krapper-specific marshalling baked onto the tree, not parse output — the C++-AST
    // front-end does not perform them, so the golden compare must see the tree without
    // them).
    dumpParsedModelPath?.let { path ->
        File(path).writeText(Json.encodeToString(tu.serialized()))
        Log.i("Dumped parsed model to $path (parse-only mode, exiting)")
        exitProcess(0)
    }
    // T1.10: bake view-return rewrites (e.g. llvm::StringRef -> std::string via `.str()`)
    // into the parsed tree BEFORE resolution. ParsedResolver.resolve re-reads classes from
    // this TU (not the findClasses() result), so the rewrite must live on the tree itself.
    rewriteViewReturns(tu)
    // T1.7e: a by-value `std::unique_ptr<T>` return -> raw `T*` (ownership transferred via
    // `.release()`). Same pre-resolution baking rationale as rewriteViewReturns.
    rewriteUniquePtrReturns(tu)
    return ParsedResolver(tu)
}

private fun DeferScope.parseHeader(
    index: CXIndex,
    file: String,
    resolverBuilder: ResolverBuilder,
    includePaths: Array<String>,
    args: Array<String> = arrayOf("-xc++", "--std=c++14") + includePaths.map { "-I$it" }
        .toTypedArray(),
    debug: Boolean = false,
    strictDiagnostics: Boolean = false
): WrappedTU {
    val tu = index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
    val droppedUsrs = tu.handleDiagnostics(file, strictDiagnostics)
    defer {
        tu.dispose()
    }
    val cursor = tu.cursor
    if (debug) {
        File(
            File("/tmp"),
            "cursor_${File(file).name}.json"
        ).writeText(Json.encodeToString(Utils.CursorTreeInfo(cursor)))
    }
    // Excise declarations the diagnostic policy dropped (issue #9) so the broken decl and
    // its members are never carried into the model / resolution.
    WrappedElement.setDroppedUsrs(droppedUsrs)
    val element = WrappedElement.mapAll(tu.cursor, resolverBuilder)
    return element as? WrappedTU ?: error("$element is not a WrappedTU, ${tu.cursor.kind}")
}

/**
 * A single error-severity parse diagnostic, paired with the declaration it could be
 * attributed to. [text] is the formatted diagnostic line; [symbol]/[usr] are the spelling
 * and USR of the enclosing top-level declaration the error landed inside, or `null` when
 * the error can't be tied to a single declaration (a translation-unit-level / fatal
 * error — a missing include, "too many errors", an error at the TU root). [fatal] is true
 * for `CXDiagnostic_Fatal`, which always aborts because the rest of the AST can't be
 * trusted.
 */
private data class ErrorDiagnostic(
    val text: String,
    val symbol: String?,
    val usr: String?,
    val fatal: Boolean
) {
    /** Attributable to a single declaration -> droppable when not strict and not fatal. */
    val isAttributable: Boolean get() = symbol != null && usr != null && !fatal
}

/**
 * Inspect this translation unit's diagnostics and decide whether to abort the run.
 *
 * Historically (and still under [strictDiagnostics]) ANY `error:` diagnostic threw,
 * taking the whole import down — fine for curated clean headers, fatal for pointing at a
 * real library where one bad declaration would block everything else. The default policy
 * now SKIPS the affected symbol instead: an error attributable to a single top-level
 * declaration drops that declaration into the [DropLedger] (PARSE phase, the diagnostic
 * text as the reason) and binding continues over the rest of the header. libclang still
 * recovers a usable AST for the surviving declarations, and the downstream model build
 * (`mapAll`) is already skip-not-crash, so dropping the named symbol here is safe.
 *
 * The boundary, by design:
 *  - strict mode             -> abort on the first error/fatal (the old behavior).
 *  - fatal severity          -> always abort (recovered AST is untrustworthy).
 *  - unattributable error    -> always abort (an error at the TU root / with no enclosing
 *                               declaration is translation-unit-level, e.g. a missing
 *                               include; we can't surgically drop a single symbol).
 *  - attributable error      -> drop that symbol into the ledger, continue.
 */
private fun CXTranslationUnit.handleDiagnostics(
    file: String,
    strictDiagnostics: Boolean
): Set<String> {
    val errors = collectErrorDiagnostics()
    if (errors.isEmpty()) {
        return emptySet()
    }
    val mustAbort = strictDiagnostics || errors.any { !it.isAttributable }
    if (mustAbort) {
        val reason = if (strictDiagnostics) {
            "strict diagnostics"
        } else {
            "fatal / translation-unit-level diagnostic(s)"
        }
        throw RuntimeException(
            "Parse failure ($reason) in $file:\n" + errors.joinToString("\n") { it.text }
        )
    }
    // Lenient: every error is attributable -> drop each affected symbol and continue.
    // Dedup by USR: one bad declaration can raise several diagnostics, but it's one drop.
    val droppedUsrs = mutableSetOf<String>()
    for (error in errors) {
        val symbol = error.symbol ?: continue
        val usr = error.usr ?: continue
        if (!droppedUsrs.add(usr)) {
            continue
        }
        DropLedger.record(symbol, error.text, DropPhase.PARSE)
        println(
            "WARN skip-not-crash: dropping declaration '$symbol' in $file on parse " +
                "diagnostic: ${error.text}"
        )
    }
    return droppedUsrs
}

/**
 * Collect this TU's `error:`/fatal diagnostics, attributing each to the enclosing
 * top-level declaration where one exists (via the diagnostic's source location ->
 * [clang_getCursor] -> walk semantic parents up to the TU root).
 */
private fun CXTranslationUnit.collectErrorDiagnostics(): List<ErrorDiagnostic> {
    val nbDiag = clang_getNumDiagnostics(this)
    val errors = mutableListOf<ErrorDiagnostic>()
    for (currentDiag in 0 until nbDiag.toInt()) {
        val diagnostic = clang_getDiagnostic(this, currentDiag.toUInt())
        try {
            val severity = clang_getDiagnosticSeverity(diagnostic)
            val isError = severity == CXDiagnosticSeverity.CXDiagnostic_Error ||
                severity == CXDiagnosticSeverity.CXDiagnostic_Fatal
            if (!isError) {
                continue
            }
            val formatted =
                clang_formatDiagnostic(diagnostic, clang_defaultDiagnosticDisplayOptions())
            val text = formatted.toKString().orEmpty()
            clang_disposeString(formatted)
            val location = clang_getDiagnosticLocation(diagnostic)
            val topLevel = topLevelDecl(getCursor(location))
            errors.add(
                ErrorDiagnostic(
                    text = text,
                    symbol = topLevel?.spelling?.toKString()?.takeIf { it.isNotBlank() },
                    usr = topLevel?.usr?.toKString()?.takeIf { it.isNotBlank() },
                    fatal = severity == CXDiagnosticSeverity.CXDiagnostic_Fatal
                )
            )
        } finally {
            clang_disposeDiagnostic(diagnostic)
        }
    }
    return errors
}

/**
 * Walk [cursor] up its semantic parents to the outermost named, non-namespace declaration
 * — the one whose semantic parent is the TU or a namespace — and return that cursor, or
 * `null` when the error can't be tied to a single declaration (a null/invalid cursor, the
 * TU root itself, or no named non-namespace ancestor — i.e. a translation-unit-level
 * error). That outermost decl is what the model binds as a top-level symbol, so excising
 * it (by USR, in WrappedElement.map) cleanly drops the whole bad declaration and all of
 * its members. Namespaces are skipped as drop targets: a namespace is a container, not a
 * bindable symbol, and dropping it would take every sibling declaration with it.
 */
private fun CXTranslationUnit.topLevelDecl(cursor: CValue<CXCursor>): CValue<CXCursor>? {
    val tuCursor = this.cursor
    if (cursor.kind.isInvalid || cursor.equals(tuCursor)) {
        return null
    }
    var current = cursor
    var named: CValue<CXCursor>? = null
    while (!current.kind.isInvalid && !current.equals(tuCursor)) {
        if (current.kind.isDeclaration &&
            current.kind != CXCursorKind.CXCursor_Namespace &&
            !current.spelling.toKString().isNullOrBlank()
        ) {
            named = current
        }
        current = current.semanticParent
    }
    return named
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
