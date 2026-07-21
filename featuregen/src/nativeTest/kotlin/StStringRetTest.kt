import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.memScoped
import root.StringFeature
import std.__cxx11.Basic_string__Char

// ST-string-ret: std::string returned by value. The generated binding returns a
// Holder-backed Basic_string__Char whose storage is bound to the enclosing
// MemScope — freed when the scope exits (ownership = scope-bounded). The wrapper
// placement-news the result into that Holder memory (see krapper_gen ARG_CAST).
class StStringRetTest {
    private fun produced(): String? = memScoped {
        with(StringFeature) { with(Basic_string__Char) { produce().c_str() } }
    }

    private fun producedIsEmpty(): Boolean = memScoped {
        with(StringFeature) { with(Basic_string__Char) { produce().empty() } }
    }

    @Test fun produce_returns_expected_content() = assertEquals("produced!", produced())

    @Test fun produced_string_is_not_empty() = assertFalse(producedIsEmpty())

    @Test fun produce_is_stable_across_calls() = assertEquals(produced(), produced())
}
