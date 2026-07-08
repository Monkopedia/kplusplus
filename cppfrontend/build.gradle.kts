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
                // libdir + LLVM major are DISCOVERED by settings.gradle.kts's #11(b) probe
                // (llvm-config --libdir / --version) and threaded via rootProject extras;
                // the literal fallbacks match this host and only apply if the probe didn't
                // run. (See :clangwalk's build.gradle.kts for the full link rationale.)
                compilerOpts(
                    "-L${rootProject.extra.properties["llvmLibDir"] ?: "/usr/lib"}",
                    "-lclang-cpp",
                    "-lLLVM-${rootProject.extra.properties["llvmMajor"] ?: "22"}",
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
        // NEW for brick 5 (enums): an enum-typed leaf reaches its EnumDecl through the
        // already-bound Type::getAsTagDecl() + a kind-gated re-view (TypeBuilder).
        // clang::EnumType is deliberately NOT bound: it declares no constructors of its
        // own (only inherited `using TagType::TagType`), so no ctor cursor ever marks
        // hasConstructor and krapper synthesizes a `new clang::EnumType()` default-
        // construct wrapper that C++ rejects (implicitly deleted — generator gap noted
        // on #44). EnumDecl carries getIntegerType() (the underlying type,
        // clang_getEnumDeclIntegerType's source) and its DeclContext holds the
        // EnumConstantDecls. llvm::APSInt is EnumConstantDecl::getInitVal()'s carrier;
        // only its OWN surface is needed (getExtValue — see TypeBuilder's value bridge),
        // so the heavy llvm::APInt base stays unbound.
        "clang::EnumDecl",
        "clang::EnumConstantDecl",
        "llvm::APSInt",
        // NEW for brick 6 (function-pointer typedefs): a typedef over a pointer-to-
        // function-proto decodes through FunctionProtoType — getNumParams()/getParamType()
        // + the inherited getReturnType(). Unlike TypedefType, this chain survives
        // resolution intact (the generated Type.asFunctionProtoType() dyn-cast is real);
        // FunctionType is bound as the bridging base.
        "clang::FunctionType",
        "clang::FunctionProtoType",
        // NEW for brick 6 (default arguments): the smallest bindable surface for the
        // default's source text — an inline helper in the slice header wrapping
        // Lexer::getSourceText over ParmVarDecl::getDefaultArgRange() (see clang_slice.h
        // for why Lexer/SourceManager/LangOptions aren't bound wholesale yet).
        "kppbridge::defaultArgText",
        // NEW for Phase D (#46): WrappedTemplateParam.defaultType capture — the
        // TemplateArgumentLoc unwrap for TemplateTypeParmDecl::getDefaultArgument()
        // (see clang_slice.h).
        "kppbridge::defaultArgType",
        // NEW for Phase D (#46): inline-namespace-preserving qualified names
        // (std::__cxx11::basic_string — see clang_slice.h).
        "kppbridge::qualifiedName",
        // NEW for #45 brick 3 (instantiation forcing): the forcing-parse entry point —
        // buildASTFromCode with REAL driver args ('\n'-joined; -resource-dir + -std), the
        // smallest bridge until std::vector<std::string> params are bindable (see
        // clang_slice.h).
        "kppbridge::buildASTWithArgs",
        // NEW for #45 brick 3: the CIndex GetTemplateArguments mirror — written-args
        // preference + dependent-specialization decode (see clang_slice.h).
        "kppbridge::numTemplateArgs",
        "kppbridge::templateArgAsType",
        "kppbridge::templateBaseName"
    )
    // FIXUPS (documented generator gaps, #44 brick 5): krapper's operator generation
    // emits invalid Kotlin for four of llvm::APSInt's C++ operators —
    //  * operator==(int64_t) / operator<(int64_t): the Long overloads of operators whose
    //    APSInt overload already claimed the idiomatic name (equals/compareTo) are
    //    emitted as `override fun _equals` / `override operator fun _compareTo`
    //    (override-nothing + illegal operator name + pointer-vs-Long argument mixups);
    //  * prefix operator++/operator--: emitted as `operator fun inc(): APSInt?` whose
    //    NULLABLE return violates Kotlin's inc/dec operator convention.
    // None of the four is needed here (the enum value bridge is APSInt::getExtValue(),
    // TypeBuilder.buildEnumType); remove them by uniqueCName until krapper_gen learns
    // mixed-argument operator overloads (generator gap recorded on #44).
    fixup {
        removeMethod("llvm_APSInt_op_eq__long")
        removeMethod("llvm_APSInt_op_lt__long")
        removeMethod("llvm_APSInt_op_increment")
        removeMethod("llvm_APSInt_op_decrement")
        // #122 (self-hosting spike): LLVM-22 added clang::ASTContext::getPredefinedSugarType,
        // whose parameter is the PROTECTED enum clang::Type::PredefinedSugarKind. ASTContext
        // is in the allowlist, so krapper_gen binds the method and emits a cast to the
        // protected enum — `clang::Type::PredefinedSugarKind KD_cast = ...` — which the
        // wrapper compile rejects ("protected member of 'clang::Type'"). The bridge doesn't
        // need it (nothing here calls getPredefinedSugarType); remove it by uniqueCName until
        // krapper_gen learns to skip methods over inaccessible types. The committed seed
        // predates this LLVM surface, so it never carried the method.
        removeMethod("clang_ASTContext_get_predefined_sugar_type")
    }
    // Materialize the range returns the walk iterates. Brick 3 walks members through
    // DeclContext::decls() (source order, all member kinds) instead of methods(), so the
    // CXXMethodDecl vector instantiation is gone.
    instantiate("std::vector<clang::Decl*>")
    instantiate("std::vector<clang::CXXBaseSpecifier*>")
}

