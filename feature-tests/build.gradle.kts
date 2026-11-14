import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

val cppSrcDir = layout.projectDirectory.dir("src/cppMain")
val cppBuildDir = layout.buildDirectory.dir("cpp")
val cppObjDir = cppBuildDir.map { it.dir("obj") }
val cppLibDir = cppBuildDir.map { it.dir("lib") }
val cppLib = cppLibDir.map { it.file("libfeature_tests.a") }

val compileFeatureTestsCpp by tasks.registering {
    group = "build"
    description = "Compile all feature-tests C++ rows into libfeature_tests.a"
    val sources = fileTree(cppSrcDir.dir("rows")) { include("**/*.cc") }
    val includeDir = cppSrcDir.dir("include").asFile
    inputs.files(sources)
    inputs.dir(includeDir)
    outputs.file(cppLib)
    doLast {
        val objDir = cppObjDir.get().asFile.also { it.mkdirs() }
        val libFile = cppLib.get().asFile.also { it.parentFile.mkdirs() }
        val objs =
            sources.files.map { src ->
                val obj = objDir.resolve(src.nameWithoutExtension + ".o")
                val cmd =
                    listOf(
                        "clang++",
                        "-std=c++17",
                        "-fPIC",
                        "-c",
                        "-I${includeDir.absolutePath}",
                        src.absolutePath,
                        "-o",
                        obj.absolutePath,
                    )
                val rc = ProcessBuilder(cmd).inheritIO().start().waitFor()
                if (rc != 0) throw GradleException("clang++ failed on $src (rc=$rc)")
                obj.absolutePath
            }
        // Recreate the archive each run so removed rows drop out.
        if (libFile.exists()) libFile.delete()
        val arCmd = listOf("ar", "rcs", libFile.absolutePath) + objs
        val rc = ProcessBuilder(arCmd).inheritIO().start().waitFor()
        if (rc != 0) throw GradleException("ar failed (rc=$rc)")
    }
}

kotlin {
    linuxX64("native") {
        compilations.getByName("main") {
            cinterops {
                create("feature_tests") {
                    defFile(project.file("src/nativeInterop/cinterop/feature_tests.def"))
                    includeDirs(cppSrcDir.dir("include"))
                }
            }
        }
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
        binaries {
            // No executable — tests only.
        }
    }
}

tasks.matching { it.name.startsWith("cinteropFeature_testsNative") }.configureEach {
    dependsOn(compileFeatureTestsCpp)
}

// ---------------------------------------------------------------------------
// matrixReport: write each row's Status into docs/features.md from THIS run's test results,
// and FAIL whenever the matrix and the tests disagree (#223), so the matrix's "Status is set
// by the test harness, not by hand" promise is real.
//
// Harness-statused rows are those of a table with a `Test cases` column (the contract
// tables: each bullet is an @Test). A row's tests are the `FooTest` classes its cells cite
// in backticks, else the class derived from its id (PR-int-rt -> PrIntRtTest). Tables
// without `Test cases` are statused by hand; their rows' citations only count towards
// which test classes have a row.
//
// Verdicts (stampMatrix):
//   * every test passed            -> 🟢 (a hand-set 🟡 "documented workaround" is kept)
//   * any failure/error            -> 🔴, and the task FAILS — for ANY failing class, row
//                                     or not, since the harness tests run with
//                                     ignoreFailures under this task so 🔴 can be stamped;
//   * the tests executed nothing    -> 🔴, FAILS (an all-@Ignore'd class measured nothing);
//   * no results for a cited class, or for a row that claims 🟢/🟡 -> ⚪, FAILS;
//   * no results and the row is ⚪ or 🔴 -> left alone: neither claims support, so this is
//     the legitimate "not built yet" state, reported but not failed (by decision);
//   * no contract row measured at all -> FAILS (vacuity: a moved header must not pass).
// A test class with no row is reported, not failed: many classes are krapper regression
// tests (TsSkipTest, TdDefArgTest, ...) that have no feature row by design.
//
// Stale results: a test task clears its JUnit XML directory when it runs, and under this
// task the harness tests always run (see the end of this file), so the results read here
// are exactly the ones this build produced. A deleted test class leaves no XML behind, and
// its row goes ⚪ and fails.
//
// Not cached: no outputs are declared, so it always re-reads docs/features.md. CI runs it in
// root-build.yml's generator-suite job and fails if it changed the committed file.
// ---------------------------------------------------------------------------

