package com.monkopedia.slice.std

import kotlin.Int
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal.std_initializer_list_int_align_of
import krapper.slice.internal.std_initializer_list_int_new
import krapper.slice.internal.std_initializer_list_int_size
import krapper.slice.internal.std_initializer_list_int_size_of
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::initializer_list<int>

@krapper.CppBinding("std::initializer_list<int>")
class Initializer_list__Int(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun size(): size_t {
        return std_initializer_list_int_size(ptr)
    }
    companion object {
        val _size: Int
            inline get() {
                return std_initializer_list_int_size_of()
            }

        val align: Int
            inline get() {
                return std_initializer_list_int_align_of()
            }

        fun MemScope.Initializer_list__Int(): Initializer_list__Int {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_initializer_list_int_new(memory) ?: error("Creation failed"))
            return Initializer_list__Int(obj, this)
        }
        fun MemScope.Initializer_list__Int_Holder(): Initializer_list__Int {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            return Initializer_list__Int(memory, this)
        }
    }
}

// END KRAPPER GEN for std::initializer_list<int>


