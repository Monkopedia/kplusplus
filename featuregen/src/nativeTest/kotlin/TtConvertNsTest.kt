import kotlinx.cinterop.memScoped
import root.Convertible
import kotlin.test.Test
import kotlin.test.assertEquals

// T-typename-a: a user-defined conversion operator to a NAMESPACED struct
// (`Convertible::operator nn::Tok() const`). libclang spells the method `operator Tok`
// (target UNqualified), so the generated C wrapper used to emit
// `thiz_cast->operator Tok()` -> "unknown type name 'Tok'; did you mean 'nn::Tok'?",
// failing the featuregen native compile. The fix re-spells the conversion call with the
// fully-qualified return type (`thiz_cast->operator nn::Tok()`).
//
// Coverage is structural: this file COMPILING + a successful sync proves the qualified
// call was emitted (the unfixed generator never links). The assert exercises the
// conversion to confirm it is wired and returns the converted value.
class TtConvertNsTest {
    @Test fun conversion_to_namespaced_type_binds_and_runs() = memScoped {
        val ms = this
        with(Convertible) {
            val c = ms.Convertible__int(42)
            // The conversion `operator nn::Tok()` yields an nn::Tok whose `v` is `n`.
            val tok = c.toTok()
            assertEquals(42, tok.value())
        }
    }
}