// A JUnit XML file's counts, summed over EVERY `<testsuite ` start tag: the arithmetic of
// .github/test-evidence-gate/gate.sh. `tests=` counts skipped testcases, so executed is
// `tests - skipped`. Reading only start tags keeps attribute-like <system-out> text out, and
// the space after `testsuite` keeps a `<testsuites>` root's totals from counting twice.
data class Counts(
    val executed: Int,
    val failed: Int,
) {
    operator fun plus(other: Counts) = Counts(executed + other.executed, failed + other.failed)
}

fun countsOf(xml: String): Counts =
    Regex("<testsuite [^>]*>")
        .findAll(xml)
        .map { tag ->
            fun attr(name: String) =
                Regex(" $name=\"(\\d+)\"")
                    .find(tag.value)
                    ?.groupValues
                    ?.get(1)
                    ?.toInt() ?: 0
            Counts(attr("tests") - attr("skipped"), attr("failures") + attr("errors"))
        }.fold(Counts(0, 0), Counts::plus)

// PR-int-rt -> PrIntRtTest, ST-cstr-in -> StCstrInTest: the category code Title-cased, the
// rest PascalCased, then `Test`.
fun rowIdToClass(id: String): String =
    id.split("-").joinToString("") { it.lowercase().replaceFirstChar(Char::uppercaseChar) } + "Test"

class Stamped(
    val text: String,
    val problems: List<String>,
    val unscaffolded: List<String>,
    val orphans: List<String>,
)

fun stampMatrix(
    matrix: String,
    results: Map<String, Counts>,
): Stamped {
    val problems = mutableListOf<String>()
    val unscaffolded = mutableListOf<String>()
    val claimed = mutableSetOf("SmokeTest")
    var statusIdx = -1 // the current table's Status column; -1 outside a contract table
    var width = 0
    var measured = 0
    val text =
        matrix.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
                width = 0
                return@joinToString line
            }
            val cells =
                trimmed
                    .removePrefix("|")
                    .removeSuffix("|")
                    .split("|")
                    .map { it.trim() }
            if ("ID" in cells && "Status" in cells) {
                statusIdx = if ("Test cases" in cells) cells.indexOf("Status") else -1
                width = cells.size
                return@joinToString line
            }
            val id = cells.first()
            if (cells.size != width ||
                !Regex("^[A-Z]{2,}-").containsMatchIn(id)
            ) {
                return@joinToString line
            }
            val cited =
                cells.flatMap { cell ->
                    Regex("`([A-Z][A-Za-z0-9]*Test)`").findAll(cell).map { it.groupValues[1] }
                }
            val classes = cited.ifEmpty { listOf(rowIdToClass(id)) }
            claimed += classes
            if (statusIdx == -1) return@joinToString line

            val current = cells[statusIdx]
            val missing = classes.filter { it !in results }
            val counts = classes.mapNotNull(results::get).fold(Counts(0, 0), Counts::plus)
            val claimsSupport = current != "⚪" && current != "🔴"
            val status =
                if (missing.isNotEmpty()) {
                    if (cited.isEmpty() && !claimsSupport) {
                        unscaffolded += "$id ($current, expected $classes)"
                        return@joinToString line
                    }
                    problems += "$id: is $current, but no test results for $missing"
                    "⚪"
                } else {
                    measured++
                    val ranNothing = counts.executed == 0
                    if (ranNothing) problems += "$id: $classes executed zero tests"
                    // A failing class fails the task below, whether or not a row owns it.
                    when {
                        counts.failed > 0 || ranNothing -> "🔴"
                        "🟡" in current -> current
                        else -> "🟢"
                    }
                }
            "| " + cells.toMutableList().also { it[statusIdx] = status }.joinToString(" | ") + " |"
        }
    problems +=
        results.filterValues { it.failed > 0 }.map { (cls, c) ->
            "$cls: ${c.failed} failing test(s)"
        }
    if (measured == 0) problems += "matrix: no harness-statused row matched a test that ran"
    return Stamped(text, problems, unscaffolded, (results.keys - claimed).sorted())
}

