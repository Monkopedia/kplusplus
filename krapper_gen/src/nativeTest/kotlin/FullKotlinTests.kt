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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.generator.builders.CodeStringBuilder
import com.monkopedia.krapper.generator.builders.KotlinCodeBuilder
import com.monkopedia.krapper.generator.builders.LocalVar
import com.monkopedia.krapper.generator.codegen.KotlinWriter
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedElement
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedClass
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedElement
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedField
import com.monkopedia.krapper.generator.resolvedmodel.ResolvedMethod
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.runBlocking

class FullKotlinTests {

    private val stdVectorStringNew = "fun MemScope.Vector__String(): Vector__String {\n" +
        "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
        "?: error(\"Allocation failed\"))\n" +
        "    val obj: COpaquePointer = (std_vector_std_string_new(memory) ?: " +
        "error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        std_vector_std_string_dispose(obj)\n" +
        "    }\n" +
        "    return Vector__String(obj, this)\n" +
        "}\n\n"

    private val stdVectorStringDispose = ""

    private val stdVectorStringPushBack =
        "inline fun push_back(str: String?): Unit {\n" +
            "    return std_vector_std_string_push_back(ptr, str)\n" +
            "}"

    private val testlibOtherclassNew =
        "fun MemScope.OtherClass(): OtherClass {\n" +
            "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
            "?: error(\"Allocation failed\"))\n" +
            "    val obj: COpaquePointer = (TestLib_OtherClass_new(memory) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_OtherClass_dispose(obj)\n" +
            "    }\n" +
            "    return OtherClass(obj, this)\n" +
            "}"

    private val testlibOtherclassDispose = ""

    private val testlibOtherclassGetPrivateString =
        "inline fun getPrivateString(): String? {\n" +
            "    val str: CPointer<ByteVar>? = TestLib_OtherClass_get_private_string(ptr)\n" +
            "    val ret: String? = str?.toKString()\n" +
            "    free(str)\n" +
            "    return ret\n" +
            "}"

    private val testlibOtherclassSetPrivateString =
        "inline fun setPrivateString(value: String?): Unit {\n" +
            "    return TestLib_OtherClass_set_private_string(ptr, value)\n" +
            "}"

    private val testlibOtherclassAppendText =
        "inline fun appendText(text: Vector__String): Unit {\n" +
            "    return TestLib_OtherClass_append_text(ptr, text.ptr)\n" +
            "}"
    private val testlibOtherclassCopies =
        "inline fun copies(): MyPair__OtherClass_P? {\n" +
            "    return MyPair__OtherClass_P((TestLib_OtherClass_copies(ptr) ?: return null)" +
            ", memScope)\n" +
            "}"
    private val testlibOtherclassInts =
        "inline fun ints(): MyPair__Int? {\n" +
            "    return MyPair__Int((TestLib_OtherClass_ints(ptr) ?: return null)" +
            ", memScope)\n" +
            "}"

    private val testlibTestclassB =
        "var b: Boolean\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_b_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_b_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassSt =
        "var st: Size_t\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_st_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_st_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUit =
        "var uit: UShort\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_uit_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_uit_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassStr =
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

    private val testlibTestclassC =
        "var c: Byte\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_c_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_c_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUc =
        "var uc: UByte\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_uc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_uc_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassS =
        "var s: Short\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_s_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_s_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUs =
        "var us: UShort\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_us_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_us_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassI =
        "var i: Int\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_i_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_i_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUi =
        "var ui: UInt\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ui_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ui_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassL =
        "var l: Long\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_l_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_l_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUl =
        "var ul: ULong\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ul_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ul_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassLl =
        "var ll: Long\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ll_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ll_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassUll =
        "var ull: ULong\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ull_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ull_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassF =
        "var f: Float\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_f_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_f_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassD =
        "var d: Double\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_d_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_d_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassLd =
        "var ld: Double\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ld_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ld_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPb =
        "var pb: CValuesRef<BooleanVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pb_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pb_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPc =
        "var pc: CValuesRef<ByteVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pc_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPuc =
        "var puc: CValuesRef<UByteVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_puc_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_puc_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPs =
        "var ps: CValuesRef<ShortVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_ps_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_ps_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPus =
        "var pus: CValuesRef<UShortVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pus_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pus_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPi =
        "var pi: CValuesRef<IntVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pi_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pi_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPui =
        "var pui: CValuesRef<UIntVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pui_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pui_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPl =
        "var pl: CValuesRef<LongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pl_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pl_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPul =
        "var pul: CValuesRef<ULongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pul_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pul_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPll =
        "var pll: CValuesRef<LongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pll_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pll_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPull =
        "var pull: CValuesRef<ULongVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pull_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pull_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPf =
        "var pf: CValuesRef<FloatVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pf_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pf_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassPd =
        "var pd: CValuesRef<DoubleVar>?\n" +
            "    inline get() {\n" +
            "        return TestLib_TestClass_pd_get(ptr)\n" +
            "    }\n" +
            "    inline set(value) {\n" +
            "        TestLib_TestClass_pd_set(ptr, value)\n" +
            "    }"

