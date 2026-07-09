package clang

import clang.decl.Kind
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_add_specialization
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_align_of
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_classof
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_classof_kind
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_get_canonical_decl
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_get_instantiated_from_member_template
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_get_most_recent_decl
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_get_previous_decl
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_get_templated_decl
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_is_this_declaration_a_definition
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_load_lazy_specializations
import krapper.krapper_parse.internal.clang_ClassTemplateDecl_size_of

// BEGIN KRAPPER GEN for clang::ClassTemplateDecl

@krapper.CppBinding("clang::ClassTemplateDecl")
class ClassTemplateDecl(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun LoadLazySpecializations(OnlyPartial: Boolean = false): Unit {
        return clang_ClassTemplateDecl_load_lazy_specializations(ptr, OnlyPartial)
    }
    inline fun getTemplatedDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_ClassTemplateDecl_get_templated_decl(ptr) ?: return null), memScope)
    }
    inline fun isThisDeclarationADefinition(): Boolean {
        return clang_ClassTemplateDecl_is_this_declaration_a_definition(ptr)
    }
    inline fun AddSpecialization(D: ClassTemplateSpecializationDecl?, InsertPos: COpaquePointer?): Unit {
        return clang_ClassTemplateDecl_add_specialization(ptr, D?.ptr, InsertPos)
    }
    inline fun getCanonicalDecl(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_ClassTemplateDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    inline fun getPreviousDecl(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_ClassTemplateDecl_get_previous_decl(ptr) ?: return null), memScope)
    }
    inline fun getMostRecentDecl(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_ClassTemplateDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun getInstantiatedFromMemberTemplate(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_ClassTemplateDecl_get_instantiated_from_member_template(ptr) ?: return null), memScope)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ClassTemplateDecl_size_of()
            }

        val align: Int
            inline get() {
                return clang_ClassTemplateDecl_align_of()
            }

        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_ClassTemplateDecl_classof(D?.ptr)
        }
        inline fun MemScope.classofKind(K: Kind): Boolean {
            return clang_ClassTemplateDecl_classof_kind(K.value)
        }
        fun MemScope.ClassTemplateDecl_Holder(): ClassTemplateDecl {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return ClassTemplateDecl(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::ClassTemplateDecl


