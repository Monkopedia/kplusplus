import feature_tests.pr_ushort_echo
import feature_tests.pr_ushort_last_arg
import feature_tests.pr_ushort_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrUshortTest {
    @BeforeTest fun reset() = pr_ushort_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0u.toUShort(), 30000u.toUShort(), 65535u.toUShort())) {
            pr_ushort_reset()
            assertEquals(v, pr_ushort_echo(v))
            assertEquals(v, pr_ushort_last_arg())
        }
    }
}
