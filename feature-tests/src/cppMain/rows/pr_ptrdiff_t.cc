#include "feature_tests.h"

namespace { ptrdiff_t last_arg = 0; }

extern "C" ptrdiff_t pr_ptrdiff_t_echo(ptrdiff_t x) { last_arg = x; return x; }
extern "C" ptrdiff_t pr_ptrdiff_t_last_arg(void) { return last_arg; }
extern "C" void pr_ptrdiff_t_reset(void) { last_arg = 0; }
