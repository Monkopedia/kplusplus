---
name: scaffold-feature-row
description: This skill should be used when the user asks to "scaffold row PR-XYZ", "add row XYZ", "scaffold feature-test row", "land row XYZ", or otherwise refers to materializing a signed-off row from docs/v2-features/matrix.md into actual test files under :feature-tests. Drives the C++ surface + Kotlin tests + status update workflow.
version: 0.1.0
---

# scaffold-feature-row

Translates a signed-off row in `docs/v2-features/matrix.md` into real test
artifacts under `:feature-tests` and updates the row's status when the test
runs.

## When this skill applies

- User says "scaffold row PR-XYZ" / "land row PR-XYZ" / "add row XYZ".
- User signs off on a new row in `matrix.md` and asks to materialize it.
- User asks to "run the matrix" or "catch the harness up to the matrix" —
  in which case loop over every `⚪`-status row.

## Source of truth

- **Rows:** `docs/v2-features/matrix.md`. Status column owns the row state
  (`⚪` not scaffolded → `🟢` / `🟡` / `🔴`).
- **Tests:** `feature-tests/`. C++ in `src/cppMain/`, Kotlin in
  `src/nativeTest/kotlin/`. One `.cc` per row in `rows/`, one Kotlin test
  class per row.
- **Build wiring:** lives in `feature-tests/build.gradle.kts`. Adding a new
  row's `.cc` or `.kt` file requires no Gradle edits — the build picks them
  up automatically.

## Procedure (do these in order)

1. **Read the row** from `matrix.md`. Extract: id, C++ signature, test-case
   bullets, notes.
2. **Pick the family.** Match the C++ signature shape against the family
   templates in `families/`:
   - Value arg + value return → `families/value-rt.md` (e.g. PR-bool, PR-int-rt)
   - Out-by-ptr / out-by-ref → `families/out-by-ptr.md` (e.g. PR-int-out)
   - C strings (`const char*` / `char*` buffer / wide) → `families/c-strings.md`
     (e.g. ST-cstr-in, ST-charbuf-out, ST-wcstr)
   - *(more families get added here as new row shapes are signed off)*
   If no family matches yet, **stop and ask the user** which existing family
   it most resembles, or whether to author a new family file as part of this
   row's work. Authoring a new family file is part of landing the first row of
   a new shape — do it, don't shoehorn.
3. **Add the extern "C" declarations** for the row to
   `feature-tests/src/cppMain/include/feature_tests.h`, under a
   `// ---- <ID>: <one-line summary> ----` banner. Always include:
   - the function(s) under test, prefixed `<id_lower>_*` (the row id lowercased
     with `-`→`_`: `PR-int-rt` → `pr_int_rt_*`, `ST-cstr-in` → `st_cstr_in_*`)
   - inspector helpers needed to assert C++-side state from Kotlin
   - a `<id_lower>_reset(void)` for test isolation (unless the row is stateless)
4. **Add the impl file** at `feature-tests/src/cppMain/rows/<id_lower>.cc`.
   Inspector state lives in an anonymous namespace inside the file. Each
   row's state is private to its file — no cross-row sharing.
5. **Add the Kotlin test class** at
   `feature-tests/src/nativeTest/kotlin/<Class>.kt`. **Expand each matrix
   bullet into many concrete `@Test`s** — the matrix is the contract at review
   altitude; the test file is where comprehensiveness lives (one `const char*`
   row → ~10 assertions over ASCII/empty/long/UTF-8 2-3-4-byte/embedded-NUL/
   null/OOB). The user trusts this expansion and does not review it per-row.
   One `@Test` per *scenario*, not mechanically one per bullet — a scenario
   that asserts both the return value and the C++-side inspector keeps both
   assertions in the same test, and a set of equivalent boundary values may
   share one looping test. Split into separate `@Test`s when the scenarios are
   genuinely distinct (e.g. finite round-trip vs. NaN vs. ±Inf for floats), so
   a failure localizes. `@BeforeTest` calls `<id_lower>_reset()` when the row
   has one.
   - **Class name must derive from the row id**, because `matrixReport` maps
     them by a deterministic forward derivation: split the id on `-`, Title-case
     the first segment (the category code), PascalCase the rest, append `Test`.
     `PR-int-rt` → `PrIntRtTest`, `PR-uchar` → `PrUcharTest`,
     `ST-cstr-in` → `StCstrInTest`. A misnamed class is reported as drift.
6. **Run the build:** `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew
   :feature-tests:nativeTest`. Fix until green.
7. **Write status back to the matrix** — do NOT hand-edit the Status column.
   Run `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew
   :feature-tests:matrixReport`. It re-runs the tests, rolls each row up to
   🟢 (all pass) / 🔴 (any failure), rewrites the Status column, and logs
   drift (rows with no matching test class, and orphan test classes). A
   manual 🟡 (passes with a documented workaround) is preserved when tests
   are green — set 🟡 and its Notes by hand, the task won't clobber it.
8. **Commit** with subject `Land row <ID> — <status emoji>` (or a batch subject
   for a chunk) and a body summarizing the scenarios covered and any notes worth
   surfacing.

## Conventions baked in

- **Symbol prefixing:** every extern "C" symbol is `<id_lower>_<name>` (row id
  lowercased, `-`→`_`) to avoid collisions across rows in the shared static
  library.
- **C-mode bool:** rows that use C `bool` need `#include <stdbool.h>` reachable
  from `feature_tests.h` (already wired — leave it in place).
- **Inspector state:** anonymous-namespace globals in the row's `.cc`. Reset
  function clears them. Never share state across rows.
- **Aspirational "Desired Kotlin":** the matrix's Desired Kotlin column shows
  the consumer-aesthetic shape. Tests call the actual cinterop binding
  (`<id_lower>_*`) directly. If there's a gap, capture it in the Notes column,
  do not invent a hand-written facade just to make the test look pretty.
- **cinterop type surprises (seen so far):** `const char*` param → `String?`
  (auto UTF-8 convert); `char*` out-buffer → `CValuesRef<ByteVar>?`;
  `const wchar_t*` → `CValuesRef<IntVar>?` on Linux (4-byte) — `.wcstr` is
  2-byte so build the wide array by hand; `size_t`/`wchar_t`/`ptrdiff_t`
  arrive as `platform.posix.<name>` aliases.

## Verification

When done, the row is considered scaffolded if:
- `./gradlew :feature-tests:nativeTest` passes,
- the per-row `TEST-*.xml` exists under the name `matrixReport` derives from
  the row id (so the row maps cleanly, no drift logged),
- `./gradlew :feature-tests:matrixReport` set the row's status (no longer `⚪`)
  and reported no unexpected drift,
- the commit message names the row id.

## Reference files

- `families/value-rt.md` — value-in / value-return pattern
- `families/out-by-ptr.md` — out-parameter pattern
- `families/c-strings.md` — C strings (`const char*`, `char*` buffer, wide)
