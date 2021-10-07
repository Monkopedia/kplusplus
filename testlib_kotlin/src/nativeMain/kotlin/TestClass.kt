/*
 * Copyright 2021 Jason Monk
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
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
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
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import krapper.testlibwrapper.internal.TestLib_OtherClass_append_text
import krapper.testlibwrapper.internal.TestLib_OtherClass_get_private_string
import krapper.testlibwrapper.internal.TestLib_OtherClass_set_private_string
import krapper.testlibwrapper.internal.TestLib_TestClass_b_get
import krapper.testlibwrapper.internal.TestLib_TestClass_b_set
import krapper.testlibwrapper.internal.TestLib_TestClass_c_get
import krapper.testlibwrapper.internal.TestLib_TestClass_c_set
import krapper.testlibwrapper.internal.TestLib_TestClass_d_get
import krapper.testlibwrapper.internal.TestLib_TestClass_d_set
import krapper.testlibwrapper.internal.TestLib_TestClass_f_get
import krapper.testlibwrapper.internal.TestLib_TestClass_f_set
import krapper.testlibwrapper.internal.TestLib_TestClass_i_get
import krapper.testlibwrapper.internal.TestLib_TestClass_i_set
import krapper.testlibwrapper.internal.TestLib_TestClass_l_get
import krapper.testlibwrapper.internal.TestLib_TestClass_l_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ld_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ld_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ll_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ll_set
import krapper.testlibwrapper.internal.TestLib_TestClass_long_pointer
import krapper.testlibwrapper.internal.TestLib_TestClass_new
import krapper.testlibwrapper.internal.TestLib_TestClass_op_and
import krapper.testlibwrapper.internal.TestLib_TestClass_op_binary_and
import krapper.testlibwrapper.internal.TestLib_TestClass_op_binary_or
import krapper.testlibwrapper.internal.TestLib_TestClass_op_decrement
import krapper.testlibwrapper.internal.TestLib_TestClass_op_divide
import krapper.testlibwrapper.internal.TestLib_TestClass_op_eq
import krapper.testlibwrapper.internal.TestLib_TestClass_op_gt
import krapper.testlibwrapper.internal.TestLib_TestClass_op_gteq
import krapper.testlibwrapper.internal.TestLib_TestClass_op_increment
import krapper.testlibwrapper.internal.TestLib_TestClass_op_ind
import krapper.testlibwrapper.internal.TestLib_TestClass_op_inv
import krapper.testlibwrapper.internal.TestLib_TestClass_op_lt
import krapper.testlibwrapper.internal.TestLib_TestClass_op_lteq
import krapper.testlibwrapper.internal.TestLib_TestClass_op_minus
import krapper.testlibwrapper.internal.TestLib_TestClass_op_mod
import krapper.testlibwrapper.internal.TestLib_TestClass_op_neq
import krapper.testlibwrapper.internal.TestLib_TestClass_op_or
import krapper.testlibwrapper.internal.TestLib_TestClass_op_plus
import krapper.testlibwrapper.internal.TestLib_TestClass_op_post_decrement
import krapper.testlibwrapper.internal.TestLib_TestClass_op_post_increment
import krapper.testlibwrapper.internal.TestLib_TestClass_op_shl
import krapper.testlibwrapper.internal.TestLib_TestClass_op_shr
import krapper.testlibwrapper.internal.TestLib_TestClass_op_times
import krapper.testlibwrapper.internal.TestLib_TestClass_op_unary_minus
import krapper.testlibwrapper.internal.TestLib_TestClass_op_unary_plus
import krapper.testlibwrapper.internal.TestLib_TestClass_op_xor
import krapper.testlibwrapper.internal.TestLib_TestClass_output
import krapper.testlibwrapper.internal.TestLib_TestClass_pb_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pb_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pc_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pc_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pd_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pd_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pf_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pf_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pi_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pi_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pl_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pl_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pll_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pll_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ps_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ps_set
import krapper.testlibwrapper.internal.TestLib_TestClass_puc_get
import krapper.testlibwrapper.internal.TestLib_TestClass_puc_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pui_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pui_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pul_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pul_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pull_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pull_set
import krapper.testlibwrapper.internal.TestLib_TestClass_pus_get
import krapper.testlibwrapper.internal.TestLib_TestClass_pus_set
import krapper.testlibwrapper.internal.TestLib_TestClass_s_get
import krapper.testlibwrapper.internal.TestLib_TestClass_s_set
import krapper.testlibwrapper.internal.TestLib_TestClass_set_pointers
import krapper.testlibwrapper.internal.TestLib_TestClass_set_private_from
import krapper.testlibwrapper.internal.TestLib_TestClass_set_private_string
import krapper.testlibwrapper.internal.TestLib_TestClass_set_some
import krapper.testlibwrapper.internal.TestLib_TestClass_str_get
import krapper.testlibwrapper.internal.TestLib_TestClass_str_set
import krapper.testlibwrapper.internal.TestLib_TestClass_sum
import krapper.testlibwrapper.internal.TestLib_TestClass_uc_get
import krapper.testlibwrapper.internal.TestLib_TestClass_uc_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ui_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ui_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ul_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ul_set
import krapper.testlibwrapper.internal.TestLib_TestClass_ull_get
import krapper.testlibwrapper.internal.TestLib_TestClass_ull_set
import krapper.testlibwrapper.internal.TestLib_TestClass_us_get
import krapper.testlibwrapper.internal.TestLib_TestClass_us_set
import krapper.testlibwrapper.internal._TestLib_TestClass_new
import krapper.testlibwrapper.internal.__TestLib_TestClass_new
import krapper.testlibwrapper.internal.___TestLib_TestClass_new
import krapper.testlibwrapper.internal._std_vector_std_string_new
import krapper.testlibwrapper.internal.std_vector_std_string_at
import krapper.testlibwrapper.internal.std_vector_std_string_new
import platform.linux.free
import platform.posix.size_t

value class Vector_String private constructor(val source: Pair<COpaquePointer, MemScope>) {
    val ptr: COpaquePointer
        inline get() {
            return source.first
        }
    val memScope: MemScope
        inline get() {
            return source.second
        }

    inline fun at(pos: size_t): String? {
        val str = std_vector_std_string_at(ptr, pos)
        val ret = str?.toKString()
        free(str)
        return ret
    }

    companion object {

        fun MemScope.Vector_String(other: Vector_String): Vector_String {
            val obj = _std_vector_std_string_new(other.ptr)
            defer {
                free(obj)
            }
            return Vector_String((obj ?: error("Creation failed")) to this)
        }

        fun MemScope.Vector_String(): Vector_String {
            val obj = std_vector_std_string_new()
            defer {
                free(obj)
            }
            return Vector_String((obj ?: error("Creation failed")) to this)
        }
    }
}

value class OtherClass private constructor(val source: Pair<COpaquePointer, MemScope>) {
    val ptr: COpaquePointer
        inline get() {
            return source.first
        }
    val memScope: MemScope
        inline get() {
            return source.second
        }

    inline fun getPrivateString(): String? {
        val result = TestLib_OtherClass_get_private_string(ptr)
        val str = result?.toKString()
        free(result)
        return str
    }

    inline fun setPrivateString(str: String?) {
        TestLib_OtherClass_set_private_string(ptr, str)
    }

    inline fun appendText(text: StdVectorStdString) {
        TestLib_OtherClass_append_text(ptr, text.ptr)
    }

    companion object {
        fun MemScope.OtherClass(): OtherClass {
            val obj = TestLib_OtherClass_new()
            defer {
                free(obj)
            }
            return OtherClass((obj ?: error("Creation failed")) to this)
        }
    }
}

value class TestClass private constructor(val source: Pair<COpaquePointer, MemScope>) {
    val ptr: COpaquePointer
        inline get() {
            return source.first
        }
    val memScope: MemScope
        inline get() {
            return source.second
        }

    var str: String?
        inline get() {
            return TestLib_TestClass_str_get(ptr)?.toKString()
        }
        inline set(value) {
            TestLib_TestClass_str_set(ptr, value?.cstr)
        }

    var b: Boolean
        inline get() {
            return TestLib_TestClass_b_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_b_set(ptr, value)
        }

    var c: Byte
        inline get() {
            return TestLib_TestClass_c_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_c_set(ptr, value)
        }

    var uc: UByte
        inline get() {
            return TestLib_TestClass_uc_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_uc_set(ptr, value)
        }

    var s: Short
        inline get() {
            return TestLib_TestClass_s_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_s_set(ptr, value)
        }

    var us: UShort
        inline get() {
            return TestLib_TestClass_us_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_us_set(ptr, value)
        }

    var i: Int
        inline get() {
            return TestLib_TestClass_i_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_i_set(ptr, value)
        }

    var ui: UInt
        inline get() {
            return TestLib_TestClass_ui_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ui_set(ptr, value)
        }

    var l: Long
        inline get() {
            return TestLib_TestClass_l_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_l_set(ptr, value)
        }

    var ul: ULong
        inline get() {
            return TestLib_TestClass_ul_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ul_set(ptr, value)
        }

    var ll: Long
        inline get() {
            return TestLib_TestClass_ll_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ll_set(ptr, value)
        }

    var ull: ULong
        inline get() {
            return TestLib_TestClass_ull_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ull_set(ptr, value)
        }

    var f: Float
        inline get() {
            return TestLib_TestClass_f_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_f_set(ptr, value)
        }

    var d: Double
        inline get() {
            return TestLib_TestClass_d_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_d_set(ptr, value)
        }

    var ld: Double
        inline get() {
            return TestLib_TestClass_ld_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ld_set(ptr, value)
        }
    var pb: CPointer<BooleanVar>?
        inline get() {
            return TestLib_TestClass_pb_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pb_set(ptr, value)
        }

    var pc: CPointer<ByteVar>?
        inline get() {
            return TestLib_TestClass_pc_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pc_set(ptr, value)
        }

    var puc: CPointer<UByteVar>?
        inline get() {
            return TestLib_TestClass_puc_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_puc_set(ptr, value)
        }

    var ps: CPointer<ShortVar>?
        inline get() {
            return TestLib_TestClass_ps_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_ps_set(ptr, value)
        }

    var pus: CPointer<UShortVar>?
        inline get() {
            return TestLib_TestClass_pus_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pus_set(ptr, value)
        }

    var pi: CPointer<IntVar>?
        inline get() {
            return TestLib_TestClass_pi_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pi_set(ptr, value)
        }

    var pui: CPointer<UIntVar>?
        inline get() {
            return TestLib_TestClass_pui_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pui_set(ptr, value)
        }

    var pl: CPointer<LongVar>?
        inline get() {
            return TestLib_TestClass_pl_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pl_set(ptr, value)
        }

    var pul: CPointer<ULongVar>?
        inline get() {
            return TestLib_TestClass_pul_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pul_set(ptr, value)
        }

    var pll: CPointer<LongVar>?
        inline get() {
            return TestLib_TestClass_pll_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pll_set(ptr, value)
        }

    var pull: CPointer<ULongVar>?
        inline get() {
            return TestLib_TestClass_pull_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pull_set(ptr, value)
        }

    var pf: CPointer<FloatVar>?
        inline get() {
            return TestLib_TestClass_pf_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pf_set(ptr, value)
        }

    var pd: CPointer<DoubleVar>?
        inline get() {
            return TestLib_TestClass_pd_get(ptr)
        }
        inline set(value) {
            TestLib_TestClass_pd_set(ptr, value)
        }

    inline fun output() {
        TestLib_TestClass_output(ptr)
    }

    inline fun sum(): Long {
        return TestLib_TestClass_sum(ptr)
    }

    inline fun longPointer(): CPointer<LongVar>? {
        return TestLib_TestClass_long_pointer(ptr)
    }

    inline fun setSome(a: Int, b: Long, c: Long) {
        TestLib_TestClass_set_some(ptr, a, b, c)
    }

    inline fun setPointers(a: CValuesRef<IntVar>, b: CValuesRef<LongVar>, c: CValuesRef<LongVar>) {
        TestLib_TestClass_set_pointers(ptr, a, b, c)
    }

    inline fun setPrivateString(value: String) {
        TestLib_TestClass_set_private_string(ptr, value)
    }

    inline fun setPrivateFrom(other: OtherClass) {
        TestLib_TestClass_set_private_from(ptr, other.ptr)
    }

    inline operator fun unaryPlus(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_unary_plus(ptr, retValue.ptr)
        return retValue
    }

    inline operator fun unaryMinus(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_unary_minus(ptr, retValue.ptr)
        return retValue
    }

    inline operator fun plus(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_plus(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun minus(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_minus(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun times(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_times(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun div(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_divide(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun rem(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_mod(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun inc(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_increment(ptr, retValue.ptr)
        return retValue
    }

    inline operator fun dec(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_decrement(ptr, retValue.ptr)
        return retValue
    }

    inline fun postIncrement(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_post_increment(ptr, 0, retValue.ptr)
        return retValue
    }

    inline fun postDecrement(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_post_decrement(ptr, 0, retValue.ptr)
        return retValue
    }

    inline fun eq(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_eq(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun notEq(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_neq(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun lt(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_lt(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun lteq(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_lteq(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun gt(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_gt(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun gteq(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_gteq(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun not(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_gteq(ptr, retValue.ptr)
        return retValue
    }

    inline infix fun binAnd(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_binary_and(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline infix fun binOr(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_binary_or(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline fun inv(): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_inv(ptr, retValue.ptr)
        return retValue
    }

    inline infix fun and(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_and(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline infix fun or(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_or(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline infix fun xor(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_xor(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline infix fun shr(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_shr(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline infix fun shl(other: TestClass): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_shl(ptr, other.ptr, retValue.ptr)
        return retValue
    }

    inline operator fun get(str: String): TestClass {
        val retValue = memScope.TestClass()
        TestLib_TestClass_op_ind(ptr, str, retValue.ptr)
        return retValue
    }

    companion object {
        fun MemScope.TestClass(): TestClass {
            val obj = TestLib_TestClass_new()
            defer {
                free(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }

        fun TestClass(other: TestClass): TestClass = with(other.memScope) {
            val obj = _TestLib_TestClass_new(other.ptr)
            defer {
                free(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }

        fun MemScope.TestClass(a: Int): TestClass {
            val obj = __TestLib_TestClass_new(a)
            defer {
                free(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }

        fun MemScope.TestClass(a: Int, b: Double): TestClass {
            val obj = ___TestLib_TestClass_new(a, b)
            defer {
                free(obj)
            }
            return TestClass((obj ?: error("Creation failed")) to this)
        }
    }
}
