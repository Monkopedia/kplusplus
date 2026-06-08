import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import root.HandlerProbe
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// T-forcing-scope-2b: a NAMESPACE-SCOPED function-pointer typedef (`hh::handler_t`,
// `void(*)()`) used as a method return / parameter type. This is the exact shape of the
// std::set_terminate / std::set_new_handler free functions the --instantiate forcing
// pulls in from <exception>/<new>: on the UNFIXED generator the C++ wrapper named the
// typedef UNqualified (`handler_t ...(handler_t)`), which is "unknown type name
// 'handler_t'; did you mean 'hh::handler_t'?" outside the namespace. The fix qualifies
// the typedef with its namespace in the generated .cc wrapper (`hh::handler_t`), while
// the C interop header keeps the unqualified extern-"C" form. Both spellings are aliases
// for the identical `void(*)()`, so the binding compiles AND round-trips the pointer.
class TfNamespaceFnPtrTest {
    @Test fun namespace_scoped_function_pointer_typedef_round_trips() = memScoped {
        val ms = this
        with(HandlerProbe) {
            // The default handler pointer is a real, in-scope `hh::handler_t`.
            val def = ms.defaultHandler()
            assertNotNull(def)
            assertTrue(ms.isDefault(def))

            // setHandler stores it and returns the previous (initially null) handler.
            ms.setHandler(def)
            // Re-reading hands back the same pointer; isDefault confirms identity.
            val current = ms.getHandler()
            assertNotNull(current)
            assertTrue(ms.isDefault(current))
        }
    }

    @Test fun stored_handler_is_invocable_through_the_pointer() = memScoped {
        val ms = this
        with(HandlerProbe) {
            val def = ms.defaultHandler()
            assertNotNull(def)
            ms.setHandler(def)
            val got = ms.getHandler()
            assertNotNull(got)
            // The returned `hh::handler_t` is a callable `void()` C function pointer.
            got()
            assertTrue(ms.isDefault(got))
        }
    }
}
