package com.monkopedia.slice.std.__cxx11

import com.monkopedia.slice.std.Initializer_list__Wchar_t
import com.monkopedia.slice.std.__cxx11.Basic_string__Wchar_t.Companion.Basic_string__Wchar_t_Holder
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_align_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append__size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_append__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign__size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_assign__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_c_str
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_capacity
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_clear
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_copy
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_data
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_dispose
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_empty
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_erase
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find__const_std___cxx11_basic_string_and_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_not_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_not_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_first_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_not_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_not_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_find_last_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_insert__size_t_size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_length
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_max_size
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_size_t_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__size_t_template__CharT_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_new__std_initializer_list_template__CharT_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_assign
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_assign__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_assign__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_assign__template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_plus_equals
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_plus_equals__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_plus_equals__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_op_plus_equals__template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_pop_back
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_push_back
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_replace
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_replace__size_t_size_t_size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_reserve
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_resize
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_resize__size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_rfind
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_rfind__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_shrink_to_fit
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_size
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_size_of
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_substr
import krapper.slice.internal.std___cxx11_basic_string_wchar_t_swap
import platform.posix.size_t
import platform.posix.wchar_t

// BEGIN KRAPPER GEN for std::__cxx11::basic_string<wchar_t>

@krapper.CppBinding("std::__cxx11::basic_string<wchar_t>")
class Basic_string__Wchar_t(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline infix fun assign(__str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_assign(ptr, __str.ptr) ?: return null), memScope)
    }
    inline infix fun _assign(__s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_assign__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline infix fun __assign(__c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_assign__template__CharT(ptr, __c) ?: return null), memScope)
    }
    inline infix fun ___assign(__l: Initializer_list__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_assign__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun size(): size_t {
        return std___cxx11_basic_string_wchar_t_size(ptr)
    }
    inline fun length(): size_t {
        return std___cxx11_basic_string_wchar_t_length(ptr)
    }
    inline fun max_size(): size_t {
        return std___cxx11_basic_string_wchar_t_max_size(ptr)
    }
    inline fun resize__size_t_wchar_t(__n: size_t, __c: wchar_t): Unit {
        return std___cxx11_basic_string_wchar_t_resize(ptr, __n, __c)
    }
    inline fun resize(__n: size_t): Unit {
        return std___cxx11_basic_string_wchar_t_resize__size_t(ptr, __n)
    }
    inline fun shrink_to_fit(): Unit {
        return std___cxx11_basic_string_wchar_t_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std___cxx11_basic_string_wchar_t_capacity(ptr)
    }
    inline fun reserve(__res_arg: size_t): Unit {
        return std___cxx11_basic_string_wchar_t_reserve(ptr, __res_arg)
    }
    inline fun clear(): Unit {
        return std___cxx11_basic_string_wchar_t_clear(ptr)
    }
    inline fun empty(): Boolean {
        return std___cxx11_basic_string_wchar_t_empty(ptr)
    }
    inline infix fun plusEquals(__str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_plus_equals(ptr, __str.ptr) ?: return null), memScope)
    }
    inline infix fun _plusEquals(__s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_plus_equals__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline infix fun __plusEquals(__c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_plus_equals__template__CharT(ptr, __c) ?: return null), memScope)
    }
    inline infix fun ___plusEquals(__l: Initializer_list__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_op_plus_equals__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun append(__str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append(ptr, __str.ptr) ?: return null), memScope)
    }
    inline fun append__const_std___cxx11_basic_string_wchar_t_size_t_size_t(__str: Basic_string__Wchar_t, __pos: size_t, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append__const_std___cxx11_basic_string_and_size_t_size_t(ptr, __str.ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun append__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append__const_template__CharT_P_size_t(ptr, __s, __n) ?: return null), memScope)
    }
    inline fun append__const_wchar_t_P(__s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline fun append__size_t_wchar_t(__n: size_t, __c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append__size_t_template__CharT(ptr, __n, __c) ?: return null), memScope)
    }
    inline fun append__std_initializer_list_wchar_t(__l: Initializer_list__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_append__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun push_back(__c: wchar_t): Unit {
        return std___cxx11_basic_string_wchar_t_push_back(ptr, __c)
    }
    inline fun assign_method(__str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign(ptr, __str.ptr) ?: return null), memScope)
    }
    inline fun assign_method__const_std___cxx11_basic_string_wchar_t_size_t_size_t(__str: Basic_string__Wchar_t, __pos: size_t, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign__const_std___cxx11_basic_string_and_size_t_size_t(ptr, __str.ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun assign_method__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P_size_t(ptr, __s, __n) ?: return null), memScope)
    }
    inline fun assign_method__const_wchar_t_P(__s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline fun assign_method__size_t_wchar_t(__n: size_t, __c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign__size_t_template__CharT(ptr, __n, __c) ?: return null), memScope)
    }
    inline fun assign_method__std_initializer_list_wchar_t(__l: Initializer_list__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_assign__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun insert(__pos1: size_t, __str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and(ptr, __pos1, __str.ptr) ?: return null), memScope)
    }
    inline fun insert__size_t_const_std___cxx11_basic_string_wchar_t_size_t_size_t(__pos1: size_t, __str: Basic_string__Wchar_t, __pos2: size_t, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __str.ptr, __pos2, __n) ?: return null), memScope)
    }
    inline fun insert__size_t_const_wchar_t_P_size_t(__pos: size_t, __s: CValuesRef<IntVar>?, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P_size_t(ptr, __pos, __s, __n) ?: return null), memScope)
    }
    inline fun insert__size_t_const_wchar_t_P(__pos: size_t, __s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_insert__size_t_const_template__CharT_P(ptr, __pos, __s) ?: return null), memScope)
    }
    inline fun insert__size_t_size_t_wchar_t(__pos: size_t, __n: size_t, __c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_insert__size_t_size_t_template__CharT(ptr, __pos, __n, __c) ?: return null), memScope)
    }
    inline fun erase(__pos: size_t, __n: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_erase(ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun pop_back(): Unit {
        return std___cxx11_basic_string_wchar_t_pop_back(ptr)
    }
    inline fun replace(__pos: size_t, __n: size_t, __str: Basic_string__Wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_replace(ptr, __pos, __n, __str.ptr) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_const_std___cxx11_basic_string_wchar_t_size_t_size_t(__pos1: size_t, __n1: size_t, __str: Basic_string__Wchar_t, __pos2: size_t, __n2: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __n1, __str.ptr, __pos2, __n2) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_const_wchar_t_P_size_t(__pos: size_t, __n1: size_t, __s: CValuesRef<IntVar>?, __n2: size_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P_size_t(ptr, __pos, __n1, __s, __n2) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_const_wchar_t_P(__pos: size_t, __n1: size_t, __s: CValuesRef<IntVar>?): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_replace__size_t_size_t_const_template__CharT_P(ptr, __pos, __n1, __s) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_size_t_wchar_t(__pos: size_t, __n1: size_t, __n2: size_t, __c: wchar_t): Basic_string__Wchar_t? {
        return Basic_string__Wchar_t((std___cxx11_basic_string_wchar_t_replace__size_t_size_t_size_t_template__CharT(ptr, __pos, __n1, __n2, __c) ?: return null), memScope)
    }
    inline fun copy(__s: CValuesRef<IntVar>?, __n: size_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_copy(ptr, __s, __n, __pos)
    }
    inline fun swap(__s: Basic_string__Wchar_t): Unit {
        return std___cxx11_basic_string_wchar_t_swap(ptr, __s.ptr)
    }
    inline fun c_str(): CValuesRef<IntVar>? {
        return std___cxx11_basic_string_wchar_t_c_str(ptr)
    }
    inline fun data(): CValuesRef<IntVar>? {
        return std___cxx11_basic_string_wchar_t_data(ptr)
    }
    inline fun find__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find(ptr, __s, __pos, __n)
    }
    inline fun find(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find__const_std___cxx11_basic_string_and_size_t(ptr, __str.ptr, __pos)
    }
    inline fun find__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun rfind(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_rfind(ptr, __str.ptr, __pos)
    }
    inline fun rfind__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun rfind__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_rfind__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun rfind__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_rfind__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_first_of(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_of(ptr, __str.ptr, __pos)
    }
    inline fun find_first_of__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_first_of__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_first_of__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_last_of(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_of(ptr, __str.ptr, __pos)
    }
    inline fun find_last_of__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_last_of__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_last_of__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_first_not_of(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_not_of(ptr, __str.ptr, __pos)
    }
    inline fun find_first_not_of__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_first_not_of__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_not_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_first_not_of__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_first_not_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_last_not_of(__str: Basic_string__Wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_not_of(ptr, __str.ptr, __pos)
    }
    inline fun find_last_not_of__const_wchar_t_P_size_t_size_t(__s: CValuesRef<IntVar>?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_last_not_of__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_not_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_last_not_of__wchar_t_size_t(__c: wchar_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_wchar_t_find_last_not_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun substr(__pos: size_t, __n: size_t): Basic_string__Wchar_t {
        val retValue: Basic_string__Wchar_t = memScope.Basic_string__Wchar_t_Holder()
        std___cxx11_basic_string_wchar_t_substr(ptr, __pos, __n, retValue.ptr)
        return retValue
    }
    inline fun compare(__str: Basic_string__Wchar_t): Int {
        return std___cxx11_basic_string_wchar_t_compare(ptr, __str.ptr)
    }
    inline fun compare__size_t_size_t_const_std___cxx11_basic_string_wchar_t(__pos: size_t, __n: size_t, __str: Basic_string__Wchar_t): Int {
        return std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and(ptr, __pos, __n, __str.ptr)
    }
    inline fun compare__size_t_size_t_const_std___cxx11_basic_string_wchar_t_size_t_size_t(__pos1: size_t, __n1: size_t, __str: Basic_string__Wchar_t, __pos2: size_t, __n2: size_t): Int {
        return std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __n1, __str.ptr, __pos2, __n2)
    }
    inline fun compare__const_wchar_t_P(__s: CValuesRef<IntVar>?): Int {
        return std___cxx11_basic_string_wchar_t_compare__const_template__CharT_P(ptr, __s)
    }
    inline fun compare__size_t_size_t_const_wchar_t_P(__pos: size_t, __n1: size_t, __s: CValuesRef<IntVar>?): Int {
        return std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P(ptr, __pos, __n1, __s)
    }
    inline fun compare__size_t_size_t_const_wchar_t_P_size_t(__pos: size_t, __n1: size_t, __s: CValuesRef<IntVar>?, __n2: size_t): Int {
        return std___cxx11_basic_string_wchar_t_compare__size_t_size_t_const_template__CharT_P_size_t(ptr, __pos, __n1, __s, __n2)
    }
    companion object {
        val _size: Int
            inline get() {
                return std___cxx11_basic_string_wchar_t_size_of()
            }

        val align: Int
            inline get() {
                return std___cxx11_basic_string_wchar_t_align_of()
            }

        fun MemScope.Basic_string__Wchar_t(): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new(memory) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__const_std___cxx11_basic_string_wchar_t(__str: Basic_string__Wchar_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and(memory, __str.ptr) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__const_std___cxx11_basic_string_wchar_t_size_t(__str: Basic_string__Wchar_t, __pos: size_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(memory, __str.ptr, __pos) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__const_std___cxx11_basic_string_wchar_t_size_t_size_t(__str: Basic_string__Wchar_t, __pos: size_t, __n: size_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__const_std___cxx11_basic_string_and_size_t_size_t(memory, __str.ptr, __pos, __n) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__const_wchar_t_P_size_t(__s: CValuesRef<IntVar>?, __n: size_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_size_t_const_template__Alloc_and(memory, __s, __n) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__const_wchar_t_P(__s: CValuesRef<IntVar>?): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__const_template__CharT_P_const_template__Alloc_and(memory, __s) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__size_t_wchar_t(__n: size_t, __c: wchar_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__size_t_template__CharT_const_template__Alloc_and(memory, __n, __c) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t__std_initializer_list_wchar_t(__l: Initializer_list__Wchar_t): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_wchar_t_new__std_initializer_list_template__CharT_const_template__Alloc_and(memory, __l.ptr) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(obj)
            }
            return Basic_string__Wchar_t(obj, this)
        }
        fun MemScope.Basic_string__Wchar_t_Holder(): Basic_string__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std___cxx11_basic_string_wchar_t_dispose(memory)
            }
            return Basic_string__Wchar_t(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std___cxx11_basic_string_wchar_t_dispose(ptr)
    }
    inline fun owned(): Basic_string__Wchar_t {
        memScope.defer {
            std___cxx11_basic_string_wchar_t_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::__cxx11::basic_string<wchar_t>


