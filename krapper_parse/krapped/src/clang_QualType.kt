package clang

import clang.LangAS.Companion.fromValue
import clang.QualType.Companion.QualType_Holder
import clang.qualType.DestructionKind
import clang.qualType.DestructionKind.Companion.fromValue as DestructionKindCompanionfromValue
import clang.qualType.PrimitiveCopyKind
import clang.qualType.PrimitiveCopyKind.Companion.fromValue as PrimitiveCopyKindCompanionfromValue
import clang.qualType.PrimitiveDefaultInitializeKind
import clang.qualType.PrimitiveDefaultInitializeKind.Companion.fromValue as PrimitiveDefaultInitializeKindCompanionfromValue
import clang.qualifiers.GC
import clang.qualifiers.GC.Companion.fromValue as CompanionfromValue
import clang.qualifiers.ObjCLifetime
import clang.qualifiers.ObjCLifetime.Companion.fromValue as ObjCLifetimeCompanionfromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.UInt
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.krapper_parse.internal._clang_QualType_dump
import krapper.krapper_parse.internal._clang_QualType_get_as_string
import krapper.krapper_parse.internal.clang_QualType_add_const
import krapper.krapper_parse.internal.clang_QualType_add_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_add_restrict
import krapper.krapper_parse.internal.clang_QualType_add_volatile
import krapper.krapper_parse.internal.clang_QualType_align_of
import krapper.krapper_parse.internal.clang_QualType_dump
import krapper.krapper_parse.internal.clang_QualType_get_address_space
import krapper.krapper_parse.internal.clang_QualType_get_as_opaque_ptr
import krapper.krapper_parse.internal.clang_QualType_get_atomic_unqualified_type
import krapper.krapper_parse.internal.clang_QualType_get_canonical_type
import krapper.krapper_parse.internal.clang_QualType_get_cvr_qualifiers
import krapper.krapper_parse.internal.clang_QualType_get_desugared_type
import krapper.krapper_parse.internal.clang_QualType_get_from_opaque_ptr
import krapper.krapper_parse.internal.clang_QualType_get_local_cvr_qualifiers
import krapper.krapper_parse.internal.clang_QualType_get_local_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_get_local_unqualified_type
import krapper.krapper_parse.internal.clang_QualType_get_non_l_value_expr_type
import krapper.krapper_parse.internal.clang_QualType_get_non_pack_expansion_type
import krapper.krapper_parse.internal.clang_QualType_get_non_reference_type
import krapper.krapper_parse.internal.clang_QualType_get_obj_c_lifetime
import krapper.krapper_parse.internal.clang_QualType_get_obj_cgc_attr
import krapper.krapper_parse.internal.clang_QualType_get_single_step_desugared_type
import krapper.krapper_parse.internal.clang_QualType_get_type_ptr
import krapper.krapper_parse.internal.clang_QualType_get_type_ptr_or_null
import krapper.krapper_parse.internal.clang_QualType_get_unqualified_type
import krapper.krapper_parse.internal.clang_QualType_has_address_discriminated_pointer_auth
import krapper.krapper_parse.internal.clang_QualType_has_address_space
import krapper.krapper_parse.internal.clang_QualType_has_local_non_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_has_local_qualifiers
import krapper.krapper_parse.internal.clang_QualType_has_non_trivial_obj_c_lifetime
import krapper.krapper_parse.internal.clang_QualType_has_non_trivial_to_primitive_copy_c_union
import krapper.krapper_parse.internal.clang_QualType_has_non_trivial_to_primitive_default_initialize_c_union
import krapper.krapper_parse.internal.clang_QualType_has_non_trivial_to_primitive_destruct_c_union
import krapper.krapper_parse.internal.clang_QualType_has_qualifiers
import krapper.krapper_parse.internal.clang_QualType_has_strong_or_weak_obj_c_lifetime
import krapper.krapper_parse.internal.clang_QualType_ignore_parens
import krapper.krapper_parse.internal.clang_QualType_is_address_space_overlapping
import krapper.krapper_parse.internal.clang_QualType_is_at_least_as_qualified_as
import krapper.krapper_parse.internal.clang_QualType_is_bitwise_cloneable_type
import krapper.krapper_parse.internal.clang_QualType_is_c_forbidden_l_value_type
import krapper.krapper_parse.internal.clang_QualType_is_canonical
import krapper.krapper_parse.internal.clang_QualType_is_canonical_as_param
import krapper.krapper_parse.internal.clang_QualType_is_const_qualified
import krapper.krapper_parse.internal.clang_QualType_is_constant
import krapper.krapper_parse.internal.clang_QualType_is_constant_storage
import krapper.krapper_parse.internal.clang_QualType_is_cxx11pod_type
import krapper.krapper_parse.internal.clang_QualType_is_cxx98pod_type
import krapper.krapper_parse.internal.clang_QualType_is_destructed_type
import krapper.krapper_parse.internal.clang_QualType_is_local_const_qualified
import krapper.krapper_parse.internal.clang_QualType_is_local_restrict_qualified
import krapper.krapper_parse.internal.clang_QualType_is_local_volatile_qualified
import krapper.krapper_parse.internal.clang_QualType_is_more_qualified_than
import krapper.krapper_parse.internal.clang_QualType_is_non_trivial_to_primitive_copy
import krapper.krapper_parse.internal.clang_QualType_is_non_trivial_to_primitive_default_initialize
import krapper.krapper_parse.internal.clang_QualType_is_non_trivial_to_primitive_destructive_move
import krapper.krapper_parse.internal.clang_QualType_is_non_weak_in_mrr_with_obj_c_weak
import krapper.krapper_parse.internal.clang_QualType_is_null
import krapper.krapper_parse.internal.clang_QualType_is_obj_cgc_strong
import krapper.krapper_parse.internal.clang_QualType_is_obj_cgc_weak
import krapper.krapper_parse.internal.clang_QualType_is_pod_type
import krapper.krapper_parse.internal.clang_QualType_is_referenceable
import krapper.krapper_parse.internal.clang_QualType_is_restrict_qualified
import krapper.krapper_parse.internal.clang_QualType_is_trivial_type
import krapper.krapper_parse.internal.clang_QualType_is_trivially_copy_constructible_type
import krapper.krapper_parse.internal.clang_QualType_is_trivially_copyable_type
import krapper.krapper_parse.internal.clang_QualType_is_volatile_qualified
import krapper.krapper_parse.internal.clang_QualType_is_web_assembly_externref_type
import krapper.krapper_parse.internal.clang_QualType_is_web_assembly_funcref_type
import krapper.krapper_parse.internal.clang_QualType_is_web_assembly_reference_type
import krapper.krapper_parse.internal.clang_QualType_may_be_dynamic_class
import krapper.krapper_parse.internal.clang_QualType_may_be_not_dynamic_class
import krapper.krapper_parse.internal.clang_QualType_new
import krapper.krapper_parse.internal.clang_QualType_new__const_clang_Type_P_unsigned_int
import krapper.krapper_parse.internal.clang_QualType_op_pointer_reference
import krapper.krapper_parse.internal.clang_QualType_op_reference
import krapper.krapper_parse.internal.clang_QualType_remove_local_const
import krapper.krapper_parse.internal.clang_QualType_remove_local_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_remove_local_fast_qualifiers__unsigned_int
import krapper.krapper_parse.internal.clang_QualType_remove_local_restrict
import krapper.krapper_parse.internal.clang_QualType_remove_local_volatile
import krapper.krapper_parse.internal.clang_QualType_set_local_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_size_of
import krapper.krapper_parse.internal.clang_QualType_strip_obj_c_kind_of_type
import krapper.krapper_parse.internal.clang_QualType_subst_obj_c_member_type
import krapper.krapper_parse.internal.clang_QualType_use_excess_precision
import krapper.krapper_parse.internal.clang_QualType_with_const
import krapper.krapper_parse.internal.clang_QualType_with_cvr_qualifiers
import krapper.krapper_parse.internal.clang_QualType_with_exact_local_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_with_fast_qualifiers
import krapper.krapper_parse.internal.clang_QualType_with_restrict
import krapper.krapper_parse.internal.clang_QualType_with_volatile
import krapper.krapper_parse.internal.clang_QualType_without_local_fast_qualifiers
import platform.linux.free

