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
import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.DiagnosticPhase
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * #341: a specialization reached under INCLUDE_MISSING only through a pointer or reference
 * was never instantiated by clang, so it still named the primary template as its pattern and
 * `Tm<float*>` bound the primary's `v` — the wrapper failed with `no member named 'v'`. The
 * model builder now has clang instantiate it, so the partial specialization's members bind.
 * Each case expands `p::User` alone and runs the real service path, which compiles the
 * generated `.cc`.
 */
class PointerSpecializationTest {

    private class Output(
        val wrapper: String,
        val kotlin: Map<String, String>,
        val drops: List<Diagnostic>
    ) {
        /** The Kotlin file binding [spec]. */
        fun bindingOf(spec: String): String = kotlin.values
            .singleOrNull { "@krapper.CppBinding(\"$spec\")" in it }
            ?: error("no single binding of $spec in ${kotlin.keys}")
    }

    private fun header(user: String) = "#pragma once\n" +
        "namespace b { struct X { int i; }; }\n" +
        "namespace p {\n" +
        "template<class T> struct Tm { int v; int get() const { return v; } };\n" +
        "template<> struct Tm<b::X> { long w; long twice() const { return w * 2; } };\n" +
        "template<class T> struct Tm<T*> { T *q; short s; };\n" +
        "template<class T> struct Q;\n" +
        // No `int::nope`: clang cannot instantiate `Tm<int**>`.
        "template<class T> struct Tm<T**> { typename T::nope n; };\n" +
        // `Tm2<float**>` fails its static_assert; instantiating `O2<float*>` instantiates it.
        "template<class T> struct Tm2 { int v2; };\n" +
        "template<class T> struct Tm2<T**> { int ok; static_assert(sizeof(T) == 0, \"no\"); };\n" +
        "template<class T> struct O2 { int o; };\n" +
        "template<class T> struct O2<T*> { Tm2<T**> held; int k; };\n" +
        // `A<int*, int>` matches both partials and neither is more specialized.
        "template<class T, class U> struct A { int p; };\n" +
        "template<class T> struct A<T*, int> { int x; };\n" +
        "template<class U> struct A<int*, U> { int y; };\n" +
        // An undefined primary whose only partial does not match `U<int>`.
        "template<class T> struct U;\n" +
        "template<class T> struct U<T*> { int u; };\n" +
        "struct User { $user };\n" +
        "}\n"

    private fun generate(user: String): Output = runBlocking {
        val dir = File.createTempDir("krapper_pointer_specialization")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header(user)) }
            val out = File(dir, "out")
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    compiler = "clang++",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = ReferencePolicy.INCLUDE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(listOf("p::User")))
            // Compiles P.cc: a member the specialization lacks fails the run here.
            indexed.writeTo(out.path)
            val src = File(out, "src").listFiles()
            val index = File(out, BindingIndex.FILE_NAME).readText()
            Output(
                File(out, "P.cc").readText(),
                src.associate { it.name to it.readText() },
                Json.decodeFromString(BindingIndex.serializer(), index).drops
            )
        } finally {
            dir.rmR()
        }
    }

    private fun assertPartial(out: Output) {
        val tm = out.bindingOf("p::Tm<float*>")
        // `T *q` substituted with the partial's deduced `T = float`.
        assertTrue("var s: Short" in tm && "var q: CValuesRef<FloatVar>?" in tm, tm)
        assertFalse("var v" in tm || "fun get" in tm, tm)
        assertTrue("->q" in out.wrapper && "->v" !in out.wrapper, out.wrapper)
        assertFalse(out.drops.any { it.symbol == "p::Tm<float*>" }, "${out.drops}")
    }

    @Test
    fun a_partial_match_seen_through_a_pointer_binds_the_partial() =
        assertPartial(generate("Tm<float*> *ptr;"))

    @Test
    fun a_partial_match_seen_through_a_reference_binds_the_partial() =
        assertPartial(generate("void use(Tm<float*> &ref);"))

    @Test
    fun an_explicit_specialization_seen_through_a_pointer_binds_its_own_members() {
        val out = generate("Tm<b::X> *ptr;")
        val tm = out.bindingOf("p::Tm<b::X>")
        assertTrue("var w: Long" in tm && "fun twice(): Long" in tm, tm)
        assertFalse("var v" in tm, tm)
    }

    @Test
    fun the_primary_seen_through_a_pointer_still_binds_the_primary() {
        val out = generate("Tm<int> *ptr;")
        val tm = out.bindingOf("p::Tm<int>")
        assertTrue("var v: Int" in tm && "fun get(): Int" in tm, tm)
        assertFalse("var s" in tm || "var q" in tm || "var w" in tm, tm)
    }

    @Test
    fun an_undefined_template_seen_through_a_pointer_does_not_kill_the_run() {
        // Q has no definition, so there is nothing to instantiate: the run still completes
        // and binds the pointer member (#340 tracks dropping the unbindable type itself).
        val out = generate("Q<int> *q; Tm<float*> *ptr;")
        assertPartial(out)
        assertEquals(1, out.kotlin.values.count { "@krapper.CppBinding(\"p::User\")" in it })
    }

    @Test
    fun a_specialization_clang_cannot_instantiate_is_dropped_and_ledgered() {
        val out = generate("Tm<int**> *bad; Tm<float*> *ptr;")
        assertPartial(out)
        assertFalse(out.kotlin.values.any { "@krapper.CppBinding(\"p::Tm<int**>\")" in it })
        assertFalse("var bad" in out.bindingOf("p::User"), out.bindingOf("p::User"))
        assertEquals(
            listOf(DiagnosticPhase.PARSE),
            out.drops.filter { it.symbol == "p::Tm<int**>" }.map { it.phase },
            "${out.drops}"
        )
    }

    // Asserts [spec] is not bound, the User member [field] that names it is dropped, and
    // [spec] is ledgered once, at parse.
    private fun assertDropped(out: Output, spec: String, field: String) {
        assertPartial(out)
        assertFalse(out.kotlin.values.any { "@krapper.CppBinding(\"$spec\")" in it })
        val user = out.bindingOf("p::User")
        assertFalse("var $field" in user, user)
        assertEquals(
            listOf(DiagnosticPhase.PARSE),
            out.drops.filter { it.symbol == spec }.map { it.phase },
            "${out.drops}"
        )
    }

    private fun ledger(out: Output) = out.drops.map { it.symbol to it.phase }.toSet()

    @Test
    fun a_specialization_an_earlier_one_left_invalid_is_dropped_in_either_order() {
        val outerFirst = generate("O2<float*> *outer; Tm2<float**> *bad; Tm<float*> *ptr;")
        val innerFirst = generate("Tm2<float**> *bad; O2<float*> *outer; Tm<float*> *ptr;")
        for (out in listOf(outerFirst, innerFirst)) {
            assertDropped(out, "p::Tm2<float**>", "bad")
            assertDropped(out, "p::O2<float*>", "outer")
        }
        assertEquals(ledger(innerFirst), ledger(outerFirst))
    }

    @Test
    fun a_specialization_with_ambiguous_partials_is_dropped_and_ledgered() =
        assertDropped(generate("A<int*, int> *amb; Tm<float*> *ptr;"), "p::A<int*, int>", "amb")

    @Test
    fun an_undefined_primary_no_partial_matches_is_dropped_and_ledgered() =
        // Unlike `Q` (no partials, nothing asked of clang), `U` has a partial, so clang is
        // asked to complete `U<int>` and cannot: it has no members to bind.
        assertDropped(generate("U<int> *un; Tm<float*> *ptr;"), "p::U<int>", "un")
}
