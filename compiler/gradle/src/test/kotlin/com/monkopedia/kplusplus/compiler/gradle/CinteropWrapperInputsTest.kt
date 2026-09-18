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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

/**
 * THE CINTEROP TASK'S REAL INPUTS (#249, and #16 before it).
 *
 * `interop.definitionFile` is the only input the cinterop task declares by itself, and the
 * generated `.def` is deterministic from the module name + this checkout's krapped dir — so its
 * content NEVER changes. The artifacts it points at (`headers = <module>.h`,
 * `staticLibraries = lib<module>.a`) DO change, on every edit to the C++ the module binds, and
 * the klib packages the archive into itself. Under-declared, that froze the archive: an edited
 * `clang_slice.h` regenerated `libkrapper.a`, `cinteropKplusplusNative` went UP-TO-DATE against
 * the unchanged `.def`, and `nativeTest` went UP-TO-DATE having run ZERO tests against an
 * archive built minutes earlier. `BUILD SUCCESSFUL`, and symmetric — the REVERT did not land
 * either, so both states went green with neither exercised.
 *
 * #16 wired `dependsOn(kplusplusSync)` onto the same task so it "can't process a stale def".
 * That fixed the POINTER and not the POINTEE, which is why
 * [declared_inputs_cover_every_artifact_the_generated_def_points_at] asserts the two as one
 * invariant rather than spot-checking two filenames: adding a third artifact to the `.def`
 * without declaring it is the same defect again, and it must fail here.
 */
class CinteropWrapperInputsTest {

    private val moduleName = "demo"

    private fun taskDependencyNames(task: Task): Set<String> =
        task.taskDependencies.getDependencies(task).map { it.name }.toSet()

    // The whole wiring, on a real (if headless) Gradle task graph: the generator runs first
    // (#16) AND the artifacts it writes are declared inputs (#249). Both assertions live in one
    // test on purpose — they are the two halves of "this task cannot run against stale input",
    // and half of it passing is exactly the state this issue was filed on.
    @Test
    fun interop_task_declares_the_generated_wrapper_artifacts_as_inputs() {
        val project = ProjectBuilder.builder().build()
        val krappedDir = File(project.projectDir, "build/krapped-cpp")
        project.tasks.register("kplusplusSync")
        // Configure BEFORE the task exists: KGP creates the cinterop task itself, well after
        // the plugin's afterEvaluate runs, so the production wiring is a lazy
        // matching{}.configureEach{} and the test must exercise that ordering.
        configureInteropTask(project, "cinteropKplusplusNative", krappedDir, moduleName)
        project.tasks.register("cinteropKplusplusNative")
        val interop = project.tasks.named("cinteropKplusplusNative").get()

        assertTrue(
            "kplusplusSync" in taskDependencyNames(interop),
            "the generator must run first, or the .def itself is stale (#16): " +
                taskDependencyNames(interop)
        )
        assertEquals(
            cinteropWrapperArtifacts(krappedDir, moduleName).toSet(),
            interop.inputs.files.files,
            "the cinterop task must declare the wrapper header AND archive as inputs, or a " +
                "changed C++ bridge leaves it UP-TO-DATE against the archive already baked " +
                "into the klib (#249)"
        )
    }

    // The invariant, not the filenames: whatever the generated `.def` NAMES inside the krapped
    // dir is what the cinterop task actually reads, so the declared input set and the `.def`'s
    // own references must be the same set. A third artifact added to the `.def` and not
    // declared is #249 all over again, and fails right here.
    @Test
    fun declared_inputs_cover_every_artifact_the_generated_def_points_at() {
        val krappedDir = File("/tmp/does-not-need-to-exist/krapped")
        val def = seedDefContent(moduleName, krappedDir)
        assertEquals(
            cinteropWrapperArtifacts(krappedDir, moduleName).toSet(),
            defArtifactsIn(def, krappedDir).toSet(),
            "every file the .def points at must be a declared cinterop input, and vice " +
                "versa — the #16 fix declared the pointer and not the pointee.\n$def"
        )
    }

    // A control for the test above: it must be able to NOTICE an artifact that is referenced
    // but not declared. Without this, a `defArtifactsIn` that parsed nothing at all would make
    // both sides empty on a `.def` with no references and the assertion would pass while
    // measuring nothing.
    @Test
    fun the_coverage_assertion_notices_an_artifact_the_def_adds_without_declaring() {
        val krappedDir = File("/tmp/does-not-need-to-exist/krapped")
        val extended = seedDefContent(moduleName, krappedDir).replace(
            "staticLibraries = lib$moduleName.a",
            "staticLibraries = lib$moduleName.a libextra.a"
        )
        val referenced = defArtifactsIn(extended, krappedDir).toSet()
        assertTrue(
            File(krappedDir, "libextra.a") in referenced,
            "the .def reader must see every staticLibraries entry: $referenced"
        )
        assertTrue(
            referenced != cinteropWrapperArtifacts(krappedDir, moduleName).toSet(),
            "an undeclared extra artifact must break the coverage equality, or that " +
                "assertion is an instrument that cannot go red"
        )
    }

    // Neither artifact exists at configuration time on a clean checkout — kplusplusSync writes
    // them, and the dependsOn above is what orders it. The declaration has to tolerate that,
    // which is why it is `inputs.files(...).optional(true)` and not `inputs.file(...)`: the
    // singular form aborts at validation on a missing path and would turn every clean build
    // into a configuration failure.
    @Test
    fun declared_inputs_tolerate_artifacts_that_do_not_exist_yet() {
        val project = ProjectBuilder.builder().build()
        val krappedDir = File(project.projectDir, "build/krapped-cpp")
        assertTrue(!krappedDir.exists(), "precondition: nothing is generated yet")
        project.tasks.register("kplusplusSync")
        configureInteropTask(project, "cinteropKplusplusNative", krappedDir, moduleName)
        project.tasks.register("cinteropKplusplusNative")

        val declared = project.tasks.named("cinteropKplusplusNative").get().inputs.files.files
        assertEquals(
            cinteropWrapperArtifacts(krappedDir, moduleName).toSet(),
            declared,
            "the inputs must resolve to the not-yet-written paths rather than throwing or " +
                "silently collapsing to nothing"
        )
    }
}
