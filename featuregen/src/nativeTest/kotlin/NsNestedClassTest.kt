import outer.Inner
import root.Outer
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// NS-nested-class: a C++ nested class `Outer::Inner` does NOT collide in `root`
// (the review's prediction). Instead krapper turns the outer-class name into a
// *package* segment: `Outer::Inner` -> `package outer`, class `Inner`, imported
// as `outer.Inner`, with C symbols prefixed `Outer_Inner_*`. The outer class
// itself stays `root.Outer`. Both are usable side by side with distinct imports
// (no clash, because Inner lives in package `outer` and Outer in `root`).
class NsNestedClassTest {
    @Test fun outer_constructs_in_root() = memScoped {
        val ms = this
        val o = with(Outer) { ms.Outer() }
        assertEquals(7, o.who)
        assertEquals(7, o.whoVal())
    }

    @Test fun inner_constructs_in_outer_package() = memScoped {
        val ms = this
        val i = with(Inner) { ms.Inner__int(42) }
        assertEquals(42, i.v)
        assertEquals(42, i.get())
        i.v = 3
        assertEquals(3, i.v)
    }

    @Test fun inner_default_ctor_and_both_usable_together() = memScoped {
        val ms = this
        val o = with(Outer) { ms.Outer() }
        val i = with(Inner) { ms.Inner() }
        assertEquals(7, o.who)
        assertEquals(0, i.v)
    }
}
