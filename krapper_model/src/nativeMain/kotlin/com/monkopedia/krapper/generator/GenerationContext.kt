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

import com.monkopedia.krapper.generator.model.type.WrappedType

/**
 * Run-scoped state for a single generation pass: the [WrappedType] intern cache plus the
 * two config values that are baked into types at resolve time.
 *
 * **This is per-run state, not process state (brick B4, docs/design/live-service.md §1.4).**
 * It used to be an `object` whose state was `reset()` before each run, which made it a
 * process-global shared by every generation the process ever performed: a second run could
 * only start clean by *destroying* the first one's state, and two runs alive at once (a
 * persistent krapper serving successive builds — brick B5) would silently share an intern
 * cache and a root package. Now each run owns an instance, and [current] resolves to
 * whichever instance is installed by the enclosing [using] scope.
 *
 * The reads are the reason for the installed-instance indirection rather than a threaded
 * parameter: `WrappedType.invoke` / `WrappedType.kotlinType` / `fullyQualifiedType` are
 * context-less surfaces called pervasively from non-suspend code, so passing a carrier to
 * every call site would be sprawling churn for no behavioral gain.
 *
 * **Single-tenancy still applies within one process.** [using] is stack-disciplined (it
 * restores the previous installation), so nested and *sequential* runs are correct and
 * independent. Two runs interleaving at a suspension point are not: that is what B5's
 * mutex is for. What B4 buys is that the state itself is no longer shared, so serializing
 * calls is now sufficient — before B4 it was not, because run B's `reset()` destroyed
 * run A's state outright.
 */
class GenerationContext(
    /**
     * The configured root package every generated wrapper binding is nested under
     * (`config.rootPackage`), or null for the legacy top-level layout. A type's Kotlin
     * package is baked into its `ResolvedKotlinType` at resolve time, so this must be in
     * place before any resolution — i.e. the run's context is installed up front.
     */
    val rootPackage: String? = null,
    /**
     * True when the target C++ library is compiled `-fno-rtti` (e.g. v8's monolith),
     * so it exports no `typeinfo` symbols. Under this flag the down-cast helpers must NOT
     * emit a generic `dynamic_cast<D*>` (it would reference a `typeinfo for D` the library
     * doesn't provide and fail to LINK); only the LLVM-style `classof` down-cast — which
     * needs no RTTI — is emitted.
     */
    val noRtti: Boolean = false
) {
    /**
     * Intern/dedup cache for [WrappedType] values keyed by their string spelling, so
     * structurally-identical types share one instance within a pass. Per-run: a fresh
     * context starts empty, matching a fresh process.
     */
    val internedTypes = mutableMapOf<String, WrappedType>()

    companion object {
        /**
         * The context used when no run is installed. Exists for unit tests that drive
         * pieces of the pipeline directly (`WrappedType("int")` in a fixture builder);
         * every production read happens inside a `KrapperRun.using` scope, which is what
         * `DeterminismTest.twoConfigsInOneProcessMatchFreshProcesses` proves — a
         * production read that escaped the scope would land here, be shared between the
         * two configs, and diverge from the fresh-process baselines.
         */
        @PublishedApi
        internal val detached = GenerationContext()

        @PublishedApi
        internal var installed: GenerationContext = detached

        /** The context of the run currently in scope. */
        val current: GenerationContext
            get() = installed

        /**
         * Install [context] for the duration of [body] and restore the previous
         * installation afterwards, so scopes nest and a run cannot leave its context
         * behind for the next one. Callers should prefer `KrapperRun.using`, which
         * installs the whole run (ledger and parse config included) in one step.
         */
        inline fun <T> using(context: GenerationContext, body: () -> T): T {
            val previous = installed
            installed = context
            try {
                return body()
            } finally {
                installed = previous
            }
        }
    }
}