// ---- #44 brick 7 / post-flip (#92): the cpp golden EMIT ----
// goldenEmit makes the cpp front-end parse the fixture and write the bytes (fixture.h) +
// the full-fidelity ModelIo handoff JSON (model.json) that handoffGenerate consumes. The
// historical goldenDump/goldenCompare tasks compared this against krapper_gen's libclang-C
// reducer (--dumpParsedModel); that reducer was DELETED in the self-hosting flip (B5, #88),
// so the libclang baseline is gone and the comparison was removed (#92) — the standing cpp
// regression lock is now handoffGenerate (cpp e2e, below) + :featuregen:nativeTest.
// Gated with the module (-PenableClang).
val goldenDir = layout.buildDirectory.dir("golden").get().asFile
val cppfrontendBinary = layout.buildDirectory
    .file("bin/klinker/cppfrontendRelease/cppfrontend").get().asFile
val krapperGenKexe = rootProject.layout.projectDirectory
    .file("krapper_gen/build/bin/native/releaseExecutable/krapper_gen.kexe").asFile

val goldenEmit = tasks.register<Exec>("goldenEmit") {
    dependsOn("linkReleaseExecutableKlinker")
    doFirst { goldenDir.mkdirs() }
    commandLine(cppfrontendBinary.absolutePath, "--golden-emit", goldenDir.absolutePath)
}

// ---- #101: NONCOPYABLE special-member determinism guard ----
// Runs the cppfrontend binary's `--noncopyable-determinism` mode, which parses a
// self-contained reduction of v8::Persistent's NonCopyable shape N times (separate
// ASTContexts) and asserts the special-member set is byte-identical and complete across
// parses — the standing regression lock for the Persistent<v8::Value> parse-dependent
// copy-member report (a non-zero exit fails the task). Gated with the module (-PenableClang).
tasks.register<Exec>("noncopyableDeterminismCheck") {
    dependsOn("linkReleaseExecutableKlinker")
    commandLine(cppfrontendBinary.absolutePath, "--noncopyable-determinism")
}

// ---- #45 brick 2: THE HANDOFF (Phase C) ----
// The first time generated-bindings-parsed C++ flows through krapper_gen's REAL pipeline:
//   goldenEmit       — the cppfrontend binary parses the fixture with the Clang C++ AST
//                      and writes model.json (full-fidelity ModelIo JSON) next to the
//                      compare projection;
//   handoffGenerate  — krapper_gen --frontend=cpp loads that model (libclang parse
//                      SKIPPED), runs resolution + codegen, emits the Kotlin bindings +
//                      C++ wrapper for the fixture, and COMPILES the wrapper against
//                      fixture.h (writeTo's CppCompiler step — clang++ -c; a non-zero
//                      exit fails the task), proving the handed-off model carries
//                      everything the real pipeline consumes.
// --instantiate is scoped out on this path (forcing re-parses synthesized headers
// through libclang); the fixture needs no instantiations. Gated with the module
// (-PenableClang), like the golden tasks above.
val handoffDir = layout.buildDirectory.dir("handoff").get().asFile

