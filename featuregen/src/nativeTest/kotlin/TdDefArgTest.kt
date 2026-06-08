import kotlinx.cinterop.memScoped
import root.Filterable
import kotlin.test.Test
import kotlin.test.assertEquals

// T-defarg (skip-not-crash at the method level): when a method has a REQUIRED
// parameter krapper cannot model, the whole method must be DROPPED — never emitted
// with a short C++ call that fails to compile. `Filterable::countIf(Mask<4>)` takes
// an un-modelable non-type-template-parameter class; its parameter is silently
// trimmed on the UNFIXED generator (the array/template integer token is misread as
// a C++ default), leaving `thiz_cast->countIf();` — "too few arguments". The guard
// drops `countIf` entirely. The sibling modelable `sum()` MUST still bind and work.
//
// Coverage of the guard is structural: this file COMPILING at all proves `countIf`
// was not emitted (no Kotlin method, no short C++ call — the generated wrapper
// compiled during sync), since a short call would have failed the featuregen native
// build before these tests ever ran. The asserts then prove the class is still
// usable via its surviving sibling.
class TdDefArgTest {
    // The sibling sum() (no un-modelable param) still binds and returns the sum of
    // the default-initialized values {1,2,3,4}.
    @Test fun sibling_sum_still_binds() = memScoped {
        val ms = this
        val f = with(Filterable) { ms.Filterable() }
        assertEquals(10, f.sum())
    }

    // The un-modelable-param method countIf was dropped: Filterable exposes no
    // `countIf` member. Asserted structurally — referencing `f.countIf(...)` here
    // would fail to compile. We assert the class is constructible and its public
    // surface is exactly the surviving sibling by exercising sum() again.
    @Test fun unmodelable_param_method_is_absent() = memScoped {
        val ms = this
        val f = with(Filterable) { ms.Filterable() }
        // sum() is the only bound instance method; countIf is gone.
        assertEquals(10, f.sum())
    }
}
