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

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}
plugins {
    kotlin("multiplatform") version "2.4.0"
    // The kplusplus compiler subplugin, resolved from its published mavenLocal marker
    // (R2, #128) — no includeBuild. Everything kplusplus-specific lives in the narrow
    // `kplusplus { ... }` block at the bottom of this file.
    id("com.monkopedia.kplusplus.compiler") version "0.3.4"
    id("com.monkopedia.klinker.plugin") version "0.2.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

// ---- Build the C++ static library that the bindings wrap ----------------------
// The plugin's `library(...)` consumes a pre-built `.a`. The geometry library is
// hand-written here in cpp/, so compile it ourselves: cpp/geometry.cc -> an object
// -> build/cpplib/libgeometry.a, using the SAME clang++ the front-end parses with.
// kplusplusSync is wired to depend on this below so the archive always exists first.
val cppLibDir = layout.buildDirectory.dir("cpplib").get().asFile
val geometryArchive = File(cppLibDir, "libgeometry.a")
val compileGeometryLib = tasks.register<Exec>("compileGeometryLib") {
    group = "kplusplus"
    description = "Compile cpp/geometry.cc into a static libgeometry.a for the bindings."
    val source = file("cpp/geometry.cc")
    inputs.file(source)
    inputs.file(file("cpp/geometry.h"))
    outputs.file(geometryArchive)
    val objFile = File(cppLibDir, "geometry.o")
    doFirst { cppLibDir.mkdirs() }
    // Two steps in one task: clang++ -c (compile) then ar rcs (archive). -fPIC so the
    // object can be linked into K/N's position-independent output.
    commandLine(
        "sh", "-c",
        "clang++ -std=c++17 -c -fPIC '${source.absolutePath}' -o '${objFile.absolutePath}' && " +
            "ar rcs '${geometryArchive.absolutePath}' '${objFile.absolutePath}'"
    )
}

// ---- LINK the executable against the SYSTEM libstdc++ -------------------------
// PORT of the v8 sample's recipe (see samples/v8/build.gradle.kts). The cpp front-end
// (kpp.frontend.minimal=cpp) parses geometry.h against the SYSTEM libstdc++, so the
// generated wrapper may reference newer-libstdc++ internals that K/N's BUNDLED gcc-8.3
// sysroot cannot resolve. Point K/N's linker at the REAL system libstdc++.so by ABSOLUTE
// PATH (NOT -lstdc++ — konan's bundled-sysroot -L is searched first and would shadow to
// the OLD lib). The .so is DISCOVERED at configure time from `clang++ -print-file-name`
// (a GNU-ld linker script/symlink), canonicalized to the real shared object — host-
// portable, not hardcoded. Same mechanism clangwalk/krapper/featuregen/v8 all use.
val systemStdcxxSo: File = run {
    val proc = ProcessBuilder("clang++", "-print-file-name=libstdc++.so")
        .redirectErrorStream(true).start()
    val out = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    proc.waitFor()
    File(out).canonicalFile.takeIf { it.isFile }
        ?: error(
            "could not resolve the system libstdc++.so from " +
                "`clang++ -print-file-name=libstdc++.so` (got: '$out')"
        )
}
val systemStdcxxLibDir = systemStdcxxSo.parentFile.absolutePath

kotlin {
    val hostOs = System.getProperty("os.name")
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
                // The libgeometry.a link comes from the kplusplus { library(...) } entry
                // below. Here we add the system-libstdc++ link recipe (see the block above
                // the `kotlin {` for the full WHY): the REAL libstdc++.so by ABSOLUTE PATH,
                // plus dead-strip + shlib-undefined relaxation + rpath.
                linkerOpts(
                    systemStdcxxSo.absolutePath,
                    // Dead-strip the stale platform.linux cinterop cache (one section per
                    // wrapper) — the same trick the klinked clang consumers use.
                    "--gc-sections",
                    // The system libstdc++.so.6 references NEWER glibc symbols than konan's
                    // bundled old-sysroot glibc stub defines; they resolve at RUNTIME via the
                    // host glibc. This ONLY relaxes undefined refs from SHARED LIBRARIES — the
                    // generated wrapper's own symbols (and libgeometry.a) stay fully checked.
                    "--allow-shlib-undefined",
                    // Load the system .so at runtime via rpath (its SONAME wins over bundled).
                    "-rpath",
                    systemStdcxxLibDir,
                    "-rpath-link",
                    systemStdcxxLibDir
                )
                compilerOpts("-lm")
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

    // `:nativeTest` (src/nativeTest/kotlin) asserts the generated bindings actually
    // behave as documented, and — just as importantly — forces src/nativeMain to
    // compile as part of an ordinary `check`/test run. This is the guard for #195:
    // samples/minimal is a from-published consumer (resolves the released plugin, not
    // the in-tree generator), so it can't catch a generated-shape change at the moment
    // the generator changes — only at the moment this sample next builds against a new
    // plugin version. Wiring nativeTest in means that moment fails loudly instead of
    // silently, the way the `rect.origin = …` break in this issue did.
    sourceSets["nativeTest"].dependencies {
        implementation(kotlin("test"))
    }
}

// The bindings can only be generated after libgeometry.a exists: kplusplusSync compiles
// and links the generated C++ wrapper against the library, so build the archive first.
tasks.matching { it.name == "kplusplusSync" }.configureEach {
    dependsOn(compileGeometryLib)
}

// ---- The entire kplusplus configuration --------------------------------------
// A tiny, fixup-free binding: the geometry header is plain structs + value methods,
// so it binds clean with no fixup { } block at all.
kplusplus {
    header("cpp/geometry.h")
    library("build/cpplib/libgeometry.a")
    cppStandard = "c++17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
