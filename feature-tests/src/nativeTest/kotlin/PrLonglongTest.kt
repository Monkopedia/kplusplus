import feature_tests.pr_longlong_echo
import feature_tests.pr_longlong_last_arg
import feature_tests.pr_longlong_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrLonglongTest {
    @BeforeTest fun reset() = pr_longlong_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            pr_longlong_reset()
            assertEquals(v, pr_longlong_echo(v))
            assertEquals(v, pr_longlong_last_arg())
        }
    }
}
