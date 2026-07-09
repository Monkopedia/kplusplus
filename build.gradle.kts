plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    // The native-binary modules (:krapper_gen, :krapper_parse) apply the vanniktech BASE
    // plugin to add the Central Portal upload + signing to their hand-rolled artifact-only
    // publications. Declared apply-false here so their `alias(...)` resolves the version once.
    alias(libs.plugins.vannik.publish.base) apply false
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
        val note =
            if (findProject(":krapper_parse") == null) {
                "\nNOTE: :krapper_parse was NOT published — it is LLVM-gated. Re-run with " +
                    "-PenableClang (or -PllvmConfig=<path>) on an LLVM host to publish the parser."
            } else {
                ""
            }
        println("publishAllToMavenLocal: published the kplusplus consumer set to mavenLocal.$note")
    }
}

// ---- Outward release: publish the whole set to the Central Portal (CI/release ONLY) ----
//
// The Central-bound half of the artifact set. Requires the Central Portal token + the GPG
// signing key/passphrase in the environment (see docs/releasing.md + .github/workflows/
// release.yml). The Gradle plugin ALSO goes to the Gradle Plugin Portal via
// `:kplusplus-compiler-gradle:publishPlugins` — driven separately by the release workflow.
//
// This targets the per-publication Central tasks so ONLY the release artifacts ship (the
// stage0 mavenLocal anchor is deliberately excluded). :krapper_parse is LLVM-gated, so on a
// non-LLVM host it is absent and the parser must be released from an LLVM host.
tasks.register("publishAllToMavenCentral") {
    group = "kplusplus"
    description =
        "Publish the kplusplus release artifact set to the Central Portal (CI/release — " +
        "requires Central Portal credentials + GPG signing in the environment)."
    dependsOn(gradle.includedBuild("compiler").task(":publishCompilerToMavenCentral"))
    // vanniktech's per-module `publishToMavenCentral` uploads that module's Portal bundle;
    // the unwanted KMP/stage0 publications are disabled for Central in each build file, so
    // each bundle carries only its Central-valid release artifact(s).
    dependsOn(":krapper_gen:publishToMavenCentral")
    if (findProject(":krapper_parse") != null) {
        dependsOn(":krapper_parse:publishToMavenCentral")
    }
    doLast {
        val note =
            if (findProject(":krapper_parse") == null) {
                "\nWARNING: :krapper_parse was NOT published — it is LLVM-gated. Release the " +
                    "parser from an LLVM host with -PenableClang (or -PllvmConfig=<path>)."
            } else {
                ""
            }
        println("publishAllToMavenCentral: uploaded the kplusplus release set to Central.$note")
    }
}
