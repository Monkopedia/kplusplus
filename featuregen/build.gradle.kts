// Generator-backed feature-test harness. Unlike :feature-tests (pure cinterop),
// this module runs the real v2 flow: the kplusplus compiler plugin + krapper_gen
// wrap a C++ header into Kotlin bindings, and the tests exercise *those*. This
// is where std::-type rows (Group B in the matrix) get validated.
//
// Two-stage build, as established: `./gradlew :featuregen:kplusplusSync` first
// (generates krapped/featuregen.def + krapped/src), then the normal build picks
// the generated bindings up.

plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler")
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
                    // Generated bindings now carry `context(scope: MemScope)` on their
                    // allocating/cleanup-registering members (MemScope-field → context-param
                    // migration), so compiling them needs Kotlin 2.4 context parameters.
                    freeCompilerArgs.add("-Xcontext-parameters")
                }
            }
        }
        binaries {
            // tests only
        }
    }
}

kplusplus {
    // Header-only C++ surface under test (inline functions, so krapper_gen's
    // generated wrapper compiles them in — no separate impl library).
    header("src/cppMain/include/strings_feature.h")
    // Stays on the c++14 default: bumping to c++17 (to reach std::string_view)
    // both fails to wrap the view type AND drops std::string's const char*
    // constructor — see ST-stringview-in / Known issues. The cppStandard knob
    // exists and works; it's just not a clean win for these rows yet.
    //
    // Seed the user template `Box<T>` instantiations: their Kotlin facade is itself
    // generated, so the compiler can't request them via SYNC_REQUIRED (see the
    // instantiate() KDoc). One per featuregen Box test.
    instantiate("Box<int>")              // UT-box: primitive arg
    instantiate("Box<Point>")            // UT-box-point: user-type arg -> Box__Point
    instantiate("Box<Box<int>>")         // UT-box-nested: nested -> Box<Box__Int>()
    instantiate("Pair2<int, double>")    // UT-pair2: 2-type-param user template -> Pair2__Int__Double
    instantiate("std::unique_ptr<int>")  // SP-unique: get/release/operator-> via pointer typedef
    // RG-range (T1.3): RangeHolder::items() is materialized into std::vector<Thing*>.
    // Like the Box facade, this vector instantiation is generated on demand by the
    // range rewrite (not via a Kotlin call site), so it can't be discovered through
    // SYNC_REQUIRED — seed it here so the container binding exists.
    instantiate("std::vector<Thing*>")
}

// ---- cpp-front-end run link (under -Pkpp.frontend.featuregen=cpp) ----
// Under that property the kplusplus plugin points this module's cinterop + generated sources
// at build/krapped-cpp and GENERATES them from THIS module's own kplusplus{} config inside
// kplusplusSync (krapper_parse parse -> ModelIo -> krapper_gen), auto-wiring
// :krapper_parse:linkReleaseExecutableKlinker. What remains module-specific (below) is purely
// the RUN link, not the generation:
if (providers.gradleProperty("kpp.frontend.featuregen").orNull == "cpp") {
    // MAKE THE TESTS LINK + RUN. The cpp front-end parses featuregen's surface against the
    // SYSTEM libstdc++ (gcc-16 here), so the
    // faithful generated wrapper references newer-libstdc++ internals
    // (std::__throw_bad_array_new_length@GLIBCXX_3.4.29,
    // std::__glibcxx_assert_fail@GLIBCXX_3.4.30, std::__size_to_integer). K/N's default
    // test link resolves libstdc++ from its BUNDLED gcc-8.3 sysroot, which caps at
    // GLIBCXX_3.4.25 → those symbols are undefined and linkDebugTestNative fails. The
    // system libstdc++ HAS them and is backward-ABI-compatible (it provides every older
    // GLIBCXX version too). Point K/N's own linker at it: link the REAL system
    // libstdc++.so by ABSOLUTE PATH (not -L/-lstdc++ — konan's bundled gcc-8.3 sysroot
    // -L is searched first, so `-lstdc++` would resolve to the OLD lib and the newer
    // symbols would stay undefined; an absolute path can't be shadowed by search order),
    // --gc-sections to dead-strip the stale platform.linux cinterop cache (the same trick
    // the klinked clang consumers use — see clangwalk/build.gradle.kts), and
    // -rpath/-rpath-link so the test binary loads that .so at runtime (its SONAME
    // libstdc++.so.6 wins over the bundled one via the rpath). The .so is DISCOVERED at
    // configure time from `clang++ -print-file-name=libstdc++.so` (a GNU-ld linker
    // script/symlink), canonicalized to the real shared object — host-portable, not
    // hardcoded. This is ENTIRELY under the property guard: the default
    // `./gradlew :featuregen:nativeTest` never enters this block, so the standard test
    // link path is untouched.
    val systemStdcxxSo: File = run {
        val proc = ProcessBuilder("clang++", "-print-file-name=libstdc++.so")
            .redirectErrorStream(true).start()
        val out = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
        proc.waitFor()
        File(out).canonicalFile.takeIf { it.isFile }
            ?: error(
                "#78: could not resolve the system libstdc++.so from " +
                    "`clang++ -print-file-name=libstdc++.so` (got: '$out')"
            )
    }
    val systemStdcxxLibDir = systemStdcxxSo.parentFile.absolutePath
    kotlin {
        linuxX64("native") {
            binaries.getTest("DEBUG").linkerOpts(
                systemStdcxxSo.absolutePath,
                "--gc-sections",
                // The system libstdc++.so.6 in turn references NEWER glibc symbols
                // (pthread_*@GLIBC_2.34 — the libpthread→libc merge, strfromf128@GLIBC_2.26,
                // __isoc23_*@GLIBC_2.38) that konan's bundled old-sysroot glibc stub doesn't
                // define, which --no-allow-shlib-undefined (K/N's default) rejects. These
                // are resolved at RUNTIME by the host glibc (the test runs on this box, the
                // same toolchain that parsed the bindings), so relax the shlib check.
                // Crucially this only relaxes undefined refs coming from SHARED LIBRARIES —
                // the generated libfeaturegen.a archive's own symbols are still fully
                // checked, so a genuinely missing binding symbol would still fail the link.
                "--allow-shlib-undefined",
                "-rpath",
                systemStdcxxLibDir,
                "-rpath-link",
                systemStdcxxLibDir
            )
        }
    }
}
