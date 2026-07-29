plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    // Declared here (never applied) purely to put the kplusplus plugin on the ROOT buildscript
    // classpath, next to the Kotlin plugin. Its version comes from settings.gradle.kts's
    // pluginManagement pin, exactly as in the modules that really apply it.
    //
    // WHY this is load-bearing, not tidiness: since 0.3.4 the plugin drives krapper over ksrpc,
    // so its runtime needs kotlinx-coroutines 1.11 / ktor-io 3.5 in the Gradle daemon. Gradle
    // gives each project's buildscript its OWN classloader whose PARENT is the root's, and
    // class loading is parent-first. With the Kotlin plugin declared only here, the root
    // classloader owns kotlin-compiler-runner's kotlinx-coroutines 1.8.0; a module that also
    // applies the kplusplus plugin resolves coroutines 1.11.0 into its CHILD classloader, but
    // `kotlinx.coroutines.Job` still loads from the parent at 1.8.0. ktor-io then calls
    // `Job.invokeOnCompletion$default`, which 1.9+ moved onto the interface itself
    // (-Xjvm-default=all) and 1.8.0 keeps in `Job$DefaultImpls`, so kplusplusSync dies with a
    // NoSuchMethodError before krapper is even started. Declaring both plugins in the SAME
    // (root) block puts them in ONE classpath, where Gradle's conflict resolution picks
    // coroutines 1.11.0 and the parent-first lookup finds the right class.
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
