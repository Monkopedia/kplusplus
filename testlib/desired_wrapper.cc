#include "desired_wrapper.h"
#include "testlib.hh"

#include <string>

extern "C" {

using namespace std;
using namespace TestLib;

std_vector_string std_vector_string_new() {
    return new vector<string>();
}
void std_vector_string_dispose(std_vector_string thiz) {
    vector<string>* thiz_cast = reinterpret_cast<vector<string>*>(thiz);
    delete thiz_cast;
}
void std_vector_string_push_back(std_vector_string thiz, const char* str) {
    vector<string>* thiz_cast = reinterpret_cast<vector<string>*>(thiz);
    thiz_cast->push_back(str);
}

TestLib_OtherClass TestLib_OtherClass_new() {
    return new OtherClass();
}

void TestLib_OtherClass_dispose(TestLib_OtherClass thiz) {
    OtherClass* thiz_cast = reinterpret_cast<OtherClass*>(thiz);
    delete thiz_cast;
}

const char* TestLib_OtherClass_getPrivateString(TestLib_OtherClass thiz) {
    OtherClass* thiz_cast = reinterpret_cast<OtherClass*>(thiz);
    string str = thiz_cast->getPrivateString();
    //char* buffer = new char[str.length() + 1];
    //str.copy(buffer, str.length(), 0);
    return &str[0];
}

void TestLib_OtherClass_setPrivateString(TestLib_OtherClass thiz, const char* value) {
    OtherClass* thiz_cast = reinterpret_cast<OtherClass*>(thiz);
    thiz_cast->setPrivateString(value);
}

void TestLib_OtherClass_appendText(TestLib_OtherClass thiz, std_vector_string value) {
    OtherClass* thiz_cast = reinterpret_cast<OtherClass*>(thiz);
    vector<string>* value_cast = reinterpret_cast<vector<string>*>(value);
    thiz_cast->appendText(*value_cast);
}

void TestLib_TestClass_setPrivateFrom(TestLib_TestClass thiz, TestLib_OtherClass value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    OtherClass* value_cast = reinterpret_cast<OtherClass*>(value);
    thiz_cast->setPrivateFrom(value_cast);
}

TestLib_TestClass TestLib_TestClass_new() {
    return new TestClass();
}

TestLib_TestClass _TestLib_TestClass_new(const TestLib_TestClass other) {
    TestClass* other_cast = reinterpret_cast<TestClass*>(other);
    return new TestClass(*other_cast);
}

TestLib_TestClass __TestLib_TestClass_new(int a) {
    return new TestClass(a);
}

TestLib_TestClass ___TestLib_TestClass_new(int a, double b) {
    return new TestClass(a, b);
}

void TestLib_TestClass_dispose(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    delete thiz_cast;
}


bool TestLib_TestClass_b_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->b;
}

void TestLib_TestClass_b_set(TestLib_TestClass thiz, bool value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->b = value;
}

size_t TestLib_TestClass_st_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->st;
}

void TestLib_TestClass_st_set(TestLib_TestClass thiz, size_t value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->st = value;
}

uint16_t TestLib_TestClass_uit_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->uit;
}

void TestLib_TestClass_uit_set(TestLib_TestClass thiz, uint16_t value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->uit = value;
}


void TestLib_TestClass_array_get(TestLib_TestClass thiz, int* out) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    out[0] = thiz_cast->array[0];
    out[1] = thiz_cast->array[1];
    out[2] = thiz_cast->array[2];
    out[3] = thiz_cast->array[3];
    out[4] = thiz_cast->array[4];
}

void TestLib_TestClass_array_set(TestLib_TestClass thiz, int value[5]) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->array[0] = value[0];
    thiz_cast->array[1] = value[1];
    thiz_cast->array[2] = value[2];
    thiz_cast->array[3] = value[3];
    thiz_cast->array[4] = value[4];
}

char* TestLib_TestClass_str_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->str;
}

void TestLib_TestClass_str_set(TestLib_TestClass thiz, char* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->str = value;
}


signed char TestLib_TestClass_c_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->c;
}

void TestLib_TestClass_c_set(TestLib_TestClass thiz, signed char value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->c = value;
}

unsigned char TestLib_TestClass_uc_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->uc;
}

void TestLib_TestClass_uc_set(TestLib_TestClass thiz, unsigned char value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->uc = value;
}

signed short TestLib_TestClass_s_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->s;
}

void TestLib_TestClass_s_set(TestLib_TestClass thiz, signed short value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->s = value;
}

unsigned short TestLib_TestClass_us_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->us;
}

void TestLib_TestClass_us_set(TestLib_TestClass thiz, unsigned short value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->us = value;
}

signed int TestLib_TestClass_i_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->i;
}

void TestLib_TestClass_i_set(TestLib_TestClass thiz, signed int value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->i = value;
}

unsigned int TestLib_TestClass_ui_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ui;
}

void TestLib_TestClass_ui_set(TestLib_TestClass thiz, unsigned int value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ui = value;
}

signed long TestLib_TestClass_l_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->l;
}

void TestLib_TestClass_l_set(TestLib_TestClass thiz, signed long value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->l = value;
}

unsigned long TestLib_TestClass_ul_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ul;
}

void TestLib_TestClass_ul_set(TestLib_TestClass thiz, unsigned long value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ul = value;
}

signed long long TestLib_TestClass_ll_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ll;
}

void TestLib_TestClass_ll_set(TestLib_TestClass thiz, signed long long value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ll = value;
}

unsigned long long TestLib_TestClass_ull_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ull;
}

void TestLib_TestClass_ull_set(TestLib_TestClass thiz, unsigned long long value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ull = value;
}

float TestLib_TestClass_f_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->f;
}

void TestLib_TestClass_f_set(TestLib_TestClass thiz, float value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->f = value;
}

