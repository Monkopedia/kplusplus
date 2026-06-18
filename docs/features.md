# kplusplus v2 — feature support matrix

Rolling table of C++ features and how they round-trip through the K/N boundary
under v2. Rows land in batches as we sign off on the shape, then get scaffolded
into `feature-tests/` by the scaffold-feature-row skill.

**Columns.** `Status` is set by the test harness, not by hand. `Test cases` is
the contract — each bullet becomes an `@Test` in `Cases.kt`.

| Status legend | |
|---|---|
| ⚪ | row signed off, scaffolding not yet authored |
| 🟢 | scaffolded and all cases pass |
| 🟡 | scaffolded; some cases pass with documented workaround |
| 🔴 | scaffolded; cases fail and need engineering |

## Primitives

| ID | Feature | C++ sig | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| PR-bool | `bool` round-trip | `bool negate(bool b);` | `assertFalse(negate(true))` | • call with `true` / `false`, assert return<br>• repeat in loop, assert C++ inspector counter advances | 🟢 | warm-up; cinterop handles bool natively (needs `#include <stdbool.h>` for C-mode header parse) |
| PR-int-rt | `int` arg + return | `int addOne(int x);` | `assertEq(8, addOne(7))` | • boundaries: 0, -1, 1, INT_MIN, INT_MAX<br>• negative round-trip<br>• Kotlin asserts C++ inspector `lastArg()` matches | 🟢 | reference primitive case; INT_MAX boundary only checks input propagation, not `+1` return (signed overflow UB) |
| PR-int-out | `int` out via ptr | `void produce(int* out);` | `val r = intRef(); produce(r); assertEq(42, r.value)` | • Kotlin asserts `r.value == 42` post-call<br>• inspector reports `producedCount()` incremented<br>• null-pointer path: Kotlin passes null, assert C++ inspector flagged it | 🟢 | first out-param case; tests use `memScoped { alloc<IntVar>() }` + `.ptr` directly (no `intRef()` facade yet) |
| PR-char | `char` round-trip | `char echo(char c);` | `assertEq('A'.code.toByte(), echo(...))` | • round-trip `'A'`, `0`, `0x7F`<br>• inspector `lastArg()` matches | 🟢 | signedness platform-dependent; treat as `Byte` |
| PR-uchar | `unsigned char` round-trip | `unsigned char echo(unsigned char c);` | `Byte`/`UByte` | • round-trip `0u`, `255u`, mid<br>• inspector matches | 🟢 | full 0–255 range, no sign confusion |
| PR-short | `short` round-trip | `short echo(short x);` | `Short` | • boundaries `0, -1, SHRT_MIN, SHRT_MAX`<br>• inspector matches | 🟢 |  |
| PR-ushort | `unsigned short` round-trip | `unsigned short echo(unsigned short x);` | `UShort` | • boundaries `0u, 65535u, mid`<br>• inspector matches | 🟢 |  |
| PR-uint | `unsigned int` round-trip | `unsigned int echo(unsigned int x);` | `UInt` | • boundaries `0u, UINT_MAX, mid`<br>• inspector matches | 🟢 | sign-bit-set value (`0xFFFFFFFFu`) survives |
| PR-long | `long` round-trip | `long echo(long x);` | `Long` (LP64) | • boundaries `0, -1, LONG_MIN, LONG_MAX`<br>• inspector matches | 🟢 | Linux/macOS x64 = 64-bit; Windows would be 32-bit (out of scope) |
| PR-ulong | `unsigned long` round-trip | `unsigned long echo(unsigned long x);` | `ULong` | • boundaries `0u, ULONG_MAX, mid`<br>• inspector matches | 🟢 |  |
| PR-longlong | `long long` round-trip | `long long echo(long long x);` | `Long` | • boundaries `0, -1, LLONG_MIN, LLONG_MAX`<br>• inspector matches | 🟢 |  |
| PR-float | `float` round-trip | `float echo(float x);` | `Float` | • round-trip `0f, -1.5f, 3.14f`<br>• special: `NaN`/`+Inf`/`-Inf`<br>• inspector matches | 🟢 | NaN asserted via `isNaN()`, not `==` |
| PR-double | `double` round-trip | `double echo(double x);` | `Double` | • round-trip `0.0, -1.5, PI`<br>• special: `NaN`/`+Inf`/`-Inf`<br>• inspector matches | 🟢 | same NaN caveat |
| PR-size-t | `size_t` round-trip | `size_t echo(size_t x);` | `platform.posix.size_t` (= `ULong` LP64) | • boundaries `0u, mid, ULONG_MAX`<br>• inspector matches | 🟢 | cinterop preserves the alias; krapper_gen collapses it to bare `ULong` (tracked, see Known issues) |
| PR-ptrdiff-t | `ptrdiff_t` round-trip | `ptrdiff_t echo(ptrdiff_t x);` | `platform.posix.ptrdiff_t` (= `Long`) | • boundaries `0, -1, LONG_MIN, LONG_MAX`<br>• inspector matches | 🟢 | signed counterpart of size_t |
| PR-intptr | `intptr_t` / `uintptr_t` round-trip | `intptr_t echo(intptr_t x);` | `platform.posix.intptr_t`/`uintptr_t` (= `Long`/`ULong`) | • intptr boundaries `0, -1, LONG_MIN, LONG_MAX`<br>• uintptr boundaries `0u, mid, ULONG_MAX`<br>• inspector matches | 🟢 | integer-that-holds-a-pointer; handle-style APIs |
| PR-wchar | `wchar_t` round-trip | `wchar_t echo(wchar_t x);` | `platform.posix.wchar_t` (= `Int`, 4B Linux) | • round-trip ASCII + astral `0x1F600`<br>• inspector matches | 🟢 | **Linux/macOS only**: 4-byte wchar_t. Windows is 2-byte (`UShort`) and the astral case would not fit — revisit when a Windows target is in scope |
| PR-char16 | `char16_t` round-trip | `char16_t echo(char16_t x);` | `UShort` | • round-trip ASCII + BMP `0x20AC €`<br>• inspector matches | 🟢 | UTF-16 code unit |
| PR-char32 | `char32_t` round-trip | `char32_t echo(char32_t x);` | `UInt` | • round-trip ASCII + astral `0x1F600 😀`<br>• inspector matches | 🟢 | UTF-32 code point |
| PR-char8 | `char8_t` round-trip | `char8_t echo(char8_t c);` | `UByte` | • round-trip `0u, 0xFFu` | ⚪ | **Deferred**: C++20-only; harness compiles at `-std=c++17` so it doesn't surface. Bump the standard to enable. No test yet (matrixReport will log it as unscaffolded). |

## Strings

Group A is pure cinterop (testable under the current harness). Group B needs a
**generator-backed harness mode** (krapper_gen + compiler plugin wired into
`:feature-tests`) — those rows are signed off in shape but not yet scaffolded,
and their "Desired Kotlin" is pinned down when that harness lands.

