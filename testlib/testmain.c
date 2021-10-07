#include "desired_wrapper.h"
#include <stdio.h>
#include <stdlib.h>

int main() {
    /*void* c1 = TestLib_Complex_new();*/
    /*void* c2 = TestLib_Complex_new();*/
    /*void* result = TestLib_Complex_new();*/

    /*printf("Enter first complex number:\n");*/
    /*TestLib_Complex_input(c1);*/

    /*printf("Enter second complex number:\n");*/
    /*TestLib_Complex_input(c2);*/

    /*// In case of operator overloading of binary operators in C++ programming, */
    /*// the object on right hand side of operator is always assumed as argument by compiler.*/
    /*TestLib_Complex_operator_minus(c1, c2, result);*/
    /*TestLib_Complex_output(result);*/

    /*free(c1);*/
    /*free(c2);*/
    /*free(result);*/

    TestLib_TestClass c1 = TestLib_TestClass_new(), c2 = TestLib_TestClass_new(), result = TestLib_TestClass_new();

    TestLib_TestClass_b_set(c1, 1);
    printf("Set boolean\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_st_set(c1, 1);
    printf("Set st\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_uit_set(c1, 2);
    printf("Set uit\n");
    TestLib_TestClass_output(c1);

    int array[] = {3,4,5,6,7};
    TestLib_TestClass_array_set(c1, array);
    printf("Set array\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_str_set(c1, "My test string");
    printf("Set str\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_c_set(c1, 8);
    printf("Set c\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_uc_set(c1, 9);
    printf("Set uc\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_s_set(c1, 10);
    printf("Set s\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_us_set(c1, 11);
    printf("Set us\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_i_set(c1, 12);
    printf("Set 12\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ui_set(c1, 13);
    printf("Set ui\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_l_set(c1, 14);
    printf("Set l\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ul_set(c1, 15);
    printf("Set ul\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ll_set(c1, 16);
    printf("Set ll\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ull_set(c1, 17);
    printf("Set ull\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_f_set(c1, 18);
    printf("Set f\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_d_set(c1, 19);
    printf("Set d\n");
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ld_set(c1, 20);
    printf("Set ld\n");
    TestLib_TestClass_output(c1);

    bool pb = false;
    signed char pc = 21;
    unsigned char puc = 22;
    signed short ps = 23;
    unsigned short pus = 24;
    signed int pi = 25;
    unsigned int pui = 26;
    signed long pl = 27;
    unsigned long pul = 28;
    signed long long pll = 29;
    unsigned long long pull = 30;
    float pf = 31;
    double pd = 32;

    TestLib_TestClass_pb_set(c1, &pb);
    printf("Set pb\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pc_set(c1, &pc);
    printf("Set pc\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pc_set(c1, &pc);
    printf("Set pc\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_puc_set(c1, &puc);
    printf("Set puc\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_ps_set(c1, &ps);
    printf("Set ps\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pus_set(c1, &pus);
    printf("Set pus\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pi_set(c1, &pi);
    printf("Set pi\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pl_set(c1, &pl);
    printf("Set pl\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pul_set(c1, &pul);
    printf("Set pul\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pll_set(c1, &pll);
    printf("Set pll\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pull_set(c1, &pull);
    printf("Set pull\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pf_set(c1, &pf);
    printf("Set pf\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_pd_set(c1, &pd);
    printf("Set pd\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_TestClass_setPrivateString(c1, "Unary private string");
    TestLib_TestClass_setPrivateString(c2, "My private string");
    TestLib_TestClass op1 = TestLib_TestClass_new();
    TestLib_TestClass_op_minus(c1, c2, op1);
    printf("op1 minus\n"); fflush(stdout);
    TestLib_TestClass_output(op1);

    TestLib_TestClass op2 = TestLib_TestClass_new();
    TestLib_TestClass_op_minus_unary(c1, op2);
    printf("op2 unary minus\n"); fflush(stdout);
    TestLib_TestClass_output(op2);

    TestLib_TestClass op3 = TestLib_TestClass_new();
    TestLib_TestClass_op_plus(c1, c2, op3);
    printf("op3 plus\n"); fflush(stdout);
    TestLib_TestClass_output(op3);

    TestLib_TestClass op4 = TestLib_TestClass_new();
    TestLib_TestClass_op_plus_unary(c1, op4);
    printf("op4 unary plus\n"); fflush(stdout);
    TestLib_TestClass_output(op4);

    TestLib_TestClass op5 = TestLib_TestClass_new();
    TestLib_TestClass_op_times(c1, c2, op5);
    printf("op5 times\n"); fflush(stdout);
    TestLib_TestClass_output(op5);

    TestLib_TestClass op6 = TestLib_TestClass_new();
    TestLib_TestClass_op_divide(c1, c2, op6);
    printf("op6 divide\n"); fflush(stdout);
    TestLib_TestClass_output(op6);

    TestLib_TestClass op7 = TestLib_TestClass_new();
    TestLib_TestClass_op_modulo(c1, c2, op7);
    printf("op7 mod\n"); fflush(stdout);
    TestLib_TestClass_output(op7);

    TestLib_TestClass op8 = TestLib_TestClass_new();
    TestLib_TestClass_op_pre_inc(c1, op8);
    printf("op8 preinc\n"); fflush(stdout);
    TestLib_TestClass_output(op8);

    TestLib_TestClass op9 = TestLib_TestClass_new();
    TestLib_TestClass_op_post_inc(c1, 0, op9);
    printf("op9 postinc\n"); fflush(stdout);
    TestLib_TestClass_output(op9);

    TestLib_TestClass op10 = TestLib_TestClass_new();
    TestLib_TestClass_op_pre_dec(c1, op10);
    printf("op10 predec\n"); fflush(stdout);
    TestLib_TestClass_output(op10);

    TestLib_TestClass op11 = TestLib_TestClass_new();
    TestLib_TestClass_op_post_dec(c1, 0, op11);
    printf("op11 postdec\n"); fflush(stdout);
    TestLib_TestClass_output(op11);

    TestLib_TestClass op12 = TestLib_TestClass_new();
    TestLib_TestClass_op_eq_cmp(c1, c2, op12);
    printf("op12 eq comp\n"); fflush(stdout);
    TestLib_TestClass_output(op12);

    TestLib_TestClass op13 = TestLib_TestClass_new();
    TestLib_TestClass_op_neq(c1, c2, op13);
    printf("op13 neq\n"); fflush(stdout);
    TestLib_TestClass_output(op13);

    TestLib_TestClass op14 = TestLib_TestClass_new();
    TestLib_TestClass_op_lt(c1, c2, op14);
    printf("op14 less than\n"); fflush(stdout);
    TestLib_TestClass_output(op14);

    TestLib_TestClass op15 = TestLib_TestClass_new();
    TestLib_TestClass_op_gt(c1, c2, op15);
    printf("op15 greater than\n"); fflush(stdout);
    TestLib_TestClass_output(op15);

    TestLib_TestClass op16 = TestLib_TestClass_new();
    TestLib_TestClass_op_lteq(c1, c2, op16);
    printf("op16 lt eq\n"); fflush(stdout);
    TestLib_TestClass_output(op16);

    TestLib_TestClass op17 = TestLib_TestClass_new();
    TestLib_TestClass_op_gteq(c1, c2, op17);
    printf("op17 gt eq\n"); fflush(stdout);
    TestLib_TestClass_output(op17);

    TestLib_TestClass op18 = TestLib_TestClass_new();
    TestLib_TestClass_op_bnot(c1, op18);
    printf("op18 boolean not\n"); fflush(stdout);
    TestLib_TestClass_output(op18);

    TestLib_TestClass op19 = TestLib_TestClass_new();
    TestLib_TestClass_op_band(c1, c2, op19);
    printf("op19 boolean and\n"); fflush(stdout);
    TestLib_TestClass_output(op19);

    TestLib_TestClass op20 = TestLib_TestClass_new();
    TestLib_TestClass_op_bor(c1, c2, op20);
    printf("op20 boolean or\n"); fflush(stdout);
    TestLib_TestClass_output(op20);

    TestLib_TestClass op21 = TestLib_TestClass_new();
    TestLib_TestClass_op_not(c1, op21);
    printf("op21 bit not\n"); fflush(stdout);
    TestLib_TestClass_output(op21);

    TestLib_TestClass op22 = TestLib_TestClass_new();
    TestLib_TestClass_op_and(c1, c2, op22);
    printf("op22 bit and\n"); fflush(stdout);
    TestLib_TestClass_output(op22);

    TestLib_TestClass op23 = TestLib_TestClass_new();
    TestLib_TestClass_op_or(c1, c2, op23);
    printf("op23 bit or\n"); fflush(stdout);
    TestLib_TestClass_output(op23);

    TestLib_TestClass op24 = TestLib_TestClass_new();
    TestLib_TestClass_op_xor(c1, c2, op24);
    printf("op24 xor\n"); fflush(stdout);
    TestLib_TestClass_output(op24);

    TestLib_TestClass op25 = TestLib_TestClass_new();
    TestLib_TestClass_op_shl(c1, c2, op25);
    printf("op25 shl\n"); fflush(stdout);
    TestLib_TestClass_output(op25);

    TestLib_TestClass op26 = TestLib_TestClass_new();
    TestLib_TestClass_op_shr(c1, c2, op26);
    printf("op26 shr\n"); fflush(stdout);
    TestLib_TestClass_output(op26);

    const char* str = "something";
    TestLib_TestClass op27 = TestLib_TestClass_new();
    TestLib_TestClass_op_ind(c1, str, op27);
    printf("op27 shr\n"); fflush(stdout);
    TestLib_TestClass_output(op27);

    printf("Sum: %ld\n" , TestLib_TestClass_sum(c1)); fflush(stdout);
    printf("Long pointer: %ld\n", *TestLib_TestClass_longPointer(c1)); fflush(stdout);

    TestLib_TestClass_setSome(c1, 1, 2, 3);
    printf("setSome\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    int a = 11;
    long b = 12;
    long long c = 13;
    TestLib_TestClass_setPointers(c1, &a, &b, &c);
    printf("setPointers\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    TestLib_OtherClass other = TestLib_OtherClass_new();
    std_vector_string vect = std_vector_string_new();
    std_vector_string_push_back(vect, "First!");
    std_vector_string_push_back(vect, "second one");
    std_vector_string_push_back(vect, "THIRD");

    TestLib_OtherClass_setPrivateString(other, "Prefix: ");
    TestLib_OtherClass_appendText(other, vect);
    printf("Other text: %s\n", TestLib_OtherClass_getPrivateString(other)); fflush(stdout);

    TestLib_TestClass_setPrivateFrom(c1, other);
    printf("Set from other\n"); fflush(stdout);
    TestLib_TestClass_output(c1);

    return 0;
}

