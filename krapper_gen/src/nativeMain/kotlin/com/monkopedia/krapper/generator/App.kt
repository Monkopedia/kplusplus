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

import clang.CXChildVisitResult
import clang.CXCursor
import clang.CXCursorKind
import clang.clang_visitChildren
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.ErrorPolicy.FAIL
import com.monkopedia.krapper.ErrorPolicy.LOG
import com.monkopedia.krapper.Fixup
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.KrapperService
import com.monkopedia.krapper.generator.builders.CodeGenerationPolicy
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.getcwd
import com.monkopedia.krapper.generator.resolvedmodel.resolvedSerializerModule
import com.monkopedia.ksrpc.ErrorListener
import com.monkopedia.ksrpc.channels.registerDefault
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.sockets.asConnection
import com.monkopedia.ksrpc.sockets.posixFileReadChannel
import com.monkopedia.ksrpc.sockets.posixFileWriteChannel
import kotlin.system.exitProcess
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO

val ErrorPolicy.policy: CodeGenerationPolicy
    get() = when (this) {
        FAIL -> ThrowPolicy
        LOG -> LogPolicy
    }

typealias ErrorPolicy = com.monkopedia.krapper.ErrorPolicy
typealias ReferencePolicy = com.monkopedia.krapper.ReferencePolicy

class KrapperGen : CliktCommand() {
    val header by option(
        "-h",
        "--header",
        help = "Specify a header of classes to import"
    ).multiple()
    val library by option(
        "-l",
        "--lib",
        help = "Specify a library to that contains the header specified"
    ).multiple()
    val pkg by option("-p", "--package", help = "Desired package for wrappers to be placed")
    val rootPackage by option(
        "--root-package",
        help = "Root package for generated bindings. When unset, top-level types go to " +
            "package `root` and C++ namespaces map to their bare path (e.g. `std`). When set " +
            "(e.g. com.acme.app), every binding's package becomes <root-package> + namespace path."
    )
    val compiler by option(
        "-c",
        "--compiler",
        help = "Compiler to use for creating wrapper module"
    ).default("clang++")
    val cppStandard by option(
        "--std",
        help = "C++ standard libclang parses headers under, e.g. c++14, c++17, " +
            "c++20. Needed for C++17+ types like std::string_view. Defaults to c++14."
    ).default("c++14")
    val moduleName by argument(help = "Name of the wrapper module created").optional()
    val output by option("-o", "--outdir", help = "Directory to place generated files")
    val errorPolicy by option("--policy", help = "How to handle errors")
        .enum<ErrorPolicy>()
        .default(ErrorPolicy.LOG)
    val debug by option(
        "-d",
        "--debugOutput",
        help = "Specify file to output debug dump of state"
    ).flag()

