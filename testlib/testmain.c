#include "desired_wrapper.h"
#include <stdio.h>

int main() {
    Complex c1 = testlib_complex_new();
    Complex c2 = testlib_complex_new();
    Complex result = testlib_complex_new();

    printf("Enter first complex number:\n");
    testlib_complex_input(c1);

    printf("Enter second complex number:\n");
    testlib_complex_input(c2);

    // In case of operator overloading of binary operators in C++ programming, 
    // the object on right hand side of operator is always assumed as argument by compiler.
    testlib_complex_minus(c1, c2, result);
    testlib_complex_output(result);

    testlib_complex_destroy(c1);
    testlib_complex_destroy(c2);
    testlib_complex_destroy(result);

    return 0;
}

