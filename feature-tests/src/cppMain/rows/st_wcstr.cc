#include "feature_tests.h"
#include <cwchar>

namespace {
const size_t CAP = 1024;
wchar_t buf[CAP];
size_t len = 0;
}

extern "C" void st_wcstr_take(const wchar_t* s) {
    if (s == nullptr) { len = 0; return; }
    len = std::wcslen(s);
    size_t n = len < CAP ? len : CAP - 1;
    for (size_t i = 0; i < n; i++) buf[i] = s[i];
}

extern "C" size_t st_wcstr_last_len(void) { return len; }

extern "C" long st_wcstr_code_at(size_t i) {
    if (i >= len || i >= CAP) return -1;
    return (long) buf[i];
}

// "A€😀" — ASCII, a BMP code point (U+20AC), and an astral one (U+1F600).
extern "C" const wchar_t* st_wcstr_name(void) {
    static const wchar_t* const s = L"A€\U0001F600";
    return s;
}

extern "C" void st_wcstr_reset(void) { len = 0; }
