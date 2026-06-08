import kotlinx.cinterop.memScoped
import root.PassObj
import root.Point2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// OB-* return conventions: by-value (placement-new into a Holder), borrowed
// pointer (non-owning wrapper, no free), owned pointer (manual freePoint),
// reference (non-owning wrapper over the address), nullable pointer.
//
// Key generated shape: pointer/reference returns are wrapped as a *non-owning*
// `Point2?` — `Point2(rawptr, memScope)` with NO Holder and NO dispose. So a
// borrowed/static pointer is never auto-freed (no double-free), and an owned
// heap pointer must be freed explicitly via freePoint (no automatic help).
class ObReturnTest {

    private fun pt(ms: kotlinx.cinterop.MemScope, x: Int, y: Int): Point2 =
        with(Point2) { ms.Point2__int_int(x, y) }

    // OB-return-byval: Point2 clonePoint(const Point2&) → placement-new into a
    // Point2_Holder; fields match input.
    @Test fun return_by_value_matches_input() = memScoped {
        val ms = this
        with(PassObj) {
            val q = ms.clonePoint(pt(ms, 3, 4))
            assertEquals(3, q.x)
            assertEquals(4, q.y)
        }
    }

    // Returned object is independent: mutate original, clone unchanged.
    @Test fun return_by_value_is_independent() = memScoped {
        val ms = this
        with(PassObj) {
            val p = pt(ms, 1, 2)
            val q = ms.clonePoint(p)
            p.x = 99
            p.y = 99
            assertEquals(1, q.x)
            assertEquals(2, q.y)
        }
    }

    // Called in a loop — no crash / Holder leak.
    @Test fun return_by_value_in_loop() = memScoped {
        val ms = this
        with(PassObj) {
            var acc = 0
            for (i in 0 until 50) {
                val q = ms.clonePoint(pt(ms, i, i + 1))
                acc += q.x + q.y
            }
            assertEquals((0 until 50).sumOf { it + (it + 1) }, acc)
        }
    }

    // OB-return-byval (the makePoint variant — returns by value from primitives).
    @Test fun make_point_returns_by_value() = memScoped {
        val ms = this
        with(PassObj) {
            val q = ms.makePoint(12, 34)
            assertEquals(12, q.x)
            assertEquals(34, q.y)
        }
    }

    // OB-return-ptr-borrowed: Point2* borrowGlobal() → non-null wrapper over a
    // static; fields readable; same address each call; no free on scope exit.
    @Test fun borrowed_pointer_readable_and_stable() = memScoped {
        val ms = this
        with(PassObj) {
            val a = ms.borrowGlobal()
            assertNotNull(a)
            assertEquals(11, a.x)
            assertEquals(22, a.y)
            // calling twice returns the same underlying storage: mutate via one,
            // observe via the other (stable global, single non-owning wrapper).
            a.x = 100
            val b = ms.borrowGlobal()
            assertNotNull(b)
            assertEquals(100, b.x)
            // restore so test order doesn't matter
            b.x = 11
        }
    }

    // OB-return-ref: Point2& globalRef() → non-owning wrapper over the address.
    // Writing a field through the binding mutates the underlying global; read it
    // back via a second call.
    @Test fun reference_return_write_back() = memScoped {
        val ms = this
        with(PassObj) {
            val r = ms.globalRef()
            assertNotNull(r)
            assertEquals(33, r.x)
            assertEquals(44, r.y)
            r.x = 99
            r.y = 88
            val r2 = ms.globalRef()
            assertNotNull(r2)
            assertEquals(99, r2.x)
            assertEquals(88, r2.y)
            // restore
            r2.x = 33
            r2.y = 44
        }
    }

    // OB-return-ptr-owned: Point2* allocPoint(x,y) → heap pointer wrapped
    // non-owning; freed explicitly via freePoint (no auto Holder). Caller is
    // fully responsible for the lifecycle.
    @Test fun owned_pointer_alloc_then_free() = memScoped {
        val ms = this
        with(PassObj) {
            val p = ms.allocPoint(7, 8)
            assertNotNull(p)
            assertEquals(7, p.x)
            assertEquals(8, p.y)
            ms.freePoint(p)   // explicit free does not crash
        }
    }

    @Test fun owned_pointer_many_alloc_free_no_leak_crash() = memScoped {
        val ms = this
        with(PassObj) {
            for (i in 0 until 100) {
                val p = ms.allocPoint(i, i)
                assertNotNull(p)
                assertEquals(i, p.x)
                ms.freePoint(p)
            }
        }
    }

    // OB-null-return: Point2* maybePoint(bool) → nullability preserved.
    @Test fun null_return_when_false() = memScoped {
        val ms = this
        with(PassObj) {
            assertNull(ms.maybePoint(false))
        }
    }

    @Test fun non_null_return_when_true() = memScoped {
        val ms = this
        with(PassObj) {
            val p = ms.maybePoint(true)
            assertNotNull(p)
            assertEquals(55, p.x)
            assertEquals(66, p.y)
        }
    }

    @Test fun null_path_repeatable() = memScoped {
        val ms = this
        with(PassObj) {
            repeat(20) { assertNull(ms.maybePoint(false)) }
            assertNotNull(ms.maybePoint(true))
            Unit
        }
    }
}
