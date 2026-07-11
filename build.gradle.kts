plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
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

// Publishing is driven from the `compiler` included build (see docs/releasing.md): the
// release BUNDLES the krapper_gen/krapper_parse tool binaries INTO the plugin jar
// (0.4.0 model, #139/#140) and ships only the three compiler coordinates via
// `-p compiler publishToMavenCentral -Pkpp.bundleTools.*` — there are no standalone tool
// artifacts to publish. The former `publishAllToMavenLocal`/`publishAllToMavenCentral`
// aggregate tasks (which published the now-deleted standalone krapper_gen/krapper_parse
// classified artifacts, and referenced a `compiler` included build that the flat build
// no longer wires) were removed with those artifacts.
