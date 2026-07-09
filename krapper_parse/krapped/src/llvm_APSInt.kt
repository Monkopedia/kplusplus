package llvm

import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.UInt
import kotlin.ULong
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.krapper_parse.internal.llvm_APSInt_align_of
import krapper.krapper_parse.internal.llvm_APSInt_compare_values
import krapper.krapper_parse.internal.llvm_APSInt_ext_or_trunc
import krapper.krapper_parse.internal.llvm_APSInt_extend
import krapper.krapper_parse.internal.llvm_APSInt_get
import krapper.krapper_parse.internal.llvm_APSInt_get_ext_value
import krapper.krapper_parse.internal.llvm_APSInt_get_max_value
import krapper.krapper_parse.internal.llvm_APSInt_get_min_value
import krapper.krapper_parse.internal.llvm_APSInt_get_unsigned
import krapper.krapper_parse.internal.llvm_APSInt_is_negative
import krapper.krapper_parse.internal.llvm_APSInt_is_non_negative
import krapper.krapper_parse.internal.llvm_APSInt_is_representable_by_int64
import krapper.krapper_parse.internal.llvm_APSInt_is_same_value
import krapper.krapper_parse.internal.llvm_APSInt_is_signed
import krapper.krapper_parse.internal.llvm_APSInt_is_strictly_positive
import krapper.krapper_parse.internal.llvm_APSInt_is_unsigned
import krapper.krapper_parse.internal.llvm_APSInt_new
import krapper.krapper_parse.internal.llvm_APSInt_new__llvm_StringRef
import krapper.krapper_parse.internal.llvm_APSInt_new__unsigned_int_bool
import krapper.krapper_parse.internal.llvm_APSInt_op_and
import krapper.krapper_parse.internal.llvm_APSInt_op_assign__unsigned_long
import krapper.krapper_parse.internal.llvm_APSInt_op_divide
import krapper.krapper_parse.internal.llvm_APSInt_op_eq
import krapper.krapper_parse.internal.llvm_APSInt_op_gt
import krapper.krapper_parse.internal.llvm_APSInt_op_gt__long
import krapper.krapper_parse.internal.llvm_APSInt_op_gteq
import krapper.krapper_parse.internal.llvm_APSInt_op_gteq__long
import krapper.krapper_parse.internal.llvm_APSInt_op_inv
import krapper.krapper_parse.internal.llvm_APSInt_op_lt
import krapper.krapper_parse.internal.llvm_APSInt_op_lteq
import krapper.krapper_parse.internal.llvm_APSInt_op_lteq__long
import krapper.krapper_parse.internal.llvm_APSInt_op_minus
import krapper.krapper_parse.internal.llvm_APSInt_op_mod
import krapper.krapper_parse.internal.llvm_APSInt_op_neq
import krapper.krapper_parse.internal.llvm_APSInt_op_neq__long
import krapper.krapper_parse.internal.llvm_APSInt_op_or
import krapper.krapper_parse.internal.llvm_APSInt_op_plus
import krapper.krapper_parse.internal.llvm_APSInt_op_plus_equals
import krapper.krapper_parse.internal.llvm_APSInt_op_post_decrement
import krapper.krapper_parse.internal.llvm_APSInt_op_post_increment
import krapper.krapper_parse.internal.llvm_APSInt_op_shl
import krapper.krapper_parse.internal.llvm_APSInt_op_shr
import krapper.krapper_parse.internal.llvm_APSInt_op_times
import krapper.krapper_parse.internal.llvm_APSInt_op_unary_minus
import krapper.krapper_parse.internal.llvm_APSInt_op_xor
import krapper.krapper_parse.internal.llvm_APSInt_operator_andeq
import krapper.krapper_parse.internal.llvm_APSInt_operator_diveq
import krapper.krapper_parse.internal.llvm_APSInt_operator_minuseq
import krapper.krapper_parse.internal.llvm_APSInt_operator_modeq
import krapper.krapper_parse.internal.llvm_APSInt_operator_oreq
import krapper.krapper_parse.internal.llvm_APSInt_operator_shleq
import krapper.krapper_parse.internal.llvm_APSInt_operator_shreq
import krapper.krapper_parse.internal.llvm_APSInt_operator_timeseq
import krapper.krapper_parse.internal.llvm_APSInt_operator_xoreq
import krapper.krapper_parse.internal.llvm_APSInt_relative_shl
import krapper.krapper_parse.internal.llvm_APSInt_relative_shr
import krapper.krapper_parse.internal.llvm_APSInt_set_is_signed
import krapper.krapper_parse.internal.llvm_APSInt_set_is_unsigned
import krapper.krapper_parse.internal.llvm_APSInt_size_of
import krapper.krapper_parse.internal.llvm_APSInt_trunc
import llvm.APSInt.Companion.APSInt_Holder

