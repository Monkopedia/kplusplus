rootProject.name = "kplusplus"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    // FLAT build (no composite): the repo consumes the PUBLISHED kplusplus plugin rather than
    // includeBuild("compiler"). A composite substitutes any com.monkopedia.kplusplus:* module
    // dependency with the matching in-build project, which would shadow our own coordinates and
    // block :krapper from resolving a published tool; flat has no such substitution. The tool
    // modules (krapper/krapper_model) stay siblings here, so their dev loop is still immediate;
    // PLUGIN development happens in an example (samples/*) that includeBuilds compiler. Bump
    // this version on each release.
    plugins {
        id("com.monkopedia.kplusplus.compiler") version "0.3.3"
    }
}

include(":krapper_model")
include(":feature-tests")
include(":featuregen")

// :clangwalk binds Clang's own C++ AST (libclang-cpp) and walks a real AST on the generated
// bindings. :krapper is THE TOOL — it parses headers with the Clang C++ AST and generates the
// bindings in one binary (#184); :cppfixture is the generic cpp-path demo. All three link
// against the box's LLVM toolchain (system clang++, libclang-cpp, libLLVM).
//
// LLVM IS A HARD REQUIREMENT: :krapper (-Pkpp.frontend.<module>=cpp) is the only way to PARSE a
// header into bindings, so these modules are ALWAYS included and the LLVM toolchain is probed
// UNCONDITIONALLY — an absent/mis-pointed toolchain fails right here at settings-eval with an
// actionable message. Full project, or a clear error; never a silent partial build.
//
// `-PllvmConfig=<path-to-llvm-config>` is ONLY a LOCATION OVERRIDE (point the probe at a
// specific/side-by-side install); its ABSENCE means "use `llvm-config` on PATH".
//
// The toolchain is PROBED ONCE here and the discovered values (major version + libdir) are
// threaded to all three modules via rootProject extras, so an LLVM major bump (22 -> 23) or a
// non-/usr/lib install needs NO per-module edits, and an absent/mis-pointed toolchain fails
// here with an ACTIONABLE message instead of a cryptic `cannot find -lLLVM-22` linker error
// ~8 minutes into a link.

// Look up an executable: an absolute/relative path is taken verbatim; a bare name is
// searched on PATH. Returns null when nothing executable is found.
fun resolveExecutable(name: String): File? {
    val direct = File(name)
    if (direct.path.contains(File.separatorChar)) {
        return direct.takeIf { it.canExecute() }
    }
    return System
        .getenv("PATH")
        .orEmpty()
        .split(File.pathSeparator)
        .map { File(it, name) }
        .firstOrNull { it.canExecute() }
}

// First line of `<exec> <arg>` stdout, trimmed (e.g. `llvm-config --version`/`--libdir`).
fun probe(
    exec: File,
    arg: String,
): String {
    val proc = ProcessBuilder(exec.absolutePath, arg).redirectErrorStream(true).start()
    val out =
        proc.inputStream
            .readBytes()
            .toString(Charsets.UTF_8)
            .trim()
    proc.waitFor()
    return out
}

// `-PllvmConfig=<exec>` is ONLY a LOCATION OVERRIDE: it points the probe at a specific
// llvm-config (e.g. a side-by-side install or a versioned `llvm-config-22`). Its ABSENCE
// means "use `llvm-config` on PATH".
val llvmConfigProp = settings.providers.gradleProperty("llvmConfig").orNull

// LLVM is a HARD REQUIREMENT: probe the toolchain UNCONDITIONALLY and fail fast here if it
// is absent. (Top-level script `val`s do not smart-cast across the throw, so bind a non-null
// `llvmConfig` local afterwards.)
val llvmConfigProbe = resolveExecutable(llvmConfigProp ?: "llvm-config")
val clangxx = resolveExecutable("clang++")
if (llvmConfigProbe == null || clangxx == null) {
    throw GradleException(
        """
        |LLVM is REQUIRED to build kplusplus, but the LLVM/Clang toolchain was not found:
        |  - llvm-config: ${
            llvmConfigProbe?.absolutePath
                ?: (
                    llvmConfigProp?.let { "'$it' is not an executable file" }
                        ?: "not found on PATH"
                )
        }
        |  - clang++:     ${clangxx?.absolutePath ?: "not found on PATH"}
        |
        |Fix one of:
        |  * install LLVM 22 so `llvm-config` and `clang++` are on PATH; or
        |  * point the build at a specific install:
        |        ./gradlew <task> -PllvmConfig=/path/to/llvm-config
        """.trimMargin(),
    )
}
val llvmConfig: File = llvmConfigProbe!!
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
            "found there; the gated link may fail. Override with -PllvmConfig.",
    )
}

include(":clangwalk")
// :krapper is the LLVM-linked tool: it constructs :krapper_model's parse-output model from the
// libclang-cpp bindings and resolves + generates the Kotlin bindings from it, in one process.
include(":krapper")
// :cppfixture is the generality demo: a minimal, std-free binding consumer that proves the
// GENERIC `-Pkpp.frontend.<module>=cpp` path drives a module's own config (not just
// featuregen's).
include(":cppfixture")

// Thread the discovered toolchain shape to all three clang modules (read in their
// build.gradle.kts as rootProject.extra, with /usr/lib + 22 fallbacks).
gradle.rootProject {
    extra["llvmMajor"] = llvmMajor
    extra["llvmLibDir"] = llvmLibDir
}
