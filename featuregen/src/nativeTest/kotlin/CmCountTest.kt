import com.monkopedia.kplusplus.cppMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CM-count: count() recovered by the size_type typedef fix.
class CmCountTest {
    @Test fun count_is_membership() = memScoped {
        val m = cppMap<Int, Int>()
        m[1]?.getPointer(this)?.pointed?.value = 10
        assertEquals(1uL, m.count(1))
        assertEquals(0uL, m.count(99))
    }
}
