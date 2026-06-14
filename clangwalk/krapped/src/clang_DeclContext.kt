package clang

import clang.decl.Kind
import clang.decl.Kind.Companion.fromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.clangwalk.internal.clang_DeclContext_add_decl
import krapper.clangwalk.internal.clang_DeclContext_add_decl_internal
import krapper.clangwalk.internal.clang_DeclContext_add_hidden_decl
import krapper.clangwalk.internal.clang_DeclContext_align_of
import krapper.clangwalk.internal.clang_DeclContext_classof
import krapper.clangwalk.internal.clang_DeclContext_classof__const_clang_DeclContext_P
import krapper.clangwalk.internal.clang_DeclContext_contains_decl
import krapper.clangwalk.internal.clang_DeclContext_contains_decl_and_load
import krapper.clangwalk.internal.clang_DeclContext_decls
import krapper.clangwalk.internal.clang_DeclContext_decls_empty
import krapper.clangwalk.internal.clang_DeclContext_dispose
import krapper.clangwalk.internal.clang_DeclContext_dump_as_decl
import krapper.clangwalk.internal.clang_DeclContext_dump_as_decl__const_clang_ASTContext_P
import krapper.clangwalk.internal.clang_DeclContext_dump_decl_context
import krapper.clangwalk.internal.clang_DeclContext_dump_lookups
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_CXXMethodDecl
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_CXXRecordDecl
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_FunctionDecl
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_RecordDecl
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_TagDecl
import krapper.clangwalk.internal.clang_DeclContext_dyncast_clang_TranslationUnitDecl
import krapper.clangwalk.internal.clang_DeclContext_encloses
import krapper.clangwalk.internal.clang_DeclContext_equals
import krapper.clangwalk.internal.clang_DeclContext_get_decl_kind
import krapper.clangwalk.internal.clang_DeclContext_get_decl_kind_name
import krapper.clangwalk.internal.clang_DeclContext_get_enclosing_namespace_context
import krapper.clangwalk.internal.clang_DeclContext_get_lexical_parent
import krapper.clangwalk.internal.clang_DeclContext_get_lookup_parent
import krapper.clangwalk.internal.clang_DeclContext_get_non_closure_ancestor
import krapper.clangwalk.internal.clang_DeclContext_get_non_transparent_context
import krapper.clangwalk.internal.clang_DeclContext_get_outer_lexical_record_context
import krapper.clangwalk.internal.clang_DeclContext_get_parent
import krapper.clangwalk.internal.clang_DeclContext_get_parent_ast_context
import krapper.clangwalk.internal.clang_DeclContext_get_primary_context
import krapper.clangwalk.internal.clang_DeclContext_get_redecl_context
import krapper.clangwalk.internal.clang_DeclContext_has_external_lexical_storage
import krapper.clangwalk.internal.clang_DeclContext_has_external_visible_storage
import krapper.clangwalk.internal.clang_DeclContext_has_valid_decl_kind
import krapper.clangwalk.internal.clang_DeclContext_in_enclosing_namespace_set_of
import krapper.clangwalk.internal.clang_DeclContext_is_closure
import krapper.clangwalk.internal.clang_DeclContext_is_decl_in_lexical_traversal
import krapper.clangwalk.internal.clang_DeclContext_is_dependent_context
import krapper.clangwalk.internal.clang_DeclContext_is_extern_c_context
import krapper.clangwalk.internal.clang_DeclContext_is_extern_cxx_context
import krapper.clangwalk.internal.clang_DeclContext_is_file_context
import krapper.clangwalk.internal.clang_DeclContext_is_function_or_method
import krapper.clangwalk.internal.clang_DeclContext_is_inline_namespace
import krapper.clangwalk.internal.clang_DeclContext_is_lookup_context
import krapper.clangwalk.internal.clang_DeclContext_is_namespace
import krapper.clangwalk.internal.clang_DeclContext_is_obj_c_container
import krapper.clangwalk.internal.clang_DeclContext_is_record
import krapper.clangwalk.internal.clang_DeclContext_is_requires_expr_body
import krapper.clangwalk.internal.clang_DeclContext_is_std_namespace
import krapper.clangwalk.internal.clang_DeclContext_is_translation_unit
import krapper.clangwalk.internal.clang_DeclContext_is_transparent_context
import krapper.clangwalk.internal.clang_DeclContext_lexically_encloses
import krapper.clangwalk.internal.clang_DeclContext_make_decl_visible_in_context
import krapper.clangwalk.internal.clang_DeclContext_noload_decls
import krapper.clangwalk.internal.clang_DeclContext_remove_decl
import krapper.clangwalk.internal.clang_DeclContext_set_has_external_lexical_storage
import krapper.clangwalk.internal.clang_DeclContext_set_has_external_visible_storage
import krapper.clangwalk.internal.clang_DeclContext_set_must_build_lookup_table
import krapper.clangwalk.internal.clang_DeclContext_set_use_qualified_lookup
import krapper.clangwalk.internal.clang_DeclContext_should_use_qualified_lookup
import krapper.clangwalk.internal.clang_DeclContext_size_of
import std.Vector__Decl_P
import std.Vector__Decl_P.Companion.Vector__Decl_P_Holder

