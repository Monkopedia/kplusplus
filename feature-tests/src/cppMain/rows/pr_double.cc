#include "feature_tests.h"

namespace { double last_arg = 0.0; }

extern "C" double pr_double_echo(double x) { last_arg = x; return x; }
extern "C" double pr_double_last_arg(void) { return last_arg; }
extern "C" void pr_double_reset(void) { last_arg = 0.0; }