// The harness whose results matrixReport reads: this module (pure cinterop) and the
// generator-backed :featuregen, which statuses the std::-type rows.
evaluationDependsOn(":featuregen")
val harnessTests =
    listOf(
        tasks.named<KotlinNativeTest>("nativeTest"),
        project(":featuregen").tasks.named<KotlinNativeTest>("nativeTest"),
    )

// stampMatrix's verdicts, proven on the test-evidence gate's committed JUnit fixtures before
// matrixReport trusts it: a correct stamper and a broken one are indistinguishable while
// every row is green, which is how #223 survived.
val verifyMatrixReport =
    tasks.register("verifyMatrixReport") {
        group = "verification"
        description = "Check matrixReport's verdicts against the test-evidence-gate fixtures"
        val fixtures =
            rootProject.layout.projectDirectory.dir(
                ".github/test-evidence-gate/fixtures",
            )
        inputs.dir(fixtures)
        doLast {
            fun fixture(name: String) = countsOf(fixtures.file("$name.xml").asFile.readText())
            val wrong = mutableListOf<String>()

            fun expect(
                what: String,
                want: Any,
                got: Any,
            ) {
                if (want != got) wrong += "$what: expected $want, got $got"
            }
            expect("healthy.xml", Counts(9, 0), fixture("healthy"))
            expect("all-skipped.xml", Counts(0, 0), fixture("all-skipped"))
            expect("boundary.xml", Counts(1, 0), fixture("boundary"))
            expect("nested.xml", Counts(6, 0), fixture("nested"))
            expect("multi-suite-failure.xml", Counts(10, 1), fixture("multi-suite-failure"))
            expect("errored.xml", Counts(6, 2), fixture("errored"))
            expect("decoy-system-out.xml", Counts(2, 0), fixture("decoy-system-out"))

            val results =
                mapOf(
                    "AaPassTest" to fixture("healthy"),
                    "AaWorkaroundTest" to fixture("boundary"),
                    "AaFailTest" to fixture("multi-suite-failure"),
                    "AaErrorTest" to fixture("errored"),
                    "AaIgnoredTest" to fixture("all-skipped"),
                    "SharedTest" to fixture("nested"),
                    "AaHandTest" to fixture("healthy"),
                    "AaOrphanTest" to fixture("healthy"),
                )
            val matrix =
                """
                | ID | Feature | Test cases | Status | Notes |
                |---|---|---|---|---|
                | AA-pass | f | • c | ⚪ | |
                | AA-workaround | f | • c | 🟡 | |
                | AA-fail | f | • c | 🟢 | |
                | AA-error | f | • c | 🟢 | |
                | AA-ignored | f | • c | 🟢 | |
                | AA-deleted | f | • c | 🟢 | |
                | AA-todo | f | • c | ⚪ | |
                | AA-blocked | f | • c | 🔴 | |
                | AA-cited | f | • c | 🔴 | `SharedTest` |
                | AA-cited-gone | f | • c | ⚪ | `GoneTest` |

                | ID | Feature | Status | Notes |
                |---|---|---|---|
                | AA-hand | f | 🔴 | `AaHandTest` |
                """.trimIndent()
            val stamped = stampMatrix(matrix, results)
            val statuses =
                stamped.text.lines().filter { it.startsWith("| AA-") }.associate {
                    val cells = it.split("|").map(String::trim)
                    cells[1] to cells[cells.size - 3]
                }
            expect(
                "statuses",
                mapOf(
                    "AA-pass" to "🟢",
                    "AA-workaround" to "🟡",
                    "AA-fail" to "🔴",
                    "AA-error" to "🔴",
                    "AA-ignored" to "🔴",
                    "AA-deleted" to "⚪",
                    "AA-todo" to "⚪",
                    "AA-blocked" to "🔴",
                    "AA-cited" to "🟢",
                    "AA-cited-gone" to "⚪",
                    "AA-hand" to "🔴",
                ),
                statuses,
            )
            expect(
                "failed on",
                listOf(
                    "AA-ignored",
                    "AA-deleted",
                    "AA-cited-gone",
                    "AaFailTest",
                    "AaErrorTest",
                ).sorted(),
                stamped.problems.map { it.substringBefore(':') }.sorted(),
            )
            expect(
                "unscaffolded",
                listOf("AA-todo", "AA-blocked"),
                stamped.unscaffolded.map {
                    it.substringBefore(' ')
                },
            )
            expect("orphans", listOf("AaOrphanTest"), stamped.orphans)
            // Vacuity: the same matrix with no `Test cases` column statuses nothing, and must say so.
            expect(
                "no contract table",
                listOf("matrix: no harness-statused row matched a test that ran"),
                stampMatrix(
                    matrix.replace("| Test cases |", "| Cases |"),
                    mapOf("AaPassTest" to fixture("healthy")),
                ).problems,
            )
            if (wrong.isNotEmpty()) {
                throw GradleException(
                    "matrixReport's stamper is wrong:\n  " + wrong.joinToString("\n  "),
                )
            }
        }
    }

