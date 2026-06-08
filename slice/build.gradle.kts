import com.monkopedia.klinker.klinkedExecutable

plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler")
    id("com.monkopedia.klinker.plugin") version "0.2.0"
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
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
                }
            }
        }
    }
}

// Proves the configurable root package end-to-end: every generated binding nests
// under this package (so the std container bindings land in
// `com.monkopedia.slice.std.*`), and the FIR plugin resolves cppVector<T>() to them.
kplusplus {
    rootPackage = "com.monkopedia.slice"
}
