import com.monkopedia.kplusplus.cppUnorderedSet
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// CU-unordered-set: std::unordered_set<int> via the cppUnorderedSet<T> facade.
//
// Like std::set, the membership surface now works: the set-style value_type
// reducer recovers the collapsed value_type/key_type (→ element template param),
// and the pair<iterator,bool>→bool rewrite gives `insert` a Boolean return. Real
// iteration (find/begin/end) stays dropped — needs nested iterator-class wrapping.
class CuUnorderedSetTest {
    @Test fun empty_set_is_empty() = memScoped {
        val s = cppUnorderedSet<Int>()
        assertTrue(s.empty())
        assertEquals(0uL, s.size())
    }

    @Test fun reserve_and_clear_keep_it_empty() = memScoped {
        val s = cppUnorderedSet<Int>()
        s.reserve(8uL)
        assertTrue(s.empty())
        s.clear()
        assertTrue(s.empty())
        assertEquals(0uL, s.size())
    }

    @Test fun insert_count_erase() = memScoped {
        val s = cppUnorderedSet<Int>()
        assertTrue(s.insert(7))          // newly inserted
        assertFalse(s.insert(7))         // already present
        assertEquals(1uL, s.count(7))
        assertEquals(0uL, s.count(8))
        assertEquals(1uL, s.size())
        s.erase(7)
        assertEquals(0uL, s.count(7))
        assertTrue(s.empty())
    }
}
