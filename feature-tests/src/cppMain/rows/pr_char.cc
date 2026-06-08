#include "feature_tests.h"

namespace { char last_arg = 0; }

extern "C" char pr_char_echo(char c) { last_arg = c; return c; }
extern "C" char pr_char_last_arg(void) { return last_arg; }
extern "C" void pr_char_reset(void) { last_arg = 0; }
