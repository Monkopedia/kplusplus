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

import com.monkopedia.krapper.generator.ReferencePolicy.INCLUDE_MISSING
import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.HeaderWriter
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
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.runBlocking

class CppHeaderTests {

    private val empty = """
        |#ifndef __DESIRED_WRAPPER__
        |    #define __DESIRED_WRAPPER__
        |
        |    #include <stdlib.h>
        |    #include <stdint.h>
        |    #include <stdbool.h>
        |
        |    #ifdef __cplusplus
        |        extern "C" {
        |    #endif //__cplusplus
        |
        |
        |    #ifdef __cplusplus
        |        }
        |    #endif //__cplusplus
        |
        |#endif //__DESIRED_WRAPPER__
        |
    """.trimMargin()

    private val stdVectorStringNew = "void* std_vector_std_string_new(void* location);\n\n"

    private val stdVectorStringDispose = "void std_vector_std_string_dispose(void* thiz);\n\n"

    private val stdVectorStringPushBack =
        "void std_vector_std_string_push_back(void* thiz, const char* str);\n\n"

    private val testlibOtherclassNew = "void* TestLib_OtherClass_new(void* location);\n\n"

    private val testlibOtherclassDispose = "void TestLib_OtherClass_dispose(void* thiz);\n\n"

    private val testlibOtherclassGetPrivateString =
        "const char* TestLib_OtherClass_get_private_string(void* thiz);\n\n"

    private val testlibOtherclassSetPrivateString =
        "void TestLib_OtherClass_set_private_string(void* thiz, const char* value);\n\n"

    private val testlibOtherclassAppendText =
        "void TestLib_OtherClass_append_text(void* thiz, void* text);\n\n"
    private val testlibOtherclassCopies =
        "void* TestLib_OtherClass_copies(void* thiz);\n\n"
    private val testlibOtherclassInts =
        "void* TestLib_OtherClass_ints(void* thiz);\n\n"

    private val testlibTestclassB =
        "bool TestLib_TestClass_b_get(void* thiz);\n\n" +
            "void TestLib_TestClass_b_set(void* thiz, bool value);\n\n"

    private val testlibTestclassSt =
        "size_t TestLib_TestClass_st_get(void* thiz);\n\n" +
            "void TestLib_TestClass_st_set(void* thiz, size_t value);\n\n"

    private val testlibTestclassUit =
        "uint16_t TestLib_TestClass_uit_get(void* thiz);\n\n" +
            "void TestLib_TestClass_uit_set(void* thiz, uint16_t value);\n\n"

    private val testlibTestclassStr =
        "const char* TestLib_TestClass_str_get(void* thiz);\n\n" +
            "void TestLib_TestClass_str_set(void* thiz, const char* value);\n\n"

    private val testlibTestclassC =
        "signed char TestLib_TestClass_c_get(void* thiz);\n\n" +
            "void TestLib_TestClass_c_set(void* thiz, signed char value);\n\n"

    private val testlibTestclassUc =
        "unsigned char TestLib_TestClass_uc_get(void* thiz);\n\n" +
            "void TestLib_TestClass_uc_set(void* thiz, unsigned char value);\n\n"

    private val testlibTestclassS =
        "signed short TestLib_TestClass_s_get(void* thiz);\n\n" +
            "void TestLib_TestClass_s_set(void* thiz, signed short value);\n\n"

    private val testlibTestclassUs =
        "unsigned short TestLib_TestClass_us_get(void* thiz);\n\n" +
            "void TestLib_TestClass_us_set(void* thiz, unsigned short value);\n\n"

    private val testlibTestclassI =
        "signed int TestLib_TestClass_i_get(void* thiz);\n\n" +
            "void TestLib_TestClass_i_set(void* thiz, signed int value);\n\n"

    private val testlibTestclassUi =
        "unsigned int TestLib_TestClass_ui_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ui_set(void* thiz, unsigned int value);\n\n"

    private val testlibTestclassL =
        "signed long TestLib_TestClass_l_get(void* thiz);\n\n" +
            "void TestLib_TestClass_l_set(void* thiz, signed long value);\n\n"

    private val testlibTestclassUl =
        "unsigned long TestLib_TestClass_ul_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ul_set(void* thiz, unsigned long value);\n\n"

    private val testlibTestclassLl =
        "signed long long TestLib_TestClass_ll_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ll_set(void* thiz, signed long long value);\n\n"

    private val testlibTestclassUll =
        "unsigned long long TestLib_TestClass_ull_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ull_set(void* thiz, unsigned long long value);\n\n"

    private val testlibTestclassF =
        "float TestLib_TestClass_f_get(void* thiz);\n\n" +
            "void TestLib_TestClass_f_set(void* thiz, float value);\n\n"

