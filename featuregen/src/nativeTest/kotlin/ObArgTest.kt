import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import root.PassObj
import root.Point2

// OB-* argument-passing conventions: by-value (copy), const-ref, pointer
// (mutating), non-const-ref (out-param). All free-function-style methods live on
// PassObj's companion as `MemScope.<name>(...)` (krapper_gen only wraps struct
// members, not namespace-scope free functions). Point2 objects are constructed
// via `with(Point2) { Point2__int_int(x, y) }` and fields read through x/y accessors.
class ObArgTest {

    context(scope: kotlinx.cinterop.MemScope)
    private fun pt(x: Int, y: Int): Point2 = with(Point2) { Point2__int_int(x, y) }

    // OB-constref-arg: int sumCoords(const Point2&) → MemScope.sumCoords(p): Int
    @Test fun const_ref_arg_sums() = memScoped {
        with(PassObj) {
            assertEquals(7, sumCoords(pt(3, 4)))
            assertEquals(0, sumCoords(pt(0, 0)))
            assertEquals(2, sumCoords(pt(-3, 5)))
        }
    }

    // OB-constref-arg: original object unchanged after the call.
    @Test fun const_ref_arg_leaves_original_intact() = memScoped {
        val p = pt(3, 4)
        with(PassObj) { sumCoords(p) }
        assertEquals(3, p.x)
        assertEquals(4, p.y)
    }

    // OB-byval-arg: void describePoint(Point2) — copy on entry. The C++ side sees
    // the field values (confirms a copy was made) and mutates its local copy;
    // the caller's object must be unchanged.
    @Test fun by_value_arg_makes_copy() = memScoped {
        with(PassObj) {
            val before = describeCountVal()
            val p = pt(8, 9)
            describePoint(p)
            // inspector saw the right field values → a real copy reached C++
            assertEquals(8, lastDescribeXVal())
            assertEquals(9, lastDescribeYVal())
            // counter incremented
            assertEquals(before + 1, describeCountVal())
            // describePoint mutated ITS copy to (-999,-999); caller untouched
            assertEquals(8, p.x)
            assertEquals(9, p.y)
        }
    }

    @Test fun by_value_arg_counter_increments_each_call() = memScoped {
        with(PassObj) {
            val before = describeCountVal()
            describePoint(pt(1, 1))
            describePoint(pt(2, 2))
            assertEquals(before + 2, describeCountVal())
        }
    }

    // OB-ptr-arg: void shiftPoint(Point2*, dx, dy) — mutating, caller keeps
    // ownership. Generated as shiftPoint(p: Point2?, dx, dy); passes p?.ptr.
    @Test fun pointer_arg_mutates_in_place() = memScoped {
        with(PassObj) {
            val p = pt(10, 20)
            shiftPoint(p, 3, 4)
            assertEquals(13, p.x)
            assertEquals(24, p.y)
            assertTrue(shiftSeenVal())
        }
    }

    @Test fun pointer_arg_multiple_shifts_accumulate() = memScoped {
        with(PassObj) {
            val p = pt(0, 0)
            shiftPoint(p, 1, 0)
            shiftPoint(p, 0, 1)
            shiftPoint(p, -5, -5)
            assertEquals(-4, p.x)
            assertEquals(-4, p.y)
        }
    }

    // OB-mutate-ref: void zeroPoint(Point2&) writes back through the reference.
    // The wrapper passes p.ptr and C++ binds it to Point2&, so mutations land on
    // the caller-visible struct (no defensive copy).
    @Test fun non_const_ref_writes_back() = memScoped {
        with(PassObj) {
            val p = pt(7, 11)
            zeroPoint(p)
            assertEquals(0, p.x)
            assertEquals(0, p.y)
            assertTrue(zeroSeenVal())
        }
    }

    // Same object — no new allocation: zero, then mutate again, still the same
    // binding.
    @Test fun non_const_ref_same_object() = memScoped {
        with(PassObj) {
            val p = pt(5, 5)
            zeroPoint(p)
            p.x = 42
            assertEquals(42, p.x)
            assertEquals(0, p.y)
        }
    }
}
