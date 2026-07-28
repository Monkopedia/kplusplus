# IDE Gradle-sync performance (investigation, 2026-07-21)

> **Historical record — written 2026-07-21, committed 2026-07-28.** Module paths below predate
> the single-binary merge (#184, `b787196`): `:krapper_parse` and `:krapper_gen` are now one
> `:krapper` module, so `:krapper_parse:kplusplusSync` is `:krapper:kplusplusSync` and
> `:krapper_gen:linkDebugExecutableNative` is `:krapper:…`. Deliberately not rewritten — the
> measurements were taken against the two-module layout and rewriting them would falsify the
> record.
>
> **Lever 3 is still UNIMPLEMENTED** as of 2026-07-28. The recurring ~1 min per sync (of which
> ~46 s is the tool module regenerating its own bindings) is still paid on every sync, because
> the header-driven `kplusplusSync` still declares no outputs. Lever 4 ("don't `clean`") remains
> the only mitigation in force. Re-measure before implementing: #184 changed the module graph
> and #185 (`e75707b`) replaced the subprocess driving with a ksrpc session, so the task-level
> numbers here are stale even where the analysis holds.

Investigation of the owner's ~5 min IntelliJ Gradle SYNC. Measure-first; **no production
code changed.** Profiled on a fresh worktree off `origin/main` @ `1b6dc19` (PR #183, the
`kplusplusSync dependsOn(:krapper_parse:linkReleaseExecutableKlinker)` bridge).

Env: `JAVA_HOME=java-21-openjdk`, `-Xmx12g`, `--no-daemon`, LLVM 22 on box, committed
default frontends (`kpp.frontend.{featuregen,cppfixture,krapper_parse}=cpp`). The IDE
model-fetch is reproduced with
`./gradlew :featuregen:prepareKotlinIdeaImport :cppfixture:prepareKotlinIdeaImport
:krapper_parse:prepareKotlinIdeaImport`.

## TL;DR

- **The #183 hypothesis is half right.** A cold/`clean` sync IS dominated by the
  in-tree `krapper_parse` build chain that #183 wired in — but the expensive part is
  **NOT** a "3-min K/N *release executable* link." The final `linkReleaseExecutableKlinker`
  step is ~1.5 s. The cold ~3 min is the whole self-generation + self-compile + static-link
  chain it transitively pulls in.
- **Cold sync: ~3 m 46 s. Warm (nothing changed) re-sync: ~1 m 3 s.**
- **The 5 min is split:** a one-time-cold component (~2.5–3 min of K/N compile + link that
  *caches* and goes UP-TO-DATE on the next sync) **plus** a **recurring ~1 min** that runs on
  **every** sync because the `kplusplusSync` tasks are **always-out-of-date** (they declare no
  output on the header-driven path). Of that recurring minute, **46 s is
  `:krapper_parse:kplusplusSync` regenerating krapper_parse's own bindings.**
- **Cheapest win, do-able NOW:** make the header-driven `kplusplusSync` **up-to-date** (declare
  its `krapped/` output + its transitive-header inputs) so warm syncs skip it → recurring cost
  ~1 m → a few seconds. **BUT** this lives in the plugin, which is the CONSUMED 0.3.3 jar today,
  so it only lands at **0.3.4** (the in-tree plugin). Do-able-now mitigations are limited to
  "don't `clean`" discipline (keeps the cold chain cached) — which already gets warm syncs to
  ~1 min.
- **The debug-vs-release parser lever (#1) is a dead end:** debug klinker link is ~185 s cold
  vs ~182 s release — no meaningful saving, because the cost is the shared upstream chain, not
  the final link mode.

## Step 1 — the profile

### Cold (`rm -rf */build/bin */build/krapped-cpp`, worst case) — 3 m 46 s

Per-task (from `build/reports/profile/*.html`; the executable/klib tasks all EXECUTED cold):

| time | task | what |
|---|---|---|
| 46.7 s | `:krapper_parse:kplusplusSync` | parse Clang slice + krapper_gen codegen of krapper_parse's OWN bindings (uses the **bundled 0.3.3 tool**, no link) |
| 26.0 s | `:krapper_gen:linkDebugExecutableNative` | K/N debug link of the generator kexe (needed to run codegen) |
| 19.6 s | `:krapper_parse:linkDebugStaticNative` | K/N static-lib compile of krapper_parse's Kotlin+cinterop |
| 11.5 s | `:featuregen:kplusplusSync` | parse featuregen surface + codegen (uses the in-tree RELEASE parser binary) |
| 8.6 s | `:krapper_parse:compileKotlinNative` | |
| 6.1 s | `:krapper_gen:compileKotlinNative` | |
| 5.2 s | `:krapper_model:compileKotlinNative` | |
| ~2 s each | `cinteropKplusplusNative` (×3), `:clangwalk` | |
| **1.5 s** | **`:krapper_parse:linkReleaseExecutableKlinker`** | the final clang++ link — **cheap** |

Plus a large slice (~90 s) of externally-attributed `clang++` native compile/link inside the
klinker `linkRelease*` tasks that Gradle's profiler under-counts (summed task time ≈ 92 s vs
182 s wall for the isolated cold link chain — see below).

**Isolated cold link chain** (`:krapper_parse:linkReleaseExecutableKlinker` from scratch):
**3 m 1 s (182 s)**. Its transitive graph (from `--dry-run`) is the whole self-host loop:
`krapper_gen:linkDebugExecutableNative` → `krapper_parse:kplusplusSync` →
`krapper_parse:compileKotlinNative` → `linkDebugStaticNative` + `linkReleaseStaticNative` →
`linkReleaseExecutableKlinker`.

### Warm (immediate re-run, nothing changed) — 1 m 3 s

Every native compile/cinterop/link task reports **UP-TO-DATE**. Only these EXECUTE:

| time | task | status |
|---|---|---|
| **46.1 s** | `:krapper_parse:kplusplusSync` | always-out-of-date |
| **11.2 s** | `:featuregen:kplusplusSync` | always-out-of-date |
| 0.24 s | `:cppfixture:kplusplusSync` | always-out-of-date |

`--info` on `:krapper_parse:kplusplusSync`:
`Task ':krapper_parse:kplusplusSync' is not up-to-date because: Task has not declared any
outputs despite executing actions.` → it re-runs **every** invocation.

### Where the 5 minutes go

- **One-time-cold (~2.5–3 min):** K/N `compileKotlinNative` + `linkDebug/ReleaseStaticNative`
  + `krapper_gen:linkDebugExecutableNative` + the clang++ native link. **Caches** — goes
  UP-TO-DATE on the next sync and stays there until a `clean`, a source change, or cache
  eviction. This is the delta #183 introduced (the in-tree parser now builds during sync).
- **Recurring (~1 min every sync):** the three always-out-of-date `kplusplusSync` tasks,
  **~57 s of which is regeneration** (46 s krapper_parse + 11 s featuregen). This runs on
  every sync forever, warm or cold.

The owner's "~5 min" is most consistent with a cold-ish / recently-`clean`ed state (one-time
chain + recurring). A steady warm sync is ~1 min, still too slow, and that minute is entirely
the always-out-of-date regeneration.

