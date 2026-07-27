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
package com.monkopedia.krapper.generator.model

/**
 * The synthesized forcing header for a template instantiation request (`--instantiate
 * "std::vector<T*>"`): a `KrapperForce_*` struct whose `value` member's type IS the
 * requested specialization, `#include`-ing the std headers the target names plus the
 * consumer's own headers. Resolving the struct's member materializes the specialization.
 *
 * One definition of these bytes (used by IndexedServiceImpl.requestInstantiation, which
 * parses this header as its own translation unit) guarantees the specialization is always
 * forced from the same translation unit — and that the `forceName` the generated wrapper
 * #includes by always matches.
 */
object ForcingHeader {

    /**
     * The forcing struct's name (also the synthesized header's file name). A pointer/
     * reference template arg (e.g. std::vector<Thing*>, from the T1.3 range
     * materialization) would otherwise leak `*`/`&` into the struct's identifier and the
     * filename — both illegal — crashing the parse with `expected unqualified-id`. Map
     * them to a token instead. The forcing struct's `value` member still uses the real
     * target (Thing*), so the instantiation is unaffected.
     */
    fun forceName(target: String): String = "KrapperForce_" + target
        .replace("::", "_")
        .replace("<", "_")
        .replace(">", "")
        .replace(", ", "_")
        .replace(" ", "")
        .replace("*", "Ptr")
        .replace("&", "Ref")

    /**
     * The synthesized header's content. Every type in the instantiation must be declared,
     * not just the outer base (std::vector<std::string> needs basic_string declared too),
     * so the std includes are TARGETED to the types named in [target] (scanning, so nested
     * element types are covered) plus the consumer's own [userHeaders] (for user element
     * types like a wrapped struct in std::vector<Point>). Targeted rather than a blanket
     * bundle: a broad include drags unrelated system structs in under INCLUDE_MISSING
     * (e.g. <sys/timex.h>'s `timex`), which then generate invalid wrappers.
     */
    fun contentFor(target: String, userHeaders: List<String>): String = buildString {
        for (stdHeader in stdHeadersFor(target)) appendLine("#include $stdHeader")
        for (userHeader in userHeaders) appendLine("#include \"$userHeader\"")
        appendLine("struct ${forceName(target)} { $target value; };")
    }

    /**
     * The std headers needed to declare the types named in an instantiation target
     * string. Scans for type tokens so nested element types are covered (e.g.
     * std::vector<std::string> -> <vector> + <string>).
     */
    fun stdHeadersFor(target: String): List<String> =
        STD_TYPE_HEADERS.filter { (token, _) -> target.contains(token) }
            .map { it.second }
            .distinct()

    // token (as it appears in a canonical type string) -> std header.
    // "basic_string" matches std::__cxx11::basic_string (canonical std::string).
    private val STD_TYPE_HEADERS = listOf(
        "basic_string" to "<string>",
        "vector" to "<vector>",
        "unordered_map" to "<unordered_map>",
        "unordered_set" to "<unordered_set>",
        "map" to "<map>",
        "set" to "<set>",
        "list" to "<list>",
        "deque" to "<deque>",
        "pair" to "<utility>",
        "tuple" to "<tuple>",
        "unique_ptr" to "<memory>",
        "shared_ptr" to "<memory>",
        "weak_ptr" to "<memory>"
    )
}
