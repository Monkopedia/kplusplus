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
    // #234: the repo's style gate. The root build applies it to its own subprojects, but this is
    // a separate build, so root tasks never reach it; applied to every project here (below).
    alias(libs.plugins.ktlint) apply false
    // Gives this root a `check` lifecycle, so `-p compiler check` runs verifyWorkflowPaths.
    base
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
            java.util
                .Properties()
                .apply { load(text.reader()) }
                .getProperty("version")
                ?: error("No `version=` in the repo root gradle.properties")
        }.get()

allprojects {
    group = "com.monkopedia.kplusplus"
    version = kplusplusVersion
}

// #234: lint this build with the same plugin, ktlint-cli version and repo-root .editorconfig as
// the root build (build.gradle.kts), so `-p compiler check` runs `ktlintCheck` on every module.
// Lint only the sources this build owns. The rest belong to someone else or are regenerated:
//   * build/generated/ — `generatePluginVersion`'s output (compiler/gradle);
//   * krapper/ and krapper_model/ — shared source dirs compiled into compiler/gradle, owned
//     and already linted by the root build (`:krapper`, `:krapper_model`);
//   * compiler/native's main source set — a gitignored copy of compiler/plugin's sources made
//     by `syncSource`, linted where it is owned; its tasks are disabled in that module.
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.cli)
        android.set(true)
        filter {
            exclude { element ->
                val path = element.file.invariantSeparatorsPath
                "/build/generated/" in path ||
                    "/krapper/src/" in path ||
                    "/krapper_model/src/" in path
            }
        }
    }
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
            "kplusplus-compiler-plugin-native",
        ),
    )
    // #185: the Gradle plugin compiles krapper's service protocol from shared source (see
    // compiler/gradle/build.gradle.kts) so both ends of the ksrpc channel come from one
    // definition. Those classes are an INTERNAL wire contract between the plugin and the
    // tool it bundles — never a consumer-facing surface — and their shape is owned by
    // :krapper, so gating them here would just duplicate the churn.
    ignoredPackages.addAll(
        listOf(
            "com.monkopedia.krapper",
            "com.monkopedia.krapper.generator.resolvedmodel",
        ),
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
        ":kplusplus-compiler-plugin:publishToMavenLocal",
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
        ":kplusplus-compiler-plugin:publishToMavenCentral",
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

// #230: compiler-check.yml only runs on PRs touching its `pull_request.paths`, so every repo
// file `-p compiler check` reads must be matched there — see gradle/workflow-paths.gradle.kts,
// shared with the root build's root-build.yml check (#231). On top of what it discovers, this
// run also executes verify.sh (the step before `check`), which reads every file under
// .github/test-evidence-gate and .github/workflows, and the workflow's v8-configure job reads
// samples/v8's build configuration (#306).
//
// #237: root-build.yml also runs this build, for `:kplusplus-compiler-plugin:bindingIndexGate`,
// and passes `-PworkflowPaths.workflow=root-build.yml` so that invocation's reads are checked
// against the workflow that runs it. Its steps call only gate.sh, already in that list.
val workflowName =
    providers.gradleProperty("workflowPaths.workflow").getOrElse("compiler-check.yml")
extra["workflowPaths.workflow"] = rootDir.resolve("../.github/workflows/$workflowName")
extra["workflowPaths.alsoRead"] =
    if (workflowName == "compiler-check.yml") {
        listOf(
            ".github/test-evidence-gate",
            ".github/workflows",
            "samples/v8/settings.gradle.kts",
            "samples/v8/build.gradle.kts",
            "samples/v8/gradle.properties",
        )
    } else {
        emptyList()
    }
apply(from = "../gradle/workflow-paths.gradle.kts")

// #231: README.md and docs/ARCHITECTURE.md each carry a module table, and both silently missed
// samples/multiproject when it landed. Derive the module set instead of trusting either table:
// every root-build `include(":…")`, this build, and every samples/<dir> with its own settings
// script must be named (as `<module>/`) in both. It runs here because this is the cheapest job,
// and the sample settings scripts are declared inputs, so verifyWorkflowPaths forces
// compiler-check.yml to fire on the PR that adds a sample.
val verifyModuleDocs =
    tasks.register("verifyModuleDocs") {
        group = "verification"
        description =
            "Check every module and sample is named in README.md and docs/ARCHITECTURE.md."
        val repoRoot = rootDir.parentFile
        val settings = repoRoot.resolve("settings.gradle.kts")
        val samples = fileTree(repoRoot.resolve("samples")) { include("*/settings.gradle.kts") }
        val docs = listOf("README.md", "docs/ARCHITECTURE.md").map(repoRoot::resolve)
        inputs.files(settings, samples, docs)
        doLast {
            // Every argument of every `include(...)`, so `include(":a", ":b")` counts both.
            val included =
                Regex("""include\(([^)]*)\)""")
                    .findAll(settings.readText())
                    .flatMap { Regex("\":([^\"]+)\"").findAll(it.groupValues[1]) }
                    .map { it.groupValues[1] }
                    .toList()
            val sampleDirs = samples.files.map { "samples/${it.parentFile.name}" }
            check(included.isNotEmpty() && sampleDirs.isNotEmpty()) {
                "Discovered includes $included and samples $sampleDirs — the discovery broke."
            }
            val modules = included + "compiler" + sampleDirs
            val missing =
                docs.flatMap { doc ->
                    val text = doc.readText()
                    modules
                        .filter { "`$it/`" !in text }
                        .map { "  ${doc.relativeTo(repoRoot)}: `$it/`" }
                }
            check(missing.isEmpty()) {
                "These modules exist but their docs do not name them:\n" +
                    missing.joinToString("\n")
            }
        }
    }

tasks.check {
    dependsOn(verifyModuleDocs)
    // Finalized, not a dependency: it must run after every requested `check` (see
    // gradle/workflow-paths.gradle.kts), which a dependency of this one would make a cycle.
    finalizedBy("verifyWorkflowPaths")
}
