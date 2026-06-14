package com.monkopedia.slice.std.__cxx11

import com.monkopedia.slice.std.Initializer_list__Char
import com.monkopedia.slice.std.__cxx11.Basic_string__Char.Companion.Basic_string__Char_Holder
import kotlin.Boolean
import kotlin.Byte
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.slice.internal.std___cxx11_basic_string_char_align_of
import krapper.slice.internal.std___cxx11_basic_string_char_append
import krapper.slice.internal.std___cxx11_basic_string_char_append__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_append__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_append__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_append__size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_append__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_assign
import krapper.slice.internal.std___cxx11_basic_string_char_assign__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_assign__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_assign__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_assign__size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_assign__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_c_str
import krapper.slice.internal.std___cxx11_basic_string_char_capacity
import krapper.slice.internal.std___cxx11_basic_string_char_clear
import krapper.slice.internal.std___cxx11_basic_string_char_compare
import krapper.slice.internal.std___cxx11_basic_string_char_compare__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_copy
import krapper.slice.internal.std___cxx11_basic_string_char_data
import krapper.slice.internal.std___cxx11_basic_string_char_dispose
import krapper.slice.internal.std___cxx11_basic_string_char_empty
import krapper.slice.internal.std___cxx11_basic_string_char_erase
import krapper.slice.internal.std___cxx11_basic_string_char_find
import krapper.slice.internal.std___cxx11_basic_string_char_find__const_std___cxx11_basic_string_and_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_not_of
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_not_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_of
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_first_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_not_of
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_not_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_of
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_find_last_of__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_insert__size_t_size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_length
import krapper.slice.internal.std___cxx11_basic_string_char_max_size
import krapper.slice.internal.std___cxx11_basic_string_char_new
import krapper.slice.internal.std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and
import krapper.slice.internal.std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_new__const_template__CharT_P_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_char_new__const_template__CharT_P_size_t_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_char_new__size_t_template__CharT_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_char_new__std_initializer_list_template__CharT_const_template__Alloc_and
import krapper.slice.internal.std___cxx11_basic_string_char_op_assign
import krapper.slice.internal.std___cxx11_basic_string_char_op_assign__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_op_assign__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_op_assign__template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_op_plus_equals
import krapper.slice.internal.std___cxx11_basic_string_char_op_plus_equals__const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_op_plus_equals__std_initializer_list_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_op_plus_equals__template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_pop_back
import krapper.slice.internal.std___cxx11_basic_string_char_push_back
import krapper.slice.internal.std___cxx11_basic_string_char_replace
import krapper.slice.internal.std___cxx11_basic_string_char_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P
import krapper.slice.internal.std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_replace__size_t_size_t_size_t_template__CharT
import krapper.slice.internal.std___cxx11_basic_string_char_reserve
import krapper.slice.internal.std___cxx11_basic_string_char_resize
import krapper.slice.internal.std___cxx11_basic_string_char_resize__size_t
import krapper.slice.internal.std___cxx11_basic_string_char_rfind
import krapper.slice.internal.std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_rfind__template__CharT_size_t
import krapper.slice.internal.std___cxx11_basic_string_char_shrink_to_fit
import krapper.slice.internal.std___cxx11_basic_string_char_size
import krapper.slice.internal.std___cxx11_basic_string_char_size_of
import krapper.slice.internal.std___cxx11_basic_string_char_substr
import krapper.slice.internal.std___cxx11_basic_string_char_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::__cxx11::basic_string<char>

