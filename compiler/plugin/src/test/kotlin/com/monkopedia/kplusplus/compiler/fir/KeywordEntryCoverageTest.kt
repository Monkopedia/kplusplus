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
package com.monkopedia.kplusplus.compiler.fir

import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens

/**
 * #319: featuregen's `KeywordEntry` enum must spell every Kotlin keyword a C++ enumerator can
 * spell. featuregen then compiles the enum krapper generates from it. Together the two checks
 * show krapper's enum-entry escaping covers the Kotlin lexer's actual keyword tables, rather
 * than a hand list that could drift from them. A keyword a newer Kotlin adds fails this test
 * until the fixture gains it, and then fails the featuregen compile until krapper escapes it.
 */
class KeywordEntryCoverageTest {
    @Test
    fun fixture_spells_every_kotlin_keyword_a_cpp_enumerator_can() {
        val keywords = (KtTokens.KEYWORDS.types + KtTokens.SOFT_KEYWORDS.types)
            .map { (it as KtKeywordToken).value }
            .filter { IDENTIFIER.matches(it) }
            .toSet()
        // A silently-empty read would pass over nothing.
        check(keywords.size > 60) { "Only ${keywords.size} keywords read from KtTokens" }
        val fixture = FIXTURE_ENUM.find(FeaturegenSurface.repoFile(HEADER_PROPERTY).readText())
            ?.groupValues?.get(1)?.split(',')?.map(String::trim)?.toSet()
            ?: error("No `enum class KeywordEntry : int { ... }` in $HEADER_PROPERTY")
        val missing = keywords - CPP_RESERVED - fixture
        assertTrue(missing.isEmpty(), "Kotlin keywords missing from KeywordEntry: $missing")
    }

    private companion object {
        const val HEADER_PROPERTY = "kplusplus.test.featuregenHeader"
        val IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")
        val FIXTURE_ENUM = Regex("""enum class KeywordEntry : int \{([^}]*)\}""")

        // Kotlin keywords that C++ reserves too, so no enumerator can carry them. `typeof` is a
        // keyword in the GNU dialects.
        val CPP_RESERVED = setOf(
            "break", "catch", "class", "const", "continue", "do", "else", "enum", "false", "for",
            "if", "inline", "operator", "private", "protected", "public", "return", "this",
            "throw", "true", "try", "typeof", "while"
        )
    }
}
