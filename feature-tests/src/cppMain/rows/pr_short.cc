#include "feature_tests.h"

namespace { short last_arg = 0; }

extern "C" short pr_short_echo(short x) { last_arg = x; return x; }
extern "C" short pr_short_last_arg(void) { return last_arg; }
extern "C" void pr_short_reset(void) { last_arg = 0; }
