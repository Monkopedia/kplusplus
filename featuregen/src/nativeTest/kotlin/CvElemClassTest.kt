import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.memScoped
import root.Point

// CV-elem-class: std::vector<Point> where Point is a user-defined wrapped struct.
// The "user type inside a generated container" case — transitive instantiation
// off Point's @CppBinding("Point"). Construct + field access verified directly on
// the Point binding; push + size verify the container; and read_points_back_out
// verifies element read-back (operator[]/at/front/back wrap the element pointer).
class CvElemClassTest {
    @Test fun point_binding_fields_work() = memScoped {
        val p = with(Point) { Point__int_int(3, 4) }
        assertEquals(3, p.x)
        assertEquals(4, p.y)
    }

    @Test fun push_points_into_vector() = memScoped {
        val v = cppVector<Point>()
        assertEquals(0uL, v.size())
        v.push_back(with(Point) { Point__int_int(1, 2) })
        v.push_back(with(Point) { Point__int_int(5, 6) })
        assertEquals(2uL, v.size())
        assertFalse(v.empty())
    }

    // The read-back the matrix flagged as untested: operator[]/at/front/back
    // return a Point wrapping the element's own pointer, so the struct fields
    // read straight off the stored element.
    @Test fun read_points_back_out() = memScoped {
        val v = cppVector<Point>()
        v.push_back(with(Point) { Point__int_int(1, 2) })
        v.push_back(with(Point) { Point__int_int(5, 6) })

        val p0 = v[0uL]
        assertEquals(1, p0?.x)
        assertEquals(2, p0?.y)
        val p1 = v.at(1uL)
        assertEquals(5, p1?.x)
        assertEquals(6, p1?.y)

        assertEquals(1, v.front()?.x)
        assertEquals(2, v.front()?.y)
        assertEquals(5, v.back()?.x)
        assertEquals(6, v.back()?.y)
    }
}
