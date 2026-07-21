import com.monkopedia.kplusplus.cppMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CM-get: read mapped values back via subscript / at(). Both return a pointer to
// the mapped value; deref to read.
class CmGetTest {
    @Test fun reads_back_inserted_values() = memScoped {
        val m = cppMap<Int, Int>()
        m[1]?.getPointer(this)?.pointed?.value = 10
        m[7]?.getPointer(this)?.pointed?.value = 70
        assertEquals(10, m[1]?.getPointer(this)?.pointed?.value)
        assertEquals(70, m[7]?.getPointer(this)?.pointed?.value)
        assertEquals(70, m.at(7)?.getPointer(this)?.pointed?.value)
    }
}
