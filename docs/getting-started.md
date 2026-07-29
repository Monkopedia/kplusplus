# Getting started with K++

This walks a **real external consumer** from an empty Kotlin/Native project to running Kotlin
that calls a C++ library — using the **published** K++ plugin (no `includeBuild`, no cloning the
K++ repo). If you want to read a finished version of exactly this, see
[`samples/minimal`](../samples/minimal).

## Prerequisites

- **JDK 21** to run the Gradle build.
- **An LLVM/Clang 22 toolchain.** K++'s front-end is self-hosted on Clang: it parses your headers
  with a bundled Clang. `clang++` must be on `PATH` (it locates Clang's builtin headers and also
  compiles the generated C++ wrapper). If your LLVM-22 install is *not* on the default path (e.g.
  an apt.llvm.org install under `/usr/lib/llvm-22`), see [Pointing K++ at your LLVM](#pointing-k-at-your-llvm)
  below.
- A C++ library to bind (headers + a `.a`/`.so`), or just hand-write a small one as the sample does.

You do **not** need to build K++ from source: the `krapper` tool binary rides bundled inside the
published plugin jar; the plugin extracts and runs it itself. (The LLVM toolchain requirement
inside the K++ repository does not apply to a from-published consumer — it only needs LLVM present
at *runtime*, since the bundled `krapper` is LLVM-linked.)

## 1. Declare the plugin

`settings.gradle.kts` — the plugin resolves from the Gradle Plugin Portal / Maven Central:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

`build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.0"
    id("com.monkopedia.kplusplus.compiler") version "0.3.4"
}

repositories {
    mavenCentral()
}
```

In a **multi-project** build, apply this in whichever project owns the bindings; the plugin
does not need to be declared at the root alongside the Kotlin plugin. (Under **0.3.4 only** it
did — the plugin's own runtime classes were shadowed by whatever the root buildscript
classloader owned, and the first `kplusplusSync` failed with
`NoSuchMethodError: Job.invokeOnCompletion$default`. Adding
`id("com.monkopedia.kplusplus.compiler") apply false` to the root `plugins { }` block works
around it on 0.3.4; upgrading is the real fix. See
[#194](https://github.com/Monkopedia/kplusplus/issues/194).)

## 2. Configure the binding

Add a `kplusplus { }` block pointing at the header(s) and library you want to bind:

```kotlin
kplusplus {
    header("cpp/geometry.h")            // the header(s) to bind
    library("build/cpplib/libgeometry.a") // the native library to link
    cppStandard = "c++17"               // default is c++14
    // headerDirectory("cpp/include")   // extra -I roots for your headers, if any
    // instantiate("std::vector<int>")  // force template specializations you call
}
```

Wire the native binary so it links against the system libstdc++ (the front-end parses against it),
build the `.a`, and add a `main` that calls the generated API. The complete, runnable version of
all of this is [`samples/minimal/build.gradle.kts`](../samples/minimal/build.gradle.kts) — copy it
as your starting point.

## 3. Build and run

```
./gradlew runReleaseExecutableKlinker
```

The first build is slow (it downloads the plugin + bundled tools, generates the bindings, then
compiles and links the generated C++ wrapper); later runs are incremental.

## Pointing K++ at your LLVM

The cpp front-end finds Clang's own builtin headers (`stddef.h` etc.) via `clang++` on `PATH`.
Your **library's** headers under a versioned LLVM install (e.g. `<clang/AST/...>` under
`/usr/lib/llvm-22/include`) are on the default include search path only for a *system* install. If
your LLVM-22 is a side-by-side / apt.llvm.org install, the plugin needs to know where its headers
are.

By default the plugin auto-discovers this by running `llvm-config --includedir` on `PATH` (the same
probe the K++ repo's own build uses) and threads that directory into the parse. If `llvm-config`
is not on `PATH`, or points at the wrong install, override it — in order of precedence:

1. The DSL, in your `kplusplus { }` block:

   ```kotlin
   kplusplus {
       // ...
       llvmConfig = "/usr/lib/llvm-22/bin/llvm-config"
   }
   ```

2. Or the project property, on the command line / in `gradle.properties`:

   ```
   ./gradlew runReleaseExecutableKlinker -PllvmConfig=/usr/lib/llvm-22/bin/llvm-config
   ```

When a configured `llvm-config` is not an executable, or does not report a valid include directory,
the build fails immediately with a message telling you exactly what to set — rather than surfacing a
cryptic `file not found` deep in the parse. When `llvm-config` reports a default system path (e.g.
`/usr/include`), nothing extra is added: Clang already searches it.

## See also

- [`samples/minimal`](../samples/minimal) — the smallest end-to-end binding, a working copy of this guide.
- [`samples/multiproject`](../samples/multiproject) — the same thing as two Gradle projects, with the
  Kotlin plugin declared at the root.
- [`samples/v8`](../samples/v8) — the heavyweight demo (binds and runs V8).
- [README](../README.md) — the `kplusplus { }` DSL reference and `fixup { }` escape hatch.
- [docs/ARCHITECTURE.md](ARCHITECTURE.md) — how the self-hosted front-end and generator fit together.
