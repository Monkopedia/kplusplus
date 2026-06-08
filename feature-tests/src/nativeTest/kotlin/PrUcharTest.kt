import feature_tests.pr_uchar_echo
import feature_tests.pr_uchar_last_arg
import feature_tests.pr_uchar_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrUcharTest {
    @BeforeTest fun reset() = pr_uchar_reset()

    @Test fun round_trips_full_range() {
        for (v in listOf(0u.toUByte(), 128u.toUByte(), 255u.toUByte())) {
            pr_uchar_reset()
            assertEquals(v, pr_uchar_echo(v))
            assertEquals(v, pr_uchar_last_arg())
        }
    }
}
