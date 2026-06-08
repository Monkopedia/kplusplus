import kotlinx.cinterop.memScoped
import root.FieldSetterHolder
import root.NonCopyableHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// T-skip (skip-not-crash for non-copyable by-value shapes): a by-value (Holder) field
// getter whose element type has a DELETED copy constructor, and a generated field
// SETTER whose field type has a DELETED copy ASSIGNMENT, both emit copies the type
// doesn't have and won't compile. The guard DROPS the offending field/setter (and logs),
// leaving the sibling members of the same class binding normally.
//
// Coverage is structural: this file COMPILING + a successful sync proves the unmodelable
// shapes were not emitted. The unfixed generator would have emitted:
//   * `new (ret_value_cast) NonCopyable(thiz->nc)` -> "call to deleted constructor"
//   * `thiz_cast->locked = *value_cast`            -> "copy assignment ... is deleted"
// either of which fails the featuregen native build before tests run. The asserts
// exercise the surviving siblings to prove the classes are still usable.
class TsSkipTest {
    // T-skip (a): NonCopyableHolder.nc is a non-copy-constructible field -> its by-value
    // getter is unmodelable, so the whole `nc` field is DROPPED. The sibling `tag` (an
    // int field) still binds with both getter and setter. Referencing `h.nc` here would
    // not compile (the field is absent).
    @Test fun non_copyable_field_dropped_sibling_binds() = memScoped {
        val ms = this
        val h = with(NonCopyableHolder) { ms.NonCopyableHolder() }
        assertEquals(42, h.tag)
        h.tag = 7
        assertEquals(7, h.tag)
    }

    // T-skip (b): FieldSetterHolder.locked is a non-copy-assignable field -> its SETTER
    // is dropped while its GETTER survives. The getter returns a non-owning wrapper over
    // the member; its weight() is readable.
    @Test fun non_assignable_field_getter_survives() = memScoped {
        val ms = this
        val h = with(FieldSetterHolder) { ms.FieldSetterHolder() }
        val locked = h.locked
        assertNotNull(locked)
        assertEquals(5, locked.weight())
        // `h.locked = ...` would not compile: the setter was dropped (val, not var).
    }

    // T-skip (b) control: the ordinary `count` field keeps BOTH getter and setter.
    @Test fun ordinary_field_keeps_getter_and_setter() = memScoped {
        val ms = this
        val h = with(FieldSetterHolder) { ms.FieldSetterHolder() }
        assertEquals(0, h.count)
        h.count = 17
        assertEquals(17, h.count)
    }
}
