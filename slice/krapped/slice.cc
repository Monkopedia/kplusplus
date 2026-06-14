#include "slice.h"
#include "KrapperForce_std_map_int_int.h"
#include "KrapperForce_std_vector_double.h"
#include "KrapperForce_std_vector_float.h"
#include "KrapperForce_std_vector_int.h"
#include "KrapperForce_std_vector_std_vector_int.h"
#include <vector>
#include <string>
#include <iterator>

extern "C" {

typedef void (*StackConstructorCallback)(void*, void*);

// BEGIN KRAPPER GEN for std::map<int, int>

void* std_map_int___int_new(void* location) {
    return new (location) std::map<int, int>();
}

void* std_map_int___int_new__const_std_map_and(void* location, void* _arg_0) {
    const std::map<int, int>* _arg_0_cast = reinterpret_cast<const std::map<int, int>*>(_arg_0);
    return new (location) std::map<int, int>(*_arg_0_cast);
}

void std_map_int___int_dispose(void* thiz) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    thiz_cast->~map();
}

void* std_map_int___int_op_assign(void* thiz, void* _arg_0) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    const std::map<int, int>* _arg_0_cast = reinterpret_cast<const std::map<int, int>*>(_arg_0);
    return (void*)&((*thiz_cast = *_arg_0_cast));
}

bool std_map_int___int_empty(void* thiz) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return thiz_cast->empty();
}

size_t std_map_int___int_size(void* thiz) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return thiz_cast->size();
}

size_t std_map_int___int_max_size(void* thiz) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return thiz_cast->max_size();
}

int* std_map_int___int_op_ind(void* thiz, const int __k) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return &(thiz_cast->operator[](__k));
}

int* std_map_int___int_at(void* thiz, const int __k) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return &(thiz_cast->at(__k));
}

size_t std_map_int___int_erase(void* thiz, const int __x) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return thiz_cast->erase(__x);
}

void std_map_int___int_swap(void* thiz, void* __x) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    std::map<int, int>* __x_cast = reinterpret_cast<std::map<int, int>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_map_int___int_clear(void* thiz) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    thiz_cast->clear();
}

size_t std_map_int___int_count(void* thiz, const int __x) {
    std::map<int, int>* thiz_cast = reinterpret_cast<std::map<int, int>*>(thiz);
    return thiz_cast->count(__x);
}

int std_map_int___int_size_of() {
    return sizeof(std::map<int, int>);
}

int std_map_int___int_align_of() {
    return alignof(std::map<int, int>);
}


// END KRAPPER GEN for std::map<int, int>


// BEGIN KRAPPER GEN for std::initializer_list<char>

void* std_initializer_list_char_new(void* location) {
    return new (location) std::initializer_list<char>();
}

size_t std_initializer_list_char_size(void* thiz) {
    std::initializer_list<char>* thiz_cast = reinterpret_cast<std::initializer_list<char>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_char_size_of() {
    return sizeof(std::initializer_list<char>);
}

int std_initializer_list_char_align_of() {
    return alignof(std::initializer_list<char>);
}


// END KRAPPER GEN for std::initializer_list<char>


// BEGIN KRAPPER GEN for std::__cxx11::basic_string<char>

void* std___cxx11_basic_string_char_new(void* location) {
    return new (location) std::__cxx11::basic_string<char>();
}

void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and(void* location, void* __str) {
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return new (location) std::__cxx11::basic_string<char>(*__str_cast);
}

void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(void* location, void* __str, size_t __pos) {
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return new (location) std::__cxx11::basic_string<char>(*__str_cast, __pos);
}

void* std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_size_t(void* location, void* __str, size_t __pos, size_t __n) {
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return new (location) std::__cxx11::basic_string<char>(*__str_cast, __pos, __n);
}

void* std___cxx11_basic_string_char_new__const_template__CharT_P_size_t_const_template__Alloc_and(void* location, const char* __s, size_t __n) {
    return new (location) std::__cxx11::basic_string<char>(__s, __n);
}

void* std___cxx11_basic_string_char_new__const_template__CharT_P_const_template__Alloc_and(void* location, const char* __s) {
    return new (location) std::__cxx11::basic_string<char>(__s);
}

void* std___cxx11_basic_string_char_new__size_t_template__CharT_const_template__Alloc_and(void* location, size_t __n, char __c) {
    return new (location) std::__cxx11::basic_string<char>(__n, __c);
}

void* std___cxx11_basic_string_char_new__std_initializer_list_template__CharT_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<char>* __l_cast = reinterpret_cast<std::initializer_list<char>*>(__l);
    return new (location) std::__cxx11::basic_string<char>(*__l_cast);
}

void std___cxx11_basic_string_char_dispose(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->~basic_string();
}

void* std___cxx11_basic_string_char_op_assign(void* thiz, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&((*thiz_cast = *__str_cast));
}

void* std___cxx11_basic_string_char_op_assign__const_template__CharT_P(void* thiz, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&((*thiz_cast = __s));
}

