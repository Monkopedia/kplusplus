#include "feature_tests.h"

namespace { wchar_t last_arg = 0; }

extern "C" wchar_t pr_wchar_echo(wchar_t x) { last_arg = x; return x; }
extern "C" wchar_t pr_wchar_last_arg(void) { return last_arg; }
extern "C" void pr_wchar_reset(void) { last_arg = 0; }
