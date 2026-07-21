import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Box

// UT-box: a user class template `Box<T>` instantiated from Kotlin as `Box<Int>()`
// via the generated interface + same-named scoped factory (the kplusplus compiler
// plugin refines the call to the concrete `Box__Int` binding).
class UtBoxTest {
    @Test fun box_int_roundtrips() = memScoped {
        val b = Box<Int>()
        b.set(7)
        assertEquals(7, b.get())
    }
}
