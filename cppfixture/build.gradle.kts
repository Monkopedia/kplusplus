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

// The GENERALITY DEMO. A minimal, std-free binding consumer that exercises the GENERIC
// cpp-front-end path: under `-Pkpp.frontend.cppfixture=cpp` the kplusplus plugin drives THIS
// module's own kplusplus { header(...) } config through the krapper tool into
// build/krapped-cpp, then compiles + runs the tests against those bindings — the same
// generic machinery featuregen uses, on a different module's own config.
// `kpp.frontend.cppfixture=cpp` is the committed default.
//
// Run the demo:
//   ./gradlew :cppfixture:nativeTest -Pkpp.frontend.cppfixture=cpp  (cpp path)

plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler")
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                    // Generated bindings now carry `context(scope: MemScope)` on their
                    // allocating/cleanup-registering members (MemScope-field → context-param
                    // migration), so compiling them needs Kotlin 2.4 context parameters.
                    freeCompilerArgs.add("-Xcontext-parameters")
                }
            }
        }
        binaries {
            // tests only
        }
    }
}

// Single, std-free header — the cpp front-end parses it without any libstdc++ weight, so this
// module is "cpp-ready" today (unlike v8/clang-slice consumers). Declared out here so the
// transitional sync bridge below can reuse it without duplicating the value.
val fixtureHeader = "include/point2d.h"

kplusplus {
    header(fixtureHeader)
}

// ---- TRANSITIONAL SINGLE-BINARY SYNC BRIDGE (#184) ----
// DELETE once the plugin version pinned in settings.gradle.kts is 0.3.4+. See the same block in
// featuregen/build.gradle.kts for the full rationale: the CONSUMED 0.3.3 plugin's kplusplusSync
// drives two tool binaries (`krapper_parse --parity-emit` -> `krapper_gen --parsedModel`) whose
// CLIs no longer exist now that #184 merged them into ONE `:krapper` binary with an in-process
// handoff, so the task's ACTION is swapped for that single invocation. The rest of the plugin's
// wiring (build/krapped-cpp cinterop + .def + generated sources, compile ordering) is
// path-based and still correct.
if (providers.gradleProperty("kpp.frontend.cppfixture").orNull == "cpp") {
    val krappedDir =
        layout.buildDirectory
            .dir("krapped-cpp")
            .get()
            .asFile
    val krapper =
        rootProject.layout.projectDirectory
            .file("krapper/build/bin/klinker/krapperRelease/krapper")
            .asFile
    val manifest = File(projectDir, "krapped/requested.txt")
    val headerPath = file(fixtureHeader).absolutePath
    // Realize the task EAGERLY (`.get()`) so the plugin's own configuration action — the one
    // that installs its two-binary doLast — has already run by the time we clear the actions.
    afterEvaluate {
        val sync = tasks.named("kplusplusSync").get()
        sync.dependsOn(":krapper:linkReleaseExecutableKlinker")
        sync.actions.clear()
        sync.doLast {
            val requested =
                if (manifest.exists()) {
                    manifest
                        .readLines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()
                } else {
                    emptyList()
                }
            krappedDir.mkdirs()
            val args =
                listOf(
                    krapper.absolutePath,
                    "cppfixture",
                    "-r",
                    "INCLUDE_MISSING",
                    "-p",
                    "krapper.cppfixture",
                    "-c",
                    "clang++",
                    "-o",
                    krappedDir.absolutePath,
                    "--std",
                    "c++14",
                ) + requested.flatMap { listOf("--instantiate", it) } +
                    listOf("--header", headerPath)
            println("kplusplusSync[cppfixture]: krapper over ${requested.size} instantiation(s)")
            val exit = ProcessBuilder(args).inheritIO().start().waitFor()
            if (exit != 0) {
                throw GradleException("kplusplusSync[cppfixture]: krapper failed: exit $exit")
            }
            File(krappedDir, "generated.txt").writeText(requested.joinToString("\n") + "\n")
        }
    }
}
