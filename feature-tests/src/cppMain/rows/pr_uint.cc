#include "feature_tests.h"

namespace { unsigned int last_arg = 0; }

extern "C" unsigned int pr_uint_echo(unsigned int x) { last_arg = x; return x; }
extern "C" unsigned int pr_uint_last_arg(void) { return last_arg; }
extern "C" void pr_uint_reset(void) { last_arg = 0; }
