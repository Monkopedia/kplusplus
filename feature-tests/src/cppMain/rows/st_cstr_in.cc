#include "feature_tests.h"
#include <cstring>

namespace {
const size_t CAP = 8192;
char buf[CAP];
size_t len = 0;
int wasNull = 0;
}

extern "C" void st_cstr_in_take(const char* s) {
    if (s == nullptr) { wasNull = 1; len = 0; return; }
    wasNull = 0;
    len = std::strlen(s);                 // const char* has no length — strlen is it
    size_t n = len < CAP ? len : CAP - 1; // store what fits for byte inspection
    std::memcpy(buf, s, n);
}

extern "C" int st_cstr_in_was_null(void) { return wasNull; }
extern "C" size_t st_cstr_in_last_len(void) { return len; }

extern "C" int st_cstr_in_byte_at(size_t i) {
    if (i >= len || i >= CAP) return -1;
    return (unsigned char) buf[i];
}

extern "C" void st_cstr_in_reset(void) { len = 0; wasNull = 0; }
