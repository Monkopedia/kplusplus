plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vannik.publish) apply false
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.cli)
        android.set(true)
    }
}

// ---- R1 (#128): publish the whole consumer artifact set to mavenLocal at 0.3.0 ----
//
// One convenience entry point that publishes everything a from-published consumer (and
// k++'s own self-build) needs to resolve from a repo:
//   * com.monkopedia.kplusplus:com.monkopedia.kplusplus.compiler.gradle.plugin (plugin marker)
//   * com.monkopedia.kplusplus:kplusplus-compiler-gradle           (the Gradle plugin jar)
//   * com.monkopedia.kplusplus:kplusplus-compiler-plugin           (the FIR kotlinc plugin jar)
//   * com.monkopedia.kplusplus:krapper_gen:<ver> (classifier linuxX64) (resolve+codegen tool)
//   * com.monkopedia.kplusplus:krapper_parse:<ver> (classifier linuxX64) (LLVM parser tool)
//
// The two JVM plugin jars live in the `compiler` INCLUDED build, so they are published via
// that build's own aggregate task (`:publishCompilerToMavenLocal`). The native tool binaries
// are LLVM-gated modules (:krapper_gen is always present; :krapper_parse only under
// -PenableClang), so their publish tasks are wired lazily.
//
// mavenLocal only — nothing outward.
tasks.register("publishAllToMavenLocal") {
    group = "kplusplus"
    description = "Publish the full kplusplus consumer artifact set to mavenLocal (0.3.0)."
    // The compiler included build's aggregate (Gradle plugin + marker + FIR plugin jar).
    dependsOn(gradle.includedBuild("compiler").task(":publishCompilerToMavenLocal"))
    // krapper_gen is always in the build.
    dependsOn(":krapper_gen:publishReleaseBinaryToMavenLocal")
    // krapper_parse is only in the build under -PenableClang / -PllvmConfig.
    if (findProject(":krapper_parse") != null) {
        dependsOn(":krapper_parse:publishReleaseBinaryToMavenLocal")
    }
    doLast {
        val note = if (findProject(":krapper_parse") == null) {
            "\nNOTE: :krapper_parse was NOT published — it is LLVM-gated. Re-run with " +
                "-PenableClang (or -PllvmConfig=<path>) on an LLVM host to publish the parser."
        } else {
            ""
        }
        println("publishAllToMavenLocal: published the kplusplus consumer set to mavenLocal.$note")
    }
}
