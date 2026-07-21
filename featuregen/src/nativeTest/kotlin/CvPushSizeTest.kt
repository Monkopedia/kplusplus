import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.memScoped

// CV-push-size: push_back grows size().
class CvPushSizeTest {
    @Test fun size_reflects_pushes() = memScoped {
        val v = cppVector<Int>()
        v.push_back(10)
        v.push_back(20)
        v.push_back(30)
        assertEquals(3uL, v.size())
        assertFalse(v.empty())
    }

    @Test fun size_grows_one_per_push() = memScoped {
        val v = cppVector<Int>()
        for (i in 1..5) {
            v.push_back(i)
            assertEquals(i.toULong(), v.size())
        }
    }
}
