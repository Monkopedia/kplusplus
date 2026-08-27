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

import com.monkopedia.krapper.Diagnostic
import com.monkopedia.krapper.DiagnosticPhase
import com.monkopedia.krapper.Severity
import com.monkopedia.krapper.SourceLocation

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
    DEDUP;

    /** The protocol-level phase this drop is reported as over the service channel. */
    val diagnosticPhase: DiagnosticPhase
        get() = when (this) {
            PARSE -> DiagnosticPhase.PARSE
            RESOLVE -> DiagnosticPhase.RESOLVE
            DEDUP -> DiagnosticPhase.DEDUP
        }
}

/**
 * A single skip-not-crash drop: a symbol the generator could not model and chose to
 * skip rather than crash on. [symbol] is the best identity available at the drop site
 * (an element's `toString()`, a type spelling, a cursor name); [reason] is the
 * human-readable cause; [phase] is which pass dropped it.
 *
 * [severity] is the diagnostic level this drop is reported at (#196). Almost every drop
 * is [Severity.WARNING]: a binding the consumer might have expected is missing, and the
 * reason is the actionable "why". A drop site may instead pass [Severity.INFO] when the
 * drop is a STRUCTURALLY-EXPECTED consequence of the C++ shape itself — nothing a human
 * could act on, because there is no alternative binding to produce (e.g. a non-const
 * overload that is redundant with its const sibling, or a container whose element type
 * is proven to lack `operator==`). Getting this wrong in the INFO direction is worse
 * than the noise it fixes (a real gap goes quiet), so INFO is opt-in per call site, not
 * a default any drop can fall into by omission.
 */
data class DropRecord(
    val symbol: String,
    val reason: String,
    val phase: DropPhase,
    /** Where the dropped declaration lives in C++, when the model carried a location. */
    val location: SourceLocation? = null,
    val severity: Severity = Severity.WARNING
)

/**
 * Run-scoped collector for skip-not-crash binding drops.
 *
 * The breadth strategy drops unmodelable methods/fields/classes with only a log line.
 * Each known drop site funnels its `{symbol, reason}` here so a run can report exactly
 * what it dropped and why — turning "the binding isn't there" from an unanswerable
 * question ("did C++ not have it, or did the generator drop it?") into a diffable list.
 *
 * **One ledger per run, not per process (brick B4, docs/design/live-service.md §1.4).**
 * This was an `object` with a `reset()` the service called before each generation, which
 * meant a second run could only start clean by erasing the first one's drops — fine while
 * krapper was one run per process, wrong the moment a process serves successive builds
 * (brick B5). The ledger now belongs to the [KrapperRun] in scope; drop sites reach it
 * through [dropLedger] rather than threading a handle through every resolve signature.
 */
class DropLedger {
    private val records = mutableListOf<DropRecord>()

    /** The drops recorded so far, in the order they occurred. */
    val drops: List<DropRecord>
        get() = records

    /**
     * Record a skip-not-crash drop of [symbol] for [reason] in [phase].
     *
     * [severity] defaults to [Severity.WARNING] — a binding is missing and [reason] is
     * why. Pass [Severity.INFO] only at a call site that drops a symbol for a
     * structurally-expected reason with nothing to act on (#196); see [DropRecord].
     */
    fun record(
        symbol: String,
        reason: String,
        phase: DropPhase,
        location: SourceLocation? = null,
        severity: Severity = Severity.WARNING
    ) {
        records.add(DropRecord(symbol, reason, phase, location, severity))
    }

    /**
     * The ledger as structured [Diagnostic]s for the service channel (#185).
     *
     * A drop is a WARNING by default: generation completed, but a binding the consumer might
     * have expected is not there. A [DropRecord.severity] of INFO (#196) marks a drop that is
     * structurally expected by the C++ shape itself rather than a modeling gap — it still
     * reports (nothing is hidden: it stays in the full list and the per-severity summary
     * tally, and logs at `--info`), it just does not compete with actionable drops for the
     * capped WARNING budget the build surfaces by default. The reason is the ledger's
     * verbatim text; the hint translates it into what the CONSUMER sees in Kotlin.
     */
    fun diagnostics(): List<Diagnostic> = records.map { record ->
        Diagnostic(
            severity = record.severity,
            message = record.reason,
            symbol = record.symbol,
            location = record.location,
            phase = record.phase.diagnosticPhase,
            hint = "no Kotlin binding is generated for this symbol"
        )
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
                val at = record.location?.let { " ($it)" } ?: ""
                appendLine("    [${record.phase}] ${record.symbol}$at — ${record.reason}")
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
