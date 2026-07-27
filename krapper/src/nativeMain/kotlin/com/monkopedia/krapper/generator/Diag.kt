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

/**
 * Flag-gated diagnostics. Every helper here is a ZERO-OVERHEAD passthrough unless the
 * `diag.timing` experimental flag is on (see [ExperimentalFlags.DIAG_TIMING]): with the flag
 * off, [timed] just runs its block and [line] does nothing, so an ordinary run is byte-identical
 * to one with no diagnostics wired in at all.
 *
 * When on, measurements use [TimeSource.Monotonic] (wall-clock, available in Kotlin/Native — the
 * tool is a real native binary) and are logged to stderr, keeping stdout — the machine-readable
 * generated-output channel — untouched.
 */
object Diag {
    val timingEnabled: Boolean get() = ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING)

    /**
     * Run [block], and — only when `diag.timing` is on — log `<phase>: <ms> ms` to stderr with
     * the elapsed wall-clock. Returns the block's result. Off-flag path adds no measurement.
     */
    inline fun <T> timed(phase: String, block: () -> T): T {
        if (!timingEnabled) return block()
        val mark = TimeSource.Monotonic.markNow()
        val result = block()
        line("$phase: ${mark.elapsedNow().inWholeMicroseconds / 1000.0} ms")
        return result
    }

    /** Emit a diagnostic [message] to stderr only when `diag.timing` is on. */
    fun line(message: String) {
        if (timingEnabled) printerrln("diag: $message")
    }
}
