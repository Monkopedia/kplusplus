/*
 * Copyright 2022 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.utf8
import platform.posix.uint16_t
import testlib.TestLib_TestClass
import testlib.TestLib_TestClass_array_get
import testlib.TestLib_TestClass_array_set
import testlib.TestLib_TestClass_b_get
import testlib.TestLib_TestClass_b_set
import testlib.TestLib_TestClass_c_get
import testlib.TestLib_TestClass_c_set
import testlib.TestLib_TestClass_d_get
import testlib.TestLib_TestClass_d_set
import testlib.TestLib_TestClass_dispose
import testlib.TestLib_TestClass_f_get
import testlib.TestLib_TestClass_f_set
import testlib.TestLib_TestClass_i_get
import testlib.TestLib_TestClass_i_set
import testlib.TestLib_TestClass_l_get
import testlib.TestLib_TestClass_l_set
import testlib.TestLib_TestClass_ld_get
import testlib.TestLib_TestClass_ld_set
import testlib.TestLib_TestClass_ll_get
import testlib.TestLib_TestClass_ll_set
import testlib.TestLib_TestClass_longPointer
import testlib.TestLib_TestClass_new
import testlib.TestLib_TestClass_op_and
import testlib.TestLib_TestClass_op_band
import testlib.TestLib_TestClass_op_bnot
import testlib.TestLib_TestClass_op_bor
import testlib.TestLib_TestClass_op_divide
import testlib.TestLib_TestClass_op_eq_cmp
import testlib.TestLib_TestClass_op_gt
import testlib.TestLib_TestClass_op_gteq
import testlib.TestLib_TestClass_op_ind
import testlib.TestLib_TestClass_op_lt
import testlib.TestLib_TestClass_op_lteq
import testlib.TestLib_TestClass_op_minus
import testlib.TestLib_TestClass_op_minus_unary
import testlib.TestLib_TestClass_op_modulo
import testlib.TestLib_TestClass_op_neq
import testlib.TestLib_TestClass_op_not
import testlib.TestLib_TestClass_op_or
import testlib.TestLib_TestClass_op_plus
import testlib.TestLib_TestClass_op_plus_unary
import testlib.TestLib_TestClass_op_post_dec
import testlib.TestLib_TestClass_op_post_inc
import testlib.TestLib_TestClass_op_pre_dec
import testlib.TestLib_TestClass_op_pre_inc
import testlib.TestLib_TestClass_op_shl
import testlib.TestLib_TestClass_op_shr
import testlib.TestLib_TestClass_op_times
import testlib.TestLib_TestClass_op_xor
import testlib.TestLib_TestClass_output
import testlib.TestLib_TestClass_pb_get
import testlib.TestLib_TestClass_pb_set
import testlib.TestLib_TestClass_pc_get
import testlib.TestLib_TestClass_pc_set
import testlib.TestLib_TestClass_pd_get
import testlib.TestLib_TestClass_pd_set
import testlib.TestLib_TestClass_pf_get
import testlib.TestLib_TestClass_pf_set
import testlib.TestLib_TestClass_pi_get
import testlib.TestLib_TestClass_pi_set
import testlib.TestLib_TestClass_pl_get
import testlib.TestLib_TestClass_pl_set
import testlib.TestLib_TestClass_pll_get
import testlib.TestLib_TestClass_pll_set
import testlib.TestLib_TestClass_ps_get
import testlib.TestLib_TestClass_ps_set
import testlib.TestLib_TestClass_puc_get
import testlib.TestLib_TestClass_puc_set
import testlib.TestLib_TestClass_pui_get
import testlib.TestLib_TestClass_pui_set
import testlib.TestLib_TestClass_pul_get
import testlib.TestLib_TestClass_pul_set
import testlib.TestLib_TestClass_pull_get
import testlib.TestLib_TestClass_pull_set
import testlib.TestLib_TestClass_pus_get
import testlib.TestLib_TestClass_pus_set
import testlib.TestLib_TestClass_s_get
import testlib.TestLib_TestClass_s_set
import testlib.TestLib_TestClass_setPointers
import testlib.TestLib_TestClass_setPrivateFrom
import testlib.TestLib_TestClass_setPrivateString
import testlib.TestLib_TestClass_setSome
import testlib.TestLib_TestClass_st_get
import testlib.TestLib_TestClass_st_set
import testlib.TestLib_TestClass_str_get
import testlib.TestLib_TestClass_str_set
import testlib.TestLib_TestClass_sum
import testlib.TestLib_TestClass_uc_get
import testlib.TestLib_TestClass_uc_set
import testlib.TestLib_TestClass_ui_get
import testlib.TestLib_TestClass_ui_set
import testlib.TestLib_TestClass_uit_get
import testlib.TestLib_TestClass_uit_set
import testlib.TestLib_TestClass_ul_get
import testlib.TestLib_TestClass_ul_set
import testlib.TestLib_TestClass_ull_get
import testlib.TestLib_TestClass_ull_set
import testlib.TestLib_TestClass_us_get
import testlib.TestLib_TestClass_us_set
import testlib._TestLib_TestClass_new
import testlib.__TestLib_TestClass_new
import testlib.___TestLib_TestClass_new

value class TestClass private constructor(val source: Pair<TestLib_TestClass, MemScope>) {
    val ptr: testlib.TestLib_TestClass
        inline get() = source.first

    var b: Boolean
        get() = TestLib_TestClass_b_get(ptr)
        set(value) {
            TestLib_TestClass_b_set(ptr, value)
        }

    var st: ULong
        get() = TestLib_TestClass_st_get(ptr)
        set(value) {
            TestLib_TestClass_st_set(ptr, value)
        }
    var uit: uint16_t
        get() = TestLib_TestClass_uit_get(ptr)
        set(value) {
            TestLib_TestClass_uit_set(ptr, value)
        }

    var array: IntArray
        get() {
            return memScoped {
                val array = allocArray<IntVar>(5)
                TestLib_TestClass_array_get(ptr, array)
                return IntArray(5) { array[it] }
            }
        }
        set(value) {
            memScoped {
                require(value.size == 5) {
                    "Wrong size for array"
                }
                val array = allocArray<IntVar>(5)
                value.forEachIndexed { index, i ->
                    array[index] = i
                }
                TestLib_TestClass_array_set(ptr, array)
            }
        }
    var str: String?
        get() = TestLib_TestClass_str_get(ptr)?.toKString()
        set(value) {
            memScoped {
                TestLib_TestClass_str_set(ptr, value?.utf8?.placeTo(source.second))
            }
        }

    var c: Byte
        get() = TestLib_TestClass_c_get(ptr)
        set(value) {
            TestLib_TestClass_c_set(ptr, value)
        }
    var uc: UByte
        get() = TestLib_TestClass_uc_get(ptr)
        set(value) {
            TestLib_TestClass_uc_set(ptr, value)
        }
    var s: Short
        get() = TestLib_TestClass_s_get(ptr)
        set(value) {
            TestLib_TestClass_s_set(ptr, value)
        }
    var us: uint16_t
        get() = TestLib_TestClass_us_get(ptr)
        set(value) {
            TestLib_TestClass_us_set(ptr, value)
        }
    var i: Int
        get() = TestLib_TestClass_i_get(ptr)
        set(value) {
            TestLib_TestClass_i_set(ptr, value)
        }
    var ui: UInt
        get() = TestLib_TestClass_ui_get(ptr)
        set(value) {
            TestLib_TestClass_ui_set(ptr, value)
        }
    var l: Long
        get() = TestLib_TestClass_l_get(ptr)
        set(value) {
            TestLib_TestClass_l_set(ptr, value)
        }
    var ul: ULong
        get() = TestLib_TestClass_ul_get(ptr)
        set(value) {
            TestLib_TestClass_ul_set(ptr, value)
        }
    var ll: Long
        get() = TestLib_TestClass_ll_get(ptr)
        set(value) {
            TestLib_TestClass_ll_set(ptr, value)
        }
    var ull: ULong
        get() = TestLib_TestClass_ull_get(ptr)
        set(value) {
            TestLib_TestClass_ull_set(ptr, value)
        }
    var f: Float
        get() = TestLib_TestClass_f_get(ptr)
        set(value) {
            TestLib_TestClass_f_set(ptr, value)
        }
    var d: Double
        get() = TestLib_TestClass_d_get(ptr)
        set(value) {
            TestLib_TestClass_d_set(ptr, value)
        }
    var ld: Double
        get() = TestLib_TestClass_ld_get(ptr)
        set(value) {
            TestLib_TestClass_ld_set(ptr, value)
        }

    var pb: CPointer<BooleanVar>?
        get() = TestLib_TestClass_pb_get(ptr)
        set(value) {
            TestLib_TestClass_pb_set(ptr, value)
        }
    var pc: CPointer<ByteVar>?
        get() = TestLib_TestClass_pc_get(ptr)
        set(value) {
            TestLib_TestClass_pc_set(ptr, value)
        }
    var puc: CPointer<UByteVar>?
        get() = TestLib_TestClass_puc_get(ptr)
        set(value) {
            TestLib_TestClass_puc_set(ptr, value)
        }
    var ps: CPointer<ShortVar>?
        get() = TestLib_TestClass_ps_get(ptr)
        set(value) {
            TestLib_TestClass_ps_set(ptr, value)
        }
    var pus: CPointer<UShortVar>?
        get() = TestLib_TestClass_pus_get(ptr)
        set(value) {
            TestLib_TestClass_pus_set(ptr, value)
        }
    var pi: CPointer<IntVar>?
        get() = TestLib_TestClass_pi_get(ptr)
        set(value) {
            TestLib_TestClass_pi_set(ptr, value)
        }
    var pui: CPointer<UIntVar>?
        get() = TestLib_TestClass_pui_get(ptr)
        set(value) {
            TestLib_TestClass_pui_set(ptr, value)
        }
    var pl: CPointer<LongVar>?
        get() = TestLib_TestClass_pl_get(ptr)
        set(value) {
            TestLib_TestClass_pl_set(ptr, value)
        }
    var pul: CPointer<ULongVar>?
        get() = TestLib_TestClass_pul_get(ptr)
        set(value) {
            TestLib_TestClass_pul_set(ptr, value)
        }
    var pll: CPointer<LongVar>?
        get() = TestLib_TestClass_pll_get(ptr)
        set(value) {
            TestLib_TestClass_pll_set(ptr, value)
        }
    var pull: CPointer<ULongVar>?
        get() = TestLib_TestClass_pull_get(ptr)
        set(value) {
            TestLib_TestClass_pull_set(ptr, value)
        }
    var pf: CPointer<FloatVar>?
        get() = TestLib_TestClass_pf_get(ptr)
        set(value) {
            TestLib_TestClass_pf_set(ptr, value)
        }
    var pd: CPointer<DoubleVar>?
        get() = TestLib_TestClass_pd_get(ptr)
        set(value) {
            TestLib_TestClass_pd_set(ptr, value)
        }

    inline fun sum(): Long {
        return TestLib_TestClass_sum(ptr)
    }
    inline fun longPointer(): CPointer<LongVar>? {
        return TestLib_TestClass_longPointer(ptr)
    }
    inline fun setSome(a: Int, b: Long, c: Long) {
        TestLib_TestClass_setSome(ptr, a, b, c)
    }
    inline fun setPointers(a: CPointer<IntVar>, b: CPointer<LongVar>, c: CPointer<LongVar>) {
        TestLib_TestClass_setPointers(ptr, a, b, c)
    }
    inline fun setPrivateString(str: String) {
        TestLib_TestClass_setPrivateString(ptr, str)
    }

    // Operator overloading
    operator fun minus(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_minus(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun unaryMinus(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_minus_unary(ptr, ret.ptr)
        return ret
    }
    inline operator fun plus(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_plus(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun unaryPlus(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_plus_unary(ptr, ret.ptr)
        return ret
    }
    inline operator fun times(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_times(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun div(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_divide(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun rem(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_modulo(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun inc(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_pre_inc(ptr, ret.ptr)
        return ret
    }
    inline fun postInc(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_post_inc(ptr, 0, ret.ptr)
        return ret
    }
    inline operator fun dec(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_pre_dec(ptr, ret.ptr)
        return ret
    }
    inline fun postDec(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_post_dec(ptr, 0, ret.ptr)
        return ret
    }
    inline infix fun eq(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_eq_cmp(ptr, ret.ptr, ret.ptr)
        return ret
    }
    inline infix fun neq(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_neq(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun lt(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_lt(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun gt(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_gt(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun lteq(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_lteq(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun gteq(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_gteq(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun not(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_bnot(ptr, ret.ptr)
        return ret
    }
    inline infix fun band(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_band(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun bor(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_bor(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline fun inv(): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_not(ptr, ret.ptr)
        return ret
    }
    inline infix fun and(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_and(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun or(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_or(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun xor(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_xor(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun shl(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_shl(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline infix fun shr(other: TestClass): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_shr(ptr, other.ptr, ret.ptr)
        return ret
    }
    inline operator fun get(str: String): TestClass {
        val ret = source.second.TestClass()
        TestLib_TestClass_op_ind(ptr, str, ret.ptr)
        return ret
    }

    inline fun output() {
        TestLib_TestClass_output(ptr)
    }

    inline fun setPrivateFrom(other: OtherClass) {
        TestLib_TestClass_setPrivateFrom(source.first, other.source.first)
    }

    companion object {
        fun MemScope.TestClass(): TestClass {
            val obj = TestLib_TestClass_new()
            defer {
                TestLib_TestClass_dispose(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }
        fun MemScope.TestClass(other: TestClass): TestClass {
            val obj = _TestLib_TestClass_new(other.ptr)
            defer {
                TestLib_TestClass_dispose(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }
        fun MemScope.TestClass(a: Int): TestClass {
            val obj = __TestLib_TestClass_new(a)
            defer {
                TestLib_TestClass_dispose(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }
        fun MemScope.TestClass(a: Int, b: Double): TestClass {
            val obj = ___TestLib_TestClass_new(a, b)
            defer {
                TestLib_TestClass_dispose(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }
    }
}
