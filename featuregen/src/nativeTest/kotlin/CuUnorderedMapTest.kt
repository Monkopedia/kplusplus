import com.monkopedia.kplusplus.cppUnorderedMap
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// CU-unordered-map: std::unordered_map<int,int> via the cppUnorderedMap<K,V> facade.
//
// The key→value accessors at/operator[]/count are recovered by the associative
// key_type/mapped_type typedef reducer: libstdc++'s unordered_map defines those
// member typedefs through its _Hashtable base as unresolvable dependent
// self-references (`typename _Hashtable::key_type` / `::mapped_type`), so the
// generator reconstructs them from the container's own template params (_Key/_Tp).
// (std::map defines them directly over _Key/_Tp and was already fine.) insert and
// the iterator-returning members (find/equal_range/begin/end) stay dropped — they
// need the real value_type pair + iterator wrapping — tracked as a follow-up.
class CuUnorderedMapTest {
    @Test fun empty_map_is_empty() = memScoped {
        val m = cppUnorderedMap<Int, Int>()
        assertTrue(m.empty())
        assertEquals(0uL, m.size())
    }

    @Test fun reserve_and_clear_keep_it_empty() = memScoped {
        val m = cppUnorderedMap<Int, Int>()
        m.reserve(16uL)
        assertTrue(m.empty())
        m.clear()
        assertTrue(m.empty())
        assertEquals(0uL, m.size())
    }

    @Test fun subscript_inserts_and_reads_back() = memScoped {
        val m = cppUnorderedMap<Int, Int>()
        // operator[] default-inserts the key, returning a pointer to the mapped slot.
        m[1]?.getPointer(this)?.pointed?.value = 10
        m[7]?.getPointer(this)?.pointed?.value = 70
        assertEquals(10, m[1]?.getPointer(this)?.pointed?.value)
        assertEquals(70, m[7]?.getPointer(this)?.pointed?.value)
        // at() reads an existing key's mapped value.
        assertEquals(70, m.at(7)?.getPointer(this)?.pointed?.value)
        assertEquals(2uL, m.size())
    }

    @Test fun count_is_membership() = memScoped {
        val m = cppUnorderedMap<Int, Int>()
        m[1]?.getPointer(this)?.pointed?.value = 10
        assertEquals(1uL, m.count(1))
        assertEquals(0uL, m.count(99))
    }
}
