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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.codegen.DefWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.NameHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins [DefWriter] — the cpp path's cinterop `.def` writer — to the golden the Gradle plugin's
 * `.def` reader is tested against (#251).
 *
 * The plugin declares the cinterop task's inputs by READING the generated `.def`, so the two can
 * only drift if `DefWriter` starts emitting a shape the reader does not resolve. The plugin is
 * JVM and `DefWriter` is Kotlin/Native, so they meet at one checked-in file:
 * `compiler/gradle/src/test/resources/defwriter-golden.def`. If this fails, `DefWriter`'s output
 * changed — update the golden and make `CinteropWrapperInputsTest` pass against it.
 */
class DefWriterGoldenTest {

    // The test binary runs with the krapper project dir as its working directory.
    private val golden = File("../compiler/gradle/src/test/resources/defwriter-golden.def")

    @Test
    fun `DefWriter emits the golden def for a module linking an out-of-tree archive`() {
        assertTrue(golden.parent.isDir(), "golden fixture dir not found: ${golden.parent.path}")
        val emitted = DefWriter(NameHandler()).generateDef(
            outputBase = File("/@KRAPPED@"),
            pkg = "krapper.demo.internal",
            moduleName = "demo",
            headers = emptyList(),
            libraries = listOf("/@PREBUILT@/libfoo.a")
        )
        assertEquals(golden.readText(), emitted, "DefWriter drifted from ${golden.path}")
    }
}
