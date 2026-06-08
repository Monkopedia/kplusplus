#include "feature_tests.h"

namespace { float last_arg = 0.0f; }

extern "C" float pr_float_echo(float x) { last_arg = x; return x; }
extern "C" float pr_float_last_arg(void) { return last_arg; }
extern "C" void pr_float_reset(void) { last_arg = 0.0f; }
