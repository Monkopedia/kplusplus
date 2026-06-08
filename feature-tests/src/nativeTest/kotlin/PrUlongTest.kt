import feature_tests.pr_ulong_echo
import feature_tests.pr_ulong_last_arg
import feature_tests.pr_ulong_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrUlongTest {
    @BeforeTest fun reset() = pr_ulong_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0uL, 9_000_000_000uL, ULong.MAX_VALUE)) {
            pr_ulong_reset()
            assertEquals(v, pr_ulong_echo(v))
            assertEquals(v, pr_ulong_last_arg())
        }
    }
}
