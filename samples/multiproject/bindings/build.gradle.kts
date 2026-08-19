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

// kplusplus is applied HERE, in a subproject, while the Kotlin plugin comes from the root's
// `plugins { }` block (see ../build.gradle.kts and ../settings.gradle.kts for why that
// arrangement is the whole point of this sample). The version pin matches samples/minimal's:
// this is a from-published consumer.
plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler") version "0.3.6"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                    // The generated bindings carry `context(scope: MemScope)` on their
                    // allocating members, which needs Kotlin 2.4 context parameters.
                    freeCompilerArgs.add("-Xcontext-parameters")
                }
            }
        }
    }
}

// One header, no library, no fixups — see cpp/counter.h.
kplusplus {
    header("cpp/counter.h")
}
