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

import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * #252 — the lenient parse-error default must not bind clang's ERROR-RECOVERED declaration.
 *
 * Clang does not delete a declaration it cannot parse: it keeps it, marks it `invalid`, and
 * substitutes `int` for the unknown type. Before the fix the walker never read that flag, so
 * `void set(Handle h)` with `Handle` undeclared shipped as `set(h: Int)` while the drop ledger
 * simultaneously reported the parse error as a drop — output and ledger disagreeing.
 *
 * Each case runs the real pipeline (`parseHeader` -> `findClasses` -> `resolveAll` ->
 * `KotlinWriter`) and asserts the EMITTED Kotlin and the ledger together. The typedef-present
 * control is what keeps "emit nothing" from passing: the same method must still bind, with
 * its real type, and nothing may be ledgered.
 */
class InvalidDeclTest {

    private class Outcome(
        val kotlin: String,
        val bound: List<String>,
        val drops: List<DropRecord>,
        val headerPath: String
    )

    private fun generate(header: String): Outcome = runBlocking {
        val dir = File.createTempDir("krapper_invalid_decl")
        try {
            val headerFile = File(dir, "lib.h").also { it.writeText(header) }
            val run = KrapperRun(GenerationContext())
            KrapperRun.using(run) {
                val resolver = parseHeader(headerFile.path, "c++17")
                val classes = resolver.findClasses { defaultFilter() }
                    .resolveAll(resolver, ReferencePolicy.IGNORE_MISSING)
                val srcDir = File(dir, "src")
                KotlinWriter("").generate(srcDir, classes)
                Outcome(
                    kotlin = buildString { collectKotlin(srcDir, this) },
                    bound = classes.filterIsInstance<ResolvedClass>().map { it.type.toString() },
                    drops = run.drops.drops.toList(),
                    headerPath = headerFile.path
                )
            }
        } finally {
            dir.rmR()
        }
    }

    private fun collectKotlin(dir: File, into: StringBuilder) {
        for (file in dir.listFiles().sortedBy { it.name }) {
            if (file.isDir()) collectKotlin(file, into) else into.append(file.readText())
        }
    }

    @Test
    fun an_invalid_method_is_not_emitted_and_is_ledgered() {
        val out = generate("namespace p { struct W { void set(Handle h); int get(); }; }\n")
        assertFalse(
            "fun set(" in out.kotlin,
            "the error-recovered `set` must not be bound:\n${out.kotlin}"
        )
        assertTrue("fun get(" in out.kotlin, "its valid sibling still binds:\n${out.kotlin}")
        assertTrue("p::W" in out.bound, "the class itself is valid and binds: ${out.bound}")
        val drop = out.drops.singleOrNull { it.symbol == "p::W::set" }
        assertTrue(drop != null, "`p::W::set` must be ledgered by name: ${out.drops}")
        assertEquals(DropPhase.PARSE, drop.phase)
        assertEquals(1, drop.location?.line, "the drop carries the decl's line: $drop")
    }

    // MUST DIFFER: the same header with the typedef present. Without this, a walker that
    // emitted nothing at all would satisfy the test above.
    @Test
    fun with_the_typedef_present_the_method_binds_with_its_real_type() {
        val out = generate(
            "namespace p { typedef long Handle; struct W { void set(Handle h); int get(); }; }\n"
        )
        assertTrue(
            "fun set(h: Long)" in out.kotlin,
            "a valid `set(Handle)` must bind as Long:\n${out.kotlin}"
        )
        assertEquals(emptyList(), out.drops, "a clean parse ledgers nothing")
    }

