import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped

// CV-construct: an empty std::vector<int> via the transparent facade.
class CvConstructTest {
    @Test fun empty_vector_has_zero_size() = memScoped {
        val v = cppVector<Int>()
        assertEquals(0uL, v.size())
        assertTrue(v.empty())
    }
}
