import kotlinx.cinterop.memScoped
import root.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals

// OP-plus / OP-minus / OP-scale / OP-unary-minus: arithmetic operators on the
// user-defined value type Vec2. All four map to REAL Kotlin `operator fun`s
// (plus/minus/times/unaryMinus), so Kotlin `+ - * -prefix` syntax works. Each
// returns a new Vec2 by value (placement-new into a Holder), so operands stay
// unchanged and the result is independent.
class OpArithmeticTest {

    // OP-plus: a + b sums both components; operands unchanged; result independent;
    // adding the zero-vector is identity.
    @Test fun plus_sums_components() = memScoped {
        val ms = this
        with(Vec2) {
            val a = ms.Vec2__double_double(1.0, 2.0)
            val b = ms.Vec2__double_double(3.0, 4.0)
            val c = a + b
            assertEquals(4.0, c.x)
            assertEquals(6.0, c.y)
            // operands unchanged
            assertEquals(1.0, a.x); assertEquals(2.0, a.y)
            assertEquals(3.0, b.x); assertEquals(4.0, b.y)
            // result is independent: mutating it leaves operands alone
            c.x = 99.0
            assertEquals(1.0, a.x)
            assertEquals(3.0, b.x)
            // adding zero-vector is identity
            val zero = ms.Vec2()
            val id = a + zero
            assertEquals(1.0, id.x); assertEquals(2.0, id.y)
        }
    }

    // OP-minus: a - b subtracts components; operands unchanged; v - v == zero.
    @Test fun minus_subtracts_components() = memScoped {
        val ms = this
        with(Vec2) {
            val a = ms.Vec2__double_double(5.0, 7.0)
            val b = ms.Vec2__double_double(2.0, 1.0)
            val d = a - b
            assertEquals(3.0, d.x)
            assertEquals(6.0, d.y)
            // operands unchanged
            assertEquals(5.0, a.x); assertEquals(7.0, a.y)
            // v - v yields the zero-vector
            val z = a - a
            assertEquals(0.0, z.x); assertEquals(0.0, z.y)
        }
    }

    // OP-scale: v * scalar scales each component; rhs is Double (heterogeneous);
    // *1.0 identity, *0.0 zero; operand unchanged.
    @Test fun times_scales_by_scalar() = memScoped {
        val ms = this
        with(Vec2) {
            val v = ms.Vec2__double_double(2.0, -3.0)
            val s = v * 2.0
            assertEquals(4.0, s.x)
            assertEquals(-6.0, s.y)
            // *1.0 is identity
            val same = v * 1.0
            assertEquals(2.0, same.x); assertEquals(-3.0, same.y)
            // *0.0 is the zero-vector. (Note -3.0 * 0.0 == -0.0 in IEEE-754, so
            // normalize signed zero by adding 0.0 before comparing.)
            val z = v * 0.0
            assertEquals(0.0, z.x + 0.0); assertEquals(0.0, z.y + 0.0)
            // operand unchanged
            assertEquals(2.0, v.x); assertEquals(-3.0, v.y)
        }
    }

    // OP-unary-minus: -v negates both components; original unchanged; double
    // negation is identity. Maps to Kotlin prefix `-` (operator fun unaryMinus).
    @Test fun unary_minus_negates() = memScoped {
        val ms = this
        with(Vec2) {
            val v = ms.Vec2__double_double(3.0, -4.0)
            val neg = -v
            assertEquals(-3.0, neg.x)
            assertEquals(4.0, neg.y)
            // original unchanged
            assertEquals(3.0, v.x); assertEquals(-4.0, v.y)
            // double negation is identity
            val back = -neg
            assertEquals(3.0, back.x); assertEquals(-4.0, back.y)
        }
    }
}
