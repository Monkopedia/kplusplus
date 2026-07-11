import kotlinx.cinterop.memScoped
import root.Widget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UC-static-method / UC-operator-eq / UC-operator-plus: the static factory
// `make(count_)` returning a Widget by value (placement-new into a Holder), and
// the two operators. NOTE the operator mapping: `operator==` maps to a real
// Kotlin `equals` override (so `a == b` works) plus an always-generated
// `hashCode`. `operator+` maps to a real Kotlin `operator fun plus`, so `a + b`
// works.
class UcStaticOpTest {
    // UC-static-method: static make() returns a constructed Widget by value.
    @Test fun static_make_returns_widget() = memScoped {
        // make()/the ctor factories now take the ambient MemScope as a context parameter
        // (the memScoped receiver supplies it) rather than a MemScope receiver.
        val w = with(Widget) { make(7) }
        assertEquals(7, w.count)
        assertEquals(1.0, w.scale) // make() fixes scale to 1.0
    }

    // UC-operator-eq: equal widgets compare true, differing compare false.
    // Surfaces as idiomatic Kotlin `a == b` (equals override), with a paired
    // hashCode that agrees for equal instances.
    @Test fun equality_operator_compares_fields() = memScoped {
        with(Widget) {
            val a = Widget__int_double(3, 1.5)
            val b = Widget__int_double(3, 1.5)
            val c = Widget__int_double(3, 2.0) // differing scale
            val d = Widget__int_double(4, 1.5) // differing count
            assertTrue(a == b)
            assertFalse(a == c)
            assertFalse(a == d)
            // equal objects must hash equal
            assertEquals(a.hashCode(), b.hashCode())
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
