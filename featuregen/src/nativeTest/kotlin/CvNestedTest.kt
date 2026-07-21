import com.monkopedia.kplusplus.cppVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import std.Vector__Int

// CV-nested: std::vector<std::vector<int>>. The inner type is the generated
// Vector__Int binding (which cppVector<Int>() itself produces), pushed into an
// outer cppVector<Vector__Int>(). Exercises transitive instantiation (the inner
// std::vector<int> must be generated before the outer).
class CvNestedTest {
    @Test fun push_inner_vectors_and_size() = memScoped {
        val outer = cppVector<Vector__Int>()
        assertEquals(0uL, outer.size())

        val a = cppVector<Int>()
        a.push_back(1)
        a.push_back(2)
        val b = cppVector<Int>()
        b.push_back(3)

        outer.push_back(a)
        outer.push_back(b)

        assertEquals(2uL, outer.size())
        assertFalse(outer.empty())
    }

    // Read the inner vector back out of the outer-element pointer (the caveat the
    // matrix flagged): outer[i] wraps the inner vector's own pointer as a
    // Vector__Int, whose size()/subscript then read the inner ints.
    @Test fun read_inner_vectors_back() = memScoped {
        val outer = cppVector<Vector__Int>()
        val a = cppVector<Int>()
        a.push_back(1)
        a.push_back(2)
        val b = cppVector<Int>()
        b.push_back(3)
        outer.push_back(a)
        outer.push_back(b)

        val inner0 = outer[0uL]
        assertEquals(2uL, inner0?.size())
        assertEquals(1, inner0?.get(0uL)?.getPointer(this)?.pointed?.value)
        assertEquals(2, inner0?.get(1uL)?.getPointer(this)?.pointed?.value)

        val inner1 = outer[1uL]
        assertEquals(1uL, inner1?.size())
        assertEquals(3, inner1?.get(0uL)?.getPointer(this)?.pointed?.value)
    }
}