| ID | Feature | C++ sig | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| ST-cstr-in | `const char*` arg in | `void take(const char* s);` | `take("hello")` | • ASCII round-trip (inspector sees bytes + length)<br>• empty string<br>• UTF-8 2/3/4-byte byte-exact<br>• embedded NUL truncates (documents the limitation)<br>• null pointer flagged<br>• OOB byte read | 🟢 | Group A. cinterop maps `const char*` param to `String?` (UTF-8 convert) |
| ST-cstr-ret | `const char*` return (borrowed) | `const char* name();` | `name()?.toKString()` | • content decodes<br>• non-null<br>• pointer stable across calls<br>• content stable when repeated | 🟢 | Group A. borrowed static storage; caller never frees |
| ST-charbuf-out | fill caller buffer | `size_t fill(char* buf, size_t n);` | `allocArray<ByteVar>(n)` → `fill` → `toKString()` | • fills full payload + returns count<br>• NUL-terminated<br>• truncates without overrun<br>• `n==0` writes nothing<br>• payload-len helper | 🟢 | Group A. classic C buffer-fill size/truncation contract |
| ST-wcstr | wide C string | `const wchar_t* wname(); void wtake(const wchar_t*);` | `wname()?.toKStringFromUtf32()` | • ASCII round-trip<br>• BMP code point (€)<br>• empty<br>• OOB read<br>• returned astral (😀) decodes | 🟢 | Group A. 4-byte wchar_t → `CValuesRef<IntVar>?`; built by hand (`.wcstr` is 2-byte) |
| ST-string-in | `const std::string&` arg in | `void take(const std::string& s);` | `take(__Basic_string__Char("hi"))` | • construct from Kotlin String, pass by const ref<br>• inspector size + bytes<br>• empty / UTF-8 byte-exact / longer<br>• embedded NUL truncates at construction | 🟢 | Generator-backed (`:featuregen`). std::string built from Kotlin String via `MemScope.__Basic_string__Char(String?)`; no `size()` on the binding (filtered) so inspector reads size. Embedded NUL truncates because the String→std::string bridge goes through `const char*` |
| ST-string-rt | `std::string` full round-trip | `std::string echo(const std::string&);` | `echo(s).c_str()` | • echo round-trip via c_str()<br>• UTF-8 byte-exact<br>• empty<br>• constructed-empty reports empty | 🟢 | return-by-value now placement-news into the Holder (krapper_gen ARG_CAST fix); see Known issues |
| ST-string-ret | `std::string` return by value | `std::string produce();` | `produce().c_str()` | • content matches<br>• not empty<br>• stable across calls | 🟢 | ownership = scope-bounded (Holder freed with the MemScope); fixed by the same placement-new change |
| ST-stringview-in | `std::string_view` arg in | `void take(std::string_view sv);` | TBD | • content + length<br>• lifetime (view must not outlive source)<br>• empty view | 🔴 | C++ standard is now configurable (`kplusplus { cppStandard = "c++17" }`), which fixes the parse — but two blockers remain: krapper_gen doesn't wrap `string_view` as a type (the `take` method is silently dropped, like `wstring`), and bumping to c++17 separately drops `std::string`'s `const char*` constructor. See Known issues |
| ST-wstring | `std::wstring` round-trip | `std::wstring echo(const std::wstring&);` | TBD | • ASCII + non-ASCII round-trip<br>• size() matches | 🔴 | **`std::basic_string<wchar_t>` is not wrapped.** Methods taking/returning `std::wstring` are silently dropped from the generated binding (only the non-wstring members of WStringFeature survive) |

**Deferred (strings):** `std::u16string` / `std::u32string` — pair with the
deferred `char8_t` / Unicode-string work.

## Containers — std::vector (transparent `cppVector<T>()` flow)

The v2 headline: `cppVector<T>()` written transparently, the compiler plugin
detecting the instantiation, krapper_gen generating the binding. Tested in
`:featuregen` (plugin applied + sync). The facade entry points
(`cppVector`/`cppMap`) live in package `com.monkopedia.kplusplus` — the plugin
recognizes them there (was hardcoded to the slice demo's package; now neutral,
anticipating a runtime lib).

| ID | Feature | C++ surface | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| CV-construct | empty vector | `std::vector<int>` | `cppVector<Int>()` | • size()==0<br>• empty()==true | 🟢 |  |
| CV-push-size | push + size | `push_back`, `size` | `v.push_back(7); v.size()` | • push N → size()==N<br>• size grows one per push | 🟢 |  |
| CV-clear | empty out | `clear()` | `v.clear()` | • after clear size()==0, empty()==true | 🟢 |  |
| CV-elem-double | non-Int element | `std::vector<double>` | `cppVector<Double>()` | • construct/push/size | 🟢 | element-type generality |
| CV-index-get | element out by index | `operator[]` / `at` | `v[0uL]?.getPointer(this)?.pointed?.value` | • read back pushed values<br>• at() matches subscript | 🟢 | accessors recovered by the reference-typedef fix; return a pointer to the element, deref to read |
| CV-front-back | front / back | `front()`, `back()` | `v.front()?.getPointer(this)?.pointed?.value` | • front==first, back==last | 🟢 | recovered by the same fix |
| CV-iterate | traverse | indexed read over size() | `while (i < v.size()) { v[i]... }` | • sum + count over elements | 🟢 | index traversal via size() + subscript |
| CV-nested | vector of vector | `std::vector<std::vector<int>>` | `cppVector<Vector__Int>()` | • push inner vectors (built via `cppVector<Int>()`)<br>• outer size reflects<br>• read inner vectors back (`outer[i]` → inner `size()`/subscript) | 🟢 | transitive instantiation (inner generated before outer). `outer[i]` wraps the inner vector's own pointer as a `Vector__Int`, so its `size()`/subscript read the inner ints — read-back verified (`read_inner_vectors_back`) |
| CV-elem-string | vector of std::string | `std::vector<std::string>` | `cppVector<Basic_string__Char>()` | • push std::strings<br>• size reflects<br>• read strings back (`v[i]`/`at`/`front`/`back` → `c_str()`) | 🟢 | needed the forcing-TU header fix (Known issues); `v[i]` wraps the element's own pointer as a `Basic_string__Char`, so `c_str()` decodes the stored bytes — read-back verified (`read_strings_back_out`) |
| CV-elem-class | vector of a user struct | `std::vector<Point>` | `cppVector<Point>()` | • Point binding fields (x/y) round-trip<br>• push Points<br>• size reflects<br>• read elements back (`v[i]`/`at`/`front`/`back` → Point fields) | 🟢 | "user type in a container" via Point's `@CppBinding("Point")`. `operator[]`/`at`/`front`/`back` wrap the element's own pointer as a `Point`, so `v[i]?.x`/`?.y` read straight off the stored element — read-back now verified (`read_points_back_out`) |

## Containers — std::map (transparent `cppMap<K,V>()` flow)

Tested in `:featuregen`. Map's `operator[]` survives the resolver (returns
`mapped_type&` → `int&`), so subscript insert/read work. `size()`/`count()`/
`erase(key)` are now present too — they were dropped because their `size_type`
return is another dependent member typedef libclang left unexposed; the
`size_type` typedef fix recovers them (same family as the `reference` accessor
fix). The iterator-returning overloads (`find`, iterator-arg `erase`,
`insert` returning `pair<iterator,bool>`) remain dropped pending iterator
support.

