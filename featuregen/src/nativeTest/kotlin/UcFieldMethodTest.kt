import kotlinx.cinterop.memScoped
import root.Widget
import kotlin.test.Test
import kotlin.test.assertEquals

// UC-field-rw / UC-method-void / UC-method-ret / UC-const-method: public field
// read+write, the void mutator `reset()`, the value-returning `compute(x)`, and
// the const `scaled()`. `count`/`scale` surface as read/write `var`s; reset()
// zeroes them in place; compute() returns count+x; scaled() returns count*scale
// without mutating.
class UcFieldMethodTest {
    // UC-field-rw: write then read back, both fields independent across types.
    @Test fun fields_read_write_independently() = memScoped {
        val w = with(Widget) { Widget() }
        w.count = 11
        assertEquals(11, w.count)
        assertEquals(0.0, w.scale) // unchanged by count write
        w.scale = 4.5
        assertEquals(4.5, w.scale)
        assertEquals(11, w.count) // unchanged by scale write
    }

    // UC-method-void: reset() mutates state, observable via field read after.
    @Test fun reset_zeroes_fields() = memScoped {
        val w = with(Widget) { Widget__int_double(9, 3.0) }
        assertEquals(9, w.count)
        w.reset()
        assertEquals(0, w.count)
        assertEquals(0.0, w.scale)
    }

    // UC-method-ret: compute(x) returns count + x; arg + state round-trip.
    @Test fun compute_returns_count_plus_arg() = memScoped {
        val w = with(Widget) { Widget__int_double(10, 0.0) }
        assertEquals(15, w.compute(5))
        assertEquals(10, w.compute(0))   // boundary: zero arg
        assertEquals(9, w.compute(-1))   // boundary: negative arg
    }

    // UC-const-method: scaled() = count * scale, and calling it does not mutate.
    @Test fun scaled_is_pure_const_method() = memScoped {
        val w = with(Widget) { Widget__int_double(4, 2.5) }
        assertEquals(10.0, w.scaled())
        // const method must not mutate the receiver
        assertEquals(4, w.count)
        assertEquals(2.5, w.scale)
        assertEquals(10.0, w.scaled()) // stable across repeated calls
    }
}
