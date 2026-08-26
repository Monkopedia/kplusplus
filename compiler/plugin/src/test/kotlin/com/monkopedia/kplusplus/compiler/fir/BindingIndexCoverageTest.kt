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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * The **coverage gate** for #186 brick B2 — the replacement for the G5 "index/derivation
 * agreement" gate `docs/design/live-service.md` used to define.
 *
 * G5 was written when the plugin's own mangling was the only source of binding names; comparing
 * it against the index then tested something, because either side could be wrong. Once the index
 * is authoritative that comparison degenerates into asserting that two implementations of one
 * rule agree — which is the maintenance burden #206 was filed to end, not a drift detector. So
 * the assertion changed shape with the code:
 *
 * > For every `cppVector<T>`-family binding the plugin resolves over featuregen's real
 * > instantiation surface, the ClassId comes from `binding-index.json`; a spec the index does
 * > not list is a sync request; and a lookup with no readable index is a **diagnostic**, never a
 * > silently derived name.
 *
 * The denominator is read from the repo rather than hand-listed ([featuregenSpecs]): featuregen's
 * committed `krapped/requested.txt` — the specs the FIR checker itself asked for — plus the
 * `instantiate(...)` calls in its build script, which are the specs seeded before any compile
 * could request them. A new feature row that adds an instantiation is covered automatically.
 *
 * Every index this test builds maps its specs to names that **cannot be derived from the spec**
 * (`probe.Binding17`). That is what makes it a real gate: a plugin that reconstructed krapper's
 * mangling would produce `std.Vector__Int` and fail here, so the test cannot pass unless the
 * name genuinely came out of the file.
 */
class BindingIndexCoverageTest {

    @BeforeTest
    fun reset() {
        CppVectorMapping.configureRootPackage(null)
        CppVectorMapping.configureBindingIndex(null)
    }

    @AfterTest
    fun clear() {
        CppVectorMapping.configureBindingIndex(null)
        tempFiles.forEach { it.delete() }
        tempFiles.clear()
    }

    // ---- coverage ----------------------------------------------------------------------

    @Test
    fun everySpecFeaturegenInstantiatesResolvesOutOfTheIndex() {
        val expected = featuregenSpecs.withIndex().associate { (i, spec) -> spec to "probe.B$i" }
        CppVectorMapping.configureBindingIndex(writeIndex(expected))
        val wrong = featuregenSpecs.mapNotNull { spec ->
            when (val resolution = CppVectorMapping.resolveSpec(spec)) {
                is BindingResolution.Resolved ->
                    if (resolution.classId == expected.getValue(spec).toClassId()) {
                        null
                    } else {
                        "$spec -> ${resolution.classId} (index says ${expected.getValue(spec)})"
                    }
                is BindingResolution.NotGenerated -> "$spec -> reported NOT GENERATED"
                is BindingResolution.NoIndex -> "$spec -> ${resolution.reason}"
            }
        }
        assertTrue(
            wrong.isEmpty(),
            wrong.joinToString(
                prefix = "${wrong.size} of ${featuregenSpecs.size} of featuregen's " +
                    "instantiations did not resolve to the ClassId krapper's index names:\n",
                separator = "\n"
            ) { "  $it" }
        )
    }

    /**
     * Guards the denominator. A silently-empty parse would make the test above pass over
     * nothing, so pin both the size and the shapes that matter — a pointer element, a nested
     * template, a two-argument container, and a user `@CppTemplate` base.
     */
    @Test
    fun theDenominatorIsReadFromFeaturegen() {
        assertTrue(
            featuregenSpecs.size >= MIN_EXPECTED_SPECS,
            "Only ${featuregenSpecs.size} specs came out of $REQUESTED_PROPERTY and " +
                "$BUILD_SCRIPT_PROPERTY; featuregen had $MIN_EXPECTED_SPECS when this test " +
                "was written, so the read is probably broken rather than the surface shrunk."
        )
        listOf(
            "std::vector<int>",
            "std::vector<Thing*>",
            "std::vector<std::vector<int>>",
            "std::map<int, int>",
            "Box<int>"
        ).forEach {
            assertTrue(it in featuregenSpecs, "featuregen no longer instantiates $it")
        }
    }

    // ---- the container table spells specs the way krapper indexes them ------------------

