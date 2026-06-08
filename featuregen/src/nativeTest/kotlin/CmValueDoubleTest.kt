import com.monkopedia.kplusplus.cppMap
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals

// CM-value-double: value-type generality — std::map<int,double>.
class CmValueDoubleTest {
    @Test fun double_values_round_trip() = memScoped {
        val m = cppMap<Int, Double>()
        m[1]?.getPointer(this)?.pointed?.value = 1.5
        m[2]?.getPointer(this)?.pointed?.value = -2.5
        assertEquals(1.5, m[1]?.getPointer(this)?.pointed?.value)
        assertEquals(-2.5, m[2]?.getPointer(this)?.pointed?.value)
    }
}
