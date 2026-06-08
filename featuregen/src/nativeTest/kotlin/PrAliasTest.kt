import kotlinx.cinterop.memScoped
import root.AliasFeature
import kotlin.test.Test
import kotlin.test.assertEquals

// PR-ptrdiff / PR-wchar (generator-backed): a direct ptrdiff_t / wchar_t param
// surfaces as the platform.posix alias (ptrdiff_t -> Long, wchar_t -> Int on
// Linux x64) instead of a bare integer, matching cinterop. Value round-trip here;
// the alias preservation itself is asserted by the generated import in
// root_AliasFeature.kt.
class PrAliasTest {
    @Test fun ptrdiff_round_trips() = memScoped {
        val ms = this
        with(AliasFeature) {
            assertEquals(0L, ms.echoDiff(0L))
            assertEquals(-5L, ms.echoDiff(-5L))
            assertEquals(123456789L, ms.echoDiff(123456789L))
            assertEquals(Long.MIN_VALUE, ms.echoDiff(Long.MIN_VALUE))
        }
    }

    @Test fun wchar_round_trips() = memScoped {
        val ms = this
        with(AliasFeature) {
            assertEquals(65, ms.echoWide(65))           // 'A'
            assertEquals(0x20AC, ms.echoWide(0x20AC))   // BMP €
            assertEquals(0x1F600, ms.echoWide(0x1F600)) // astral 😀 (4-byte wchar_t)
        }
    }
}
