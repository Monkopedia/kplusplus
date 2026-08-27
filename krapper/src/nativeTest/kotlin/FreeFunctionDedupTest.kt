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
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Free-function dedup invariant (issue #62, the namespace sibling of #60's class dedup).
 *
 * Every `requestInstantiation` re-appends a re-resolved copy of each already-bound
 * top-level element — both classes (#60) and namespace FREE FUNCTIONS (#62). #60's
 * [dedupClassesLastWins] collapsed only the class copies and passed free functions through
 * untouched, so featuregen's 17 instantiation requests left 18 copies of every namespace
 * free function in the element list. Each writer then emitted a full wrapper section per
 * copy: 18 `.h` declarations + 18 `.cc` definitions (`_`-uniquified into dead variants) and
 * 18 Kotlin facades (`default_handler`, `_default_handler`, `__default_handler`, ...).
 *
 * These tests lock the extended invariant directly on [dedupClassesLastWins]: N forcing
 * passes of a namespace free function collapse to ONE section, while genuine OVERLOADS
 * (same name, different parameters) stay distinct.
 */
class FreeFunctionDedupTest {

    /** A namespaced free function — a STATIC [WrappedMethod] parented to a [WrappedNamespace]. */
    private fun freeFn(
        ns: WrappedNamespace,
        name: String,
        argTypes: List<WrappedType> = emptyList()
    ) = WrappedMethod(name, WrappedType("int"), MethodType.STATIC).also { fn ->
        argTypes.forEachIndexed { i, type ->
            fn.addChild(WrappedArgument("a$i", type))
        }
        ns.addChild(fn)
        fn.parent = ns
    }

    /**
     * Resolve a TU whose `Ns` namespace holds [build] free functions, returning the
     * top-level resolved element list (free functions surface as top-level [ResolvedMethod]s,
     * exactly as the writers consume them). Installs a fresh per-run [KrapperRun] as
     * `IndexedServiceImpl` does for every service call.
     */
    private fun resolveFreeFns(build: (WrappedNamespace) -> Unit): List<ResolvedElement> =
        runBlocking {
            KrapperRun.using(KrapperRun(GenerationContext())) {
                val tu = WrappedTU()
                val ns = WrappedNamespace("Ns").also {
                    tu.addChild(it)
                    it.parent = tu
                }
                build(ns)
                val resolver = ParsedResolver(tu)
                resolver.findClasses { defaultFilter() }
                    .resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)
            }
        }

    private val ResolvedMethod.identity: String
        get() = "$qualified::$name(${args.joinToString(",") { it.type.toString() }})"

    // N forcing passes of the SAME free function collapse to a single section. We simulate
    // the cumulative `requestInstantiation` append by concatenating the resolved list 18×
    // (featuregen's header parse + 17 instantiation requests) and running the real dedup.
    @Test
    fun repeated_forcing_passes_collapse_a_free_function_to_one_section() {
        val resolved = resolveFreeFns { ns -> freeFn(ns, "handler") }
        val handler = resolved.filterIsInstance<ResolvedMethod>().single { it.name == "handler" }

        val forced: List<ResolvedElement> = (1..18).flatMap { resolved }
        assertEquals(
            18,
            forced.filterIsInstance<ResolvedMethod>().count { it.name == "handler" },
            "sanity: the simulated forcing passes must produce 18 copies before dedup"
        )

        val deduped = forced.dedupClassesLastWins()
        val handlersAfter = deduped.filterIsInstance<ResolvedMethod>().filter {
            it.name == "handler"
        }
        assertEquals(
            1,
            handlersAfter.size,
            "18 forcing copies of Ns::handler must dedup to a single section, got " +
                handlersAfter.map { it.identity }
        )
        assertEquals(handler.identity, handlersAfter.single().identity)
    }

    // Genuine overloads — same name, different parameter types — must NOT collapse into each
    // other: each distinct C++ signature survives as its own section after dedup.
    @Test
    fun overloaded_free_functions_stay_distinct_through_dedup() {
        val resolved = resolveFreeFns { ns ->
            freeFn(ns, "combine", listOf(WrappedType("int")))
            freeFn(ns, "combine", listOf(WrappedType("double")))
            freeFn(ns, "combine", listOf(WrappedType("int"), WrappedType("int")))
        }
        val combinesBefore = resolved.filterIsInstance<ResolvedMethod>()
            .filter { it.name == "combine" }
        assertEquals(
            3,
            combinesBefore.size,
            "the three overloads must all resolve: ${combinesBefore.map { it.identity }}"
        )

        // 18 forcing passes over all three overloads, then dedup.
        val deduped = ((1..18).flatMap { resolved }).dedupClassesLastWins()
        val combinesAfter = deduped.filterIsInstance<ResolvedMethod>().filter {
            it.name == "combine"
        }
        assertEquals(
            3,
            combinesAfter.size,
            "each distinct overload signature must survive dedup as its own section, got " +
                combinesAfter.map { it.identity }
        )
        assertEquals(
            combinesBefore.map { it.identity }.toSet(),
            combinesAfter.map { it.identity }.toSet(),
            "dedup must preserve exactly the distinct overload signatures"
        )
    }

    // Dedup is order-independent: shuffling the forcing copies yields the same surviving
    // identities (the bootstrap reproducibility property #60/#62 depend on).
    @Test
    fun dedup_is_order_independent() {
        val resolved = resolveFreeFns { ns ->
            freeFn(ns, "handler")
            freeFn(ns, "combine", listOf(WrappedType("int")))
            freeFn(ns, "combine", listOf(WrappedType("double")))
        }
        fun survivors(list: List<ResolvedElement>) =
            list.dedupClassesLastWins().filterIsInstance<ResolvedMethod>()
                .map { it.identity }.toSet()

        val forward = (1..18).flatMap { resolved }
        val reversed = forward.reversed()
        assertEquals(survivors(forward), survivors(reversed))
        assertTrue(survivors(forward).isNotEmpty(), "the model must resolve at least one free fn")
    }
}
