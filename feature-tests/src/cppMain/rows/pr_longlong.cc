#include "feature_tests.h"

namespace { long long last_arg = 0; }

extern "C" long long pr_longlong_echo(long long x) { last_arg = x; return x; }
extern "C" long long pr_longlong_last_arg(void) { return last_arg; }
extern "C" void pr_longlong_reset(void) { last_arg = 0; }
