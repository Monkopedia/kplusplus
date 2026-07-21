import feature_tests.pr_char16_echo
import feature_tests.pr_char16_last_arg
import feature_tests.pr_char16_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// char16_t — UTF-16 code unit, maps to UShort.
class PrChar16Test {
    @BeforeTest fun reset() = pr_char16_reset()

    @Test fun round_trips_ascii_and_bmp() {
        // 0x41 = 'A', 0x0 = NUL, 0x20AC = '€'
        for (v in listOf(0x41u.toUShort(), 0u.toUShort(), 0x20ACu.toUShort())) {
            pr_char16_reset()
            assertEquals(v, pr_char16_echo(v))
            assertEquals(v, pr_char16_last_arg())
        }
    }
}
