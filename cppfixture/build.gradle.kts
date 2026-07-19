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
// module's own kplusplus { header(...) } config through krapper_parse -> ModelIo -> krapper_gen
// into build/krapped-cpp, then compiles + runs the tests against those bindings — the same
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

kplusplus {
    // Single, std-free header — the cpp front-end parses it without any libstdc++ weight,
    // so this module is "cpp-ready" today (unlike v8/clang-slice consumers).
    header("include/point2d.h")
}
