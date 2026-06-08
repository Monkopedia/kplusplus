#include "feature_tests.h"

namespace {
int last_arg = 0;
}

extern "C" int pr_int_rt_add_one(int x) {
    last_arg = x;
    // INT_MAX case is undefined behavior under signed overflow rules; tests
    // never read the return value when last_arg == INT_MAX, only the inspector.
    return x + 1;
}

extern "C" int pr_int_rt_last_arg(void) {
    return last_arg;
}

extern "C" void pr_int_rt_reset(void) {
    last_arg = 0;
}
