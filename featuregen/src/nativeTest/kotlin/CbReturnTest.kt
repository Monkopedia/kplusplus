import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import root.CbProbe

// CB-cfnptr-ret / CB-cfnptr-arg (stage 1, C->Kotlin direction): a method whose
// parameter or return type is a C function-pointer typedef (`typedef int (*IntTransform)(int)`)
// now survives generation instead of being silently dropped. The typedef surfaces on
// the Kotlin side as the raw cinterop `CPointer<CFunction<(Int) -> Int>>?`, which is:
//   - callable: a returned pointer is invoked directly (`fn(21)`), no CFunction leak, and
//   - re-passable: the same pointer is accepted back by another method unchanged.
// No Kotlin-lambda -> C trampoline is built here (that is the Mode-1 stage); this is the
// already-real C pointer round-tripping, the "separate, easy" half of the callbacks design.
class CbReturnTest {
    @Test fun returned_function_pointer_is_callable_and_repassable() = memScoped {
        with(CbProbe) {
            val fn = makeDouble()
            assertNotNull(fn)
            assertEquals(42, fn(21))
            assertEquals(10, applyTransform(5, fn))
        }
    }
}
