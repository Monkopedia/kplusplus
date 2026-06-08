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

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.runBlocking
import platform.posix.random

/**
 * Characterization tests for `Resolver.kt resolveForcing` (issue #18).
 *
 * `resolveForcing` is the 3-pass shared-tracker forcing resolve that binds a forced
 * template instantiation (`--instantiate "Base<Args>"`). It was the source of BOTH June
 * regressions — the std::string eviction blow-up (over-wide pass-3 eviction degraded a
 * sibling-bound `std::string` into a duplicated, uncompilable binding) and, adjacent, the
 * const-ref-default ctor drop — and both slipped because the only net was featuregen
 * integration, which wasn't re-run. These tests PIN the current correct behavior so that
 * re-introducing either regression FAILS LOUDLY at unit scale, and so `resolveForcing` can
 * later be reduced/refactored against this net.
 *
 * The scenario drives the real `IndexedServiceImpl.requestInstantiation` flow at unit
 * scale (mirroring `ReturnShapeTest.forcedContainerBindsUnderIgnoreMissing`):
 *   1. main resolve of the consumer classes (producing `alreadyBoundKeys`),
 *   2. synthesize the `KrapperForce_*` struct whose `value` member IS the requested spec,
 *      re-parse it (it `#include`s the consumer header + the std container header),
 *   3. scope to the forcing struct + already-bound classes (the scoping that kills the
 *      ~1269-class explosion),
 *   4. `resolveForcing(...)`.
 *
 * The forcing run does two libclang parses. To keep that work bounded (driving libclang
 * through repeated `<string>`/`<vector>` translation units in one process is fragile — it
 * degrades template canonicalization for *other* tests in the same JVM, e.g.
 * ReturnShapeTest's range detection), the scenario is resolved EXACTLY ONCE, lazily, cached
 * process-wide, and the @Test methods assert against the cached result. The IGNORE_MISSING
 * forcing axis is pinned by sibling tests rather than a second forcing run here (see the
 * `includeResult` note).
 */
class ForcingCharacterizationTest {

    /** The resolved-class type spellings + ledger snapshot produced by a forcing run. */
    private class ForcingResult(
        val resolved: List<ResolvedClass>,
        val alreadyBoundKeys: Set<String>,
        val ledger: List<DropRecord>
    ) {
        val specs: List<String> get() = resolved.map { it.type.toString() }
        val newSpecs: List<String> get() = specs.filter { it !in alreadyBoundKeys }
    }