## Step 2 — lever evaluation

### Lever 1 — Debug vs Release parser: **DEAD END**

Measured cold `:krapper_parse:linkDebugExecutableKlinker` = **185 s** vs release **182 s**.
No saving, because the expense is the shared upstream chain (`kplusplusSync` 46 s +
`krapper_gen` link 26 s + `linkDebugStaticNative` 18–19 s), not the final link mode; the
release variant only adds a cheap `linkReleaseStaticNative`, and the final link is ~1.5–1.9 s
in both. Warm, the link is UP-TO-DATE and skipped regardless. Also, the consumed 0.3.3 plugin
hard-codes the RELEASE path
(`krapper_parse/build/bin/klinker/krapper_parseRelease/krapper_parse`), so a debug build alone
wouldn't be found without a symlink/copy hack. **Not worth it.**

### Lever 2 — Bundled published tool for featuregen/cppfixture sync: **REJECTED (breaks the harness)**

`:krapper_parse`'s own sync already uses the bundled 0.3.3 tool (`extractBundledTool` →
`~/.gradle/kplusplus/tools/0.3.3/krapper_parse`, no link). But in the sibling layout
`resolveKrapperParse` binds featuregen/cppfixture to the **LIVE in-tree**
`linkReleaseExecutableKlinker` binary (plugin `KPlusPlusCompilerGradlePlugin.kt:792-797`),
deliberately — featuregen is the **generator regression harness**; its bindings MUST come from
the current in-tree generator, not the one-release-behind bundled tool. Pointing the sync at the
bundled tool would make featuregen validate STALE bindings and defeat its purpose. A clean split
("bundled tool for IDE resolution, in-tree for the explicit test") is not clean here: the
IDE-resolved `krapped/` sources ARE what the tests compile — there is no separate resolution
artifact to swap. **Rejected.**

