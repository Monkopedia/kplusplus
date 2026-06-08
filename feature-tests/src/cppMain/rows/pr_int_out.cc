#include "feature_tests.h"

namespace {
int produced_count = 0;
int null_flagged = 0;
}

extern "C" void pr_int_out_produce(int* out) {
    if (out == nullptr) {
        ++null_flagged;
        return;
    }
    *out = 42;
    ++produced_count;
}

extern "C" int pr_int_out_produced_count(void) {
    return produced_count;
}

extern "C" int pr_int_out_null_flagged(void) {
    return null_flagged;
}

extern "C" void pr_int_out_reset(void) {
    produced_count = 0;
    null_flagged = 0;
}
