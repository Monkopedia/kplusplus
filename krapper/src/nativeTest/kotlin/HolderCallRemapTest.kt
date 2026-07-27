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

import com.monkopedia.krapper.generator.builders.buildCode
import com.monkopedia.krapper.generator.builders.extensionMethod
import com.monkopedia.krapper.generator.resolvedmodel.type.ResolvedKotlinType
import com.monkopedia.krapper.generator.resolvedmodel.type.plainDeclaredName
import com.monkopedia.krapper.generator.resolvedmodel.type.plainName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Issue #75 — the REFERENCE-side sibling of #73.
 *
 * A class with an ARG_CAST return is constructed via a generated `<Type>_Holder` factory.
 * That factory is DECLARED once (in the class's own file) under the canonical name —
 * `KotlinWriter` builds the declaration token from `plainDeclaredName`, which ignores the
 * per-file import-alias `remap`. The factory is CALLED from `generateMethodBody`'s ARG_CAST
 * path, which is emitted in the file of the METHOD's owning class. If that remote file had a
 * same-file import collision and aliased the returned type (`clang.attr.Kind` -> `AttrKind`),
 * reading `plainName` there yields the alias, so the call became `AttrKind_Holder` — an
 * undeclared symbol, because the factory is declared as `Kind_Holder`. K/N compile failed.
 *
 * The invariant locked here: a generated SIBLING factory's name is canonical at BOTH ends.
 * Declaration-name == call-name; neither follows the per-file alias.
 */
class HolderCallRemapTest {

    /**
     * Builds the `<Type>_Holder` call symbol exactly as `KotlinWriter.generateMethodBody`
     * does for an ARG_CAST wrapper return, then renders it. [holderName] selects the name
     * accessor so the test can contrast the fixed (`plainDeclaredName`) path with the
     * pre-fix (`plainName`) path.
     */
    private fun renderHolderCall(
        type: ResolvedKotlinType,
        holderName: ResolvedKotlinType.() -> String
    ): String {
        val symbol = extensionMethod(
            type.fullyQualified + ".Companion",
            type.holderName() + "_Holder"
        )
        return buildCode { symbol.build(this) }
    }

    @Test
    fun holderCallStaysCanonicalUnderAReferenceFileAlias() {
        val kind = ResolvedKotlinType("clang.attr.Kind", isWrapper = true)

        // No collision yet: both accessors agree, the holder call is canonical.
        assertEquals("Kind_Holder", renderHolderCall(kind) { plainDeclaredName })

        // A remote calling file has a same-file import collision and aliases this import;
        // ImportBlock stamps the alias onto the SHARED instance the ARG_CAST return reads.
        kind.setNameRemap(mapOf("clang.attr.Kind" to "AttrKind"))

        // The fix: the holder CALL uses the canonical declared name, so it still names the
        // factory that was declared canonically — regardless of the calling file's alias.
        assertEquals("Kind_Holder", renderHolderCall(kind) { plainDeclaredName })

        // Pre-fix the call read `plainName`, which follows the alias — `AttrKind_Holder`,
        // an undeclared symbol. This asserts the exact regression the fix removes.
        assertEquals("AttrKind_Holder", renderHolderCall(kind) { plainName })
    }
}
