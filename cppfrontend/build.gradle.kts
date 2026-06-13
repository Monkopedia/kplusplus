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
    }
    // Materialize the range returns the walk iterates. Brick 3 walks members through
    // DeclContext::decls() (source order, all member kinds) instead of methods(), so the
    // CXXMethodDecl vector instantiation is gone.
    instantiate("std::vector<clang::Decl*>")
    instantiate("std::vector<clang::CXXBaseSpecifier*>")
}

// ---- #44 brick 7: THE GOLDEN TEST (Phase B exit criterion / Phase C entry) ----
// Run BOTH front-ends over the same fixture and structurally compare their parse-output
// models through the canonical SerializedElement projection (:krapper_model):
//   goldenEmit    — this front-end writes the fixture + its cpp.json (the fixture is
//                   emitted by the binary itself so both parses consume identical bytes);
//   goldenDump    — the REAL krapper_gen kexe parses that fixture with
//                   --dumpParsedModel (parse-only mode, Parsing.kt) -> libclang.json;
//   goldenCompare — the tree-diff under the documented normalizer ledger
//                   (GoldenCompare.kt), non-zero exit on unexplained divergence.
// This is the automated regression lock for the front-end rewrite (issue #8's spirit):
// any construction drift between ModelFactories/TypeFactories and ModelBuilder/TypeBuilder
// fails this task. Gated with the module (-PenableClang).
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

val goldenDump = tasks.register<Exec>("goldenDump") {
    dependsOn(goldenEmit, ":krapper_gen:linkReleaseExecutableNative")
    commandLine(
        krapperGenKexe.absolutePath,
        "-h",
        File(goldenDir, "fixture.h").absolutePath,
        // cppfrontend's buildASTFromCode parses fixture.cc under the clang driver default;
        // the fixture's surface is identical under c++14..c++20, pin c++17 to match the
        // module's own binding standard.
        "--std",
        "c++17",
        "--dumpParsedModel",
        File(goldenDir, "libclang.json").absolutePath
    )
}

