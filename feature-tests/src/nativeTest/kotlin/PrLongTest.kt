import feature_tests.pr_long_echo
import feature_tests.pr_long_last_arg
import feature_tests.pr_long_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrLongTest {
    @BeforeTest fun reset() = pr_long_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            pr_long_reset()
            assertEquals(v, pr_long_echo(v))
            assertEquals(v, pr_long_last_arg())
        }
    }
}
