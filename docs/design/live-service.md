# The live krapper service and the FIR client (#186, brick 3)

**Status:** design (no production change). Written 2026-07-29 against `main` @ `34fb7a3`
(v0.3.4 self-hosted). Bricks 1 (#184, `b787196`) and 2 (#185, `e75707b`) are done.
**Settled inputs (owner, 2026-07-28, not re-litigated here):** the FIR plugin becomes a live
client; lifetime is Gradle-daemon-scoped with an idle timeout; featuregen SYNC byte-identity
stays the hard gate; no session watchdog.

---

## 0. TL;DR

Three findings from reading the code reshape what this issue can deliver. None of them
contradict the ruling; all of them change the *mechanism*.

1. **There is no live Clang AST today, anywhere.** `CppParser.parse` wraps the entire parse in
   `memScoped` (`krapper/src/nativeMain/kotlin/com/monkopedia/krapper/parser/CppParser.kt:76-83`),
   so the `ASTUnit` — and with it `ASTContext` and `Sema` — is destroyed the moment the parse
   returns; only the pure-Kotlin `WrappedTU` survives. The bound surface
   (`krapper/include/clang_slice.h:9-15`) contains **zero Sema** symbols. "A persistent Clang
   instance HAS Sema" is therefore not a property persistence *unlocks*; it is a **new
   capability** persistence makes *affordable*. It needs its own probe before anyone plans on it.
2. **A compiler cannot materialize a binding mid-compilation, ever.** Generated bindings enter
   the build as (a) a source dir added to the compilation
   (`KPlusPlusCompilerGradlePlugin.kt:819`) and (b) a cinterop klib built from the generated
   `.def` + `lib<module>.a` (`:803-815`). Both are fixed before `compileKotlinNative` starts. A
   FIR-synthesized class would have no backing native symbol and would fail to link. So "lazy,
   on-demand binding driven by the compiler" is real, but its unit of time is **the next sync**,
   not the current compilation. What the compiler can do live is **ask** and **request**.
3. **Everything the FIR plugin currently needs is immutable for the duration of a sync.** The
   mangling it re-derives (`CppVectorMapping.kt:248-268`), the Kotlin→C++ type table (`:176-182`),
   the container list (`:53-107`), the drop reasons — all of it is a pure function of the last
   krapper run. That means the live client's payload can and should be a **snapshot fetched once
   per compilation**, not a per-call round trip. This is not a retreat from "live client": the
   snapshot is *krapper's own answer* replacing a hand-maintained re-derivation, which is the
   substance of the ruling. It is also the only form that survives the Gradle build-cache and
   IDE constraints (§6, §5 D4).

The design that follows is therefore: **a canonical binding index emitted by krapper → consumed
by the FIR plugin as a compilation-start snapshot → served either from disk (default, always
correct) or from a live, persistent krapper (faster, richer, strictly optional)**. Lazy binding
and Sema are separate, probe-gated bricks stacked on top.

**If only one thing ships from this issue, ship §4.1 + §4.3 (the index and its consumer).** It
removes a live duplication bug, needs no persistence, needs no new transport, and is verifiable
by a single equality test.

---

## 1. The ground truth (what exists, with citations)

### 1.1 The service, as of #185

`KrapperService` / `IndexedService` (`krapper/src/commonMain/kotlin/KrapperService.kt:54-100`)
are compiled into *both* sides — the tool and the Gradle plugin compile the same source
(`compiler/gradle/build.gradle.kts:76-84`). The plugin drives one process per sync over the
subprocess stdio pipe (`KrapperSession.run`, `compiler/gradle/.../KrapperSession.kt:135-169`),
in the fixed order `setLogger → setConfig → index → filterAndResolve / requestInstantiation →
applyFixups → writeTo` (`:215-226`). The tool hosts the service in `-s` mode and exits when the
pipe closes (`App.kt:348-371`, `connection.onClose { exitProcess(0) }`).

`KrapperSession`'s own KDoc already names this issue: *"The session is per-run… Keeping it alive
across builds (and adding query methods to interrogate a live model) is #186"* (`:130-131`).

### 1.2 The FIR plugin, as of today

The plugin jar has **zero runtime dependencies** —
`compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")` and nothing else
(`compiler/plugin/build.gradle.kts:62-64`). Three extension points are registered
(`KPlusPlusComponentRegistrar.kt:21-34`):

| extension | phase | what it does | can it do I/O? |
|---|---|---|---|
| `CppVectorCallRefinement : FirFunctionCallRefinementExtension` | **candidate resolution** (body resolve), per candidate | refines `cppVector<Int>()`'s type to `std.Vector__Int` by looking the ClassId up in the symbol provider (`CppVectorCallRefinement.kt:32-34`) | **No.** Hot path, re-entered per candidate, and its result *is* compiled output. |
| `CppVectorCallChecker` in `KPlusPlusFirCheckersComponent : FirAdditionalCheckersExtension` | **checkers**, once per call expression | reports `SYNC_REQUIRED` and appends the spec to `krapped/requested.txt` (`CppVectorCallChecker.kt:44-53`, `RequestedManifest.kt:40-62`) | Side effects already live here — but the IDE runs checkers for on-the-fly highlighting, so **blocking** I/O here is an editor freeze. |
| `CppVectorIrExtension : IrGenerationExtension` | IR | lowers the facade call to the binding's companion factory, reading the target off the already-refined type (`CppVectorIrExtension.kt:64-81`) | Too late to matter. |

Per-compilation configuration arrives as `SubpluginOption`s (`requestManifestPath`,
`rootPackage` — `KPlusPlusCommandLineProcessor.kt:44-68`) and is stashed into plugin-static state
in `registerExtensions` (`KPlusPlusComponentRegistrar.kt:26-31`). **That method is the one place
per compilation where a blocking fetch is architecturally safe**, and it is already the
established home for exactly this kind of setup. This is the answer to "when in the FIR pipeline
can it query" (§4.3).

### 1.3 What `CppVectorMapping` re-derives statically (the present-day defect)

Grounding the query surface in what the file actually does:

- **Name mangling.** `bindingClassId` + `mangleTemplateArg` (`CppVectorMapping.kt:248-268`)
  reimplement `KotlinWriter`'s class-name mangling: last `::` segment, capitalize first char,
  replace `` ` ` <,>* `` with `_`. The package rule (`bindingPackage ?: "$rootPackage.std" ?: "std"`,
  `:253`) reimplements the generator's package rooting. **Nothing checks these two implementations
  against each other.** A generator-side change silently degrades to "binding not found" →
  spurious `SYNC_REQUIRED` on a binding that exists. The file's own header comment concedes this:
  *"Binding-name mangling mirrors what krapper's KotlinWriter emits"* (`:26-31`).
- **Kotlin→C++ type table.** `cppElementType` (`:176-182`) knows exactly four types: `Int`, `Long`,
  `Double`, `Float`. `cppVector<Boolean>()` / `<Short>` / `<Byte>` / `<Char>` are reported as
  *"type arguments are not supported C++ element types"* (`CppVectorCallChecker.kt:31-38`) even
  where krapper's own `TypeMapping` handles them. The KDoc says a later step should derive it from
  the generator (`:31-33`). This is that step.
- **The container list.** Seven hardcoded `Container`s (`:53-107`), plus the generic
  `@krapper.CppTemplate`-derived path (`:139-159`) — the annotation krapper itself emits
  (`IndexedServiceImpl.kt:365-385`).
- **"Is it there?"** `symbolProvider.getClassLikeSymbolByClassId(...) != null`
  (`CppVectorCallChecker.kt:40-43`). This is **correct and must stay** — the symbol provider is
  ground truth for *this* compilation, and nothing krapper says can override it.

So the real, present-day query surface is: *give me the ClassId for this spec*, *give me the type
map*, *tell me why this spec has no binding*. All three are pure functions of the last sync.

### 1.4 Krapper is single-tenant by construction

`IndexedServiceImpl.init` resets three **process-global** objects before any resolution:
`DropLedger.reset()`, `GenerationContext.reset(config.rootPackage, config.noRtti)`,
`BaseBindProfiler.reset()` (`IndexedServiceImpl.kt:106-110`). `KrapperServiceImpl.index` sets two
more process-global vars, `cppParseIncludeDirs` and `cppModelDumpDir`
(`KrapperServiceImpl.kt:45-46`, declared at `Parsing.kt:97,103`). A third,
`cppBaseModelTu` (`Parsing.kt:134`), carries the base tree into every forcing parse.

`GenerationContext`'s KDoc states the assumption in so many words: *"the generator runs one
request at a time under `runBlocking`"* (`krapper_model/.../GenerationContext.kt:32-34`);
`DropLedger` repeats it (`DropLedger.kt:67`). A persistent process serving two modules — or one
module under a parallel Gradle build — violates this **today**. De-globalizing (or explicitly
serializing) is a prerequisite for persistence, not a nice-to-have.

### 1.5 Output is wholesale, and ordering is insertion-ordered

`KotlinWriter.generate` **deletes every file in the output dir** before writing
(`KotlinWriter.kt:150-156`), then writes one file per class (`:165-171`). The wrapper is one
`.cc` compiled to one `lib<module>.a` plus one `.def` (`IndexedServiceImpl.kt:276-323`).
Ordering throughout `KotlinWriter` is `LinkedHashMap`/`LinkedHashSet` insertion order
(`:241, :954, :1094, :1213, :1310`), i.e. **resolve order is emission order**.

Byte-identity survives today because the *request set is canonicalized before the run*:
`requested = (manifestSpecs + ext.instantiations).distinct().sorted()`
(`KPlusPlusCompilerGradlePlugin.kt:303`, commented "the deterministic instantiation order"), and
each forcing parse is reordered into the base tree's first-seen order (`reorderToFirstSeen`,
`Parsing.kt:184-203`). **Preserving exactly these two mechanisms is the whole determinism story
for a lazy binder** (§5).

