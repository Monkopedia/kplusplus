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

import com.monkopedia.krapper.AddToChild
import com.monkopedia.krapper.AddToParent
import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.FilterDefinition
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.IndexedService
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.MapRequest
import com.monkopedia.krapper.MapResult
import com.monkopedia.krapper.MappingService
import com.monkopedia.krapper.NoChange
import com.monkopedia.krapper.RemoveChild
import com.monkopedia.krapper.RemoveParent
import com.monkopedia.krapper.ReplaceChild
import com.monkopedia.krapper.ReplaceParent
import com.monkopedia.krapper.ResolverService
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.CppCompiler
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.DefWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.model.ForcingHeader
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.kotlinType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.recursiveSequence
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedCType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedType
import kotlinx.cinterop.Arena
import kotlinx.coroutines.runBlocking

// A fuller identity for a resolved method than its (possibly-null, possibly-colliding)
// uniqueCName: the C++ qualified name + simple name + full argument-type signature, plus
// the mangled cName. Used to decide whether a free function pulled in by a forcing
// re-parse was ALREADY main-bound (keep) or is incidental (drop). Two distinct functions
// that happen to mangle to the same uniqueCName get different identities here, so a
// collision can't false-keep an incidental function (which would emit non-compiling Kotlin).
private val ResolvedMethod.forcingIdentity: String
    get() = "$qualified::$name(${args.joinToString(",") { it.type.toString() }})#$uniqueCName"

