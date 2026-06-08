import kotlinx.cinterop.memScoped
import root.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// OP-eq / OP-neq / OP-compare / OP-index: comparison + subscript operators.
//
// NAMING NOTE: krapper maps C++ operator== to a real Kotlin `equals` override
// (so idiomatic `a == b` works), and ALWAYS pairs it with a `hashCode` override
// derived from the same public fields (equal ⇒ equal hash). C++ operator< maps
// to a real Kotlin `operator fun compareTo(other): Int`, synthesized from the
// single `<` by calling the `_op_lt` C wrapper twice with swapped operands, so
// idiomatic `a < b`, `a > b`, `compareTo`, and `sorted()` all work. operator!=
// -> `neq` remains an INFIX METHOD (no Kotlin `!=`). operator[] DOES map to
// `operator fun get(i: Int): Double`, so real Kotlin `v[i]` subscript works
// (returns a primitive Double directly).
class OpRelationalTest {

    // OP-eq: identical Vec2 compare true via `==`; differing in x or y compare
    // false; self-comparison true. Equal instances must share a hashCode.
    @Test fun eq_compares_components() = memScoped {
        val ms = this
        with(Vec2) {
            val a = ms.Vec2__double_double(1.0, 2.0)
            val b = ms.Vec2__double_double(1.0, 2.0)
            val diffX = ms.Vec2__double_double(9.0, 2.0)
            val diffY = ms.Vec2__double_double(1.0, 9.0)
            assertTrue(a == b)
            assertTrue(a == a) // self-comparison
            assertFalse(a == diffX)
            assertFalse(a == diffY)
            // equals/hashCode contract: equal objects hash equal.
            assertEquals(a.hashCode(), b.hashCode())
        }
    }

    // OP-neq: differing instances return true; identical return false; logically
    // consistent with `==` (neq == !equals).
    @Test fun neq_is_inverse_of_eq() = memScoped {
        val ms = this
        with(Vec2) {
            val a = ms.Vec2__double_double(1.0, 2.0)
            val same = ms.Vec2__double_double(1.0, 2.0)
            val diff = ms.Vec2__double_double(1.0, 3.0)
            assertTrue(a neq diff)
            assertFalse(a neq same)
            // consistency with ==
            assertEquals(a == same, !(a neq same))
            assertEquals(a == diff, !(a neq diff))
        }
    }

    // OP-compare: lexicographic ordering via real Kotlin `compareTo` (so `<`, `>`,
    // and `sorted()` work). smaller x is less; equal x defers to y; reflexive
    // equal -> 0; antisymmetric. Synthesized from the single C++ `operator<`.
    @Test fun compareTo_orders_lexicographically() = memScoped {
        val ms = this
        with(Vec2) {
            val a = ms.Vec2__double_double(1.0, 5.0)
            val biggerX = ms.Vec2__double_double(2.0, 0.0)
            val sameXbiggerY = ms.Vec2__double_double(1.0, 9.0)
            // smaller x is less regardless of y, via idiomatic `<` / `>`
            assertTrue(a < biggerX)
            assertFalse(biggerX < a)
            assertTrue(biggerX > a)
            // equal x defers to y
            assertTrue(a < sameXbiggerY)
            assertFalse(sameXbiggerY < a)
            // equal compares to 0 (reflexive)
            assertEquals(0, a.compareTo(ms.Vec2__double_double(1.0, 5.0)))
            assertEquals(0, ms.Vec2__double_double(1.0, 1.0).compareTo(ms.Vec2__double_double(1.0, 1.0)))
            // compareTo sign matches the order
            assertTrue(a.compareTo(biggerX) < 0)
            assertTrue(biggerX.compareTo(a) > 0)
            // sorted() orders lexicographically (smaller x first, then smaller y)
            val sorted = listOf(
                ms.Vec2__double_double(2.0, 0.0),
                ms.Vec2__double_double(1.0, 9.0),
                ms.Vec2__double_double(1.0, 1.0)
            ).sorted()
            assertEquals(1.0, sorted[0].x); assertEquals(1.0, sorted[0].y)
            assertEquals(1.0, sorted[1].x); assertEquals(9.0, sorted[1].y)
            assertEquals(2.0, sorted[2].x); assertEquals(0.0, sorted[2].y)
        }
    }

    // OP-index: v[0] returns x, v[1] returns y, matching construction values.
    // Real Kotlin subscript via operator fun get(Int): Double.
    @Test fun get_returns_component() = memScoped {
        val ms = this
        with(Vec2) {
            val v = ms.Vec2__double_double(7.5, -2.5)
            assertEquals(7.5, v[0])
            assertEquals(-2.5, v[1])
            // matches the fields set at construction
            assertEquals(v.x, v[0])
            assertEquals(v.y, v[1])
        }
    }
}
