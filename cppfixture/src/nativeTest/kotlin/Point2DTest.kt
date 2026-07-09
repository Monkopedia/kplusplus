import kotlinx.cinterop.memScoped
import root.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals

// #47 flip brick B2 generality demo: these tests run against bindings for cppfixture's OWN
// header (point2d.h). Under `-Pkpp.frontend.cppfixture=cpp` those bindings are produced by
// the cpp front-end through the GENERIC plugin path (krapper_parse -> ModelIo -> krapper_gen
// --frontend=cpp), driven by this module's own kplusplus{} config — proving the path
// generalizes beyond featuregen. Under plain `-PenableClang` they are the libclang bindings;
// the same tests pass either way.
class Point2DTest {
    @Test fun construct_fields_and_const_method() = memScoped {
        val ms = this
        val p = with(Point2D) { ms.Point2D__double_double(3.0, 4.0) }
        assertEquals(3.0, p.x)
        assertEquals(4.0, p.y)
        assertEquals(25.0, p.lengthSq()) // 3^2 + 4^2

        p.x = 6.0
        assertEquals(6.0, p.x)
    }

    @Test fun default_ctor_and_static_factory() = memScoped {
        val ms = this
        val z = with(Point2D) { ms.Point2D() }
        assertEquals(0.0, z.x)
        assertEquals(0.0, z.y)

        val m = with(Point2D) { ms.make(1.0, 2.0) }
        assertEquals(1.0, m.x)
        assertEquals(2.0, m.y)
    }
}
