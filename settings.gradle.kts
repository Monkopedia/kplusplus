rootProject.name = "kplusplus"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    // FLAT build (no composite): the repo consumes the PUBLISHED kplusplus plugin rather than
    // includeBuild("compiler"). A composite substitutes any com.monkopedia.kplusplus:* module
    // dependency with the matching in-build project — which is what shadowed our own coordinates
    // and blocked krapper_parse from ever resolving a published tool. Flat has no such
    // substitution. The tool modules (krapper_gen/krapper_parse/krapper_model) stay siblings here,
    // so their dev loop is still immediate; PLUGIN development happens in an example (samples/*)
    // that includeBuilds compiler. Bump this version on each release.
    plugins {
        id("com.monkopedia.kplusplus.compiler") version "0.3.1"
    }
}

include(":krapper_gen")
include(":krapper_model")
include(":feature-tests")
include(":featuregen")

// :clangwalk is the stage1 self-bootstrap harness — it binds Clang's own C++ AST
// (libclang-cpp) and walks a real AST on the generated bindings. It REQUIRES an LLVM
// toolchain on the box (system clang++, libclang-cpp, libLLVM) that an ordinary build
// host won't have, so it is OFF by default and never configured on an LLVM-absent box.
// Opt in with `-PenableClang` (or set `enableClang=true`); the modules also auto-enable
// when an explicit toolchain is pointed at via `-PllvmConfig=<path-to-llvm-config>`.
//
// LLVM-MANDATORY (flip B5, #47): krapper_gen's in-tree libclang-C reducer was DELETED, so
// the only way to PARSE a header into bindings is the LLVM krapper_parse (-Pkpp.frontend.
// <module>=cpp). The default-build cpp consumers (:featuregen, :cppfixture) therefore now
// REQUIRE this flag — without it :krapper_parse is not in the build and their kplusplusSync
// fails fast with an actionable "build with -PenableClang" message (runKrapperParseSync).
// The flag stays opt-in (not default-ON) only so the LLVM-FREE modules — :krapper_gen
// (now a pure ModelIo consumer) and :krapper_model — and the committed-seed modules
// (:krapper_parse/:clangwalk, kpp.frontend=seed: a plain C++ recompile, no parse)
// still build/test on a box without an LLVM toolchain.
//
// #11(b) — toolchain probe hardening: when the modules ARE enabled, the LLVM toolchain
// is PROBED ONCE here and the discovered values (major version + libdir) are threaded to
// both clang modules via rootProject extras. So an LLVM major bump (22 -> 23) or a
// non-/usr/lib install needs NO per-module edits, and an absent/mis-pointed toolchain
// fails right here with an ACTIONABLE message instead of a cryptic `cannot find -lLLVM-22`
// linker error ~8 minutes into a release link.

// Look up an executable: an absolute/relative path is taken verbatim; a bare name is
// searched on PATH. Returns null when nothing executable is found.
fun resolveExecutable(name: String): File? {
    val direct = File(name)
    if (direct.path.contains(File.separatorChar)) {
        return direct.takeIf { it.canExecute() }
    }
    return System.getenv("PATH").orEmpty().split(File.pathSeparator)
        .map { File(it, name) }.firstOrNull { it.canExecute() }
}

// First line of `<exec> <arg>` stdout, trimmed (e.g. `llvm-config --version`/`--libdir`).
fun probe(exec: File, arg: String): String {
    val proc = ProcessBuilder(exec.absolutePath, arg).redirectErrorStream(true).start()
    val out = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    proc.waitFor()
    return out
}

val enableClang = settings.providers.gradleProperty("enableClang").orNull
// `-PllvmConfig=<exec>` is BOTH an opt-in (its presence enables the modules) AND the
// override that points the probe at a specific llvm-config (e.g. a side-by-side install
// or a versioned `llvm-config-22`); without it the probe uses `llvm-config` on PATH.
val llvmConfigProp = settings.providers.gradleProperty("llvmConfig").orNull
val clangEnabled = (enableClang != null && enableClang != "false") || llvmConfigProp != null
if (clangEnabled) {
    val llvmConfig = resolveExecutable(llvmConfigProp ?: "llvm-config")
    val clangxx = resolveExecutable("clang++")
    if (llvmConfig == null || clangxx == null) {
        throw GradleException(
            """
            |The clang modules (:clangwalk, :krapper_parse) are enabled${
                if (llvmConfigProp != null) " (-PllvmConfig=$llvmConfigProp)" else " (-PenableClang)"
            } but the
            |LLVM/Clang toolchain they link against was not found:
            |  - llvm-config: ${
                llvmConfig?.absolutePath
                    ?: (llvmConfigProp?.let { "'$it' is not an executable file" }
                        ?: "not found on PATH")
            }
            |  - clang++:     ${clangxx?.absolutePath ?: "not found on PATH"}
            |
            |Fix one of:
            |  * install LLVM/Clang so `llvm-config` and `clang++` are on PATH; or
            |  * point the build at a specific install:
            |        ./gradlew <task> -PllvmConfig=/path/to/llvm-config
            |  * to build WITHOUT the clang modules, just drop -PenableClang/-PllvmConfig
            |    (the default build excludes them and needs no LLVM).
            """.trimMargin()
        )
    }
    // Discover the toolchain shape ONCE (was hardcoded `LLVM-22` + `/usr/lib` in each
    // module): `--version` -> the major (`-lLLVM-<major>`), `--libdir` -> the `-L<dir>`.
    val llvmMajor = probe(llvmConfig, "--version").substringBefore('.').ifEmpty { "22" }
    val llvmLibDir = probe(llvmConfig, "--libdir").ifEmpty { "/usr/lib" }
    // Soft check: surface the exact missing `-l` target now (a clearer hint than the
    // linker's), but don't hard-fail — distros name the monolithic lib differently and
    // the linker is the real authority on what it can resolve.
    if (!File(llvmLibDir, "libLLVM-$llvmMajor.so").exists() &&
        !File(llvmLibDir, "libclang-cpp.so").exists()
    ) {
        println(
            "WARNING: clang modules expect libLLVM-$llvmMajor.so + libclang-cpp.so under " +
                "$llvmLibDir (from ${llvmConfig.absolutePath} --libdir), but neither was " +
                "found there; the gated link may fail. Override with -PllvmConfig."
        )
    }

    include(":clangwalk")
    // :krapper_parse is the stage1 front-end scaffold (#44 brick 2): it constructs
    // :krapper_model's parse-output model from the same gated libclang-cpp bindings.
    include(":krapper_parse")
    // :cppfixture is the #47 flip-brick-B2 generality demo: a minimal, std-free binding
    // consumer that proves the GENERIC `-Pkpp.frontend.<module>=cpp` path drives a module's
    // own config (not just featuregen's). Gated with the clang modules (it needs krapper_parse
    // for the cpp path); the default build never includes it, so it cannot affect it.
    include(":cppfixture")

    // Thread the discovered toolchain shape to both clang modules (read in their
    // build.gradle.kts as rootProject.extra, with /usr/lib + 22 fallbacks).
    gradle.rootProject {
        extra["llvmMajor"] = llvmMajor
        extra["llvmLibDir"] = llvmLibDir
    }
}
