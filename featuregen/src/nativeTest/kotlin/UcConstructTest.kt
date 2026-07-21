import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Widget

// UC-ctor-default / UC-ctor-args / UC-ctor-overload: the two constructors of the
// plain user class `Widget`. The default ctor surfaces as `Widget()` and zeroes
// the fields; the args ctor gets a content-based overload name `Widget__int_double`
// and both int + double args round-trip. Both entry points are reachable from the
// single `Widget` binding, exercising overload selection.
class UcConstructTest {
    @Test fun default_ctor_zeroes_fields() = memScoped {
        val w = with(Widget) { Widget() }
        assertEquals(0, w.count)
        assertEquals(0.0, w.scale)
    }

    @Test fun args_ctor_reflects_arguments() = memScoped {
        val w = with(Widget) { Widget__int_double(3, 1.5) }
        assertEquals(3, w.count)
        assertEquals(1.5, w.scale)
    }

    // UC-ctor-overload: both ctors usable from one binding, each with the right state.
    @Test fun both_ctors_usable_from_one_binding() = memScoped {
        with(Widget) {
            val def = Widget()
            val args = Widget__int_double(7, 2.25)
            assertEquals(0, def.count)
            assertEquals(0.0, def.scale)
            assertEquals(7, args.count)
            assertEquals(2.25, args.scale)
        }
    }
}
