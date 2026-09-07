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
import com.monkopedia.krapper.parser.CppParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * THE `--strict-diagnostics` GATE (#224).
 *
 * The flag shipped with an eight-line help text and no reader at all: setting it changed
 * nothing, because clang::tooling recovers from a parse error, hands back an AST missing
 * whatever it could not read, and nothing looked at the diagnostics. These tests pin the gate
 * in BOTH directions — the only shape of test that can distinguish an implemented gate from
 * the one that was there before, which reported success while doing nothing.
 *
 * Two layers, on purpose:
 *  * [real_clang_attributable_error_is_recoverable] / [real_clang_missing_include_is_fatal]
 *    run REAL clang through the `kppbridge::lastParseDiagnostics` bridge, so the C++ half
 *    (installing the DiagnosticConsumer, the wire format, the severity mapping) is exercised
 *    against the actual front-end rather than a hand-written string; and
 *  * the policy tests drive [applyParseDiagnostics] directly, so each of the four
 *    (strict x recoverable) cells is asserted independently of what clang happens to emit.
 */
class ParseDiagnosticsTest {

    private fun attributable(line: Int = 2) = ParseDiagnostic(
        fatal = false,
        location = SourceLocation("header.h", line, 12),
        message = "expected class name"
    )

    private val fatal = ParseDiagnostic(
        fatal = true,
        location = SourceLocation("header.h", 1, 10),
        message = "'nope.h' file not found"
    )

    private val unattributable = ParseDiagnostic(
        fatal = false,
        location = null,
        message = "invalid value for -std"
    )

    // ---- the wire format ----------------------------------------------------------------

    @Test
    fun decode_reads_severity_position_and_message() {
        val decoded = decodeParseDiagnostics(
            "error\tinput.cc:2:12\texpected class name\n" +
                "fatal\tinput.cc:1:10\t'nope.h' file not found\n"
        )
        assertEquals(2, decoded.size, "both records must decode: $decoded")
        assertEquals(
            ParseDiagnostic(false, SourceLocation("input.cc", 2, 12), "expected class name"),
            decoded[0]
        )
        assertEquals(
            ParseDiagnostic(true, SourceLocation("input.cc", 1, 10), "'nope.h' file not found"),
            decoded[1]
        )
        assertTrue(decoded[0].attributable, "a located non-fatal error is attributable")
        assertFalse(decoded[1].attributable, "a fatal error is never attributable")
    }

    @Test
    fun decode_of_a_clean_parse_is_empty() {
        assertEquals(emptyList(), decodeParseDiagnostics(""), "a clean parse decodes to nothing")
        assertEquals(emptyList(), decodeParseDiagnostics("\n\n"), "blank lines are not records")
    }

    // An error with no position must survive the decode as an UNATTRIBUTABLE record rather
    // than being dropped: a diagnostic quietly swallowed by the decode is the exact defect
    // this gate exists to catch, one layer down.
    @Test
    fun decode_keeps_an_error_with_no_position_as_unattributable() {
        val decoded = decodeParseDiagnostics("error\t\tinvalid value for -std\n")
        assertEquals(1, decoded.size, "the record must survive: $decoded")
        assertEquals(null, decoded[0].location, "an empty position field means no position")
        assertFalse(decoded[0].attributable, "no position means nothing can be attributed")
    }

    // ---- the policy, all four (strict x recoverable) cells --------------------------------

    @Test
    fun lenient_drops_an_attributable_error_and_continues() = runBlocking {
        val ledger = DropLedger()
        applyParseDiagnostics("header.h", listOf(attributable()), strict = false, ledger = ledger)
        assertEquals(1, ledger.drops.size, "the error must be ledgered: ${ledger.drops}")
        assertEquals(DropPhase.PARSE, ledger.drops[0].phase, "a parse error is a PARSE drop")
        assertEquals(SourceLocation("header.h", 2, 12), ledger.drops[0].location)
        assertTrue(
            "expected class name" in ledger.drops[0].reason,
            "the drop must carry clang's own text: ${ledger.drops[0].reason}"
        )
        // The whole point of ledgering it: --fail-on-drop can now catch a degraded parse.
        assertTrue(ledger.hasDrops(), "the drop must make --fail-on-drop's gate fire")
    }

    @Test
    fun strict_aborts_on_an_attributable_error() {
        val ledger = DropLedger()
        val failure = assertFailsWith<IllegalStateException> {
            runBlocking {
                applyParseDiagnostics(
                    "header.h",
                    listOf(attributable()),
                    strict = true,
                    ledger = ledger
                )
            }
        }
        assertTrue(
            "--strict-diagnostics" in failure.message.orEmpty(),
            "the abort must name the flag that caused it: ${failure.message}"
        )
        assertTrue(
            "expected class name" in failure.message.orEmpty(),
            "the abort must report what failed to parse: ${failure.message}"
        )
    }

    @Test
    fun a_fatal_diagnostic_aborts_even_when_lenient() {
        val failure = assertFailsWith<IllegalStateException> {
            runBlocking {
                applyParseDiagnostics("header.h", listOf(fatal), false, DropLedger())
            }
        }
        assertTrue(
            "unrecoverable" in failure.message.orEmpty(),
            "a fatal diagnostic aborts regardless of the flag: ${failure.message}"
        )
    }

    @Test
    fun an_unattributable_error_aborts_even_when_lenient() {
        val failure = assertFailsWith<IllegalStateException> {
            runBlocking {
                applyParseDiagnostics("header.h", listOf(unattributable), false, DropLedger())
            }
        }
        assertTrue(
            "unrecoverable" in failure.message.orEmpty(),
            "an error with no attributable position aborts too: ${failure.message}"
        )
    }

    // The gate does not false-positive: a clean parse neither aborts nor ledgers, with the
    // flag ON as well as off. Without this a gate that always fired would pass every test
    // above.
    @Test
    fun a_clean_parse_neither_aborts_nor_drops_in_either_mode() = runBlocking {
        for (strict in listOf(false, true)) {
            val ledger = DropLedger()
            applyParseDiagnostics("header.h", emptyList(), strict, ledger)
            assertFalse(ledger.hasDrops(), "a clean parse must drop nothing (strict=$strict)")
        }
    }

    // ---- the real front-end, through the bridge -------------------------------------------

    // A recoverable error: clang skips `struct B` and parses `struct A`/`struct C` around it,
    // so the diagnostic is a non-fatal, LOCATED one — the case the lenient default keeps
    // binding through.
    @Test
    fun real_clang_attributable_error_is_recoverable() {
        CppParser.parse(
            "struct A { int x; };\nstruct B : NotAThing { int y; };\nstruct C { int z; };\n",
            "diag_attributable.cc",
            CppParser.driverArgs(std = "c++17", includeDirs = emptyList())
        )
        val diagnostics = CppParser.lastDiagnostics()
        assertEquals(1, diagnostics.size, "clang must report exactly one error: $diagnostics")
        assertTrue(
            diagnostics[0].attributable,
            "a recoverable error must be attributable: ${diagnostics[0].render()}"
        )
        assertEquals(
            2,
            diagnostics[0].location?.line,
            "it must point at the offending line: ${diagnostics[0].render()}"
        )
    }

    // A missing include is FATAL: clang stops there, so everything after it is absent from
    // the recovered AST and the run must abort whatever the flag says.
    @Test
    fun real_clang_missing_include_is_fatal() {
        CppParser.parse(
            "#include \"definitely_not_on_this_box.h\"\nstruct A { int x; };\n",
            "diag_fatal.cc",
            CppParser.driverArgs(std = "c++17", includeDirs = emptyList())
        )
        val diagnostics = CppParser.lastDiagnostics()
        assertEquals(1, diagnostics.size, "clang must report the missing include: $diagnostics")
        assertTrue(diagnostics[0].fatal, "a missing include is fatal: ${diagnostics[0].render()}")
        assertFalse(
            diagnostics[0].attributable,
            "a fatal diagnostic is never a bounded drop: ${diagnostics[0].render()}"
        )
    }

    // The collector is CLEARED per parse: a clean parse after a failing one must report
    // nothing. Without the clear, every later parse in a run would inherit the first bad
    // header's errors and --strict-diagnostics would abort on a header that parsed fine.
    @Test
    fun real_clang_diagnostics_do_not_leak_between_parses() {
        val args = CppParser.driverArgs(std = "c++17", includeDirs = emptyList())
        CppParser.parse("struct B : NotAThing { int y; };\n", "diag_dirty.cc", args)
        assertTrue(
            CppParser.lastDiagnostics().isNotEmpty(),
            "the dirty parse must report an error (control for the assertion below)"
        )
        CppParser.parse("struct A { int x; };\n", "diag_clean.cc", args)
        assertEquals(
            emptyList(),
            CppParser.lastDiagnostics(),
            "a clean parse must not inherit the previous parse's diagnostics"
        )
    }
}
