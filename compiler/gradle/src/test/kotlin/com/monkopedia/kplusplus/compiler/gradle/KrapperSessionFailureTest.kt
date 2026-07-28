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
package com.monkopedia.kplusplus.compiler.gradle

import com.monkopedia.krapper.ErrorPolicy
import com.monkopedia.krapper.IndexRequest
import com.monkopedia.krapper.KrapperConfig
import com.monkopedia.krapper.ReferencePolicy
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import org.gradle.api.logging.Logging

/**
 * The subprocess lifecycle, fault-injected (#185 review follow-up).
 *
 * The pre-#185 path was `ProcessBuilder(...).inheritIO()` + `waitFor()`, so however badly a run
 * went it always reported `exit N`. Driving krapper over a channel puts that at risk: ksrpc
 * reports every teardown with no application exception in flight as a bare
 * `CancellationException("Closing MultiChannel")`, which says nothing about whether the tool
 * crashed, spoke gibberish, or was never a tool at all. These tests stand in a script for the
 * krapper binary and pin the message the build actually gets.
 *
 * Deterministic by construction: each stand-in exits immediately on a fixed path, so nothing
 * here depends on timing. (A tool that hangs forever is deliberately NOT covered — the old
 * `waitFor()` had the same unbounded wait, so it is not a regression, and a test for it could
 * only be a timeout race.)
 */
class KrapperSessionFailureTest {

    private val logger = Logging.getLogger(KrapperSessionFailureTest::class.java)
    private val scratch = File.createTempFile("krapper-session-test", "").let {
        it.delete()
        it.mkdirs()
        it
    }

    @AfterTest
    fun cleanUp() {
        scratch.deleteRecursively()
    }

    @Test
    fun aToolThatDiesBeforeAnyRoundTripReportsItsExitCode() {
        // The failure class the old exit-code path handled reliably: a corrupt/incompatible
        // binary, an ABI mismatch, a segfault — anything that kills krapper before it talks.
        val failure = runAgainst(stubTool("exit 134"))

        assertContains(failure.message.orEmpty(), "134")
        assertContains(failure.message.orEmpty(), "exited with code")
    }

    @Test
    fun aToolThatSpeaksGibberishIsNotReportedAsACrash() {
        // A clean exit with a malformed protocol stream must NOT look like the crash above —
        // conflating the two is exactly what the raw channel error did.
        val failure = runAgainst(stubTool("printf 'not a ksrpc frame at all'", "exit 0"))

        val message = failure.message.orEmpty()
        assertContains(message, "protocol stream ended unexpectedly")
    }

    @Test
    fun aToolThatCannotBeStartedSaysSoRatherThanFailingOnTheChannel() {
        val failure = assertFailsWith<KrapperRunException> {
            KrapperSession.run(File(scratch, "not-a-real-krapper"), job(), logger)
        }

        assertContains(failure.message.orEmpty(), "could not be started")
    }

    /** Run a job against [tool] and return the failure it produced. */
    private fun runAgainst(tool: File): KrapperRunException =
        assertFailsWith<KrapperRunException> { KrapperSession.run(tool, job(), logger) }

    /** An executable stand-in for the krapper binary that runs [lines] and nothing else. */
    private fun stubTool(vararg lines: String): File =
        File(scratch, "krapper-stub-${lines.hashCode()}.sh").apply {
            writeText(lines.joinToString("\n", prefix = "#!/bin/sh\n", postfix = "\n"))
            check(setExecutable(true)) { "could not make the stub tool executable: $this" }
        }

    /**
     * The smallest job that reaches the channel: the session sets the logger and the config
     * before anything else, so a tool that is already dead fails there, ahead of any parse.
     */
    private fun job() = KrapperJob(
        moduleName = "stub",
        config = KrapperConfig(
            pkg = "krapper.stub",
            compiler = "clang++",
            moduleName = "stub",
            errorPolicy = ErrorPolicy.LOG,
            referencePolicy = ReferencePolicy.INCLUDE_MISSING,
            debug = false
        ),
        request = IndexRequest(headers = emptyList(), libraries = emptyList()),
        allowList = emptyList(),
        instantiations = emptyList(),
        fixups = emptyList(),
        outputDir = File(scratch, "out")
    )
}
