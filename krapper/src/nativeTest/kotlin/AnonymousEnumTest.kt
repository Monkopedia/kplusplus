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
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.checkDeclarationName
import com.monkopedia.krapper.generator.codegen.sourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * #316: a declaration whose type is an anonymous enum (`struct S { enum { A, B } e; };`) bound
 * the enum under clang's spelling `(unnamed enum at /abs/p.h:2:26)`, which became a generated
 * file name and aborted the sync. It now binds as the underlying integer, the wrapper casting
 * through `decltype` of the declarator. Each case runs the real service path, which compiles
 * the generated `.cc`.
 */
class AnonymousEnumTest {

    private class Output(
        val dir: String,
        val files: List<String>,
        val index: String,
        val wrapper: String,
        val kotlin: Map<String, String>
    )

    private fun generate(body: String, only: List<String>): Output = runBlocking {
        val dir = File.createTempDir("krapper_anon_enum")
        try {
            val headerFile = File(dir, "p.h")
                .also { it.writeText("#pragma once\nnamespace p {\n$body\n}\n") }
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
            indexed.filterAndResolve(AllowListFilter(only))
            indexed.writeTo(out.path)
            val src = File(out, "src").listFiles()
            Output(
                dir.path,
                (out.listFiles() + src).map { it.name },
                File(out, "binding-index.json").readText(),
                File(out, "P.cc").readText(),
                src.associate { it.name to it.readText() }
            )
        } finally {
            dir.rmR()
        }
    }

    /** Nothing generated may carry clang's unnamed spelling or the checkout path (D1). */
    private fun assertNoUnnamedSpelling(out: Output) {
        for (name in out.files) {
            assertFalse(name.any { it in "() /" }, "unsafe generated file name `$name`")
        }
        for ((name, text) in out.kotlin + ("P.cc" to out.wrapper) + ("index" to out.index)) {
            assertFalse(
                "unnamed" in text || out.dir in text,
                "$name leaks a clang spelling:\n$text"
            )
        }
    }

    @Test
    fun a_field_of_anonymous_enum_type_binds_as_its_underlying_integer() {
        val out = generate("struct S { enum { A, B } e; };", listOf("p::S"))
        assertNoUnnamedSpelling(out)
        val s = out.kotlin.getValue("p_S.kt")
        assertTrue("var e: UInt" in s, s)
        assertTrue("p_S_e_set(ptr, value)" in s, "no enum class to unwrap:\n$s")
        // C++ converts int -> enum only by an explicit cast, and the type has no name.
        assertTrue("(decltype(p::S::e))value" in out.wrapper, out.wrapper)
        assertTrue("\"drops\": []" in out.index, out.index)
    }

    @Test
    fun two_anonymous_enum_fields_each_cast_through_their_own_declarator() {
        val out = generate("struct S { enum { A, B } e; enum { C, D } f; };", listOf("p::S"))
        assertNoUnnamedSpelling(out)
        val s = out.kotlin.getValue("p_S.kt")
        assertTrue("var e: UInt" in s && "var f: UInt" in s, s)
        assertTrue("(decltype(p::S::e))value" in out.wrapper, out.wrapper)
        assertTrue("(decltype(p::S::f))value" in out.wrapper, out.wrapper)
    }

    /** A namespace-scope variable is not bound, but a function can still name its type. */
    @Test
    fun a_namespace_scope_anonymous_enum_binds_through_its_variable() {
        val out = generate(
            "enum { X, Y } g;\n" +
                "inline decltype(g) getG() { return g; }\n" +
                "inline void setG(decltype(g) v) { g = v; }",
            listOf("p::getG", "p::setG")
        )
        assertNoUnnamedSpelling(out)
        val functions = out.kotlin.getValue("p_Functions.kt")
        assertTrue("fun getG(): UInt" in functions, functions)
        assertTrue("fun setG(v: UInt)" in functions, functions)
        assertTrue("(decltype(p::g))v" in out.wrapper, out.wrapper)
    }

    /** An unnamed struct has no spellable name either: its field is dropped, path-free. */
    @Test
    fun an_anonymous_enum_field_inside_an_unnamed_struct_does_not_crash() {
        val out = generate(
            "struct T { struct { enum { B } f; int x; } inner; int y; };",
            listOf("p::T")
        )
        assertNoUnnamedSpelling(out)
        val t = out.kotlin.getValue("p_T.kt")
        assertTrue("var y: Int" in t, t)
        assertFalse("inner" in t, t)
        assertTrue("\"symbol\": \"inner: unresolveable\"" in out.index, out.index)
    }

    /** `enum { A } *q;` gives no declarator of the enum itself to `decltype`: dropped. */
    @Test
    fun a_pointer_to_an_anonymous_enum_is_dropped_and_ledgered() {
        val out = generate("struct S { enum { A } *q; int x; };", listOf("p::S"))
        assertNoUnnamedSpelling(out)
        val s = out.kotlin.getValue("p_S.kt")
        assertTrue("var x: Int" in s, s)
        assertFalse("var q" in s, s)
        assertTrue("\"symbol\": \"q: unresolveable*\"" in out.index, out.index)
    }

