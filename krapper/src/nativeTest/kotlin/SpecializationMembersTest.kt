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
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.InstantiationRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * #332: a forced class-template instantiation bound the PRIMARY template's members whatever
 * specialization clang picked, so `Tm<b::X>` under `template<> struct Tm<b::X> { long w; }`
 * emitted a getter for the primary's `v` and the wrapper failed to compile. An explicit
 * specialization now binds its own members and a partial specialization its pattern's. Each
 * case runs the real service path, which compiles the generated `.cc`.
 */
class SpecializationMembersTest {

    private class Output(val wrapper: String, val kotlin: Map<String, String>) {
        /** The Kotlin file binding [spec]. */
        fun bindingOf(spec: String): String = kotlin.values
            .singleOrNull { "@krapper.CppBinding(\"$spec\")" in it }
            ?: error("no single binding of $spec in ${kotlin.keys}")
    }

    private val header =
        "#pragma once\n" +
            "namespace b { struct X { int i; }; }\n" +
            "namespace p {\n" +
            "template<class T> struct Tm { int v; int get() const { return v; } };\n" +
            "template<> struct Tm<b::X> {\n" +
            "  long w; int get() const { return 1; } long twice() const { return w * 2; }\n" +
            "};\n" +
            "template<class T> struct Tm<T*> { T *q; short s; };\n" +
            "}\n" +
            // Un-namespaced, so it also gets the generated `interface Bx<T>` + facade.
            "template<class T> struct Bx { T get() const { return T(); } };\n" +
            "template<> struct Bx<long> {\n" +
            "  int get() const { return 1; } long twice() const { return 2; }\n" +
            "};\n"

    private fun generate(vararg args: String): Output =
        generate(args.map { InstantiationRequest("p::Tm", listOf(it)) })

    private fun generate(requests: List<InstantiationRequest>): Output = runBlocking {
        val dir = File.createTempDir("krapper_specialization")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            val out = File(dir, "out")
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    compiler = "clang++",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = ReferencePolicy.IGNORE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(listOf("b::X")))
            requests.forEach { indexed.requestInstantiation(it) }
            // Compiles P.cc: a member the specialization lacks fails the run here.
            indexed.writeTo(out.path)
            val src = File(out, "src").listFiles()
            Output(File(out, "P.cc").readText(), src.associate { it.name to it.readText() })
        } finally {
            dir.rmR()
        }
    }

    private fun assertExplicit(out: Output) {
        val tm = out.bindingOf("p::Tm<b::X>")
        assertTrue("var w: Long" in tm && "fun twice(): Long" in tm, tm)
        assertFalse("var v" in tm, tm)
        assertTrue("p_Tm_b_X_w_get" in out.wrapper, out.wrapper)
        assertFalse("p_Tm_b_X_v_" in out.wrapper, out.wrapper)
    }

    private fun assertPrimary(out: Output) {
        val tm = out.bindingOf("p::Tm<int>")
        assertTrue("var v: Int" in tm, tm)
        assertFalse("var w" in tm || "var s" in tm || "twice" in tm, tm)
    }

    @Test
    fun an_explicit_specialization_binds_its_own_members() = assertExplicit(generate("b::X"))

    @Test
    fun a_partial_specialization_binds_its_pattern_members() {
        val out = generate("int*")
        val tm = out.bindingOf("p::Tm<int*>")
        // `T *q` substituted with the partial's deduced `T = int`, not the written `int*`.
        assertTrue("var s: Short" in tm && "var q: CValuesRef<IntVar>?" in tm, tm)
        assertFalse("var v" in tm, tm)
        assertTrue("->q" in out.wrapper && "->v" !in out.wrapper, out.wrapper)
    }

    @Test
    fun the_primary_still_binds_where_no_specialization_matches() = assertPrimary(generate("int"))

    @Test
    fun a_specialization_and_a_primary_instantiation_bind_side_by_side() {
        val out = generate("b::X", "int")
        assertExplicit(out)
        assertPrimary(out)
    }

    @Test
    fun the_template_interface_offers_only_what_every_instantiation_binds() {
        val out = generate(listOf("int", "long").map { InstantiationRequest("Bx", listOf(it)) })
        val bxLong = out.bindingOf("Bx<long>")
        assertTrue(") : Bx<Long> {" in bxLong, bxLong)
        assertTrue("override fun get(): Int" in bxLong && "fun twice(): Long" in bxLong, bxLong)
        assertTrue("override fun get(): Int" in out.bindingOf("Bx<int>"))
        // `get` returns `int` on both, so the interface fixes it to Int (the primary alone
        // would have made it `T`); `twice` exists only on the specialization.
        val facade = out.kotlin.getValue("root_Bx_Facade.kt")
        assertTrue("interface Bx<T> {\n    fun get(): kotlin.Int\n" in facade, facade)
        assertFalse("twice" in facade, facade)
    }
}
