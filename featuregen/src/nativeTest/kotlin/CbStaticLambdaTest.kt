import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import root.CbProbe

// CB-lambda-mode2: a context-free C callback param (`typedef int (*IntTransform)(int)`)
// surfaces as the raw cinterop `CPointer<CFunction<(Int) -> Int>>?`, so a caller passes
// `staticCFunction { … }` directly. The Kotlin/Native compiler rejects a *capturing*
// lambda here at compile time (the type enforces the limitation honestly) — exactly the
// Mode-2 contract. This already works from the stage-1 function-pointer resolution; no
// trampoline involved. (Mode 1 — a plain capturing Kotlin lambda via a void* context
// channel — is the separate, not-yet-built feature.)
class CbStaticLambdaTest {
    @Test fun static_function_passes_as_mode2_callback() = memScoped {
        with(CbProbe) {
            val inc = staticCFunction { n: Int -> n + 1 }
            assertEquals(8, applyTransform(7, inc))
        }
    }
}
