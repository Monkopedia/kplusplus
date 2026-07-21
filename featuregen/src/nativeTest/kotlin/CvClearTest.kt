import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped

// CV-clear: clear() empties the vector.
class CvClearTest {
    @Test fun clear_empties() = memScoped {
        val v = cppVector<Int>()
        v.push_back(1)
        v.push_back(2)
        v.clear()
        assertEquals(0uL, v.size())
        assertTrue(v.empty())
    }
}
