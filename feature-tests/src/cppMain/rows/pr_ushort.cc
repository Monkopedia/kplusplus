#include "feature_tests.h"

namespace { unsigned short last_arg = 0; }

extern "C" unsigned short pr_ushort_echo(unsigned short x) { last_arg = x; return x; }
extern "C" unsigned short pr_ushort_last_arg(void) { return last_arg; }
extern "C" void pr_ushort_reset(void) { last_arg = 0; }
