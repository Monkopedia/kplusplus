plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    // 0.3.4-ONLY WORKAROUND (#194) — DELETE THIS LINE with the pluginManagement bump to 0.3.5.
    // Declared here (never applied) purely to put the kplusplus plugin on the ROOT buildscript
    // classpath next to the Kotlin plugin, so both resolve as ONE classpath. 0.3.4's plugin
    // carried its ksrpc/ktor/coroutines runtime as ordinary dependencies, which a multi-project
    // consumer resolves into a CHILD classloader while parent-first loading keeps serving the
    // ROOT's older kotlinx-coroutines — `NoSuchMethodError: Job.invokeOnCompletion$default`
    // before krapper is even started. 0.3.5 SHADES that runtime into the plugin jar (see
    // compiler/gradle/build.gradle.kts), which is the actual fix; this line is only what keeps
    // THIS repo building while it self-hosts one release behind, on 0.3.4.
    id("com.monkopedia.kplusplus.compiler") apply false
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
        // Exclude generated Kotlin from ktlint.  Three generators put output on a source
        // set of a module we DO lint:
        //   • <module>/krapped/src/          — kplusplus bindings, committed seed (default
        //                                      / libclang path)
        //   • build/krapped-cpp/src/         — kplusplus bindings, cpp front-end output
        //                                      (kpp.frontend.<mod>=cpp)
        //   • build/gen/klinker/*/kotlin/    — klinker's `klinker_main.kt` entry-point
        //                                      shim, added to the default source set of
        //                                      every klinked module (:krapper, :clangwalk,
        //                                      samples).  It is upstream-owned and would
        //                                      regenerate, so it must be skipped, not fixed.
        // Linting machine-generated code produces spurious violations that drown real
        // hand-written source findings, so we skip all three trees here.
        filter {
            exclude { element ->
                val path = element.file.absolutePath
                path.contains("/krapped/") ||
                    path.contains("/krapped-cpp/") ||
                    path.contains("/build/gen/klinker/")
            }
        }
    }

    // klinker registers its generated directory ON the module's default source set, so the
    // ktlint source-set tasks read another task's OUTPUT.  Gradle rejects that as an
    // undeclared implicit dependency and fails the ktlint task whenever codegen is in the
    // same build (e.g. `check`).  Filtering the files out (above) does not help: the
    // *directory* is still an input of the task.  ktlint must not DEPEND on codegen — that
    // would make linting drag the generator in — so declare ordering only.  klinker's task
    // is matched by name (`generateKotlinSource<binary>`) since its plugin is not on the
    // root build's classpath; matching a task that is not klinker's would only order it.
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
        mustRunAfter(provider { tasks.matching { it.name.startsWith("generateKotlinSource") } })
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
