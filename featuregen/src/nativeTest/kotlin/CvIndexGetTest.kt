import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

// CV-index-get: read elements back by index. operator[]/at now resolve (the
// reference-typedef fix) and return a pointer to the element; deref to read.
class CvIndexGetTest {
    @Test fun read_back_by_index() = memScoped {
        val v = cppVector<Int>()
        v.push_back(10)
        v.push_back(20)
        v.push_back(30)
        assertEquals(10, v[0uL]?.getPointer(this)?.pointed?.value)
        assertEquals(20, v[1uL]?.getPointer(this)?.pointed?.value)
        assertEquals(30, v[2uL]?.getPointer(this)?.pointed?.value)
    }

    @Test fun at_matches_subscript() = memScoped {
        val v = cppVector<Int>()
        v.push_back(7)
        v.push_back(8)
        assertEquals(7, v.at(0uL)?.getPointer(this)?.pointed?.value)
        assertEquals(8, v.at(1uL)?.getPointer(this)?.pointed?.value)
    }
}
