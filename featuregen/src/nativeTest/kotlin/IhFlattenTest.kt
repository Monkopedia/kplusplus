import kotlinx.cinterop.memScoped
import root.Derived
import kotlin.test.Test
import kotlin.test.assertEquals

// IH-base-method / IH-name-conflict / IH-virtual-override / IH-derived-method:
// single (one base) public inheritance flattened onto the derived binding
// (decision A). `Derived : Base` re-exposes Base's public methods/fields directly:
//  - inherited non-virtual move(d) mutates the inherited base field bx;
//  - baseOnly()/bx are inherited and reachable with no downcast;
//  - the virtual override area() stays the bare name and dispatches to Derived's
//    own override (returns 42, not Base's 0);
//  - Base::label and the shadowing non-virtual Derived::label coexist: label()
//    is the inherited base's ("base"), derivedLabel() is Derived's own ("derived");
//  - derivedOnly()/dy are the derived-only members.
class IhFlattenTest {
    // IH-base-method: inherited non-virtual move(d) accumulates into base field bx,
    // and baseOnly()/bx read the same inherited state.
    @Test fun inherited_move_and_base_field() = memScoped {
        val ms = this
        val d = with(Derived) { ms.Derived() }
        assertEquals(0, d.bx)
        assertEquals(0, d.baseOnly())
        d.move(3)
        assertEquals(3, d.bx)
        assertEquals(3, d.baseOnly())
        d.move(4) // accumulates
        assertEquals(7, d.bx)
        assertEquals(7, d.baseOnly())
        d.bx = 100 // inherited field is writable
        assertEquals(100, d.baseOnly())
    }

    // IH-virtual-override: area() keeps the bare name and dispatches to Derived's
    // concrete override (42), not Base's default (0).
    @Test fun virtual_override_keeps_bare_name() = memScoped {
        val ms = this
        val d = with(Derived) { ms.Derived() }
        assertEquals(42, d.area())
    }

    // IH-name-conflict: inherited Base::label keeps the bare name (= "base"); the
    // shadowing non-virtual Derived::label yields the name and is class-prefixed
    // to derivedLabel() (= "derived"). Both reachable.
    @Test fun shadowed_label_splits_into_label_and_derivedLabel() = memScoped {
        val ms = this
        val d = with(Derived) { ms.Derived() }
        assertEquals("base", d.label())          // inherited Base::label
        assertEquals("derived", d.derivedLabel()) // Derived's own shadow
    }

    // IH-derived-method: derived-only members generate normally on Derived.
    @Test fun derived_only_members() = memScoped {
        val ms = this
        val d = with(Derived) { ms.Derived() }
        assertEquals(0, d.derivedOnly())
        d.dy = 9
        assertEquals(9, d.dy)
        assertEquals(9, d.derivedOnly())
    }
}
