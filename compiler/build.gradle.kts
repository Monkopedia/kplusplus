plugins {
    // Declared apply-false so the sub-modules can `alias(...)` them (resolves the versions
    // from the catalog once, at the included-build root). The Kotlin plugin is ALSO declared
    // here apply-false so the vanniktech publish plugin can access the Kotlin plugin classes
    // via a shared buildscript classloader (vanniktech requires this — otherwise it fails
    // with "not able to access Kotlin plugin classes").
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.vannik.publish) apply false
    alias(libs.plugins.gradle.plugin.publish) apply false
    // JetBrains binary-compatibility-validator: an ABI gate on the consumer-facing public
    // surface of the two Central-published JVM modules (the Gradle plugin + its DSL, and the
    // FIR kotlinc-plugin jar). Applied at the included-build root; it wires an `apiCheck` task
    // (run as part of `check`) that diffs the current public API against the committed `.api`
    // dumps. Regenerate the baseline with `apiDump` when the public API changes intentionally.
    alias(libs.plugins.binary.compatibility.validator)
}

// #192: the project version has exactly ONE declaration — `version` in the REPO ROOT's
// gradle.properties — and everything else derives from it. It used to be copied into six
// places (that file, the three module build scripts below, and a `PLUGIN_VERSION` literal in
// plugin source); the 0.3.4 prep bumped five and missed the sixth, which a green build cannot
// detect, and which would have made every upgrading consumer keep running the previous
// release's cached tool binary out of ~/.gradle/kplusplus/tools/<version>/.
//
// `compiler` is a SEPARATE build (not an includeBuild of the root), so it cannot read the
// root's project version — it reads the file. That cross-build reach by relative path is
// already how this build shares the root's version catalog (see settings.gradle.kts).
val kplusplusVersion =
    providers
        .fileContents(layout.projectDirectory.file("../gradle.properties"))
        .asText
        .map { text ->
            java.util.Properties().apply { load(text.reader()) }.getProperty("version")
                ?: error("No `version=` in the repo root gradle.properties")
        }.get()

allprojects {
    group = "com.monkopedia.kplusplus"
    version = kplusplusVersion
}

// Scope the ABI gate to the PUBLISHED modules only. The compiler build's published set is the
// Gradle plugin (`:kplusplus-compiler-gradle`) and the FIR jar (`:kplusplus-compiler-plugin`)
// — see docs/releasing.md. Everything else is not published, so it has no consumer ABI:
//   * the included-build root project (no code, no artifact);
//   * `:kplusplus-compiler-plugin-native` — an internal K/N compiler variant that shares the
//     plugin's sources but is never published as a coordinate.
apiValidation {
    ignoredProjects.addAll(
        listOf(
            rootProject.name,
            "kplusplus-compiler-plugin-native"
        )
    )
    // #185: the Gradle plugin compiles krapper's service protocol from shared source (see
    // compiler/gradle/build.gradle.kts) so both ends of the ksrpc channel come from one
    // definition. Those classes are an INTERNAL wire contract between the plugin and the
    // tool it bundles — never a consumer-facing surface — and their shape is owned by
    // :krapper, so gating them here would just duplicate the churn.
    ignoredPackages.addAll(
        listOf(
            "com.monkopedia.krapper",
            "com.monkopedia.krapper.generator.resolvedmodel"
        )
    )
}

// Aggregate publish for the two consumer-facing JVM artifacts that live in this
// `compiler` build — the Gradle plugin (+ its plugin marker) and the FIR kotlinc plugin jar.
// A convenience wrapper over the per-module publish tasks; the release/local-dry-run path
// (docs/releasing.md) drives `-p compiler publishToMavenLocal -Pkpp.bundleTools.*` directly,
// which also bundles the tool binaries into the plugin jar. mavenLocal only.
tasks.register("publishCompilerToMavenLocal") {
    group = "kplusplus"
    description =
        "Publish the Gradle plugin (+ marker) and the FIR kotlinc-plugin jar to mavenLocal."
    dependsOn(
        ":kplusplus-compiler-gradle:publishToMavenLocal",
        ":kplusplus-compiler-plugin:publishToMavenLocal"
    )
}

// Aggregate publish of the two JVM artifacts to the Central Portal (CI/release only — needs
// the Central Portal token + GPG passphrase from the environment). The Gradle plugin is ALSO
// published to the Gradle Plugin Portal via `:kplusplus-compiler-gradle:publishPlugins`; the
// release workflow drives both. mavenCentral aggregate here covers Central only.
tasks.register("publishCompilerToMavenCentral") {
    group = "kplusplus"
    description =
        "Publish the Gradle plugin (+ marker) and the FIR kotlinc-plugin jar to the " +
            "Central Portal (release/CI — requires credentials + signing in the environment)."
    dependsOn(
        ":kplusplus-compiler-gradle:publishToMavenCentral",
        ":kplusplus-compiler-plugin:publishToMavenCentral"
    )
}

// Publish the Gradle plugin (+ marker) to the Gradle Plugin Portal. Separate from the
// Central path — driven by the release workflow with the Portal key/secret in the
// environment (see docs/releasing.md). The `com.gradle.plugin-publish` plugin owns this.
tasks.register("publishPluginToPortal") {
    group = "kplusplus"
    description =
        "Publish the com.monkopedia.kplusplus.compiler Gradle plugin to the Gradle Plugin " +
            "Portal (release/CI — requires GRADLE_PUBLISH_KEY/GRADLE_PUBLISH_SECRET)."
    dependsOn(":kplusplus-compiler-gradle:publishPlugins")
}
