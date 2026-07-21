import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.cinterop.memScoped
import root.MiProbe
import root.PlainBase
import root.Tagged
import root.VDerived
import root.Widget2

// IH-multi-inherit / IH-cast: uniform offset-correct `.asBase()` upcast methods. Each
// is backed by a `D_as_B` C helper doing `static_cast<B*>(reinterpret_cast<D*>(p))`,
// which applies the base subobject's (possibly non-zero) offset — correct for BOTH a
// 2nd base of a multiply-inheriting class AND a single base pushed off offset 0 by a
// vtable-adding derived. A plain reinterpret_cast (offset 0) would mis-read either.
class IhCastTest {
    // MULTIPLE INHERITANCE. Widget2 : Drawable2, Tagged. Drawable2 is the primary base
    // (offset 0); Tagged is the SECOND base, at a non-zero offset inside Widget2. Both
    // upcasts must dispatch to Widget2's distinct overrides (draw=11, tag=22). If the
    // 2nd-base offset were wrong, asTagged().tag() would return garbage / crash.
    @Test fun multiple_inheritance_both_bases_dispatch_correctly() = memScoped {
        val w = with(Widget2) { Widget2() }
        // Primary base (offset 0).
        assertEquals(11, w.asDrawable2().draw())
        // Second base (NON-zero offset) — the whole point: static_cast applies the offset.
        assertEquals(22, w.asTagged().tag())
    }

    // The upcast wrapper is a genuine, correctly-offset base pointer: pass it where a
    // `const Tagged*` is wanted and read the virtual back through the base pointer. A
    // mis-offset pointer here would dispatch wrong / read garbage.
    @Test fun upcast_passed_to_base_pointer_probe() = memScoped {
        val w = with(Widget2) { Widget2() }
        val tagged: Tagged = w.asTagged()
        assertEquals(22, with(MiProbe) { tagOf(tagged) })
        assertEquals(11, with(MiProbe) { drawOf(w.asDrawable2()) })
    }

    // SINGLE INHERITANCE, NON-ZERO OFFSET (Case 2). VDerived : PlainBase, but VDerived
    // ADDS a virtual (vid()), so it gains a vptr at offset 0 and PlainBase is pushed to
    // a non-zero offset. Going through the offset-correct `asPlainBase()` view, a px
    // written there is read straight back (the static_cast applies the same correct
    // offset on both sides). With a plain reinterpret_cast the PlainBase subobject would
    // be misplaced at offset 0, over the vptr.
    @Test fun single_inheritance_nonzero_offset_round_trips_field() = memScoped {
        val v = with(VDerived) { VDerived() }
        val base: PlainBase = v.asPlainBase()
        base.setPx(7)
        assertEquals(7, base.getPx())
        // The derived's own virtual still dispatches (the object isn't corrupted by the
        // base-subobject write).
        assertEquals(99, v.vid())
    }

    // The non-zero offset is REAL, not 0: a write through the offset-correct `asPlainBase()`
    // view lands at a different address than the offset-0 reinterpret_cast the inherited
    // (flattened) `getPx()` uses. So after `asPlainBase().setPx(7)`, the flattened
    // `v.getPx()` — which reads at offset 0 — does NOT see 7. (This is exactly the bug the
    // cast methods exist to avoid; the flatten path is intentionally left offset-0.)
    @Test fun nonzero_offset_is_genuinely_nonzero() = memScoped {
        val v = with(VDerived) { VDerived() }
        v.asPlainBase().setPx(7)
        // Read through the correctly-offset cast view: sees 7.
        assertEquals(7, v.asPlainBase().getPx())
        // Read through the offset-0 flattened view: does NOT see 7 (different address).
        assertNotEquals(7, v.getPx())
    }
}
