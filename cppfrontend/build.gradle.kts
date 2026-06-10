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
    alias(libs.plugins.kotlin.serialization)
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
                // Same wiring as :clangwalk (see its build.gradle.kts for the full
                // rationale): system clang++ link for modern glibc/libstdc++ symbol
                // versions, --gc-sections so DEBUG dead-strips the stale platform.linux
                // cinterop cache, and libclang-cpp + LLVM for the Clang C++ AST API.
                linkerOpts("--gc-sections")
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
    sourceSets["nativeMain"].dependencies {
        // The whole point of this module (#44 brick 2): construct :krapper_model's pure
        // parse-output model (WrappedTU/WrappedClass/WrappedMethod/WrappedType) from the
        // kplusplus-generated libclang-cpp bindings instead of libclang-C cursors.
        implementation(project(":krapper_model"))
        implementation(libs.serialization.json)
    }
}

// Stage1 front-end scaffold (#44 brick 2): bind the same Clang C++ AST slice :clangwalk
// proved out (EXTRACT/TRAVERSE/IDENTITY), parse a fixture, and CONSTRUCT a WrappedTU from
// the walk. Scoped import (only + IGNORE_MISSING) keeps the bound surface to the allowlist.
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
        // The Decl hierarchy the construction walks (clangwalk's proven slice).
        "clang::Decl",
        "clang::NamedDecl",
        "clang::DeclContext",
        "clang::TranslationUnitDecl",
        // NEW for brick 3 (construction depth): namespace nesting. NamespaceBaseDecl is
        // NamespaceDecl's direct base (the Clang 22 NamespaceDecl/alias split); it bridges
        // the NamespaceDecl -> NamedDecl upcast chain the same way FunctionDecl bridges
        // CXXMethodDecl's.
        "clang::NamespaceBaseDecl",
        "clang::NamespaceDecl",
        "clang::TagDecl",
        "clang::RecordDecl",
        "clang::CXXRecordDecl",
        // FunctionDecl bridges the CXXMethodDecl -> NamedDecl upcast chain and carries
        // getReturnType()/getNumParams()/getParamDecl() — the signature payload.
        "clang::FunctionDecl",
        "clang::CXXMethodDecl",
        // NEW for brick 3: real Decl classes for ctors/dtors (libclang only has cursor
        // kinds) — CXXConstructorDecl carries is{Copy,Default}Constructor, the C++-AST
        // equivalents of clang_CXXConstructor_is{Copy,Default}Constructor.
        "clang::CXXConstructorDecl",
        "clang::CXXDestructorDecl",
        "clang::FieldDecl",
        "clang::ValueDecl",
        "clang::DeclaratorDecl",
        "clang::TypeDecl",
        "clang::CXXBaseSpecifier",
        // NEW over clangwalk's slice: parameters. FunctionDecl::getParamDecl(i) returns a
        // ParmVarDecl* — without ParmVarDecl bound, IGNORE_MISSING drops getParamDecl and
        // arguments are unreachable (clangwalk never bound it; the walk didn't need args).
        // VarDecl is ParmVarDecl's direct base: it bridges the ParmVarDecl ->
        // DeclaratorDecl/ValueDecl/NamedDecl upcast chain (same reason FunctionDecl is
        // bound for CXXMethodDecl), so a parameter exposes its name + QualType.
        "clang::ParmVarDecl",
        "clang::VarDecl",
        // QualType is the carrier for a decl's type; getAsString() (by-value std::string,
        // normalized to a Kotlin String by #37) spells the leaves.
        "clang::QualType",
        // NEW for brick 4 (structural type construction, TypeBuilder.kt): clang::Type
        // carries the shape-classification predicates (isPointerType/isReferenceType/
        // isRValueReferenceType/isFunctionPointerType/isConstantArrayType/isEnumeralType),
        // getPointeeType(), and the record recovery (getAsCXXRecordDecl) — the decode
        // surface proven on clangwalk/type-decode-probe (59ebf04).
        "clang::Type",
        // Typedef/alias decode: a sugared QualType's Type dyn-casts to TypedefType, whose
        // getDecl() (a TypedefNameDecl — covers both `typedef` and `using`) provides the
        // alias NAME (the size_t/size_type preservation rules) and getUnderlyingType()
        // (the canonical-collapse recursion). TypeDecl (already bound) bridges the
        // TypedefNameDecl -> NamedDecl upcast chain.
        "clang::TypedefType",
        "clang::TypedefNameDecl",
        // Constant-array decode: ConstantArrayType::getZExtSize() is the extent and
        // ArrayType (its direct base) carries getElementType() — together they reconstruct
        // the libclang "elem [N]" array leaf structurally.
        "clang::ArrayType",
        "clang::ConstantArrayType",
        // Template-specialization decode (probe-proven): a template-typed field/param's
        // canonical record dyn-casts to ClassTemplateSpecializationDecl ->
        // getTemplateArgs() -> TemplateArgument::getAsType() per argument.
        "clang::ClassTemplateSpecializationDecl",
        "clang::TemplateArgument",
        "clang::TemplateArgumentList",
        // NEW for brick 5 (template DECLARATIONS): a `template <typename T> class` is a
        // ClassTemplateDecl whose getTemplatedDecl() is the pattern CXXRecordDecl;
        // getTemplateParameters() lives on TemplateDecl (the primary base, reached by
        // re-view — see ModelBuilder) and yields a TemplateParameterList (size/getParam).
        // TemplateTypeParmDecl is each type param's decl; TemplateTypeParmType is the
        // DEPENDENT `T` in member signatures, whose getDecl() keys the WrappedTemplateRef
        // back to the matching WrappedTemplateParam.
        "clang::TemplateDecl",
        "clang::ClassTemplateDecl",
        "clang::TemplateParameterList",
        "clang::TemplateTypeParmDecl",
        "clang::TemplateTypeParmType",
        // NEW for brick 5 (enums): an enum-typed leaf reaches its EnumDecl through
        // EnumType::getDecl() (the Type-side dyn-cast is bridged by TypeClass check —
        // TypeBuilder); EnumDecl carries getIntegerType() (the underlying type,
        // clang_getEnumDeclIntegerType's source) and its DeclContext holds the
        // EnumConstantDecls. llvm::APSInt is EnumConstantDecl::getInitVal()'s carrier;
        // only its OWN surface is needed (getExtValue — see TypeBuilder's value bridge),
        // so the heavy llvm::APInt base stays unbound.
        "clang::EnumDecl",
        "clang::EnumConstantDecl",
        "clang::EnumType",
        "llvm::APSInt"
    )
    // Materialize the range returns the walk iterates. Brick 3 walks members through
    // DeclContext::decls() (source order, all member kinds) instead of methods(), so the
    // CXXMethodDecl vector instantiation is gone.
    instantiate("std::vector<clang::Decl*>")
    instantiate("std::vector<clang::CXXBaseSpecifier*>")
}