    private val testlibTestclassD =
        "double TestLib_TestClass_d_get(void* thiz);\n\n" +
            "void TestLib_TestClass_d_set(void* thiz, double value);\n\n"

    private val testlibTestclassLd =
        "double TestLib_TestClass_ld_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ld_set(void* thiz, double value);\n\n"

    private val testlibTestclassPb =
        "bool* TestLib_TestClass_pb_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pb_set(void* thiz, bool* value);\n\n"

    private val testlibTestclassPc =
        "signed char* TestLib_TestClass_pc_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pc_set(void* thiz, signed char* value);\n\n"

    private val testlibTestclassPuc =
        "unsigned char* TestLib_TestClass_puc_get(void* thiz);\n\n" +
            "void TestLib_TestClass_puc_set(void* thiz, unsigned char* value);\n\n"

    private val testlibTestclassPs =
        "signed short* TestLib_TestClass_ps_get(void* thiz);\n\n" +
            "void TestLib_TestClass_ps_set(void* thiz, signed short* value);\n\n"

    private val testlibTestclassPus =
        "unsigned short* TestLib_TestClass_pus_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pus_set(void* thiz, unsigned short* value);\n\n"

    private val testlibTestclassPi =
        "signed int* TestLib_TestClass_pi_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pi_set(void* thiz, signed int* value);\n\n"

    private val testlibTestclassPui =
        "unsigned int* TestLib_TestClass_pui_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pui_set(void* thiz, unsigned int* value);\n\n"

    private val testlibTestclassPl =
        "signed long* TestLib_TestClass_pl_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pl_set(void* thiz, signed long* value);\n\n"

    private val testlibTestclassPul =
        "unsigned long* TestLib_TestClass_pul_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pul_set(void* thiz, unsigned long* value);\n\n"

    private val testlibTestclassPll =
        "signed long long* TestLib_TestClass_pll_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pll_set(void* thiz, signed long long* value);\n\n"

    private val testlibTestclassPull =
        "unsigned long long* TestLib_TestClass_pull_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pull_set(void* thiz, unsigned long long* value);\n\n"

    private val testlibTestclassPf =
        "float* TestLib_TestClass_pf_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pf_set(void* thiz, float* value);\n\n"

    private val testlibTestclassPd =
        "double* TestLib_TestClass_pd_get(void* thiz);\n\n" +
            "void TestLib_TestClass_pd_set(void* thiz, double* value);\n\n"

    private val testlibTestclassNew = "void* TestLib_TestClass_new(void* location);\n\n"

    private val testlibTestclass2New =
        "void* _TestLib_TestClass_new(void* location, void* other);\n\n"

    private val testlibTestclass3New =
        "void* __TestLib_TestClass_new(void* location, int a);\n\n"

    private val testlibTestclass4New =
        "void* ___TestLib_TestClass_new(void* location, int a, double b);\n\n"

    private val testlibTestclassDispose = "void TestLib_TestClass_dispose(void* thiz);\n\n"

    private val testlibTestclassSum = "long TestLib_TestClass_sum(void* thiz);\n\n"

    private val testlibTestclassLongPointer =
        "long* TestLib_TestClass_long_pointer(void* thiz);\n\n"

    private val testlibTestclassSetSome =
        "void TestLib_TestClass_set_some(void* thiz, int a, long b, long long c);\n\n"

    private val testlibTestclassSetPointers =
        "void TestLib_TestClass_set_pointers(void* thiz, int* a, long* b, long long* c);\n\n"

    private val testlibTestclassSetPrivateString =
        "void TestLib_TestClass_set_private_string(void* thiz, const char* value);\n\n"

    private val testlibTestclassSetPrivateFrom =
        "void TestLib_TestClass_set_private_from(void* thiz, void* value);\n\n"

    private val testlibTestclassOutput = "void TestLib_TestClass_output(void* thiz);\n\n"

    private val testlibTestclassMinus =
        "void TestLib_TestClass_op_minus(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassMinusUnary =
        "void TestLib_TestClass_op_unary_minus(void* thiz, void* ret_value);\n\n"

    private val testlibTestclassPlus =
        "void TestLib_TestClass_op_plus(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassPlusUnary =
        "void TestLib_TestClass_op_unary_plus(void* thiz, void* ret_value);\n\n"

    private val testlibTestclassTimes =
        "void TestLib_TestClass_op_times(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassDivide =
        "void TestLib_TestClass_op_divide(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassModulo =
        "void TestLib_TestClass_op_mod(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassPreInc =
        "void TestLib_TestClass_op_increment(void* thiz, void* ret_value);\n\n"

    private val testlibTestclassPostInc =
        "void TestLib_TestClass_op_post_increment(void* thiz, int dummy, void* ret_value);\n\n"

    private val testlibTestclassPreDec =
        "void TestLib_TestClass_op_decrement(void* thiz, void* ret_value);\n\n"

