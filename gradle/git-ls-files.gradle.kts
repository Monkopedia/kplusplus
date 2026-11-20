// #231: the one `git ls-files` the repo-wide self-checks share — verifyWorkflowPaths
// (gradle/workflow-paths.gradle.kts, applied by both the root and the compiler build) and
// verifyCatalogPins (compiler/gradle/build.gradle.kts). Its own script rather than a function in
// workflow-paths.gradle.kts, because a script plugin can export a value only through `extra`, and
// a subproject reading its root project's `extra` silently depends on that project having applied
// the other script first. Applied here, each consumer names what it uses.
//
// Applied with `apply(from = …)`, it sets on the project
//   extra["gitLsFiles"]: (repoRoot: File, pathspecs: List<String>) -> Provider<List<String>>
// — the repo-root-relative paths of every file a PR could contain that matches the pathspecs
// (all of them for none): the tracked ones, plus untracked files not ignored by .gitignore, so a
// local run also checks a new file before it is `git add`ed. With no checkout it fails naming
// the cause and carrying git's own message, rather than Gradle's bare "exit value 128".
val gitLsFiles: (File, List<String>) -> Provider<List<String>> = { repoRoot, pathspecs ->
    val git =
        providers.exec {
            commandLine(
                listOf("git", "-C", repoRoot.path, "ls-files", "--cached", "--others") +
                    listOf("--exclude-standard", "--") + pathspecs
            )
            isIgnoreExitValue = true
        }
    git.result.map { result ->
        if (result.exitValue != 0) {
            val noGit =
                if (repoRoot.resolve(".git").exists()) "" else " (no .git found at $repoRoot)"
            throw GradleException(
                "This check needs a git checkout$noGit: `git ls-files` exited " +
                    "${result.exitValue}:\n${git.standardError.asText.get().trim()}"
            )
        }
        git.standardOutput.asText.get().lines().filter(String::isNotEmpty).distinct()
    }
}
extra["gitLsFiles"] = gitLsFiles
