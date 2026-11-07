import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

// #262: the native-runner form of a Gradle test pattern (see the KotlinNativeTest block below).
fun withSimpleNames(patterns: Set<String>) =
    patterns.flatMap { if (it.first().isUpperCase()) listOf(it, "*.$it") else listOf(it) }

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

    // #262: KotlinNativeTest hands `--tests` patterns VERBATIM to test.kexe
    // (`--ktest_gradle_filter`), which matches only the FULLY-QUALIFIED class or test name.
    // Gradle also matches a pattern that starts upper-case against the SIMPLE class name, so
    // `--tests InvalidDeclTest` selected nothing — and since the native runner does the
    // matching, Gradle's no-matching-tests failure never fires: BUILD SUCCESSFUL, zero tests.
    // Fix both halves: add the package-wildcard form Gradle implies, and fail a filtered run
    // that still executes nothing. Unfiltered runs are untouched (CI guards those itself).
    tasks.withType<KotlinNativeTest>().configureEach {
        // Internal API: the `--tests` patterns are only readable on the DefaultTestFilter.
        val testFilter = filter as DefaultTestFilter
        var executed = 0
        addTestListener(
            object : TestListener {
                override fun beforeSuite(suite: TestDescriptor) {}

                override fun afterSuite(
                    suite: TestDescriptor,
                    result: TestResult,
                ) {}

                override fun beforeTest(testDescriptor: TestDescriptor) {}

                override fun afterTest(
                    testDescriptor: TestDescriptor,
                    result: TestResult,
                ) {
                    executed++
                }
            },
        )
        doFirst {
            setTestNameIncludePatterns(withSimpleNames(testFilter.commandLineIncludePatterns))
            testFilter.setIncludePatterns(
                *withSimpleNames(testFilter.includePatterns).toTypedArray(),
            )
            executed = 0
        }
        doLast {
            val patterns = testFilter.includePatterns + testFilter.commandLineIncludePatterns
            if (patterns.isNotEmpty() && executed == 0) {
                throw GradleException("$path executed zero tests for filter $patterns")
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
