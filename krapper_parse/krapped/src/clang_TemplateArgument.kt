package clang

import clang.QualType.Companion.QualType_Holder
import clang.TemplateArgument.Companion.TemplateArgument_Holder
import clang.templateArgument.ArgKind
import clang.templateArgument.ArgKind.Companion.fromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.UInt
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.krapper_parse.internal._clang_TemplateArgument_dump
import krapper.krapper_parse.internal.clang_TemplateArgument_align_of
import krapper.krapper_parse.internal.clang_TemplateArgument_contains_unexpanded_parameter_pack
import krapper.krapper_parse.internal.clang_TemplateArgument_get_as_decl
import krapper.krapper_parse.internal.clang_TemplateArgument_get_as_integral
import krapper.krapper_parse.internal.clang_TemplateArgument_get_as_type
import krapper.krapper_parse.internal.clang_TemplateArgument_get_empty_pack
import krapper.krapper_parse.internal.clang_TemplateArgument_get_integral_type
import krapper.krapper_parse.internal.clang_TemplateArgument_get_is_defaulted
import krapper.krapper_parse.internal.clang_TemplateArgument_get_kind
import krapper.krapper_parse.internal.clang_TemplateArgument_get_non_type_template_argument_type
import krapper.krapper_parse.internal.clang_TemplateArgument_get_null_ptr_type
import krapper.krapper_parse.internal.clang_TemplateArgument_get_pack_expansion_pattern
import krapper.krapper_parse.internal.clang_TemplateArgument_get_param_type_for_decl
import krapper.krapper_parse.internal.clang_TemplateArgument_get_structural_value_type
import krapper.krapper_parse.internal.clang_TemplateArgument_is_canonical_expr
import krapper.krapper_parse.internal.clang_TemplateArgument_is_concept_or_concept_template_parameter
import krapper.krapper_parse.internal.clang_TemplateArgument_is_dependent
import krapper.krapper_parse.internal.clang_TemplateArgument_is_instantiation_dependent
import krapper.krapper_parse.internal.clang_TemplateArgument_is_null
import krapper.krapper_parse.internal.clang_TemplateArgument_is_pack_expansion
import krapper.krapper_parse.internal.clang_TemplateArgument_new
import krapper.krapper_parse.internal.clang_TemplateArgument_new__clang_QualType_bool_bool
import krapper.krapper_parse.internal.clang_TemplateArgument_new__clang_ValueDecl_P_clang_QualType_bool
import krapper.krapper_parse.internal.clang_TemplateArgument_new__const_clang_ASTContext_and_const_llvm_APSInt_and_clang_QualType_bool
import krapper.krapper_parse.internal.clang_TemplateArgument_new__const_clang_TemplateArgument_and_clang_QualType
import krapper.krapper_parse.internal.clang_TemplateArgument_pack_size
import krapper.krapper_parse.internal.clang_TemplateArgument_set_integral_type
import krapper.krapper_parse.internal.clang_TemplateArgument_set_is_defaulted
import krapper.krapper_parse.internal.clang_TemplateArgument_size_of
import krapper.krapper_parse.internal.clang_TemplateArgument_structurally_equals
import llvm.APSInt
import llvm.APSInt.Companion.APSInt_Holder

// BEGIN KRAPPER GEN for clang::TemplateArgument

