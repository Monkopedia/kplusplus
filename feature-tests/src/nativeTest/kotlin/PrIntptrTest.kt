import feature_tests.pr_intptr_echo
import feature_tests.pr_intptr_last_arg
import feature_tests.pr_intptr_reset
import feature_tests.pr_uintptr_echo
import feature_tests.pr_uintptr_last_arg
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// intptr_t/uintptr_t — integer-that-holds-a-pointer; Long / ULong on LP64.
class PrIntptrTest {
    @BeforeTest fun reset() = pr_intptr_reset()

    @Test fun intptr_round_trips_boundaries() {
        for (v in listOf(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            pr_intptr_reset()
            assertEquals(v, pr_intptr_echo(v))
            assertEquals(v, pr_intptr_last_arg())
        }
    }

    @Test fun uintptr_round_trips_boundaries() {
        for (v in listOf(0uL, 9_000_000_000uL, ULong.MAX_VALUE)) {
            pr_intptr_reset()
            assertEquals(v, pr_uintptr_echo(v))
            assertEquals(v, pr_uintptr_last_arg())
        }
    }
}