void* std___cxx11_basic_string_char_op_assign__template__CharT(void* thiz, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&((*thiz_cast = __c));
}

void* std___cxx11_basic_string_char_op_assign__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::initializer_list<char>* __l_cast = reinterpret_cast<std::initializer_list<char>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

size_t std___cxx11_basic_string_char_size(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->size();
}

size_t std___cxx11_basic_string_char_length(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->length();
}

size_t std___cxx11_basic_string_char_max_size(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->max_size();
}

void std___cxx11_basic_string_char_resize(void* thiz, size_t __n, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->resize(__n, __c);
}

void std___cxx11_basic_string_char_resize__size_t(void* thiz, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->resize(__n);
}

void std___cxx11_basic_string_char_shrink_to_fit(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std___cxx11_basic_string_char_capacity(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->capacity();
}

void std___cxx11_basic_string_char_reserve(void* thiz, size_t __res_arg) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->reserve(__res_arg);
}

void std___cxx11_basic_string_char_clear(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->clear();
}

bool std___cxx11_basic_string_char_empty(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->empty();
}

void* std___cxx11_basic_string_char_op_plus_equals(void* thiz, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&((*thiz_cast += *__str_cast));
}

void* std___cxx11_basic_string_char_op_plus_equals__const_template__CharT_P(void* thiz, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&((*thiz_cast += __s));
}

void* std___cxx11_basic_string_char_op_plus_equals__template__CharT(void* thiz, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&((*thiz_cast += __c));
}

void* std___cxx11_basic_string_char_op_plus_equals__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::initializer_list<char>* __l_cast = reinterpret_cast<std::initializer_list<char>*>(__l);
    return (void*)&((*thiz_cast += *__l_cast));
}

void* std___cxx11_basic_string_char_append(void* thiz, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->append(*__str_cast));
}

void* std___cxx11_basic_string_char_append__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->append(*__str_cast, __pos, __n));
}

void* std___cxx11_basic_string_char_append__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->append(__s, __n));
}

void* std___cxx11_basic_string_char_append__const_template__CharT_P(void* thiz, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->append(__s));
}

void* std___cxx11_basic_string_char_append__size_t_template__CharT(void* thiz, size_t __n, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->append(__n, __c));
}

void* std___cxx11_basic_string_char_append__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::initializer_list<char>* __l_cast = reinterpret_cast<std::initializer_list<char>*>(__l);
    return (void*)&(thiz_cast->append(*__l_cast));
}

void std___cxx11_basic_string_char_push_back(void* thiz, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->push_back(__c);
}

void* std___cxx11_basic_string_char_assign(void* thiz, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->assign(*__str_cast));
}

void* std___cxx11_basic_string_char_assign__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->assign(*__str_cast, __pos, __n));
}

void* std___cxx11_basic_string_char_assign__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->assign(__s, __n));
}

void* std___cxx11_basic_string_char_assign__const_template__CharT_P(void* thiz, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->assign(__s));
}

void* std___cxx11_basic_string_char_assign__size_t_template__CharT(void* thiz, size_t __n, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->assign(__n, __c));
}

void* std___cxx11_basic_string_char_assign__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::initializer_list<char>* __l_cast = reinterpret_cast<std::initializer_list<char>*>(__l);
    return (void*)&(thiz_cast->assign(*__l_cast));
}

void* std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos1, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->insert(__pos1, *__str_cast));
}

void* std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, void* __str, size_t __pos2, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->insert(__pos1, *__str_cast, __pos2, __n));
}

void* std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, const char* __s, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __s, __n));
}

void* std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P(void* thiz, size_t __pos, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __s));
}

void* std___cxx11_basic_string_char_insert__size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __n, __c));
}

void* std___cxx11_basic_string_char_erase(void* thiz, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->erase(__pos, __n));
}

void std___cxx11_basic_string_char_pop_back(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    thiz_cast->pop_back();
}

void* std___cxx11_basic_string_char_replace(void* thiz, size_t __pos, size_t __n, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->replace(__pos, __n, *__str_cast));
}

void* std___cxx11_basic_string_char_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return (void*)&(thiz_cast->replace(__pos1, __n1, *__str_cast, __pos2, __n2));
}

void* std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const char* __s, size_t __n2) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __s, __n2));
}

void* std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __s));
}

void* std___cxx11_basic_string_char_replace__size_t_size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n1, size_t __n2, char __c) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __n2, __c));
}

size_t std___cxx11_basic_string_char_copy(void* thiz, char* __s, size_t __n, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->copy(__s, __n, __pos);
}

void std___cxx11_basic_string_char_swap(void* thiz, void* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::__cxx11::basic_string<char>* __s_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(__s);
    thiz_cast->swap(*__s_cast);
}

const char* std___cxx11_basic_string_char_c_str(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->c_str();
}

const char* std___cxx11_basic_string_char_data(void* thiz) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->data();
}

