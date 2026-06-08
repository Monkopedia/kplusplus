import feature_tests.pr_int_rt_add_one
import feature_tests.pr_int_rt_last_arg
import feature_tests.pr_int_rt_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrIntRtTest {
    @BeforeTest fun reset() = pr_int_rt_reset()

    @Test fun arithmetic_round_trip() {
        assertEquals(8, pr_int_rt_add_one(7))
        assertEquals(0, pr_int_rt_add_one(-1))
        assertEquals(-99, pr_int_rt_add_one(-100))
    }

    @Test fun negative_round_trip_preserves_input() {
        pr_int_rt_add_one(-1234567)
        assertEquals(-1234567, pr_int_rt_last_arg())
    }

    @Test fun boundaries_preserve_input() {
        // INT_MAX + 1 is UB under signed overflow; only check the input
        // propagated to the inspector, not the return value.
        for (b in listOf(0, -1, 1, Int.MIN_VALUE, Int.MAX_VALUE)) {
            pr_int_rt_reset()
            pr_int_rt_add_one(b)
            assertEquals(b, pr_int_rt_last_arg(), "boundary $b failed to round-trip")
        }
    }
}
