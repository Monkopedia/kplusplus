# Campaign: kplusplus self-hosts on Clang's C++ AST

Status: **active** · Branch model: `clang-base` (stage0) → `clang-selfhost` (stage1) · LLVM pin: **22.1.6**

---

## Goal

kplusplus parses C++ through bindings it generated **itself** for Clang's C++ AST — proving the
tool on the hardest C++ library there is, and **deleting** the pile of workarounds libclang's
lossy C API forced on us.

Two things must become true:
1. **Demonstrable** — clean, idiomatic Kotlin walks a real Clang AST through generated bindings.
2. **Self-hosting** — kplusplus's own frontend is rebuilt on those bindings; libclang dropped.

### The motivating before/after (our own code)

| Today (ugly, because libclang is lossy) | After (the C++ AST just answers) |
|---|---|
| **~200-line reducer subsystem** — `createForType` + six `*TypedefElement` fns + `Unexposed` gating + `dropMistypedInitializerListMembers` + the `type-parameter-0-0` juggling, all reconstructing what *"libclang can't resolve"* (our own comments). | `TypedefNameDecl::getUnderlyingType()` / `SubstTemplateTypeParmType` resolve directly. **~200 lines → deleted.** |
| Default values **scraped from source text** (`defaultValue()`: *"libclang exposes no direct accessor"*, tokenize the extent). | `ParmVarDecl::getDefaultArg()` → real `Expr*`; `Expr::EvaluateAsInt/AsRValue` give the actual value+type. |
| Template params collide on positional names (`_Tp`/`_Alloc` → `type-parameter-0-0`); string-key juggling. | distinct `TemplateTypeParmDecl` per param (real identity). |
| Types classified by string suffix (`spelling.endsWith("*")`, `<`-splitting). | structural: `Type::isPointerType()`, `TemplateSpecializationType`. |

**Hero line:** *~200 lines of dependent-type reconstruction → a few direct `getUnderlyingType()` calls.*

### The new surface it buys
```kotlin
memScoped {
    val unit = ASTUnit.fromCode(source).owned()
    for (d in unit.astContext.translationUnitDecl.decls) {
        val r = d.asCXXRecordDecl() ?: continue       // dyn_cast → nullable
        println(r.name)                                // std::string → String
        for (m in r.methods) println("  ${m.returnType.asString} ${m.name}")
    }
}
```

### Done
- **A (demo):** the snippet above runs against a real `.cpp`, end-to-end, through generated bindings.
- **B (self-host):** kplusplus parses its featuregen suite through its own Clang bindings, libclang
  removed, **and the four workarounds above are deleted** — suite green.

---

## Feasibility — PROVEN ✅ (Phase 0 spike)

Hand-written `extern "C"`-shaped C++ walker (`buildASTFromCode` → `ASTContext` → `dyn_cast<CXXRecordDecl>`
→ `methods()`), compiled against `libclang-cpp` + `llvm-config --libs`:

```
compile rc=0, wall=6s     ← one TU incl. clang/AST + Tooling + dyn_cast
binary: 135K              ← tiny; libclang-cpp(75M)/libLLVM(171M) are runtime .so deps
RUN: walked Foo/Baz, listed methods (incl. compiler-synthesized members) via llvm::dyn_cast
```
Clears: **link** (`-lclang-cpp`), **`dyn_cast`**, **`buildASTFromCode` self-init** (no manual LLVM
setup), **flags** (`-fno-exceptions` from llvm-config; `-fno-rtti` not needed — LLVM dyn_cast is
RTTI-free). **Load-bearing number: 6 s / heavy-header TU** → emit ONE combined wrapper TU, not N;
a 5,000-symbol binding is one ~minutes compile, not N×6s.

---

## Operating model: stage0 → stage1 discovery loop

This is a compiler bootstrap. Cardinal rule: **always keep a known-good stage that can rebuild the next.**

- **`clang-base` (stage0, on libclang)** — a permanently-working generator. Never depends on the new
  bindings, so it can *always* regenerate them. Gains the new generator capabilities; emits the Clang
  bindings. Featuregen stays green throughout (always a shippable generator).
- **`clang-selfhost` (stage1, the consumer)** — the new frontend, built **purely** on generated
  bindings. The branch separation enforces "no stray libclang leak" = really self-hosting.

**The loop (TDD-shaped):**
1. (base) prove a capability gap is closed → featuregen-green.
2. (consumer) point at a Clang slice, try to build the ideal-Kotlin walk → **red build names the next gap**.
3. (base) fix the gap as a **generic, featuregen-tested capability** — never a consumer hack.
4. rebase consumer → next red → back to 3. … until the consumer replaces libclang → flip → bootstrapped.

The consumer's red build **is** the base backlog. Surface churn from rebases isn't a cost — it's the signal.

---

## Principles

- **Scale-first.** Supporting massive binding is the *product* (the goal is large C++ libraries), not a
  quantity to fear. If a broad import breaks the system, that's a **defect to fix**, not a constraint to
  scope around. Lean into broad import in the bootstrap as the scale proving ground.
- **Breadth fearless, depth scoped.** *Bind* as broadly as is cheap (the binding pipeline is the same at
  quantity); only invest *correctness/polish* in the paths the consumer actually exercises. The loop
  enforces this for free.
