package com.monkopedia.kplusplus.compiler.gradle

import com.monkopedia.krapper.BindingIndex
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
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
     */
    private fun registerSyncTask(target: Project) {
        target.tasks.register("kplusplusSync") {
            it.group = "kplusplus"
            it.description =
                "Generate C++ template instantiations requested by the compiler plugin."
            // ONE tool (#184): `krapper` parses the headers AND generates the bindings. It
            // lives either as a sibling project in the same Gradle build (canonical dev usage),
            // in an included build (example/consumer usage), or bundled in this plugin's jar.
            // Resolve the link task and the binary through whichever path is wired up.
            val (linkTask, kexe) = resolveKrapper(target)
            // Seed mode: a module sources its bindings from a COMMITTED seed instead of
            // (re)parsing its headers. The tool is neither built nor run, so don't depend on its
            // link task and don't declare its binary an input — the committed seed is the only
            // input.
            val seedMode = target.findProperty("kpp.frontend.${target.name}") == "seed"
            if (!seedMode) linkTask?.let { lt -> it.dependsOn(lt) }
            val krappedDir = krappedDirFor(target)
            val manifestFile = File(krappedDir, "requested.txt")
            val moduleName = target.name
            // Declare up-to-date inputs/outputs so Gradle can (a) order this task
            // ahead of the native compile that consumes krapped/ (see
            // wireGeneratedBindings), and (b) skip the regeneration when nothing
            // that feeds it has changed. Inputs: the generator kexe, the
            // compiler-written manifest, the configured headers, and the
            // extension config that becomes krapper CLI args. Output: the
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
                it.inputs.files(kexe).withPropertyName("krapperBinary")
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
                it.inputs.property("llvmConfig", e.llvmConfig ?: "")
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
                // Every binding-generating module must declare HOW it produces bindings via
                // `kpp.frontend.<module>`:
                //   = seed -> recompile the module's COMMITTED stage-0 seed (no tool run, no
                //             parse — see runSeedSync); or
                //   = cpp  -> the LLVM-linked krapper tool parses the header and generates the
                //             bindings from it (see runCppSync).
                // A module with NEITHER set has no parse path, so fail clearly instead of
                // silently doing nothing.
                if (seedMode) {
                    runSeedSync(target, ext, moduleName, krappedDir)
                    return@doLast
                }
                if (target.findProperty("kpp.frontend.$moduleName") == "cpp") {
                    // Re-resolve at execution time: for the published path this re-stages
                    // the tool from mavenLocal if build/ was wiped by a same-invocation
                    // `clean` after configuration staged it (resolvePublishedTool is
                    // idempotent). For the in-tree paths this returns the same binary.
                    val execKexe = resolveKrapper(target).second
                    if (!execKexe.exists()) {
                        throw GradleException(
                            "kplusplusSync: the krapper tool was not found at $execKexe. The " +
                                "cpp front-end needs the LLVM-linked krapper — either run " +
                                ":krapper:linkReleaseExecutableKlinker in the kplusplus " +
                                "included build, check the includeBuild(\"...\") path in your " +
                                "settings.gradle.kts, or (for a from-published consumer) " +
                                "declare mavenLocal() so " +
                                "com.monkopedia.kplusplus:krapper:$PLUGIN_VERSION resolves. " +
                                "An LLVM/Clang toolchain must be present at runtime either way."
                        )
                    }
                    runCppSync(target, ext, moduleName, krappedDir, execKexe)
                    return@doLast
                }
                throw GradleException(
                    "kplusplusSync[$moduleName]: no front-end selected. " +
                        "Set kpp.frontend.$moduleName=cpp (generate with the LLVM krapper tool) " +
                        "or kpp.frontend.$moduleName=seed (recompile the committed stage-0 seed)."
                )
            }
        }
    }

    /**
     * The STAGE-0 SEED path (`-Pkpp.frontend.<module>=seed`).
     *
     * Builds a module's bindings from its COMMITTED seed instead of (re)parsing its headers —
     * the mechanism by which modules that are NOT cpp-parseable (e.g. clangwalk) still produce
     * bindings without running the tool.
     *
     * The committed seed is a DETERMINISTIC TEXT output living under `<module>/krapped/`: the
     * Kotlin bindings (`src/`), the C++ wrapper (`<module>.cc`/`<module>.h`) and the forcing
     * headers (`KrapperForce_*.h`). This step regenerates only the two NON-committed, host-local
     * build products from that text:
     *  - the cinterop `.def` (deterministic from the module name + this checkout's krapped dir,
     *    so no absolute path is ever committed to git); and
     *  - the compiled wrapper object `lib<module>.a`, via the SAME plain C++ compile
     *    krapper's CppCompiler runs (`<compiler> -std=<std> -c -fPIE -I<krapped>
     *    -DV8_COMPRESS_POINTERS <module>.cc`) — a compile with NO header parsing. krapper is
     *    never invoked.
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
                    "Generate it once via the cpp front-end and commit it."
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
        // Recompile the wrapper object the .def links — the SAME command CppCompiler runs
        // (the committed .cc is self-contained / re-compilable).
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
                "binding source(s); recompiling lib$moduleName.a (no krapper run)."
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
     * GENERIC cpp-front-end binding generation (`-Pkpp.frontend.<module>=cpp`).
     *
     * Drives the consuming module's OWN `kplusplus { header(...) ; instantiate(...) }` config
     * through ONE krapper run (#184): it parses the module's header with the Clang C++ AST,
     * parses each requested instantiation's synthesized forcing header, resolves the lot and
     * generates the Kotlin bindings + C++ wrapper.
     *
     * Since #185 that run is a TYPED SESSION, not a subprocess with flags: krapper hosts its
     * ksrpc service on its stdio pipe and this drives it call by call (see [KrapperSession]),
     * so the config travels as data and krapper reports back structured [Diagnostic]s — each
     * drop with its C++ `file:line:col` and the reason the drop ledger recorded — instead of
     * an exit code plus interleaved stdout.
     *
     * Any module flipped to `-Pkpp.frontend.<module>=cpp` generates from its own config, no
     * per-module wiring.
     *
     * Fail-fast: if the run yields an EMPTY krapped-cpp (no Kotlin bindings / no .def — e.g.
     * the headers didn't parse into anything), throw naming the module instead of silently
     * compiling against nothing.
     */
    private fun runCppSync(
        target: Project,
        ext: KPlusPlusExtension?,
        moduleName: String,
        krappedDir: File,
        kexe: File
    ) {
        val headers = ext?.headers.orEmpty()
        if (headers.isEmpty()) {
            throw GradleException(
                "kplusplusSync[$moduleName]: -Pkpp.frontend.$moduleName=cpp requires a " +
                    "kplusplus { header(...) } — the cpp front-end parses a header to build " +
                    "its model, but $moduleName declares none."
            )
        }
        val std = ext?.cppStandard ?: DEFAULT_CPP_STANDARD
        // The module's `kplusplus { headerDirectory(...) }` roots, resolved to absolute paths.
        // These reach BOTH the parse (so the front-end's clang sees them) AND the wrapper
        // compile (so its quote-includes resolve) — a header dropped on either side aborts the
        // wrapper compile (exit 134) on cross-directory includes.
        //
        // The consumer's LLVM include dir rides along (#124, #128 R4): a from-published consumer
        // whose LLVM-22 headers are NOT on the default include path (e.g. apt.llvm.org under
        // /usr/lib/llvm-22/include) would otherwise fail to resolve <clang/AST/...>-style
        // headers, because the tool's bundled Clang only searches its resource-dir + the
        // driver's default paths. Discovered via `llvm-config --includedir` (or the
        // kplusplus { llvmConfig = ... } / -PllvmConfig override), mirroring settings.gradle.kts.
        val includeDirs = resolveLlvmIncludeDirs(target, ext) +
            ext?.headerDirectories.orEmpty().map { target.file(it).absolutePath }

        // The worklist: the compiler-written manifest (ALWAYS at
        // <projectDir>/krapped/requested.txt — the FIR checker hardcodes that path, see
        // applyToCompilation) plus the build-seeded instantiate() calls, deduped + sorted (the
        // deterministic instantiation order).
        val manifestFile = File(target.projectDir, "krapped/requested.txt")
        val manifestSpecs = if (manifestFile.exists()) {
            manifestFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        val requested = (manifestSpecs + ext?.instantiations.orEmpty()).distinct().sorted()

        krappedDir.mkdirs()
        val headerPaths = headers.map { target.file(it).absolutePath }
        val request = IndexRequest(
            headers = headerPaths,
            libraries = ext?.libraries.orEmpty().map { target.file(it).absolutePath }
        ).let {
            it.copy(
                headerDirectories = (it.headerDirectories + includeDirs).distinct(),
                includeDirs = includeDirs,
                // Debug aid (`-Pkpp.dumpModel`): keep the ModelIo JSON of every tree the run
                // parsed. Off by default — the parse -> resolve handoff is in-process.
                dumpModelDir = target.findProperty("kpp.dumpModel")
                    ?.let { _ -> File(krappedDir, "cpp-models").apply { mkdirs() }.absolutePath }
            )
        }
        val job = KrapperJob(
            moduleName = moduleName,
            config = KrapperConfig(
                pkg = "krapper.$moduleName",
                // The wrapper is compiled with clang++ (matching what the cpp front-end
                // PARSED the header against — system libstdc++), NOT the konan-bundled gcc:
                // a model parsed against system libstdc++ and a wrapper compiled against it
                // agree. Override via kplusplus { compiler = "..." }.
                compiler = ext?.compiler ?: "clang++",
                moduleName = moduleName,
                errorPolicy = ErrorPolicy.LOG,
                referencePolicy = ReferencePolicy.valueOf(
                    ext?.referencePolicy ?: ReferencePolicy.INCLUDE_MISSING.name
                ),
                debug = false,
                cppStandard = std,
                rootPackage = ext?.rootPackage,
                noRtti = ext?.noRtti == true
            ),
            request = request,
            allowList = resolveAllowList(target, ext),
            instantiations = requested,
            fixups = ext?.fixups.orEmpty(),
            outputDir = krappedDir
        )
        target.logger.lifecycle(
            "kplusplusSync[$moduleName]: krapper over ${requested.size} instantiation(s) from " +
                "${headerPaths.first()} -> $krappedDir"
        )
        // Experimental / diagnostic flag passthrough: `-Pkpp.x=diag.timing` (comma-separated
        // for several) is forwarded verbatim to the tool via its KRAPPER_X env var, so an
        // experiment can be driven through the build without touching source. Off by default.
        val toolEnv = (target.findProperty("kpp.x") as? String)
            ?.takeIf { it.isNotBlank() }
            ?.let { mapOf("KRAPPER_X" to it) }
            .orEmpty()
        // krapper's own exception text is the headline; the diagnostics it streamed before
        // dying (the wrapper compile's `file:line:col` errors, the drops that led there) are
        // appended — the whole difference from the exit-code era.
        val report = try {
            KrapperSession.run(kexe, job, target.logger, toolEnv)
        } catch (t: KrapperRunException) {
            throw GradleException(
                "kplusplusSync[$moduleName]: krapper failed: ${t.message}" + t.report.detail(),
                t.cause
            )
        }

        // Fail-fast: refuse to leave the module compiling against nothing.
        // A successful run ALWAYS emits the CppBinding.kt boilerplate + the .def, so "no kt
        // files at all" never happens after a successful run — the real empty-output mode is
        // the header parsing into ZERO classes, leaving only that boilerplate. So require at
        // least one ACTUAL binding source.
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
                    "present=${defFile.exists()}). krapper could not turn $moduleName's header " +
                    "(${headerPaths.first()}) into usable bindings — refusing to compile " +
                    "$moduleName against nothing. Re-run with -Pkpp.dumpModel to inspect the " +
                    "parsed model." + report.detail()
            )
        }
        val ktCount = bindingKt.size + 1 // + the CppBinding.kt boilerplate
        // Mirror generated.txt so the plugin's already-generated bookkeeping stays coherent.
        File(krappedDir, "generated.txt").writeText(requested.joinToString("\n") + "\n")
        target.logger.lifecycle(
            "kplusplusSync[$moduleName]: cpp front-end generated $ktCount Kotlin file(s); " +
                report.summary()
        )
    }

    /**
     * The scoped-import allowlist: `only(...)` entries merged with `onlyFile`'s lines (one
     * fully-qualified name per line; blank / `#` lines ignored). Read here rather than handed
     * to krapper as a path, so the filter travels over the channel as the data it is.
     */
    private fun resolveAllowList(target: Project, ext: KPlusPlusExtension?): List<String> {
        val fromFile = ext?.onlyFile?.let { path ->
            val file = target.file(path)
            if (!file.exists()) {
                target.logger.warn("kplusplus: --only-file $file does not exist; ignoring.")
                emptyList()
            } else {
                file.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
            }
        }.orEmpty()
        return (ext?.only.orEmpty() + fromFile).distinct()
    }

    /**
     * Resolve the consumer's LLVM/Clang include dir(s) to thread into the cpp-parse as `-I`
     * (#124, #128 R4). The cpp front-end's bundled Clang finds Clang's OWN builtin headers via
     * its resource-dir (`clang++ -print-resource-dir`, resolved inside krapper) and system
     * C++ headers via the driver's default GCC detection, but a library's OWN headers under a
     * NON-default-path LLVM install (e.g. apt.llvm.org's /usr/lib/llvm-22/include, where
     * <clang/AST/...> lives) are found ONLY if explicitly on the include path. In-tree this box
     * has system LLVM-22 on the default path so no -I was needed; a from-published consumer on a
     * versioned install has neither the settings.gradle.kts probe nor that default path — this
     * closes that gap.
     *
     * Discovery order, mirroring settings.gradle.kts:
     *   1. the `kplusplus { llvmConfig = ... }` DSL override;
     *   2. the `-PllvmConfig=<path>` project property (the SAME opt-in settings.gradle.kts uses);
     *   3. `llvm-config` on PATH.
     * When an override is set but unusable, fail fast with an actionable message. When NOTHING is
     * configured and `llvm-config` is not on PATH, return empty (the header may still be on the
     * default path, as in-tree) rather than hard-failing — if it is not, krapper's own
     * parse error surfaces. Idempotent + additive: absent config on a default-path box adds no -I.
     */
    private fun resolveLlvmIncludeDirs(target: Project, ext: KPlusPlusExtension?): List<String> {
        val llvmConfigOverride = ext?.llvmConfig?.takeIf { it.isNotBlank() }
            ?: (target.findProperty("llvmConfig") as? String)?.takeIf { it.isNotBlank() }
        val llvmConfigExec = resolveExecutable(llvmConfigOverride ?: "llvm-config")
        if (llvmConfigExec == null) {
            if (llvmConfigOverride != null) {
                throw GradleException(
                    "kplusplus: the configured llvm-config '$llvmConfigOverride' " +
                        "(kplusplus { llvmConfig = ... } or -PllvmConfig=) is not an executable " +
                        "file. Point it at your LLVM-22 install's llvm-config, e.g. " +
                        "/usr/lib/llvm-22/bin/llvm-config."
                )
            }
            // Nothing configured + none on PATH: don't hard-fail — the header may still be on
            // the default include path (the in-tree case). Surface the override hint at info.
            target.logger.info(
                "kplusplus: no llvm-config on PATH and none configured; the cpp front-end will " +
                    "rely on the default include path for library headers. If parsing fails to " +
                    "find your LLVM/library headers, set kplusplus { llvmConfig = " +
                    "\"/path/to/llvm-config\" } or pass -PllvmConfig=<path>."
            )
            return emptyList()
        }
        val includeDir = probeExec(llvmConfigExec, "--includedir")
        if (includeDir.isNullOrBlank() || !File(includeDir).isDirectory) {
            throw GradleException(
                "kplusplus: `${llvmConfigExec.absolutePath} --includedir` returned " +
                    "'${includeDir.orEmpty()}', which is not a directory. Is this a valid LLVM " +
                    "install? Override with kplusplus { llvmConfig = \"...\" } or -PllvmConfig=."
            )
        }
        // Additive-only guard (protects the in-tree path): a system LLVM install reports its
        // includedir as a DEFAULT system path (e.g. /usr/include). Clang already searches those,
        // so injecting them as an explicit `-I` adds nothing AND would reorder search precedence
        // (user `-I` before system) — exactly the in-tree case we must not alter. Only thread the
        // dir when it is a NON-default path (the versioned-install case #124 is about, e.g.
        // /usr/lib/llvm-22/include).
        val canonical = File(includeDir).canonicalPath
        if (canonical in DEFAULT_SYSTEM_INCLUDE_DIRS) return emptyList()
        return listOf(includeDir)
    }

    /** First line of `<exec> <arg>` stdout, trimmed; null on non-zero exit or no output. */
    private fun probeExec(exec: File, arg: String): String? {
        return try {
            val proc = ProcessBuilder(exec.absolutePath, arg)
                .redirectErrorStream(true).start()
            val out = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
            if (proc.waitFor() != 0) null else out.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Look up an executable: an absolute/relative path is taken verbatim (must be executable);
     * a bare name is searched on PATH. Mirrors settings.gradle.kts's resolveExecutable so the
     * consumer-side discovery matches the in-tree toolchain probe. Null when nothing is found.
     */
    private fun resolveExecutable(name: String): File? {
        val direct = File(name)
        if (direct.path.contains(File.separatorChar)) {
            return direct.takeIf { it.canExecute() }
        }
        return System.getenv("PATH").orEmpty().split(File.pathSeparatorChar)
            .map { File(it, name) }.firstOrNull { it.canExecute() }
    }

    /**
     * Pick a C++ compiler for the wrapper-library step. krapper
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
     * Extract the tool binary BUNDLED in the plugin jar — the 0.4.0 distribution model. The
     * released plugin carries its own `krapper` binary on its classpath at
     * `/com/monkopedia/kplusplus/tools/linuxX64/<tool>`, so a consumer NEVER resolves a separate
     * tool Maven coordinate (that coordinate resolution is what collided with an in-build
     * `:krapper` project). Copy the binary to a per-plugin-version cache under the Gradle user
     * home, chmod +x (jar entries carry no exec bit), and return it. Returns null when the jar
     * carries no bundled binary (an unbundled in-tree/dev jar) so the caller can fall through to
     * the sibling/includedBuild/published paths.
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
            // Per-invocation UNIQUE temp file (issue #149): two parallel first-time builds for
            // the same plugin version both see cache.exists()==false; a FIXED "$tool.part" name
            // let them race on one path so one could copyTo(cache) a torn write → corrupt cache
            // binary. createTempFile hands each invocation its own name; delete it on all paths.
            val tmp = java.nio.file.Files.createTempFile(cache.parentFile.toPath(), tool, ".part")
                .toFile()
            try {
                stream.use { input -> tmp.outputStream().use { out -> input.copyTo(out) } }
                tmp.copyTo(cache, overwrite = true)
            } finally {
                tmp.delete()
            }
            cache.setExecutable(true)
            cache
        } catch (e: Exception) {
            target.logger.warn("kplusplus: failed to extract bundled $tool: ${e.message}")
            null
        }
    }

    /**
     * Find the `krapper` binary and its link task. Four layouts, in order:
     *
     *   1. SELF-HOST (`:krapper` itself): the tool module's own bindings must be generated by
     *      the PREVIOUS release's tool, never by itself — resolving to its own link task would
     *      be a Gradle dependency cycle. `-Pkpp.stageZeroKrapper=<abs-path>` names an external
     *      stage-0 binary; otherwise the binary bundled in the applied (published) plugin jar is
     *      used. Both declare a NULL link task, so no self-edge is wired. Present only when the
     *      applied plugin is a bundled RELEASE; a dev-composite unbundled plugin returns null
     *      and falls through (use `=seed` or the override there).
     *   2. Sibling project: `:krapper` is part of the same Gradle build as the consumer (the
     *      canonical dev case). The binary sits under `<root>/krapper/build/…`.
     *   3. Included build: the consumer pulls the kplusplus repo in via `includeBuild("../../")`
     *      (the samples/v8 case). `gradle.includedBuilds` is asked for the link task, and the
     *      binary is resolved relative to that build's projectDir. Only the build that actually
     *      HOSTS the module is a candidate: `IncludedBuild.task(...)` returns a LAZY reference
     *      that does NOT validate the project exists, so an unrelated included build (e.g.
     *      `compiler`) would otherwise falsely match and wire a dependsOn to a project that
     *      isn't there — breaking even IDE sync. Filter on the module dir.
     *   4. TRUE external consumer (plugin declared, no includeBuild): the binary bundled in the
     *      plugin jar, else the published Maven coordinate (for an unbundled/0.3.x jar).
     *
     * Returns the link task (null on every path where nothing in THIS build builds the tool)
     * plus the binary File; the caller fails fast at execution time if it doesn't exist.
     */
    private fun resolveKrapper(target: Project): Pair<Any?, File> {
        val relBinary = "krapper/build/bin/klinker/krapperRelease/krapper"
        if (target.name == TOOL_NAME) {
            (target.findProperty("kpp.stageZeroKrapper") as? String)?.takeIf { it.isNotBlank() }
                ?.let { return null to File(it) }
            extractBundledTool(target, TOOL_NAME)?.let { return null to it }
        }
        val direct = target.rootProject.findProject(":$TOOL_NAME")
        if (direct != null) {
            return direct.tasks.findByName("linkReleaseExecutableKlinker") to
                direct.layout.buildDirectory
                    .file("bin/klinker/krapperRelease/krapper").get().asFile
        }
        for (included in target.gradle.includedBuilds) {
            if (!File(included.projectDir, TOOL_NAME).isDirectory) continue
            val taskRef = try {
                included.task(":$TOOL_NAME:linkReleaseExecutableKlinker")
            } catch (_: Throwable) {
                null
            }
            val binary = File(included.projectDir, relBinary)
            if (taskRef != null || binary.exists()) {
                return taskRef to binary
            }
        }
        extractBundledTool(target, TOOL_NAME)?.let { return null to it }
        resolvePublishedTool(target, TOOL_NAME)?.let { return null to it }
        // Final fallback: the conventional in-tree path. The doLast throws a helpful error if
        // it doesn't exist.
        return null to target.rootProject.file(relBinary)
    }

    /**
     * Published-artifact fallback (R2, #128) — resolve the `krapper` tool binary
     * from its published Maven coordinate for a
     * TRUE external consumer that declares the plugin + a repository but NO
     * `includeBuild` (neither sibling project nor included build is present).
     *
     * Models the #126 stage-0 consume seam: a resolvable [Configuration] with a single
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
                                    // on any releaseBinary publication of the tool.
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
     * The directory the generated cinterop .def + Kotlin sources live in (and that
     * kplusplusSync writes). Defaults to `<projectDir>/krapped` — the committed, standard
     * location. Under `-Pkpp.frontend.<module>=cpp` the cpp-front-end bindings live under
     * `<buildDir>/krapped-cpp` instead, generated by `runCppSync` (the krapper binary over the
     * module's own config), generically, no per-module task.
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
        // task + wireGeneratedBindings already use, so the on-disk wiring stays consistent.
        val manifestPath = File(project.projectDir, "krapped/requested.txt").absolutePath
        // Mirror the generator's rootPackage to the FIR plugin. Since #206/B2 the plugin no
        // longer positions bindings with it — it cross-checks it against the rootPackage the
        // index records, so a krapped tree predating a rootPackage change is reported rather
        // than resolved against.
        val rootPackage = project.extensions
            .findByType(KPlusPlusExtension::class.java)?.rootPackage
        // krapper's own record of which Kotlin class each C++ instantiation became (#186 B1),
        // written into the SAME directory the generated sources and .def come from — so it
        // tracks whichever front-end produced them (build/krapped-cpp for the cpp path,
        // <projectDir>/krapped otherwise). The FIR plugin reads binding names out of this file
        // instead of recomputing krapper's mangling, which is what #206 was filed about.
        //
        // Passed as a plain path, not a FilesSubpluginOption: the index's binding list is a
        // function of the same generation that writes krapped/src, and those sources are
        // already a declared input of this compilation, so content-hashing the index adds no
        // up-to-date signal the compile task does not already have. (The one part of the index
        // that CAN move on its own is `drops`, which this plugin never reads.)
        val bindingIndexPath =
            File(krappedDirFor(project), BindingIndex.FILE_NAME).absolutePath
        return project.provider {
            buildList {
                add(SubpluginOption("requestManifestPath", manifestPath))
                rootPackage?.let { add(SubpluginOption("rootPackage", it)) }
                add(SubpluginOption("bindingIndexPath", bindingIndexPath))
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
                interop.definitionFile.set(krappedDef)
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
        // PLUGIN_VERSION is GENERATED from the project version (#192) — see
        // `generatePluginVersion` in compiler/gradle/build.gradle.kts. It was a literal here
        // until it went stale for the whole 0.3.4 prep out of a green build.

        // The ONE tool binary (#184) — also the Gradle project name, the bundled-resource name
        // under /com/monkopedia/kplusplus/tools/linuxX64/, and the published coordinate.
        const val TOOL_NAME = "krapper"
        const val MIN_KOTLIN_VERSION = "2.3.20"

        // The C++ standard the cpp front-end parses (and compiles the wrapper)
        // under when a module sets no kplusplus { cppStandard = ... }. Matches krapper's
        // own default and the former featuregen-hardcoded cpp path.
        const val DEFAULT_CPP_STANDARD = "c++14"

        // Default system include dirs a system LLVM reports as its --includedir. When
        // llvm-config --includedir returns one of these, threading it as an explicit `-I`
        // is a no-op that would only reorder Clang's search precedence, so it is skipped
        // (keeps the in-tree, system-LLVM cpp path byte-for-byte unchanged). See
        // resolveLlvmIncludeDirs.
        val DEFAULT_SYSTEM_INCLUDE_DIRS = setOf("/usr/include", "/usr/local/include")
    }
}
