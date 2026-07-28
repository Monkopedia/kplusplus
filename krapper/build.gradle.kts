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
import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi

// THE TOOL (#184). One binary: it PARSES C++ with Clang's C++ AST and RESOLVES + GENERATES the
// Kotlin bindings + C++ wrapper. These were two binaries (`krapper_parse` / `krapper_gen`) that
// handed the parse-output model over as ModelIo JSON through a temp file; the handoff is a plain
// in-process object now.
//
// The module is simultaneously:
//   * a CONSUMER of the published kplusplus plugin — its ~60-entry `clang::*` binding surface is
//     generated from include/clang_slice.h by the tool bundled in the LAST released plugin
//     (settings.gradle.kts pins the version; `kpp.frontend.krapper=cpp` in gradle.properties).
//     That is the self-host loop: the previous release's tool builds this one; and
//   * the tool the NEXT release bundles (`-Pkpp.bundleTools.krapper=<path>`).

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksrpc)
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
                    "-lpthread",
                )
                runTask()
            }
            all {
                @OptIn(KotlinNativeCacheApi::class)
                disableNativeCache(
                    version = DisableCacheInKotlinVersion.`2_4_10`,
                    reason = "clikt 5.1.0 duplicate-symbol with cached native libs",
                )
            }
        }
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                    // The generated bindings carry `context(scope: MemScope)` on their
                    // allocating/cleanup-registering members (MemScope-field → context-param
                    // migration), so compiling them needs Kotlin 2.4 context parameters.
                    freeCompilerArgs.add("-Xcontext-parameters")
                }
            }
        }
    }
    sourceSets["commonMain"].dependencies {
        // The shared parse-output model (the Wrapped* element/type tree).
        api(project(":krapper_model"))
        implementation(libs.coroutines.core)
        implementation(libs.serialization.json)
        api(kotlin("stdlib"))
        api(libs.ksrpc.core)
    }
    sourceSets["nativeMain"].dependencies {
        implementation(libs.coroutines.core)
        implementation(libs.serialization.json)
        implementation(libs.clikt)
        api(kotlin("reflect"))
        api(libs.ksrpc.core)
        implementation(libs.ksrpc.sockets)
    }
}

// ---- MAKE THE UNIT TESTS LINK + RUN ----
// klinker owns the EXECUTABLE link (system clang++ + libclang-cpp/LLVM), but the K/N test
// binary is linked by konan's own ld.lld against its bundled old sysroot — which resolves
// neither the `clang::*` symbols the generated cinterop wrapper references nor the modern
// libstdc++ versions libclang-cpp needs. Give the test binary the same libraries by hand:
//   * the system libstdc++.so by ABSOLUTE PATH (not -lstdc++: konan's bundled gcc-8.3 sysroot
//     -L is searched first, so -l would resolve the OLD lib and the newer GLIBCXX symbols
//     would stay undefined; an absolute path can't be shadowed by search order). Discovered
//     from `clang++ -print-file-name=libstdc++.so` and canonicalized — host-portable;
//   * libclang-cpp + libLLVM from the probed LLVM libdir;
//   * --gc-sections to dead-strip the stale platform.linux cinterop cache (the same trick the
//     klinked consumers use), and --allow-shlib-undefined because those shared libraries in
//     turn reference newer glibc symbols konan's old sysroot stub doesn't define (they resolve
//     at RUNTIME against the host glibc). Only SHARED-library undefined refs are relaxed — a
//     genuinely missing symbol in our own objects still fails the link;
//   * -rpath so the test binary finds both at runtime.
// Resolve a host shared library by absolute, canonical path (a GNU-ld linker script or symlink
// is followed to the real .so) — host-portable, nothing hardcoded.
fun hostLibrary(name: String): File {
    val proc =
        ProcessBuilder("clang++", "-print-file-name=$name")
            .redirectErrorStream(true)
            .start()
    val out =
        proc.inputStream
            .readBytes()
            .toString(Charsets.UTF_8)
            .trim()
    proc.waitFor()
    return File(out).canonicalFile.takeIf { it.isFile }
        ?: error("could not resolve $name from `clang++ -print-file-name=$name` (got: '$out')")
}

val systemStdcxxSo = hostLibrary("libstdc++.so")
// The generated wrapper's own objects reference glibc symbols newer than konan's bundled
// sysroot stub (`__libc_single_threaded`, from libstdc++'s inlined shared_ptr refcounting).
// Those are DIRECT undefined refs, so --allow-shlib-undefined does not cover them; link the
// host libc explicitly. Same ABI, and only one libc.so.6 is loaded at runtime anyway.
val systemLibcSo = hostLibrary("libc.so.6")
val llvmLibDir = rootProject.extra.properties["llvmLibDir"]?.toString() ?: "/usr/lib"
val llvmMajor = rootProject.extra.properties["llvmMajor"]?.toString() ?: "22"

kotlin {
    linuxX64("native") {
        binaries.getTest("DEBUG").linkerOpts(
            systemStdcxxSo.absolutePath,
            systemLibcSo.absolutePath,
            "-L$llvmLibDir",
            "-lclang-cpp",
            "-lLLVM-$llvmMajor",
            "--gc-sections",
            "--allow-shlib-undefined",
            "-rpath",
            systemStdcxxSo.parentFile.absolutePath,
            "-rpath",
            llvmLibDir,
        )
    }
}