    /**
     * The lookup key is the C++ spec, so [CppVectorMapping.cppSpec] has to spell it byte for
     * byte the way krapper does — including the space after the comma in a two-argument
     * container, which is how the emitted index really reads (`"std::map<int, int>"`).
     */
    @Test
    fun containerSpecsMatchKrappersSpelling() {
        val cases = listOf(
            Triple(CppVectorMapping.cppVector, listOf("int"), "std::vector<int>"),
            Triple(CppVectorMapping.cppVector, listOf("Thing*"), "std::vector<Thing*>"),
            Triple(CppVectorMapping.cppMap, listOf("int", "double"), "std::map<int, double>"),
            Triple(CppVectorMapping.cppPair, listOf("int", "int"), "std::pair<int, int>"),
            Triple(CppVectorMapping.cppSet, listOf("int"), "std::set<int>"),
            Triple(
                CppVectorMapping.cppUniquePtr,
                listOf("int"),
                "std::unique_ptr<int>"
            ),
            Triple(
                CppVectorMapping.cppUnorderedMap,
                listOf("int", "int"),
                "std::unordered_map<int, int>"
            ),
            Triple(
                CppVectorMapping.cppUnorderedSet,
                listOf("int"),
                "std::unordered_set<int>"
            )
        )
        CppVectorMapping.configureBindingIndex(
            writeIndex(cases.associate { (_, _, spec) -> spec to "probe.$Q" })
        )
        cases.forEach { (container, args, spec) ->
            val resolution = CppVectorMapping.resolveBinding(container, args)
            assertEquals(
                BindingResolution.Resolved(spec, "probe.$Q".toClassId()),
                resolution,
                "${container.cppBase} with $args did not look up '$spec'"
            )
        }
    }

    // ---- misses are answers, and absences are diagnostics -------------------------------

