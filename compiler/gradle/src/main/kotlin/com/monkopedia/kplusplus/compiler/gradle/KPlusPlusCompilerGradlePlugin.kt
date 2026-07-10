package com.monkopedia.kplusplus.compiler.gradle

import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation

class KPlusPlusCompilerGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        // The compiler plugin uses FIR APIs that change between Kotlin versions; running
        // against an older Kotlin than the one we compile against throws NoClassDefFoundError
        // mid-compilation. Fail fast with a clear message. withType fires regardless of
        // plugin apply order (id(...) or version-catalog alias path).
        target.plugins.withType(KotlinBasePlugin::class.java) { kotlinPlugin ->
            val kotlinVersion = kotlinPlugin.pluginVersion
            if (compareVersions(kotlinVersion, MIN_KOTLIN_VERSION) < 0) {
                error(
                    "kplusplus compiler plugin requires Kotlin $MIN_KOTLIN_VERSION " +
                        "or later (found $kotlinVersion). Upgrade your kotlin plugin version."
                )
            }
        }

        // v2 escape hatch: the `kplusplus { … }` extension carries optional
        // headers/library imports and the narrow fixup directives. Tasks
        // read it at execution time; absence is the canonical "pure
        // template-instantiation" path that has worked since M7.
        target.extensions.create("kplusplus", KPlusPlusExtension::class.java)

        registerSyncTask(target)
    }

    /**
     * Register the kplusplusSync task on the consumer project. Reads
     * `<projectDir>/krapped/requested.txt` (which the FIR checker writes) and
     * generates exactly the requested instantiations, deduping against
     * `<projectDir>/krapped/generated.txt`.
     *
     * v0: assumes `:krapper_gen` is a sibling project producing the kexe at the
     * conventional path. Productionization will resolve the kexe from a published
     * artifact or a SubpluginOption.
     */
    private fun registerSyncTask(target: Project) {
        target.tasks.register("kplusplusSync") {
            it.group = "kplusplus"
            it.description =
                "Generate C++ template instantiations requested by the compiler plugin."
            // Two layouts: krapper_gen lives either as a sibling project in
            // the same Gradle build (canonical dev/slice usage) or in an
            // included build under one of the standard names (example/
            // consumer usage). Resolve the linkDebugExecutableNative task
            // and the kexe artifact through whichever path is wired up.
            val (linkTask, kexe) = resolveKrapperGen(target)
            // Phase E step 3 (#47, flip brick B1, Path A) — the stage-0 SEED anchor. A module
            // that is NOT yet cpp-parseable (krapper_parse, the laggards) sources its bindings
            // from a COMMITTED seed instead of (re)parsing its headers with the in-tree
            // libclang krapper_gen. In seed mode krapper_gen is neither built nor run (post-B5
            // its libclang front-end is gone), so don't depend on its link task and don't
            // declare its kexe an input — the committed seed is the only input.
            val seedMode = target.findProperty("kpp.frontend.${target.name}") == "seed"
            if (!seedMode) linkTask?.let { lt -> it.dependsOn(lt) }
            // Phase E step 3 (#47, flip brick B2): in cpp-front-end mode this sync drives
            // the module's own config through the :krapper_parse binary, so build it first.
            // Gated by the property + only when :krapper_parse is in the build (LLVM-gated,
            // -PenableClang); the default path never references it. This replaces the old
            // per-module manual `dependsOn(":krapper_parse:featuregenCppBindings")` wiring —
            // ANY module flipped to cpp now self-wires the front-end build generically.
            // resolveKrapperParse covers BOTH the sibling layout (featuregen/cppfixture) and
            // the included-build layout (the standalone v8 example, where krapper_parse lives in
            // the kplusplus build pulled in via includeBuild) — its link task is null when
            // clang isn't enabled, in which case the doLast fails fast with the -PenableClang
            // guidance.
            if (target.findProperty("kpp.frontend.${target.name}") == "cpp") {
                resolveKrapperParse(target).first?.let { ct -> it.dependsOn(ct) }
            }
            val krappedDir = krappedDirFor(target)
            val manifestFile = File(krappedDir, "requested.txt")
            val moduleName = target.name
            // Declare up-to-date inputs/outputs so Gradle can (a) order this task
            // ahead of the native compile that consumes krapped/ (see
            // wireGeneratedBindings), and (b) skip the regeneration when nothing
            // that feeds it has changed. Inputs: the generator kexe, the
            // compiler-written manifest, the configured headers, and the
            // extension config that becomes krapper_gen CLI args. Output: the
            // whole krapped/ dir (the kexe regenerates it wholesale each run).
            //
            // The header-driven path intentionally has no per-method change
            // tracking (see the hasHeaders force-regenerate note below), so when
            // headers are configured we leave the task always out-of-date by
            // declaring no output — Gradle then re-runs it every invocation,
            // which is the existing, correct behaviour for that path.
            val ext = target.extensions.findByType(KPlusPlusExtension::class.java)
            val hasHeadersAtConfig = ext?.headers?.isNotEmpty() == true
            if (!seedMode) {
                // inputs.files (plural), NOT inputs.file (singular): for a from-published
                // consumer (R2, #128) the kexe is a mavenLocal artifact staged under
                // build/ (resolvePublishedTool). If a `clean` in the SAME invocation wipes
                // build/ AFTER configuration staged it, singular inputs.file would abort at
                // validation on the missing path; the plural form tolerates a non-existent
                // entry, lets the task run, and the doLast re-stages it via resolveKrapperGen
                // (idempotent) or fails fast on a genuinely-missing tool. When the file is
                // present it still drives the up-to-date check identically.
                it.inputs.files(kexe).withPropertyName("krapperGenKexe")
                    .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
            }
            it.inputs.files(manifestFile).withPropertyName("requestedManifest")
                .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
                .optional(true)
            ext?.let { e ->
                it.inputs.property("instantiations", e.instantiations.sorted())
                it.inputs.property("referencePolicy", e.referencePolicy ?: "")
                it.inputs.property("cppStandard", e.cppStandard ?: "")
                it.inputs.property("noRtti", e.noRtti)
                it.inputs.property("rootPackage", e.rootPackage ?: "")
                it.inputs.property("only", e.only.sorted())
                it.inputs.property("onlyFile", e.onlyFile ?: "")
                it.inputs.property("fixups", e.fixups.toJsonArray())
                if (e.headers.isNotEmpty()) {
                    it.inputs.files(e.headers.map { h -> target.file(h) })
                        .withPropertyName("headers")
                        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
                }
            }
            if (!hasHeadersAtConfig) {
                it.outputs.dir(krappedDir).withPropertyName("krappedDir")
            }
            it.doLast {
                // flip B5 (#47): krapper_gen's in-tree libclang front-end was DELETED, so
                // every binding-generating module must declare HOW it produces bindings via
                // `kpp.frontend.<module>`:
                //   = seed -> recompile the module's COMMITTED stage-0 seed (no krapper_gen,
                //             no parse — see runSeedSync); or
                //   = cpp  -> the LLVM krapper_parse parses the header into ModelIo JSON and
                //             krapper_gen consumes it (--frontend=cpp — see runKrapperParseSync).
                // A module with NEITHER set has no parse path (the libclang reducer is gone),
                // so fail clearly instead of silently doing nothing.
                if (seedMode) {
                    runSeedSync(target, ext, moduleName, krappedDir)
                    return@doLast
                }
                if (target.findProperty("kpp.frontend.$moduleName") == "cpp") {
                    // Re-resolve at execution time: for the published path this re-stages
                    // the tool from mavenLocal if build/ was wiped by a same-invocation
                    // `clean` after configuration staged it (resolvePublishedTool is
                    // idempotent). For the in-tree paths this returns the same kexe.
                    val execKexe = resolveKrapperGen(target).second
                    if (!execKexe.exists()) {
                        throw GradleException(
                            "kplusplusSync: krapper_gen kexe not found at $execKexe. Either " +
                                "run :krapper_gen:linkDebugExecutableNative in the kplusplus " +
                                "included build, check the includeBuild(\"...\") path in your " +
                                "settings.gradle.kts, or (for a from-published consumer) " +
                                "declare mavenLocal() so " +
                                "com.monkopedia.kplusplus:krapper_gen:$PLUGIN_VERSION resolves."
                        )
                    }
                    runKrapperParseSync(target, ext, moduleName, krappedDir, execKexe)
                    return@doLast
                }
                throw GradleException(
                    "kplusplusSync[$moduleName]: the in-tree libclang front-end was removed " +
                        "in flip B5 (#47) — krapper_gen no longer parses headers in-process. " +
                        "Set kpp.frontend.$moduleName=cpp (generate via the LLVM krapper_parse; " +
                        "build with -PenableClang or -PllvmConfig=<path>) or " +
                        "kpp.frontend.$moduleName=seed (recompile the committed stage-0 seed)."
                )
            }
        }
    }

    /**
     * Phase E step 3 (#47, flip brick B1, Path A) — the STAGE-0 SEED anchor.
     *
     * Builds a module's bindings from its COMMITTED seed instead of (re)parsing its headers
     * with the in-tree libclang krapper_gen. Triggered by `-Pkpp.frontend.<module>=seed`;
     * the mechanism by which modules that are NOT yet cpp-parseable (krapper_parse — the engine
     * that can't bootstrap itself — plus example/clangwalk/slice) still produce bindings AFTER
     * the libclang reducer is deleted from in-tree krapper_gen (B5).
     *
     * The committed seed is the DETERMINISTIC TEXT output of a one-time libclang generation,
     * living under `<module>/krapped/`: the Kotlin bindings (`src/`), the C++ wrapper
     * (`<module>.cc`/`<module>.h`) and the forcing headers (`KrapperForce_*.h`). This step
     * regenerates only the two NON-committed, host-local build products from that text:
     *  - the cinterop `.def` (deterministic from the module name + this checkout's krapped dir,
     *    so no absolute path is ever committed to git); and
     *  - the compiled wrapper object `lib<module>.a`, via the SAME plain C++ compile
     *    krapper_gen's CppCompiler runs (`<compiler> -std=<std> -c -fPIE -I<krapped>
     *    -DV8_COMPRESS_POINTERS <module>.cc`) — a compile with NO header parsing, exactly the
     *    capability that survives the B5 libclang delete. krapper_gen is never invoked.
     *
     * Additive + gated: default-absent the property, this never fires and the libclang path is
     * byte-for-byte unchanged. B5 flips the laggards' default to this seed source.
     */
    private fun runSeedSync(
        target: Project,
        ext: KPlusPlusExtension?,
        moduleName: String,
        krappedDir: File
    ) {
        val srcDir = File(krappedDir, "src")
        val bindingSources = srcDir.listFiles { f -> f.isFile && f.extension == "kt" }
            ?.filter { it.name != "CppBinding.kt" }
            .orEmpty()
        val cppWrapper = File(krappedDir, "$moduleName.cc")
        if (bindingSources.isEmpty() || !cppWrapper.exists()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: -Pkpp.frontend.$moduleName=seed but no committed " +
                    "stage-0 seed under $krappedDir (expected $moduleName.cc + src/*.kt). " +
                    "Generate it once with the libclang front-end and commit it (see #47, B1)."
            )
        }
        val std = ext?.cppStandard ?: DEFAULT_CPP_STANDARD
        val compilerPath = ext?.compiler ?: resolveDefaultCompiler()
        // Regenerate the cinterop .def — deterministic from the module name + this checkout's
        // krapped dir, so the committed seed carries no host-specific absolute path. Mirrors
        // DefWriter/CompileFlags for the static-wrapper case the laggards use.
        File(krappedDir, "$moduleName.def").writeText(
            buildString {
                appendLine("headers = $moduleName.h")
                appendLine(
                    "compilerOpts = -I${krappedDir.absolutePath} -DV8_COMPRESS_POINTERS"
                )
                appendLine("staticLibraries = lib$moduleName.a")
                appendLine("libraryPaths = ${krappedDir.absolutePath}")
                appendLine("package = krapper.$moduleName.internal")
            }
        )
        // Recompile the wrapper object the .def links — the SAME command CppCompiler runs,
        // minus any libclang parse (the committed .cc is self-contained / re-compilable).
        val libFile = File(krappedDir, "lib$moduleName.a")
        val cmd = mutableListOf(
            compilerPath, "-std=$std", "-c", "-fPIE",
            "-o", libFile.absolutePath,
            "-I${krappedDir.absolutePath}", "-DV8_COMPRESS_POINTERS"
        )
        // Dynamic (.so) wrapper deps, matching CompileFlags(linkStatics = true). The laggards
        // pass none (they link Clang/LLVM at the module's own K/N link), but stay general.
        ext?.libraries?.map { target.file(it) }?.filter { it.name.endsWith(".so") }?.let { sos ->
            sos.map { it.parentFile.absolutePath }.toSet().forEach { cmd += "-L$it" }
            sos.forEach { cmd += "-l${it.name.removePrefix("lib").removeSuffix(".so")}" }
        }
        cmd += cppWrapper.absolutePath
        println(
            "kplusplusSync[$moduleName]: stage-0 seed — ${bindingSources.size} committed " +
                "binding source(s); recompiling lib$moduleName.a (no krapper_gen, no libclang)."
        )
        val proc = ProcessBuilder(cmd).inheritIO().start()
        val exit = proc.waitFor()
        if (exit != 0) {
            throw GradleException(
                "kplusplusSync[$moduleName]: stage-0 seed wrapper compile failed (exit $exit): " +
                    cmd.joinToString(" ")
            )
        }
    }

    /**
     * Phase E step 3 (#47, flip brick B2) — GENERIC cpp-front-end binding generation.
     *
     * Drives the consuming module's OWN `kplusplus { header(...) ; instantiate(...) }`
     * config through the two-artifact cpp pipeline, in place of the libclang sync:
     *  1. the gated `:krapper_parse` binary parses the module's header (the base model) and
     *     each forced instantiation (the forcing models) into :krapper_model ModelIo JSON
     *     (one binary invocation per payload — a parse crash on one spec is isolated and
     *     named, failing THIS module rather than corrupting the rest);
     *  2. krapper_gen `--frontend=cpp` loads those models (libclang parse SKIPPED) and runs
     *     the SAME resolve+codegen the libclang path does — invoked with the SAME CLI the
     *     libclang sync builds (module name, reference policy, package, output, --instantiate
     *     / --only / --header / fixups / --std / --root-package), minus the parse.
     *
     * This is the generalization of the former featuregen-hardcoded
     * `:krapper_parse:featuregenCppBindings` task: any module flipped to
     * `-Pkpp.frontend.<module>=cpp` generates from its own config, no per-module wiring.
     *
     * Fail-fast (de-risks the B3 default flip): if the cpp front-end yields an EMPTY
     * krapped-cpp (no Kotlin bindings / no .def — e.g. krapper_parse couldn't parse the
     * module's headers), throw naming the module instead of silently compiling against
     * nothing.
     */
    private fun runKrapperParseSync(
        target: Project,
        ext: KPlusPlusExtension?,
        moduleName: String,
        krappedDir: File,
        kexe: File
    ) {
        // Resolve the krapper_parse binary across BOTH layouts (sibling project / included
        // build) — see resolveKrapperParse. A non-existent binary means clang isn't enabled
        // (krapper_parse is LLVM-gated and then in neither build), so guide to -PenableClang.
        val cppBinary = resolveKrapperParse(target).second
        if (!cppBinary.exists()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: -Pkpp.frontend.$moduleName=cpp but the krapper_parse " +
                    "binary was not found at $cppBinary. The cpp front-end needs the LLVM-gated " +
                    "modules — build with -PenableClang (or -PllvmConfig=<path-to-llvm-config>); " +
                    "in a standalone consumer that pulls kplusplus in via includeBuild, the flag " +
                    "must reach that included build too."
            )
        }
        val headers = ext?.headers.orEmpty()
        if (headers.isEmpty()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: -Pkpp.frontend.$moduleName=cpp requires a " +
                    "kplusplus { header(...) } — the cpp front-end parses a header to build " +
                    "its model, but $moduleName declares none."
            )
        }
        // krapper_parse's --parity-emit parses ONE root header (the forcing models #include it
        // by the same absolute path krapper_gen receives via --header). Multi-header cpp
        // parsing is a later extension; the cpp-ready modules (featuregen, fixtures) are
        // single-header, so the primary header is the parse root.
        val headerPath = target.file(headers.first()).absolutePath
        val std = ext?.cppStandard ?: DEFAULT_CPP_STANDARD
        // The module's `kplusplus { headerDirectory(...) }` roots, resolved to absolute paths.
        // These must reach BOTH the cpp parse (so the front-end's clang sees them) AND the
        // krapper_gen wrapper compile (so its quote-includes resolve) — a header dropped on
        // either side aborts the wrapper compile (exit 134) on cross-directory includes.
        val includeDirs = ext?.headerDirectories.orEmpty().map { target.file(it).absolutePath }

        // The worklist is the SAME union kplusplusSync's libclang path uses: the
        // compiler-written manifest (ALWAYS at <projectDir>/krapped/requested.txt — the FIR
        // checker hardcodes that path, see applyToCompilation) plus the build-seeded
        // instantiate() calls, deduped + sorted (the deterministic instantiation order).
        val manifestFile = File(target.projectDir, "krapped/requested.txt")
        val manifestSpecs = if (manifestFile.exists()) {
            manifestFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        val requested = (manifestSpecs + ext?.instantiations.orEmpty()).distinct().sorted()

        val modelsDir = File(krappedDir, "cpp-models").apply { mkdirs() }
        // The include dirs ride the trailing args as -I tokens; krapper_parse partitions them
        // from the type specs (which never start with -I) — see --parity-emit / parityEmit.
        val includeFlags = includeDirs.map { "-I$it" }
        fun emit(specs: List<String>): String {
            val proc = ProcessBuilder(
                listOf(cppBinary.absolutePath, "--parity-emit", modelsDir.absolutePath, headerPath, std) +
                    includeFlags + specs
            ).redirectErrorStream(true).start()
            val out = proc.inputStream.readBytes().toString(Charsets.UTF_8)
            val exit = proc.waitFor()
            print(out)
            if (exit != 0) {
                throw GradleException(
                    "kplusplusSync[$moduleName]: krapper_parse --parity-emit failed (exit $exit) " +
                        "for ${if (specs.isEmpty()) "the base model" else specs.joinToString()} " +
                        "— see the output above."
                )
            }
            return out
        }

        emit(emptyList())
        val baseModel = File(modelsDir, "base_model.json")
        if (!baseModel.exists()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: krapper_parse emitted no base model at $baseModel."
            )
        }
        val forcingArgs = mutableListOf<String>()
        for (spec in requested) {
            val out = emit(listOf(spec))
            val path = out.lineSequence()
                .firstOrNull { it.startsWith("PARITY_MODEL $spec=") }
                ?.substringAfter('=')
                ?: throw GradleException(
                    "kplusplusSync[$moduleName]: krapper_parse emitted no forcing model for '$spec'."
                )
            forcingArgs += "--forcingModel"
            forcingArgs += "$spec=$path"
        }

        krappedDir.mkdirs()
        // The wrapper is compiled with clang++ (matching what the cpp front-end PARSED the
        // header against — system libstdc++), NOT the konan-bundled gcc the libclang default
        // picks: a model parsed against system libstdc++ and a wrapper compiled against it
        // agree. Override via kplusplus { compiler = "..." }.
        val compilerPath = ext?.compiler ?: "clang++"
        val args = mutableListOf(
            kexe.absolutePath,
            moduleName,
            "-r", (ext?.referencePolicy ?: "INCLUDE_MISSING"),
            "-p", "krapper.$moduleName",
            "-c", compilerPath,
            "-o", krappedDir.absolutePath,
            "--std", std
        )
        ext?.rootPackage?.let {
            args += "--root-package"
            args += it
        }
        if (ext?.noRtti == true) {
            args += "--no-rtti"
        }
        for (spec in requested) {
            args += "--instantiate"
            args += spec
        }
        if (ext != null) {
            for (entry in ext.only) {
                args += "--only"
                args += entry
            }
            ext.onlyFile?.let {
                args += "--only-file"
                args += target.file(it).absolutePath
            }
            for (h in ext.headers) {
                args += "--header"
                args += target.file(h).absolutePath
            }
            // headerDirectory(...) roots -> krapper_gen's wrapper-compile -I set.
            for (dir in includeDirs) {
                args += "--include-dir"
                args += dir
            }
            for (lib in ext.libraries) {
                args += "-l"
                args += target.file(lib).absolutePath
            }
            if (ext.fixups.isNotEmpty()) {
                val fixupFile = File(krappedDir, "fixups.json")
                fixupFile.writeText(ext.fixups.toJsonArray())
                args += "--fixup-file"
                args += fixupFile.absolutePath
            }
        }
        // krapper_gen has a single front-end (the libclang-C reducer was removed in flip
        // B5, #47): it consumes the krapper_parse-produced ModelIo model via --parsedModel.
        // The legacy --frontend flag is gone.
        args += "--parsedModel"
        args += baseModel.absolutePath
        args += forcingArgs
        println(
            "kplusplusSync[$moduleName]: krapper_gen (cpp model) over ${requested.size} " +
                "instantiation(s) from $headerPath -> $krappedDir"
        )
        val exit = ProcessBuilder(args).inheritIO().start().waitFor()
        if (exit != 0) {
            throw GradleException(
                "kplusplusSync[$moduleName]: krapper_gen (--parsedModel) failed: exit $exit"
            )
        }

        // Fail-fast (B3 de-risk): refuse to leave the module compiling against nothing.
        // A successful krapper_gen ALWAYS emits the CppBinding.kt boilerplate + the .def, so
        // "no kt files at all" never happens after a 0-exit run — the real empty-output mode
        // is krapper_parse parsing the header into ZERO classes (e.g. it couldn't parse it),
        // leaving only that boilerplate. So require at least one ACTUAL binding source.
        val srcDir = File(krappedDir, "src")
        val bindingKt = if (srcDir.isDirectory) {
            srcDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.name != "CppBinding.kt" }
                .toList()
        } else {
            emptyList()
        }
        val defFile = File(krappedDir, "$moduleName.def")
        if (bindingKt.isEmpty() || !defFile.exists()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: the cpp front-end produced an EMPTY binding set " +
                    "in $krappedDir (binding kotlin files=${bindingKt.size}, $moduleName.def " +
                    "present=${defFile.exists()}). krapper_parse could not turn $moduleName's " +
                    "header ($headerPath) into usable bindings — refusing to compile " +
                    "$moduleName against nothing. Inspect the models under $modelsDir and the " +
                    "krapper_gen output above."
            )
        }
        val ktCount = bindingKt.size + 1 // + the CppBinding.kt boilerplate
        // Mirror generated.txt so the plugin's already-generated bookkeeping stays coherent.
        File(krappedDir, "generated.txt").writeText(requested.joinToString("\n") + "\n")
        println("kplusplusSync[$moduleName]: cpp front-end generated $ktCount Kotlin file(s).")
    }

    /**
     * Pick a C++ compiler for krapper_gen's wrapper-library step. krapper_gen
     * generates C++ code that references some libstdc++ internals (matching
     * what v8's own headers expose); newer host gcc / clang reject those.
     * The konan-bundled toolchain (gcc 8.3 / glibcxx 7) accepts them. Try
     * the konan toolchain first if present; otherwise fall back to clang++.
     * Override via `kplusplus { compiler = "..." }`.
     */
    private fun resolveDefaultCompiler(): String {
        val konanRoot = System.getProperty("user.home") + "/.konan/dependencies"
        val konanDir = File(konanRoot)
        if (konanDir.isDirectory) {
            val gpp = konanDir.walkTopDown()
                .maxDepth(3)
                .firstOrNull {
                    it.isFile && it.canExecute() && it.name == "x86_64-unknown-linux-gnu-g++"
                }
            if (gpp != null) return gpp.absolutePath
        }
        return "clang++"
    }

    /**
     * Find the krapper_gen kexe and its linking task. Two layouts:
     *
     *   1. Sibling project: krapper_gen is part of the same Gradle build as
     *      the consumer (canonical dev / slice case). `rootProject.findProject`
     *      resolves it directly; the kexe sits under `<root>/krapper_gen/build/…`.
     *   2. Included build: the consumer's build pulls the kplusplus repo in
     *      via `includeBuild("../../")` (canonical example / external
     *      consumer case). `gradle.includedBuilds` lists each included
     *      build; we ask each one for its `:krapper_gen:linkDebugExecutableNative`
     *      task and pick the first hit, then resolve the kexe relative to
     *      that included build's projectDir.
     *
     * Returns the task (or null if not found — kexe must already exist on
     * disk in that case) plus the kexe File. The caller is expected to fail
     * fast at execution time if the kexe doesn't exist.
     */
    /**
     * Extract the tool binary BUNDLED in the plugin jar — the 0.4.0 distribution model. The
     * released plugin carries its own `krapper_gen`/`krapper_parse` binaries on its classpath at
     * `/com/monkopedia/kplusplus/tools/linuxX64/<tool>`, so a consumer NEVER resolves a separate
     * tool Maven coordinate (that coordinate resolution is what collided with an in-build
     * `:krapper_parse` project). Copy the binary to a per-plugin-version cache under the Gradle
     * user home, chmod +x (jar entries carry no exec bit), and return it. Returns null when the
     * jar carries no bundled binary (an unbundled in-tree/dev jar) so the caller can fall through
     * to the sibling/includedBuild/published paths.
     */
    private fun extractBundledTool(target: Project, tool: String): File? {
        val cache = File(
            target.gradle.gradleUserHomeDir,
            "kplusplus/tools/$PLUGIN_VERSION/$tool",
        )
        if (cache.exists() && cache.length() > 0L) return cache
        val stream = javaClass.getResourceAsStream(
            "/com/monkopedia/kplusplus/tools/linuxX64/$tool",
        ) ?: return null
        return try {
            cache.parentFile.mkdirs()
            val tmp = File(cache.parentFile, "$tool.part")
            stream.use { input -> tmp.outputStream().use { out -> input.copyTo(out) } }
            tmp.copyTo(cache, overwrite = true)
            tmp.delete()
            cache.setExecutable(true)
            cache
        } catch (e: Exception) {
            target.logger.warn("kplusplus: failed to extract bundled $tool: ${e.message}")
            null
        }
    }

    private fun resolveKrapperGen(target: Project): Pair<Any?, File> {
        val direct = target.rootProject.findProject(":krapper_gen")
        if (direct != null) {
            return direct.tasks.findByName("linkDebugExecutableNative") to target.rootProject.file(
                "krapper_gen/build/bin/native/debugExecutable/krapper_gen.kexe"
            )
        }
        for (included in target.gradle.includedBuilds) {
            val taskRef = try {
                included.task(":krapper_gen:linkDebugExecutableNative")
            } catch (_: Throwable) {
                null
            }
            val kexe = File(
                included.projectDir,
                "krapper_gen/build/bin/native/debugExecutable/krapper_gen.kexe"
            )
            if (taskRef != null || kexe.exists()) {
                return taskRef to kexe
            }
        }
        // Consumer path (no in-tree source): a TRUE external consumer that declares the plugin
        // but no includeBuild. Prefer the binary BUNDLED in the plugin jar (0.4.0 model — the
        // released plugin carries its tools). The published-coordinate resolution stays as a
        // fallback for a plugin whose jar has no bundled binary (e.g. a 0.3.0 consumer).
        extractBundledTool(target, "krapper_gen")?.let { return null to it }
        resolvePublishedTool(target, "krapper_gen")?.let { return null to it }

        // Final fallback: the legacy hardcoded path. The doLast will throw a
        // helpful error if it doesn't exist.
        return null to target.rootProject.file(
            "krapper_gen/build/bin/native/debugExecutable/krapper_gen.kexe"
        )
    }

    /**
     * Published-artifact fallback (R2, #128) — resolve a tool binary
     * (`krapper_gen` / `krapper_parse`) from its published Maven coordinate for a
     * TRUE external consumer that declares the plugin + a repository but NO
     * `includeBuild` (neither sibling project nor included build is present).
     *
     * Models the #126 stage-0 consume seam (`resolveStage0Path` in
     * krapper_parse/build.gradle.kts): a resolvable [Configuration] with a single
     * dependency on `com.monkopedia.kplusplus:<tool>:<PLUGIN_VERSION>` selecting the
     * `linuxX64` classifier + `.kexe` extension (the publications are artifact-only, no
     * `.jar`), `isTransitive=false`. The resolved artifact comes back mode 0644 (a
     * Maven repo drops the exec bit), so we copy it into the consumer's build dir and
     * `setExecutable(true)` — exactly what `resolveStage0Path` does — because the sync
     * launches these via ProcessBuilder, which needs +x.
     *
     * Returns the staged, executable File, or null if resolution fails (no mavenLocal
     * repo / artifact not published) — the caller then falls through to its
     * fail-fast-at-execution error path. The consumer must declare `mavenLocal()`.
     */
    private fun resolvePublishedTool(target: Project, tool: String): File? {
        return try {
            val coordinate = "$PLUGIN_GROUP:$tool:$PLUGIN_VERSION"
            val configName = "kplusplusTool${tool.replaceFirstChar { it.uppercase() }}"
            val config = target.configurations.findByName(configName)
                ?: target.configurations.create(configName) {
                    it.isCanBeConsumed = false
                    it.isCanBeResolved = true
                    it.dependencies.add(
                        target.dependencies.create(coordinate).also { dep ->
                            (dep as? org.gradle.api.artifacts.ModuleDependency)?.apply {
                                isTransitive = false
                                artifact { art ->
                                    art.name = tool
                                    art.classifier = "linuxX64"
                                    // Real `.kexe` extension — the published release binary
                                    // uses it (an empty extension makes a trailing-dot filename
                                    // the Central Portal rejects). Must match the `extension`
                                    // on the releaseBinary publications in krapper_gen/
                                    // krapper_parse build.gradle.kts.
                                    art.extension = "kexe"
                                }
                            }
                        }
                    )
                }
            val resolved = config.resolve().singleOrNull() ?: return null
            if (!resolved.exists()) return null
            // Maven drops the exec bit — stage an executable copy under the build dir.
            val staged = File(
                target.layout.buildDirectory.get().asFile,
                "kplusplus/tools/$tool"
            )
            staged.parentFile.mkdirs()
            if (!staged.exists() ||
                staged.length() != resolved.length() ||
                staged.lastModified() < resolved.lastModified()
            ) {
                resolved.copyTo(staged, overwrite = true)
                staged.setExecutable(true)
            }
            staged
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Find the :krapper_parse release binary and its link task — the SAME two layouts
     * resolveKrapperGen handles, because a cpp-front-end module can equally be a sibling
     * project (featuregen/cppfixture in the kplusplus build) or a consumer that pulls
     * kplusplus in via `includeBuild("../../")` (the standalone v8 example). In the included
     * case `rootProject.findProject(":krapper_parse")` is null — krapper_parse lives in the OTHER
     * build — so we resolve its task + binary through `gradle.includedBuilds`.
     *
     * krapper_parse is LLVM-gated (`-PenableClang`); when the flag is absent it is in NEITHER
     * build, so a null binary here means "not enabled". Returns the link task (or null) plus
     * the binary File; the caller fails fast at execution time if the binary doesn't exist.
     */
    private fun resolveKrapperParse(target: Project): Pair<Any?, File> {
        val relBinary = "krapper_parse/build/bin/klinker/krapper_parseRelease/krapper_parse"
        // Bootstrap override (#122, step 1.1): when an EXTERNAL stage-0 krapper_parse binary is
        // supplied via -Pkpp.stageZeroKrapperParse=<abs-path>, use it and declare NO task
        // dependency (null link task). This is what breaks the self-hosting cycle: building
        // :krapper_parse via -Pkpp.frontend.krapper_parse=cpp would otherwise resolve the parser
        // to :krapper_parse's own link task -> a Gradle dependency cycle. With a null task the
        // caller (dependsOn only when non-null) wires no self-edge, and runKrapperParseSync
        // uses this .second path as the parser. Additive + gated: absent property = today's
        // behavior exactly.
        //
        // SCOPE (#122 capstone): the override applies ONLY when resolving the front-end for
        // :krapper_parse ITSELF — that is the sole module whose self-cycle the stage-0 breaks.
        // A DOWNSTREAM consumer (featuregen/cppfixture) on the cpp path must resolve the LIVE
        // :krapper_parse link task/binary so that, when :krapper_parse is simultaneously =cpp, the
        // consumer is fed :krapper_parse's SELF-GENERATED binary — not the stage-0 seed directly.
        // Without this scope the global property would short-circuit every consumer to the
        // stage-0 seed, and the "krapper_parse=cpp feeding featuregen=cpp" composition could never
        // exercise the self-generated parser. Absent property = today's behavior exactly.
        if (target.name == "krapper_parse") {
            (target.findProperty("kpp.stageZeroKrapperParse") as? String)?.takeIf { it.isNotBlank() }
                ?.let { return null to File(it) }
        }
        val direct = target.rootProject.findProject(":krapper_parse")
        if (direct != null) {
            return direct.tasks.findByName("linkReleaseExecutableKlinker") to
                direct.layout.buildDirectory
                    .file("bin/klinker/krapper_parseRelease/krapper_parse").get().asFile
        }
        for (included in target.gradle.includedBuilds) {
            // Only the build that actually HOSTS the krapper_parse module is a candidate.
            // The root kplusplus build ALWAYS pulls in the `compiler` build via
            // includeBuild("compiler"), and IncludedBuild.task(":krapper_parse:...") returns
            // a LAZY reference that does NOT validate the project exists — so without this
            // guard the `compiler` build (which has no :krapper_parse) falsely matches when
            // :krapper_parse isn't in the root build (no -PenableClang) yet the module is on
            // the cpp front-end, and we wire a dependsOn to a project that lives in neither
            // build → "Project with path ':krapper_parse' not found in build ':compiler'" at
            // graph-resolution time (breaking even IDE sync). Filter on the module dir.
            if (!File(included.projectDir, "krapper_parse").isDirectory) continue
            val taskRef = try {
                included.task(":krapper_parse:linkReleaseExecutableKlinker")
            } catch (_: Throwable) {
                null
            }
            val binary = File(included.projectDir, relBinary)
            if (taskRef != null || binary.exists()) {
                return taskRef to binary
            }
        }
        // Published-artifact fallback (R2, #128): a TRUE external consumer has neither
        // a sibling :krapper_parse project nor an includeBuild that hosts it. Resolve
        // the LLVM parser tool from its published Maven coordinate (needs mavenLocal()).
        // The published krapper_parse binary is LLVM-linked and needs LLVM present at
        // RUNTIME — but a standalone consumer has no in-tree LLVM-gated modules, so no
        // -PenableClang is needed here (that flag only gates in-tree root-build modules).
        // Consumer path: prefer the binary bundled in the plugin jar (0.4.0 model), then the
        // published Maven coordinate (fallback for an unbundled/0.3.0 plugin jar).
        extractBundledTool(target, "krapper_parse")?.let { return null to it }
        resolvePublishedTool(target, "krapper_parse")?.let { return null to it }
        return null to target.rootProject.file(relBinary)
    }

    /**
     * The directory the generated cinterop .def + Kotlin sources live in (and that
     * kplusplusSync writes). Defaults to `<projectDir>/krapped` — the committed, standard
     * location. The single gated, additive override (default-absent, so the default path
     * is byte-for-byte unchanged) is the Phase E (#47) flip-safety harness:
     * `-Pkpp.frontend.<module>=cpp` consumes cpp-front-end bindings under
     * `<buildDir>/krapped-cpp` instead: kplusplusSync's libclang body is skipped and it
     * generates that dir itself via `runKrapperParseSync` (the :krapper_parse binary over the
     * module's own config → krapper_gen --frontend=cpp), generically, no per-module task.
     *
     * Used by BOTH the sync task (where it writes) and wireGeneratedBindings (where the
     * cinterop/srcDir are pointed), so the on-disk wiring stays consistent.
     */
    private fun krappedDirFor(target: Project): File {
        if (target.findProperty("kpp.frontend.${target.name}") == "cpp") {
            return File(target.layout.buildDirectory.get().asFile, "krapped-cpp")
        }
        return File(target.projectDir, "krapped")
    }

    private fun compareVersions(a: String, b: String): Int {
        val aParts = a.split(".", "-").mapNotNull { it.toIntOrNull() }
        val bParts = b.split(".", "-").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(aParts.size, bParts.size)) {
            val x = aParts.getOrNull(i) ?: 0
            val y = bParts.getOrNull(i) ?: 0
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = PLUGIN_GROUP,
        artifactId = PLUGIN_NAME,
        version = PLUGIN_VERSION
    )

    // Kotlin 2.4.0 removed getPluginArtifactForNative — the plugin artifact from
    // getPluginArtifact() is now used for native compilations too (the separate
    // `-native` host artifact is no longer a distinct plugin-loading path).

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        // Auto-add ksrpc-jni to this compilation's compiler-plugin classpath with
        // isTransitive=true so the in-compiler JNI bridge resolves at runtime.
        val configName = "kotlinCompilerPluginClasspath" +
            kotlinCompilation.target.targetName.replaceFirstChar { it.uppercase() } +
            kotlinCompilation.name.replaceFirstChar { it.uppercase() }
        project.afterEvaluate {
            project.configurations.findByName(configName)?.let { cfg ->
                cfg.isTransitive = true
                project.dependencies.add(configName, "com.monkopedia.ksrpc:ksrpc-jni:1.1.0")
            }
        }
        // Auto-wire the generated cinterop .def + Kotlin source dir for native
        // main compilations, conditional on existence at configure time. After a
        // kplusplusSync run, the next Gradle invocation re-evaluates configuration
        // and the wiring picks up.
        if (kotlinCompilation is KotlinNativeCompilation && kotlinCompilation.name == "main") {
            wireGeneratedBindings(kotlinCompilation, project)
            // The main compile consumes the generated krapped/ sources and
            // cinterop .def. Make it (and thereby the test compile, which
            // depends on it) depend on kplusplusSync so the generator runs —
            // and Gradle's up-to-date check regenerates stale output — before
            // anything compiles against it. Without this, `:featuregen:nativeTest`
            // run as its own invocation would compile whatever krapped/ happened
            // to be on disk (issue #16).
            kotlinCompilation.compileTaskProvider.configure { compileTask ->
                compileTask.dependsOn("kplusplusSync")
            }
        }
        // Per-compilation manifest path: matches the krapped/ layout the sync
        // task + wireGeneratedBindings already use, so the on-disk wiring stays
        // identical to the v0 hardcoded slice path — just no longer hardcoded.
        val manifestPath = File(project.projectDir, "krapped/requested.txt").absolutePath
        // Mirror the generator's rootPackage to the FIR plugin so its binding lookup
        // (bindingClassId) finds the container bindings under the same package.
        val rootPackage = project.extensions
            .findByType(KPlusPlusExtension::class.java)?.rootPackage
        return project.provider {
            buildList {
                add(SubpluginOption("requestManifestPath", manifestPath))
                rootPackage?.let { add(SubpluginOption("rootPackage", it)) }
            }
        }
    }

    private fun wireGeneratedBindings(
        compilation: KotlinNativeCompilation,
        project: Project
    ) {
        val moduleName = project.name
        val krappedDir = krappedDirFor(project)
        val krappedDef = File(krappedDir, "$moduleName.def")
        val krappedSrc = File(krappedDir, "src")
        // Wire the generated cinterop .def + Kotlin source dir into the main
        // native compilation. This must happen even when the artifacts don't yet
        // exist at configuration time: kplusplusSync produces them, and the
        // cinterop/compile tasks depend on it (below), so they run after the
        // generator. Conditioning on existence (the old behaviour) was the
        // chicken-and-egg in issue #16 — a stale/missing def at configure time
        // meant the cinterop was never created, so a sync that regenerated the
        // def couldn't be picked up in the same invocation and the compile failed
        // on the unresolved `<module>` cinterop package.
        //
        // Only wire when the project is actually configured to generate bindings
        // (a kplusplus { } block with headers/instantiations, or a manifest the
        // compiler wrote, or already-present artifacts). A project that does
        // neither has no krapped/ to consume and must not get a cinterop pointed
        // at a def that will never exist.
        project.afterEvaluate {
            val ext = project.extensions.findByType(KPlusPlusExtension::class.java)
            val manifestFile = File(krappedDir, "requested.txt")
            val willGenerate = ext?.headers?.isNotEmpty() == true ||
                ext?.instantiations?.isNotEmpty() == true ||
                manifestFile.exists()
            if (!willGenerate && !krappedDef.exists() && !krappedSrc.exists()) {
                return@afterEvaluate
            }
            compilation.cinterops.create("kplusplus") { interop ->
                interop.defFile = krappedDef
            }
            // The cinterop task reads the generated .def; make it regenerate via
            // kplusplusSync first so it can't process a stale def (issue #16).
            // The interop task is named cinterop<Name><Target>; for the
            // "kplusplus" interop on the "native" target that is
            // cinteropKplusplusNative.
            val interopTaskName = "cinteropKplusplus" +
                compilation.target.targetName.replaceFirstChar { it.uppercase() }
            project.tasks.matching { it.name == interopTaskName }.configureEach {
                it.dependsOn("kplusplusSync")
            }
            // The generated Kotlin sources live under krapped/src. srcDir tolerates
            // a not-yet-existing directory at configure time; kplusplusSync (a
            // compile dependency) creates it before compilation reads it.
            compilation.defaultSourceSet.kotlin.srcDir(krappedSrc)
        }
    }

    private companion object {
        const val PLUGIN_ID = "com.monkopedia.kplusplus.compiler"
        const val PLUGIN_GROUP = "com.monkopedia.kplusplus"
        const val PLUGIN_NAME = "kplusplus-compiler-plugin"
        const val PLUGIN_VERSION = "0.3.0"
        const val MIN_KOTLIN_VERSION = "2.3.20"

        // The C++ standard the cpp front-end parses (and krapper_gen compiles the wrapper)
        // under when a module sets no kplusplus { cppStandard = ... }. Matches krapper_gen's
        // own default and the former featuregen-hardcoded cpp path.
        const val DEFAULT_CPP_STANDARD = "c++14"
    }
}
