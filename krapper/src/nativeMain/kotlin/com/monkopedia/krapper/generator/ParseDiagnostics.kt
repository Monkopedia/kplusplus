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

import com.monkopedia.krapper.SourceLocation

/**
 * One `error:`-severity diagnostic Clang emitted while parsing a translation unit (#224).
 *
 * Decoded from the `kppbridge::lastParseDiagnostics` wire format (see clang_slice.h): the
 * parse installs a [clang DiagnosticConsumer][kppbridge] of its own instead of letting
 * clang::tooling print to stderr and forget, so the generator can see what the parse lost.
 *
 * @param fatal the diagnostic was `Fatal` severity — clang stopped, and everything after the
 *   diagnostic's position was never parsed at all (a missing `#include`, "too many errors")
 * @param location the presumed `file:line:col` the diagnostic was reported against, or null
 *   when it has none (a command-line-level or otherwise unattributable error)
 * @param message clang's own diagnostic text, verbatim
 */
internal data class ParseDiagnostic(
    val fatal: Boolean,
    val location: SourceLocation?,
    val message: String
) {
    /**
     * True when this error can be pinned to ONE source position, so the damage is bounded:
     * clang recovered, skipped the declaration it was reading, and carried on parsing the
     * rest of the header. That is exactly the case the lenient default handles by dropping
     * that position into the drop ledger and continuing.
     *
     * A [fatal] diagnostic is never attributable no matter how good its position, because
     * the position is where clang STOPPED, not what it skipped: the rest of the translation
     * unit is missing from the recovered AST, so binding "the rest" would silently emit a
     * fraction of the header.
     */
    val attributable: Boolean
        get() = !fatal && location != null

    /** `file:line:col: error: text`, the shape compilers print. */
    fun render(): String = buildString {
        append(location?.toString() ?: "<no source position>").append(": ")
        append(if (fatal) "fatal error" else "error").append(": ")
        append(message)
    }
}

/**
 * Decode the `kppbridge::lastParseDiagnostics` wire format: one record per line, three
 * tab-separated fields — `severity`, `file:line:col` (possibly empty), `message`. Blank
 * lines are ignored, so a clean parse (which returns an empty string) decodes to an empty
 * list.
 *
 * Anything that isn't the documented shape decodes conservatively rather than being dropped:
 * an unrecognised severity is a non-fatal error, an unparseable position is "no position"
 * (which makes it unattributable, i.e. treated as unrecoverable) and a missing message field
 * is reported as such. A diagnostic silently swallowed by a strict decode would be the exact
 * defect this gate exists to prevent.
 */
internal fun decodeParseDiagnostics(raw: String): List<ParseDiagnostic> =
    raw.lineSequence().filter { it.isNotBlank() }.map { line ->
        val fields = line.split('\t', limit = 3)
        ParseDiagnostic(
            fatal = fields[0] == "fatal",
            location = fields.getOrNull(1)?.let(SourceLocation.Companion::parse),
            message = fields.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "(no diagnostic text)"
        )
    }.toList()

/**
 * THE `--strict-diagnostics` GATE (#224). Apply the parse-error policy for [source]'s parse
 * to [diagnostics], recording what was lost in [ledger] and throwing when the run must not
 * continue.
 *
 * Three outcomes, and the flag only moves the first:
 *
 *  * an **attributable** error (non-fatal, with a source position) is a bounded loss — clang
 *    skipped that declaration and parsed the rest of the header. Lenient (the default) drops
 *    it into the drop ledger as a PARSE-phase drop and continues, so one un-parseable
 *    declaration in a real-world library no longer takes the whole import down; the drop is
 *    reported in the ledger, streamed as a structured `Diagnostic`, and makes the opt-in
 *    `--fail-on-drop` gate fire. With [strict] on it aborts instead.
 *  * a **fatal** diagnostic (a missing `#include`, "too many errors") aborts regardless of
 *    [strict]: clang stopped parsing there, so everything after it is absent from the
 *    recovered AST and binding it would emit a silent fraction of the header.
 *  * an **unattributable** error (no source position at all) aborts regardless of [strict]
 *    for the same reason: nothing identifies what was lost, so it cannot be ledgered as a
 *    bounded drop.
 *
 * Every collected error is ledgered before any decision, so the drop report and the streamed
 * diagnostics carry what the parse lost whether or not the run goes on to abort.
 */
internal suspend fun applyParseDiagnostics(
    source: String,
    diagnostics: List<ParseDiagnostic>,
    strict: Boolean,
    ledger: DropLedger
) {
    if (diagnostics.isEmpty()) return
    for (diagnostic in diagnostics) {
        ledger.record(
            symbol = diagnostic.location?.toString() ?: "$source (translation unit)",
            reason = "parse error: ${diagnostic.message}",
            phase = DropPhase.PARSE,
            location = diagnostic.location
        )
    }
    val unrecoverable = diagnostics.filterNot { it.attributable }
    if (unrecoverable.isNotEmpty()) {
        error(
            "krapper: $source did not parse — ${unrecoverable.size} unrecoverable " +
                "diagnostic(s) (fatal, or with no attributable source position). Clang " +
                "stopped there, so the rest of the translation unit is absent from the " +
                "recovered AST and binding it would emit a silent fraction of the header:" +
                unrecoverable.joinToString("") { "\n  ${it.render()}" }
        )
    }
    if (strict) {
        error(
            "--strict-diagnostics: ${diagnostics.size} parse error(s) in $source. Without " +
                "the flag each of these drops its declaration into the drop ledger and " +
                "binding continues:" + diagnostics.joinToString("") { "\n  ${it.render()}" }
        )
    }
    Log.w(
        "cpp front-end: $source parsed with ${diagnostics.size} recoverable error(s); the " +
            "affected declaration(s) are dropped and ledgered (pass --strict-diagnostics to " +
            "abort instead):" + diagnostics.joinToString("") { "\n  ${it.render()}" }
    )
}
