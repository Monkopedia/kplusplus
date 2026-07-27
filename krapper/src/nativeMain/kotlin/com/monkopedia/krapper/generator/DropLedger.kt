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

/**
 * The phase of the generation pipeline a drop happened in. PARSE drops are the
 * cursor-level skip-not-crash decisions made while building the [WrappedElement]
 * model (no [ResolveContext] exists yet); RESOLVE drops are made while resolving a
 * built element against the type graph; DEDUP drops are the post-resolve model
 * cleanups (e.g. const-overload de-duplication).
 */
enum class DropPhase {
    PARSE,
    RESOLVE,
    DEDUP
}

/**
 * A single skip-not-crash drop: a symbol the generator could not model and chose to
 * skip rather than crash on. [symbol] is the best identity available at the drop site
 * (an element's `toString()`, a type spelling, a cursor name); [reason] is the
 * human-readable cause; [phase] is which pass dropped it.
 */
data class DropRecord(val symbol: String, val reason: String, val phase: DropPhase)

/**
 * Process-scoped collector for skip-not-crash binding drops.
 *
 * The breadth strategy drops unmodelable methods/fields/classes with only a log line.
 * Each known drop site funnels its `{symbol, reason}` here so a run can report exactly
 * what it dropped and why — turning "the binding isn't there" from an unanswerable
 * question ("did C++ not have it, or did the generator drop it?") into a diffable list.
 *
 * Modeled as an object (matching the sibling [Log] and [GenerationContext] objects):
 * the generator runs one request at a time under `runBlocking`, so a shared
 * collector is the cohesive carrier without threading a ledger handle through every
 * resolve signature. [reset] clears it between runs (tests and the service path both
 * call it before a fresh generation).
 */
object DropLedger {
    private val records = mutableListOf<DropRecord>()

    /** The drops recorded so far, in the order they occurred. */
    val drops: List<DropRecord>
        get() = records

    /** Record a skip-not-crash drop of [symbol] for [reason] in [phase]. */
    fun record(symbol: String, reason: String, phase: DropPhase) {
        records.add(DropRecord(symbol, reason, phase))
    }

    /** Clear all recorded drops. Call before each fresh generation run. */
    fun reset() {
        records.clear()
    }

    /**
     * Render the end-of-run drop report: a `requested R / bound B / dropped D` summary
     * line followed by the dropped symbols and their reasons. [requested] is the
     * allowlist/instantiation the run was asked for (empty when the run was an
     * unrestricted DefaultFilter import); [bound] is the set of binding identities that
     * actually resolved. When an allowlist was supplied, any requested entry that is
     * neither bound nor explicitly dropped is reported as MISSING (coverage gap).
     */
    fun report(requested: Collection<String>, bound: Collection<String>): String = buildString {
        val boundSet = bound.toSet()
        appendLine(
            "Drop ledger: requested ${requested.size} / bound ${boundSet.size} / " +
                "dropped ${records.size}"
        )
        if (records.isNotEmpty()) {
            appendLine("  Dropped (${records.size}):")
            for (record in records) {
                appendLine("    [${record.phase}] ${record.symbol} — ${record.reason}")
            }
        }
        if (requested.isNotEmpty()) {
            val droppedSymbols = records.map { it.symbol }.toSet()
            val uncovered = requested.filter { it !in boundSet && it !in droppedSymbols }
            if (uncovered.isNotEmpty()) {
                appendLine("  Requested but NOT bound and NOT dropped (${uncovered.size}):")
                for (entry in uncovered) {
                    appendLine("    $entry")
                }
            }
        }
    }.trimEnd('\n')

    /** True when at least one symbol was dropped (used by the opt-in --fail-on-drop gate). */
    fun hasDrops(): Boolean = records.isNotEmpty()
}
