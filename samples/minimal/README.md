# samples/minimal

The smallest end-to-end kplusplus sample: a tiny hand-written C++ geometry library bound
to Kotlin/Native and run. No standard-library template surface, no 62 MB monolith — the
fastest first contact with kplusplus.

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

```
./gradlew runReleaseExecutableKlinker -PenableClang
```

Expected output:

```
Rect area: 6.0
distance (0,0)->(3,4): 5.0
center: (1.0, 1.5)
```

## Requirements

kplusplus's front-end is **self-hosted on Clang** (it parses your headers on
generated Clang-AST bindings), so a build-time **LLVM/Clang toolchain is required** and the
front-end is gated behind `-PenableClang`. `clang++` must be on `PATH` — it both parses the
header and compiles `cpp/geometry.cc`. JDK 21 is required to run the build.

The first build is slow (it builds the self-hosted front-end, generates the bindings, then
compiles and links the generated C++ wrapper); later runs are incremental.

## See also

- The repository [README](../../README.md) — quick start and the `kplusplus { }` DSL.
- [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md) — how the self-hosted front-end and
  generator fit together.
- [samples/v8](../v8) — the heavyweight demo (binds and runs Google's V8 engine).
