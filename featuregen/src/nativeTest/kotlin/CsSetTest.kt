import com.monkopedia.kplusplus.cppSet
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// CS-set: std::set<int> via the cppSet<T> facade.
//
// insert(const value_type&) returns std::pair<iterator, bool>; the iterator half is
// an unwrappable nested class, so the wrapper rewrites the return to the pair's
// `.second` (was-newly-inserted bool). count() gives membership (0/1), recovered via
// the value_type/key_type set reducer + the size_type typedef fix; erase(key) removes.
class CsSetTest {
    @Test fun insert_reports_newly_inserted_and_membership() = memScoped {
        val s = cppSet<Int>()
        assertTrue(s.insert(7), "first insert of 7 is newly inserted")
        assertFalse(s.insert(7), "second insert of 7 is already present")
        assertEquals(1uL, s.count(7))
        assertEquals(0uL, s.count(8))
        assertEquals(1uL, s.size())
        s.erase(7)
        assertEquals(0uL, s.count(7))
    }
}
