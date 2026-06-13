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
            linkTask?.let { lt -> it.dependsOn(lt) }
            // Phase E step 3 (#47, flip brick B2): in cpp-front-end mode this sync drives
            // the module's own config through the :cppfrontend binary, so build it first.
            // Gated by the property + only when :cppfrontend is in the build (LLVM-gated,
            // -PenableClang); the default path never references it. This replaces the old
            // per-module manual `dependsOn(":cppfrontend:featuregenCppBindings")` wiring —
            // ANY module flipped to cpp now self-wires the front-end build generically.
            if (target.findProperty("kpp.frontend.${target.name}") == "cpp" &&
                target.rootProject.findProject(":cppfrontend") != null
            ) {
                it.dependsOn(":cppfrontend:linkReleaseExecutableKlinker")
            }
            val krappedDir = krappedDirFor(target)
            val manifestFile = File(krappedDir, "requested.txt")
            val generatedFile = File(krappedDir, "generated.txt")
            val fixupFile = File(krappedDir, "fixups.json")
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
            it.inputs.file(kexe).withPropertyName("krapperGenKexe")
                .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
            it.inputs.files(manifestFile).withPropertyName("requestedManifest")
                .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
                .optional(true)
            ext?.let { e ->
                it.inputs.property("instantiations", e.instantiations.sorted())
                it.inputs.property("referencePolicy", e.referencePolicy ?: "")
                it.inputs.property("cppStandard", e.cppStandard ?: "")
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
                if (!kexe.exists()) {
                    throw GradleException(
                        "kplusplusSync: krapper_gen kexe not found at $kexe. Either run " +
                            ":krapper_gen:linkDebugExecutableNative in the kplusplus included " +
                            "build, or check the includeBuild(\"...\") path in your " +
                            "settings.gradle.kts."
                    )
                }
                // Phase E step 3 (#47, flip brick B2): when this module is flipped to the
                // cpp front-end (-Pkpp.frontend.<module>=cpp), GENERATE its bindings from
                // the module's OWN kplusplus{} config (its header + instantiation worklist)
                // through the generic cpp pipeline — cppfrontend parse -> ModelIo JSON ->
                // krapper_gen --frontend=cpp — into build/krapped-cpp, then return. The
                // libclang body below is skipped. Fully additive + gated: absent the
                // property this never fires and the default path is byte-for-byte unchanged.
                if (target.findProperty("kpp.frontend.$moduleName") == "cpp") {
                    runCppFrontendSync(target, ext, moduleName, krappedDir, kexe)
                    return@doLast
                }
                // The v2 extension (created in apply(), captured above) is
                // optional — projects with no header import and no fixups
                // still work via the existing pure-instantiation flow.
                val hasHeaders = ext?.headers?.isNotEmpty() == true
                val seededInstantiations = ext?.instantiations.orEmpty()
                val hasManifest = manifestFile.exists()
                if (!hasManifest && !hasHeaders && seededInstantiations.isEmpty()) {
                    println(
                        "kplusplusSync: no manifest at $manifestFile and no kplusplus " +
                            "{ header(...) } configured — nothing to do yet. Run a build " +
                            "first to let the compiler tell us what to generate, or add an " +
                            "import block."
                    )
                    return@doLast
                }
                val manifestSpecs = if (hasManifest) {
                    manifestFile.readLines()
                        .map { line -> line.trim() }
                        .filter { line -> line.isNotEmpty() }
                } else {
                    emptyList()
                }
                // Union the compiler-written manifest with build-script-seeded
                // instantiations (the bootstrap path for user templates).
                //
                // Sort for a deterministic instantiation order. krapper_gen carries
                // cross-instantiation state within a single run, so the order the
                // specs are forced in can change the result: `std::unordered_map`'s
                // `value_type`/`mapped_type` member typedefs (which libclang collapses
                // to a bare template parameter) poison the identically-spelled
                // typedefs of a later `std::map`, dropping map's at/operator[]/count.
                // Sorting makes the order deterministic — and alphabetical order happens
                // to place `std::map` before `std::unordered_map`, so both resolve
                // regardless of which test seeded the manifest first. This is a workaround,
                // not a guarantee: a future container could need explicit ordering. The
                // underlying order-sensitivity is a deeper krapper_gen issue tracked separately.
                val requested = (manifestSpecs + seededInstantiations).distinct().sorted()
                val alreadyGenerated = if (generatedFile.exists()) {
                    generatedFile.readLines()
                        .map { line -> line.trim() }
                        .filter { line -> line.isNotEmpty() }
                        .toMutableSet()
                } else {
                    mutableSetOf()
                }
                val newRequests = requested.filter { spec -> spec !in alreadyGenerated }
                // Force regeneration whenever a header set is configured —
                // there's no per-method tracking for header-driven imports
                // and skipping would leave a stale wrapper. The skip
                // optimization only applies to the pure-instantiation path.
                if (!hasHeaders && newRequests.isEmpty() && requested.isNotEmpty()) {
                    println(
                        "kplusplusSync: all ${requested.size} requested instantiations " +
                            "already generated."
                    )
                    return@doLast
                }
                // The kexe regenerates the whole output dir each run, so always pass
                // the full set of requested instantiations — otherwise a sync that
                // adds one new spec would wipe the previously-generated bindings.
                krappedDir.mkdirs()
                val compilerPath = ext?.compiler ?: resolveDefaultCompiler()
                val args = mutableListOf(
                    kexe.absolutePath,
                    moduleName,
                    "-r", (ext?.referencePolicy ?: "INCLUDE_MISSING"),
                    "-p", "krapper.$moduleName",
                    "-c", compilerPath,
                    "-o", krappedDir.absolutePath
                )
                ext?.cppStandard?.let { std ->
                    args += "--std"
                    args += std
                }
                ext?.rootPackage?.let { rootPackage ->
                    args += "--root-package"
                    args += rootPackage
                }
                for (spec in requested) {
                    args += "--instantiate"
                    args += spec
                }
                if (ext != null) {
                    // Scoped-import allowlist (--only / --only-file). When set, only these
                    // classes are fully bound; everything else falls to referencePolicy.
                    // Essential for slicing a large library (Clang/LLVM) — see KPlusPlusExtension.
                    // Pass each entry as its OWN `--only <entry>` (the CLI option is
                    // .multiple()). Comma-joining them into a single arg would corrupt a
                    // templated entry like `std::map<int, int>`: the CLI splits --only on
                    // `,`, so the entry would be torn into `std::map<int` and ` int>`.
                    for (entry in ext.only) {
                        args += "--only"
                        args += entry
                    }
                    ext.onlyFile?.let { onlyFilePath ->
                        args += "--only-file"
                        args += target.file(onlyFilePath).absolutePath
                    }
                    for (headerPath in ext.headers) {
                        args += "--header"
                        args += target.file(headerPath).absolutePath
                    }
                    // headerDirectories are forwarded by adding files in those
                    // dirs via -I implicitly through IndexRequest; krapper_gen
                    // computes headerDirectories from each --header path, so
                    // an explicit user-supplied include dir works by being
                    // the parent of a --header entry. For now we also pass
                    // each library through -l.
                    for (libPath in ext.libraries) {
                        args += "-l"
                        args += target.file(libPath).absolutePath
                    }
                    if (ext.fixups.isNotEmpty()) {
                        fixupFile.writeText(ext.fixups.toJsonArray())
                        args += "--fixup-file"
                        args += fixupFile.absolutePath
                    } else if (fixupFile.exists()) {
                        // Stale fixup file from a previous configuration —
                        // delete so krapper_gen doesn't accidentally apply
                        // old fixups against a now-empty config.
                        fixupFile.delete()
                    }
                }
                val summary = buildString {
                    append("kplusplusSync: ")
                    if (hasHeaders) append("headers=${ext.headers.size} ")
                    if (requested.isNotEmpty()) {
                        append("regenerating ${requested.size} instantiation(s): ")
                        append(requested.joinToString(", "))
                    } else {
                        append("(no instantiations requested)")
                    }
                }
                println(summary)
                val proc = ProcessBuilder(args).inheritIO().start()
                val exit = proc.waitFor()
                if (exit != 0) {
                    throw GradleException("krapper_gen failed: exit $exit")
                }
                if (newRequests.isNotEmpty()) {
                    alreadyGenerated.addAll(newRequests)
                    generatedFile.writeText(
                        alreadyGenerated.sorted().joinToString("\n") + "\n"
                    )
                }
            }
        }
    }

    /**
     * Phase E step 3 (#47, flip brick B2) — GENERIC cpp-front-end binding generation.
     *
     * Drives the consuming module's OWN `kplusplus { header(...) ; instantiate(...) }`
     * config through the two-artifact cpp pipeline, in place of the libclang sync:
     *  1. the gated `:cppfrontend` binary parses the module's header (the base model) and
     *     each forced instantiation (the forcing models) into :krapper_model ModelIo JSON
     *     (one binary invocation per payload — a parse crash on one spec is isolated and
     *     named, failing THIS module rather than corrupting the rest);
     *  2. krapper_gen `--frontend=cpp` loads those models (libclang parse SKIPPED) and runs
     *     the SAME resolve+codegen the libclang path does — invoked with the SAME CLI the
     *     libclang sync builds (module name, reference policy, package, output, --instantiate
     *     / --only / --header / fixups / --std / --root-package), minus the parse.
     *
     * This is the generalization of the former featuregen-hardcoded
     * `:cppfrontend:featuregenCppBindings` task: any module flipped to
     * `-Pkpp.frontend.<module>=cpp` generates from its own config, no per-module wiring.
     *
     * Fail-fast (de-risks the B3 default flip): if the cpp front-end yields an EMPTY
     * krapped-cpp (no Kotlin bindings / no .def — e.g. cppfrontend couldn't parse the
     * module's headers), throw naming the module instead of silently compiling against
     * nothing.
     */
    private fun runCppFrontendSync(
        target: Project,
        ext: KPlusPlusExtension?,
        moduleName: String,
        krappedDir: File,
        kexe: File
    ) {
        val cppfrontendProject = target.rootProject.findProject(":cppfrontend")
            ?: throw GradleException(
                "kplusplusSync[$moduleName]: -Pkpp.frontend.$moduleName=cpp but :cppfrontend " +
                    "is not in this build. The cpp front-end needs the LLVM-gated modules — " +
                    "build with -PenableClang (or -PllvmConfig=<path-to-llvm-config>)."
            )
        val cppBinary = cppfrontendProject.layout.buildDirectory
            .file("bin/klinker/cppfrontendRelease/cppfrontend").get().asFile
        if (!cppBinary.exists()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: cppfrontend binary not found at $cppBinary — the " +
                    "sync should depend on :cppfrontend:linkReleaseExecutableKlinker."
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
        // cppfrontend's --parity-emit parses ONE root header (the forcing models #include it
        // by the same absolute path krapper_gen receives via --header). Multi-header cpp
        // parsing is a later extension; the cpp-ready modules (featuregen, fixtures) are
        // single-header, so the primary header is the parse root.
        val headerPath = target.file(headers.first()).absolutePath
        val std = ext?.cppStandard ?: DEFAULT_CPP_STANDARD

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
        fun emit(specs: List<String>): String {
            val proc = ProcessBuilder(
                listOf(cppBinary.absolutePath, "--parity-emit", modelsDir.absolutePath, headerPath, std) +
                    specs
            ).redirectErrorStream(true).start()
            val out = proc.inputStream.readBytes().toString(Charsets.UTF_8)
            val exit = proc.waitFor()
            print(out)
            if (exit != 0) {
                throw GradleException(
                    "kplusplusSync[$moduleName]: cppfrontend --parity-emit failed (exit $exit) " +
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
                "kplusplusSync[$moduleName]: cppfrontend emitted no base model at $baseModel."
            )
        }
        val forcingArgs = mutableListOf<String>()
        for (spec in requested) {
            val out = emit(listOf(spec))
            val path = out.lineSequence()
                .firstOrNull { it.startsWith("PARITY_MODEL $spec=") }
                ?.substringAfter('=')
                ?: throw GradleException(
                    "kplusplusSync[$moduleName]: cppfrontend emitted no forcing model for '$spec'."
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
        args += "--frontend"
        args += "cpp"
        args += "--parsedModel"
        args += baseModel.absolutePath
        args += forcingArgs
        println(
            "kplusplusSync[$moduleName]: --frontend=cpp over ${requested.size} " +
                "instantiation(s) from $headerPath -> $krappedDir"
        )
        val exit = ProcessBuilder(args).inheritIO().start().waitFor()
        if (exit != 0) {
            throw GradleException(
                "kplusplusSync[$moduleName]: krapper_gen --frontend=cpp failed: exit $exit"
            )
        }

        // Fail-fast (B3 de-risk): refuse to leave the module compiling against nothing.
        // A successful krapper_gen ALWAYS emits the CppBinding.kt boilerplate + the .def, so
        // "no kt files at all" never happens after a 0-exit run — the real empty-output mode
        // is cppfrontend parsing the header into ZERO classes (e.g. it couldn't parse it),
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
                    "present=${defFile.exists()}). cppfrontend could not turn $moduleName's " +
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
        // Final fallback: the legacy hardcoded path. The doLast will throw a
        // helpful error if it doesn't exist.
        return null to target.rootProject.file(
            "krapper_gen/build/bin/native/debugExecutable/krapper_gen.kexe"
        )
    }

    /**
     * The directory the generated cinterop .def + Kotlin sources live in (and that
     * kplusplusSync writes). Defaults to `<projectDir>/krapped` — the committed, standard
     * location. The single gated, additive override (default-absent, so the default path
     * is byte-for-byte unchanged) is the Phase E (#47) flip-safety harness:
     * `-Pkpp.frontend.<module>=cpp` consumes cpp-front-end bindings under
     * `<buildDir>/krapped-cpp` instead: kplusplusSync's libclang body is skipped and it
     * generates that dir itself via `runCppFrontendSync` (the :cppfrontend binary over the
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
        const val PLUGIN_VERSION = "0.2.2"
        const val MIN_KOTLIN_VERSION = "2.3.20"

        // The C++ standard the cpp front-end parses (and krapper_gen compiles the wrapper)
        // under when a module sets no kplusplus { cppStandard = ... }. Matches krapper_gen's
        // own default and the former featuregen-hardcoded cpp path.
        const val DEFAULT_CPP_STANDARD = "c++14"
    }
}