### Lever 3 — Make `kplusplusSync` up-to-date: **THE RECOMMENDED WIN (0.3.4)**

Root cause is explicit in `KPlusPlusCompilerGradlePlugin.kt:125-127`:

```kotlin
if (!hasHeadersAtConfig) {
    it.outputs.dir(krappedDir).withPropertyName("krappedDir")
}
```

When headers ARE configured (all three cpp modules use `header(...)`), the task declares **no
output**, so Gradle re-runs it every sync. The header-driven path was *intentionally* left
always-out-of-date (comment lines 87-91) because it has "no per-method change tracking." The
real correctness gap: the plugin declares only the **top-level `header(...)`** files as inputs
(lines 119-123, RELATIVE sensitivity) — NOT the **transitive `#include` closure** nor the
system libstdc++/libclang headers. So if it simply declared `krapped/` as output and became
up-to-date, editing a transitively-included header (or a system-header/LLVM bump) would not
retrigger regeneration → stale bindings.

**Fix shape (0.3.4):** declare the `krapped/` output on the header path too, and broaden inputs
to the transitive include closure (e.g. capture the `#include` set the parser already resolves,
or a fingerprint of the header-directory trees + parser/tool version) so up-to-date is
*correct*. Then a warm sync skips all three syncs → recurring cost **~1 min → a few seconds**.

- **Expected speedup:** removes the entire recurring ~57 s; warm sync ≈ Gradle
  config/model overhead only (a handful of seconds). Cold stays ~3–4 min (one-time).
- **Effort:** moderate — the risk is entirely in getting the input fingerprint complete enough
  that a header edit still retriggers. A conservative first cut: keep re-running when the
  compiler-written `requested.txt` manifest OR any file under the configured
  `headerDirectory(...)` roots changes (directory-tree input, RELATIVE sensitivity), plus the
  parser+generator binary as input (already declared). This covers the common edit-a-header
  case; the residual risk is an out-of-tree system-header bump, which is rare and already
  handled by "the tool binary changed" for LLVM bumps.
- **Now vs 0.3.4:** the logic lives in the plugin = the CONSUMED 0.3.3 jar today, so it only
  takes effect at **0.3.4** when the repo self-hosts the in-tree plugin. The #183 bridge
  (`featuregen`/`cppfixture/build.gradle.kts`, `dependsOn(:krapper_parse:linkReleaseExecutableKlinker)`)
  is itself a 0.3.3-only workaround slated for deletion at 0.3.4.
- **Correctness risk:** medium if under-fingerprinted (stale bindings after a transitive-header
  edit). Mitigate by fingerprinting the header-directory trees, not just the top-level header.
  For the featuregen TEST path specifically, consider keeping the explicit test/gate task
  always-out-of-date (force regenerate) while letting the IDE-sync path be up-to-date — but note
  they currently share one task, so this needs the test to depend on a `--rerun` / a separate
  force-regen entry point.

### Lever 4 — "don't clean" + warm caching: **the do-able-now mitigation**

The one-time-cold chain caches perfectly (all UP-TO-DATE on warm re-run). So the practical
now-advice, with **zero code change and no 0.3.4 dependency**:

- Avoid `clean` / `rm -rf */build` before an IDE sync. Keeping `krapper_parse/build/bin` +
  `*/build/classes` warm turns the 3 m 46 s cold sync into the ~1 min warm sync.
- Ensure the Gradle build cache / `--build-cache` is on so an eviction doesn't silently force
  the cold chain.

This does NOT touch the recurring ~1 min (that needs lever 3). It only guarantees you pay the
~3 min once, not every sync.

## Recommendation

1. **Now (no release needed):** "don't `clean` before sync" + warm build cache. Gets syncs to
   the ~1 min warm floor. (Docs/runbook note, not code.)
2. **0.3.4 (the real fix — lever 3):** make the header-driven `kplusplusSync` up-to-date by
   declaring the `krapped/` output and a *complete* input fingerprint (top-level header +
   header-directory trees + generator/parser binary + `requested.txt`). Cuts the recurring
   warm-sync cost from ~1 min to a few seconds. Land it together with the removal of the #183
   bridge and the `krapper_parse` self-sync (its 46 s is the single biggest recurring item).
3. **Skip** lever 1 (debug parser — no saving) and lever 2 (bundled tool for
   featuregen — breaks the regression harness).

Expected end state after (2): **cold sync ~3–4 min once, warm sync a few seconds.**
