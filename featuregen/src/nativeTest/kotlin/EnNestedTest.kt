import ensig.Bus
import ensig.Signal
import machine.State
import root.Machine
import kotlin.test.Test
import kotlin.test.assertEquals

// EN-nested (#10 broad-binding correctness): a nested enum (in a namespace `ensig::Signal`
// or a class `Machine::State`) round-trips through a wrapped static method. The point of
// the fixture is the GENERATED C++: the RAW_CAST / ENUM_RETURN must spell the enum with
// its enclosing scope (`ensig::Signal` / `Machine::State`), never a bare `Signal`/`State`
// that would be an undeclared identifier at the wrapper's file scope. If that regressed,
// featuregen sync would fail to COMPILE the wrapper — so a green build already proves the
// qualification; these assertions additionally prove the round-trip is behaviourally sound.
class EnNestedTest {
    @Test fun namespace_nested_enum_round_trips() {
        // invert: SigLow <-> SigHigh, SigFloat fixed. arg + return are both ensig::Signal.
        assertEquals(Signal.SigHigh, Bus.invert(Signal.SigLow))
        assertEquals(Signal.SigLow, Bus.invert(Signal.SigHigh))
        assertEquals(Signal.SigFloat, Bus.invert(Signal.SigFloat))
        // level: nested-enum arg -> int (ENUM arg cast, plain int return).
        assertEquals(0, Bus.level(Signal.SigLow))
        assertEquals(2, Bus.level(Signal.SigFloat))
    }

    @Test fun class_nested_enum_round_trips() {
        // advance cycles Idle -> Running -> Halted -> Idle; arg + return are Machine::State.
        assertEquals(State.Running, Machine.advance(State.Idle))
        assertEquals(State.Halted, Machine.advance(State.Running))
        assertEquals(State.Idle, Machine.advance(State.Halted))
        assertEquals(2, Machine.stateInt(State.Halted))
    }
}
