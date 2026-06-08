import feature_tests.pr_bool_call_count
import feature_tests.pr_bool_negate
import feature_tests.pr_bool_reset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrBoolTest {
    @BeforeTest fun reset() = pr_bool_reset()

    @Test fun negate_true_returns_false() {
        assertFalse(pr_bool_negate(true))
    }

    @Test fun negate_false_returns_true() {
        assertTrue(pr_bool_negate(false))
    }

    @Test fun loop_advances_inspector_counter() {
        repeat(10) { pr_bool_negate(it % 2 == 0) }
        assertEquals(10, pr_bool_call_count())
    }
}
