import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.Direction
import root.DirectionFeature

// EN-unscoped: plain `enum Direction { North=0, East=1, South=2, West=3 }`. Its
// underlying type is unsigned, so the synthesized Kotlin `enum class
// Direction(val value: UInt)` carries UInt values (vs. the scoped enum's Int).
// Same boundary handling: the binding passes `d.value` and wraps the returned
// `unsigned int` via `Direction.fromValue(...)`, so even an unscoped enum
// round-trips through a real Kotlin enum class.
class EnUnscopedTest {
    @Test fun enum_class_values() {
        assertEquals(0u, Direction.North.value)
        assertEquals(1u, Direction.East.value)
        assertEquals(3u, Direction.West.value)
        assertEquals(Direction.South, Direction.fromValue(2u))
    }

    @Test fun turn_right_cycles() = memScoped {
        with(DirectionFeature) {
            assertEquals(Direction.East, turnRight(Direction.North))
            assertEquals(Direction.South, turnRight(Direction.East))
            assertEquals(Direction.North, turnRight(Direction.West)) // wraps via %4
        }
    }

    @Test fun to_and_from_int_round_trip() = memScoped {
        with(DirectionFeature) {
            assertEquals(0, toInt(Direction.North))
            assertEquals(3, toInt(Direction.West))
            assertEquals(Direction.South, fromInt(2))
            assertEquals(Direction.East, fromInt(5)) // fromInt takes %4 -> East(1)
        }
    }
}
