import com.monkopedia.kplusplus.cppMap
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals

// CM-size: size() recovered by the size_type typedef fix.
class CmSizeTest {
    @Test fun size_reflects_distinct_keys() = memScoped {
        val m = cppMap<Int, Int>()
        assertEquals(0uL, m.size())
        m[1]?.getPointer(this)?.pointed?.value = 10
        m[2]?.getPointer(this)?.pointed?.value = 20
        assertEquals(2uL, m.size())
        // re-inserting an existing key doesn't grow
        m[1]?.getPointer(this)?.pointed?.value = 11
        assertEquals(2uL, m.size())
    }
}
