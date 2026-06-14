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
 * Issue #81 — the CONSTRUCTOR-factory sibling of #73 (declaration side) and #75 (the
 * `_Holder` factory call).
 *
 * A wrapper class is constructed via a generated `MemScope.<Type>(...)` factory. That
 * factory is DECLARED once (in the class's own file) under the canonical name. `KotlinWriter`
 * builds the CALL to it in `constructorMethod` via `extensionMethod(type.pkg, <name>)`. If the
 * file emitting the call had a same-file import collision and aliased the constructed type
 * (`clang.attr.Kind` -> `AttrKind`), reading `plainName` there yields the alias, so the call
 * became `AttrKind(...)` — an undeclared symbol, because the factory is declared as `Kind`.
 *
 * The fix mirrors #75: the constructor-factory call uses the canonical `plainDeclaredName`,
 * which ignores the per-file `remap`. The invariant locked here: a generated SIBLING factory's
 * name is canonical at BOTH ends. Declaration-name == call-name; neither follows the alias.
 */
class ConstructorCallRemapTest {

    /**
     * Builds the `MemScope.<Type>(...)` constructor-factory call symbol exactly as
     * `KotlinWriter.constructorMethod` does, then renders it. [factoryName] selects the name
     * accessor so the test can contrast the fixed (`plainDeclaredName`) path with the pre-fix
     * (`plainName`) path.
     */
    private fun renderConstructorCall(
        type: ResolvedKotlinType,
        factoryName: ResolvedKotlinType.() -> String
    ): String {
        val symbol = extensionMethod(type.pkg, type.factoryName())
        return buildCode { symbol.build(this) }
    }

    @Test
    fun constructorCallStaysCanonicalUnderAReferenceFileAlias() {
        val kind = ResolvedKotlinType("clang.attr.Kind", isWrapper = true)

        // No collision yet: both accessors agree, the constructor-factory call is canonical.
        assertEquals("Kind", renderConstructorCall(kind) { plainDeclaredName })

        // A remote calling file has a same-file import collision and aliases this import;
        // ImportBlock stamps the alias onto the SHARED instance the constructor call reads.
        kind.setNameRemap(mapOf("clang.attr.Kind" to "AttrKind"))

        // The fix: the constructor call uses the canonical declared name, so it still names
        // the factory that was declared canonically — regardless of the calling file's alias.
        assertEquals("Kind", renderConstructorCall(kind) { plainDeclaredName })

        // Pre-fix the call read `plainName`, which follows the alias — `AttrKind`, an
        // undeclared symbol. This asserts the exact regression the fix removes.
        assertEquals("AttrKind", renderConstructorCall(kind) { plainName })
    }
}