    /** Must-differ: a typedef names the enum for linkage, so it stays a Kotlin enum class. */
    @Test
    fun a_typedef_named_anonymous_enum_is_still_an_enum_class() {
        val out = generate("typedef enum { A, B } E;\nstruct S { E e; };", listOf("p::S"))
        assertNoUnnamedSpelling(out)
        assertTrue("var e: E" in out.kotlin.getValue("p_S.kt"))
        val enum = out.kotlin.getValue("p_E.kt")
        assertTrue("enum class E(val value: UInt)" in enum, enum)
        assertTrue("A(0u), B(1u)" in enum, enum)
    }

    /**
     * #323: an enum that is a member of a class template specialization (`p::Tm<int>::E`) was
     * named by a template parse that tore its spelling at the first `<`: `enum class ` with no
     * name, in `package int>`, file `int>_.kt`, and a field `var g: ` with no type. Each
     * specialized scope now flattens as the specialization's own binding is named.
     */
    private fun assertTemplateMemberEnum(body: String, pkg: String, field: String, cpp: String) {
        val out = generate(body, listOf("p::U"))
        assertNoUnnamedSpelling(out)
        val enumFile = pkg.replace(".", "_") + "_E.kt"
        val enum = out.kotlin[enumFile] ?: error("no $enumFile in ${out.kotlin.keys}")
        assertTrue("package $pkg\n" in enum, enum)
        assertTrue("enum class E(val value: UInt)" in enum, enum)
        assertTrue("fun fromValue(v: UInt): E =" in enum, enum)
        val u = out.kotlin.getValue("p_U.kt")
        assertTrue("import $pkg.E\n" in u, u)
        assertTrue("var $field: E\n" in u, u)
        assertTrue("($cpp)value" in out.wrapper, out.wrapper)
    }

    @Test
    fun a_typedef_named_anonymous_enum_in_a_class_template_is_named_by_its_specialization() {
        assertTemplateMemberEnum(
            "template<class T> struct Tm { typedef enum { A } E; E e; };\n" +
                "struct U { Tm<int> t; Tm<int>::E g; };",
            pkg = "p.tm__Int",
            field = "g",
            cpp = "p::Tm<int>::E"
        )
    }

    @Test
    fun a_named_enum_in_a_class_template_is_named_by_its_specialization() {
        assertTemplateMemberEnum(
            "template<class T> struct Tm { enum E { A }; E e; };\n" +
                "struct U { Tm<int> t; Tm<int>::E g; };",
            pkg = "p.tm__Int",
            field = "g",
            cpp = "p::Tm<int>::E"
        )
    }

    /** Every argument names the scope: before #323 this one bound silently as `root.Int`. */
    @Test
    fun an_enum_in_a_two_parameter_class_template_keeps_both_arguments() {
        assertTemplateMemberEnum(
            "template<class T, class V> struct Tm { typedef enum { A } E; };\n" +
                "struct U { Tm<int, float>::E h; };",
            pkg = "p.tm__Int__Float",
            field = "h",
            cpp = "p::Tm<int, float>::E"
        )
    }

    @Test
    fun an_enum_in_a_nested_class_template_names_every_specialized_scope() {
        assertTemplateMemberEnum(
            "template<class T> struct O {\n" +
                "template<class V> struct I { typedef enum { A } E; };\n" +
                "};\nstruct U { O<int>::I<float>::E n; };",
            pkg = "p.o__Int.i__Float",
            field = "n",
            cpp = "p::O<int>::I<float>::E"
        )
    }

    @Test
    fun a_declaration_name_must_be_a_kotlin_identifier() {
        assertEquals("E", checkDeclarationName("p.tm__Int", "E"))
        assertEquals("E", checkDeclarationName("", "E"))
        val invalid = listOf("int>" to "", "p" to "", "int>" to "E", "p" to "1E", "p..q" to "E")
        for ((pkg, name) in invalid) {
            val error = assertFailsWith<IllegalStateException> { checkDeclarationName(pkg, name) }
            assertTrue("refusing to declare Kotlin `$name` in package `$pkg`" in error.message!!)
        }
    }

    @Test
    fun a_clang_unnamed_spelling_cannot_become_a_file_name() {
        val dir = File("/tmp/out")
        assertEquals("p_S.kt", sourceFile(dir, "p_S").name)
        val unsafe = listOf(
            "p_s_(unnamed enum at /abs/p.h:2:26)", "a b", "a/b", "",
            // #323: a template spelling torn mid-name, and the rest of its punctuation.
            "int>_", "Tm<int", "a,b", "a*", "a&"
        )
        for (stem in unsafe) {
            val error = assertFailsWith<IllegalStateException> { sourceFile(dir, stem) }
            assertTrue("refusing to write generated source `$stem.kt`" in error.message!!)
        }
    }
}
