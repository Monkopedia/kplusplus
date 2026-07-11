plugins {
    // Declared apply-false so the sub-modules can `alias(...)` them (resolves the versions
    // from the catalog once, at the included-build root). The Kotlin plugin is ALSO declared
    // here apply-false so the vanniktech publish plugin can access the Kotlin plugin classes
    // via a shared buildscript classloader (vanniktech requires this — otherwise it fails
    // with "not able to access Kotlin plugin classes").
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.vannik.publish) apply false
    alias(libs.plugins.gradle.plugin.publish) apply false
}

allprojects {
    group = "com.monkopedia.kplusplus"
    version = "0.3.2"
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