@krapper.CppBinding("std::__cxx11::basic_string<char>")
class Basic_string__Char(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline infix fun assign(__str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_assign(ptr, __str.ptr) ?: return null), memScope)
    }
    inline infix fun _assign(__s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_assign__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline infix fun __assign(__c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_assign__template__CharT(ptr, __c) ?: return null), memScope)
    }
    inline infix fun ___assign(__l: Initializer_list__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_assign__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun size(): size_t {
        return std___cxx11_basic_string_char_size(ptr)
    }
    inline fun length(): size_t {
        return std___cxx11_basic_string_char_length(ptr)
    }
    inline fun max_size(): size_t {
        return std___cxx11_basic_string_char_max_size(ptr)
    }
    inline fun resize__size_t_char(__n: size_t, __c: Byte): Unit {
        return std___cxx11_basic_string_char_resize(ptr, __n, __c)
    }
    inline fun resize(__n: size_t): Unit {
        return std___cxx11_basic_string_char_resize__size_t(ptr, __n)
    }
    inline fun shrink_to_fit(): Unit {
        return std___cxx11_basic_string_char_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std___cxx11_basic_string_char_capacity(ptr)
    }
    inline fun reserve(__res_arg: size_t): Unit {
        return std___cxx11_basic_string_char_reserve(ptr, __res_arg)
    }
    inline fun clear(): Unit {
        return std___cxx11_basic_string_char_clear(ptr)
    }
    inline fun empty(): Boolean {
        return std___cxx11_basic_string_char_empty(ptr)
    }
    inline infix fun plusEquals(__str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_plus_equals(ptr, __str.ptr) ?: return null), memScope)
    }
    inline infix fun _plusEquals(__s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_plus_equals__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline infix fun __plusEquals(__c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_plus_equals__template__CharT(ptr, __c) ?: return null), memScope)
    }
    inline infix fun ___plusEquals(__l: Initializer_list__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_op_plus_equals__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun append__const_std___cxx11_basic_string_char(__str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append(ptr, __str.ptr) ?: return null), memScope)
    }
    inline fun append__const_std___cxx11_basic_string_char_size_t_size_t(__str: Basic_string__Char, __pos: size_t, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append__const_std___cxx11_basic_string_and_size_t_size_t(ptr, __str.ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun append__const_char_P_size_t(__s: String?, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append__const_template__CharT_P_size_t(ptr, __s, __n) ?: return null), memScope)
    }
    inline fun append(__s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline fun append__size_t_char(__n: size_t, __c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append__size_t_template__CharT(ptr, __n, __c) ?: return null), memScope)
    }
    inline fun append__std_initializer_list_char(__l: Initializer_list__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_append__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun push_back(__c: Byte): Unit {
        return std___cxx11_basic_string_char_push_back(ptr, __c)
    }
    inline fun assign_method__const_std___cxx11_basic_string_char(__str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign(ptr, __str.ptr) ?: return null), memScope)
    }
    inline fun assign_method__const_std___cxx11_basic_string_char_size_t_size_t(__str: Basic_string__Char, __pos: size_t, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign__const_std___cxx11_basic_string_and_size_t_size_t(ptr, __str.ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun assign_method__const_char_P_size_t(__s: String?, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign__const_template__CharT_P_size_t(ptr, __s, __n) ?: return null), memScope)
    }
    inline fun assign_method(__s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign__const_template__CharT_P(ptr, __s) ?: return null), memScope)
    }
    inline fun assign_method__size_t_char(__n: size_t, __c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign__size_t_template__CharT(ptr, __n, __c) ?: return null), memScope)
    }
    inline fun assign_method__std_initializer_list_char(__l: Initializer_list__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_assign__std_initializer_list_template__CharT(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun insert__size_t_const_std___cxx11_basic_string_char(__pos1: size_t, __str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and(ptr, __pos1, __str.ptr) ?: return null), memScope)
    }
    inline fun insert__size_t_const_std___cxx11_basic_string_char_size_t_size_t(__pos1: size_t, __str: Basic_string__Char, __pos2: size_t, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_insert__size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __str.ptr, __pos2, __n) ?: return null), memScope)
    }
    inline fun insert__size_t_const_char_P_size_t(__pos: size_t, __s: String?, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P_size_t(ptr, __pos, __s, __n) ?: return null), memScope)
    }
    inline fun insert(__pos: size_t, __s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_insert__size_t_const_template__CharT_P(ptr, __pos, __s) ?: return null), memScope)
    }
    inline fun insert__size_t_size_t_char(__pos: size_t, __n: size_t, __c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_insert__size_t_size_t_template__CharT(ptr, __pos, __n, __c) ?: return null), memScope)
    }
    inline fun erase(__pos: size_t, __n: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_erase(ptr, __pos, __n) ?: return null), memScope)
    }
    inline fun pop_back(): Unit {
        return std___cxx11_basic_string_char_pop_back(ptr)
    }
    inline fun replace__size_t_size_t_const_std___cxx11_basic_string_char(__pos: size_t, __n: size_t, __str: Basic_string__Char): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_replace(ptr, __pos, __n, __str.ptr) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_const_std___cxx11_basic_string_char_size_t_size_t(__pos1: size_t, __n1: size_t, __str: Basic_string__Char, __pos2: size_t, __n2: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_replace__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __n1, __str.ptr, __pos2, __n2) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_const_char_P_size_t(__pos: size_t, __n1: size_t, __s: String?, __n2: size_t): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P_size_t(ptr, __pos, __n1, __s, __n2) ?: return null), memScope)
    }
    inline fun replace(__pos: size_t, __n1: size_t, __s: String?): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_replace__size_t_size_t_const_template__CharT_P(ptr, __pos, __n1, __s) ?: return null), memScope)
    }
    inline fun replace__size_t_size_t_size_t_char(__pos: size_t, __n1: size_t, __n2: size_t, __c: Byte): Basic_string__Char? {
        return Basic_string__Char((std___cxx11_basic_string_char_replace__size_t_size_t_size_t_template__CharT(ptr, __pos, __n1, __n2, __c) ?: return null), memScope)
    }
    inline fun copy(__s: CValuesRef<ByteVar>?, __n: size_t, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_copy(ptr, __s, __n, __pos)
    }
    inline fun swap(__s: Basic_string__Char): Unit {
        return std___cxx11_basic_string_char_swap(ptr, __s.ptr)
    }
    inline fun c_str(): String? {
        val str: CPointer<ByteVar>? = std___cxx11_basic_string_char_c_str(ptr)
        val ret: String? = str?.toKString()
        return ret
    }
    inline fun data(): String? {
        val str: CPointer<ByteVar>? = std___cxx11_basic_string_char_data(ptr)
        val ret: String? = str?.toKString()
        return ret
    }
    inline fun find__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_find(ptr, __s, __pos, __n)
    }
    inline fun find__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find__const_std___cxx11_basic_string_and_size_t(ptr, __str.ptr, __pos)
    }
    inline fun find__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun rfind__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_rfind(ptr, __str.ptr, __pos)
    }
    inline fun rfind__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun rfind__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_rfind__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun rfind(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_rfind__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_first_of__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_of(ptr, __str.ptr, __pos)
    }
    inline fun find_first_of__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_first_of__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_first_of(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_last_of__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_of(ptr, __str.ptr, __pos)
    }
    inline fun find_last_of__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_last_of__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_last_of(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_first_not_of__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_not_of(ptr, __str.ptr, __pos)
    }
    inline fun find_first_not_of__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_first_not_of__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_not_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_first_not_of(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_first_not_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun find_last_not_of__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_not_of(ptr, __str.ptr, __pos)
    }
    inline fun find_last_not_of__const_char_P_size_t_size_t(__s: String?, __pos: size_t, __n: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t_size_t(ptr, __s, __pos, __n)
    }
    inline fun find_last_not_of__const_char_P_size_t(__s: String?, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_not_of__const_template__CharT_P_size_t(ptr, __s, __pos)
    }
    inline fun find_last_not_of(__c: Byte, __pos: size_t): size_t {
        return std___cxx11_basic_string_char_find_last_not_of__template__CharT_size_t(ptr, __c, __pos)
    }
    inline fun substr(__pos: size_t, __n: size_t): Basic_string__Char {
        val retValue: Basic_string__Char = memScope.Basic_string__Char_Holder()
        std___cxx11_basic_string_char_substr(ptr, __pos, __n, retValue.ptr)
        return retValue
    }
    inline fun compare__const_std___cxx11_basic_string_char(__str: Basic_string__Char): Int {
        return std___cxx11_basic_string_char_compare(ptr, __str.ptr)
    }
    inline fun compare__size_t_size_t_const_std___cxx11_basic_string_char(__pos: size_t, __n: size_t, __str: Basic_string__Char): Int {
        return std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and(ptr, __pos, __n, __str.ptr)
    }
    inline fun compare__size_t_size_t_const_std___cxx11_basic_string_char_size_t_size_t(__pos1: size_t, __n1: size_t, __str: Basic_string__Char, __pos2: size_t, __n2: size_t): Int {
        return std___cxx11_basic_string_char_compare__size_t_size_t_const_std___cxx11_basic_string_and_size_t_size_t(ptr, __pos1, __n1, __str.ptr, __pos2, __n2)
    }
    inline fun compare(__s: String?): Int {
        return std___cxx11_basic_string_char_compare__const_template__CharT_P(ptr, __s)
    }
    inline fun compare__size_t_size_t_const_char_P(__pos: size_t, __n1: size_t, __s: String?): Int {
        return std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P(ptr, __pos, __n1, __s)
    }
    inline fun compare__size_t_size_t_const_char_P_size_t(__pos: size_t, __n1: size_t, __s: String?, __n2: size_t): Int {
        return std___cxx11_basic_string_char_compare__size_t_size_t_const_template__CharT_P_size_t(ptr, __pos, __n1, __s, __n2)
    }
    companion object {
        val _size: Int
            inline get() {
                return std___cxx11_basic_string_char_size_of()
            }

        val align: Int
            inline get() {
                return std___cxx11_basic_string_char_align_of()
            }

        fun MemScope.Basic_string__Char(): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new(memory) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__const_std___cxx11_basic_string_char(__str: Basic_string__Char): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and(memory, __str.ptr) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__const_std___cxx11_basic_string_char_size_t(__str: Basic_string__Char, __pos: size_t): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_const_template__Alloc_and(memory, __str.ptr, __pos) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__const_std___cxx11_basic_string_char_size_t_size_t(__str: Basic_string__Char, __pos: size_t, __n: size_t): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__const_std___cxx11_basic_string_and_size_t_size_t(memory, __str.ptr, __pos, __n) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__const_char_P_size_t(__s: String?, __n: size_t): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__const_template__CharT_P_size_t_const_template__Alloc_and(memory, __s, __n) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__const_char_P(__s: String?): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__const_template__CharT_P_const_template__Alloc_and(memory, __s) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__size_t_char(__n: size_t, __c: Byte): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__size_t_template__CharT_const_template__Alloc_and(memory, __n, __c) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char__std_initializer_list_char(__l: Initializer_list__Char): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std___cxx11_basic_string_char_new__std_initializer_list_template__CharT_const_template__Alloc_and(memory, __l.ptr) ?: error("Creation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(obj)
            }
            return Basic_string__Char(obj, this)
        }
        fun MemScope.Basic_string__Char_Holder(): Basic_string__Char {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std___cxx11_basic_string_char_dispose(memory)
            }
            return Basic_string__Char(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std___cxx11_basic_string_char_dispose(ptr)
    }
    inline fun owned(): Basic_string__Char {
        memScope.defer {
            std___cxx11_basic_string_char_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::__cxx11::basic_string<char>


