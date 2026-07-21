import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import root.Button
import root.Drawable
import root.DrawableApi
import root.DrawableProbe

// IH-abstract: `Drawable` has a pure-virtual draw()/kind() so it's an abstract C++
// class. Its constructors are dropped in resolution, so NO `Drawable()` construction
// factory is emitted (a generated comment notes the absence) — only the wrapper +
// the `DrawableApi` interface. `Button` is a concrete subclass that implements both
// pure virtuals; it constructs normally and is usable as a `DrawableApi` with virtual
// dispatch (kind() -> 7). DrawableProbe takes/returns a `Drawable*` (-> `DrawableApi?`).
//
// Note there is intentionally no test that constructs `Drawable` directly: the
// abstract class has no `MemScope.Drawable()` factory to call. That absence IS the
// behavior under test — the only way to obtain a DrawableApi is via a concrete Button.
class IhAbstractTest {
    // Button constructs (concrete) and its own kind() is Button's override (7).
    @Test fun concrete_button_constructs_and_dispatches() = memScoped {
        val b = with(Button) { Button() }
        assertEquals(7, b.kind())
        // The concrete field is usable too.
        b.painted = 3
        assertEquals(3, b.painted)
    }

    // Button used through the abstract base interface: kind() dispatches to Button's
    // override even when the static type is the abstract `DrawableApi`.
    @Test fun button_as_drawable_api_dispatches() = memScoped {
        val b = with(Button) { Button() }
        val asAbstract: DrawableApi = b
        assertEquals(7, asAbstract.kind())
        // draw() is a pure-virtual void; calling it through the base just runs the impl.
        asAbstract.draw()
    }

    // IH-upcast through the probe: a Button passed where a `Drawable*` is wanted
    // dispatches kind() to Button's override (7), and the returned Drawable* upcast
    // still dispatches to the Button it really is.
    @Test fun button_upcast_through_drawable_probe() = memScoped {
        val b = with(Button) { Button() }
        assertEquals(7, with(DrawableProbe) { kindOf(b) })
        val back: DrawableApi? = with(DrawableProbe) { identity(b) }
        assertTrue(back != null)
        assertEquals(7, back.kind())
    }
}
