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

Non-negotiable safety constraint: **byte-identical output on the regenerate-and-diff gate**
— regenerating featuregen's `build/krapped-cpp/` binding tree and diffing it against the
unfiltered baseline (this loop is load-bearing for ALL binding, not just the broad case).
(This gate replaces the retired `:cppfrontend:featuregenParity` + 17-instantiation-unit
harness, removed in the Phase-E flip; see the "Stale gate names" note in §5.)

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

> The bullets above were the original terse sketch. The section below is the **PR-#156
> follow-up investigation** that expands (b) into a concrete, safety-verified design — because
> after #156 restricted pass 3, **pass 1 re-resolving every bound class on every request is
> the remaining dominant cost**, and cross-request memoization of pass 1 is the named lever.

#### (b.1) What pass 1 actually is, and why it re-runs N times

Each `requestInstantiation` (IndexedServiceImpl.kt:134) calls `loadForcingModel(path)`
(Parsing.kt:131), which **`ModelIo.decodeFromString`s a FRESH `WrappedTU`** (a brand-new
object graph — the forcing TU is re-decoded from JSON per request; there is no cursor→element
memo across loads, Parsing.kt:105 comment). `resolveForcing` then builds a **fresh
`ResolveTracker` via `withClasses`** (Resolver.kt:252, `ResolveContext.withClasses` at
Resolver.kt:566 constructs `ResolveTracker(...)` anew and resets the `NameHandler`). So across
N instantiation requests, pass 1 (Resolver.kt:267-277) re-resolves the *same base-bound class
set* — re-decoded, fresh-tracker, from scratch — **N times**. That is the O(bound × N) cost
`#156` left on the table (it only removed the *second* O(bound) walk, pass 3, and only for
non-consumers).

#### (b.2) Is a bound class's pass-1 resolution a pure function of the class? — **NO.** (hazard #3)

`WrappedClass.resolve` (ModelResolution.kt:163) is **not** a pure projection of the class. Two
concrete impurities:

1. **It mutates the model element in place.** `modifyMethodsIfNeeded` (ModelResolution.kt:262)
   calls `removeChild` (const-overload dedup, hidden-delete dtor removal), `addChild` (synthetic
   default ctor), and flips `metadata.hasConstructor/hasDefaultConstructor/hasCopyConstructor`.
   On the *fresh* per-request TU copy this is harmless (each request mutates its own copy), but a
   memo that cached and **reused a `ResolvedClass` built against one request's WrappedClass**
   would be aliasing state whose provenance is a now-discarded TU. The memo must therefore key on
   and return the **`ResolvedClass` value** (already fully materialized, no back-reference to the
   mutated `WrappedClass`), never re-run `resolve` against a cached `WrappedClass`.

2. **Its output depends on the shared tracker, not just the class.** Every member type flows
   through `resolverContext.resolve → map → typeMapping → tracker.canResolve` (Resolver.kt:519,
   677). Under **IGNORE_MISSING** a member `C::m() -> D` keeps `m` iff `canResolve(D)` — i.e. `D`
   is in `tracker.classes`/`resolvedClasses` at that instant. Under **INCLUDE_MISSING**,
   `canResolve` being false triggers **on-demand materialization** (Resolver.kt:707-763) that
   **mutates the shared tracker** (`resolvedClasses[...] =`, `classes[...] =`) and pulls sibling
   classes in. So the bytes `C` resolves to are a function of *tracker contents at resolve time*,
   which is a function of *request order and which siblings/containers are present*.

**Verdict on hazard #3:** resolve-once-reuse is **not** a clean function of the class alone. A
safe memo must (a) store the finished `ResolvedClass` value, and (b) only be consulted when the
tracker state that would drive `canResolve` for that class's members is **provably identical** to
the state under which the cached value was produced. That provability is the whole game — see
(b.3)/(b.4).

#### (b.3) Which classes are request-INVARIANT vs request-DEPENDENT? (hazard #1)

