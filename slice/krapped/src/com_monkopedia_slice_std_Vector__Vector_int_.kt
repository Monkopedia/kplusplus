package com.monkopedia.slice.std

import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_vector_std_vector_int_align_of
import krapper.slice.internal.std_vector_std_vector_int_assign
import krapper.slice.internal.std_vector_std_vector_int_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_std_vector_int_at
import krapper.slice.internal.std_vector_std_vector_int_back
import krapper.slice.internal.std_vector_std_vector_int_capacity
import krapper.slice.internal.std_vector_std_vector_int_clear
import krapper.slice.internal.std_vector_std_vector_int_data
import krapper.slice.internal.std_vector_std_vector_int_dispose
import krapper.slice.internal.std_vector_std_vector_int_empty
import krapper.slice.internal.std_vector_std_vector_int_front
import krapper.slice.internal.std_vector_std_vector_int_max_size
import krapper.slice.internal.std_vector_std_vector_int_new
import krapper.slice.internal.std_vector_std_vector_int_new__const_std_vector_and
import krapper.slice.internal.std_vector_std_vector_int_new__size_t_const_template__Alloc_and
import krapper.slice.internal.std_vector_std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and
import krapper.slice.internal.std_vector_std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and
import krapper.slice.internal.std_vector_std_vector_int_op_assign
import krapper.slice.internal.std_vector_std_vector_int_op_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_std_vector_int_op_ind
import krapper.slice.internal.std_vector_std_vector_int_pop_back
import krapper.slice.internal.std_vector_std_vector_int_push_back
import krapper.slice.internal.std_vector_std_vector_int_reserve
import krapper.slice.internal.std_vector_std_vector_int_resize
import krapper.slice.internal.std_vector_std_vector_int_resize__size_t_const_template__Tp_and
import krapper.slice.internal.std_vector_std_vector_int_shrink_to_fit
import krapper.slice.internal.std_vector_std_vector_int_size
import krapper.slice.internal.std_vector_std_vector_int_size_of
import krapper.slice.internal.std_vector_std_vector_int_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::vector<std::vector<int>>

