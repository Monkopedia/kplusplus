import kotlinx.cinterop.MemScope
import platform.linux.free
import testlib.TestLib_MyPair__OtherClass__P
import testlib.TestLib_MyPair__OtherClass__P_a_get
import testlib.TestLib_MyPair__OtherClass__P_a_set
import testlib.TestLib_MyPair__OtherClass__P_b_get
import testlib.TestLib_MyPair__OtherClass__P_b_set
import testlib.TestLib_MyPair__OtherClass__P_get_max
import testlib.TestLib_MyPair__OtherClass__P_new

value class MyPair_OtherClass_P constructor(
    val source: Pair<TestLib_MyPair__OtherClass__P, MemScope>
) {
    val ptr: TestLib_MyPair__OtherClass__P
        inline get() = source.first
    val memScope: MemScope
        inline get() = source.second

    var a: OtherClass?
        inline get() {
            val ptr = TestLib_MyPair__OtherClass__P_a_get(ptr) ?: return null
            return OtherClass(ptr to memScope)
        }
        inline set(value) {
            TestLib_MyPair__OtherClass__P_a_set(ptr, value?.ptr)
        }

    var b: OtherClass?
        inline get() {
            val ptr = TestLib_MyPair__OtherClass__P_b_get(ptr) ?: return null
            return OtherClass(ptr to memScope)
        }
        inline set(value) {
            TestLib_MyPair__OtherClass__P_b_set(ptr, value?.ptr)
        }

    inline fun getMax(): OtherClass? {
        val ptr = TestLib_MyPair__OtherClass__P_get_max(ptr) ?: return null
        return OtherClass(ptr to memScope)
    }

    companion object {
        inline fun MemScope.MyPair_OtherClass_P(
            first: OtherClass,
            second: OtherClass
        ): MyPair_OtherClass_P {
            val obj = TestLib_MyPair__OtherClass__P_new(first.ptr, second.ptr)
                ?: error("Failed to create object")
            defer {
                free(obj)
            }
            return MyPair_OtherClass_P(obj to this)
        }
    }
}