val matrixReport =
    tasks.register("matrixReport") {
        group = "verification"
        description =
            "Run the feature tests, write per-row status into docs/features.md, fail on drift"
        dependsOn(verifyMatrixReport, harnessTests)
        val xmlDirs =
            files(
                harnessTests.map { test ->
                    test.map {
                        it.reports.junitXml.outputLocation
                            .get()
                    }
                },
            )
        val matrixFile = rootProject.file("docs/features.md")
        inputs.files(xmlDirs)
        inputs.file(matrixFile)

        doLast {
            val results =
                xmlDirs.asFileTree
                    .matching { include("TEST-*.xml") }
                    .groupBy { it.name.removePrefix("TEST-nativeTest.").removeSuffix(".xml") }
                    .mapValues { (_, xml) ->
                        xml.map { countsOf(it.readText()) }.reduce(Counts::plus)
                    }
            val old = matrixFile.readText()
            val stamped = stampMatrix(old, results)
            val changed = stamped.text != old
            if (changed) matrixFile.writeText(stamped.text)

            val where = matrixFile.relativeTo(rootProject.projectDir)
            logger.lifecycle("matrixReport: ${if (changed) "updated" else "no change to"} $where")
            if (stamped.unscaffolded.isNotEmpty()) {
                logger.lifecycle(
                    "  ⚪/🔴 rows with no tests yet: ${stamped.unscaffolded.joinToString(", ")}",
                )
            }
            if (stamped.orphans.isNotEmpty()) {
                logger.lifecycle(
                    "  test classes with no matrix row: ${stamped.orphans.joinToString(", ")}",
                )
            }
            if (stamped.problems.isNotEmpty()) {
                throw GradleException(
                    "$where disagrees with the tests:\n  " + stamped.problems.joinToString("\n  "),
                )
            }
        }
    }

// Under matrixReport the harness tests always RUN: an UP-TO-DATE test task does not clear its
// JUnit XML directory, so a report no run of this build wrote would be read, and excluding
// one (`-x`) would leave its last run's reports. A failing test must reach the
// matrix as 🔴 rather than stop the build first; matrixReport then fails on it itself.
gradle.taskGraph.whenReady {
    if (!hasTask(matrixReport.get())) return@whenReady
    for (test in harnessTests.map { it.get() }) {
        val excluded = !hasTask(test)
        if (excluded) throw GradleException("${matrixReport.get().path} must run ${test.path}")
        test.outputs.upToDateWhen { false }
        test.ignoreFailures = true
    }
}
