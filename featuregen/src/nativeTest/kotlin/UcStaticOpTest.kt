import kotlinx.cinterop.memScoped
import root.Widget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UC-static-method / UC-operator-eq / UC-operator-plus: the static factory
// `make(count_)` returning a Widget by value (placement-new into a Holder), and
// the two operators. NOTE the operator mapping: Widget's `operator==` is
// HAND-WRITTEN, so it can't be proven memberwise and maps to an IDENTITY Kotlin
// `equals` (backing-pointer equality; hashCode is ptr.hashCode()) with the C++
// value comparison exposed as `valueEquals` — NOT a delegating `==`. (A C++20
// `= default`-ed `operator==` would instead keep the delegating `equals`; see
// OpRelationalTest's VecEq.) `operator+` maps to a real Kotlin `operator fun
// plus`, so `a + b` works.
class UcStaticOpTest {
    // UC-static-method: static make() returns a constructed Widget by value.
    @Test fun static_make_returns_widget() = memScoped {
        // make()/the ctor factories now take the ambient MemScope as a context parameter
        // (the memScoped receiver supplies it) rather than a MemScope receiver.
        val w = with(Widget) { make(7) }
        assertEquals(7, w.count)
        assertEquals(1.0, w.scale) // make() fixes scale to 1.0
    }

    // UC-operator-eq: Widget's `operator==` is hand-written, so Kotlin `==` is IDENTITY
    // (distinct instances are never `==`), and the C++ field comparison is reachable via
    // `valueEquals`. hashCode is identity (ptr.hashCode()), so the contract still holds.
    @Test fun equality_operator_compares_fields() = memScoped {
        with(Widget) {
            val a = Widget__int_double(3, 1.5)
            val b = Widget__int_double(3, 1.5)
            val c = Widget__int_double(3, 2.0) // differing scale
            val d = Widget__int_double(4, 1.5) // differing count
            // Kotlin `==` is identity: same value, different instance ⇒ NOT equal.
            assertTrue(a == a)
            assertFalse(a == b)
            // The C++ value comparison stays reachable via valueEquals.
            assertTrue(a.valueEquals(b))
            assertFalse(a.valueEquals(c))
            assertFalse(a.valueEquals(d))
            // equals/hashCode contract: an object hashes equal to itself.
            assertEquals(a.hashCode(), a.hashCode())
        }
    }

    // UC-operator-plus: a + b sums both fields, operands unchanged, result independent.
    @Test fun plus_operator_combines_fields() = memScoped {
        with(Widget) {
            val a = Widget__int_double(3, 1.5)
            val b = Widget__int_double(4, 2.0)
            val sum = a + b
            assertEquals(7, sum.count)
            assertEquals(3.5, sum.scale)
            // operands unchanged
            assertEquals(3, a.count)
            assertEquals(1.5, a.scale)
            assertEquals(4, b.count)
            assertEquals(2.0, b.scale)
            // result is an independent object: mutating it leaves operands alone
            sum.count = 99
            assertEquals(3, a.count)
            assertEquals(4, b.count)
        }
    }
}
