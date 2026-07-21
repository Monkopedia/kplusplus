import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.AliasFeature

// PR-ptrdiff / PR-wchar (generator-backed): a direct ptrdiff_t / wchar_t param
// surfaces as the platform.posix alias (ptrdiff_t -> Long, wchar_t -> Int on
// Linux x64) instead of a bare integer, matching cinterop. Value round-trip here;
// the alias preservation itself is asserted by the generated import in
// root_AliasFeature.kt.
class PrAliasTest {
    @Test fun ptrdiff_round_trips() = memScoped {
        with(AliasFeature) {
            assertEquals(0L, echoDiff(0L))
            assertEquals(-5L, echoDiff(-5L))
            assertEquals(123456789L, echoDiff(123456789L))
            assertEquals(Long.MIN_VALUE, echoDiff(Long.MIN_VALUE))
        }
    }

    @Test fun wchar_round_trips() = memScoped {
        with(AliasFeature) {
            assertEquals(65, echoWide(65)) // 'A'
            assertEquals(0x20AC, echoWide(0x20AC)) // BMP €
            assertEquals(0x1F600, echoWide(0x1F600)) // astral 😀 (4-byte wchar_t)
        }
    }
}
