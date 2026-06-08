import kotlinx.cinterop.memScoped
import root.DcBase
import root.DcFactory
import root.Derived1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// IH-down-cast (T1.2 dyn_cast): the CHECKED, nullable `B.asD(): D?` down-cast. Backed by a
// `DcBase_dyncast_DerivedN` C shim doing `dynamic_cast<DerivedN*>` (the generic RTTI path),
// it returns a wrapper only when the runtime object really is that derived, else null. This
// is the inverse of the (already-shipped) `.asBase()` up-cast.
class IhDownCastTest {
    // A `DcBase*` that actually points at a Derived1. The down-cast to the REAL type
    // succeeds and the Derived1-only method works; the down-cast to the WRONG sibling
    // (Derived2) returns null — the whole point of a checked cast.
    @Test fun down_cast_to_real_type_succeeds_wrong_sibling_is_null() = memScoped {
        val ms = this
        // The factory hands back a base-typed pointer (DcBaseApi?); rewrap it as the
        // concrete DcBase wrapper that carries the generated down-cast methods. The
        // pointer still points at a real Derived1 at runtime.
        val base = DcBase(with(DcFactory) { ms.makeDerived1() }!!.ptr, ms)

        // Correct down-cast: non-null, and the derived-only method is reachable + correct.
        val d1: Derived1? = base.asDerived1()
        assertNotNull(d1)
        assertEquals(111, d1.one())
        // The cast view is the SAME object: the virtual still reports Derived1's kind.
        assertEquals(1, d1.kind())

        // Wrong sibling: the object is NOT a Derived2, so the checked cast yields null.
        assertNull(base.asDerived2())
    }

    // The down-cast is a borrowed, non-owning VIEW of the same object — round-tripping
    // up then back down lands on the same instance with the same state.
    @Test fun down_cast_is_a_view_of_the_same_object() = memScoped {
        val ms = this
        // The factory hands back a base-typed pointer (DcBaseApi?); rewrap it as the
        // concrete DcBase wrapper that carries the generated down-cast methods. The
        // pointer still points at a real Derived1 at runtime.
        val base = DcBase(with(DcFactory) { ms.makeDerived1() }!!.ptr, ms)
        // kind() dispatches virtually to Derived1 whether read through the base or the
        // down-cast view.
        assertEquals(1, base.kind())
        assertEquals(1, base.asDerived1()!!.kind())
    }
}
