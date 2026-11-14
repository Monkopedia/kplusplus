/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.kplusplus.compiler.gradle

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation

/**
 * WHAT `kplusplusSync` READS MUST BE WHAT IT DECLARES (#226).
 *
 * The task reads `<projectDir>/krapped/requested.txt` — the manifest the FIR checker grows — but
 * declared `<krappedDir>/requested.txt`, which on the cpp front-end is `build/krapped-cpp`, where
 * nothing ever writes one. And `compiler`, `headerDirectories`, `libraries` and the *contents* of
 * `onlyFile` all feed the generator without being inputs at all. Today the header-driven path
 * declares no outputs, so Gradle re-runs it every time and masks both; the moment the task is
 * incremental, a new instantiation or a changed knob would compile against stale bindings (#16).
 *
 * The UP-TO-DATE tests make the task incremental the way that future would — an output dir and
 * a stand-in action — so the outcome is decided by exactly the inputs the plugin declared.
 */
class SyncTaskInputsTest {

    private fun cppProject(): Project {
        val project = ProjectBuilder.builder().withName("demo").build()
        project.extensions.extraProperties["kpp.frontend.demo"] = "cpp"
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply(KPlusPlusCompilerGradlePlugin::class.java)
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).linuxX64("native")
        return project
    }

    private fun Project.mainCompilation(): KotlinNativeCompilation =
        extensions.getByType(KotlinMultiplatformExtension::class.java)
            .targets.getByName("native").compilations.getByName("main") as KotlinNativeCompilation

    @Test
    fun the_declared_manifest_is_the_one_the_fir_checker_writes() {
        val project = cppProject()
        val firManifest = KPlusPlusCompilerGradlePlugin()
            .applyToCompilation(project.mainCompilation()).get()
            .single { it.key == "requestManifestPath" }.value
        val declared = project.tasks.getByName("kplusplusSync").inputs.files.files
        assertTrue(
            File(firManifest) in declared,
            "kplusplusSync must declare the manifest the FIR checker writes ($firManifest), " +
                "not one nothing writes: $declared"
        )
    }

    @Test
    fun a_manifest_alone_wires_the_generated_bindings() {
        val project = cppProject()
        File(project.projectDir, "krapped/requested.txt").apply {
            parentFile.mkdirs()
            writeText("std::vector<int>\n")
        }
        (project as ProjectInternal).evaluate()
        assertNotNull(
            project.mainCompilation().cinterops.findByName("kplusplus"),
            "a module whose only binding source is the compiler-written manifest must still " +
                "get the generated cinterop (the canonical pure template-instantiation path)"
        )
    }

    // ---- UP-TO-DATE, through real Gradle runs ----------------------------------------------

    private val projectDir = createTempDirectory("kpp-sync-inputs").toFile()

    private fun writeBuild() {
        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"demo\"\n")
        File(projectDir, "gradle.properties").writeText(
            "kpp.frontend.demo=cpp\norg.gradle.jvmargs=-Xmx512m\n" +
                "org.gradle.daemon.idletimeout=60000\n"
        )
        File(projectDir, "demo.h").writeText("struct Demo {};\n")
        File(projectDir, "allow.txt").writeText("Demo\n")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.monkopedia.kplusplus.compiler") }
            fun knob(name: String, default: String) =
                (findProperty("knob.${'$'}name") as String?) ?: default
            kplusplus {
                header("demo.h")
                headerDirectory(knob("headerDirectory", "include"))
                library(knob("library", "libdemo.a"))
                onlyFile("allow.txt")
                compiler = knob("compiler", "clang++")
            }
            // The generator is not under test, only the inputs the plugin declared.
            tasks.named("kplusplusSync") {
                actions.clear()
                val out = layout.buildDirectory.dir("sync-out")
                outputs.dir(out)
                doLast { out.get().asFile.resolve("ran").writeText("") }
            }
            """.trimIndent()
        )
    }

    private fun sync(vararg args: String): TaskOutcome? = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(listOf("kplusplusSync", "--stacktrace") + args)
        .build()
        .task(":kplusplusSync")?.outcome

    /** Run once, prove the run is UP-TO-DATE unchanged (the control), then apply [change]. */
    private fun assertReRunsAfter(what: String, change: () -> Array<String>) {
        writeBuild()
        assertEquals(TaskOutcome.SUCCESS, sync())
        assertEquals(TaskOutcome.UP_TO_DATE, sync(), "control: nothing changed")
        assertEquals(
            TaskOutcome.SUCCESS,
            sync(*change()),
            "changing $what must make kplusplusSync out of date"
        )
    }

    @Test
    fun an_edited_manifest_reruns_the_sync() = assertReRunsAfter("krapped/requested.txt") {
        File(projectDir, "krapped/requested.txt").apply {
            parentFile.mkdirs()
            appendText("std::vector<int>\n")
        }
        emptyArray()
    }

    @Test
    fun a_changed_compiler_reruns_the_sync() =
        assertReRunsAfter("compiler") { arrayOf("-Pknob.compiler=g++") }

    @Test
    fun a_changed_header_directory_reruns_the_sync() =
        assertReRunsAfter("headerDirectories") { arrayOf("-Pknob.headerDirectory=other") }

    @Test
    fun a_changed_library_reruns_the_sync() =
        assertReRunsAfter("libraries") { arrayOf("-Pknob.library=libother.a") }

    @Test
    fun an_edited_only_file_reruns_the_sync() = assertReRunsAfter("the onlyFile contents") {
        File(projectDir, "allow.txt").appendText("Other\n")
        emptyArray()
    }
}