size_t std___cxx11_basic_string_char_find(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_find__const_std___cxx11_basic_string_and_size_t(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->find(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_find__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find(__s, __pos);
}

size_t std___cxx11_basic_string_char_find__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find(__c, __pos);
}

size_t std___cxx11_basic_string_char_rfind(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->rfind(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->rfind(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->rfind(__s, __pos);
}

size_t std___cxx11_basic_string_char_rfind__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->rfind(__c, __pos);
}

size_t std___cxx11_basic_string_char_find_first_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->find_first_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_of(__s, __pos);
}

size_t std___cxx11_basic_string_char_find_first_of__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_of(__c, __pos);
}

size_t std___cxx11_basic_string_char_find_last_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->find_last_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_of(__s, __pos);
}

size_t std___cxx11_basic_string_char_find_last_of__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_of(__c, __pos);
}

size_t std___cxx11_basic_string_char_find_first_not_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->find_first_not_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_not_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_not_of(__s, __pos);
}

size_t std___cxx11_basic_string_char_find_first_not_of__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_first_not_of(__c, __pos);
}

size_t std___cxx11_basic_string_char_find_last_not_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->find_last_not_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const char* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_not_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t(void* thiz, const char* __s, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_not_of(__s, __pos);
}

size_t std___cxx11_basic_string_char_find_last_not_of__template__CharT_size_t(void* thiz, char __c, size_t __pos) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->find_last_not_of(__c, __pos);
}

void std___cxx11_basic_string_char_substr(void* thiz, size_t __pos, size_t __n, void* ret_value) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    std::__cxx11::basic_string<char>* ret_value_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(ret_value);
    new (ret_value_cast) std::__cxx11::basic_string<char>(thiz_cast->substr(__pos, __n));
}

int std___cxx11_basic_string_char_compare(void* thiz, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->compare(*__str_cast);
}

int std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos, size_t __n, void* __str) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->compare(__pos, __n, *__str_cast);
}

int std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    const std::__cxx11::basic_string<char>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<char>*>(__str);
    return thiz_cast->compare(__pos1, __n1, *__str_cast, __pos2, __n2);
}

int std___cxx11_basic_string_char_compare__const_template__CharT_P(void* thiz, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->compare(__s);
}

int std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const char* __s) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->compare(__pos, __n1, __s);
}

int std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const char* __s, size_t __n2) {
    std::__cxx11::basic_string<char>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<char>*>(thiz);
    return thiz_cast->compare(__pos, __n1, __s, __n2);
}

int std___cxx11_basic_string_char_size_of() {
    return sizeof(std::__cxx11::basic_string<char>);
}

int std___cxx11_basic_string_char_align_of() {
    return alignof(std::__cxx11::basic_string<char>);
}


// END KRAPPER GEN for std::__cxx11::basic_string<char>


// BEGIN KRAPPER GEN for std::initializer_list<wchar_t>

void* std_initializer_list_wchar_t_new(void* location) {
    return new (location) std::initializer_list<wchar_t>();
}

size_t std_initializer_list_wchar_t_size(void* thiz) {
    std::initializer_list<wchar_t>* thiz_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_wchar_t_size_of() {
    return sizeof(std::initializer_list<wchar_t>);
}

int std_initializer_list_wchar_t_align_of() {
    return alignof(std::initializer_list<wchar_t>);
}


// END KRAPPER GEN for std::initializer_list<wchar_t>


// BEGIN KRAPPER GEN for std::__cxx11::basic_string<wchar_t>

void* std___cxx11_basic_string_wchar_t_new(void* location) {
    return new (location) std::__cxx11::basic_string<wchar_t>();
}

void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and(void* location, void* __str) {
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return new (location) std::__cxx11::basic_string<wchar_t>(*__str_cast);
}

void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(void* location, void* __str, size_t __pos) {
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return new (location) std::__cxx11::basic_string<wchar_t>(*__str_cast, __pos);
}

void* std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_size_t(void* location, void* __str, size_t __pos, size_t __n) {
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return new (location) std::__cxx11::basic_string<wchar_t>(*__str_cast, __pos, __n);
}

void* std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_size_t_const_template__Alloc_and(void* location, const wchar_t* __s, size_t __n) {
    return new (location) std::__cxx11::basic_string<wchar_t>(__s, __n);
}

void* std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_const_template__Alloc_and(void* location, const wchar_t* __s) {
    return new (location) std::__cxx11::basic_string<wchar_t>(__s);
}

void* std___cxx11_basic_string_wchar_t_new__size_t_template__CharT_const_template__Alloc_and(void* location, size_t __n, wchar_t __c) {
    return new (location) std::__cxx11::basic_string<wchar_t>(__n, __c);
}

void* std___cxx11_basic_string_wchar_t_new__std_initializer_list_template__CharT_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<wchar_t>* __l_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(__l);
    return new (location) std::__cxx11::basic_string<wchar_t>(*__l_cast);
}

void std___cxx11_basic_string_wchar_t_dispose(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->~basic_string();
}

void* std___cxx11_basic_string_wchar_t_op_assign(void* thiz, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&((*thiz_cast = *__str_cast));
}

