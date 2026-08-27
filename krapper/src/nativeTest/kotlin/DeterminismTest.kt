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

import com.monkopedia.krapper.AllowListFilter
import com.monkopedia.krapper.DefaultFilter
import com.monkopedia.krapper.FilterDefinition
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
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix.getenv
import platform.posix.readlink
import platform.posix.system

// The child process is this same test binary, re-executed with these two variables set: one
// names the config to generate, the other where to write the transcript. Absent (the normal,
// parent-side run of `childFreshProcessBaseline`) the child entry point does nothing.
private const val CHILD_CONFIG_ENV = "KRAPPER_DETERMINISM_CHILD_CONFIG"
private const val CHILD_OUT_ENV = "KRAPPER_DETERMINISM_CHILD_OUT"

/** The single `@Test` the parent re-executes; matched by the runner's `--ktest_filter`. */
private const val CHILD_TEST_NAME = "childFreshProcessBaseline"

/**
 * Hermeticity lock at the SELF-HOSTED boundary (issue #89, the #11a determinism guard
 * relocated), extended by brick B4 (#186) with the **G3** gate.
 *
 * The deleted original proved that two sequential generations IN ONE PROCESS over the same
 * input were byte-identical — its load-bearing job was catching a process-global that leaked
 * across runs (the `WrappedType` intern cache + `rootPackage` in [GenerationContext], the
 * [DropLedger], and the now-removed libclang cursor->element memo). It drove that through the
 * in-process libclang parse the self-hosting flip (B5, #88) deleted, so it can't be ported
 * as-is; this file runs the real deserialize -> resolve -> codegen pipeline instead (the path
 * the self-hosted CLI takes from a front-end-produced ModelIo, minus the clang COMPILE).
 *
 * Three tests, and the third is the one B4 exists for:
 *
 *  - [twoInProcessGenerationsAreByteIdentical] — the original guard: same config twice in one
 *    process, byte-identical.
 *  - [shuffledRequestOrderIsByteIdentical] — **G3(a)**: the same request SET presented in a
 *    different ORDER emits the same bytes. Ordering throughout the writers is insertion order
 *    (design doc §1.5, "resolve order is emission order"), so this pins that the emitted
 *    content is a function of the set and not of the arrival order.
 *  - [twoConfigsInOneProcessMatchFreshProcesses] — **G3(b)**: two DIFFERENT configs run
 *    back-to-back in one process, each compared against **a genuine fresh-process run of the
 *    same config**. This is the direct test of §1.4 and the gate for B4.
 *
 * **Why (b) needs a real second process.** A single in-process run proves nothing about
 * leaked state, and neither does comparing two in-process runs to each other: if both sides
 * share the leak the comparison is satisfied and reports nothing. The failure this brick
 * prevents is config A's leftovers changing config B's output *within one process*, so the
 * only baseline that can detect it is one produced where config A never ran. [freshProcess]
 * gets that by re-executing this very test binary (`/proc/self/exe`) with
 * [CHILD_CONFIG_ENV] set, filtered down to [CHILD_TEST_NAME] — the same production code, in a
 * process that has generated nothing else.
 *
 * **What is compared.** Every emitted file, plus the run's drop-ledger report under the
 * `<drop-ledger>` key. The ledger is included deliberately: it is per-run state that the
 * emitted sources do not reflect, so without it a ledger that accumulated config A's drops
 * into config B's run would go unnoticed (the same reason G2 diffs the ledger).
 *
 * The two configs differ in the ways run-scoped state actually varies: root package, the
 * `-fno-rtti` down-cast flag, the reference policy, the module/package names, and the model
 * itself — [Config.A]'s model carries a member whose return type resolves nowhere, so under
 * `IGNORE_MISSING` it is DROPPED and A's ledger is non-empty while B's is empty. A ledger
 * leaking A into B is therefore visible in B's transcript.
 */
class DeterminismTest {

    /** Key under which a run's drop-ledger report joins the emitted files in the transcript. */
    private val ledgerEntry = "<drop-ledger>"