    // Through a TYPEDEF clang marks only the typedef invalid (as `int`), never its users:
    // `void f(H2 h)` is a valid decl to clang. The walk follows each signature's typedef
    // chain, so a parameter / return / field reaching an invalid typedef is dropped too —
    // through two levels here, so a one-level check would fail.
    @Test
    fun a_signature_through_an_invalid_typedef_chain_is_not_emitted_and_is_ledgered() {
        val out = generate(
            "namespace p {\ntypedef Handle H2;\ntypedef H2 H3;\n" +
                "struct W { void f(H2 h); H3 g(); void k(H3* h); int ok(); };\n" +
                "struct S { H3 v; int w; };\n}\n"
        )
        for (name in listOf("f", "g", "k")) {
            assertFalse(
                "fun $name(" in out.kotlin,
                "`$name` reaches the invalid typedef and must not be bound:\n${out.kotlin}"
            )
            assertEquals(
                1,
                out.drops.count { it.symbol == "p::W::$name" },
                "`p::W::$name` must be ledgered exactly once: ${out.drops}"
            )
        }
        assertTrue("fun ok(" in out.kotlin, "the valid sibling still binds:\n${out.kotlin}")
        assertTrue("p::S" in out.bound, "the record itself is valid and binds: ${out.bound}")
        assertFalse("var v:" in out.kotlin, "the field `v` must not be bound:\n${out.kotlin}")
        assertTrue("var w:" in out.kotlin, "the valid field `w` still binds:\n${out.kotlin}")
        assertEquals(1, out.drops.count { it.symbol == "p::S::v" }, "${out.drops}")
        // `H3` is valid to clang but aliases the invalid `H2`: it is dropped with it.
        assertEquals(1, out.drops.count { it.symbol == "p::H3" }, "${out.drops}")
    }

    // MUST DIFFER for the chain: the same two-level chain over a KNOWN type binds through.
    @Test
    fun a_valid_typedef_chain_binds_with_its_real_type() {
        val out = generate(
            "namespace p {\ntypedef long H1;\ntypedef H1 H2;\ntypedef H2 H3;\n" +
                "struct W { void f(H3 h); };\n}\n"
        )
        assertTrue("fun f(h: Long)" in out.kotlin, "a valid chain binds:\n${out.kotlin}")
        assertEquals(emptyList(), out.drops, "a clean parse ledgers nothing")
    }

    // Propagation: clang marks a RECORD invalid when one of its fields is (its layout is
    // unknown, so no wrapper could allocate it). The whole class is skipped and ledgered
    // once, by name; an unrelated valid class beside it still binds.
    @Test
    fun a_class_with_an_invalid_field_is_not_emitted_and_is_ledgered() {
        val out = generate(
            "namespace p {\nstruct F { Handle f; int g; };\nstruct Ok { int x; };\n}\n"
        )
        assertFalse("p::F" in out.bound, "the invalid record must not bind: ${out.bound}")
        assertTrue("p::Ok" in out.bound, "the valid sibling still binds: ${out.bound}")
        assertEquals(
            1,
            out.drops.count { it.symbol == "p::F" },
            "`p::F` must be ledgered exactly once: ${out.drops}"
        )
    }

    // A forward declaration is never itself invalid — only the (later) definition is. The
    // class must still be skipped, or the forward decl attaches an empty `p::Fwd`.
    @Test
    fun a_forward_declared_class_with_an_invalid_definition_is_not_emitted() {
        val out = generate("namespace p {\nstruct Fwd;\nstruct Fwd { Handle q; };\n}\n")
        assertFalse("p::Fwd" in out.bound, "the invalid record must not bind: ${out.bound}")
        assertEquals(
            1,
            out.drops.count { it.symbol == "p::Fwd" },
            "`p::Fwd` must be ledgered exactly once: ${out.drops}"
        )
    }

    // The ledger names the user's header, not the virtual `parity_base.cc` the base parse
    // runs under — both the parse-error rows and the per-declaration rows.
    @Test
    fun ledger_locations_name_the_real_header() {
        val out = generate("namespace p { struct W { void set(Handle h); }; }\n")
        assertTrue(out.drops.isNotEmpty(), "control: the bad header must ledger something")
        for (drop in out.drops) {
            assertEquals(
                out.headerPath,
                drop.location?.file,
                "every drop must point at the real header: $drop"
            )
        }
    }
}
