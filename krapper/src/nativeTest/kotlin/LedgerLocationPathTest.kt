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

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.BindingIndex
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.getcwd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.posix.chdir

/**
 * #258 — a drop's `file` in `binding-index.json` must not depend on krapper's working directory.
 *
 * The header is handed over by a RELATIVE path from two different working directories, so the
 * two spellings differ ("inc/p.h" vs "../../inc/p.h"). Both runs must record the same `file`:
 * the header relative to the index's own directory, which is where any consumer resolves it.
 */
class LedgerLocationPathTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val header = """
        #pragma once
        namespace p {
        struct Base { int bv(); };
        struct Derived : Base { int dv(); };
        }
    """.trimIndent()

    /** Index [relativeHeader] from [cwd] into [out] and return the recorded drop files. */
    private fun dropFiles(cwd: File, relativeHeader: String, out: File): List<String?> =
        runBlocking {
            val original = getcwd()
            check(chdir(cwd.path) == 0) { "chdir ${cwd.path}" }
            try {
                val service = KrapperServiceImpl()
                service.setConfig(
                    KrapperConfig(
                        pkg = "p",
                        compiler = "clang++",
                        moduleName = "P",
                        errorPolicy = ErrorPolicy.LOG,
                        // Base is unlisted, so Derived is ledgered as a RESOLVE drop.
                        referencePolicy = ReferencePolicy.IGNORE_MISSING,
                        debug = false
                    )
                )
                val indexed = service.index(IndexRequest(listOf(relativeHeader), emptyList()))
                indexed.filterAndResolve(AllowListFilter(listOf("p::Derived")))
                indexed.writeTo(out.path)
            } finally {
                chdir(original)
            }
            json.decodeFromString(
                BindingIndex.serializer(),
                File(out, BindingIndex.FILE_NAME).readText()
            ).drops.map { it.location?.file }
        }

    @Test
    fun drop_file_is_relative_to_the_index_whatever_the_working_directory() {
        val dir = File.createTempDir("krapper_ledger_path")
        try {
            File(dir, "inc").mkdirs()
            File(File(dir, "inc"), "p.h").writeText(header)
            val deep = File(File(dir, "b"), "c").also { it.mkdirs() }
            val out = File(dir, "out")

            val fromRoot = dropFiles(dir, "inc/p.h", out)
            val fromDeep = dropFiles(deep, "../../inc/p.h", out)

            assertEquals(listOf("../inc/p.h"), fromRoot, "one drop, naming the real header")
            assertEquals(fromRoot, fromDeep, "the same header from another cwd, the same file")
            assertEquals(header, File("${out.path}/${fromRoot.single()}").readText())
        } finally {
            dir.rmR()
        }
    }
}
