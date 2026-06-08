import kotlinx.cinterop.memScoped
import root.Color
import root.EnumFeature
import kotlin.test.Test
import kotlin.test.assertEquals

// EN-scoped: `enum class Color : int { Red=0, Green=1, Blue=2 }` with static
// methods taking/returning Color. krapper now synthesizes a real Kotlin
// `enum class Color(val value: Int)`, so the methods surface as Color -> Color.
// Across the boundary the binding passes `c.value` to the C call and wraps the
// returned int via `Color.fromValue(...)`, round-tripping through the real C++
// enum logic.
class EnScopedTest {
    @Test fun enum_class_values() {
        assertEquals(0, Color.Red.value)
        assertEquals(1, Color.Green.value)
        assertEquals(2, Color.Blue.value)
        assertEquals(Color.Green, Color.fromValue(1))
    }

    @Test fun fromValue_out_of_range_does_not_crash() {
        // Ticket #7 fix #1: an out-of-range value (flag combo / newer ABI) must NOT throw
        // NoSuchElementException; fromValue falls back to the first entry.
        assertEquals(Color.Red, Color.fromValue(999))
    }

    @Test fun next_cycles_through_values() = memScoped {
        val ms = this
        with(EnumFeature) {
            assertEquals(Color.Green, ms.next(Color.Red))
            assertEquals(Color.Blue, ms.next(Color.Green))
            assertEquals(Color.Red, ms.next(Color.Blue)) // wraps via %3
        }
    }

    @Test fun to_and_from_int_round_trip() = memScoped {
        val ms = this
        with(EnumFeature) {
            assertEquals(0, ms.toInt(Color.Red))
            assertEquals(2, ms.toInt(Color.Blue))
            // fromInt -> toInt is identity over the enum's value range
            assertEquals(Color.Green, ms.fromInt(1))
            assertEquals(2, ms.toInt(ms.fromInt(2)))
        }
    }
}
