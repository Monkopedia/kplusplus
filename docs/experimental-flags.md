# Experimental / diagnostic flags

krapper is effectively a compiler, so it accretes *experiments* (new resolve strategies,
caches, alternate codegen) and *diagnostics* (timing, dumps) that we want gated behind a switch
rather than shipped on by default. Rather than let each grow its own ad-hoc mechanism, they share
one home: the **experimental flag registry**.

## Declaring a flag (the single source of truth)

Every flag is declared once in
[`ExperimentalFlags`](../krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/ExperimentalFlags.kt)
with a namespaced name, a type (`BoolFlag` / `StringFlag` / `IntFlag`), a default, and a one-line
description, then added to the `all` list:

```kotlin
val DIAG_TIMING = ExperimentalFlag.BoolFlag(
    "diag.timing",
    default = false,
    description = "Log per-phase wall-clock timings (ms) + counts to stderr."
)

val all: List<ExperimentalFlag<*>> = listOf(DIAG_TIMING)
```

Read it anywhere via the typed accessor:

```kotlin
if (ExperimentalFlags.isEnabled(ExperimentalFlags.DIAG_TIMING)) { ... }
val mode = ExperimentalFlags.get(SomeStringFlag)   // returns the flag's default when unset
```

Names are dotted by area, e.g. `diag.timing`, `forcing.crossRequestMemo`.

## Setting a flag

Resolved **once** at tool startup, with precedence **CLI `-X` > env `KRAPPER_X` > default**:

- **Env:** `KRAPPER_X` holds a comma-separated list of `name` (bool → on) or `name=value` entries.
  `export KRAPPER_X=diag.timing,forcing.memo=lru`
- **CLI:** repeatable `-X name[=value]` / `--experimental name[=value]` merges over (and overrides)
  the env. `krapper ... -X diag.timing -X forcing.memo=lru`
- **Gradle:** `-Pkpp.x=<value>` is forwarded to krapper as `KRAPPER_X` by the compiler plugin's
  `kplusplusSync` (`./gradlew :featuregen:kplusplusSync -Pkpp.frontend.featuregen=cpp -Pkpp.x=diag.timing`).

**Inert when unset.** With no flag set, every read returns the declared default, so behavior and
generated output are byte-identical to a build with no flag machinery.

Discoverability:

- Unknown names **warn** to stderr (typo visibility), never crash.
- Active (non-default) flags are logged to stderr at startup for reproducibility.
- `krapper --list-experimental` prints every declared flag with its default + description.

## First consumer: `diag.timing`

`diag.timing` gates
[`Diag.timed`](../krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/Diag.kt), a
zero-overhead-when-off timing helper wired into `resolveForcing` (per-pass wall-clock + class
counts, and a per-request total). This is the instrumentation the upcoming cross-request
memoization experiment needs.

## `diag.baseBindTiming` — base-resolve profiler

`diag.baseBindTiming` profiles the **base resolve** (`filterAndResolve` / `resolveAll` — the pass
that runs *before* any forcing), gating
[`BaseBindProfiler`](../krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/BaseBindProfiler.kt).
It exists to answer the issue-#10 question of whether the base-bind cost at broad Clang/LLVM scale
is uniform-breadth (O(n)) or a hotspot/superlinear (O(n²)) — and because that run can time out, it
emits **streaming** progress so a partial run still yields a profile:

- every 100 resolved types (or 3 s), a `basebind: resolved=… elapsed=… inst=…/s cum=…/s walks=…
  nodes=…` line — a decaying instantaneous rate is the O(n²) fingerprint;
- a running **top-N slowest types** (names the pathological types, if any);
- **tree-walk counters** — every `ParsedResolver.resolve`/`resolveTemplate` does a full-TU
  `filterRecursive` walk, so `walks`/`nodes` expose the O(distinct-types × tree-size) cost directly;
- INCLUDE_MISSING on-demand materialization + re-entry counts.

`BaseBindProfiler.report()` (after `resolveAll` returns) dumps the final distribution. **Inert +
byte-identical when off** (every method early-returns; the profiler is measurement-only and never
changes what gets bound). Run it e.g. with
`krapper … --referencePolicy INCLUDE_MISSING -X diag.baseBindTiming`.

## `diag.detachedReads` — run-scope escape detector

Since brick B4 (#186) the drop ledger, `WrappedType` intern cache, root package, `-fno-rtti` flag
and cpp front-end parse config belong to a
[`KrapperRun`](../krapper/src/nativeMain/kotlin/com/monkopedia/krapper/generator/KrapperRun.kt)
that each service call installs for the duration of that call. A read that happens *outside* any
such scope does not fail — it silently gets the `detached` fallback, which exists so unit tests can
build fixtures without a run and is **shared by every caller that reaches it**.

That is a bad failure mode to leave silent: the symptom shows up much later as "the generated
output diverged from its baseline", with nothing naming the cause. `diag.detachedReads` turns it
into a named diagnostic — the first such read per process logs

```
krapper: WARNING — GenerationContext.current read run-scoped state with no KrapperRun installed;
it got the detached fallback, which is SHARED. Reported once per process.
```

**Once** per process, deliberately: these reads come from `WrappedType.invoke`'s interner, so a
per-read warning would emit thousands of lines and bury the signal. Both carriers
(`GenerationContext.current` and `KrapperRun.current`) share one latch, so the warning names
whichever site got there first.

The reporting is an installable hook (`GenerationContext.onFirstDetachedRead`) rather than a direct
log call, because the carriers live in `:krapper_model`, which cannot see this flag registry or the
logger; `App.run` installs the stderr warning next to where the flags are resolved. **Inert +
byte-identical when off** — the only cost on the hot path is an identity compare against the
fallback, and nothing is logged unless the flag is on. Run it e.g. with
`krapper … -X diag.detachedReads`.

## Candidates to migrate later (NOT done here — kept out of scope)

These pre-existing ad-hoc toggles could later move into the registry for consistency:

- `KRAPPER_ROUNDTRIP_MODEL` env / `--roundTripModel` CLI (model round-trip oracle)
- `--dumpParsedModel` CLI (parse-only dump)
- `KRAPPER_DEBUG_RESOLVE` env (resolve debug filter, `Resolver.kt`)
- `-Pkpp.frontend.<module>` build property (front-end selection)
