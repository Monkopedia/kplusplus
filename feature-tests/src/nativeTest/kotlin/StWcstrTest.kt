import feature_tests.st_wcstr_code_at
import feature_tests.st_wcstr_last_len
import feature_tests.st_wcstr_name
import feature_tests.st_wcstr_reset
import feature_tests.st_wcstr_take
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKStringFromUtf32
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Wide C string on Linux/macOS: wchar_t is 4 bytes, so cinterop types the param
// as CValuesRef<IntVar>? (not the 2-byte .wcstr). We build the wchar_t array by
// hand from char codes — the take path uses BMP-only strings (no Kotlin
// surrogate pairs), so one Char == one wchar_t. The astral 😀 is exercised on
// the return path, where toKStringFromUtf32 recombines from the 4-byte units.
class StWcstrTest {
    @BeforeTest fun reset() = st_wcstr_reset()

    private fun send(s: String) = memScoped {
        val arr = allocArray<IntVar>(s.length + 1)
        for (i in s.indices) arr[i] = s[i].code
        arr[s.length] = 0
        st_wcstr_take(arr)
    }

    @Test fun ascii_round_trip() {
        send("ABC")
        assertEquals(3uL, st_wcstr_last_len())
        assertEquals('A'.code.toLong(), st_wcstr_code_at(0uL))
        assertEquals('C'.code.toLong(), st_wcstr_code_at(2uL))
    }

    @Test fun bmp_code_point_round_trip() {
        send("A€B") // € = U+20AC, BMP, single wchar_t
        assertEquals(3uL, st_wcstr_last_len())
        assertEquals(0x20ACL, st_wcstr_code_at(1uL))
    }

    @Test fun empty_string() {
        send("")
        assertEquals(0uL, st_wcstr_last_len())
    }

    @Test fun code_at_out_of_range_is_minus_one() {
        send("A")
        assertEquals(-1L, st_wcstr_code_at(5uL))
    }

    @Test fun returned_wide_string_incl_astral_decodes() {
        // C side returns L"A€😀"; 4-byte wchar_t holds U+1F600 directly.
        assertEquals("A€😀", st_wcstr_name()?.toKStringFromUtf32())
    }
}
