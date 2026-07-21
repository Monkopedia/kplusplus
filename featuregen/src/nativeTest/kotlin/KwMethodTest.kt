import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import root.KeywordMethods

// Ticket #7 fix #2: a C++ method whose name IS a Kotlin hard keyword (`in`/`is`) must be
// back-tick-escaped in the generated binding — otherwise the Kotlin emits `fun in(...)` and
// won't parse (the whole featuregen build fails to compile without the fix).
class KwMethodTest {
    @Test fun keyword_named_methods_bind_and_call() = memScoped {
        val k = with(KeywordMethods) { KeywordMethods() }
        k.value = 5
        assertEquals(5, k.`in`())
        assertEquals(true, k.`is`())
    }
}