// The ~60-entry Clang C++ AST slice the front-end walks. Scoped import (only + IGNORE_MISSING)
// keeps the bound surface to the allowlist.
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
        // Namespace nesting. NamespaceBaseDecl is NamespaceDecl's direct base (the Clang 22
        // NamespaceDecl/alias split); it bridges the NamespaceDecl -> NamedDecl upcast chain
        // the same way FunctionDecl bridges CXXMethodDecl's.
        "clang::NamespaceBaseDecl",
        "clang::NamespaceDecl",
        "clang::TagDecl",
        "clang::RecordDecl",
        "clang::CXXRecordDecl",
        // FunctionDecl bridges the CXXMethodDecl -> NamedDecl upcast chain and carries
        // getReturnType()/getNumParams()/getParamDecl() — the signature payload.
        "clang::FunctionDecl",
        "clang::CXXMethodDecl",
        // Real Decl classes for ctors/dtors (libclang only had cursor kinds) —
        // CXXConstructorDecl carries is{Copy,Default}Constructor.
        "clang::CXXConstructorDecl",
        "clang::CXXDestructorDecl",
        "clang::FieldDecl",
        "clang::ValueDecl",
        "clang::DeclaratorDecl",
        "clang::TypeDecl",
        "clang::CXXBaseSpecifier",
        // Parameters. FunctionDecl::getParamDecl(i) returns a ParmVarDecl* — without
        // ParmVarDecl bound, IGNORE_MISSING drops getParamDecl and arguments are unreachable.
        // VarDecl is ParmVarDecl's direct base: it bridges the ParmVarDecl -> DeclaratorDecl/
        // ValueDecl/NamedDecl upcast chain (same reason FunctionDecl is bound for
        // CXXMethodDecl), so a parameter exposes its name + QualType.
        "clang::ParmVarDecl",
        "clang::VarDecl",
        // QualType is the carrier for a decl's type; getAsString() (by-value std::string,
        // normalized to a Kotlin String by #37) spells the leaves.
        "clang::QualType",
        // Structural type construction (TypeBuilder.kt): clang::Type carries the
        // shape-classification predicates (isPointerType/isReferenceType/isRValueReferenceType/
        // isFunctionPointerType/isConstantArrayType/isEnumeralType), getPointeeType(), and the
        // record recovery (getAsCXXRecordDecl).
        "clang::Type",
        // Typedef/alias decode: a sugared QualType's Type dyn-casts to TypedefType, whose
        // getDecl() (a TypedefNameDecl — covers both `typedef` and `using`) provides the alias
        // NAME (the size_t/size_type preservation rules) and getUnderlyingType() (the
        // canonical-collapse recursion). TypeDecl (already bound) bridges the TypedefNameDecl ->
        // NamedDecl upcast chain.
        "clang::TypedefType",
        "clang::TypedefNameDecl",
        // Constant-array decode: ConstantArrayType::getZExtSize() is the extent and ArrayType
        // (its direct base) carries getElementType().
        "clang::ArrayType",
        "clang::ConstantArrayType",
        // Template-specialization decode: a template-typed field/param's canonical record
        // dyn-casts to ClassTemplateSpecializationDecl -> getTemplateArgs() ->
        // TemplateArgument::getAsType() per argument.
        "clang::ClassTemplateSpecializationDecl",
        "clang::TemplateArgument",
        "clang::TemplateArgumentList",
        // Template DECLARATIONS: a `template <typename T> class` is a ClassTemplateDecl whose
        // getTemplatedDecl() is the pattern CXXRecordDecl; getTemplateParameters() lives on
        // TemplateDecl (the primary base, reached by re-view — see ModelBuilder) and yields a
        // TemplateParameterList (size/getParam). TemplateTypeParmDecl is each type param's
        // decl; TemplateTypeParmType is the DEPENDENT `T` in member signatures, whose getDecl()
        // keys the WrappedTemplateRef back to the matching WrappedTemplateParam.
        "clang::TemplateDecl",
        "clang::ClassTemplateDecl",
        "clang::TemplateParameterList",
        "clang::TemplateTypeParmDecl",
        "clang::TemplateTypeParmType",
        // Enums: an enum-typed leaf reaches its EnumDecl through the already-bound
        // Type::getAsTagDecl() + a kind-gated re-view (TypeBuilder). clang::EnumType is
        // deliberately NOT bound: it declares no constructors of its own (only inherited
        // `using TagType::TagType`), so no ctor cursor ever marks hasConstructor and krapper
        // synthesizes a `new clang::EnumType()` default-construct wrapper that C++ rejects
        // (implicitly deleted — generator gap noted on #44). EnumDecl carries getIntegerType()
        // (the underlying type) and its DeclContext holds the EnumConstantDecls. llvm::APSInt
        // is EnumConstantDecl::getInitVal()'s carrier; only its OWN surface is needed
        // (getExtValue — see TypeBuilder's value bridge), so the heavy llvm::APInt base stays
        // unbound.
        "clang::EnumDecl",
        "clang::EnumConstantDecl",
        "llvm::APSInt",
        // Function-pointer typedefs: a typedef over a pointer-to-function-proto decodes through
        // FunctionProtoType — getNumParams()/getParamType() + the inherited getReturnType().
        // Unlike TypedefType, this chain survives resolution intact (the generated
        // Type.asFunctionProtoType() dyn-cast is real); FunctionType is bound as the bridging
        // base.
        "clang::FunctionType",
        "clang::FunctionProtoType",
        // Default arguments: the smallest bindable surface for the default's source text — an
        // inline helper in the slice header wrapping Lexer::getSourceText over
        // ParmVarDecl::getDefaultArgRange() (see clang_slice.h for why Lexer/SourceManager/
        // LangOptions aren't bound wholesale yet).
        "kppbridge::defaultArgText",
        // WrappedTemplateParam.defaultType capture — the TemplateArgumentLoc unwrap for
        // TemplateTypeParmDecl::getDefaultArgument() (see clang_slice.h).
        "kppbridge::defaultArgType",
        // Inline-namespace-preserving qualified names (std::__cxx11::basic_string).
        "kppbridge::qualifiedName",
        // #185: a decl's presumed `file:line:col`, so a dropped binding can be reported
        // against real C++ source (SourceManager/PresumedLoc are not a bindable surface).
        "kppbridge::declLocation",
        // The forcing-parse entry point — buildASTFromCode with REAL driver args ('\n'-joined;
        // -resource-dir + -std), the smallest bridge until std::vector<std::string> params are
        // bindable (see clang_slice.h).
        "kppbridge::buildASTWithArgs",
        // The CIndex GetTemplateArguments mirror — written-args preference + dependent-
        // specialization decode (see clang_slice.h).
        "kppbridge::numTemplateArgs",
        "kppbridge::templateArgAsType",
        "kppbridge::templateBaseName",
    )
    // FIXUPS (documented generator gaps, #44 brick 5): krapper's operator generation emits
    // invalid Kotlin for four of llvm::APSInt's C++ operators —
    //  * operator==(int64_t) / operator<(int64_t): the Long overloads of operators whose APSInt
    //    overload already claimed the idiomatic name (equals/compareTo) are emitted as
    //    `override fun _equals` / `override operator fun _compareTo` (override-nothing + illegal
    //    operator name + pointer-vs-Long argument mixups);
    //  * prefix operator++/operator--: emitted as `operator fun inc(): APSInt?` whose NULLABLE
    //    return violates Kotlin's inc/dec operator convention.
    // None of the four is needed here (the enum value bridge is APSInt::getExtValue(),
    // TypeBuilder.buildEnumType); remove them by uniqueCName until krapper learns
    // mixed-argument operator overloads (generator gap recorded on #44).
    fixup {
        removeMethod("llvm_APSInt_op_eq__long")
        removeMethod("llvm_APSInt_op_lt__long")
        removeMethod("llvm_APSInt_op_increment")
        removeMethod("llvm_APSInt_op_decrement")
        // #122: LLVM-22 added clang::ASTContext::getPredefinedSugarType, whose parameter is the
        // PROTECTED enum clang::Type::PredefinedSugarKind. ASTContext is in the allowlist, so a
        // wrapper cast to the protected enum — `clang::Type::PredefinedSugarKind KD_cast = ...`
        // — is rejected ("protected member of 'clang::Type'").
        //
        // #123 makes this durable: TypeBuilder resolves a protected/private nested tag type to
        // UNRESOLVABLE, so a NEWLY built krapper auto-drops this method (and any future
        // inaccessible-signature method) with no per-symbol fixup. This fixup is retained ONLY
        // as a bootstrap crutch: this module self-generates its own bindings with the tool
        // bundled in the LAST RELEASED plugin, which predates the #123 fix — without the
        // removeMethod that older tool re-emits the broken cast and the sync aborts. Drop this
        // line once the consumed plugin is past #123.
        removeMethod("clang_ASTContext_get_predefined_sugar_type")
    }
    // Materialize the range returns the walk iterates. The member walk goes through
    // DeclContext::decls() (source order, all member kinds), so no CXXMethodDecl vector.
    instantiate("std::vector<clang::Decl*>")
    instantiate("std::vector<clang::CXXBaseSpecifier*>")
}

// ---- #101: NONCOPYABLE special-member determinism guard ----
// Runs the tool's `--noncopyable-determinism` mode, which parses a self-contained reduction of
// v8::Persistent's NonCopyable shape N times (separate ASTContexts) and asserts the
// special-member set is byte-identical and complete across parses — the standing regression
// lock for the Persistent<v8::Value> parse-dependent copy-member report (a non-zero exit fails
// the task).
tasks.register<Exec>("noncopyableDeterminismCheck") {
    dependsOn("linkReleaseExecutableKlinker")
    commandLine(
        layout.buildDirectory
            .file("bin/klinker/krapperRelease/krapper")
            .get()
            .asFile
            .absolutePath,
        "--noncopyable-determinism",
    )
}