    /**
     * One generation's worth of configuration — the axes `KrapperConfig` varies that this
     * pipeline can see without a clang parse.
     */
    private data class Config(
        val name: String,
        val rootPackage: String?,
        val noRtti: Boolean,
        val policy: ReferencePolicy,
        val module: String,
        val kotlinPackage: String,
        /** Add a member that resolves nowhere, so the run drops it and ledgers the drop. */
        val withUnresolvableMember: Boolean
    ) {
        companion object {
            val A = Config(
                name = "A",
                rootPackage = null,
                noRtti = false,
                policy = ReferencePolicy.IGNORE_MISSING,
                module = "alpha",
                kotlinPackage = "alpha.internal",
                withUnresolvableMember = true
            )
            val B = Config(
                name = "B",
                rootPackage = "com.example.beta",
                noRtti = true,
                policy = ReferencePolicy.INCLUDE_MISSING,
                module = "beta",
                kotlinPackage = "beta.internal",
                withUnresolvableMember = false
            )

            fun byName(name: String): Config = when (name) {
                A.name -> A
                B.name -> B
                else -> error("unknown determinism config '$name'")
            }
        }
    }

    private fun method(name: String, returnType: WrappedType, isConst: Boolean = false) =
        WrappedMethod(name, returnType, MethodType.METHOD).also { it.isConst = isConst }

