# Design: migrating the carried `MemScope` to context parameters

Status: design proposal (read-only analysis; no generator changes made)
Author: analysis pass, 2026-07
Scope: `krapper_gen` Kotlin codegen (`KotlinWriter.kt`) + the kplusplus Gradle plugin's consumer-compile wiring

## TL;DR / verdict

The carried `memScope` field **can be dropped from the vast majority of wrappers, but not
all of them.** The precise split:

- **Plain value wrappers** (`geo::Vec`, `Coord`, `Vec2`, `Point`, …): the carried field is
  **fully droppable**. Its only runtime read is the by-value `_Holder` allocation, which
  should come from the *ambient* scope, not the source object's scope. Context params fix
  the known lifetime bug here for free.
- **Borrowed views** (up/down-casts `asDerived()`, container element access `at()`/`get()`/
  `front()`, the by-`&`/`*` returns): these carry `memScope` **only to satisfy the
  constructor signature — they never use it.** They should carry **nothing**.
- **Owning wrappers** (anything with a destructor, i.e. everything that emits `dispose()` /
  `owned()`): the field is needed by exactly one method — `owned()` — which defers
  destruction onto *a* scope. This does **not** need the *object's own creation scope*; it
  needs *a* scope, and the ambient one is the correct one. So the field is **droppable here
  too, provided `owned()` becomes `context(MemScope) fun owned()`.**

=> **Full-drop is viable for every wrapper we generate today**, because every runtime use
of the carried field wants "a scope from the call site", which is exactly what a context
parameter supplies. There is **no case in the current surface where a derived value
genuinely needs the *source object's own* scope rather than the ambient one.**

The honest caveat is not a wrapper that *needs* the field; it is a **behavioral change**
(see Edge E5) and a **`_Holder` ambiguity risk** (E2): making `_Holder` / `owned()` /
allocating operators context-dependent means a call site with **no** `MemScope` in scope
that compiles today will **stop compiling** (good: it was a latent bug), and a site with
**two** MemScopes in scope becomes **ambiguous** and must disambiguate. Those are the real
edges, and they are compile-time, not silent.

---

## 1. Current model (what the carried field actually does)

Every wrapper is `class T(val ptr: COpaquePointer, val memScope: MemScope)`. `ptr` is the C++
object; `memScope` is the `kotlinx.cinterop.MemScope` (an `Arena`) that was *active when the
wrapper was constructed*. It is set from the factory's `MemScope` extension receiver
(`KotlinWriter.kt` threads builder `thiz` — literally `this` — as the third constructor
argument at every `generateConstructorCall`).

Exhaustively, the carried field is read at runtime in only **two** shapes (verified by
grepping every `.memScope.` read across `featuregen/krapped/src`):

1. **By-value-return allocation** — `memScope.<T>_Holder()`
   (`KotlinWriter.generateMethodBody`, ARG_CAST path, ~line 2127). An instance method /
   getter / operator that returns a C++ value-type allocates the return slot via the
   `_Holder` factory **on the receiver object's carried scope**:

   ```kotlin
   // ConstRetHolder.kt — instance method returning by value
   inline fun makeConst(): Coord {
       val retValue: Coord = memScope.Coord_Holder()   // <-- this.memScope
       ConstRetHolder_make_const(ptr, retValue.ptr)
       return retValue
   }
   ```

   **This is the "lifetime weirdness" the owner flagged.** `val c = holder.makeConst()`
   ties `c`'s storage to `holder`'s scope, not to the enclosing `memScoped {}`. If `holder`
   outlives the block or was itself made in a wider arena, `c`'s lifetime is silently wrong.

2. **`owned()` deferral** — `memScope.defer { T_dispose(ptr) }`
   (`KotlinWriter.generateDisposeMethods`, ~line 477). Registers destruction of a
   *non-owning* wrapper onto the carried scope at the caller's request:

   ```kotlin
   inline fun owned(): Animal {
       memScope.defer { Animal_dispose(ptr) }   // <-- this.memScope
       return this
   }
   ```

Everything *else* that mentions `memScope` **passes it into a constructor and never reads
it** — it is dead weight kept only because the constructor demands a third argument:

