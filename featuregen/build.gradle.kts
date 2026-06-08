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
