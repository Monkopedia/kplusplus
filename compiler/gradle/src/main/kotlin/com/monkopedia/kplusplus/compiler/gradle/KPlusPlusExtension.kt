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
package com.monkopedia.kplusplus.compiler.gradle

import org.gradle.api.Action

/**
 * v2 Gradle DSL: `kplusplus { … }` on the consumer build script.
 *
 * Three categories of input:
 *   1. Imports — `header("foo.h")`, `headerDirectory("../include/")`,
 *      `library("/path/to/libfoo.a")`. Required only when binding a header
 *      set (e.g. v8). The pure template-instantiation flow (cppVector<T>()
 *      → std::vector<T>) needs neither.
 *   2. Fixups — `fixup { removeMethod(...); stripConstFromReturnType(...); … }`
 *      Narrow escape hatch for the semantic corrections v2 can't derive
 *      from the call site. Each directive is a small declarative datum
 *      forwarded to krapper_gen via --fixup-file.
 *   3. (future) Per-spec compiler options, package overrides, etc.
 *
 * This block intentionally does not re-introduce the v1 mapping DSL. If
 * you need arbitrary FIR introspection or per-method closures, that's a
 * signal to either (a) extend the [Fixup] sealed type with a new narrow
 * directive or (b) drop back to the legacy `com.monkopedia.kplusplus.plugin`
 * (which is still maintained for backwards compatibility).
 */
open class KPlusPlusExtension {

    internal val headers: MutableList<String> = mutableListOf()
    internal val headerDirectories: MutableList<String> = mutableListOf()
    internal val libraries: MutableList<String> = mutableListOf()
    internal val fixups: MutableList<FixupDirective> = mutableListOf()
    internal val instantiations: MutableList<String> = mutableListOf()

    /**
     * Optional override for the C++ compiler krapper_gen uses to build the
     * generated wrapper library. Default: try the konan-bundled
     * x86_64-unknown-linux-gnu-g++ first (gcc 8.3 / glibcxx that the
     * generated wrapper code targets), then fall back to clang++ on PATH.
     * Set this if your host has a usable g++/clang++ at a known path and
     * the autodetection picks the wrong one (e.g. host gcc 16+ chokes on
     * private libstdc++ identifiers v8 headers exercise).
     */
    var compiler: String? = null

    /**
     * C++ standard krapper_gen parses headers under (e.g. "c++17", "c++20").
     * Forwarded to krapper_gen as `--std`. Null leaves krapper_gen's default
     * (c++14). Set to "c++17" for std::string_view, "c++20" for char8_t, etc.
     */
    var cppStandard: String? = null

    /**
     * Root Kotlin package for the generated bindings (e.g. "com.acme.app").
     * Forwarded to krapper_gen as `--root-package`; every binding's package
     * becomes `<rootPackage>.<C++ namespace path>` (so `std::vector<int>` lands
     * in `<rootPackage>.std`). Null leaves the historical layout (top-level
     * types in `root`, C++ namespaces in their bare path like `std`/`geo`).
     */
    var rootPackage: String? = null

    /**
     * Reference policy for types referenced-but-not-listed (forwarded to krapper_gen
     * as `-r`/`--referencePolicy`). One of `INCLUDE_MISSING` (default — recursively bind
     * referenced types), `IGNORE_MISSING` (drop them), `OPAQUE_MISSING` (bind as opaque
     * pointers), `THROW_MISSING`. For a SCOPED import of a large library (see [only]),
     * `IGNORE_MISSING` is what keeps the bound surface to the allowlist instead of
     * exploding into the whole transitive closure. Null leaves the historical
     * `INCLUDE_MISSING`.
     */
    var referencePolicy: String? = null

    internal val only: MutableList<String> = mutableListOf()
    internal var onlyFile: String? = null

    /**
     * Scoped-import allowlist (forwarded to krapper_gen as `--only`): the
     * fully-qualified class names to bind, e.g. `only("clang::Decl", "clang::CXXRecordDecl")`.
     * Only listed classes are fully bound; types they reference but that aren't listed fall
     * to [referencePolicy]. When neither [only] nor [onlyFile] is set, every non-std class
     * in the header set is bound. Essential for binding a slice of a large library (Clang,
     * LLVM) without dragging in its whole surface.
     */
    fun only(vararg names: String) {
        only += names
    }

    /**
     * Like [only], but reads the allowlist from a file (one fully-qualified class name per
     * line; blank/`#` lines ignored), forwarded as `--only-file`. Merged with any [only]
     * entries. Path resolved relative to projectDir.
     */
    fun onlyFile(path: String) {
        onlyFile = path
    }

    /** Add a header file (path resolved relative to projectDir). */
    fun header(path: String) {
        headers += path
    }

