import feature_tests.st_charbuf_out_fill
import feature_tests.st_charbuf_out_payload_len
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlin.test.Test
import kotlin.test.assertEquals

// char* buffer fill: caller allocates, C writes the payload NUL-terminated and
// never exceeds n bytes. Tests the content / return-count / truncation contract.
class StCharbufOutTest {
    @Test fun fills_full_payload() {
        memScoped {
            val n = 16uL
            val buf = allocArray<ByteVar>(n.toInt())
            val written = st_charbuf_out_fill(buf, n)
            assertEquals(10uL, written)
            assertEquals("abcdefghij", buf.toKString())
        }
    }

    @Test fun result_is_nul_terminated() {
        memScoped {
            val buf = allocArray<ByteVar>(16)
            st_charbuf_out_fill(buf, 16uL)
            assertEquals(0, buf[10].toInt()) // NUL right after the 10 payload chars
        }
    }

    @Test fun truncates_without_overrun() {
        memScoped {
            val n = 5uL
            val buf = allocArray<ByteVar>(n.toInt()) // exactly n bytes — any overrun corrupts
            val written = st_charbuf_out_fill(buf, n)
            assertEquals(4uL, written)        // n-1 payload chars
            assertEquals("abcd", buf.toKString())
            assertEquals(0, buf[4].toInt())   // NUL at the last byte
        }
    }

    @Test fun zero_length_writes_nothing() {
        memScoped {
            val buf = allocArray<ByteVar>(1)
            buf[0] = 0x7F.toByte()
            val written = st_charbuf_out_fill(buf, 0uL)
            assertEquals(0uL, written)
            assertEquals(0x7F, buf[0].toInt()) // untouched
        }
    }

    @Test fun payload_len_helper() {
        assertEquals(10uL, st_charbuf_out_payload_len())
    }
}