| ID | Feature | C++ surface | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| CM-construct | empty map | `std::map<int,int>` | `cppMap<Int,Int>()` | • empty()==true | 🟢 | no `size()` on the binding, so construct asserted via empty() only |
| CM-insert | insert via subscript | `m[k] = v` | `m[k]?.getPointer(this)?.pointed?.value = v` | • subscript inserts; empty() flips false | 🟢 | `operator[]` default-inserts + returns a pointer to the mapped value; write through it |
| CM-get | read via subscript / at | `v = m[k]` | `m[k]?.getPointer(this)?.pointed?.value` | • read back inserted values<br>• at() matches subscript | 🟢 | pointer deref, same as vector elements |
| CM-clear | empty out | `clear()` | `m.clear()` | • clear → empty() true | 🟢 |  |
| CM-value-double | non-Int value | `std::map<int,double>` | `cppMap<Int,Double>()` | • insert/read double values | 🟢 | value-type generality |
| CM-size | element count | `size()` | `m.size()` | • size==distinct keys<br>• re-insert same key doesn't grow | 🟢 | recovered by the `size_type` typedef fix |
| CM-count | membership | `count(k)` | `m.count(k)` | • 1 present / 0 absent | 🟢 | recovered by the `size_type` typedef fix |
| CM-erase | remove a key | `erase(k)` | `m.erase(k)` | • erase by key shrinks map + returns count removed<br>• erase absent → 0 | 🟢 | `erase(key)` returns `size_type` → recovered. The iterator-arg `erase` overloads are still dropped (iterators unwrapped), but the by-key one is what consumers want |

## Product types — pair / tuple

| ID | Feature | C++ | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| CP-pair | `std::pair<A,B>` | `cppPair<A,B>()` | `p.first` / `p.second` | • first/second round-trip<br>• fields independent | 🟢 | `first`/`second` generate as `var` field accessors. pair's default ctor isn't wrapped, so the facade falls back to the `_Holder` factory (valid for trivially-constructible element types; flagged for non-trivial). Added `cppPair` to the plugin facade registry |
| CP-tuple | `std::tuple<...>` | `cppTuple<...>()` | — | — | 🔴 | no binding generated at all — `std::get<N>` uses non-type template params + free functions krapper doesn't wrap. Deferred |

## Containers — set / unordered

Now scaffolded with tests and wired into the plugin registry (`cppSet`/
`cppUnorderedMap`/`cppUnorderedSet`). The lookup/membership surface works across
all three; only real *iteration* (`find`/`begin`/`end`, range-for) is still 🔴 —
it needs nested iterator-class wrapping (a genuinely deep, separate feature).

| ID | Feature | C++ | Desired Kotlin | Status | Notes |
|---|---|---|---|---|---|
| CS-set | `std::set<T>` | `cppSet<T>()` | `insert`/`count`/`size`/`erase`/`clear` | 🟢 | usable: `insert(7): Boolean` (was-newly-inserted), `count` (membership), `size`, `erase`, `clear`. value_type reducer (set branch of assocTypedefElement) + a `pair<iterator,bool>`→`bool` rewrite (returnsPairSecond flag → `.second`). `find`/iterator-returning members dropped (iteration is a deep separate feature). `CsSetTest` |
| CU-map | `std::unordered_map<K,V>` | `cppUnorderedMap<K,V>()` | construct, `at`/`operator[]`/`count`/`erase`, `size`/`clear`/… | 🟢 | `std::map` parity for the lookup surface: a `mappedTypedefElement` reducer reconstructs unordered's collapsed `key_type`→param0 / `mapped_type`→param1 (libclang reports them as unresolvable `typename _Hashtable::…` since unordered has no base of its own), so `at`/`operator[]`/`count`/`erase` resolve. Earlier crash (uncompilable `initializer_list` ctor) also fixed. `find`/`insert`/`begin`/`end` still dropped (need iterator wrapping + the real `pair` value_type). `CuUnorderedMapTest` |
| CU-set | `std::unordered_set<T>` | `cppUnorderedSet<T>()` | `insert`/`count`/`size`/`erase`/`clear` | 🟢 | usable like `std::set` — the set-style value_type reducer + `pair<iterator,bool>`→`bool` rewrite landed for `std::set` also unblocked unordered_set (same shape). `insert(7): Boolean`, `count`, `erase`. `find`/iteration still 🔴. `CuUnorderedSetTest` |

## Smart pointers (unique_ptr partial 🟢; shared_ptr deeper 🔴)

Probed by direct instantiation; not scaffolded. The typedef-reducer pattern that
unblocked container accessors/size doesn't reach these — smart pointers route
their member types through impl-class indirection that needs template-argument
substitution, not just sibling-typedef reduction.

| ID | Feature | C++ | Desired Kotlin | Status | Notes |
|---|---|---|---|---|---|
| SP-unique | `std::unique_ptr<T>` | `cppUniquePtr<T>()` | `get`/`release`/`pointer_reference` (operator->) | 🟢 partial | `get`/`release`/`operator->` + the pointer-taking `reset` now resolve: a `pointerTypedefElement` reducer maps the `pointer` C++11 alias → `_Tp*` via sibling `element_type`, then the existing `_Tp`→concrete engine substitutes. `operator*` (a dependent `add_lvalue_reference<element_type>::type` expr) and constructing a non-null `unique_ptr` from Kotlin (the `unique_ptr(pointer)` ctor) are follow-ups. `SpUniquePtrTest` |
| SP-shared | `std::shared_ptr<T>` | `cppSharedPtr<T>()` | — | 🔴 | generates **nothing** — base-class resolution fails: `shared_ptr` → `__shared_ptr<_Tp,_Lp>` → `__shared_ptr_access<...>` carry an unsubstituted `_Tp` plus an `unresolveable` `_Lp` lock-policy (a non-type/enum template param). Needs base-class template-arg substitution **and** non-type-param handling — strictly deeper than unique_ptr. Same base-class gap blocks the unordered accessors |
| SP-weak | `std::weak_ptr<T>` | `cppWeakPtr<T>()` | — | 🔴 | not probed; expected to share shared_ptr's blockers |

## Enums (`enum` / `enum class` → real Kotlin `enum class`)

An enum-typed param/return surfaces as a **synthesized Kotlin `enum class`** with
the original named constants. krapper represents the C++ enum as a
`WrappedEnumType` carrying the real spelling, its underlying integer, AND its
constants (name + value). The Kotlin binding emits
`enum class Color(val value: Int) { Red(0), Green(1), Blue(2); companion object {
fun fromValue(v: Int): Color = entries.first { it.value == v } } }` and methods
surface as `Color → Color`. The C/C++ boundary is unchanged (the wrapper's C
signature stays the underlying integer; the generated C++ casts `(Color)c` /
`(int)call`); the Kotlin edge converts `arg.value` in and `Color.fromValue(int)`
out. Tested in `:featuregen`.

