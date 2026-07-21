import com.monkopedia.kplusplus.cppMap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CM-insert: insert via subscript. operator[] (generated `get`) default-inserts
// and returns a pointer to the mapped value; write through it to set.
class CmInsertTest {
    @Test fun subscript_inserts() = memScoped {
        val m = cppMap<Int, Int>()
        assertTrue(m.empty())
        m[1]?.getPointer(this)?.pointed?.value = 100
        m[2]?.getPointer(this)?.pointed?.value = 200
        assertFalse(m.empty())
    }
}
