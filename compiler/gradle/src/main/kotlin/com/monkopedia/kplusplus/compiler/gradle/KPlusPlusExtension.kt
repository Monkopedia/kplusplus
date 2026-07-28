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

import com.monkopedia.krapper.AddUniquePtrGet
import com.monkopedia.krapper.Fixup
import com.monkopedia.krapper.RemoveMethodByCName
import com.monkopedia.krapper.ScriptOriginOptionsFix
import com.monkopedia.krapper.StripConstFromReturnType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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
 *      forwarded to krapper via --fixup-file.
 *   3. (future) Per-spec compiler options, package overrides, etc.
 *
 * This block intentionally does not re-introduce the v1 mapping DSL (the v1
 * `com.monkopedia.kplusplus.plugin` was removed). If you need a semantic
 * correction the current fixups can't express, extend the [Fixup] sealed type
 * with a new narrow directive rather than reaching for arbitrary closures.
 */
open class KPlusPlusExtension {

    internal val headers: MutableList<String> = mutableListOf()
    internal val headerDirectories: MutableList<String> = mutableListOf()
    internal val libraries: MutableList<String> = mutableListOf()
    internal val fixups: MutableList<Fixup> = mutableListOf()
    internal val instantiations: MutableList<String> = mutableListOf()

    /**
     * Optional override for the C++ compiler krapper uses to build the
     * generated wrapper library. Default: try the konan-bundled
     * x86_64-unknown-linux-gnu-g++ first (gcc 8.3 / glibcxx that the
     * generated wrapper code targets), then fall back to clang++ on PATH.
     * Set this if your host has a usable g++/clang++ at a known path and
     * the autodetection picks the wrong one (e.g. host gcc 16+ chokes on
     * private libstdc++ identifiers v8 headers exercise).
     */
    var compiler: String? = null

    /**
     * C++ standard krapper parses headers under (e.g. "c++17", "c++20").
     * Forwarded to krapper as `--std`. Null leaves krapper's default
     * (c++14). Set to "c++17" for std::string_view, "c++20" for char8_t, etc.
     */
    var cppStandard: String? = null

    /**
     * The target C++ library is built `-fno-rtti` (e.g. v8's monolith) and exports no
     * `typeinfo` symbols. Forwarded to krapper as `--no-rtti`: the generated generic
     * `dynamic_cast<D*>` down-cast helpers (`Base.asDerived()`) would reference a missing
     * `typeinfo for Derived` and fail to LINK, so they are skipped (only RTTI-free LLVM
     * `classof` down-casts survive). Default false (RTTI-on library keeps the generic
     * checked down-casts).
     */
    var noRtti: Boolean = false

    /**
     * Root Kotlin package for the generated bindings (e.g. "com.acme.app").
     * Forwarded to krapper as `--root-package`; every binding's package
     * becomes `<rootPackage>.<C++ namespace path>` (so `std::vector<int>` lands
     * in `<rootPackage>.std`). Null leaves the historical layout (top-level
     * types in `root`, C++ namespaces in their bare path like `std`/`geo`).
     */
    var rootPackage: String? = null

    /**
     * Reference policy for types referenced-but-not-listed (forwarded to krapper
     * as `-r`/`--referencePolicy`). One of `INCLUDE_MISSING` (default — recursively bind
     * referenced types), `IGNORE_MISSING` (drop them), `OPAQUE_MISSING` (bind as opaque
     * pointers), `THROW_MISSING`. For a SCOPED import of a large library (see [only]),
     * `IGNORE_MISSING` is what keeps the bound surface to the allowlist instead of
     * exploding into the whole transitive closure. Null leaves the historical
     * `INCLUDE_MISSING`.
     */
    var referencePolicy: String? = null

    /**
     * Path to the consumer's `llvm-config` (#124, #128 R4). The cpp front-end
     * (`kpp.frontend.<module>=cpp`) parses your headers with a bundled Clang. Clang's
     * OWN builtin headers (stddef.h etc.) are located by krapper via
     * `clang++ -print-resource-dir` on `PATH`; but a library's headers under a
     * NON-default-path LLVM install (e.g. apt.llvm.org under `/usr/lib/llvm-22/include`,
     * where `<clang/AST/...>` lives) are only found if explicitly on the include path.
     * When left null the plugin auto-discovers via `llvm-config` on `PATH` (mirroring
     * the root `settings.gradle.kts` probe) and honors `-PllvmConfig=<path>`; set this
     * to point at a specific install's `llvm-config`, and the plugin runs
     * `<llvmConfig> --includedir` and threads that dir into the parse as a `-I`. Takes
     * precedence over the `-PllvmConfig` property.
     */
    var llvmConfig: String? = null

    internal val only: MutableList<String> = mutableListOf()
    internal var onlyFile: String? = null

    /**
     * Scoped-import allowlist (forwarded to krapper as `--only`): the
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
 * one-to-one with the Fixup sealed subclasses in krapper's commonMain.
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
 * If you hit one of these, prefer extending [Fixup] (in krapper's
 * commonMain) and [FixupApplier] (nativeMain) with a new directive
 * rather than reintroducing arbitrary closures.
 */
class FixupSpec {

    internal val collected: MutableList<Fixup> = mutableListOf()

    fun removeMethod(uniqueCName: String) {
        collected += RemoveMethodByCName(uniqueCName)
    }

    fun stripConstFromReturnType(matchPrefix: String) {
        collected += StripConstFromReturnType(matchPrefix)
    }

    fun scriptOriginOptionsFix() {
        collected += ScriptOriginOptionsFix
    }

    fun addUniquePtrGet() {
        collected += AddUniquePtrGet
    }
}

/**
 * The fixup list as JSON — the sync task's up-to-date key.
 *
 * #185 removed a hand-written `FixupDirective` mirror of krapper's sealed [Fixup] (and its
 * hand-rolled JSON emitter) that existed only because the plugin could not reference
 * krapper's classes. It now compiles them from the same source krapper does, so this is the
 * real serializer rather than a mirror that could silently drift from it.
 */
internal fun List<Fixup>.toJsonArray(): String =
    Json.encodeToString(ListSerializer(Fixup.serializer()), this)