    // Build the input model (the moral equivalent of the deleted test's parsed header):
    //   namespace fixture { class Dep { int depValue() const; };
    //                       class Widget { Widget(); int value() const;
    //                                      int value(int scale) const; const Dep& dep() const;
    //                                      [fixture::Nowhere unresolvable();] };
    //                       class Gadget { Gadget(); int size() const; }; }
    // Shaped to MUTATE during resolution (the `value()` overload pair, the `const Dep&`
    // accessor that pulls `Dep` in under INCLUDE_MISSING) so a non-idempotent reuse of
    // carried-over state has the best chance to surface. `Gadget` is a third independent
    // top-level class so G3(a) has a request set worth permuting.
    private fun buildModel(config: Config): WrappedTU {
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
            if (config.withUnresolvableMember) {
                // `fixture::Nowhere` is declared nowhere, so under IGNORE_MISSING this member
                // is dropped and the drop is ledgered — the run's ledger is non-empty.
                widget.addChild(method("unresolvable", WrappedType("fixture::Nowhere")))
            }
        }
        WrappedClass("Gadget").also { gadget ->
            ns.addChild(gadget)
            gadget.parent = ns
            gadget.addChild(
                WrappedConstructor("Gadget", WrappedType("fixture::Gadget"), false, true)
            )
            gadget.addChild(method("size", WrappedType("int"), isConst = true))
        }
        return tu
    }

    /**
     * One full deserialize -> resolve -> emit pass into a fresh output dir, returning every
     * generated file as relativePath -> content plus the drop-ledger report. Mirrors
     * `IndexedServiceImpl.writeTo`'s header/cpp/kotlin emission (the three real writers) but
     * omits the clang COMPILE + .def (which need the original headers on disk); determinism
     * of the emitted SOURCE is what the bootstrap reproducibility depends on.
     *
     * Installs a fresh [KrapperRun] built from [config], exactly as `IndexedServiceImpl` does
     * for every service call: since B4 that is what carries the intern cache, the root
     * package, the `-fno-rtti` flag and the drop ledger, so the ONLY thing that can travel
     * between two calls of this function is state that escaped the run.
     *
     * [filter] is the request the run was made with — G3(a) varies the ORDER of an
     * [AllowListFilter]'s names through it.
     */
    private suspend fun generateOnce(
        config: Config,
        modelJson: String,
        filter: FilterDefinition = DefaultFilter
    ): Map<String, String> = generateWith(runFor(config), config, modelJson, filter)

    /**
     * The [KrapperRun] a service would build for [config] — CONSTRUCTION ONLY, deliberately
     * separated from use.
     *
     * G3(b) constructs each run on the line before it installs it, so the sequence there is
     * `construct A -> use A -> construct B -> use B` and the two runs are never configured at
     * the same time. That is enough to catch a global with no reset at all, but a global that
     * is **reset at construction** — the actual pre-B4 shape, where `IndexedServiceImpl.init`
     * called `GenerationContext.reset(config.rootPackage, config.noRtti)` — is invisible to
     * it, because each construction happens after the previous run is already finished.
     * G3(c) exists to make the overlap real, and it needs this seam to do it.
     */
    private fun runFor(config: Config) =
        KrapperRun(GenerationContext(config.rootPackage, config.noRtti))

    /** Generate with an ALREADY-CONSTRUCTED [run]; see [generateOnce] for what it emits. */
    private suspend fun generateWith(
        run: KrapperRun,
        config: Config,
        modelJson: String,
        filter: FilterDefinition = DefaultFilter
    ): Map<String, String> {
        return KrapperRun.using(run) {
            val tu = ModelIo.decodeFromString(modelJson)
            val resolver = ParsedResolver(tu)
            val requests = resolver.findClasses(filter.wrapperFilter())
            val classes: List<ResolvedElement> = requests.resolveAll(resolver, config.policy)

            val outDir = File.createTempDir("krapper_determinism")
            try {
                val module = config.module
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
                KotlinWriter(config.kotlinPackage).generate(srcDir, classes)

                val files = mutableMapOf<String, String>()
                collect(outDir, outDir, files)
                // The drop ledger is run-scoped state the emitted sources do not reflect, so
                // it is compared alongside them (the `report` shape IndexedServiceImpl logs).
                files[ledgerEntry] = run.drops.report(
                    requested = requests.filterIsInstance<WrappedClass>()
                        .map { it.type.toString() }.sorted(),
                    bound = classes.filterIsInstance<ResolvedClass>().map { it.type.toString() }
                )
                files
            } finally {
                outDir.rmR()
            }
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

    // ---- transcript framing (parent <-> child) -------------------------------------------
    //
    // Length-prefixed so it round-trips content containing any delimiter: for each entry,
    // "<key>\n<charCount>\n<content>". Keys are emitted in sorted order.

    private fun encodeTranscript(files: Map<String, String>): String = buildString {
        for (key in files.keys.sorted()) {
            val content = files.getValue(key)
            append(key).append('\n').append(content.length).append('\n').append(content)
        }
    }

    private fun decodeTranscript(text: String): Map<String, String> {
        val files = mutableMapOf<String, String>()
        var index = 0
        while (index < text.length) {
            val keyEnd = text.indexOf('\n', index)
            check(keyEnd >= 0) { "truncated transcript at $index" }
            val key = text.substring(index, keyEnd)
            val lenEnd = text.indexOf('\n', keyEnd + 1)
            check(lenEnd >= 0) { "truncated transcript length for $key" }
            val length = text.substring(keyEnd + 1, lenEnd).toInt()
            val contentEnd = lenEnd + 1 + length
            check(contentEnd <= text.length) { "truncated transcript content for $key" }
            files[key] = text.substring(lenEnd + 1, contentEnd)
            index = contentEnd
        }
        return files
    }

    // ---- fresh-process baseline ----------------------------------------------------------

    /** This test binary's own path, so the parent can re-execute it as a child. */
    private fun selfExecutablePath(): String = memScoped {
        val size = 4096
        val buffer = allocArray<ByteVar>(size)
        val written = readlink("/proc/self/exe", buffer, (size - 1).toULong())
        assertTrue(
            written > 0,
            "readlink(/proc/self/exe) failed — G3(b) cannot produce a fresh-process baseline, " +
                "and a baseline taken in THIS process would share any leak it is meant to catch"
        )
        buffer[written] = 0
        buffer.toKString()
    }

    /**
     * Generate [config] in a genuinely fresh process: re-execute this test binary with the
     * child environment set and the runner filtered down to [CHILD_TEST_NAME], then read back
     * the transcript it wrote. Nothing else has generated anything in that process, so its
     * output cannot contain another config's leftovers by construction.
     */
    private fun freshProcess(config: Config): Map<String, String> {
        val exe = selfExecutablePath()
        val dir = File.createTempDir("krapper_determinism_fresh")
        try {
            val transcript = File(dir, "${config.name}.transcript")
            val log = File(dir, "${config.name}.log")
            val command = "$CHILD_CONFIG_ENV=${config.name} " +
                "$CHILD_OUT_ENV='${transcript.path}' " +
                "'$exe' --ktest_logger=SILENT --ktest_filter='*.$CHILD_TEST_NAME' " +
                "> '${log.path}' 2>&1"
            val status = system(command)
            val output = if (log.exists()) log.readText() else "<no output captured>"
            assertEquals(
                0,
                status,
                "the fresh-process baseline for config ${config.name} failed " +
                    "(status $status) running: $command\n$output"
            )
            assertTrue(
                transcript.exists(),
                "the fresh-process baseline for config ${config.name} wrote no transcript — " +
                    "the child entry point did not run, so the comparison would have been " +
                    "against nothing. Command: $command\n$output"
            )
            return decodeTranscript(transcript.readText())
        } finally {
            dir.rmR()
        }
    }

    private fun assertSameGeneration(
        expected: Map<String, String>,
        actual: Map<String, String>,
        what: String
    ) {
        assertTrue(expected.isNotEmpty(), "$what: the generation produced nothing to compare")
        assertEquals(expected.keys.sorted(), actual.keys.sorted(), "$what: different file set")
        for ((path, content) in expected) {
            assertEquals(content, actual[path], "$what: $path differs")
        }
    }

    // ---- tests ---------------------------------------------------------------------------

    /**
     * The child entry point. Inert in the parent's own run of the suite (the environment is
     * unset); in a child it generates the requested config and writes the transcript the
     * parent reads back.
     */
    @Test
    fun childFreshProcessBaseline(): Unit = runBlocking {
        val configName = getenv(CHILD_CONFIG_ENV)?.toKString() ?: return@runBlocking
        val out = getenv(CHILD_OUT_ENV)?.toKString()
            ?: error("$CHILD_CONFIG_ENV set without $CHILD_OUT_ENV")
        val config = Config.byName(configName)
        val files = generateOnce(config, ModelIo.encodeToString(buildModel(config)))
        File(out).writeText(encodeTranscript(files))
    }

    /** The original guard: the same config generated twice in one process is byte-identical. */
    @Test
    fun twoInProcessGenerationsAreByteIdentical(): Unit = runBlocking {
        val modelJson = ModelIo.encodeToString(buildModel(Config.A))
        val first = generateOnce(Config.A, modelJson)
        val second = generateOnce(Config.A, modelJson)

        assertSameGeneration(
            first,
            second,
            "two in-process runs of the same config — a process-global leaked across runs " +
                "(issue #11 hermeticity)"
        )
    }

    /**
     * **G3(a)** — the same request SET, presented in a different request ORDER, emits the
     * same bytes; and the two permutations run back-to-back in one process, so nothing
     * either of them leaves behind may change the other's output either.
     *
     * The order that reaches the writers is the parse tree's, not the request list's:
     * `findClasses` walks the TU and tests each element against the filter, and
     * `AllowListFilter` is a set membership test. That is what makes the emission order
     * (insertion order throughout the writers — design doc §1.5, "resolve order is emission
     * order") a function of the model rather than of how the build happened to enumerate its
     * requests. This pins it: a `findClasses` that iterated the allowlist instead of the tree
     * would leave the Gradle plugin's `requested = (...).distinct().sorted()` as the only
     * thing standing between a reordered build script and a different `krapped/` tree.
     */
    @Test
    fun shuffledRequestOrderIsByteIdentical(): Unit = runBlocking {
        val modelJson = ModelIo.encodeToString(buildModel(Config.A))
        val names = listOf("fixture::Dep", "fixture::Widget", "fixture::Gadget")
        val inOrder = generateOnce(Config.A, modelJson, AllowListFilter(names))
        val reversed = generateOnce(Config.A, modelJson, AllowListFilter(names.reversed()))
        // A rotation as well as a reversal: a reversal alone is satisfied by any
        // order-symmetric handling, a rotation moves a DIFFERENT name into first position.
        val rotated =
            generateOnce(Config.A, modelJson, AllowListFilter(names.drop(1) + names.take(1)))

        // Positive control: the allowlist must actually have selected all three classes. A
        // typo would empty every run alike and the equalities below would hold vacuously.
        val emitted = inOrder.getValue("/${Config.A.module}.cc")
        for (name in names) {
            assertTrue(
                name.replace("::", "_") in emitted,
                "the allowlist must select $name — otherwise this test compares empty runs"
            )
        }
        assertSameGeneration(inOrder, reversed, "allowlist reversed")
        assertSameGeneration(inOrder, rotated, "allowlist rotated")
    }

    /**
     * **G3(b)** — the brick's gate. Two different configs run back-to-back in ONE process,
     * each compared against a fresh-process run of the same config.
     *
     * Config A runs first and is the one with drops and no root package; config B follows
     * with a root package, `-fno-rtti`, a different reference policy and different
     * module/package names. Before B4 the two shared one `GenerationContext`, one
     * `DropLedger` and one set of `Parsing.kt` vars, and B could only start by destroying
     * A's — so anything A left in place that B did not overwrite became part of B's output.
     * Each side is measured against a process where the other config never ran, so a shared
     * leak cannot satisfy the comparison.
     */
    @Test
    fun twoConfigsInOneProcessMatchFreshProcesses(): Unit = runBlocking {
        val freshA = freshProcess(Config.A)
        val freshB = freshProcess(Config.B)

        // Sanity, measured on the FRESH baselines rather than on the in-process runs: the
        // two configs must actually generate different things, or the comparisons below
        // would hold no matter how badly state leaked between them. Asserting this on the
        // in-process pair would be self-defeating — a leak that made B look like A would
        // trip this precondition instead of the gate it is there to protect.
        assertTrue(
            freshA != freshB,
            "the two configs must generate different output for this gate to mean anything"
        )
        assertTrue(
            freshA.getValue(ledgerEntry) != freshB.getValue(ledgerEntry),
            "config A must drop something config B does not, so a leaked ledger is visible"
        )

        val inProcessA = generateOnce(Config.A, ModelIo.encodeToString(buildModel(Config.A)))
        val inProcessB = generateOnce(Config.B, ModelIo.encodeToString(buildModel(Config.B)))

        assertSameGeneration(
            freshA,
            inProcessA,
            "config A run first in-process vs. its own fresh process"
        )
        assertSameGeneration(
            freshB,
            inProcessB,
            "config B run AFTER config A in-process vs. its own fresh process — run-scoped " +
                "state leaked from A into B (docs/design/live-service.md §1.4, brick B4)"
        )
    }

    /**
     * **G3(c)** — INTERLEAVED LIFETIMES: two runs configured and alive AT THE SAME TIME, used
     * out of construction order, each still matching its own fresh-process baseline.
     *
     * This exists because G3(b), as the design doc originally specified it, does not gate what
     * B4 actually fixed. G3(b) runs `construct A -> use A -> construct B -> use B`, so no two
     * runs are ever configured at once — and the pre-B4 code satisfies that, because
     * `IndexedServiceImpl.init` reset the process globals AT CONSTRUCTION and each
     * construction happened after the previous run had finished. A shared global that is reset
     * per construction passes G3(b). It cannot pass this.
     *
     * The claim being gated is [GenerationContext]'s own: *two runs alive at once would
     * silently share an intern cache and a root package.* So both runs are built BEFORE either
     * generates, and then:
     *
     *  1. **B is used first, though A was constructed first.** Under a construction-time reset
     *     the shared state holds B's config here, which is the one ordering that would still
     *     look right — so it is the control, not the catch.
     *  2. **A is used second, long after B's construction overwrote the global.** This is the
     *     catch: a shared, construction-reset global makes A emit B's root package, and A
     *     diverges from its own fresh-process baseline.
     *  3. **B is used again, after A ran.** Re-entering a still-live run must not have been
     *     disturbed by the run that came between.
     *
     * Leg 3 compares the emitted FILES only. Its ledger legitimately differs: a ledger belongs
     * to the run, and a run used twice has recorded twice — that is accumulation WITHIN one
     * run, which is the ledger doing its job, not state crossing between runs.
     */
    @Test
    fun interleavedRunLifetimesMatchFreshProcesses(): Unit = runBlocking {
        val freshA = freshProcess(Config.A)
        val freshB = freshProcess(Config.B)
        assertTrue(
            freshA != freshB,
            "the two configs must generate different output for this gate to mean anything"
        )

        val modelA = ModelIo.encodeToString(buildModel(Config.A))
        val modelB = ModelIo.encodeToString(buildModel(Config.B))

        // BOTH runs configured before EITHER generates — the overlap G3(b) never creates.
        val runA = runFor(Config.A)
        val runB = runFor(Config.B)

        val bFirst = generateWith(runB, Config.B, modelB)
        val aSecond = generateWith(runA, Config.A, modelA)
        val bAgain = generateWith(runB, Config.B, modelB)

        assertSameGeneration(
            freshB,
            bFirst,
            "run B used first (constructed second) vs. its own fresh process"
        )
        assertSameGeneration(
            freshA,
            aSecond,
            "run A used SECOND, after run B was constructed and used, vs. its own fresh " +
                "process — the two runs were alive at the same time and A read B's state " +
                "(docs/design/live-service.md §1.4, brick B4). A shared global that is reset " +
                "per construction fails exactly here and passes G3(b)"
        )
        assertSameGeneration(
            bFirst.filterKeys { it != ledgerEntry },
            bAgain.filterKeys { it != ledgerEntry },
            "run B re-entered after run A generated in between — a live run was disturbed " +
                "by another run's use"
        )
    }
}
