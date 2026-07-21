import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CV-front-back: front()/back() now resolve (reference-typedef fix).
class CvFrontBackTest {
    @Test fun front_and_back() = memScoped {
        val v = cppVector<Int>()
        v.push_back(11)
        v.push_back(22)
        v.push_back(33)
        assertEquals(11, v.front()?.getPointer(this)?.pointed?.value)
        assertEquals(33, v.back()?.getPointer(this)?.pointed?.value)
    }
}
