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
import com.monkopedia.klinker.klinkedExecutable

plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler")
    id("com.monkopedia.klinker.plugin") version "0.2.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        binaries {
            klinkedExecutable {
                // klinker links with the system clang++ (needed for modern libclang-cpp's
                // glibc/libstdc++ symbol versions, absent from K/N's bundled old sysroot).
                // K/N's DEBUG build pulls in the precompiled platform.linux cinterop cache
                // WHOLE (no whole-program DCE), and it references dozens of OBSOLETE glibc
                // symbols (__argp_parse, __argz_*, cfree, getmsg, …) modern glibc removed →
                // ~111 undefined refs. `--gc-sections` lets the system linker dead-strip the
                // unreferenced per-function sections (K/N emits one section per wrapper for
                // exactly this) — giving DEBUG the same DCE release already gets from -opt.
                // So BOTH build types link now: runDebugExecutableKlinker + the release one.
                // (The durable home for this is a klinker default — see its LinkTask.)
                linkerOpts("--gc-sections")
                // The generated wrapper calls into Clang's C++ AST API, so the final
                // executable links libclang-cpp (the monolithic LLVM/Clang shared lib)
                // + LLVM core. -lstdc++ for the C++ wrapper runtime.
                compilerOpts(
                    "-L/usr/lib",
                    "-lclang-cpp",
                    "-lLLVM-22",
                    "-lstdc++",
                    "-lm",
                    "-lpthread"
                )
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
}

// Stage1 self-bootstrap consumer: bind a SLICE of Clang's C++ AST (the Decl hierarchy +
// the buildASTFromCode entry point) and walk a real AST on the generated Kotlin bindings.
// Scoped import (only + IGNORE_MISSING) keeps the bound surface to the allowlist instead
// of exploding into Clang's whole transitive closure.
kplusplus {
    header("include/clang_slice.h")
    // Clang's AST headers must be compiled with clang++, NOT the konan-bundled GCC-8.3
    // (resolveDefaultCompiler's default) — GCC-8.3/libstdc++ aborts on them (exit 134).
    compiler = "clang++"
    cppStandard = "c++17"
    referencePolicy = "IGNORE_MISSING"
    only(
        // Entry point: build an AST from a code string, reach the TU.
        "clang::tooling::buildASTFromCode",
        "clang::ASTUnit",
        "clang::ASTContext",
        // The Decl hierarchy we walk.
        "clang::Decl",
        "clang::NamedDecl",
        "clang::DeclContext",
        "clang::TranslationUnitDecl",
        "clang::TagDecl",
        "clang::RecordDecl",
        "clang::CXXRecordDecl",
        "clang::CXXMethodDecl",
        "clang::FieldDecl",
        "clang::ValueDecl",
        "clang::DeclaratorDecl",
        "clang::TypeDecl",
        "clang::CXXBaseSpecifier",
        // QualType is the carrier for a decl's type. Binding it lets ValueDecl::getType()
        // survive and exposes QualType::getAsString() (a by-value std::string return) — the
        // #37 EXTRACT payoff: under IGNORE_MISSING the string return now normalizes to a
        // Kotlin String instead of dropping the whole method.
        "clang::QualType"
    )
    // Materialize the range returns the walk iterates (decls()/methods()/bases()).
    instantiate("std::vector<clang::Decl*>")
    instantiate("std::vector<clang::CXXMethodDecl*>")
    instantiate("std::vector<clang::CXXBaseSpecifier*>")
    // ASTContext::adjustType(QualType, llvm::function_ref<QualType(QualType)>) has an
    // un-modelable function_ref second arg. With QualType now bound its return resolves, so
    // the method is no longer dropped-by-unresolved-return — and the function_ref param is
    // mis-flagged omittable-default (the cursor-children default heuristic fires on the
    // function_ref's type tokens), emitting an under-arity `adjustType(Old)` call that won't
    // compile. Drop it explicitly until that heuristic is tightened (a krapper_gen follow-up).
    fixup {
        removeMethod("clang_ASTContext_adjust_type")
    }
}
