/*
 * Copyright 2022 Jason Monk
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

import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Hermeticity lock (issue #11a, process-global determinism): two sequential generation
 * runs IN ONE PROCESS over the SAME input must produce byte-identical output.
 *
 * This mimics service-mode reuse — the v2 plugin's `kplusplusSync` drives krapper_gen as
 * a long-lived process and issues one generation per `IndexedServiceImpl` instance, so the
 * process-global state (the WrappedType intern cache + rootPackage in GenerationContext,
 * the DropLedger, and the libclang cursor->element memo in ModelFactories) outlives any
 * single run. Each `IndexedServiceImpl.init` resets all three; if any leaked, run #2 would
 * build on run #1's already-resolved/mutated elements and the generated bindings would
 * diverge. Asserting identical output is the determinism gate the self-hosted stage-0 flip
 * depends on.
 */
class DeterminismTest {

    private fun config() = KrapperConfig(
        pkg = "krapper.slice",
        compiler = "clang++",
        moduleName = "slice",
        errorPolicy = ErrorPolicy.LOG,
        referencePolicy = ReferencePolicy.INCLUDE_MISSING,
        debug = false
    )

    // One full generation run (parse + force std::vector<int> + write) into a fresh dir,
    // returning every generated file as relativePath -> content.
    private suspend fun generateOnce(): Map<String, String> {
        val outDir = File.createTempDir("krapper_determinism")
        try {
            val service = IndexedServiceImpl(config(), IndexRequest(emptyList(), emptyList()))
            service.requestInstantiation(InstantiationRequest("std::vector", listOf("int")))
            service.writeTo(outDir.path)
            val files = mutableMapOf<String, String>()
            collect(outDir, outDir, files)
            return files
        } finally {
            outDir.rmR()
        }
    }

    private fun collect(dir: File, base: File, into: MutableMap<String, String>) {
        for (file in dir.listFiles().sortedBy { it.name }) {
            if (file.isDir()) {
                collect(file, base, into)
            } else {
                into[file.path.substring(base.path.length)] = file.readText()
            }
        }
    }

    @Test
    fun twoInProcessRunsAreByteIdentical(): Unit = runBlocking {
        val first = generateOnce()
        val second = generateOnce()

        assertTrue(
            first.isNotEmpty(),
            "the generation must produce at least one output file to compare"
        )
        // Same set of generated files.
        assertEquals(
            first.keys.sorted(),
            second.keys.sorted(),
            "the second in-process run produced a different set of files — a process-global " +
                "leaked across runs"
        )
        // Identical content for every file.
        for ((path, content) in first) {
            assertEquals(
                content,
                second[path],
                "generated file $path differs between two in-process runs — a process-global " +
                    "leaked across runs (issue #11 hermeticity)"
            )
        }
    }
}