    private val testlibTestclassNew = "fun MemScope.TestClass(): TestClass {\n" +
        "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
        "?: error(\"Allocation failed\"))\n" +
        "    val obj: COpaquePointer = " +
        "(TestLib_TestClass_new(memory) ?: error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        TestLib_TestClass_dispose(obj)\n" +
        "    }\n" +
        "    return TestClass(obj, this)\n" +
        "}"

    private val testlibTestclass2New =
        "fun MemScope.TestClass(other: TestClass): TestClass {\n" +
            "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
            "?: error(\"Allocation failed\"))\n" +
            "    val obj: COpaquePointer = (_TestLib_TestClass_new(memory, other.ptr) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_TestClass_dispose(obj)\n" +
            "    }\n" +
            "    return TestClass(obj, this)\n" +
            "}"

    private val testlibTestclass3New = "fun MemScope.TestClass(a: Int): TestClass {\n" +
        "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
        "?: error(\"Allocation failed\"))\n" +
        "    val obj: COpaquePointer = (__TestLib_TestClass_new(memory, a) ?: " +
        "error(\"Creation failed\"))\n" +
        "    defer {\n" +
        "        TestLib_TestClass_dispose(obj)\n" +
        "    }\n" +
        "    return TestClass(obj, this)\n" +
        "}"

    private val testlibTestclass4New =
        "fun MemScope.TestClass(a: Int, b: Double): TestClass {\n" +
            "    val memory: COpaquePointer = (interpretCPointer(alloc(size, size).rawPtr) " +
            "?: error(\"Allocation failed\"))\n" +
            "    val obj: COpaquePointer = (___TestLib_TestClass_new(memory, a, b) ?: " +
            "error(\"Creation failed\"))\n" +
            "    defer {\n" +
            "        TestLib_TestClass_dispose(obj)\n" +
            "    }\n" +
            "    return TestClass(obj, this)\n" +
            "}"

    private val testlibTestclassDispose = ""

    private val testlibTestclassSum = "inline fun sum(): Long {\n" +
        "    return TestLib_TestClass_sum(ptr)\n" +
        "}"

    private val testlibTestclassLongPointer =
        "inline fun longPointer(): CValuesRef<LongVar>? {\n" +
            "    return TestLib_TestClass_long_pointer(ptr)\n" +
            "}"

    private val testlibTestclassSetSome =
        "inline fun setSome(a: Int, b: Long, c: Long): Unit {\n" +
            "    return TestLib_TestClass_set_some(ptr, a, b, c)\n" +
            "}"

    private val testlibTestclassSetPointers =
        "inline fun setPointers(a: CValuesRef<IntVar>?, b: CValuesRef<LongVar>?, " +
            "c: CValuesRef<LongVar>?): Unit {\n" +
            "    return TestLib_TestClass_set_pointers(ptr, a, b, c)\n" +
            "}"

    private val testlibTestclassSetPrivateString =
        "inline fun setPrivateString(value: String?): Unit {\n" +
            "    return TestLib_TestClass_set_private_string(ptr, value)\n" +
            "}"

    private val testlibTestclassSetPrivateFrom =
        "inline fun setPrivateFrom(value: OtherClass?): Unit {\n" +
            "    return TestLib_TestClass_set_private_from(ptr, value?.ptr)\n" +
            "}"

    private val testlibTestclassOutput = "inline fun output(): Unit {\n" +
        "    return TestLib_TestClass_output(ptr)\n" +
        "}"

