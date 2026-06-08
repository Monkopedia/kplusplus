import kotlinx.cinterop.memScoped
import root.StringFeature
import std.__cxx11.Basic_string__Char
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// ST-string-rt: full std::string round-trip via echo(), read back through
// c_str(). The generated basic_string binding has no size()/length() (filtered
// out), so content is verified through c_str().
class StStringRtTest {
    private fun echoStr(input: String): String? = memScoped {
        val ms = this
        with(StringFeature) {
            with(Basic_string__Char) {
                ms.echo(ms.Basic_string__Char__const_char_P(input)).c_str()
            }
        }
    }

    private fun constructedIsEmpty(input: String): Boolean = memScoped {
        val ms = this
        with(Basic_string__Char) { ms.Basic_string__Char__const_char_P(input).empty() }
    }

    @Test fun ascii_round_trips() = assertEquals("hello", echoStr("hello"))

    // std::string is byte-oriented, so UTF-8 survives intact through echo.
    @Test fun utf8_round_trips_byte_exact() = assertEquals("café", echoStr("café"))

    @Test fun empty_round_trips() = assertEquals("", echoStr(""))

    @Test fun constructed_empty_reports_empty() = assertTrue(constructedIsEmpty(""))
}
