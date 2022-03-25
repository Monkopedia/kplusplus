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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.resolved_model.ResolvedClass
import com.monkopedia.krapper.generator.resolved_model.ResolvedElement
import com.monkopedia.krapper.generator.resolved_model.ResolvedField
import com.monkopedia.krapper.generator.resolved_model.ResolvedMethod
import kotlin.test.Test
import kotlin.test.fail

class FullKotlinTests {

    private val STD_VECTOR_STRING_NEW = "fun MemScope.vector__String(): vector__String {\n" +
        "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
        "    val obj: COpaquePointer = (std_vector_std_string_new(memory) ?: " +
        "error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        std_vector_std_string_dispose(obj)\n" +
        "    }\n" +
        "    return vector__String((obj to this))\n" +
        "}\n\n"

    private val STD_VECTOR_STRING_DISPOSE = ""

    private val STD_VECTOR_STRING_PUSH_BACK =
        "inline fun push_back(str: String?): Unit {\n" +
            "    return std_vector_std_string_push_back(ptr, str)\n" +
            "}"

    private val TESTLIB_OTHERCLASS_NEW =
        "fun MemScope.OtherClass(): OtherClass {\n" +
            "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
            "    val obj: COpaquePointer = (TestLib_OtherClass_new(memory) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_OtherClass_dispose(obj)\n" +
            "    }\n" +
            "    return OtherClass((obj to this))\n" +
            "}"

    private val TESTLIB_OTHERCLASS_DISPOSE = ""

    private val TESTLIB_OTHERCLASS_GET_PRIVATE_STRING =
        "inline fun getPrivateString(): String? {\n" +
            "    val str: CPointer<ByteVar>? = TestLib_OtherClass_get_private_string(ptr)\n" +
            "    val ret: String? = str?.toKString()\n" +
            "    free(str)\n" +
            "    return ret\n" +
            "}"

    private val TESTLIB_OTHERCLASS_SET_PRIVATE_STRING =
        "inline fun setPrivateString(value: String?): Unit {\n" +
            "    return TestLib_OtherClass_set_private_string(ptr, value)\n" +
            "}"

    private val TESTLIB_OTHERCLASS_APPEND_TEXT =
        "inline fun appendText(text: vector__String): Unit {\n" +
            "    return TestLib_OtherClass_append_text(ptr, text.ptr)\n" +
            "}"
    private val TESTLIB_OTHERCLASS_COPIES =
        "inline fun copies(): MyPair__OtherClass? {\n" +
            "    return MyPair__OtherClass(((TestLib_OtherClass_copies(ptr) ?: return null) " +
            "to memScope))\n" +
            "}"
    private val TESTLIB_OTHERCLASS_INTS =
        "inline fun ints(): MyPair__Int? {\n" +
            "    return MyPair__Int(((TestLib_OtherClass_ints(ptr) ?: return null) " +
            "to memScope))\n" +
            "}"

    private val TESTLIB_TESTCLASS_B =
        "var b: Boolean\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_b_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_b_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_ST =
        "var st: size_t\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_st_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_st_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_UIT =
        "var uit: UShort\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_uit_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_uit_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_STR =
        "var str: String?\n" +
            "    inline get() {\n" +
            "        val _str: CPointer<ByteVar>? = TestLib_TestClass_str_get(ptr)\n" +
            "        val ret: String? = _str?.toKString()\n" +
            "        free(_str)\n" +
            "        return ret\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_str_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_C =
        "var c: Byte\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_c_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_c_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_UC =
        "var uc: UByte\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_uc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_uc_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_S =
        "var s: Short\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_s_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_s_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_US =
        "var us: UShort\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_us_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_us_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_I =
        "var i: Int\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_i_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_i_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_UI =
        "var ui: UInt\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ui_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ui_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_L =
        "var l: Long\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_l_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_l_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_UL =
        "var ul: ULong\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ul_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ul_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_LL =
        "var ll: Long\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ll_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ll_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_ULL =
        "var ull: ULong\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ull_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ull_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_F =
        "var f: Float\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_f_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_f_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_D =
        "var d: Double\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_d_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_d_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_LD =
        "var ld: Double\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ld_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ld_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PB =
        "var pb: CValuesRef<BooleanVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pb_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pb_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PC =
        "var pc: CValuesRef<ByteVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pc_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PUC =
        "var puc: CValuesRef<UByteVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_puc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_puc_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PS =
        "var ps: CValuesRef<ShortVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ps_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ps_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PUS =
        "var pus: CValuesRef<UShortVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pus_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pus_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PI =
        "var pi: CValuesRef<IntVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pi_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pi_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PUI =
        "var pui: CValuesRef<UIntVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pui_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pui_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PL =
        "var pl: CValuesRef<LongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pl_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pl_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PUL =
        "var pul: CValuesRef<ULongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pul_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pul_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PLL =
        "var pll: CValuesRef<LongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pll_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pll_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PULL =
        "var pull: CValuesRef<ULongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pull_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pull_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PF =
        "var pf: CValuesRef<FloatVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pf_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pf_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_PD =
        "var pd: CValuesRef<DoubleVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pd_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pd_set(ptr, value)\n" +
            "    }"