    val referencePolicy by option(
        "-r",
        "--referencePolicy",
        help = "Sets policy of how to handle non-included classes"
    )
        .enum<ReferencePolicy>()
        .default(ReferencePolicy.IGNORE_MISSING)
    val serviceMode by option(
        "-s",
        help = "Tells Krapper to host a ksrpc service on std in/out, and ignores all other options"
    ).flag()
    val instantiate by option(
        "--instantiate",
        help = "Generate bindings for a C++ template instantiation, e.g. std::vector<int>. " +
            "May be repeated. May be combined with --header/--lib (v2 fixup flow): headers " +
            "are parsed first, then each instantiation is synthesized on top of the resolved " +
            "model. When --header is omitted, only the requested instantiations are emitted."
    ).multiple()
    val only by option(
        "--only",
        help = "Scoped-import allowlist: a fully-qualified (namespaced) class name to " +
            "bind, e.g. clang::CXXRecordDecl. Repeatable. A single value may also be a " +
            "comma-separated list (split on TOP-LEVEL commas only, so a templated entry " +
            "like `std::map<int, int>` is kept whole). Only listed classes are fully " +
            "bound; types they reference but that aren't listed fall to --referencePolicy " +
            "(borrowed/opaque) rather than being recursively bound. May be combined with " +
            "--only-file. When neither is set, DefaultFilter binds every non-std class."
    ).multiple()
    val onlyFile by option(
        "--only-file",
        help = "Like --only, but reads the allowlist from a file (one fully-qualified " +
            "class name per line; blank lines and lines starting with # are ignored). " +
            "Merged with any --only entries."
    )
    val failOnDrop by option(
        "--fail-on-drop",
        help = "Fail the run (non-zero exit) if the generator dropped any unmodelable " +
            "symbol (skip-not-crash). Default: lenient — drops are logged + reported in " +
            "the drop ledger, but the run still succeeds."
    ).flag()
    val strictDiagnostics by option(
        "--strict-diagnostics",
        help = "Abort the whole run on ANY parse `error:` diagnostic (the historical " +
            "behavior, for curated clean headers). Default: lenient — an error attributable " +
            "to a single declaration drops THAT symbol into the drop ledger and binding " +
            "continues, so one bad symbol in a real library doesn't take the import down. " +
            "Fatal / translation-unit-level errors (missing include, unattributable) abort " +
            "regardless."
    ).flag()
    val dumpParsedModel by option(
        "--dumpParsedModel",
        help = "Debug/golden-test flag (#44 brick 7): parse the headers, project the PARSED " +
            "(pre-resolution, pre-rewrite) WrappedTU through the canonical " +
            "SerializedElement DTO (:krapper_model ModelSerializer) and write it as JSON " +
            "to the given path, then EXIT without resolving or generating (parse-only " +
            "mode). :cppfrontend:goldenCompare diffs this libclang front-end's parse " +
            "output against the C++-AST front-end's."
    )
    val roundTripModel by option(
        "--roundTripModel",
        help = "Debug/oracle flag (#45 brick 1): after each parse, serialize the full " +
            "WrappedTU through the ModelIo round-trip JSON, deserialize it back, and run " +
            "resolution+generation on the DESERIALIZED model. Output should be " +
            "byte-identical to a run without the flag — the verification gate for the " +
            "--frontend=cpp handoff format. Also enabled by KRAPPER_ROUNDTRIP_MODEL=1 " +
            "in the environment (which reaches service-mode runs too)."
    ).flag()
    val fixupFile by option(
        "--fixup-file",
        help = "Path to a JSON file containing a list of Fixup directives (see Fixup.kt). " +
            "Each directive registers a narrow per-binding-spec correction (remove a method " +
            "by uniqueCName, strip a stale `const ` prefix from matching return types, etc.). " +
            "Applied during writeTo()."
    )

    override fun run() {
        if (serviceMode) {
            return runService()
        }
        // Golden-test dump (#44 brick 7): arm the parse-only hook in Parsing.kt. The
        // process exits inside parseHeader right after the JSON is written, so the
        // resolution/generation flow below never runs with the flag set.
        dumpParsedModelPath = dumpParsedModel
        if (roundTripModel) roundTripParsedModel = true
        runBlocking {
            val service = KrapperServiceImpl()
            val resolvedModule = moduleName
                ?: header.firstOrNull()?.let { File(it).name }
                ?: error("A module name argument is required")
            service.setConfig(
                KrapperConfig(
                    pkg = pkg ?: "krapper.$resolvedModule",
                    compiler = compiler,
                    moduleName = resolvedModule,
                    errorPolicy = errorPolicy,
                    referencePolicy = referencePolicy,
                    debug = debug,
                    cppStandard = cppStandard,
                    rootPackage = rootPackage,
                    failOnDrop = failOnDrop,
                    strictDiagnostics = strictDiagnostics
                )
            )
            val indexService = service.index(IndexRequest(header, library))
            try {
                // v2 flow: when both --header and --instantiate are present, parse
                // the headers first (filterAndResolve) then layer the requested
                // template instantiations on top. This is what the v8 example
                // needs — a "wrap this whole header set" import combined with the
                // narrow fixups passed via --fixup-file.
                val hasHeaders = header.isNotEmpty()
                val hasInstantiations = instantiate.isNotEmpty()
                if (hasHeaders) {
                    // Scoped-import allowlist (T1.0a): when --only/--only-file name an
                    // explicit class set, bind ONLY those (referenced-but-unlisted types
                    // fall to --referencePolicy). Otherwise keep the DefaultFilter
                    // (bind-everything-non-std) behavior.
                    val allowList = loadAllowList()
                    val filter = if (allowList.isEmpty()) {
                        DefaultFilter
                    } else {
                        Log.i("Scoped import: binding only ${allowList.size} class(es): $allowList")
                        AllowListFilter(allowList)
                    }
                    indexService.filterAndResolve(filter)
                }
                if (hasInstantiations) {
                    for (spec in instantiate) {
                        indexService.requestInstantiation(parseInstantiation(spec))
                    }
                }
                // Apply declarative fixups from --fixup-file (v2 escape hatch).
                // Empty / missing file is a no-op.
                val fixups = loadFixups(fixupFile)
                if (fixups.isNotEmpty()) {
                    Log.i("Loaded ${fixups.size} fixup(s) from $fixupFile")
                    FixupApplier.apply(indexService, fixups)
                }
                if (hasInstantiations && !hasHeaders) {
                    // Pure-instantiation path: matches the original M11 sync flow.
                    // Skip the legacy hardcoded v8 mappings entirely (they have
                    // been migrated to the user-supplied --fixup-file).
                    indexService.writeTo(output ?: getcwd())
                    return@runBlocking
                }
                // Legacy / headers-only / headers+instantiations path: previous
                // releases also wired a hardcoded set of v8-specific mappings
                // here. Those have been migrated to the declarative Fixup
                // directives (see kplusplus { fixup { ... } } in the compiler
                // gradle subplugin). The block remains so the same code path
                // serves both the legacy CLI invocation and the new sync flow.
//            for (file in header) {
//                val tu =
//                    index.parseTranslationUnit(file, args, null) ?: error("Failed to parse $file")
//                tu.printDiagnostics()
//                defer {
//                    tu.dispose()
//                }
//                        val info = Utils.CursorTreeInfo(tu.cursor)
//                        Log.i("Writing ${tu.cursor.usr.toKString()}")
//                        File("/tmp/full_tree.json").writeText(Json.encodeToString(info))
//                tu.cursor.filterChildrenRecursive {
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("OtherClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_otherclass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassDecl && it.fullyQualified.contains("TestClass")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/testlib_testClass.json").writeText(Json.encodeToString(info))
//                    }
//                    if (it.kind == CXCursorKind.CXCursor_ClassTemplate && it.fullyQualified.contains("basic_string") && !it.fullyQualified.contains("basic_stringbuf")) {
//                        val info = Utils.CursorTreeInfo(it)
//                        Log.i("Writing ${it.usr.toKString()}")
//                        File("/tmp/basic_string.json").writeText(Json.encodeToString(info))
//                    }
//                    false
//                }
// //                tu.cursor.filterChildren { true }.forEach {
// //                    val cursor = KXCursor.generate(tu.cursor)
// //                    Log.i("Cursor $cursor ${cursor?.children?.size} ${tu.cursor.kind}")
// //                }
//            }
                indexService.writeTo(output ?: getcwd())
            } finally {
                // Always release the index and delete the run's throwaway temp dir, even
                // when resolution/writeTo throws — so forcing-header intermediates never
                // leak into $TMPDIR.
                indexService.close()
            }
        }
    }

