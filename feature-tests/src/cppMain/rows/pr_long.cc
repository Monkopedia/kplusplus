#include "feature_tests.h"

namespace { long last_arg = 0; }

extern "C" long pr_long_echo(long x) { last_arg = x; return x; }
extern "C" long pr_long_last_arg(void) { return last_arg; }
extern "C" void pr_long_reset(void) { last_arg = 0; }
