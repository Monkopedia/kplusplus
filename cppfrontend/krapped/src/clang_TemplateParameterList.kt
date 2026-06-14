package clang

import kotlin.Boolean
import kotlin.Int
import kotlin.UInt
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.cppfrontend.internal.clang_TemplateParameterList_align_of
import krapper.cppfrontend.internal.clang_TemplateParameterList_contains_unexpanded_parameter_pack
import krapper.cppfrontend.internal.clang_TemplateParameterList_empty
import krapper.cppfrontend.internal.clang_TemplateParameterList_get_depth
import krapper.cppfrontend.internal.clang_TemplateParameterList_get_min_required_arguments
import krapper.cppfrontend.internal.clang_TemplateParameterList_get_param
import krapper.cppfrontend.internal.clang_TemplateParameterList_has_associated_constraints
import krapper.cppfrontend.internal.clang_TemplateParameterList_has_parameter_pack
import krapper.cppfrontend.internal.clang_TemplateParameterList_size
import krapper.cppfrontend.internal.clang_TemplateParameterList_size_of

// BEGIN KRAPPER GEN for clang::TemplateParameterList

@krapper.CppBinding("clang::TemplateParameterList")
class TemplateParameterList(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun size(): UInt {
        return clang_TemplateParameterList_size(ptr)
    }
    inline fun empty(): Boolean {
        return clang_TemplateParameterList_empty(ptr)
    }
    inline fun getParam(Idx: UInt): NamedDeclApi? {
        return NamedDecl((clang_TemplateParameterList_get_param(ptr, Idx) ?: return null), memScope)
    }
    inline fun getMinRequiredArguments(): UInt {
        return clang_TemplateParameterList_get_min_required_arguments(ptr)
    }
    inline fun getDepth(): UInt {
        return clang_TemplateParameterList_get_depth(ptr)
    }
    inline fun containsUnexpandedParameterPack(): Boolean {
        return clang_TemplateParameterList_contains_unexpanded_parameter_pack(ptr)
    }
    inline fun hasParameterPack(): Boolean {
        return clang_TemplateParameterList_has_parameter_pack(ptr)
    }
    inline fun hasAssociatedConstraints(): Boolean {
        return clang_TemplateParameterList_has_associated_constraints(ptr)
    }
    companion object {
        val _size: Int
            inline get() {
                return clang_TemplateParameterList_size_of()
            }

        val align: Int
            inline get() {
                return clang_TemplateParameterList_align_of()
            }

        fun MemScope.TemplateParameterList_Holder(): TemplateParameterList {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            return TemplateParameterList(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::TemplateParameterList


