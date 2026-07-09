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

import com.monkopedia.krapper.ReferencePolicy
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.HeaderWriter
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.ModelIo
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Hermeticity lock at the SELF-HOSTED boundary (issue #89, the #11a determinism guard
 * relocated).
 *
 * The deleted [DeterminismTest] proved that two sequential generations IN ONE PROCESS over
 * the same input were byte-identical — its load-bearing job was catching a process-global
 * that leaked across runs (the `WrappedType` intern cache + `rootPackage` in
 * [GenerationContext], the [DropLedger], and the now-removed libclang cursor->element memo).
 * It drove that through the in-process libclang parse the self-hosting flip (B5, #88)
 * deleted, so it can't be ported as-is. Two of its three guarded process-globals survive
 * the flip (GenerationContext + DropLedger), and determinism of the self-hosted output is
 * exactly the property the stage-0 bootstrap depends on.
 *
 * This test relocates the guard onto the surviving boundary: it runs the real
 * deserialize -> resolve -> codegen pipeline (the path the self-hosted CLI takes from a
 * krapper_parse-produced ModelIo, minus the clang COMPILE) TWICE in one process and asserts
 * byte-identical output. Each run DESERIALIZES A FRESH model from the same ModelIo JSON and
 * resets the process-scoped state (as `IndexedServiceImpl.init` does) before resolving, so
 * the only thing carried between runs is leaked global state — if the intern cache,
 * rootPackage, or DropLedger leaked run #1 into run #2, the output would diverge and this
 * fails. The model is shaped to MUTATE during resolution (the `value()` overload pair, the
 * `const Dep&` accessor that pulls `Dep` in under INCLUDE_MISSING) so a non-idempotent reuse
 * of carried-over state has the best chance to surface.
 */
class DeterminismTest {

    private fun method(name: String, returnType: WrappedType, isConst: Boolean = false) =
        WrappedMethod(name, returnType, MethodType.METHOD).also { it.isConst = isConst }

    // Build the input model (the moral equivalent of the deleted test's parsed header):
    //   namespace fixture { class Dep { int depValue() const; };
    //                       class Widget { Widget(); int value() const;
    //                                      int value(int scale) const; const Dep& dep() const; }; }
    private fun buildModel(): WrappedTU {
        val tu = WrappedTU()
        val ns = WrappedNamespace("fixture").also {
            tu.addChild(it)
            it.parent = tu
        }
        val depType = WrappedType("fixture::Dep")
        WrappedClass("Dep").also { dep ->
            ns.addChild(dep)
            dep.parent = ns
            dep.addChild(method("depValue", WrappedType("int"), isConst = true))
        }
        WrappedClass("Widget").also { widget ->
            ns.addChild(widget)
            widget.parent = ns
            widget.addChild(
                WrappedConstructor("Widget", WrappedType("fixture::Widget"), false, true)
            )
            widget.addChild(method("value", WrappedType("int"), isConst = true))
            widget.addChild(
                method("value", WrappedType("int"), isConst = true).also {
                    it.addChild(WrappedArgument("scale", WrappedType("int")))
                }
            )
            widget.addChild(method("dep", referenceTo(const(depType)), isConst = true))
        }
        return tu
    }

    // One full deserialize -> resolve -> emit pass into a fresh output dir, returning every
    // generated file as relativePath -> content. Mirrors `IndexedServiceImpl.writeTo`'s
    // header/cpp/kotlin emission (the three real writers) but omits the clang COMPILE + .def
    // (which need the original headers on disk); determinism of the emitted SOURCE is what
    // the bootstrap reproducibility depends on. Resets the process-scoped state up front,
    // exactly as `IndexedServiceImpl.init` does before any resolution.
    private suspend fun generateOnce(modelJson: String): Map<String, String> {
        DropLedger.reset()
        GenerationContext.reset(null)
        val tu = ModelIo.decodeFromString(modelJson)
        val resolver = ParsedResolver(tu)
        val classes: List<ResolvedElement> = resolver.findClasses { defaultFilter() }
            .resolveAll(resolver, ReferencePolicy.INCLUDE_MISSING)

        val outDir = File.createTempDir("krapper_determinism")
        try {
            val module = "fixture"
            val headers = listOf("fixture.h")
            File(outDir, "$module.h").writeText(
                CppCodeBuilder().also {
                    HeaderWriter(it).generate(module, headers, classes)
                }.toString()
            )
            File(outDir, "$module.cc").writeText(
                CppCodeBuilder(qualifyFunctionPointers = true).also {
                    CppWriter(File(outDir, "$module.cc"), it).generate(module, headers, classes)
                }.toString()
            )
            val srcDir = File(outDir, "src")
            KotlinWriter("fixture.internal").generate(srcDir, classes)

            val files = mutableMapOf<String, String>()
            collect(outDir, outDir, files)
            return files
        } finally {
            outDir.rmR()
        }
    }

    private fun collect(dir: File, base: File, into: MutableMap<String, String>) {
        for (file in dir.listFiles().sortedBy { it.name }) {
            if (file.isDir()) {
                collect(file, base, into)
            } else {
                // Normalize the per-run output dir (a legitimate per-invocation INPUT) so the
                // comparison catches real cross-run leaks (stale element content, ordering,
                // counters) without flagging the expected temp-path difference.
                into[file.path.substring(base.path.length)] =
                    file.readText().replace(base.path, "<OUTDIR>")
            }
        }
    }

    @Test
    fun twoInProcessGenerationsAreByteIdentical(): Unit = runBlocking {
        val modelJson = ModelIo.encodeToString(buildModel())
        val first = generateOnce(modelJson)
        val second = generateOnce(modelJson)

        assertTrue(
            first.isNotEmpty(),
            "the generation must produce at least one output file to compare"
        )
        assertEquals(
            first.keys.sorted(),
            second.keys.sorted(),
            "the second in-process run produced a different set of files — a process-global " +
                "leaked across runs (issue #11 hermeticity)"
        )
        for ((path, content) in first) {
            assertEquals(
                content,
                second[path],
                "generated file $path differs between two in-process runs — a process-global " +
                    "leaked across runs (issue #11 hermeticity)"
            )
        }
    }
}
