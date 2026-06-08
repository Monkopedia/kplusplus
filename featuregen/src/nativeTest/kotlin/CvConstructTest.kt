import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// CV-construct: an empty std::vector<int> via the transparent facade.
class CvConstructTest {
    @Test fun empty_vector_has_zero_size() = memScoped {
        val v = cppVector<Int>()
        assertEquals(0uL, v.size())
        assertTrue(v.empty())
    }
}
