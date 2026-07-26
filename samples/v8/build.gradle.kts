/*
 * Copyright 2021 Jason Monk
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
import com.monkopedia.klinker.klinkedExecutable

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}
plugins {
    kotlin("multiplatform") version "2.4.0"
    // v2: the compiler subplugin replaces the legacy mapping-DSL plugin.
    // Headers / library / fixups live in the narrow `kplusplus { ... }`
    // block at the bottom of this file; nothing else here changed.
    id("com.monkopedia.kplusplus.compiler")
    id("com.monkopedia.klinker.plugin") version "0.2.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

// ---- v8 brick 3 (#99): LINK the executable against the system libstdc++ ----
// PORT of featuregen's #78 recipe (see featuregen/build.gradle.kts). The cpp front-end
// (kpp.frontend.v8=cpp) parses v8 against the SYSTEM libstdc++ (gcc-16 here),
// so the faithful generated wrapper references newer-libstdc++ internals that K/N's
// BUNDLED gcc-8.3 sysroot (caps at GLIBCXX_3.4.25) cannot resolve. The system libstdc++
// HAS those symbols and is backward-ABI-compatible, so point K/N's own linker at the REAL
// system libstdc++.so by ABSOLUTE PATH (NOT -lstdc++ — konan's bundled-sysroot -L is
// searched first, so `-lstdc++` would shadow to the OLD lib and the newer symbols would
// stay undefined; an absolute path can't be shadowed by search order). The .so is
// DISCOVERED at configure time from `clang++ -print-file-name=libstdc++.so` (a GNU-ld
// linker script/symlink), canonicalized to the real shared object — host-portable, not
// hardcoded. This is the SAME mechanism clangwalk/krapper/featuregen already use for
// their klinked binaries; here it is applied to the example's EXECUTABLE binary.
val systemStdcxxSo: File = run {
    val proc = ProcessBuilder("clang++", "-print-file-name=libstdc++.so")
        .redirectErrorStream(true).start()
    val out = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    proc.waitFor()
    File(out).canonicalFile.takeIf { it.isFile }
        ?: error(
            "#99: could not resolve the system libstdc++.so from " +
                "`clang++ -print-file-name=libstdc++.so` (got: '$out')"
        )
}
val systemStdcxxLibDir = systemStdcxxSo.parentFile.absolutePath

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget =
        when {
            hostOs == "Mac OS X" -> macosX64("native")

            hostOs == "Linux" -> linuxX64("native")

            hostOs.startsWith("Windows") -> mingwX64("native")

            else -> throw GradleException(
                "Host OS '$hostOs' is not supported in Kotlin/Native $project.",
            )
        }

    hostTarget.apply {
        binaries {
            klinkedExecutable {
                // The v8 monolith link itself comes from the kplusplus { library(...) }
                // entry below. Here we add the system-libstdc++ link recipe (see the
                // block above the `kotlin {` for the full WHY): the REAL libstdc++.so by
                // ABSOLUTE PATH (replaces -lstdc++, which would shadow to konan's old
                // bundled lib), plus dead-strip + shlib-undefined relaxation + rpath.
                linkerOpts(
                    systemStdcxxSo.absolutePath,
                    // Dead-strip the stale platform.linux cinterop cache (K/N emits one
                    // section per wrapper for exactly this) — the same trick the klinked
                    // clang consumers use (see clangwalk/build.gradle.kts).
                    "--gc-sections",
                    // The system libstdc++.so.6 in turn references NEWER glibc symbols
                    // (pthread_*@GLIBC_2.34, __isoc23_*@GLIBC_2.38, …) that konan's bundled
                    // old-sysroot glibc stub doesn't define; they are resolved at RUNTIME by
                    // the host glibc. This ONLY relaxes undefined refs coming from SHARED
                    // LIBRARIES — the generated wrapper archive's own symbols (and the v8
                    // monolith archive) stay fully checked, so a genuinely missing binding
                    // symbol still fails the link.
                    "--allow-shlib-undefined",
                    // Load the system .so at runtime: its SONAME libstdc++.so.6 wins over
                    // the bundled one via the rpath.
                    "-rpath",
                    systemStdcxxLibDir,
                    "-rpath-link",
                    systemStdcxxLibDir
                )
                // -lstdc++ REMOVED (it shadowed to the old bundled lib — the exact #78 bug);
                // the absolute-path .so above replaces it. Keep -lm/-lpthread.
                compilerOpts("-lm", "-lpthread")
                runTask()
            }
        }
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                    freeCompilerArgs.add("-g")
                }
            }
        }
    }
    sourceSets["nativeMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
}

// v2: the entire kplusplus configuration. Compare with the v1 import { map { … } }
// block (lines 86-208 of the pre-M12 build.gradle.kts) — that one enumerated
// the v8 import and then re-implemented four semantic fixups as arbitrary
// Kotlin closures running against the krapper IR model. Same four fixups
// here, expressed declaratively. No Kotlin closures, no krapper-IR imports.
kplusplus {
    header("include/v8-combined.h")
    headerDirectory("include/")
    library("libv8_monolith.a")
    // v8 needs C++17 (the default is c++14); the cpp front-end parses AND the wrapper
    // compiles under this standard. The frontend flip itself is in gradle.properties
    // (kpp.frontend.v8=cpp); an LLVM 22 toolchain must be installed.
    cppStandard = "c++17"

    // v8 brick 2 (#99): force the v8::Maybe<int> model so Value::Int32Value(context) — whose
    // return is v8::Maybe<int32_t> (int32_t == int) — survives codegen instead of being
    // dropped (IGNORE_MISSING) for want of a forcing model. Unblocks the richer number→int
    // demo in Main.kt (`numResult.Int32Value(context).FromJust()`). The Maybe<int> member
    // set (FromJust/ToChecked/IsJust/…) is the primary Maybe<T> projection — no void-spec
    // workarounds apply here.
    instantiate("v8::Maybe<int>")

    // v8 brick 3 (#99): the v8 monolith is built `-fno-rtti`, so it exports no `typeinfo`
    // symbols. Without this, the generated generic `dynamic_cast<D*>` down-cast helpers
    // (e.g. TracingController.asTracingController(), ExternalStringResourceBase.as*()) emit
    // `undefined reference to typeinfo for v8::...` at LINK time. noRtti skips those generic
    // checked down-casts (none are on the hello-world path); RTTI-free LLVM `classof`
    // down-casts, if any, are unaffected.
    noRtti = true

    fixup {
        // v8::ScriptOrigin::options returns a stale `const ScriptOriginOptions*`
        // in the generated binding; consumers use the value by-copy. Built-in
        // because the rewrite (strip const + trim trailing * + flip return
        // style to COPY_CONSTRUCTOR) is specific to this exact callee.
        scriptOriginOptionsFix()

        // The five v8 wrapper-type families return `const v8::Foo<...>` from
        // many methods; the leading const breaks the generated Kotlin
        // bindings because the underlying type is value-semantic.
        stripConstFromReturnType("v8::Local<")
        stripConstFromReturnType("v8::Maybe<")
        stripConstFromReturnType("v8::MaybeLocal<")
        stripConstFromReturnType("v8::ScriptOrigin<")
        stripConstFromReturnType("v8::Location<")

        // v8 methods that generate uncompilable C++ wrappers; drop them by uniqueCName.
        // v8::Persistent<v8::Value> is NonCopyable (NonCopyablePersistentTraits::Copy is a
        // `static_assert(sizeof(S) < 0)`), so neither its copy ctor nor its copy-assignment
        // may be instantiated — the generated wrapper that copy-constructs / assigns into the
        // Holder buffer fails to compile. Both members are USER-DECLARED on the Persistent
        // template (NOT implicit), so the cpp front-end emits them DETERMINISTICALLY (#101):
        // it walks the template PATTERN — never the lazily-instantiated specialization — so
        // BOTH always surface on every parse (proven by the krapper
        // `--noncopyable-determinism` guard). The un-instantiability is body-only, invisible
        // to copy-constructibility AST queries, so the front-end can't auto-suppress them;
        // drop both here by uniqueCName (`op_assign` is the libclang-era name, still valid).
        removeMethod("v8_Persistent_v8_Value_new__const_v8_Persistent_and")
        removeMethod("v8_Persistent_v8_Value_op_assign")
        removeMethod(
            "v8_platform_tracing_TraceWriter_create_system_instrumentation_trace_writer"
        )

        // v8::Maybe<void> is an explicit specialization carrying only IsJust/IsNothing/==/!=,
        // but the front-end projects the primary Maybe<T> member set onto it, so these five
        // reference members that don't exist on the void specialization (ToChecked/Check/To/
        // FromJust/FromMaybe — the last also takes a `void` value). Drop them.
        removeMethod("v8_Maybe_void_to_checked")
        removeMethod("v8_Maybe_void_check")
        removeMethod("v8_Maybe_void_to")
        removeMethod("v8_Maybe_void_from_just")
        removeMethod("v8_Maybe_void_from_maybe")

        // v8 brick 3 (#99): these methods generated Kotlin bindings that DON'T COMPILE
        // (brick 1 only proved the C++ WRAPPER compiles; brick 3 is the first to compile +
        // link the Kotlin). They fall into two latent codegen-bug families the v8 surface
        // exposed (#103); none are on the hello-world path.
        //
        // Family 1 — OUT-POINTER PARAM modeled as a value: a `T* out` / `T& out` parameter
        // (enum*, size_t&, size_t*) was bound as a by-value param, so the call passed a
        // scalar where the extern's `CValuesRef<...>` pointer is expected. FIXED at source
        // in krapper (#103, `typeToKotlinType`: `size_t*`/`size_t&` -> `CValuesRef<
        // ULongVar>?`, `enum*` -> the `void*`-backed `COpaquePointer`), so the six
        // Maybe::To / GetExternalStringResourceBase / GetCodeRange / GetEmbeddedCodeRange /
        // TraceBufferChunk::AddTraceEvent / ReallocateBufferMemory workarounds are dropped.
        //
        // Family 2 — REFERENCE-RETURN of a value-reduced type: an `operator|=` / `operator&=`
        // / `operator^=` returning `T&` (where T reduces to a Kotlin enum or UByte) is bound
        // as a by-value return, but the extern returns the reference as a pointer, so the
        // generated body feeds a CPointer into `fromValue(Int)` / a UByte slot. These are the
        // bitmask compound-assignment operators that leak in from libstdc++ <ios> (std::byte
        // and the _Ios_* flag types) — pure transitive-include noise, unused by the example.
        removeMethod("std_operator_oreq")   // std::byte& operator|=
        removeMethod("std_operator_andeq")  // std::byte& operator&=
        removeMethod("std_operator_xoreq")  // std::byte& operator^=
        removeMethod("std_operator_oreq__std__Ios_Fmtflags_and_std__Ios_Fmtflags")
        removeMethod("std_operator_andeq__std__Ios_Fmtflags_and_std__Ios_Fmtflags")
        removeMethod("std_operator_xoreq__std__Ios_Fmtflags_and_std__Ios_Fmtflags")
        removeMethod("std_operator_oreq__std__Ios_Openmode_and_std__Ios_Openmode")
        removeMethod("std_operator_andeq__std__Ios_Openmode_and_std__Ios_Openmode")
        removeMethod("std_operator_xoreq__std__Ios_Openmode_and_std__Ios_Openmode")
        removeMethod("std_operator_oreq__std__Ios_Iostate_and_std__Ios_Iostate")
        removeMethod("std_operator_andeq__std__Ios_Iostate_and_std__Ios_Iostate")
        removeMethod("std_operator_xoreq__std__Ios_Iostate_and_std__Ios_Iostate")

        // std::unique_ptr<>'s auto-generated `get()` doesn't model ownership
        // properly; this fixup emits a custom one per instantiation.
        addUniquePtrGet()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
