#include "feature_tests.h"

namespace { unsigned long last_arg = 0; }

extern "C" unsigned long pr_ulong_echo(unsigned long x) { last_arg = x; return x; }
extern "C" unsigned long pr_ulong_last_arg(void) { return last_arg; }
extern "C" void pr_ulong_reset(void) { last_arg = 0; }