    private val TESTLIB_TESTCLASS_NEW = "fun MemScope.TestClass(): TestClass {\n" +
        "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
        "    val obj: COpaquePointer = (TestLib_TestClass_new(memory) ?: error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        TestLib_TestClass_dispose(obj)\n" +
        "    }\n" +
        "    return TestClass((obj to this))\n" +
        "}"

    private val TESTLIB_TESTCLASS__NEW =
        "fun MemScope.TestClass(other: TestClass): TestClass {\n" +
            "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
            "    val obj: COpaquePointer = (_TestLib_TestClass_new(memory, other.ptr) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_TestClass_dispose(obj)\n" +
            "    }\n" +
            "    return TestClass((obj to this))\n" +
            "}"

    private val TESTLIB_TESTCLASS___NEW = "fun MemScope.TestClass(a: Int): TestClass {\n" +
        "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
        "    val obj: COpaquePointer = (__TestLib_TestClass_new(memory, a) ?: " +
        "error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        TestLib_TestClass_dispose(obj)\n" +
        "    }\n" +
        "    return TestClass((obj to this))\n" +
        "}"

    private val TESTLIB_TESTCLASS____NEW =
        "fun MemScope.TestClass(a: Int, b: Double): TestClass {\n" +
            "    val memory: COpaquePointer = alloc(size, size).reinterpret()\n" +
            "    val obj: COpaquePointer = (___TestLib_TestClass_new(memory, a, b) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_TestClass_dispose(obj)\n" +
            "    }\n" +
            "    return TestClass((obj to this))\n" +
            "}"

    private val TESTLIB_TESTCLASS_DISPOSE = ""

    private val TESTLIB_TESTCLASS_SUM = "inline fun sum(): Long {\n" +
        "    return TestLib_TestClass_sum(ptr)\n" +
        "}"

    private val TESTLIB_TESTCLASS_LONG_POINTER =
        "inline fun longPointer(): CValuesRef<LongVar>? {\n" +
            "    return TestLib_TestClass_long_pointer(ptr)\n" +
            "}"

    private val TESTLIB_TESTCLASS_SET_SOME =
        "inline fun setSome(a: Int, b: Long, c: Long): Unit {\n" +
            "    return TestLib_TestClass_set_some(ptr, a, b, c)\n" +
            "}"

    private val TESTLIB_TESTCLASS_SET_POINTERS =
        "inline fun setPointers(a: CValuesRef<IntVar>?, b: CValuesRef<LongVar>?, " +
            "c: CValuesRef<LongVar>?): Unit {\n" +
            "    return TestLib_TestClass_set_pointers(ptr, a, b, c)\n" +
            "}"

    private val TESTLIB_TESTCLASS_SET_PRIVATE_STRING =
        "inline fun setPrivateString(value: String?): Unit {\n" +
            "    return TestLib_TestClass_set_private_string(ptr, value)\n" +
            "}"

    private val TESTLIB_TESTCLASS_SET_PRIVATE_FROM =
        "inline fun setPrivateFrom(value: OtherClass?): Unit {\n" +
            "    return TestLib_TestClass_set_private_from(ptr, value?.ptr)\n" +
            "}"

