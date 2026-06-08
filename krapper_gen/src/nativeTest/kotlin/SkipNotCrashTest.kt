/*
 * Copyright 2022 Jason Monk
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
import platform.posix.random

/**
 * T1.4 skip-not-crash fixtures. Each test writes a header containing ONE
 * unmodelable shape next to an ordinary, bindable member, runs the full
 * generator end-to-end (parse -> resolve -> emit C++ -> COMPILE the wrapper with
 * clang -> emit Kotlin) and asserts:
 *   1. the run completes without crashing,
 *   2. the generated wrapper actually compiles (writeTo throws on a non-zero
 *      clang exit, so a green run is the compile proof),
 *   3. the bad element was skipped while the good one still bound.
 *
 * These are generic generator-robustness proofs, not Clang-specific hacks: the
 * shapes (gnarly template spellings, missing-spelling cursors, anon members,
 * unbindable params, const members, deleted ctors) recur across any large C++
 * library.
 */
class SkipNotCrashTest {

    private fun generate(header: String): String {
        val tag = "${random()}_${random()}"
        val headerFile = "/tmp/krapper_skip_$tag.h"
        File(headerFile).writeText(header)
        val outDir = "/tmp/krapper_skip_out_$tag"
        runBlocking {
            val config = KrapperConfig(
                pkg = "krapper.skip",
                compiler = "clang++",
                moduleName = "skip",
                errorPolicy = ErrorPolicy.LOG,
                referencePolicy = ReferencePolicy.INCLUDE_MISSING,
                debug = false
            )
            val service = IndexedServiceImpl(
                config,
                IndexRequest(listOf(headerFile), emptyList())
            )
            // DefaultFilter resolves the whole header. writeTo compiles the emitted
            // C++ with clang and throws on failure — so reaching the end is the
            // "generates + compiles green" proof.
            service.filterAndResolve(com.monkopedia.krapper.DefaultFilter)
            service.writeTo(outDir)
        }
        val cc = File(File(outDir), "skip.cc").readText()
        File(outDir).rmR()
        File(headerFile).delete()
        return cc
    }

    /**
     * Run the full generator over [header] and return the drop-ledger records collected
     * during the run. Mirrors [generate] but hands back the ledger instead of the emitted
     * C++, so a test can assert WHAT was dropped and WHY (not just that the run survived).
     * [failOnDrop] toggles the strict gate; [runBody] receives the snapshot taken right
     * after writeTo (a successful run) — for the strict-failure case the caller inspects
     * the throw separately.
     */
    private fun generateLedger(header: String, failOnDrop: Boolean = false): List<DropRecord> {
        val tag = "${random()}_${random()}"
        val headerFile = "/tmp/krapper_ledger_$tag.h"
        File(headerFile).writeText(header)
        val outDir = "/tmp/krapper_ledger_out_$tag"
        val records = runBlocking {
            val config = KrapperConfig(
                pkg = "krapper.skip",
                compiler = "clang++",
                moduleName = "skip",
                errorPolicy = ErrorPolicy.LOG,
                referencePolicy = ReferencePolicy.INCLUDE_MISSING,
                debug = false,
                failOnDrop = failOnDrop
            )
            val service = IndexedServiceImpl(
                config,
                IndexRequest(listOf(headerFile), emptyList())
            )
            service.filterAndResolve(com.monkopedia.krapper.DefaultFilter)
            service.writeTo(outDir)
            // The ledger is process-scoped; snapshot it before any teardown runs.
            DropLedger.drops.toList()
        }
        File(outDir).rmR()
        File(headerFile).delete()
        return records
    }