void* std___cxx11_basic_string_wchar_t_op_assign__const_template__CharT_P(void* thiz, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&((*thiz_cast = __s));
}

void* std___cxx11_basic_string_wchar_t_op_assign__template__CharT(void* thiz, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&((*thiz_cast = __c));
}

void* std___cxx11_basic_string_wchar_t_op_assign__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::initializer_list<wchar_t>* __l_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

size_t std___cxx11_basic_string_wchar_t_size(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->size();
}

size_t std___cxx11_basic_string_wchar_t_length(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->length();
}

size_t std___cxx11_basic_string_wchar_t_max_size(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->max_size();
}

void std___cxx11_basic_string_wchar_t_resize(void* thiz, size_t __n, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->resize(__n, __c);
}

void std___cxx11_basic_string_wchar_t_resize__size_t(void* thiz, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->resize(__n);
}

void std___cxx11_basic_string_wchar_t_shrink_to_fit(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std___cxx11_basic_string_wchar_t_capacity(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->capacity();
}

void std___cxx11_basic_string_wchar_t_reserve(void* thiz, size_t __res_arg) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->reserve(__res_arg);
}

void std___cxx11_basic_string_wchar_t_clear(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->clear();
}

bool std___cxx11_basic_string_wchar_t_empty(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->empty();
}

void* std___cxx11_basic_string_wchar_t_op_plus_equals(void* thiz, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&((*thiz_cast += *__str_cast));
}

void* std___cxx11_basic_string_wchar_t_op_plus_equals__const_template__CharT_P(void* thiz, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&((*thiz_cast += __s));
}

void* std___cxx11_basic_string_wchar_t_op_plus_equals__template__CharT(void* thiz, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&((*thiz_cast += __c));
}

void* std___cxx11_basic_string_wchar_t_op_plus_equals__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::initializer_list<wchar_t>* __l_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(__l);
    return (void*)&((*thiz_cast += *__l_cast));
}

void* std___cxx11_basic_string_wchar_t_append(void* thiz, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->append(*__str_cast));
}

void* std___cxx11_basic_string_wchar_t_append__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->append(*__str_cast, __pos, __n));
}

void* std___cxx11_basic_string_wchar_t_append__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->append(__s, __n));
}

void* std___cxx11_basic_string_wchar_t_append__const_template__CharT_P(void* thiz, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->append(__s));
}

void* std___cxx11_basic_string_wchar_t_append__size_t_template__CharT(void* thiz, size_t __n, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->append(__n, __c));
}

void* std___cxx11_basic_string_wchar_t_append__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::initializer_list<wchar_t>* __l_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(__l);
    return (void*)&(thiz_cast->append(*__l_cast));
}

void std___cxx11_basic_string_wchar_t_push_back(void* thiz, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->push_back(__c);
}

void* std___cxx11_basic_string_wchar_t_assign(void* thiz, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->assign(*__str_cast));
}

void* std___cxx11_basic_string_wchar_t_assign__const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, void* __str, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->assign(*__str_cast, __pos, __n));
}

void* std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->assign(__s, __n));
}

void* std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P(void* thiz, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->assign(__s));
}

void* std___cxx11_basic_string_wchar_t_assign__size_t_template__CharT(void* thiz, size_t __n, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->assign(__n, __c));
}

void* std___cxx11_basic_string_wchar_t_assign__std_initializer_list_template__CharT(void* thiz, void* __l) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::initializer_list<wchar_t>* __l_cast = reinterpret_cast<std::initializer_list<wchar_t>*>(__l);
    return (void*)&(thiz_cast->assign(*__l_cast));
}

void* std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos1, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->insert(__pos1, *__str_cast));
}

void* std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, void* __str, size_t __pos2, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->insert(__pos1, *__str_cast, __pos2, __n));
}

void* std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, const wchar_t* __s, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __s, __n));
}

void* std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P(void* thiz, size_t __pos, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __s));
}

void* std___cxx11_basic_string_wchar_t_insert__size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->insert(__pos, __n, __c));
}

void* std___cxx11_basic_string_wchar_t_erase(void* thiz, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->erase(__pos, __n));
}

void std___cxx11_basic_string_wchar_t_pop_back(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    thiz_cast->pop_back();
}

void* std___cxx11_basic_string_wchar_t_replace(void* thiz, size_t __pos, size_t __n, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->replace(__pos, __n, *__str_cast));
}

void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return (void*)&(thiz_cast->replace(__pos1, __n1, *__str_cast, __pos2, __n2));
}

void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s, size_t __n2) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __s, __n2));
}

void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __s));
}

void* std___cxx11_basic_string_wchar_t_replace__size_t_size_t_size_t_template__CharT(void* thiz, size_t __pos, size_t __n1, size_t __n2, wchar_t __c) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return (void*)&(thiz_cast->replace(__pos, __n1, __n2, __c));
}

size_t std___cxx11_basic_string_wchar_t_copy(void* thiz, wchar_t* __s, size_t __n, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->copy(__s, __n, __pos);
}

