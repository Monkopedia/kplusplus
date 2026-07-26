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
package com.monkopedia.krapper.parser

import com.monkopedia.krapper.generator.model.ModelIo
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import kotlin.system.exitProcess
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kppbridge.buildASTWithArgs
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

/**
 * THE C++ FRONT-END (#184). Parses C++ source with Clang's C++ AST — on kplusplus-generated
 * `libclang-cpp` bindings, i.e. the self-hosted part — and constructs the pure parse-output
 * [WrappedTU] the resolve/codegen pipeline consumes.
 *
 * This used to be a SEPARATE `krapper_parse` binary that handed its tree to `krapper_gen` as
 * ModelIo JSON through a temp file (`--parity-emit` -> `--parsedModel`). The two tools are one
 * binary now, so the handoff is a plain in-process object; ModelIo JSON survives only as the
 * optional `--dump-model` debug dump.
 */
object CppParser {

    /**
     * The driver arguments every parse runs under. The tooling driver can't compute its own
     * resource dir (its "binary" is the virtual `clang-tool`), so the builtin headers
     * (`<stddef.h>` and friends, behind `<vector>`) only resolve with an explicit
     * `-resource-dir`; system C++ headers are found by the driver's normal GCC detection.
     * [includeDirs] are the module's `kplusplus { headerDirectory(...) }` roots, so
     * cross-directory quote-includes resolve exactly as the generated wrapper's compile sees
     * them. `\n`-joined — the `kppbridge.buildASTWithArgs` transport (see clang_slice.h).
     */
    fun driverArgs(std: String, includeDirs: List<String>): String = (
        listOf("-std=$std", "-resource-dir=$resourceDir") + includeDirs.map { "-I$it" }
        ).joinToString("\n")

    // Probed once per process: a run parses the header plus one forcing TU per --instantiate,
    // and the answer can't change under it.
    private val resourceDir by lazy { commandOutput("clang++ -print-resource-dir") }

    /**
     * Parse [code] as the virtual C++ file [filename] with the `\n`-joined driver [args] and
     * construct the [WrappedTU].
     *
     * The filename must spell a C++ extension: the driver infers the language from it, and a
     * `.h` parses as C (where `int area() const` is ill-formed and a struct is a plain
     * RecordDecl the CXXRecordDecl walk never sees).
     *
     * Each parse gets its own [memScoped]: the wrappers around Clang's AST objects register
     * their destructors there, so the whole `ASTUnit` is released when the parse returns. The
     * constructed [WrappedTU] is pure Kotlin data and outlives it.
     */
    fun parse(code: String, filename: String, args: String): WrappedTU = memScoped {
        buildWrappedTU(
            buildASTWithArgs(code, filename, args)
                ?.getASTContext()
                ?.getTranslationUnitDecl()
                ?: fail("clang could not build an AST for $filename")
        )
    }
}

// ---- #101: the NONCOPYABLE special-member determinism guard --------------------------------

// A self-contained reduction of v8::Persistent's exact shape: a NON-COPYABLE base (`= delete`d
// copy ctor + copy assignment), a traits class whose `Copy` is a `static_assert(sizeof(T) < 0)`
// (un-instantiable on use), and the NC template that USER-DECLARES a copy ctor + copy assignment
// whose bodies call the trap. The `ForceNC` struct's `value` member implicitly INSTANTIATES
// `NC<Value>` — the same trigger the synthesized forcing header (ForcingHeader) is for
// `v8::Persistent<v8::Value>`.
//
// THE INVARIANT THIS LOCKS (#101): the special-member set of a NonCopyable type must be
// IDENTICAL across parses. The original report saw `Persistent<v8::Value>` surface its copy
// ctor on one parse and its copy assignment on another. The root cause that COULD produce that
// is lazy template-member instantiation: Clang instantiates a specialization's member FUNCTIONS
// on first use, so an implicitly-instantiated specialization's `decls()` carries only what THIS
// parse happened to trigger — a parse-dependent set. This front-end is immune BY CONSTRUCTION
// because it never walks the specialization (ModelBuilder.addContextDecls SKIPS
// ClassTemplateSpecialization, citing #101): NC's members ride the template PATTERN, whose
// `decls()` are fixed source-order declarations, and the resolver materializes `NC<Value>` by
// deterministic param substitution (Parsing.typedAs). This guard parses the fixture repeatedly
// and asserts the pattern's special-member set never varies — so a future change that started
// walking the lazily-instantiated specialization would fail here.
private val NONCOPYABLE_FIXTURE = """
    struct Value { int x; };

    template <class T>
    class NCBase {
     public:
        void reset();
        bool isEmpty() const { return ptr_ == nullptr; }
        NCBase(const NCBase&) = delete;
        void operator=(const NCBase&) = delete;
     protected:
        explicit NCBase(T* p) : ptr_(p) {}
        T* ptr_;
    };

    template <class T>
    struct NonCopyableTraits {
        template <class S>
        static void Copy(const S* from, S* to) {
            static_assert(sizeof(T) < 0, "NonCopyableTraits::Copy is not instantiable");
        }
    };

    template <class T, class M = NonCopyableTraits<T> >
    class NC : public NCBase<T> {
     public:
        NC() : NCBase<T>(nullptr) {}
        NC(const NC& that) : NCBase<T>(nullptr) { M::Copy(that.ptr_, this->ptr_); }
        NC& operator=(const NC& that) { M::Copy(that.ptr_, this->ptr_); return *this; }
        ~NC() {}
        int get() const;
    };

    struct ForceNC { NC<Value> value; };
""".trimIndent()

