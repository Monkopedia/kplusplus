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
import com.monkopedia.krapper.InstantiationRequest
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
 * #340: forcing a type clang cannot complete emitted `struct KrapperForce_… { T value; }`
 * into the wrapper, which failed with `field has incomplete type` and killed the run. Such a
 * request is now dropped and ledgered, its forcing header is not emitted, and the good
 * request beside it still binds. Each case runs the real service path (the same
 * `requestInstantiation` the CLI's `--instantiate` calls), which compiles the generated `.cc`.
 */
class ForcedIncompleteTest {

    private class Output(
        val wrapper: String,
        val kotlin: Map<String, String>,
        val files: List<String>,
        val drops: List<Diagnostic>
    )

    private val header = "#pragma once\n" +
        "namespace p {\n" +
        "template<class T> struct Tm { int v; };\n" +
        "template<> struct Tm<short>;\n" +
        "template<class T> struct Q;\n" +
        "template<class T> struct Sa { int ok; static_assert(sizeof(T) == 0, \"no\"); };\n" +
        "template<class T, class U> struct A { int p; };\n" +
        "template<class T> struct A<T*, int> { int x; };\n" +
        "template<class U> struct A<int*, U> { int y; };\n" +
        "}\n"

    private fun generate(bad: InstantiationRequest): Output = runBlocking {
        val dir = File.createTempDir("krapper_forced_incomplete")
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
                    referencePolicy = ReferencePolicy.INCLUDE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(listOf("p::Tm<int>")))
            // The bad request sits between two good ones: neither may die with it.
            indexed.requestInstantiation(InstantiationRequest("p::Tm", listOf("int")))
            indexed.requestInstantiation(bad)
            indexed.requestInstantiation(InstantiationRequest("p::Tm", listOf("long")))
            // Compiles P.cc, which #includes every emitted forcing header.
            indexed.writeTo(out.path)
            val src = File(out, "src").listFiles()
            val index = File(out, BindingIndex.FILE_NAME).readText()
            Output(
                File(out, "P.cc").readText(),
                src.associate { it.name to it.readText() },
                out.listFiles().map { it.name },
                Json.decodeFromString(BindingIndex.serializer(), index).drops
            )
        } finally {
            dir.rmR()
        }
    }

    private fun assertDropped(spec: String, bad: InstantiationRequest, reason: String) {
        val out = generate(bad)
        for (good in listOf("p::Tm<int>", "p::Tm<long>")) {
            assertEquals(
                1,
                out.kotlin.values.count { "@krapper.CppBinding(\"$good\")" in it },
                "${out.kotlin.keys}"
            )
        }
        assertTrue("->v" in out.wrapper, out.wrapper)
        assertFalse(out.kotlin.values.any { "@krapper.CppBinding(\"$spec\")" in it })
        val forceName = "KrapperForce_" + spec.replace("::", "_").replace("<", "_")
            .replace(">", "").replace(", ", "_").replace("*", "Ptr")
        assertFalse("$forceName.h" in out.files, "${out.files}")
        assertFalse(forceName in out.wrapper, out.wrapper)
        assertTrue("KrapperForce_p_Tm_long.h" in out.files, "${out.files}")
        val drops = out.drops.filter { it.symbol == spec }
        assertEquals(listOf(DiagnosticPhase.PARSE), drops.map { it.phase }, "${out.drops}")
        assertTrue(reason in drops.single().message, drops.single().message)
    }

    @Test
    fun a_declared_only_explicit_specialization_is_dropped_and_ledgered() = assertDropped(
        "p::Tm<short>",
        InstantiationRequest("p::Tm", listOf("short")),
        "incomplete type"
    )

    @Test
    fun an_undefined_primary_is_dropped_and_ledgered() =
        assertDropped("p::Q<int>", InstantiationRequest("p::Q", listOf("int")), "incomplete type")

    @Test
    fun an_instantiation_failing_a_static_assert_is_dropped_and_ledgered() = assertDropped(
        "p::Sa<int>",
        InstantiationRequest("p::Sa", listOf("int")),
        "invalid"
    )

    @Test
    fun an_ambiguous_partial_is_dropped_and_ledgered() = assertDropped(
        "p::A<int*, int>",
        InstantiationRequest("p::A", listOf("int*", "int")),
        "incomplete type"
    )

    @Test
    fun an_undeclared_template_is_ledgered_as_undeclared() = assertDropped(
        "p::Nope<int>",
        InstantiationRequest("p::Nope", listOf("int")),
        "undeclared type: the template or an argument p::Nope<int> names is not declared"
    )
}
