# Family: value-in / value-return

For rows whose C++ signature is `T2 func(T1 x);` — a single value arg in,
a single value return out, no aliasing or out-parameters.

Example rows: **PR-bool** (`bool negate(bool b)`), **PR-int-rt**
(`int addOne(int x)`).

## extern "C" surface

In `feature_tests.h`:

```c
// ---- PR-<id>: <one-line summary> ----
<ret> pr_<id>_<funcname>(<arg>);
<ret> pr_<id>_last_arg(void);    // or last_arg_<n> if multi-arg
int   pr_<id>_call_count(void);  // optional but recommended
void  pr_<id>_reset(void);
```

## Impl pattern

In `rows/pr_<id>.cc`:

```cpp
#include "feature_tests.h"

namespace {
<arg-type> last_arg = <default>;
int call_count = 0;
}

extern "C" <ret> pr_<id>_<funcname>(<arg-type> x) {
    last_arg = x;
    ++call_count;
    return <expression that exercises the feature>;
}

extern "C" <arg-type> pr_<id>_last_arg(void)  { return last_arg; }
extern "C" int        pr_<id>_call_count(void) { return call_count; }
extern "C" void       pr_<id>_reset(void) { last_arg = <default>; call_count = 0; }
```

## Kotlin test pattern

In `nativeTest/kotlin/Pr<Id>Test.kt`:

```kotlin
class Pr<Id>Test {
    @BeforeTest fun reset() = pr_<id>_reset()

    @Test fun <case_1>() { /* return-value assertion */ }
    @Test fun <case_2>() { /* inspector-state assertion */ }
    @Test fun <case_3>() { /* loop or boundary assertion */ }
}
```

## Common pitfalls

- **Signed overflow UB**: `INT_MAX + 1` is UB. For boundary cases where the
  expression would overflow, assert only via the inspector (`last_arg`), not
  the return value.
- **bool in C-mode headers**: covered globally by `#include <stdbool.h>` in
  `feature_tests.h` — no per-row action needed.