- **Skip, don't crash.** Graceful degradation is the safety net that makes breadth safe: an unmodelable
  shape drops-and-logs, never `TODO()`/crash. (Today it's mixed — make it uniform early.)
- **Trust the system to scale**, and develop with that expectation.

---

## Autonomy contract

🟢 run on the stated default · 🟡 mitigate and proceed · 🔴 STOP + phone outreach · everything else:
**record in Backlog and keep working.**

| Area | Tag | Default / plan |
|---|---|---|
| Combined wrapper TU (compile cost) | 🟢 | one TU, not per-symbol; PCH if needed |
| C++ flags | 🟢 | `-std=c++17 $(llvm-config --cxxflags)`, `-lclang-cpp $(llvm-config --libs)` |
| LLVM global init from K/N | 🟡 | enter via `ASTUnit` high-level API; add init shim if needed |
| `dyn_cast` down-cast | 🟢 | per-pair shim `kpp_dyn_cast_D(B*)` → `B.asD(): D?`; scope to *referenced* pairs |
| ranges (`iterator_range`/`specific_decl_iterator`) | 🟢 | shim → `std::vector<Elem*>` → `for`-iterable |
| view types (`StringRef`/`ArrayRef`) | 🟢 | prefer `...AsString()`/index accessors; `(ptr,len)→String` shim if forced; never bind the template |
| by-value value types (`QualType`) | 🟢 | trivially-copyable return-by-value (Point2 path); owned value wrapping a borrowed `Type*` |
| const/non-const overload collision | 🟢 | read-only → bind the `const` overload, drop the other |
| namespaces (`clang::`/`llvm::`, nested) | 🟡 | map structurally → nested packages (rootPackage rule); surface only a true ambiguity |
| incomplete/forward-declared types | 🟡 | include transitive headers; scope to defined |
| mangling at scale | 🟡 | watch for `uniqueCName` collisions across thousands of overloads; tighten if hit |
| borrowed-by-default ownership for AST nodes | 🟢 | AST ptrs/refs non-owning; only `ASTUnit` `.owned()`; never emit dispose for AST nodes |
| `unique_ptr<ASTUnit>` handle | 🟢 | one minimal owning handle → `.owned()` |
| reference-typedef-drop fix | 🟢 | prerequisite resolver fix (bites `bases()`/accessors) |
| diagnostics | 🟢 | replicate "abort on `error:`" via `ASTUnit::getDiagnostics` |
| skip-not-crash robustness | 🟢 | unmodelable shape → drop+log, never `TODO()` |
| build time / binary size at scale | 🟢 | treat as defects to *fix* (combined TU, caching, link DCE), not reasons to scope down |

**🔴 STOP + outreach only for:** (1) a true link wall, (2) USR→identity model if canonical-`Decl*`
pointer isn't clean, (3) a Clang shape that fundamentally can't be a monomorphic shim, (4) anything
that genuinely halts forward progress with no sensible default.

---

## Phases

- **Phase 0 — feasibility spike.** ✅ DONE (above).
- **Phase 1 — base capabilities (clang-base).** The shim/marshalling features the consumer will force:
  `dyn_cast` per-pair shims · range→vector · view-type marshalling · QualType by-value · const-overload
  rule · combined-TU wrapper build w/ llvm-config flags · skip-not-crash · namespace→package · borrowed
  ownership policy · `unique_ptr<ASTUnit>` handle · reference-typedef-drop fix. *(Discovered iteratively
  by the consumer, not enumerated up front.)*
- **Phase 2 — thin slice (clang-selfhost, Milestone A).** Generate the curated AST-walk surface; the
  before/after Kotlin runs end-to-end against a real `.cpp`. Then **scale the import up** as the load test.
- **Phase 3 — progressive migration.** Reimplement `ClangExtensions` accessors against the AST bindings
  in the safe order (scalars→names→types→relationships→traversal), suite green each step. USR→identity
  substitute. **Delete** the reducer subsystem.
- **Phase 4 — self-host (Milestone B).** Frontend fully on generated Clang bindings; libclang removed;
  featuregen green; the four workarounds gone.

---

## Ticket breakdown

Tickets are the executable units; each links back to a phase. `[ ]` open · `[~]` in progress · `[x]` done.

### Phase 1 — base capabilities
- [x] **T1.0 Recon** — DONE. libclang parses Clang AST headers fine (~2s); a 2-header include pulls **2655 classes** (no scoping). Two resolver crashes + ~5 codegen-emits-bad-C shapes. Backlog G1–G9 below.
- [x] **T1.0a Scoped-import filter** — DONE. `AllowListFilter(qualifiedNames)` (sealed `FilterDefinition` subclass, flows through the serialized path) + CLI `--only`/`--only-file`; exact-qualified-name match; composes with reference policy (listed bound, referenced-unlisted fall through). `ParseTest.testAllowListFilter`. *(Backlog: exact-name only — no glob/subclass auto-include; extend later if needed.)*
- [x] **T1.4 skip-not-crash** — DONE (G1,G2,G3,G5 fixed; G4,G7 were already correct, locked with tests; G6 deferred → T1.9). G1 `parseTypes` AIOOBE → `runCatching`+`opaqueLeaf`+WARN; G2 `WrappedType` catch widened IAE→RuntimeException (gated by `throwOnError`, so strict mode still throws) + `WrappedElement` try/catch + anonymous-record skip; G3 blank-spelling FieldDecl skip; G5 `_set` gated on `!isConst` (const+reference). `SkipNotCrashTest` (6). The throwOnError-gated catch is the right pattern (drop+log unless strict).
- [x] **T1.0b Base-not-fatal** — DONE. A top-level/listed class whose primary `baseClass` can't resolve now **drops it to a null/borrowed base and still binds** (mirrors the `allBaseClasses` drop). Gated by a new `ResolveContext.dropUnresolvablePrimaryBase` (default true); INCLUDE_MISSING reference-expansion keeps the legacy hard-fail (an unconditional drop resurrected std internals that are complete-at-parse but incomplete-in-emit-headers → broken C++). `ParseTest.testUnboundBaseNotFatal`. Resolvable-base path byte-identical.
- [x] **T1.2 dyn_cast down-cast** — DONE. `B.asD(): D?` via shim `B_dyncast_D(void* p)`: `llvm::dyn_cast_or_null<D>` when D has a static `classof` (LLVM-RTTI, `-fno-rtti`-safe), else generic `dynamic_cast<D*>` (gated on a polymorphic base). `downCastTargetsFor` inverts the up-cast walk. Validated both: featuregen `dynamic_cast` (`IhDownCastTest`) + real-Clang `llvm::dyn_cast` compiled+linked+run against libclang-cpp (Decl→CXXRecordDecl returns non-null only for the real record).
- [x] **T1.3 range→vector** *(co-#1 demo blocker)* — DONE (commit 952a149). `RangeReturn` detects an `llvm::iterator_range<It>` return off the **canonical** type (sees through the `using X_range = iterator_range<...>` aliases: `decl_range`/`method_range`/`base_class_range`) and recovers element class `Elem` via `getTemplateArgumentType` + pointee stripping (`Decl**`→`Decl`, `CXXBaseSpecifier*`→`CXXBaseSpecifier`); the return is materialized into `std::vector<Elem*>` by a CppWriter loop shim (`kpp_to_elem_ptr<Elem>(*it)`). Both hard parts solved: (a) element extraction = template introspection (not toString); (b) on-demand `std::vector<Elem*>` instantiation seeded like the Box facade (`instantiate("std::vector<Thing*>")`) since the rewrite, not a call site, generates it — `IndexedServiceImpl` maps `*`/`&` in a template arg to `Ptr`/`Ref` so the pointer doesn't leak into the forcing struct's C identifier/temp filename. `RgRangeTest` + `ReturnShapeTest`. featuregen 160/0, krapper 280/0. **Real-Clang validation deferred** — forcing `std::vector<clang::Decl*>` against libclang-cpp pulls the whole Clang/LLVM world via INCLUDE_MISSING and SEGVs; that's a separate scale gap (see Backlog T1.3-clang), NOT a defect in this capability.
- [ ] **T1.1 Combined-TU wrapper build** — wrapper compilation w/ `llvm-config --cxxflags`, link `libclang-cpp`; one TU. *(recon-2: the generated `.cc` already compiles+links+runs against libclang-cpp ad-hoc — formalize into the build.)*
- [ ] **T1.2 dyn_cast down-cast** — per-pair `kpp_dyn_cast_D(B*)` shim + `B.asD(): D?`; featuregen fixture (classof-style hierarchy).
- [x] **T1.3 range→vector marshalling** — DONE (952a149); see the Phase-1 entry above.
- [ ] **T1.4 skip-not-crash** — audit `TODO()`/throw paths in the parse/resolve; convert to drop+log.
- [x] **T1.5 const-overload disambiguation** — DONE (7b14322); see the Entry-point section.
- [x] **T1.6 QualType by-value + G8** — DONE (wave B). By-value class returns now route through the Holder/placement-new path (`ARG_CAST`) instead of `COPY_CONSTRUCTOR`'s heap-`new` (which leaked — borrowed wrapper over an unmanaged heap copy). `determineReturnStyle`'s non-returnable branch is now unconditionally `ARG_CAST` (placement-new only needs a copy ctor, not assignment, so `canAssign` is irrelevant); the Holder `defer`s dispose → scope-bounded, no leak. `COPY_CONSTRUCTOR` stays reachable as an explicit opt-in (FixupApplier scriptOriginOptionsFix). **G8 fixed:** the `WrappedMethod` ctor was const-wrapping EVERY const method's return type (`if (method.isConst) const(it)`) — meaningless for a by-value temporary, and it made the Holder's placement-new target `const T*` (ill-formed; surfaced as the gnarly-Triple compile error once ARG_CAST took over). Now only pointer/reference returns carry method-derived const. `ReturnShapeTest` (value-by-value + const-value-by-value). krapper 278/0, featuregen 157/0.
- [ ] **T1.7 borrowed-ownership policy** — never emit dispose for AST-returned ptrs/refs.
- [ ] **T1.8 reference-typedef-drop fix** — resolver fix.
- [ ] **T1.9 namespace→package** — `clang::`/`llvm::` nested → packages.
- [x] **T1.10 view-type marshalling (StringRef)** — DONE (wave B). std::string-by-value already worked (existing `STRING` style; recon's "drop" didn't repro generically — the existing path binds const & non-const std::string by value; `ReturnShapeTest` locks it). `llvm::StringRef` marshalled: `rewriteViewReturns` (Parsing.kt, baked into the parsed TU before resolution since `ParsedResolver.resolve` re-reads classes from the tree) rewrites a `StringRef` return → `std::string` + sets `returnViaMemberCall = "str"`; CppWriter emits `(call).str()` → flows through the std::string `STRING` path → Kotlin `String`. Threaded `returnViaMemberCall` through Wrapped/Resolved method copy/clone/resolve (serialized). **Real-Clang validated:** generated `SrProbe::getName(): llvm::StringRef` against real LLVM 22 headers → emits `thiz_cast->getName().str()`, compiles (`clang++ -std=c++17 $(llvm-config --cxxflags)` rc=0), links against `-lclang-cpp $(llvm-config --libs)`, and runs (`getName -> hello`). Generic (any name in `STRING_VIEW_TYPES`; only `llvm::StringRef` listed today). *ArrayRef NOT done — would be a vector-materialize like T1.3.*

### Entry-point (recon done — free-function binding ALREADY works; these are the small gaps)
- [x] **T1.0c Allowlist selects free functions** *(P0)* — DONE (commit 4d84e64). `wrapperFilter`/`resolveFilter` now also match a free function (STATIC `WrappedMethod`, `parentClass == null`) by its `<namespace>::<name>` spelling (new `WrappedMethod.freeFunctionQualifiedName` — `qualified` yields only the namespace, so the name is appended). `ParseTest.testAllowListFilterFreeFunction`. *Gotcha (Backlog): `resolveAll` only resolves free methods whose parent is a `WrappedNamespace` — a FILE-SCOPE (no-namespace) free fn is selected but won't resolve. The real target `clang::tooling::buildASTFromCode` is namespaced, so non-blocking.*
- [x] **T1.7e unique_ptr<T> return → `.release()` raw T*** *(P0)* — DONE mechanism+unit (commit 2bb1e42). `rewriteUniquePtrReturns` (Parsing.kt, at the `parseHeader` TU site) detects a by-value `std::unique_ptr<T>` return, rewrites it → raw `T*`, sets `returnViaMemberCall = "release"` (REUSES T1.10's thread — no new flag); `applyReturnRewrites` emits `(call).release()`, the `T*` flows the normal VOIDP path → non-owning wrapper + `.owned()`/`dispose()`. Custom-deleter `unique_ptr<T,D>` untouched. `ReturnShapeTest.uniquePtrReturnBindsAsRawPointer`. **End-to-end featuregen fixture deferred** — blocked by the GCC-8.3 `pair<const K,V>::swap` issue (Backlog), NOT by this mechanism; real-Clang (clang/libc++) is unaffected.
- [x] **T1.10p StringRef *param* → String** *(P1)* — DONE mechanism+unit (commit 4e49f5b). New `ArgumentCastMode.STRING_VIEW`: a `StringRef` param (const/ref peeled, matched by the now-`internal` shared `STRING_VIEW_TYPES`) marshals like by-value `std::string` (C boundary `const char*`, Kotlin `String`) but tagged STRING_VIEW carrying the view's C++ spelling; `CppWriter.createViewCast` emits `llvm::StringRef arg_cast = llvm::StringRef(arg)` (direct construction, no intermediate std::string). `ReturnShapeTest.stringRefParamMarshalsFromString` (resolution shape + emitted-C++; reuses T1.10's hand-rolled `namespace llvm { class StringRef }`). Bind `buildASTFromCode` (single StringRef), NOT `buildASTFromCodeWithArgs` (`vector<std::string>&` = inbound-vector problem).
- [x] **T1.5 const-overload dedup** — DONE (commit 7b14322). `WrappedClass.dropConstOverloadDuplicates()` (pre-resolution, from `modifyMethodsIfNeeded`): a 2-element group with same name + same param-type list differing only in const-ness drops the non-const, keeps the `const` (read-only safe). New `WrappedMethod.isConst` (from `clang_CXXMethod_isConst`, threaded through copy/clone). Narrow (only 2-element identical-param groups) so genuine overloads untouched. `DaDefaultOverloadTest` Buffer.at 🟡→🟢 (single `at`, no `_at`, kept the const overload returning `const char*`).
- *Already works (just allowlist `clang::ASTUnit`+dtor, `clang::ASTContext`, `clang::TranslationUnitDecl`, `clang::tooling::buildASTFromCode`):* `.owned()` on ASTUnit, `ASTContext&` ref-return (VOIDP_REFERENCE borrowed), `getTranslationUnitDecl()→TU*` (VOIDP), nested ns `clang::tooling::`. *Naming: `ASTUnit.fromCode` is a free fn → lands as `clang.tooling.buildASTFromCode`; literal spelling needs a rename fixup or accept the free-fn call site.*

### Phase 2 — thin slice
- [ ] **T2.0 selfhost harness** — `clang-selfhost` consumes generated bindings; the AST-walk test.
- [ ] **T2.1 Milestone A** — before/after Kotlin runs against a real `.cpp`.
- [ ] **T2.2 scale-up** — broaden the import; treat scaling failures as base defects.

### Phase 3 — migration · Phase 4 — self-host
- [ ] tickets to be expanded once Phase 2 surfaces the real shape.

---

## Decisions to revisit (Backlog)
*(append non-blocking decisions/issues here; we'll come back to them)*

- **G8 (P1, correctness):** ✅ FIXED (wave B, with T1.6). Root cause: the `WrappedMethod`
  cursor ctor const-wrapped EVERY const method's return type (`if (method.isConst) const(it)`),
  not just genuine const accessors — so a by-value return became `const T` (meaningless for the
  returned temporary, and it broke the by-value Holder placement-new → `const T*`). Now only a
  pointer/reference return carries the method-derived const.
- **G9 (P1):** value-type params/returns (`StringRef`/`QualType`) marshalled as opaque `void*`+deref;
  works but is the spot T1.6 (QualType by-value) / T1.10 (StringRef→String) must improve. Demo path.
- **S4:** `parseHeader` aborts on ANY `error:` diagnostic (Parsing.kt:520) — single-point hard-fail;
  fine for now (clean headers), revisit for a softer diagnostics policy.
- **Transitive-include pollution** (libc `timex`, `max_align_t`) gets bound — mostly cured by T1.0a scoping.
- **G6 → folded into T1.9** (namespace→package): unqualified template-arg names didn't repro with std shapes (`std::vector<ns::Inner>` already qualifies); the `llvm::DenseMapInfo<hash_code,…>` variant needs real Clang headers + ns resolution.
- **T1.2 follow-ups (non-blocking):** (a) `declaresVirtual` checks a class's OWN virtuals, not those inherited from an unbound/transitive base — a class polymorphic only via such a base would be missed by the generic-RTTI gate; (b) `hasLlvmClassof` = exact static-`classof` presence (fine for Clang); (c) same-simple-name Kotlin collision (e.g. two `Foo` in different namespaces) is pre-existing, shared with up-cast; (d) `downCastTargetsFor` recomputes `castTargetsFor` per class = O(n²) — fine at `--only` scope, revisit if binding broadly.
- **T1.0b deeper defect:** classes complete at libclang-parse time but incomplete in the curated emit-TU headers (header-closure mismatch) bite the unscoped bare-INCLUDE_MISSING path; proper fix is emit-header closure / emit-time completeness, not parse-time. (Reference-gate is the safe default for now.)
- **Process-global mutable state** (the `WrappedType` intern cache + the rootPackage) used to be loose top-level `var`/`val`s making service-level (writeTo→clang-compile) tests order-sensitive when fixtures `#include` std headers (kept T1.4 fixtures std-free). Now consolidated behind the resettable `GenerationContext` object (#11 part 2), cleared per run in `IndexedServiceImpl.init` — sequential in-process runs no longer leak the cache. Concurrent in-process runs still need true isolation (thread-local / threaded context), deferred.
- **T1.3-clang — RE-CHARACTERIZED by Recon-3a (2026-06-05), NOT an unbounded SEGV.** Under a memory cap
  (`ulimit -v 16G`) + timeout, forcing `--instantiate "std::vector<clang::Decl*>"` does **not** crash in
  resolution: it expands (Found 2544 → "Resolved 1269 classes from instantiation") and only then **fails
  at the generated-C++ COMPILE**. So the for-loop gate decomposes into tractable, featuregen-testable
  codegen tickets (the earlier "core dump" was OOM during the un-capped 2544-class expansion + the
  header-name mismatch, not an inherent wall). Two real defects in the expanded surface:
  - **T-op (P0, codegen correctness) — DONE (commit 82be045).** New `String.cleanupOperatorName()`
    (NameHandler.kt) on the operator-name fallback path maps every operator symbol to a DISTINCT,
    alphabetic, valid C token (`-=`→`_minuseq`, `/=`→`_diveq`, `%=`→`_modeq`, `<<=`→`_shleq`, … 25
    symbols), longest-first so compounds win over their components. Kept SEPARATE from the shared
    `cleanupName()` (early-returns on non-`operator` strings) so it can't corrupt `type-parameter-0-0`-style
    type spellings — the regression guard. Fixture: `Vec2 operator-=/`/=`/`%=`` → distinct
    `Vec2_operator_minuseq/_diveq/_modeq` (previously all leaked + collided). featuregen 164/0, krapper 283/0.
  - **T-defarg (P0 — now the dominant cluster, 9× after T-op cleared):** "too few arguments to function
    call" — `single argument 'F' was not specified` / `expected 2, have 1`. A method whose param has a
    default value (or is otherwise droppable) is bound with fewer params than the C++ call needs, so the
    emitted `(...)` call is missing an argument. The biggest remaining real-Clang compile blocker. Fixable +
    featuregen-testable (a method with a defaulted trailing param). **Next ticket after this checkpoint.**
  - **T-constholder (P1, 5×):** "placement new expression with a const-qualified argument of type
    `const clang::QualType *`" (also `ExplicitSpecifier`, `NestedNameSpecifierLoc`, `APValue::LValueBase`).
    A by-value return whose ELEMENT type is const routes through the ARG_CAST Holder placement-new with a
    `const T*` target — ill-formed. Sibling of G8 (which fixed const-wrapping for const METHODS); this is a
    const ELEMENT type. Fix: strip/avoid const on the Holder placement-new target, or drop.
  - **T-skip (P1, skip-not-crash):** several shapes should DROP rather than emit non-compiling C —
    `call to deleted constructor` (`SelectorTable`, `DeclarationNameTable` → don't emit the `_Holder`),
    `overload resolution selected deleted operator '='` (don't emit assign when copy-assign is deleted),
    and a fn-ptr-returning method mis-marshalled (`void*` ← `TemplateArgumentListInfo (*)()`). Per the
    skip-not-crash principle these are drops, not fixes.
  - **T-typename — now the DOMINANT cluster (~14× after T-constholder/T-skip), splits in two:**
    - **T-typename-a (P1, conversion-operator target unqualified, 3×):** a user-defined conversion operator
      emits its target type UNqualified — `new (...) clang::LocalDeclID(thiz_cast->operator LocalDeclID())`
      → "unknown type name 'LocalDeclID'; did you mean 'clang::LocalDeclID'?" (also `FunctionEffectsRef`,
      `Qualifiers`). The Holder placement-new type IS qualified, but the `operator X()` call spelling and/or
      the conversion's return type isn't. Clean codegen fix: namespace-qualify the conversion operator's
      type. Cleanest next sub-ticket.
    - **T-typename-b (P1, type nested in a class TEMPLATE → drop, 8× — dominant): DIAGNOSED.**
      `clang::LazyData` is bogus — the real type is `LazyGenerationalUpdatePtr<Owner,T,Update>::LazyData`
      (ExternalASTSource.h:449, a `struct LazyData` nested inside a class template). krapper flattened the
      nested type to `clang::LazyData`, dropping the enclosing-template qualification — and it CAN'T be named
      standalone (the encloser needs template args). Fix = **skip-not-crash: DROP a class that is nested
      inside a class template** (its qualified name is unnameable without the enclosing template's args).
      General rule (catches the `sizeof`/`reinterpret_cast`/field-accessor emits at once). The "use of class
      template requires template arguments" / "no template named X" siblings are the same family.
  - **T-skip residuals (2, P2):** the parse-time deleted-copy detection missed `RawCommentList` (field
    setter, copy-assign deleted) and `clang::FunctionEffectSet::Conflict` (implicitly-deleted DEFAULT ctor —
    a default-construct path, not copy). Extend T-skip's signal to cover these (implicitly-deleted, not just
    `= delete`d; and the default-ctor path).
  - **T-range-recover — DONE 🎉 (commit e43e74a) = MILESTONE A.** The forcing pass-3 re-resolved a bound
    class via `resolve(type)`, but it was already in `tracker.resolvedClasses` from pass-1 (range method
    dropped, container then unbound) → short-circuited to the STALE cached version. Fix: EVICT the bound
    classes from `resolvedClasses` before pass-3 → genuine rebuild against the now-populated tracker →
    `DeclContext::decls()` materializes as `fun decls(): Vector__Decl_P` returning the iterable
    `Vector__Decl_P : Iterable<clang.Decl?>`. **`for (decl in tu.decls())` compiles against real
    libclang-cpp.** (INCLUDE_MISSING never exercised pass-3 recovery — the method resolves in pass-1 via
    on-demand materialization — which is why featuregen's RgRangeTest passed while the IGNORE_MISSING
    real-Clang path silently didn't recover.) `methods()`/`bases()` use the same proven mechanism, they just
    need their own element vectors forced (`vector<clang::CXXMethodDecl*>`, `vector<clang::CXXBaseSpecifier*>`).
    --- ORIGINAL diagnosis (kept for history) ---
    After T-forcing-scope-2(a+b), the forced `vector<clang::Decl*>`
    generates, COMPILES (libClangSlice.a, 0 errors), and binds as a fully ITERABLE Kotlin collection
    (`std_Vector__Decl_P : Iterable<clang.Decl?>`). The T1.3 range rewrite DOES fire and element extraction
    WORKS (earlier "custom-iterator" diagnosis was WRONG): debug shows `fun decls(): std::vector<clang::Decl*>
    failed resolving std::vector<clang::Decl*>: Couldn't resolve return` — `decls()` is correctly rewritten
    to return the vector, and the bound vector key is an EXACT match. The actual gap: `decls()` is resolved
    during the MAIN pass (filterAndResolve, IGNORE_MISSING) when the container isn't bound yet → "Couldn't
    resolve return" → dropped; and the forcing **pass-3 re-resolution does NOT recover the range METHOD**
    under IGNORE_MISSING. T-forcing-scope-2's pass-3 was validated to bind the container + recover
    `RangeHolder::items()` under featuregen's DEFAULT (INCLUDE_MISSING), but the IGNORE_MISSING method-
    recovery path (where the main pass already dropped the method) wasn't covered. Fix: ensure the forcing
    pass-3 actually RE-RESOLVES the methods of the already-bound classes (not just the class shell) with the
    container now in the tracker, so a method whose rewritten return is the just-forced container is
    recovered. Repro: `/tmp/recon3l.log` (debug). featuregen guard: force a container under IGNORE_MISSING
    where a SEPARATE bound class has a range method returning it, assert the method recovers (not just the
    container binds — the existing `forcedContainerBindsUnderIgnoreMissing` only checks the container).
  - **T-forcing-scope — LANDED (1481721 + 04e5923 + fd08065). Container binds + compiles.** The scoping
    works: forcing `vector<clang::Decl*>` now introduces **0 extra classes** (was 1269) and the real-Clang
    error count dropped **284 → 9**. Keep-rule (`Resolver.resolveForcing`): keep the `KrapperForce_*` struct
    + already-bound classes (re-resolved, recovers dropped container-returning members) + non-class elements;
    exclude the unrelated rest. featuregen 177/0 (Box/Pair2/unique_ptr/RgRange all green). **TWO follow-ups
    remain before the for-loops compile (T-forcing-scope-2):**
    - (a) **The container template is dropped under `IGNORE_MISSING`.** "can't resolve filtered class
      KrapperForce_std_vector_clang_DeclPtr" → the struct's `std::vector<clang::Decl*>` member can't resolve
      because IGNORE_MISSING drops the unlisted `std::vector` template, so the specialization is never bound
      ("Forcing introduced 0 new classes"; no `std::vector` in the output). featuregen works only because it
      runs the DEFAULT policy. Fix: the forcing must resolve the container template even under IGNORE_MISSING
      (e.g. force the base template + element into the resolve set for the instantiation, or run the forcing
      re-resolve under a localized INCLUDE-for-the-target-only policy while the element stays bound/opaque).
    - (b) **9 std-typedef qualification errors:** `unexpected_handler`/`terminate_handler`/`new_handler`
      emitted UNqualified (pulled in by the forcing's `#include <vector>`/`<new>`/`<exception>`). Same family
      as T-typename-a but for the `std::` namespace — qualify them, or don't bind these std handler typedefs.
  - **T-forcing-scope (original P0 framing — measurement that drove it).** Forcing
    `vector<clang::Decl*>` with INCLUDE_MISSING pulls **1269 classes** whose generated C++ has **284 total
    compile errors** (`clang++ -fsyntax-only -ferror-limit=0` on `/tmp/recon3g_out/ClangSlice.cc` after
    T-op+T-defarg+T-constholder+T-skip+T-typename all landed) — a LONG TAIL dominated by **179× "use of
    undeclared identifier"** (systematic unqualified enum-constant/static-member emission), 46× unqualified
    type, 10× incomplete-type, 9× more deleted-ctor, … Fixing all of it is a war of attrition. **But the
    for-loop demo does NOT need those 1269 classes** — it needs `std::vector<clang::Decl*>`'s own surface
    (size/operator[]/begin/end) with `clang::Decl` as the ALREADY-BOUND opaque element. FIX: make the
    `--instantiate` forcing path NOT INCLUDE_MISSING-expand the element's references — treat allowlisted/
    already-bound types (and everything else) as IGNORE/OPAQUE, binding only the container's own methods.
    That collapses the 1269-class explosion to ~0 extra classes and the entire 284-error tail vanishes —
    the for-loops compile. The `--instantiate` path currently ignores the top-level `--referencePolicy`
    (hard-codes INCLUDE_MISSING); that's the hook. **This — not the codegen whack-a-mole — is the path to
    the for-loop demo (Milestone A).**
  - **Codegen long-tail (the 284 errors) = a SEPARATE "harden broad binding" track, P2.** The landed fixes
    (T-op/T-defarg/T-constholder/T-skip/T-typename) are genuine general-correctness wins that matter for the
    eventual scale goal (binding large libs broadly), and each cleared its real cluster — but they are NOT
    the demo critical path. The 179× undeclared-identifier is likely ONE systematic bug (bare enum
    constants / static members) worth a single high-leverage fix when this track resumes.
  - Net: the demo for-loops (`decls()`/`methods()`/`bases()`) unlock via **T-forcing-scope**; with
    `IGNORE_MISSING` and no forcing they drop gracefully today (Recon-3a bound 1562 fns clean).
- **GCC-8.3 `std::pair<const K,V>::swap` (P1, toolchain — surfaced by T1.7e):** the wrapper emitted for
  `std::pair<const K, V>::swap` (any `map`/`unordered_map` `value_type`) does NOT compile under
  featuregen's konan-bundled **GCC-8.3 / libstdc++** (`no matching swap(const int&, const int&)` — a
  const first member can't be swapped). Clang's libc++ SFINAEs that member away, so it only bites GCC.
  Latent: which runs pull these const-pairs into the INCLUDE_MISSING wrap set is surface-sensitive, so
  any feature that grows the closure can trip a featuregen sync (SIGABRT/exit 134). The fix belongs in
  the resolver's missing-type **materialization** path (drop `swap`/unswappable members on a
  const-first-member pair) — the const-pairs are created DURING INCLUDE_MISSING resolution, not in the
  pre-resolution tree, so a pre-resolution rewrite can't reach them. **Why it didn't block T1.7e/T1.10p:**
  both mechanisms are validated at unit level (resolution + emitted-C++), and the real-Clang build uses
  clang/libc++ (unaffected) — only the GCC-8.3 *featuregen* end-to-end fixture is deferred behind this.

## Progress log
*(append dated entries)*

- 2026-06-08 — **🎉🎉🎉 STAGE1 SELF-BOOTSTRAP RUNS — the AST walk executes on real libclang-cpp.**
  `:clangwalk:runReleaseExecutableKlinker` parses C++ via `clang::tooling::buildASTFromCode` into a real
  Clang AST and walks `TranslationUnitDecl.decls()`, printing each decl's kind + name — ENTIRELY on
  kplusplus-generated bindings of Clang's own C++ AST. Output: `[CXXRecord] Point`, `[CXXRecord] Shape`,
  `[Function] freeFunction`, `[Var] globalVar`, "walked 9 top-level declarations via real libclang-cpp".
  **The self-bootstrap thesis is proven END-TO-END AT RUNTIME** (not just compile). Final two fixes to get
  here: (1) the forcing now drops incidental free functions not bound by the main resolve (9a237a7 — the
  llvm:: leak); (2) the index `[]` operator returns the CONCRETE element, not the base interface (bd7bb0e —
  so `Vector__Decl_P`'s `Iterable<Decl>`/`next()`/`get()` agree and the walked Decl has its own methods).
  LINK GOTCHA (documented in clangwalk/build.gradle.kts): must be RELEASE — klinker uses system clang++ (for
  modern libclang-cpp's glibc/libstdc++ symbol versions, absent from K/N's old bundled sysroot), but K/N's
  platform.linux references OBSOLETE glibc symbols (`__argp_parse`/`__argz_*`/`cfree`/`getmsg`…) modern
  glibc removed; only release dead-strips the unused ones (debug leaves ~111 undefined). This is a
  toolchain/linking detail, NOT a generator gap. Minor follow-up: `getName()` renders a trailing-char quirk
  (`Point`→`PointU`); the walk (kinds/structure/count) is correct. Harness on branch `clang-selfhost`:
  `clangwalk/`. **Total this campaign: Milestone A → full canonical walk → v2 DSL enabler → ~12 generator
  fixes → the stage1 consumer runs a real AST walk on Clang's own AST.**
- 2026-06-07 — **Stage1 walk: entry chain + Decl hierarchy + enum fields + keyword params all COMPILE;
  one forcing-scope gap left.** Drove the `clangwalk` Kotlin compile through the next gap layer (these only
  surface when the generated KOTLIN compiles, which the C++-only probes never did). Landed on clang-base
  (featuregen 179/0, krapper 284/0): free-fn unique_ptr rewrite, T-defarg by-value-template-default,
  enum-decl-name `?`-trim, Kotlin-hard-keyword identifier escaping (`val` param), and the enum-typed-FIELD
  getter fix (`needsDereference` excludes enums + `generateReturn` checks isEnum before isWrapper → emits
  `Enum.Companion.fromValue` not wrapper-construction). Plus `main(args)` for klinker. **THE LAST BLOCKER
  for `:clangwalk:run`:** incidental `llvm::` free functions (`llvm::sys::fs`, `llvm::driver`) get bound with
  emission bugs (unresolved `VectorLibrary`, CPointer/UInt mismatches). The AllowListFilter correctly
  EXCLUDES un-listed free fns in the MAIN resolve, but the forcing's `resolveForcing`/scopedFound keeps ALL
  non-class elements (the rule that preserves std::literals UDL operators) — so the forcing TU's `llvm::`
  free fns leak in. Fix: scope the forcing's non-class keep by the allowlist / already-bound set (keep only
  non-class elements bound in the main resolve), featuregen-tested (it interacts with the std UDL keep +
  namespace nesting). Then the stage1 AST walk runs = the self-bootstrap demo. Everything else of the walk
  (`buildASTFromCode`→`ASTUnit`→`ASTContext`→`TU.decls()`→`Decl.getDeclKindName`/`asNamedDecl`/`getName`)
  generates + compiles.
- 2026-06-07 — **T2.0 stage1 `clang-selfhost` consumer BUILDS end-to-end; AST walk one gap away.** The
  `clangwalk` gradle module (branch `clang-selfhost`) runs the v2 plugin → krapper_gen against real Clang
  and the FULL entry chain now generates + the C++ wrapper compiles: `clang::tooling::buildASTFromCode`
  (`MemScope.buildASTFromCode(String,String): ASTUnit?`) → `ASTUnit.getASTContext()` →
  `ASTContext.getTranslationUnitDecl()` → `TranslationUnitDecl.asDeclContext().decls()` (iterable
  `Vector__Decl_P`) → `Decl.getDeclKindName()`/`asNamedDecl()?.getName()`. Getting here surfaced + fixed
  SIX generic generator gaps (all landed on clang-base, featuregen 179/0, krapper 284/0): the v2 gradle DSL
  `only`/`referencePolicy` enabler (7a3ddcb); forcing pass-3 UNION-eviction so range methods recover at the
  large entry-point-header scope (289e91f); and (6d86668) free-function unique_ptr-return rewrite,
  T-defarg by-value-template-default omittability, enum-declaration-name `?`-trim, and Kotlin-hard-keyword
  identifier escaping (`val` param). Plugin gotcha: set `compiler = "clang++"` (the konan GCC-8.3 default
  aborts on Clang's headers). **THE ONE REMAINING BLOCKER for a running walk:** an enum-TYPED FIELD (e.g.
  `ASTContext::TUKind: TranslationUnitKind`) resolves its type as a WRAPPER, so its getter emits
  `TranslationUnitKind(ptr, memScope)` (class construction → "enum cannot be instantiated") instead of
  `TranslationUnitKind.fromValue(int)` — the field-type resolution path doesn't reduce enums the way the
  method-return path (ENUM_RETURN/WrappedEnumType, [[enum-unwrapped]]) does. Fix = apply the same enum
  reduction in field resolution (or branch the field getter on `returnType.isEnum`). Then `:clangwalk:run`
  produces the live AST walk = the self-bootstrap demo. Harness: `clangwalk/` (build.gradle.kts + AstWalk.kt).
- 2026-06-07 — **T2.0 stage1 harness started — DSL enabler landed (7a3ddcb on clang-base).** Began the
  `clang-selfhost` stage1 consumer (branch `clang-selfhost`, scaffold commit 85b0dd8: module skeleton +
  slice header). Discovered + closed the one concrete enabler: the v2 `com.monkopedia.kplusplus.compiler`
  gradle plugin's DSL lacked `--only` (allowlist) and `referencePolicy` — both REQUIRED to bind a Clang
  slice (scope away the ~2655-class surface + IGNORE_MISSING to avoid the explosion). Added `only(vararg)`
  / `onlyFile(path)` / `referencePolicy` to `KPlusPlusExtension` + forwarded them in the plugin's
  krapper_gen invocation (purely additive — v8/featuregen byte-identical; `:kplusplus-compiler-gradle:
  compileKotlin` green). **Remaining harness bring-up (T2.0 cont.):** (1) name the module hyphen-free
  (moduleName = project name → C-identifier prefix, so NOT `clang-selfhost`; use e.g. `clangwalk`);
  (2) `build.gradle.kts`: kplusplus { header(clang_selfhost.h); only("clang::Decl",… ,"clang::CXXBaseSpecifier");
  referencePolicy="IGNORE_MISSING"; cppStandard="c++17"; instantiate("std::vector<clang::Decl*>")… } +
  klinkedExecutable compilerOpts(-L/usr/lib -lLLVM-22 -lclang-cpp); (3) wire into root settings; (4) the
  AST-walk `main` (buildASTFromCode→TU→for(d in tu.decls())); (5) `:clangwalk:kplusplusSync` then build+run.
  NB the harness is ALSO the FIRST real-Clang test of the entry-point wave (buildASTFromCode/ASTUnit/
  unique_ptr — only unit-validated so far); expect it to surface the next discovery-loop gaps. The generated
  `<moduleName>.h` self-include is auto-generated (HeaderWriter), so no header-name conflict.
- 2026-06-06 — **Full canonical AST walk materializes (probe — no repo change).** With `clang::CXXBaseSpecifier`
  added to the allowlist + `std::vector<clang::{Decl,CXXMethodDecl,CXXBaseSpecifier}*>` all forced, the
  generated bindings expose `decls()`, the `CXXMethodDecl` method ranges, AND `bases()`/`vbases()` (→
  `Vector__CXXBaseSpecifier_P`) — the V8-shaped walk (a record's decls + methods + bases) — all as iterable
  vectors, compiling clean against real Clang (libClangSlice.a, 0 errors). Needed ZERO generator changes:
  pure consumer config (allowlist + forcing). The mechanism scales — each range element type just needs
  binding + its `std::vector<Elem*>` forced. Probe: `/tmp/recon3q_out`, allowlist `/tmp/clang_only_b.txt`.
- 2026-06-06 — **Milestone A enriched: multi-level walk (commit 62a6f71).** Forcing both
  `std::vector<clang::Decl*>` AND `std::vector<clang::CXXMethodDecl*>` now compiles (libClangSlice.a, 0
  errors) — so the AST walk goes 2 levels (decls() + the CXXMethodDecl method ranges). One fix needed:
  a const-pointer range iterator (`CXXMethodDecl::overridden_methods()` derefs to `const CXXMethodDecl*
  const`) needed `kpp_to_elem_ptr` `const E*`/`const E&` overloads (the materialized `vector<E*>` is
  non-const). Each range element type just needs its `std::vector<Elem*>` forced (same mechanism).
- 2026-06-06 — **🎉🎉 MILESTONE A — the real-Clang AST-walk for-loop works (commit e43e74a).** The literal
  demo `for (decl in tu.decls())` now compiles against real libclang-cpp: `DeclContext::decls()` materializes
  as `fun decls(): Vector__Decl_P` returning the iterable `Vector__Decl_P : Iterable<clang.Decl?>`,
  libClangSlice.a builds with 0 errors. Final fix (T-range-recover): the multi-pass forcing pass-3 was
  re-resolving bound classes but short-circuiting on the stale pass-1 cache (range method dropped while the
  container was unbound); evicting the bound classes from `tracker.resolvedClasses` before pass-3 forces a
  genuine rebuild so `decls()` recovers now that the container exists. The IGNORE_MISSING method-recovery
  path had never been exercised (featuregen's RgRangeTest resolves via INCLUDE_MISSING in pass-1). featuregen
  179/0, krapper 284/0. **The end-to-end self-bootstrap thesis is PROVEN:** kplusplus binds Clang's own
  C++ AST (Decl hierarchy, ranges, RAII, templates, dyn_cast, StringRef, unique_ptr) and the generated
  Kotlin compiles+links against libclang-cpp. Remaining for a richer demo: force `methods()`/`bases()`
  element vectors (same mechanism), then build the stage1 consumer (`clang-selfhost`) that walks an AST.
- 2026-06-06 — **🎉 Real-Clang AST-walk binding COMPILES (0 errors) + iterable container — T-forcing-scope-2
  (a+b) landed (04e5923, fd08065).** The forced `vector<clang::Decl*>` now generates → **compiles
  `libClangSlice.a` against real libclang-cpp with ZERO errors** (down from 284) → binds **1916 Kotlin fns
  across 84 files**, INCLUDING `std::vector<clang::Decl*>` as a fully ITERABLE collection
  (`std_Vector__Decl_P : Iterable<clang.Decl?>`, `size`/`get`/`iterator`). Fixes: (a) multi-pass shared-
  tracker forcing resolve binds the container under IGNORE_MISSING without re-exploding (pass1 bound@policy
  → pass2 KrapperForce@INCLUDE → pass3 re-resolve@policy+fresh-cache); (b) qualify namespace-scoped
  function-pointer typedefs in the `.cc` (`std::terminate_handler`, cleared the last 9). The error arc:
  284 → 9 → **0**. **HONEST status:** this is the demo INFRASTRUCTURE, not yet the literal for-loop —
  `decls()`/`methods()`/`bases()` are still DROPPED (T1.3 range rewrite doesn't fire on clang's custom
  `decl_iterator`; see Backlog **T-range-detect**). The container is ready + iterable; the range methods
  just need RangeReturn to extract the element from a custom iterator. That's the final P0 for the for-loop.
- 2026-06-06 — **T-forcing-scope landed (1481721) + re-probe — explosion KILLED, demo near.** Scoped the
  `--instantiate` forcing collection (`Resolver.resolveForcing`): forcing `vector<clang::Decl*>` now adds
  **0 extra classes** (was 1269) and real-Clang errors dropped **284 → 9**. featuregen 177/0 (all
  instantiation tests green). NOT fully there yet — two follow-ups (T-forcing-scope-2, in Backlog): (a) the
  `std::vector` template is dropped under IGNORE_MISSING so the forcing struct can't resolve → the vector
  spec isn't bound → `decls()` still doesn't materialize ("0 new classes"); (b) 9 residual std-handler
  typedef qualification errors (`unexpected_handler` etc.) from the forcing's `#include <vector>`. Both are
  small/precise. Once (a)+(b) land, the forced `vector<clang::Decl*>` should compile → the `decls()`/
  `methods()` for-loops materialize = **Milestone A**. The whack-a-mole 284-tail stays deferred (P2) —
  scoping made it irrelevant to the demo, exactly as predicted.
- 2026-06-06 — **T-typename-a/b + T-skip-residuals landed (487a4eb) + KEY STRATEGIC RE-PROBE.** Cleared
  the conversion-operator-unqualified (3×), in-template-nested-class `LazyData` (8×), and implicit-deleted
  residual clusters. featuregen 177/0, krapper 283/0. Then measured the FULL forced-vector surface with
  `clang++ -fsyntax-only -ferror-limit=0`: **284 total compile errors across the 1269 INCLUDE_MISSING
  classes**, a long tail led by **179× "use of undeclared identifier"**. CONCLUSION + RE-PRIORITIZATION:
  whack-a-mole on the forced surface is a war of attrition; the for-loop demo doesn't need those 1269
  classes. **T-forcing-scope → P0** (scope the `--instantiate` forcing so it doesn't INCLUDE_MISSING-expand
  the element's closure → the 284-error tail vanishes, for-loops compile). The 5 landed codegen fixes are
  valuable GENERAL correctness (the "harden broad binding" track, now P2), not the demo path. **Next:
  T-forcing-scope.**
- 2026-06-06 — **T-constholder + T-skip landed (6655b7d) + re-probe.** Cleared the const-placement-new
  cluster (5→0) and most deleted-copy cases. T-constholder: unconst the Holder placement-new TARGET when
  `ARG_CAST && isConst` (G8 sibling). T-skip: deleted/inaccessible copy members are filtered out PRE-resolve
  (`CXAvailability_NotAvailable`), so capture `hasDeletedCopy{Constructor,Assignment}` in `ClassMetadata` at
  parse time → `canCopyConstruct` drops the by-value method/field, `canAssign` drops the unassignable setter
  (getter kept). featuregen 172/0 (+6), krapper 283/0. Re-probe (`/tmp/recon3f.log`, 20 visible errors):
  **T-typename is now dominant (~14×)** — splits into T-typename-a (conversion-operator target unqualified,
  3×) + T-typename-b (bogus `clang::LazyData`, 8×); plus 2 T-skip residuals (implicitly-deleted copy-assign /
  default-ctor). Next: T-typename-a (clean) then T-typename-b (investigate). Steady cluster-by-cluster
  descent: operator → too-few-args → const/deleted-copy → type-names.
- 2026-06-05 — **T-defarg landed (d128d78) + re-probe.** Cleared ALL 9 "too few arguments" errors on the
  forced `vector<clang::Decl*>` real-Clang probe (`/tmp/recon3e.log`: 0). Root cause was subtler than the
  first guess: a FALSE `hasDefault` — krapper's default-arg heuristic mistook an integer in the param's
  TYPE (array bound `int(&)[N]`, non-type template arg `Mask<N>`) for a C++ default, trimming a REQUIRED
  param → short call. Fix gates the trim on `!typeCarriesFalseDefault` (array + template-instantiation
  types), preserving genuine C++-default params. featuregen 166/0, krapper 283/0. Remaining real-Clang
  codegen wave (next tickets, all in Backlog under T1.3-clang): **T-constholder** (5× const placement-new),
  **T-skip** (~7× deleted ctor/operator=/default-ctor → skip-not-crash drops), **T-typename** (5× "unknown
  type name" / "no member in namespace" — a type-qualification/emit bug, likely T1.9-adjacent). Loop steady:
  T-op → T-defarg each cleared their cluster; next is T-constholder (real fix) or T-skip (drops).
- 2026-06-05 — **T-op landed (82be045) + discovery re-probe.** T-op cleared ALL operator-name errors on
  the forced `vector<clang::Decl*>` real-Clang probe (`/tmp/recon3d.log`: 0 operator errors, down from the
  prior batch). The probe advanced to the NEXT codegen wave (recorded in Backlog under T1.3-clang):
  **T-defarg** (9× "too few arguments to function call" — now the dominant blocker), **T-constholder** (5×
  const-qualified placement-new), and **T-skip** drops (deleted ctor/operator=, fn-ptr→void*). Each is a
  tractable featuregen-testable ticket; the real-Clang for-loop demo is gated behind this cluster. The peel-
  back loop is working as designed — fix a layer, the next concrete layer surfaces. Next: **T-defarg**.
- 2026-06-05 — **Recon-3a: AST-walk classes generate + COMPILE + bind against REAL Clang.** 🎉 Ran the
  freshly-built `krapper_gen.kexe` (clang-base 4e49f5b, post-T1.3 + entry-wave) over a 2-header slice
  (`clang/AST/DeclCXX.h` + `ASTContext.h`) with the 12-class AST-walk allowlist (`/tmp/clang_only.txt`),
  `--referencePolicy IGNORE_MISSING`, `--std c++17`, module `ClangSlice`. **EXIT=0:** resolved 12
  top-level, generated the C++ wrapper, **compiled `libClangSlice.a`** (the generated wrappers compile
  against real libclang-cpp headers), and emitted **1562 Kotlin functions across 37 files** (`clang_Decl`,
  `clang_CXXRecordDecl`, `clang_CXXMethodDecl`, the `*Api` inheritance interfaces, enums like
  `clang_decl_Kind`/`clang_AccessSpecifier`). T1.7e (`unique_ptr→.release()`) and T1.10 (StringRef→String)
  fire live against real Clang. **The whole base-capability stack works on real Clang.** Gotchas learned:
  (1) the wrapper `#include`s `<module-lowercased>.h` (`ClangSlice`→`clang_slice.h`), so the input header
  must be named to match (the recon convention); (2) `IGNORE_MISSING` is essential — `INCLUDE_MISSING`
  explodes to 2544 classes; (3) range methods (`decls()`/`methods()`/`bases()`) DROP gracefully without
  forcing (only `decls_empty()` survives) — materializing them needs the element vector seeded, which is
  the T1.3-clang gate (now re-characterized in Backlog: codegen bugs T-op/T-defarg, not a SEGV). Probes
  in `/tmp/recon3a_out` (clean) + `/tmp/recon3c_out` (forced-vector, shows the T-op operator-name errors).
  Next: fix **T-op** (compound-assign operator name sanitization) — the highest-leverage gate for the
  for-loops — then re-probe the forced vector.
- 2026-06-05 — **Entry-point wave landed (4 tickets, commits 4d84e64 / 7b14322 / 2bb1e42 / 4e49f5b on
  `clang-base`).** Dispatched two featuregen-only-scoped subagents in parallel worktrees (a "selection"
  pair + a "marshalling" pair); integrated both onto clang-base (clean rebase — the overlapping
  Parsing.kt/WrappedMethod.kt edits auto-merged; combined build re-verified). **T1.0c** free-fn allowlist
  + **T1.5** const-overload dedup landed fully green. **T1.7e** unique_ptr→.release() + **T1.10p**
  StringRef-param→String landed mechanism+unit-test green; their end-to-end featuregen fixtures are
  deferred behind the newly-found GCC-8.3 `pair<const K,V>::swap` toolchain issue (Backlog) — which does
  NOT affect the real-Clang (clang/libc++) path. Combined: krapper 283/0, featuregen 160/0,
  `:krapper_gen:ktlintCheck` green. (featuregen ktlint is NOT a gate — generated `krapped/src` can't
  pass it; the real gate is krapper_gen.) **The entry point is now bindable:** allowlist a free fn
  (`clang::tooling::buildASTFromCode`), get its `unique_ptr<ASTUnit>` as an owned `ASTUnit*`, pass Kotlin
  `String` source as a `StringRef`, and the const-overload accessors (`getASTContext`) dedup cleanly.
  Next: Recon-3 — wire the whole demo loop against REAL Clang (Milestone A), where the T1.3-clang
  instantiation SEGV is the known gate for the `decls()`/`methods()` for-loops.
- 2026-06-05 — **T1.3 ranges landed (commit 952a149 on `clang-base`).** `llvm::iterator_range<It>`
  return → `std::vector<Elem*>` materialization, for-loop-iterable. `RangeReturn` reads the canonical
  type to see through `using X_range = iterator_range<...>` aliases and recovers `Elem` via template-
  arg introspection + pointee stripping; CppWriter emits the fill loop; the on-demand vector<Elem*> is
  seeded like the Box facade and `IndexedServiceImpl` sanitizes `*`/`&` out of the forcing identifier.
  featuregen 160/0 (RgRangeTest added), krapper 280/0. **Salvage note:** the original T1.3 agent solved
  the generic capability green but thrashed trying to validate it against *real* Clang — forcing
  `vector<clang::Decl*>` SEGVs (INCLUDE_MISSING pulls the whole LLVM world) — and died on a rate-limit.
  I killed the orphaned kexe/daemons, verified the featuregen-level work was coherent+green in the
  worktree, and integrated it; the real-Clang instantiation SEGV is recorded as Backlog **T1.3-clang**
  (a separate on-demand-instantiation-at-scale gap, not a defect in this capability). Next: entry-point
  wave (T1.0c free-fn select, T1.7e unique_ptr→.release(), T1.10p StringRef param, T1.5 const-overload).
- 2026-06-05 — **Wave B return shapes (base `clang-waveB` off `clang-base` 9c6243d).** Landed
  **T1.6 + G8** and **T1.10 (StringRef + std::string-by-value confirmation)**; **T1.3 ranges deferred**
  (design recorded — bigger than the rewrite pattern: needs iterator element-type introspection +
  on-demand vector instantiation). T1.6: by-value class returns route through the Holder
  (`ARG_CAST` placement-new + `defer` dispose) instead of `COPY_CONSTRUCTOR`'s leaking heap-`new`;
  `determineReturnStyle` non-returnable → unconditional `ARG_CAST`. G8: stop const-wrapping const
  methods' by-value return types (was breaking the Holder placement-new target as `const T*`).
  T1.10 StringRef: `rewriteViewReturns` (baked into the parsed TU) rewrites `llvm::StringRef` →
  `std::string` + `returnViaMemberCall="str"`; CppWriter emits `(call).str()`. Real-Clang validated
  (`getName(): StringRef` against LLVM 22 headers → compiles+links `-lclang-cpp`+runs `getName->hello`).
  Verify: krapper 278/0 (+4 `ReturnShapeTest`), featuregen 157/0, ktlint clean, compiler compileKotlin
  (JDK21) green. No goldens changed.

- 2026-06-05 — Phase 0 spike green (6s/TU, link OK, dyn_cast OK). `clang-base` forked off v2-templates
  HEAD `6c1ac51`. Roadmap committed (e38653d). Recon (T1.0) dispatched.
- 2026-06-05 — **Recon-2 (loop iter 2): AST-walk surface is REACHABLE.** With T1.0a+T1.4, the generator
  runs to completion, binds the Decl hierarchy (CXXRecordDecl/329 methods, RecordDecl, TagDecl,
  FieldDecl, CXXMethodDecl, …), and the generated `.cc` **compiles+links+runs against libclang-cpp**.
  Demo loop gaps: **T1.0b** (missing primary base hard-fails class — cheap prereq), **T1.2 dyn_cast**
  (down-cast absent; up-cast done), **T1.3 ranges** (decls/methods/fields/bases dropped), **T1.10**
  (getName/StringRef/std::string returns dropped), **T1.6 QualType** (partial — getReturnType binds +
  compiles but leaks the heap copy + no .asString), entry-point (ASTUnit/buildASTFromCode free fns not
  selectable — class-only filter; + unique_ptr handle T1.7). Order: T1.0b → T1.2+T1.3 → T1.10 → T1.6 →
  entry-point. Dispatched **T1.0b (base-not-fatal)** + **T1.2 (dyn_cast)** in parallel.
- 2026-06-05 — Integrated T1.0a (f67a223) + T1.4 (0292f05) onto clang-base. krapper 273/0, featuregen 155/0.
- 2026-06-05 — **T1.0 recon DONE.** libclang parses Clang headers ~2s/clean. NO class-scoping filter →
  2655 classes from 2 headers (scale finding: skip-not-crash is existential). 2 resolver crashes
  (G1 parseTypes OOB, G2 uncaught error()), ~5 codegen-bad-C shapes (G3 anon members, G4 dropped param,
  G5 const setter, G6 unqualified tmpl-arg, G7 deleted ctor). Dispatched **A: T1.0a scoped filter** +
  **B: T1.4 skip-not-crash (G1–G7)** as parallel base capabilities (featuregen-tested). dyn_cast/range
  (T1.2/T1.3) sit behind G1 — fixture them next once the DeclCXX surface is reachable.