Partition `boundClasses` by the `referencesAnyContainer` predicate already shipped in #156
(Resolver.kt:431) — but computed over the **cumulative** container set for the *whole run*
(every `forcingTargets` ∪ the final `forcedContainerKeys`), not one request's:

- **Container consumers** (member return/arg/field type-string mentions any forced container).
  These are **request-DEPENDENT by construction**: consumer `C` gains its range accessor
  `decls() -> vector<Decl*>` only in the request(s) whose tracker holds `vector<Decl*>`, and
  loses it (dropped in pass 1, not recovered in pass 3) in requests that force a *different*
  container. This is exactly the multi-consumer hazard the task names: `CXXRecordDecl` has
  `methods()`, `bases()`, `decls()` over three different containers and resolves to a **different
  method set per request**. The cumulative-recovery machinery (`forcedContainerKeys` seeding
  `otherResolved`, Resolver.kt:353-366, IGNORE-only) exists *specifically* so the last-processed
  request's copy carries all three — last-wins dedup then keeps that richest copy. **Memoizing a
  consumer's pass-1 result across requests would freeze it at whichever container was live first
  and defeat cumulative recovery → wrong method set for exactly the classes #10 cares about
  (Clang's `DeclContext`/`CXXRecordDecl`).** Consumers are **NOT** safely memoizable per-class.

- **Non-consumers** (no member mentions any forced container, over the whole-run cumulative set).
  These never gain or lose a member from *any* forcing — pass 3 already skips them (#156), and
  their pass-1 result is candidate-invariant. This is the memoizable subset. **But invariance
  still has to survive hazard #2 (policy) and hazard #3(2) (tracker-driven `canResolve`), below.**

#### (b.4) The IGNORE/INCLUDE first-resolution trap — does it reappear? (hazard #2) — **YES, on the non-consumers too.**

This is the trap that forced #156 to leave pass 1 eager, and it **re-applies to cross-request
memoization**, one level subtler:

Under the **IGNORE_MISSING broad path** (clangwalk — the actual #10 target), a non-consumer `C`'s
member `C::m() -> D` where `D` is another bound class resolves in pass 1 **iff `D` is in the
tracker at that moment**. The tracker is seeded by `withClasses(scopedFound)` where `scopedFound`
= base-bound keys ∪ the forcing struct. Across requests the base-bound set is stable — **but
`alreadyBoundKeys` grows** (IndexedServiceImpl.kt:185, recomputed from the accumulating `classes`
each request), because each request appends its forced container(s) as new `ResolvedClass`es. So
a **later** request's `scopedFound` can include a prior-forced container as a bound key, which can
flip `canResolve(D)` for a member of `C` that references it. Concretely: a non-consumer w.r.t.
*this* request's container can still reference a container forced by a *prior* request; whether
that reference resolves in pass 1 depends on whether that prior container re-entered the scoped
set — an order- and history-dependent fact. A per-class memo keyed only on the class name would
serve request-2's answer to request-5 and silently drop or keep the wrong member.

Worse, under **INCLUDE_MISSING** (featuregen — the *only* machine-check we have) a non-consumer's
member that references an unbound sibling **materializes it on demand and mutates the tracker**
(Resolver.kt:738-747). If request 1 resolved `C` and, as a side effect, pulled sibling `S` into
the tracker, then request 2's `C` — served from a cross-request memo — would **skip that
side-effecting pull**, and `S` would be absent from request 2's tracker when some *other* class
in request 2 needs it. The memo would change not `C`'s bytes but a **sibling's** availability →
divergence located far from the memo, invisible at the call site. This is the same shape as the
#156 pass-1 trap ("skipping a class shifts its first resolution's side effects"), now triggered
by *reusing* a class's resolution rather than *deferring* it.

**Verdict on hazard #2:** a bare per-class memo is unsafe under *both* policies. It is only sound
if the memo key pins **the entire tracker-visible resolution context** the cached value was
produced under — i.e. the memo must be invalidated whenever the set of keys the class's members
can `canResolve`-reach changes. For the base-bound non-consumer set under IGNORE_MISSING that set
is *almost* stable, but "almost" is exactly the fragility #156 refused to trade a provable
equivalence for.

#### (b.5) The only provably-safe memo key

The memo can be consulted for a class `C` in request `R` **only if** all of:

1. `C ∈ non-consumers` over the **whole-run cumulative** container set (never a container
   consumer in any request — so pass 3 never touches it and forcing can't change its members);
   **and**
2. every type `C`'s members reference resolves the **same way** in `R` as in the request that
   populated the memo — operationalized as: **the set of tracker keys reachable from `C`'s member
   type-strings is unchanged between the two requests.** The cheap conservative proxy for "the
   reachable set is unchanged" is: **`C` references no forced container (already ⊆ condition 1)
   AND references no class that is itself a container consumer** (a consumer's own shape changes
   per request, so a class referencing one inherits that request-dependence transitively); **and**
3. the run is **IGNORE_MISSING**. Under INCLUDE_MISSING the on-demand materialization side effect
   (b.4) makes even a "value-identical" memo hit skip tracker mutations a sibling depends on;
   INCLUDE_MISSING is featuregen's policy and its output must stay byte-identical, so the memo
   must be **disabled entirely under INCLUDE_MISSING** (it buys nothing there anyway — featuregen
   is ~120 classes × 7 requests, already cheap, and is the machine-check we cannot perturb).

So the safe memo key is **`(class-type-string)` scoped to the IGNORE_MISSING, whole-run-non-
consumer, non-consumer-referencing subset**, and its stored value is the finished `ResolvedClass`
(hazard #3(1)). Equivalently: memoize pass 1 for the classes that are **opaque leaves of the
forcing dance** — bound once, never recovered, never a side-effecting INCLUDE pull, never
referencing anything that changes shape. Everything else re-resolves per request as today.

#### (b.6) How big is the safe subset, and what is the realistic speedup?

At broad Clang scale (~3221 bound), the container-consumer set is "a few dozen" (the doc's own
estimate; the classes with `decls()/methods()/bases()`-style range accessors — `DeclContext`,
`CXXRecordDecl`, `TranslationUnitDecl`, a handful of `*List`/range holders). The
consumer-*referencing* exclusion (condition 2) is larger but still a minority — most of the 3221
(`llvm::vfs`, `llvm::opt`, `clang::ento`, `llvm::cl`, Sema internals, the serializer) neither
consume nor reference containers; they are exactly the opaque leaves the memo targets. So the
**memoizable subset is the overwhelming majority — plausibly ~3000 of ~3221.**

Cost model. Today (post-#156): per request, pass 1 = O(bound) ≈ 3221 resolves, pass 3 =
O(consumers) ≈ dozens. Over N requests: **N × 3221** pass-1 resolves. With the memo: request 1
pays 3221; requests 2..N pay only (consumers + consumer-referencing) ≈ hundreds, served-from-memo
for the ~3000 leaves. Over N requests: **3221 + (N−1) × ~few-hundred** — i.e. pass 1 collapses
from O(bound × N) to ≈ O(bound + refset × N). For the realistic broad config (a handful of
forced containers, N small), that is the difference between "3221 × N re-resolves of a densely
cyclic graph" and "one full pass plus a few cheap deltas."

#### (b.7) Does (b) actually get broad forcing to codegen?

Honest answer: **(c)-pass-3 + (b)-pass-1-memo together remove *both* O(bound) whole-set walks**,
which is the whole per-request cost `resolveForcing` adds. What remains per request after both is
the container force (pass 2, O(1) container) + the consumer/ref re-resolve (pass 3 + memo misses,
O(hundreds)). That is no longer O(bound); it is proportional to the *forced* surface. So the
compounding stall the re-baseline hit (never reaching "Forcing introduced") is removed **in
principle** by (b) on top of #156.

**But** — and this is the load-bearing caveat — the base bind itself (`filterAndResolve`, §1a)
still resolves ~3700 classes **once**, and the broad `INCLUDE_MISSING`/`DefaultFilter` blow-up in
Scenarios A/B (§2) is that *base* cost plus the densely-cyclic graph, not the forcing loop alone.
(b) makes *forcing* cheap; it does **not** make the base bind cheap, and it does not change what
gets bound (§3(i) — that's consumer config). So (b) plausibly gets *forcing* to codegen, and the
*next* wall becomes the base-bind / broad-C++-compile tail — the separate "harden broad binding"
track the campaign already anticipated (§"Out of scope"). Order-of-magnitude: forcing drops ~2
orders (as (c)-both projected), base bind is unchanged.

#### (b.8) Byte-identical verification plan (the post-flip machine-check)

The parity/17-unit gates are retired (see the #156 correction below). The equivalent check:

1. **Regenerate featuregen's `build/krapped-cpp/` tree** twice — memo OFF vs memo ON — and
   **diff byte-for-byte.** Because the memo is **disabled under INCLUDE_MISSING** (condition 3),
   featuregen (INCLUDE_MISSING) must be *byte-identical by construction* — the memo code path is
   never entered. This gate proves the guard, not the memo, but that is the safety-critical claim:
   the change cannot perturb the only machine-check we have.
2. **`DeterminismTest.twoInProcessRunsAreByteIdentical`** stays green — the memo is a process-
   scoped cache and MUST be reset per run (like `DropLedger`/`GenerationContext`,
   IndexedServiceImpl.kt:98-107) or it leaks across in-process runs (the exact class of bug #11a
   fixed for `elementLookup`). This test is the standing guard for that.
3. **A targeted unit** (new): two instantiation requests over a fixture with (i) a container
   consumer, (ii) a non-consumer opaque leaf, (iii) a non-consumer that *references* a consumer,
   all under IGNORE_MISSING. Assert: the leaf is resolved once and served from memo on request 2
   (instrument a hit counter); the consumer and the consumer-referencer are re-resolved each
   request; and the final emitted set is byte-identical to the memo-OFF run. This is the only test
   that actually exercises the memo path (featuregen can't — it's INCLUDE_MISSING).
4. **Broad IGNORE_MISSING smoke** (the payoff measurement): run the clangwalk-style `only(...) +
   IGNORE_MISSING` broad config with several `--instantiate` specs; confirm forcing now reaches
   "Forcing introduced …" and codegen in seconds, and capture the base-bind time separately so the
   remaining (base-bind) wall is attributed correctly, not to forcing.

#### (b.9) Blast radius

- **Code:** a process-scoped `Map<String, ResolvedClass>` memo, populated/consulted in
  `resolveForcing`'s pass-1 loop **only** on the IGNORE_MISSING branch and **only** for classes
  passing the whole-run-non-consumer + non-consumer-referencing predicate; reset in the
  `IndexedServiceImpl` init block alongside `DropLedger`/`GenerationContext`. No change to pass 2,
  pass 3, dedup, or the base resolve.
- **Behavioral surface it can touch:** the IGNORE_MISSING forcing path (clangwalk / broad #10).
  It **cannot** touch the INCLUDE_MISSING path (guarded off) — so featuregen and the plugin
  default are untouched by construction.
- **The failure mode if the predicate is wrong:** a class wrongly classified as a memoizable leaf
  would serve a stale resolution → a dropped/kept-wrong member on the IGNORE broad path,
  **invisible to the featuregen machine-check** (same blind spot as #156). This is why condition 2
  (exclude consumer-referencers) is deliberately conservative, and why unit test #3 above asserts
  the predicate directly rather than trusting the featuregen diff.

#### (b.10) Recommendation on Option (b)

**Proceed-with-caveats, and only after (c)/#156, and only guarded to IGNORE_MISSING.** The
mechanism is sound *if* the memo is (i) value-based (`ResolvedClass`, not a re-run against a
mutated `WrappedClass`), (ii) restricted to the whole-run non-consumer, non-consumer-referencing
subset, (iii) disabled under INCLUDE_MISSING, and (iv) process-reset per run. Under those four
conditions it is byte-identical on the machine-check *by construction* (the check never enters the
path) and removes the last O(bound)-per-request walk.

**The honest caveat that tempers the enthusiasm:** the byte-identical *proof surface we have*
(featuregen, INCLUDE_MISSING) **cannot exercise the memo at all**, because the memo is disabled
there. So (b)'s correctness on the path that matters (IGNORE_MISSING broad) rests on the
*predicate argument* + the targeted unit (test #3), **not** on the regenerate-and-diff machine
check — exactly the gate-blindness that already bit #156's pass-1 attempt. That is a real,
named risk: we are asserting safety on the un-gated path by construction-and-unit-test, which is a
weaker guarantee than the featuregen diff gives on the gated path. It is *defensible* here only
because the memo is a strict subset-skip of work whose output last-wins already reproduces — but
reviewers should weigh that the machine-check is structurally unable to catch a predicate bug.

**If that residual risk is judged too high for the win:** the fully-safe alternative is to **stop
treating broad forcing as an in-CI gate at all** and run it as an **offline long-running
measurement** — accept that `resolveForcing` is O(bound × N) and simply let the broad force run
for its tens of minutes as a periodic (not per-build) probe of the real #10 question (does the
broadly-bound C++ compile?). #156 already made it ~2× faster; if the base-bind wall (b.7) is the
real gating cost anyway, the pass-1 memo buys a faster *forcing* stage in front of an unchanged
*base-bind* stage, and the offline-measurement framing may be the better cost/risk trade than
adding an un-machine-checkable cache to the load-bearing resolve loop.

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
Do (c) first (kills the pass-3 dominant cost, zero output risk — shipped as #156). If the
remaining per-request **pass-1** cost (still O(bound) every request — #156 could not filter
pass 1 safely, see the correction below) is material at scale, add (b)'s cross-request memo
for the **non-consumer** subset only, guarded to IGNORE_MISSING. See the fully-specified
(b.1)–(b.10) design above for the exact key, the four safety conditions, and the honest
caveat that the featuregen machine-check structurally cannot exercise the memo path.

**Correction to the original sketch:** the terse Hybrid note above said "(b)'s cross-request
memo for the container-consumer set only." That is **backwards** — the (b.3) analysis shows
container consumers are precisely the request-DEPENDENT classes that must NOT be memoized
(their method set changes per forced container). The memoizable subset is the **non-consumers
that also reference no consumer**. Read the (b.1)–(b.10) section as authoritative.

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
  memoization** — now fully specified in the expanded (b.1)–(b.10) section above. NB the
  memoizable subset is the **non-consumer** set, NOT the consumer set (consumers are exactly
  the request-DEPENDENT classes — see (b.3)); and the "safe because it changes *when* not
  *which policy*" intuition holds **only** under the four conditions (b.5) pins down (value-
  based, non-consumer + non-consumer-referencing, IGNORE_MISSING-only, process-reset). The
  honest caveat (b.10): the featuregen machine-check runs INCLUDE_MISSING and therefore
  **cannot exercise the memo path at all**, so (b)'s safety on the IGNORE_MISSING broad path
  rests on the predicate argument + a targeted unit, not the regenerate-and-diff gate — the
  same gate-blindness that bit #156's pass-1 attempt. A valid alternative to accepting that
  residual risk is to run broad forcing as an **offline measurement**, not a per-build gate.

- **Retired gate names (flip landed).** Earlier drafts of this doc cited
  `:cppfrontend:featuregenParity` and "the 17-instantiation ALL unit" as the safety gate;
  **both no longer exist** (the Phase-E flip removed the parity harness and that unit, and
  the `cppfrontend` module is now `krapper_parse`). The equivalent machine-check used to
  validate PR #156 — now written throughout this doc — is **regenerating featuregen's
  `build/krapped-cpp/` binding tree and diffing it** against the unfiltered baseline
  (byte-identical), which is what was done for this change.

### Verification plan (how we prove no regression)
1. **Byte-identical gate (blocking):** regenerating featuregen's `build/krapped-cpp/`
   binding tree under `--frontend=cpp` and diffing it against the unfiltered baseline must
   show zero regression. Because (c) only *skips re-resolving classes that recover
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
