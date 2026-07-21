import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import root.Box

// UT-box-abstract: the generated `interface Box<T>` now carries the template's
// T-preserving method surface (`fun set(x: T)`, `fun get(): T`), so callers can
// abstract over `Box<Int>` through the interface — `readBox` takes the interface
// type and calls `get()` on it. (Previously `Box<T>` was an empty marker and this
// did not compile.) `get()` returns `T`, a position a wrapper instantiation would
// allocate, so the interface method carries `context(scope: MemScope)` uniformly —
// hence `readBox` declares the same context.
context(scope: MemScope)
fun readBox(b: Box<Int>): Int = b.get()

class UtBoxAbstractTest {
    @Test fun box_abstracts_over_interface() = memScoped {
        val b = Box<Int>()
        b.set(9)
        assertEquals(9, readBox(b))
    }
}
