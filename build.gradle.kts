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
        // Exclude generated binding output from ktlint.  The kplusplus compiler plugin
        // places generated Kotlin bindings in two possible directories:
        //   • <module>/krapped/src/   — the committed seed (default / libclang path)
        //   • build/krapped-cpp/src/  — the cpp front-end output (kpp.frontend.<mod>=cpp)
        // Linting machine-generated code produces thousands of spurious violations that
        // drown real hand-written source findings, so we skip both trees here.
        filter {
            exclude { element ->
                val path = element.file.absolutePath
                path.contains("/krapped/") || path.contains("/krapped-cpp/")
            }
        }
    }
}

// Publishing is driven from the `compiler` included build (see docs/releasing.md): the
// release BUNDLES the `krapper` tool binary INTO the plugin jar (0.4.0 model, #139/#140) and
// ships only the three compiler coordinates via
// `-p compiler publishToMavenCentral -Pkpp.bundleTools.krapper=…` — there are no standalone
// tool artifacts to publish. The former `publishAllToMavenLocal`/`publishAllToMavenCentral`
// aggregate tasks (which published the now-deleted standalone tool classified artifacts, and
// referenced a `compiler` included build that the flat build no longer wires) were removed
// with those artifacts.
