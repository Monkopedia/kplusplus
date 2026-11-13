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
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * #255 — a method the parse-time return rewrites replaced (here `llvm::StringRef` ->
 * `std::string`) must keep its `file:line:col` when it later fails to resolve, exactly as an
 * unrewritten sibling dropped for the same reason does. A template instantiation, which has no
 * declaration of its own, is reported at the template's.
 */
class RewrittenMethodLocationTest {

    // Line numbers are load-bearing: `viewed` is on line 6, `plain` on line 7.
    private val header = """
        #pragma once
        namespace llvm { struct StringRef { const char* data; }; }
        namespace p {
        struct Hidden { int h; };
        struct S {
          llvm::StringRef viewed(Hidden h);
          int plain(Hidden h);
        };
        }
    """.trimIndent()

    /** Resolve [allow] from [header]; Hidden is unlisted, so anything naming it won't bind. */
    private fun drops(vararg allow: String): List<DropRecord> = runBlocking {
        val dir = File.createTempDir("krapper_rewritten_location")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            val run = KrapperRun(GenerationContext())
            KrapperRun.using(run) {
                val resolver = parseHeader(headerFile.path, "c++14")
                resolver.findClasses(AllowListFilter(allow.toList()).wrapperFilter())
                    .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            }
            run.drops.drops.toList()
        } finally {
            dir.rmR()
        }
    }

    @Test
    fun rewritten_and_unrewritten_drops_both_keep_their_line() {
        val all = drops("p::S")
        val drops = listOf("viewed", "plain").associateWith { name ->
            all.single { "$name(" in it.symbol }
        }
        val viewed = drops.getValue("viewed").symbol
        assertTrue(viewed.endsWith("std::string"), "precondition: not rewritten: $viewed")
        val lines = drops.mapValues { it.value.location?.line }
        assertEquals(mapOf("viewed" to 6, "plain" to 7), lines, "$all")
    }

    @Test
    fun template_instantiation_is_reported_at_the_template() = runBlocking {
        val dir = File.createTempDir("krapper_instantiation_location")
        try {
            // std::pair's base (std::__pair_base) doesn't resolve, so the instantiation itself
            // is ledgered: the featuregen row that was null.
            val headerFile = File(dir, "p.h").also { it.writeText("#include <utility>\n") }
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    // The index is written only after the wrapper compile; `true` stands in.
                    compiler = "true",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = ReferencePolicy.IGNORE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(emptyList()))
            indexed.requestInstantiation(InstantiationRequest("std::pair", listOf("int", "int")))
            val out = File(dir, "out")
            indexed.writeTo(out.path)
            val all = Json { ignoreUnknownKeys = true }.decodeFromString(
                BindingIndex.serializer(),
                File(out, BindingIndex.FILE_NAME).readText()
            ).drops
            val pair = all.singleOrNull { it.symbol == "std::pair<int, int>" }
            assertTrue(pair?.location?.file?.endsWith("/stl_pair.h") == true, "$all")
        } finally {
            dir.rmR()
        }
    }
}
