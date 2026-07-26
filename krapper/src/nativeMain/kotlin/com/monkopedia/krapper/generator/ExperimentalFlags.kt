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
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * A single experimental / diagnostic toggle for the krapper tool.
 *
 * krapper is effectively a compiler, so it accretes experiments (new resolve strategies,
 * caches, alternate codegen) and diagnostics (timing, dumps) we want gated behind a switch
 * rather than shipped on-by-default. Historically each of those grew its own ad-hoc mechanism
 * (an env var here, a `-P` gradle property there, a one-off CLI flag). This gives them ONE
 * home: a flag is DECLARED once in [ExperimentalFlags] (name + type + default + description),
 * parsed ONCE at startup from the environment and the CLI, and read through a typed accessor.
 *
 * A flag's [name] is namespaced with a dotted prefix by area, e.g. `diag.timing`,
 * `forcing.crossRequestMemo`. The [default] is what every read returns when the flag is unset,
 * so an untouched tool run is byte-for-byte identical to one with no flag machinery at all.
 */
sealed class ExperimentalFlag<T>(val name: String, val default: T, val description: String) {
    /** Parse a raw string [value] (from `name=value`) into [T]; null = malformed, keep default. */
    abstract fun parse(value: String): T?

    class BoolFlag(name: String, default: Boolean, description: String) :
        ExperimentalFlag<Boolean>(name, default, description) {
        // A bare `name` (no `=value`) means "turn it on"; parse() only runs for an explicit
        // `name=value`, where we accept the usual truthy/falsy spellings.
        override fun parse(value: String): Boolean? = when (value.trim().lowercase()) {
            "", "true", "1", "on", "yes" -> true
            "false", "0", "off", "no" -> false
            else -> null
        }
    }

    class StringFlag(name: String, default: String, description: String) :
        ExperimentalFlag<String>(name, default, description) {
        override fun parse(value: String): String = value
    }

    class IntFlag(name: String, default: Int, description: String) :
        ExperimentalFlag<Int>(name, default, description) {
        override fun parse(value: String): Int? = value.trim().toIntOrNull()
    }
}

/**
 * The central registry: the single source of truth for every experimental / diagnostic flag.
 *
 * Declaring a new flag is a one-liner here — add an [ExperimentalFlag] to [all] and read it via
 * [ExperimentalFlags.get] (or the flag's own convenience accessor). Values are resolved ONCE by
 * [ExperimentalFlags.init] at tool startup with the precedence:
 *
 *     CLI `-X name[=value]`  overrides  env `KRAPPER_X`  overrides  the declared default.
 *
 * Off-by-default and inert: before [init] runs, and for any flag left unset, [get] returns the
 * declared [ExperimentalFlag.default], so the tool behaves and emits byte-identically to a build
 * with no flag machinery. Unknown flag names are WARNed to stderr (typo visibility), never fatal.
 */
object ExperimentalFlags {
    /** Env var holding a comma-separated `name` / `name=value` list. */
    const val ENV_VAR = "KRAPPER_X"

    // ---- Declared flags (the single source of truth). ----

    // Emit per-phase wall-clock timings (and bound-class counts) to stderr. The first consumer
    // of the flag system: gates Diag.timed. Feeds the upcoming cross-request memo experiment.
    val DIAG_TIMING = ExperimentalFlag.BoolFlag(
        "diag.timing",
        default = false,
        description = "Log per-phase wall-clock timings (ms) + counts to stderr."
    )

    // Profile the BASE resolve (filterAndResolve / resolveAll — the pass that runs BEFORE any
    // forcing). Emits STREAMING progress (cumulative classes resolved, elapsed, classes/sec) so a
    // run that times out still yields a profile, plus a per-type resolve-time distribution: a
    // running top-N of the slowest types and the full-tree-walk (`filterRecursive`) counters that
    // reveal whether the cost is uniform-breadth O(n) or a superlinear O(n²) tree-walk hotspot. Off
    // by default and byte-identical when off (measurement only — never changes what gets bound).
    val DIAG_BASE_BIND_TIMING = ExperimentalFlag.BoolFlag(
        "diag.baseBindTiming",
        default = false,
        description = "Profile the base resolve: streaming classes/sec progress + per-type " +
            "slowest-N distribution + tree-walk counters (O(n) vs O(n²) diagnosis)."
    )

