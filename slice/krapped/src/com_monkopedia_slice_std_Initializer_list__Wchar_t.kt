package com.monkopedia.slice.std

import kotlin.Int
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_initializer_list_wchar_t_align_of
import krapper.slice.internal.std_initializer_list_wchar_t_new
import krapper.slice.internal.std_initializer_list_wchar_t_size
import krapper.slice.internal.std_initializer_list_wchar_t_size_of
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::initializer_list<wchar_t>

@krapper.CppBinding("std::initializer_list<wchar_t>")
class Initializer_list__Wchar_t(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun size(): size_t {
        return std_initializer_list_wchar_t_size(ptr)
    }
    companion object {
        val _size: Int
            inline get() {
                return std_initializer_list_wchar_t_size_of()
            }

        val align: Int
            inline get() {
                return std_initializer_list_wchar_t_align_of()
            }

        fun MemScope.Initializer_list__Wchar_t(): Initializer_list__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_initializer_list_wchar_t_new(memory) ?: error("Creation failed"))
            return Initializer_list__Wchar_t(obj, this)
        }
        fun MemScope.Initializer_list__Wchar_t_Holder(): Initializer_list__Wchar_t {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            return Initializer_list__Wchar_t(memory, this)
        }
    }
}

// END KRAPPER GEN for std::initializer_list<wchar_t>


