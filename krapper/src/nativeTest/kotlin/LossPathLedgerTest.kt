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
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * The two ways a binding used to lose API with an EMPTY drop ledger, so `failOnDrop` (which
 * reads only the ledger) could not see them (#253). Driven through the real parse ->
 * resolve -> emit path the plugin runs, under [LogPolicy] (the plugin's hardcoded
 * `ErrorPolicy.LOG`).
 *
 *  - Path 1 (IGNORE_MISSING): an unlisted public base is unresolvable, so the derived class
 *    loses its inherited members and `asBase()`. That must be a RESOLVE drop.
 *  - Path 2 (OPAQUE_MISSING): the Kotlin writer throws on the class; LOG swallowed it and
 *    left a `.kt` ending in a dangling annotation. That must be a GENERATE drop, and the
 *    half-written class must be discarded rather than written.
 *  - The must-differ case: listing both classes drops nothing.
 */
class LossPathLedgerTest {

    private val header = """
        namespace p {
        struct Base { int bv(); };
        struct Derived : Base { int dv(); };
        }
    """.trimIndent()

    private class Output(
        val drops: List<DropRecord>,
        val cHeader: String,
        val kotlin: Map<String, String>
    )

    private fun generate(only: List<String>, policy: ReferencePolicy): Output = runBlocking {
        val run = KrapperRun(GenerationContext())
        val dir = File.createTempDir("krapper_loss_path")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            KrapperRun.using(run) {
                val resolver = parseHeader(headerFile.path, "c++14")
                val classes = resolver.findClasses(AllowListFilter(only).wrapperFilter())
                    .resolveAll(resolver, policy)
                val cHeader = CppCodeBuilder().also {
                    HeaderWriter(it, LogPolicy).generate("P", listOf(headerFile.path), classes)
                }.toString()
                val srcDir = File(dir, "src")
                KotlinWriter("p.internal", LogPolicy).generate(srcDir, classes)
                Output(
                    run.drops.drops.toList(),
                    cHeader,
                    srcDir.listFiles().associate { it.name to it.readText() }
                )
            }
        } finally {
            dir.rmR()
        }
    }

    @Test
    fun both_classes_listed_binds_inherited_members_and_drops_nothing() {
        val out = generate(listOf("p::Derived", "p::Base"), ReferencePolicy.IGNORE_MISSING)
        val derived = out.kotlin.getValue("p_Derived.kt")
        assertTrue("fun bv(" in derived, "inherited bv() must bind:\n$derived")
        assertTrue("fun asBase(" in derived, "asBase() must bind:\n$derived")
        assertEquals(emptyList(), out.drops, "a clean config must not record a drop")
    }

    @Test
    fun unresolvable_base_is_ledgered_as_a_resolve_drop() {
        val out = generate(listOf("p::Derived"), ReferencePolicy.IGNORE_MISSING)
        val derived = out.kotlin.getValue("p_Derived.kt")
        assertTrue("fun dv(" in derived, "own members still bind:\n$derived")
        assertFalse("fun bv(" in derived, "precondition: inherited bv() is lost:\n$derived")
        val drop = out.drops.singleOrNull { it.symbol == "p::Derived" }
        assertTrue(
            drop != null && drop.phase == DropPhase.RESOLVE && "p::Base" in drop.reason,
            "the lost base must be ledgered against p::Derived: ${out.drops}"
        )
    }

    @Test
    fun codegen_failure_is_ledgered_and_the_half_written_class_discarded() {
        val out = generate(listOf("p::Derived"), ReferencePolicy.OPAQUE_MISSING)
        val drop = out.drops.singleOrNull { it.symbol == "p::Derived" }
        assertTrue(
            drop != null && drop.phase == DropPhase.GENERATE,
            "the class the Kotlin writer failed on must be ledgered: ${out.drops}"
        )
        assertFalse(
            "p_Derived.kt" in out.kotlin,
            "the half-written class must be discarded, not left truncated:\n" +
                out.kotlin["p_Derived.kt"]
        )
    }
}
