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

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExperimentalFlagsTest {
    @AfterTest
    fun tearDown() {
        // Never leak resolved flag state into other tests (or other flag tests).
        ExperimentalFlags.resetForTest()
    }

    private fun resolve(env: String?, cli: List<String>): Map<String, String> {
        val (values, _) = ExperimentalFlags.parse(env, cli)
        ExperimentalFlags.resetForTest(values)
        return values
    }

    @Test
    fun defaultIsInertWhenNothingSet() {
        resolve(null, emptyList())
        assertFalse(ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING))
        assertEquals("", ExperimentalFlags.activeSummary())
    }

    @Test
    fun envBareNameTurnsBoolOn() {
        resolve("diag.timing", emptyList())
        assertTrue(ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING))
    }

    @Test
    fun envExplicitFalseKeepsBoolOff() {
        resolve("diag.timing=false", emptyList())
        assertFalse(ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING))
    }

    @Test
    fun envCommaSeparatedListParses() {
        val (_, warnings) = ExperimentalFlags.parse("diag.timing , ", emptyList())
        assertTrue(warnings.isEmpty(), "trailing empty entry should be ignored: $warnings")
        resolve("diag.timing", emptyList())
        assertTrue(ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING))
    }

    @Test
    fun cliOverridesEnv() {
        // env turns it on, CLI turns it back off -> CLI wins.
        resolve("diag.timing", listOf("diag.timing=off"))
        assertFalse(ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING))
    }

    @Test
    fun unknownFlagWarnsButDoesNotCrash() {
        val (values, warnings) = ExperimentalFlags.parse("diag.timeing", emptyList())
        assertTrue(values.isEmpty(), "unknown flag must not resolve to a value")
        assertEquals(1, warnings.size)
        assertTrue(warnings.single().contains("unknown experimental flag 'diag.timeing'"))
    }

    @Test
    fun malformedIntValueWarns() {
        // Use the same warn path via a malformed bool value.
        val (values, warnings) = ExperimentalFlags.parse("diag.timing=maybe", emptyList())
        assertTrue(values.isEmpty())
        assertEquals(1, warnings.size)
        assertTrue(warnings.single().contains("malformed value 'maybe'"))
    }

    @Test
    fun activeSummaryListsOnlyNonDefault() {
        resolve("diag.timing", emptyList())
        assertEquals("diag.timing=true", ExperimentalFlags.activeSummary())
    }

    @Test
    fun listingCoversEveryDeclaredFlag() {
        val listing = ExperimentalFlags.listing()
        for (flag in ExperimentalFlags.all) {
            assertTrue(listing.contains(flag.name), "listing missing ${flag.name}")
        }
    }
}
