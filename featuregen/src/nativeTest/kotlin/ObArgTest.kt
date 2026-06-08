import kotlinx.cinterop.memScoped
import root.PassObj
import root.Point2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// OB-* argument-passing conventions: by-value (copy), const-ref, pointer
// (mutating), non-const-ref (out-param). All free-function-style methods live on
// PassObj's companion as `MemScope.<name>(...)` (krapper_gen only wraps struct
// members, not namespace-scope free functions). Point2 objects are constructed
// via `with(Point2) { ms.Point2__int_int(x, y) }` and fields read through x/y accessors.
class ObArgTest {

    private fun pt(ms: kotlinx.cinterop.MemScope, x: Int, y: Int): Point2 =
        with(Point2) { ms.Point2__int_int(x, y) }

    // OB-constref-arg: int sumCoords(const Point2&) → MemScope.sumCoords(p): Int
    @Test fun const_ref_arg_sums() = memScoped {
        val ms = this
        with(PassObj) {
            assertEquals(7, ms.sumCoords(pt(ms, 3, 4)))
            assertEquals(0, ms.sumCoords(pt(ms, 0, 0)))
            assertEquals(2, ms.sumCoords(pt(ms, -3, 5)))
        }
    }

    // OB-constref-arg: original object unchanged after the call.
    @Test fun const_ref_arg_leaves_original_intact() = memScoped {
        val ms = this
        val p = pt(ms, 3, 4)
        with(PassObj) { ms.sumCoords(p) }
        assertEquals(3, p.x)
        assertEquals(4, p.y)
    }

    // OB-byval-arg: void describePoint(Point2) — copy on entry. The C++ side sees
    // the field values (confirms a copy was made) and mutates its local copy;
    // the caller's object must be unchanged.
    @Test fun by_value_arg_makes_copy() = memScoped {
        val ms = this
        with(PassObj) {
            val before = ms.describeCountVal()
            val p = pt(ms, 8, 9)
            ms.describePoint(p)
            // inspector saw the right field values → a real copy reached C++
            assertEquals(8, ms.lastDescribeXVal())
            assertEquals(9, ms.lastDescribeYVal())
            // counter incremented
            assertEquals(before + 1, ms.describeCountVal())
            // describePoint mutated ITS copy to (-999,-999); caller untouched
            assertEquals(8, p.x)
            assertEquals(9, p.y)
        }
    }

    @Test fun by_value_arg_counter_increments_each_call() = memScoped {
        val ms = this
        with(PassObj) {
            val before = ms.describeCountVal()
            ms.describePoint(pt(ms, 1, 1))
            ms.describePoint(pt(ms, 2, 2))
            assertEquals(before + 2, ms.describeCountVal())
        }
    }

    // OB-ptr-arg: void shiftPoint(Point2*, dx, dy) — mutating, caller keeps
    // ownership. Generated as shiftPoint(p: Point2?, dx, dy); passes p?.ptr.
    @Test fun pointer_arg_mutates_in_place() = memScoped {
        val ms = this
        with(PassObj) {
            val p = pt(ms, 10, 20)
            ms.shiftPoint(p, 3, 4)
            assertEquals(13, p.x)
            assertEquals(24, p.y)
            assertTrue(ms.shiftSeenVal())
        }
    }

    @Test fun pointer_arg_multiple_shifts_accumulate() = memScoped {
        val ms = this
        with(PassObj) {
            val p = pt(ms, 0, 0)
            ms.shiftPoint(p, 1, 0)
            ms.shiftPoint(p, 0, 1)
            ms.shiftPoint(p, -5, -5)
            assertEquals(-4, p.x)
            assertEquals(-4, p.y)
        }
    }

    // OB-mutate-ref: void zeroPoint(Point2&) writes back through the reference.
    // The wrapper passes p.ptr and C++ binds it to Point2&, so mutations land on
    // the caller-visible struct (no defensive copy).
    @Test fun non_const_ref_writes_back() = memScoped {
        val ms = this
        with(PassObj) {
            val p = pt(ms, 7, 11)
            ms.zeroPoint(p)
            assertEquals(0, p.x)
            assertEquals(0, p.y)
            assertTrue(ms.zeroSeenVal())
        }
    }

    // Same object — no new allocation: zero, then mutate again, still the same
    // binding.
    @Test fun non_const_ref_same_object() = memScoped {
        val ms = this
        with(PassObj) {
            val p = pt(ms, 5, 5)
            ms.zeroPoint(p)
            p.x = 42
            assertEquals(42, p.x)
            assertEquals(0, p.y)
        }
    }
}
