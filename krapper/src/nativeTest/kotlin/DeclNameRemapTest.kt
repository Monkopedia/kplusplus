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

import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.plainDeclaredName
import com.monkopedia.krapper.generator.resolvedmodel.type.plainName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Issue #73: a per-file import-alias `remap` must NOT corrupt a type's own DECLARATION name.
 *
 * `ImportBlock.build` disambiguates a same-file collision (two `Kind` imports) by aliasing
 * one — `clang.attr.Kind` -> `AttrKind` — and stamping that alias onto the SHARED
 * `ResolvedKotlinType.remap`. That instance is reused for the enum's own declaration file,
 * so the declaration was emitted as `enum class AttrKind` while its file/package and every
 * reference still used `clang.attr.Kind` -> unresolved reference, K/N compile failed.
 *
 * The invariant locked here: REFERENCES (`name`/`plainName`) honor the alias; the
 * DECLARATION name (`declaredName`/`plainDeclaredName`) is canonical and ignores `remap`.
 */
class DeclNameRemapTest {

    @Test
    fun aliasRemapRenamesReferencesButNotTheDeclaration() {
        val kind = ResolvedKotlinType("clang.attr.Kind", isWrapper = false)

        // Before any collision: reference and declaration agree on the canonical name.
        assertEquals("Kind", kind.plainName)
        assertEquals("Kind", kind.plainDeclaredName)

        // A colliding file aliases this import; ImportBlock stamps the alias onto the
        // shared instance.
        kind.setNameRemap(mapOf("clang.attr.Kind" to "AttrKind"))

        // A REFERENCE to the aliased import must use the alias — that is the remap's job.
        assertEquals("AttrKind", kind.name)
        assertEquals("AttrKind", kind.plainName)

        // The DECLARATION name must stay canonical, independent of the per-file alias —
        // this is the #73 fix. (Pre-fix, `plainDeclaredName` did not exist and the
        // declaration sites read `plainName`, yielding the corrupt `AttrKind`.)
        assertEquals("Kind", kind.declaredName)
        assertEquals("Kind", kind.plainDeclaredName)
    }
}