    @Test
    fun aSpecTheIndexDoesNotListIsASyncRequestNotAName() {
        CppVectorMapping.configureBindingIndex(
            writeIndex(mapOf("std::vector<int>" to "probe.$Q"))
        )
        assertEquals(
            BindingResolution.NotGenerated("std::vector<double>"),
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("double"))
        )
    }

    @Test
    fun noConfiguredIndexIsReportedNotDerived() {
        val resolution =
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        val noIndex = assertIsNoIndex(resolution)
        assertEquals("std::vector<int>", noIndex.spec)
        assertTrue(
            "bindingIndexPath" in noIndex.reason,
            "The diagnostic must name the missing option; got: ${noIndex.reason}"
        )
    }

    @Test
    fun aMissingIndexFileIsReportedWithItsPath() {
        val absent = File(newTempFile().parentFile, "definitely-not-here.json").absolutePath
        CppVectorMapping.configureBindingIndex(absent)
        val noIndex = assertIsNoIndex(
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        )
        assertTrue(absent in noIndex.reason, "Diagnostic should name $absent: ${noIndex.reason}")
        assertTrue(
            "kplusplusSync" in noIndex.reason,
            "Diagnostic should say how to produce it: ${noIndex.reason}"
        )
    }

    /**
     * The one cross-check that survives deleting the second implementation: plugin and tool are
     * configured from a single `rootPackage`, so a disagreement means the krapped tree on disk
     * predates a configuration change and does not describe this compilation.
     */
    @Test
    fun anIndexGeneratedUnderADifferentRootPackageIsRefused() {
        CppVectorMapping.configureBindingIndex(
            writeIndex(mapOf("std::vector<int>" to "old.std.Vector__Int"), rootPackage = "old")
        )
        CppVectorMapping.configureRootPackage("new")
        val noIndex = assertIsNoIndex(
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        )
        assertTrue(
            "rootPackage=old" in noIndex.reason && "rootPackage=new" in noIndex.reason,
            "Diagnostic should name both root packages; got: ${noIndex.reason}"
        )
    }

    @Test
    fun aMatchingRootPackageResolvesNormally() {
        CppVectorMapping.configureBindingIndex(
            writeIndex(mapOf("std::vector<int>" to "app.std.Vector__Int"), rootPackage = "app")
        )
        CppVectorMapping.configureRootPackage("app")
        assertEquals(
            BindingResolution.Resolved("std::vector<int>", "app.std.Vector__Int".toClassId()),
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        )
    }

    /** A rewritten index inside one daemon session must be re-read, not served from cache. */
    @Test
    fun aRegeneratedIndexIsPickedUp() {
        val file = newTempFile()
        file.writeText(indexJson(mapOf("std::vector<int>" to "probe.First"), null))
        CppVectorMapping.configureBindingIndex(file.absolutePath)
        assertEquals(
            BindingResolution.Resolved("std::vector<int>", "probe.First".toClassId()),
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        )
        file.writeText(
            indexJson(
                mapOf("std::vector<int>" to "probe.Second", "std::set<int>" to "probe.Third"),
                null
            )
        )
        // Only the content-derived stamp may drive the reload; force a distinct mtime so the
        // assertion is about re-reading and not about a filesystem timestamp granularity.
        file.setLastModified(file.lastModified() + 2000)
        assertEquals(
            BindingResolution.Resolved("std::vector<int>", "probe.Second".toClassId()),
            CppVectorMapping.resolveBinding(CppVectorMapping.cppVector, listOf("int"))
        )
    }

    // ---- helpers -----------------------------------------------------------------------

    private fun assertIsNoIndex(resolution: BindingResolution): BindingResolution.NoIndex =
        assertIs<BindingResolution.NoIndex>(
            resolution,
            "Expected a reported failure, but the plugin answered with $resolution — which it " +
                "can only do by inventing a name (#206)."
        )

    private fun String.toClassId(): ClassId = ClassId(
        FqName(substringBeforeLast('.', missingDelimiterValue = "")),
        Name.identifier(substringAfterLast('.'))
    )

    private val tempFiles = mutableListOf<File>()

    private fun newTempFile(): File =
        File.createTempFile("binding-index", ".json").also {
            it.deleteOnExit()
            tempFiles += it
        }

    private fun writeIndex(bindings: Map<String, String>, rootPackage: String? = null): String =
        newTempFile().also { it.writeText(indexJson(bindings, rootPackage)) }.absolutePath

    /**
     * A document shaped like krapper's real emitter output: pretty-printed, `encodeDefaults`,
     * the same field order, and the `templates`/`kotlinTypeMap`/`drops` members the plugin has
     * to skip past.
     */
    private fun indexJson(bindings: Map<String, String>, rootPackage: String?): String =
        buildString {
            appendLine("{")
            appendLine("""    "schemaVersion": 2,""")
            appendLine("""    "runId": "0123456789abcdef",""")
            appendLine(
                "    \"rootPackage\": ${rootPackage?.let { "\"$it\"" } ?: "null"},"
            )
            appendLine("""    "bindings": [""")
            bindings.entries.forEachIndexed { i, (spec, classId) ->
                appendLine("        {")
                appendLine("""            "spec": "$spec",""")
                appendLine("""            "classId": "$classId",""")
                appendLine("""            "pkg": "${classId.substringBeforeLast('.', "")}",""")
                appendLine(
                    """            "simpleName": "${classId.substringAfterLast('.')}","""
                )
                appendLine("""            "cppBase": null,""")
                appendLine("""            "templateArgs": null""")
                appendLine(if (i == bindings.size - 1) "        }" else "        },")
            }
            appendLine("    ],")
            appendLine("""    "templates": null,""")
            appendLine("""    "kotlinTypeMap": {},""")
            appendLine("""    "drops": [""")
            appendLine("        {")
            appendLine("""            "severity": "INFO",""")
            appendLine("""            "message": "a \"quoted\" reason, with a \\ in it",""")
            appendLine("""            "symbol": "Buffer::at",""")
            appendLine("""            "location": {""")
            appendLine("""                "file": "../../src/x.h",""")
            appendLine("""                "line": 339,""")
            appendLine("""                "column": 11""")
            appendLine("            },")
            appendLine("""            "phase": "DEDUP",""")
            appendLine("""            "hint": "no Kotlin binding is generated for this symbol"""")
            appendLine("        }")
            appendLine("    ]")
            append("}")
        }

    /**
     * featuregen's real instantiation surface: the committed manifest the FIR checker grew, plus
     * the build-seeded `instantiate(...)` calls (`Box<int>`, `std::vector<Thing*>`, …) which the
     * checker cannot request because they are used through generated facades.
     */
    private val featuregenSpecs: List<String> by lazy {
        val requested = repoFile(REQUESTED_PROPERTY).readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() }
        val seeded = INSTANTIATE_CALL.findAll(repoFile(BUILD_SCRIPT_PROPERTY).readText())
            .map { it.groupValues[1] }
            .toList()
        check(seeded.isNotEmpty()) {
            "No `instantiate(\"…\")` calls found in ${repoFile(BUILD_SCRIPT_PROPERTY)}; the " +
                "denominator would be short by featuregen's user-template surface."
        }
        (requested + seeded).distinct().sorted()
    }

    private fun repoFile(property: String): File {
        val path = System.getProperty(property)
            ?: error(
                "System property $property is unset; it is wired up in " +
                    "compiler/plugin/build.gradle.kts."
            )
        return File(path).also { check(it.isFile) { "$property does not point at a file: $path" } }
    }

    private companion object {
        /** featuregen's committed `krapped/requested.txt`, supplied by the build. */
        const val REQUESTED_PROPERTY = "kplusplus.test.featuregenRequested"

        /** featuregen's `build.gradle.kts`, supplied by the build. */
        const val BUILD_SCRIPT_PROPERTY = "kplusplus.test.featuregenBuildScript"

        /** featuregen instantiated 17 distinct specs when this gate was written. */
        const val MIN_EXPECTED_SPECS = 17

        /** A name no mangling of any spec could produce. */
        const val Q = "NotAnythingAManglerWouldProduce"

        val INSTANTIATE_CALL = Regex("""instantiate\("([^"]+)"\)""")
    }
}
