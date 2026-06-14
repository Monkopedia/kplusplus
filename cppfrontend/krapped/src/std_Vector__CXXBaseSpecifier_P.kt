package std

import clang.CXXBaseSpecifier
import kotlin.Boolean
import kotlin.Int
import kotlin.Unit
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_align_of
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_at
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_back
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_capacity
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_clear
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_data
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_dispose
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_empty
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_front
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_max_size
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_new
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_new__const_std_vector_and
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_new__size_t_const_allocator_type_and
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_op_assign
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_op_ind
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_pop_back
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_reserve
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_resize
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_shrink_to_fit
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_size
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_size_of
import krapper.cppfrontend.internal.std_vector_clang_CXXBaseSpecifier_P_swap
import platform.posix.size_t

// BEGIN KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>

@krapper.CppBinding("std::vector<clang::CXXBaseSpecifier*>")
class Vector__CXXBaseSpecifier_P(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) : kotlin.collections.Iterable<clang.CXXBaseSpecifier?> {
    inline infix fun assign(__x: Vector__CXXBaseSpecifier_P): Vector__CXXBaseSpecifier_P? {
        return Vector__CXXBaseSpecifier_P((std_vector_clang_CXXBaseSpecifier_P_op_assign(ptr, __x.ptr) ?: return null), memScope)
    }
    inline fun size(): size_t {
        return std_vector_clang_CXXBaseSpecifier_P_size(ptr)
    }
    inline fun max_size(): size_t {
        return std_vector_clang_CXXBaseSpecifier_P_max_size(ptr)
    }
    inline fun resize(__new_size: size_t): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_resize(ptr, __new_size)
    }
    inline fun shrink_to_fit(): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_shrink_to_fit(ptr)
    }
    inline fun capacity(): size_t {
        return std_vector_clang_CXXBaseSpecifier_P_capacity(ptr)
    }
    inline fun empty(): Boolean {
        return std_vector_clang_CXXBaseSpecifier_P_empty(ptr)
    }
    inline fun reserve(__n: size_t): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_reserve(ptr, __n)
    }
    inline operator fun get(__n: size_t): CXXBaseSpecifier? {
        return CXXBaseSpecifier((std_vector_clang_CXXBaseSpecifier_P_op_ind(ptr, __n) ?: return null), memScope)
    }
    inline fun at(__n: size_t): CXXBaseSpecifier? {
        return CXXBaseSpecifier((std_vector_clang_CXXBaseSpecifier_P_at(ptr, __n) ?: return null), memScope)
    }
    inline fun front(): CXXBaseSpecifier? {
        return CXXBaseSpecifier((std_vector_clang_CXXBaseSpecifier_P_front(ptr) ?: return null), memScope)
    }
    inline fun back(): CXXBaseSpecifier? {
        return CXXBaseSpecifier((std_vector_clang_CXXBaseSpecifier_P_back(ptr) ?: return null), memScope)
    }
    inline fun data(): CXXBaseSpecifier? {
        return CXXBaseSpecifier((std_vector_clang_CXXBaseSpecifier_P_data(ptr) ?: return null), memScope)
    }
    inline fun pop_back(): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_pop_back(ptr)
    }
    inline fun swap(__x: Vector__CXXBaseSpecifier_P): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_swap(ptr, __x.ptr)
    }
    inline fun clear(): Unit {
        return std_vector_clang_CXXBaseSpecifier_P_clear(ptr)
    }
    override operator fun iterator(): kotlin.collections.Iterator<clang.CXXBaseSpecifier?> =
        object : kotlin.collections.Iterator<clang.CXXBaseSpecifier?> {
            private var __i = (size() - size())
            override fun hasNext(): Boolean = __i < size()
            override fun next(): clang.CXXBaseSpecifier? = get(__i++)
        }
    companion object {
        val _size: Int
            inline get() {
                return std_vector_clang_CXXBaseSpecifier_P_size_of()
            }

        val align: Int
            inline get() {
                return std_vector_clang_CXXBaseSpecifier_P_align_of()
            }

        fun MemScope.Vector__CXXBaseSpecifier_P(): Vector__CXXBaseSpecifier_P {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_clang_CXXBaseSpecifier_P_new(memory) ?: error("Creation failed"))
            defer {
                std_vector_clang_CXXBaseSpecifier_P_dispose(obj)
            }
            return Vector__CXXBaseSpecifier_P(obj, this)
        }
        fun MemScope.Vector__CXXBaseSpecifier_P__size_t(__n: size_t): Vector__CXXBaseSpecifier_P {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_clang_CXXBaseSpecifier_P_new__size_t_const_allocator_type_and(memory, __n) ?: error("Creation failed"))
            defer {
                std_vector_clang_CXXBaseSpecifier_P_dispose(obj)
            }
            return Vector__CXXBaseSpecifier_P(obj, this)
        }
        fun MemScope.Vector__CXXBaseSpecifier_P__const_std_vector_clang_CXXBaseSpecifier_P(__x: Vector__CXXBaseSpecifier_P): Vector__CXXBaseSpecifier_P {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            val obj: COpaquePointer = (std_vector_clang_CXXBaseSpecifier_P_new__const_std_vector_and(memory, __x.ptr) ?: error("Creation failed"))
            defer {
                std_vector_clang_CXXBaseSpecifier_P_dispose(obj)
            }
            return Vector__CXXBaseSpecifier_P(obj, this)
        }
        fun MemScope.Vector__CXXBaseSpecifier_P_Holder(): Vector__CXXBaseSpecifier_P {
            val memory: COpaquePointer = (interpretCPointer(alloc(_size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                std_vector_clang_CXXBaseSpecifier_P_dispose(memory)
            }
            return Vector__CXXBaseSpecifier_P(memory, this)
        }
    }
    inline fun dispose(): Unit {
        std_vector_clang_CXXBaseSpecifier_P_dispose(ptr)
    }
    inline fun owned(): Vector__CXXBaseSpecifier_P {
        memScope.defer {
            std_vector_clang_CXXBaseSpecifier_P_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>