void std___cxx11_basic_string_wchar_t_swap(void* thiz, void* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::__cxx11::basic_string<wchar_t>* __s_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(__s);
    thiz_cast->swap(*__s_cast);
}

const wchar_t* std___cxx11_basic_string_wchar_t_c_str(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->c_str();
}

const wchar_t* std___cxx11_basic_string_wchar_t_data(void* thiz) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->data();
}

size_t std___cxx11_basic_string_wchar_t_find(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_find__const_std___cxx11_basic_string_and_size_t(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->find(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find(__c, __pos);
}

size_t std___cxx11_basic_string_wchar_t_rfind(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->rfind(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->rfind(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->rfind(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_rfind__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->rfind(__c, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->find_first_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_of(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_of(__c, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->find_last_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_of(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_of(__c, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_not_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->find_first_not_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_not_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_not_of(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_first_not_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_first_not_of(__c, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_not_of(void* thiz, void* __str, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->find_last_not_of(*__str_cast, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t_size_t(void* thiz, const wchar_t* __s, size_t __pos, size_t __n) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_not_of(__s, __pos, __n);
}

size_t std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t(void* thiz, const wchar_t* __s, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_not_of(__s, __pos);
}

size_t std___cxx11_basic_string_wchar_t_find_last_not_of__template__CharT_size_t(void* thiz, wchar_t __c, size_t __pos) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->find_last_not_of(__c, __pos);
}

void std___cxx11_basic_string_wchar_t_substr(void* thiz, size_t __pos, size_t __n, void* ret_value) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    std::__cxx11::basic_string<wchar_t>* ret_value_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(ret_value);
    new (ret_value_cast) std::__cxx11::basic_string<wchar_t>(thiz_cast->substr(__pos, __n));
}

int std___cxx11_basic_string_wchar_t_compare(void* thiz, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->compare(*__str_cast);
}

int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and(void* thiz, size_t __pos, size_t __n, void* __str) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->compare(__pos, __n, *__str_cast);
}

int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(void* thiz, size_t __pos1, size_t __n1, void* __str, size_t __pos2, size_t __n2) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    const std::__cxx11::basic_string<wchar_t>* __str_cast = reinterpret_cast<const std::__cxx11::basic_string<wchar_t>*>(__str);
    return thiz_cast->compare(__pos1, __n1, *__str_cast, __pos2, __n2);
}

int std___cxx11_basic_string_wchar_t_compare__const_template__CharT_P(void* thiz, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->compare(__s);
}

int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->compare(__pos, __n1, __s);
}

int std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P_size_t(void* thiz, size_t __pos, size_t __n1, const wchar_t* __s, size_t __n2) {
    std::__cxx11::basic_string<wchar_t>* thiz_cast = reinterpret_cast<std::__cxx11::basic_string<wchar_t>*>(thiz);
    return thiz_cast->compare(__pos, __n1, __s, __n2);
}

int std___cxx11_basic_string_wchar_t_size_of() {
    return sizeof(std::__cxx11::basic_string<wchar_t>);
}

int std___cxx11_basic_string_wchar_t_align_of() {
    return alignof(std::__cxx11::basic_string<wchar_t>);
}


// END KRAPPER GEN for std::__cxx11::basic_string<wchar_t>


// BEGIN KRAPPER GEN for std::initializer_list<double>

void* std_initializer_list_double_new(void* location) {
    return new (location) std::initializer_list<double>();
}

size_t std_initializer_list_double_size(void* thiz) {
    std::initializer_list<double>* thiz_cast = reinterpret_cast<std::initializer_list<double>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_double_size_of() {
    return sizeof(std::initializer_list<double>);
}

int std_initializer_list_double_align_of() {
    return alignof(std::initializer_list<double>);
}


// END KRAPPER GEN for std::initializer_list<double>


// BEGIN KRAPPER GEN for std::vector<double>

void* std_vector_double_new(void* location) {
    return new (location) std::vector<double>();
}

void* std_vector_double_new__size_t_const_template__Alloc_and(void* location, size_t __n) {
    return new (location) std::vector<double>(__n);
}

void* std_vector_double_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const double __value) {
    return new (location) std::vector<double>(__n, __value);
}

void* std_vector_double_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<double>* __x_cast = reinterpret_cast<const std::vector<double>*>(__x);
    return new (location) std::vector<double>(*__x_cast);
}

void* std_vector_double_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<double>* __l_cast = reinterpret_cast<std::initializer_list<double>*>(__l);
    return new (location) std::vector<double>(*__l_cast);
}

void std_vector_double_dispose(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_double_op_assign(void* thiz, void* __x) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    const std::vector<double>* __x_cast = reinterpret_cast<const std::vector<double>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

void* std_vector_double_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    std::initializer_list<double>* __l_cast = reinterpret_cast<std::initializer_list<double>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

void std_vector_double_assign(void* thiz, size_t __n, const double __val) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->assign(__n, __val);
}

void std_vector_double_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    std::initializer_list<double>* __l_cast = reinterpret_cast<std::initializer_list<double>*>(__l);
    thiz_cast->assign(*__l_cast);
}

size_t std_vector_double_size(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_double_max_size(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_double_resize(void* thiz, size_t __new_size) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_double_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const double __x) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->resize(__new_size, __x);
}

void std_vector_double_shrink_to_fit(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_double_capacity(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_double_empty(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_double_reserve(void* thiz, size_t __n) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->reserve(__n);
}

double* std_vector_double_op_ind(void* thiz, size_t __n) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return &(thiz_cast->operator[](__n));
}

double* std_vector_double_at(void* thiz, size_t __n) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return &(thiz_cast->at(__n));
}

double* std_vector_double_front(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return &(thiz_cast->front());
}

double* std_vector_double_back(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return &(thiz_cast->back());
}

double* std_vector_double_data(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    return thiz_cast->data();
}

void std_vector_double_push_back(void* thiz, const double __x) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->push_back(__x);
}

void std_vector_double_pop_back(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_double_swap(void* thiz, void* __x) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    std::vector<double>* __x_cast = reinterpret_cast<std::vector<double>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_double_clear(void* thiz) {
    std::vector<double>* thiz_cast = reinterpret_cast<std::vector<double>*>(thiz);
    thiz_cast->clear();
}

int std_vector_double_size_of() {
    return sizeof(std::vector<double>);
}

int std_vector_double_align_of() {
    return alignof(std::vector<double>);
}


// END KRAPPER GEN for std::vector<double>


// BEGIN KRAPPER GEN for std::_Bit_reference

void* std__Bit_reference_new(void* location, unsigned long* __x, unsigned long __y) {
    return new (location) std::_Bit_reference(__x, __y);
}

void* _std__Bit_reference_new(void* location) {
    return new (location) std::_Bit_reference();
}

bool std__Bit_reference_op_to_boolean(void* thiz) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    return (bool)*thiz_cast;
}

void* std__Bit_reference_op_assign(void* thiz, bool __x) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    return (void*)&((*thiz_cast = __x));
}

void* std__Bit_reference_op_assign__const_std__Bit_reference_and(void* thiz, void* __x) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    const std::_Bit_reference* __x_cast = reinterpret_cast<const std::_Bit_reference*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

bool std__Bit_reference_op_eq(void* thiz, void* __x) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    const std::_Bit_reference* __x_cast = reinterpret_cast<const std::_Bit_reference*>(__x);
    return *thiz_cast == *__x_cast;
}

bool std__Bit_reference_op_lt(void* thiz, void* __x) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    const std::_Bit_reference* __x_cast = reinterpret_cast<const std::_Bit_reference*>(__x);
    return *thiz_cast < *__x_cast;
}

void std__Bit_reference_flip(void* thiz) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    thiz_cast->flip();
}

int std__Bit_reference_size_of() {
    return sizeof(std::_Bit_reference);
}

int std__Bit_reference_align_of() {
    return alignof(std::_Bit_reference);
}

unsigned long* std__Bit_reference__M_p_get(void* thiz) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    return thiz_cast->_M_p;
}

