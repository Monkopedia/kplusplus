import feature_tests.pr_short_echo
import feature_tests.pr_short_last_arg
import feature_tests.pr_short_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrShortTest {
    @BeforeTest fun reset() = pr_short_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0.toShort(), (-1).toShort(), Short.MIN_VALUE, Short.MAX_VALUE)) {
            pr_short_reset()
            assertEquals(v, pr_short_echo(v))
            assertEquals(v, pr_short_last_arg())
        }
    }
}
