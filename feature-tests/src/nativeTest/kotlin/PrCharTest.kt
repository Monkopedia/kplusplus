import feature_tests.pr_char_echo
import feature_tests.pr_char_last_arg
import feature_tests.pr_char_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrCharTest {
    @BeforeTest fun reset() = pr_char_reset()

    @Test fun round_trips_values() {
        for (v in listOf('A'.code.toByte(), 0.toByte(), 0x7F.toByte())) {
            pr_char_reset()
            assertEquals(v, pr_char_echo(v))
            assertEquals(v, pr_char_last_arg())
        }
    }
}
