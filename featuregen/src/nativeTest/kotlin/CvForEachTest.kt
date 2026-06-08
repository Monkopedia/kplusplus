import com.monkopedia.kplusplus.cppVector
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals

// CV-foreach: the synthesized `operator fun iterator()` on Vector__Int makes
// `for (x in v)` and the whole Iterable surface (map/toList/sumOf/…) work — it
// loops `0 until size()` over the existing index get, no C++-iterator wrapping.
//
// Vector<int>'s index get returns an element POINTER (CValuesRef<IntVar>?), so the
// iterator yields pointers (faithful to the existing subscript surface); we deref
// each one to read the Int — exactly like the v[i] reads in CvIndexGetTest.
class CvForEachTest {
    private fun MemScope.deref(p: CValuesRef<IntVar>?): Int =
        p?.getPointer(this)?.pointed?.value ?: error("null element")

    @Test fun for_in_loop_visits_every_element() = memScoped {
        val v = cppVector<Int>()
        val input = listOf(3, 1, 4, 1, 5, 9)
        input.forEach { v.push_back(it) }
        val seen = mutableListOf<Int>()
        for (x in v) {
            seen += deref(x)
        }
        assertEquals(input, seen)
    }

    @Test fun toList_collects_all_elements() = memScoped {
        val v = cppVector<Int>()
        listOf(10, 20, 30).forEach { v.push_back(it) }
        // toList() comes for free from the iterator(); map the pointers to Ints.
        val asInts = v.toList().map { deref(it) }
        assertEquals(listOf(10, 20, 30), asInts)
    }

    @Test fun map_transforms_each_element() = memScoped {
        val v = cppVector<Int>()
        listOf(1, 2, 3, 4).forEach { v.push_back(it) }
        val doubled = v.map { deref(it) * 2 }
        assertEquals(listOf(2, 4, 6, 8), doubled)
    }

    @Test fun sumOf_folds_over_elements() = memScoped {
        val v = cppVector<Int>()
        val input = listOf(1, 2, 3, 4, 5)
        input.forEach { v.push_back(it) }
        val sum = v.sumOf { deref(it) }
        assertEquals(15, sum)
    }

    @Test fun empty_vector_iterates_zero_times() = memScoped {
        val v = cppVector<Int>()
        var count = 0
        for (x in v) count++
        assertEquals(0, count)
        assertEquals(emptyList(), v.toList())
    }
}