    /** Add a directory that contains headers, mirroring `-I` to the compiler. */
    fun headerDirectory(path: String) {
        headerDirectories += path
    }

    /** Add a library (.a / .so) to link the generated wrapper against. */
    fun library(path: String) {
        libraries += path
    }

    /**
     * Seed a C++ template instantiation to generate eagerly (e.g.
     * `instantiate("Box<int>")`). Needed to bootstrap a USER template whose facade
     * is itself generated — until it exists `Box<Int>()` can't resolve, so the
     * compiler can't request it via SYNC_REQUIRED. Unioned with requested.txt.
     */
    fun instantiate(spec: String) {
        instantiations += spec
    }

    /** Register one or more narrow per-binding-spec fixups. */
    fun fixup(configure: Action<FixupSpec>) {
        val spec = FixupSpec()
        configure.execute(spec)
        fixups += spec.collected
    }
}

/**
 * Builder for the `kplusplus { fixup { ... } }` block. Methods correspond
 * one-to-one with the Fixup sealed subclasses in krapper_gen's commonMain.
 *
 * Currently supported (matches the v8 example's needs):
 *   * [removeMethod] — drop a method by its generated uniqueCName.
 *   * [stripConstFromReturnType] — strip leading `const ` from return
 *     types whose typestring starts with the supplied prefix.
 *   * [scriptOriginOptionsFix] — the v8::ScriptOrigin::options
 *     return-type-and-style cleanup.
 *   * [addUniquePtrGet] — emit a custom `get()` for every std::unique_ptr<>
 *     binding.
 *
 * What is NOT yet supported (gaps a future M12.x can close):
 *   * Adding arbitrary custom methods beyond the unique_ptr case.
 *   * Rewriting argument types.
 *   * Operator overload renames.
 *   * Per-element-type compile-time predicates richer than "starts with".
 *
 * If you hit one of these, prefer extending [Fixup] (in krapper_gen's
 * commonMain) and [FixupApplier] (nativeMain) with a new directive
 * rather than reintroducing arbitrary closures.
 */
class FixupSpec {

    internal val collected: MutableList<FixupDirective> = mutableListOf()

    fun removeMethod(uniqueCName: String) {
        collected += FixupDirective.RemoveMethodByCName(uniqueCName)
    }

    fun stripConstFromReturnType(matchPrefix: String) {
        collected += FixupDirective.StripConstFromReturnType(matchPrefix)
    }

    fun scriptOriginOptionsFix() {
        collected += FixupDirective.ScriptOriginOptionsFix
    }

    fun addUniquePtrGet() {
        collected += FixupDirective.AddUniquePtrGet
    }
}

/**
 * Gradle-side mirror of krapper_gen's sealed [com.monkopedia.krapper.Fixup]
 * type. Kept here as a separate sealed hierarchy so the gradle plugin
 * doesn't depend on krapper_gen's runtime classes (the plugin runs on the
 * JVM build classpath; krapper_gen is a Kotlin/Native binary).
 *
 * [toJson] produces a JSON object that round-trips through kotlinx
 * serialization on the krapper_gen side using the polymorphic serializer
 * for [com.monkopedia.krapper.Fixup]. The discriminator `"type"` and the
 * class shortnames match what kotlinx.serialization emits.
 */
internal sealed class FixupDirective {

    abstract fun toJson(): String

    data class RemoveMethodByCName(val uniqueCName: String) : FixupDirective() {
        override fun toJson(): String =
            """{"type":"com.monkopedia.krapper.RemoveMethodByCName",""" +
                """"uniqueCName":${quote(uniqueCName)}}"""
    }

    data class StripConstFromReturnType(val matchPrefix: String) : FixupDirective() {
        override fun toJson(): String =
            """{"type":"com.monkopedia.krapper.StripConstFromReturnType",""" +
                """"matchPrefix":${quote(matchPrefix)}}"""
    }

    object ScriptOriginOptionsFix : FixupDirective() {
        override fun toJson(): String =
            """{"type":"com.monkopedia.krapper.ScriptOriginOptionsFix"}"""
    }

    object AddUniquePtrGet : FixupDirective() {
        override fun toJson(): String =
            """{"type":"com.monkopedia.krapper.AddUniquePtrGet"}"""
    }

    companion object {
        // Minimal JSON string escape — fine for the inputs we accept here
        // (C++ identifier-shaped names with `<`, `>`, `:`, `_`). Quotes
        // and backslashes still get escaped defensively.
        private fun quote(s: String): String {
            val escaped = s.replace("\\", "\\\\").replace("\"", "\\\"")
            return "\"$escaped\""
        }
    }
}

internal fun List<FixupDirective>.toJsonArray(): String =
    joinToString(prefix = "[", postfix = "]", separator = ",") { it.toJson() }
