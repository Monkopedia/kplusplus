import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Buffer
import root.DefaultArgs
import std.Initializer_list__Int

// DA-* : Default arguments & overloading.
//
// Empirically observed generation (inspect featuregen/krapped/src/root_DefaultArgs.kt
// and root_Buffer.kt):
//
//  - C++ default-arg VALUES whose default is a simple renderable literal now
//    surface as Kotlin default parameter values. `int add(int a, int b=10, int c=0)`
//    becomes `MemScope.add(a: Int, b: Int = 10, c: Int = 0)`, so `add(1)` == 11 and
//    `add(1, 2)` == 3 with the defaults supplied by Kotlin. `format(int s,
//    int width=0, char fill=' ')` becomes `format(s: Int, width: Int = 0,
//    fill: Byte = ' '.code.toByte())`. (DA-omit-default, DA-default-method → 🟢.)
//    A default that is NOT a simple literal (constructor call, named constant,
//    unhandled enum) falls back to a mandatory param — correctness over coverage.
//
//  - Overloads are disambiguated by an UNDERSCORE-PREFIX scheme (NOT the
//    `__<argtypes>` suffix the chunk predicted): the first claimant keeps the bare
//    name, each subsequent overload gets one extra leading `_`:
//        process(int)         -> process
//        process(int,int)     -> _process
//        process(double)      -> __process
//    (DA-overload-free / DA-overload-member → 🟢, but via prefix not suffix.)
//
//  - const/non-const pair `at(int)` / `at(int) const` (T1.5): the two wrappers
//    differ ONLY in this-constness (same name, same param signature), so they are
//    the read-only/mutable halves of one logical accessor — not distinct overloads.
//    The non-const one is DROPPED and only the `const` overload binds, under the
//    bare name `at` (no `_at` sibling). Read-only is the safe choice. (DA-const-
//    overload → 🟢: exactly one binding, reachable, calls through; the surface is a
//    raw byte-pointer ref, so value-reads are asserted via the sibling getAt().)
//
//  - std::initializer_list<int> is WRAPPED (std.Initializer_list__Int) but only
//    exposes a default (empty) ctor + size() — no way to populate elements. So
//    sumIlist over a constructible list is always 0. (DA-initlist → 🔴: binding
//    present but functionally inert / no element synthesis.)
//
//  - C variadic `sumN(int count, ...)` is wrapped with the `...` DROPPED:
//    `MemScope.sumN(count: Int)`. Extra args cannot be supplied; the body sees 0
//    variadic ints. (DA-variadic → 🔴: drops gracefully, build-clean, but no
//    variadic forwarding.)
class DaDefaultOverloadTest {

    // DA-default-arg: all three args supplied explicitly → straight through.
    @Test fun default_arg_all_supplied() = memScoped {
        with(DefaultArgs) {
            assertEquals(6, add(1, 2, 3))
            // inspector confirms all three values crossed the boundary
            assertEquals(1, recvA())
            assertEquals(2, recvB())
            assertEquals(3, recvC())
        }
    }

    // DA-omit-default: 🟢 — the C++ defaults (b=10, c=0) are emitted as Kotlin
    // default parameter values, so the caller can omit the trailing args and the
    // binding fills them in before forwarding to the fixed-arity C wrapper.
    @Test fun omit_default_uses_kotlin_defaults() = memScoped {
        with(DefaultArgs) {
            // add(1) -> 1 + 10 + 0
            assertEquals(11, add(1))
            assertEquals(1, recvA())
            assertEquals(10, recvB())
            assertEquals(0, recvC())
            // add(1, 2) -> 1 + 2 + 0
            assertEquals(3, add(1, 2))
            assertEquals(2, recvB())
            assertEquals(0, recvC())
        }
    }

    // DA-default-method: 🟢 — format(s, width=0, fill=' ') emits Kotlin defaults for
    // width (Int = 0) and fill (Byte = ' '.code.toByte()), so format(s) is callable.
    @Test fun default_method_omits_trailing_args() = memScoped {
        with(DefaultArgs) {
            // width<=0 path (default width 0): returns s.
            assertEquals(42, format(42))
            // width supplied, fill defaulted: width>0 path returns width.
            assertEquals(8, format(42, 8))
            // all supplied still works.
            assertEquals(5, format(5, 0, '*'.code.toByte()))
        }
    }

    // DA-overload-free / DA-overload-member: 🟢 (via underscore-prefix names).
    @Test fun overloads_each_callable_independently() = memScoped {
        with(DefaultArgs) {
            assertEquals(5, process(5)) // process(int): x*1
            assertEquals(7, _process(3, 4)) // process(int,int): x+y
            assertEquals(25, __process(2.5)) // process(double): x*10
        }
    }

    @Test fun overloads_do_not_shadow_each_other() = memScoped {
        with(DefaultArgs) {
            // distinct names → each resolves to its own logic, no collision
            assertEquals(9, process(9))
            assertEquals(9, _process(4, 5))
            assertEquals(90, __process(9.0))
        }
    }

    // DA-const-overload (T1.5): 🟢 — the const/non-const at() pair collapses to a
    // SINGLE binding (the const overload, under the bare name `at`); the non-const
    // duplicate is dropped, so there is no `_at` sibling. We confirm the kept binding
    // calls through (returns a non-null ref) and the buffer is readable via getAt().
    @Test fun const_overload_collapses_to_single_binding() = memScoped {
        with(Buffer) {
            val b = Buffer()
            // the kept (const) overload binds under the bare name and calls through.
            assertEquals(true, b.at(0) != null)
            assertEquals(true, b.at(1) != null)
            // underlying data readable: Buffer ctor seeds {'a','b','c','d'}
            assertEquals('a'.code, b.getAt(0))
            assertEquals('d'.code, b.getAt(3))
        }
    }

    // DA-initlist: 🔴 — std::initializer_list<int> wraps, but only an empty
    // default ctor + size() are exposed; no element population. A constructed
    // list is therefore empty and sumIlist returns 0.
    @Test fun initializer_list_is_inert_empty() = memScoped {
        val il = with(Initializer_list__Int) { Initializer_list__Int() }
        assertEquals(0uL, il.size())
        with(DefaultArgs) {
            assertEquals(0, sumIlist(il)) // no way to make it non-empty
        }
    }

    // DA-variadic: 🔴 — `...` dropped; sumN(count) sees zero variadic ints, so
    // the body sums nothing regardless of count. Binding is present & build-clean.
    @Test fun variadic_dropped_sees_no_extra_args() = memScoped {
        with(DefaultArgs) {
            // count=3 but no varargs forwardable → loop reads 3 garbage/zero ints.
            // We only assert the binding is callable and returns an Int without
            // crashing; the value is unspecified (no varargs were passed).
            val r = sumN(0) // count=0 → loop body never runs → deterministic 0
            assertEquals(0, r)
        }
    }
}