double TestLib_TestClass_d_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->d;
}

void TestLib_TestClass_d_set(TestLib_TestClass thiz, double value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->d = value;
}

double TestLib_TestClass_ld_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ld;
}

void TestLib_TestClass_ld_set(TestLib_TestClass thiz, double value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ld = value;
}


bool *TestLib_TestClass_pb_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pb;
}

void TestLib_TestClass_pb_set(TestLib_TestClass thiz, bool* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pb = value;
}

signed char *TestLib_TestClass_pc_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pc;
}

void TestLib_TestClass_pc_set(TestLib_TestClass thiz, signed char* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pc = value;
}

unsigned char *TestLib_TestClass_puc_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->puc;
}

void TestLib_TestClass_puc_set(TestLib_TestClass thiz, unsigned char* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->puc = value;
}

signed short *TestLib_TestClass_ps_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->ps;
}

void TestLib_TestClass_ps_set(TestLib_TestClass thiz, signed short* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->ps = value;
}

unsigned short *TestLib_TestClass_pus_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pus;
}

void TestLib_TestClass_pus_set(TestLib_TestClass thiz, unsigned short* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pus = value;
}

signed int *TestLib_TestClass_pi_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pi;
}

void TestLib_TestClass_pi_set(TestLib_TestClass thiz, signed int* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pi = value;
}

unsigned int *TestLib_TestClass_pui_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pui;
}

void TestLib_TestClass_pui_set(TestLib_TestClass thiz, unsigned int* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pui = value;
}

signed long *TestLib_TestClass_pl_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pl;
}

void TestLib_TestClass_pl_set(TestLib_TestClass thiz, signed long* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pl = value;
}

unsigned long *TestLib_TestClass_pul_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pul;
}

void TestLib_TestClass_pul_set(TestLib_TestClass thiz, unsigned long* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pul = value;
}

signed long long *TestLib_TestClass_pll_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pll;
}

void TestLib_TestClass_pll_set(TestLib_TestClass thiz, signed long long* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pll = value;
}

unsigned long long *TestLib_TestClass_pull_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pull;
}

void TestLib_TestClass_pull_set(TestLib_TestClass thiz, unsigned long long* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pull = value;
}

float *TestLib_TestClass_pf_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pf;
}

void TestLib_TestClass_pf_set(TestLib_TestClass thiz, float* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pf = value;
}

double *TestLib_TestClass_pd_get(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->pd;
}

void TestLib_TestClass_pd_set(TestLib_TestClass thiz, double* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->pd = value;
}


long TestLib_TestClass_sum(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->sum();
}

long* TestLib_TestClass_longPointer(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    return thiz_cast->longPointer();
}

void TestLib_TestClass_setSome(TestLib_TestClass thiz, int a, long b, long long c) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->setSome(a, b, c);
}

void TestLib_TestClass_setPointers(TestLib_TestClass thiz, int* a, long* b, long long* c) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->setPointers(a, b, c);
}

void TestLib_TestClass_setPrivateString(TestLib_TestClass thiz, const char* value) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    string str(value);
    thiz_cast->setPrivateString(str);
}


void TestLib_TestClass_output(TestLib_TestClass thiz) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    thiz_cast->output();
}


// Operator overloading
void TestLib_TestClass_op_minus(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator-(*c2_cast);
}

void TestLib_TestClass_op_minus_unary(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator-();
}

void TestLib_TestClass_op_plus(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator+(*c2_cast);
}

void TestLib_TestClass_op_plus_unary(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator+();
}

void TestLib_TestClass_op_times(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator*(*c2_cast);
}

void TestLib_TestClass_op_divide(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator/(*c2_cast);
}

void TestLib_TestClass_op_modulo(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator%(*c2_cast);
}

void TestLib_TestClass_op_pre_inc(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator++();
}

void TestLib_TestClass_op_post_inc(TestLib_TestClass thiz, int dummy, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator++(dummy);
}

void TestLib_TestClass_op_pre_dec(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator--();
}

void TestLib_TestClass_op_post_dec(TestLib_TestClass thiz, int dummy, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator--(dummy);
}

void TestLib_TestClass_op_eq_cmp(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator==(*c2_cast);
}

void TestLib_TestClass_op_neq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator!=(*c2_cast);
}

void TestLib_TestClass_op_lt(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator<(*c2_cast);
}

void TestLib_TestClass_op_gt(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator>(*c2_cast);
}

void TestLib_TestClass_op_lteq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator<=(*c2_cast);
}

void TestLib_TestClass_op_gteq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator>=(*c2_cast);
}

void TestLib_TestClass_op_bnot(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator!();
}

void TestLib_TestClass_op_band(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator&&(*c2_cast);
}

void TestLib_TestClass_op_bor(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator||(*c2_cast);
}

void TestLib_TestClass_op_not(TestLib_TestClass thiz, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator~();
}

void TestLib_TestClass_op_and(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator&(*c2_cast);
}

void TestLib_TestClass_op_or(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator|(*c2_cast);
}

void TestLib_TestClass_op_xor(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator^(*c2_cast);
}

void TestLib_TestClass_op_shl(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator<<(*c2_cast);
}

void TestLib_TestClass_op_shr(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    TestClass* c2_cast = reinterpret_cast<TestClass*>(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator>>(*c2_cast);
}

void TestLib_TestClass_op_ind(TestLib_TestClass thiz, const char *c2, TestLib_TestClass ret) {
    TestClass* thiz_cast = reinterpret_cast<TestClass*>(thiz);
    string c2_cast(c2);
    TestClass* ret_cast = reinterpret_cast<TestClass*>(ret);
    *ret_cast = thiz_cast->operator[](c2_cast);
}

}

