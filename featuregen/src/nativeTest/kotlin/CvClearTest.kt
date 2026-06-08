import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// CV-clear: clear() empties the vector.
class CvClearTest {
    @Test fun clear_empties() = memScoped {
        val v = cppVector<Int>()
        v.push_back(1); v.push_back(2)
        v.clear()
        assertEquals(0uL, v.size())
        assertTrue(v.empty())
    }
}
