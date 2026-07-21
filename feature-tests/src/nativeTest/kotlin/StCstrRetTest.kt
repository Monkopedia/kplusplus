import feature_tests.st_cstr_ret_name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.cinterop.toKString

// const char* return, borrowed from static storage: content decodes, the
// pointer is stable across calls, and the caller never frees it.
class StCstrRetTest {
    @Test fun content_decodes() {
        assertEquals("kplusplus", st_cstr_ret_name()?.toKString())
    }

    @Test fun pointer_is_non_null() {
        assertNotNull(st_cstr_ret_name())
    }

    @Test fun pointer_is_stable_across_calls() {
        val a = st_cstr_ret_name()
        val b = st_cstr_ret_name()
        assertEquals(a?.rawValue, b?.rawValue)
    }

    @Test fun content_stable_when_called_repeatedly() {
        repeat(3) { assertEquals("kplusplus", st_cstr_ret_name()?.toKString()) }
    }
}
