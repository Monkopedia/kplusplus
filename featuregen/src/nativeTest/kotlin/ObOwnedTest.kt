import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.cinterop.memScoped
import root.Tracked

// OB-return-ptr-owned / IH-destruction: caller-controlled destruction. A `T*`
// return is a NON-owning wrapper; the wrapper exposes two caller-choice methods
// on any class that has a destructor:
//   * dispose() — run ~Tracked() now (the same Tracked_dispose C wrapper the
//                 construction factory's `defer` calls).
//   * owned()   — register memScope.defer { ~Tracked() } and return `this`, so
//                 the object is destroyed at scope end.
// `Tracked`'s ~Tracked() bumps a static counter (destroyedCount()), so each test
// can PROVE the destructor actually ran (or, for the borrowed case, did NOT).
class ObOwnedTest {

    // Immediate dispose(): the destructor runs right away (counter +1).
    @Test fun dispose_runs_destructor_immediately() = memScoped {
        with(Tracked) {
            resetDestroyed()
            val t = makeTracked(7)
            assertNotNull(t)
            assertEquals(7, t.getId())
            assertEquals(0, destroyedCount(), "destructor must not have run yet")
            t.dispose()
            assertEquals(1, destroyedCount(), "dispose() must run ~Tracked() now")
        }
    }

    // owned(): the destructor is deferred to scope end. Inside the inner scope the
    // counter is unchanged; after the scope closes it has incremented.
    @Test fun owned_defers_destructor_to_scope_end() = memScoped {
        with(Tracked) {
            resetDestroyed()
            val before = destroyedCount()
            memScoped {
                val t = makeTracked(9)
                assertNotNull(t)
                assertEquals(t, t.owned())
                assertEquals(9, t.getId())
                // Still alive inside the inner scope — defer hasn't fired.
                assertEquals(before, destroyedCount(), "owned() must defer, not dispose now")
            }
            // Inner scope closed -> the deferred ~Tracked() ran exactly once.
            assertEquals(before + 1, destroyedCount(), "owned() must dispose at scope end")
        }
    }

    // owned() returns `this` (chainable): the wrapper is usable after the call.
    @Test fun owned_returns_this_for_chaining() = memScoped {
        with(Tracked) {
            resetDestroyed()
            val t = makeTracked(5)
            assertNotNull(t)
            assertEquals(t, t.owned(), "owned() returns the same wrapper")
            assertEquals(5, t.getId())
        }
    }

    // Default is still NON-owning: a borrowed wrapper that is neither owned() nor
    // dispose()d is NOT destroyed within the scope (no auto-dispose on a raw return).
    @Test fun borrowed_is_not_disposed_in_scope() = memScoped {
        with(Tracked) {
            resetDestroyed()
            memScoped {
                val t = makeTracked(3)
                assertNotNull(t)
                assertEquals(3, t.getId())
            }
            // No owned()/dispose() was called -> nothing freed it (it leaks, by design:
            // the caller owns a raw `new`ed pointer and chose to do nothing).
            assertEquals(0, destroyedCount(), "borrowed default must not auto-dispose")
        }
    }
}
