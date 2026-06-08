#include "feature_tests.h"

namespace { char16_t last_arg = 0; }

extern "C" char16_t pr_char16_echo(char16_t x) { last_arg = x; return x; }
extern "C" char16_t pr_char16_last_arg(void) { return last_arg; }
extern "C" void pr_char16_reset(void) { last_arg = 0; }
