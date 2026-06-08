import kotlinx.cinterop.memScoped
import root.CbCtx
import kotlin.test.Test
import kotlin.test.assertEquals

// CB-lambda-mode1: a C callback that carries a `void*` context slot
// (`typedef int (*IntCtxFn)(void* ctx, int n)` + a sibling `void* userData`) is
// bridged to a plain CAPTURING Kotlin lambda. The binding boxes the lambda in a
// StableRef, passes a non-capturing trampoline that recovers it from the void* ctx,
// and consumes both the function-pointer and the void* userData args — so the caller
// passes only the lambda and the remaining args. The StableRef lifetime is exactly
// this (synchronous) call.
class CbMode1Test {
    @Test fun capturing_lambda_via_void_context() = memScoped {
        val ms = this
        with(CbCtx) {
            val base = 10
            // void* userData + fn are consumed; caller passes only the lambda + x.
            assertEquals(15, ms.callWith({ n -> n + base }, 5))
        }
    }
}
