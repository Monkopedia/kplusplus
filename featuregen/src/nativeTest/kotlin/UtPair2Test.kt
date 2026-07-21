import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Pair2

// UT-pair2: a user class template over TWO type params, instantiated from Kotlin as
// `Pair2<Int, Double>()` via the generated `interface Pair2<T1, T2>` + same-named
// scoped factory. The kplusplus compiler plugin refines the call to the concrete
// `Pair2__Int__Double` binding. Exercises the arity>1 facade/plugin path.
class UtPair2Test {
    @Test fun pair2_int_double_roundtrips() = memScoped {
        val p = Pair2<Int, Double>()
        p.setA(7)
        p.setB(1.5)
        assertEquals(7, p.getA())
        assertEquals(1.5, p.getB())
    }
}
