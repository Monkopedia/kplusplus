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
package com.monkopedia.krapper

import kotlinx.serialization.Serializable

/** How bad a [Diagnostic] is; maps onto the build's error/warn/info logging levels. */
enum class Severity {
    /** Informational — reported, never fails a build. */
    INFO,

    /** A binding was degraded or dropped, but generation completed. */
    WARNING,

    /** Generation could not produce what was asked for. */
    ERROR
}

/** Which pass of the pipeline produced a [Diagnostic]. */
enum class DiagnosticPhase {
    /** Building the model from the Clang AST. */
    PARSE,

    /** Resolving a built element against the type graph. */
    RESOLVE,

    /** Post-resolve model cleanup (e.g. const-overload de-duplication). */
    DEDUP,

    /** Emitting the Kotlin bindings / C++ wrapper. */
    GENERATE,

    /** Compiling the generated C++ wrapper. */
    COMPILE
}

/**
 * ONE structured thing that went wrong (or was silently degraded) during a generation run
 * (#185).
 *
 * Before this existed the Gradle plugin learned exactly one fact about a krapper run — its
 * exit code — and everything krapper knew (which decl, where in C++, why it was dropped) was
 * flattened into interleaved subprocess stdout. A `Diagnostic` is that knowledge kept
 * structured all the way to the build, where it can be logged at the right level, attributed
 * to a real source position, and turned into an actionable failure message.
 *
 * @param severity how bad it is
 * @param message what happened, in prose (for a drop, the drop-ledger reason verbatim)
 * @param symbol the C++ declaration it happened to, as spelled by the model
 * @param location where that declaration (or the failing compile) is in C++
 * @param phase which pass produced it
 * @param hint what it means for the CONSUMER's Kotlin — the "so what", when we can say
 */
@Serializable
data class Diagnostic(
    val severity: Severity,
    val message: String,
    val symbol: String? = null,
    val location: SourceLocation? = null,
    val phase: DiagnosticPhase? = null,
    val hint: String? = null
) {
    /** One-line rendering, in the `file:line:col: severity: text` shape compilers use. */
    fun render(): String = buildString {
        location?.let { append(it).append(": ") }
        append(severity.name.lowercase()).append(": ")
        phase?.let { append('[').append(it.name).append("] ") }
        symbol?.let { append(it).append(" — ") }
        append(message)
        hint?.let { append(" (").append(it).append(')') }
    }
}
