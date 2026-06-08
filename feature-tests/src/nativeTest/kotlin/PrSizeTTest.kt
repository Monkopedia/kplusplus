import feature_tests.pr_size_t_echo
import feature_tests.pr_size_t_last_arg
import feature_tests.pr_size_t_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// size_t arrives through cinterop as platform.posix.size_t (= ULong on LP64).
class PrSizeTTest {
    @BeforeTest fun reset() = pr_size_t_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0uL, 9_000_000_000uL, ULong.MAX_VALUE)) {
            pr_size_t_reset()
            assertEquals(v, pr_size_t_echo(v))
            assertEquals(v, pr_size_t_last_arg())
        }
    }
}
