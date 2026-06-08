# Family: C strings

For rows that pass or return C strings: `const char*`, mutable `char*` buffers,
and wide `const wchar_t*`. Pure cinterop — no generator needed. (Group B
`std::string`-family rows are a *different* family that needs the
generator-backed harness; do not put them here.)

Example rows: **ST-cstr-in** (`const char*` in), **ST-cstr-ret**
(`const char*` borrowed return), **ST-charbuf-out** (`char*` buffer fill),
**ST-wcstr** (wide).

## cinterop mapping cheat-sheet (verified on linuxX64)

| C type | cinterop Kotlin type | how to pass / read |
|---|---|---|
| `const char*` (param) | `String?` | pass the Kotlin `String` directly; cinterop UTF-8-converts. `null` for the null case. |
| `const char*` (return) | `CPointer<ByteVar>?` | `.toKString()` (UTF-8 decode); `.rawValue` to compare pointers |
| `char*` (out buffer) | `CValuesRef<ByteVar>?` | `memScoped { allocArray<ByteVar>(n) }`, pass it, `.toKString()` to read |
| `const wchar_t*` | `CValuesRef<IntVar>?` (4-byte!) | **`.wcstr` is 2-byte — wrong.** Build by hand: `allocArray<IntVar>(len+1)`, fill `arr[i] = ch.code`, NUL-terminate |
| `const wchar_t*` (return) | `CPointer<IntVar>?` | `.toKStringFromUtf32()` |

## Inspector pattern

C strings have no length on the C side, so the inspector captures what the C
code can actually see:

```cpp
#include "feature_tests.h"
#include <cstring>
namespace { const size_t CAP = 8192; char buf[CAP]; size_t len = 0; int wasNull = 0; }

extern "C" void <id>_take(const char* s) {
    if (!s) { wasNull = 1; len = 0; return; }
    wasNull = 0;
    len = std::strlen(s);                 // strlen IS the contract — embedded NUL truncates
    size_t n = len < CAP ? len : CAP - 1;
    std::memcpy(buf, s, n);
}
extern "C" size_t <id>_last_len(void) { return len; }
extern "C" int <id>_byte_at(size_t i) { return (i < len && i < CAP) ? (unsigned char)buf[i] : -1; }
extern "C" int <id>_was_null(void) { return wasNull; }
extern "C" void <id>_reset(void) { len = 0; wasNull = 0; }
```

## Scenarios to expand into (don't ship fewer)

- ASCII round-trip — inspector sees the bytes + correct length
- empty string
- a long string (e.g. 4 KB) — length exact, spot-check first/last byte
- UTF-8 multibyte **byte-exact**: 2-byte (`café`), 3-byte (`€`), 4-byte (`😀`),
  compared against `s.encodeToByteArray()`
- embedded NUL truncates (documents the `const char*` vs `std::string` gap)
- null pointer flagged (not crashed)
- out-of-range inspector read returns the sentinel (`-1`)
- for buffer-fill: NUL-termination, return count, **truncation without overrun**
  (allocate exactly `n` so any overrun corrupts), `n==0` writes nothing
- for wide: ASCII + a BMP code point on the take path (BMP avoids Kotlin
  surrogate pairs), and an astral code point on the *return* path (where
  `toKStringFromUtf32` recombines)

## Common pitfalls

- Don't reach for `.utf8`/`.cstr` on a `const char*` *param* — it's `String?`,
  pass the string. Those helpers are for `CValuesRef`/`CPointer` params.
- `buf[i] = intValue` needs `intValue.toByte()` and `import kotlinx.cinterop.set`.
- `.wcstr` is 2-byte (Windows wchar_t); Linux/macOS `wchar_t` is 4-byte —
  build the `IntVar` array by hand.
