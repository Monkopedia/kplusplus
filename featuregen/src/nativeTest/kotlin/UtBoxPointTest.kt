import root.Box
import root.Point
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// UT-box-point: the same user class template `Box<T>` instantiated over a
// USER-defined type — `Box<Point>()` (where `Point` is itself a generated
// `@CppBinding("Point")` struct). The kplusplus compiler plugin refines the
// `Box<Point>()` facade call to the concrete `Box__Point : Box<Point>` binding,
// whose set/get carry the `Point` element across the boundary by pointer.
class UtBoxPointTest {
    @Test fun box_point_roundtrips() = memScoped {
        val p = with(Point) { Point__int_int(3, 4) }
        val b = Box<Point>()
        b.set(p)
        val out = b.get()
        assertEquals(3, out.x)
        assertEquals(4, out.y)
    }
}
