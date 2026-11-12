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
        val classes: List<ResolvedClass>,
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
                    classes = classes.filterIsInstance<ResolvedClass>(),
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

    // Every type constructor that CONTAINS a type must be walked, not just typedef sugar and
    // pointees. Each shape runs twice over the same `H`: once aliasing the unknown `Handle`
    // (dropped + ledgered by name) and once aliasing `long` (no PARSE drop; bound when krapper
    // binds that shape at all). The valid half is what proves the walk does not over-drop.
    private fun assertShape(decl: String, bindsWhenValid: Boolean, prelude: String = "") {
        fun header(target: String) = "namespace p {\ntypedef $target H;\n$prelude\n" +
            "struct M { int m; };\ntemplate <class T> struct Box { T t; };\n" +
            "struct W { $decl; int ok(); };\n}\n"
        val bad = generate(header("Handle"))
        assertFalse("fun f(" in bad.kotlin, "[$decl] must not bind over `Handle`:\n${bad.kotlin}")
        assertTrue("fun ok(" in bad.kotlin, "[$decl] the valid sibling still binds")
        assertEquals(
            1,
            bad.drops.count { it.symbol == "p::W::f" && it.phase == DropPhase.PARSE },
            "[$decl] `p::W::f` must be ledgered once as a PARSE drop: ${bad.drops}"
        )
        val good = generate(header("long"))
        assertEquals(
            emptyList(),
            good.drops.filter { it.phase == DropPhase.PARSE },
            "[$decl] over `long` nothing is a parse drop"
        )
        if (bindsWhenValid) {
            assertTrue("fun f(" in good.kotlin, "[$decl] must bind over `long`:\n${good.kotlin}")
        }
    }

    @Test
    fun a_function_pointer_parameter_type_is_walked() =
        assertShape("void f(Cb c)", bindsWhenValid = true, prelude = "typedef void (*Cb)(H);")

    @Test
    fun a_function_pointer_return_type_is_walked() =
        assertShape("void f(Cb c)", bindsWhenValid = true, prelude = "typedef H (*Cb)(int);")

    @Test
    fun an_array_typedef_element_is_walked() =
        assertShape("void f(Arr* a)", bindsWhenValid = true, prelude = "typedef H Arr[4];")

    @Test
    fun a_pointer_to_array_element_is_walked() =
        assertShape("void f(H (*a)[4])", bindsWhenValid = true)

    @Test
    fun a_member_pointer_pointee_is_walked() =
        assertShape("void f(H M::* m)", bindsWhenValid = false)

    @Test
    fun a_template_argument_is_walked() = assertShape("void f(Box<H>* b)", bindsWhenValid = false)

    // The DIRECT case no type walk can see: clang marks the PARAMETER inside the typedef's
    // declarator invalid, not the typedef, and the typedef's type is a plain `int (*)(int)`.
    // Only the typedef's source-level declarator (its TypeSourceInfo) carries the flag.
    @Test
    fun an_invalid_parameter_inside_a_function_pointer_typedef_is_seen() {
        val bad = generate(
            "namespace p {\ntypedef void (*Cb)(Handle);\nstruct W { void f(Cb c); int ok(); };\n}\n"
        )
        assertFalse("fun f(" in bad.kotlin, "`f` must not bind:\n${bad.kotlin}")
        assertEquals(1, bad.drops.count { it.symbol == "p::W::f" }, "${bad.drops}")
        assertEquals(1, bad.drops.count { it.symbol == "p::Cb" }, "${bad.drops}")
        val good = generate(
            "namespace p {\ntypedef void (*Cb)(long);\nstruct W { void f(Cb c); int ok(); };\n}\n"
        )
        assertTrue("fun f(" in good.kotlin, "`f` binds over long:\n${good.kotlin}")
        assertEquals(emptyList(), good.drops, "a clean parse ledgers nothing")
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

    // #257 — clang marks NEITHER of the next two shapes invalid, and no type walk reaches the
    // loss: the declaration owns the parse error by SOURCE RANGE (it lies inside the decl's
    // written range and outside every member's), which is what drops it.

    // `enum E : Handle` with `Handle` undeclared: clang keeps E with an `int` underlying type,
    // a guessed C boundary width. E is ledgered by name, and its users drop with it.
    @Test
    fun an_enum_with_an_unknown_underlying_type_is_not_emitted_and_is_ledgered() {
        val out = generate(
            "namespace p {\nenum E : Handle { A, B };\nstruct W { void f(E e); int ok(); };\n}\n"
        )
        assertFalse("fun f(" in out.kotlin, "`f(E)` must not bind over a guessed E:\n${out.kotlin}")
        assertTrue("fun ok(" in out.kotlin, "the valid sibling still binds:\n${out.kotlin}")
        assertEquals(1, out.drops.count { it.symbol == "p::E" }, "${out.drops}")
        assertEquals(1, out.drops.count { it.symbol == "p::W::f" }, "${out.drops}")
    }

    // MUST DIFFER: a KNOWN underlying type binds at its real width.
    @Test
    fun an_enum_with_a_known_underlying_type_binds_at_its_width() {
        val out = generate(
            "namespace p {\nenum E : long { A, B };\nstruct W { void f(E e); int ok(); };\n}\n"
        )
        assertTrue("fun f(e: E)" in out.kotlin, "`f(E)` binds:\n${out.kotlin}")
        assertTrue("value: Long" in out.kotlin, "E is Long-backed:\n${out.kotlin}")
        assertEquals(emptyList(), out.drops, "a clean parse ledgers nothing")
    }

    // `struct B : NotAThing`: clang DROPS the bad base specifier, so B is valid to clang and
    // would bind missing its base's API. B is not emitted and is ledgered by name.
    @Test
    fun a_class_with_an_unknown_base_is_not_emitted_and_is_ledgered() {
        val out = generate("namespace p {\nstruct B : NotAThing { int m(); };\n}\n")
        assertFalse("p::B" in out.bound, "B must not bind without its base: ${out.bound}")
        assertFalse("fun m(" in out.kotlin, "no member of B is emitted:\n${out.kotlin}")
        assertEquals(1, out.drops.count { it.symbol == "p::B" }, "${out.drops}")
    }

    // MUST DIFFER: a known base binds, base API included.
    @Test
    fun a_class_with_a_known_base_binds_with_it() {
        val out = generate(
            "namespace p {\nstruct ValidBase { int v(); };\nstruct B : ValidBase { int m(); };\n}\n"
        )
        assertTrue("p::B" in out.bound, "B binds: ${out.bound}")
        val b = out.classes.single { it.type.toString() == "p::B" }
        assertTrue("ValidBase" in b.baseClass.toString(), "B keeps its base: ${b.baseClass}")
        assertEquals(emptyList(), out.drops, "a clean parse ledgers nothing")
    }

    // MUST DIFFER (ownership): an error is owned by the INNERMOST declaration whose range holds
    // it. A sibling's error, a nested record's error, and an error inside a method body all
    // leave the enclosing / neighbouring declaration bound and unledgered.
    @Test
    fun an_error_owned_by_another_declaration_drops_nothing_else() {
        val out = generate(
            "namespace p {\nstruct Bad : NotAThing { int m(); };\n" +
                "struct Clean { int x; int y(); };\n" +
                "void g(Unknown u);\n" +
                "struct Outer { struct Inner : NotAThing { int i(); }; int o(); };\n" +
                "struct Body { int f() { return undeclared(); } int h(); };\n}\n"
        )
        for (name in listOf("p::Clean", "p::Outer", "p::Body")) {
            assertTrue(name in out.bound, "$name must still bind: ${out.bound}")
            assertEquals(0, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
        for (name in listOf("y", "o", "f", "h")) {
            assertTrue("fun $name(" in out.kotlin, "`$name` must still bind:\n${out.kotlin}")
        }
        for (name in listOf("p::Bad", "p::Outer::Inner", "p::g")) {
            assertEquals(1, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
    }

    // MUST DIFFER (ownership): clang REMOVES a rejected initializer from the AST, so the
    // member's AST range no longer covers its error. The member still owns it — by its
    // written extent, through the `;` — so the class binds, only the member is affected.
    @Test
    fun a_rejected_member_initializer_drops_only_the_member() {
        val out = generate(
            "namespace p {\nstruct D { int x = undeclared; int m(); };\n" +
                "struct S { static constexpr int k = undeclared; int n(); };\n}\n"
        )
        for (name in listOf("p::D", "p::S")) {
            assertTrue(name in out.bound, "$name must still bind: ${out.bound}")
            assertEquals(0, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
        assertTrue("fun m(" in out.kotlin && "fun n(" in out.kotlin, out.kotlin)
        assertEquals(1, out.drops.count { it.symbol == "p::D::x" }, "${out.drops}")
    }

    // ...while an error no member's written extent covers — a member clang discarded
    // outright — is still the class's: it would bind missing that member.
    @Test
    fun a_discarded_member_drops_the_class() {
        val out = generate("namespace p {\nstruct W { int a; 42; int b(); };\n}\n")
        assertFalse("p::W" in out.bound, "W must not bind missing a member: ${out.bound}")
        assertEquals(1, out.drops.count { it.symbol == "p::W" }, "${out.drops}")
    }

    // A rejected enumerator value leaves the AST like any initializer; no enumerator's range
    // covers it, so the enum owns it and drops (with its users) rather than bind a guess.
    @Test
    fun an_enum_with_a_rejected_enumerator_value_is_not_emitted() {
        val out = generate(
            "namespace p {\nenum E { A = undeclared, B };\nstruct W { void f(E e); int ok(); };\n}\n"
        )
        assertFalse("fun f(" in out.kotlin, out.kotlin)
        assertEquals(1, out.drops.count { it.symbol == "p::E" }, "${out.drops}")
    }

    // #263 — one macro expanding to a whole class collapses every expansion location onto the
    // invocation, so a member's range seemed to hold the bad base and MB bound without it.
    // Ownership is decided in expanded token order instead: the base lies before any member.
    @Test
    fun a_macro_generated_class_with_an_unknown_base_is_not_emitted() {
        val out = generate(
            "#define DECLARE_BAD(n) struct n : NotAThing { int m(); };\n" +
                "namespace p {\nDECLARE_BAD(MB)\n}\n"
        )
        assertFalse("p::MB" in out.bound, "MB must not bind without its base: ${out.bound}")
        assertEquals(1, out.drops.count { it.symbol == "p::MB" }, "${out.drops}")
    }

    // MUST DIFFER: the same macro over a known base binds, base included.
    @Test
    fun a_macro_generated_class_with_a_known_base_binds_with_it() {
        val out = generate(
            "#define DECLARE_OK(n) struct n : ValidBase { int m(); };\n" +
                "namespace p {\nstruct ValidBase { int v(); };\nDECLARE_OK(MB)\n}\n"
        )
        val mb = out.classes.single { it.type.toString() == "p::MB" }
        assertTrue("ValidBase" in mb.baseClass.toString(), "MB keeps its base: ${mb.baseClass}")
        assertTrue("fun m(" in out.kotlin, out.kotlin)
        assertEquals(emptyList(), out.drops, "a clean parse ledgers nothing")
    }

    // MUST DIFFER: a macro that writes one bad MEMBER charges that member alone, whether the
    // error is in the expansion (a method's unknown parameter) or after it (`FIELD(x) = bad;`).
    @Test
    fun a_macro_generated_bad_member_drops_only_the_member() {
        val out = generate(
            "#define FIELD(n) int n\n#define METHOD(n) void n(Handle h);\n" +
                "namespace p {\nstruct Mf { FIELD(x) = undeclared; int k(); };\n" +
                "struct Mm { METHOD(set) int k(); };\n}\n"
        )
        for (name in listOf("p::Mf", "p::Mm")) {
            assertTrue(name in out.bound, "$name must still bind: ${out.bound}")
            assertEquals(0, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
        assertEquals(1, out.drops.count { it.symbol == "p::Mf::x" }, "${out.drops}")
        assertEquals(1, out.drops.count { it.symbol == "p::Mm::set" }, "${out.drops}")
    }

    // A macro field ends at its `;`, whether the macro supplies it or the invocation is
    // followed by it: the `42;` after is a discarded member, the class's, just as a real
    // discarded member after it is.
    @Test
    fun a_discarded_member_after_a_macro_field_drops_the_class() {
        val out = generate(
            "#define FIELD(n) int n\n#define FIELD2(n) int n;\n" +
                "namespace p {\nstruct Mi { FIELD(x); 42; int k(); };\n" +
                "struct Mg { FIELD2(x) 42; int k(); };\n" +
                "struct Mh { FIELD2(x) NotAThing<int> y; int k(); };\n" +
                "struct Mj { FIELD(a); FIELD2(b) FIELD2(c) int k(); };\n}\n"
        )
        for (name in listOf("p::Mi", "p::Mg", "p::Mh")) {
            assertFalse(name in out.bound, "$name must not bind missing a member: ${out.bound}")
            assertEquals(1, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
        // MUST DIFFER: macro fields with no discarded member between them bind.
        assertTrue("p::Mj" in out.bound, "Mj must still bind: ${out.bound}")
        assertEquals(0, out.drops.count { it.symbol.startsWith("p::Mj") }, "${out.drops}")
    }

    // MUST DIFFER (direction): a macro DEFINED inside Host and EXPANDED inside Victim charges
    // Victim's member, never Host — order is by expansion, not by where the text is spelled.
    @Test
    fun a_macro_error_belongs_where_the_macro_is_expanded() {
        val out = generate(
            "namespace p {\nstruct Host { int h();\n#define VFIELD int z = undeclared;\n};\n" +
                "struct Victim { VFIELD int v(); };\n}\n"
        )
        for (name in listOf("p::Host", "p::Victim")) {
            assertTrue(name in out.bound, "$name must still bind: ${out.bound}")
            assertEquals(0, out.drops.count { it.symbol == name }, "$name: ${out.drops}")
        }
        assertEquals(1, out.drops.count { it.symbol == "p::Victim::z" }, "${out.drops}")
    }

    // A bit-field width clang rejected leaves the field valid with no width: it would bind
    // as a plain field. The field owns the error by its written extent, so it alone drops.
    @Test
    fun a_bit_field_with_a_rejected_width_drops_only_the_field() {
        val out = generate("namespace p {\nstruct Bf { int a : undeclared; int b(); };\n}\n")
        assertTrue("p::Bf" in out.bound, "Bf must still bind: ${out.bound}")
        assertEquals(0, out.drops.count { it.symbol == "p::Bf" }, "${out.drops}")
        assertFalse("var a:" in out.kotlin, "`a` must not bind without its width:\n${out.kotlin}")
        assertTrue("fun b(" in out.kotlin, out.kotlin)
        assertEquals(1, out.drops.count { it.symbol == "p::Bf::a" }, "${out.drops}")
    }

    // MUST DIFFER: valid bit-fields bind (a multi-declarator line included, beside a
    // brace-initialized field), while a rejected initializer on a valid width, or a discarded member after
    // one, keeps its owner — the field, or the class.
    @Test
    fun a_valid_bit_field_binds_and_its_neighbours_keep_their_owners() {
        val ok = generate(
            "namespace p {\nstruct Bv { int a : 3; int c : 2, d : 4; int e{1}; int b(); };\n}\n"
        )
        for (name in listOf("a", "c", "d", "e")) {
            assertTrue("var $name:" in ok.kotlin, "`$name` must bind:\n${ok.kotlin}")
        }
        assertEquals(emptyList(), ok.drops, "a clean parse ledgers nothing")
        val bad = generate(
            "namespace p {\nstruct Bg { int a : 3 = undeclared; int b(); };\n" +
                "struct Bh { int a : 3; 42; int b(); };\n}\n"
        )
        assertTrue("p::Bg" in bad.bound, "Bg must still bind: ${bad.bound}")
        assertEquals(1, bad.drops.count { it.symbol == "p::Bg::a" }, "${bad.drops}")
        assertFalse("p::Bh" in bad.bound, "Bh must not bind missing a member: ${bad.bound}")
        assertEquals(1, bad.drops.count { it.symbol == "p::Bh" }, "${bad.drops}")
    }

    // Runs `struct R { [body] int k(); }` and asserts the error is [guilty]'s alone: R binds,
    // [guilty] is ledgered and not emitted, and every [innocent] field still emits.
    private fun assertFieldOwnsError(
        body: String,
        guilty: String,
        innocent: List<String> = emptyList(),
        prelude: String = ""
    ) {
        val out = generate("$prelude\nnamespace p {\nstruct R { $body int k(); };\n}\n")
        assertTrue("p::R" in out.bound, "[$body] R must still bind: ${out.bound}")
        assertEquals(0, out.drops.count { it.symbol == "p::R" }, "[$body] ${out.drops}")
        assertEquals(1, out.drops.count { it.symbol == "p::R::$guilty" }, "[$body] ${out.drops}")
        assertFalse("var $guilty:" in out.kotlin, "[$body] `$guilty` must drop:\n${out.kotlin}")
        for (name in innocent) {
            assertTrue("var $name:" in out.kotlin, "[$body] `$name` must emit:\n${out.kotlin}")
            assertEquals(0, out.drops.count { it.symbol == "p::R::$name" }, "[$body] ${out.drops}")
        }
    }

    private fun assertClassOwnsError(body: String) {
        val out = generate("namespace p {\nstruct R { $body };\n}\n")
        assertFalse("p::R" in out.bound, "[$body] R must not bind: ${out.bound}")
        assertEquals(1, out.drops.count { it.symbol == "p::R" }, "[$body] ${out.drops}")
    }

    // Every declarator of `int a, b;` begins at the shared `int` and runs on to the shared
    // `;`, so each one's extent is its own declarator: from its name to the next sibling's.
    @Test
    fun an_initializer_error_on_the_first_declarator_drops_only_it() =
        assertFieldOwnsError("int a = undeclared, b;", guilty = "a", innocent = listOf("b"))

    @Test
    fun an_initializer_error_on_the_last_declarator_drops_only_it() =
        assertFieldOwnsError("int a, b = undeclared;", guilty = "b", innocent = listOf("a"))

    @Test
    fun a_bit_field_width_error_in_a_multi_declarator_line_drops_only_its_declarator() =
        assertFieldOwnsError("int a : undeclared, b : 2;", guilty = "a", innocent = listOf("b"))

    @Test
    fun a_macro_argument_multi_declarator_line_drops_only_the_bad_declarator() =
        assertFieldOwnsError(
            "FWD(int a = 1, b = undeclared;)",
            guilty = "b",
            innocent = listOf("a"),
            prelude = "#define FWD(...) __VA_ARGS__"
        )

    // A `,` inside a template argument list is not a declarator boundary.
    @Test
    fun a_template_argument_comma_does_not_split_a_declarator() = assertFieldOwnsError(
        "int x = f<1, 2>(), y = undeclared;",
        guilty = "y",
        innocent = listOf("x"),
        prelude = "template <int A, int B> int f() { return A + B; }"
    )

    // The `;` that ends a field is the first outside brackets, string and character literals
    // and comments, in expanded order.
    @Test
    fun a_field_extent_ends_at_its_own_semicolon() {
        assertFieldOwnsError(
            "int (*f)() = [] { int q = 1; return undeclared; }; int v;",
            guilty = "f",
            innocent = listOf("v")
        )
        assertFieldOwnsError(
            "int (*f)() = [] { return 1; }; int g = undeclared; int v;",
            guilty = "g",
            innocent = listOf("v")
        )
        assertFieldOwnsError("int a[2] = {1, undeclared}; int v;", "a", listOf("v"))
        assertFieldOwnsError("int s = sizeof(\";\") + undeclared; int v;", "s", listOf("v"))
        assertFieldOwnsError("int c = ';' + undeclared; int v;", "c", listOf("v"))
        assertFieldOwnsError("int x = /* ; */ undeclared; int v;", "x", listOf("v"))
        assertFieldOwnsError("int x = // ;\nundeclared; int v;", "x", listOf("v"))
        assertFieldOwnsError("F(x) int v;", "x", listOf("v"), "#define F(n) int n = undeclared;")
        assertFieldOwnsError("G(x) = undeclared; int v;", "x", listOf("v"), "#define G(n) int n")
    }

    // #274 — a `";"` literal at depth 0 is one token, not the field's `;`.
    @Test
    fun a_semicolon_string_literal_outside_brackets_does_not_end_a_field() =
        assertFieldOwnsError("const char *s = \";\" + undeclared; int v;", "s", listOf("v"))

    // #274 — clang recovers a rejected BRACE initializer as an expression with no location,
    // so the field's AST range has no end; its written extent still runs to its `;`.
    @Test
    fun a_rejected_brace_initializer_drops_only_the_member() {
        assertFieldOwnsError("int a{undeclared}; int v;", "a", listOf("v"))
        assertFieldOwnsError("int a{1}, b{undeclared}; int v;", "b", listOf("a", "v"))
    }

    // #274, documented: an array bound clang rejected leaves the FIELD's type invalid, and
    // clang then marks the RECORD invalid itself (its layout is unknown) — the propagation
    // a_class_with_an_invalid_field_is_not_emitted_and_is_ledgered relies on. So the class
    // drops, whether the field stands alone or shares a line with a valid sibling.
    @Test
    fun a_rejected_array_bound_drops_the_class_clang_marked_invalid() {
        assertClassOwnsError("int b[undeclared]; int k();")
        assertClassOwnsError("int *a, b[undeclared]; int k();")
    }

    // #274 — an anonymous struct or union never binds, but its guilty member is still
    // ledgered by name, beside the parse error; the enclosing class keeps binding.
    @Test
    fun an_error_in_an_anonymous_record_ledgers_its_member() {
        for (kind in listOf("struct", "union")) {
            val out = generate(
                "namespace p {\nstruct R { $kind { int y = undeclared; int z; }; int k(); };\n}\n"
            )
            assertTrue("p::R" in out.bound, "[$kind] R must still bind: ${out.bound}")
            assertEquals(0, out.drops.count { it.symbol == "p::R" }, "[$kind] ${out.drops}")
            assertTrue("fun k(" in out.kotlin, "[$kind] `k` must emit:\n${out.kotlin}")
            val guilty = "p::R::(anonymous $kind)::y"
            assertEquals(1, out.drops.count { it.symbol == guilty }, "[$kind] ${out.drops}")
            assertEquals(2, out.drops.size, "[$kind] only `y` beside the error: ${out.drops}")
        }
    }

    // ...and an error after that `;` that no member holds is the class's.
    @Test
    fun an_error_after_a_field_extent_is_the_class() {
        assertClassOwnsError("int (*f)() = [] { return 1; }; 42; int k();")
        assertClassOwnsError("int a{1}; 42; int k();")
        assertClassOwnsError("int k(); int x = undeclared")
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
