import root.Box
import root.Box__Int
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// UT-box-nested: the user template nested in itself — `Box<Box<int>>`. As with the
// cppVector<Vector__Int> two-cycle pattern, the inner `Box<int>` is named at the
// Kotlin level by its concrete @CppBinding binding `Box__Int`, so the outer reads
// as `Box<Box__Int>()`. The plugin refines it to the concrete `Box__Box_int_ :
// Box<Box__Int>`, whose set/get carry the inner Box by pointer.
class UtBoxNestedTest {
    @Test fun box_box_int_roundtrips() = memScoped {
        val inner = Box<Int>()
        inner.set(42)

        val outer = Box<Box__Int>()
        outer.set(inner)

        assertEquals(42, outer.get().get())
    }
}