- Casts / views: `asDerived()`, `asDog()` → `Derived(raw, memScope)` — borrowed, no dispose.
- Container element access: `at`/`get`/`front`/`back`/`data`/`op[]` → `Point(ptr, memScope)`
  — borrowed views into the container's storage, no dispose.
- By-reference `assign()` returns → `Vector__Point(ptr, memScope)` — borrowed.

The construction-time `defer` inside the *factories* (`MemScope.Animal()`, the container
constructors) does **not** use the carried field — it uses the factory's own `MemScope`
receiver (`this`/`thiz`). That is already "ambient-at-construction" and is unaffected by the
migration except cosmetically (`this.defer` becomes `contextScope.defer`).

---

## 2. Proposed model (context parameters)

Kotlin 2.4 `-Xcontext-parameters` (already enabled in `compiler/native` and
`compiler/plugin`; **not** the deprecated `-Xcontext-receivers`).

Replace `fun MemScope.T()` factories and every allocating member with a **context
parameter** that supplies the allocation scope from the ambient context at the *use* site:

```kotlin
class T(val ptr: COpaquePointer)                       // no carried scope

context(scope: MemScope) fun T(): T { … }              // factory: alloc in ambient scope
context(scope: MemScope) fun T.byValueReturn(): U { … } // by-value method: alloc in ambient scope
context(scope: MemScope) fun T.owned(): T { … }         // register dispose on ambient scope
```

### Allocation scope

At a by-value return / factory, the `_Holder` / `alloc()` runs against the **context
`scope`** rather than `this.memScope`. So:

```kotlin
context(scope: MemScope) fun ConstRetHolder.makeConst(): Coord {
    val retValue: Coord = scope.Coord_Holder()   // ambient, not the holder's scope
    ConstRetHolder_make_const(ptr, retValue.ptr)
    return retValue
}
```

`val c = holder.makeConst()` now allocates `c` in whatever `MemScope` is in scope at the
call — i.e. the enclosing `memScoped {}`. **The lifetime bug is gone.**

### Destructor registration

The owner's specific question: does construction-time context fully cover ownership without
a carried field? **Yes.** Two independent ownership sites, both scope-at-call:

- **Owning factories** already defer at construction on the factory's scope. Under context
  params that becomes the context scope:
  ```kotlin
  context(scope: MemScope) fun Animal(): Animal {
      val memory = interpretCPointer(scope.alloc(size, align).rawPtr) ?: error(…)
      val obj = Animal_new(memory) ?: error(…)
      scope.defer { Animal_dispose(obj) }   // deferred on the AMBIENT scope
      return Animal(obj)
  }
  ```
  This is strictly *more* correct than today: the object and its cleanup now live in the
  ambient scope instead of being decided by wherever `MemScope.Animal()` happened to be
  called (which today is the same scope, but only because the factory *is* the receiver —
  the point is nothing is lost).

- **`owned()`** on a borrowed pointer becomes `context(scope) fun T.owned()` and defers on
  the ambient scope — again correct-by-construction. It no longer needs the object to
  *remember* a scope; it takes one now.

No wrapper needs to store a scope to make destruction work, because destruction is always
registered **at a point where a scope is in context** (construction, or the explicit
`owned()` call). The carried field was only ever a *convenience copy* of a scope that is
available ambiently at every site that reads it.

---

## 3. Before / after on three real shapes

### (1) Plain value type — `geo::Vec`

**Before** (`geo_Vec.kt`):
```kotlin
class Vec(val ptr: COpaquePointer, val memScope: MemScope) {
    inline fun lengthSq(): Double = geo_Vec_length_sq(ptr)
    companion object {
        fun MemScope.Vec(): Vec {
            val memory = interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed")
            val obj = geo_Vec_new(memory) ?: error("Creation failed")
            return Vec(obj, this)
        }
        inline fun MemScope.make(x_: Double, y_: Double): Vec {
            val retValue: Vec = memScope.Vec_Holder()   // NB: free-fn receiver here, not a field
            geo_Vec_make(x_, y_, retValue.ptr)
            return retValue
        }
        fun MemScope.Vec_Holder(): Vec { … return Vec(memory, this) }
    }
}
```

