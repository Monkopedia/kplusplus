#include "feature_tests.h"

namespace { char32_t last_arg = 0; }

extern "C" char32_t pr_char32_echo(char32_t x) { last_arg = x; return x; }
extern "C" char32_t pr_char32_last_arg(void) { return last_arg; }
extern "C" void pr_char32_reset(void) { last_arg = 0; }
