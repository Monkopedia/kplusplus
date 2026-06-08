import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals

// CV-iterate: traverse by index using size() + subscript.
class CvIterateTest {
    @Test fun sum_and_count_via_index() = memScoped {
        val v = cppVector<Int>()
        val input = listOf(1, 2, 3, 4, 5)
        input.forEach { v.push_back(it) }
        var sum = 0
        var count = 0
        var i = 0uL
        while (i < v.size()) {
            sum += v[i]?.getPointer(this)?.pointed?.value ?: 0
            count++
            i++
        }
        assertEquals(15, sum)
        assertEquals(input.size, count)
    }
}
