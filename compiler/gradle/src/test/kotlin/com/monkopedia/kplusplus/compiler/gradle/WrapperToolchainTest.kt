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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/**
 * THE WRAPPER COMPILE USES THE CONFIGURED LLVM (#297).
 *
 * Both front-ends compile a C++ wrapper: seed mode recompiles the committed `.cc` itself, and the
 * cpp path hands krapper the compiler to build the one it generates. With `llvmConfig` naming an
 * LLVM that is not the one on PATH, that compiler must be the `clang++` in that LLVM's `--bindir`,
 * with its headers from `--includedir`, not whatever `clang++` PATH finds first, and that holds
 * for the default compiler too. And since the toolchain decides the object, its identity (path +
 * `--version`) is a declared input.
 *
 * Both compilers here are stubs: the PATH one fails loudly, the llvm-config one records its
 * arguments and writes the object, and answers `--version` from a file the test can edit. Every
 * build runs with `KONAN_DATA_DIR` pointed at an empty dir, so the seed default is `clang++`
 * rather than the konan g++ this box may have.
 */
class WrapperToolchainTest {

    private val dir = createTempDirectory("kpp-wrapper-toolchain").toFile()
    private val projectDir = File(dir, "demo").apply { mkdirs() }
    private val llvmBin = File(dir, "llvm/bin")
    private val llvmConfig = File(llvmBin, "llvm-config")
    private val llvmClang = File(llvmBin, "clang++")
    private val llvmInclude = File(dir, "llvm/include").apply { mkdirs() }
    private val pathBin = File(dir, "path-bin")
    private val llvmArgs = File(dir, "llvm-clang.args")
    private val llvmVersion = File(dir, "llvm-clang.version")
    private val pathRan = File(dir, "path-clang.ran")

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    private fun script(file: File, body: String) {
        file.parentFile.mkdirs()
        file.writeText("#!/bin/sh\n$body\n")
        file.setExecutable(true)
    }

    private fun writeToolchain() {
        script(
            llvmConfig,
            """
            case "${'$'}1" in
              --bindir) echo "$llvmBin" ;;
              --includedir) echo "$llvmInclude" ;;
              --version) echo 22.1.0 ;;
              *) exit 1 ;;
            esac
            """.trimIndent()
        )
        llvmVersion.writeText("stub clang version 22.1.0\n")
        script(
            llvmClang,
            """
            if [ "${'$'}1" = --version ]; then cat "$llvmVersion"; exit 0; fi
            echo "${'$'}@" > "$llvmArgs"
            while [ ${'$'}# -gt 0 ]; do
              if [ "${'$'}1" = -o ]; then : > "${'$'}2"; fi
              shift
            done
            """.trimIndent()
        )
        script(File(pathBin, "clang++"), ": > \"$pathRan\"\nexit 1")
    }

