import feature_tests.pr_wchar_echo
import feature_tests.pr_wchar_last_arg
import feature_tests.pr_wchar_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// wchar_t is 4 bytes on Linux/macOS (platform.posix.wchar_t = Int); 2 bytes on
// Windows. These cases assume the 4-byte width; the >0xFFFF case would not fit
// a 2-byte wchar_t — revisit if/when a Windows target is in scope.
class PrWcharTest {
    @BeforeTest fun reset() = pr_wchar_reset()

    @Test fun round_trips_ascii_and_astral() {
        for (v in listOf('A'.code, 0, 0x1F600 /* 😀, needs 4-byte wchar_t */)) {
            pr_wchar_reset()
            assertEquals(v, pr_wchar_echo(v))
            assertEquals(v, pr_wchar_last_arg())
        }
    }
}
