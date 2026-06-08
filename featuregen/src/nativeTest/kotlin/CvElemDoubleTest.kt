import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals

// CV-elem-double: element-type generality — std::vector<double>.
class CvElemDoubleTest {
    @Test fun construct_push_size() = memScoped {
        val v = cppVector<Double>()
        v.push_back(1.5)
        v.push_back(-2.5)
        v.push_back(3.14)
        assertEquals(3uL, v.size())
    }
}
