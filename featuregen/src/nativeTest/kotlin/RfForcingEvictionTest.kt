import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import root.StringFeature
import std.__cxx11.Basic_string__Char
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// RF-forcing-eviction (issue #18, the eviction-policy gate that BLEW UP in June):
// resolveForcing's pass-3 eviction must, under INCLUDE_MISSING, evict ONLY the forcing
// keys — NOT the `alreadyBoundKeys` union. featuregen binds `std::string`
// (`Basic_string__Char`) two ways at once: as a SIBLING type referenced by `StringFeature`
// (StringFeature::take/echo/produce), AND as the ELEMENT of a forced
// `std::vector<std::string>` instantiation (auto-discovered SYNC_REQUIRED from the
// cppVector<Basic_string__Char>() call sites below). That is exactly the scenario that
// degraded in June: widening the INCLUDE_MISSING eviction back to the union evicts the
// sibling-bound `std::string` from the forcing tracker, so pass-3 re-pulls it as a fresh
// INCLUDE_MISSING reference — emitting a DUPLICATE, degraded `basic_string` whose param
// casts lose their `<char>` template arg, which is uncompilable C++ and aborts
// `kplusplusSync` (krapper_gen exit 134).
//
// REGRESSION GUARD: re-widening the INCLUDE_MISSING eviction to the union makes
// `:featuregen:kplusplusSync` FAIL (the degraded `Basic_string__Char` cast won't compile),
// so this whole module fails to generate/build. When it DOES build, this test asserts the
// `Basic_string__Char` surface is intact — its const-char* constructor + c_str() round-trip
// AND the vector<string> element read-back both work on the single, correctly-`<char>`-
// templated binding. (Verified to bite: with the union-evict re-introduced, sync aborts at
// exit 134.) The generated static methods are MemScope-extension members on the companion,
// so feature calls are `method(...)` inside `with(StringFeature)`.
class RfForcingEvictionTest {

    // The SIBLING std::string surface (StringFeature) must round-trip: take() stores a
    // std::string built from a const char*, echo() returns it, produce() makes one. If the
    // sibling binding were degraded by the over-wide eviction, these would not compile.
    @Test fun sibling_stringFeature_roundTrips() = memScoped {
        with(StringFeature) {
            with(Basic_string__Char) {
                reset()
                take(Basic_string__Char__const_char_P("héllo"))
                assertEquals(6uL, lastSize()) // "héllo" is 6 UTF-8 bytes
                assertEquals("produced!", produce().c_str())
                assertEquals("round", echo(Basic_string__Char__const_char_P("round")).c_str())
            }
        }
    }

    // The FORCED std::vector<std::string> uses the SAME single Basic_string__Char element
    // binding. Pushing const-char*-constructed strings and reading them back via c_str()
    // proves the element binding kept its `<char>` template arg (a degraded duplicate would
    // mis-decode or fail to compile the element cast).
    @Test fun forced_vectorOfString_sharesIntactStringBinding() = memScoped {
        val v = cppVector<Basic_string__Char>()
        v.push_back(with(Basic_string__Char) { Basic_string__Char__const_char_P("alpha") })
        v.push_back(with(Basic_string__Char) { Basic_string__Char__const_char_P("beta") })
        assertEquals(2uL, v.size())
        with(Basic_string__Char) {
            assertEquals("alpha", v[0uL]?.c_str())
            assertEquals("beta", v.back()?.c_str())
        }
    }

    // Cross-check that the sibling and the vector element are the SAME std::string binding
    // (one `Basic_string__Char`), not a degraded second copy: a string produced by
    // StringFeature flows into the forced vector and reads back byte-exact.
    @Test fun siblingString_flowsThroughForcedVector() = memScoped {
        val v = cppVector<Basic_string__Char>()
        with(StringFeature) {
            with(Basic_string__Char) {
                v.push_back(echo(Basic_string__Char__const_char_P("shared")))
            }
        }
        assertEquals(1uL, v.size())
        assertTrue(with(Basic_string__Char) { v.front()?.c_str() } == "shared")
    }
}