    /** Every declared flag. Add new flags here. */
    val all: List<ExperimentalFlag<*>> = listOf(
        DIAG_TIMING,
        DIAG_BASE_BIND_TIMING
    )

    private val byName: Map<String, ExperimentalFlag<*>> = all.associateBy { it.name }

    // Resolved raw string values keyed by flag name; only NON-default entries live here, so an
    // empty map == everything at its default == fully inert. Replaced wholesale by init().
    private var resolved: Map<String, String> = emptyMap()

    /**
     * Parse [env] (the `KRAPPER_X` string) and [cli] (the repeatable `-X` option values), apply
     * precedence (CLI over env over default), warn on unknown names, and install the result.
     * Idempotent; safe to call once at startup. Env-independent core is [parse] for testability.
     */
    fun init(env: String? = getenv(ENV_VAR)?.toKString(), cli: List<String> = emptyList()) {
        val (values, warnings) = parse(env, cli)
        resolved = values
        for (w in warnings) {
            printerrln("kplusplus warn: $w")
        }
        logActive()
    }

    /**
     * Pure resolver (no I/O): returns the resolved NON-default values plus a list of warnings for
     * unknown / malformed entries. CLI entries are applied after env entries, so they win.
     */
    fun parse(env: String?, cli: List<String>): Pair<Map<String, String>, List<String>> {
        val values = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()
        fun apply(entry: String, source: String) {
            val trimmed = entry.trim()
            if (trimmed.isEmpty()) return
            val eq = trimmed.indexOf('=')
            val name = (if (eq < 0) trimmed else trimmed.substring(0, eq)).trim()
            val rawValue = if (eq < 0) "" else trimmed.substring(eq + 1)
            val flag = byName[name]
            if (flag == null) {
                warnings += "unknown experimental flag '$name' (from $source); ignoring. " +
                    "Known flags: ${byName.keys.sorted().joinToString(", ")}"
                return
            }
            if (flag.parse(rawValue) == null) {
                warnings += "malformed value '$rawValue' for experimental flag '$name' " +
                    "(from $source); ignoring."
                return
            }
            // Store the canonical raw string; typed reads re-parse through the flag.
            values[name] = rawValue
        }
        env?.split(',')?.forEach { apply(it, "$ENV_VAR env") }
        cli.forEach { apply(it, "-X") }
        return values to warnings
    }

    /** Typed read: the resolved value if set (and valid), else the declared default. */
    fun <T> get(flag: ExperimentalFlag<T>): T {
        val raw = resolved[flag.name] ?: return flag.default
        return (flag.parse(raw) ?: flag.default)
    }

    /** Convenience for the common boolean case. */
    fun isEnabled(flag: ExperimentalFlag.BoolFlag): Boolean = get(flag)

    /** Human-readable list of the active (non-default) flags, for the startup log. */
    fun activeSummary(): String = activeFlags().joinToString(", ") { "${it.name}=${get(it)}" }

    private fun activeFlags(): List<ExperimentalFlag<*>> = all.filter { it.name in resolved }

    /** Full listing of every declared flag with default + description (for --list-experimental). */
    fun listing(): String = buildString {
        appendLine("Experimental / diagnostic flags (set via $ENV_VAR env or repeatable -X):")
        for (flag in all.sortedBy { it.name }) {
            val type = when (flag) {
                is ExperimentalFlag.BoolFlag -> "bool"
                is ExperimentalFlag.StringFlag -> "string"
                is ExperimentalFlag.IntFlag -> "int"
            }
            appendLine("  ${flag.name} [$type, default=${flag.default}] — ${flag.description}")
        }
    }

    private fun logActive() {
        if (resolved.isEmpty()) return
        printerrln("kplusplus: active experimental flags: ${activeSummary()}")
    }

    /** Test-only reset so unit tests don't leak resolved state into each other. */
    internal fun resetForTest(values: Map<String, String> = emptyMap()) {
        resolved = values
    }
}