// Parse the NonCopyable fixture this many times; any cross-parse divergence fails the guard.
private const val NONCOPYABLE_PARSE_COUNT = 8

/**
 * Parse [NONCOPYABLE_FIXTURE] [NONCOPYABLE_PARSE_COUNT] times (each a SEPARATE parse /
 * ASTContext) and assert:
 *  1. every parse's full serialized model is byte-identical — the front-end is deterministic
 *     parse-to-parse for a NonCopyable type (the exact #101 acceptance);
 *  2. the `NC` template PATTERN carries BOTH special members on EVERY parse — a copy
 *     constructor AND a copy assignment — so the set is COMPLETE and never the report's "one
 *     parse the ctor, another the assignment".
 *
 * Note this guard is at the FRONT-END (model) boundary on purpose: the un-instantiable copy ops
 * are a body-only `static_assert`, invisible to every copy-constructibility AST query
 * (`isDeleted()` / `is_copy_constructible` are FALSE/true respectively — the trap only fires if
 * the body is instantiated), so the front-end cannot faithfully SUPPRESS them; it can only emit
 * them DETERMINISTICALLY (the consumer drops the un-compilable pair by uniqueCName).
 *
 * Returns the number of failed checks (0 = pass); the caller turns that into an exit code.
 */
fun noncopyableDeterminismCheck(): Int {
    var failures = 0
    fun check(name: String, pass: Boolean, detail: String = "") {
        if (!pass) failures++
        val suffix = if (detail.isEmpty()) "" else " — $detail"
        println("  [${if (pass) "PASS" else "FAIL"}] $name$suffix")
    }

    val args = CppParser.driverArgs(std = "c++17", includeDirs = emptyList())
    val models = (1..NONCOPYABLE_PARSE_COUNT).map { i ->
        CppParser.parse(NONCOPYABLE_FIXTURE, "noncopyable_$i.cc", args)
    }
    val jsons = models.map { ModelIo.encodeToString(it) }
    val divergent = jsons.withIndex().filter { it.value != jsons.first() }.map { it.index + 1 }
    check(
        "$NONCOPYABLE_PARSE_COUNT parses of the NonCopyable fixture are byte-identical (#101)",
        divergent.isEmpty(),
        "parse(s) $divergent diverged from parse 1"
    )
    // The special-member set of the NC PATTERN, per parse: the copy-ctor + copy-assignment
    // signal that survives substitution into NC<Value>. Both must be present on EVERY parse.
    fun specialMembers(tu: WrappedTU): Pair<Boolean, Boolean> {
        val members = tu.children.filterIsInstance<WrappedTemplate>()
            .find { it.name == "NC" }?.children.orEmpty()
        val hasCopyCtor = members.filterIsInstance<WrappedConstructor>().any {
            it.isCopyConstructor
        }
        val hasCopyAssign = members.filterIsInstance<WrappedMethod>()
            .any { it !is WrappedConstructor && it !is WrappedDestructor && it.name == "operator=" }
        return hasCopyCtor to hasCopyAssign
    }
    val sets = models.map { specialMembers(it) }
    check(
        "NC pattern carries BOTH copy ctor and copy assignment on every parse (#101)",
        sets.all { it == (true to true) },
        "per-parse (copyCtor, copyAssign): $sets"
    )
    // Sanity: the fixture really did parse (an empty TU would pass both checks vacuously).
    val first = models.first()
    check(
        "the fixture parsed (the NCBase/NC templates and the ForceNC record are present)",
        first.children.filterIsInstance<WrappedTemplate>().map { it.name }
            .containsAll(listOf("NCBase", "NC")) &&
            first.children.filterIsInstance<WrappedClass>().any { it.name == "ForceNC" }
    )
    println(
        if (failures == 0) {
            "krapper: NONCOPYABLE DETERMINISM CHECK PASSED ($NONCOPYABLE_PARSE_COUNT parses)"
        } else {
            "krapper: $failures NonCopyable-determinism check(s) FAILED"
        }
    )
    return failures
}

// ---- shared helpers ------------------------------------------------------------------------

/** First line of [cmd]'s stdout (popen), trimmed. Used for `clang++ -print-resource-dir`. */
private fun commandOutput(cmd: String): String = memScoped {
    val fp = popen(cmd, "r") ?: fail("popen failed: $cmd")
    val buffer = allocArray<ByteVar>(BUFFER_SIZE)
    val line = fgets(buffer, BUFFER_SIZE, fp)?.toKString().orEmpty().trim()
    pclose(fp)
    if (line.isEmpty()) fail("no output from: $cmd") else line
}

private const val BUFFER_SIZE = 1024

private fun fail(message: String): Nothing {
    println("krapper: parse FAIL — $message")
    exitProcess(1)
}
