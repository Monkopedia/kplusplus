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
import kotlin.test.assertTrue
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

/**
 * THE CINTEROP TASK'S REAL INPUTS (#249, #251, and #16 before them).
 *
 * `interop.definitionFile` is the only input the cinterop task declares by itself, but the
 * `.def` only POINTS AT what cinterop reads — the wrapper header and the static archives — and
 * the klib packages those archives into itself. Under-declared, that froze the archive: an
 * edited `clang_slice.h` regenerated `libkrapper.a`, `cinteropKplusplusNative` went UP-TO-DATE
 * against the unchanged `.def`, and `nativeTest` went UP-TO-DATE having run ZERO tests.
 *
 * The rule: the declared input set must be exactly the set of files the generated `.def` names.
 * Since #251 the plugin derives the inputs FROM the `.def` at execution time, so the rule holds
 * by construction for both writers (the seed path's `seedDefContent` and krapper's `DefWriter`
 * on the cpp path). What is left to pin is that the reader resolves what each writer actually
 * emits: `defwriter-golden.def` is `DefWriter`'s output for a module linking an out-of-tree
 * archive, and krapper's `DefWriterGoldenTest` fails if `DefWriter` drifts from it.
 */
class CinteropWrapperInputsTest {

    private val moduleName = "demo"

    private fun taskDependencyNames(task: Task): Set<String> =
        task.taskDependencies.getDependencies(task).map { it.name }.toSet()

    // krapper's DefWriter output (pinned by DefWriterGoldenTest), with its placeholder dirs
    // pointed at real ones.
    private fun goldenDef(krappedDir: File, prebuiltDir: File): String =
        javaClass.getResource("/defwriter-golden.def")!!.readText()
            .replace("/@KRAPPED@", krappedDir.absolutePath)
            .replace("/@PREBUILT@", prebuiltDir.absolutePath)

    private fun touch(vararg files: File) = files.forEach {
        it.parentFile.mkdirs()
        it.writeText("")
    }

    // Configure BEFORE the task exists: KGP creates the cinterop task itself, well after the
    // plugin's afterEvaluate runs, so the production wiring is a lazy
    // matching{}.configureEach{} and the tests must exercise that ordering.
    private fun interopTask(project: Project, def: File): Task {
        project.tasks.register("kplusplusSync")
        configureInteropTask(project, "cinteropKplusplusNative", def)
        project.tasks.register("cinteropKplusplusNative")
        return project.tasks.named("cinteropKplusplusNative").get()
    }

    // The whole wiring, on the cpp path's real `.def` shape: the generator runs first (#16) AND
    // every artifact the `.def` names is a declared input (#249) — including an archive OUTSIDE
    // the krapped dir, which basename resolution used to declare at a path that does not hold
    // it, leaving the real one undeclared (#251).
    @Test
    fun interop_task_declares_every_artifact_the_cpp_path_def_names() {
        val project = ProjectBuilder.builder().build()
        val krappedDir = File(project.projectDir, "build/krapped-cpp")
        val prebuiltDir = File(project.projectDir, "prebuilt")
        val expected = setOf(
            File(krappedDir, "demo.h"),
            File(krappedDir, "libdemo.a"),
            File(prebuiltDir, "libfoo.a")
        )
        touch(*expected.toTypedArray())
        val def = File(krappedDir, "$moduleName.def")
        def.writeText(goldenDef(krappedDir, prebuiltDir))
        val interop = interopTask(project, def)

        assertTrue(
            "kplusplusSync" in taskDependencyNames(interop),
            "the generator must run first, or the .def itself is stale (#16): " +
                taskDependencyNames(interop)
        )
        assertEquals(
            expected,
            interop.inputs.files.files,
            "the cinterop task must declare every header and archive the .def names, where " +
                "the .def names it, or a changed C++ bridge leaves it UP-TO-DATE against the " +
                "archive already baked into the klib (#249, #251)\n${def.readText()}"
        )
    }

    // The inputs are read from the `.def` when Gradle fingerprints them — after kplusplusSync
    // has written it — not captured at configuration time. A clean checkout has no `.def` while
    // configuring, and a regenerated one may name a different set; both must be tolerated and
    // followed, or the declared set is once again a snapshot that can drift from the pointee.
    @Test
    fun declared_inputs_follow_the_def_written_after_configuration() {
        val project = ProjectBuilder.builder().build()
        val krappedDir = File(project.projectDir, "build/krapped-cpp")
        val def = File(krappedDir, "$moduleName.def")
        val interop = interopTask(project, def)
        assertEquals(
            emptySet(),
            interop.inputs.files.files,
            "no .def yet: nothing to declare, and configuring must not throw"
        )

        krappedDir.mkdirs()
        def.writeText(seedDefContent(moduleName, krappedDir))
        assertEquals(
            setOf(File(krappedDir, "demo.h"), File(krappedDir, "libdemo.a")),
            interop.inputs.files.files,
            "the seed path's .def, written after configuration, must be what is declared"
        )

        def.writeText(
            seedDefContent(moduleName, krappedDir)
                .replace("libdemo.a", "libdemo.a libextra.a")
        )
        assertTrue(
            File(krappedDir, "libextra.a") in interop.inputs.files.files,
            "an archive the regenerated .def adds must become a declared input: " +
                interop.inputs.files.files
        )
    }

    // The reader's resolution, and its control: it must find each archive where the LINKER
    // would — the first `libraryPaths` entry that holds it — not by basename in the krapped dir.
    // Without the out-of-tree case, a reader that always resolved into the krapped dir passed
    // every assertion here while declaring the wrong file for `library("/abs/libfoo.a")`.
    @Test
    fun the_def_reader_resolves_each_entry_along_its_search_path_in_order() {
        val root = createTempDirectory("cinterop-inputs").toFile()
        val krappedDir = File(root, "krapped")
        val prebuiltDir = File(root, "prebuilt")
        touch(File(krappedDir, "demo.h"), File(krappedDir, "libdemo.a"))
        // libfoo.a exists only outside the krapped dir.
        touch(File(prebuiltDir, "libfoo.a"))
        assertEquals(
            listOf(
                File(krappedDir, "demo.h"),
                File(prebuiltDir, "libfoo.a"),
                File(krappedDir, "libdemo.a")
            ),
            defArtifactsIn(goldenDef(krappedDir, prebuiltDir)),
            "an out-of-tree archive must resolve to where it lives (#251)"
        )

        // A same-named archive in an EARLIER libraryPaths entry shadows the later one, as it
        // does for the linker.
        touch(File(prebuiltDir, "libdemo.a"))
        assertTrue(
            File(prebuiltDir, "libdemo.a") in defArtifactsIn(goldenDef(krappedDir, prebuiltDir)),
            "the first libraryPaths entry holding the archive must win"
        )

        // Not generated yet: still declared, at the first place it would be looked for.
        assertEquals(
            listOf(File(root, "nowhere/demo.h"), File(root, "nowhere/libdemo.a")),
            defArtifactsIn(seedDefContent(moduleName, File(root, "nowhere"))),
            "an entry that exists nowhere must still be tracked, not silently dropped"
        )
    }
}
