import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Point2D

// #47 flip brick B2 generality demo: these tests run against bindings for cppfixture's OWN
// header (point2d.h). Under `-Pkpp.frontend.cppfixture=cpp` those bindings are produced by
// the cpp front-end through the GENERIC plugin path (krapper_parse -> ModelIo -> krapper_gen
// --frontend=cpp), driven by this module's own kplusplus{} config — proving the path
// generalizes beyond featuregen.
class Point2DTest {
    @Test fun construct_fields_and_const_method() = memScoped {
        val p = with(Point2D) { Point2D__double_double(3.0, 4.0) }
        assertEquals(3.0, p.x)
        assertEquals(4.0, p.y)
        assertEquals(25.0, p.lengthSq()) // 3^2 + 4^2

        p.x = 6.0
        assertEquals(6.0, p.x)
    }

    @Test fun default_ctor_and_static_factory() = memScoped {
        val z = with(Point2D) { Point2D() }
        assertEquals(0.0, z.x)
        assertEquals(0.0, z.y)

        val m = with(Point2D) { make(1.0, 2.0) }
        assertEquals(1.0, m.x)
        assertEquals(2.0, m.y)
    }
}
