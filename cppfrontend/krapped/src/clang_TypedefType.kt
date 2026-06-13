package clang

import clang.QualType.Companion.QualType_Holder
import kotlin.Boolean
import kotlin.Int
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.cppfrontend.internal.clang_TypedefType_align_of
import krapper.cppfrontend.internal.clang_TypedefType_classof
import krapper.cppfrontend.internal.clang_TypedefType_desugar
import krapper.cppfrontend.internal.clang_TypedefType_get_decl
import krapper.cppfrontend.internal.clang_TypedefType_is_sugared
import krapper.cppfrontend.internal.clang_TypedefType_size_of
import krapper.cppfrontend.internal.clang_TypedefType_type_matches_decl

// BEGIN KRAPPER GEN for clang::TypedefType

@krapper.CppBinding("clang::TypedefType")
class TypedefType(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun getDecl(): TypedefNameDecl? {
        return TypedefNameDecl((clang_TypedefType_get_decl(ptr) ?: return null), memScope)
    }
    inline fun isSugared(): Boolean {
        return clang_TypedefType_is_sugared(ptr)
    }
    inline fun desugar(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_TypedefType_desugar(ptr, retValue.ptr)
        return retValue
    }
    inline fun typeMatchesDecl(): Boolean {
        return clang_TypedefType_type_matches_decl(ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_TypedefType_size_of()
            }

        val align: Int
            inline get() {
                return clang_TypedefType_align_of()
            }

        inline fun MemScope.classof(T: TypeApi?): Boolean {
            return clang_TypedefType_classof(T?.ptr)
        }
        fun MemScope.TypedefType_Holder(): TypedefType {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return TypedefType(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::TypedefType


