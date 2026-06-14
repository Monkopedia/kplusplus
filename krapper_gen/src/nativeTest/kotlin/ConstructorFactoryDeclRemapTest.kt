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
 * Issue #96 — the last latent DECLARATION-side residual of the #73/#75/#81 remap-hazard
 * family, and the declaration-side sibling of ConstructorCallRemapTest (#81, the call side).
 *
 * A wrapper class is constructed via a generated `MemScope.<Type>(...)` factory. That factory
 * is DECLARED once (in the class's own file) by `KotlinWriter.constructorFactoryName` (the
 * direct path) and `generateStackConstructor` (the stack path). Both used to read `name`, which
 * routes through the per-file import-alias `remap` that `ImportBlock.build` stamps onto the
 * SHARED `ResolvedKotlinType` instance while emitting one file. A DECLARATION name must be the
 * type's own identity, independent of any alias — otherwise a stale `remap` contaminating the
 * declaring file's build would DECLARE the factory under the aliased name while #81's call names
 * it canonically (`plainDeclaredName`), producing an undeclared-symbol mismatch.
 *
 * The fix: both factory-declaration sites use the canonical `plainDeclaredName`, which ignores
 * `remap` — matching the `_Holder` declaration (#75) and the `class X` token (#73). With the
 * call already canonical (#81), declaration-name == call-name at BOTH ends; neither follows the
 * alias. This closes the #73/#75/#81/#96 family. Accessor-contrast, since featuregen has no
 * import collision (no alias → canonical and aliased names coincide, output byte-identical).
 */
class ConstructorFactoryDeclRemapTest {

    /**
     * Renders the constructor-factory DECLARATION name exactly as `constructorFactoryName`'s
     * base / `generateStackConstructor` does — `type.<accessor>()`. [declName] selects the
     * accessor so the test can contrast the fixed (`plainDeclaredName`) path with the pre-fix
     * (`plainName`) path.
     */
    private fun renderFactoryDecl(
        type: ResolvedKotlinType,
        declName: ResolvedKotlinType.() -> String
    ): String = type.declName()

    @Test
    fun constructorFactoryDeclStaysCanonicalUnderAFileAlias() {
        val kind = ResolvedKotlinType("clang.attr.Kind", isWrapper = true)

        // No collision yet: both accessors agree, the factory is declared canonically.
        assertEquals("Kind", renderFactoryDecl(kind) { plainDeclaredName })

        // A stale/contaminating same-file import collision aliases this import; ImportBlock
        // stamps the alias onto the SHARED instance the declaration reads.
        kind.setNameRemap(mapOf("clang.attr.Kind" to "AttrKind"))

        // The fix: the factory DECLARATION uses the canonical declared name, so it is declared
        // under the same name #81's call uses — regardless of any contaminating alias.
        assertEquals("Kind", renderFactoryDecl(kind) { plainDeclaredName })

        // Pre-fix the declaration read `plainName`, which follows the alias — `AttrKind`,
        // mismatching the canonical call (#81). This asserts the exact residual the fix removes.
        assertEquals("AttrKind", renderFactoryDecl(kind) { plainName })
    }
}