| ID | Feature | C++ | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| EN-unscoped | unscoped `enum E` arg/return | `enum Direction { North, East, South, West }` | `enum class Direction(val value: UInt)` | • `Direction.fromValue`/`.value`<br>• `turnRight` cycles N→E→S→W→N<br>• `toInt`/`fromInt` round-trip | 🟢 | Unsigned underlying → `value: UInt`. Even an unscoped enum needs the boundary cast (C++ won't implicitly convert integer→enum for an *argument*) |
| EN-scoped | scoped `enum class E : int` arg/return | `enum class Color : int { Red, Green, Blue }` | `enum class Color(val value: Int)` | • `Color.Red.value == 0`, `Color.fromValue(1) == Green`<br>• `next` cycles Red→Green→Blue→Red<br>• `toInt`/`fromInt` round-trip | 🟢 | Surfaces as a real Kotlin enum class with named constants; `value` is the `: int` underlying |

**Implementation.** `WrappedEnumType` (toString = enum spelling, `cType`/
`kotlinType` = underlying integer, `isEnum = true`); `createForType` builds it for
`CXCursor_EnumDecl` from `clang_getEnumDeclIntegerType`. Enum args resolve with
`castMode = RAW_CAST` (emits `(E)(x)`) and `needsDereference = false`; enum returns
use a new `ReturnStyle.ENUM_RETURN` that casts the result to the integer. `isEnum`
propagates through const/reference wrappers (a `const E` / `const E&` is still an
enum value; a pointer/array to one is not). `canResolve` short-circuits enums true.
Surfacing previously-dropped enum operators also required sanitizing the bitwise
operator symbols (`& | ^ ~`) in `NameHandler.cleanupName` — V8's free flag-enum
operators (`std::operator&(E,E)`) now generate valid C names.

**Kotlin enum-class synthesis (the richer surface).** `WrappedEnumType` also carries
the enum's constants (recovered from the `CXCursor_EnumConstantDecl` children via
`clang_getEnumConstantDeclValue`). When present, `WrappedKotlinType` maps the enum to
an `EnumKotlinType` (delegating its name/pkg to a same-named wrapper type, carrying
the constants), `ResolvedKotlinType` gains `enumEntries`/`enumUnderlying`, and
`KotlinWriter.generateEnums` emits one `enum class` file per distinct enum; `reference`
passes `arg.value` and `generateReturn` wraps results in `fromValue(...)`. The C/C++
side is byte-identical to the integer-reduction version. Deferred: enum-typed *fields*
(handled but untested — no enum field in the probe), enum pointers/references, and
anonymous/duplicate-value enums (`fromValue` picks the first matching constant).

## User-defined class features

A plain (non-template) user class `Widget` exercised through `:featuregen` — the
systematic version of what `Point`/`StringFeature`/`EnumFeature` showed piecemeal.
All rows 🟢 (scaffolded by an orchestrated subagent; `UcConstructTest`,
`UcFieldMethodTest`, `UcStaticOpTest`).

| ID | Feature | C++ surface | Desired Kotlin | Test cases | Status | Notes |
|---|---|---|---|---|---|---|
| UC-ctor-default | default construction | `Widget()` | `with(Widget){ ms.Widget() }` | • construct; default fields (count==0, scale==0.0) | 🟢 | |
| UC-ctor-args | construct with args | `Widget(int, double)` | `ms.Widget__int_double(3, 1.5)` | • fields reflect args; int+double both round-trip | 🟢 | content-based factory name (overload-naming fix) |
| UC-ctor-overload | overload selection | default + (int,double) | both factories reachable | • both ctors callable from one binding | 🟢 | no brittle `_`-prefix — distinct names |
| UC-field-rw | public field read/write | `int count; double scale;` | `w.count` / `w.count = n` | • read both; write then read back; independent | 🟢 | `var` accessors |
| UC-method-void | void mutator | `void reset()` | `w.reset()` | • mutates state (fields zeroed), observable after | 🟢 | |
| UC-method-ret | value-returning method | `int compute(int) const` | `w.compute(5)` | • return reflects state+arg; arg round-trips | 🟢 | |
| UC-const-method | const method | `double scaled() const` | `w.scaled()` | • callable; does not mutate | 🟢 | const `this` wrapped |
| UC-static-method | static factory | `static Widget make(int)` | `with(Widget){ ms.make(7) }` | • returns a constructed Widget by value; fields correct | 🟢 | return-by-value (Holder placement-new) |
| UC-operator-eq | equality operator | `bool operator==(const Widget&) const` | `a eq b` | • equal→true, differing→false | 🟢 | **maps to `infix fun eq`, NOT Kotlin `==`** — asymmetric with `operator+` |
| UC-operator-plus | arithmetic operator (by value) | `Widget operator+(const Widget&) const` | `a + b` | • sum combines fields; operands unchanged; independent result | 🟢 | maps to a real `operator fun plus` |

**Note:** operator mapping is asymmetric — `operator+` becomes Kotlin `operator fun plus`
(`a + b` works), but `operator==` currently becomes `infix fun eq` (not `==`/`equals`).

**`operator==` → `equals` + `hashCode` (BUILT 🟢).** `operator==` now maps to a real
Kotlin `equals(other: Any?)` override (`other is T && T_op_eq(ptr, other.ptr)`), so
`==` works idiomatically — unconditionally (a conditional `equals`-vs-`eq` mapping was
rejected as confusing). An `equals`-bearing class always also gets a `hashCode()` that
folds its public fields (`return 0` when none) to honour the contract — a
poor-distribution hashCode is still contract-*correct* as it's derived from the same
state `==` compares (equal ⇒ equal hash). (`operator<` → `compareTo` is now built: a
single `operator<` synthesizes the whole ordering — `this<o → -1`, `o<this → 1`, else
`0` — and Kotlin derives `>`/`>=`/`<=`. See OP-compare.)

## Operators on user value types

A `Vec2` value type with overloaded operators, scaffolded into `:featuregen`
(`OpArithmeticTest`, `OpRelationalTest`, `OpAssignTest`). krapper maps most
operators (see `Operators.kt`); the gaps are `Vec2&`-returning compound ops and
`operator()`/conversion operators.

| ID | C++ | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| OP-eq | `operator==` | `override fun equals(other: Any?)` + `override fun hashCode()` | 🟢 | idiomatic `a == b`; `equals` does `other is T && T_op_eq(...)`; an always-generated `hashCode` folds the public fields (`return 0` if none) to honour the contract |
| OP-neq | `operator!=` | `infix fun neq(o): Boolean` | 🟢 | |
| OP-plus | `operator+` | `operator fun plus(o): Vec2` | 🟢 | real `a + b`; return-by-value via Holder |
| OP-minus | `operator-` (binary) | `operator fun minus(o): Vec2` | 🟢 | |
| OP-scale | `operator*(double)` | `operator fun times(scalar: Double): Vec2` | 🟢 | heterogeneous rhs resolves cleanly |
| OP-index | `operator[](int)` | `operator fun get(i: Int): Double` | 🟢 | real `v[i]`; rhs is `Int` (the C++ `int`), unlike vector's `ULong` |
| OP-compare | `operator<` | `operator fun compareTo(o): Int` (+ `: Comparable<Self>`) | 🟢 | maps to a real Kotlin `compareTo` synthesized from the single `<` (`this<o → -1`, `o<this → 1`, else `0`), so `a < b`/`a > b`/`sorted()` all work. GT/LTEQ/GTEQ stay infix (Kotlin derives them from compareTo). `OpRelationalTest` |
| OP-unary-minus | `operator-()` | `operator fun unaryMinus(): Vec2` | 🟢 | real prefix `-v` |
| OP-assign | `Vec2& operator=` | `infix fun assign(o): Vec2?` | 🟢 | in-place copy works AND the `Vec2&` self-reference surfaces as a non-owning `Vec2?` (same OB-return-ref path), so the result is usable and chains. `OpAssignTest` |
| OP-pluseq | `Vec2& operator+=` | `infix fun plusEquals(o): Vec2?` | 🟢 | mutation works AND returns the receiver wrapper (non-owning `Vec2?`), so `(a plusEquals b)!! plusEquals c` chains onto the same storage. `OpAssignTest` |
| OP-call | `operator()` | `operator fun invoke(s: Double): Double` | 🟢 | idiomatic — `v(s)`. A `CALL` operator (matches `operator()` of any arity) → `KotlinOperator("invoke")`. (Was a SIGABRT, briefly a `_call` fallback, now real `invoke`.) |
| OP-convert | `explicit operator double()` | — | 🔴 | conversion operators silently dropped (no `toDouble`); not in `ALL_OPERATORS` |

