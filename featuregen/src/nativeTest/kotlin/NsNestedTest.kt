import geo.detail.Impl
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// NS-nested: a two-level C++ namespace `geo::detail { struct Impl; }` maps to
// `package geo.detail`, class `Impl`, imported as `geo.detail.Impl`. The C
// wrapper symbols are prefixed `geo_detail_Impl_*`. Confirms the namespace
// chain `::` -> `.` + decapitalize produces a nested Kotlin package.
class NsNestedTest {
    @Test fun construct_and_field_round_trip() = memScoped {
        val ms = this
        val i = with(Impl) { ms.Impl__int(21) }
        assertEquals(21, i.tag)
        i.tag = 5
        assertEquals(5, i.tag)
    }

    @Test fun default_ctor_and_const_method() = memScoped {
        val ms = this
        val d = with(Impl) { ms.Impl() }
        assertEquals(0, d.tag)
        val i = with(Impl) { ms.Impl__int(9) }
        assertEquals(18, i.doubled())
    }
}