// BEGIN KRAPPER GEN for clang::QualType

@krapper.CppBinding("clang::QualType")
class QualType(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun getLocalFastQualifiers(): UInt {
        return clang_QualType_get_local_fast_qualifiers(ptr)
    }
    inline fun setLocalFastQualifiers(Quals: UInt): Unit {
        return clang_QualType_set_local_fast_qualifiers(ptr, Quals)
    }
    inline fun UseExcessPrecision(Ctx: ASTContext): Boolean {
        return clang_QualType_use_excess_precision(ptr, Ctx.ptr)
    }
    inline fun getTypePtr(): TypeApi? {
        return Type((clang_QualType_get_type_ptr(ptr) ?: return null), memScope)
    }
    inline fun getTypePtrOrNull(): TypeApi? {
        return Type((clang_QualType_get_type_ptr_or_null(ptr) ?: return null), memScope)
    }
    inline fun getAsOpaquePtr(): COpaquePointer? {
        return clang_QualType_get_as_opaque_ptr(ptr)
    }
    inline fun reference(): TypeApi? {
        return Type((clang_QualType_op_reference(ptr) ?: return null), memScope)
    }
    inline fun pointer_reference(): TypeApi? {
        return Type((clang_QualType_op_pointer_reference(ptr) ?: return null), memScope)
    }
    inline fun isCanonical(): Boolean {
        return clang_QualType_is_canonical(ptr)
    }
    inline fun isCanonicalAsParam(): Boolean {
        return clang_QualType_is_canonical_as_param(ptr)
    }
    inline fun isNull(): Boolean {
        return clang_QualType_is_null(ptr)
    }
    inline fun isReferenceable(): Boolean {
        return clang_QualType_is_referenceable(ptr)
    }
    inline fun isLocalConstQualified(): Boolean {
        return clang_QualType_is_local_const_qualified(ptr)
    }
    inline fun isConstQualified(): Boolean {
        return clang_QualType_is_const_qualified(ptr)
    }
    inline fun isConstantStorage(Ctx: ASTContext, ExcludeCtor: Boolean, ExcludeDtor: Boolean): Boolean {
        return clang_QualType_is_constant_storage(ptr, Ctx.ptr, ExcludeCtor, ExcludeDtor)
    }
    inline fun isLocalRestrictQualified(): Boolean {
        return clang_QualType_is_local_restrict_qualified(ptr)
    }
    inline fun isRestrictQualified(): Boolean {
        return clang_QualType_is_restrict_qualified(ptr)
    }
    inline fun isLocalVolatileQualified(): Boolean {
        return clang_QualType_is_local_volatile_qualified(ptr)
    }
    inline fun isVolatileQualified(): Boolean {
        return clang_QualType_is_volatile_qualified(ptr)
    }
    inline fun hasLocalQualifiers(): Boolean {
        return clang_QualType_has_local_qualifiers(ptr)
    }
    inline fun hasQualifiers(): Boolean {
        return clang_QualType_has_qualifiers(ptr)
    }
    inline fun hasLocalNonFastQualifiers(): Boolean {
        return clang_QualType_has_local_non_fast_qualifiers(ptr)
    }
    inline fun getLocalCVRQualifiers(): UInt {
        return clang_QualType_get_local_cvr_qualifiers(ptr)
    }
    inline fun getCVRQualifiers(): UInt {
        return clang_QualType_get_cvr_qualifiers(ptr)
    }
    inline fun isConstant(Ctx: ASTContext): Boolean {
        return clang_QualType_is_constant(ptr, Ctx.ptr)
    }
    inline fun isPODType(Context: ASTContext): Boolean {
        return clang_QualType_is_pod_type(ptr, Context.ptr)
    }
    inline fun isCXX98PODType(Context: ASTContext): Boolean {
        return clang_QualType_is_cxx98pod_type(ptr, Context.ptr)
    }
    inline fun isCXX11PODType(Context: ASTContext): Boolean {
        return clang_QualType_is_cxx11pod_type(ptr, Context.ptr)
    }
    inline fun isTrivialType(Context: ASTContext): Boolean {
        return clang_QualType_is_trivial_type(ptr, Context.ptr)
    }
    inline fun isTriviallyCopyableType(Context: ASTContext): Boolean {
        return clang_QualType_is_trivially_copyable_type(ptr, Context.ptr)
    }
    inline fun isBitwiseCloneableType(Context: ASTContext): Boolean {
        return clang_QualType_is_bitwise_cloneable_type(ptr, Context.ptr)
    }
    inline fun isTriviallyCopyConstructibleType(Context: ASTContext): Boolean {
        return clang_QualType_is_trivially_copy_constructible_type(ptr, Context.ptr)
    }
    inline fun mayBeDynamicClass(): Boolean {
        return clang_QualType_may_be_dynamic_class(ptr)
    }
    inline fun mayBeNotDynamicClass(): Boolean {
        return clang_QualType_may_be_not_dynamic_class(ptr)
    }
    inline fun isWebAssemblyReferenceType(): Boolean {
        return clang_QualType_is_web_assembly_reference_type(ptr)
    }
    inline fun isWebAssemblyExternrefType(): Boolean {
        return clang_QualType_is_web_assembly_externref_type(ptr)
    }
    inline fun isWebAssemblyFuncrefType(): Boolean {
        return clang_QualType_is_web_assembly_funcref_type(ptr)
    }
    inline fun addConst(): Unit {
        return clang_QualType_add_const(ptr)
    }
    inline fun withConst(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_const(ptr, retValue.ptr)
        return retValue
    }
    inline fun addVolatile(): Unit {
        return clang_QualType_add_volatile(ptr)
    }
    inline fun withVolatile(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_volatile(ptr, retValue.ptr)
        return retValue
    }
    inline fun addRestrict(): Unit {
        return clang_QualType_add_restrict(ptr)
    }
    inline fun withRestrict(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_restrict(ptr, retValue.ptr)
        return retValue
    }
    inline fun withCVRQualifiers(CVR: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_cvr_qualifiers(ptr, CVR, retValue.ptr)
        return retValue
    }
    inline fun addFastQualifiers(TQs: UInt): Unit {
        return clang_QualType_add_fast_qualifiers(ptr, TQs)
    }
    inline fun removeLocalConst(): Unit {
        return clang_QualType_remove_local_const(ptr)
    }
    inline fun removeLocalVolatile(): Unit {
        return clang_QualType_remove_local_volatile(ptr)
    }
    inline fun removeLocalRestrict(): Unit {
        return clang_QualType_remove_local_restrict(ptr)
    }
    inline fun removeLocalFastQualifiers(): Unit {
        return clang_QualType_remove_local_fast_qualifiers(ptr)
    }
    inline fun removeLocalFastQualifiers__unsigned_int(Mask: UInt): Unit {
        return clang_QualType_remove_local_fast_qualifiers__unsigned_int(ptr, Mask)
    }
    inline fun withFastQualifiers(TQs: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_fast_qualifiers(ptr, TQs, retValue.ptr)
        return retValue
    }
    inline fun withExactLocalFastQualifiers(TQs: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_with_exact_local_fast_qualifiers(ptr, TQs, retValue.ptr)
        return retValue
    }
    inline fun withoutLocalFastQualifiers(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_without_local_fast_qualifiers(ptr, retValue.ptr)
        return retValue
    }
    inline fun getCanonicalType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_canonical_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getLocalUnqualifiedType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_local_unqualified_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUnqualifiedType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_unqualified_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun isMoreQualifiedThan(Other: QualType, Ctx: ASTContext): Boolean {
        return clang_QualType_is_more_qualified_than(ptr, Other.ptr, Ctx.ptr)
    }
    inline fun isAtLeastAsQualifiedAs(Other: QualType, Ctx: ASTContext): Boolean {
        return clang_QualType_is_at_least_as_qualified_as(ptr, Other.ptr, Ctx.ptr)
    }
    inline fun getNonReferenceType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_non_reference_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getNonLValueExprType(Context: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_non_l_value_expr_type(ptr, Context.ptr, retValue.ptr)
        return retValue
    }
    inline fun getNonPackExpansionType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_non_pack_expansion_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getDesugaredType(Context: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_desugared_type(ptr, Context.ptr, retValue.ptr)
        return retValue
    }
    inline fun getSingleStepDesugaredType(Context: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_single_step_desugared_type(ptr, Context.ptr, retValue.ptr)
        return retValue
    }
    inline fun IgnoreParens(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_ignore_parens(ptr, retValue.ptr)
        return retValue
    }
    inline fun getAsString(): String? {
        val str: CPointer<ByteVar>? = _clang_QualType_get_as_string(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun dump__const_char_P(s: String?): Unit {
        return clang_QualType_dump(ptr, s)
    }
    inline fun dump(): Unit {
        return _clang_QualType_dump(ptr)
    }
    inline fun hasAddressSpace(): Boolean {
        return clang_QualType_has_address_space(ptr)
    }
    inline fun getAddressSpace(): LangAS {
        return fromValue(clang_QualType_get_address_space(ptr))
    }
    inline fun isAddressSpaceOverlapping(T: QualType, Ctx: ASTContext): Boolean {
        return clang_QualType_is_address_space_overlapping(ptr, T.ptr, Ctx.ptr)
    }
    inline fun getObjCGCAttr(): GC {
        return CompanionfromValue(clang_QualType_get_obj_cgc_attr(ptr))
    }
    inline fun isObjCGCWeak(): Boolean {
        return clang_QualType_is_obj_cgc_weak(ptr)
    }
    inline fun isObjCGCStrong(): Boolean {
        return clang_QualType_is_obj_cgc_strong(ptr)
    }
    inline fun getObjCLifetime(): ObjCLifetime {
        return ObjCLifetimeCompanionfromValue(clang_QualType_get_obj_c_lifetime(ptr))
    }
    inline fun hasNonTrivialObjCLifetime(): Boolean {
        return clang_QualType_has_non_trivial_obj_c_lifetime(ptr)
    }
    inline fun hasStrongOrWeakObjCLifetime(): Boolean {
        return clang_QualType_has_strong_or_weak_obj_c_lifetime(ptr)
    }
    inline fun isNonWeakInMRRWithObjCWeak(Context: ASTContext): Boolean {
        return clang_QualType_is_non_weak_in_mrr_with_obj_c_weak(ptr, Context.ptr)
    }
    inline fun hasAddressDiscriminatedPointerAuth(): Boolean {
        return clang_QualType_has_address_discriminated_pointer_auth(ptr)
    }
    inline fun isNonTrivialToPrimitiveDefaultInitialize(): PrimitiveDefaultInitializeKind {
        return PrimitiveDefaultInitializeKindCompanionfromValue(clang_QualType_is_non_trivial_to_primitive_default_initialize(ptr))
    }
    inline fun isNonTrivialToPrimitiveCopy(): PrimitiveCopyKind {
        return PrimitiveCopyKindCompanionfromValue(clang_QualType_is_non_trivial_to_primitive_copy(ptr))
    }
    inline fun isNonTrivialToPrimitiveDestructiveMove(): PrimitiveCopyKind {
        return PrimitiveCopyKindCompanionfromValue(clang_QualType_is_non_trivial_to_primitive_destructive_move(ptr))
    }
    inline fun isDestructedType(): DestructionKind {
        return DestructionKindCompanionfromValue(clang_QualType_is_destructed_type(ptr))
    }
    inline fun hasNonTrivialToPrimitiveDefaultInitializeCUnion(): Boolean {
        return clang_QualType_has_non_trivial_to_primitive_default_initialize_c_union(ptr)
    }
    inline fun hasNonTrivialToPrimitiveDestructCUnion(): Boolean {
        return clang_QualType_has_non_trivial_to_primitive_destruct_c_union(ptr)
    }
    inline fun hasNonTrivialToPrimitiveCopyCUnion(): Boolean {
        return clang_QualType_has_non_trivial_to_primitive_copy_c_union(ptr)
    }
    inline fun isCForbiddenLValueType(): Boolean {
        return clang_QualType_is_c_forbidden_l_value_type(ptr)
    }
    inline fun substObjCMemberType(objectType: QualType, dc: DeclContext?, context: ObjCSubstitutionContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_subst_obj_c_member_type(ptr, objectType.ptr, dc?.ptr, context.value, retValue.ptr)
        return retValue
    }
    inline fun stripObjCKindOfType(ctx: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_strip_obj_c_kind_of_type(ptr, ctx.ptr, retValue.ptr)
        return retValue
    }
    inline fun getAtomicUnqualifiedType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_QualType_get_atomic_unqualified_type(ptr, retValue.ptr)
        return retValue
    }
    companion object {
        val size: Int
            inline get() {
                return clang_QualType_size_of()
            }

        val align: Int
            inline get() {
                return clang_QualType_align_of()
            }

        fun MemScope.QualType(): QualType {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_QualType_new(memory) ?: error("Creation failed"))
            return QualType(obj, this)
        }
        fun MemScope.QualType__const_clang_Type_P_unsigned_int(Ptr: Type?, Quals: UInt): QualType {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_QualType_new__const_clang_Type_P_unsigned_int(memory, Ptr?.ptr, Quals) ?: error("Creation failed"))
            return QualType(obj, this)
        }
        inline fun MemScope.getFromOpaquePtr(Ptr: COpaquePointer?): QualType {
            val retValue: QualType = memScope.QualType_Holder()
            clang_QualType_get_from_opaque_ptr(Ptr, retValue.ptr)
            return retValue
        }
        fun MemScope.QualType_Holder(): QualType {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return QualType(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::QualType


