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
import com.monkopedia.krapper.generator.codegen.CppWriter
import com.monkopedia.krapper.generator.codegen.File
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

class CppCodeTests {
    private val file = File("/tmp/out.cpp")
    private val empty = """
        #include "desired_wrapper.h"
        #include <vector>
        #include <string>
        #include <iterator>
        
        extern "C" {
        
        typedef void (*StackConstructorCallback)(void*, void*);
        
        
        }
    """.trimIndent()
    private val stdVectorStringNew = """
        void* std_vector_std_string_new(void* location) {
            return new (location) std::vector<std::string>();
        }
    """.trimIndent()
    private val stdVectorStringDispose = """
        void std_vector_std_string_dispose(void* thiz) {
            std::vector<std::string>* thiz_cast = reinterpret_cast<std::vector<std::string>*>(thiz);
            thiz_cast->~std::vector();
        }
    """.trimIndent()
    private val stdVectorStringPushBack = """
        void std_vector_std_string_push_back(void* thiz, const char* str) {
            std::vector<std::string>* thiz_cast = reinterpret_cast<std::vector<std::string>*>(thiz);
            std::string str_cast = std::string(str);
            thiz_cast->push_back(str_cast);
        }
    """.trimIndent()
    private val testlibOtherclassNew = """
        void* TestLib_OtherClass_new(void* location) {
            return new (location) TestLib::OtherClass();
        }
    """.trimIndent()
    private val testlibOtherclassDispose = """
        void TestLib_OtherClass_dispose(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            thiz_cast->~TestLib::OtherClass();
        }
    """.trimIndent()
    private val testlibOtherclassGetPrivateString = """
        const char* TestLib_OtherClass_get_private_string(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::string ret_value = thiz_cast->getPrivateString();
            char* ret_value_cast = new char[ret_value.length() + 1];
            ret_value.copy(ret_value_cast, ret_value.length(), 0);
            return ret_value_cast;
        }
    """.trimIndent()
    private val testlibOtherclassSetPrivateString = """
        void TestLib_OtherClass_set_private_string(void* thiz, const char* value) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::string value_cast = std::string(value);
            thiz_cast->setPrivateString(value_cast);
        }
    """.trimIndent()
    private val testlibOtherclassAppendText = """
        void TestLib_OtherClass_append_text(void* thiz, void* text) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::vector<std::string>* text_cast = reinterpret_cast<std::vector<std::string>*>(text);
            thiz_cast->appendText(*text_cast);
        }
    """.trimIndent()
    private val testlibOtherclassCopies = """
        void* TestLib_OtherClass_copies(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            return (void*)thiz_cast->copies();
        }
    """.trimIndent()
    private val testlibOtherclassInts = """
        void* TestLib_OtherClass_ints(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            return (void*)thiz_cast->ints();
        }
    """.trimIndent()
    private val testlibTestclassSetPrivateFrom = """
        void TestLib_TestClass_set_private_from(void* thiz, void* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::OtherClass* value_cast = reinterpret_cast<TestLib::OtherClass*>(value);
            thiz_cast->setPrivateFrom(value_cast);
        }

    """.trimIndent()
    private val testlibTestclassNew = """
        void* TestLib_TestClass_new(void* location) {
            return new (location) TestLib::TestClass();
        }
    """.trimIndent()
    private val testlibTestclass2New = """
        void* _TestLib_TestClass_new(void* location, void* other) {
            const TestLib::TestClass* other_cast = reinterpret_cast<const TestLib::TestClass*>(other);
            return new (location) TestLib::TestClass(*other_cast);
        }
    """.trimIndent()
    private val testlibTestclass3New = """
        void* __TestLib_TestClass_new(void* location, int a) {
            return new (location) TestLib::TestClass(a);
        }
    """.trimIndent()
    private val testlibTestclass4New = """
        void* ___TestLib_TestClass_new(void* location, int a, double b) {
            return new (location) TestLib::TestClass(a, b);
        }
    """.trimIndent()
    private val testlibTestclassDispose = """
        void TestLib_TestClass_dispose(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->~TestClass();
        }
    """.trimIndent()
    private val testlibTestclassB = """
        bool TestLib_TestClass_b_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->b;
        }
        
        void TestLib_TestClass_b_set(void* thiz, bool value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->b = value);
        }
    """.trimIndent()
    private val testlibTestclassSt = """
        size_t TestLib_TestClass_st_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->st;
        }
        
        void TestLib_TestClass_st_set(void* thiz, size_t value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->st = value);
        }
    """.trimIndent()
    private val testlibTestclassUit = """
        uint16_t TestLib_TestClass_uit_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->uit;
        }
        
        void TestLib_TestClass_uit_set(void* thiz, uint16_t value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->uit = value);
        }
    """.trimIndent()
    private val testlibTestclassStr = """
        const char* TestLib_TestClass_str_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string ret_value = thiz_cast->str;
            char* ret_value_cast = new char[ret_value.length() + 1];
            ret_value.copy(ret_value_cast, ret_value.length(), 0);
            return ret_value_cast;
        }
        
        void TestLib_TestClass_str_set(void* thiz, const char* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string value_cast = std::string(value);
            (thiz_cast->str = value_cast);
        }
    """.trimIndent()
    private val testlibTestclassC = """
        signed char TestLib_TestClass_c_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->c;
        }
        
        void TestLib_TestClass_c_set(void* thiz, signed char value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->c = value);
        }
    """.trimIndent()
    private val testlibTestclassUc = """
        unsigned char TestLib_TestClass_uc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->uc;
        }
        
        void TestLib_TestClass_uc_set(void* thiz, unsigned char value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->uc = value);
        }
    """.trimIndent()
    private val testlibTestclassS = """
        signed short TestLib_TestClass_s_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->s;
        }
        
        void TestLib_TestClass_s_set(void* thiz, signed short value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->s = value);
        }
    """.trimIndent()
    private val testlibTestclassUs = """
        unsigned short TestLib_TestClass_us_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->us;
        }
        
        void TestLib_TestClass_us_set(void* thiz, unsigned short value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->us = value);
        }
    """.trimIndent()
    private val testlibTestclassI = """
        signed int TestLib_TestClass_i_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->i;
        }
        
        void TestLib_TestClass_i_set(void* thiz, signed int value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->i = value);
        }
    """.trimIndent()
    private val testlibTestclassUi = """
        unsigned int TestLib_TestClass_ui_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ui;
        }
        
        void TestLib_TestClass_ui_set(void* thiz, unsigned int value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->ui = value);
        }
    """.trimIndent()
    private val testlibTestclassL = """
        signed long TestLib_TestClass_l_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->l;
        }
        
        void TestLib_TestClass_l_set(void* thiz, signed long value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->l = value);
        }
    """.trimIndent()
    private val testlibTestclassUl = """
        unsigned long TestLib_TestClass_ul_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ul;
        }
        
        void TestLib_TestClass_ul_set(void* thiz, unsigned long value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->ul = value);
        }
    """.trimIndent()
    private val testlibTestclassLl = """
        signed long long TestLib_TestClass_ll_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ll;
        }
        
        void TestLib_TestClass_ll_set(void* thiz, signed long long value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->ll = value);
        }
    """.trimIndent()
    private val testlibTestclassUll = """
        unsigned long long TestLib_TestClass_ull_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ull;
        }
        
        void TestLib_TestClass_ull_set(void* thiz, unsigned long long value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->ull = value);
        }
    """.trimIndent()
    private val testlibTestclassF = """
        float TestLib_TestClass_f_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->f;
        }
        
        void TestLib_TestClass_f_set(void* thiz, float value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->f = value);
        }
    """.trimIndent()
    private val testlibTestclassD = """
        double TestLib_TestClass_d_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->d;
        }
        
        void TestLib_TestClass_d_set(void* thiz, double value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->d = value);
        }
    """.trimIndent()
    private val testlibTestclassLd = """
        double TestLib_TestClass_ld_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ld;
        }
        
        void TestLib_TestClass_ld_set(void* thiz, double value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            long double value_cast = (long double)value;
            (thiz_cast->ld = value_cast);
        }
    """.trimIndent()
    private val testlibTestclassPb = """
         bool* TestLib_TestClass_pb_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pb;
        }
        
        void TestLib_TestClass_pb_set(void* thiz, bool* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pb = value);
        }
    """.trimIndent()
    private val testlibTestclassPc = """
        signed char* TestLib_TestClass_pc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pc;
        }
        
        void TestLib_TestClass_pc_set(void* thiz, signed char* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pc = value);
        }
    """.trimIndent()
    private val testlibTestclassPuc = """
        unsigned char* TestLib_TestClass_puc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->puc;
        }
        
        void TestLib_TestClass_puc_set(void* thiz, unsigned char* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->puc = value);
        }
    """.trimIndent()
    private val testlibTestclassPs = """
         signed short* TestLib_TestClass_ps_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ps;
        }
        
        void TestLib_TestClass_ps_set(void* thiz, signed short* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->ps = value);
        }
    """.trimIndent()
    private val testlibTestclassPus = """
         unsigned short* TestLib_TestClass_pus_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pus;
        }
        
        void TestLib_TestClass_pus_set(void* thiz, unsigned short* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pus = value);
        }
    """.trimIndent()
    private val testlibTestclassPi = """
        signed int* TestLib_TestClass_pi_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pi;
        }
        
        void TestLib_TestClass_pi_set(void* thiz, signed int* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pi = value);
        }
    """.trimIndent()
    private val testlibTestclassPui = """
        unsigned int* TestLib_TestClass_pui_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pui;
        }
        
        void TestLib_TestClass_pui_set(void* thiz, unsigned int* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pui = value);
        }
    """.trimIndent()
    private val testlibTestclassPl = """
        signed long* TestLib_TestClass_pl_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pl;
        }
        
        void TestLib_TestClass_pl_set(void* thiz, signed long* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pl = value);
        }
    """.trimIndent()
    private val testlibTestclassPul = """
        unsigned long* TestLib_TestClass_pul_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pul;
        }
        
        void TestLib_TestClass_pul_set(void* thiz, unsigned long* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pul = value);
        }
    """.trimIndent()
    private val testlibTestclassPll = """
        signed long long* TestLib_TestClass_pll_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pll;
        }
        
        void TestLib_TestClass_pll_set(void* thiz, signed long long* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pll = value);
        }
    """.trimIndent()
    private val testlibTestclassPull = """
        unsigned long long* TestLib_TestClass_pull_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pull;
        }
        
        void TestLib_TestClass_pull_set(void* thiz, unsigned long long* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pull = value);
        }
    """.trimIndent()
    private val testlibTestclassPf = """
        float* TestLib_TestClass_pf_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pf;
        }
        
        void TestLib_TestClass_pf_set(void* thiz, float* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pf = value);
        }
    """.trimIndent()
    private val testlibTestclassPd = """
        double* TestLib_TestClass_pd_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pd;
        }
        
        void TestLib_TestClass_pd_set(void* thiz, double* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            (thiz_cast->pd = value);
        }
    """.trimIndent()
    private val testlibTestclassSum = """
         long TestLib_TestClass_sum(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->sum();
        }
    """.trimIndent()
    private val testlibTestclassLongPointer = """
        long* TestLib_TestClass_long_pointer(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->longPointer();
        }
    """.trimIndent()
    private val testlibTestclassSetSome = """
        void TestLib_TestClass_set_some(void* thiz, int a, long b, long long c) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->setSome(a, b, c);
        }
    """.trimIndent()
    private val testlibTestclassSetPointers = """
        void TestLib_TestClass_set_pointers(void* thiz, int* a, long* b, long long* c) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->setPointers(a, b, c);
        }
    """.trimIndent()
    private val testlibTestclassSetPrivateString = """
        void TestLib_TestClass_set_private_string(void* thiz, const char* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string value_cast = std::string(value);
            thiz_cast->setPrivateString(value_cast);
        }
    """.trimIndent()
    private val testlibTestclassOutput = """
        void TestLib_TestClass_output(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->output();
        }
    """.trimIndent()
    private val testlibTestclassMinus = """
        void TestLib_TestClass_op_minus(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast - *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassMinusUnary = """
            void TestLib_TestClass_op_unary_minus(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator-());
        }
    """.trimIndent()
    private val testlibTestclassPlus = """
          void TestLib_TestClass_op_plus(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast + *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassPlusUnary = """
            void TestLib_TestClass_op_unary_plus(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator+());
        }
    """.trimIndent()
    private val testlibTestclassTimes = """
        void TestLib_TestClass_op_times(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast * *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassDivide = """
        void TestLib_TestClass_op_divide(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast / *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassModulo = """
        void TestLib_TestClass_op_mod(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast % *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassPreInc = """
        void TestLib_TestClass_op_increment(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator++());
        }
    """.trimIndent()
    private val testlibTestclassPostInc = """
         void TestLib_TestClass_op_post_increment(void* thiz, int dummy, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator++(dummy));
        }
    """.trimIndent()
    private val testlibTestclassPreDec = """
        void TestLib_TestClass_op_decrement(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator--());
        }
    """.trimIndent()
    private val testlibTestclassPostDec = """
        void TestLib_TestClass_op_post_decrement(void* thiz, int dummy, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator--(dummy));
        }
    """.trimIndent()
    private val testlibTestclassEqCmp = """
        void TestLib_TestClass_op_eq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast == *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassNeq = """
        void TestLib_TestClass_op_neq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast != *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassLt = """
        void TestLib_TestClass_op_lt(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast < *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassGt = """
        void TestLib_TestClass_op_gt(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast > *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassLteq = """
        void TestLib_TestClass_op_lteq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast <= *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassGteq = """
        void TestLib_TestClass_op_gteq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast >= *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassBnot = """
        void* TestLib_TestClass_op_not(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return (void*)&(thiz_cast->operator!());
        }
    """.trimIndent()
    private val testlibTestclassBand = """
        void TestLib_TestClass_op_binary_and(void* thiz, void* c, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c_cast = reinterpret_cast<TestLib::TestClass*>(c);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast && *c_cast);
        }
    """.trimIndent()
    private val testlibTestclassBor = """
        void TestLib_TestClass_op_binary_or(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast || *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassNot = """
        void* TestLib_TestClass_op_inv(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return (void*)thiz_cast->operator~();
        }
    """.trimIndent()
    private val testlibTestclassAnd = """
        void TestLib_TestClass_op_and(void* thiz, void* c, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c_cast = reinterpret_cast<TestLib::TestClass*>(c);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast & *c_cast);
        }
    """.trimIndent()
    private val testlibTestclassOr = """
        void TestLib_TestClass_op_or(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast | *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassXor = """
        void TestLib_TestClass_op_xor(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast ^ *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassShl = """
        void TestLib_TestClass_op_shl(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast << *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassShr = """
        void TestLib_TestClass_op_shr(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = *thiz_cast >> *c2_cast);
        }
    """.trimIndent()
    private val testlibTestclassInd = """
        void TestLib_TestClass_op_ind(void* thiz, const char* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string c2_cast = std::string(c2);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            (*ret_value_cast = thiz_cast->operator[](c2_cast));
        }
    """.trimIndent()
    private val testlibMypairTestlibOtherclassA = """
        void* TestLib_MyPair_TestLib_OtherClass_P_a_get(void* thiz) {
            TestLib::MyPair<TestLib::OtherClass*>* thiz_cast = reinterpret_cast<TestLib::MyPair<TestLib::OtherClass*>*>(thiz);
            return (void*)thiz_cast->a;
        }

        void TestLib_MyPair_TestLib_OtherClass_P_a_set(void* thiz, void* value) {
            TestLib::MyPair<TestLib::OtherClass*>* thiz_cast = reinterpret_cast<TestLib::MyPair<TestLib::OtherClass*>*>(thiz);
            TestLib::OtherClass* value_cast = reinterpret_cast<TestLib::OtherClass*>(value);
            (thiz_cast->a = value_cast);
        }
    """.trimIndent()
    private val v8MaybeDoubleToChecked = """
        const double v8_Maybe_double_to_checked(void* thiz) {
            v8::Maybe<double>* thiz_cast = reinterpret_cast<v8::Maybe<double>*>(thiz);
            return thiz_cast->ToChecked();
        }
    """.trimIndent()

    @Test
    fun testV8Maybe_double() = runTest(
        cls = TestData.maybe.cls,
        target = TestData.maybe.ToChecked,
        expected = v8MaybeDoubleToChecked
    )

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

    @Test
    fun testMyPair_OtherClass_a() = runTest(
        cls = TestData.myPair.cls,
        target = TestData.myPair.cls.first.children[1] as WrappedField,
        expected = testlibMypairTestlibOtherclassA
    )

    @Test
    fun testHeaderEmpty() {
        val code = codeBuilder()
        val writer = cppWriter(code)
        writer.generate("desiredWrapper", emptyList(), emptyList())
        assertCode(empty, code.toString())
    }

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
        val writer = cppWriter(code)
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
        val writer = cppWriter(code)
        val (rcls, element) = resolveType(cls, target)
        val target = element as ResolvedMethod
        with(writer) {
            println("Generating for:\n$rcls\n\nTarget:\n$element\n\n")
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
        val writer = cppWriter(code)
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
        val writer = cppWriter(code)
        val ctx =
            resolveContext()
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

    private fun cppWriter(code: CppCodeBuilder) = CppWriter(file, code)
}
