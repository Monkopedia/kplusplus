# Experimental / diagnostic flags

krapper_gen is effectively a compiler, so it accretes *experiments* (new resolve strategies,
caches, alternate codegen) and *diagnostics* (timing, dumps) that we want gated behind a switch
rather than shipped on by default. Rather than let each grow its own ad-hoc mechanism, they share
one home: the **experimental flag registry**.

## Declaring a flag (the single source of truth)

Every flag is declared once in
[`ExperimentalFlags`](../krapper_gen/src/nativeMain/kotlin/com/monkopedia/krapper/generator/ExperimentalFlags.kt)
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
  the env. `krapper_gen ... -X diag.timing -X forcing.memo=lru`
- **Gradle:** `-Pkpp.x=<value>` is forwarded to krapper_gen as `KRAPPER_X` by the compiler plugin's
  `kplusplusSync` (`./gradlew :featuregen:kplusplusSync -Pkpp.frontend.featuregen=cpp -Pkpp.x=diag.timing`).

**Inert when unset.** With no flag set, every read returns the declared default, so behavior and
generated output are byte-identical to a build with no flag machinery.

Discoverability:

- Unknown names **warn** to stderr (typo visibility), never crash.
- Active (non-default) flags are logged to stderr at startup for reproducibility.
- `krapper_gen --list-experimental` prints every declared flag with its default + description.

## First consumer: `diag.timing`

`diag.timing` gates
[`Diag.timed`](../krapper_gen/src/nativeMain/kotlin/com/monkopedia/krapper/generator/Diag.kt), a
zero-overhead-when-off timing helper wired into `resolveForcing` (per-pass wall-clock + class
counts, and a per-request total). This is the instrumentation the upcoming cross-request
memoization experiment needs.

## Candidates to migrate later (NOT done here — kept out of scope)

These pre-existing ad-hoc toggles could later move into the registry for consistency:

- `KRAPPER_ROUNDTRIP_MODEL` env / `--roundTripModel` CLI (model round-trip oracle)
- `--dumpParsedModel` CLI (parse-only dump)
- `KRAPPER_DEBUG_RESOLVE` env (resolve debug filter, `Resolver.kt`)
- `-Pkpp.frontend.<module>` build property (front-end selection)
