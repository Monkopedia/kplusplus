package com.monkopedia.slice.std

import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.ULong
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.interpretCPointer
import krapper.slice.internal._std__Bit_reference_new
import krapper.slice.internal.std__Bit_reference__M_mask_get
import krapper.slice.internal.std__Bit_reference__M_mask_set
import krapper.slice.internal.std__Bit_reference__M_p_get
import krapper.slice.internal.std__Bit_reference__M_p_set
import krapper.slice.internal.std__Bit_reference_align_of
import krapper.slice.internal.std__Bit_reference_flip
import krapper.slice.internal.std__Bit_reference_new
import krapper.slice.internal.std__Bit_reference_op_assign
import krapper.slice.internal.std__Bit_reference_op_assign__const_std__Bit_reference_and
import krapper.slice.internal.std__Bit_reference_op_eq
import krapper.slice.internal.std__Bit_reference_op_lt
import krapper.slice.internal.std__Bit_reference_op_to_boolean
import krapper.slice.internal.std__Bit_reference_size_of

// BEGIN KRAPPER GEN for std::_Bit_reference

@krapper.CppBinding("std::_Bit_reference")
class _Bit_reference(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.Comparable<_Bit_reference> {
    inline fun toBoolean(): Boolean {
        return std__Bit_reference_op_to_boolean(ptr)
    }
    inline infix fun assign(__x: Boolean): _Bit_reference? {
        return _Bit_reference((std__Bit_reference_op_assign(ptr, __x) ?: return null), memScope)
    }
    inline infix fun _assign(__x: _Bit_reference): _Bit_reference? {
        return _Bit_reference((std__Bit_reference_op_assign__const_std__Bit_reference_and(ptr, __x.ptr) ?: return null), memScope)
    }
    override fun equals(other: Any?): Boolean {
        return other is _Bit_reference && std__Bit_reference_op_eq(ptr, (other as _Bit_reference).ptr)
    }
    override operator fun compareTo(other: _Bit_reference): Int {
        return if (std__Bit_reference_op_lt(ptr, other.ptr)) -1 else if (std__Bit_reference_op_lt(other.ptr, ptr)) 1 else 0
    }
    inline fun flip(): Unit {
        return std__Bit_reference_flip(ptr)
    }
    override fun hashCode(): Int {
        var result = _M_p.hashCode()
        result = 31 * result + _M_mask.hashCode()
        return result
    }
    companion object {
        val size: Int
            inline get() {
                return std__Bit_reference_size_of()
            }

        val align: Int
            inline get() {
                return std__Bit_reference_align_of()
            }

        fun MemScope._Bit_reference__unsigned_long_P_unsigned_long(__x: CValuesRef<ULongVar>?, __y: ULong): _Bit_reference {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std__Bit_reference_new(memory, __x, __y) ?: error("Creation failed"))
            return _Bit_reference(obj, this)
        }
        fun MemScope._Bit_reference(): _Bit_reference {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (_std__Bit_reference_new(memory) ?: error("Creation failed"))
            return _Bit_reference(obj, this)
        }
        fun MemScope._Bit_reference_Holder(): _Bit_reference {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return _Bit_reference(memory, this)
        }
    }
    var _M_p: CValuesRef<ULongVar>?
        inline get() {
            return std__Bit_reference__M_p_get(ptr)
        }
        inline set(value) {
            std__Bit_reference__M_p_set(ptr, value)
        }

    var _M_mask: ULong
        inline get() {
            return std__Bit_reference__M_mask_get(ptr)
        }
        inline set(value) {
            std__Bit_reference__M_mask_set(ptr, value)
        }

}

// END KRAPPER GEN for std::_Bit_reference