void std__Bit_reference__M_p_set(void* thiz, unsigned long* value) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    (thiz_cast->_M_p = value);
}

unsigned long std__Bit_reference__M_mask_get(void* thiz) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    return thiz_cast->_M_mask;
}

void std__Bit_reference__M_mask_set(void* thiz, unsigned long value) {
    std::_Bit_reference* thiz_cast = reinterpret_cast<std::_Bit_reference*>(thiz);
    (thiz_cast->_M_mask = value);
}


// END KRAPPER GEN for std::_Bit_reference


// BEGIN KRAPPER GEN for std::initializer_list<float>

void* std_initializer_list_float_new(void* location) {
    return new (location) std::initializer_list<float>();
}

size_t std_initializer_list_float_size(void* thiz) {
    std::initializer_list<float>* thiz_cast = reinterpret_cast<std::initializer_list<float>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_float_size_of() {
    return sizeof(std::initializer_list<float>);
}

int std_initializer_list_float_align_of() {
    return alignof(std::initializer_list<float>);
}


// END KRAPPER GEN for std::initializer_list<float>


// BEGIN KRAPPER GEN for std::vector<float>

void* std_vector_float_new(void* location) {
    return new (location) std::vector<float>();
}

void* std_vector_float_new__size_t_const_template__Alloc_and(void* location, size_t __n) {
    return new (location) std::vector<float>(__n);
}

void* std_vector_float_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const float __value) {
    return new (location) std::vector<float>(__n, __value);
}

void* std_vector_float_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<float>* __x_cast = reinterpret_cast<const std::vector<float>*>(__x);
    return new (location) std::vector<float>(*__x_cast);
}

void* std_vector_float_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<float>* __l_cast = reinterpret_cast<std::initializer_list<float>*>(__l);
    return new (location) std::vector<float>(*__l_cast);
}