@krapper.CppBinding("clang::TemplateArgument")
class TemplateArgument(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun getKind(): ArgKind {
        return fromValue(clang_TemplateArgument_get_kind(ptr))
    }
    inline fun isNull(): Boolean {
        return clang_TemplateArgument_is_null(ptr)
    }
    inline fun isDependent(): Boolean {
        return clang_TemplateArgument_is_dependent(ptr)
    }
    inline fun isInstantiationDependent(): Boolean {
        return clang_TemplateArgument_is_instantiation_dependent(ptr)
    }
    inline fun containsUnexpandedParameterPack(): Boolean {
        return clang_TemplateArgument_contains_unexpanded_parameter_pack(ptr)
    }
    inline fun isPackExpansion(): Boolean {
        return clang_TemplateArgument_is_pack_expansion(ptr)
    }
    inline fun isConceptOrConceptTemplateParameter(): Boolean {
        return clang_TemplateArgument_is_concept_or_concept_template_parameter(ptr)
    }
    inline fun getAsType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_as_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getAsDecl(): ValueDeclApi? {
        return ValueDecl((clang_TemplateArgument_get_as_decl(ptr) ?: return null), memScope)
    }
    inline fun getParamTypeForDecl(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_param_type_for_decl(ptr, retValue.ptr)
        return retValue
    }
    inline fun getNullPtrType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_null_ptr_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getAsIntegral(): APSInt {
        val retValue: APSInt = memScope.APSInt_Holder()
        clang_TemplateArgument_get_as_integral(ptr, retValue.ptr)
        return retValue
    }
    inline fun getIntegralType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_integral_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setIntegralType(T: QualType): Unit {
        return clang_TemplateArgument_set_integral_type(ptr, T.ptr)
    }
    inline fun setIsDefaulted(v: Boolean): Unit {
        return clang_TemplateArgument_set_is_defaulted(ptr, v)
    }
    inline fun getIsDefaulted(): Boolean {
        return clang_TemplateArgument_get_is_defaulted(ptr)
    }
    inline fun getStructuralValueType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_structural_value_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getNonTypeTemplateArgumentType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TemplateArgument_get_non_type_template_argument_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun isCanonicalExpr(): Boolean {
        return clang_TemplateArgument_is_canonical_expr(ptr)
    }
    inline fun pack_size(): UInt {
        return clang_TemplateArgument_pack_size(ptr)
    }
    inline fun structurallyEquals(Other: TemplateArgument): Boolean {
        return clang_TemplateArgument_structurally_equals(ptr, Other.ptr)
    }
    inline fun getPackExpansionPattern(): TemplateArgument {
        val retValue: TemplateArgument = memScope.TemplateArgument_Holder()
        clang_TemplateArgument_get_pack_expansion_pattern(ptr, retValue.ptr)
        return retValue
    }
    inline fun dump(): Unit {
        return _clang_TemplateArgument_dump(ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_TemplateArgument_size_of()
            }

        val align: Int
            inline get() {
                return clang_TemplateArgument_align_of()
            }

        fun MemScope.TemplateArgument(): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_TemplateArgument_new(memory) ?: error("Creation failed"))
            return TemplateArgument(obj, this)
        }
        fun MemScope.TemplateArgument__clang_QualType_bool_bool(T: QualType, isNullPtr: Boolean, IsDefaulted: Boolean): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_TemplateArgument_new__clang_QualType_bool_bool(memory, T.ptr, isNullPtr, IsDefaulted) ?: error("Creation failed"))
            return TemplateArgument(obj, this)
        }
        fun MemScope.TemplateArgument__clang_ValueDecl_P_clang_QualType_bool(D: ValueDecl?, QT: QualType, IsDefaulted: Boolean): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_TemplateArgument_new__clang_ValueDecl_P_clang_QualType_bool(memory, D?.ptr, QT.ptr, IsDefaulted) ?: error("Creation failed"))
            return TemplateArgument(obj, this)
        }
        fun MemScope.TemplateArgument__const_clang_ASTContext_const_llvm_APSInt_clang_QualType_bool(Ctx: ASTContext, Value: APSInt, Type: QualType, IsDefaulted: Boolean): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_TemplateArgument_new__const_clang_ASTContext_and_const_llvm_APSInt_and_clang_QualType_bool(memory, Ctx.ptr, Value.ptr, Type.ptr, IsDefaulted) ?: error("Creation failed"))
            return TemplateArgument(obj, this)
        }
        fun MemScope.TemplateArgument__const_clang_TemplateArgument_clang_QualType(Other: TemplateArgument, Type: QualType): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_TemplateArgument_new__const_clang_TemplateArgument_and_clang_QualType(memory, Other.ptr, Type.ptr) ?: error("Creation failed"))
            return TemplateArgument(obj, this)
        }
        inline fun MemScope.getEmptyPack(): TemplateArgument {
            val retValue: TemplateArgument = memScope.TemplateArgument_Holder()
            clang_TemplateArgument_get_empty_pack(retValue.ptr)
            return retValue
        }
        fun MemScope.TemplateArgument_Holder(): TemplateArgument {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return TemplateArgument(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::TemplateArgument


