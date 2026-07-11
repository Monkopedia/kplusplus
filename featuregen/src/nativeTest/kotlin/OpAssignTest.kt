import kotlinx.cinterop.memScoped
import root.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// OP-assign / OP-pluseq: the assignment-style operators on Vec2.
//
// Both `operator=` and `operator+=` return `Vec2&` in C++ (a self-reference).
// krapper surfaces that, like any `T&` return (OB-return-ref), as a non-owning
// `Vec2?` wrapper over the same storage: `infix fun assign(o): Vec2?` /
// `infix fun plusEquals(o): Vec2?`. So the in-place mutation is observable via field
// reads AND the returned reference is usable for chaining (e.g.
// `(a plusEquals b)!! plusEquals c`).
class OpAssignTest {

    // OP-assign: after `a assign b`, a's fields match b's; b is unchanged. The call
    // returns the receiver `a` (same backing storage), so reads through the result
    // see the mutated values.
    @Test fun assign_copies_fields() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val b = Vec2__double_double(8.0, 9.0)
            val r = a assign b
            assertEquals(8.0, a.x)
            assertEquals(9.0, a.y)
            // the returned reference is the receiver `a` over the same storage
            assertNotNull(r)
            assertEquals(8.0, r.x)
            assertEquals(9.0, r.y)
            // b unchanged
            assertEquals(8.0, b.x)
            assertEquals(9.0, b.y)
        }
    }

    // OP-pluseq: `a plusEquals b` updates a in-place (a.x += b.x, a.y += b.y);
    // b is unchanged; observable via field read after the call.
    @Test fun plus_equals_mutates_in_place() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val b = Vec2__double_double(3.0, 4.0)
            a plusEquals b
            assertEquals(4.0, a.x)
            assertEquals(6.0, a.y)
            // b unchanged
            assertEquals(3.0, b.x)
            assertEquals(4.0, b.y)
            // a second compound assignment accumulates onto the same storage
            a plusEquals b
            assertEquals(7.0, a.x)
            assertEquals(10.0, a.y)
        }
    }

    // OP-pluseq chaining: `a plusEquals b` returns the receiver, so the result is a
    // usable Vec2 and folding more onto it accumulates onto the same `a`.
    @Test fun plus_equals_returns_receiver_and_chains() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val b = Vec2__double_double(3.0, 4.0)
            // the returned reference is `a` over the same storage
            val r = a plusEquals b
            assertNotNull(r)
            assertEquals(4.0, r.x)
            assertEquals(6.0, r.y)
            r.x = 100.0
            assertEquals(100.0, a.x)

            // chain: c then d are folded onto a (which already absorbed b above, then
            // was bumped to x=100), via the reference plusEquals returns.
            val c = Vec2__double_double(1.0, 1.0)
            val d = Vec2__double_double(2.0, 2.0)
            // (a plusEquals c) yields a; plusEquals d then folds d onto a too.
            (a plusEquals c)!! plusEquals d
            assertEquals(103.0, a.x) // 100 + 1 + 2
            assertEquals(9.0, a.y) //   6 + 1 + 2
        }
    }

    // OP-assign chaining: `a assign b` returns a, so the result feeds the next
    // assignment — `c assign (a assign b)` lands b's value across the whole chain.
    @Test fun assign_returns_receiver_and_chains() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(1.0, 2.0)
            val b = Vec2__double_double(5.0, 6.0)
            val c = Vec2__double_double(0.0, 0.0)
            // assign b into a, then assign the result (a) into c: both end at b's value
            c assign (a assign b)!!
            assertEquals(5.0, a.x)
            assertEquals(6.0, a.y)
            assertEquals(5.0, c.x)
            assertEquals(6.0, c.y)
        }
    }

    // T-op regression guard: the compound-assignment operators that are NOT in
    // ALL_OPERATORS (`operator-=`, `operator/=`, `operator%=`). On the unfixed
    // generator their C wrapper names leaked the raw `-` / `/` / `%` symbol and
    // truncated to a colliding `..._operator`, so the sync didn't even compile.
    // Hardened `cleanupOperatorName()` now maps each to a DISTINCT valid C name
    // (Vec2_operator_minuseq / _diveq / _modeq) and the methods surface in Kotlin
    // as operator_minus_eq / operator_div_eq / operator_mod_eq, each returning the
    // mutated receiver as a non-owning `Vec2?` (like the other `T&`-returning ops).
    @Test fun minus_equals_mutates_in_place() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(10.0, 8.0)
            val b = Vec2__double_double(3.0, 2.0)
            val r = a.operator_minus_eq(b)
            assertEquals(7.0, a.x)
            assertEquals(6.0, a.y)
            // returns the receiver over the same storage
            assertNotNull(r)
            assertEquals(7.0, r.x)
            assertEquals(6.0, r.y)
            // b unchanged
            assertEquals(3.0, b.x)
            assertEquals(2.0, b.y)
        }
    }

    @Test fun div_equals_scales_in_place() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(8.0, 6.0)
            val r = a.operator_div_eq(2.0)
            assertEquals(4.0, a.x)
            assertEquals(3.0, a.y)
            assertNotNull(r)
            assertEquals(4.0, r.x)
            assertEquals(3.0, r.y)
        }
    }

    @Test fun mod_equals_reduces_in_place() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(17.0, 10.0)
            val r = a.operator_mod_eq(5)
            assertEquals(2.0, a.x) // 17 % 5
            assertEquals(0.0, a.y) // 10 % 5
            assertNotNull(r)
            assertEquals(2.0, r.x)
            assertEquals(0.0, r.y)
        }
    }

    // The three compound-assign ops bind to DISTINCT wrapper functions (no
    // collision/truncation): each mutates its receiver independently.
    @Test fun compound_assigns_are_distinct() = memScoped {
        with(Vec2) {
            val a = Vec2__double_double(12.0, 9.0)
            val b = Vec2__double_double(2.0, 3.0)
            a.operator_minus_eq(b) // (10, 6)
            a.operator_div_eq(2.0) //  (5, 3)
            a.operator_mod_eq(4)   //  (1, 3)
            assertEquals(1.0, a.x)
            assertEquals(3.0, a.y)
        }
    }
}
