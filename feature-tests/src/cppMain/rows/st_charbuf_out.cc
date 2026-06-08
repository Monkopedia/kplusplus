#include "feature_tests.h"
#include <cstring>

namespace {
const char* const PAYLOAD = "abcdefghij";
const size_t PLEN = 10;
}

extern "C" size_t st_charbuf_out_payload_len(void) { return PLEN; }

extern "C" size_t st_charbuf_out_fill(char* buf, size_t n) {
    if (buf == nullptr || n == 0) return 0;
    size_t copy = PLEN < (n - 1) ? PLEN : (n - 1); // leave room for the NUL
    std::memcpy(buf, PAYLOAD, copy);
    buf[copy] = '\0';
    return copy;
}
