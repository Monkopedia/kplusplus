package clang

import clang.TranslationUnitKind.Companion.fromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.UInt
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.krapper_parse.internal.clang_ASTUnit_add_file_level_decl
import krapper.krapper_parse.internal.clang_ASTUnit_add_top_level_decl
import krapper.krapper_parse.internal.clang_ASTUnit_align_of
import krapper.krapper_parse.internal.clang_ASTUnit_cached_completion_size
import krapper.krapper_parse.internal.clang_ASTUnit_dispose
import krapper.krapper_parse.internal.clang_ASTUnit_enable_source_file_diagnostics
import krapper.krapper_parse.internal.clang_ASTUnit_get_ast_context
import krapper.krapper_parse.internal.clang_ASTUnit_get_ast_file_name
import krapper.krapper_parse.internal.clang_ASTUnit_get_current_top_level_hash_value
import krapper.krapper_parse.internal.clang_ASTUnit_get_main_file_name
import krapper.krapper_parse.internal.clang_ASTUnit_get_only_local_decls
import krapper.krapper_parse.internal.clang_ASTUnit_get_original_source_file_name
import krapper.krapper_parse.internal.clang_ASTUnit_get_owns_remapped_file_buffers
import krapper.krapper_parse.internal.clang_ASTUnit_get_preamble_counter_for_tests
import krapper.krapper_parse.internal.clang_ASTUnit_get_translation_unit_kind
import krapper.krapper_parse.internal.clang_ASTUnit_has_sema
import krapper.krapper_parse.internal.clang_ASTUnit_is_main_file_ast
import krapper.krapper_parse.internal.clang_ASTUnit_is_module_file
import krapper.krapper_parse.internal.clang_ASTUnit_is_unsafe_to_free
import krapper.krapper_parse.internal.clang_ASTUnit_reset_for_parse
import krapper.krapper_parse.internal.clang_ASTUnit_save
import krapper.krapper_parse.internal.clang_ASTUnit_set_owns_remapped_file_buffers
import krapper.krapper_parse.internal.clang_ASTUnit_set_unsafe_to_free
import krapper.krapper_parse.internal.clang_ASTUnit_size_of
import krapper.krapper_parse.internal.clang_ASTUnit_stored_diag_size
import krapper.krapper_parse.internal.clang_ASTUnit_top_level_empty
import krapper.krapper_parse.internal.clang_ASTUnit_top_level_size
import platform.linux.free
import platform.posix.size_t

// BEGIN KRAPPER GEN for clang::ASTUnit

@krapper.CppBinding("clang::ASTUnit")
class ASTUnit(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun isMainFileAST(): Boolean {
        return clang_ASTUnit_is_main_file_ast(ptr)
    }
    inline fun isUnsafeToFree(): Boolean {
        return clang_ASTUnit_is_unsafe_to_free(ptr)
    }
    inline fun setUnsafeToFree(Value: Boolean): Unit {
        return clang_ASTUnit_set_unsafe_to_free(ptr, Value)
    }
    inline fun getASTContext(): ASTContext? {
        return ASTContext((clang_ASTUnit_get_ast_context(ptr) ?: return null), memScope)
    }
    inline fun enableSourceFileDiagnostics(): Unit {
        return clang_ASTUnit_enable_source_file_diagnostics(ptr)
    }
    inline fun hasSema(): Boolean {
        return clang_ASTUnit_has_sema(ptr)
    }
    inline fun getOnlyLocalDecls(): Boolean {
        return clang_ASTUnit_get_only_local_decls(ptr)
    }
    inline fun getOwnsRemappedFileBuffers(): Boolean {
        return clang_ASTUnit_get_owns_remapped_file_buffers(ptr)
    }
    inline fun setOwnsRemappedFileBuffers(`val`: Boolean): Unit {
        return clang_ASTUnit_set_owns_remapped_file_buffers(ptr, `val`)
    }
    inline fun top_level_size(): size_t {
        return clang_ASTUnit_top_level_size(ptr)
    }
    inline fun top_level_empty(): Boolean {
        return clang_ASTUnit_top_level_empty(ptr)
    }
    inline fun addTopLevelDecl(D: DeclApi?): Unit {
        return clang_ASTUnit_add_top_level_decl(ptr, D?.ptr)
    }
    inline fun addFileLevelDecl(D: DeclApi?): Unit {
        return clang_ASTUnit_add_file_level_decl(ptr, D?.ptr)
    }
    inline fun getCurrentTopLevelHashValue(): CValuesRef<UIntVar>? {
        return clang_ASTUnit_get_current_top_level_hash_value(ptr)
    }
    inline fun getPreambleCounterForTests(): UInt {
        return clang_ASTUnit_get_preamble_counter_for_tests(ptr)
    }
    inline fun stored_diag_size(): UInt {
        return clang_ASTUnit_stored_diag_size(ptr)
    }
    inline fun cached_completion_size(): UInt {
        return clang_ASTUnit_cached_completion_size(ptr)
    }
    inline fun isModuleFile(): Boolean {
        return clang_ASTUnit_is_module_file(ptr)
    }
    inline fun getTranslationUnitKind(): TranslationUnitKind {
        return fromValue(clang_ASTUnit_get_translation_unit_kind(ptr))
    }
    inline fun ResetForParse(): Unit {
        return clang_ASTUnit_reset_for_parse(ptr)
    }
    inline fun Save(File: String?): Boolean {
        return clang_ASTUnit_save(ptr, File)
    }
    inline fun getOriginalSourceFileName(): String? {
        val str: CPointer<ByteVar>? = clang_ASTUnit_get_original_source_file_name(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getMainFileName(): String? {
        val str: CPointer<ByteVar>? = clang_ASTUnit_get_main_file_name(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getASTFileName(): String? {
        val str: CPointer<ByteVar>? = clang_ASTUnit_get_ast_file_name(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ASTUnit_size_of()
            }

        val align: Int
            inline get() {
                return clang_ASTUnit_align_of()
            }

        fun MemScope.ASTUnit_Holder(): ASTUnit {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                clang_ASTUnit_dispose(memory)
            }
            return ASTUnit(memory, this)
        }
    }
    inline fun dispose(): Unit {
        clang_ASTUnit_dispose(ptr)
    }
    inline fun owned(): ASTUnit {
        memScope.defer {
            clang_ASTUnit_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for clang::ASTUnit


