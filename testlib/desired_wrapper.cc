#include "desired_wrapper.h"
#include "testlib.hh"


#ifdef __cplusplus
extern "C" {
#endif

Complex testlib_complex_new() {
    return new TestLib::Complex();
}
void testlib_complex_destroy(Complex thiz) {
    TestLib::Complex* c = reinterpret_cast<TestLib::Complex*>(thiz);
    delete c;
}

void testlib_complex_input(Complex thiz) {
    TestLib::Complex* c = reinterpret_cast<TestLib::Complex*>(thiz);
    c->input();
}

// Operator overloading
void testlib_complex_minus(Complex thiz, Complex c2, Complex ret) {
    TestLib::Complex* thiz_cast = reinterpret_cast<TestLib::Complex*>(thiz);
    TestLib::Complex* c2_cast = reinterpret_cast<TestLib::Complex*>(c2);
    TestLib::Complex* ret_cast = reinterpret_cast<TestLib::Complex*>(ret);
    *ret_cast = *thiz_cast - *c2_cast;
}

void testlib_complex_output(Complex thiz) {
    TestLib::Complex* c = reinterpret_cast<TestLib::Complex*>(thiz);
    c->output();
}

#ifdef __cplusplus
}
#endif

