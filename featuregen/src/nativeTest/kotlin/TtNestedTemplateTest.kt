import kotlinx.cinterop.memScoped
import outer2.Inner2
import root.Outer2
import kotlin.test.Test
import kotlin.test.assertEquals

// T-typename-b: a class nested inside a class TEMPLATE (`Wrapper<T>::Inner`) has a
// qualified name that can't be spelled without the enclosing template's argument, so
// krapper used to flatten it to a bare unnameable `Inner` and emit sizeof/reinterpret_cast/
// accessors for a type the compiler can't find. Skip-not-crash: such a class is DROPPED.
//
// Negative coverage is structural: there is intentionally NO `import ...Inner` for the
// in-template nested struct (no wrapper is generated for it), and the sync COMPILING proves
// no bogus flattened `Inner` binding was emitted. The positive control is a nested class
// inside a NON-template struct (`Outer2::Inner2`), which IS nameable and must still bind.
class TtNestedTemplateTest {
    @Test fun nested_in_nontemplate_class_still_binds() = memScoped {
        val ms = this
        val o = with(Outer2) { ms.Outer2() }
        assertEquals(11, o.who2)
    }

    // Outer2::Inner2 binds in its own `outer2` package (same nested-class -> package rule
    // as Outer::Inner). Constructing it proves the non-template nested class is unaffected
    // by the in-template drop.
    @Test fun nontemplate_nested_inner_binds() = memScoped {
        val ms = this
        val inner = with(Inner2) { ms.Inner2__int(5) }
        assertEquals(5, inner.y)
        assertEquals(5, inner.getY())
    }
}