void std_vector_float_dispose(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_float_op_assign(void* thiz, void* __x) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    const std::vector<float>* __x_cast = reinterpret_cast<const std::vector<float>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

void* std_vector_float_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    std::initializer_list<float>* __l_cast = reinterpret_cast<std::initializer_list<float>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

void std_vector_float_assign(void* thiz, size_t __n, const float __val) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->assign(__n, __val);
}

void std_vector_float_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    std::initializer_list<float>* __l_cast = reinterpret_cast<std::initializer_list<float>*>(__l);
    thiz_cast->assign(*__l_cast);
}

size_t std_vector_float_size(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_float_max_size(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_float_resize(void* thiz, size_t __new_size) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_float_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const float __x) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->resize(__new_size, __x);
}

void std_vector_float_shrink_to_fit(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_float_capacity(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_float_empty(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_float_reserve(void* thiz, size_t __n) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->reserve(__n);
}

float* std_vector_float_op_ind(void* thiz, size_t __n) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return &(thiz_cast->operator[](__n));
}

float* std_vector_float_at(void* thiz, size_t __n) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return &(thiz_cast->at(__n));
}

float* std_vector_float_front(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return &(thiz_cast->front());
}

float* std_vector_float_back(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return &(thiz_cast->back());
}

float* std_vector_float_data(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    return thiz_cast->data();
}

void std_vector_float_push_back(void* thiz, const float __x) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->push_back(__x);
}

void std_vector_float_pop_back(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_float_swap(void* thiz, void* __x) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    std::vector<float>* __x_cast = reinterpret_cast<std::vector<float>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_float_clear(void* thiz) {
    std::vector<float>* thiz_cast = reinterpret_cast<std::vector<float>*>(thiz);
    thiz_cast->clear();
}

int std_vector_float_size_of() {
    return sizeof(std::vector<float>);
}

int std_vector_float_align_of() {
    return alignof(std::vector<float>);
}


// END KRAPPER GEN for std::vector<float>


// BEGIN KRAPPER GEN for std::initializer_list<int>

void* std_initializer_list_int_new(void* location) {
    return new (location) std::initializer_list<int>();
}

size_t std_initializer_list_int_size(void* thiz) {
    std::initializer_list<int>* thiz_cast = reinterpret_cast<std::initializer_list<int>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_int_size_of() {
    return sizeof(std::initializer_list<int>);
}

int std_initializer_list_int_align_of() {
    return alignof(std::initializer_list<int>);
}


// END KRAPPER GEN for std::initializer_list<int>


// BEGIN KRAPPER GEN for std::vector<int>

void* std_vector_int_new(void* location) {
    return new (location) std::vector<int>();
}

void* std_vector_int_new__size_t_const_template__Alloc_and(void* location, size_t __n) {
    return new (location) std::vector<int>(__n);
}

void* std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, const int __value) {
    return new (location) std::vector<int>(__n, __value);
}

void* std_vector_int_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<int>* __x_cast = reinterpret_cast<const std::vector<int>*>(__x);
    return new (location) std::vector<int>(*__x_cast);
}

void* std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<int>* __l_cast = reinterpret_cast<std::initializer_list<int>*>(__l);
    return new (location) std::vector<int>(*__l_cast);
}

void std_vector_int_dispose(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_int_op_assign(void* thiz, void* __x) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    const std::vector<int>* __x_cast = reinterpret_cast<const std::vector<int>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

void* std_vector_int_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    std::initializer_list<int>* __l_cast = reinterpret_cast<std::initializer_list<int>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

void std_vector_int_assign(void* thiz, size_t __n, const int __val) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->assign(__n, __val);
}

void std_vector_int_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    std::initializer_list<int>* __l_cast = reinterpret_cast<std::initializer_list<int>*>(__l);
    thiz_cast->assign(*__l_cast);
}

size_t std_vector_int_size(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_int_max_size(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_int_resize(void* thiz, size_t __new_size) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_int_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, const int __x) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->resize(__new_size, __x);
}

void std_vector_int_shrink_to_fit(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_int_capacity(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_int_empty(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_int_reserve(void* thiz, size_t __n) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->reserve(__n);
}

int* std_vector_int_op_ind(void* thiz, size_t __n) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return &(thiz_cast->operator[](__n));
}

int* std_vector_int_at(void* thiz, size_t __n) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return &(thiz_cast->at(__n));
}

int* std_vector_int_front(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return &(thiz_cast->front());
}

int* std_vector_int_back(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return &(thiz_cast->back());
}

int* std_vector_int_data(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    return thiz_cast->data();
}

void std_vector_int_push_back(void* thiz, const int __x) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->push_back(__x);
}