    /**
     * Build the scoped-import allowlist from --only (comma lists, repeatable) and
     * --only-file (one name per line). Returns an empty list when neither is set,
     * which the caller maps to DefaultFilter (bind-everything-non-std).
     */
    private fun loadAllowList(): List<String> {
        val fromOption = only.flatMap { it.splitTopLevelCommas() }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val fromFile = onlyFile?.let { path ->
            val file = File(path)
            if (!file.exists()) {
                println("kplusplus warn: --only-file=$path does not exist; ignoring.")
                emptyList()
            } else {
                file.readText().lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .toList()
            }
        } ?: emptyList()
        return (fromOption + fromFile).distinct()
    }

    /** Read the --fixup-file JSON (if provided) into the declarative list. */
    private fun loadFixups(path: String?): List<Fixup> {
        if (path == null) return emptyList()
        val file = File(path)
        if (!file.exists()) {
            // Not using Log.w because this helper is invoked synchronously
            // from run() and Log.w is suspend. The miss is recoverable.
            println("kplusplus warn: --fixup-file=$path does not exist; treating as no fixups.")
            return emptyList()
        }
        val text = file.readText().trim()
        if (text.isEmpty()) return emptyList()
        return Json { ignoreUnknownKeys = true }.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Fixup.serializer()),
            text
        )
    }

    private fun runService() {
        val input = posixFileReadChannel(STDIN_FILENO)
        val output = posixFileWriteChannel(STDOUT_FILENO)
        withoutIcanon {
            runBlocking {
                val env = ksrpcEnvironment(
                    Json {
                        serializersModule = resolvedSerializerModule
                    }
                ) {
                    errorListener = ErrorListener { t ->
                        println("Exception: " + t.message + "\n" + t.stackTraceToString())
                    }
                }
                val connection = (input to output).asConnection(env)
                connection.registerDefault(KrapperServiceImpl())
                val deferred = CompletableDeferred<Unit>()
                connection.onClose {
                    exitProcess(0)
                }
                deferred.await()
            }
        }
    }
}