    // G1: a method whose param/return type has a deeply-nested template spelling
    // that previously overflowed parseTypes/findTemplates index math. The class
    // must still bind its plain `value()` method; the gnarly one degrades but the
    // run neither crashes nor emits broken C.
    @Test
    fun g1_gnarly_nested_template_spelling_does_not_crash() {
        // Local templates only (no std headers) so the fixture stays small and
        // self-contained; the point is the deeply-nested template SPELLING that
        // overflowed the hand-rolled parseTypes/findTemplates index math.
        val cc = generate(
            """
            #pragma once
            template <class T> struct Box1 { T v; };
            template <class A, class B, class C> struct Triple { A a; B b; C c; };
            struct Leaf { int n; };
            struct G1 {
                int value() const { return 7; }
                Triple<Box1<Box1<Box1<Leaf>>>,
                       Box1<Triple<Leaf, Box1<Leaf>, int>>,
                       Box1<Box1<Triple<int, Leaf, Box1<Leaf>>>>> gnarly() const {
                    return {};
                }
            };
            """.trimIndent()
        )
        assertTrue("G1_value" in cc, "plain method must still bind:\n$cc")
        assertFalse(
            "thiz_cast->;" in cc,
            "no truncated member access should be emitted:\n$cc"
        )
    }

    // G2: a member typed by an anonymous (unnamed) struct has no spelling/usr, so
    // resolving it reaches a missing-spelling error() path (IllegalStateException,
    // not IllegalArgumentException) that the narrow per-branch catches let escape.
    // The bad member is dropped; the plain `ok()` still binds and the run doesn't
    // crash.
    @Test
    fun g2_missing_spelling_member_is_dropped_not_fatal() {
        val cc = generate(
            """
            #pragma once
            struct G2 {
                int ok() const { return 3; }
                struct { int a; int b; } anon;   // anonymous-struct-typed member
            };
            """.trimIndent()
        )
        assertTrue("G2_ok" in cc, "plain method must still bind:\n$cc")
    }

    // G3: an anonymous bitfield (`int :3;`) has a blank spelling. It must not emit
    // an accessor (which would be `thiz_cast->;` with an empty name); the named
    // field beside it still binds.
    @Test
    fun g3_anonymous_bitfield_member_is_skipped() {
        val cc = generate(
            """
            #pragma once
            struct G3 {
                int named;
                int : 3;     // anonymous bitfield padding -> blank spelling
            };
            """.trimIndent()
        )
        assertTrue("G3_named" in cc, "named field must still bind:\n$cc")
        assertFalse("thiz_cast->;" in cc, "no empty member access:\n$cc")
    }

    // G4: a method with a REQUIRED param whose type can't be bound must be skipped
    // entirely (a partial call with too few args would not compile). The class's
    // plain method still binds. resolveArguments() already enforces this — when a
    // non-defaulted arg fails to resolve it drops the whole method (the partial-list
    // shortcut only fires when every remaining arg is defaulted). This test locks
    // that contract against regressions and proves the generated wrapper compiles.
    @Test
    fun g4_required_unbindable_param_drops_whole_method() {
        val cc = generate(
            """
            #pragma once
            template <class T> struct Opaque { T x; };
            struct G4 {
                int fine(int a) const { return a; }
                // `Opaque<int>&&` rvalue-ref param is unbindable; the whole method
                // must drop rather than emit `obj->takesBad()` with too few args.
                void takesBad(Opaque<int>&& bad) {}
            };
            """.trimIndent()
        )
        assertTrue("G4_fine" in cc, "bindable method must still bind:\n$cc")
        assertFalse(
            "takes_bad" in cc || "takesBad" in cc,
            "method with unbindable required param must be dropped:\n$cc"
        )
    }

    // G5: a const data member must get a getter but NO setter (a `_set` body would
    // be "cannot assign to const-qualified"). A non-const member beside it keeps
    // both.
    @Test
    fun g5_const_member_emits_no_setter() {
        val cc = generate(
            """
            #pragma once
            struct G5 {
                const int k = 5;
                int m = 0;
            };
            """.trimIndent()
        )
        assertTrue("G5_k_get" in cc, "const member getter must bind:\n$cc")
        assertFalse("G5_k_set" in cc, "const member must have NO setter:\n$cc")
        assertTrue("G5_m_set" in cc, "non-const member keeps its setter:\n$cc")
    }

