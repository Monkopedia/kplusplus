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
package com.monkopedia.kplusplus.compiler.fir

import java.io.File

/**
 * featuregen's real instantiation surface — the denominator both index gates resolve: the
 * committed manifest the FIR checker grew, plus the build-seeded `instantiate(...)` calls
 * (`Box<int>`, `std::vector<Thing*>`, …) which the checker cannot request because they are used
 * through generated facades.
 *
 * Read from the repo rather than hand-listed, so a new feature row that adds an instantiation is
 * covered automatically. The paths come in as system properties set by
 * `compiler/plugin/build.gradle.kts`.
 */
internal object FeaturegenSurface {
    /** featuregen's committed `krapped/requested.txt`. */
    const val REQUESTED_PROPERTY = "kplusplus.test.featuregenRequested"

    /** featuregen's `build.gradle.kts`. */
    const val BUILD_SCRIPT_PROPERTY = "kplusplus.test.featuregenBuildScript"

    /** featuregen instantiated 17 distinct specs when the coverage gate was written. */
    const val MIN_EXPECTED_SPECS = 17

    private val INSTANTIATE_CALL = Regex("""instantiate\("([^"]+)"\)""")

    val specs: List<String> by lazy {
        val requested = repoFile(REQUESTED_PROPERTY).readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() }
        val seeded = INSTANTIATE_CALL.findAll(repoFile(BUILD_SCRIPT_PROPERTY).readText())
            .map { it.groupValues[1] }
            .toList()
        check(seeded.isNotEmpty()) {
            "No `instantiate(\"…\")` calls found in ${repoFile(BUILD_SCRIPT_PROPERTY)}; the " +
                "denominator would be short by featuregen's user-template surface."
        }
        (requested + seeded).distinct().sorted().also {
            // A silently-short parse would let every gate over it pass over nothing.
            check(it.size >= MIN_EXPECTED_SPECS) {
                "Only ${it.size} specs came out of $REQUESTED_PROPERTY and " +
                    "$BUILD_SCRIPT_PROPERTY; featuregen had $MIN_EXPECTED_SPECS when this was " +
                    "written, so the read is probably broken rather than the surface shrunk."
            }
        }
    }

    fun repoFile(property: String): File {
        val path = System.getProperty(property)
            ?: error(
                "System property $property is unset; it is wired up in " +
                    "compiler/plugin/build.gradle.kts."
            )
        return File(path).also { check(it.isFile) { "$property does not point at a file: $path" } }
    }
}