## Passing & returning user objects

A `Point2` aggregate + a `PassObj` struct of static methods exercising every
param/return passing convention (`ObArgTest`, `ObReturnTest`). **All 9 rows 🟢** —
better than predicted. Key mechanic: pointer/reference returns get a uniform
**non-owning** wrapper (`Type(rawptr, memScope)`, no Holder, no dispose), so they
neither double-free nor auto-free; only **by-value** returns get a `_Holder`
(placement-new). `T*` maps to a nullable Kotlin type end-to-end.

| ID | Convention | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| OB-byval-arg | by-value arg | `describePoint(p: Point2): Unit` | 🟢 | real copy-on-entry; C++ mutating its copy doesn't touch the caller's |
| OB-constref-arg | `const T&` arg | `sumCoords(p: Point2): Int` | 🟢 | |
| OB-ptr-arg | `T*` arg (mutating) | `shiftPoint(p: Point2?, …)` | 🟢 | nullable; in-place mutation visible + accumulates |
| OB-mutate-ref | `T&` out-param | `zeroPoint(p: Point2): Unit` | 🟢 | **no defensive copy** — write-back lands on the caller's struct |
| OB-return-byval | return by value | `makePoint(x,y): Point2` | 🟢 | placement-new into `Point2_Holder()` |
| OB-return-ptr-borrowed | borrowed `T*` return | `borrowGlobal(): Point2?` | 🟢 | non-owning wrapper, no dispose — safe |
| OB-return-ptr-owned | owned `T*` return | `allocPoint()?.owned()` or `.dispose()` | 🟢 | returns a non-owning wrapper (default = borrowed, safer); caller opts into destruction via the generated `.owned()` (defers `~T()` to scope end, returns `this`) or `.dispose()` (now). Surfaces the existing dispose C fn; virtual-aware. `ObOwnedTest` |
| OB-return-ref | `T&` return | `globalRef(): Point2?` | 🟢 | non-owning; **write-through the binding's setters mutates the underlying object** (predicted 🔴, actually works) |
| OB-null-return | nullable `T*` return | `maybePoint(give): Point2?` | 🟢 | `false`→null, `true`→non-null |

## Default arguments & overloading

A `DefaultArgs`/`Buffer` probe (`DaDefaultOverloadTest`). Overloads work; C++
default *values* are ABI-erased; variadic/`initializer_list` don't.