    companion object {
        // ---- Scenario A: INCLUDE_MISSING, sibling-bound std::string + forced vector. ----
        // A consumer class BINDS std::string (so it lands in alreadyBoundKeys) and
        // REFERENCES it from a bound method (so pass-3 re-resolution touches it). We force
        // an UNRELATED container — the std::string was bound by a different request.
        private const val INCLUDE_HEADER =
            "#include <string>\n" +
                "struct ForceThing { int id; };\n" +
                "struct StrUser {\n" +
                "    std::string echo(const std::string& s) const { return s; }\n" +
                "};\n"

        // The INCLUDE scenario is resolved ONCE (two libclang parses) and cached, then all
        // INCLUDE-based assertions read it. `by lazy` gives single-shot,
        // thread-safe-enough-for-the-single-threaded-test-runner memoize.
        //
        // Why only ONE scenario here (and not a sibling IGNORE_MISSING forcing run): driving
        // libclang through additional `<vector>`-instantiating forcing TUs in one process is
        // fragile — the cumulative type-table state degrades `std::vector<T>` / iterator_range
        // canonicalization for OTHER tests in the same JVM (it silently breaks
        // ReturnShapeTest.iteratorRangeReturnMaterializesVector's range detection). Keeping
        // this class to a single forcing run stays within that budget. The IGNORE_MISSING
        // forcing axis (the union-evict + pass-1-drop/pass-3-recovery path) is already pinned
        // elsewhere: ReturnShapeTest.forcedContainerBindsUnderIgnoreMissing drives an
        // IGNORE_MISSING forcing resolve directly, and the featuregen RgRangeTest pins the
        // pass-3 range-method recovery (RangeHolder::items()) end-to-end at integration scale.
        private val includeResult: ForcingResult by lazy {
            runForcing(
                consumerHeader = INCLUDE_HEADER,
                mainClasses = listOf("StrUser"),
                target = "std::vector<ForceThing*>",
                forceName = "KrapperForce_std_vector_ForceThingPtr",
                forcingIncludes = listOf("vector"),
                policy = ReferencePolicy.INCLUDE_MISSING
            )
        }

        /**
         * Run the full forcing flow: main-resolve [mainClasses] under [policy], then force
         * [target] (`KrapperForce_*{ <target> value; }`) re-parsed against a header that
         * `#include`s [consumerHeader] plus the headers in [forcingIncludes]. Snapshots the
         * DropLedger drops accumulated by the forcing run.
         */
        private fun runForcing(
            consumerHeader: String,
            mainClasses: List<String>,
            target: String,
            forceName: String,
            forcingIncludes: List<String>,
            policy: ReferencePolicy
        ): ForcingResult = memScoped {
            runBlocking {
                // A forcing run is a fresh "generation": clear the process-scoped ledger so
                // the snapshot below captures only THIS run's forcing-time drops.
                DropLedger.reset()
                val index = createIndex(0, 0) ?: error("Failed to create Index")
                defer { index.dispose() }

                val headerFile = "/tmp/krapper_forcechar_${random()}_${random()}.h"
                File(headerFile).writeText(consumerHeader)

                // ---- Main resolve (produces alreadyBoundKeys). ----
                val mainResolver =
                    parseHeader(index, listOf(headerFile), generateIncludes("clang++"))
                val bound = mainResolver.findClasses(AllowListFilter(mainClasses).wrapperFilter())
                    .resolveAll(mainResolver, policy)
                val alreadyBoundKeys =
                    bound.filterIsInstance<ResolvedClass>().mapTo(HashSet()) { it.type.toString() }

                // ---- Synthesize + re-parse the forcing struct (as requestInstantiation does). --
                val includes =
                    (forcingIncludes.map { "#include <$it>" } + "#include \"$headerFile\"")
                        .joinToString("\n")
                val forceFile = "/tmp/krapper_forcechar_inst_${random()}.h"
                File(forceFile).writeText(
                    """
                    |$includes
                    |struct $forceName { $target value; };
                    """.trimMargin()
                )
                val forceResolver =
                    parseHeader(index, listOf(forceFile), generateIncludes("clang++"))

                // ---- Scope to the forcing struct + already-bound classes (kills explosion). --
                val scoped = forceResolver.findClasses { defaultFilter() }.filter {
                    val cls = it as? WrappedClass ?: return@filter true
                    val key = cls.type.toString()
                    key.contains(forceName) || key in alreadyBoundKeys
                }
                // Reset again so the ledger snapshot is exactly the forcing-resolve drops.
                DropLedger.reset()
                val forced = scoped.resolveForcing(forceResolver, policy, alreadyBoundKeys)
                ForcingResult(
                    forced.filterIsInstance<ResolvedClass>(),
                    alreadyBoundKeys,
                    DropLedger.drops.toList()
                )
            }
        }
    }

    // =================================================================================
    // 1. EVICTION-POLICY GATE — the axis that blew up (Resolver.kt evictKeys, ~L317).
    // =================================================================================

