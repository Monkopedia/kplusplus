import acolor.Tag
import bflavor.Tag as FlavorTag
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// NS-collision: same short name `Tag` in two namespaces `acolor` and `bflavor`.
// Because the Kotlin package is derived from the full C++ qualified name, the
// two land in distinct packages (`acolor.Tag` vs `bflavor.Tag`) with distinct C
// symbol prefixes (`acolor_Tag_*` vs `bflavor_Tag_*`) — no link or runtime
// clash. In one Kotlin file the two short names DO collide at import level, so
// one must be aliased (`import bflavor.Tag as FlavorTag`); both are still fully
// usable. The fields/methods of each are distinct.
class NsCollisionTest {
    @Test fun color_tag_distinct_from_flavor_tag() = memScoped {
        val c = with(Tag) { Tag__int_int_int(10, 20, 30) }
        assertEquals(10, c.r)
        assertEquals(20, c.g)
        assertEquals(30, c.b)
        assertEquals(60, c.sum())

        val f = with(FlavorTag) { Tag__int_int(4, 5) }
        assertEquals(4, f.sweet)
        assertEquals(5, f.sour)
        assertEquals(9, f.total())
    }

    @Test fun both_default_construct_independently() = memScoped {
        val c = with(Tag) { Tag() }
        val f = with(FlavorTag) { Tag() }
        assertEquals(0, c.r)
        assertEquals(0, f.sweet)
    }
}
