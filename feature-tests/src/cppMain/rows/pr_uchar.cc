#include "feature_tests.h"

namespace { unsigned char last_arg = 0; }

extern "C" unsigned char pr_uchar_echo(unsigned char c) { last_arg = c; return c; }
extern "C" unsigned char pr_uchar_last_arg(void) { return last_arg; }
extern "C" void pr_uchar_reset(void) { last_arg = 0; }