Consequence for "granularity": whatever krapper's internal laziness, the *downstream* unit of
materialization is the module's entire `krapped/` tree plus a full wrapper recompile plus a
cinterop re-run plus a recompile of every generated Kotlin file. Per-spec incremental
materialization does not exist and is not worth inventing.

### 1.6 The recorded finding that constrains lazy binding

`docs/design/broad-forcing-resolve.md:210-224` evaluates **exactly** the "bound the closure"
idea and rates it **"HIGH / disqualifying as the primary fix"** — not on performance grounds but
because it *changes what gets bound*, and featuregen's cross-instantiation story depends on
`INCLUDE_MISSING`'s transitive semantics. That verdict has not been superseded. Lazy binding is
that option under a new name, so it **cannot be the default policy**; it must be an opt-in
alongside `INCLUDE_MISSING` / `IGNORE_MISSING`, and the byte-identity gate applies unchanged to
the existing policies (§4.6, §5).

---

## 2. What this issue can and cannot deliver

| The framing says | Reality, grounded | What we do instead |
|---|---|---|
| Lazy binding removes the closure explosion | True for the broad-force scenario; but it changes bound output, which `broad-forcing-resolve.md:216` disqualifies as a *default* | An opt-in `ON_DEMAND` policy, measured against #10's residual ~185, with the eager policies byte-identical (§4.6) |
| A persistent Clang instance HAS Sema | Not today: the `ASTUnit` dies inside `memScoped` (`CppParser.kt:76-83`) and no Sema symbol is bound (`clang_slice.h`) | Probe P1/P2 first. The likely mechanism is a **PCH + `-fsyntax-only` probe TU**, not a retained Sema (§4.5) |
| The FIR plugin resolves bindings on demand during compilation | It can *ask* and *record*; it cannot *materialize* — the cinterop klib and source set are frozen before the compile (`KPlusPlusCompilerGradlePlugin.kt:803-819`) | Snapshot-at-compilation-start + better diagnostics + request-for-next-sync (§4.3) |
| Instantiation manifests stop being necessary | `requested.txt` is the request ledger and is a **declared task input** (`:96-98`); removing it removes Gradle's ability to know the sync is stale | Keep the manifest; make the service write it, and enrich it |

