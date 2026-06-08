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

import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace

interface Namer {
    val cName: String
    val WrappedMethod.uniqueCName: String
    val WrappedField.uniqueCGetter: String
    val WrappedField.uniqueCSetter: String

    fun uniqify(source: Any, name: String): String
}

class NameHandler {
    object Empty : Namer {
        override val cName: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedMethod.uniqueCName: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedField.uniqueCGetter: String
            get() = throw NotImplementedError("Root naming unavailable")
        override val WrappedField.uniqueCSetter: String
            get() = throw NotImplementedError("Root naming unavailable")

        override fun uniqify(source: Any, name: String): String =
            throw NotImplementedError("Root naming unavailable")
    }

    private val allNames = mutableSetOf<String>()
    private val namerStorage = mutableMapOf<WrappedElement, NamerImpl>()

    private fun uniqueNameFor(name: String): String {
        if (!allNames.add(name)) {
            return uniqueNameFor("_$name")
        }
        return name
    }

    fun namerFor(wrappedClass: WrappedClass): Namer = namerStorage.getOrPut(wrappedClass) {
        NamerImpl(
            cName = wrappedClass.type.toString().cleanupName()
        )
    }

    fun namerFor(wrappedClass: WrappedNamespace): Namer = namerStorage.getOrPut(wrappedClass) {
        NamerImpl(
            cName = wrappedClass.fullyQualified.cleanupName()
        )
    }

    private inner class NamerImpl(override val cName: String) : Namer {
        private val nameLookup = mutableMapOf<Any, String>()

        override val WrappedMethod.uniqueCName: String
            get() = name(this) {
                val base = when (methodType) {
                    MethodType.CONSTRUCTOR -> cName + "_new"

                    MethodType.DESTRUCTOR -> cName + "_dispose"

                    MethodType.SIZE_OF -> cName + "_size_of"

                    MethodType.ALIGN_OF -> cName + "_align_of"

                    MethodType.STATIC,
                    MethodType.STATIC_OP,
                    MethodType.METHOD ->
                        Operator.from(this)?.let {
                            it.name(this@NamerImpl, this)
                        } ?: (
                            cName + "_" + name.splitCamelcase()
                                .joinToString("_") { it.lowercase() }
                                .cleanupOperatorName()
                                .cleanupName()
                            )
                }
                uniqueOverloadName(base, this)
            }

        // First claimant keeps the base name; later overloads of it get an
        // order-independent `__<argtypes>` suffix from their own signature (same
        // convention as KotlinWriter.constructorFactoryName). Falls back to the
        // `_`-prefix uniquifier on the rare collision after typing.
        private fun uniqueOverloadName(base: String, method: WrappedMethod): String {
            if (allNames.add(base)) return base
            val suffix = method.args.joinToString("_") { "${it.type}" }.cleanupName()
            return uniqueNameFor(if (suffix.isEmpty()) base else "${base}__$suffix")
        }

        private inline fun name(obj: Any, generator: () -> String): String =
            nameLookup.getOrPut(obj, generator)

        override val WrappedField.uniqueCGetter: String
            get() = name(this) {
                "${cName}_${name}_get"
            }
        override val WrappedField.uniqueCSetter: String
            get() = name(this.other) {
                "${cName}_${name}_set"
            }

        override fun uniqify(source: Any, name: String): String = name(source) { name }
    }
}

// Defensive sanitization for the C wrapper name of any operator method whose
// symbol is NOT covered by ALL_OPERATORS (so it falls through to the generic
// name path). Every C++ operator symbol is mapped to a DISTINCT, alphabetic,
// valid C-identifier token so that (a) no invalid character leaks into the C
// name and (b) two different operators never collide / truncate to the same
// token. Multi-character symbols are replaced before their single-character
// components so e.g. `<<=` does not get eaten by the `<`/`=` rules first.
//
// This is intentionally a SEPARATE pass from `cleanupName()` (which is also
// applied to type spellings like `type-parameter-0-0`): mapping `-`/`+`/`/`
// globally there would corrupt those type names. Here we only touch strings on
// the operator-name path, and we run before `cleanupName()` so that by the time
// the shared pass runs there are no operator symbols left for it to mishandle.
internal fun String.cleanupOperatorName(): String {
    // Only operator method names carry these symbols; bail early otherwise so
    // the common (non-operator) case stays cheap and untouched.
    if (!startsWith("operator")) return this
    var result = this
    // (symbol -> token), ordered longest-first so multi-char compounds win.
    val replacements = listOf(
        "<<=" to "_shleq",
        ">>=" to "_shreq",
        "<<" to "_shl",
        ">>" to "_shr",
        "<=" to "_le",
        ">=" to "_ge",
        "!=" to "_ne",
        "->" to "_arrow",
        "+=" to "_pluseq",
        "-=" to "_minuseq",
        "*=" to "_timeseq",
        "/=" to "_diveq",
        "%=" to "_modeq",
        "&=" to "_andeq",
        "|=" to "_oreq",
        "^=" to "_xoreq",
        "++" to "_inc",
        "--" to "_dec",
        "+" to "_plus",
        "-" to "_minus",
        "/" to "_div",
        "%" to "_mod",
        "<" to "_lt",
        ">" to "_gt",
        "," to "_comma"
    )
    for ((symbol, token) in replacements) {
        result = result.replace(symbol, token)
    }
    return result
}

internal fun String.cleanupName(): String = replace("::", "_")
    .replace("<", "_")
    .replace(",", "__")
    .replace("!", "_EXP")
    .replace(">", "")
    .replace("*", "_P")
    .replace(" ", "_")
    .replace("==", "_cmp")
    .replace("[]", "_ind")
    // `operator()` (call operator) — not in ALL_OPERATORS, so it reaches this
    // fallback; without sanitizing the parens it produced an invalid C identifier
    // and SIGABRT'd the whole sync. Maps to a plain `_call` method (idiomatic
    // `invoke` mapping is a future improvement).
    .replace("()", "_call")
    .replace("=", "_eq")
    .replace("\"", "_qt")
    // Bitwise operator overloads (notably free flag-enum operators like
    // `std::operator&(E, E)`): sanitize the symbols into valid C identifiers.
    // These only surface now that enum-typed operators resolve.
    .replace("&", "_and")
    .replace("|", "_or")
    .replace("^", "_xor")
    .replace("~", "_inv")
    // An unexposed self-referential typedef arg (e.g. std::set's `value_type`) reaches
    // the overload-suffix mangler as its raw USR (`c:<file>@<offset>`, e.g.
    // `c:stl_set.h@3529`); sanitize `:` `@` `.` so the emitted C identifier is valid
    // (else the wrapper fails to compile / SIGABRTs).
    .replace(":", "_")
    .replace("@", "_")
    .replace(".", "_")
