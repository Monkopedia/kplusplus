import feature_tests.pr_char32_echo
import feature_tests.pr_char32_last_arg
import feature_tests.pr_char32_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// char32_t — UTF-32 code point, maps to UInt.
class PrChar32Test {
    @BeforeTest fun reset() = pr_char32_reset()

    @Test fun round_trips_ascii_and_astral() {
        // 0x41 = 'A', 0x0 = NUL, 0x1F600 = '😀' (astral plane)
        for (v in listOf(0x41u, 0u, 0x1F600u)) {
            pr_char32_reset()
            assertEquals(v, pr_char32_echo(v))
            assertEquals(v, pr_char32_last_arg())
        }
    }
}