---

## 3. Design overview

Five layers. Each is independently landable and independently valuable; each falls back to
today's behaviour when the layer above is absent.

```
  L4  ON_DEMAND binding policy            (opt-in; probe-gated; #10)
  L3  wellFormed() oracle                 (probe-gated; narrows #175 keep-on-doubt)
  L2  persistent krapper + parse cache    (gated on #194; pure speed)
  L1  live query channel                  (optional; diagnostics only)
  L0  the BINDING INDEX                   (the artifact everything else serves)
```

L0 is the contract. L1–L4 are optimisations and capabilities *over the same contract*.

---

## 4. The design

### 4.1 L0 — the binding index (`krapped/binding-index.json`)

krapper's `writeTo` gains one more emitted artifact, alongside the `.h`/`.cc`/`.def`/`src/`
(`IndexedServiceImpl.kt:274-333`). It is deterministic: every collection sorted by a stable key,
no absolute host paths (same rule the forcing headers already follow — `:258-265`, #86).

```kotlin
@Serializable
data class BindingIndex(
    /** Bumped on any incompatible shape change; a consumer that doesn't know it falls back. */
    val schemaVersion: Int,
    /** Identity of the run that produced this: tool build id + config digest + header digests. */
    val runId: String,
    val rootPackage: String?,
    /** Every emitted binding, sorted by `spec`. */
    val bindings: List<BindingRef>,
    /** Every `@CppTemplate` facade emitted, sorted by `base`. */
    val templates: List<TemplateFacadeRef>,
    /** Kotlin fq-name -> C++ spelling, krapper's own table. Sorted by key. */
    val kotlinTypeMap: Map<String, String>,
    /** The drop ledger, keyed for lookup by spec/symbol. Sorted. */
    val drops: List<Diagnostic>
)

@Serializable
data class BindingRef(
    /** Canonical C++ spelling, exactly as krapper spells it: "std::vector<int>". */
    val spec: String,
    /** `ClassId.asString()`, e.g. "std/Vector__Int" — the generator's own answer. */
    val classId: String,
    val pkg: String,
    val simpleName: String,
    /** Companion default-ctor factory name, when one was emitted (see CppVectorIrExtension:76). */
    val factory: String?,
    /** `<name>_Holder` fallback factory, when one was emitted (CppVectorIrExtension:78-80). */
    val holderFactory: String?,
    val cppBase: String?,        // "std::vector" for a specialization, else null
    val templateArgs: List<String>
)

@Serializable
data class TemplateFacadeRef(
    val base: String, val prefix: String, val pkg: String, val arity: Int
)
```

`BindingRef.classId` / `pkg` / `simpleName` / `factory` are **read off the emitted model**, not
recomputed — they come from the same `ResolvedClass.type.kotlinType.fullyQualified` the writer
used (`KotlinWriter.kt:166-167`). That is the entire point: one source of truth for mangling.

`Diagnostic` is reused verbatim (`krapper/src/commonMain/kotlin/Diagnostic.kt:70-77`), so drops
already carry severity, symbol, C++ `file:line:col`, phase and consumer hint.

**Why a file and not only an RPC:** it is a build artifact, so it can be a declared Gradle task
input; it works in the IDE with no process; it works when the daemon is off; it is diffable, so
it becomes its own regression gate (`git diff` on a committed seed's index catches mangling
drift). See §5 D4 and §6.

### 4.2 L1 — service surface

Additive only. Nothing existing changes shape, so a 0.3.4 plugin talking to a 0.3.5 tool (or
vice versa) keeps working for the pipeline calls.

On `IndexedService` — **read-only queries against the already-resolved model**:

```kotlin
/** The whole index for this run. The bulk fetch; the FIR client uses only this. */
@KsMethod("/index_table")   suspend fun bindingIndex(u: Unit): BindingIndex

/** What does this C++ spec bind to? Bound / Dropped(+why) / Unknown / NotWellFormed. */
@KsMethod("/resolve")       suspend fun resolve(spec: String): ResolveResult

/** Why is there no binding for this? The DropLedger already has the answer. */
@KsMethod("/explain_drop")  suspend fun explainDrop(spec: String): List<Diagnostic>

/** Bound + dropped MEMBERS of a bound type — powers the "unresolved member" diagnostic (§4.3). */
@KsMethod("/members")       suspend fun membersOf(spec: String): MemberReport

/** Pull-based diagnostics for a client that ATTACHED to an existing index and so missed the
 *  push stream `RemoteLogger.diagnostics` delivered during the original run. */
@KsMethod("/diagnostics")   suspend fun diagnostics(sinceIndex: Int): List<Diagnostic>

/** Resolve a specialization into the model AND re-emit the output tree. NOT for the compiler:
 *  it mutates the model and rewrites krapped/. Callers: the sync task, and a future watch mode. */
@KsMethod("/instantiate_now") suspend fun instantiateNow(request: InstantiationRequest): ResolveResult
```

```kotlin
@Serializable
sealed interface ResolveResult {
    @Serializable data class Bound(val ref: BindingRef) : ResolveResult
    @Serializable data class Dropped(val spec: String, val why: List<Diagnostic>) : ResolveResult
    /** Never seen by this run. Materializing it requires a sync — the caller must not block. */
    @Serializable data class Unknown(val spec: String) : ResolveResult
    /** The compiler says this cannot be instantiated (L3 only). */
    @Serializable data class NotWellFormed(val spec: String, val why: Diagnostic) : ResolveResult
}
```

On `KrapperService` — daemon management (L2 only):

```kotlin
/** Reattach to a cached, already-resolved index; null when the key isn't cached. */
@KsMethod("/lookup")   suspend fun lookup(key: IndexKey): IndexedService?
/** uptime, cached keys, RSS, idle deadline — for `kplusplusDaemonStatus` and for tests. */
@KsMethod("/status")   suspend fun status(u: Unit): DaemonStatus
@KsMethod("/evict")    suspend fun evict(key: IndexKey)
```

`IndexKey` = a digest over (tool build id, the full `KrapperConfig`, the full `IndexRequest`,
content hashes of every header reached by the parse). **Anything less is a determinism bug**
(§5 D3).

Deliberately **not** on the surface: any method that lets a client mutate resolve state
concurrently with another client's run (see §1.4), and any method the FIR plugin could use to
change what compiles (see §5 D4).

### 4.3 L2 — the FIR client: the snapshot handshake

**The mechanism.** `KPlusPlusComponentRegistrar.registerExtensions`
(`KPlusPlusComponentRegistrar.kt:21-34`) — once per compilation, before any FIR runs, off every
hot path, already the home of `RequestedManifest.configure` and
`CppVectorMapping.configureRootPackage` — loads **one immutable snapshot** and hands it to the
extensions. Everything downstream reads memory.

Snapshot source, in order:

1. `bindingIndexPath` `SubpluginOption` (a new sibling of `requestManifestPath`,
   `KPlusPlusCommandLineProcessor.kt:44-55`) → read + parse `binding-index.json`. **This is the
   authoritative source.**
2. If `krapperEndpoint` is also configured *and* the index is absent or its `runId` is stale,
   attempt one `bindingIndex()` call with a hard deadline (default 250 ms, `kpp.fir.queryTimeoutMs`).
   The result may only be used for **diagnostics** (§5 D4).
3. Neither available / unparsable / unknown `schemaVersion` → fall back to today's static
   derivation (`CppVectorMapping`'s current code, kept verbatim), log once at warning.

**Per-extension rules.**

- `CppVectorCallRefinement.intercept` — snapshot lookups only: `spec → BindingRef.classId`.
  Never I/O, never a timeout, never a fallible call. The existing
  `symbolProvider.getClassLikeSymbolByClassId` check stays authoritative
  (`CppVectorCallRefinement.kt:32-34`): if the index claims a ClassId the symbol provider does not
  have, the call is left unrefined exactly as today.
- `CppVectorCallChecker` (checkers phase) — snapshot lookups for the ClassId; snapshot `drops`
  for the message. `SYNC_REQUIRED` becomes actionable: instead of
  `kplusplus-sync-required: std::vector<clang::Token>` the user gets
  `std::vector<clang::Token> was dropped: clang::Token declares no operator== (RESOLVE) at
  clang/Lex/Token.h:37:8` — the ledger reason and the real C++ position, both already carried by
  `Diagnostic` (`Diagnostic.kt:70-88`). Still appends to `requested.txt`.
- **New:** `UnboundMemberChecker` (checkers phase, on `FirQualifiedAccessExpression` with an
  unresolved callee) — takes the receiver's type, maps it back to a C++ spec via the snapshot,
  and if that spec has a *dropped member* of that name, replaces the bare `unresolved reference`
  with the drop reason. **This is the honest form of "materialize only what the code touches"**:
  the consuming Kotlin's own unresolved references become the request stream, generalized from
  containers to arbitrary members. Feasibility needs prototyping against Kotlin 2.4 (probe P5).
- `CppVectorIrExtension` — unchanged; it reads the refined type.

**When the service is unavailable or slow.** Non-negotiable invariants:

- **I1.** The snapshot load happens **at most once** per compilation, at `registerExtensions`.
- **I2.** Every service call has a hard deadline and a per-compilation budget (default 250 ms /
  2 s). First timeout disables the channel for the rest of the compilation.
- **I3.** A failed or slow query is **never** an error; it degrades to the on-disk index, then to
  static derivation. The compiler cannot hang on krapper because it never waits on krapper during
  resolution — only during setup, and even then only with a deadline.
- **I4.** In an IDE analysis session, the live channel is **off by default**
  (`kpp.fir.liveQueries=false` unless the endpoint is explicitly configured), because an IDE runs
  checkers on every keystroke.

This is a live client — the plugin consumes krapper's *own* answers rather than a hand-rolled
re-derivation, at compile time — implemented in the only shape that cannot freeze a compiler.

### 4.4 L2 — persistence: lifetime, memory, eviction

**What persists: the model, not the AST.** Recommendation: the daemon retains resolved
`IndexedServiceImpl` state and cached `WrappedTU` parse results keyed by `IndexKey`. It does
**not** retain `ASTUnit`s. Rationale: the measured RSS of a *transient* krapper run is already
~0.4 GB scoped / ~1.15 GB for the broad clang force (#10, 2026-07 comment); an `ASTUnit` over
clang's own headers costs more than that again, and holding several across a daemon session is
the failure mode that gets the feature turned off. The `WrappedTU` is pure Kotlin data and is
already serializable (`ModelIo`, used by `--dump-model`, `Parsing.kt:124-130`), which makes the
cache trivially testable (§5 gate G4). Sema, if it happens at all, gets a *separate, short-lived*
probe mechanism (§4.5) — not a retained AST.

**Transport, two options — take the cheap one first.**

- **D1 (recommended first): keep the stdio pipe; hold the `Process` across builds.**
  `KrapperSession` gets a `KrapperDaemon` holder in plugin-classloader-static state, keyed by
  tool binary hash. No new protocol, no listener, no new native code. Reachable only from the
  Gradle daemon JVM that spawned it — which is exactly where the sync task runs. **Risk:** if the
  buildscript classloader is discarded, the handle is lost and the child is orphaned; mitigated by
  the in-tool idle timeout (below) plus a JVM shutdown hook.
- **D2 (only if L1's live channel is justified): a unix-domain-socket listener in krapper.**
  Needed if a *different process* (the Kotlin/Native compiler, a fresh Gradle daemon) must attach.
  This is genuinely new code: `ksrpc-sockets` on `linuxX64` exposes `asConnection` over channels
  plus `posixFileReadChannel`/`posixFileWriteChannel` (used at `App.kt:349-350,362`) but no
  server accept loop, so the `socket`/`bind`/`listen`/`accept` half must be written against
  `platform.posix` and the accepted fd wrapped in the existing channel helpers. ~100 lines, plus
  a lockfile/pid/socket rendezvous under `$GRADLE_USER_HOME/kplusplus/daemon/<toolHash>/`.

**Idle timeout, and why it is not a watchdog.** The owner's "no watchdog" ruling is about
*client-side timeouts on a call in flight* — a legitimately slow parse (v8 ≈ 4 min) must not be
killed. An **idle timeout inside the tool** is a different thing and is the recorded default:
after N seconds with **no call in flight and no call received** (default 30 min,
`-Pkpp.daemon.idleTimeout`, `0` = persistence off → today's behaviour exactly), the tool exits.
Also legitimate and consistent with the ruling: a **liveness deadline on the handshake** — a
daemon that does not answer `ping` within 5 s is declared dead, killed by pid, and replaced. That
bounds the new failure mode persistence introduces (a wedged daemon from a previous build hanging
every subsequent build) without putting any deadline on real work.

**Eviction.**
- At most **one** retained `IndexedService` per `IndexKey`; at most **K = 2** keys (LRU),
  `-Pkpp.daemon.maxIndexes`.
- Any `KrapperConfig` / `IndexRequest` / header-content change is a different `IndexKey` → new
  index, old one evicted. There is no partial invalidation and there must not be.
- A soft RSS ceiling (default 4 GB, read from `/proc/self/statm`; Linux is the only supported
  host): over it → evict all but the active index; still over → finish the call and exit. A
  process that grows without bound is worse than a slow build.
- `./gradlew kplusplusDaemonStop` / `kplusplusDaemonStatus` tasks, so the escape hatch is
  discoverable.

**Single-tenancy.** Until §1.4's globals are per-index (brick B4), the daemon takes a mutex
around every call that touches resolve state and documents that concurrent module syncs
serialize. Do **not** ship persistence before B4 or the mutex; a parallel Gradle build sharing
one `GenerationContext` is a silent-wrong-output bug, which is the exact class featuregen SYNC
has caught twice.

**The measurable payoff, and it is not the FIR client.** `docs/design/ide-sync-perf.md` measured
a recurring ~1 min on *every* IDE sync, of which ~46 s is the tool module regenerating its own
bindings, because the header-driven `kplusplusSync` declares no outputs and is always
out-of-date (`KPlusPlusCompilerGradlePlugin.kt:115-117`). A cross-build parse cache attacks that
directly and is independently verifiable. **Probe P3 should decide whether persistence ships at
all**: if the second sync of the same headers is not dramatically cheaper, descope L2 to "process
reuse only" or drop it.

### 4.5 L3 — Sema, probe-gated

The premise (`#186`, and `wellformedness-binding-gate.md:11-16`) is that a persistent Clang has
Sema, making overload resolution / SFINAE / instantiability askable. §1's F1 says we do not have
that today and would have to build it. Two candidate mechanisms:

- **(a) Retain the `ASTUnit` and use its `Sema`.** Requires lifting `CppParser.parse` out of
  `memScoped` (`CppParser.kt:76-83`), owning the `ASTUnit*` explicitly (`buildASTWithArgs`
  already `.release()`s it — `clang_slice.h:60`), and binding `ASTUnit::getSema()` plus whatever
  entry points a query needs. **Genuinely uncertain**: whether a `Sema` retained after
  `EndOfTranslationUnit` can still perform lookups, overload resolution and template
  instantiation without crashing is not something to assume. Probe P1.
- **(b) A PCH + `-fsyntax-only` probe TU.** Emit a precompiled header of the module's headers once
  per sync; each well-formedness question becomes a tiny TU (`static_assert(...)`, an explicit
  `template class std::vector<T>;` instantiation, a `decltype(a == b)` expression) compiled with
  `-include-pch`. **Faithful by construction — it is the compiler — stateless, and needs no new
  bindings beyond another `tooling` invocation.** Cost is the question. Probe P2.

**Recommendation: design for (b), and only pursue (a) if P1 comes back clean and P2 comes back
too slow.** Persistence is what makes (b) affordable (the PCH is built once and reused), which is
the real sense in which this issue unlocks Sema.

**How it wires in.** `wellFormed(spec, capability)` becomes an *oracle that may only narrow*
#175's keep-on-doubt: where the AST predicate says "keep (unsure)", the oracle may downgrade to a
proven drop; where the AST predicate says "drop (proven)", the oracle is not consulted. That
preserves #175's safety invariant (`wellformedness-binding-gate.md:306-320`: never drop a valid
binding) while removing the per-family predicate treadmill for the unsure cases. Every
oracle-driven drop goes through `DropLedger.record` like any other, so the ledger diff remains
the audit.

### 4.6 L4 — lazy binding

**Trigger granularity.** The unit of request is a **string** — a C++ type spec
(`std::vector<int>`) or a qualified member (`clang::Decl::getBody`). Both are already the keys
the ledger and the forcing flow use (`IndexedServiceImpl.kt:63-64,188-213`). The request set
accumulates in `requested.txt` across builds and is canonicalized (sorted) before each run
(`KPlusPlusCompilerGradlePlugin.kt:303`). The unit of *materialization* is the module's whole
`krapped/` tree (§1.5) — so batching per sync is mandatory and per-spec incrementality is not on
the table.

**The mechanism.** A new opt-in `ReferencePolicy.ON_DEMAND` (or a separate `BindingPolicy`,
whichever reads better against `KrapperConfig`): resolve the requested specs and expand
referenced types only far enough to *emit their signatures* — referenced-but-unrequested types
become borrowed/opaque, as `IGNORE_MISSING` already does — but additionally **record every such
type in the ledger as `AVAILABLE_NOT_REQUESTED`**. Those records flow into the index, and the FIR
`UnboundMemberChecker` (§4.3) turns "you touched a method whose return type isn't bound" into a
`SYNC_REQUIRED` naming that type. That closes the loop: Kotlin usage drives the request set; the
request set drives the next sync; the closure is never enumerated.

**What it cannot be.** It cannot be the default, because it changes bound output and
`broad-forcing-resolve.md:216` rates that "disqualifying as the primary fix" for exactly the
reason featuregen's cross-instantiation story depends on `INCLUDE_MISSING`'s transitivity.
`INCLUDE_MISSING` and `IGNORE_MISSING` must remain byte-identical.

**Convergence cost.** Under `ON_DEMAND` a fresh consumer converges in as many
build→sync→build cycles as the depth of the type graph they touch. Today's `SYNC_REQUIRED` loop
already costs one extra invocation and is accepted; a deep graph could cost several, which is a
worse UX. Two mitigations, both Gradle-level and both *out of scope until someone measures the
pain*: (i) a `kplusplusScan` analysis-only pre-pass that collects requests before the sync — needs
prototyping, and it has its own chicken-and-egg (the scan compile needs the cinterop klib that
does not exist yet); (ii) a compile-retry that re-runs sync + compile once when the only errors
are `SYNC_REQUIRED`. **Do not build either speculatively.**

---

## 5. Determinism contract

The invariant, stated so it can be tested:

> **D1.** For a fixed (tool build, `KrapperConfig`, `IndexRequest`, header contents, request
> **set**), the emitted tree is byte-identical regardless of the order in which requests arrived,
> the order/number of queries interleaved, whether the process was fresh or reused, and whether
> any other index was previously served by that process.

Supporting rules:

- **D2 — canonical set, never arrival order.** Every run is driven from the sorted, deduped
  request set (`KPlusPlusCompilerGradlePlugin.kt:303`) and every forcing parse is reordered to the
  base tree's first-seen order (`Parsing.kt:184-203`). Laziness may change *which* specs are in
  the set; it may never change how the set is ordered before the run.
- **D3 — cache keys are total.** Any cached parse or resolved index is keyed on the full
  `IndexKey` (tool build id + full config + full request + header content hashes). A cache keyed
  on anything less is a determinism bug of exactly the kind featuregen SYNC has caught twice.
- **D4 — the live channel may not affect compiled output.** The FIR plugin's *resolution*
  behaviour may depend only on the on-disk `binding-index.json`, which is a **declared task
  input**. A live query may only affect *diagnostic text*. Without this rule the compile task's
  inputs would not capture what it actually depended on, and Gradle's up-to-date checks and the
  build cache would be silently wrong.
- **D5 — queries are read-only.** No method reachable by the FIR client may mutate resolve state.
  `instantiateNow` is reachable by the sync task only (§4.2).

Machine-checkable gates, per brick:

- **G1** — `:featuregen:kplusplusSync` (cpp path) regenerate-and-diff: every pre-existing file
  byte-identical. The one intended new file is `binding-index.json`. This is the gate that has
  caught every generator regression to date and it is not negotiable.
- **G2** — `:cppfixture` + `:clangwalk` regenerate-and-diff, plus a `DropLedger` diff (the pattern
  `wellformedness-binding-gate.md:329-333` established).
- **G3** — extend `DeterminismTest` (`krapper/src/nativeTest/kotlin/DeterminismTest.kt`, which
  exists precisely to catch leaked process globals) with: (a) two runs in one process over the
  **same** set in **shuffled request order** → identical; (b) two runs in one process over
  **different configs** → each identical to its own fresh-process run. (b) is the direct test of
  §1.4 and is a prerequisite for persistence.
- **G4** — a cached `WrappedTU`'s `ModelIo.encodeToString` equals a fresh parse's, for
  featuregen's header and for `clang_slice.h`.
- **G5** — index/derivation agreement: for every `BindingRef` in featuregen's index,
  `CppVectorMapping.bindingClassId(...)` (the static derivation) produces the same ClassId. This
  is the drift detector §1.3 says we currently lack, and it is why the static path should be kept
  rather than deleted.
- **G6** (L3 only) — the oracle may only turn keeps into drops: the post-oracle ledger must be a
  **superset** of the pre-oracle ledger, and G1 must stay green.

---

## 6. IDE and incrementality — the load-bearing constraint

Generated bindings are files on disk, and that is what makes IDE resolution and Gradle
incrementality work. **Nothing in this design changes that.** Concretely:

- The index is one more file in the same directory the IDE and Gradle already track. It becomes a
  declared input of the compile task (alongside `requestedManifest`,
  `KPlusPlusCompilerGradlePlugin.kt:96-98`).
- The FIR plugin never synthesizes declarations. It refines to classes the symbol provider already
  has (`CppVectorCallRefinement.kt:32-34`) and that check remains authoritative.
- Live queries are **off by default in an IDE session** (§4.3 I4) and, when on, may only produce
  diagnostics (D4). An IDE analysis pass that cannot reach the daemon behaves exactly as today.
- The daemon is a **cache**, never a source of truth. Deleting it, killing it, or never starting
  it changes wall time and nothing else. That property is what makes the whole thing safe to ship
  incrementally, and it is worth stating in the release notes.

One genuine improvement available here: `binding-index.json` gives the header-driven
`kplusplusSync` a *declarable output* (`:115-117` currently declares none, which is the root of
the recurring ~1 min IDE-sync cost measured in `ide-sync-perf.md`). Worth pursuing on its own
merits, and it is a side-effect of L0.

---

## 7. #194 interaction

#194 (0.3.4: multi-project consumers get the root classloader's coroutines 1.8.0 instead of the
resolved 1.11.0, breaking ktor-io inside `KrapperSession.start`) is a hard dependency for **L2
only**, and it has a design implication beyond "fix it first":

- The Gradle-plugin side already carries ksrpc/ktor/coroutines (`compiler/gradle/build.gradle.kts:49-52`).
  A *persistent* connection holds that stack open across the whole build and across builds,
  instead of for one task, so it is exercised far harder than #185's per-run session. **L2 must
  not land before #194's fix.**
- #194 offers two remedies: (1) shade/relocate, (2) an isolated-classloader Gradle worker. #194
  rates (1) simpler. **For #186, (2) is the better fit**: a worker gives the daemon handle a
  stable, non-buildscript classloader home, which is exactly what cross-build persistence needs
  (§4.4 D1's orphan risk *is* the classloader-discard risk). Worth routing back to #194 as input;
  if (1) ships, L2's holder needs an explicit orphan-reaper.
- **The FIR-plugin side must stay dependency-free.** `compiler/plugin` today declares only
  `compileOnly kotlin-compiler-embeddable` (`compiler/plugin/build.gradle.kts:62-64`). Putting
  ksrpc/ktor/coroutines on the *kotlinc plugin* classpath would repeat #194's failure mode one
  level deeper, inside the Kotlin/Native compiler's plugin classloader, next to kotlinc's own
  bundled coroutines. If L1 ever ships, its client is a hand-written length-prefixed JSON-lines
  reader over `java.net`, ~150 lines, no third-party classes. This is a hard constraint on L1's
  design, not a preference.
- #194 also notes there is **no multi-project consumer in the test matrix**. Adding one is a
  prerequisite for trusting any of this.

---

## 8. Brick sequence

Each brick is independently landable, independently verifiable, and reversible by reverting one
commit. Nothing is deleted until its replacement has shipped for a release.

| # | Brick | Verifies with | Reversible by | Blocked on |
|---|---|---|---|---|
| **B0** | **Probes** (§9). No production code; each lands as a measurement note or a `nativeTest`. | the probe's own output | n/a | — |
| **B1** | **Emit the index.** `writeTo` also writes `binding-index.json`. No consumer. | G1 (all pre-existing files byte-identical), G2 | delete the writer | — |
| **B2** | **Consume the index.** `bindingIndexPath` SubpluginOption; `CppVectorMapping` prefers the index, keeps static derivation as fallback. Includes **G5** (the drift detector). | G5, full featuregen test run, `samples/minimal` | drop the option | B1, released tool |
| **B3** | **Real diagnostics.** `SYNC_REQUIRED` carries the ledger reason + C++ `file:line:col`; `kotlinTypeMap` replaces the 4-entry table (`CppVectorMapping.kt:176-182`). | new FIR diagnostic tests; a `cppVector<Boolean>()` row in featuregen | revert the message change | B2 |
| **B4** | **De-globalize.** `DropLedger` / `GenerationContext` / `cppParseIncludeDirs` / `cppModelDumpDir` / `cppBaseModelTu` become per-`IndexedService` state (or an explicit mutex + documented single-tenancy). | **G3(b)** — two configs in one process match two fresh processes | revert | — (independent, do early) |
| **B5** | **Persistence (D1).** Parse cache keyed on `IndexKey`; process held across builds; idle timeout; `kplusplusDaemonStop/Status`; falls back to today's per-run session on any failure. | G1, G3, G4; **measured** second-sync wall time; `kill -9` mid-build degrades cleanly | `-Pkpp.daemon.idleTimeout=0` | B4, **#194**, P3 |
| **B6** | **`UnboundMemberChecker`** — unresolved member on a bound type gets the drop reason. | FIR diagnostic tests | remove the checker | B3, P5 |
| **B7** | **`wellFormed()` oracle** wired as a narrowing of #175's keep-on-doubt. | **G6** + G1 + re-measured broad-force error count | config flag off | P1/P2, B5 |
| **B8** | **`ON_DEMAND` policy** + re-measure #10's residual ~185 against a live-Sema binder. | G1 unchanged for the eager policies; a new broad-force measurement | policy not selected | B7 |
| **B9** | **Live channel (L1)**, dependency-free client, diagnostics-only. **Only if** a query is demonstrated that B1–B3 cannot serve. | I1–I4 as tests; D4 by construction | endpoint not configured | B5, #194, and a *demonstrated need* |

Note the shape: **B9 is last and conditional.** The ruling that the FIR plugin becomes a live
client is satisfied by B2/B3/B6 — the plugin consumes krapper's own answers at compile time
instead of re-deriving them. B9 adds a socket only if we find a question that is not immutable
for the duration of a sync. As of this reading, we have not found one.

Release-lag reminder: the root build consumes the *published* plugin
(`settings.gradle.kts:17`), so B2/B3/B6's plugin-side changes only reach this repo's own modules
at the next release. Sequence tool-side (B1, B4, B5) and plugin-side (B2, B3, B6) accordingly.

---

## 9. Probes — what to measure before committing

Each is cheap, and each can kill a brick.

- **P1 — Sema liveness.** Standalone C++ (like `wellformedness-binding-gate.md`'s
  `/tmp/wfprobe/probe.cpp`): after `clang::tooling::buildASTFromCodeWithArgs` returns, is
  `ASTUnit::getSema()` non-null, and can it perform a lookup / overload resolution / explicit
  template instantiation without asserting? **Kills or confirms §4.5(a). Half a day.**
- **P2 — PCH probe cost.** Build a PCH of `featuregen/src/cppMain/include/strings_feature.h` and
  of `krapper/include/clang_slice.h`; time 100 `-fsyntax-only` probe TUs. **Target: <50 ms/probe
  on featuregen, <300 ms on the clang slice.** If it is seconds, per-binding well-formedness is
  unaffordable and #175's AST predicates stay the mechanism. **Decides B7/B8.**
- **P3 — does persistence actually pay?** One process, two sequential `index()` + `writeTo` calls
  for featuregen with the parse cached vs. not. If the second is not dramatically cheaper,
  descope B5 to process reuse or drop it. **Decides B5.**
- **P4 — in-process repeat on the real parse path.** Run `filterAndResolve` + `writeTo` twice in
  one process for featuregen and diff the trees. `DeterminismTest` covers a synthetic model
  (`DeterminismTest.kt:44-63`); this covers the real front-end. **Cheapest possible test of §1.4
  and a prerequisite to B4/B5. An hour.**
- **P5 — FIR feasibility.** Prototype a checkers-phase checker that sees an *unresolved* callee,
  recovers the receiver's `ConeKotlinType`, and reports a custom diagnostic, against Kotlin 2.4.
  **Decides B6.**
- **P6 — RSS drift.** Peak and steady RSS after 5 sequential featuregen indexes in one process.
  **Decides B5's ceiling and eviction constants.**
- **P7 (optional) — multi-project consumer.** Stand up the two-module sample #194 asks for and
  confirm B5's daemon behaves. **Prerequisite for trusting any of this in the field.**

---

## 10. Risks

- **R1 — the two #10 premises may not both dissolve.** Laziness genuinely removes the closure
  enumeration for the broad-force scenario, but it cannot be the default without changing bound
  output (`broad-forcing-resolve.md:216`), so #10's featuregen-shaped surface is unaffected.
  Sema is unproven (F1) and gated on P1/P2. **Honest position: this issue makes both premises
  attackable; it does not by itself dissolve them.** #10 should stay parked until B7/B8 produce a
  measurement.
- **R2 — the headline capability is unreachable.** Nothing can materialize a binding inside a
  running compilation (F2). If the expectation set by #186 is "the compiler pulls bindings into
  existence as it resolves", that expectation needs correcting now rather than at review.
- **R3 — a persistent process is a new class of user-visible bug.** Leaked processes holding
  gigabytes, a wedged daemon hanging every subsequent build, stale-cache wrong output. Mitigated
  by: idle timeout, handshake liveness deadline, total cache keys (D3), RSS ceiling, a documented
  stop task, and `idleTimeout=0` returning to today's behaviour exactly. Still the biggest
  operational risk in the plan.
- **R4 — single-tenancy (§1.4).** Shipping persistence before B4 risks silently-wrong output
  under parallel Gradle builds. Sequenced first for this reason.
- **R5 — #194, one level deeper.** A persistent connection leans harder on ktor/coroutines
  (§7), and an ksrpc dependency on the *kotlinc* plugin classpath would be strictly worse than
  #194. Hard constraint, not a preference.
- **R6 — cross-build cache invalidation.** The exact bug class featuregen SYNC exists to catch.
  D3 + G4 are the defence; if either is hard to hold, drop the parse cache and keep only process
  reuse.
- **R7 — convergence UX under `ON_DEMAND`.** Several build cycles to converge on a deep type
  graph (§4.6). Measure before building a scan pass or a compile-retry.

---

## 11. Where I would spend less ambition

Stated plainly, per the brief:

**B1 + B2 + B3 is ~80% of the realizable value of this issue and needs no persistence, no
socket, and no Sema.** It replaces a silently-drifting re-derivation of the generator's mangling
(`CppVectorMapping.kt:248-268`) with the generator's own answer, replaces a 4-entry type table
with krapper's, and turns `kplusplus-sync-required: <spec>` into a real compiler diagnostic
carrying the C++ position and the reason. It is three small, independently verifiable changes
with an equality test (G5) that would have caught the drift class we currently cannot see.

Everything above that line — persistence, the socket, Sema, lazy binding — should each be gated
on its own probe, and each is allowed to come back negative. In particular: if P3 says a second
sync is not much cheaper with a cached parse, **do not build the daemon**; and if P2 says a PCH
probe costs seconds, **do not build the oracle** — #175's AST predicates remain the honest shape,
exactly as that document argued.

---

## 12. Open questions for the owner

1. **Is a bare `SYNC_REQUIRED` failure + one extra invocation acceptable UX?** If yes, §4.6's
   convergence mitigations (scan pass / compile-retry) stay unbuilt and this issue shrinks
   considerably. If no, that is a separate, Gradle-level piece of work worth its own issue.
2. **Is `binding-index.json` a committed artifact for seed modules?** Committing it makes mangling
   drift a `git diff`, which is the cheapest possible regression gate — but it adds a file to
   every seed tree.
3. **#194's remedy.** (2) isolated-classloader worker is the better substrate for #186 than (1)
   shade/relocate (§7). Worth deciding there with #186 in view.
4. **Confirm the read of `broad-forcing-resolve.md:216`** — that bounded/lazy closure cannot be the
   default binding policy. The lazy-binding brick's whole shape depends on it.