// BEGIN KRAPPER GEN for clang::DeclContext

@krapper.CppBinding("clang::DeclContext")
class DeclContext(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun hasValidDeclKind(): Boolean {
        return clang_DeclContext_has_valid_decl_kind(ptr)
    }
    inline fun getDeclKind(): Kind {
        return fromValue(clang_DeclContext_get_decl_kind(ptr))
    }
    inline fun getDeclKindName(): String? {
        val str: CPointer<ByteVar>? = clang_DeclContext_get_decl_kind_name(ptr)
        val ret: String? = str?.toKString()
        return ret
    }
    inline fun getParent(): DeclContext? {
        return DeclContext((clang_DeclContext_get_parent(ptr) ?: return null), memScope)
    }
    inline fun getLexicalParent(): DeclContext? {
        return DeclContext((clang_DeclContext_get_lexical_parent(ptr) ?: return null), memScope)
    }
    inline fun getLookupParent(): DeclContext? {
        return DeclContext((clang_DeclContext_get_lookup_parent(ptr) ?: return null), memScope)
    }
    inline fun getParentASTContext(): ASTContext? {
        return ASTContext((clang_DeclContext_get_parent_ast_context(ptr) ?: return null), memScope)
    }
    inline fun isClosure(): Boolean {
        return clang_DeclContext_is_closure(ptr)
    }
    inline fun isObjCContainer(): Boolean {
        return clang_DeclContext_is_obj_c_container(ptr)
    }
    inline fun isFunctionOrMethod(): Boolean {
        return clang_DeclContext_is_function_or_method(ptr)
    }
    inline fun isLookupContext(): Boolean {
        return clang_DeclContext_is_lookup_context(ptr)
    }
    inline fun isFileContext(): Boolean {
        return clang_DeclContext_is_file_context(ptr)
    }
    inline fun isTranslationUnit(): Boolean {
        return clang_DeclContext_is_translation_unit(ptr)
    }
    inline fun isRecord(): Boolean {
        return clang_DeclContext_is_record(ptr)
    }
    inline fun isRequiresExprBody(): Boolean {
        return clang_DeclContext_is_requires_expr_body(ptr)
    }
    inline fun isNamespace(): Boolean {
        return clang_DeclContext_is_namespace(ptr)
    }
    inline fun isStdNamespace(): Boolean {
        return clang_DeclContext_is_std_namespace(ptr)
    }
    inline fun isInlineNamespace(): Boolean {
        return clang_DeclContext_is_inline_namespace(ptr)
    }
    inline fun isDependentContext(): Boolean {
        return clang_DeclContext_is_dependent_context(ptr)
    }
    inline fun isTransparentContext(): Boolean {
        return clang_DeclContext_is_transparent_context(ptr)
    }
    inline fun isExternCContext(): Boolean {
        return clang_DeclContext_is_extern_c_context(ptr)
    }
    inline fun isExternCXXContext(): Boolean {
        return clang_DeclContext_is_extern_cxx_context(ptr)
    }
    inline fun Equals(DC: DeclContext?): Boolean {
        return clang_DeclContext_equals(ptr, DC?.ptr)
    }
    inline fun Encloses(DC: DeclContext?): Boolean {
        return clang_DeclContext_encloses(ptr, DC?.ptr)
    }
    inline fun LexicallyEncloses(DC: DeclContext?): Boolean {
        return clang_DeclContext_lexically_encloses(ptr, DC?.ptr)
    }
    inline fun getNonClosureAncestor(): DeclApi? {
        return Decl((clang_DeclContext_get_non_closure_ancestor(ptr) ?: return null), memScope)
    }
    inline fun getNonTransparentContext(): DeclContext? {
        return DeclContext((clang_DeclContext_get_non_transparent_context(ptr) ?: return null), memScope)
    }
    inline fun getPrimaryContext(): DeclContext? {
        return DeclContext((clang_DeclContext_get_primary_context(ptr) ?: return null), memScope)
    }
    inline fun getRedeclContext(): DeclContext? {
        return DeclContext((clang_DeclContext_get_redecl_context(ptr) ?: return null), memScope)
    }
    inline fun getEnclosingNamespaceContext(): DeclContext? {
        return DeclContext((clang_DeclContext_get_enclosing_namespace_context(ptr) ?: return null), memScope)
    }
    inline fun getOuterLexicalRecordContext(): RecordDeclApi? {
        return RecordDecl((clang_DeclContext_get_outer_lexical_record_context(ptr) ?: return null), memScope)
    }
    inline fun InEnclosingNamespaceSetOf(NS: DeclContext?): Boolean {
        return clang_DeclContext_in_enclosing_namespace_set_of(ptr, NS?.ptr)
    }
    inline fun decls(): Vector__Decl_P {
        val retValue: Vector__Decl_P = memScope.Vector__Decl_P_Holder()
        clang_DeclContext_decls(ptr, retValue.ptr)
        return retValue
    }
    inline fun decls_empty(): Boolean {
        return clang_DeclContext_decls_empty(ptr)
    }
    inline fun noload_decls(): Vector__Decl_P {
        val retValue: Vector__Decl_P = memScope.Vector__Decl_P_Holder()
        clang_DeclContext_noload_decls(ptr, retValue.ptr)
        return retValue
    }
    inline fun addDecl(D: DeclApi?): Unit {
        return clang_DeclContext_add_decl(ptr, D?.ptr)
    }
    inline fun addDeclInternal(D: DeclApi?): Unit {
        return clang_DeclContext_add_decl_internal(ptr, D?.ptr)
    }
    inline fun addHiddenDecl(D: DeclApi?): Unit {
        return clang_DeclContext_add_hidden_decl(ptr, D?.ptr)
    }
    inline fun removeDecl(D: DeclApi?): Unit {
        return clang_DeclContext_remove_decl(ptr, D?.ptr)
    }
    inline fun containsDecl(D: DeclApi?): Boolean {
        return clang_DeclContext_contains_decl(ptr, D?.ptr)
    }
    inline fun containsDeclAndLoad(D: DeclApi?): Boolean {
        return clang_DeclContext_contains_decl_and_load(ptr, D?.ptr)
    }
    inline fun makeDeclVisibleInContext(D: NamedDeclApi?): Unit {
        return clang_DeclContext_make_decl_visible_in_context(ptr, D?.ptr)
    }
    inline fun setMustBuildLookupTable(): Unit {
        return clang_DeclContext_set_must_build_lookup_table(ptr)
    }
    inline fun hasExternalLexicalStorage(): Boolean {
        return clang_DeclContext_has_external_lexical_storage(ptr)
    }
    inline fun setHasExternalLexicalStorage(ES: Boolean = true): Unit {
        return clang_DeclContext_set_has_external_lexical_storage(ptr, ES)
    }
    inline fun hasExternalVisibleStorage(): Boolean {
        return clang_DeclContext_has_external_visible_storage(ptr)
    }
    inline fun setHasExternalVisibleStorage(ES: Boolean = true): Unit {
        return clang_DeclContext_set_has_external_visible_storage(ptr, ES)
    }
    inline fun isDeclInLexicalTraversal(D: DeclApi?): Boolean {
        return clang_DeclContext_is_decl_in_lexical_traversal(ptr, D?.ptr)
    }
    inline fun setUseQualifiedLookup(use: Boolean = true): Unit {
        return clang_DeclContext_set_use_qualified_lookup(ptr, use)
    }
    inline fun shouldUseQualifiedLookup(): Boolean {
        return clang_DeclContext_should_use_qualified_lookup(ptr)
    }
    inline fun dumpAsDecl(): Unit {
        return clang_DeclContext_dump_as_decl(ptr)
    }
    inline fun dumpAsDecl__const_clang_ASTContext_P(Ctx: ASTContext?): Unit {
        return clang_DeclContext_dump_as_decl__const_clang_ASTContext_P(ptr, Ctx?.ptr)
    }
    inline fun dumpDeclContext(): Unit {
        return clang_DeclContext_dump_decl_context(ptr)
    }
    inline fun dumpLookups(): Unit {
        return clang_DeclContext_dump_lookups(ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_DeclContext_size_of()
            }

        val align: Int
            inline get() {
                return clang_DeclContext_align_of()
            }

        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_DeclContext_classof(D?.ptr)
        }
        inline fun MemScope._classof(D: DeclContext?): Boolean {
            return clang_DeclContext_classof__const_clang_DeclContext_P(D?.ptr)
        }
        fun MemScope.DeclContext_Holder(): DeclContext {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                clang_DeclContext_dispose(memory)
            }
            return DeclContext(memory, this)
        }
    }
    inline fun dispose(): Unit {
        clang_DeclContext_dispose(ptr)
    }
    inline fun owned(): DeclContext {
        memScope.defer {
            clang_DeclContext_dispose(ptr)
        }
        return this
    }
    inline fun asTagDecl(): TagDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_TagDecl(ptr) ?: return null)
        return TagDecl(raw, memScope)
    }
    inline fun asRecordDecl(): RecordDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_RecordDecl(ptr) ?: return null)
        return RecordDecl(raw, memScope)
    }
    inline fun asFunctionDecl(): FunctionDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_FunctionDecl(ptr) ?: return null)
        return FunctionDecl(raw, memScope)
    }
    inline fun asCXXMethodDecl(): CXXMethodDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_CXXMethodDecl(ptr) ?: return null)
        return CXXMethodDecl(raw, memScope)
    }
    inline fun asCXXRecordDecl(): CXXRecordDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_CXXRecordDecl(ptr) ?: return null)
        return CXXRecordDecl(raw, memScope)
    }
    inline fun asTranslationUnitDecl(): TranslationUnitDecl? {
        val raw: COpaquePointer = (clang_DeclContext_dyncast_clang_TranslationUnitDecl(ptr) ?: return null)
        return TranslationUnitDecl(raw, memScope)
    }
}

// END KRAPPER GEN for clang::DeclContext


