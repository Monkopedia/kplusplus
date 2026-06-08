#include "feature_tests.h"

namespace {
intptr_t  last_i = 0;
uintptr_t last_u = 0;
}

extern "C" intptr_t  pr_intptr_echo(intptr_t x) { last_i = x; return x; }
extern "C" intptr_t  pr_intptr_last_arg(void) { return last_i; }
extern "C" uintptr_t pr_uintptr_echo(uintptr_t x) { last_u = x; return x; }
extern "C" uintptr_t pr_uintptr_last_arg(void) { return last_u; }
extern "C" void pr_intptr_reset(void) { last_i = 0; last_u = 0; }
