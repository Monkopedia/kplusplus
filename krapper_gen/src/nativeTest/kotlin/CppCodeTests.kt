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

import com.monkopedia.krapper.generator.builders.CppCodeBuilder
import com.monkopedia.krapper.generator.codegen.File
import com.monkopedia.krapper.generator.codegen.NameHandler
import com.monkopedia.krapper.generator.codegen.WrappedCppWriter
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import kotlin.test.Test
import kotlin.test.fail

class CppCodeTests {
    private val file = File("/tmp/out.cpp")
    private val EMPTY = """
        #include "desired_wrapper.h"
        #include <vector>
        #include <string>
        #include <iterator>
        
        extern "C" {
        
        
        }
    """.trimIndent()
    private val STD_VECTOR_STRING_NEW = """
        void* std_vector_std_string_new() {
            return new std::vector<std::string>();
        }
    """.trimIndent()
    private val STD_VECTOR_STRING_DISPOSE = """
        void std_vector_std_string_dispose(void* thiz) {
            std::vector<std::string>* thiz_cast = reinterpret_cast<std::vector<std::string>*>(thiz);
            delete thiz_cast;
        }
    """.trimIndent()
    private val STD_VECTOR_STRING_PUSH_BACK = """
        void std_vector_std_string_push_back(void* thiz, const char* str) {
            std::vector<std::string>* thiz_cast = reinterpret_cast<std::vector<std::string>*>(thiz);
            std::string str_cast = std::string(str);
            thiz_cast->push_back(str_cast);
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_NEW = """
        void* TestLib_OtherClass_new() {
            return new TestLib::OtherClass();
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_DISPOSE = """
        void TestLib_OtherClass_dispose(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            delete thiz_cast;
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_GET_PRIVATE_STRING = """
        const char* TestLib_OtherClass_get_private_string(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::string ret_value = thiz_cast->getPrivateString();
            char* ret_value_cast = new char[ret_value.length() + 1];
            ret_value.copy(ret_value_cast, ret_value.length(), 0);
            return ret_value_cast;
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_SET_PRIVATE_STRING = """
        void TestLib_OtherClass_set_private_string(void* thiz, const char* value) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::string value_cast = std::string(value);
            thiz_cast->setPrivateString(value_cast);
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_APPEND_TEXT = """
        void TestLib_OtherClass_append_text(void* thiz, void* text) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            std::vector<std::string>* text_cast = reinterpret_cast<std::vector<std::string>*>(text);
            thiz_cast->appendText(*(text_cast));
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_COPIES = """
        void* TestLib_OtherClass_copies(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            return thiz_cast->copies();
        }
    """.trimIndent()
    private val TESTLIB_OTHERCLASS_INTS = """
        void* TestLib_OtherClass_ints(void* thiz) {
            TestLib::OtherClass* thiz_cast = reinterpret_cast<TestLib::OtherClass*>(thiz);
            return thiz_cast->ints();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SET_PRIVATE_FROM = """
        void TestLib_TestClass_set_private_from(void* thiz, void* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::OtherClass* value_cast = reinterpret_cast<TestLib::OtherClass*>(value);
            thiz_cast->setPrivateFrom(value_cast);
        }

    """.trimIndent()
    private val TESTLIB_TESTCLASS_NEW = """
        void* TestLib_TestClass_new() {
            return new TestLib::TestClass();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS__NEW = """
        void* TestLib_TestClass_new(void* other) {
            TestLib::TestClass* other_cast = reinterpret_cast<TestLib::TestClass*>(other);
            return new TestLib::TestClass(*(other_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS___NEW = """
        void* TestLib_TestClass_new(int a) {
            return new TestLib::TestClass(a);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS____NEW = """
        void* TestLib_TestClass_new(int a, double b) {
            return new TestLib::TestClass(a, b);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_DISPOSE = """
        void TestLib_TestClass_dispose(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            delete thiz_cast;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_B = """
        bool TestLib_TestClass_b_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->b;
        }
        
        void TestLib_TestClass_b_set(void* thiz, bool ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->b = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_ST = """
        size_t TestLib_TestClass_st_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->st;
        }
        
        void TestLib_TestClass_st_set(void* thiz, size_t ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->st = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_UIT = """
        uint16_t TestLib_TestClass_uit_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->uit;
        }
        
        void TestLib_TestClass_uit_set(void* thiz, uint16_t ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->uit = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_STR = """
        const char* TestLib_TestClass_str_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string ret_value = thiz_cast->str;
            char* ret_value_cast = new char[ret_value.length() + 1];
            ret_value.copy(ret_value_cast, ret_value.length(), 0);
            return ret_value_cast;
        }
        
        void TestLib_TestClass_str_set(void* thiz, const char* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->str = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_C = """
        signed char TestLib_TestClass_c_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->c;
        }
        
        void TestLib_TestClass_c_set(void* thiz, signed char ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->c = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_UC = """
        unsigned char TestLib_TestClass_uc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->uc;
        }
        
        void TestLib_TestClass_uc_set(void* thiz, unsigned char ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->uc = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_S = """
        signed short TestLib_TestClass_s_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->s;
        }
        
        void TestLib_TestClass_s_set(void* thiz, signed short ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->s = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_US = """
        unsigned short TestLib_TestClass_us_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->us;
        }
        
        void TestLib_TestClass_us_set(void* thiz, unsigned short ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->us = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_I = """
        signed int TestLib_TestClass_i_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->i;
        }
        
        void TestLib_TestClass_i_set(void* thiz, signed int ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->i = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_UI = """
        unsigned int TestLib_TestClass_ui_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ui;
        }
        
        void TestLib_TestClass_ui_set(void* thiz, unsigned int ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ui = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_L = """
        signed long TestLib_TestClass_l_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->l;
        }
        
        void TestLib_TestClass_l_set(void* thiz, signed long ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->l = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_UL = """
        unsigned long TestLib_TestClass_ul_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ul;
        }
        
        void TestLib_TestClass_ul_set(void* thiz, unsigned long ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ul = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_LL = """
        signed long long TestLib_TestClass_ll_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ll;
        }
        
        void TestLib_TestClass_ll_set(void* thiz, signed long long ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ll = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_ULL = """
        unsigned long long TestLib_TestClass_ull_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ull;
        }
        
        void TestLib_TestClass_ull_set(void* thiz, unsigned long long ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ull = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_F = """
        float TestLib_TestClass_f_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->f;
        }
        
        void TestLib_TestClass_f_set(void* thiz, float ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->f = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_D = """
        double TestLib_TestClass_d_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->d;
        }
        
        void TestLib_TestClass_d_set(void* thiz, double ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->d = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_LD = """
        double TestLib_TestClass_ld_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ld;
        }
        
        void TestLib_TestClass_ld_set(void* thiz, double ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ld = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PB = """
         bool* TestLib_TestClass_pb_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pb;
        }
        
        void TestLib_TestClass_pb_set(void* thiz, bool* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pb = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PC = """
        signed char* TestLib_TestClass_pc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pc;
        }
        
        void TestLib_TestClass_pc_set(void* thiz, signed char* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pc = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PUC = """
        unsigned char* TestLib_TestClass_puc_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->puc;
        }
        
        void TestLib_TestClass_puc_set(void* thiz, unsigned char* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->puc = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PS = """
         signed short* TestLib_TestClass_ps_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->ps;
        }
        
        void TestLib_TestClass_ps_set(void* thiz, signed short* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->ps = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PUS = """
         unsigned short* TestLib_TestClass_pus_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pus;
        }
        
        void TestLib_TestClass_pus_set(void* thiz, unsigned short* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pus = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PI = """
        signed int* TestLib_TestClass_pi_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pi;
        }
        
        void TestLib_TestClass_pi_set(void* thiz, signed int* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pi = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PUI = """
        unsigned int* TestLib_TestClass_pui_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pui;
        }
        
        void TestLib_TestClass_pui_set(void* thiz, unsigned int* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pui = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PL = """
        signed long* TestLib_TestClass_pl_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pl;
        }
        
        void TestLib_TestClass_pl_set(void* thiz, signed long* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pl = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PUL = """
        unsigned long* TestLib_TestClass_pul_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pul;
        }
        
        void TestLib_TestClass_pul_set(void* thiz, unsigned long* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pul = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PLL = """
        signed long long* TestLib_TestClass_pll_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pll;
        }
        
        void TestLib_TestClass_pll_set(void* thiz, signed long long* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pll = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PULL = """
        unsigned long long* TestLib_TestClass_pull_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pull;
        }
        
        void TestLib_TestClass_pull_set(void* thiz, unsigned long long* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pull = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PF = """
        float* TestLib_TestClass_pf_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pf;
        }
        
        void TestLib_TestClass_pf_set(void* thiz, float* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pf = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PD = """
        double* TestLib_TestClass_pd_get(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->pd;
        }
        
        void TestLib_TestClass_pd_set(void* thiz, double* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->pd = ret_value;
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SUM = """
         long TestLib_TestClass_sum(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->sum();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_LONG_POINTER = """
        long* TestLib_TestClass_long_pointer(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->longPointer();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SET_SOME = """
        void TestLib_TestClass_set_some(void* thiz, int a, long b, long long c) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->setSome(a, b, c);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SET_POINTERS = """
        void TestLib_TestClass_set_pointers(void* thiz, int* a, long* b, long long* c) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->setPointers(a, b, c);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SET_PRIVATE_STRING = """
        void TestLib_TestClass_set_private_string(void* thiz, const char* value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            std::string value_cast = std::string(value);
            thiz_cast->setPrivateString(value_cast);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_OUTPUT = """
        void TestLib_TestClass_output(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            thiz_cast->output();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_MINUS = """
        void TestLib_TestClass_op_minus(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator-(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_MINUS_UNARY = """
            void TestLib_TestClass_op_unary_minus(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator-();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PLUS = """
          void TestLib_TestClass_op_plus(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator+(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PLUS_UNARY = """
            void TestLib_TestClass_op_unary_plus(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator+();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_TIMES = """
        void TestLib_TestClass_op_times(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator*(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_DIVIDE = """
        void TestLib_TestClass_op_divide(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator/(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_MODULO = """
        void TestLib_TestClass_op_mod(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator%(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PRE_INC = """
        void TestLib_TestClass_op_increment(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator++();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_POST_INC = """
         void TestLib_TestClass_op_post_increment(void* thiz, int dummy, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator++(dummy);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_PRE_DEC = """
        void TestLib_TestClass_op_decrement(void* thiz, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator--();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_POST_DEC = """
        void TestLib_TestClass_op_post_decrement(void* thiz, int dummy, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            *(ret_value_cast) = thiz_cast->operator--(dummy);
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_EQ_CMP = """
        void TestLib_TestClass_op_eq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator==(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_NEQ = """
        void TestLib_TestClass_op_neq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator!=(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_LT = """
        void TestLib_TestClass_op_lt(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator<(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_GT = """
        void TestLib_TestClass_op_gt(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator>(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_LTEQ = """
        void TestLib_TestClass_op_lteq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator<=(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_GTEQ = """
        void TestLib_TestClass_op_gteq(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator>=(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_BNOT = """
        void* TestLib_TestClass_op_not(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->operator!();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_BAND = """
        void TestLib_TestClass_op_binary_and(void* thiz, void* c, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c_cast = reinterpret_cast<TestLib::TestClass*>(c);
            *(ret_value_cast) = thiz_cast->operator&&(*(c_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_BOR = """
        void TestLib_TestClass_op_binary_or(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator||(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_NOT = """
        void* TestLib_TestClass_op_inv(void* thiz) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            return thiz_cast->operator~();
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_AND = """
        void TestLib_TestClass_op_and(void* thiz, void* c, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c_cast = reinterpret_cast<TestLib::TestClass*>(c);
            *(ret_value_cast) = thiz_cast->operator&(*(c_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_OR = """
        void TestLib_TestClass_op_or(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator|(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_XOR = """
        void TestLib_TestClass_op_xor(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator^(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SHL = """
        void TestLib_TestClass_op_shl(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator<<(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_SHR = """
        void TestLib_TestClass_op_shr(void* thiz, void* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            TestLib::TestClass* c2_cast = reinterpret_cast<TestLib::TestClass*>(c2);
            *(ret_value_cast) = thiz_cast->operator>>(*(c2_cast));
        }
    """.trimIndent()
    private val TESTLIB_TESTCLASS_IND = """
        void TestLib_TestClass_op_ind(void* thiz, const char* c2, void* ret_value) {
            TestLib::TestClass* thiz_cast = reinterpret_cast<TestLib::TestClass*>(thiz);
            TestLib::TestClass* ret_value_cast = reinterpret_cast<TestLib::TestClass*>(ret_value);
            std::string c2_cast = std::string(c2);
            *(ret_value_cast) = thiz_cast->operator[](c2_cast);
        }
    """.trimIndent()

    @Test
    fun testVector_new() = runTest(
        cls = TestData.Vector.cls,
        target = TestData.Vector.constructor,
        expected = STD_VECTOR_STRING_NEW,
    )

    @Test
    fun testVector_dispose() = runTest(
        cls = TestData.Vector.cls,
        target = TestData.Vector.destructor,
        expected = STD_VECTOR_STRING_DISPOSE,
    )

    @Test
    fun testVector_pushBack() = runTest(
        cls = TestData.Vector.cls,
        target = TestData.Vector.cls.children[3] as WrappedMethod,
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

    private fun buildCode(
        cls: WrappedClass,
        target: WrappedField
    ): CppCodeBuilder {
        val code = CppCodeBuilder()
        val writer = WrappedCppWriter(NameHandler(), file, code)
        with(writer) {
            code.onGenerate(cls, target)
        }
        return code
    }

    private fun buildCode(
        cls: WrappedClass,
        target: WrappedMethod
    ): CppCodeBuilder {
        val code = CppCodeBuilder()
        val writer = WrappedCppWriter(NameHandler(), file, code)
        with(writer) {
            code.onGenerate(cls, target)
        }
        return code
    }

    @Test
    fun testHeaderEmpty() {
        val code = CppCodeBuilder()
        val writer = WrappedCppWriter(NameHandler(), file, code)
        writer.generate("desiredWrapper", emptyList(), emptyList())
        assertCode(EMPTY, code.toString())
    }
}
