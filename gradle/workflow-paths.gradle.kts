// #230, #231: a workflow's `pull_request.paths` is a hand-kept list, and a list that omits a
// file does not fail — it just does not fire, so a gate is silently filtered off the very PR
// that could trip it. This makes the list report its own gaps: every repo file the Gradle run
// reads must be matched by some entry of the workflow that runs it.
//
// Applied with `apply(from = …)` by compiler/build.gradle.kts (compiler-check.yml) and the root
// build.gradle.kts (root-build.yml), after setting:
//   extra["workflowPaths.workflow"] — that workflow's File (the repo root is its grandparent's
//                                    parent: `<root>/.github/workflows/<name>.yml`);
//   extra["workflowPaths.alsoRead"] — repo-root-relative files/dirs the run reads that no task
//                                    declares (what the workflow's own steps run, e.g. gate.sh).
// It registers `verifyWorkflowPaths`, which must run in the SAME invocation as the tasks it
// covers, since it reads their task graph, and AFTER them: request it by name beside them
// (root-build.yml), or finalize the aggregate with it (compiler's `check`).
//
// What counts as "read", discovered rather than listed wherever Gradle can see it:
//   * the declared input files of every task in this run's graph (compile sources, a gate's
//     `inputs.files` — a task that reads a repo file must declare it, which is also what keeps
//     it from going UP-TO-DATE when that file changes);
//   * every Test system property whose value is an existing file;
//   * every build script and the settings script of this build;
//   * the entry points every build here shares — the root wrapper, catalog and gradle.properties
//     — plus this file and the workflow itself.
// A file that changes the build but appears in none of these (a test reading a path it never
// declares) stays invisible: declare it.
val workflow = (extra["workflowPaths.workflow"] as File).canonicalFile
val repoRoot = workflow.parentFile.parentFile.parentFile
val workflowPath = workflow.relativeTo(repoRoot).invariantSeparatorsPath
check(workflowPath.startsWith(".github/workflows/")) { "$workflow is not a workflow file." }
@Suppress("UNCHECKED_CAST")
val alsoRead = extra["workflowPaths.alsoRead"] as List<String>

tasks.register("verifyWorkflowPaths") {
    group = "verification"
    description =
        "Check every tracked file this run reads is matched by $workflowPath's pull_request.paths."
    // Only a tracked file can be in a PR's diff, so only tracked files need a trigger: build
    // outputs and generated sources do not, wherever they land.
    val tracked =
        providers.exec { commandLine("git", "-C", repoRoot.path, "ls-files") }
            .standardOutput.asText.map { it.lines().toSet() }
    fun rel(files: Iterable<File>) =
        files.map { it.canonicalFile.relativeTo(repoRoot).invariantSeparatorsPath }
    fun reads(t: Task) =
        t.inputs.files.files +
            ((t as? Test)?.systemProperties?.values.orEmpty())
                .map { file(it.toString()) }.filter { it.isFile }
    val fromGraph = sortedSetOf<String>()
    // Some inputs are mapped from another task's output and cannot be resolved before it runs
    // (K/N's distribution download, for one). Those tasks are read again once they have run, so
    // when this task is requested by name it must run after the tasks requested with it.
    val deferred = mutableListOf<Task>()
    val requested = gradle.startParameter.taskNames
    val self = this
    mustRunAfter(
        provider {
            if (requested.none { it == name || it == path }) return@provider emptyList<Task>()
            requested.flatMap { n ->
                if (n.startsWith(":")) {
                    listOfNotNull(rootProject.tasks.findByPath(n))
                } else {
                    rootProject.allprojects.mapNotNull { it.tasks.findByName(n) }
                }
            } - self
        }
    )
    gradle.taskGraph.whenReady {
        if (!hasTask(this@register)) return@whenReady
        for (t in allTasks) {
            try {
                fromGraph += rel(reads(t))
            } catch (e: Exception) {
                deferred += t
            }
        }
    }
    doLast {
        val unrun = deferred.filterNot { it.state.executed }
        check(unrun.isEmpty()) {
            "Cannot read the inputs of ${unrun.map { it.path }} before they run; " +
                "request them on the same command line as $path, so it can run after them."
        }
        deferred.forEach { fromGraph += rel(reads(it)) }
        // GitHub filter-pattern semantics for the subset used: `**` crosses `/` (and `**/` may
        // match no directory at all), `*` does not. GitHub's `?`/`+`/`[]`/`!` mean something
        // else from shell globs, so refuse them rather than guess.
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
        // Vacuity guard: run on its own, the graph is just this task and reads nothing.
        check(fromGraph.any(tracked.get()::contains)) {
            "No task in this run reads a repo file — run this with the tasks $workflowPath runs."
        }
        val read =
            (fromGraph +
                rel(
                    (
                        alsoRead + workflowPath + "gradle/workflow-paths.gradle.kts" + "gradlew" +
                            "gradle/wrapper/gradle-wrapper.properties" +
                            "gradle/libs.versions.toml" + "gradle.properties"
                    ).flatMap { repoRoot.resolve(it).walk().filter(File::isFile).toList() } +
                        rootProject.allprojects.map { it.buildFile }.filter(File::isFile) +
                        rootDir.resolve("settings.gradle.kts")
                )).filter(tracked.get()::contains)
        val globs = paths.map(::glob)
        val missed = read.filter { f -> globs.none { it.matches(f) } }
        check(missed.isEmpty()) {
            "This run reads these files, but $workflowPath's pull_request.paths matches none " +
                "of them, so a PR changing only them skips the workflow (#230, #231):\n" +
                missed.joinToString("\n") { "  $it" } + "\nAdd them (or a glob) to $workflow."
        }
        logger.lifecycle("$workflowPath paths cover all ${read.size} file(s) this run reads.")
    }
}
