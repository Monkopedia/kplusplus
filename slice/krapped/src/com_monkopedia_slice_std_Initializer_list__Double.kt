package com.monkopedia.slice.std

import kotlin.Int
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_initializer_list_double_align_of
import krapper.slice.internal.std_initializer_list_double_new
import krapper.slice.internal.std_initializer_list_double_size
import krapper.slice.internal.std_initializer_list_double_size_of
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::initializer_list<double>

@krapper.CppBinding("std::initializer_list<double>")
class Initializer_list__Double(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun size(): size_t {
        return std_initializer_list_double_size(ptr)
    }
    companion object {
        val _size: Int
            inline get() {
                return std_initializer_list_double_size_of()
            }

        val align: Int
            inline get() {
                return std_initializer_list_double_align_of()
            }

        fun MemScope.Initializer_list__Double(): Initializer_list__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_initializer_list_double_new(memory) ?: error("Creation failed"))
            return Initializer_list__Double(obj, this)
        }
        fun MemScope.Initializer_list__Double_Holder(): Initializer_list__Double {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            return Initializer_list__Double(memory, this)
        }
    }
}

// END KRAPPER GEN for std::initializer_list<double>