**After**:
```kotlin
class Vec(val ptr: COpaquePointer) {                    // field dropped
    inline fun lengthSq(): Double = geo_Vec_length_sq(ptr)
    companion object {
        context(scope: MemScope) fun Vec(): Vec {
            val memory = interpretCPointer(scope.alloc(size, align).rawPtr) ?: error("Allocation failed")
            val obj = geo_Vec_new(memory) ?: error("Creation failed")
            return Vec(obj)
        }
        context(scope: MemScope) fun make(x_: Double, y_: Double): Vec {
            val retValue: Vec = Vec_Holder()            // context propagates into Vec_Holder
            geo_Vec_make(x_, y_, retValue.ptr)
            return retValue
        }
        context(scope: MemScope) fun Vec_Holder(): Vec { … return Vec(memory) }
    }
}
```

Call site is unchanged: `memScoped { Vec() }` still resolves (see §5).

### (2) Container — `std::vector<Point>`

**Before** (`std_Vector__Point.kt`, excerpts):
```kotlin
class Vector__Point(val ptr: COpaquePointer, val memScope: MemScope) : Iterable<Point?> {
    inline operator fun get(__n: size_t): Point? =
        Point((std_vector_Point_op_ind(ptr, __n) ?: return null), memScope)  // borrowed elem
    inline fun at(__n: size_t): Point? =
        Point((std_vector_Point_at(ptr, __n) ?: return null), memScope)      // borrowed elem
    inline fun push_back(__x: Point): Unit = std_vector_Point_push_back(ptr, __x.ptr)
    companion object {
        fun MemScope.Vector__Point(): Vector__Point {
            … ; defer { std_vector_Point_dispose(obj) } ; return Vector__Point(obj, this)
        }
    }
    inline fun owned(): Vector__Point { memScope.defer { std_vector_Point_dispose(ptr) }; return this }
}
```

**After**:
```kotlin
class Vector__Point(val ptr: COpaquePointer) : Iterable<Point?> {
    inline operator fun get(__n: size_t): Point? =
        Point(std_vector_Point_op_ind(ptr, __n) ?: return null)   // borrowed — NO scope
    inline fun at(__n: size_t): Point? =
        Point(std_vector_Point_at(ptr, __n) ?: return null)       // borrowed — NO scope
    inline fun push_back(__x: Point): Unit = std_vector_Point_push_back(ptr, __x.ptr)
    companion object {
        context(scope: MemScope) fun Vector__Point(): Vector__Point {
            … ; scope.defer { std_vector_Point_dispose(obj) } ; return Vector__Point(obj)
        }
    }
    context(scope: MemScope) fun owned(): Vector__Point {
        scope.defer { std_vector_Point_dispose(ptr) }; return this
    }
}
```

**Element access is the owner's #1 worry — and it resolves cleanly.** Today
`at(i)`/`get(i)` return `Point(rawElemPtr, memScope)`: the pointer aliases *into the
container's own heap storage*, and **no dispose is ever registered on it** — the wrapper is
a pure borrowed view whose real lifetime is the container's C++ buffer. The carried
`memScope` on that element is **completely unused** (dead). After migration the element
wrapper carries **nothing**, which is *more honest*: its validity is bounded by the
container, not by any Kotlin scope. `push_back(x)` copies `x` into the container (C++ value
semantics) and returns `Unit` — no wrapper, no scope, unaffected.

The subtle point to be explicit about: **the element's lifetime was never actually tied to
the carried scope even today.** `Point(elemPtr, memScope).owned()` would have been a
use-after-free waiting to happen (deferring a `delete` of an interior element pointer). So
dropping the scope from element views removes a footgun rather than a feature.

### (3) Owning / polymorphic type — `Animal` (virtual, has destructor, up/down-casts)

