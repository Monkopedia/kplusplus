import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.AccessProbe
import root.Color

// T-inaccessible (#123): a public method whose signature references a protected/private
// nested type is un-nameable from the generated free-function wrapper, so binding it
// would emit a cast to an inaccessible type (the LLVM-22 repro:
// `clang::ASTContext::getPredefinedSugarType(clang::Type::PredefinedSugarKind)`, whose
// param is the PROTECTED enum `clang::Type::PredefinedSugarKind`). Skip-not-crash: the
// generator DROPS such a method (its signature type resolves to UNRESOLVABLE in
// TypeBuilder, dropping the method through the same notifyFailed path as any other
// unbindable type), while sibling methods with accessible signatures still bind.
//
// Coverage is structural: this file COMPILING + a successful sync proves the two
// inaccessible-signature methods (`useHiddenEnum` over a protected enum, `makeHiddenTag`
// returning a private struct) were NOT emitted — before the fix the wrapper compile
// failed on the inaccessible cast. Referencing `p.useHiddenEnum(...)` / `p.makeHiddenTag()`
// here would not compile (they are absent). The asserts exercise the surviving siblings —
// a plain-int method and a method over the accessible top-level `Color` enum — to prove
// AccessProbe is still usable and that only the inaccessible-signature methods dropped.
class TiInaccessibleTypeTest {
    // The plain-int sibling always binds: proves the class itself survived.
    @Test fun plain_sibling_method_binds() = memScoped {
        val p = with(AccessProbe) { AccessProbe() }
        assertEquals(99, p.plain())
    }

    // A method over an ACCESSIBLE enum stays bound (control for the drop): the top-level
    // `Color` enum is nameable, so useColor(Color) is emitted and round-trips through C++.
    @Test fun accessible_enum_signature_method_binds() = memScoped {
        val p = with(AccessProbe) { AccessProbe() }
        assertEquals(1, p.useColor(Color.Green))
        assertEquals(2, p.useColor(Color.Blue))
    }
}
