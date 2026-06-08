import feature_tests.pr_int_out_null_flagged
import feature_tests.pr_int_out_produce
import feature_tests.pr_int_out_produced_count
import feature_tests.pr_int_out_reset
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrIntOutTest {
    @BeforeTest fun reset() = pr_int_out_reset()

    @Test fun produce_writes_42_to_out_param() {
        memScoped {
            val r = alloc<IntVar>()
            pr_int_out_produce(r.ptr)
            assertEquals(42, r.value)
            assertEquals(1, pr_int_out_produced_count())
        }
    }

    @Test fun repeated_calls_increment_inspector_counter() {
        memScoped {
            val r = alloc<IntVar>()
            repeat(5) { pr_int_out_produce(r.ptr) }
            assertEquals(5, pr_int_out_produced_count())
        }
    }

    @Test fun null_pointer_path_is_flagged_not_crashed() {
        pr_int_out_produce(null)
        assertEquals(1, pr_int_out_null_flagged())
        assertEquals(0, pr_int_out_produced_count())
    }
}
