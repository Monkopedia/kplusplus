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
// #47 flip brick B2 — the GENERALITY DEMO. A minimal, std-free binding consumer that
// proves the GENERIC cpp-front-end path: under `-Pkpp.frontend.cppfixture=cpp` the kplusplus
// plugin drives THIS module's own kplusplus { header(...) } config through cppfrontend ->
// ModelIo -> krapper_gen --frontend=cpp into build/krapped-cpp, then compiles + runs the
// tests against those cpp-front-end bindings — exactly the same generic machinery featuregen
// now uses, on a different module's own config. The default `./gradlew` never sees this
// module (it is included only under the LLVM gate in settings.gradle.kts), so it cannot
// affect the default build.
//
// Run the demo:
//   ./gradlew :cppfixture:nativeTest -PenableClang                       (libclang path)
//   ./gradlew :cppfixture:nativeTest -PenableClang -Pkpp.frontend.cppfixture=cpp  (cpp path)

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
