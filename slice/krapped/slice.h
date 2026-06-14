#ifndef __SLICE__
    #define __SLICE__

    #include <stdlib.h>
    #include <stdint.h>
    #include <stdbool.h>
    #include <stddef.h>

    #ifdef __cplusplus
        extern "C" {
    #endif //__cplusplus

    // BEGIN KRAPPER GEN for std::map<int, int>

    void* std_map_int___int_new(void* location);

    void* std_map_int___int_new__const_std_map_and(void* location, void* _arg_0);

    void std_map_int___int_dispose(void* thiz);

    void* std_map_int___int_op_assign(void* thiz, void* _arg_0);

    bool std_map_int___int_empty(void* thiz);

    size_t std_map_int___int_size(void* thiz);

    size_t std_map_int___int_max_size(void* thiz);

    int* std_map_int___int_op_ind(void* thiz, const int __k);

    int* std_map_int___int_at(void* thiz, const int __k);

    size_t std_map_int___int_erase(void* thiz, const int __x);

    void std_map_int___int_swap(void* thiz, void* __x);

    void std_map_int___int_clear(void* thiz);

    size_t std_map_int___int_count(void* thiz, const int __x);

    int std_map_int___int_size_of();

    int std_map_int___int_align_of();


    // END KRAPPER GEN for std::map<int, int>


    // BEGIN KRAPPER GEN for std::initializer_list<char>

    void* std_initializer_list_char_new(void* location);

    size_t std_initializer_list_char_size(void* thiz);

    int std_initializer_list_char_size_of();

    int std_initializer_list_char_align_of();


    // END KRAPPER GEN for std::initializer_list<char>


    // BEGIN KRAPPER GEN for std::__cxx11::basic_string<char>

    void* std___cxx11_basic_string_char_new(void* location);

    void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and(void* location, void* __str);

    void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(void* location, void* __str, size_t __pos);

    void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_size_t(void* location, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_char_new__const_template__CharT_P_size_t_const_template__Alloc_and(void* location, const char* __s, size_t __n);

    void* std___cxx11_basic_string_char_new__const_template__CharT_P_const_template__Alloc_and(void* location, const char* __s);

    void* std___cxx11_basic_string_char_new__size_t_template__CharT_const_template__Alloc_and(void* location, size_t __n, char __c);

    void* std___cxx11_basic_string_char_new__std_initializer_list_template__CharT_const_template__Alloc_and(void* location, void* __l);

    void std___cxx11_basic_string_char_dispose(void* thiz);

    void* std___cxx11_basic_string_char_op_assign(void* thiz, void* __str);

    void* std___cxx11_basic_string_char_op_assign__const_template__CharT_P(void* thiz, const char* __s);

    void* std___cxx11_basic_string_char_op_assign__template__CharT(void* thiz, char __c);

    void* std___cxx11_basic_string_char_op_assign__std_initializer_list_template__CharT(void* thiz, void* __l);

    size_t std___cxx11_basic_string_char_size(void* thiz);

    size_t std___cxx11_basic_string_char_length(void* thiz);

    size_t std___cxx11_basic_string_char_max_size(void* thiz);

    void std___cxx11_basic_string_char_resize(void* thiz, size_t __n, char __c);

    void std___cxx11_basic_string_char_resize__size_t(void* thiz, size_t __n);

    void std___cxx11_basic_string_char_shrink_to_fit(void* thiz);

    size_t std___cxx11_basic_string_char_capacity(void* thiz);

    void std___cxx11_basic_string_char_reserve(void* thiz, size_t __res_arg);

    void std___cxx11_basic_string_char_clear(void* thiz);

    bool std___cxx11_basic_string_char_empty(void* thiz);

    void* std___cxx11_basic_string_char_op_plus_equals(void* thiz, void* __str);

    void* std___cxx11_basic_string_char_op_plus_equals__const_template__CharT_P(void* thiz, const char* __s);

    void* std___cxx11_basic_string_char_op_plus_equals__template__CharT(void* thiz, char __c);

    void* std___cxx11_basic_string_char_op_plus_equals__std_initializer_list_template__CharT(void* thiz, void* __l);

    void* std___cxx11_basic_string_char_append(void* thiz, void* __str);

    void* std___cxx11_basic_string_char_append__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_char_append__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __n);

    void* std___cxx11_basic_string_char_append__const_template__CharT_P(void* thiz, const char* __s);

    void* std___cxx11_basic_string_char_append__size_t_template__CharT(void* thiz, size_t __n, char __c);

    void* std___cxx11_basic_string_char_append__std_initializer_list_template__CharT(void* thiz, void* __l);

    void std___cxx11_basic_string_char_push_back(void* thiz, char __c);

    void* std___cxx11_basic_string_char_assign(void* thiz, void* __str);

    void* std___cxx11_basic_string_char_assign__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_char_assign__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __n);

    void* std___cxx11_basic_string_char_assign__const_template__CharT_P(void* thiz, const char* __s);

    void* std___cxx11_basic_string_char_assign__size_t_template__CharT(void* thiz, size_t __n, char __c);

    void* std___cxx11_basic_string_char_assign__std_initializer_list_template__CharT(void* thiz, void* __l);

    void* std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos1, void* __str);

    void* std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, void* __str, size_t __pos2, size_t __n);

    void* std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, const char* __s, size_t __n);

    void* std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P(void* thiz, size_t __pos, const char* __s);

    void* std___cxx11_basic_string_char_insert__size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n, char __c);

    void* std___cxx11_basic_string_char_erase(void* thiz, size_t __pos, size_t __n);

    void std___cxx11_basic_string_char_pop_back(void* thiz);

    void* std___cxx11_basic_string_char_replace(void* thiz, size_t __pos, size_t __n, void* __str);

    void* std___cxx11_basic_string_char_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2);

    void* std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const char* __s, size_t __n2);

    void* std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const char* __s);

    void* std___cxx11_basic_string_char_replace__size_t_size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n1, size_t __n2, char __c);

    size_t std___cxx11_basic_string_char_copy(void* thiz, char* __s, size_t __n, size_t __pos);

    void std___cxx11_basic_string_char_swap(void* thiz, void* __s);

    const char* std___cxx11_basic_string_char_c_str(void* thiz);

    const char* std___cxx11_basic_string_char_data(void* thiz);

    size_t std___cxx11_basic_string_char_find(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_find__const_std___cxx11_basic_string_and_size_t(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_find__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_find__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    size_t std___cxx11_basic_string_char_rfind(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_rfind__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_of__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_of__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_not_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_find_first_not_of__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_not_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos);

    size_t std___cxx11_basic_string_char_find_last_not_of__template__CharT_size_t(void* thiz, char __c, size_t __pos);

    void std___cxx11_basic_string_char_substr(void* thiz, size_t __pos, size_t __n, void* ret_value);

    int std___cxx11_basic_string_char_compare(void* thiz, void* __str);

    int std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos, size_t __n, void* __str);

    int std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2);

    int std___cxx11_basic_string_char_compare__const_template__CharT_P(void* thiz, const char* __s);

    int std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const char* __s);

    int std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const char* __s, size_t __n2);

    int std___cxx11_basic_string_char_size_of();

    int std___cxx11_basic_string_char_align_of();


    // END KRAPPER GEN for std::__cxx11::basic_string<char>


    // BEGIN KRAPPER GEN for std::initializer_list<wchar_t>

    void* std_initializer_list_wchar_t_new(void* location);

    size_t std_initializer_list_wchar_t_size(void* thiz);

    int std_initializer_list_wchar_t_size_of();

    int std_initializer_list_wchar_t_align_of();


    // END KRAPPER GEN for std::initializer_list<wchar_t>


    // BEGIN KRAPPER GEN for std::__cxx11::basic_string<wchar_t>

    void* std___cxx11_basic_string_wchar_t_new(void* location);

    void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and(void* location, void* __str);

    void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(void* location, void* __str, size_t __pos);

    void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_size_t(void* location, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_size_t_const_template__Alloc_and(void* location, const wchar_t* __s, size_t __n);

    void* std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_const_template__Alloc_and(void* location, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_new__size_t_template__CharT_const_template__Alloc_and(void* location, size_t __n, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_new__std_initializer_list_template__CharT_const_template__Alloc_and(void* location, void* __l);

    void std___cxx11_basic_string_wchar_t_dispose(void* thiz);

    void* std___cxx11_basic_string_wchar_t_op_assign(void* thiz, void* __str);

    void* std___cxx11_basic_string_wchar_t_op_assign__const_template__CharT_P(void* thiz, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_op_assign__template__CharT(void* thiz, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_op_assign__std_initializer_list_template__CharT(void* thiz, void* __l);

    size_t std___cxx11_basic_string_wchar_t_size(void* thiz);

    size_t std___cxx11_basic_string_wchar_t_length(void* thiz);

    size_t std___cxx11_basic_string_wchar_t_max_size(void* thiz);

    void std___cxx11_basic_string_wchar_t_resize(void* thiz, size_t __n, wchar_t __c);

    void std___cxx11_basic_string_wchar_t_resize__size_t(void* thiz, size_t __n);

    void std___cxx11_basic_string_wchar_t_shrink_to_fit(void* thiz);

    size_t std___cxx11_basic_string_wchar_t_capacity(void* thiz);

    void std___cxx11_basic_string_wchar_t_reserve(void* thiz, size_t __res_arg);

    void std___cxx11_basic_string_wchar_t_clear(void* thiz);

    bool std___cxx11_basic_string_wchar_t_empty(void* thiz);

    void* std___cxx11_basic_string_wchar_t_op_plus_equals(void* thiz, void* __str);

    void* std___cxx11_basic_string_wchar_t_op_plus_equals__const_template__CharT_P(void* thiz, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_op_plus_equals__template__CharT(void* thiz, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_op_plus_equals__std_initializer_list_template__CharT(void* thiz, void* __l);

    void* std___cxx11_basic_string_wchar_t_append(void* thiz, void* __str);

    void* std___cxx11_basic_string_wchar_t_append__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_wchar_t_append__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __n);

    void* std___cxx11_basic_string_wchar_t_append__const_template__CharT_P(void* thiz, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_append__size_t_template__CharT(void* thiz, size_t __n, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_append__std_initializer_list_template__CharT(void* thiz, void* __l);

    void std___cxx11_basic_string_wchar_t_push_back(void* thiz, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_assign(void* thiz, void* __str);

    void* std___cxx11_basic_string_wchar_t_assign__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n);

    void* std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __n);

    void* std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P(void* thiz, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_assign__size_t_template__CharT(void* thiz, size_t __n, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_assign__std_initializer_list_template__CharT(void* thiz, void* __l);

    void* std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos1, void* __str);

    void* std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, void* __str, size_t __pos2, size_t __n);

    void* std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, const wchar_t* __s, size_t __n);

    void* std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P(void* thiz, size_t __pos, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_insert__size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n, wchar_t __c);

    void* std___cxx11_basic_string_wchar_t_erase(void* thiz, size_t __pos, size_t __n);

    void std___cxx11_basic_string_wchar_t_pop_back(void* thiz);

    void* std___cxx11_basic_string_wchar_t_replace(void* thiz, size_t __pos, size_t __n, void* __str);

    void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2);

    void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s, size_t __n2);

    void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s);

    void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n1, size_t __n2, wchar_t __c);

    size_t std___cxx11_basic_string_wchar_t_copy(void* thiz, wchar_t* __s, size_t __n, size_t __pos);

    void std___cxx11_basic_string_wchar_t_swap(void* thiz, void* __s);

    const wchar_t* std___cxx11_basic_string_wchar_t_c_str(void* thiz);

    const wchar_t* std___cxx11_basic_string_wchar_t_data(void* thiz);

    size_t std___cxx11_basic_string_wchar_t_find(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_find__const_std___cxx11_basic_string_and_size_t(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_rfind(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_rfind__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_not_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_first_not_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_not_of(void* thiz, void* __str, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n);

    size_t std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos);

    size_t std___cxx11_basic_string_wchar_t_find_last_not_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos);

    void std___cxx11_basic_string_wchar_t_substr(void* thiz, size_t __pos, size_t __n, void* ret_value);

    int std___cxx11_basic_string_wchar_t_compare(void* thiz, void* __str);

    int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos, size_t __n, void* __str);

    int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2);

    int std___cxx11_basic_string_wchar_t_compare__const_template__CharT_P(void* thiz, const wchar_t* __s);

    int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s);

    int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s, size_t __n2);

    int std___cxx11_basic_string_wchar_t_size_of();

    int std___cxx11_basic_string_wchar_t_align_of();


    // END KRAPPER GEN for std::__cxx11::basic_string<wchar_t>


    // BEGIN KRAPPER GEN for std::initializer_list<double>

    void* std_initializer_list_double_new(void* location);

    size_t std_initializer_list_double_size(void* thiz);

    int std_initializer_list_double_size_of();

    int std_initializer_list_double_align_of();


    // END KRAPPER GEN for std::initializer_list<double>


    // BEGIN KRAPPER GEN for std::vector<double>

    void* std_vector_double_new(void* location);

    void* std_vector_double_new__size_t_const_template__Alloc_and(void* location, size_t __n);

    void* std_vector_double_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const double __value);

    void* std_vector_double_new__const_std_vector_and(void* location, void* __x);

    void* std_vector_double_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l);

    void std_vector_double_dispose(void* thiz);

    void* std_vector_double_op_assign(void* thiz, void* __x);

    void* std_vector_double_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    void std_vector_double_assign(void* thiz, size_t __n, const double __val);

    void std_vector_double_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    size_t std_vector_double_size(void* thiz);

    size_t std_vector_double_max_size(void* thiz);

    void std_vector_double_resize(void* thiz, size_t __new_size);

    void std_vector_double_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const double __x);

    void std_vector_double_shrink_to_fit(void* thiz);

    size_t std_vector_double_capacity(void* thiz);

    bool std_vector_double_empty(void* thiz);

    void std_vector_double_reserve(void* thiz, size_t __n);

    double* std_vector_double_op_ind(void* thiz, size_t __n);

    double* std_vector_double_at(void* thiz, size_t __n);

    double* std_vector_double_front(void* thiz);

    double* std_vector_double_back(void* thiz);

    double* std_vector_double_data(void* thiz);

    void std_vector_double_push_back(void* thiz, const double __x);

    void std_vector_double_pop_back(void* thiz);

    void std_vector_double_swap(void* thiz, void* __x);

    void std_vector_double_clear(void* thiz);

    int std_vector_double_size_of();

    int std_vector_double_align_of();


    // END KRAPPER GEN for std::vector<double>


    // BEGIN KRAPPER GEN for std::_Bit_reference

    void* std__Bit_reference_new(void* location, unsigned long* __x, unsigned long __y);

    void* _std__Bit_reference_new(void* location);

    bool std__Bit_reference_op_to_boolean(void* thiz);

    void* std__Bit_reference_op_assign(void* thiz, bool __x);

    void* std__Bit_reference_op_assign__const_std__Bit_reference_and(void* thiz, void* __x);

    bool std__Bit_reference_op_eq(void* thiz, void* __x);

    bool std__Bit_reference_op_lt(void* thiz, void* __x);

    void std__Bit_reference_flip(void* thiz);

    int std__Bit_reference_size_of();

    int std__Bit_reference_align_of();

    unsigned long* std__Bit_reference__M_p_get(void* thiz);

    void std__Bit_reference__M_p_set(void* thiz, unsigned long* value);

    unsigned long std__Bit_reference__M_mask_get(void* thiz);

    void std__Bit_reference__M_mask_set(void* thiz, unsigned long value);


    // END KRAPPER GEN for std::_Bit_reference


    // BEGIN KRAPPER GEN for std::initializer_list<float>

    void* std_initializer_list_float_new(void* location);

    size_t std_initializer_list_float_size(void* thiz);

    int std_initializer_list_float_size_of();

    int std_initializer_list_float_align_of();


    // END KRAPPER GEN for std::initializer_list<float>


    // BEGIN KRAPPER GEN for std::vector<float>

    void* std_vector_float_new(void* location);

    void* std_vector_float_new__size_t_const_template__Alloc_and(void* location, size_t __n);

    void* std_vector_float_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const float __value);

    void* std_vector_float_new__const_std_vector_and(void* location, void* __x);

    void* std_vector_float_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l);

    void std_vector_float_dispose(void* thiz);

    void* std_vector_float_op_assign(void* thiz, void* __x);

    void* std_vector_float_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    void std_vector_float_assign(void* thiz, size_t __n, const float __val);

    void std_vector_float_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    size_t std_vector_float_size(void* thiz);

    size_t std_vector_float_max_size(void* thiz);

    void std_vector_float_resize(void* thiz, size_t __new_size);

    void std_vector_float_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const float __x);

    void std_vector_float_shrink_to_fit(void* thiz);

    size_t std_vector_float_capacity(void* thiz);

    bool std_vector_float_empty(void* thiz);

    void std_vector_float_reserve(void* thiz, size_t __n);

    float* std_vector_float_op_ind(void* thiz, size_t __n);

    float* std_vector_float_at(void* thiz, size_t __n);

    float* std_vector_float_front(void* thiz);

    float* std_vector_float_back(void* thiz);

    float* std_vector_float_data(void* thiz);

    void std_vector_float_push_back(void* thiz, const float __x);

    void std_vector_float_pop_back(void* thiz);

    void std_vector_float_swap(void* thiz, void* __x);

    void std_vector_float_clear(void* thiz);

    int std_vector_float_size_of();

    int std_vector_float_align_of();


    // END KRAPPER GEN for std::vector<float>


    // BEGIN KRAPPER GEN for std::initializer_list<int>

    void* std_initializer_list_int_new(void* location);

    size_t std_initializer_list_int_size(void* thiz);

    int std_initializer_list_int_size_of();

    int std_initializer_list_int_align_of();


    // END KRAPPER GEN for std::initializer_list<int>


    // BEGIN KRAPPER GEN for std::vector<int>

    void* std_vector_int_new(void* location);

    void* std_vector_int_new__size_t_const_template__Alloc_and(void* location, size_t __n);

    void* std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const int __value);

    void* std_vector_int_new__const_std_vector_and(void* location, void* __x);

    void* std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l);

    void std_vector_int_dispose(void* thiz);

    void* std_vector_int_op_assign(void* thiz, void* __x);

    void* std_vector_int_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    void std_vector_int_assign(void* thiz, size_t __n, const int __val);

    void std_vector_int_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    size_t std_vector_int_size(void* thiz);

    size_t std_vector_int_max_size(void* thiz);

    void std_vector_int_resize(void* thiz, size_t __new_size);

    void std_vector_int_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const int __x);

    void std_vector_int_shrink_to_fit(void* thiz);

    size_t std_vector_int_capacity(void* thiz);

    bool std_vector_int_empty(void* thiz);

    void std_vector_int_reserve(void* thiz, size_t __n);

    int* std_vector_int_op_ind(void* thiz, size_t __n);

    int* std_vector_int_at(void* thiz, size_t __n);

    int* std_vector_int_front(void* thiz);

    int* std_vector_int_back(void* thiz);

    int* std_vector_int_data(void* thiz);

    void std_vector_int_push_back(void* thiz, const int __x);

    void std_vector_int_pop_back(void* thiz);

    void std_vector_int_swap(void* thiz, void* __x);

    void std_vector_int_clear(void* thiz);

    int std_vector_int_size_of();

    int std_vector_int_align_of();


    // END KRAPPER GEN for std::vector<int>


    // BEGIN KRAPPER GEN for std::initializer_list<std::vector<int>>

    void* std_initializer_list_std_vector_int_new(void* location);

    size_t std_initializer_list_std_vector_int_size(void* thiz);

    int std_initializer_list_std_vector_int_size_of();

    int std_initializer_list_std_vector_int_align_of();


    // END KRAPPER GEN for std::initializer_list<std::vector<int>>


    // BEGIN KRAPPER GEN for std::vector<std::vector<int>>

    void* std_vector_std_vector_int_new(void* location);

    void* std_vector_std_vector_int_new__size_t_const_template__Alloc_and(void* location, size_t __n);

    void* std_vector_std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, void* __value);

    void* std_vector_std_vector_int_new__const_std_vector_and(void* location, void* __x);

    void* std_vector_std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l);

    void std_vector_std_vector_int_dispose(void* thiz);

    void* std_vector_std_vector_int_op_assign(void* thiz, void* __x);

    void* std_vector_std_vector_int_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    void std_vector_std_vector_int_assign(void* thiz, size_t __n, void* __val);

    void std_vector_std_vector_int_assign__std_initializer_list_template__Tp(void* thiz, void* __l);

    size_t std_vector_std_vector_int_size(void* thiz);

    size_t std_vector_std_vector_int_max_size(void* thiz);

    void std_vector_std_vector_int_resize(void* thiz, size_t __new_size);

    void std_vector_std_vector_int_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, void* __x);

    void std_vector_std_vector_int_shrink_to_fit(void* thiz);

    size_t std_vector_std_vector_int_capacity(void* thiz);

    bool std_vector_std_vector_int_empty(void* thiz);

    void std_vector_std_vector_int_reserve(void* thiz, size_t __n);

    void* std_vector_std_vector_int_op_ind(void* thiz, size_t __n);

    void* std_vector_std_vector_int_at(void* thiz, size_t __n);

    void* std_vector_std_vector_int_front(void* thiz);

    void* std_vector_std_vector_int_back(void* thiz);

    void* std_vector_std_vector_int_data(void* thiz);

    void std_vector_std_vector_int_push_back(void* thiz, void* __x);

    void std_vector_std_vector_int_pop_back(void* thiz);

    void std_vector_std_vector_int_swap(void* thiz, void* __x);

    void std_vector_std_vector_int_clear(void* thiz);

    int std_vector_std_vector_int_size_of();

    int std_vector_std_vector_int_align_of();


    // END KRAPPER GEN for std::vector<std::vector<int>>



    #ifdef __cplusplus
        }
    #endif //__cplusplus

#endif //__SLICE__
