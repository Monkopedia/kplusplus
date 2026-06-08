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
            val krappedDir = File(target.projectDir, "krapped")
            val manifestFile = File(krappedDir, "requested.txt")
            val generatedFile = File(krappedDir, "generated.txt")
            val fixupFile = File(krappedDir, "fixups.json")
            val moduleName = target.name
            it.doLast {
                if (!kexe.exists()) {
                    throw GradleException(
                        "kplusplusSync: krapper_gen kexe not found at $kexe. Either run " +
                            ":krapper_gen:linkDebugExecutableNative in the kplusplus included " +
                            "build, or check the includeBuild(\"...\") path in your " +
                            "settings.gradle.kts."
                    )
                }
                // Resolve the v2 extension (created in apply()). The block is
                // optional — projects with no header import and no fixups
                // still work via the existing pure-instantiation flow.
                val ext = target.extensions.findByType(KPlusPlusExtension::class.java)
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
        val krappedDir = File(project.projectDir, "krapped")
        val krappedDef = File(krappedDir, "$moduleName.def")
        val krappedSrc = File(krappedDir, "src")
        if (krappedDef.exists()) {
            compilation.cinterops.create("kplusplus") { interop ->
                interop.defFile = krappedDef
            }
        }
        if (krappedSrc.exists()) {
            compilation.defaultSourceSet.kotlin.srcDir(krappedSrc)
        }
    }

    private companion object {
        const val PLUGIN_ID = "com.monkopedia.kplusplus.compiler"
        const val PLUGIN_GROUP = "com.monkopedia.kplusplus"
        const val PLUGIN_NAME = "kplusplus-compiler-plugin"
        const val PLUGIN_VERSION = "0.2.2"
        const val MIN_KOTLIN_VERSION = "2.3.20"
    }
}
