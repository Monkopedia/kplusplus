import com.monkopedia.kplusplus.cppMap
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.cinterop.memScoped

// CM-construct: empty std::map<int,int>. Note: the generated map binding has no
// size() (dropped), so construct is asserted via empty() only.
class CmConstructTest {
    @Test fun empty_map_is_empty() = memScoped {
        val m = cppMap<Int, Int>()
        assertTrue(m.empty())
    }
}
