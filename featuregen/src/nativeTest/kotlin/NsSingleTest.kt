import geo.Vec
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// NS-single: a struct in a single C++ namespace `geo { struct Vec; }` maps to
// `package geo`, class `Vec`, imported as `geo.Vec`. The C wrapper symbols are
// prefixed `geo_Vec_*`. Construct via default + args ctor, read/write the
// fields, exercise a const method and a static factory.
class NsSingleTest {
    @Test fun construct_and_fields_round_trip() = memScoped {
        val ms = this
        val v = with(Vec) { ms.Vec__double_double(3.0, 4.0) }
        assertEquals(3.0, v.x)
        assertEquals(4.0, v.y)
        v.x = 6.0
        assertEquals(6.0, v.x)
        assertEquals(4.0, v.y)
    }

    @Test fun default_ctor_zeroes() = memScoped {
        val ms = this
        val v = with(Vec) { ms.Vec() }
        assertEquals(0.0, v.x)
        assertEquals(0.0, v.y)
    }

    @Test fun const_method_and_static_factory() = memScoped {
        val ms = this
        val v = with(Vec) { ms.Vec__double_double(3.0, 4.0) }
        assertEquals(25.0, v.lengthSq()) // 3^2 + 4^2
        val m = with(Vec) { ms.make(1.0, 2.0) }
        assertEquals(1.0, m.x)
        assertEquals(2.0, m.y)
    }
}
