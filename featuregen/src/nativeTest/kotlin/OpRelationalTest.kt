import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import root.Vec2
import root.VecEq

// OP-eq / OP-neq / OP-compare / OP-index: comparison + subscript operators.
//
// NAMING NOTE: krapper's mapping of C++ operator== depends on whether it is a
// C++20 DEFAULTED (`= default`) comparison or HAND-WRITTEN:
//  * DEFAULTED == (VecEq) is guaranteed memberwise over all members, so it maps
//    to a real Kotlin `equals` override (idiomatic `a == b`) PAIRED with a
//    field-fold `hashCode` (equal ⇒ equal hash) — the contract is sound.
//  * HAND-WRITTEN == (Vec2) can't be proven memberwise, so binding it to Kotlin
//    `equals` would risk the equals/hashCode contract. It instead gets an
//    IDENTITY `equals` (backing-pointer equality; hashCode is ptr.hashCode()),
//    and the C++ value comparison stays reachable as `valueEquals(other)`.
// C++ operator< maps to a real Kotlin `operator fun compareTo(other): Int`,
// synthesized from the single `<` by calling the `_op_lt` C wrapper twice with
// swapped operands, so idiomatic `a < b`, `a > b`, `compareTo`, and `sorted()`
// all work. operator!= -> `neq` remains an INFIX METHOD (no Kotlin `!=`).
// operator[] DOES map to `operator fun get(i: Int): Double`, so real Kotlin
// `v[i]` subscript works (returns a primitive Double directly).
class OpRelationalTest {

    // OP-eq (hand-written ==): Vec2's `operator==` is hand-written, so Kotlin `==`
    // is IDENTITY on the backing pointer — two distinct-but-equal-valued instances
    // are NOT `==`, but ARE `valueEquals`. hashCode is identity (ptr.hashCode()), so
    // the equals/hashCode contract still holds: `a == a` ⇒ same hash.
    @Test fun eq_is_identity_for_handwritten() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val sameValue = Vec2__double_double(1.0, 2.0)
            val diff = Vec2__double_double(9.0, 2.0)
            // Kotlin `==` is identity: same value, different instance ⇒ NOT equal.
            assertTrue(a == a) // self-comparison (same backing pointer)
            assertFalse(a == sameValue)
            assertFalse(a == diff)
            // The C++ value comparison is still reachable via valueEquals.
            assertTrue(a.valueEquals(sameValue))
            assertFalse(a.valueEquals(diff))
            // equals/hashCode contract: an object hashes equal to itself.
            assertEquals(a.hashCode(), a.hashCode())
        }
    }

    // OP-eq-defaulted: VecEq's `operator== = default` IS memberwise over all members,
    // so Kotlin `==` runs the C++ comparison and a field-fold hashCode agrees — two
    // distinct-but-equal-valued instances ARE `==` and hash equal.
    @Test fun eq_defaulted_compares_components() = memScoped {
        with(VecEq) {
            val a = VecEq__double_double(1.0, 2.0)
            val b = VecEq__double_double(1.0, 2.0)
            val diffX = VecEq__double_double(9.0, 2.0)
            val diffY = VecEq__double_double(1.0, 9.0)
            assertTrue(a == b)
            assertTrue(a == a) // self-comparison
            assertFalse(a == diffX)
            assertFalse(a == diffY)
            // equals/hashCode contract: equal objects hash equal.
            assertEquals(a.hashCode(), b.hashCode())
        }
    }

    // OP-neq: differing instances return true; identical return false. `neq` is Vec2's
    // C++ `operator!=` (a VALUE comparison, infix method), so it is the inverse of the
    // VALUE comparison `valueEquals` — NOT of Kotlin's identity `==` (see eq test).
    @Test fun neq_is_inverse_of_valueEquals() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val same = Vec2__double_double(1.0, 2.0)
            val diff = Vec2__double_double(1.0, 3.0)
            assertTrue(a neq diff)
            assertFalse(a neq same)
            // consistency with the value comparison
            assertEquals(a.valueEquals(same), !(a neq same))
            assertEquals(a.valueEquals(diff), !(a neq diff))
        }
    }

    // OP-compare: lexicographic ordering via real Kotlin `compareTo` (so `<`, `>`,
    // and `sorted()` work). smaller x is less; equal x defers to y; reflexive
    // equal -> 0; antisymmetric. Synthesized from the single C++ `operator<`.
    @Test fun compareTo_orders_lexicographically() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 5.0)
            val biggerX = Vec2__double_double(2.0, 0.0)
            val sameXbiggerY = Vec2__double_double(1.0, 9.0)
            // smaller x is less regardless of y, via idiomatic `<` / `>`
            assertTrue(a < biggerX)
            assertFalse(biggerX < a)
            assertTrue(biggerX > a)
            // equal x defers to y
            assertTrue(a < sameXbiggerY)
            assertFalse(sameXbiggerY < a)
            // equal compares to 0 (reflexive)
            assertEquals(0, a.compareTo(Vec2__double_double(1.0, 5.0)))
            assertEquals(0, Vec2__double_double(1.0, 1.0).compareTo(Vec2__double_double(1.0, 1.0)))
            // compareTo sign matches the order
            assertTrue(a.compareTo(biggerX) < 0)
            assertTrue(biggerX.compareTo(a) > 0)
            // sorted() orders lexicographically (smaller x first, then smaller y)
            val sorted = listOf(
                Vec2__double_double(2.0, 0.0),
                Vec2__double_double(1.0, 9.0),
                Vec2__double_double(1.0, 1.0)
            ).sorted()
            assertEquals(1.0, sorted[0].x)
            assertEquals(1.0, sorted[0].y)
            assertEquals(1.0, sorted[1].x)
            assertEquals(9.0, sorted[1].y)
            assertEquals(2.0, sorted[2].x)
            assertEquals(0.0, sorted[2].y)
        }
    }

    // OP-index: v[0] returns x, v[1] returns y, matching construction values.
    // Real Kotlin subscript via operator fun get(Int): Double.
    @Test fun get_returns_component() = memScoped {
        with(Vec2) {
            val v = Vec2__double_double(7.5, -2.5)
            assertEquals(7.5, v[0])
            assertEquals(-2.5, v[1])
            // matches the fields set at construction
            assertEquals(v.x, v[0])
            assertEquals(v.y, v[1])
        }
    }
}
