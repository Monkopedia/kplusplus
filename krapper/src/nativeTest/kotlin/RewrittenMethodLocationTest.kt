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
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * #255 — a method the parse-time return rewrites replaced (here `llvm::StringRef` ->
 * `std::string`) must keep its `file:line:col` when it later fails to resolve, exactly as an
 * unrewritten sibling dropped for the same reason does.
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

    @Test
    fun rewritten_and_unrewritten_drops_both_keep_their_line() = runBlocking {
        val dir = File.createTempDir("krapper_rewritten_location")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            val run = KrapperRun(GenerationContext())
            KrapperRun.using(run) {
                // Hidden is unlisted, so both methods' `Hidden` argument won't bind.
                val resolver = parseHeader(headerFile.path, "c++14")
                resolver.findClasses(AllowListFilter(listOf("p::S")).wrapperFilter())
                    .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
            }
            val drops = listOf("viewed", "plain").associateWith { name ->
                run.drops.drops.single { "$name(" in it.symbol }
            }
            val viewed = drops.getValue("viewed").symbol
            assertTrue(viewed.endsWith("std::string"), "precondition: not rewritten: $viewed")
            val lines = drops.mapValues { it.value.location?.line }
            assertEquals(mapOf("viewed" to 6, "plain" to 7), lines, "${run.drops.drops}")
        } finally {
            dir.rmR()
        }
    }
}