@krapper.CppBinding("std::vector<std::vector<int>>")
class Vector__Vector_int_(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.collections.Iterable<com.monkopedia.slice.std.Vector__Int?> {
    inline infix fun assign(__x: Vector__Vector_int_): Vector__Vector_int_? {
        return Vector__Vector_int_((std_vector_std_vector_int_op_assign(ptr, __x.ptr) ?: return null), memScope)
    }
    inline infix fun _assign(__l: Initializer_list__Vector_int_): Vector__Vector_int_? {
        return Vector__Vector_int_((std_vector_std_vector_int_op_assign__std_initializer_list_template__Tp(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun assign_method(__n: size_t, __val: Vector__Int): Unit {
        return std_vector_std_vector_int_assign(ptr, __n, __val.ptr)
    }
    inline fun assign_method__std_initializer_list_std_vector_int(__l: Initializer_list__Vector_int_): Unit {
        return std_vector_std_vector_int_assign__std_initializer_list_template__Tp(ptr, __l.ptr)
    }
    inline fun size(): size_t {
        return std_vector_std_vector_int_size(ptr)
    }
    inline fun max_size(): size_t {
        return std_vector_std_vector_int_max_size(ptr)
    }
    inline fun resize(__new_size: size_t): Unit {
        return std_vector_std_vector_int_resize(ptr, __new_size)
    }
    inline fun resize__size_t_const_std_vector_int(__new_size: size_t, __x: Vector__Int): Unit {
        return std_vector_std_vector_int_resize__size_t_const_template__Tp_and(ptr, __new_size, __x.ptr)
    }
    inline fun shrink_to_fit(): Unit {
        return std_vector_std_vector_int_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std_vector_std_vector_int_capacity(ptr)
    }
    inline fun empty(): Boolean {
        return std_vector_std_vector_int_empty(ptr)
    }
    inline fun reserve(__n: size_t): Unit {
        return std_vector_std_vector_int_reserve(ptr, __n)
    }
    inline operator fun get(__n: size_t): Vector__Int? {
        return Vector__Int((std_vector_std_vector_int_op_ind(ptr, __n) ?: return null), memScope)
    }
    inline fun at(__n: size_t): Vector__Int? {
        return Vector__Int((std_vector_std_vector_int_at(ptr, __n) ?: return null), memScope)
    }
    inline fun front(): Vector__Int? {
        return Vector__Int((std_vector_std_vector_int_front(ptr) ?: return null), memScope)
    }
    inline fun back(): Vector__Int? {
        return Vector__Int((std_vector_std_vector_int_back(ptr) ?: return null), memScope)
    }
    inline fun data(): Vector__Int? {
        return Vector__Int((std_vector_std_vector_int_data(ptr) ?: return null), memScope)
    }
    inline fun push_back(__x: Vector__Int): Unit {
        return std_vector_std_vector_int_push_back(ptr, __x.ptr)
    }
    inline fun pop_back(): Unit {
        return std_vector_std_vector_int_pop_back(ptr)
    }
    inline fun swap(__x: Vector__Vector_int_): Unit {
        return std_vector_std_vector_int_swap(ptr, __x.ptr)
    }
    inline fun clear(): Unit {
        return std_vector_std_vector_int_clear(ptr)
    }
    override operator fun iterator(): kotlin.collections.Iterator<com.monkopedia.slice.std.Vector__Int?> =
        object : kotlin.collections.Iterator<com.monkopedia.slice.std.Vector__Int?> {
            private var __i = (size() - size())
            override fun hasNext(): Boolean = __i < size()
            override fun next(): com.monkopedia.slice.std.Vector__Int? = get(__i++)
        }
    companion object {
        val _size: Int
            inline get() {
                return std_vector_std_vector_int_size_of()
            }

        val align: Int
            inline get() {
                return std_vector_std_vector_int_align_of()
            }

        fun MemScope.Vector__Vector_int_(): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_std_vector_int_new(memory) ?: error("Creation failed"))
            defer {
                std_vector_std_vector_int_dispose(obj)
            }
            return Vector__Vector_int_(obj, this)
        }
        fun MemScope.Vector__Vector_int___size_t(__n: size_t): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_std_vector_int_new__size_t_const_template__Alloc_and(memory, __n) ?: error("Creation failed"))
            defer {
                std_vector_std_vector_int_dispose(obj)
            }
            return Vector__Vector_int_(obj, this)
        }
        fun MemScope.Vector__Vector_int___size_t_const_std_vector_int(__n: size_t, __value: Vector__Int): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_std_vector_int_new__size_t_const_template__Tp_and_const_template__Alloc_and(memory, __n, __value.ptr) ?: error("Creation failed"))
            defer {
                std_vector_std_vector_int_dispose(obj)
            }
            return Vector__Vector_int_(obj, this)
        }
        fun MemScope.Vector__Vector_int___const_std_vector_std_vector_int(__x: Vector__Vector_int_): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_std_vector_int_new__const_std_vector_and(memory, __x.ptr) ?: error("Creation failed"))
            defer {
                std_vector_std_vector_int_dispose(obj)
            }
            return Vector__Vector_int_(obj, this)
        }
        fun MemScope.Vector__Vector_int___std_initializer_list_std_vector_int(__l: Initializer_list__Vector_int_): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_std_vector_int_new__std_initializer_list_template__Tp_const_template__Alloc_and(memory, __l.ptr) ?: error("Creation failed"))
            defer {
                std_vector_std_vector_int_dispose(obj)
            }
            return Vector__Vector_int_(obj, this)
        }
        fun MemScope.Vector__Vector_int__Holder(): Vector__Vector_int_ {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std_vector_std_vector_int_dispose(memory)
            }
            return Vector__Vector_int_(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std_vector_std_vector_int_dispose(ptr)
    }
    inline fun owned(): Vector__Vector_int_ {
        memScope.defer {
            std_vector_std_vector_int_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::vector<std::vector<int>>


