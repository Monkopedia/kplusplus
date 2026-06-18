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
        val objs = sources.files.map { src ->
            val obj = objDir.resolve(src.nameWithoutExtension + ".o")
            val cmd = listOf(
                "clang++", "-std=c++17", "-fPIC", "-c",
                "-I${includeDir.absolutePath}",
                src.absolutePath,
                "-o", obj.absolutePath
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
// matrixReport: run the feature tests, then write each row's Status back into
// docs/features.md so the matrix's "Status is set by the harness, not
// by hand" promise is real. Row id -> test class is a deterministic forward
// derivation (PR-int-rt -> PrIntRtTest). Status rollup is per row (test class):
// any failure/error -> 🔴, otherwise 🟢. A manual 🟡 ("passes with a documented
// workaround") whose tests are green is preserved, since 🟡 is human judgment.
// Drift is logged both ways: a row with no matching test xml, and a test class
// with no matching row.
// ---------------------------------------------------------------------------
val matrixReport by tasks.registering {
    group = "verification"
    description = "Run feature tests and write per-row status back into features.md"
    dependsOn(tasks.named("nativeTest"))
    // The generator-backed harness (:featuregen) contributes std::-type rows;
    // include its results so those rows get statused too.
    dependsOn(":featuregen:nativeTest")

    // Every module's nativeTest results dir. The pure-cinterop harness
    // (this module) plus any generator-backed modules.
    val resultDirs = listOf(
        layout.buildDirectory.dir("test-results/nativeTest").get().asFile,
        rootProject.file("featuregen/build/test-results/nativeTest")
    )
    val matrixFile = rootProject.file("docs/features.md")
    resultDirs.filter { it.exists() }.forEach { inputs.dir(it) }
    inputs.file(matrixFile)
    outputs.file(matrixFile)

    doLast {
        // Row id -> test class, generalized across categories. The first
        // segment is the category code (PR, ST, …), Title-cased; the rest are
        // PascalCase and concatenated. PR-int-rt -> PrIntRtTest,
        // ST-cstr-in -> StCstrInTest.
        fun rowIdToClass(id: String): String {
            val parts = id.split("-")
            val cat = parts.first().lowercase().replaceFirstChar(Char::uppercaseChar)
            val rest = parts.drop(1).joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }
            return "$cat${rest}Test"
        }
        val idPattern = Regex("^[A-Z]{2,}-")

        val attr = { text: String, name: String ->
            Regex("""$name="(\d+)"""").find(text)?.groupValues?.get(1)?.toInt() ?: 0
        }

        val xmlByClass = resultDirs
            .flatMap { (it.listFiles { f -> f.name.endsWith(".xml") } ?: emptyArray()).toList() }
            .associateBy { it.name.removePrefix("TEST-nativeTest.").removeSuffix(".xml") }

        val lines = matrixFile.readLines()
        var statusIdx = -1
        var headerCells = 0
        val matchedClasses = mutableSetOf<String>()
        val notScaffolded = mutableListOf<String>()
        val turnedRed = mutableListOf<String>()

        val out = lines.map { line ->
            val trimmed = line.trim()
            val isTableRow = trimmed.startsWith("|") && trimmed.endsWith("|")
            if (!isTableRow) return@map line
            val cells = trimmed.removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

            // Lock onto the header that owns the Status column.
            if (statusIdx == -1 && cells.contains("Status") && cells.contains("ID")) {
                statusIdx = cells.indexOf("Status")
                headerCells = cells.size
                return@map line
            }
            // Only rewrite real data rows of that table.
            if (statusIdx == -1 || cells.size != headerCells) return@map line
            val id = cells.firstOrNull() ?: return@map line
            if (!idPattern.containsMatchIn(id)) return@map line

            val cls = rowIdToClass(id)
            val xml = xmlByClass[cls]
            if (xml == null) {
                notScaffolded += "$id (expected $cls)"
                return@map line
            }
            matchedClasses += cls
            val text = xml.readText()
            val failed = attr(text, "failures") + attr(text, "errors")
            val current = cells[statusIdx]
            val newStatus = when {
                failed > 0 -> "🔴" // 🔴
                current.contains("🟡") -> current // keep manual 🟡
                else -> "🟢" // 🟢
            }
            if (failed > 0) turnedRed += "$id ($cls: $failed failing)"
            val updated = cells.toMutableList().also { it[statusIdx] = newStatus }
            "| " + updated.joinToString(" | ") + " |"
        }

        val newText = out.joinToString("\n") + "\n"
        val changed = newText != matrixFile.readText()
        if (changed) matrixFile.writeText(newText)

        val orphans = (xmlByClass.keys - matchedClasses - "SmokeTest").sorted()
        logger.lifecycle("matrixReport: ${if (changed) "updated" else "no change to"} ${matrixFile.relativeTo(rootProject.projectDir)}")
        if (turnedRed.isNotEmpty()) logger.lifecycle("  🔴 failing rows: ${turnedRed.joinToString(", ")}")
        if (notScaffolded.isNotEmpty()) logger.lifecycle("  ⚪ rows with no tests yet: ${notScaffolded.joinToString(", ")}")
        if (orphans.isNotEmpty()) logger.lifecycle("  ⚠️  test classes with no matrix row: ${orphans.joinToString(", ")}")
    }
}
