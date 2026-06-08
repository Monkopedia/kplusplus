#include "feature_tests.h"

// Borrowed pointer into static storage — the caller must NOT free it, and
// repeated calls return the same address.
extern "C" const char* st_cstr_ret_name(void) {
    static const char* const s = "kplusplus";
    return s;
}
