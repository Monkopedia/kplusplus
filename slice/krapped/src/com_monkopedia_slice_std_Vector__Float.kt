package com.monkopedia.slice.std

import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_vector_float_align_of
import krapper.slice.internal.std_vector_float_assign
import krapper.slice.internal.std_vector_float_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_float_at
import krapper.slice.internal.std_vector_float_back
import krapper.slice.internal.std_vector_float_capacity
import krapper.slice.internal.std_vector_float_clear
import krapper.slice.internal.std_vector_float_data
import krapper.slice.internal.std_vector_float_dispose
import krapper.slice.internal.std_vector_float_empty
import krapper.slice.internal.std_vector_float_front
import krapper.slice.internal.std_vector_float_max_size
import krapper.slice.internal.std_vector_float_new
import krapper.slice.internal.std_vector_float_new__const_std_vector_and
import krapper.slice.internal.std_vector_float_new__size_t_const_template__Alloc_and
import krapper.slice.internal.std_vector_float_new__size_t_const_template__Tp_and_const_template__Alloc_and
import krapper.slice.internal.std_vector_float_new__std_initializer_list_template__Tp_const_template__Alloc_and
import krapper.slice.internal.std_vector_float_op_assign
import krapper.slice.internal.std_vector_float_op_assign__std_initializer_list_template__Tp
import krapper.slice.internal.std_vector_float_op_ind
import krapper.slice.internal.std_vector_float_pop_back
import krapper.slice.internal.std_vector_float_push_back
import krapper.slice.internal.std_vector_float_reserve
import krapper.slice.internal.std_vector_float_resize
import krapper.slice.internal.std_vector_float_resize__size_t_const_template__Tp_and
import krapper.slice.internal.std_vector_float_shrink_to_fit
import krapper.slice.internal.std_vector_float_size
import krapper.slice.internal.std_vector_float_size_of
import krapper.slice.internal.std_vector_float_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::vector<float>

@krapper.CppBinding("std::vector<float>")
class Vector__Float(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.collections.Iterable<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.FloatVar>?> {
    inline infix fun assign(__x: Vector__Float): Vector__Float? {
        return Vector__Float((std_vector_float_op_assign(ptr, __x.ptr) ?: return null), memScope)
    }
    inline infix fun _assign(__l: Initializer_list__Float): Vector__Float? {
        return Vector__Float((std_vector_float_op_assign__std_initializer_list_template__Tp(ptr, __l.ptr) ?: return null), memScope)
    }
    inline fun assign_method(__n: size_t, __val: Float): Unit {
        return std_vector_float_assign(ptr, __n, __val)
    }
    inline fun assign_method__std_initializer_list_float(__l: Initializer_list__Float): Unit {
        return std_vector_float_assign__std_initializer_list_template__Tp(ptr, __l.ptr)
    }
    inline fun size(): size_t {
        return std_vector_float_size(ptr)
    }
    inline fun max_size(): size_t {
        return std_vector_float_max_size(ptr)
    }
    inline fun resize(__new_size: size_t): Unit {
        return std_vector_float_resize(ptr, __new_size)
    }
    inline fun resize__size_t_const_float(__new_size: size_t, __x: Float): Unit {
        return std_vector_float_resize__size_t_const_template__Tp_and(ptr, __new_size, __x)
    }
    inline fun shrink_to_fit(): Unit {
        return std_vector_float_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std_vector_float_capacity(ptr)
    }
    inline fun empty(): Boolean {
        return std_vector_float_empty(ptr)
    }
    inline fun reserve(__n: size_t): Unit {
        return std_vector_float_reserve(ptr, __n)
    }
    inline operator fun get(__n: size_t): CValuesRef<FloatVar>? {
        return std_vector_float_op_ind(ptr, __n)
    }
    inline fun at(__n: size_t): CValuesRef<FloatVar>? {
        return std_vector_float_at(ptr, __n)
    }
    inline fun front(): CValuesRef<FloatVar>? {
        return std_vector_float_front(ptr)
    }
    inline fun back(): CValuesRef<FloatVar>? {
        return std_vector_float_back(ptr)
    }
    inline fun data(): CValuesRef<FloatVar>? {
        return std_vector_float_data(ptr)
    }
    inline fun push_back(__x: Float): Unit {
        return std_vector_float_push_back(ptr, __x)
    }
    inline fun pop_back(): Unit {
        return std_vector_float_pop_back(ptr)
    }
    inline fun swap(__x: Vector__Float): Unit {
        return std_vector_float_swap(ptr, __x.ptr)
    }
    inline fun clear(): Unit {
        return std_vector_float_clear(ptr)
    }
    override operator fun iterator(): kotlin.collections.Iterator<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.FloatVar>?> =
        object : kotlin.collections.Iterator<kotlinx.cinterop.CValuesRef<kotlinx.cinterop.FloatVar>?> {
            private var __i = (size() - size())
            override fun hasNext(): Boolean = __i < size()
            override fun next(): kotlinx.cinterop.CValuesRef<kotlinx.cinterop.FloatVar>? = get(__i++)
        }
    companion object {
        val _size: Int
            inline get() {
                return std_vector_float_size_of()
            }

        val align: Int
            inline get() {
                return std_vector_float_align_of()
            }

        fun MemScope.Vector__Float(): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_float_new(memory) ?: error("Creation failed"))
            defer {
                std_vector_float_dispose(obj)
            }
            return Vector__Float(obj, this)
        }
        fun MemScope.Vector__Float__size_t(__n: size_t): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_float_new__size_t_const_template__Alloc_and(memory, __n) ?: error("Creation failed"))
            defer {
                std_vector_float_dispose(obj)
            }
            return Vector__Float(obj, this)
        }
        fun MemScope.Vector__Float__size_t_const_float(__n: size_t, __value: Float): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_float_new__size_t_const_template__Tp_and_const_template__Alloc_and(memory, __n, __value) ?: error("Creation failed"))
            defer {
                std_vector_float_dispose(obj)
            }
            return Vector__Float(obj, this)
        }
        fun MemScope.Vector__Float__const_std_vector_float(__x: Vector__Float): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_float_new__const_std_vector_and(memory, __x.ptr) ?: error("Creation failed"))
            defer {
                std_vector_float_dispose(obj)
            }
            return Vector__Float(obj, this)
        }
        fun MemScope.Vector__Float__std_initializer_list_float(__l: Initializer_list__Float): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_float_new__std_initializer_list_template__Tp_const_template__Alloc_and(memory, __l.ptr) ?: error("Creation failed"))
            defer {
                std_vector_float_dispose(obj)
            }
            return Vector__Float(obj, this)
        }
        fun MemScope.Vector__Float_Holder(): Vector__Float {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std_vector_float_dispose(memory)
            }
            return Vector__Float(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std_vector_float_dispose(ptr)
    }
    inline fun owned(): Vector__Float {
        memScope.defer {
            std_vector_float_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::vector<float>


