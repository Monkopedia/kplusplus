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

import com.monkopedia.krapper.BindingIndex
import com.monkopedia.krapper.BindingRef
import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.DiagnosticPhase
import com.monkopedia.krapper.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.serialization.json.Json

/**
 * Determinism lock for the binding index (#186 brick B1).
 *
 * The index's whole value is that a consumer can trust it as krapper's own answer. That is only
 * worth anything if the bytes are a function of WHAT was emitted and not of the order things
 * happened to arrive in — §5 D1. These tests drive [BindingIndex.canonical], which is where that
 * guarantee lives, so a regression shows up here rather than as an unexplained diff in a
 * generated tree.
 */
class BindingIndexTest {

    private val json = Json { prettyPrint = true }

    private fun ref(spec: String, classId: String) = BindingRef(
        spec = spec,
        classId = classId,
        pkg = classId.substringBeforeLast('.', ""),
        simpleName = classId.substringAfterLast('.'),
        cppBase = if ('<' in spec) spec.substringBefore('<') else null
    )

    private val vectorInt = ref("std::vector<int>", "std.Vector__Int")
    private val vectorLong = ref("std::vector<long>", "std.Vector__Long")
    private val mapIntInt = ref("std::map<int, int>", "std.Map__Int__Int")

    @Test
    fun `arrival order does not change the emitted bytes`() {
        val forward = BindingIndex.canonical(
            runId = "r",
            bindings = listOf(vectorInt, vectorLong, mapIntInt),
            kotlinTypeMap = mapOf(
                vectorInt.classId to vectorInt.spec,
                vectorLong.classId to vectorLong.spec,
                mapIntInt.classId to mapIntInt.spec
            )
        )
        val reversed = BindingIndex.canonical(
            runId = "r",
            bindings = listOf(mapIntInt, vectorLong, vectorInt),
            kotlinTypeMap = mapOf(
                mapIntInt.classId to mapIntInt.spec,
                vectorLong.classId to vectorLong.spec,
                vectorInt.classId to vectorInt.spec
            )
        )
        assertEquals(
            json.encodeToString(BindingIndex.serializer(), forward),
            json.encodeToString(BindingIndex.serializer(), reversed),
            "the index must be a function of what was emitted, not of arrival order"
        )
    }

    @Test
    fun `bindings and the type map are sorted rather than merely stable`() {
        // A stable sort of already-sorted input would pass the test above while leaving the
        // order arbitrary. Assert the actual canonical order.
        val index = BindingIndex.canonical(
            runId = "r",
            bindings = listOf(vectorLong, mapIntInt, vectorInt),
            kotlinTypeMap = mapOf(
                vectorLong.classId to vectorLong.spec,
                mapIntInt.classId to mapIntInt.spec,
                vectorInt.classId to vectorInt.spec
            )
        )
        assertEquals(
            listOf("std::map<int, int>", "std::vector<int>", "std::vector<long>"),
            index.bindings.map { it.spec }
        )
        assertEquals(
            listOf("std.Map__Int__Int", "std.Vector__Int", "std.Vector__Long"),
            index.kotlinTypeMap.keys.toList()
        )
    }

    @Test
    fun `drops are ordered by symbol then location then message`() {
        val a = Diagnostic(Severity.WARNING, "second", symbol = "zzz")
        val b = Diagnostic(Severity.WARNING, "first", symbol = "aaa")
        val c = Diagnostic(Severity.WARNING, "aaa-message", symbol = "aaa")
        val index = BindingIndex.canonical(runId = "r", drops = listOf(a, b, c))
        assertEquals(
            listOf("aaa" to "aaa-message", "aaa" to "first", "zzz" to "second"),
            index.drops.map { it.symbol to it.message }
        )
    }

    @Test
    fun `a different emitted set produces a different index`() {
        // The negative control: if the two above passed because canonical() collapses
        // everything to a constant, this would pass too. It must not.
        val one = BindingIndex.canonical(runId = "r", bindings = listOf(vectorInt))
        val two = BindingIndex.canonical(runId = "r", bindings = listOf(vectorLong))
        assertNotEquals(
            json.encodeToString(BindingIndex.serializer(), one),
            json.encodeToString(BindingIndex.serializer(), two)
        )
    }

    @Test
    fun `relativizePath strips the checkout root so no host path reaches the index`() {
        // The real shape, from featuregen: every located drop pointed at a header inside the
        // checkout, and the emitted index contained /home/<user>/... until this was added.
        assertEquals(
            "../../src/cppMain/include/strings_feature.h",
            BindingIndex.relativizePath(
                "/home/someone/git/kplusplus/featuregen/src/cppMain/include/strings_feature.h",
                "/home/someone/git/kplusplus/featuregen/build/krapped-cpp"
            )
        )
        // Same tree, different checkout location: the relative answer must be identical, which
        // is the whole point (D1 — a tree generated in one checkout matches another).
        assertEquals(
            BindingIndex.relativizePath("/a/b/c/src/x.h", "/a/b/c/build/out"),
            BindingIndex.relativizePath("/zz/yy/c/src/x.h", "/zz/yy/c/build/out")
        )
        // Nothing in common but the filesystem root (a system header): left alone rather than
        // rewritten into a longer, less useful path.
        assertEquals(
            "/usr/lib/llvm-22/include/clang/AST/Decl.h",
            BindingIndex.relativizePath(
                "/usr/lib/llvm-22/include/clang/AST/Decl.h",
                "/home/someone/git/kplusplus/featuregen/build/krapped-cpp"
            )
        )
        // Already relative: untouched.
        assertEquals("src/x.h", BindingIndex.relativizePath("src/x.h", "/a/b"))
        // path IS base: every segment shared, nothing left over. "" is not a path.
        assertEquals(".", BindingIndex.relativizePath("/a/b/c", "/a/b/c"))
    }

    @Test
    fun `drops tying on symbol location and message are still ordered totally`() {
        // Without severity and phase in the comparator these two tie on every compared key, so
        // their relative order would come from the input and D1 would hold only by luck.
        val info = Diagnostic(Severity.INFO, "same", symbol = "s", phase = DiagnosticPhase.DEDUP)
        val warn = Diagnostic(
            Severity.WARNING,
            "same",
            symbol = "s",
            phase = DiagnosticPhase.RESOLVE
        )
        val forward = BindingIndex.canonical(runId = "r", drops = listOf(info, warn)).drops
        val reversed = BindingIndex.canonical(runId = "r", drops = listOf(warn, info)).drops
        assertEquals(forward, reversed, "ordering must not depend on arrival order")
        assertEquals(
            listOf(DiagnosticPhase.DEDUP, DiagnosticPhase.RESOLVE),
            forward.map { it.phase }
        )
    }

    @Test
    fun `the index round-trips through JSON`() {
        val index = BindingIndex.canonical(
            runId = "abc123",
            rootPackage = "com.example",
            bindings = listOf(vectorInt, mapIntInt),
            kotlinTypeMap = mapOf(vectorInt.classId to vectorInt.spec),
            drops = listOf(Diagnostic(Severity.WARNING, "dropped", symbol = "std::deque<int>"))
        )
        val encoded = json.encodeToString(BindingIndex.serializer(), index)
        assertEquals(index, json.decodeFromString(BindingIndex.serializer(), encoded))
        assertEquals(BindingIndex.SCHEMA_VERSION, index.schemaVersion)
    }
}
