import feature_tests.pr_float_echo
import feature_tests.pr_float_last_arg
import feature_tests.pr_float_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrFloatTest {
    @BeforeTest fun reset() = pr_float_reset()

    @Test fun round_trips_finite_values() {
        for (v in listOf(0.0f, -1.5f, 3.14f)) {
            pr_float_reset()
            assertEquals(v, pr_float_echo(v))
            assertEquals(v, pr_float_last_arg())
        }
    }

    @Test fun nan_survives() {
        assertTrue(pr_float_echo(Float.NaN).isNaN())
        assertTrue(pr_float_last_arg().isNaN())
    }

    @Test fun infinities_survive() {
        assertEquals(Float.POSITIVE_INFINITY, pr_float_echo(Float.POSITIVE_INFINITY))
        assertEquals(Float.NEGATIVE_INFINITY, pr_float_echo(Float.NEGATIVE_INFINITY))
        assertEquals(Float.NEGATIVE_INFINITY, pr_float_last_arg())
    }
}