    /**
     * A [frontend]-mode project; [compiler] null leaves `kplusplus { compiler }` unset. Only the
     * cpp one declares a header: in seed mode that would drop the outputs, so never UP-TO-DATE.
     */
    private fun writeFixture(
        frontend: String = "seed",
        compiler: String? = "clang++",
        buildTail: String = ""
    ) {
        writeToolchain()
        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"demo\"\n")
        File(projectDir, "gradle.properties").writeText(
            "kpp.frontend.demo=$frontend\norg.gradle.jvmargs=-Xmx512m\n" +
                "org.gradle.daemon.idletimeout=60000\n"
        )
        File(projectDir, "krapped/src/Demo.kt").apply { parentFile.mkdirs() }
            .writeText("package krapper.demo\n")
        File(projectDir, "krapped/demo.cc").writeText("int demo() { return 1; }\n")
        File(projectDir, "demo.h").writeText("struct Demo {};\n")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.monkopedia.kplusplus.compiler") }
            kplusplus {
                ${compiler?.let { "compiler = \"$it\"" }.orEmpty()}
                llvmConfig = "$llvmConfig"
                ${if (frontend == "cpp") "header(\"demo.h\")" else ""}
            }
            tasks.register("printToolchain") {
                doLast {
                    val sync = tasks.getByName("kplusplusSync")
                    println("TOOLCHAIN<" + sync.inputs.properties["toolchain"] + ">")
                }
            }
            """.trimIndent() + "\n" + buildTail
        )
    }

    /** [poisonPath] puts the failing stub first on PATH. */
    private fun run(vararg tasks: String, poisonPath: Boolean = false): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withEnvironment(
                System.getenv() + ("KONAN_DATA_DIR" to noKonan.path) +
                    if (poisonPath) {
                        mapOf("PATH" to "$pathBin${File.pathSeparator}${System.getenv("PATH")}")
                    } else {
                        emptyMap()
                    }
            )
            .withArguments(tasks.toList() + "--stacktrace")
            .build()

    private fun sync(poisonPath: Boolean = false): TaskOutcome? =
        run("kplusplusSync", poisonPath = poisonPath).task(":kplusplusSync")?.outcome

    private fun assertSeedCompiledByLlvmClang() {
        assertFalse(pathRan.exists(), "the seed compile ran the clang++ PATH found first")
        val args = llvmArgs.readText()
        assertTrue(
            "-I$llvmInclude" in args.split(' '),
            "the seed compile must see the llvm-config include dir: $args"
        )
        assertTrue(File(projectDir, "krapped/libdemo.a").exists(), "no wrapper object written")
    }

    @Test
    fun the_seed_compile_takes_clang_from_llvm_config_not_path() {
        writeFixture()
        assertEquals(TaskOutcome.SUCCESS, sync(poisonPath = true))
        assertSeedCompiledByLlvmClang()
    }

    // No `compiler` and no konan g++: the default `clang++` goes through the same bindir lookup,
    // for the compile and for the identity it declares.
    @Test
    fun the_default_seed_compiler_is_llvm_config_clang() {
        writeFixture(compiler = null)
        val result = run("kplusplusSync", "printToolchain", poisonPath = true)
        assertEquals(TaskOutcome.SUCCESS, result.task(":kplusplusSync")?.outcome)
        assertSeedCompiledByLlvmClang()
        assertTrue(
            "TOOLCHAIN<$llvmClang\n" in result.output,
            "the declared toolchain must be llvm-config's clang++: ${result.output}"
        )
    }

    @Test
    fun a_changed_toolchain_reruns_the_seed_sync() {
        writeFixture()
        assertEquals(TaskOutcome.SUCCESS, sync())
        assertEquals(TaskOutcome.UP_TO_DATE, sync(), "control: nothing changed")
        llvmVersion.writeText("stub clang version 22.1.1\n")
        assertEquals(
            TaskOutcome.SUCCESS,
            sync(),
            "a different clang++ --version must make the seed sync out of date"
        )
    }

    // The cpp path's wrapper is compiled by krapper, with the compiler the job names.
    @Test
    fun the_cpp_wrapper_compiler_is_llvm_config_clang() {
        writeToolchain()
        val project = ProjectBuilder.builder().withProjectDir(projectDir).withName("demo").build()
        project.extensions.extraProperties["kpp.frontend.demo"] = "cpp"
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply(KPlusPlusCompilerGradlePlugin::class.java)
        val ext = project.extensions.getByType(KPlusPlusExtension::class.java)
        ext.header("demo.h")
        ext.llvmConfig = llvmConfig.path
        val job = KPlusPlusCompilerGradlePlugin()
            .cppKrapperJob(project, ext, "demo", File(dir, "krapped-cpp"))
        assertEquals(llvmClang.path, job.config.compiler)
        assertTrue(llvmInclude.path in job.request.includeDirs, "${job.request.includeDirs}")
    }

    // The generator is not under test, only the inputs the plugin declared: a stand-in action
    // and an output dir make the cpp sync incremental, as SyncTaskInputsTest does.
    @Test
    fun a_changed_toolchain_reruns_the_cpp_sync() {
        writeFixture(
            frontend = "cpp",
            compiler = null,
            buildTail =
            """
            tasks.named("kplusplusSync") {
                actions.clear()
                val out = layout.buildDirectory.dir("sync-out")
                outputs.dir(out)
                doLast { out.get().asFile.resolve("ran").writeText("") }
            }
            """.trimIndent()
        )
        assertEquals(TaskOutcome.SUCCESS, sync())
        assertEquals(TaskOutcome.UP_TO_DATE, sync(), "control: nothing changed")
        llvmVersion.writeText("stub clang version 22.1.1\n")
        assertEquals(
            TaskOutcome.SUCCESS,
            sync(),
            "a different clang++ --version must make the cpp sync out of date"
        )
    }

    private companion object {
        /** A konan data dir with no toolchains in it. */
        val noKonan = File(System.getProperty("java.io.tmpdir"), "kpp-wrapper-toolchain-no-konan")
    }
}
