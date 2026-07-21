import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.memScoped
import std.__cxx11.Basic_string__Char

// CV-elem-string: std::vector<std::string> — a generated element type
// (Basic_string__Char) inside a generated container. Transitive instantiation:
// std::string must be generated before std::vector<std::string>.
class CvElemStringTest {
    @Test fun push_strings_and_size() = memScoped {
        val v = cppVector<Basic_string__Char>()
        assertEquals(0uL, v.size())

        val a = with(Basic_string__Char) { Basic_string__Char__const_char_P("hello") }
        val b = with(Basic_string__Char) { Basic_string__Char__const_char_P("world") }
        v.push_back(a)
        v.push_back(b)

        assertEquals(2uL, v.size())
        assertFalse(v.empty())
    }

    // Read the std::string element back out (the caveat the matrix flagged):
    // operator[]/at/front/back wrap the element's own pointer as a
    // Basic_string__Char, whose c_str() then decodes the stored bytes.
    @Test fun read_strings_back_out() = memScoped {
        val v = cppVector<Basic_string__Char>()
        v.push_back(with(Basic_string__Char) { Basic_string__Char__const_char_P("hello") })
        v.push_back(with(Basic_string__Char) { Basic_string__Char__const_char_P("world") })

        with(Basic_string__Char) {
            assertEquals("hello", v[0uL]?.c_str())
            assertEquals("world", v.at(1uL)?.c_str())
            assertEquals("hello", v.front()?.c_str())
            assertEquals("world", v.back()?.c_str())
        }
    }
}
