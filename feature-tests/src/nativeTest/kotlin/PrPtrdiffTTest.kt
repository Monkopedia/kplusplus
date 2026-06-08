import feature_tests.pr_ptrdiff_t_echo
import feature_tests.pr_ptrdiff_t_last_arg
import feature_tests.pr_ptrdiff_t_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// ptrdiff_t arrives as platform.posix.ptrdiff_t (= Long on LP64).
class PrPtrdiffTTest {
    @BeforeTest fun reset() = pr_ptrdiff_t_reset()

    @Test fun round_trips_boundaries() {
        for (v in listOf(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            pr_ptrdiff_t_reset()
            assertEquals(v, pr_ptrdiff_t_echo(v))
            assertEquals(v, pr_ptrdiff_t_last_arg())
        }
    }
}
