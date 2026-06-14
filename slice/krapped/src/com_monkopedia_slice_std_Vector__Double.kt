package com.monkopedia.slice.std

import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_vector_double_align_of
import krapper.slice.internal.std_vector_double_assign
import krapper.slice.internal.std_vector_double_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_double_at
import krapper.slice.internal.std_vector_double_back
import krapper.slice.internal.std_vector_double_capacity
import krapper.slice.internal.std_vector_double_clear
import krapper.slice.internal.std_vector_double_data
import krapper.slice.internal.std_vector_double_dispose
import krapper.slice.internal.std_vector_double_empty
import krapper.slice.internal.std_vector_double_front
import krapper.slice.internal.std_vector_double_max_size
import krapper.slice.internal.std_vector_double_new
import krapper.slice.internal.std_vector_double_new__const_std_vector_and
import krapper.slice.internal.std_vector_double_new__size_t_const_template__Alloc_and
import krapper.slice.internal.std_vector_double_new__size_t_const_template__Tp_and_const_template__Alloc_and
import krapper.slice.internal.std_vector_double_new__std_initializer_list_template__Tp_const_template__Alloc_and
import krapper.slice.internal.std_vector_double_op_assign
import krapper.slice.internal.std_vector_double_op_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_double_op_ind
import krapper.slice.internal.std_vector_double_pop_back
import krapper.slice.internal.std_vector_double_push_back
import krapper.slice.internal.std_vector_double_reserve
import krapper.slice.internal.std_vector_double_resize
import krapper.slice.internal.std_vector_double_resize__size_t_const_template__Tp_and
import krapper.slice.internal.std_vector_double_shrink_to_fit
import krapper.slice.internal.std_vector_double_size
import krapper.slice.internal.std_vector_double_size_of
import krapper.slice.internal.std_vector_double_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::vector<double>

@krapper.CppBinding("std::vector<double>")
class Vector__Double(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.collections.Iterable<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.DoubleVar>?> {
    inline infix fun assign(__x: Vector__Double): Vector__Double? {
        return Vector__Double((std_vector_double_op_assign(ptr, __x.ptr) ?: return null), memScope)
    }
    inline infix fun _assign(__l: Initializer_list__Double): Vector__Double? {
        return Vector__Double((std_vector_double_op_assign__std_initializer_list_template__Tp(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun assign_method(__n: size_t, __val: Double): Unit {
        return std_vector_double_assign(ptr, __n, __val)
    }
    inline fun assign_method__std_initializer_list_double(__l: Initializer_list__Double): Unit {
        return std_vector_double_assign__std_initializer_list_template__Tp(ptr, __l.ptr)
    }
    inline fun size(): size_t {
        return std_vector_double_size(ptr)
    }
    inline fun max_size(): size_t {
        return std_vector_double_max_size(ptr)
    }
    inline fun resize(__new_size: size_t): Unit {
        return std_vector_double_resize(ptr, __new_size)
    }
    inline fun resize__size_t_const_double(__new_size: size_t, __x: Double): Unit {
        return std_vector_double_resize__size_t_const_template__Tp_and(ptr, __new_size, __x)
    }
    inline fun shrink_to_fit(): Unit {
        return std_vector_double_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std_vector_double_capacity(ptr)
    }
    inline fun empty(): Boolean {
        return std_vector_double_empty(ptr)
    }
    inline fun reserve(__n: size_t): Unit {
        return std_vector_double_reserve(ptr, __n)
    }
    inline operator fun get(__n: size_t): CValuesRef<DoubleVar>? {
        return std_vector_double_op_ind(ptr, __n)
    }
    inline fun at(__n: size_t): CValuesRef<DoubleVar>? {
        return std_vector_double_at(ptr, __n)
    }
    inline fun front(): CValuesRef<DoubleVar>? {
        return std_vector_double_front(ptr)
    }
    inline fun back(): CValuesRef<DoubleVar>? {
        return std_vector_double_back(ptr)
    }
    inline fun data(): CValuesRef<DoubleVar>? {
        return std_vector_double_data(ptr)
    }
    inline fun push_back(__x: Double): Unit {
        return std_vector_double_push_back(ptr, __x)
    }
    inline fun pop_back(): Unit {
        return std_vector_double_pop_back(ptr)
    }
    inline fun swap(__x: Vector__Double): Unit {
        return std_vector_double_swap(ptr, __x.ptr)
    }
    inline fun clear(): Unit {
        return std_vector_double_clear(ptr)
    }
    override operator fun iterator(): kotlin.collections.Iterator<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.DoubleVar>?> =
        object : kotlin.collections.Iterator<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.DoubleVar>?> {
            private var __i = (size() - size())
            override fun hasNext(): Boolean = __i < size()
            override fun next(): kotlinx.cinterop.CValuesRef<kotlinx.cinterop.DoubleVar>? = get(__i++)
        }
    companion object {
        val _size: Int
            inline get() {
                return std_vector_double_size_of()
            }

        val align: Int
            inline get() {
                return std_vector_double_align_of()
            }

        fun MemScope.Vector__Double(): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_double_new(memory) ?: error("Creation failed"))
            defer {
                std_vector_double_dispose(obj)
            }
            return Vector__Double(obj, this)
        }
        fun MemScope.Vector__Double__size_t(__n: size_t): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_double_new__size_t_const_template__Alloc_and(memory, __n) ?: error("Creation failed"))
            defer {
                std_vector_double_dispose(obj)
            }
            return Vector__Double(obj, this)
        }
        fun MemScope.Vector__Double__size_t_const_double(__n: size_t, __value: Double): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_double_new__size_t_const_template__Tp_and_const_template__Alloc_and(memory, __n, __value) ?: error("Creation failed"))
            defer {
                std_vector_double_dispose(obj)
            }
            return Vector__Double(obj, this)
        }
        fun MemScope.Vector__Double__const_std_vector_double(__x: Vector__Double): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_double_new__const_std_vector_and(memory, __x.ptr) ?: error("Creation failed"))
            defer {
                std_vector_double_dispose(obj)
            }
            return Vector__Double(obj, this)
        }
        fun MemScope.Vector__Double__std_initializer_list_double(__l: Initializer_list__Double): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_double_new__std_initializer_list_template__Tp_const_template__Alloc_and(memory, __l.ptr) ?: error("Creation failed"))
            defer {
                std_vector_double_dispose(obj)
            }
            return Vector__Double(obj, this)
        }
        fun MemScope.Vector__Double_Holder(): Vector__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std_vector_double_dispose(memory)
            }
            return Vector__Double(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std_vector_double_dispose(ptr)
    }
    inline fun owned(): Vector__Double {
        memScope.defer {
            std_vector_double_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::vector<double>


