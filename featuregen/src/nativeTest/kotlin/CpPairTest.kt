import com.monkopedia.kplusplus.cppPair
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import std.Pair__Int__Int

// CP-pair: std::pair<int,int> via the transparent cppPair<A,B>() facade. pair's
// first/second are plain fields, which generate as var accessors.
class CpPairTest {
    @Test fun first_second_round_trip() = memScoped {
        val p = cppPair<Int, Int>()
        p.first = 3
        p.second = 7
        assertEquals(3, p.first)
        assertEquals(7, p.second)
    }

    @Test fun fields_are_independent() = memScoped {
        val p = cppPair<Int, Int>()
        p.first = 100
        p.second = 200
        p.first = 101
        assertEquals(101, p.first)
        assertEquals(200, p.second)
    }
}
