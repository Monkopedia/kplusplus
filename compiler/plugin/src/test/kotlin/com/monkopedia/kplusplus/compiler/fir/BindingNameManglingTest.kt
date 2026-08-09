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

import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * The plugin's binding-name mangling, cross-checked against krapper's (#206).
 *
 * krapper GENERATES the binding classes; this plugin only LOOKS THEM UP, by recomputing the
 * name krapper will have emitted. Two implementations of one rule, in two builds that cannot
 * share code (krapper's `WrappedKotlinType` lives in `nativeMain`, which the plugin does not
 * compile), so nothing but a test can keep them in step — and they had already drifted on `*`
 * when this test was written.
 *
 * The oracle is NOT a third hand copy of the rule: it is read out of krapper's own source
 * file at test time (see [krapperReplacements]), so a change to krapper's replacement chain
 * that the plugin does not follow fails here rather than silently producing a `ClassId` for a
 * class that does not exist.
 */
class BindingNameManglingTest {

    @BeforeTest
    fun resetRootPackage() {
        // The object caches the configured root package for the compiler-daemon lifetime;
        // null is the historical layout, in which std containers live in bare `std`.
        CppVectorMapping.configureRootPackage(null)
    }

    /**
     * C++ template-argument spellings that reach `bindingClassId`. Every kind of punctuation
     * the two implementations claim to handle appears at least once, and — the point of the
     * ticket — so does the pointer.
     */
    private val specs = listOf(
        "int",
        "long",
        "double",
        "unsigned int",
        "long long",
        "std::string",
        "clang::CXXRecordDecl",
        // Pointers: the character the two implementations disagreed on.
        "int*",
        "char**",
        "clang::CXXBaseSpecifier*",
        "std::string *",
        // Nested templates.
        "std::vector<int>",
        "std::vector<int*>",
        "std::pair<int, int>"
    )

    @Test
    fun pluginManglingAgreesWithKrapper() {
        val divergent = specs.mapNotNull { spec ->
            val krapper = krapperMangle(spec)
            val plugin = CppVectorMapping.mangleTemplateArg(spec)
            if (krapper == plugin) null else Triple(spec, krapper, plugin)
        }
        assertTrue(
            divergent.isEmpty(),
            divergent.joinToString(
                prefix = "The FIR plugin computes a binding name krapper never emits " +
                    "(${divergent.size} of ${specs.size} specs):\n",
                separator = "\n"
            ) { (spec, krapper, plugin) ->
                "  $spec -> krapper emits '$krapper', plugin looks up '$plugin'"
            }
        )
    }

    /**
     * The end-to-end shape, pinned to a binding that exists on disk today: krapper's own
     * self-hosting run emits `std/Vector__CXXBaseSpecifier_P.kt` carrying
     * `@krapper.CppBinding("std::vector<clang::CXXBaseSpecifier*>")`. Before #206 the plugin
     * resolved that call to `std.Vector__CXXBaseSpecifier_`, which is nothing.
     */
    @Test
    fun aPointerElementResolvesToTheBindingKrapperEmitted() {
        assertEquals(
            ClassId(FqName("std"), Name.identifier("Vector__CXXBaseSpecifier_P")),
            CppVectorMapping.bindingClassId(
                CppVectorMapping.cppVector,
                listOf("clang::CXXBaseSpecifier*")
            )
        )
    }

    /**
     * Guards the oracle itself. If the parse below silently produced an empty chain, every
     * other assertion here would pass vacuously, so assert the one mapping the ticket is
     * about actually came out of krapper's file.
     */
    @Test
    fun theOracleIsReadFromKrapperSource() {
        assertEquals(
            "_P",
            krapperReplacements.toMap()["*"],
            "Expected krapper's mangleToIdentifier to map '*'; parsed chain: " +
                "$krapperReplacements"
        )
    }

    /**
     * The oracle reproduces krapper's `.replace(...)` chain faithfully, but hand-copies the
     * prelude that runs before it (split on `::`, take the last segment, capitalize). Pin
     * that prelude to krapper's source too, so a change to it cannot slip through as "the
     * replacements still match".
     */
    @Test
    fun krapperStillAppliesTheExpectedPrelude() {
        val prelude = """it.toString().split("::").last().capitalize().mangleToIdentifier()"""
        assertTrue(
            krapperSource.collapseWhitespace().contains(prelude.collapseWhitespace()),
            "krapper's WrappedTemplateType branch no longer reads `$prelude`; the oracle's " +
                "hand-copied prelude (and probably CppVectorMapping.mangleTemplateArg) needs " +
                "to be brought back into line."
        )
    }

    // ---- the oracle -------------------------------------------------------------------

    /** krapper's mangling, applied the way krapper's `WrappedTemplateType` branch applies it. */
    private fun krapperMangle(spec: String): String = krapperReplacements.fold(
        spec.split("::").last().replaceFirstChar { it.uppercase() }
    ) { mangled, (from, to) -> mangled.replace(from, to) }

    /**
     * krapper's `String.mangleToIdentifier()` replacement chain, in order, parsed straight out
     * of its source. Deliberately brittle: any restructuring of that function fails this test
     * loudly instead of degrading into a copy that no longer tracks the original.
     */
    private val krapperReplacements: List<Pair<String, String>> by lazy {
        val lines = krapperSource.lines()
        val declaration = lines.indexOfFirst { it.contains(MANGLE_DECLARATION) }
        check(declaration >= 0) {
            "No `$MANGLE_DECLARATION` in $KRAPPER_SOURCE_PROPERTY — krapper's mangling moved."
        }
        val chain = lines.asSequence()
            .drop(declaration + 1)
            .map(String::trim)
            .takeWhile { it.startsWith(".replace(") }
            .map { line ->
                val match = REPLACE_CALL.matchEntire(line)
                    ?: error("Cannot read krapper's replacement from: $line")
                val (from, to) = match.destructured
                check('\\' !in from && '\\' !in to) {
                    "Escaped replacement in krapper's mangling ($line); this oracle only " +
                        "understands literal arguments."
                }
                from to to
            }.toList()
        check(chain.isNotEmpty()) {
            "`$MANGLE_DECLARATION` is no longer followed by a `.replace(...)` chain."
        }
        chain
    }

    private val krapperSource: String by lazy {
        val path = System.getProperty(KRAPPER_SOURCE_PROPERTY)
            ?: error(
                "System property $KRAPPER_SOURCE_PROPERTY is unset; it is wired up in " +
                    "compiler/plugin/build.gradle.kts."
            )
        val file = File(path)
        check(file.isFile) { "krapper's mangling source is not at $path" }
        file.readText()
    }

    private fun String.collapseWhitespace(): String = replace(WHITESPACE, "")

    private companion object {
        /** Absolute path of krapper's `WrappedKotlinType.kt`, supplied by the build. */
        const val KRAPPER_SOURCE_PROPERTY = "kplusplus.test.krapperManglingSource"
        const val MANGLE_DECLARATION = "fun String.mangleToIdentifier()"
        val REPLACE_CALL = Regex("""\.replace\("(.*)", "(.*)"\)""")
        val WHITESPACE = Regex("""\s+""")
    }
}
