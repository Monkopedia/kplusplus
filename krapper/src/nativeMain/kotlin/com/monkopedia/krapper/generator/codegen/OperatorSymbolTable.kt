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
package com.monkopedia.krapper.generator.codegen

/**
 * Single source of truth for the C++ operator-symbol set and the DISTINCT token
 * each renderer substitutes for it. Two renderers read this table:
 *
 *  - [String.kotlinMethodName] (KotlinWriter) reads [kotlinToken] — the token that
 *    makes the operator name a valid **Kotlin** identifier fragment.
 *  - [String.cleanupOperatorName] (NameHandler) reads [cOperatorToken] — the token
 *    that makes it a valid, non-colliding **C** identifier fragment.
 *
 * The Kotlin and C tokens intentionally DIFFER (different identifier rules and
 * historical spellings, e.g. `*` -> `_star` in Kotlin but `_Times`-style names come
 * from the [Operator] enum on the C side); a `null` column means that renderer does
 * not rewrite that symbol at all. Keeping the symbol set + ordering in one list means
 * adding or changing an operator happens in ONE place and the two renderers cannot
 * silently drift apart.
 *
 * ORDER IS LOAD-BEARING: entries are applied top-to-bottom by a plain
 * [String.replace] fold, so every multi-character compound MUST precede any single
 * character it contains (e.g. `<<=` before `<<` before `<`, `""` before `"`, `==`
 * before `=`). The list below is sorted longest-first with those pairings preserved,
 * which reproduces each renderer's original per-chain ordering exactly (proven
 * byte-identical by fuzzing the folded table against the original chains).
 *
 * NB: [String.cleanupName] is deliberately NOT driven from this table. It is a
 * GENERAL C-identifier sanitizer (it also rewrites non-operator spellings like `::`,
 * ` `, `@`, type-parameter names) whose ordering has its own semantics — notably it
 * deletes `>` to empty, which can fuse a following `=` into a new `==`. Folding it in
 * here would force an awkward interleave of operator and non-operator rows for no
 * maintainability gain, so it keeps its own chain.
 */
internal data class OperatorSymbol(
    /** The C++ operator symbol to match. */
    val symbol: String,
    /** Replacement producing a valid Kotlin identifier fragment, or null to skip. */
    val kotlinToken: String?,
    /** Replacement producing a valid C identifier fragment, or null to skip. */
    val cOperatorToken: String?
)

internal val OPERATOR_SYMBOLS = listOf(
    OperatorSymbol("<<=", kotlinToken = null, cOperatorToken = "_shleq"),
    OperatorSymbol(">>=", kotlinToken = null, cOperatorToken = "_shreq"),
    OperatorSymbol("<<", kotlinToken = null, cOperatorToken = "_shl"),
    OperatorSymbol(">>", kotlinToken = null, cOperatorToken = "_shr"),
    OperatorSymbol("<=", kotlinToken = null, cOperatorToken = "_le"),
    OperatorSymbol(">=", kotlinToken = null, cOperatorToken = "_ge"),
    OperatorSymbol("!=", kotlinToken = null, cOperatorToken = "_ne"),
    OperatorSymbol("->", kotlinToken = null, cOperatorToken = "_arrow"),
    OperatorSymbol("+=", kotlinToken = null, cOperatorToken = "_pluseq"),
    OperatorSymbol("-=", kotlinToken = null, cOperatorToken = "_minuseq"),
    OperatorSymbol("*=", kotlinToken = null, cOperatorToken = "_timeseq"),
    OperatorSymbol("/=", kotlinToken = null, cOperatorToken = "_diveq"),
    OperatorSymbol("%=", kotlinToken = null, cOperatorToken = "_modeq"),
    OperatorSymbol("&=", kotlinToken = null, cOperatorToken = "_andeq"),
    OperatorSymbol("|=", kotlinToken = null, cOperatorToken = "_oreq"),
    OperatorSymbol("^=", kotlinToken = null, cOperatorToken = "_xoreq"),
    OperatorSymbol("++", kotlinToken = null, cOperatorToken = "_inc"),
    OperatorSymbol("--", kotlinToken = null, cOperatorToken = "_dec"),
    OperatorSymbol("\"\"", kotlinToken = "_qts", cOperatorToken = null),
    OperatorSymbol("==", kotlinToken = "_cmp", cOperatorToken = null),
    OperatorSymbol("()", kotlinToken = "_call", cOperatorToken = null),
    OperatorSymbol("\"", kotlinToken = "_qt", cOperatorToken = null),
    OperatorSymbol("=", kotlinToken = "_eq", cOperatorToken = null),
    OperatorSymbol("+", kotlinToken = "_plus", cOperatorToken = "_plus"),
    OperatorSymbol("-", kotlinToken = "_minus", cOperatorToken = "_minus"),
    OperatorSymbol("/", kotlinToken = "_div", cOperatorToken = "_div"),
    OperatorSymbol("*", kotlinToken = "_star", cOperatorToken = null),
    OperatorSymbol("%", kotlinToken = "_mod", cOperatorToken = "_mod"),
    OperatorSymbol("<", kotlinToken = "_lt", cOperatorToken = "_lt"),
    OperatorSymbol(">", kotlinToken = "_gt", cOperatorToken = "_gt"),
    OperatorSymbol("!", kotlinToken = "_not", cOperatorToken = null),
    OperatorSymbol("&", kotlinToken = "_and", cOperatorToken = null),
    OperatorSymbol("|", kotlinToken = "_or", cOperatorToken = null),
    OperatorSymbol("^", kotlinToken = "_xor", cOperatorToken = null),
    OperatorSymbol("~", kotlinToken = "_inv", cOperatorToken = null),
    OperatorSymbol(",", kotlinToken = null, cOperatorToken = "_comma")
)

/** Fold the selected token column of [OPERATOR_SYMBOLS] over [this], in table order. */
private inline fun String.replaceOperatorSymbols(token: (OperatorSymbol) -> String?): String {
    var result = this
    for (entry in OPERATOR_SYMBOLS) {
        val replacement = token(entry) ?: continue
        result = result.replace(entry.symbol, replacement)
    }
    return result
}

/** Rewrite C++ operator symbols to their Kotlin-identifier tokens (see [OPERATOR_SYMBOLS]). */
internal fun String.replaceKotlinOperatorTokens(): String = replaceOperatorSymbols {
    it.kotlinToken
}

/** Rewrite C++ operator symbols to their C-identifier tokens (see [OPERATOR_SYMBOLS]). */
internal fun String.replaceCOperatorTokens(): String = replaceOperatorSymbols { it.cOperatorToken }
