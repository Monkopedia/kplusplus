import kotlinx.cinterop.memScoped
import root.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals

// OP-convert: a C++ user-defined conversion operator. Vec2 has
// `explicit operator double() const { return x*x + y*y; }` (its squared length),
// which the generator surfaces as an idiomatic Kotlin `toDouble()` named after the
// destination scalar type. The body forwards to a C wrapper that does `(double)*self`.
class OpConvertTest {

    // toDouble() returns the C++ conversion result (the squared length), leaving the
    // receiver unchanged.
    @Test fun to_double_returns_conversion() = memScoped {
        with(Vec2) {
            val v = Vec2__double_double(3.0, 4.0)
            // 3*3 + 4*4 == 25
            assertEquals(25.0, v.toDouble())
            // receiver unchanged
            assertEquals(3.0, v.x)
            assertEquals(4.0, v.y)

            // zero-vector converts to 0.0
            val zero = Vec2()
            assertEquals(0.0, zero.toDouble())

            // negative components square to positive
            val n = Vec2__double_double(-1.0, -2.0)
            assertEquals(5.0, n.toDouble())
        }
    }
}
