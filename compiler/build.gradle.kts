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
    // Gives this root a `check` lifecycle, so `-p compiler check` runs verifyCompilerCheckPaths.
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

// #230: compiler-check.yml only runs on PRs touching its `pull_request.paths`, and a list that
// omits a file does not fail — it just does not fire, so a gate here is silently filtered off
// the very PR that could trip it. Make the list report its own gaps: every repo file this
// `check` run reads from OUTSIDE `compiler/**` must be matched by some entry. `compiler/**`
// itself always triggers the job, so a new read wired in here fails on the PR that adds it.
//
// What counts as "read", discovered rather than listed wherever Gradle can see it:
//   * the declared input files of every task in this run's graph (compile sources, the
//     `verify*` tasks' `inputs.files` — a gate that reads a repo file must declare it);
//   * every Test system property whose value is an existing file (BindingIndexCoverageTest's
//     featuregen denominator reaches its test that way, not as a declared input);
//   * what verify.sh reads (the step before `check`): every file under
//     .github/test-evidence-gate and .github/workflows, listed from disk;
//   * the build's own entry points, read before any task exists. These four are the one
//     hand-kept part: the wrapper, and the two root files compiler/*.gradle.kts load by path.
val verifyCompilerCheckPaths =
    tasks.register("verifyCompilerCheckPaths") {
        group = "verification"
        description =
            "Check every repo file `-p compiler check` reads outside compiler/ is matched by " +
                "compiler-check.yml's pull_request.paths."
        val repoRoot = layout.projectDirectory.asFile.parentFile
        val workflow = repoRoot.resolve(".github/workflows/compiler-check.yml")
        fun rel(files: Iterable<File>) =
            files
                .map { it.canonicalFile.relativeTo(repoRoot.canonicalFile).invariantSeparatorsPath }
                .filter { !it.startsWith("../") && !it.startsWith("compiler/") }
        val fromGraph = sortedSetOf<String>()
        gradle.taskGraph.whenReady {
            if (!hasTask(this@register)) return@whenReady
            fromGraph +=
                rel(
                    allTasks.flatMap { t ->
                        t.inputs.files.files +
                            ((t as? Test)?.systemProperties?.values.orEmpty())
                                .map { file(it.toString()) }.filter { it.isFile }
                    }
                )
        }
        doLast {
            // GitHub filter-pattern semantics for the subset used: `**` crosses `/` (and `**/`
            // may match no directory at all), `*` does not. GitHub's `?`/`+`/`[]`/`!` mean
            // something else from shell globs, so refuse them rather than guess.
            fun glob(p: String): Regex {
                check(p.none { it in "?+[]!" }) { "Unsupported filter syntax `$p`; extend glob()." }
                return Regex(
                    Regex("""\*\*/|\*\*|\*|[^*]+""").findAll(p).joinToString("") {
                        when (it.value) {
                            "**/" -> "(?:.*/)?"
                            "**" -> ".*"
                            "*" -> "[^/]*"
                            else -> Regex.escape(it.value)
                        }
                    }
                )
            }
            check(glob("a/**").matches("a/b/c") && !glob("a/*").matches("a/b/c")) { "glob() broke" }
            // The `paths:` block under `pull_request:`: its `- '…'` items, comments skipped.
            val lines = workflow.readLines()
            val paths =
                lines.drop(lines.indexOfFirst { it.trim() == "pull_request:" })
                    .dropWhile { it.trim() != "paths:" }.drop(1)
                    .map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
                    .takeWhile { it.startsWith("- ") }
                    .map { it.removePrefix("- ").substringBefore(" #").trim().trim('\'', '"') }
            check(paths.isNotEmpty()) { "Parsed no pull_request.paths out of $workflow." }
            // Vacuity guard: run on its own (not as part of `check`) the graph reads nothing.
            check(fromGraph.isNotEmpty()) {
                "No task in this run reads outside compiler/ — run this as part of `check`."
            }
            val read =
                fromGraph +
                    rel(
                        listOf(".github/test-evidence-gate", ".github/workflows")
                            .flatMap { repoRoot.resolve(it).walk().filter(File::isFile).toList() } +
                            listOf(
                                "gradlew",
                                "gradle/wrapper/gradle-wrapper.properties",
                                "gradle/libs.versions.toml",
                                "gradle.properties",
                            ).map(repoRoot::resolve)
                    )
            val globs = paths.map(::glob)
            val missed = read.filter { f -> globs.none { it.matches(f) } }
            check(missed.isEmpty()) {
                "`-p compiler check` reads these files, but compiler-check.yml's " +
                    "pull_request.paths matches none of them, so a PR changing only them " +
                    "skips this job (#230):\n" +
                    missed.joinToString("\n") { "  $it" } + "\nAdd them (or a glob) to $workflow."
            }
            logger.lifecycle(
                "compiler-check.yml paths cover all ${read.size} file(s) read outside compiler/."
            )
        }
    }

tasks.check {
    dependsOn(verifyCompilerCheckPaths)
}
