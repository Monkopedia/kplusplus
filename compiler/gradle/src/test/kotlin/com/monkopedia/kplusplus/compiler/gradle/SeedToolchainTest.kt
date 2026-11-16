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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/**
 * THE SEED WRAPPER COMPILE USES THE CONFIGURED LLVM (#297).
 *
 * Seed mode recompiles the committed wrapper `.cc`. With `llvmConfig` naming an LLVM that is not
 * the one on PATH, that compile must take `clang++` from that LLVM's `--bindir` and its headers
 * from `--includedir`, as the cpp path does, not whatever `clang++` PATH finds first. And since
 * the toolchain decides the object, its identity (path + `--version`) is a declared input.
 *
 * Both compilers here are stubs: the PATH one fails loudly, the llvm-config one records its
 * arguments and writes the object, and answers `--version` from a file the test can edit.
 */
class SeedToolchainTest {

    private val dir = createTempDirectory("kpp-seed-toolchain").toFile()
    private val projectDir = File(dir, "demo").apply { mkdirs() }
    private val llvmBin = File(dir, "llvm/bin")
    private val llvmInclude = File(dir, "llvm/include").apply { mkdirs() }
    private val pathBin = File(dir, "path-bin")
    private val llvmArgs = File(dir, "llvm-clang.args")
    private val llvmVersion = File(dir, "llvm-clang.version")
    private val pathRan = File(dir, "path-clang.ran")

    private fun script(file: File, body: String) {
        file.parentFile.mkdirs()
        file.writeText("#!/bin/sh\n$body\n")
        file.setExecutable(true)
    }

    private fun writeFixture() {
        script(
            File(llvmBin, "llvm-config"),
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
            File(llvmBin, "clang++"),
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

        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"demo\"\n")
        File(projectDir, "gradle.properties").writeText(
            "kpp.frontend.demo=seed\norg.gradle.jvmargs=-Xmx512m\n" +
                "org.gradle.daemon.idletimeout=60000\n"
        )
        File(projectDir, "krapped/src/Demo.kt").apply { parentFile.mkdirs() }
            .writeText("package krapper.demo\n")
        File(projectDir, "krapped/demo.cc").writeText("int demo() { return 1; }\n")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.monkopedia.kplusplus.compiler") }
            kplusplus {
                compiler = "clang++"
                llvmConfig = "${File(llvmBin, "llvm-config")}"
            }
            """.trimIndent()
        )
    }

    /** [poisonPath] puts the failing stub first on PATH. */
    private fun sync(poisonPath: Boolean = false): TaskOutcome? = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .apply {
            if (poisonPath) {
                val path = "$pathBin${File.pathSeparator}${System.getenv("PATH")}"
                withEnvironment(System.getenv() + ("PATH" to path))
            }
        }
        .withArguments("kplusplusSync", "--stacktrace")
        .build()
        .task(":kplusplusSync")?.outcome

    @Test
    fun the_seed_compile_takes_clang_from_llvm_config_not_path() {
        writeFixture()
        assertEquals(TaskOutcome.SUCCESS, sync(poisonPath = true))
        assertFalse(pathRan.exists(), "the seed compile ran the clang++ PATH found first")
        val args = llvmArgs.readText()
        assertTrue(
            "-I$llvmInclude" in args.split(' '),
            "the seed compile must see the llvm-config include dir: $args"
        )
        assertTrue(File(projectDir, "krapped/libdemo.a").exists(), "no wrapper object written")
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
}