tasks.register<Exec>("goldenCompare") {
    dependsOn(goldenDump)
    commandLine(
        cppfrontendBinary.absolutePath,
        "--golden-compare",
        File(goldenDir, "cpp.json").absolutePath,
        File(goldenDir, "libclang.json").absolutePath
    )
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
        // Same standard pin as goldenDump: the wrapper compile must match the parse.
        "--std",
        "c++17",
        "--frontend",
        "cpp",
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

// ---- #45 brick 3: INSTANTIATION FORCING on the cpp path ----
// The gate to Phase D: `--frontend=cpp --instantiate` end-to-end on an instantiation-
// bearing fixture (Bag/Item + std::vector<Item*> — featuregen's RangeHolder shape).
//   handoffInstEmit     — the cppfrontend binary parses the fixture (base model) AND the
//                         synthesized KrapperForce header (forcing model, a separate
//                         args-bearing parse pulling in <vector>), emitting both as
//                         ModelIo JSON (CppFrontend.handoffEmit);
//   handoffInstGenerate — krapper_gen --frontend=cpp loads BOTH models (libclang never
//                         called), runs the SAME resolveForcing 3-pass flow over them,
//                         and emits + compiles the wrapper: the recovered range accessor
//                         (Bag::items()) and the vector specialization's bindings;
//   handoffInstBaseline — the SAME fixture + instantiation through the libclang path;
//   handoffInstDiff     — THE STRONG CHECK: the two outputs must be byte-identical
//                         (path-modulo: the .def bakes the output dir's absolute path).
val handoffInstDir = layout.buildDirectory.dir("handoff_inst").get().asFile

val handoffInstEmit = tasks.register<Exec>("handoffInstEmit") {
    dependsOn("linkReleaseExecutableKlinker")
    doFirst { handoffInstDir.mkdirs() }
    commandLine(cppfrontendBinary.absolutePath, "--handoff-emit", handoffInstDir.absolutePath)
}

// The shared CLI tail: same fixture, standard, allowlist scope and instantiation on both
// front-ends, so the diff isolates the front-end swap.
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

val handoffInstGenerate = tasks.register<Exec>("handoffInstGenerate") {
    dependsOn(handoffInstEmit, ":krapper_gen:linkReleaseExecutableNative")
    val outDir = File(handoffInstDir, "out_cpp")
    doFirst { outDir.mkdirs() }
    commandLine(
        listOf(
            krapperGenKexe.absolutePath,
            "--frontend",
            "cpp",
            "--parsedModel",
            File(handoffInstDir, "bag_model.json").absolutePath,
            "--forcingModel",
            "std::vector<Item*>=" +
                File(handoffInstDir, "KrapperForce_std_vector_ItemPtr.json").absolutePath
        ) + instGenerateArgs(outDir)
    )
}

// The libclang baseline ALSO dumps each parse's ModelIo tree (--dumpModels): the main
// bag.h parse and the KrapperForce forcing re-parse, exactly the two payloads the cpp
// path consumes — the ORACLE for the file-handoff flow.
val handoffInstBaseline = tasks.register<Exec>("handoffInstBaseline") {
    dependsOn(handoffInstEmit, ":krapper_gen:linkReleaseExecutableNative")
    val outDir = File(handoffInstDir, "out_libclang")
    doFirst { outDir.mkdirs() }
    commandLine(
        listOf(
            krapperGenKexe.absolutePath,
            "--dumpModels",
            File(handoffInstDir, "oracle_models").absolutePath
        ) + instGenerateArgs(outDir)
    )
}

// THE FLOW PROOF: feed the libclang-dumped models back through --frontend=cpp
// --forcingModel. Output must be byte-identical to the in-process libclang run — this
// isolates the file-handoff instantiation flow (fresh instances, no cursor->element
// memo, resolveForcing over loaded trees) from cppfrontend model-construction fidelity.
val handoffOracleGenerate = tasks.register<Exec>("handoffOracleGenerate") {
    dependsOn(handoffInstBaseline)
    val outDir = File(handoffInstDir, "out_oracle_cpp")
    doFirst { outDir.mkdirs() }
    commandLine(
        listOf(
            krapperGenKexe.absolutePath,
            "--frontend",
            "cpp",
            "--parsedModel",
            File(handoffInstDir, "oracle_models/model_0_bag.json").absolutePath,
            "--forcingModel",
            "std::vector<Item*>=" + File(
                handoffInstDir,
                "oracle_models/model_1_KrapperForce_std_vector_ItemPtr.json"
            ).absolutePath
        ) + instGenerateArgs(outDir)
    )
}

// ---- Phase D entry (#46): FEATUREGEN PARITY ----
// The first full featuregen parity attempt under --frontend=cpp: for EACH of featuregen's
// instantiation requests (krapped/requested.txt + the build's seeded instantiate() calls —
// the same worklist kplusplusSync hands krapper_gen), generate featuregen's bindings
// through BOTH front-ends and diff: the libclang baseline vs --frontend=cpp over
// cppfrontend-emitted models, with an IDENTICAL CLI tail so the diff isolates the
// front-end swap. One unit per spec (header + that single instantiation — per-spec
// failure isolation) plus the ALL unit (every spec at once, the real sync's shape).
// Verdicts: identical (byte-identical, path-modulo .def) / diff (N lines, the categorized
// worklist) / fail (generation failed). The task exits nonzero only on REGRESSION against
// parity-expectations.txt (the committed expected-state ledger, like a test-skip list —
// CI-able while Phase D converges); improvements are reported so the ledger ratchets.
val parityDir = layout.buildDirectory.dir("parity").get().asFile
val parityHeader = rootProject.layout.projectDirectory
    .file("featuregen/src/cppMain/include/strings_feature.h").asFile
val parityManifest = rootProject.layout.projectDirectory
    .file("featuregen/krapped/requested.txt").asFile
val parityExpectations = layout.projectDirectory.file("parity-expectations.txt").asFile

// featuregen's seeded instantiate() calls (featuregen/build.gradle.kts kplusplus{}) — the
// manifest only carries the compiler-plugin-requested specs, seeds are build config.
val paritySeeds = listOf(
    "Box<int>",
    "Box<Point>",
    "Box<Box<int>>",
    "Pair2<int, double>",
    "std::unique_ptr<int>",
    "std::vector<Thing*>"
)

// The same merge the gradle plugin's kplusplusSync performs: manifest + seeds, deduped,
// sorted (the deterministic instantiation order).
fun paritySpecs(): List<String> = (
    parityManifest.readLines().map { it.trim() }.filter { it.isNotEmpty() } + paritySeeds
    ).distinct().sorted()

fun runLogged(args: List<String>, log: File): Int {
    log.parentFile.mkdirs()
    return ProcessBuilder(args)
        .redirectErrorStream(true)
        .redirectOutput(log)
        .start()
        .waitFor()
}

fun paritySlug(unit: String) = unit.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')

val parityEmit = tasks.register("parityEmit") {
    dependsOn("linkReleaseExecutableKlinker")
    doLast {
        val modelsDir = File(parityDir, "models").apply { mkdirs() }
        val logsDir = File(parityDir, "logs")
        // One binary invocation per payload: a crash on one spec's parse (expected while
        // Phase D converges) never takes the other models down.
        fun emit(specs: List<String>, log: File): Int = runLogged(
            listOf(
                cppfrontendBinary.absolutePath, "--parity-emit",
                modelsDir.absolutePath, parityHeader.absolutePath, "c++14"
            ) + specs,
            log
        )
        check(emit(emptyList(), File(logsDir, "emit_base.log")) == 0) {
            "parityEmit: base model emit failed (see $logsDir/emit_base.log)"
        }
        val map = StringBuilder()
        for ((i, spec) in paritySpecs().withIndex()) {
            val log = File(logsDir, "emit_$i.log")
            val line = if (emit(listOf(spec), log) == 0) {
                log.readLines().firstOrNull { it.startsWith("PARITY_MODEL ") }
                    ?.removePrefix("PARITY_MODEL ")
            } else {
                null
            }
            map.appendLine(line ?: "$spec=EMIT_FAILED")
        }
        File(parityDir, "forcing_map.txt").writeText(map.toString())
        println("parityEmit: base + ${paritySpecs().size} forcing model(s) under $modelsDir")
    }
}

tasks.register("featuregenParity") {
    dependsOn(parityEmit, ":krapper_gen:linkReleaseExecutableNative")
    doLast {
        val specs = paritySpecs()
        val forcingPaths = File(parityDir, "forcing_map.txt").readLines()
            .filter { it.isNotEmpty() }
            .associate { it.substringBefore('=') to it.substringAfter('=') }
        val baseModel = File(parityDir, "models/base_model.json")
        val logsDir = File(parityDir, "logs")
        val diffsDir = File(parityDir, "diffs").apply { deleteRecursively() }

        // The featuregen CLI tail, mirroring kplusplusSync's invocation (module name,
        // INCLUDE_MISSING, krapper.featuregen package, --header) — identical on both
        // sides so the diff isolates the front-end swap.
        fun genArgs(outDir: File, instSpecs: List<String>) = listOf(
            krapperGenKexe.absolutePath, "featuregen",
            "-r", "INCLUDE_MISSING",
            "-p", "krapper.featuregen",
            "-c", "clang++",
            "-o", outDir.absolutePath
        ) + instSpecs.flatMap { listOf("--instantiate", it) } +
            listOf("--header", parityHeader.absolutePath)

        // Byte-compare the two outputs: same file set, every file identical (the .def
        // path-modulo — it bakes the output dir's absolute path; .a excluded — derived
        // from the compared .cc). On divergence, a unified diff per file lands under
        // diffs/<unit>/ for the worklist analysis. Third element: the diff-line count,
        // ratcheted per-unit against the ledger's `diff:<N>` bound (D2 brick) so an
        // intra-diff regression — more divergence while staying in the diff class —
        // fails the task just like a class regression.
        fun diffUnit(unit: String, libclang: File, cpp: File): Triple<String, String, Int> {
            fun files(dir: File) = dir.walkTopDown()
                .filter { it.isFile && it.extension != "a" }
                .map { it.relativeTo(dir).path }.toSortedSet()
            val a = files(libclang)
            val b = files(cpp)
            val notes = mutableListOf<String>()
            if (a != b) notes += "file sets differ: only-libclang=${a - b} only-cpp=${b - a}"
            var diffLines = 0
            var diffFiles = 0
            for (rel in a intersect b) {
                fun read(dir: File) =
                    File(dir, rel).readText().replace(dir.absolutePath, "@OUT@")
                val ta = read(libclang)
                val tb = read(cpp)
                if (ta == tb) continue
                diffFiles++
                val diffFile = File(diffsDir, "${paritySlug(unit)}/$rel.diff")
                diffFile.parentFile.mkdirs()
                val tmpA = File(diffFile.parentFile, "a.tmp").apply { writeText(ta) }
                val tmpB = File(diffFile.parentFile, "b.tmp").apply { writeText(tb) }
                val proc = ProcessBuilder(
                    "diff", "-u", tmpA.absolutePath, tmpB.absolutePath
                ).redirectErrorStream(true).start()
                val out = proc.inputStream.readBytes().toString(Charsets.UTF_8)
                proc.waitFor()
                tmpA.delete()
                tmpB.delete()
                diffFile.writeText(out)
                diffLines += out.lineSequence().count {
                    (it.startsWith("+") && !it.startsWith("+++")) ||
                        (it.startsWith("-") && !it.startsWith("---"))
                }
            }
            return if (notes.isEmpty() && diffFiles == 0) {
                Triple("identical", "byte-identical (path-modulo .def) across ${a.size} files", 0)
            } else {
                Triple(
                    "diff",
                    (notes + "$diffLines diff line(s) across $diffFiles file(s)").joinToString("; "),
                    diffLines
                )
            }
        }

        data class Verdict(val unit: String, val cls: String, val detail: String, val lines: Int)
        val verdicts = mutableListOf<Verdict>()
        val units = specs.map { it to listOf(it) } + ("ALL" to specs)
        for ((unit, instSpecs) in units) {
            val unitDir = File(parityDir, "out/${paritySlug(unit)}")
            val outLib = File(unitDir, "libclang").apply { deleteRecursively(); mkdirs() }
            val outCpp = File(unitDir, "cpp").apply { deleteRecursively(); mkdirs() }
            val libLog = File(logsDir, "${paritySlug(unit)}_libclang.log")
            val libExit = runLogged(genArgs(outLib, instSpecs), libLog)
            if (libExit != 0) {
                verdicts += Verdict(
                    unit, "fail",
                    "LIBCLANG BASELINE failed (exit $libExit — infra, see $libLog)",
                    0
                )
                continue
            }
            val missing = instSpecs.filter { (forcingPaths[it] ?: "EMIT_FAILED") == "EMIT_FAILED" }
            if (missing.isNotEmpty()) {
                verdicts += Verdict(unit, "fail", "cppfrontend model emit failed for $missing", 0)
                continue
            }
            val cppLog = File(logsDir, "${paritySlug(unit)}_cpp.log")
            val cppExit = runLogged(
                genArgs(outCpp, instSpecs) + listOf(
                    "--frontend", "cpp",
                    "--parsedModel", baseModel.absolutePath
                ) + instSpecs.flatMap { listOf("--forcingModel", "$it=${forcingPaths[it]}") },
                cppLog
            )
            if (cppExit != 0) {
                val tail = cppLog.readLines().lastOrNull { it.isNotBlank() }.orEmpty()
                verdicts += Verdict(unit, "fail", "cpp generation failed (exit $cppExit): $tail", 0)
                continue
            }
            val (cls, detail, lines) = diffUnit(unit, outLib, outCpp)
            verdicts += Verdict(unit, cls, detail, lines)
        }

        // Scoreboard + the regression gate against the committed expected-state ledger.
        val rank = mapOf("identical" to 0, "diff" to 1, "fail" to 2)
        val expected = if (parityExpectations.exists()) {
            parityExpectations.readLines().mapNotNull { line ->
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) {
                    null
                } else {
                    t.substringBeforeLast('=').trim() to t.substringAfterLast('=').trim()
                }
            }.toMap()
        } else {
            emptyMap()
        }
        var regressions = 0
        val report = buildString {
            appendLine("featuregenParity scoreboard (${verdicts.size} units):")
            for (v in verdicts) {
                // A `diff` expectation may carry a line bound (`diff:<N>`): the unit
                // regresses if it diverges MORE than recorded, and an improvement
                // (fewer lines) prompts the ledger ratchet — same discipline as the
                // class ranking, one level finer.
                val expEntry = expected[v.unit] ?: "identical"
                val exp = expEntry.substringBefore(':')
                val expLines = expEntry.substringAfter(':', "").toIntOrNull()
                val marker = when {
                    (rank[v.cls] ?: 2) > (rank[exp] ?: 0) -> {
                        regressions++
                        "REGRESSION (expected $exp)"
                    }

                    (rank[v.cls] ?: 2) < (rank[exp] ?: 0) ->
                        "IMPROVED (expected $exp — ratchet the ledger)"

                    v.cls == "diff" && expLines != null && v.lines > expLines -> {
                        regressions++
                        "REGRESSION (${v.lines} diff lines > ledger bound $expLines)"
                    }

                    v.cls == "diff" && expLines != null && v.lines < expLines ->
                        "IMPROVED (${v.lines} < $expLines diff lines — ratchet the ledger)"

                    else -> "as expected"
                }
                appendLine("  [${v.cls.uppercase().padEnd(9)}] ${v.unit} — ${v.detail} [$marker]")
            }
            appendLine(
                "totals: ${verdicts.count { it.cls == "identical" }} identical / " +
                    "${verdicts.count { it.cls == "diff" }} diff / " +
                    "${verdicts.count { it.cls == "fail" }} fail " +
                    "(of ${verdicts.size}; unit diffs under $diffsDir, logs under $logsDir)"
            )
        }
        File(parityDir, "report.txt").writeText(report)
        println(report)
        check(regressions == 0) {
            "featuregenParity: $regressions regression(s) vs $parityExpectations"
        }
    }
}