class IndexedServiceImpl(private val config: KrapperConfig, private val request: IndexRequest) :
    IndexedService {
    private val scope = Arena()
    private val index = createIndex(0, 0) ?: error("Failed to create Index")
    private var classes: List<ResolvedElement> = emptyList()
    private val mappings = mutableListOf<MappingService>()

    // Forcing headers synthesized by requestInstantiation to make libclang instantiate a
    // template specialization. Keyed forceName -> file contents. They are PARSED from a
    // per-run temp dir (hermetic, cleaned in close()) and then re-materialized into the
    // output dir by writeTo, so the generated wrapper #includes them by a stable, relative,
    // self-contained path (no /tmp leak, deterministic across runs).
    private val forcingHeaders = linkedMapOf<String, String>()

    // The output-relative paths the generated .cc/.h should #include for the forcing headers.
    // Populated by writeTo once the output dir is known.
    private val syntheticHeaders = mutableListOf<String>()

    // Process-unique scratch dir for parsing the forcing headers. Created lazily on the
    // first instantiation (a run with no --instantiate touches nothing) and removed in
    // close(). Per-run unique (pid + counter) so concurrent runs never clobber each other.
    private var tempDir: File? = null
    private fun tempDir(): File =
        tempDir ?: File.createTempDir("krapper_inst").also { tempDir = it }

    // What the run was explicitly ASKED to bind: the --only allowlist entries plus each
    // --instantiate target. The drop-ledger report diffs this against what actually
    // bound so a coverage gap (requested-but-neither-bound-nor-dropped) is visible. Empty
    // for an unrestricted DefaultFilter import (every non-std class is requested).
    private val requested = mutableListOf<String>()

    // The container specializations forced by instantiation requests SO FAR, accumulated
    // across every requestInstantiation call. Threaded into each resolveForcing so a later
    // request's pass-3 re-resolve of an already-bound class can recover EVERY range accessor
    // of that class (one per forced container) at once — not just the one this request forced,
    // which the per-call fresh tracker + last-wins merge would otherwise leave as the sole
    // survivor (e.g. CXXRecordDecl keeping only decls() and dropping methods()/bases()). See
    // `resolveForcing`'s [forcedContainerKeys] doc.
    private val forcedContainerKeys = mutableSetOf<String>()

    init {
        scope.defer {
            index.dispose()
        }
        // Start each generation run from clean process-scoped state so its output and
        // report reflect only this run: the drop ledger (shared with any prior run) and
        // the GenerationContext (the WrappedType intern cache + the rootPackage). Both
        // must be reset BEFORE any resolution (requestInstantiation / filterAndResolve):
        // a type's Kotlin package is baked into its ResolvedKotlinType at resolve time,
        // so rooting it later (e.g. in writeTo) would be too late.
        DropLedger.reset()
        GenerationContext.reset(config.rootPackage)
    }

    private val includePaths = generateIncludes(config.compiler)

    // Clang parse args for libclang. Built per-call so the C++ standard
    // (config.cppStandard) and any request-supplied header directories both flow
    // through — previously a dead field defaulted to c++14, blocking C++17 types.
    private fun parseArgs(extraIncludes: List<String>): Array<String> =
        arrayOf("-xc++", "--std=${config.cppStandard}") +
            (includePaths + extraIncludes).map { "-I$it" }.toTypedArray()

    init {
        if (config.debug) {
            runBlocking {
                Log.i("Args: ${parseArgs(request.headerDirectories).toList()}")
            }
        }
    }

    override suspend fun filterAndResolve(filter: FilterDefinition) {
        // Record the explicit allowlist as the requested set (DefaultFilter requests
        // everything, so it contributes nothing to diff against).
        if (filter is AllowListFilter) {
            requested.addAll(filter.qualifiedNames)
        }
        Log.i("Parsing headers: ${request.headers}")
        val resolver = scope.parseHeader(
            index,
            request.headers,
            includePaths + request.headerDirectories,
            args = parseArgs(request.headerDirectories),
            debug = config.debug,
            strictDiagnostics = config.strictDiagnostics
        )
        val initialClasses = resolver.findClasses(filter.wrapperFilter())
        Log.i("Found ${initialClasses.size} classes to resolve")
        if (config.debug) {
            val resolvingStr = initialClasses
                .map { (it as? WrappedClass)?.type?.toString() ?: it.toString() }
                .sorted()
                .joinToString(",\n    ")
            Log.i("Resolving: [\n    $resolvingStr\n]")
        }
        classes = initialClasses.resolveAll(resolver, config.referencePolicy)
        Log.i("Resolved ${classes.size} top-level elements")
    }

    override suspend fun addMapping(mappingService: MappingService) {
        mappings.add(mappingService)
    }

    override suspend fun requestInstantiation(req: InstantiationRequest) {
        val target = "${req.base}<${req.args.joinToString(", ")}>"
        requested.add(target)
        // The forcing struct's name + header content are SHARED with the cpp front-end
        // (ForcingHeader, :krapper_model): the cppfrontend binary synthesizes the same
        // bytes for its forcing parse, so both paths force the same specialization from
        // the same translation unit.
        val forceName = ForcingHeader.forceName(target)
        val forcingContent = ForcingHeader.contentFor(target, request.headers)
        forcingHeaders[forceName] = forcingContent
        // #45 brick 3: with --frontend=cpp the forcing-parse tree was already produced by
        // the cppfrontend binary (which synthesizes the SAME forcing header and parses it
        // with the Clang C++ AST); load it instead of parsing through libclang. The
        // forcing header content above is still recorded either way: writeTo
        // re-materializes it so the generated wrapper can #include the specialization's
        // declaration. Everything downstream of obtaining the resolver — the scoping,
        // resolveForcing's 3-pass dance, cumulative forcedContainerKeys, dedup — is the
        // SAME code on both paths.
        val resolver = cppForcingModelPaths[target]?.let { path ->
            Log.i("cpp front-end forcing handoff: loading $target model from $path")
            loadParsedModel(path)
        } ?: run {
            // Parse from a per-run temp file (cleaned in close()); the same header is
            // re-materialized into the output dir by writeTo for the generated wrapper.
            val tmpFile = File(tempDir(), "$forceName.h").path
            File(tmpFile).writeText(forcingContent)
            Log.i("Forcing instantiation of $target via $tmpFile")
            scope.parseHeader(
                index,
                listOf(tmpFile),
                includePaths + request.headerDirectories,
                args = parseArgs(request.headerDirectories),
                debug = config.debug,
                strictDiagnostics = config.strictDiagnostics
            )
        }
        // SCOPE THE FORCING COLLECTION. The synthetic header #includes the consumer's
        // own headers, so `findClasses { defaultFilter() }` collects EVERY class in the
        // re-parsed TU — against a large library that is the whole transitive surface
        // (~1269 classes for Clang), not just the specialization we forced. We drop ONLY
        // the UNRELATED classes and otherwise leave the collection identical to before,
        // so the only behavioral delta at scale is "don't re-resolve the world".
        //
        // A `findClasses` result is KEPT when it is:
        //   (a) the `KrapperForce_*` forcing struct — its `value` member's type IS the
        //       target specialization, so resolving it materializes the specialization;
        //   (b) a class ALREADY bound by filterAndResolve — re-resolved here so a method
        //       dropped the first time because it referenced the (then unbound) container
        //       is recovered now that the container exists (e.g.
        //       `RangeHolder::items() -> std::vector<Thing*>`); the writer's last-wins
        //       dedup then adopts the improved version; or
        //   (c) any non-class element (e.g. a namespace static method) — these are few
        //       and namespace-scoped, and excluding them would diverge from the original
        //       handling (it dropped UDL operators like `operator""s` from the C++ side
        //       while the Kotlin side still emitted them).
        // Excluded: a WrappedClass that is neither the forcing struct nor already bound —
        // i.e. the ~1269 unrelated classes. Referenced by nothing in the kept set, they
        // are not pulled back in under INCLUDE_MISSING either. Net: forcing adds ~1 class
        // (the specialization), not the world.
        val alreadyBoundKeys = classes.filterIsInstance<ResolvedClass>()
            .mapTo(HashSet()) { it.type.toString() }
        // The free functions ALREADY bound by the main resolve (by uniqueCName) — captured
        // before forcing. resolveForcing keeps ALL non-class elements from the re-parsed TU
        // (the rule that preserves an already-bound std::literals UDL operator's C++ side),
        // but that TU's #includes also pull in UNRELATED namespace free functions the
        // AllowListFilter excluded from the main resolve (e.g. llvm::sys::fs / llvm::driver,
        // which then emit non-compiling Kotlin). Keep only the free functions that were
        // already bound; drop the incidental ones. The UDL case is safe because its operator
        // IS main-bound (the Kotlin side emits it), so its uniqueCName is in this set.
        // Key on a FULLER identity than the bare `uniqueCName`: the C++ qualified name
        // plus the full argument-type signature (and the mangled cName). Keying on
        // `uniqueCName` alone let an incidental free function that mangles to the SAME
        // cName as a main-bound one false-keep — and emit the non-compiling Kotlin this
        // filter exists to prevent. A null `uniqueCName` is folded into the identity (not
        // dropped, as `mapNotNullTo` did) so a null-cName collision can't slip through.
        val boundMethodIds = classes.filterIsInstance<ResolvedMethod>()
            .mapTo(HashSet()) { it.forcingIdentity }
        val scopedFound = resolver.findClasses { defaultFilter() }.filter {
            val cls = it as? WrappedClass
                ?: return@filter true // keep non-class elements (static methods) as-is
            val key = cls.type.toString()
            // EXACT match on the forcing struct's type (was a substring `contains`, which
            // wrongly kept any nested type whose spelling merely EMBEDDED forceName).
            key == forceName || key in alreadyBoundKeys
        }
        Log.i("Scoped forcing to ${scopedFound.size} element(s) for $target")
        val newClasses = scopedFound.resolveForcing(
            resolver,
            config.referencePolicy,
            alreadyBoundKeys,
            forcedContainerKeys
        )
        // One entry per class type-key, last copy wins (#60): without this, every
        // writer received a fresh full copy of each bound class per instantiation
        // request and the C++ writers emitted a complete wrapper section per copy
        // (18 sections per class in featuregen). See dedupClassesLastWins.
        classes = (
            classes + newClasses.filter {
                it !is ResolvedMethod || it.forcingIdentity in boundMethodIds
            }
            ).dedupClassesLastWins()
    }

    override suspend fun writeTo(output: String) {
        if (config.debug) {
            Log.i("Running mapping")
        }
        if (mappings.isNotEmpty()) {
            executeMappings()
        }
        if (config.debug) {
            val resolvedClasses = classes
                .map { (it as? ResolvedClass)?.type?.toString() ?: it.toString() }
                .sorted()
                .joinToString(",\n    ")
            Log.i("Generating for [\n    $resolvedClasses\n]")
        }
        val outputBase = File(output)
        outputBase.mkdirs()
        // Re-materialize the forcing headers into the output dir so the generated wrapper
        // #includes them by a stable relative path (no /tmp), and the emitted .cc is
        // self-contained / re-compilable. Deterministic name -> deterministic output.
        syntheticHeaders.clear()
        for ((forceName, content) in forcingHeaders) {
            val forcingFile = File(outputBase, "$forceName.h")
            forcingFile.writeText(content)
            syntheticHeaders.add(forcingFile.path)
        }
        val headers = request.headers + syntheticHeaders
        val namer = NameHandler()
        Log.i("Generating header file")
        File(outputBase, "${config.moduleName}.h").writeText(
            CppCodeBuilder().also {
                HeaderWriter(
                    it,
                    policy = config.errorPolicy.policy
                ).generate(config.moduleName!!, headers, classes)
            }.toString()
        )
        Log.i("Generating C++ wrapper")
        val cppFile = File(outputBase, "${config.moduleName}.cc")
        cppFile.writeText(
            // qualifyFunctionPointers: the .cc wrapper names namespace-scoped
            // function-pointer typedefs with their fully-qualified spelling so they
            // resolve in C++ (the .h header keeps the unqualified extern-"C" form).
            CppCodeBuilder(qualifyFunctionPointers = true).also {
                CppWriter(cppFile, it, policy = config.errorPolicy.policy).generate(
                    config.moduleName!!,
                    headers,
                    classes
                )
            }.toString()
        )
        val pkg = config.pkg
        File(outputBase, "${config.moduleName}.def").writeText(
            DefWriter(namer).generateDef(
                outputBase,
                "$pkg.internal",
                config.moduleName!!,
                headers,
                request.libraries
            )
        )
        Log.i("Compiling native wrapper library")
        CppCompiler(
            File(outputBase, "lib${config.moduleName}.a"),
            config.compiler,
            config.cppStandard
        ).compile(
            cppFile,
            headers,
            request.libraries
        )
        Log.i("Generating Kotlin bindings")
        val srcDir = File(outputBase, "src")
        KotlinWriter(
            "$pkg.internal",
            policy = config.errorPolicy.policy
        ).generate(
            srcDir,
            classes
        )
        writeCppBindingAnnotation(srcDir)
        reportDrops()
        Log.i("Code generation complete")
    }

    /**
     * Emit the drop-ledger report (requested / bound / dropped + the dropped list and any
     * requested-but-uncovered entries) and, when [KrapperConfig.failOnDrop] is set, fail
     * the run if anything was dropped. Default (failOnDrop=false) only logs, so existing
     * green runs are unchanged: drops are surfaced but never fatal unless opted in.
     */
    private suspend fun reportDrops() {
        val bound = classes.filterIsInstance<ResolvedClass>().map { it.type.toString() }
        Log.i(DropLedger.report(requested, bound))
        if (config.failOnDrop && DropLedger.hasDrops()) {
            error(
                "--fail-on-drop: ${DropLedger.drops.size} symbol(s) were dropped " +
                    "(see the drop ledger above)"
            )
        }
    }

    /**
     * Emit the `@krapper.CppBinding(val spec: String)` annotation alongside the
     * generated bindings so consumer source (and the kplusplus compiler plugin)
     * can resolve it without depending on a separate artifact. Each generated
     * class gets `@krapper.CppBinding("<cppSpec>")` so the plugin can recover
     * the C++ instantiation spec from a Kotlin element type.
     */
    private fun writeCppBindingAnnotation(srcDir: File) {
        File(srcDir, "CppBinding.kt").writeText(
            """
            |package krapper
            |
            |annotation class CppBinding(val spec: String)
            |
            |// Marks a generated scoped template factory (`fun <T> MemScope.Box(): Box<T>`)
            |// so the kplusplus compiler plugin can derive its container mapping (C++ base,
            |// binding-name prefix, package) and refine `Box<Int>()` to the concrete
            |// `Box__Int` binding — the generic generalization of the hardcoded cppVector
            |// facade recognition. (Arity comes from the factory's own type params.)
            |annotation class CppTemplate(
            |    val base: String,
            |    val prefix: String,
            |    val pkg: String
            |)
            |
            """.trimMargin()
        )
    }

    private suspend fun executeMappings() {
        val resolver = object : ResolverService {
            override suspend fun resolvedType(typeStr: String): ResolvedType =
                toResolvedCppType(WrappedType(typeStr))

            override suspend fun resolvedKotlinType(typeStr: String): ResolvedKotlinType =
                toResolvedKotlinType(WrappedType(typeStr).kotlinType)

            override suspend fun resolvedCType(typeStr: String): ResolvedCType =
                toResolvedCType(WrappedType(typeStr).cType)
        }
        Log.i("Resolving ${mappings.size} mapping filters")
        val mappingsAndFilters = mappings.map {
            it.getFilter(resolver).resolveFilter() to it
        }

        val allElements = classes.recursiveSequence().toList()
        Log.i("Applying mappings to ${allElements.size} elements")
        var matched = 0
        for ((index, element) in allElements.withIndex()) {
            if (index > 0 && index % 500 == 0) {
                Log.i("  Processed $index/${allElements.size} elements ($matched matched)")
            }
            for ((filter, mapper) in mappingsAndFilters) {
                if (!filter(element)) continue
                matched++
                if (config.debug) {
                    Log.i("Executing mapping ($mapper) on $element")
                }

                try {
                    val results = mapper.mapElement(
                        MapRequest(
                            element.parent ?: element,
                            element.parent?.children?.indexOf(element) ?: -1
                        )
                    )
                    if (config.debug) {
                        Log.i("     --> $results")
                    }
                    for (result in results) {
                        applyResult(result, element)
                    }
                } catch (t: Throwable) {
                    Log.w(t.message + "\n" + t.stackTraceToString())
                    throw RuntimeException("Mapping failed for $element", t)
                }
            }
        }
        Log.i("Mappings complete: $matched matches across ${allElements.size} elements")
    }

    private fun applyResult(result: MapResult, element: ResolvedElement) = when (result) {
        RemoveChild -> {
            element.parent?.removeChild(element)
        }

        RemoveParent -> {
            element.parent?.let { parent ->
                parent.parent?.removeChild(parent)
            }
        }

        is AddToChild -> {
            element.addChild(result.newChild)
        }

        is AddToParent -> {
            element.parent?.addChild(result.newChild)
        }

        is ReplaceChild -> {
            element.parent?.let { parent ->
                parent.removeChild(element)
                parent.addChild(result.newChild)
            }
        }

        is ReplaceParent -> {
            element.parent?.let { parent ->
                parent.parent?.let { parentParent ->
                    parentParent.removeChild(parent)
                    parentParent.addChild(result.newChild)
                }
            }
        }

        NoChange -> {
            // Nothing to do.
        }
    }

    override suspend fun close() {
        super.close()
        // Throwaway forcing-header intermediates — written to a per-run temp dir purely so
        // libclang can parse them; never part of generated output, so always safe to delete.
        // Cleared here so they don't accumulate in $TMPDIR across runs.
        tempDir?.takeIf { it.exists() }?.rmR()
        // index is disposed by the Arena's deferred cleanup in scope.clear(); disposing it
        // here too would double-free (clang_disposeIndex on a freed handle → abort).
        scope.clear()
    }
}