void std_vector_int_pop_back(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_int_swap(void* thiz, void* __x) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    std::vector<int>* __x_cast = reinterpret_cast<std::vector<int>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_int_clear(void* thiz) {
    std::vector<int>* thiz_cast = reinterpret_cast<std::vector<int>*>(thiz);
    thiz_cast->clear();
}

int std_vector_int_size_of() {
    return sizeof(std::vector<int>);
}

int std_vector_int_align_of() {
    return alignof(std::vector<int>);
}


// END KRAPPER GEN for std::vector<int>


// BEGIN KRAPPER GEN for std::initializer_list<std::vector<int>>

void* std_initializer_list_std_vector_int_new(void* location) {
    return new (location) std::initializer_list<std::vector<int>>();
}

size_t std_initializer_list_std_vector_int_size(void* thiz) {
    std::initializer_list<std::vector<int>>* thiz_cast = reinterpret_cast<std::initializer_list<std::vector<int>>*>(thiz);
    return thiz_cast->size();
}

int std_initializer_list_std_vector_int_size_of() {
    return sizeof(std::initializer_list<std::vector<int>>);
}

int std_initializer_list_std_vector_int_align_of() {
    return alignof(std::initializer_list<std::vector<int>>);
}


// END KRAPPER GEN for std::initializer_list<std::vector<int>>


// BEGIN KRAPPER GEN for std::vector<std::vector<int>>

void* std_vector_std_vector_int_new(void* location) {
    return new (location) std::vector<std::vector<int>>();
}

void* std_vector_std_vector_int_new__size_t_const_template__Alloc_and(void* location, size_t __n) {
    return new (location) std::vector<std::vector<int>>(__n);
}

void* std_vector_std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and(void* location, size_t __n, void* __value) {
    const std::vector<int>* __value_cast = reinterpret_cast<const std::vector<int>*>(__value);
    return new (location) std::vector<std::vector<int>>(__n, *__value_cast);
}

void* std_vector_std_vector_int_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<std::vector<int>>* __x_cast = reinterpret_cast<const std::vector<std::vector<int>>*>(__x);
    return new (location) std::vector<std::vector<int>>(*__x_cast);
}

void* std_vector_std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and(void* location, void* __l) {
    std::initializer_list<std::vector<int>>* __l_cast = reinterpret_cast<std::initializer_list<std::vector<int>>*>(__l);
    return new (location) std::vector<std::vector<int>>(*__l_cast);
}

void std_vector_std_vector_int_dispose(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_std_vector_int_op_assign(void* thiz, void* __x) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    const std::vector<std::vector<int>>* __x_cast = reinterpret_cast<const std::vector<std::vector<int>>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

void* std_vector_std_vector_int_op_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    std::initializer_list<std::vector<int>>* __l_cast = reinterpret_cast<std::initializer_list<std::vector<int>>*>(__l);
    return (void*)&((*thiz_cast = *__l_cast));
}

void std_vector_std_vector_int_assign(void* thiz, size_t __n, void* __val) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    const std::vector<int>* __val_cast = reinterpret_cast<const std::vector<int>*>(__val);
    thiz_cast->assign(__n, *__val_cast);
}

void std_vector_std_vector_int_assign__std_initializer_list_template__Tp(void* thiz, void* __l) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    std::initializer_list<std::vector<int>>* __l_cast = reinterpret_cast<std::initializer_list<std::vector<int>>*>(__l);
    thiz_cast->assign(*__l_cast);
}

size_t std_vector_std_vector_int_size(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_std_vector_int_max_size(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_std_vector_int_resize(void* thiz, size_t __new_size) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_std_vector_int_resize__size_t_const_template__Tp_and(void* thiz, size_t __new_size, void* __x) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    const std::vector<int>* __x_cast = reinterpret_cast<const std::vector<int>*>(__x);
    thiz_cast->resize(__new_size, *__x_cast);
}

void std_vector_std_vector_int_shrink_to_fit(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_std_vector_int_capacity(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_std_vector_int_empty(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_std_vector_int_reserve(void* thiz, size_t __n) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->reserve(__n);
}

void* std_vector_std_vector_int_op_ind(void* thiz, size_t __n) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return (void*)&(thiz_cast->operator[](__n));
}

void* std_vector_std_vector_int_at(void* thiz, size_t __n) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return (void*)&(thiz_cast->at(__n));
}

void* std_vector_std_vector_int_front(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return (void*)&(thiz_cast->front());
}

void* std_vector_std_vector_int_back(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return (void*)&(thiz_cast->back());
}

void* std_vector_std_vector_int_data(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    return (void*)thiz_cast->data();
}

void std_vector_std_vector_int_push_back(void* thiz, void* __x) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    const std::vector<int>* __x_cast = reinterpret_cast<const std::vector<int>*>(__x);
    thiz_cast->push_back(*__x_cast);
}

void std_vector_std_vector_int_pop_back(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_std_vector_int_swap(void* thiz, void* __x) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    std::vector<std::vector<int>>* __x_cast = reinterpret_cast<std::vector<std::vector<int>>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_std_vector_int_clear(void* thiz) {
    std::vector<std::vector<int>>* thiz_cast = reinterpret_cast<std::vector<std::vector<int>>*>(thiz);
    thiz_cast->clear();
}

int std_vector_std_vector_int_size_of() {
    return sizeof(std::vector<std::vector<int>>);
}

int std_vector_std_vector_int_align_of() {
    return alignof(std::vector<std::vector<int>>);
}


// END KRAPPER GEN for std::vector<std::vector<int>>



}

