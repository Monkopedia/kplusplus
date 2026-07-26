# Architecture

K++ turns a C++ library into Kotlin/Native bindings. This document is the map: how the pieces fit,
how data flows, and where to look for what.

## The pipeline

A build goes through three stages:

```
   C++ headers                                                   your Kotlin code
        │                                                              ▲
        ▼                                                              │
 ┌──────────────────────────────────────────────┐   wrapper .a + bindings
 │                   krapper                    │ ──────────┘
 │  parse  ──(in-process model)──▶  resolve + generate  │
 └──────────────────────────────────────────────┘
```

One tool, `krapper/`, runs three stages in one process:

1. **Parse.** Reads the headers and builds a structural model of the declarations and types (a
   `krapper_model` tree). The front-end is itself built from K++-generated bindings of Clang's
   C++ AST — this is the self-hosting part (see below).
2. **Resolve.** Ensures every referenced type is materialized, forces the requested template
   instantiations, and applies the `fixup { }` directives from the build script.
3. **Generate.** Emits a C/C++ wrapper, compiles it into a static library, and generates the
   Kotlin/Native cinterop bindings that call into it.

The parse → resolve handoff is a plain in-process object. It used to cross a process boundary
between two binaries as `ModelIo` JSON; that serialization survives only as the optional
`--dump-model <dir>` debug dump (`-Pkpp.dumpModel` from the build).

The `com.monkopedia.kplusplus.compiler` Gradle plugin (`compiler/`) wires all of this into the
Kotlin/Native build so it runs as part of normal compilation.

## Modules

| Module | Role |
|--------|------|
| `krapper/` | The tool — one binary. `parser/` is the self-hosted C++ front-end (walks a real Clang AST, on generated Clang-AST bindings, into a `krapper_model` tree); the rest resolves references + template forcings, applies fixups, and generates the C++ wrapper + Kotlin bindings (`generator/codegen/`). |
| `krapper_model/` | The shared data model the stages speak: the `Wrapped*` element/type tree and its `ModelIo` (de)serialization. Pure data, no parsing or codegen. |
| `compiler/` | The v2 Kotlin/Gradle compiler plugin (`com.monkopedia.kplusplus.compiler`): the `kplusplus { }` DSL, the FIR integration, and the orchestration that runs krapper during a build. |
| `featuregen/` | The primary correctness harness: generates bindings for a broad surface of C++ features and runs behavioral tests against them (the self-hosted suite, ~188 tests). |
| `feature-tests/` | The raw-cinterop baseline: hand-written `.def` files compiled directly, no generator. Proves the C++ interop works before the generator is in the picture. Its `matrixReport` task drives [features.md](features.md). |
| `cppfixture/` | A small standing test that the generic front-end path works on a non-stdlib module (a regression guard for the generation orchestration). |
| `clangwalk/` | A self-host proof: parses C++ and walks the Clang AST entirely on generated bindings, with assertions. The smallest end-to-end demonstration that the bootstrap works. |
| `samples/minimal/` | A tiny standalone sample (not in the main build) binding a hand-written C++ geometry library — no stdlib, no monolith; the newcomer's first contact. |
| `samples/v8/` | The V8 sample (standalone; not in the main build). Binds and runs Google's V8 JavaScript engine. |

## Self-hosting and the build-time LLVM dependency

K++ used to parse C++ with the C `libclang` API. It now parses with **bindings it generated of
Clang's C++ AST** — `krapper` is a K++ consumer of its own output. The tool produces the parser it
runs on.

Two practical consequences:

- **LLVM/Clang is required to build.** LLVM is a hard requirement: the root `settings.gradle.kts`
  probes the toolchain unconditionally and fails fast with an actionable message if it's absent —
  full project, or a clear error, no opt-out. `-PllvmConfig=<path>` is a location override only.
  See [clang-runbook.md](clang-runbook.md) for toolchain setup.
- **Committed "seed" bindings + `-Pkpp.frontend.<module>=…`.** Some modules carry committed
  generated bindings (`<module>/krapped/`) so the tree can bootstrap without first running the
  generator. A module's source is selected per build:
  - `=cpp` — regenerate the bindings by running `krapper`.
  - `=seed` — build from the committed bindings (just recompiles the C++ wrapper; no parse).

  This is how a freshly-checked-out tree converges: laggard modules build from seed, the cpp-ready
  ones regenerate. The defaults live in the root `gradle.properties`.

The longer story of how this came to be — the phased migration off `libclang` (internally "the
flip") — is logged in [campaigns/self-hosting.md](campaigns/self-hosting.md). That's a historical
progress record, not a guide; this file is the current map.

## Where to look for…

- **Codegen** (how a C++ method becomes Kotlin + a C++ wrapper): `krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/codegen/` (`KotlinWriter.kt`, `CppWriter.kt`).
- **The model** (the `Wrapped*` tree, type reduction): `krapper_model/` and `krapper/.../generator/model/`.
- **The front-end** (Clang-AST walk → model): `krapper/src/nativeMain/kotlin/com/monkopedia/krapper/parser/` (`CppParser.kt`, `ModelBuilder.kt`, `TypeBuilder.kt`).
- **The plugin DSL** (`kplusplus { }`): `compiler/gradle/src/main/kotlin/com/monkopedia/kplusplus/compiler/gradle/` (`KPlusPlusExtension.kt`, `KPlusPlusCompilerGradlePlugin.kt`).
- **What C++ features are supported**, and their test status: [features.md](features.md).
- **How to build the LLVM-gated modules**: [clang-runbook.md](clang-runbook.md).
