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

import com.monkopedia.krapper.generator.Utils.printerrln
import kotlin.time.TimeSource

private const val PROGRESS_EVERY = 100
private const val PROGRESS_INTERVAL_MS = 3_000L
private const val TOP_N = 25

/**
 * Run-scoped, off-by-default profiler for the BASE resolve (`filterAndResolve` / `resolveAll`
 * — the pass that runs BEFORE any forcing). Gated by the `diag.baseBindTiming` experimental flag
 * ([ExperimentalFlags.DIAG_BASE_BIND_TIMING]); when the flag is OFF, every method here is an inert
 * early-return, so a normal run is byte-identical to one with no profiler wired in at all
 * (measurement only — the profiler never changes what gets bound).
 *
 * It answers the issue-#10 question the base bind poses: is the ~240 s base-resolve cost
 *   (a) UNIFORM-BREADTH — ~N classes each costing ~constant time, a steady classes/sec rate; the
 *       lever is bounding the surface; or
 *   (b) a HOTSPOT / SUPERLINEAR — a decaying rate (O(n²)) or a few pathological heavily-templated
 *       types dominating; the lever is fixing that hotspot.
 *
 * To be useful even when the run TIMES OUT before completing, it emits STREAMING progress:
 *  - every [PROGRESS_EVERY] resolved types (or [PROGRESS_INTERVAL_MS] wall-clock, whichever first)
 *    it logs cumulative count, elapsed, instantaneous rate (classes/sec over the last window) and
 *    cumulative rate — a decaying instantaneous rate is the O(n²) fingerprint;
 *  - it keeps a running TOP-N slowest types by resolve time (so the pathological types are named);
 *  - it counts full-TU tree walks (`ParsedResolver.resolve`/`resolveTemplate`'s `filterRecursive`)
 *    and the nodes visited, which is the concrete O(distinct-types × tree-size) suspect;
 *  - it counts INCLUDE_MISSING on-demand materializations and re-entries, to spot repeated work.
 *
 * [report] dumps the final distribution (top-N, walk counters, per-bucket rate curve).
 *
 * **One profiler per run, not per process (brick B4, docs/design/live-service.md §1.4).** It
 * was an `object` with a `reset()` called from `IndexedServiceImpl.init`; its counters now
 * belong to the [KrapperRun] in scope, so a partial or timed-out run cannot contribute its
 * counters to a later run in the same process (and a persistent krapper — brick B5 — can
 * profile two builds without them summing together).
 */