// BEGIN KRAPPER GEN for llvm::APSInt

@krapper.CppBinding("llvm::APSInt")
class APSInt(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.Comparable<APSInt> {
    inline fun isNegative(): Boolean {
        return llvm_APSInt_is_negative(ptr)
    }
    inline fun isNonNegative(): Boolean {
        return llvm_APSInt_is_non_negative(ptr)
    }
    inline fun isStrictlyPositive(): Boolean {
        return llvm_APSInt_is_strictly_positive(ptr)
    }
    inline infix fun assign(RHS: ULong): APSInt? {
        return APSInt((llvm_APSInt_op_assign__unsigned_long(ptr, RHS) ?: return null), memScope)
    }
    inline fun isSigned(): Boolean {
        return llvm_APSInt_is_signed(ptr)
    }
    inline fun isUnsigned(): Boolean {
        return llvm_APSInt_is_unsigned(ptr)
    }
    inline fun setIsUnsigned(Val: Boolean): Unit {
        return llvm_APSInt_set_is_unsigned(ptr, Val)
    }
    inline fun setIsSigned(Val: Boolean): Unit {
        return llvm_APSInt_set_is_signed(ptr, Val)
    }
    inline fun isRepresentableByInt64(): Boolean {
        return llvm_APSInt_is_representable_by_int64(ptr)
    }
    inline fun getExtValue(): Long {
        return llvm_APSInt_get_ext_value(ptr)
    }
    inline fun trunc(width: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_trunc(ptr, width, retValue.ptr)
        return retValue
    }
    inline fun extend(width: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_extend(ptr, width, retValue.ptr)
        return retValue
    }
    inline fun extOrTrunc(width: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_ext_or_trunc(ptr, width, retValue.ptr)
        return retValue
    }
    inline fun operator_mod_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_modeq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_div_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_diveq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline operator fun rem(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_mod(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline operator fun div(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_divide(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline infix fun shr(Amt: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_shr(ptr, Amt, retValue.ptr)
        return retValue
    }
    inline fun operator_gt_gt_eq(Amt: UInt): APSInt? {
        return APSInt((llvm_APSInt_operator_shreq(ptr, Amt) ?: return null), memScope)
    }
    inline fun relativeShr(Amt: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_relative_shr(ptr, Amt, retValue.ptr)
        return retValue
    }
    override operator fun compareTo(other: APSInt): Int {
        return if (llvm_APSInt_op_lt(ptr, other.ptr)) -1 else if (llvm_APSInt_op_lt(other.ptr, ptr)) 1 else 0
    }
    inline infix fun gt(RHS: APSInt): Boolean {
        return llvm_APSInt_op_gt(ptr, RHS.ptr)
    }
    inline infix fun lteq(RHS: APSInt): Boolean {
        return llvm_APSInt_op_lteq(ptr, RHS.ptr)
    }
    inline infix fun gteq(RHS: APSInt): Boolean {
        return llvm_APSInt_op_gteq(ptr, RHS.ptr)
    }
    override fun equals(other: Any?): Boolean {
        return other is APSInt && llvm_APSInt_op_eq(ptr, (other as APSInt).ptr)
    }
    inline infix fun neq(RHS: APSInt): Boolean {
        return llvm_APSInt_op_neq(ptr, RHS.ptr)
    }
    inline infix fun _neq(RHS: Long): Boolean {
        return llvm_APSInt_op_neq__long(ptr, RHS)
    }
    inline infix fun _lteq(RHS: Long): Boolean {
        return llvm_APSInt_op_lteq__long(ptr, RHS)
    }
    inline infix fun _gteq(RHS: Long): Boolean {
        return llvm_APSInt_op_gteq__long(ptr, RHS)
    }
    inline infix fun _gt(RHS: Long): Boolean {
        return llvm_APSInt_op_gt__long(ptr, RHS)
    }
    inline infix fun shl(Bits: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_shl(ptr, Bits, retValue.ptr)
        return retValue
    }
    inline fun operator_lt_lt_eq(Amt: UInt): APSInt? {
        return APSInt((llvm_APSInt_operator_shleq(ptr, Amt) ?: return null), memScope)
    }
    inline fun relativeShl(Amt: UInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_relative_shl(ptr, Amt, retValue.ptr)
        return retValue
    }
    inline fun postIncrement(): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_post_increment(ptr, 0, retValue.ptr)
        return retValue
    }
    inline fun postDecrement(): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_post_decrement(ptr, 0, retValue.ptr)
        return retValue
    }
    inline operator fun unaryMinus(): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_unary_minus(ptr, retValue.ptr)
        return retValue
    }
    inline infix fun plusEquals(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_op_plus_equals(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_minus_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_minuseq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_star_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_timeseq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_and_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_andeq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_or_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_oreq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline fun operator_xor_eq(RHS: APSInt): APSInt? {
        return APSInt((llvm_APSInt_operator_xoreq(ptr, RHS.ptr) ?: return null), memScope)
    }
    inline infix fun and(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_and(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline infix fun or(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_or(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline infix fun xor(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_xor(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline operator fun times(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_times(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline operator fun plus(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_plus(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline operator fun minus(RHS: APSInt): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_minus(ptr, RHS.ptr, retValue.ptr)
        return retValue
    }
    inline fun inv(): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        llvm_APSInt_op_inv(ptr, retValue.ptr)
        return retValue
    }
    override fun hashCode(): Int {
        return 0
    }
    companion object {
        val size: Int
            inline get() {
                return llvm_APSInt_size_of()
            }

        val align: Int
            inline get() {
                return llvm_APSInt_align_of()
            }

        fun MemScope.APSInt(): APSInt {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (llvm_APSInt_new(memory) ?: error("Creation failed"))
            return APSInt(obj, this)
        }
        fun MemScope.APSInt__unsigned_int_bool(BitWidth: UInt, _isUnsigned: Boolean): APSInt {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (llvm_APSInt_new__unsigned_int_bool(memory, BitWidth, _isUnsigned) ?: error("Creation failed"))
            return APSInt(obj, this)
        }
        fun MemScope.APSInt__llvm_StringRef(Str: String?): APSInt {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (llvm_APSInt_new__llvm_StringRef(memory, Str) ?: error("Creation failed"))
            return APSInt(obj, this)
        }
        inline fun MemScope.getMaxValue(numBits: UInt, Unsigned: Boolean): APSInt {
            val retValue: APSInt = memScope.APSInt_Holder()
            llvm_APSInt_get_max_value(numBits, Unsigned, retValue.ptr)
            return retValue
        }
        inline fun MemScope.getMinValue(numBits: UInt, Unsigned: Boolean): APSInt {
            val retValue: APSInt = memScope.APSInt_Holder()
            llvm_APSInt_get_min_value(numBits, Unsigned, retValue.ptr)
            return retValue
        }
        inline fun MemScope.isSameValue(I1: APSInt, I2: APSInt): Boolean {
            return llvm_APSInt_is_same_value(I1.ptr, I2.ptr)
        }
        inline fun MemScope.compareValues(I1: APSInt, I2: APSInt): Int {
            return llvm_APSInt_compare_values(I1.ptr, I2.ptr)
        }
        inline fun MemScope.get(X: Long): APSInt {
            val retValue: APSInt = memScope.APSInt_Holder()
            llvm_APSInt_get(X, retValue.ptr)
            return retValue
        }
        inline fun MemScope.getUnsigned(X: ULong): APSInt {
            val retValue: APSInt = memScope.APSInt_Holder()
            llvm_APSInt_get_unsigned(X, retValue.ptr)
            return retValue
        }
        fun MemScope.APSInt_Holder(): APSInt {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return APSInt(memory, this)
        }
    }
}

// END KRAPPER GEN for llvm::APSInt


