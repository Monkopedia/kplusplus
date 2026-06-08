#include "feature_tests.h"

namespace {
int call_count = 0;
}

extern "C" bool pr_bool_negate(bool b) {
    ++call_count;
    return !b;
}

extern "C" int pr_bool_call_count(void) {
    return call_count;
}

extern "C" void pr_bool_reset(void) {
    call_count = 0;
}
