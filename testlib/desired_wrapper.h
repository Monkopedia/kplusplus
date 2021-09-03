#ifndef __DESIRED_WRAPPER_H__
#define __DESIRED_WRAPPER_H__

#ifdef __cplusplus
extern "C" {
#endif

typedef void* Complex;
Complex testlib_complex_new();
void testlib_complex_destroy(Complex thiz);
void testlib_complex_input(Complex thiz);

// Operator overloading
void testlib_complex_minus(Complex thiz, Complex c2, Complex ret);

void testlib_complex_output(Complex thiz);

#ifdef __cplusplus
}
#endif
#endif// __DESIRED_WRAPPER_H__
