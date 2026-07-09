# samples/minimal

The smallest end-to-end kplusplus sample: a tiny hand-written C++ geometry library bound
to Kotlin/Native and run. No standard-library template surface, no 62 MB monolith — the
fastest first contact with kplusplus.

This is a **true from-published consumer** (#128, R2): it declares the plugin by version and
`mavenLocal()` — **no `includeBuild`**. The plugin marker, the FIR plugin, and the
`krapper_gen` / `krapper_parse` tool binaries are all resolved by Maven coordinate from
mavenLocal. This is the same standalone path k++ uses to self-host as its own first consumer.

## What it binds

[`cpp/geometry.h`](cpp/geometry.h) — two plain structs in `namespace geo`:

- `Point { double x, y; double distanceTo(const Point&) const; }`
- `Rect  { Point origin; double width, height; double area() const; Point center() const; }`

Plain fields plus by-value / const-ref methods. No templates, no STL includes — so it binds
with **no `fixup { }` block at all**. The implementation is in
[`cpp/geometry.cc`](cpp/geometry.cc); the build compiles it to a static `libgeometry.a`
(the `compileGeometryLib` task) before generating the bindings.

The generated Kotlin API and the `main` that drives it live in
[`src/nativeMain/kotlin/Main.kt`](src/nativeMain/kotlin/Main.kt).

## Run it

First publish the kplusplus consumer set to your local Maven repo (from the repo root, once):

```
./gradlew publishAllToMavenLocal -PenableClang
```

Then, from this directory:

```
./gradlew runReleaseExecutableKlinker
```

No `-PenableClang` is needed here: a standalone consumer has no in-tree LLVM-gated modules;
the `krapper_parse` tool is resolved as a published binary from mavenLocal (`-PenableClang`
only gates modules inside the kplusplus root build, which this consumer does not include).

Expected output:

```
Rect area: 6.0
distance (0,0)->(3,4): 5.0
center: (1.0, 1.5)
```

## Requirements

kplusplus's front-end is **self-hosted on Clang** (it parses your headers on
generated Clang-AST bindings). The published `krapper_parse` tool binary is LLVM-linked, so a
**LLVM/Clang toolchain must be present at runtime**; `clang++` must be on `PATH` — it both
parses the header and compiles `cpp/geometry.cc`. JDK 21 is required to run the build.

The first build is slow (it builds the self-hosted front-end, generates the bindings, then
compiles and links the generated C++ wrapper); later runs are incremental.

## See also

- The repository [README](../../README.md) — quick start and the `kplusplus { }` DSL.
- [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md) — how the self-hosted front-end and
  generator fit together.
- [samples/v8](../v8) — the heavyweight demo (binds and runs Google's V8 engine).
