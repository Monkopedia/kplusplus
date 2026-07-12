# Broad-binding forcing resolve: root cause + design (issue #10)

**Status:** design / investigation — for review. No production code changed.
**Author:** monkopedia-coder (investigation pass, 2026-07-12).
**Scope:** the `--forcingModel` / `requestInstantiation` resolve loop at broad binding scale.

---

## TL;DR

The blow-up the re-baseline saw ("Found 3745 classes to resolve" / "Scoped forcing to
3221 element(s)", CPU-bound for tens of minutes, never reaches codegen) is **two
compounding effects, and only the second is a bug in the forcing code**:

1. **The base resolve genuinely binds ~3700 classes** — because the broad scenario asks it
   to (either a `DefaultFilter` import, or a narrow `only(...)` under an `INCLUDE_MISSING`
   base policy that transitively materialises the whole reachable Clang+LLVM graph). That
   set is dominated by internals nobody asked for (`llvm::vfs`, `llvm::opt`, `clang::ento`
   the static analyzer, `llvm::cl`, `clang::sema`, `clang::serialization`, …). Whether
   *that* is desirable is a **policy/consumer-configuration** question, not a forcing bug.

2. **`resolveForcing` then re-resolves the ENTIRE already-bound set — all ~3221 classes —
   three times per instantiation request, with no memo across passes or across requests.**
   This loop was designed and validated at the *featuregen* regime (~120 bound classes,
   17 requests → cheap). At ~3221 densely-cyclic Clang classes it is pathological. The loop
   only needs to re-resolve the handful of classes that have a method returning the
   just-forced container (the range-accessor recovery, e.g. `DeclContext::decls()`), **not
   the whole bound universe.** Re-resolving the universe is wasted work with no output
   effect. **This is the defect the fix should target.**

**Correctness verdict:** the container-closure over-resolution that the *original* #10
thesis feared (forcing `vector<clang::Decl*>` dragging in Clang's world) was **already
fixed** — the T-forcing-scope work (see `docs/campaigns/self-hosting.md`) scoped the
forcing struct so it "introduces 0 extra classes (was 1269)". The *current* 3221 number is
**not** the forcing struct expanding a closure — it is the pre-existing broad base bind
being **needlessly re-run** by the forcing passes. So: **the remaining issue is a
tractability/efficiency bug in the re-resolution loop, not a semantic over-resolution
bug.** The recommended fix makes broad forcing tractable *without changing a single byte of
output* on any existing gate, because re-resolving classes that have no
container-returning method is provably a no-op on the emitted set.

---

## 1. The pipeline, end to end

Two binaries, one model. `krapper_parse` (the Clang C++ AST front-end) emits ModelIo JSON;
`krapper_gen` consumes it. The libclang-C reducer was deleted in flip B5 (#47) — there is
no in-process parse path.

```
krapper_parse ──▶ base_model.json ──(--parsedModel)──▶ krapper_gen
              └─▶ KrapperForce_*.json ─(--forcingModel '<spec>=<path>')─┘
```

### 1a. Base resolve — `filterAndResolve`
`IndexedServiceImpl.filterAndResolve` (IndexedServiceImpl.kt:109):

- `parseHeader()` → `loadParsedModel(cppParsedModelPath)` decodes `base_model.json` into a
  `WrappedTU`, runs the pre-resolution rewrites, wraps it in `ParsedResolver`
  (Parsing.kt:490).
- `resolver.findClasses(filter.wrapperFilter())` selects the classes to bind.
  **This count is the "Found N classes to resolve" log line** (IndexedServiceImpl.kt:118).
- `initialClasses.resolveAll(resolver, config.referencePolicy)` (Resolver.kt:116) resolves
  them under the base `--referencePolicy` (default `IGNORE_MISSING`).
  - Under `IGNORE_MISSING`: each filtered class is resolved; a referenced-but-unbound type
    is dropped (no transitive expansion).
  - Under `INCLUDE_MISSING`: the `typeMapper` (Resolver.kt:644) **materialises every
    referenced class on demand**, recursively, over the whole reachable graph. This is
    where a *narrow* `only(...)` can still explode to thousands (see §2).

### 1b. Per-instantiation forcing — `requestInstantiation`
`IndexedServiceImpl.requestInstantiation` (IndexedServiceImpl.kt:134), once per
`--instantiate`/`--forcingModel` spec:

1. `loadForcingModel(path)` (Parsing.kt:131) decodes the forcing TU (the
   `KrapperForce_<spec>` struct + everything its synthetic header `#include`d — i.e. the
   consumer's headers again, so this TU carries the *whole library surface too*), reorders
   its namespaces to the base TU's first-seen order (O1, #46).
2. **Scope the forcing collection** (IndexedServiceImpl.kt:203):
   `resolver.findClasses { defaultFilter() }` returns everything in the re-parsed TU
   (**"Found 3625 classes"**), then keeps only:
   - the `KrapperForce_*` struct (key `== forceName`), and
   - classes already bound by the base resolve (key `in alreadyBoundKeys`).
   The rest (unrelated TU classes) are dropped. **This kept count is the "Scoped forcing to
   N element(s)" line** — and `N ≈ alreadyBoundKeys.size`.
3. `scopedFound.resolveForcing(...)` (Resolver.kt:199) — the 3-pass dance (§1c).
4. `dedupClassesLastWins()` collapses the per-request re-resolved copies to one each.

### 1c. The three forcing passes — `resolveForcing` (Resolver.kt:199)
One shared `ResolveTracker` across all three passes:

- **Pass 1** (base policy): resolve every `boundClass` (all ~3221). Binds them into the
  shared tracker. A method whose return is the still-unbound container is dropped here.
- **Pass 2** (`INCLUDE_MISSING`): resolve the `KrapperForce_*` struct. Its `value` member
  *is* the container spec (`std::vector<clang::Decl*>`), so resolving it materialises the
  container. `clang::Decl` etc. are already in the tracker from pass 1, so INCLUDE does
  **not** re-expand them — this is the T-forcing-scope fix that killed the old 1269
  explosion.
- **Pass 3** (base policy, fresh `mappingCache`, bound classes evicted from the resolved
  cache): re-resolve every `boundClass` **again** (all ~3221), now that the container
  exists, to **recover the dropped range-accessor methods** (`decls()`,
  `methods()`, `bases()`).

The forcing struct is not the cost. **Passes 1 and 3 each walk the entire bound set.**

### 1d. The synthetic header
`ForcingHeader` (`:krapper_model`, ForcingHeader.kt): `contentFor` emits `#include`s
*targeted* to the types named in the spec plus the consumer headers, then
`struct KrapperForce_X { X value; };`. It is deliberately **not** a blanket bundle
(comment at ForcingHeader.kt:56). So the forcing header itself is already minimal; the
breadth comes from re-`#include`-ing the consumer's own (broad) headers, which is
unavoidable — but that breadth is neutralised by the scope filter in step 2. It is *not*
what drives the 3221.

---

## 2. Where the closure blows up — evidence

Captured logs from a prior instrumented run (`/tmp/broadforce_gen2.log`,
`/tmp/bf_scoped_inc.log`, `/tmp/fg_gen_direct.log`).

### Scenario A — broad `DefaultFilter` base (`/tmp/broadforce_gen2.log`)
```
Found 3745 classes to resolve          # DefaultFilter selects the whole non-std surface
Resolved 3705 top-level elements       # base bind — this is REQUESTED, not a forcing bug
Found 3625 classes                     # forcing re-parse findClasses
Scoped forcing to 3221 element(s) for std::vector<clang::Decl*>
… (stalls in pass-1/pass-3 re-resolving 3221 classes; never reaches "Forcing introduced")
```
The base model *itself* contains 3745 non-std classes reachable from `clang_slice.h`. The
`DefaultFilter` picks them all. The 3221 forcing set ≈ the already-bound base set.

### Scenario B — narrow `only(18)` + `INCLUDE_MISSING` base (`/tmp/bf_scoped_inc.log`)
```
Found 18 classes to resolve            # only(18) allowlist
Resolved 1134 top-level elements       # INCLUDE_MISSING transitively expanded 18 → 1134
Found 3625 classes
Scoped forcing to 2051 element(s) for std::vector<clang::Decl*>
EXIT 143 after 706s                    # killed at ~12 min, stalled in forcing re-resolve
```
Even a **narrow allowlist** explodes when the base policy is `INCLUDE_MISSING`, because the
base resolve transitively materialises the reachable graph (18 → 1134), and the forcing
then re-resolves 2051.

### Scenario C — featuregen (the working regime, `/tmp/fg_gen_direct.log`)
```
Found 157 classes to resolve
Resolved 119 top-level elements
Scoped forcing to 120 element(s) for Box<int>   (×17 requests, each ~120)
```
~120 bound classes, re-resolved 3×/request × 17 requests ≈ trivial. **Same loop, 27× fewer
classes, and each class is a small self-contained std/fixture type rather than a node in a
densely-cyclic Clang graph.** The loop is O(bound × passes × requests); it was tuned at
bound ≈ 120.

### What is IN the 3745 (Scenario A namespace histogram)
```
921 clang::     892 llvm::     766 std::
 45 llvm::vfs::  45 llvm::opt::  17 clang::ento::  16 llvm::cl::
 13 clang::sema:: 5 llvm::sys::  3 clang::tooling::  1 clang::serialization::
  1 clang::interp::  1 clang::driver::  1 clang::diag::  …
```
Confirmed: dominated by transitively-reached internals nobody requested — the static
analyzer (`ento`), option/command-line parsing (`opt`/`cl`), the virtual file system
(`vfs`), Sema, the serializer, the constexpr interpreter. None are needed to bind
`std::vector<clang::Decl*>`.

---

## 3. Correctness verdict: bug vs intended

Split the question in two, because the two effects have different verdicts.

### (i) The base binding ~3700 classes — **intended given the inputs; a consumer-config concern, not a bug.**
The base resolve binds exactly what the filter + policy select. If the consumer runs
`DefaultFilter`, or `only(...)` under `INCLUDE_MISSING`, they *asked* to bind the reachable
world. The project's own doctrine (`docs/campaigns/self-hosting.md`: "breadth fearless")
says a broad import that breaks the system is a defect to fix — but binding a large surface
per se is the requested behaviour. The narrow, correct way to bind
`std::vector<clang::Decl*>`'s own surface with `clang::Decl` opaque is exactly the working
`clangwalk` config (`only(...)` + `IGNORE_MISSING`); that path is fine. So: **not a forcing
bug.** If a consumer wants the narrow surface, the fix is their filter/policy, and that
already works (Scenario C-style).

### (ii) The forcing re-resolving the whole bound set — **a bug (efficiency / wasted work).**
`resolveForcing` re-resolves *all* `boundClasses` in passes 1 and 3, but the only *output*
effect of that re-resolution is recovering range-accessor methods whose return type is the
just-forced container. A bound class with **no** method returning the forced container
re-resolves to a byte-identical result that `dedupClassesLastWins` then throws away
(last-wins picks the same shape). At bound ≈ 120 the waste is invisible; at bound ≈ 3221,
densely cyclic, it is the whole stall. **Re-resolving a class that can't recover anything is
pure cost with zero output effect** — the definition of a tractability bug. The original
#10 over-resolution thesis (the forcing struct dragging in the closure) is **stale**: it was
fixed by T-forcing-scope, and pass 2 no longer re-expands bound classes.

**Verdict:** the remaining #10 tail is a **bounded-work bug in `resolveForcing`'s re-resolve
loop**, not a semantic over-resolution. Fixing it is a pure speedup with (provably)
byte-identical output on the existing gates.

---

## 4. Options

Non-negotiable safety constraint: **byte-identical output on `:cppfrontend:featuregenParity`
and the 17-instantiation unit gate** (this loop is load-bearing for ALL binding, not just
the broad case).

### Option (a) — bound the closure / rethink INCLUDE_MISSING semantics
Make the base resolve only expand the requested surface + genuine binding-necessary
dependencies (lazy/bounded closure, or an explicit reachability bound), so `INCLUDE_MISSING`
stops meaning "materialise everything reachable".

- **Correctness impact:** high — but it changes what gets *bound*, i.e. it changes output.
- **Risk:** **HIGH / disqualifying as the primary fix.** `INCLUDE_MISSING` is exactly
  featuregen's base policy; featuregen's whole cross-instantiation story (a bound class
  referencing a type bound by a *different* request, materialised on demand — see the pass-1
  comment at Resolver.kt:244) depends on the current transitive semantics. Any bound change
  here risks the parity gate. This is a genuine *policy* redesign and belongs to the
  consumer-config discussion in §3(i), not the tractability fix.
- **Verdict:** not the lever. If breadth is unwanted, the consumer should use `only(...) +
  IGNORE_MISSING` (already works). Do not touch INCLUDE_MISSING semantics to fix a
  performance bug.

### Option (b) — cross-pass / cross-request memoization
The three passes already share one tracker, so *within* one `resolveForcing` a class is
resolved into `resolvedClasses` and not re-expanded — **except** pass 3 deliberately
**evicts** the bound classes (Resolver.kt:331-336) to force a genuine rebuild, and each
`requestInstantiation` builds a **fresh** tracker (via `withClasses`), so across the N
instantiation requests the same ~3221 classes are re-resolved from scratch N times.

- **Correctness impact:** none (pure speedup).
- **Risk:** medium. Naively caching the resolved set across requests collides with the
  per-request namer reset and the last-wins dedup; and pass-3's eviction is load-bearing for
  range recovery (the eviction comment explains why the cached copy is stale). A safe memo
  must preserve pass-3's rebuild-for-recovery semantics.
- **Effort:** medium.
- **Verdict:** viable as a *complement*, but it still pays O(bound) per request for the
  classes that genuinely change nothing.

### Option (c, RECOMMENDED) — restrict the re-resolve to container-consumers
Re-resolve in passes 1 & 3 only the bound classes that **can recover something** — i.e. that
have a method (or field) whose return/element type is (or transitively contains) a
just-forced container spec. Everything else is bound once (cheaply, opaque) and never
re-walked.

- **The seam:** pass 3's entire purpose is range-accessor recovery (Resolver.kt:283-356).
  A class with no container-returning member produces an identical resolution the dedup
  discards. So filter `boundClasses` down to the *consumer set* — classes whose model
  references a forced-container type-string — before the pass-1/pass-3 walks. The
  `forcedContainerKeys` set (already threaded through, Resolver.kt:203/377) plus this
  request's `forcingTargets` give the container keys to match against; the match is a cheap
  structural scan of each bound class's member return/arg type-strings (no resolution).
- **Correctness impact:** none — provably byte-identical, because the excluded classes
  re-resolve to the same bytes that last-wins already discards.
- **Risk:** **LOW**, and *directly testable*: the consumer set at featuregen scale is the
  same handful of classes the loop already recovers (`RangeHolder`, `DeclContext`, …), so
  featuregen output is unchanged by construction. The blast radius is confined to *which
  bound classes get re-walked*, not *what they resolve to*.
- **Effort:** low–medium (one filter over `boundClasses`, plus a helper that scans a
  `WrappedClass`'s members for a forced-container type-string).
- **Payoff:** at broad scale the consumer set is a few dozen classes, not 3221 — the
  pass-1/pass-3 cost drops by ~2 orders of magnitude, so forcing reaches codegen and the
  *real* #10 question (does the broadly-bound C++ compile?) becomes measurable for the first
  time.

### Hybrid (c)+(b)
Do (c) first (kills the dominant cost, zero output risk). If per-request base-bind cost
(pass 1 still resolves the container-consumer set fresh each request) is still material at
scale, add (b)'s cross-request memo for the container-consumer set only. (c) alone almost
certainly suffices; keep (b) in reserve.

---

## 5. Recommendation

**Adopt Option (c): scope the forcing re-resolve to container-consumer classes.**

Rationale: it targets the actual defect (wasted re-resolution), is a *pure speedup* with
byte-identical output on the load-bearing gates, has the smallest blast radius (it changes
*which* classes are re-walked, never *what* they resolve to), and it unblocks the measurement
the re-baseline could not reach — how much of the broadly-bound Clang surface actually
compiles — which is the true remaining #10 work.

Explicitly **do not** touch `INCLUDE_MISSING` transitive semantics (Option a) to fix this:
that is a bound-changing policy redesign, it endangers the featuregen parity gate, and the
narrow-surface need it would serve is already met by `only(...) + IGNORE_MISSING` (the
working `clangwalk` config).

### Implementation note / correction (PR #156)

The above recommends scoping the re-resolve to container-consumer classes in **both
passes 1 and 3**. Implementation found that is **not** byte-identical-safe for pass 1, so
PR #156 restricted **only pass 3** and left **pass 1 eager**. The correction and its
consequences:

- **What shipped:** pass 3's re-resolve is now filtered to the container-consumer set
  (classes whose model members reference a just-forced container type-string). This is
  provably byte-identical in all policies — pass 3 exists solely for range-accessor
  recovery, and a class that consumes no forced container recovers nothing, so skipping its
  re-walk cannot change output. Pass 1 was **left eager** (still resolves every bound
  class).

- **Why pass-1 filtering is NOT byte-identical-safe.** Under the real-Clang broad path the
  base policy is `IGNORE_MISSING`, but pass 2 resolves the `KrapperForce_*` struct under
  `INCLUDE_MISSING`. If pass 1 skips a bound class, that class's **first** resolution no
  longer happens in pass 1's `IGNORE_MISSING` context — it is deferred into pass 2's
  `INCLUDE_MISSING` on-demand materialisation (the shared tracker resolves it lazily while
  expanding the container struct). Resolving a class for the first time under
  `INCLUDE_MISSING` instead of `IGNORE_MISSING` **silently materialises members** (referenced
  types the `IGNORE_MISSING` first-pass would have dropped). This shift is **invisible to the
  featuregen INCLUDE-base parity gate**, whose base policy is already `INCLUDE_MISSING` — so
  the class is resolved under INCLUDE in *both* the filtered and unfiltered runs there, and
  the gate reports byte-identical while the real-Clang `IGNORE_MISSING` broad path diverges.
  Filtering pass 1 therefore trades a provable equivalence for a policy-dependent one that
  the available gate cannot see. Pass 1 stays eager.

- **Consequence — this is ~half the win.** Restricting only pass 3 removes one of the two
  O(bound) whole-set walks per request; pass 1 still pays O(bound) per request. So broad
  forcing is roughly **twice as fast**, not the ~2-orders-of-magnitude the (c)-both target
  projected, and **this change does not yet claim broad forcing reaches codegen.** The
  remaining pass-1 O(bound) per-request cost is the **follow-up: Option (b) cross-request
  memoization over the consumer set** (§4b) — memoize pass 1's bound-class resolutions across
  the N instantiation requests rather than filtering them, which is safe because it changes
  *when* work is cached, not *which policy* first resolves a class.

- **Stale gate names (flip landed).** The safety constraint above and §5's verification plan
  cite `:cppfrontend:featuregenParity` and "the 17-instantiation ALL unit" — **both no longer
  exist** (the Phase-E flip removed the parity harness and that unit). The equivalent
  machine-check used to validate PR #156 is **regenerating featuregen's `build/krapped-cpp/`
  binding tree and diffing it** against the unfiltered baseline (byte-identical), which is
  what was done for this change. Read the `:cppfrontend:featuregenParity` / 17-unit
  references throughout this doc as "regenerate `build/krapped-cpp/` and diff".

### Verification plan (how we prove no regression)
1. **Byte-identical gates (blocking):** `:cppfrontend:featuregenParity` and the
   17-instantiation ALL unit under `--frontend=cpp` must stay on their committed ratchet
   ledger (zero regression). Because (c) only *skips re-resolving classes that recover
   nothing*, the featuregen output is unchanged by construction; the gate confirms it.
2. **`krapper_gen :nativeTest` + featuregen sync** green (the standing determinism +
   codegen guards; per the memory discipline, re-run featuregen sync after the generator
   change — `:nativeTest` alone misses it).
3. **A targeted unit** asserting the container-consumer filter: a bound class *with* a
   forced-container-returning method IS re-resolved (range method recovers), a bound class
   *without* one is NOT re-walked yet appears byte-identical in output (guards the
   equivalence claim directly, at unit scale).
4. **Broad-scale smoke (new, measurable for the first time):** the Scenario-A/B config
   should now pass forcing and *reach codegen* in seconds, not stall. Capture the resulting
   compile error count — that becomes the real, re-baselined #10 codegen tail (a separate
   "harden broad binding" track, the P2 the campaign already anticipated).
5. **Determinism:** `DeterminismTest.twoInProcessRunsAreByteIdentical` stays green (the
   filter must be order-independent — scan by type-string, matching the existing
   string-keyed merge discipline).

### Out of scope for this change
- The broadly-bound C++ compile-correctness tail (the historical 284/179× cluster) — that
  is the *next* track, and PR #154 already regression-guarded its nested-enum family. This
  change is what makes that tail measurable again; it does not fix it.
- Any change to what the base filter/policy binds (that is consumer configuration, §3(i)).

---

## Appendix — file/function index

| Concern | Location |
|---|---|
| Base resolve entry | `IndexedServiceImpl.filterAndResolve` — IndexedServiceImpl.kt:109 |
| "Found N classes to resolve" | IndexedServiceImpl.kt:118 |
| `resolveAll` (base) | Resolver.kt:116 |
| Per-instantiation forcing | `IndexedServiceImpl.requestInstantiation` — IndexedServiceImpl.kt:134 |
| Forcing scope filter / "Scoped forcing to N" | IndexedServiceImpl.kt:203-211 |
| The 3-pass forcing resolve | `List<WrappedElement>.resolveForcing` — Resolver.kt:199 |
| Pass-1 re-resolves all bound classes | Resolver.kt:251-261 |
| Pass-2 forces the container struct | Resolver.kt:271-281 |
| Pass-3 evicts + re-resolves all bound classes | Resolver.kt:331-356 |
| `INCLUDE_MISSING` transitive materialisation | `typeMapper` — Resolver.kt:644 |
| `forcedContainerKeys` (cumulative container set) | Resolver.kt:203, 348-377; IndexedServiceImpl.kt:96 |
| `dedupClassesLastWins` | Resolver.kt:411 |
| Synthetic forcing header | `ForcingHeader` — krapper_model/…/ForcingHeader.kt |
| Forcing model load + first-seen reorder | `loadForcingModel` — Parsing.kt:131 |
| Working narrow config (contrast) | clangwalk/build.gradle.kts:81-123 |
| Historical T-forcing-scope fix (0 extra classes) | docs/campaigns/self-hosting.md:281-310 |
