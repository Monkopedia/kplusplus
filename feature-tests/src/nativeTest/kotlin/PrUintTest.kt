import feature_tests.pr_uint_echo
import feature_tests.pr_uint_last_arg
import feature_tests.pr_uint_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrUintTest {
    @BeforeTest fun reset() = pr_uint_reset()

    @Test fun round_trips_boundaries_incl_sign_bit() {
        for (v in listOf(0u, 0x80000000u, UInt.MAX_VALUE)) {
            pr_uint_reset()
            assertEquals(v, pr_uint_echo(v))
            assertEquals(v, pr_uint_last_arg())
        }
    }
}