    private val testlibTestclassMinus =
        "inline operator fun minus(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_minus(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassMinusUnary =
        "inline operator fun unaryMinus(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_unary_minus(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPlus =
        "inline operator fun plus(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_plus(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPlusUnary =
        "inline operator fun unaryPlus(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_unary_plus(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassTimes =
        "inline operator fun times(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_times(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassDivide =
        "inline operator fun div(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_divide(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassModulo =
        "inline operator fun rem(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_mod(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPreInc =
        "inline operator fun inc(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_increment(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPostInc =
        "inline fun postIncrement(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_post_increment(ptr, 0, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPreDec =
        "inline operator fun dec(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_decrement(ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassPostDec =
        "inline fun postDecrement(): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_post_decrement(ptr, 0, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassEqCmp =
        "inline infix fun eq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_eq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassNeq =
        "inline infix fun neq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_neq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassLt =
        "inline infix fun lt(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_lt(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassGt =
        "inline infix fun gt(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_gt(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassLteq =
        "inline infix fun lteq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_lteq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassGteq =
        "inline infix fun gteq(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_gteq(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassBnot =
        "inline operator fun not(): TestClass? {\n" +
            "    return TestClass((TestLib_TestClass_op_not(ptr) ?: return null), memScope)\n" +
            "}"

    private val testlibTestclassBand =
        "inline infix fun binAnd(c: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_binary_and(ptr, c.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassBor =
        "inline infix fun binOr(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_binary_or(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassNot =
        "inline fun inv(): TestClass? {\n" +
            "    return TestClass((TestLib_TestClass_op_inv(ptr) ?: return null), memScope)\n" +
            "}"

    private val testlibTestclassAnd =
        "inline infix fun and(c: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_and(ptr, c.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassOr =
        "inline infix fun or(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_or(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassXor =
        "inline infix fun xor(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_xor(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassShl =
        "inline infix fun shl(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_shl(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassShr =
        "inline infix fun shr(c2: TestClass): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_shr(ptr, c2.ptr, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    private val testlibTestclassInd =
        "inline operator fun get(c2: String?): TestClass {\n" +
            "    val retValue: TestClass = memScope.TestClass_Holder()\n" +
            "    TestLib_TestClass_op_ind(ptr, c2, retValue.ptr)\n" +
            "    return retValue\n" +
            "}"

    @Test
    fun testVector_new() = runTest(
        cls = TestData.vector.cls,
        target = (TestData.vector.cls.first.children[1] as WrappedMethod),
        expected = stdVectorStringNew
    )

    @Test
    fun testVector_dispose() = runTest(
        cls = TestData.vector.cls,
        target = TestData.vector.cls.first.children[2] as WrappedMethod,
        expected = stdVectorStringDispose
    )

    @Test
    fun testVector_pushBack() = runTest(
        cls = TestData.vector.cls,
        target = TestData.vector.cls.first.children[3] as WrappedMethod,
        expected = stdVectorStringPushBack
    )

