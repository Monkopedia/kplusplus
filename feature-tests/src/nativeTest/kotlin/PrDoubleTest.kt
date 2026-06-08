import feature_tests.pr_double_echo
import feature_tests.pr_double_last_arg
import feature_tests.pr_double_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrDoubleTest {
    @BeforeTest fun reset() = pr_double_reset()

    @Test fun round_trips_finite_values() {
        for (v in listOf(0.0, -1.5, kotlin.math.PI)) {
            pr_double_reset()
            assertEquals(v, pr_double_echo(v))
            assertEquals(v, pr_double_last_arg())
        }
    }

    @Test fun nan_survives() {
        assertTrue(pr_double_echo(Double.NaN).isNaN())
        assertTrue(pr_double_last_arg().isNaN())
    }

    @Test fun infinities_survive() {
        assertEquals(Double.POSITIVE_INFINITY, pr_double_echo(Double.POSITIVE_INFINITY))
        assertEquals(Double.NEGATIVE_INFINITY, pr_double_echo(Double.NEGATIVE_INFINITY))
        assertEquals(Double.NEGATIVE_INFINITY, pr_double_last_arg())
    }
}
