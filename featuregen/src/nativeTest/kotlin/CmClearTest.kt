import com.monkopedia.kplusplus.cppMap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CM-clear: clear() empties the map.
class CmClearTest {
    @Test fun clear_empties() = memScoped {
        val m = cppMap<Int, Int>()
        m[1]?.getPointer(this)?.pointed?.value = 1
        assertFalse(m.empty())
        m.clear()
        assertTrue(m.empty())
    }
}