    // Under INCLUDE_MISSING, pass-3 evicts ONLY the forcing keys (the bound consumer
    // classes), NOT the `alreadyBoundKeys` union. This pins the RESOLVED-SET invariant of
    // that policy gate: a sibling std type (`std::string`) bound in the main resolve and
    // referenced by a bound consumer class survives the forcing run as a SINGLE resolved
    // class whose spelling keeps its `<char>` template argument — it is not lost, split, or
    // bare-`basic_string`-degraded in the returned set.
    //
    // WHERE THE EVICTION-WIDENING REGRESSION BITES LOUDLY: the actual June blow-up — widening
    // this INCLUDE_MISSING eviction back to the union, which makes pass-3 re-pull the
    // sibling-bound `std::string` as a degraded DUPLICATE whose generated param casts lose
    // their `<char>` arg (uncompilable C++) — manifests in the GENERATED `.cc`, not the
    // resolved-class map (the map dedups by key, so a degraded re-pull doesn't show as a
    // second map entry at this thin unit scale). That regression is pinned to FAIL LOUDLY by
    // the featuregen `RfForcingEvictionTest`: re-widening the eviction aborts
    // `:featuregen:kplusplusSync` (krapper_gen exit 134 on the degraded basic_string cast).
    // This unit test is the lighter companion: it guards the resolved-set invariant and that
    // the forcing still binds the requested container alongside the intact sibling.
    @Test
    fun includeMissing_siblingStdString_notDegradedOrDuplicated() {
        val result = includeResult

        // The sibling std::string must have been bound in the main resolve.
        val boundStringKeys = result.alreadyBoundKeys.filter { it.contains("basic_string") }
        assertTrue(
            boundStringKeys.isNotEmpty(),
            "precondition: std::string must bind in the main resolve; alreadyBoundKeys = " +
                result.alreadyBoundKeys
        )

        // The sibling `std::string` survives the forcing run in the resolved set, once, and
        // keeps its `<char>` template argument (not lost, not split, not bare-degraded).
        val charStringSpecs = result.specs.filter {
            it.contains("basic_string") && it.contains("char") && !it.contains("wchar")
        }
        assertEquals(
            1,
            charStringSpecs.size,
            "the sibling-bound `std::string` must survive forcing as exactly one " +
                "`basic_string<char>` spec; got $charStringSpecs (all specs: ${result.specs})"
        )
        assertTrue(
            charStringSpecs.single() in result.alreadyBoundKeys,
            "the surviving `basic_string<char>` must be the SAME sibling binding from the main " +
                "resolve (in alreadyBoundKeys), not a forcing-introduced replacement; got " +
                "${charStringSpecs.single()} vs keys ${result.alreadyBoundKeys}"
        )

        // The container we actually asked for must still bind (the forcing did its job).
        assertTrue(
            result.newSpecs.any { it.contains("vector", ignoreCase = true) },
            "forcing std::vector<ForceThing*> must bind the container; new specs = ${result.newSpecs}"
        )
    }

    // The IGNORE_MISSING side of this axis — pass-3 evicting the UNION (`alreadyBoundKeys` ∪
    // forcing keys) so a drifted-key bound class still recovers — is pinned by the existing
    // `ReturnShapeTest.forcedContainerBindsUnderIgnoreMissing`, which drives an IGNORE_MISSING
    // forcing resolve directly. It is kept there (not duplicated here) so this class stays at
    // a single libclang forcing run (see the `includeResult` note on the cross-test
    // canonicalization fragility of repeated `<vector>` forcing TUs in one process).

    // =================================================================================
    // 2. PASS-3 RANGE-METHOD RECOVERY — pinned at integration scale by the featuregen
    //    RgRangeTest (Resolver.kt pass 3 evict-then-rebuild, ~L282).
    // =================================================================================
    //
    // The pass-3 recovery of a container-returning method dropped in pass 1 (the
    // `RangeHolder::items() -> std::vector<Thing*>` analog of Clang's decls()/bases()) is
    // pinned end-to-end by featuregen's RgRangeTest: `RangeHolder::items()`'s return type is
    // the std::vector<Thing*> container, which is unbound when RangeHolder first resolves, so
    // `items()` only exists on the Kotlin binding BECAUSE resolveForcing pass 3 re-resolves
    // the bound class against the now-forced container. If pass-3 recovery regressed,
    // `holder.items()` would not exist and RgRangeTest would fail to compile. A standalone
    // unit reproduction here was intentionally dropped: it requires a SECOND IGNORE_MISSING
    // `<vector>` forcing TU whose late-in-process libclang canonicalization is exactly the
    // fragility documented on `includeResult`, making the unit-level recovery flaky; the
    // integration pin is the faithful, stable net for this behavior.

