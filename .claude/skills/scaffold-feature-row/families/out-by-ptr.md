# Family: out-by-pointer

For rows whose C++ signature is `void func(T* out);` — Kotlin allocates the
storage, passes a pointer, C++ writes into it. Includes the null-pointer path.

Example rows: **PR-int-out** (`void produce(int* out)`).

## extern "C" surface

In `feature_tests.h`:

```c
// ---- PR-<id>: <one-line summary> ----
void pr_<id>_<funcname>(<T>* out);
int  pr_<id>_produced_count(void);  // success counter
int  pr_<id>_null_flagged(void);    // null-pointer-path counter
void pr_<id>_reset(void);
```

## Impl pattern

```cpp
#include "feature_tests.h"

namespace {
int produced_count = 0;
int null_flagged = 0;
}

extern "C" void pr_<id>_<funcname>(<T>* out) {
    if (out == nullptr) { ++null_flagged; return; }
    *out = <produced value>;
    ++produced_count;
}

extern "C" int  pr_<id>_produced_count(void) { return produced_count; }
extern "C" int  pr_<id>_null_flagged(void)   { return null_flagged; }
extern "C" void pr_<id>_reset(void) { produced_count = 0; null_flagged = 0; }
```

## Kotlin test pattern

```kotlin
class Pr<Id>Test {
    @BeforeTest fun reset() = pr_<id>_reset()

    @Test fun <name>_writes_expected_value_to_out_param() {
        memScoped {
            val r = alloc<<T>Var>()
            pr_<id>_<funcname>(r.ptr)
            assertEquals(<expected>, r.value)
            assertEquals(1, pr_<id>_produced_count())
        }
    }

    @Test fun repeated_calls_increment_inspector_counter() {
        memScoped {
            val r = alloc<<T>Var>()
            repeat(N) { pr_<id>_<funcname>(r.ptr) }
            assertEquals(N, pr_<id>_produced_count())
        }
    }

    @Test fun null_pointer_path_is_flagged_not_crashed() {
        pr_<id>_<funcname>(null)
        assertEquals(1, pr_<id>_null_flagged())
        assertEquals(0, pr_<id>_produced_count())
    }
}
```

## Common pitfalls

- **`.ptr` requires MemScope**: the test body must be wrapped in
  `memScoped { … }`. `alloc<<T>Var>()` does too. The cinterop opt-in
  (`ExperimentalForeignApi`) is already enabled at the module level.
- **Null acceptance is feature-dependent**: not every out-param API accepts
  null. If the C++ contract is "non-null required, UB otherwise", drop the
  null-pointer case from the row (note in matrix Notes column) rather than
  testing UB.