    // G7: a class with a deleted default constructor + a usable other ctor must
    // bind WITHOUT emitting a placement-new wrapper for the deleted ctor (which
    // would be "call to deleted constructor"). The usable ctor still binds.
    @Test
    fun g7_deleted_constructor_wrapper_is_skipped() {
        val cc = generate(
            """
            #pragma once
            struct G7 {
                G7() = delete;
                G7(int v) : value(v) {}
                int value;
            };
            """.trimIndent()
        )
        assertTrue(
            "G7_new(void* location, int v)" in cc,
            "usable int ctor must bind:\n$cc"
        )
        // The deleted default ctor wrapper would be `G7_new(void* location)` (no
        // value arg) calling `new (location) G7()`. It must NOT be emitted.
        assertFalse(
            Regex("new \\(location\\) G7\\(\\)").containsMatchIn(cc),
            "deleted default ctor wrapper must be skipped:\n$cc"
        )
    }

    // L1: the drop ledger records a dropped symbol WITH A REASON. G4's unbindable
    // required-param method is the deliberately-unmodelable shape; after the run the
    // ledger must contain a record naming it (so a consumer can tell "the generator
    // dropped takesBad" from "C++ never had it"), while the bindable `fine` is NOT in
    // the ledger.
    @Test
    fun l1_dropped_symbol_is_recorded_in_the_ledger_with_a_reason() {
        val drops = generateLedger(
            """
            #pragma once
            template <class T> struct Opaque { T x; };
            struct L1 {
                int fine(int a) const { return a; }
                void takesBad(Opaque<int>&& bad) {}
            };
            """.trimIndent()
        )
        assertTrue(
            drops.any { "takesBad" in it.symbol },
            "the unbindable method must be recorded in the drop ledger:\n$drops"
        )
        val takesBad = drops.first { "takesBad" in it.symbol }
        assertTrue(
            takesBad.reason.isNotBlank(),
            "every drop must carry a reason:\n$takesBad"
        )
        assertFalse(
            drops.any { "fine" in it.symbol },
            "a successfully-bound method must NOT be in the ledger:\n$drops"
        )
    }

    // L2: a clean run (no unmodelable shapes) records ZERO drops — the ledger doesn't
    // false-positive on ordinary bindable members.
    @Test
    fun l2_clean_run_records_no_drops() {
        val drops = generateLedger(
            """
            #pragma once
            struct L2 {
                int value() const { return 7; }
                int field = 0;
            };
            """.trimIndent()
        )
        assertEquals(
            emptyList(),
            drops,
            "a fully-bindable header must drop nothing:\n$drops"
        )
    }

    // L3: --fail-on-drop turns a drop into a hard failure (non-zero exit / thrown
    // error), while the same header passes leniently by default. Locks the opt-in
    // strict-mode contract.
    @Test
    fun l3_fail_on_drop_fails_when_something_is_dropped() {
        val header =
            """
            #pragma once
            template <class T> struct Opaque { T x; };
            struct L3 {
                int fine(int a) const { return a; }
                void takesBad(Opaque<int>&& bad) {}
            };
            """.trimIndent()
        // Lenient (default) completes.
        assertTrue(generateLedger(header).isNotEmpty())
        // Strict mode throws.
        var threw = false
        try {
            generateLedger(header, failOnDrop = true)
        } catch (t: Throwable) {
            threw = true
            assertTrue(
                "fail-on-drop" in (t.message ?: t.cause?.message ?: ""),
                "strict-mode failure must mention fail-on-drop: ${t.message}"
            )
        }
        assertTrue(threw, "--fail-on-drop must fail the run when a symbol was dropped")
    }

    // G6 (template-arg namespace qualification) is intentionally NOT covered here:
    // the standard nested-namespace shape (`std::vector<ns::Inner>`) already emits a
    // correctly-qualified arg via referencedDecl.fullyQualified, and a fixture that
    // forces the buggy raw-spelling-fallback path (the recon's
    // `llvm::DenseMapInfo<hash_code, void>`) requires the real Clang/LLVM headers
    // (reachable only behind T1.0a scoping) and overlaps the T1.9 namespace->package
    // resolution work. Deferred — see report / backlog.
}