    private val TESTLIB_TESTCLASS_OUTPUT = "inline fun output(): Unit {\n" +
        "    return TestLib_TestClass_output(ptr)\n" +
        "}"

    private val TESTLIB_TESTCLASS_MINUS =
        "inline operator fun minus(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_minus(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_MINUS_UNARY =
        "inline operator fun unaryMinus(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_unary_minus(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_PLUS =
        "inline operator fun plus(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_plus(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_PLUS_UNARY =
        "inline operator fun unaryPlus(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_unary_plus(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_TIMES =
        "inline operator fun times(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_times(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_DIVIDE =
        "inline operator fun div(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_divide(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_MODULO =
        "inline operator fun rem(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_mod(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_PRE_INC =
        "inline operator fun inc(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_increment(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_POST_INC =
        "inline fun postIncrement(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_post_increment(ptr, 0, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_PRE_DEC =
        "inline operator fun dec(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_decrement(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_POST_DEC =
        "inline fun postDecrement(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_post_decrement(ptr, 0, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_EQ_CMP =
        "inline infix fun eq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_eq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_NEQ =
        "inline infix fun neq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_neq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_LT =
        "inline infix fun lt(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_lt(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_GT =
        "inline infix fun gt(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_gt(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_LTEQ =
        "inline infix fun lteq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_lteq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_GTEQ =
        "inline infix fun gteq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_gteq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_BNOT =
        "inline operator fun not(): TestClass {\n" +
            "    return TestClass((TestLib_TestClass_op_not(ptr) to memScope))\n" +
            "}"

    private val TESTLIB_TESTCLASS_BAND =
        "inline infix fun binAnd(c: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_binary_and(ptr, c.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_BOR =
        "inline infix fun binOr(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_binary_or(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_NOT =
        "inline fun inv(): TestClass? {\n" +
            "    return TestClass(((TestLib_TestClass_op_inv(ptr) ?: return null) to memScope))\n" +
            "}"

    private val TESTLIB_TESTCLASS_AND =
        "inline infix fun and(c: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_and(ptr, c.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_OR =
        "inline infix fun or(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_or(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_XOR =
        "inline infix fun xor(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_xor(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_SHL =
        "inline infix fun shl(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_shl(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_SHR =
        "inline infix fun shr(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_shr(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val TESTLIB_TESTCLASS_IND =
        "inline operator fun get(c2: String?): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_ind(ptr, c2, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    @Test
    fun testVector_new() = runTest(
        cls = TestData.Vector.cls,
        target = (TestData.Vector.cls.first.children[1] as WrappedMethod),
        expected = STD_VECTOR_STRING_NEW,
    )

    @Test
    fun testVector_dispose() = runTest(
        cls = TestData.Vector.cls,
        target = TestData.Vector.cls.first.children[2] as WrappedMethod,
        expected = STD_VECTOR_STRING_DISPOSE,
    )

    @Test
    fun testVector_pushBack() = runTest(
        cls = TestData.Vector.cls,
        target = TestData.Vector.cls.first.children[3] as WrappedMethod,
        expected = STD_VECTOR_STRING_PUSH_BACK,
    )

