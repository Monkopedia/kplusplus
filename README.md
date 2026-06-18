# K++

K++ lets you call C++ libraries from Kotlin/Native. It parses C++ headers and generates the
C-interop wrappers automatically — handling types, templates, references, and ownership so you
don't have to write them by hand.

> **Status:** K++ now **self-hosts** — it parses C++ using bindings it generated of Clang's own
> C++ AST, rather than the C `libclang` API it used to depend on. One consequence: building K++
> (and the C++ front-end it ships) requires an LLVM/Clang toolchain. See
> [docs/clang-runbook.md](docs/clang-runbook.md). The high-level story is in
> [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Quickstart

Apply the Gradle plugin:

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.monkopedia.kplusplus.compiler")
}
```

Point it at the C++ library you want to bind:

```kotlin
kplusplus {
    header("../include/mylib.h")      // the header(s) to bind
    headerDirectory("../include/")    // extra -I include roots
    library("../libmylib.a")          // the native library to link

    cppStandard = "c++17"             // default c++14

    // Force template specializations you use (C++ instantiates templates at
    // compile time, so each concrete type you call needs to be requested).
    instantiate("std::vector<int>")
}
```

Build, and the generated Kotlin bindings are on the classpath — call your C++ API directly.

Runnable samples live under [`samples/`](samples). Start with
[`samples/minimal`](samples/minimal) — a tiny hand-written C++ geometry library bound and run
end-to-end (no stdlib, no monolith), the fastest first contact. For the heavyweight demo that
binds and executes **V8** (Google's JavaScript engine), see [`samples/v8`](samples/v8); it links
the ~62 MB `libv8_monolith.a`. Run either with `./gradlew runReleaseExecutableKlinker -PenableClang`.

### Fixups

Real libraries have a few shapes the generator can't bind cleanly on its own. Rather than
hand-edit generated code, you adjust the model declaratively in a `fixup { }` block:

```kotlin
kplusplus {
    header("../include/v8-combined.h")
    headerDirectory("../include/")
    library("../libv8_monolith.a")
    cppStandard = "c++17"

    fixup {
        // Strip a leading `const` from value-semantic wrapper returns.
        stripConstFromReturnType("v8::Local<")
        stripConstFromReturnType("v8::Maybe<")

        // Drop specific methods (by their generated uniqueCName) that produce
        // uncompilable wrappers — e.g. copy ops of a NonCopyable type.
        removeMethod("v8_Persistent_v8_Value_op_assign")

        // Built-in fixups for known v8 / smart-pointer patterns.
        addUniquePtrGet()
        scriptOriginOptionsFix()
    }
}
```

Other knobs: `referencePolicy` (`IGNORE_MISSING` / `INCLUDE_MISSING`), `only(...)` / `onlyFile(...)`
to scope the bound surface, `noRtti` for `-fno-rtti` libraries, `rootPackage` for the generated
package root, and `compiler` to override the C++ compiler.

## How it works

K++ runs a three-stage pipeline:

1. **Parse** — the C++ front-end (`cppfrontend`) reads the headers and produces a structural model
   of the declarations and types. This front-end is itself built from K++-generated bindings of
   Clang's C++ AST (the self-hosting part).
2. **Resolve** — `krapper_gen` takes that model, makes sure every referenced type is materialized,
   forces the requested template instantiations, and applies your `fixup { }` directives.
3. **Generate** — it emits a C/C++ wrapper, compiles it into a static library, and generates the
   Kotlin/Native cinterop bindings that call into it.

The `com.monkopedia.kplusplus.compiler` Gradle plugin wires all of this into the Kotlin/Native
build so it runs as part of normal compilation. K++ is frequently paired with
[klinker](https://github.com/Monkopedia/klinker) for the final native link.

## Repository layout

| Module | What it is |
|--------|------------|
| `krapper_gen/` | The core generator: resolve + codegen, driven by the parsed model. |
| `krapper_model/` | The shared parse-output model (the data both stages speak). |
| `cppfrontend/` | The self-hosted C++ front-end (parses headers → model), built on generated Clang-AST bindings. |
| `compiler/` | The v2 Kotlin/Gradle compiler plugin (`com.monkopedia.kplusplus.compiler`). |
| `featuregen/`, `feature-tests/` | Test harnesses: the generated-binding feature suite, and the raw-cinterop baseline. |
| `cppfixture/` | A small standing test that the generic front-end path works on a non-stdlib module. |
| `clangwalk/` | A self-host proof: walks a real Clang AST entirely on generated bindings. |
| `samples/minimal/` | A tiny standalone sample binding a hand-written C++ geometry library — the newcomer's first contact. |
| `samples/v8/` | The heavyweight V8 sample. |

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full picture and
[docs/features.md](docs/features.md) for the supported-C++-feature matrix.

## Limitations

- **Last-mile fixups.** Binding a real library still needs a handful of `fixup { }` directives for
  shapes that don't map cleanly. The goal is to make that last mile small and declarative, not to
  eliminate it.
- **Template instantiations are explicit.** Because C++ specializes templates at compile time, each
  concrete specialization you call must be requested via `instantiate(...)` (or referenced from a
  bound signature). There's no automatic discovery of arbitrary specializations.
- **Unbindable shapes are dropped, not faked.** When the generator hits a construct it can't model
  faithfully, it skips it (and logs) rather than emit something that won't compile.

## Background

This started because I wanted some Kotlin/Native code to call into V8, and assumed it'd be easy —
Kotlin/Native has C interop, after all. But V8 has no C API, and Kotlin/Native only interops with C
and Objective-C, not C++. I went looking for a general way to bridge C++ and found nothing
generalizable — just instructions for hand-writing a few wrapper methods. So I tried to do it
myself.

Each layer revealed a harder one: parsing out type information to handle constants, pointers, and
references; then the special handling for native types and strings; then templates. I'd hoped to
shelve templates, but hadn't reckoned with how much of C++ is STL, or how fundamentally C++
templates (compile-time specialization) differ from Kotlin generics. Early attempts hardcoded
pieces of STL; that obviously couldn't scale to, say, a template method returning a template type
that might be native, a pointer, a const, or some combination.

The revival came from giving up on solving it for *all* libraries automatically, and instead
solving most of it for all of them — with light, well-tooled manual intervention for the last mile.
That's the three-stage parse / resolve / generate design, with customization concentrated at the
resolve stage (the `fixup { }` block). The most recent chapter took it further still: K++ replaced
its dependency on the C `libclang` API by generating its own bindings of Clang's full C++ AST — so
the tool now parses C++ using a binding it produced itself.
