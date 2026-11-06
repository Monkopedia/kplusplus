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
import com.monkopedia.krapper.DiagnosticPhase
import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.builders.LogPolicy
import com.monkopedia.krapper.generator.builders.ThrowPolicy
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.codegen.generateBindings
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import com.monkopedia.krapper.generator.resolvedmodel.ReturnStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * The ways a binding used to lose API silently or leave broken output, driven through the real
 * parse -> resolve -> emit path the plugin runs, under [LogPolicy] (the plugin's hardcoded
 * `ErrorPolicy.LOG`).
 *
 *  - Path 1 (IGNORE_MISSING): an unlisted public base is unresolvable, so the derived class
 *    loses its inherited members and `asBase()`. That must be a RESOLVE drop (#253).
 *  - Path 2 (OPAQUE_MISSING): the Kotlin writer throws on the class. It must be a GENERATE
 *    drop, and absent from EVERY output, binding-index.json included (#253, #260).
 *  - A member one writer fails on, or fails on half-way, must be absent from every output
 *    while its class keeps the rest (#260).
 *  - The must-differ cases: a clean config drops nothing and emits exactly what the writers do.
 */
class LossPathLedgerTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val header = """
        #pragma once
        namespace p {
        struct Base { int bv(); };
        struct Derived : Base { int dv(); };
        struct Val { int x; };
        struct Holder { int hv(); Val make(); Val v; };
        }
    """.trimIndent()

    private class Output(
        val drops: List<DropRecord>,
        val cHeader: String,
        val cWrapper: String,
        val kotlin: Map<String, String>
    ) {
        /** Every emitted output, for "absent everywhere" checks. */
        val all: String
            get() = cHeader + cWrapper + kotlin.values.joinToString("")
    }

    private fun generate(
        only: List<String>,
        policy: ReferencePolicy,
        breakModel: (List<ResolvedElement>) -> Unit = {}
    ): Output = runBlocking {
        val run = KrapperRun(GenerationContext())
        val dir = File.createTempDir("krapper_loss_path")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            KrapperRun.using(run) {
                val resolver = parseHeader(headerFile.path, "c++14")
                val classes = resolver.findClasses(AllowListFilter(only).wrapperFilter())
                    .resolveAll(resolver, policy)
                breakModel(classes)
                val srcDir = File(dir, "src")
                val bindings = generateBindings(
                    "P",
                    listOf(headerFile.path),
                    classes,
                    File(dir, "P.cc"),
                    "p.internal",
                    srcDir,
                    LogPolicy
                )
                Output(
                    run.drops.drops.toList(),
                    bindings.header,
                    bindings.wrapper,
                    srcDir.listFiles().associate { it.name to it.readText() }
                )
            }
        } finally {
            dir.rmR()
        }
    }

    private fun List<ResolvedElement>.holder() =
        filterIsInstance<ResolvedClass>().single { it.type.toString() == "p::Holder" }

    private fun List<ResolvedElement>.holderMethod(name: String) =
        holder().children.filterIsInstance<ResolvedMethod>().single { it.name == name }

    private fun List<ResolvedElement>.holderField(name: String) =
        holder().children.filterIsInstance<ResolvedField>().single { it.name == name }

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
    fun class_a_writer_fails_on_is_ledgered_once_and_absent_from_every_output() {
        val out = generate(listOf("p::Derived", "p::Val"), ReferencePolicy.OPAQUE_MISSING)
        assertEquals(
            listOf("p::Derived" to DropPhase.GENERATE),
            out.drops.map { it.symbol to it.phase },
            "the class the Kotlin writer failed on must be ledgered once"
        )
        assertFalse("p_Derived.kt" in out.kotlin, "no .kt for the failed class")
        assertFalse(
            "Derived" in out.cHeader + out.cWrapper,
            "the C side must not keep wrappers for a class Kotlin cannot bind:\n" +
                out.cHeader + out.cWrapper
        )
        // Must-differ: the clean class in the same run is untouched.
        assertTrue("p_Val.kt" in out.kotlin && "p_Val_x_get" in out.cWrapper, out.all)
    }

    @Test
    fun member_only_the_cpp_writer_fails_on_is_dropped_from_every_output() {
        // An ARG_CAST return whose ret slot is not a pointer: only CppWriter rejects it, so the
        // header and the Kotlin would otherwise reference a symbol the .cc never defines.
        val out = generate(listOf("p::Holder", "p::Val"), ReferencePolicy.OPAQUE_MISSING) {
            val make = it.holderMethod("make")
            check(make.returnStyle == ReturnStyle.ARG_CAST) { "precondition: $make" }
            make.argCastNeedsPointer = false
        }
        assertEquals(
            listOf("p::Holder::make" to DropPhase.GENERATE),
            out.drops.map { it.symbol to it.phase }
        )
        assertFalse("p_Holder_make" in out.all, "make must be gone from every output:\n${out.all}")
        assertTrue("p_Holder_hv" in out.cWrapper, "the class keeps its other members")
        assertTrue("fun hv(" in out.kotlin.getValue("p_Holder.kt"))
    }

    @Test
    fun member_a_writer_fails_on_half_way_leaves_no_fragment() {
        // A by-value field whose setter is unnamed: every writer emits the getter, then throws
        // on the setter, so the field would otherwise ship as a getter-only fragment.
        val out = generate(listOf("p::Holder", "p::Val"), ReferencePolicy.OPAQUE_MISSING) {
            it.holderField("v").setter.uniqueCName = null
        }
        assertEquals(
            listOf("p::Holder::v" to DropPhase.GENERATE),
            out.drops.map { it.symbol to it.phase },
            "one failed field is one drop, however many writers threw on it"
        )
        assertFalse("p_Holder_v_" in out.all, "no fragment of v may remain:\n${out.all}")
        assertTrue("p_Holder_make" in out.cWrapper && "p_Holder_hv" in out.cWrapper)
    }

    @Test
    fun clean_model_emits_exactly_what_the_writers_do() = runBlocking {
        val run = KrapperRun(GenerationContext())
        val dir = File.createTempDir("krapper_loss_path_clean")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header) }
            val headers = listOf(headerFile.path)
            val cppFile = File(dir, "P.cc")
            KrapperRun.using(run) {
                val resolver = parseHeader(headerFile.path, "c++14")
                val classes = resolver.findClasses(
                    AllowListFilter(listOf("p::Base", "p::Derived", "p::Holder", "p::Val"))
                        .wrapperFilter()
                ).resolveAll(resolver, ReferencePolicy.OPAQUE_MISSING)
                val direct = File(dir, "direct")
                KotlinWriter("p.internal", ThrowPolicy).generate(direct, classes)
                val viaBindings = File(dir, "bindings")
                val bindings = generateBindings(
                    "P",
                    headers,
                    classes,
                    cppFile,
                    "p.internal",
                    viaBindings,
                    LogPolicy
                )
                assertEquals(
                    CppCodeBuilder().also {
                        HeaderWriter(it, ThrowPolicy).generate("P", headers, classes)
                    }.toString(),
                    bindings.header
                )
                assertEquals(
                    CppCodeBuilder(qualifyFunctionPointers = true).also {
                        CppWriter(cppFile, it, ThrowPolicy).generate("P", headers, classes)
                    }.toString(),
                    bindings.wrapper
                )
                assertEquals(
                    direct.listFiles().associate { it.name to it.readText() },
                    viaBindings.listFiles().associate { it.name to it.readText() }
                )
                assertTrue(bindings.emitted === classes, "a clean model is emitted as given")
                assertEquals(emptyList(), run.drops.drops.toList())
            }
        } finally {
            dir.rmR()
        }
    }

    @Test
    fun binding_index_leaves_out_a_class_a_writer_failed_on() = runBlocking {
        val dir = File.createTempDir("krapper_loss_path_index")
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
                    referencePolicy = ReferencePolicy.OPAQUE_MISSING,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(listOf("p::Derived", "p::Val")))
            indexed.writeTo(out.path)
            val index = json.decodeFromString(
                BindingIndex.serializer(),
                File(out, BindingIndex.FILE_NAME).readText()
            )
            assertEquals(
                listOf("p::Val"),
                index.bindings.map { it.spec },
                "the index must not advertise a class with no .kt"
            )
            assertEquals(
                listOf("p::Derived" to DiagnosticPhase.GENERATE),
                index.drops.map { it.symbol to it.phase }
            )
            assertFalse("Derived" in File(out, "P.h").readText() + File(out, "P.cc").readText())
        } finally {
            dir.rmR()
        }
    }
}
