package clang

import clang.AccessSpecifier.Companion.fromValue
import clang.QualType.Companion.QualType_Holder
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_align_of
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_get_access_specifier
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_get_access_specifier_as_written
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_get_inherit_constructors
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_get_type
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_is_base_of_class
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_is_pack_expansion
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_is_virtual
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_new
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_set_inherit_constructors
import krapper.cppfrontend.internal.clang_CXXBaseSpecifier_size_of

// BEGIN KRAPPER GEN for clang::CXXBaseSpecifier

@krapper.CppBinding("clang::CXXBaseSpecifier")
class CXXBaseSpecifier(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun isVirtual(): Boolean {
        return clang_CXXBaseSpecifier_is_virtual(ptr)
    }
    inline fun isBaseOfClass(): Boolean {
        return clang_CXXBaseSpecifier_is_base_of_class(ptr)
    }
    inline fun isPackExpansion(): Boolean {
        return clang_CXXBaseSpecifier_is_pack_expansion(ptr)
    }
    inline fun getInheritConstructors(): Boolean {
        return clang_CXXBaseSpecifier_get_inherit_constructors(ptr)
    }
    inline fun setInheritConstructors(Inherit: Boolean = true): Unit {
        return clang_CXXBaseSpecifier_set_inherit_constructors(ptr, Inherit)
    }
    inline fun getAccessSpecifier(): AccessSpecifier {
        return fromValue(clang_CXXBaseSpecifier_get_access_specifier(ptr))
    }
    inline fun getAccessSpecifierAsWritten(): AccessSpecifier {
        return fromValue(clang_CXXBaseSpecifier_get_access_specifier_as_written(ptr))
    }
    inline fun getType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_CXXBaseSpecifier_get_type(ptr, retValue.ptr)
        return retValue
    }
    companion object {
        val size: Int
            inline get() {
                return clang_CXXBaseSpecifier_size_of()
            }

        val align: Int
            inline get() {
                return clang_CXXBaseSpecifier_align_of()
            }

        fun MemScope.CXXBaseSpecifier(): CXXBaseSpecifier {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (clang_CXXBaseSpecifier_new(memory) ?: error("Creation failed"))
            return CXXBaseSpecifier(obj, this)
        }
        fun MemScope.CXXBaseSpecifier_Holder(): CXXBaseSpecifier {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return CXXBaseSpecifier(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::CXXBaseSpecifier