**Before** (`root_Animal.kt`):
```kotlin
class Animal(override val ptr: COpaquePointer, val memScope: MemScope) : AnimalApi {
    override fun speak(): Int = Animal_speak(ptr)
    companion object {
        fun MemScope.Animal(): Animal {
            … ; defer { Animal_dispose(obj) } ; return Animal(obj, this)
        }
    }
    inline fun dispose(): Unit = Animal_dispose(ptr)
    inline fun owned(): Animal { memScope.defer { Animal_dispose(ptr) }; return this }
    inline fun asDog(): Dog? {
        val raw = Animal_dyncast_Dog(ptr) ?: return null
        return Dog(raw, memScope)                          // borrowed downcast view
    }
}
```

**After**:
```kotlin
class Animal(override val ptr: COpaquePointer) : AnimalApi {   // AnimalApi.ptr unchanged
    override fun speak(): Int = Animal_speak(ptr)
    companion object {
        context(scope: MemScope) fun Animal(): Animal {
            … ; scope.defer { Animal_dispose(obj) } ; return Animal(obj)
        }
    }
    inline fun dispose(): Unit = Animal_dispose(ptr)          // explicit delete — no scope
    context(scope: MemScope) fun owned(): Animal {
        scope.defer { Animal_dispose(ptr) }; return this
    }
    inline fun asDog(): Dog? {
        val raw = Animal_dyncast_Dog(ptr) ?: return null
        return Dog(raw)                                       // borrowed view — NO scope
    }
}
```

`dispose()` (delete now) needs no scope. `owned()` (delete at scope end) takes the ambient
scope as context. The `asDog()` downcast is a borrowed view of the *same* object viewed as
`Dog` and drops the scope. The `BaseApi`/`AnimalApi` interface only ever carried `ptr`
(never `memScope` — confirmed at `KotlinWriter.kt` ~line 394), so the interface surface is
**unchanged** by the migration.

---

## 4. Edge-case catalog

