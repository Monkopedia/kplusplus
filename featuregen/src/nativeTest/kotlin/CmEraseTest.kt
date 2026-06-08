import com.monkopedia.kplusplus.cppMap
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals

// CM-erase: erase(key) recovered by the size_type typedef fix (it returns the
// number of elements removed, a size_type).
class CmEraseTest {
    @Test fun erase_by_key_removes() = memScoped {
        val m = cppMap<Int, Int>()
        m[1]?.getPointer(this)?.pointed?.value = 10
        m[2]?.getPointer(this)?.pointed?.value = 20
        assertEquals(2uL, m.size())

        assertEquals(1uL, m.erase(1)) // removed one
        assertEquals(0uL, m.count(1))
        assertEquals(1uL, m.size())

        assertEquals(0uL, m.erase(99)) // nothing to remove
    }
}