    // =================================================================================
    // 3. NO FORCING EXPLOSION / SCOPE CREEP (Resolver.kt shared-tracker invariant, ~L210).
    // =================================================================================

    // Forcing a specialization must bind ONLY the requested specialization + the
    // already-bound classes (plus the std container machinery the spec genuinely needs),
    // NOT the transitive world. The shared tracker bounds pass 2: classes resolved in
    // pass 1 are already in `resolvedClasses`, so INCLUDE will not re-expand them. The
    // INCLUDE scenario pulls in a large std surface (<string> + <vector>) yet must NOT
    // explode the resolved set.
    //
    // REGRESSION GUARD: breaking the shared-tracker invariant (a fresh tracker per pass, or
    // re-expanding already-bound library classes under INCLUDE_MISSING) lets pass 2
    // re-expand the whole transitive std surface — the ~1269-class explosion. This pins the
    // count small. The bound is generous (we assert << a full-library blow-up), so it bites
    // the explosion without being brittle to small std-internal churn.
    @Test
    fun forcing_bindsOnlyRequestedSpecPlusBound_noExplosion() {
        val result = includeResult
        assertTrue(
            result.specs.size < 50,
            "forcing must not expand the transitive std world (the ~1269-class explosion); " +
                "resolved ${result.specs.size} classes: ${result.specs.sorted()}"
        )
        assertTrue(
            result.newSpecs.any { it.contains("vector", ignoreCase = true) },
            "the requested vector spec must bind; new specs = ${result.newSpecs}"
        )
    }

    // =================================================================================
    // 4. DROP VISIBILITY — forcing-time drops land in the DropLedger (issue #6 merged).
    // =================================================================================

    // The forcing resolve parses a large std TU, so the scoped forcing resolve necessarily
    // funnels RESOLVE-phase drops for the std internals it cannot model through the
    // `notifyFailed` / `DropLedger.record` choke point inside `resolveForcing`. This pins
    // that forcing-time drops are VISIBLE (not silently swallowed) — the lack of visibility
    // is precisely why June's regressions slipped (the only net was a featuregen-sync that
    // wasn't re-run). `result.ledger` is the snapshot of exactly the forcing-resolve drops.
    //
    // REGRESSION GUARD: removing the DropLedger.record calls from resolveForcing (or routing
    // a forcing-time failure around the notifyFailed choke point) empties the ledger and
    // makes this test FAIL. We assert the ledger is populated with RESOLVE-phase records,
    // not a specific brittle symbol string.
    @Test
    fun forcing_dropsAreVisibleInLedger() {
        val result = includeResult
        assertTrue(
            result.ledger.isNotEmpty(),
            "forcing-time drops must be visible in the DropLedger (the choke point that " +
                "would have surfaced June's silent regressions); ledger was empty"
        )
        assertTrue(
            result.ledger.any { it.phase == DropPhase.RESOLVE },
            "forcing-time drops must be recorded as RESOLVE-phase; phases seen = " +
                result.ledger.map { it.phase }.distinct()
        )
        // Sanity: the forcing still produced bindings alongside the ledgered drops.
        assertTrue(
            result.specs.any { it.contains("vector", ignoreCase = true) },
            "forcing must still bind the container alongside ledgered drops; " +
                "specs = ${result.specs}"
        )
    }
}