| # | Edge | Needs the *object's own* scope? | Handling |
|---|------|-------------------------------|----------|
| E1 | By-value return from an instance method/getter/operator (`makeConst`, `Vec2.plus`) | **No** — it wants the *ambient* scope (that's the bug fix) | `context(scope) fun T.m()`; `_Holder` runs on `scope` |
| E2 | `_Holder` / factory called with **no** `MemScope` in scope | n/a | **Won't compile** (today it silently compiled if you had a wrapper handy). This is a *correctness win*, but see the compile-break note below |
| E3 | Container element access `at`/`get`/`front`/`back`/`op[]` | **No** — element is a borrowed view of container storage; carried scope is already dead | Drop the scope entirely; element carries only `ptr` |
| E4 | Up/down-cast views `asDerived()`/`asDog()`, by-`&` returns (`assign`) | **No** — borrowed, never disposed | Drop the scope entirely |
| E5 | `owned()` on a borrowed pointer | **No** — needs *a* scope, ambient is the right one | `context(scope) fun T.owned()`; `scope.defer{dispose}` |
| E6 | Owning **factory** construction-time `defer` | **No** — already uses the factory's ambient receiver, not a field | `scope.defer{dispose}`; strictly ≥ as correct |
| E7 | **Escaping / stored** wrapper (field-held, callback capture — e.g. `CbCtx`) | **No, and this is the important honesty point** | See below |
| E8 | Two `MemScope`s in scope (nested `memScoped`) at an allocating call | n/a | **Ambiguity error** — must disambiguate. See §5 |
| E9 | An allocating op reached with a `MemScope` bound by `with(scope){}` rather than `memScoped{}` | n/a | Resolves fine — any implicit `MemScope` receiver satisfies the context param (§5) |

### E7 — escaping objects (the owner's second specific worry)

A wrapper stored in a Kotlin field or captured by a callback lambda (`StableRef` machinery,
`CbCtx`) survives past the `memScoped {}` that made it. **The carried `memScope` does NOT
protect such an object today.** `MemScope` is an `Arena`: at block close it frees *its*
allocations and runs *its* deferred `dispose`es regardless of who holds a Kotlin reference
to the wrapper. So an escaped wrapper is *already* a dangling pointer the moment its arena
closes — the carried field neither extends the lifetime nor records anything actionable. It
is purely vestigial for escapees.

Therefore migration changes nothing about escape *safety* (it was never safe; ownership of
an escaping C++ object is on the user, via `dispose()`/manual arena management). What
migration *does* is make the failure mode **louder**: to allocate/own an escaping object you
must now have a `MemScope` in context, so the arena you're binding to is explicit at the
call, instead of being whatever scope the source wrapper happened to memorize. Net: **the
carried field is not load-bearing for escaping objects and can be dropped**, and the design
should keep documenting (as the generated `dispose()`/`owned()` comments already do) that
cross-scope ownership is the user's responsibility.

### E2 note — the one real behavioral break

Any *currently compiling* call site that allocates by calling `_Holder`/`owned()`/an
allocating method **without** a `MemScope` in scope (relying on the receiver object's carried
one) will **stop compiling**. This is desired (it was the lifetime bug), but it is a
**source-incompatible change to consumer code** — consumers may need to wrap call sites in
`memScoped { }` or add a `context(scope: MemScope)` to their own enclosing function. This is
the single most consequential migration cost and is the thing to socialize before flipping.

---

## 5. `memScoped { Vec() }` and context resolution (verified from Kotlin semantics)

**Yes, `memScoped { Vec() }` still resolves after migration.** `memScoped { … }` is
`inline fun <R> memScoped(block: MemScope.() -> R): R` — inside the block, `MemScope` is the
lambda's **receiver** (an implicit/default receiver). The Kotlin context-parameter
resolution algorithm (KEEP §7.5, "Extended resolution algorithm") states:

> "For each potentially applicable callable, for each context parameter, we traverse the
> tower of scopes looking for **exactly one** default receiver or context parameter with a
> compatible type."

i.e. **an implicit/default receiver is a valid candidate to fill a context parameter** (the
KEEP's own `with(console) { logWithTime(...) }` example demonstrates exactly this). So the
`MemScope` receiver introduced by `memScoped {}` (or by `with(scope) {}`, or by a nested
generated factory that is itself `context(MemScope)`) satisfies `context(scope: MemScope)`
transparently. **Existing call sites of the form `memScoped { Foo() }` are unchanged.**

Two resolution edges follow directly from the "**exactly one**" rule:

- **E8 ambiguity:** if two `MemScope`s are in scope (a `memScoped {}` nested inside another,
  or a `memScoped {}` inside a `context(MemScope)` function), an allocating call is
  **ambiguous** and errors. The user disambiguates with an explicit `context(theScope) { … }`
  block or by naming. Today this "just works" (inner scope wins as the nearer receiver for
  the factory extension), so this is a **new** friction point for nested-scope code. Worth
  calling out to the owner.
- **E2 absence:** zero `MemScope`s in scope → no candidate → compile error (the desired
  behavior from §4/E2).

---

## 6. Migration impact

**What regenerates:** every generated `.kt` binding. Mechanical changes in `KotlinWriter.kt`:

1. Class decl: `cls(named(type), listOf(ptrProp, property(memScope)), …)` →
   drop `property(memScope)` (~line 398); wrappers become `class T(val ptr)`.
2. `generateConstructorCall` (~line 2588): drop the `memScope` third argument;
   `Call(constructorMethod(type), ptr)`.
3. Factories (`generateDirectConstructor`/`generateStackConstructor`/`_Holder`/template
   facade): `receiver = fqType(MEM_SCOPE)` → a context parameter `context(scope: MemScope)`;
   `thiz.reference` / `this` → the context param symbol; `defer { … }` → `scope.defer { … }`.
   (Requires a builder primitive for emitting `context(name: Type)` on a `FunctionBuilder` —
   new codegen surface, since today only `receiver = …` exists.)
4. By-value ARG_CAST path (~line 2127): `memScope dot Call(_Holder)` → `scope`-qualified (or
   bare, letting the context propagate) `_Holder()`; the enclosing method gains
   `context(scope: MemScope)`.
5. `owned()` (~line 471): `memScope.defer` → `scope.defer`; method gains `context(scope)`.
6. Casts/views (`generateCastMethods`/`generateDownCastMethods`, element access): drop the
   `memScope` argument to the constructor call — these become scope-free.

**The `-Xcontext-parameters` flag must reach the CONSUMER compile.** Today only
`compiler/native` and `compiler/plugin` set it; the **consumer** (e.g. `featuregen`, and any
downstream project using the kplusplus Gradle plugin) does **not**. The generated bindings
would now *contain* `context(...)` declarations, so the module that compiles
`krapped/src/*.kt` needs the flag. The kplusplus Gradle plugin already injects the generated
sources into the consumer's source set (`KPlusPlusCompilerGradlePlugin.kt` ~line 730), so it
should **also add `-Xcontext-parameters` (and keep the existing `-Xcontext-receivers`-free
posture) to the consumer's Kotlin native `compilerOptions.freeCompilerArgs`** as part of the
same wiring. This is a required, non-optional part of the migration — without it, generated
code won't compile in consumer projects. (Minimum Kotlin 2.4 for the non-deprecated form; the
repo is already there.)

**Observable lifetime behavior change:** `val b = a.byValueReturn()` (and any by-value
getter/operator result) now lives in the **ambient** `MemScope` at the call, not in `a`'s
scope. In well-structured code (one `memScoped {}` enclosing both) behavior is identical;
in the pathological case the owner described it is now *correct*. The one regression-shaped
change is E2/E8: some consumer sites that compiled will need an explicit scope or will need
to resolve a nested-scope ambiguity.

---

## 7. Opportunities unlocked

- **Allocating operators become natural.** `Vec2.plus` today is
  `inline operator fun plus(o: Vec2): Vec2 { val r = memScope.Vec2_Holder(); … }` — it needs
  the receiver to carry a scope, which is exactly the lifetime footgun. With context params
  it is `context(scope: MemScope) operator fun Vec2.plus(o: Vec2): Vec2` and the result lands
  in the caller's arena. This directly enables the parked operator work (memory:
  `project_operator_decisions`) — `operator+`, and by extension a clean allocating
  `compareTo`/copy path — without threading scopes through wrapper fields.
