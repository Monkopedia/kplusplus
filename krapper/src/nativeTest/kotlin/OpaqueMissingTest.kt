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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * `OPAQUE_MISSING` binds a pointer or reference to an unlisted class as a `void*` the wrapper
 * casts back to the real C++ type, and drops a by-value use (#268). Each case runs the real
 * service path, which compiles the generated `.cc`: nothing compiled it before, which is how
 * `T*` -> `void**` shipped.
 */
class OpaqueMissingTest {

    /** [derived] declares `p::Derived`: forward only, as an unlisted class often is, or defined. */
    private fun header(derived: String) = """
        #pragma once
        namespace p {
        $derived
        struct Val { int x; };
        struct User {
            Derived* d();
            void take(Derived* x);
            const Derived* cd();
            void takeConst(const Derived* x);
            Derived& r();
            void takeRef(const Derived& x);
            Derived** pp();
            void takePp(Derived** x);
            void takeVal(Derived x);
            Derived byval();
            const Derived cbyval();
            Val f;
            Derived* fp;
            int u();
        };
        }
    """.trimIndent()

    private class Output(val index: BindingIndex, val wrapper: String, val user: String)

    /** Generate and compile [only] under [policy]; the wrapper compile throws if it fails. */
    private fun generate(
        only: List<String>,
        policy: ReferencePolicy,
        derived: String = "struct Derived;"
    ): Output = runBlocking {
        val dir = File.createTempDir("krapper_opaque_missing")
        try {
            val headerFile = File(dir, "p.h").also { it.writeText(header(derived)) }
            val out = File(dir, "out")
            val service = KrapperServiceImpl()
            service.setConfig(
                KrapperConfig(
                    pkg = "p",
                    compiler = "clang++",
                    moduleName = "P",
                    errorPolicy = ErrorPolicy.LOG,
                    referencePolicy = policy,
                    debug = false
                )
            )
            val indexed = service.index(IndexRequest(listOf(headerFile.path), emptyList()))
            indexed.filterAndResolve(AllowListFilter(only))
            indexed.writeTo(out.path)
            Output(
                Json { ignoreUnknownKeys = true }.decodeFromString(
                    BindingIndex.serializer(),
                    File(out, BindingIndex.FILE_NAME).readText()
                ),
                File(out, "P.cc").readText(),
                File(File(out, "src"), "p_User.kt").readText()
            )
        } finally {
            dir.rmR()
        }
    }

    @Test
    fun pointer_and_reference_to_an_unlisted_class_compile_as_a_cast_void_pointer() {
        val out = generate(listOf("p::User"), ReferencePolicy.OPAQUE_MISSING)
        for (signature in listOf(
            "void* p_User_d(void* thiz)",
            "void p_User_take(void* thiz, void* x)",
            "void* p_User_r(void* thiz)",
            "void p_User_take_ref(void* thiz, void* x)",
            "void* p_User_pp(void* thiz)",
            "void p_User_take_pp(void* thiz, void* x)"
        )) {
            assertTrue(signature in out.wrapper, "missing `$signature`:\n${out.wrapper}")
        }
        for (cast in listOf(
            "reinterpret_cast<p::Derived*>(x)",
            "reinterpret_cast<const p::Derived*>(x)",
            "reinterpret_cast<p::Derived**>(x)",
            "return (void*)thiz_cast->d();"
        )) {
            assertTrue(cast in out.wrapper, "missing `$cast`:\n${out.wrapper}")
        }
        assertFalse("void**" in out.wrapper, out.wrapper)
        for (member in listOf(
            "fun d(): COpaquePointer?",
            "fun take(x: COpaquePointer?)",
            "fun r(): COpaquePointer?",
            "fun takeRef(x: COpaquePointer)",
            "fun pp(): COpaquePointer?",
            "var fp: COpaquePointer?"
        )) {
            assertTrue(member in out.user, "missing `$member`:\n${out.user}")
        }
        assertFalse("Derived" in out.user, "Kotlin must not name the unbound class:\n${out.user}")
    }

    /**
     * A by-value argument would be copied from `*x` in the wrapper, which does not compile for a
     * forward-declared or non-copyable class and fails the whole module.
     */
    @Test
    fun by_value_unlisted_class_is_dropped_and_ledgered() {
        val out = generate(listOf("p::User"), ReferencePolicy.OPAQUE_MISSING)
        val takeVal = "fun takeVal(x: p::Derived): void"
        assertEquals(
            setOf(
                "f: p::Val",
                "fun byval(): p::Derived",
                "fun cbyval(): const p::Derived",
                "x: p::Derived",
                takeVal
            ),
            out.index.drops.map { it.symbol }.toSet()
        )
        // The argument carries the reason; its method is ledgered as failing from it.
        for (drop in out.index.drops.filter { it.symbol != takeVal }) {
            assertTrue("OPAQUE_MISSING binds only a pointer or reference" in drop.message, "$drop")
        }
        for (member in listOf("byval", "take_val", "takeVal", "p_User_f_")) {
            assertFalse(member in out.wrapper + out.user, "$member must be gone:\n${out.wrapper}")
        }
        assertTrue("fun u(): Int" in out.user, "User keeps the rest:\n${out.user}")
    }

    /** Must-differ: a listed class is still bound as itself, by value too. */
    @Test
    fun listed_class_is_still_bound_as_its_wrapper() {
        val out = generate(
            listOf("p::User", "p::Derived", "p::Val"),
            ReferencePolicy.OPAQUE_MISSING,
            derived = "struct Derived { int dv(); };"
        )
        assertTrue("fun d(): Derived?" in out.user, out.user)
        assertTrue("fun byval(): Derived" in out.user, out.user)
        assertTrue("fun takeVal(x: Derived)" in out.user, out.user)
        assertFalse("COpaquePointer?" in out.user.substringAfter("fun d()"), out.user)
        assertEquals(emptyList(), out.index.drops)
    }

    /** Must-differ: another policy does not opaque an unlisted class. */
    @Test
    fun ignore_missing_still_drops_what_names_an_unlisted_class() {
        val out = generate(listOf("p::User"), ReferencePolicy.IGNORE_MISSING)
        assertFalse("p_User_d(" in out.wrapper || "p_User_take(" in out.wrapper, out.wrapper)
        assertFalse("COpaquePointer?" in out.user, out.user)
        assertTrue("fun u(): Int" in out.user, out.user)
    }
}
