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

import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Skip-not-crash at the SURVIVING boundary (issue #89).
 *
 * The original [SkipNotCrashTest] drove the core invariant — "an unmodelable shape is
 * DROPPED + logged, it does not crash the run" — by parsing an inline C++ header through
 * the in-process libclang reducer that the self-hosting flip (B5, #88) deleted. That parse
 * path is gone, so the test can't be ported as-is. The invariant itself is unchanged and
 * now lives on the self-hosted pipeline: a [WrappedElement] model (whatever produced it —
 * the cppfrontend binary in production, a hand-built model here) flows through
 * resolve+codegen, and any element it can't model must be dropped through the central
 * [ResolveContext.notifyFailed] choke point into the [DropLedger] rather than throwing.
 *
 * These tests build the model directly (no parse) and exercise exactly that resolve-phase
 * skip-not-crash contract:
 *   - an unresolvable return type drops the offending method while its bindable siblings
 *     still resolve, and the drop is ledgered WITH A REASON (RESOLVE phase),
 *   - a fully-bindable class drops nothing (the ledger does not false-positive), and
 *   - a const/non-const accessor pair de-dups to the const half (DEDUP phase), ledgered.
 */
class SkipNotCrashResolveTest {

    private fun intType() = WrappedType("int")

    /** A namespaced class wired into a fresh [WrappedTU], ready for resolve. */
    private fun classInTu(name: String, build: WrappedClass.() -> Unit): Pair<WrappedTU, WrappedClass> {
        val tu = WrappedTU()
        val ns = WrappedNamespace("Skip").also {
            tu.addChild(it)
            it.parent = tu
        }
        val cls = WrappedClass(name).also {
            ns.addChild(it)
            it.parent = ns
            it.build()
        }
        return tu to cls
    }

    private fun method(name: String, returnType: WrappedType, isConst: Boolean = false) =
        WrappedMethod(name, returnType, MethodType.METHOD).also { it.isConst = isConst }

    /**
     * Reset the process-scoped generation state (as `IndexedServiceImpl.init` does), then
     * run the real `findClasses { defaultFilter() }.resolveAll(...)` path over [tu] under
     * [policy] — the exact resolve the self-hosted CLI runs, minus the parse front-end.
     */
    private fun resolve(
        tu: WrappedTU,
        policy: ReferencePolicy = ReferencePolicy.IGNORE_MISSING
    ): List<ResolvedClass> = runBlocking {
        DropLedger.reset()
        GenerationContext.reset(null)
        val resolver = ParsedResolver(tu)
        resolver.findClasses { defaultFilter() }
            .resolveAll(resolver, policy)
            .filterIsInstance<ResolvedClass>()
    }

    private fun ResolvedClass.methodNames() =
        children.filterIsInstance<ResolvedMethod>().map { it.name }

    // The core invariant: a method whose return type cannot be resolved (under
    // IGNORE_MISSING) is DROPPED, not fatal. The class still binds with its resolvable
    // members; the unmodelable method is gone; and the central notifyFailed choke point
    // ledgers it as a RESOLVE-phase drop with a non-blank reason — so a consumer can tell
    // "the generator dropped bad()" from "the model never had it".
    @Test
    fun unresolvable_return_type_drops_the_method_not_the_run() {
        val (tu, _) = classInTu("Widget") {
            addChild(method("value", intType(), isConst = true))
            // Skip::Missing is declared nowhere — its return type can't resolve.
            addChild(method("bad", WrappedType("Skip::Missing")))
        }
        val resolved = resolve(tu)

        val widget = resolved.single { it.name == "Widget" }
        assertTrue(
            "value" in widget.methodNames(),
            "the bindable method must still resolve: ${widget.methodNames()}"
        )
        assertFalse(
            "bad" in widget.methodNames(),
            "the unmodelable method must be dropped: ${widget.methodNames()}"
        )
        val drop = DropLedger.drops.firstOrNull { "bad" in it.symbol }
        assertTrue(
            drop != null && drop.phase == DropPhase.RESOLVE,
            "the dropped method must be ledgered as a RESOLVE drop: ${DropLedger.drops}"
        )
        assertTrue(
            drop.reason.isNotBlank(),
            "every drop must carry a reason: $drop"
        )
        // hasDrops() is exactly the predicate the opt-in --fail-on-drop gate reads
        // (IndexedServiceImpl.reportDrops): a dropped symbol makes it true.
        assertTrue(
            DropLedger.hasDrops(),
            "a dropped symbol must make --fail-on-drop's hasDrops() gate fire"
        )
    }

    // The ledger does not false-positive: a fully-bindable class drops nothing.
    @Test
    fun fully_bindable_class_records_no_drops() {
        val (tu, _) = classInTu("Clean") {
            addChild(method("value", intType(), isConst = true))
            addChild(method("scaled", intType()).also {
                it.addChild(WrappedArgument("scale", intType()))
            })
        }
        val resolved = resolve(tu)

        assertTrue(
            "value" in resolved.single { it.name == "Clean" }.methodNames(),
            "the bindable class must resolve its members"
        )
        assertEquals(
            emptyList(),
            DropLedger.drops,
            "a fully-bindable class must drop nothing: ${DropLedger.drops}"
        )
        assertFalse(
            DropLedger.hasDrops(),
            "a clean run must leave --fail-on-drop's hasDrops() gate false"
        )
    }

    // The DEDUP skip path: a class declaring both a `const` and a non-`const` overload of
    // the same accessor (same name + params) is the read-only/mutable halves of ONE logical
    // accessor. The dedup keeps the const half and drops the non-const one, ledgering the
    // drop as a DEDUP-phase record — another skip-not-crash decision that must be visible.
    @Test
    fun const_overload_duplicate_is_deduped_and_ledgered() {
        val (tu, _) = classInTu("Accessor") {
            addChild(method("get", intType(), isConst = false))
            addChild(method("get", intType(), isConst = true))
        }
        val resolved = resolve(tu)

        val accessor = resolved.single { it.name == "Accessor" }
        assertEquals(
            listOf("get"),
            accessor.methodNames().filter { it == "get" || it == "_get" },
            "the const/non-const pair must collapse to ONE `get`: ${accessor.methodNames()}"
        )
        val dedup = DropLedger.drops.firstOrNull { it.phase == DropPhase.DEDUP }
        assertTrue(
            dedup != null && "get" in dedup.symbol,
            "the deduped non-const overload must be ledgered as a DEDUP drop: ${DropLedger.drops}"
        )
    }
}
