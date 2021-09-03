import kotlinx.cinterop.DeferScope
import testlib.testlib_complex_destroy
import testlib.testlib_complex_input
import testlib.testlib_complex_minus
import testlib.testlib_complex_new
import testlib.testlib_complex_output

value class Complex private constructor(val source: Pair<testlib.Complex, DeferScope>) {
    val ptr: testlib.Complex
        inline get() = source.first

    inline fun input() {
        testlib_complex_input(ptr)
    }

    inline fun output() {
        testlib_complex_output(ptr)
    }

    inline operator fun minus(other: Complex): Complex {
        val ret = source.second.Complex()
        testlib_complex_minus(ptr, other.ptr, ret.ptr)
        return ret
    }

    companion object {
        fun DeferScope.Complex(): Complex {
            val obj = testlib_complex_new()
            defer {
                testlib_complex_destroy(obj)
            }
            return Complex((obj ?: error("Creation failed")) to this)
        }
    }
}