    private val testlibTestclassPostDec =
        "void TestLib_TestClass_op_post_decrement(void* thiz, int dummy, void* ret_value);\n\n"

    private val testlibTestclassEqCmp =
        "void TestLib_TestClass_op_eq(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassNeq =
        "void TestLib_TestClass_op_neq(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassLt =
        "void TestLib_TestClass_op_lt(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassGt =
        "void TestLib_TestClass_op_gt(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassLteq =
        "void TestLib_TestClass_op_lteq(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassGteq =
        "void TestLib_TestClass_op_gteq(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassBnot =
        "void* TestLib_TestClass_op_not(void* thiz);\n\n"

    private val testlibTestclassBand =
        "void TestLib_TestClass_op_binary_and(void* thiz, void* c, void* ret_value);\n\n"

    private val testlibTestclassBor =
        "void TestLib_TestClass_op_binary_or(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassNot =
        "void* TestLib_TestClass_op_inv(void* thiz);\n\n"

    private val testlibTestclassAnd =
        "void TestLib_TestClass_op_and(void* thiz, void* c, void* ret_value);\n\n"

    private val testlibTestclassOr =
        "void TestLib_TestClass_op_or(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassXor =
        "void TestLib_TestClass_op_xor(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassShl =
        "void TestLib_TestClass_op_shl(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassShr =
        "void TestLib_TestClass_op_shr(void* thiz, void* c2, void* ret_value);\n\n"

    private val testlibTestclassInd =
        "void TestLib_TestClass_op_ind(void* thiz, const char* c2, void* ret_value);\n\n"

    @Test
    fun testVector_new() = runTest(
        cls = TestData.vector.cls,
        target = TestData.vector.constructor,
        expected = stdVectorStringNew
    )

    @Test
    fun testVector_dispose() = runTest(
        cls = TestData.vector.cls,
        target = TestData.vector.destructor,
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
    ): CppCodeBuilder {
        val code = codeBuilder()
        val writer = headerWriter(code)
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
    ): CppCodeBuilder {
        val code = codeBuilder()
        val writer = headerWriter(code)
        val (rcls, element) = resolveType(cls, target)
        val target = element as ResolvedMethod
        with(writer) {
            code.onGenerate(rcls, target)
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

    private suspend fun buildCode(cls: WrappedClass, target: WrappedField): CppCodeBuilder {
        val code = codeBuilder()
        val writer = headerWriter(code)
        val ctx = resolveContext()
        val rcls = cls.resolve(ctx) ?: error("Resolve failed for $cls")
        val target = target.resolve(ctx + cls)
            ?: throw UnsupportedOperationException("Couldn't resolve $target")
        with(writer) {
            code.onGenerate(rcls, target)
        }
        return code
    }

    private suspend fun buildCode(cls: WrappedClass, target: WrappedMethod): CppCodeBuilder {
        val code = codeBuilder()
        val writer = headerWriter(code)
        val ctx = resolveContext()
        val rcls = cls.resolve(ctx) ?: error("Resolve failed for $cls")
        val target = target.resolve(ctx + cls) ?: error("Resolve failed for $target")
        with(writer) {
            code.onGenerate(rcls, target)
        }
        return code
    }

    private fun resolveContext() = ResolveContext.Empty
        .withClasses(emptyList())
        .copy(resolver = ParsedResolver(TestData.TU))
        .withPolicy(INCLUDE_MISSING)

    private fun codeBuilder() = CppCodeBuilder()

    private fun headerWriter(code: CppCodeBuilder) = HeaderWriter(code)
}

fun assertCode(expected: String, actual: String) {
    assertCodeInner(expected.trim(), actual.trim())
}

fun assertCodeInner(expected: String, fullActual: String) {
    val expectedList = expected.split("\n")
    val actualList = fullActual.split("\n")
    expectedList.zip(actualList).forEachIndexed { index, (expected, actual) ->
        assertEquals(
            expected,
            actual,
            buildString {
                append("Line #$index, difference at ")
                append(expected.zip(actual).indexOfFirst { it.first != it.second })
                appendLine()
                append("expected: ")
                append(expected)
                appendLine()
                append("actual  : ")
                append(actual)
                appendLine()
                append(
                    Array(
                        expected.zip(actual).indexOfFirst { it.first != it.second }.coerceAtLeast(0)
                    ) { ' ' }.joinToString("")
                )
                append("          ^")
                appendLine()
                appendLine()
                append("Full actual:\n")
                append(fullActual)
            }
        )
    }
    assertEquals(
        expectedList.size,
        actualList.size,
        "Incorrect number of lines missing:\n${
            if (expectedList.size > actualList.size) {
                expectedList.subList(actualList.size, expectedList.size).joinToString("\n")
            } else {
                actualList.subList(expectedList.size, actualList.size).joinToString("\n")
            }
        }"
    )
}