// ---- Phase E step 3 (#47, flip brick B2): GENERIC cpp-front-end binding generation ----
// The former `featuregenCppBindings` task here was the featuregen-HARDCODED cpp binding
// generator (header + seeds baked in). It has been REPLACED by a generic per-module path
// in the kplusplus gradle plugin: under -Pkpp.frontend.<module>=cpp, kplusplusSync itself
// drives THAT module's own kplusplus{} config (its header + instantiation worklist) through
// this front-end binary's --parity-emit -> ModelIo JSON -> krapper_gen --frontend=cpp into
// build/krapped-cpp, auto-wiring :cppfrontend:linkReleaseExecutableKlinker. So ANY module
// (featuregen, the :cppfixture demo, ...) generates cpp bindings from its own config with
// no per-module Gradle wiring. featuregenParity (above) keeps the diff/regression ledger.

tasks.register("handoffInstDiff") {
    dependsOn(handoffInstGenerate, handoffInstBaseline, handoffOracleGenerate)
    doLast {
        val libclang = File(handoffInstDir, "out_libclang")
        fun files(dir: File) = dir.walkTopDown().filter { it.isFile }
            .map { it.relativeTo(dir).path }.toSortedSet()

        // ---- Gate 1 (byte): the oracle run. Same models as the in-process libclang
        // run (its own dumps), so the file-handoff flow must reproduce it exactly —
        // path-modulo: the .def bakes the output dir's absolute path.
        val oracle = File(handoffInstDir, "out_oracle_cpp")
        val names = files(libclang)
        check(names == files(oracle)) {
            "handoffInstDiff[oracle]: file sets differ — only-libclang=" +
                "${names - files(oracle)} only-cpp=${files(oracle) - names}"
        }
        val diffs = names.filter { rel ->
            fun read(dir: File) = File(dir, rel).readBytes().toString(Charsets.UTF_8)
                .replace(dir.absolutePath, "@OUT@")
            if (rel.endsWith(".def")) {
                read(libclang) != read(oracle)
            } else {
                !File(libclang, rel).readBytes().contentEquals(File(oracle, rel).readBytes())
            }
        }
        check(diffs.isEmpty()) {
            "handoffInstDiff[oracle]: file-handoff flow diverged from the in-process " +
                "run in: $diffs (compare $libclang vs $oracle)"
        }
        println(
            "handoffInstDiff[oracle]: byte-identical (path-modulo .def) across " +
                "${names.size} files"
        )

        // ---- Gate 2 (functional): the cppfrontend-model run. The C++-AST front-end's
        // std-template model is deliberately MORE faithful than libclang's lossy one
        // (no "_E*"/"_Pointer" Unexposed-spelling artifacts, no order-dependent
        // visit() collapse), and member uniqueCNames bake those pre-substitution
        // spellings — so byte-parity here would mean emulating libclang's lossiness
        // bug-for-bug, which the bootstrap exists to delete. The gate is functional:
        // the SAME file set (the specialization's bindings materialized, nothing
        // extra), the recovered range accessor, the core container surface, and the
        // wrapper COMPILED (writeTo's CppCompiler step already failed the generate
        // task otherwise). Spelling deltas are tracked on #45.
        val cpp = File(handoffInstDir, "out_cpp")
        // D1b (#46): the cpp model resolves std::initializer_list<Item*> DIRECTLY (its
        // faithful in-model initializer_list class), so vector's surviving ilist
        // ctor/op=/assign members materialize the specialization's binding on this
        // path; the libclang model only reaches that class through INCLUDE_MISSING's
        // re-parse — OFF on this default-policy flow — so its members drop. Production
        // INCLUDE_MISSING proves the two CONVERGE byte-identically (featuregenParity:
        // every std_Initializer_list__* file-set gap closed); this gate accepts the
        // cpp-side faithfulness surplus, the same rationale as the spelling deltas.
        val cppOnly = files(cpp) - names
        val unexplained = cppOnly.filter { !it.startsWith("src/std_Initializer_list__") }
        check((names - files(cpp)).isEmpty() && unexplained.isEmpty()) {
            "handoffInstDiff[functional]: file sets differ — only-libclang=" +
                "${names - files(cpp)} unexplained-only-cpp=$unexplained"
        }
        val bag = File(cpp, "src/root_Bag.kt").readText()
        check(bag.contains("fun items(): Vector__Item_P")) {
            "handoffInstDiff[functional]: Bag::items() was not recovered by pass-3 " +
                "forcing on the cpp path"
        }
        val vector = File(cpp, "src/std_Vector__Item_P.kt").readText()
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
            "handoffInstDiff[functional]: vector specialization is missing core " +
                "surface: $missing"
        }
        println(
            "handoffInstDiff[functional]: cppfrontend-model run materialized the " +
                "specialization + recovered items() (same file set, core surface present)"
        )
    }
}
