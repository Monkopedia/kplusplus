import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Vec2

// OP-call: C++ `operator()` used to SIGABRT the whole sync (operator() isn't in
// ALL_OPERATORS and its parens weren't sanitized into a valid identifier). It now
// maps to the idiomatic Kotlin `operator fun invoke`, so a caller can write `v(s)`.
// Vec2::operator()(s) returns x*s + y*s.
class OpCallTest {
    @Test fun call_operator_maps_to_invoke() = memScoped {
        with(Vec2) {
            val v = Vec2__double_double(3.0, 4.0)
            assertEquals(14.0, v(2.0)) // 3*2 + 4*2
            assertEquals(0.0, v(0.0))
            assertEquals(-7.0, Vec2__double_double(3.0, 4.0)(-1.0))
        }
    }
}
