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

/**
 * Narrow, JSON-serializable escape hatch for v2 binding generation.
 *
 * The v2 flow is deliberately *usage-driven* — the FIR compiler plugin observes
 * `cppVector<T>()` / `cppMap<K,V>()` calls and asks krapper to synthesize
 * exactly those instantiations. Some semantic corrections, though, can't be
 * derived from usage (e.g. "this method's return type has a stale `const ` we
 * need to strip", "this binding's auto-generated `get()` is wrong for the
 * unique_ptr ownership semantics"). Fixups are the narrow per-binding-spec
 * escape hatch for those cases.
 *
 * Each [Fixup] is a small, declarative directive. They are serialized to a
 * JSON file that the kplusplusSync task writes; krapper reads the file and
 * translates the directives back into the existing mapping pipeline at
 * generation time. Adding a new fixup type requires (1) a new sealed subclass
 * here and (2) handling in `FixupApplier`.
 *
 * Intentionally *narrower* than the v1 mapping DSL:
 *   - no arbitrary Kotlin closures
 *   - no full FIR introspection
 *   - no return-type rewrites beyond pattern-match-and-strip
 *
 * The four directives below cover the four use cases the v8 example needed.
 * Adding more is fine — keep them narrow & data-shaped.
 */
@Serializable
sealed class Fixup

/**
 * Remove a method by its generated `uniqueCName`. Used when the generator
 * produces a method that fails to compile for a particular instantiation —
 * e.g. `v8::Persistent<v8::Value>`'s copy assignment, which v8 deletes.
 */
@Serializable
data class RemoveMethodByCName(val uniqueCName: String) : Fixup()

/**
 * If a method's return-type string starts with [matchPrefix], strip the
 * literal `const ` prefix from it. Used for v8's `const v8::Local<...>`,
 * `const v8::MaybeLocal<...>`, etc. — the const qualifier confuses the
 * generated Kotlin binding when the underlying type is value-semantic.
 */
@Serializable
data class StripConstFromReturnType(val matchPrefix: String) : Fixup()

/**
 * Apply the v8::ScriptOrigin::options return-type cleanup. The auto-generated
 * binding returns `const ScriptOriginOptions*`; v8 expects callers to consume
 * it by value with copy-construction. This fixup encodes the very specific
 * tweak (strip leading `const `, trim trailing `*`, mark returnStyle =
 * COPY_CONSTRUCTOR). Kept as a built-in because the rule is not reusable
 * outside this exact callee.
 */
@Serializable
object ScriptOriginOptionsFix : Fixup()

/**
 * Add a `get()` method to every `std::unique_ptr<...>` binding that exposes
 * the raw underlying pointer (the auto-generated one doesn't handle unique
 * ownership properly). Generated per-instantiation so the unique C name is
 * unique per element type.
 */
@Serializable
object AddUniquePtrGet : Fixup()