    @Test
    fun testOtherClass_new() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.constructor,
        expected = TESTLIB_OTHERCLASS_NEW,
    )

    @Test
    fun testOtherClass_dispose() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.destructor,
        expected = TESTLIB_OTHERCLASS_DISPOSE,
    )

    @Test
    fun testOtherClass_getPrivateString() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.getPrivateString,
        expected = TESTLIB_OTHERCLASS_GET_PRIVATE_STRING,
    )

    @Test
    fun testOtherClass_setPrivateString() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.setPrivateString,
        expected = TESTLIB_OTHERCLASS_SET_PRIVATE_STRING,
    )

    @Test
    fun testOtherClass_appendText() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.appendText,
        expected = TESTLIB_OTHERCLASS_APPEND_TEXT,
    )

    @Test
    fun testOtherClass_copies() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.copies,
        expected = TESTLIB_OTHERCLASS_COPIES,
    )

    @Test
    fun testOtherClass_ints() = runTest(
        cls = TestData.OtherClass.cls,
        target = TestData.OtherClass.ints,
        expected = TESTLIB_OTHERCLASS_INTS,
    )

    @Test
    fun testTestClass_b() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.b,
        expected = TESTLIB_TESTCLASS_B,
    )

    @Test
    fun testTestClass_st() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.st,
        expected = TESTLIB_TESTCLASS_ST,
    )

    @Test
    fun testTestClass_uit() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.uit,
        expected = TESTLIB_TESTCLASS_UIT,
    )

    @Test
    fun testTestClass_array() =
        try {
            runTest(
                cls = TestData.TestClass.cls,
                target = TestData.TestClass.array,
                ""
            )
            fail("Exception expected, arrays unsupported")
        } catch (_: UnsupportedOperationException) {
            // Expected
        }

    @Test
    fun testTestClass_str() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.str,
        expected = TESTLIB_TESTCLASS_STR,
    )

    @Test
    fun testTestClass_c() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.c,
        expected = TESTLIB_TESTCLASS_C,
    )

    @Test
    fun testTestClass_uc() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.uc,
        expected = TESTLIB_TESTCLASS_UC,
    )

    @Test
    fun testTestClass_s() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.s,
        expected = TESTLIB_TESTCLASS_S,
    )

    @Test
    fun testTestClass_us() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.us,
        expected = TESTLIB_TESTCLASS_US,
    )

    @Test
    fun testTestClass_i() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.i,
        expected = TESTLIB_TESTCLASS_I,
    )

    @Test
    fun testTestClass_ui() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ui,
        expected = TESTLIB_TESTCLASS_UI,
    )

    @Test
    fun testTestClass_l() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.l,
        expected = TESTLIB_TESTCLASS_L,
    )

    @Test
    fun testTestClass_ul() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ul,
        expected = TESTLIB_TESTCLASS_UL,
    )

    @Test
    fun testTestClass_ll() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ll,
        expected = TESTLIB_TESTCLASS_LL,
    )

    @Test
    fun testTestClass_ull() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ull,
        expected = TESTLIB_TESTCLASS_ULL,
    )

    @Test
    fun testTestClass_f() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.f,
        expected = TESTLIB_TESTCLASS_F,
    )

    @Test
    fun testTestClass_d() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.d,
        expected = TESTLIB_TESTCLASS_D,
    )

    @Test
    fun testTestClass_ld() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ld,

        expected = TESTLIB_TESTCLASS_LD,
    )

    @Test
    fun testTestClass_pb() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pb,
        expected = TESTLIB_TESTCLASS_PB,
    )

    @Test
    fun testTestClass_pc() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pc,
        expected = TESTLIB_TESTCLASS_PC,
    )

    @Test
    fun testTestClass_puc() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.puc,
        expected = TESTLIB_TESTCLASS_PUC,
    )

    @Test
    fun testTestClass_ps() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.ps,
        expected = TESTLIB_TESTCLASS_PS,
    )

    @Test
    fun testTestClass_pus() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pus,
        expected = TESTLIB_TESTCLASS_PUS,
    )

    @Test
    fun testTestClass_pi() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pi,
        expected = TESTLIB_TESTCLASS_PI,
    )

    @Test
    fun testTestClass_pui() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pui,
        expected = TESTLIB_TESTCLASS_PUI,
    )

    @Test
    fun testTestClass_pl() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pl,
        expected = TESTLIB_TESTCLASS_PL,
    )

    @Test
    fun testTestClass_pul() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pul,
        expected = TESTLIB_TESTCLASS_PUL,
    )

    @Test
    fun testTestClass_pll() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pll,
        expected = TESTLIB_TESTCLASS_PLL,
    )

    @Test
    fun testTestClass_pull() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pull,
        expected = TESTLIB_TESTCLASS_PULL,
    )

    @Test
    fun testTestClass_pf() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pf,
        expected = TESTLIB_TESTCLASS_PF,
    )

    @Test
    fun testTestClass_pd() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.pd,
        expected = TESTLIB_TESTCLASS_PD,
    )

    @Test
    fun testTestClass_new() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.constructor,
        expected = TESTLIB_TESTCLASS_NEW,
    )

    @Test
    fun testTestClass__new() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.copyConstructor,
        expected = TESTLIB_TESTCLASS__NEW,
    )

    @Test
    fun testTestClass___new() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.otherConstructor,
        expected = TESTLIB_TESTCLASS___NEW,
    )

    @Test
    fun testTestClass____new() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.twoParamConstructor,
        expected = TESTLIB_TESTCLASS____NEW,
    )

    @Test
    fun testTestClass_dispose() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.destructor,
        expected = TESTLIB_TESTCLASS_DISPOSE,
    )

    @Test
    fun testTestClass_sum() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.sum,
        expected = TESTLIB_TESTCLASS_SUM,
    )

    @Test
    fun testTestClass_longPointer() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.longPointer,
        expected = TESTLIB_TESTCLASS_LONG_POINTER,
    )

    @Test
    fun testTestClass_setSome() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.setSome,
        expected = TESTLIB_TESTCLASS_SET_SOME,
    )

    @Test
    fun testTestClass_setPointers() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.setPointers,
        expected = TESTLIB_TESTCLASS_SET_POINTERS,
    )

    @Test
    fun testTestClass_setPrivateString() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.setPrivateString,
        expected = TESTLIB_TESTCLASS_SET_PRIVATE_STRING,
    )

    @Test
    fun testTestClass_setPrivateFrom() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.setPrivateFrom,
        expected = TESTLIB_TESTCLASS_SET_PRIVATE_FROM,
    )

    @Test
    fun testTestClass_output() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.output,
        expected = TESTLIB_TESTCLASS_OUTPUT,
    )

    @Test
    fun testTestClass_op_minus() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorMinus,
        expected = TESTLIB_TESTCLASS_MINUS,
    )

    @Test
    fun testTestClass_op_minus_unary() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorUnaryMinus,
        expected = TESTLIB_TESTCLASS_MINUS_UNARY,
    )

    @Test
    fun testTestClass_op_plus() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorPlus,
        expected = TESTLIB_TESTCLASS_PLUS,
    )

    @Test
    fun testTestClass_op_plus_unary() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorUnaryPlus,
        expected = TESTLIB_TESTCLASS_PLUS_UNARY,
    )

    @Test
    fun testTestClass_op_times() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorTimes,
        expected = TESTLIB_TESTCLASS_TIMES,
    )

    @Test
    fun testTestClass_op_divide() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorDiv,
        expected = TESTLIB_TESTCLASS_DIVIDE,
    )

    @Test
    fun testTestClass_op_modulo() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorRem,
        expected = TESTLIB_TESTCLASS_MODULO,
    )

    @Test
    fun testTestClass_op_preinc() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorInc,
        expected = TESTLIB_TESTCLASS_PRE_INC,
    )

    @Test
    fun testTestClass_op_postinc() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorPostInc,
        expected = TESTLIB_TESTCLASS_POST_INC,
    )

    @Test
    fun testTestClass_op_predec() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorDec,
        expected = TESTLIB_TESTCLASS_PRE_DEC,
    )

    @Test
    fun testTestClass_op_postdec() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorPostDec,
        expected = TESTLIB_TESTCLASS_POST_DEC,
    )

    @Test
    fun testTestClass_op_eq() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorEq,
        expected = TESTLIB_TESTCLASS_EQ_CMP,
    )

    @Test
    fun testTestClass_op_neq() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorNeq,
        expected = TESTLIB_TESTCLASS_NEQ,
    )

    @Test
    fun testTestClass_op_lt() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorLt,
        expected = TESTLIB_TESTCLASS_LT,
    )

    @Test
    fun testTestClass_op_gt() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorGt,
        expected = TESTLIB_TESTCLASS_GT,
    )

    @Test
    fun testTestClass_op_lteq() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorLteq,
        expected = TESTLIB_TESTCLASS_LTEQ,
    )

    @Test
    fun testTestClass_op_gteq() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorGteq,
        expected = TESTLIB_TESTCLASS_GTEQ,
    )

    @Test
    fun testTestClass_op_bnot() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorNot,
        expected = TESTLIB_TESTCLASS_BNOT,
    )

    @Test
    fun testTestClass_op_band() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorBinaryAnd,
        expected = TESTLIB_TESTCLASS_BAND,
    )

    @Test
    fun testTestClass_op_bor() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorBinaryOr,
        expected = TESTLIB_TESTCLASS_BOR,
    )

    @Test
    fun testTestClass_op_not() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorInv,
        expected = TESTLIB_TESTCLASS_NOT,
    )

    @Test
    fun testTestClass_op_and() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorBitwiseAnd,
        expected = TESTLIB_TESTCLASS_AND,
    )

    @Test
    fun testTestClass_op_or() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorBitwiseOr,
        expected = TESTLIB_TESTCLASS_OR,
    )

    @Test
    fun testTestClass_op_xor() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorXor,
        expected = TESTLIB_TESTCLASS_XOR,
    )

    @Test
    fun testTestClass_op_shl() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorShl,
        expected = TESTLIB_TESTCLASS_SHL,
    )

    @Test
    fun testTestClass_op_shr() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorShr,
        expected = TESTLIB_TESTCLASS_SHR,
    )

    @Test
    fun testTestClass_op_ind() = runTest(
        cls = TestData.TestClass.cls,
        target = TestData.TestClass.operatorInd,
        expected = TESTLIB_TESTCLASS_IND,
    )

    private fun runTest(cls: WrappedClass, target: WrappedMethod, expected: String) {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private fun runTest(cls: WrappedClass, target: WrappedField, expected: String) {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private fun runTest(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedMethod,
        expected: String
    ) {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private fun runTest(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedField,
        expected: String
    ) {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private fun buildCode(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedField,
    ): KotlinCodeBuilder {
        val code = codeBuilder()
        val writer = kotlinWriter(code)
        val (rcls, element) = resolveType(cls, target)
        val target = element as ResolvedField
        with(writer) {
            code.onGenerate(rcls, target)
        }
        return code
    }

    private fun buildCode(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedMethod,
    ): KotlinCodeBuilder {
        val code = codeBuilder()
        val writer = kotlinWriter(code)
        val (rcls, element) = resolveType(cls, target)
        val target = element as ResolvedMethod
        with(writer) {
            code.onGenerate(
                rcls, target,
                object : LocalVar {
                    override val name: String
                        get() = "size"

                    override fun build(builder: CodeStringBuilder) {
                        builder.append("size")
                    }
                }
            )
        }
        return code
    }

    private fun resolveType(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedElement
    ): Pair<ResolvedClass, ResolvedElement> {
        val ctx = resolveContext()
        val index =
            cls.first.children.filter { it is WrappedMethod || it is WrappedField }.indexOf(target)
        require(index >= 0) {
            "Unable to find $target in $cls"
        }
        ctx.resolve(cls.second)
        val rcls =
            ctx.tracker.resolvedClasses[cls.second.toString()] ?: error("Can't find resolved $cls")
        val target = rcls.children[index]
        return rcls to target
    }

    private fun buildCode(
        cls: WrappedClass,
        target: WrappedField
    ): KotlinCodeBuilder {
        val code = codeBuilder()
        val writer = kotlinWriter(code)
        val ctx = resolveContext()
        val rcls = cls.resolve(ctx) ?: error("Resolve failed for $cls")
        val target = target.resolve(ctx + cls)
            ?: throw UnsupportedOperationException("Couldn't resolve $target")
        with(writer) {
            code.onGenerate(rcls, target)
        }
        return code
    }

    private fun buildCode(
        cls: WrappedClass,
        target: WrappedMethod
    ): KotlinCodeBuilder {
        val code = codeBuilder()
        val writer = kotlinWriter(code)
        val ctx =
            resolveContext()
        val rcls = cls.resolve(ctx) ?: error("Resolve failed for $cls")
        val target = target.resolve(ctx + cls) ?: error("Resolve failed for $target")
        with(writer) {
            code.onGenerate(
                rcls,
                target,
                object : LocalVar {
                    override val name: String
                        get() = "size"

                    override fun build(builder: CodeStringBuilder) {
                        builder.append("size")
                    }
                }
            )
        }
        return code
    }

    private fun resolveContext() = ResolveContext.Empty
        .withClasses(emptyList())
        .copy(resolver = ParsedResolver(TestData.TU))
        .withPolicy(INCLUDE_MISSING)

    private fun codeBuilder() = KotlinCodeBuilder()

    private fun kotlinWriter(code: KotlinCodeBuilder) =
        KotlinWriter(NameHandler(), "")
}
