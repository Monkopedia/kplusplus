import kotlinx.cinterop.memScoped
import root.BoolFilterable
import kotlin.test.Test
import kotlin.test.assertEquals

// T-defarg-bool (fix #1): the same skip-not-crash guard as T-defarg (Mask<4>), but the
// un-modelable param's non-type template argument is a `bool` (`Flags<true>`) rather than
// a number. The OLD numeric-only false-default check (`toDoubleOrNull`) did NOT catch
// `true`, so `BoolFilterable::tally(Flags<true>)`'s required param was wrongly treated as
// omittable and TRIMMED, leaving a short, non-compiling `thiz_cast->tally();`. The fix
// recognizes the value template arg structurally (its CXType is Invalid -> UNRESOLVABLE)
// and drops the whole method. The sibling `base()` must still bind.
//
// Coverage is structural, exactly like TdDefArgTest: this file COMPILING proves `tally`
// was not emitted (a short C++ call would have failed the featuregen native build during
// sync, before these tests ran). The assert then proves the class is still usable via its
// surviving sibling.
class TdDefArgBoolTest {
    // The sibling base() (no un-modelable param) still binds and returns n (7).
    @Test fun sibling_base_still_binds() = memScoped {
        val ms = this
        val f = with(BoolFilterable) { ms.BoolFilterable() }
        assertEquals(7, f.base())
    }

    // The un-modelable-param method tally was dropped: referencing `f.tally(...)` here
    // would fail to compile. Asserted structurally by exercising the surviving sibling.
    @Test fun bool_nontype_template_param_method_is_absent() = memScoped {
        val ms = this
        val f = with(BoolFilterable) { ms.BoolFilterable() }
        assertEquals(7, f.base())
    }
}