/**
 * Split a `--only` value on TOP-LEVEL commas only, leaving commas inside template
 * brackets intact. Supports the legacy convenience of a comma-joined list in one arg
 * (`A,B,C`) while keeping a templated entry (`std::map<int, int>`) whole — the gradle
 * plugin now passes each entry as its own `--only`, but a hand-written comma list (or a
 * future re-join) must still not tear a templated name (`<int, int>` -> `<int` + ` int>`).
 */
internal fun String.splitTopLevelCommas(): List<String> {
    val parts = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    for (ch in this) {
        when (ch) {
            '<' -> {
                depth++
                current.append(ch)
            }

            '>' -> {
                if (depth > 0) depth--
                current.append(ch)
            }

            ',' -> if (depth == 0) {
                parts += current.toString()
                current.clear()
            } else {
                current.append(ch)
            }

            else -> current.append(ch)
        }
    }
    parts += current.toString()
    return parts
}

private fun parseInstantiation(spec: String): InstantiationRequest {
    val lt = spec.indexOf('<')
    if (lt < 0) return InstantiationRequest(spec.trim(), emptyList())
    val base = spec.substring(0, lt).trim()
    val argStr = spec.substring(lt + 1, spec.lastIndexOf('>'))
    val args = argStr.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    return InstantiationRequest(base, args)
}

private val CValue<CXCursor>.templatedName: String
    get() {
//        if (numTemplateArguments != 0) {
//            return spelling.toKString() + "<" + (0 until numTemplateArguments).joinToString(",") {
//                when (getTemplateArgumentKind(it.toUInt())) {
//                    CXTemplateArgumentKind_Null -> "__NULL__"
//                    CXTemplateArgumentKind_Type -> getTemplateArgumentType(it.toUInt()).spelling.toKString() ?: ""
//                    CXTemplateArgumentKind_Declaration -> "__DECL__"
//                    CXTemplateArgumentKind_NullPtr -> "__NULL_PTR__"
//                    CXTemplateArgumentKind_Integral -> "__INTEGRAL__"
//                    CXTemplateArgumentKind_Template -> "__TYPE__"
//                    CXTemplateArgumentKind_TemplateExpansion -> "__TEMPLATE_EXPANSION__"
//                    CXTemplateArgumentKind_Expression -> "__EXPRESSION__"
//                    CXTemplateArgumentKind_Pack -> "__PACK__"
//                    CXTemplateArgumentKind_Invalid -> "__INVALID__"
//                }
//            } + ">"
//        }
        return spelling.toKString() ?: ""
    }
val CValue<CXCursor>?.fullyQualified: String
    get() =
        if (this == null || this == CXCursor.NULL) {
            ""
        } else if (kind == CXCursorKind.CXCursor_TranslationUnit) {
            ""
        } else {
            val res = semanticParent.fullyQualified
            if (res.isNotEmpty()) {
                "$res::$templatedName"
            } else {
                templatedName ?: ""
            }
        }

typealias ChildVisitor = (child: CValue<CXCursor>, parent: CValue<CXCursor>) -> Unit

val recurseVisitor =
    staticCFunction {
            child: CValue<CXCursor>,
            parent: CValue<CXCursor>,
            children: clang.CXClientData?
        ->
        children!!.asStableRef<ChildVisitor>().get().invoke(child, parent)
        CXChildVisitResult.CXChildVisit_Recurse
    }

val visitor =
    staticCFunction {
            child: CValue<CXCursor>,
            _: CValue<CXCursor>,
            children: clang.CXClientData?
        ->
        children!!.asStableRef<(CValue<CXCursor>) -> Unit>().get()
            .invoke(child)
        CXChildVisitResult.CXChildVisit_Continue
    }

inline fun CValue<CXCursor>.forEachRecursive(noinline childHandler: ChildVisitor) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, recurseVisitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.forEach(noinline childHandler: (CValue<CXCursor>) -> Unit) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, visitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.filterChildrenRecursive(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> = mutableListOf<CValue<CXCursor>>().also { list ->
    forEachRecursive { child, _ ->
        if (filter(child)) {
            list.add(child)
        }
    }
}

inline fun CValue<CXCursor>.filterChildren(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> = mutableListOf<CValue<CXCursor>>().also { list ->
    forEach {
        if (filter(it)) {
            list.add(it)
        }
    }
}

inline fun <T> CValue<CXCursor>.mapChildren(crossinline filter: (CValue<CXCursor>) -> T): List<T> =
    mutableListOf<T>().also { list ->
        forEach {
            list.add(filter(it))
        }
    }

val CValue<CXCursor>.allChildren: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEachRecursive { child, _ ->
                list.add(child)
            }
        }
    }

val CValue<CXCursor>.children: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEach(list::add)
        }
    }
