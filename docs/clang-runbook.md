# Clang self-bootstrap runbook

Operational setup for building and running the clang self-bootstrap modules (`:clangwalk`,
`:cppfrontend`). These are the stage1 consumers that bind Clang's own C++ AST
(`libclang-cpp`) and run on kplusplus-generated bindings. They are **off by default** and
require an LLVM/Clang toolchain a plain build host won't have. The campaign strategy/log
lives in [`clang-bootstrap.md`](clang-bootstrap.md); this is the how-to-build companion.

## 1. LLVM/Clang toolchain

The clang modules link against **LLVM 22** (pinned `22.1.6` — LLVM's C++ API is
version-unstable, so the bindings are regenerated on a bump). The major version is **not**
hardcoded: `settings.gradle.kts` probes the toolchain once via `llvm-config` and threads
the discovered major (`--version`) and libdir (`--libdir`) to both modules, so a `22 → 23`
bump or a non-`/usr/lib` install needs no per-module edit.

What must be present when the modules are enabled:

- `llvm-config` and `clang++` on `PATH` (or point at a specific `llvm-config` with
  `-PllvmConfig=<exec>` — an absolute/relative path is taken verbatim, a bare name is
  searched on `PATH`, e.g. a versioned `llvm-config-22`).
- The shared libs the final link resolves: `libclang-cpp.so` and `libLLVM-<major>.so`
  under `llvm-config --libdir`.

### Enabling the modules

The clang modules are gated two equivalent ways:

- **`-PenableClang`** (or `enableClang=true`) — presence-based opt-in; any value other than
  `false` enables.
- **`-PllvmConfig=<path>`** — both an opt-in (its presence enables the modules) *and* the
  override that points the probe at a specific `llvm-config`.

Without either, `settings.gradle.kts` never includes `:clangwalk`/`:cppfrontend`, so the
default `./gradlew` build needs no LLVM and stays green everywhere.

### Failure behavior (hardened in #11b)

When the modules are enabled but the toolchain is missing, the build fails **at configure
time** with an actionable `GradleException` naming exactly what's missing (`llvm-config`
and/or `clang++`) — instead of a cryptic `cannot find -lLLVM-22` linker error ~8 minutes
into a release link. If `llvm-config` and `clang++` resolve but neither `libLLVM-<major>.so`
nor `libclang-cpp.so` is found under the reported libdir, the build prints a soft `WARNING`
(distros name the monolithic lib differently, so the linker stays the authority) and
continues.

## 2. Why `clang++`, not the konan GCC toolchain

The clang modules set `compiler = "clang++"` in their `kplusplus {}` block. Clang's AST
headers **must** be compiled with `clang++`, not the Kotlin/Native-bundled GCC 8.3 that
`resolveDefaultCompiler` picks by default: GCC-8.3 / its libstdc++ aborts on those headers
(exit 134). This is a hard requirement, not a preference.

## 3. Release builds, not debug

The gated verification tasks all build and run the **release** binaries
(`cppfrontendRelease`, `krapper_gen` `releaseExecutable`, `linkReleaseExecutableKlinker`,
`runReleaseExecutableKlinker`).

Background: klinker links the executable with the **system `clang++`** (needed for modern
`libclang-cpp`'s glibc/libstdc++ symbol versions, absent from K/N's old bundled sysroot).
K/N's *debug* build pulls in the precompiled `platform.linux` cinterop cache whole (no
whole-program DCE), and it references dozens of obsolete glibc symbols modern glibc removed
(`__argp_parse`, `__argz_*`, `cfree`, `getmsg`, …) → ~111 undefined references. Release dead-
strips the unreferenced functions via `-opt`, so it linked from day one.

> **Note (corrects the older "debug can't link" framing):** the build now passes
> `--gc-sections` so the system linker dead-strips those per-function sections in **debug**
> too — both build types link today. The verification harness still standardizes on the
> release binaries, so use the `*Release*` tasks.

## 4. JDK matrix

- **JDK 17** — the modules the campaign runs on JDK 17: `:krapper_gen`, `:krapper_model`
  (both target JVM 11 bytecode but build/run fine on 17) and `:featuregen` (native-only —
  no JVM bytecode target; it drives the generated K/N bindings).
