package com.monkopedia.slice.std

import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_map_int___int_align_of
import krapper.slice.internal.std_map_int___int_at
import krapper.slice.internal.std_map_int___int_clear
import krapper.slice.internal.std_map_int___int_count
import krapper.slice.internal.std_map_int___int_dispose
import krapper.slice.internal.std_map_int___int_empty
import krapper.slice.internal.std_map_int___int_erase
import krapper.slice.internal.std_map_int___int_max_size
import krapper.slice.internal.std_map_int___int_new
import krapper.slice.internal.std_map_int___int_new__const_std_map_and
import krapper.slice.internal.std_map_int___int_op_assign
import krapper.slice.internal.std_map_int___int_op_ind
import krapper.slice.internal.std_map_int___int_size
import krapper.slice.internal.std_map_int___int_size_of
import krapper.slice.internal.std_map_int___int_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::map<int, int>

@krapper.CppBinding("std::map<int, int>")
class Map__Int__Int(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline infix fun assign(_arg_0: Map__Int__Int): Map__Int__Int? {
        return Map__Int__Int((std_map_int___int_op_assign(ptr, _arg_0.ptr) ?: return null), memScope)
    }
    inline fun empty(): Boolean {
        return std_map_int___int_empty(ptr)
    }
    inline fun size(): size_t {
        return std_map_int___int_size(ptr)
    }
    inline fun max_size(): size_t {
        return std_map_int___int_max_size(ptr)
    }
    inline operator fun get(__k: Int): CValuesRef<IntVar>? {
        return std_map_int___int_op_ind(ptr, __k)
    }
    inline fun at(__k: Int): CValuesRef<IntVar>? {
        return std_map_int___int_at(ptr, __k)
    }
    inline fun erase(__x: Int): size_t {
        return std_map_int___int_erase(ptr, __x)
    }
    inline fun swap(__x: Map__Int__Int): Unit {
        return std_map_int___int_swap(ptr, __x.ptr)
    }
    inline fun clear(): Unit {
        return std_map_int___int_clear(ptr)
    }
    inline fun count(__x: Int): size_t {
        return std_map_int___int_count(ptr, __x)
    }
    companion object {
        val _size: Int
            inline get() {
                return std_map_int___int_size_of()
            }

        val align: Int
            inline get() {
                return std_map_int___int_align_of()
            }

        fun MemScope.Map__Int__Int(): Map__Int__Int {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_map_int___int_new(memory) ?: error("Creation failed"))
            defer {
                std_map_int___int_dispose(obj)
            }
            return Map__Int__Int(obj, this)
        }
        fun MemScope.Map__Int__Int__const_std_map_int___int(_arg_0: Map__Int__Int): Map__Int__Int {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_map_int___int_new__const_std_map_and(memory, _arg_0.ptr) ?: error("Creation failed"))
            defer {
                std_map_int___int_dispose(obj)
            }
            return Map__Int__Int(obj, this)
        }
        fun MemScope.Map__Int__Int_Holder(): Map__Int__Int {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std_map_int___int_dispose(memory)
            }
            return Map__Int__Int(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std_map_int___int_dispose(ptr)
    }
    inline fun owned(): Map__Int__Int {
        memScope.defer {
            std_map_int___int_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::map<int, int>


