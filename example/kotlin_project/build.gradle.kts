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
                compilerOpts("-lstdc++", "-lm", "-lpthread")
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
    header("../include/v8-combined.h")
    headerDirectory("../include/")
    library("../libv8_monolith.a")
    // v8 needs C++17 (the default is c++14); the cpp front-end parses AND the wrapper
    // compiles under this standard. The frontend flip itself is in gradle.properties
    // (kpp.frontend.kotlin_project=cpp), built with -PenableClang.
    cppStandard = "c++17"

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
        // may be instantiated. The cpp front-end (brick-1 flip to
        // -Pkpp.frontend.kotlin_project=cpp) materializes these IMPLICIT special members
        // parse-dependently — one run surfaces the copy ctor
        // (`..._new__const_v8_Persistent_and`), another the copy-assignment (`..._op_assign`,
        // the libclang-era name, still valid) — so drop BOTH to stay robust across parses.
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