- **Methods can move onto the type as context-scoped members / extensions** uniformly:
  every allocating member has the same `context(MemScope)` shape, so factories, by-value
  methods, and operators stop being three different scoping mechanisms (extension-receiver
  factory vs carried-field method vs carried-field operator) and become one.
- **Cleaner copy-construction.** A copy ctor / `clone`-style by-value return no longer has to
  decide "whose scope" — it is always the ambient one. This removes the special-casing
  around by-value returns and makes value semantics predictable.
- **Smaller wrappers / less state.** Dropping the field halves the wrapper's stored state
  (pointer only), removes a per-object `MemScope` reference (GC/retain pressure), and makes a
  wrapper a transparent handle over `ptr` — which also simplifies equality/`hashCode`
  reasoning (they already key on `ptr` only).

---

## 8. Recommended rule for the codegen

Drop the carried `memScope` field from **all** wrappers. Apply the context parameter by the
following decision rule at each emission site:

- **Allocates** (factory, `_Holder`, by-value return via ARG_CAST, allocating operator):
  emit `context(scope: MemScope)`; run `alloc`/`_Holder` on `scope`.
- **Registers cleanup** (`owned()`, factory construction-time `defer`): emit
  `context(scope: MemScope)`; run `scope.defer { dispose }`.
- **Borrowed / non-allocating** (element access, up/down-cast views, by-`&`/`*` returns,
  `dispose()` = delete-now, plain scalar/`ptr`-only methods): emit **no context parameter
  and no scope argument** — the wrapper and its results carry `ptr` only.

Single decision predicate the generator already has the information for: *"does this member
allocate a new C++ object or register a deferred delete?"* If yes → context param. If no →
scope-free. Every current binding classifies cleanly under this rule with **no wrapper
retaining the field**.

---

## Sources

- [Context parameters — Kotlin Documentation](https://kotlinlang.org/docs/context-parameters.html)
- [KEEP: context-parameters proposal (§7.5 resolution algorithm)](https://github.com/Kotlin/KEEP/blob/master/proposals/context-parameters.md)