    @Test
    fun testOtherClass_new() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.constructor,
        expected = testlibOtherclassNew
    )

    @Test
    fun testOtherClass_dispose() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.destructor,
        expected = testlibOtherclassDispose
    )

    @Test
    fun testOtherClass_getPrivateString() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.getPrivateString,
        expected = testlibOtherclassGetPrivateString
    )

    @Test
    fun testOtherClass_setPrivateString() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.setPrivateString,
        expected = testlibOtherclassSetPrivateString
    )

    @Test
    fun testOtherClass_appendText() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.appendText,
        expected = testlibOtherclassAppendText
    )

    @Test
    fun testOtherClass_copies() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.copies,
        expected = testlibOtherclassCopies
    )

    @Test
    fun testOtherClass_ints() = runTest(
        cls = TestData.otherClass.cls,
        target = TestData.otherClass.ints,
        expected = testlibOtherclassInts
    )

    @Test
    fun testTestClass_b() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.b,
        expected = testlibTestclassB
    )

    @Test
    fun testTestClass_st() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.st,
        expected = testlibTestclassSt
    )

    @Test
    fun testTestClass_uit() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.uit,
        expected = testlibTestclassUit
    )

    @Test
    fun testTestClass_array() = try {
        runTest(
            cls = TestData.testClass.cls,
            target = TestData.testClass.array,
            ""
        )
        fail("Exception expected, arrays unsupported")
    } catch (_: UnsupportedOperationException) {
        // Expected
    }

    @Test
    fun testTestClass_str() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.str,
        expected = testlibTestclassStr
    )

    @Test
    fun testTestClass_c() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.c,
        expected = testlibTestclassC
    )

    @Test
    fun testTestClass_uc() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.uc,
        expected = testlibTestclassUc
    )

    @Test
    fun testTestClass_s() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.s,
        expected = testlibTestclassS
    )

    @Test
    fun testTestClass_us() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.us,
        expected = testlibTestclassUs
    )

    @Test
    fun testTestClass_i() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.i,
        expected = testlibTestclassI
    )

    @Test
    fun testTestClass_ui() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ui,
        expected = testlibTestclassUi
    )

    @Test
    fun testTestClass_l() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.l,
        expected = testlibTestclassL
    )

    @Test
    fun testTestClass_ul() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ul,
        expected = testlibTestclassUl
    )

    @Test
    fun testTestClass_ll() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ll,
        expected = testlibTestclassLl
    )

    @Test
    fun testTestClass_ull() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ull,
        expected = testlibTestclassUll
    )

    @Test
    fun testTestClass_f() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.f,
        expected = testlibTestclassF
    )

    @Test
    fun testTestClass_d() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.d,
        expected = testlibTestclassD
    )

    @Test
    fun testTestClass_ld() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ld,

        expected = testlibTestclassLd
    )

    @Test
    fun testTestClass_pb() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pb,
        expected = testlibTestclassPb
    )

    @Test
    fun testTestClass_pc() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pc,
        expected = testlibTestclassPc
    )

    @Test
    fun testTestClass_puc() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.puc,
        expected = testlibTestclassPuc
    )

    @Test
    fun testTestClass_ps() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.ps,
        expected = testlibTestclassPs
    )

    @Test
    fun testTestClass_pus() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pus,
        expected = testlibTestclassPus
    )

    @Test
    fun testTestClass_pi() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pi,
        expected = testlibTestclassPi
    )

    @Test
    fun testTestClass_pui() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pui,
        expected = testlibTestclassPui
    )

    @Test
    fun testTestClass_pl() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pl,
        expected = testlibTestclassPl
    )

    @Test
    fun testTestClass_pul() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pul,
        expected = testlibTestclassPul
    )

    @Test
    fun testTestClass_pll() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pll,
        expected = testlibTestclassPll
    )

    @Test
    fun testTestClass_pull() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pull,
        expected = testlibTestclassPull
    )

    @Test
    fun testTestClass_pf() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pf,
        expected = testlibTestclassPf
    )

    @Test
    fun testTestClass_pd() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.pd,
        expected = testlibTestclassPd
    )

    @Test
    fun testTestClass_new() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.constructor,
        expected = testlibTestclassNew
    )

    @Test
    fun testTestClass__new() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.copyConstructor,
        expected = testlibTestclass2New
    )

    @Test
    fun testTestClass___new() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.otherConstructor,
        expected = testlibTestclass3New
    )

    @Test
    fun testTestClass____new() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.twoParamConstructor,
        expected = testlibTestclass4New
    )

    @Test
    fun testTestClass_dispose() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.destructor,
        expected = testlibTestclassDispose
    )

    @Test
    fun testTestClass_sum() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.sum,
        expected = testlibTestclassSum
    )

    @Test
    fun testTestClass_longPointer() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.longPointer,
        expected = testlibTestclassLongPointer
    )

    @Test
    fun testTestClass_setSome() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.setSome,
        expected = testlibTestclassSetSome
    )

    @Test
    fun testTestClass_setPointers() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.setPointers,
        expected = testlibTestclassSetPointers
    )

    @Test
    fun testTestClass_setPrivateString() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.setPrivateString,
        expected = testlibTestclassSetPrivateString
    )

    @Test
    fun testTestClass_setPrivateFrom() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.setPrivateFrom,
        expected = testlibTestclassSetPrivateFrom
    )

    @Test
    fun testTestClass_output() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.output,
        expected = testlibTestclassOutput
    )

    @Test
    fun testTestClass_op_minus() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorMinus,
        expected = testlibTestclassMinus
    )

    @Test
    fun testTestClass_op_minus_unary() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorUnaryMinus,
        expected = testlibTestclassMinusUnary
    )

    @Test
    fun testTestClass_op_plus() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorPlus,
        expected = testlibTestclassPlus
    )

    @Test
    fun testTestClass_op_plus_unary() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorUnaryPlus,
        expected = testlibTestclassPlusUnary
    )

    @Test
    fun testTestClass_op_times() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorTimes,
        expected = testlibTestclassTimes
    )

    @Test
    fun testTestClass_op_divide() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorDiv,
        expected = testlibTestclassDivide
    )

    @Test
    fun testTestClass_op_modulo() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorRem,
        expected = testlibTestclassModulo
    )

    @Test
    fun testTestClass_op_preinc() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorInc,
        expected = testlibTestclassPreInc
    )

    @Test
    fun testTestClass_op_postinc() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorPostInc,
        expected = testlibTestclassPostInc
    )

    @Test
    fun testTestClass_op_predec() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorDec,
        expected = testlibTestclassPreDec
    )

    @Test
    fun testTestClass_op_postdec() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorPostDec,
        expected = testlibTestclassPostDec
    )

    @Test
    fun testTestClass_op_eq() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorEq,
        expected = testlibTestclassEqCmp
    )

    @Test
    fun testTestClass_op_neq() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorNeq,
        expected = testlibTestclassNeq
    )

    @Test
    fun testTestClass_op_lt() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorLt,
        expected = testlibTestclassLt
    )

    @Test
    fun testTestClass_op_gt() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorGt,
        expected = testlibTestclassGt
    )

    @Test
    fun testTestClass_op_lteq() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorLteq,
        expected = testlibTestclassLteq
    )

    @Test
    fun testTestClass_op_gteq() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorGteq,
        expected = testlibTestclassGteq
    )

    @Test
    fun testTestClass_op_bnot() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorNot,
        expected = testlibTestclassBnot
    )

    @Test
    fun testTestClass_op_band() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorBinaryAnd,
        expected = testlibTestclassBand
    )

    @Test
    fun testTestClass_op_bor() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorBinaryOr,
        expected = testlibTestclassBor
    )

    @Test
    fun testTestClass_op_not() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorInv,
        expected = testlibTestclassNot
    )

    @Test
    fun testTestClass_op_and() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorBitwiseAnd,
        expected = testlibTestclassAnd
    )

    @Test
    fun testTestClass_op_or() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorBitwiseOr,
        expected = testlibTestclassOr
    )

    @Test
    fun testTestClass_op_xor() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorXor,
        expected = testlibTestclassXor
    )

    @Test
    fun testTestClass_op_shl() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorShl,
        expected = testlibTestclassShl
    )

    @Test
    fun testTestClass_op_shr() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorShr,
        expected = testlibTestclassShr
    )

    @Test
    fun testTestClass_op_ind() = runTest(
        cls = TestData.testClass.cls,
        target = TestData.testClass.operatorInd,
        expected = testlibTestclassInd
    )

    private fun runTest(cls: WrappedClass, target: WrappedMethod, expected: String): Unit =
        runBlocking {
            assertCode(expected, buildCode(cls, target).toString())
        }

    private fun runTest(cls: WrappedClass, target: WrappedField, expected: String): Unit =
        runBlocking {
            assertCode(expected, buildCode(cls, target).toString())
        }

    private fun runTest(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedMethod,
        expected: String
    ): Unit = runBlocking {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private fun runTest(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedField,
        expected: String
    ): Unit = runBlocking {
        assertCode(expected, buildCode(cls, target).toString())
    }

    private suspend fun buildCode(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedField
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

    private suspend fun buildCode(
        cls: Pair<WrappedTemplate, WrappedTemplateType>,
        target: WrappedMethod
    ): KotlinCodeBuilder {
        val code = codeBuilder()
        val writer = kotlinWriter(code)
        val (rcls, element) = resolveType(cls, target)
        val target = element as ResolvedMethod
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

    private suspend fun resolveType(
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

    private suspend fun buildCode(cls: WrappedClass, target: WrappedField): KotlinCodeBuilder {
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

    private suspend fun buildCode(cls: WrappedClass, target: WrappedMethod): KotlinCodeBuilder {
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
        .copy(resolver = ParsedResolver(TestData.tu))
        .withPolicy(INCLUDE_MISSING)

    private fun codeBuilder() = KotlinCodeBuilder()

    private fun kotlinWriter(code: KotlinCodeBuilder) = KotlinWriter("")
}
