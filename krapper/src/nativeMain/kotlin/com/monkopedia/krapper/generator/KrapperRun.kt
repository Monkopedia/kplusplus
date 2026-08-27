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

import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.generator.model.WrappedTU

/**
 * Everything one generation run owns that used to be a process global (brick B4,
 * docs/design/live-service.md §1.4).
 *
 * Before B4 the state below was five top-level `object`s and `var`s that
 * `IndexedServiceImpl.init` and `KrapperServiceImpl.index` *overwrote* at the start of every
 * run: `DropLedger.reset()`, `GenerationContext.reset(...)`, `BaseBindProfiler.reset()`,
 * `cppParseIncludeDirs = ...`, `cppModelDumpDir = ...`, plus `cppBaseModelTu` which the base
 * parse assigned. That shape has one run's state and the next run's state occupying the same
 * storage, so the only way to start clean was to destroy what came before. It is correct for
 * exactly one run per process and silently wrong for anything else — which is what B5's
 * persistent, build-to-build krapper is.
 *
 * A run's state now lives in an instance owned by its [IndexedServiceImpl]. Every service
 * method installs its run for the duration of the call via [using]; the deep, context-less
 * read sites (`WrappedType.invoke`'s intern cache, the drop-record sites in `Resolver` /
 * `Parsing` / `ModelResolution`, the parse's `-I` roots) read [current] instead of a global.
 * The alternative — threading a carrier through every resolve and codegen signature — was
 * rejected in [GenerationContext]'s KDoc for the same reason it is rejected here: those
 * surfaces are non-suspend and called pervasively, so it is sprawling churn for no
 * behavioural gain.
 *
 * **What this does and does not buy.** Two runs may be **alive at the same time** and stay
 * independent: each owns its ledger, profiler and context, so constructing or using one does
 * not disturb the other, and [using] is stack-disciplined so neither leaves state behind.
 * That is the property `DeterminismTest`'s **G3(c)** gate checks — two runs built before
 * either generates, used out of construction order, each against a genuine fresh-process
 * baseline. Its weaker sibling G3(b) only shows that *sequential* runs do not leak, which
 * the pre-B4 globals already satisfied because they were reset at construction; do not cite
 * it as the gate for this class.
 *
 * It is *not* a concurrency fix: two runs interleaving at a suspension point still share the
 * process-wide installed slot, so the daemon serializes calls (§4, "Single-tenancy").
 * De-globalizing is what makes serializing *sufficient* — before B4 even a serialized second
 * call destroyed the first run's ledger.
 */
class KrapperRun(
    /** The intern cache and the resolve-time config (root package, `-fno-rtti`). */
    val generation: GenerationContext,
    /**
     * The module's `kplusplus { headerDirectory(...) }` roots (`--include-dir`), threaded to
     * every parse as `-I` so cross-directory quote-includes resolve the same way the
     * generated wrapper's compile sees them. Deliberately NOT the header files' own parent
     * dirs: those reach only the wrapper compile (`IndexRequest.headerDirectories`).
     */
    val includeDirs: List<String> = emptyList(),
    /**
     * Optional ModelIo JSON dump dir (`--dump-model <dir>`): the base tree lands in
     * `base_model.json` and each forcing parse in `<forceName>.json`. Purely a debug aid for
     * inspecting the tree the resolver consumed — this format used to BE the inter-process
     * handoff, and is off the hot path now.
     */
    val dumpModelDir: String? = null
) {
    /** This run's skip-not-crash drop ledger. */
    val drops = DropLedger()

    /** This run's base-resolve profiler (inert unless `diag.baseBindTiming` is on). */
    val profiler = BaseBindProfiler()

    /**
     * The base tree, kept so each forcing parse can mirror libclang's first-seen ordering
     * against it (O1, #46). Written by `parseHeader`, read by `parseForcingModel`.
     */
    internal var baseModelTu: WrappedTU? = null

    /**
     * Arm a run from the service call that created it: the resolve-time config comes from
     * [config], the front-end's `-I` roots and optional model dump from [request]. This is
     * the single place a run is configured, replacing the assignments that used to be split
     * between `KrapperServiceImpl.index` and `IndexedServiceImpl.init`.
     */
    constructor(config: KrapperConfig, request: IndexRequest) : this(
        generation = GenerationContext(config.rootPackage, config.noRtti),
        includeDirs = request.includeDirs,
        dumpModelDir = request.dumpModelDir
    )

    companion object {
        /**
         * The run used when none is installed. Exists for unit tests that drive pieces of the
         * pipeline directly; every production read happens inside a [using] scope. A
         * production read that escaped its scope would land here, be shared between two
         * differently-configured runs, and diverge from their fresh-process baselines — which
         * is what `DeterminismTest`'s G3(b)/G3(c) gates measure.
         *
         * Escaping a scope otherwise fails SILENTLY, so reads of this fallback are reported
         * once per process through `GenerationContext.noteDetachedRead` (one shared latch
         * across both carriers) under the `diag.detachedReads` flag.
         */
        @PublishedApi
        internal val detached = KrapperRun(GenerationContext())

        @PublishedApi
        internal var installed: KrapperRun = detached

        /** The run currently in scope, or [detached] if there is none. */
        val current: KrapperRun
            get() = installed.also {
                if (it === detached) GenerationContext.noteDetachedRead("KrapperRun.current")
            }

        /**
         * Install [run] (and its [GenerationContext]) for the duration of [body], restoring
         * the previous installation afterwards so scopes nest and no run leaves its state
         * behind for the next one.
         */
        inline fun <T> using(run: KrapperRun, body: () -> T): T {
            val previous = installed
            installed = run
            try {
                return GenerationContext.using(run.generation, body)
            } finally {
                installed = previous
            }
        }
    }
}

/**
 * The drop ledger of the [KrapperRun] currently in scope. The drop sites in `Resolver`,
 * `Parsing`, `ModelResolution` and `WrappedKotlinType` are deep, non-suspend, and have no
 * carrier to thread (a PARSE-phase drop happens before any `ResolveContext` exists), so they
 * reach their run's ledger through here.
 */
internal val dropLedger: DropLedger
    get() = KrapperRun.current.drops

/** The base-resolve profiler of the [KrapperRun] currently in scope. */
internal val baseBindProfiler: BaseBindProfiler
    get() = KrapperRun.current.profiler
