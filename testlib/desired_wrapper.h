#ifndef __DESIRED_WRAPPER__
#define __DESIRED_WRAPPER__

#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
    extern "C" {
#endif

typedef void* TestLib_TestClass;
typedef void* TestLib_OtherClass;
typedef void* std_vector_string;

std_vector_string std_vector_string_new();
void std_vector_string_dispose(std_vector_string thiz);
void std_vector_string_push_back(std_vector_string thiz, const char* str);

TestLib_OtherClass TestLib_OtherClass_new();
void TestLib_OtherClass_dispose(TestLib_OtherClass thiz);
const char* TestLib_OtherClass_getPrivateString(TestLib_OtherClass thiz);
void TestLib_OtherClass_setPrivateString(TestLib_OtherClass thiz, const char* value);
void TestLib_OtherClass_appendText(TestLib_OtherClass thiz, std_vector_string value);

bool TestLib_TestClass_b_get(TestLib_TestClass thiz);
void TestLib_TestClass_b_set(TestLib_TestClass thiz, bool value);
size_t TestLib_TestClass_st_get(TestLib_TestClass thiz);
void TestLib_TestClass_st_set(TestLib_TestClass thiz, size_t value);
uint16_t TestLib_TestClass_uit_get(TestLib_TestClass thiz);
void TestLib_TestClass_uit_set(TestLib_TestClass thiz, uint16_t value);

void TestLib_TestClass_array_get(TestLib_TestClass thiz, int* out);
void TestLib_TestClass_array_set(TestLib_TestClass thiz, int value[5]);
char* TestLib_TestClass_str_get(TestLib_TestClass thiz);
void TestLib_TestClass_str_set(TestLib_TestClass thiz, char* value);

signed char TestLib_TestClass_c_get(TestLib_TestClass thiz);
void TestLib_TestClass_c_set(TestLib_TestClass thiz, signed char value);
unsigned char TestLib_TestClass_uc_get(TestLib_TestClass thiz);
void TestLib_TestClass_uc_set(TestLib_TestClass thiz, unsigned char value);
signed short TestLib_TestClass_s_get(TestLib_TestClass thiz);
void TestLib_TestClass_s_set(TestLib_TestClass thiz, signed short value);
unsigned short TestLib_TestClass_us_get(TestLib_TestClass thiz);
void TestLib_TestClass_us_set(TestLib_TestClass thiz, unsigned short value);
signed int TestLib_TestClass_i_get(TestLib_TestClass thiz);
void TestLib_TestClass_i_set(TestLib_TestClass thiz, signed int value);
unsigned int TestLib_TestClass_ui_get(TestLib_TestClass thiz);
void TestLib_TestClass_ui_set(TestLib_TestClass thiz, unsigned int value);
signed long TestLib_TestClass_l_get(TestLib_TestClass thiz);
void TestLib_TestClass_l_set(TestLib_TestClass thiz, signed long value);
unsigned long TestLib_TestClass_ul_get(TestLib_TestClass thiz);
void TestLib_TestClass_ul_set(TestLib_TestClass thiz, unsigned long value);
signed long long TestLib_TestClass_ll_get(TestLib_TestClass thiz);
void TestLib_TestClass_ll_set(TestLib_TestClass thiz, signed long long value);
unsigned long long TestLib_TestClass_ull_get(TestLib_TestClass thiz);
void TestLib_TestClass_ull_set(TestLib_TestClass thiz, unsigned long long value);
float TestLib_TestClass_f_get(TestLib_TestClass thiz);
void TestLib_TestClass_f_set(TestLib_TestClass thiz, float value);
double TestLib_TestClass_d_get(TestLib_TestClass thiz);
void TestLib_TestClass_d_set(TestLib_TestClass thiz, double value);
double TestLib_TestClass_ld_get(TestLib_TestClass thiz);
void TestLib_TestClass_ld_set(TestLib_TestClass thiz, double value);

bool *TestLib_TestClass_pb_get(TestLib_TestClass thiz);
void TestLib_TestClass_pb_set(TestLib_TestClass thiz, bool* value);
signed char *TestLib_TestClass_pc_get(TestLib_TestClass thiz);
void TestLib_TestClass_pc_set(TestLib_TestClass thiz, signed char* value);
unsigned char *TestLib_TestClass_puc_get(TestLib_TestClass thiz);
void TestLib_TestClass_puc_set(TestLib_TestClass thiz, unsigned char* value);
signed short *TestLib_TestClass_ps_get(TestLib_TestClass thiz);
void TestLib_TestClass_ps_set(TestLib_TestClass thiz, signed short* value);
unsigned short *TestLib_TestClass_pus_get(TestLib_TestClass thiz);
void TestLib_TestClass_pus_set(TestLib_TestClass thiz, unsigned short* value);
signed int *TestLib_TestClass_pi_get(TestLib_TestClass thiz);
void TestLib_TestClass_pi_set(TestLib_TestClass thiz, signed int* value);
unsigned int *TestLib_TestClass_pui_get(TestLib_TestClass thiz);
void TestLib_TestClass_pui_set(TestLib_TestClass thiz, unsigned int* value);
signed long *TestLib_TestClass_pl_get(TestLib_TestClass thiz);
void TestLib_TestClass_pl_set(TestLib_TestClass thiz, signed long* value);
unsigned long *TestLib_TestClass_pul_get(TestLib_TestClass thiz);
void TestLib_TestClass_pul_set(TestLib_TestClass thiz, unsigned long* value);
signed long long *TestLib_TestClass_pll_get(TestLib_TestClass thiz);
void TestLib_TestClass_pll_set(TestLib_TestClass thiz, signed long long* value);
unsigned long long *TestLib_TestClass_pull_get(TestLib_TestClass thiz);
void TestLib_TestClass_pull_set(TestLib_TestClass thiz, unsigned long long* value);
float *TestLib_TestClass_pf_get(TestLib_TestClass thiz);
void TestLib_TestClass_pf_set(TestLib_TestClass thiz, float* value);
double *TestLib_TestClass_pd_get(TestLib_TestClass thiz);
void TestLib_TestClass_pd_set(TestLib_TestClass thiz, double* value);

TestLib_TestClass TestLib_TestClass_new();
TestLib_TestClass _TestLib_TestClass_new(const TestLib_TestClass other);
TestLib_TestClass __TestLib_TestClass_new(int a);
TestLib_TestClass ___TestLib_TestClass_new(int a, double b);
void TestLib_TestClass_dispose(TestLib_TestClass thiz);

long TestLib_TestClass_sum(TestLib_TestClass thiz);
long* TestLib_TestClass_longPointer(TestLib_TestClass thiz);
void TestLib_TestClass_setSome(TestLib_TestClass thiz, int a, long b, long long c);
void TestLib_TestClass_setPointers(TestLib_TestClass thiz, int* a, long* b, long long* c);
void TestLib_TestClass_setPrivateString(TestLib_TestClass thiz, const char* value);
void TestLib_TestClass_setPrivateFrom(TestLib_TestClass thiz, TestLib_OtherClass value);

void TestLib_TestClass_output(TestLib_TestClass thiz);

// Operator overloading
void TestLib_TestClass_op_minus(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_minus_unary(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_plus(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_plus_unary(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_times(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_divide(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_modulo(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_pre_inc(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_post_inc(TestLib_TestClass thiz, int dummy, TestLib_TestClass ret);
void TestLib_TestClass_op_pre_dec(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_post_dec(TestLib_TestClass thiz, int dummy, TestLib_TestClass ret);
void TestLib_TestClass_op_eq_cmp(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_neq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_lt(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_gt(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_lteq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_gteq(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_bnot(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_band(TestLib_TestClass thiz, TestLib_TestClass c, TestLib_TestClass ret2);
void TestLib_TestClass_op_bor(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_not(TestLib_TestClass thiz, TestLib_TestClass ret);
void TestLib_TestClass_op_and(TestLib_TestClass thiz, TestLib_TestClass c, TestLib_TestClass ret2);
void TestLib_TestClass_op_or(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_xor(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_shl(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_shr(TestLib_TestClass thiz, TestLib_TestClass c2, TestLib_TestClass ret);
void TestLib_TestClass_op_ind(TestLib_TestClass thiz, const char *c2, TestLib_TestClass ret);

#ifdef __cplusplus
}
#endif

#endif// __DESIRED_WRAPPER__

