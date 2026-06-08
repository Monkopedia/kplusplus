#include "feature_tests.h"

namespace { size_t last_arg = 0; }

extern "C" size_t pr_size_t_echo(size_t x) { last_arg = x; return x; }
extern "C" size_t pr_size_t_last_arg(void) { return last_arg; }
extern "C" void pr_size_t_reset(void) { last_arg = 0; }
