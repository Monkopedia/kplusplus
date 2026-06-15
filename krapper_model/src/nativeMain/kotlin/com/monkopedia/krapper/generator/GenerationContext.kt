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

import com.monkopedia.krapper.generator.model.type.WrappedType

/**
 * Run-scoped state for a single generation pass, gathered behind one explicit,
 * resettable carrier instead of loose top-level `var`/`val`s.
 *
 * Two pieces of mutable state used to live as process-global top-levels —
 * `WrappedType`'s `existingTypes` intern cache and `WrappedKotlinType`'s
 * `rootPackageOverride`. Both are read from the context-less `WrappedType.invoke` /
 * `WrappedType.kotlinType` / `fullyQualifiedType` surfaces, which are called
 * pervasively with no carrier to thread, so passing a context parameter to every
 * call site would be sprawling churn for no behavioral gain.
 *
 * Instead this object follows the same idiom as the sibling [DropLedger] / [Log]
 * objects: the generator runs one request at a time under `runBlocking`, so a
 * process-scoped object whose state is cleared per run is the cohesive carrier
 * without threading a handle through every signature. [reset] re-establishes a
 * clean pass (called from `IndexedServiceImpl.init`, next to [DropLedger.reset]) so
 * sequential in-process runs no longer leak the intern cache into one another — the
 * order-sensitivity called out in docs/clang-bootstrap.md and issue #11.
 */
object GenerationContext {
    /**
     * Intern/dedup cache for [WrappedType] values keyed by their string spelling,
     * so structurally-identical types share one instance within a pass. Cleared by
     * [reset]; starts empty, matching a fresh process.
     */
    val internedTypes = mutableMapOf<String, WrappedType>()

    /**
     * The configured root package every generated wrapper binding is nested under
     * (`config.rootPackage`), or null for the legacy top-level layout. Set once per
     * run by [reset] before any resolution — a type's Kotlin package is baked into
     * its `ResolvedKotlinType` at resolve time, so it must be in place up front.
     */
    var rootPackage: String? = null
        private set

    /**
     * True when the target C++ library is compiled `-fno-rtti` (e.g. v8's monolith),
     * so it exports no `typeinfo` symbols. Under this flag the down-cast helpers must NOT
     * emit a generic `dynamic_cast<D*>` (it would reference a `typeinfo for D` the library
     * doesn't provide and fail to LINK); only the LLVM-style `classof` down-cast — which
     * needs no RTTI — is emitted. Set once per run by [reset] from `config.noRtti`.
     */
    var noRtti: Boolean = false
        private set

    /** Begin a fresh generation pass: clear the intern cache and root [rootPackage]/[noRtti]. */
    fun reset(rootPackage: String?, noRtti: Boolean = false) {
        internedTypes.clear()
        this.rootPackage = rootPackage
        this.noRtti = noRtti
    }
}
