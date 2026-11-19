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
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * The **spelling gate** (#237): the in-tree plugin's lookup keys against a `binding-index.json`
 * the in-tree krapper really emitted for featuregen.
 *
 * After #186/B2 the one rule plugin and tool still share is how a C++ instantiation is spelled:
 * krapper keys each index entry `spec = cls.type.toString()`, the plugin looks it up by
 * [CppVectorMapping.cppSpec]. [BindingIndexCoverageTest] cannot see a disagreement — it writes
 * its own index, keyed by the spellings it already expects. This gate reads the real one: each
 * featuregen spec is split into its base and top-level arguments, re-spelled by the plugin's
 * `cppSpec`, and must resolve. A space inside `<>`, a dropped space after a comma, or a change of
 * qualification on either side fails it, naming the spec.
 *
 * Only the top level is re-spelled. A nested argument reaches `cppSpec` verbatim off the inner
 * binding's `@krapper.CppBinding("<spec>")`, which krapper writes from the same `cls.type` as the
 * index key — so it is krapper's spelling on both sides by construction.
 *
 * It needs a sync first, so it is not part of `test`: the `bindingIndexGate` task runs it
 * (compiler/plugin/build.gradle.kts), and root-build.yml's generator-suite job runs that task
 * after the job's featuregen build has emitted the index through the in-tree tool.
 */
class BindingIndexSpellingGateTest {

    @AfterTest
    fun clear() {
        CppVectorMapping.configureRootPackage(null)
        CppVectorMapping.configureBindingIndex(null)
    }

    @Test
    fun everyFeaturegenSpecThePluginSpellsIsAKeyOfTheEmittedIndex() {
        val path = FeaturegenSurface.repoFile(INDEX_PROPERTY).absolutePath
        val load = assertIs<BindingIndexLoad.Loaded>(BindingIndexReader.load(path))
        CppVectorMapping.configureBindingIndex(path)
        CppVectorMapping.configureRootPackage(load.index.rootPackage)
        val missed = FeaturegenSurface.specs.mapNotNull { spec ->
            val (base, args) = splitSpec(spec)
            val container = CppVectorMapping.Container(PROBE_FACADE, base, args.size)
            when (val resolution = CppVectorMapping.resolveBinding(container, args)) {
                is BindingResolution.Resolved -> null
                else -> "$spec -> $resolution"
            }
        }
        assertTrue(
            missed.isEmpty(),
            "${missed.size} of ${FeaturegenSurface.specs.size} featuregen specs, spelled by " +
                "the in-tree plugin, are not keys of the index krapper emitted ($path), so " +
                "the plugin would report them as never generated:\n" +
                missed.joinToString("\n") { "  $it" } +
                "\nThe index's template keys are:\n" +
                SPEC_KEY.findAll(File(path).readText())
                    .map { it.groupValues[1] }
                    .filter { '<' in it }
                    .joinToString("\n") { "  $it" }
        )
    }

    /** `std::map<int, Box<int>>` -> (`std::map`, [`int`, `Box<int>`]): a depth-aware split. */
    private fun splitSpec(spec: String): Pair<String, List<String>> {
        val open = spec.indexOf('<')
        check(open > 0 && spec.endsWith('>')) { "$spec is not a template instantiation" }
        val args = mutableListOf<String>()
        var depth = 0
        var start = open + 1
        for (i in start until spec.length - 1) {
            when (spec[i]) {
                '<' -> depth++

                '>' -> depth--

                ',' -> if (depth == 0) {
                    args += spec.substring(start, i).trim()
                    start = i + 1
                }
            }
        }
        args += spec.substring(start, spec.length - 1).trim()
        return spec.substring(0, open).trim() to args
    }

    private companion object {
        /** The emitted index, supplied by the `bindingIndexGate` task. */
        const val INDEX_PROPERTY = "kplusplus.test.featuregenBindingIndex"

        /** `resolveBinding` keys on the base and arguments only; the facade is never read. */
        val PROBE_FACADE = CallableId(FqName("probe"), Name.identifier("facade"))

        val SPEC_KEY = Regex(""""spec":\s*"([^"]*)"""")
    }
}
