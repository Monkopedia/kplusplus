import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import root.Circle
import root.Rectangle
import root.Shape
import root.ShapeApi
import root.ShapeProbe

// IH-upcast / IH-virtual-dispatch: single public inheritance generates an
// `interface ShapeApi` (the base's ptr + virtual surface) that Shape/Circle/Rectangle
// implement, so a Circle can be passed where a Shape* is wanted (upcast) and a virtual
// area() called through the base interface vtable-dispatches to the Circle override.
class IhUpcastTest {
    // IH-upcast: ShapeProbe.areaOf takes a `const Shape*`, generated as `ShapeApi?`. A
    // Circle (which : ShapeApi) is accepted there and its area() dispatches to Circle's
    // override (π·r²), not Shape's base 0.0.
    @Test fun circle_upcast_to_shape_pointer_dispatches() = memScoped {
        val c = with(Circle) { Circle__double(2.0) }
        val expected = PI * 2.0 * 2.0
        // Sanity: the derived's own area() is the Circle override.
        assertEquals(expected, c.area(), 1e-9)
        // Passed as a base Shape* and read back through the base pointer -> still Circle's.
        val viaProbe = with(ShapeProbe) { areaOf(c) }
        assertEquals(expected, viaProbe, 1e-9)
        // And by const-ref.
        val viaRef = with(ShapeProbe) { areaOfRef(c) }
        assertEquals(expected, viaRef, 1e-9)
    }

    // The base's own binding still works through the interface (area() == 0.0).
    @Test fun base_shape_through_probe_is_zero() = memScoped {
        val s = with(Shape) { Shape() }
        assertEquals(0.0, with(ShapeProbe) { areaOf(s) }, 1e-9)
    }

    // IH-virtual-dispatch via a polymorphic variable: a Circle and a Rectangle held in a
    // `ShapeApi?` each dispatch area() to their own override.
    @Test fun polymorphic_shape_api_dispatch() = memScoped {
        val circle = with(Circle) { Circle__double(3.0) }
        val rect = with(Rectangle) { Rectangle__double_double(4.0, 5.0) }
        val shapes: List<ShapeApi> = listOf(circle, rect)
        assertEquals(PI * 9.0, shapes[0].area(), 1e-9)
        assertEquals(20.0, shapes[1].area(), 1e-9)
    }

    // IH-upcast on a RETURN: ShapeProbe.biggest returns `Shape*` (generated `ShapeApi?`),
    // really the larger argument; calling area() on the returned base interface dispatches
    // to whichever derived it actually is.
    @Test fun shape_pointer_return_upcast_dispatches() = memScoped {
        val small = with(Circle) { Circle__double(1.0) } // area = π
        val big = with(Rectangle) { Rectangle__double_double(10.0, 10.0) } // area = 100
        val winner: ShapeApi? = with(ShapeProbe) { biggest(small, big) }
        assertTrue(winner != null)
        // The Rectangle is bigger; its area dispatches through the returned base interface.
        assertEquals(100.0, winner.area(), 1e-9)
    }
}