class BaseBindProfiler {
    val enabled: Boolean
        get() = ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_BASE_BIND_TIMING)

    // ---- run-scoped state ----
    private var startMark = TimeSource.Monotonic.markNow()
    private var lastLogMark = startMark
    private var lastLogCount = 0

    // Count of times ParsedResolver.resolve produced a NEW resolution (a class actually resolved,
    // not a memo hit) — this is the true "classes resolved" the rate is computed over.
    private var resolvedCount = 0

    // filterRecursive (full-TU walk) instrumentation — the O(n²) suspect.
    private var treeWalks = 0
    private var treeNodesVisited = 0L

    // INCLUDE_MISSING on-demand materialization counters.
    private var includeMaterializations = 0
    private var includeReentries = 0

    // Per-type resolve time, kept only for the top-N slowest (bounded memory): (typeString, ms),
    // held sorted descending, capped at TOP_N.
    private val slowest = ArrayList<Pair<String, Double>>()

    // Coarse rate curve: (cumulativeCount, cumulativeElapsedMs) samples at each progress log.
    private val rateCurve = ArrayList<Pair<Int, Double>>()

    /** Announce the base resolve is starting over [totalFiltered] initially-filtered classes. */
    fun begin(totalFiltered: Int) {
        if (!enabled) return
        startMark = TimeSource.Monotonic.markNow()
        lastLogMark = startMark
        lastLogCount = 0
        printerrln(
            "basebind: START — $totalFiltered filtered classes " +
                "(transitive INCLUDE_MISSING expansion may resolve far more)"
        )
    }

    /**
     * Record that resolving [typeString] took [ms] and produced a genuinely-new resolution. This is
     * the per-type distribution sample AND the tick that drives streaming progress. Call ONLY for a
     * real resolution (not a memo hit), so the rate reflects work actually done.
     */
    fun recordResolved(typeString: String, ms: Double) {
        if (!enabled) return
        resolvedCount++
        offerSlowest(typeString, ms)
        maybeLogProgress()
    }

    /** One full-TU `filterRecursive` walk visiting [nodes] elements (the O(n²) suspect). */
    fun recordTreeWalk(nodes: Int) {
        if (!enabled) return
        treeWalks++
        treeNodesVisited += nodes
    }

    /** An INCLUDE_MISSING on-demand materialization of a referenced-but-unbound type. */
    fun recordInclude(reentry: Boolean) {
        if (!enabled) return
        if (reentry) includeReentries++ else includeMaterializations++
    }

    private fun offerSlowest(typeString: String, ms: Double) {
        if (slowest.size < TOP_N) {
            slowest.add(typeString to ms)
            slowest.sortByDescending { it.second }
        } else if (ms > slowest.last().second) {
            slowest[slowest.size - 1] = typeString to ms
            slowest.sortByDescending { it.second }
        }
    }

    private fun maybeLogProgress() {
        val sinceLog = lastLogMark.elapsedNow().inWholeMilliseconds
        if (resolvedCount - lastLogCount < PROGRESS_EVERY && sinceLog < PROGRESS_INTERVAL_MS) return
        val totalMs = startMark.elapsedNow().inWholeMicroseconds / 1000.0
        val windowCount = resolvedCount - lastLogCount
        val windowMs = if (sinceLog > 0) sinceLog.toDouble() else 1.0
        val instRate = windowCount * 1000.0 / windowMs
        val cumRate = if (totalMs > 0) resolvedCount * 1000.0 / totalMs else 0.0
        rateCurve.add(resolvedCount to totalMs)
        printerrln(
            "basebind: resolved=$resolvedCount elapsed=${fmt(totalMs)}ms " +
                "inst=${fmt(instRate)}/s cum=${fmt(cumRate)}/s " +
                "walks=$treeWalks nodes=$treeNodesVisited " +
                "incl=$includeMaterializations reentry=$includeReentries"
        )
        lastLogMark = TimeSource.Monotonic.markNow()
        lastLogCount = resolvedCount
    }

    /** Final distribution dump (call after resolveAll returns, or from a shutdown path). */
    fun report() {
        if (!enabled) return
        val totalMs = startMark.elapsedNow().inWholeMicroseconds / 1000.0
        val cumRate = if (totalMs > 0) resolvedCount * 1000.0 / totalMs else 0.0
        printerrln("basebind: ===== REPORT =====")
        printerrln(
            "basebind: resolved=$resolvedCount total=${fmt(totalMs)}ms avgRate=${fmt(cumRate)}/s " +
                "avgPerType=${fmt(if (resolvedCount > 0) totalMs / resolvedCount else 0.0)}ms"
        )
        printerrln(
            "basebind: treeWalks=$treeWalks nodesVisited=$treeNodesVisited " +
                "avgNodesPerWalk=${fmt(
                    if (treeWalks > 0) treeNodesVisited.toDouble() / treeWalks else 0.0
                )}"
        )
        printerrln(
            "basebind: includeMaterializations=$includeMaterializations reentries=$includeReentries"
        )
        val topSum = slowest.sumOf { it.second }
        // NB: no literal '%' — printerrln passes the string straight to fprintf as a FORMAT
        // string, so a '%' would be interpreted as a conversion. Report the fraction as "pct".
        printerrln(
            "basebind: top-$TOP_N slowest sum=${fmt(topSum)}ms " +
                "(${fmt(if (totalMs > 0) topSum * 100 / totalMs else 0.0)} pct of total):"
        )
        slowest.forEachIndexed { i, (name, ms) ->
            printerrln("basebind:   #${i + 1} ${fmt(ms)}ms  $name")
        }
        printerrln("basebind: rate curve (count @ elapsedMs):")
        rateCurve.forEach { (count, ms) -> printerrln("basebind:   $count @ ${fmt(ms)}ms") }
        printerrln("basebind: ==================")
    }

    private fun fmt(v: Double): String {
        val scaled = (v * 100).toLong()
        return "${scaled / 100}.${(scaled % 100).toString().padStart(2, '0')}"
    }
}
