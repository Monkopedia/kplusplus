import kotlinx.cinterop.memScoped
import root.ImplicitAssignHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// T-skip-residuals (skip-not-crash for IMPLICITLY-deleted special members): the existing
// T-skip detection only fired when an EXPLICIT `= delete`d/non-public copy member was
// filtered out as a cursor. It missed members that are IMPLICITLY deleted by a structural
// property (a const or reference data member). This extends the parse-time signal to those:
//   * ImplicitNoAssign has a `const int c` member -> its implicit copy-ASSIGN is deleted,
//     so a field of that type drops its `= *value` SETTER (getter kept).
//   * RefMember/RefHolder have a reference member -> implicit copy-assign AND implicit
//     DEFAULT ctor are deleted, so no `new T()` default-construct path is synthesized.
//
// Coverage is structural: the file COMPILING + a successful sync proves the unmodelable
// setter / default-construct paths were not emitted (the unfixed generator emits
// `thiz->slot = *value` -> "copy assignment ... is implicitly deleted" and a
// `new RefHolder()` -> "call to implicitly-deleted default constructor", either of which
// fails the featuregen native build).
class TsResidualTest {
    // ImplicitAssignHolder.slot is implicitly non-copy-assignable (const member) -> SETTER
    // dropped, GETTER kept. The sibling `tally` int field keeps both accessors.
    @Test fun implicit_no_assign_field_getter_survives_setter_dropped() = memScoped {
        val h = with(ImplicitAssignHolder) { ImplicitAssignHolder() }
        // `slot` is a by-value-wrapper field: its getter allocates via `_Holder`, so it is
        // now a `context(scope: MemScope) fun slot()` (the memScoped receiver supplies scope)
        // rather than a property.
        val slot = h.slot()
        assertNotNull(slot)
        assertEquals(9, slot.getC())
        // `h.slot = ...` would not compile: the setter was dropped (val, not var).
    }

    @Test fun ordinary_field_keeps_getter_and_setter() = memScoped {
        val h = with(ImplicitAssignHolder) { ImplicitAssignHolder() }
        assertEquals(0, h.tally)
        h.tally = 23
        assertEquals(23, h.tally)
    }
}