| ID | C++ | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| DA-default-arg | `add(int, int=10, int=0)` | `add(a, b, c): Int` | 🟢 | all params pass through |
| DA-omit-default | calling `add(1)` | `add(a, b: Int = 10, c: Int = 0)` | 🟢 | C++ default args surface as Kotlin default params — `add(1)`==11. Default value recovered from libclang (tokenize the default sub-expr); rendered per the param's Kotlin type |
| DA-default-method | `format(int, int=0, char=' ')` | `format(s, width: Int = 0, fill: Byte = ' '.code.toByte())` | 🟢 | renderable literal defaults (int/float/char/bool/`nullptr`→null) emit a Kotlin default; complex defaults (ctor calls, named consts, octal, bare-dot floats, non-Kotlin char escapes) fall back to a mandatory param. `DaDefaultOverloadTest` |
| DA-overload-free/member | `process(int)` / `(int,int)` / `(double)` | `process` / `process__int_int` / `process__double` | 🟢 | content-based, order-independent Kotlin names (overloadMethodName fix) |
| DA-const-overload | `at(int)` / `at(int) const` | `at` / `_at` → `CValuesRef<ByteVar>?` | 🟡 | both wrapped (no collision/drop — constness isn't in the suffix, so the 2nd just gets a prefix); raw byte-ref return, no const distinction in the Kotlin type |
| DA-variadic | `sumN(int, ...)` | `sumN(count): Int` | 🔴 | the `...` is silently dropped (no variadic forwarding) — builds clean but inert |
| DA-initlist | `sumIlist(std::initializer_list<int>)` | `sumIlist(vals: Initializer_list__Int)` | 🔴 | wrapped (not dropped like string_view) but **inert** — the `initializer_list` binding has only an empty ctor + `size()`, no way to populate it, so any list is empty |

## Namespaces & nested types

A multi-namespace probe (`NsSingleTest`/`NsNestedTest`/`NsNestedClassTest`/
`NsCollisionTest`). C++ namespaces map to Kotlin packages cleanly.

| ID | C++ | Generated mapping | Status | Notes |
|---|---|---|---|---|
| NS-single | `geo::Vec` | `package geo`, `import geo.Vec`, C symbols `geo_Vec_*` | 🟢 | ctors/fields/const method/static all work |
| NS-nested | `geo::detail::Impl` | `package geo.detail`, `import geo.detail.Impl` | 🟢 | two-level namespace |
| NS-nested-class | `Outer::Inner` | `package outer`, class `Inner`, `import outer.Inner`; `Outer` stays `root.Outer` | 🟢 | the outer class name becomes a package segment — so two `Outer::Inner` separate naturally (the review's "collide in root" prediction was wrong) |
| NS-collision | `acolor::Tag` / `bflavor::Tag` | distinct packages + distinct C prefixes | 🟢 | no link/runtime clash; same-file Kotlin use needs an import alias (`import bflavor.Tag as FlavorTag`) |
| NS-anon | anonymous `namespace { Hidden }` | (skipped — not bound) | 🟢 | members are now skipped (TU-internal linkage, not referenceable across a C ABI) instead of emitting an invalid empty `package ` line — `WrappedElement.map` returns null for an empty-spelling namespace |

## Inheritance & virtual dispatch

Single (one-base) public inheritance is **flattened**: a derived binding re-emits
its inherited public methods + fields so they're callable directly, with the
agreed naming rule (decision A). Probe `Base`/`Derived` (`IhFlattenTest`). The hard
polymorphism rows (base-pointer dispatch, upcast, multiple inheritance, abstract
suppression) are not built. Design decisions (naming + destruction/ownership) are in
the inheritance chunk + [[inheritance-decisions]] memory.

| ID | Feature | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| IH-base-method | inherited non-virtual method/field on a derived | `derived.move(d)`, `derived.bx`, `derived.baseOnly()` | 🟢 | flattened — calls the base's C wrapper on the derived `ptr` (single inheritance ⇒ offset 0, no this-adjust). Added an `isVirtual` flag to the model |
| IH-derived-method | derived-only method | `derived.derivedOnly()` | 🟢 | |
| IH-virtual-override | overridden virtual via the derived binding | `derived.area()` → `Derived_area` | 🟢 | the base's `area` copy is suppressed (one vtable slot); the override keeps the bare name and dispatches |
| IH-name-conflict | non-virtual method shadowing an inherited one | `derived.derivedLabel()` (own) / `derived.label()` (inherited) | 🟢 | the shadowing descendant yields the name → `<class><Method>`; the base-most decl keeps `label`. Intrinsic to the hierarchy (import-set-independent), virtual overrides exempt |
| IH-virtual-dispatch | runtime polymorphism through a base pointer | `areaOf(c: ShapeApi)` → Circle's `area()` | 🟢 | for a polymorphic base, generate `interface <Base>Api` (virtual surface); base + derived `implement` it (methods → `override`). Calling through the interface routes to the concrete wrapper's C call → the virtual C++ method vtable-dispatches. `IhUpcastTest` |
| IH-upcast | derived→base pointer | base-typed param/return → `<Base>Api?` | 🟢 | a base-class-typed param/return is remapped to the base interface, so a `Circle` (IS-A `ShapeApi`) passes where `Shape*` is wanted (upcast = identity at offset 0). This implicit path is the offset-0 common case; for non-zero offsets (2nd base, Case-2 single) use the explicit offset-correct `.asBase()` (IH-multi-inherit 🟢) |
| IH-abstract | suppress ctor factory for a pure-virtual class | `Button : DrawableApi`; no `Drawable()` factory | 🟢 | abstract class (pure-virtual member) drops its ctors (`WrappedClass.resolve`) so no construction factory is emitted, but it still gets a borrowable wrapper + its `<Name>Api` interface — usable polymorphically via a concrete subclass. A generated comment flags the absent factory. `IhAbstractTest` |
| IH-multi-inherit | multiple inheritance | `widget2.asTagged().tag()` | 🟢 | uniform `.asBase()` upcast per (transitive, public, non-virtual) superclass, backed by a synthetic C helper `D_as_B(p){ static_cast<B*>(reinterpret_cast<D*>(p)) }` — applies the correct base-subobject offset. Works for the 2nd base (non-zero offset) AND the Case-2 single-inheritance non-zero offset (a non-poly base under a vtable-adding derived). `IhCastTest`. Diamond/virtual bases skipped (out of scope). Flatten + implicit upcast stay as offset-0 sugar; `.asBase()` is the correct general path |
| IH-destruction | scope-bound auto-release (virtual-aware) + owned/borrowed | `defer { ~T() }`; `.owned()`/`.dispose()`; nv-dtor WARNING | 🟢 | (1) construction factories auto-release (`defer { ~T() }`, virtual-aware); (2) returned `T*` is borrowed by default — caller opts in via `.owned()`/`.dispose()` (`ObOwnedTest`); (3) a polymorphic class with no virtual dtor in its chain emits a generated WARNING (dtor virtuality threaded through the model, `IhNvDtorTest`) |

## Callbacks & function pointers

A method whose param/return type is a **C function-pointer typedef**
(`typedef int (*IntTransform)(int)`) used to be silently dropped — libclang gives it
as a typedef over a pointer-to-function-proto that fails `canResolve`, so the whole
method was filtered. Now captured as a `WrappedFunctionPointer` (an opaque NATIVE
pointer named by its typedef): the method survives, the Kotlin side surfaces the raw
cinterop `CPointer<CFunction<(args)->ret>>?`, the C side keeps the typedef name, and
the generated interop header re-declares the typedef (the name lives only in the user
header). Probe `CbProbe` (`CbReturnTest`). This is the **C→Kotlin** half (the
already-real C pointer, callable + re-passable) — the "separate, easy" direction. The
**Kotlin→C** direction is built too: a context-free callback takes a `staticCFunction`
directly (Mode 2), and a `void*`-context callback takes a plain capturing lambda via a
generated `StableRef` + trampoline (Mode 1, **synchronous**). Only the stored/async
callback (registration handle) remains, parked on a design decision. Full design in the
callbacks chunk + [[callback-decisions]].

| ID | Feature | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| CB-cfnptr-ret | method returns a C function-pointer typedef | `val fn = ms.makeDouble(); fn(21)` | 🟢 | returned pointer is directly invokable through cinterop, no `CFunction` leak |
| CB-cfnptr-arg | method takes a C function-pointer typedef | `ms.applyTransform(5, fn)` | 🟢 | a real C pointer (e.g. one returned from another binding) re-passes unchanged |
| CB-cfnptr-richsig | fn-pointer proto with enum/ref/class types in its signature | — | 🔴 | stage-1 guard: only native-scalar/void protos re-declare verbatim in the C header; richer ones (e.g. libstdc++ `event_callback`) fall through to the drop, to avoid a conflicting typedef redefinition |
| CB-lambda-mode1 | capturing Kotlin lambda → C callback w/ `void*` context (synchronous) | `ms.callWith({ n -> n + base }, 5)` | 🟢 | the binding boxes the lambda in a `StableRef`, forwards a non-capturing `staticCFunction` trampoline (recovers it from the `void*` ctx) + the StableRef ptr, captures the result, disposes, returns. Both the fn-pointer + `void*` args consumed. Strict detection (one fn-pointer w/ leading `void*` proto slot + one adjacent plain `void*`), gated to plain-value returns. Pure KotlinWriter codegen. `CbMode1Test` |
| CB-lambda-mode1-async | stored/async callback (outlives the call) | — | 🔴 | synchronous Mode-1 disposes the `StableRef` right after the call; a callback retained past the call needs a returned **registration handle** owning the StableRef (disposed on `unregister()`/scope exit). Parked on a design decision: emit the handle always, or only when the API provably stores the pointer |
| CB-lambda-mode2 | static lambda → context-free C callback | `ms.applyTransform(7, staticCFunction { n -> n+1 })` | 🟢 | falls out of stage 1: the param is the raw `CPointer<CFunction<…>>?`, so a caller passes `staticCFunction { … }`; the compiler rejects a *capturing* lambda (the type enforces the Mode-2 limitation honestly). `CbStaticLambdaTest` |
| CB-std-function | `std::function<…>` param | — | 🔴 | boxes state; reuses the Mode-1 trampoline once built |
| CB-memfnptr | pointer-to-member-function | — | 🔴 | no C ABI representation |

## User-defined class templates

A user C++ `template<class T> class Box {...}` is instantiable from Kotlin as the
natural **`Box<Int>()`** — the syntax settled with the user. The generator emits a
marker `interface Box<T>` (no constructor) + a same-named scoped factory
`@krapper.CppTemplate(...) fun <T> MemScope.Box(): Box<T>`, so `Box<Int>()` resolves to
the *function* (a named call, refinable), which the FIR plugin refines to the concrete
`Box__Int : Box<Int>` and the IR extension lowers to its companion factory — the stdlib
`List(n){}` pattern, NOT constructor refinement (construction stays a scoped MemScope
factory; the interface is the typed surface). Rode in on the now-package-aware plugin
(see [[namespace-root-config]]). Design + the enabler facts in [[templates-decisions]].

| ID | Feature | Generated Kotlin | Status | Notes |
|---|---|---|---|---|
| TPL-class | instantiate a user class template | `val b = Box<Int>(); b.set(7); b.get()` | 🟢 | `interface Box<T>` + `MemScope.Box()` facade → refined to `Box__Int : Box<Int>`. `UtBoxTest`. Bootstrap: the facade is itself generated, so the build seeds `instantiate("Box<int>")` (the compiler can't request it via SYNC_REQUIRED — chicken-and-egg) |
| TPL-methods | call instance methods on the instantiation | `b.set(7)`, `b.get()` | 🟢 | the refined call type is the *concrete* `Box__Int`, which carries the real (inline) methods, so they resolve even though the marker interface is empty |
| TPL-multi-arg | multi-type-param user template (`Pair2<A,B>`) | `Pair2<Int, Double>()` | 🟢 | worked out of the box — the facade machinery is arity-general (`T1, T2`; plugin derives arity from the facade's type params). `Pair2__Int__Double : Pair2<Int, Double>`. `UtPair2Test` |
| TPL-interface-methods | the generic `Box<T>` interface declaring methods | `fun readBox(b: Box<Int>): Int = b.get()` | 🟢 | `interface Box<T> { fun set(x: T); fun get(): T }`, concretes `override`. Inferred from the concrete instantiations (the raw template's T-typed methods don't survive resolution): a position is `T` only if it tracks the arg across every instantiation, else an identical concrete type, else the method is omitted. Needs ≥2 diverse instantiations to disambiguate. `UtBoxAbstractTest` |
| TPL-class-arg | template over a user/binding type (`Box<Point>`, `Box<Box<Int>>`) | `Box<Point>()`, `Box<Box__Int>()` | 🟢 | generator's `cppArgToKotlinType` derives the arg's Kotlin type from the generated binding (`Point` / mangled `Box__Int`); plugin already resolved `@CppBinding` element types (the cppVector<Vector__Int> path). Nested instantiation uses the concrete mangled name (`Box__Int`), like containers. `UtBoxPointTest` / `UtBoxNestedTest` |
| TPL-nontype-param | non-type template params (`Fixed<N>`) | — | 🔴 | no Kotlin reified equivalent (same blocker as CP-tuple) |
| TPL-free-fn | free function templates (`maxOf<Int>`) | — | 🔴 | `CXCursor_FunctionTemplate` — krapper doesn't wrap free functions |

## Out of scope

| Feature | Why |
|---|---|
| `long double` | x86-64 `long double` is 80-bit extended precision; Kotlin/Native has no 80-bit float, so cinterop can only refuse or silently truncate to `Double` (losing precision). Excluded rather than scaffolded — the generator should emit a clear "use `double`" diagnostic rather than a lossy binding. |

## Known issues

- **~~`operator()` crashes the generator~~ (FIXED — now maps to `invoke`).** A class
  with `operator()` used to abort the whole sync (exit 134): it wasn't in
  `ALL_OPERATORS`, fell through to the fallback namer whose parens weren't sanitized
  → invalid identifier → compile failure → SIGABRT. Now a `BasicCallOperator` (an
  any-arity matcher) + `ResolvedOperator.CALL` → `KotlinOperator("invoke")` generates
  a real `operator fun invoke`, so `v(s)` works (OP-call 🟢). The `()`→`_call`
  sanitization in `cleanupName`/`kotlinMethodName` remains as a defensive fallback.
- **Anonymous namespace emits invalid `package `.** A struct in an anonymous
  `namespace { }` generates a binding file with a literal empty `package `
  declaration (the empty namespace name), which fails Kotlin compilation
  (`Package name must be a '.'-separated identifier list`). It does NOT crash the
  generator (the `error("Namespace without name")` guard a review predicted is not
  hit). Fix: skip anonymous-namespace members in the parser (they're TU-internal
  anyway), or emit them into the root package. (Surfaced scaffolding namespaces.)
- **Namespace-scope free functions are not wrapped.** krapper_gen only wraps
  struct/class members; a global/namespace free function is silently dropped from
  the generated binding entirely (not in the `.kt`/`.h`/`.cc`). Probes expose their
  surface as `static` methods of a struct (as `StringFeature`/`PassObj` do) to work
  around it. If free-function support is expected, it's a gap. (Note: `std::`
  free functions DO appear via `*_Functions.kt`, so this may be specific to
  root/user-namespace functions — worth confirming.)
- **~~Overloaded regular *method* Kotlin names were order-based `_`-prefix~~ (FIXED
  for distinct-arg overloads).** The overload-naming fix now also covers
  non-constructor methods: `KotlinWriter.overloadMethodName` (mirroring
  `constructorFactoryName`) gives the first overload the bare name and each
  additional same-name method a `__<argtypes>` suffix (e.g. `append` /
  `append__const_char_P_size_t` / `append__size_t_char`), so distinct-arg overloads
  are order-independent. Zero test churn (existing tests use first-overload bare
  names). **Remaining:** const/non-const overloads have identical arg types, so their
  suffixes collide and they still fall through to the `_`-prefix uniquifier
  (`at` / `_at`) — encoding constness in the suffix is a smaller follow-up.
- **~~`operator==` → `infix fun eq`~~ (FIXED — now `equals` + `hashCode`).**
  `operator==` now generates a real Kotlin `equals` override + an always-present
  `hashCode` (folds public fields, `return 0` if none) — `a == b` works idiomatically
  and the equals/hashCode contract holds.
- **~~Forcing-TU missed nested element headers~~ (FIXED).** The v2 instantiation
  flow synthesizes a throwaway "forcing" header
  (`struct KrapperForce { std::vector<std::string> value; };`) to make libclang
  instantiate a requested template. It only `#include`d the outer base's std
  header (`<vector>`), so `std::vector<std::string>` failed to parse
  (`basic_string` undeclared) and `std::vector<Point>` would miss the user type.
  Fixed in `IndexedServiceImpl.requestInstantiation` to include a bundle of
  common std headers plus the consumer's own headers. Unblocked CV-elem-string
  and CV-elem-class. (v2-only code — not on main, so fixed directly here.)
- **~~`size_type`/`difference_type` member typedefs dropped methods~~ (FIXED).**
  Standard containers/strings define `size_type` (= `std::size_t`) and
  `difference_type` (= `std::ptrdiff_t`) via dependent trait expressions
  (`_Rep_type::size_type`, `__alloc_traits<…>::size_type`) that libclang leaves
  unexposed — so methods returning/taking them (`size`, `count`, `max_size`,
  `erase(key)`, `length`, `resize`, …) were silently dropped, exactly like the
  `reference` accessor case. Fixed in `WrappedType` with a `sizeTypedefElement`
  reducer mapping those typedefs to the concrete integral types. Recovered map
  `size`/`count`/`erase(key)` (CM-size/count/erase → 🟢) and std::string's
  `size`/`length`/`resize`. **Side effect:** recovering std::string's
  size_type-taking constructors reshuffled the `_`-prefix overload factory names
  (the const-char* ctor moved `__Basic_string__Char` → `_____Basic_string__Char`)
  — see the brittle-overload-naming note below. This fix lives on v2-templates;
  **should be backported to `main`** alongside the reference-typedef fix
  (`e38ff3f`), since both are in the shared resolver.
- **~~Brittle `_`-prefix overload factory naming~~ (FIXED).** Generated
  constructor/overload names were disambiguated by a leading-underscore count tied
  to overload *order* (`_`, `__`, `___`, …), so adding/removing any resolvable
  overload shifted every later name and silently broke call sites (seen with the
  c++17 bump and the size_type fix). Now both naming layers are content-based: the
  first method to claim a base name keeps it, and later overloads get a
  `__<argtypes>` suffix derived only from their own signature — order-independent.
  Layer 1 = C function names (`NameHandler.uniqueOverloadName`,
  e.g. `TestClass_new__int_double`); layer 2 = the Kotlin `MemScope.<Class>(...)`
  factory names (`KotlinWriter.constructorFactoryName`,
  e.g. `Point__int_int`, `Basic_string__Char__const_char_P`). Tradeoff: the names
  are longer/uglier but stable; a cleaner future option is to emit same-named
  Kotlin overloads where they aren't genuinely ambiguous. Updated the golden tests
  + featuregen call sites that pinned the old order-based names.
- **~~krapper_gen discards C typedef aliases~~ (FIXED for
  `size_t`/`ssize_t`/`ptrdiff_t`/`intptr_t`/`wchar_t`).** Generated bindings
  collapsed these to their bare underlying Kotlin type (e.g. `std::vector::size()`
  returned `ULong`, a `ptrdiff_t` param returned `Long`), whereas cinterop
  preserves the platform-correct `platform.posix.<name>` alias — so the same C++
  `size_t` surfaced as two different Kotlin types depending on binding path. Now
  these aliases are preserved end-to-end (with the `import platform.posix.<name>`),
  consistent across vector + map + direct params. Verified by `PrAliasTest`
  (`echoDiff`/`echoWide`) plus the container `size`/`count` etc. Root causes fixed:
  1. The `size_type`/`difference_type` member-typedef reducer emitted the bare
     integer; now emits `size_t`/`ptrdiff_t`, un-gated so it also covers containers
     whose typedef resolved on its own (vector).
  2. `fullyQualifiedType` unconditionally capitalized the last segment, mangling
     the lowercase typealias `platform.posix.size_t` into the undefined `Size_t`;
     now capitalizes only wrapper class names.
  3. A *direct* use of one of these typedefs was followed by libclang's `visit()`
     to its underlying integer; `cAliasTypedefElement` now captures the alias name
     before that happens.
  4. The aliases were added to the native list + `typeMap` + `pointerTypeMap`, and
     the generated header now includes `<stddef.h>` so `ptrdiff_t`/`wchar_t` are in
     scope in the C wrapper. (The `PR-size-t`/`PR-wchar` cinterop rows were 🟢
     regardless.) `char16_t`/`char32_t`/`char8_t` aliases not yet covered.
- **~~Return-by-value class types crash~~ (FIXED).** The generated C++ wrapper
  used to emit `*ret_value_cast = Foo::bar();` — copy/move *assignment* into the
  raw, unconstructed memory from `*_Holder()` (size/align alloc, no constructor),
  which is UB and crashed at runtime for non-trivial types. Fixed in krapper_gen
  `CppWriter` (ARG_CAST return style) to placement-new instead:
  `new (ret_value_cast) T(Foo::bar());`. This fix covers *every* by-value class
  return (std::string, and std::vector etc. when those rows land), not just the
  string rows. Validated by ST-string-rt / ST-string-ret going 🟢.
- **~~krapper_gen parses at `--std=c++14`~~ (config added; deeper blockers remain).**
  The standard is now configurable end-to-end: `kplusplus { cppStandard = "c++17" }`
  → krapper_gen `--std` flag → both the libclang parse (`IndexedServiceImpl`) and
  the wrapper compile (`CppCompiler`). That alone does NOT unblock
  `ST-stringview-in`, which surfaced two further krapper_gen gaps:
  1. `std::string_view` isn't wrapped as a bindable type — a method taking it is
     silently dropped from the facade (same failure mode as
     `std::basic_string<wchar_t>` below).
  2. Under c++17 the generated `std::string` binding *loses its `const char*`
     constructor* (the factory set changes when the string_view-convertible
     template ctor enters overload resolution), so the existing string rows can't
     even construct a std::string. The `_`-suffix overload disambiguation is
     standard-sensitive and brittle.
  Net: the standard knob is a necessary prerequisite that now exists, but
  `string_view` needs the type-wrapping fix and a more robust constructor-overload
  mapping before it can go 🟢.
- **`std::basic_string<wchar_t>` is not wrapped (`ST-wstring`).** Only
  `basic_string<char>` gets a binding; methods that take/return `std::wstring`
  are silently filtered out of the generated facade. The silent drop is itself a
  problem — a wrapped function vanishing should at least warn.
- **~~Container element accessors are dropped~~ (FIXED).** `std::vector`'s
  `operator[]`, `at()`, `front()`, `back()` now generate (returning a pointer to
  the element). Root cause was libstdc++ defining vector's `reference` /
  `const_reference` as a dependent trait typedef (`__alloc_traits<…>::reference`)
  that reduced to an opaque template ref the resolver couldn't map, so the
  methods were silently dropped (the failure log was gated to the v8
  "CreateParams" debugFilter). Fixed in krapper_gen `WrappedType` by rebuilding
  those member typedefs as `value_type&` / `const value_type&`, routed through
  the existing `&(...)` pointer-return path; gated by a `WrappedTemplateType`
  check so associative containers (`std::map`, whose `value_type` is
  `pair<const K,V>`) don't regress. Logging is now env-gated
  (`KRAPPER_DEBUG_RESOLVE`) instead of hardcoded. Fixed on `main`
  (`e38ff3f`), cherry-picked here. Validated: CV-index-get/front-back/iterate
  🟢, slice + map unchanged, krapper_gen unit tests show only the 2 pre-existing
  failures. **Re-checked against string_view/wstring: does NOT recover them** —
  distinct root causes. `wstring` (`ST-wstring`) still drops because
  `std::basic_string<wchar_t>` isn't wrapped at all (no binding generated), and
  `string_view` (`ST-stringview-in`) needs c++17 *and* the view type isn't
  wrapped. The reference-typedef fix was specific to sequence-container element
  accessors returning the `reference` member typedef.

  <details><summary>Original diagnosis (for the record)</summary>
  - The accessors *are* parsed — cursor dump shows them public + `Available`,
    both const and non-const overloads, with distinct USRs (so it is **not** a
    name/USR cache collision).
  - They are dropped during **resolution** — `WrappedMethod.resolve` returns
    null. The failure is **silent**: `ResolveContext.notifyFailed` only logs
    when a `debugFilter` matches (currently hardcoded to "CreateParams"), so
    these failures never surface.
  - The discriminator is the **return type**: the survivors return a pointer
    (`data()` → `_Tp*`) or a directly-spelled element ref that resolves
    (`std::map::operator[]` → `mapped_type&` → `int&`, which *does* survive).
    The casualties return vector's **`reference` / `const_reference` member
    typedef**, which resolution can't map → null. So the bug is in resolving
    the `reference` member-typedef return (probably an unresolved dependent
    typedef), not in references per se.
  - Fix direction: (1) un-gate `notifyFailed` logging so dropped methods are
    visible; (2) resolve the `reference`/`const_reference` member typedefs to
    the underlying element ref. Likely also recovers string_view/wstring drops.
    Highest-value container fix; needs deliberate work in the resolver.
  </details>