- **JDK 21** — the root **`:plugin`** module *requires* Java 21 (`sourceCompatibility` /
  `targetCompatibility = VERSION_21`, `jvmTarget = JVM_21`); any build that configures
  `:plugin` needs a JDK 21.

Local paths the campaign used: `/usr/lib/jvm/java-17-openjdk` and
`/usr/lib/jvm/java-21-openjdk` (point `org.gradle.java.home` or `JAVA_HOME` at the right one
for the task you're running).

## 5. The two front-ends and the gated tasks

`krapper_gen` takes `--frontend={libclang|cpp}` (default `libclang`):

- `libclang` — the historical libclang-C reducer parses `--header` in-process.
- `cpp` — **the handoff**: the libclang parse is skipped and the `WrappedTU` is loaded from
  `--parsedModel` (the ModelIo JSON emitted by the `cppfrontend` binary, which is built on
  kplusplus-generated `libclang-cpp` bindings).

The gated verification tasks (all require `-PenableClang`):

| Task | What it proves |
| --- | --- |
| `:cppfrontend:goldenCompare` | Runs both front-ends over one fixture and tree-diffs their `SerializedElement` models under the documented normalizer ledger; non-zero exit on unexplained divergence. |
| `:cppfrontend:handoffGenerate` | `krapper_gen --frontend=cpp` loads the cppfrontend model (libclang never called), runs resolution + codegen, and **compiles** the emitted wrapper — proving the handed-off model carries everything the real pipeline consumes. |
| `:cppfrontend:handoffInstDiff` | Two gates on `--frontend=cpp --instantiate`. **Gate 1 (oracle):** libclang's own dumped models, reloaded through the cpp file-handoff, must regenerate **byte-identical** output (path-modulo the `.def` dir) — proves the handoff itself is lossless. **Gate 2 (functional):** cppfrontend's *own* parses generate + compile, but are deliberately **not** byte-identical (matching libclang's lossy spellings byte-for-byte would mean emulating the lossiness the rewrite exists to delete). |
| `:cppfrontend:featuregenParity` | The parity oracle: runs every featuregen unit through both front-ends and fails when a unit's verdict is *worse* than `cppfrontend/parity-expectations.txt` records (the ratcheting expected-state ledger). |
| `:clangwalk:runReleaseExecutableKlinker` | The stage1 demo: walks a real Clang AST (`buildASTFromCode` → `TranslationUnitDecl` → `decls()`/`methods()`/`bases()`) entirely on generated bindings. |

featuregen's two-stage build still applies: run `:featuregen:kplusplusSync` first, then the
normal build picks up the generated bindings.

## 6. Troubleshooting

- **Out-of-memory during the release link.** The K/N release link is memory-hungry. Kotlin/
  Native compiles **and links in-process inside the Gradle daemon**, so what governs OOM is
  the *daemon* heap — `org.gradle.jvmargs` — and **not** `GRADLE_OPTS`, which does not reach
  the in-process K/N compiler. That subtlety bit the campaign repeatedly: people exported
  `GRADLE_OPTS="-Xmx…"` and still OOM'd.

  The repo now commits a sane daemon heap in the root `gradle.properties`:

  ```
  org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError
  ```

  6g reliably builds the default (LLVM-free) gate and the gated cpp front-end
  (`-PenableClang`) — no manual per-build heap bump needed. If a *heavier* gated release
  link (the cpp/clang modules under `-PenableClang`) still OOMs, raise the heap — edit that
  line, or add the same key to `~/.gradle/gradle.properties` (a per-user override that keeps
  the committed default sane for low-RAM contributors):

  ```
  org.gradle.jvmargs=-Xmx12g -XX:MaxMetaspaceSize=512m
  ```

  Then re-run, e.g.:

  ```
  ./gradlew :clangwalk:runReleaseExecutableKlinker -PenableClang
  ```

- **Known issue — `:clangwalk` release generation against LLVM-22.1.6.** There is a current
  codegen gap around `clang.attr.Kind` / `clang.attr.value` in the gated `:clangwalk`
  release generation, tracked separately. If you hit it, it's known — see the
  [#11 campaign](clang-bootstrap.md) for status.