tasks.register<Exec>("handoffGenerate") {
    dependsOn(goldenEmit, ":krapper_gen:linkReleaseExecutableNative")
    doFirst { handoffDir.mkdirs() }
    commandLine(
        krapperGenKexe.absolutePath,
        "-h",
        File(goldenDir, "fixture.h").absolutePath,
        // Pin c++17 so the generated-wrapper compile matches the cppfrontend parse.
        "--std",
        "c++17",
        "--parsedModel",
        File(goldenDir, "model.json").absolutePath,
        "-o",
        handoffDir.absolutePath,
        "fixture"
    )
    doLast {
        // The run already failed on any resolve/codegen/compile error; assert the
        // artifacts the next phase consumes actually materialized.
        val expected = listOf("fixture.h", "fixture.cc", "fixture.def", "libfixture.a")
        val missing = expected.filter { !File(handoffDir, it).exists() }
        check(missing.isEmpty()) { "handoffGenerate: missing generated artifact(s): $missing" }
        val kotlinFiles = File(handoffDir, "src").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
        check(kotlinFiles.isNotEmpty()) { "handoffGenerate: no Kotlin bindings generated" }
        println("handoffGenerate: generated " + (expected + kotlinFiles.map { it.name }))
    }
}

// ---- #45 brick 3 / post-flip (#92): INSTANTIATION FORCING on the cpp path ----
// The cpp instantiation-forcing e2e gate: `--instantiate` end-to-end on an instantiation-
// bearing fixture (Bag/Item + std::vector<Item*> — featuregen's RangeHolder shape).
//   handoffInstEmit     — the cppfrontend binary parses the fixture (base model) AND the
//                         synthesized KrapperForce header (forcing model, a separate
//                         args-bearing parse pulling in <vector>), emitting both as
//                         ModelIo JSON (CppFrontend.handoffEmit);
//   handoffInstGenerate — krapper_gen loads BOTH models (the cpp front-end; libclang never
//                         called), runs the resolveForcing 3-pass flow over them, emits +
//                         COMPILES the wrapper (a clang++ failure fails the task), then
//                         functionally asserts the recovered range accessor (Bag::items())
//                         and the vector specialization's core surface materialized.
// The historical libclang baseline arm (handoffInstBaseline/handoffOracleGenerate +
// handoffInstDiff's byte-compare) was removed in #92: krapper_gen's --dumpModels libclang
// reducer is gone (flip B5, #88), so there is no second front-end to diff against — the
// functional asserts below are the standing cpp-side check.
val handoffInstDir = layout.buildDirectory.dir("handoff_inst").get().asFile

val handoffInstEmit = tasks.register<Exec>("handoffInstEmit") {
    dependsOn("linkReleaseExecutableKlinker")
    doFirst { handoffInstDir.mkdirs() }
    commandLine(cppfrontendBinary.absolutePath, "--handoff-emit", handoffInstDir.absolutePath)
}

// The CLI tail: fixture, standard, allowlist scope and instantiation for the cpp run.
fun instGenerateArgs(outDir: File) = listOf(
    "-h",
    File(handoffInstDir, "bag.h").absolutePath,
    "--std",
    "c++17",
    "--only",
    "Bag",
    "--only",
    "Item",
    "--instantiate",
    "std::vector<Item*>",
    "-o",
    outDir.absolutePath,
    "bag"
)

tasks.register<Exec>("handoffInstGenerate") {
    dependsOn(handoffInstEmit, ":krapper_gen:linkReleaseExecutableNative")
    val outDir = File(handoffInstDir, "out_cpp")
    doFirst { outDir.mkdirs() }
    commandLine(
        listOf(
            krapperGenKexe.absolutePath,
            "--parsedModel",
            File(handoffInstDir, "bag_model.json").absolutePath,
            "--forcingModel",
            "std::vector<Item*>=" +
                File(handoffInstDir, "KrapperForce_std_vector_ItemPtr.json").absolutePath
        ) + instGenerateArgs(outDir)
    )
    doLast {
        // Functional gate (formerly handoffInstDiff gate 2, now cpp-only — the libclang
        // baseline it diffed against is gone). The wrapper already COMPILED (writeTo's
        // CppCompiler step fails the run otherwise); assert pass-3 forcing recovered the
        // range accessor and the specialization materialized its core container surface.
        val bag = File(outDir, "src/root_Bag.kt").readText()
        check(bag.contains("fun items(): Vector__Item_P")) {
            "handoffInstGenerate: Bag::items() was not recovered by pass-3 forcing"
        }
        val vector = File(outDir, "src/std_Vector__Item_P.kt").readText()
        val expectedSurface = listOf(
            "fun size(): size_t",
            "fun push_back(",
            "fun at(",
            "operator fun get(",
            "fun front(): Item?",
            "fun back(): Item?",
            "fun empty(): Boolean",
            "fun clear(): Unit"
        )
        val missing = expectedSurface.filter { !vector.contains(it) }
        check(missing.isEmpty()) {
            "handoffInstGenerate: vector specialization is missing core surface: $missing"
        }
        println(
            "handoffInstGenerate: cpp instantiation-forcing run compiled + materialized " +
                "the specialization and recovered items()"
        )
    }
}
