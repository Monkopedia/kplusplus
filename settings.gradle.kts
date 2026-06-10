rootProject.name = "kplusplus"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("compiler")

include(":testlib_kotlin")
include(":testlib_kotlin_manual")
include(":krapper_gen")
include(":krapper_model")
include(":plugin")
include(":slice")
include(":feature-tests")
include(":featuregen")

// :clangwalk is the stage1 self-bootstrap harness — it binds Clang's own C++ AST
// (libclang-cpp) and walks a real AST on the generated bindings. It REQUIRES an LLVM
// toolchain on the box (system clang++, libclang-cpp, LLVM-22) that an ordinary build
// host won't have, so it is OFF by default and never configured on an LLVM-absent box.
// Opt in with `-PenableClang` (or set `enableClang=true`); the probe also auto-includes
// it when an explicit LLVM is requested via the `llvmConfig` property pointing at a real
// llvm-config. Default OFF keeps `./gradlew` green everywhere.
val enableClang = settings.providers.gradleProperty("enableClang").orNull
val clangEnabled = (enableClang != null && enableClang != "false") ||
    settings.providers.gradleProperty("llvmConfig").orNull?.let { File(it).canExecute() } == true
if (clangEnabled) {
    include(":clangwalk")
}
