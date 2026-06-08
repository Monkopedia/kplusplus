import kotlinx.cinterop.memScoped
import root.Gadget
import root.Sprocket
import kotlin.test.Test
import kotlin.test.assertEquals

// IH-nvdtor (decision B diagnostic): `Gadget` is polymorphic (virtual tick() and
// used as a base by Sprocket) but its destructor is non-virtual. The generator
// emits a non-fatal `WARNING: polymorphic class with a non-virtual destructor ...`
// comment on the Gadget (and Sprocket) bindings. The diagnostic must NOT break
// generation: this test's job is to confirm both bindings are still produced and
// usable — construction, the virtual method, and the override all work normally.
class IhNvDtorTest {
    // The flagged base binding still generates and is fully usable.
    @Test fun polymorphic_base_with_nonvirtual_dtor_still_works() = memScoped {
        val ms = this
        val g = with(Gadget) { ms.Gadget() }
        assertEquals(0, g.read())
        g.tick()
        g.tick()
        assertEquals(2, g.read())
    }

    // The derived (which inherits the non-virtual dtor) still generates; its virtual
    // override keeps the bare name and dispatches to Sprocket's own impl.
    @Test fun derived_inheriting_nonvirtual_dtor_still_works() = memScoped {
        val ms = this
        val s = with(Sprocket) { ms.Sprocket() }
        assertEquals(0, s.spun())
        s.tick()       // override -> increments spins, not ticks
        s.tick()
        assertEquals(2, s.spun())
        assertEquals(0, s.read()) // base counter untouched by the override
    }
}
