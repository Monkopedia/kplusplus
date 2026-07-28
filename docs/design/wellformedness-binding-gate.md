# Well-formedness binding gate (#10 broad-surface tail)

> **Historical record — written 2026-07-19, committed 2026-07-28.** The design was IMPLEMENTED
> as the two gates in #175. Paths and flags below are as they were at authoring time and are
> deliberately NOT rewritten (rewriting them would falsify the record). Since then:
> `:krapper_parse` + `:krapper_gen` merged into `:krapper` (#184, `b787196`), so
> `krapper_parse/src/nativeMain/kotlin/…` is now under `krapper/src/nativeMain/kotlin/com/monkopedia/krapper/parser/…`
> and `krapper_gen/…` is `krapper/…`; and `-PenableClang` was REMOVED in #173 — LLVM is
> unconditional, so drop that flag from any command here.
>
> **Superseded in part:** §3's core finding — that a live `clang::Sema` is unavailable in the
> read-the-AST architecture, making per-capability AST predicates the honest shape — is exactly
> the premise that #186 (persistent, queryable krapper service) removes. Jason ruled on
> 2026-07-28 that the FIR plugin becomes a live client. Re-read §3 and §7 in that light before
> extending the per-family predicate approach: a live Sema makes those questions askable rather
> than guessable.

**Status:** design / feasibility investigation (no production change)
**Author:** monkopedia-coder investigation pass, 2026-07-19
**Branch context:** `fix/broad-surface-cluster-0.3.4`
**Evidence:** cached broad-force errors at `/tmp/kpp10-branch2/errors.txt` +
`clang_slice.h.cc`; a standalone libTooling AST-query probe (`/tmp/wfprobe/probe.cpp`,
clang 22.1.6, the same LLVM krapper_parse links).

---

## 1. Problem framing

The #10 broad-surface campaign fixed five systematic *spelling* bugs
(1531 → ~245 broad C++ compile errors). The remaining tail is a different KIND
of defect: the generator binds a member whose **body won't instantiate for the
given type** — not a mis-spelled name, but "this element type can't support this
operation." Two clusters dominate:

- **invalid-operands (~35 distinct element types).** The generator bound
  `llvm::ArrayRef<T>::equals` and `std::vector<T>::operator==` for element
  types `T` that define no `operator==` (`clang::Token`, `clang::TemplateArgument`,
  `clang::Builtin::Info`, `llvm::SMRange`, `clang::LambdaCapture`, …). The
  container's `==` instantiates `std::equal` → `*a == *b` on `T` → fails deep in
  `bits/stl_algobase.h:1197`, surfaced from `ArrayRef<T>::equals`
  (`clang_slice.h.cc:14186`) or the `std::vector` `operator==` path.
- **deleted-default-ctor (~13 distinct types, ~25 error lines).** The generator
  synthesizes a default `_new` for classes whose implicit default constructor is
  deleted. Four deletion reasons appear in the tail (counts from `errors.txt`):
  base class has no default ctor (5, the `llvm::StringMapEntry<...>` family),
  field has no default ctor (3), field has a deleted default ctor (3, the
  `Module::Header`/`DirectoryName` → `FileEntryRef`/`DirectoryEntryRef` family),
  field of const-qualified type (2, `TargetInfo::GCCRegAlias`/`AddlRegName`, a
  `const char *const[5]`).

Jason's framing: instead of fixing cluster-by-cluster, **gate each binding on
whether its body will actually resolve/compile** — programmatically, using the
compiler we already link. Skip the binding if not, generically.

---

## 2. Where bindings are decided (the hook map)

### 2.1 krapper_parse — model construction (has the live AST)

`krapper_parse/src/nativeMain/kotlin/ModelBuilder.kt` walks the parsed Clang AST
and emits the `WrappedTU`. The relevant decision sites:

- **`addBasesAndMembers` → `addMember` → `buildMethod`** (lines ~353–554): every
  public member method (including a template pattern's `operator==` / `equals`)
  becomes a `WrappedMethod`. The instantiated container's members flow through
  exactly this path. **This is the hook for the invalid-operands gate** — the
  `CXXMethodDecl` and its receiver's element type are live AST here.
- **`addField` / `recordFilteredMember`** (lines ~448–501): already computes
  structural deletion facts. `addField` sets
  `metadata.hasDeletedDefaultConstructor = true` for a **reference field**
  (line 498) and `hasDeletedCopyAssignment` for reference/const fields.
  **This is the existing, incomplete deleted-ctor gate** — it covers only the
  reference-field reason, not deleted-base / deleted-field-ctor / const-array.

### 2.2 krapper_gen — resolution (structural predicates only)

`krapper_gen/.../ModelResolution.kt`:

- **`modifyMethodsIfNeeded`** (lines 258–301) is where the deleted-ctor cluster
  is *created*. The synthesized `_new`:

  ```kotlin
  if (!isAbstract &&
      !metadata.hasConstructor &&
      !metadata.hasDeletedDefaultConstructor &&        // <-- the gate, today
      !baseClasses.any { it.metadata.hasConstructor }
  ) { addChild(WrappedConstructor("new", type, isDefaultConstructor = true)) }
  ```

  The `_new` is emitted whenever `hasDeletedDefaultConstructor` is false. Because
  that flag is only set for reference fields, the other three deletion reasons
  slip through and produce an uncompilable `_new`.

### 2.3 Existing narrow well-formedness gates (the pattern to unify)

The codebase already gates bindings on **AST-fact predicates computed without a
live Sema**:

- **#152 / #123** (`TypeBuilder.kt:282`, `ModelBuilder.addMember`): skip a member
  whose signature names an inaccessible (protected/private) nested type; drop a
  deleted/private member (`FunctionDecl::isDeleted()`, access specifier).
- **#163** (`codegen/CastTargets.kt:140` `downCastTargetsFor`): emit the
  `llvm::dyn_cast<D>` down-cast **only when it is well-formed for the base** —
  computed structurally from `classof` presence and the inheritance chain, at
  codegen time. No compiler invocation.

The general gate proposed here is the **same species** as #163: an AST-fact
predicate, evaluated once per binding, that answers "would this body resolve?"
for a bounded set of capability families. It unifies and extends #152/#163.

---

## 3. Sema-liveness finding (THE feasibility question)

**Verdict: a live `clang::Sema` is NOT needed, and the AST-only path is
sufficient for BOTH tail clusters.** Two independent findings:

1. **What the generator already does.** Every existing gate (#152/#163) and every
   deletion fact (`hasDeletedDefaultConstructor`, `hasDeletedCopyAssignment`,
   `isCopyConstructor`, `isDefaultConstructor`, `isDeleted`, `hasDefaultArg`) is
   an **AST predicate**, not a Sema query. The generator is architected around
   reading a finished AST, not driving semantic analysis. `krapper_parse` builds
   an `ASTUnit` via `kppbridge`/`buildASTWithArgs`; by the time `ModelBuilder`
   runs, parsing is complete and the code operates on the AST. There is no
   established live-Sema plumbing, and the bound `clang_slice.h` surface exposes
   **no overload-resolution / expression-synthesis API** (no `Sema::CheckXxx`,
   no `ActOnBinOp`) — a grep for callable `clang_Sema_*` wrappers finds none of
   the resolution entry points. Adding them would mean binding a large, stateful,
   diagnostic-emitting subsystem and keeping Sema alive past parse — a
   substantial new surface with its own correctness risk.

2. **What the AST already answers (probe-verified).** The standalone probe
   (`/tmp/wfprobe/probe.cpp`) loaded clang's own headers and queried the exact
   failing types with **AST DeclContext lookup + `CXXRecordDecl` predicates only**
   (no Sema). Results below settle both clusters.

**Conclusion:** mechanism **(A′) — the narrow AST-lookup gate — is the pragmatic
win.** The general Sema well-formedness probe (A) would need live-Sema plumbing
we do not have and do not want; (A′) covers the entire measured tail without it.

---

## 4. Probe results (evidence)

Built with `clang++ -std=c++17 -lclang-cpp -lLLVM`, run over clang's own headers.

### 4.1 invalid-operands — `operator==` DeclContext lookup

For each element type, the probe did an AST lookup for `operator==` as a member
(walking bases) and as a free function in the enclosing namespace:

```
[EQ] clang::Builtin::Info     member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::CharSourceRange   member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::FixItHint         member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::LambdaCapture     member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::TemplateArgument  member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::Token             member_or_base_op==:0  free_ns_op==:0   (fails → drop)
[EQ] clang::SourceLocation    member_or_base_op==:0  free_ns_op==:1   (VALID → keep)  ← control
[EQ] llvm::StringRef          member_or_base_op==:0  free_ns_op==:1   (VALID → keep)  ← control
```

**Clean separation, zero false positives.** All failing types have neither a
member/base nor a free `operator==`. Both controls (which DO compile
`std::equal`) are correctly retained via their **free** `operator==` — confirming
the free-function scan is load-bearing (a member-only check would false-drop
them).

### 4.2 deleted-default-ctor — `CXXRecordDecl` predicates + structural check

`hasDefaultConstructor()` is **useless as the signal** — it returns `1` even for
an implicitly-deleted default ctor (it means "declared," not "usable"). The AST
exposes two usable states depending on whether the parse already materialized the
lazy implicit member:

```
[CTOR] clang::Module::Header          needsImplicit:0  foundDeletedDefaultCtorDecl:1  STRUCT_deleted:0   → deleted (direct)
[CTOR] clang::Module::DirectoryName   needsImplicit:0  foundDeletedDefaultCtorDecl:1  STRUCT_deleted:0   → deleted (direct)
[CTOR] clang::TargetInfo::GCCRegAlias needsImplicit:1  foundDeletedDefaultCtorDecl:0  STRUCT_deleted:1(const-array)  → deleted (structural)
[CTOR] clang::TargetInfo::AddlRegName needsImplicit:1  foundDeletedDefaultCtorDecl:0  STRUCT_deleted:1(const-array)  → deleted (structural)
[CTOR] clang::Token                   needsImplicit:1  foundDeletedDefaultCtorDecl:0  STRUCT_deleted:0   → USABLE (keep) ← control
[CTOR] clang::CharSourceRange         needsImplicit:0  foundUsableDefaultCtorDecl:1   STRUCT_deleted:0   → USABLE (keep) ← control
[CTOR] clang::SourceLocation          needsImplicit:0  foundUsableDefaultCtorDecl:1   STRUCT_deleted:0   → USABLE (keep) ← control
```

So the reliable deleted-default-ctor detector is a **two-step hybrid**:

1. **Direct:** iterate `ctors()`; if a `CXXConstructorDecl` that
   `isDefaultConstructor()` also `isDeleted()`, the default ctor is deleted.
   (Catches the `Module::Header`/`DirectoryName` family — clang had already
   materialized the decl because it was odr-used in the parse.)
2. **Structural** (when the implicit default ctor is still lazy,
   `needsImplicit:1`): apply `[class.default.ctor]` deletion rules over the AST —
   for each base and each non-in-class-initialized field: a base/field of record
   type whose own default ctor is unusable (recurse), a reference field, or a
   const-qualified field of scalar/array/pointer type (`const char *const[5]`).
   This is the **existing `addField` logic (line 494–499) generalized** — it
   already handles the reference-field case.

Controls (`Token`, `CharSourceRange`, `SourceLocation`) are correctly retained:
either a usable default ctor decl exists, or the structural check finds no
deleting member. **Zero false positives.**

`defaultedDefaultConstructorIsDeleted()` — the single bit the earlier coder
looked for — genuinely **does not exist as a public method in clang 22**
(compile-checked: "no member named …"). The hybrid above is the correct
replacement and needs no new predicate that clang doesn't offer.

---

## 5. Bound-surface readiness (can the gate be written today?)

The gate primitives are essentially all already in the bound `clang_slice.h`
surface (checked against `/tmp/kpp10-branch2/clang_slice.h.h`):

| primitive | bound? | note |
|---|---|---|
| `CXXRecordDecl::ctors` | yes | direct deleted-default-ctor scan |
| `CXXConstructorDecl::isDefaultConstructor` | yes | |
| `FunctionDecl::isDeleted` | yes | already used (#152) |
| `CXXRecordDecl::bases` | yes | structural recursion |
| `DeclContext::decls` + `asFieldDecl` | yes | field iteration (already used in `addField`) |
| `QualType::isConstQualified` (via `buildWrappedType(...).isConst`) | yes | already used |
| `FieldDecl::hasInClassInitializer` | yes | in-class-init exemption |
| `DeclContext::lookup` | yes | free/member `operator==` lookup |
| `CXXRecordDecl::hasDefaultConstructor` / `needsImplicitDefaultConstructor` | yes | state discriminator |

The only construction the operator-lookup path needs beyond these is a
`DeclarationName` for `operator==` to feed `DeclContext::lookup`. Two options:
(a) bind `ASTContext::DeclarationNameTable::getCXXOperatorName(OO_EqualEqual)`
(one small new binding), or (b) skip `lookup` entirely and scan the record's
existing member/base/enclosing-namespace decls (already walked by the model) for
a method named `"operator=="` — **no new binding at all.** Option (b) is
recommended: the model already visits every member, and the enclosing-namespace
free-operator scan is a bounded `decls()` walk.

**Effort to add bindings: ~zero to one small predicate.** No live-Sema surface.

---

## 6. Recommended mechanism: (A′) narrow AST-lookup gate

Reject (A) live-Sema probe — plumbing we lack, large risky surface, unnecessary.
Reject (B) generate-compile-prune as the primary — the errors surface in STL
headers (`stl_algobase.h`), not our line, so back-attributing a deep
template-instantiation failure to the offending element wrapper is fiddly and
slow (a full compile per prune iteration on a ~13 MB `.cc`). (B) is retained only
as the **verification oracle** (§8), not the mechanism.

**Adopt (A′)**, two focused gates:

### Gate 1 — container value-equality (invalid-operands)
In `ModelBuilder.buildMethod` (or a resolve-time pass on the container element),
when about to bind a container member whose body requires `T == T`
(`ArrayRef<T>::equals`, `ArrayRef<T>::operator==`, `vector<T>::operator==`, and
the `!=` peers), consult the **element type `T`**: if `T` is a record type that
declares no member/base/enclosing-namespace `operator==` (and is not a
builtin/enum/pointer scalar, which have a builtin `==`), **skip that member** and
log to `DropLedger`. Probe §4.1 proves this separates the 35 failing types from
the valid ones with no false drops.

### Gate 2 — synthesized default ctor (deleted-default-ctor)
Generalize the existing `hasDeletedDefaultConstructor` computation so
`modifyMethodsIfNeeded`'s `_new` guard (`ModelResolution.kt:273`) covers all four
reasons. Feed it from krapper_parse via the hybrid detector (§4.2): set
`metadata.hasDeletedDefaultConstructor` when either a materialized default ctor
decl is deleted OR the structural `[class.default.ctor]` check fires. This
extends `addField`'s current reference-field branch to bases, deleted-field-ctor,
and const-array fields. The synthesized `_new` then simply isn't emitted for
these types — no uncompilable constructor.

Both gates are **AST-fact predicates in the established #163 style**, logged
through the existing `DropLedger` (`DropPhase.PARSE` for gate 1 at model build,
`RESOLVE` for gate 2).

---

## 7. Generalization + reach

- **Gate 2 covers the entire deleted-default-ctor cluster** — all four deletion
  reasons present in the tail map onto the two-step detector; the largest
  sub-families (`StringMapEntry<...>` deleted-base, `Module::*` deleted-field,
  `TargetInfo::*` const-array) are all handled. ~13 types / ~25 error lines.
- **Gate 1 covers the entire invalid-operands cluster** — 35 distinct element
  types, all failing for the one reason "T has no `==`," all detected by the
  lookup. ~35 types.

Together ≈ **most of the remaining ~245 broad errors** (the two named clusters).
The residual tail (rvalue-param-init, no-matching-ctor for *other* reasons,
template-arg-required, etc.) is **out of scope** for these two gates but is the
same *species* of problem — the (A′) pattern (an AST-fact capability check per
binding family) is the reusable template for picking those off next, one family
at a time, each with its own probe-validated predicate.

The gate is **general in mechanism** (a per-binding capability predicate over the
AST) but **specific in each predicate** (one per capability family: value-equality,
default-construction, …). That is the honest shape: there is no single AST query
that means "will any arbitrary body compile" without a live Sema; there IS a cheap
AST query for each concrete capability a bound member's body requires.

---

## 8. Safety property + verification

**Invariant:** the gate must drop ONLY genuinely-uncompilable bindings, NEVER a
valid one.

Design safeguards:

- **Conservative predicates.** Each gate drops only on a *positive* proof of
  un-compilability (no `==` anywhere in scope; a concretely-deleting base/field).
  When the AST is ambiguous the gate **keeps** the binding — a false *keep*
  merely leaves an existing error in place (no regression), whereas a false
  *drop* loses a valid method (the forbidden outcome). Probe §4.1/§4.2 show the
  controls are retained, i.e. the predicates already err on the keep side.
- **Auditability.** Every drop funnels through `DropLedger.record(symbol, reason,
  phase)` (the existing skip-not-crash ledger), turning "is this binding missing
  because C++ lacked it or because we dropped it?" into a diffable list.

Machine-checkable verification (the safety gate for the change):

1. **byte-identical featuregen SYNC** (`:featuregen:kplusplusSync`, cpp path,
   `-PenableClang`) must stay green — the ~195 behavioral rows exercise real
   containers (`vector<string>`, `vector<Item*>`, value types WITH `==`, types
   WITH usable default ctors). If the gate dropped a *valid* value-equality or
   `_new`, a featuregen row loses a method and the run reddens. Re-run after the
   generator change (MEMORY discipline: nativeTest alone misses this).
2. **byte-identical clangwalk / cppfixture** — the self-host parse of clang's own
   headers must produce the same tree modulo the two intended drops. Diff the
   `DropLedger` output before/after: the new drops must be **exactly** the 35 +
   ~13 targeted symbols and nothing else. Any extra drop is a false positive to
   investigate.
3. **(B) as oracle.** Re-run the broad force; the 35 invalid-operands + ~25
   deleted-ctor error lines must disappear and **no new errors** appear.
   Remaining error count should drop from ~245 toward ~185. Using the real
   compiler here confirms the AST predicate matched the compiler's own verdict.

The union — featuregen keeps every valid method, the ledger diff is exactly the
targeted set, and the broad-force error count falls by the cluster size with no
new errors — is the machine-checkable proof of the safety invariant.

---

## 9. Effort / risk / recommendation

**Effort:** small–moderate.
- Gate 2 (deleted ctor): generalize an existing ~6-line structural check
  (`addField`) into a small recursive `defaultCtorUnusable(record)` helper +
  wire the direct `ctors()`/`isDeleted` scan; feed the existing
  `hasDeletedDefaultConstructor` flag. **No new bindings.** ~½ day incl. tests.
- Gate 1 (value-equality): identify the container-`==`/`equals` member families
  and add the element-type `operator==` scan (member/base/enclosing-namespace,
  builtin-scalar exemption). Uses already-walked decls; no (or one trivial) new
  binding. ~½–1 day incl. tests.
- Verification: one featuregen SYNC + one clangwalk diff + one broad-force
  re-measure per gate.

**Risk:** low. The predicates are conservative (keep-on-doubt), logged, and
guarded by byte-identical featuregen + ledger-diff. The main subtlety —
lazy implicit-member materialization — is exactly why the hybrid (direct decl OR
structural) is required; the probe confirms the structural branch fires precisely
when the direct decl is absent.

**Recommendation: implement (A′) as the two gates above.** It clears both named
clusters (~35 + ~13 types, most of the ~245 residual) cheaply, with no live-Sema
plumbing, reusing the established #163 AST-predicate pattern and the DropLedger
audit. The general Sema well-formedness probe (A) is not worth its plumbing/risk
when the AST already answers every question the measured tail poses. Generate-
compile-prune (B) is kept as the verification oracle, not the mechanism. The
residual non-cluster tail is deferrable and attackable with the same per-family
(A′) recipe when owner appetite warrants.
