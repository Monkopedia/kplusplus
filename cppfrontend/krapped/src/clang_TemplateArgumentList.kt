package clang

import kotlin.Int
import kotlin.UInt
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.cppfrontend.internal.clang_TemplateArgumentList_align_of
import krapper.cppfrontend.internal.clang_TemplateArgumentList_data
import krapper.cppfrontend.internal.clang_TemplateArgumentList_get
import krapper.cppfrontend.internal.clang_TemplateArgumentList_op_ind
import krapper.cppfrontend.internal.clang_TemplateArgumentList_size
import krapper.cppfrontend.internal.clang_TemplateArgumentList_size_of

// BEGIN KRAPPER GEN for clang::TemplateArgumentList

@krapper.CppBinding("clang::TemplateArgumentList")
class TemplateArgumentList(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.collections.Iterable<clang.TemplateArgument?> {
    inline fun get(Idx: UInt): TemplateArgument? {
        return TemplateArgument((clang_TemplateArgumentList_get(ptr, Idx) ?: return null), memScope)
    }
    inline fun _get(Idx: UInt): TemplateArgument? {
        return TemplateArgument((clang_TemplateArgumentList_op_ind(ptr, Idx) ?: return null), memScope)
    }
    inline fun size(): UInt {
        return clang_TemplateArgumentList_size(ptr)
    }
    inline fun data(): TemplateArgument? {
        return TemplateArgument((clang_TemplateArgumentList_data(ptr) ?: return null), memScope)
    }
    override operator fun iterator(): kotlin.collections.Iterator<clang.TemplateArgument?> =
        object : kotlin.collections.Iterator<clang.TemplateArgument?> {
            private var __i = (size() - size())
            override fun hasNext(): Boolean = __i < size()
            override fun next(): clang.TemplateArgument? = get(__i++)
        }
    companion object {
        val _size: Int
            inline get() {
                return clang_TemplateArgumentList_size_of()
            }

        val align: Int
            inline get() {
                return clang_TemplateArgumentList_align_of()
            }

        fun MemScope.TemplateArgumentList_Holder(): TemplateArgumentList {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            return TemplateArgumentList(memory, this)
        }
    }
}

// END KRAPPER GEN for clang::TemplateArgumentList


