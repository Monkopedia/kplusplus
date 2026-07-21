import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.ConstRetHolder

// T-constholder: a by-value return whose type is top-level `const`-qualified
// (`const Coord makeConst()`) is materialized into a scope-bound Holder via
// placement-new. On the UNFIXED generator the Holder target buffer is typed
// `const Coord*`, and `new (const Coord* buf) Coord(...)` is ill-formed
// ("placement new expression with a const-qualified argument is not allowed"),
// which fails the featuregen native compile during sync. The fix strips top-level
// const from the Holder TARGET type (the source value keeps its real type; a copy
// from a const lvalue into a non-const buffer is fine), so the method binds and the
// generated wrapper compiles.
//
// Coverage is partly structural: this file COMPILING and the sync having succeeded
// proves the const Holder target was emitted as `Coord*` (the unfixed `const Coord*`
// target would have failed the native build before tests ran). The asserts then
// prove the copyable value crossed the boundary intact.
class TcConstHolderTest {
    // The const-qualified by-value return binds and the copied value is correct.
    @Test fun const_by_value_return_binds_and_copies() = memScoped {
        val h = with(ConstRetHolder) { ConstRetHolder() }
        val c = h.makeConst()
        assertEquals(3, c.x)
        assertEquals(4, c.y)
        assertEquals(7, c.sum())
    }

    // The non-const sibling returning the same copyable struct by value still binds
    // (the fix only touched the Holder target's constness, not the value path).
    @Test fun non_const_sibling_still_binds() = memScoped {
        val h = with(ConstRetHolder) { ConstRetHolder() }
        val c = h.plain()
        assertEquals(3, c.x)
        assertEquals(4, c.y)
    }

    // The returned value is an independent copy in its own Holder.
    @Test fun const_return_is_independent_copy() = memScoped {
        val h = with(ConstRetHolder) { ConstRetHolder() }
        val a = h.makeConst()
        val b = h.makeConst()
        a.x = 99
        assertEquals(3, b.x)
    }
}
