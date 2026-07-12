import kotlinx.cinterop.memScoped
import root.ByteFlags
import root.Flag
import root.FlagProbe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// OP-compound-assign-enum (#105 Family 2): a REFERENCE-return of a value-reduced type.
//
// `ByteFlags& operator|=/&=/^=(Flag)` returns a self-reference to the receiver (a real
// class), while `FlagProbe::orInto` returns `Flag&` — a reference to an ENUM, which is
// value-reduced at the boundary (an enum has no `.ptr` storage to wrap). On the unfixed
// generator the enum-reference return was routed through ENUM_RETURN with the reference's
// `void*` cType: the C wrapper emitted the ill-formed `return (void*)(enumLvalue)` and the
// Kotlin fed that pointer into `fromValue(Int)` — neither compiled. The fix strips the
// reference so an enum-reference return resolves as a by-value enum (wrapper returns the
// underlying integer, Kotlin reads it via `fromValue`), while non-enum (class) reference
// returns keep their pointer marshaling. This test proves both shapes bind + behave: the
// operators MUTATE and the returned value/reference is usable.
class OpCompoundAssignEnumTest {

    // FlagProbe::orInto returns `Flag&` (the value-reduced enum reference). The returned
    // Flag reflects the accumulated static state, and mutation is observable via stateVal().
    @Test fun enum_reference_return_reads_the_value() = memScoped {
        with(FlagProbe) {
            reset()
            assertEquals(Flag.FlagNone, orInto(Flag.FlagNone))
            assertEquals(0, stateVal())

            // Each call ORs into the shared state and returns the mutated enum value.
            val a = orInto(Flag.FlagA)
            assertNotNull(a)
            assertEquals(Flag.FlagA, a)
            assertEquals(1, stateVal())

            // OR in FlagB -> combined 1|2 = 3, which is out of the named-entry range, so
            // fromValue falls back to the first entry (FlagNone) — but the underlying C++
            // state really is 3, proven via stateVal().
            orInto(Flag.FlagB)
            assertEquals(3, stateVal())

            // OR in FlagC (4) -> 3|4 = 7.
            orInto(Flag.FlagC)
            assertEquals(7, stateVal())
        }
    }

    // ByteFlags& operator|=/&=/^= (class-type reference return): mutation is observable
    // and the returned wrapper is the receiver over the same storage.
    @Test fun class_reference_return_mutates_and_chains() = memScoped {
        with(ByteFlags) {
            val bf = ByteFlags()
            assertEquals(0, bf.value())

            val r = bf.operator_or_eq(Flag.FlagA)
            assertNotNull(r)
            assertEquals(1, bf.value())
            // the returned wrapper is the same receiver storage
            assertEquals(1, r.value())

            bf.operator_or_eq(Flag.FlagC) // 1 | 4 = 5
            assertEquals(5, bf.value())

            bf.operator_and_eq(Flag.FlagC) // 5 & 4 = 4
            assertEquals(4, bf.value())

            bf.operator_xor_eq(Flag.FlagC) // 4 ^ 4 = 0
            assertEquals(0, bf.value())
        }
    }
}
