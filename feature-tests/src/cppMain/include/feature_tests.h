#ifndef FEATURE_TESTS_H
#define FEATURE_TESTS_H

#ifndef __cplusplus
#include <stdbool.h>
#endif

// Width-variant + Unicode-unit primitives. Valid in both C (cinterop's parse
// mode) and C++ (the row impls). size_t/ptrdiff_t/wchar_t come via cinterop as
// platform.posix.<name> aliases; char16_t/char32_t need <uchar.h> in C mode.
#include <stddef.h>   // size_t, ptrdiff_t, wchar_t
#include <stdint.h>   // intptr_t, uintptr_t
#include <wchar.h>    // wchar_t
#include <uchar.h>    // char16_t, char32_t (C); harmless in C++

#ifdef __cplusplus
extern "C" {
#endif

// Harness smoke test — confirms the C++ → cinterop → Kotlin wiring is alive.
int feature_tests_ping(void);

// ---- PR-bool: bool round-trip ----
bool pr_bool_negate(bool b);
int  pr_bool_call_count(void);
void pr_bool_reset(void);

// ---- PR-int-rt: int arg + return ----
int  pr_int_rt_add_one(int x);
int  pr_int_rt_last_arg(void);
void pr_int_rt_reset(void);

// ---- PR-int-out: int out via ptr ----
// Writes 42 into *out when non-null; increments produced_count on success;
// sets null_flagged when called with a null pointer.
void pr_int_out_produce(int* out);
int  pr_int_out_produced_count(void);
int  pr_int_out_null_flagged(void);
void pr_int_out_reset(void);

// ---- PR-char: char round-trip ----
char pr_char_echo(char c);
char pr_char_last_arg(void);
void pr_char_reset(void);

// ---- PR-uchar: unsigned char round-trip ----
unsigned char pr_uchar_echo(unsigned char c);
unsigned char pr_uchar_last_arg(void);
void          pr_uchar_reset(void);

// ---- PR-short: short round-trip ----
short pr_short_echo(short x);
short pr_short_last_arg(void);
void  pr_short_reset(void);

// ---- PR-ushort: unsigned short round-trip ----
unsigned short pr_ushort_echo(unsigned short x);
unsigned short pr_ushort_last_arg(void);
void           pr_ushort_reset(void);

// ---- PR-uint: unsigned int round-trip ----
unsigned int pr_uint_echo(unsigned int x);
unsigned int pr_uint_last_arg(void);
void         pr_uint_reset(void);

// ---- PR-long: long round-trip ----
long pr_long_echo(long x);
long pr_long_last_arg(void);
void pr_long_reset(void);

// ---- PR-ulong: unsigned long round-trip ----
unsigned long pr_ulong_echo(unsigned long x);
unsigned long pr_ulong_last_arg(void);
void          pr_ulong_reset(void);

// ---- PR-longlong: long long round-trip ----
long long pr_longlong_echo(long long x);
long long pr_longlong_last_arg(void);
void      pr_longlong_reset(void);

// ---- PR-float: float round-trip ----
float pr_float_echo(float x);
float pr_float_last_arg(void);
void  pr_float_reset(void);

// ---- PR-double: double round-trip ----
double pr_double_echo(double x);
double pr_double_last_arg(void);
void   pr_double_reset(void);

// ---- PR-size-t: size_t round-trip ----
size_t pr_size_t_echo(size_t x);
size_t pr_size_t_last_arg(void);
void   pr_size_t_reset(void);

// ---- PR-ptrdiff-t: ptrdiff_t round-trip ----
ptrdiff_t pr_ptrdiff_t_echo(ptrdiff_t x);
ptrdiff_t pr_ptrdiff_t_last_arg(void);
void      pr_ptrdiff_t_reset(void);

// ---- PR-intptr: intptr_t / uintptr_t round-trip ----
intptr_t  pr_intptr_echo(intptr_t x);
intptr_t  pr_intptr_last_arg(void);
uintptr_t pr_uintptr_echo(uintptr_t x);
uintptr_t pr_uintptr_last_arg(void);
void      pr_intptr_reset(void);

// ---- PR-wchar: wchar_t round-trip ----
wchar_t pr_wchar_echo(wchar_t x);
wchar_t pr_wchar_last_arg(void);
void    pr_wchar_reset(void);

// ---- PR-char16: char16_t round-trip ----
char16_t pr_char16_echo(char16_t x);
char16_t pr_char16_last_arg(void);
void     pr_char16_reset(void);

// ---- PR-char32: char32_t round-trip ----
char32_t pr_char32_echo(char32_t x);
char32_t pr_char32_last_arg(void);
void     pr_char32_reset(void);

// ==== Strings — Group A (C strings, pure cinterop) ====

// ---- ST-cstr-in: const char* arg in ----
void   st_cstr_in_take(const char* s);
int    st_cstr_in_was_null(void);
size_t st_cstr_in_last_len(void);     // strlen of last non-null arg
int    st_cstr_in_byte_at(size_t i);  // stored byte 0..255, or -1 if out of range
void   st_cstr_in_reset(void);

// ---- ST-cstr-ret: const char* return (borrowed, static storage) ----
const char* st_cstr_ret_name(void);

// ---- ST-charbuf-out: fill a caller-provided buffer ----
// Writes the canonical payload ("abcdefghij") into buf, NUL-terminated, writing
// at most n bytes total. Returns the number of payload chars written (excluding
// the NUL). n == 0 writes nothing and returns 0.
size_t st_charbuf_out_fill(char* buf, size_t n);
size_t st_charbuf_out_payload_len(void); // 10

// ---- ST-wcstr: wide C string ----
void           st_wcstr_take(const wchar_t* s);
size_t         st_wcstr_last_len(void);    // wcslen of last non-null arg
long           st_wcstr_code_at(size_t i); // code point as long, or -1
const wchar_t* st_wcstr_name(void);        // static wide string "A€😀"
void           st_wcstr_reset(void);

#ifdef __cplusplus
}
#endif

#endif
