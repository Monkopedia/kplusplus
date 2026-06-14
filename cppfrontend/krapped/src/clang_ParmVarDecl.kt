package clang

import clang.AccessSpecifier.Companion.fromValue as AccessSpecifierCompanionfromValue
import clang.AvailabilityResult.Companion.fromValue as AvailabilityResultCompanionfromValue
import clang.LanguageLinkage.Companion.fromValue as LanguageLinkageCompanionfromValue
import clang.Linkage.Companion.fromValue as LinkageCompanionfromValue
import clang.ObjCStringFormatFamily.Companion.fromValue as ObjCStringFormatFamilyCompanionfromValue
import clang.QualType.Companion.QualType_Holder
import clang.StorageClass.Companion.fromValue
import clang.StorageDuration.Companion.fromValue as StorageDurationCompanionfromValue
import clang.TemplateSpecializationKind.Companion.fromValue as TemplateSpecializationKindCompanionfromValue
import clang.ThreadStorageClassSpecifier.Companion.fromValue as CompanionfromValue
import clang.Visibility.Companion.fromValue as VisibilityCompanionfromValue
import clang.decl.FriendObjectKind
import clang.decl.FriendObjectKind.Companion.fromValue as FriendObjectKindCompanionfromValue
import clang.decl.Kind
import clang.decl.Kind.Companion.fromValue as KindCompanionfromValue
import clang.decl.ModuleOwnershipKind
import clang.decl.ModuleOwnershipKind.Companion.fromValue as ModuleOwnershipKindCompanionfromValue
import clang.decl.ObjCDeclQualifier
import clang.decl.ObjCDeclQualifier.Companion.fromValue as ObjCDeclQualifierCompanionfromValue
import clang.qualType.DestructionKind
import clang.qualType.DestructionKind.Companion.fromValue as DestructionKindCompanionfromValue
import clang.varDecl.DefinitionKind
import clang.varDecl.DefinitionKind.Companion.fromValue as DefinitionKindCompanionfromValue
import clang.varDecl.InitializationStyle
import clang.varDecl.InitializationStyle.Companion.fromValue as InitializationStyleCompanionfromValue
import clang.varDecl.TLSKind
import clang.varDecl.TLSKind.Companion.fromValue as TLSKindCompanionfromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.UInt
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.cppfrontend.internal._clang_VarDecl_get_definition
import krapper.cppfrontend.internal._clang_VarDecl_has_definition
import krapper.cppfrontend.internal._clang_VarDecl_is_this_declaration_a_definition
import krapper.cppfrontend.internal.clang_Decl_can_be_weak_imported
import krapper.cppfrontend.internal.clang_Decl_clear_identifier_namespace
import krapper.cppfrontend.internal.clang_Decl_drop_attrs
import krapper.cppfrontend.internal.clang_Decl_dump
import krapper.cppfrontend.internal.clang_Decl_dump_color
import krapper.cppfrontend.internal.clang_Decl_get_access
import krapper.cppfrontend.internal.clang_Decl_get_access_unsafe
import krapper.cppfrontend.internal.clang_Decl_get_as_function
import krapper.cppfrontend.internal.clang_Decl_get_ast_context
import krapper.cppfrontend.internal.clang_Decl_get_availability
import krapper.cppfrontend.internal.clang_Decl_get_decl_context
import krapper.cppfrontend.internal.clang_Decl_get_decl_kind_name
import krapper.cppfrontend.internal.clang_Decl_get_described_template
import krapper.cppfrontend.internal.clang_Decl_get_described_template_params
import krapper.cppfrontend.internal.clang_Decl_get_friend_object_kind
import krapper.cppfrontend.internal.clang_Decl_get_function_type
import krapper.cppfrontend.internal.clang_Decl_get_id
import krapper.cppfrontend.internal.clang_Decl_get_identifier_namespace
import krapper.cppfrontend.internal.clang_Decl_get_kind
import krapper.cppfrontend.internal.clang_Decl_get_lexical_decl_context
import krapper.cppfrontend.internal.clang_Decl_get_max_alignment
import krapper.cppfrontend.internal.clang_Decl_get_module_ownership_kind
import krapper.cppfrontend.internal.clang_Decl_get_next_decl_in_context
import krapper.cppfrontend.internal.clang_Decl_get_non_closure_context
import krapper.cppfrontend.internal.clang_Decl_get_non_transparent_decl_context
import krapper.cppfrontend.internal.clang_Decl_get_owning_module_id
import krapper.cppfrontend.internal.clang_Decl_get_parent_function_or_method
import krapper.cppfrontend.internal.clang_Decl_get_previous_decl
import krapper.cppfrontend.internal.clang_Decl_get_template_depth
import krapper.cppfrontend.internal.clang_Decl_get_translation_unit_decl
import krapper.cppfrontend.internal.clang_Decl_has_attrs
import krapper.cppfrontend.internal.clang_Decl_has_body
import krapper.cppfrontend.internal.clang_Decl_has_defining_attr
import krapper.cppfrontend.internal.clang_Decl_has_owning_module
import krapper.cppfrontend.internal.clang_Decl_has_tag_identifier_namespace
import krapper.cppfrontend.internal.clang_Decl_invalidate_cached_linkage
import krapper.cppfrontend.internal.clang_Decl_is_canonical_decl
import krapper.cppfrontend.internal.clang_Decl_is_defined_outside_function_or_method
import krapper.cppfrontend.internal.clang_Decl_is_deprecated
import krapper.cppfrontend.internal.clang_Decl_is_file_context_decl
import krapper.cppfrontend.internal.clang_Decl_is_first_decl
import krapper.cppfrontend.internal.clang_Decl_is_from_ast_file
import krapper.cppfrontend.internal.clang_Decl_is_from_explicit_global_module
import krapper.cppfrontend.internal.clang_Decl_is_from_global_module
import krapper.cppfrontend.internal.clang_Decl_is_from_header_unit
import krapper.cppfrontend.internal.clang_Decl_is_function_or_function_template
import krapper.cppfrontend.internal.clang_Decl_is_function_pointer_type
import krapper.cppfrontend.internal.clang_Decl_is_implicit
import krapper.cppfrontend.internal.clang_Decl_is_in_anonymous_namespace
import krapper.cppfrontend.internal.clang_Decl_is_in_another_module_unit
import krapper.cppfrontend.internal.clang_Decl_is_in_current_module_unit
import krapper.cppfrontend.internal.clang_Decl_is_in_export_decl_context
import krapper.cppfrontend.internal.clang_Decl_is_in_identifier_namespace
import krapper.cppfrontend.internal.clang_Decl_is_in_local_scope_for_instantiation
import krapper.cppfrontend.internal.clang_Decl_is_in_named_module
import krapper.cppfrontend.internal.clang_Decl_is_in_std_namespace
import krapper.cppfrontend.internal.clang_Decl_is_invalid_decl
import krapper.cppfrontend.internal.clang_Decl_is_invisible_outside_the_owning_module
import krapper.cppfrontend.internal.clang_Decl_is_local_extern_decl
import krapper.cppfrontend.internal.clang_Decl_is_module_local
import krapper.cppfrontend.internal.clang_Decl_is_module_private
import krapper.cppfrontend.internal.clang_Decl_is_reachable
import krapper.cppfrontend.internal.clang_Decl_is_referenced
import krapper.cppfrontend.internal.clang_Decl_is_template_decl
import krapper.cppfrontend.internal.clang_Decl_is_template_parameter
import krapper.cppfrontend.internal.clang_Decl_is_template_parameter_pack
import krapper.cppfrontend.internal.clang_Decl_is_templated
import krapper.cppfrontend.internal.clang_Decl_is_this_declaration_referenced
import krapper.cppfrontend.internal.clang_Decl_is_top_level_decl_in_obj_c_container
import krapper.cppfrontend.internal.clang_Decl_is_unavailable
import krapper.cppfrontend.internal.clang_Decl_is_unconditionally_visible
import krapper.cppfrontend.internal.clang_Decl_is_used
import krapper.cppfrontend.internal.clang_Decl_is_weak_imported
import krapper.cppfrontend.internal.clang_Decl_mark_used
import krapper.cppfrontend.internal.clang_Decl_redecls
import krapper.cppfrontend.internal.clang_Decl_set_access
import krapper.cppfrontend.internal.clang_Decl_set_decl_context
import krapper.cppfrontend.internal.clang_Decl_set_from_ast_file
import krapper.cppfrontend.internal.clang_Decl_set_implicit
import krapper.cppfrontend.internal.clang_Decl_set_invalid_decl
import krapper.cppfrontend.internal.clang_Decl_set_is_used
import krapper.cppfrontend.internal.clang_Decl_set_lexical_decl_context
import krapper.cppfrontend.internal.clang_Decl_set_local_extern_decl
import krapper.cppfrontend.internal.clang_Decl_set_module_ownership_kind
import krapper.cppfrontend.internal.clang_Decl_set_non_member_operator
import krapper.cppfrontend.internal.clang_Decl_set_object_of_friend_decl
import krapper.cppfrontend.internal.clang_Decl_set_owning_module_id
import krapper.cppfrontend.internal.clang_Decl_set_referenced
import krapper.cppfrontend.internal.clang_Decl_set_top_level_decl_in_obj_c_container
import krapper.cppfrontend.internal.clang_Decl_set_visible_despite_owning_module
import krapper.cppfrontend.internal.clang_Decl_set_visible_promoted
import krapper.cppfrontend.internal.clang_Decl_should_emit_in_external_source
import krapper.cppfrontend.internal.clang_DeclaratorDecl_get_num_template_parameter_lists
import krapper.cppfrontend.internal.clang_DeclaratorDecl_get_template_parameter_list
import krapper.cppfrontend.internal.clang_NamedDecl_declaration_replaces
import krapper.cppfrontend.internal.clang_NamedDecl_get_formal_linkage
import krapper.cppfrontend.internal.clang_NamedDecl_get_linkage_internal
import krapper.cppfrontend.internal.clang_NamedDecl_get_most_recent_decl
import krapper.cppfrontend.internal.clang_NamedDecl_get_name
import krapper.cppfrontend.internal.clang_NamedDecl_get_name_as_string
import krapper.cppfrontend.internal.clang_NamedDecl_get_obj_cf_string_formatting_family
import krapper.cppfrontend.internal.clang_NamedDecl_get_qualified_name_as_string
import krapper.cppfrontend.internal.clang_NamedDecl_get_underlying_decl
import krapper.cppfrontend.internal.clang_NamedDecl_get_visibility
import krapper.cppfrontend.internal.clang_NamedDecl_has_external_formal_linkage
import krapper.cppfrontend.internal.clang_NamedDecl_has_linkage
import krapper.cppfrontend.internal.clang_NamedDecl_has_linkage_been_computed
import krapper.cppfrontend.internal.clang_NamedDecl_is_cxx_class_member
import krapper.cppfrontend.internal.clang_NamedDecl_is_cxx_instance_member
import krapper.cppfrontend.internal.clang_NamedDecl_is_externally_declarable
import krapper.cppfrontend.internal.clang_NamedDecl_is_externally_visible
import krapper.cppfrontend.internal.clang_NamedDecl_is_linkage_valid
import krapper.cppfrontend.internal.clang_ParmVarDecl_align_of
import krapper.cppfrontend.internal.clang_ParmVarDecl_as_clang_Decl
import krapper.cppfrontend.internal.clang_ParmVarDecl_as_clang_DeclaratorDecl
import krapper.cppfrontend.internal.clang_ParmVarDecl_as_clang_NamedDecl
import krapper.cppfrontend.internal.clang_ParmVarDecl_as_clang_ValueDecl
import krapper.cppfrontend.internal.clang_ParmVarDecl_as_clang_VarDecl
import krapper.cppfrontend.internal.clang_ParmVarDecl_classof
import krapper.cppfrontend.internal.clang_ParmVarDecl_classof_kind
import krapper.cppfrontend.internal.clang_ParmVarDecl_get_function_scope_depth
import krapper.cppfrontend.internal.clang_ParmVarDecl_get_function_scope_index
import krapper.cppfrontend.internal.clang_ParmVarDecl_get_max_function_scope_depth
import krapper.cppfrontend.internal.clang_ParmVarDecl_get_obj_c_decl_qualifier
import krapper.cppfrontend.internal.clang_ParmVarDecl_get_original_type
import krapper.cppfrontend.internal.clang_ParmVarDecl_has_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_has_inherited_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_has_uninstantiated_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_has_unparsed_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_is_destroyed_in_callee
import krapper.cppfrontend.internal.clang_ParmVarDecl_is_explicit_object_parameter
import krapper.cppfrontend.internal.clang_ParmVarDecl_is_knr_promoted
import krapper.cppfrontend.internal.clang_ParmVarDecl_is_obj_c_method_parameter
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_has_inherited_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_knr_promoted
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_obj_c_decl_qualifier
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_obj_c_method_scope_info
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_owning_function
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_scope_info
import krapper.cppfrontend.internal.clang_ParmVarDecl_set_unparsed_default_arg
import krapper.cppfrontend.internal.clang_ParmVarDecl_size_of
import krapper.cppfrontend.internal.clang_ValueDecl_get_potentially_decomposed_var_decl
import krapper.cppfrontend.internal.clang_ValueDecl_get_type
import krapper.cppfrontend.internal.clang_ValueDecl_is_parameter_pack
import krapper.cppfrontend.internal.clang_ValueDecl_is_weak
import krapper.cppfrontend.internal.clang_ValueDecl_set_type
import krapper.cppfrontend.internal.clang_VarDecl_demote_this_definition_to_declaration
import krapper.cppfrontend.internal.clang_VarDecl_get_acting_definition
import krapper.cppfrontend.internal.clang_VarDecl_get_canonical_decl
import krapper.cppfrontend.internal.clang_VarDecl_get_definition
import krapper.cppfrontend.internal.clang_VarDecl_get_init_style
import krapper.cppfrontend.internal.clang_VarDecl_get_initializing_declaration
import krapper.cppfrontend.internal.clang_VarDecl_get_instantiated_from_static_data_member
import krapper.cppfrontend.internal.clang_VarDecl_get_language_linkage
import krapper.cppfrontend.internal.clang_VarDecl_get_storage_class
import krapper.cppfrontend.internal.clang_VarDecl_get_storage_duration
import krapper.cppfrontend.internal.clang_VarDecl_get_template_instantiation_pattern
import krapper.cppfrontend.internal.clang_VarDecl_get_template_specialization_kind
import krapper.cppfrontend.internal.clang_VarDecl_get_template_specialization_kind_for_instantiation
import krapper.cppfrontend.internal.clang_VarDecl_get_tls_kind
import krapper.cppfrontend.internal.clang_VarDecl_get_tsc_spec
import krapper.cppfrontend.internal.clang_VarDecl_has_constant_initialization
import krapper.cppfrontend.internal.clang_VarDecl_has_definition
import krapper.cppfrontend.internal.clang_VarDecl_has_dependent_alignment
import krapper.cppfrontend.internal.clang_VarDecl_has_external_storage
import krapper.cppfrontend.internal.clang_VarDecl_has_flexible_array_init
import krapper.cppfrontend.internal.clang_VarDecl_has_global_storage
import krapper.cppfrontend.internal.clang_VarDecl_has_ice_initializer
import krapper.cppfrontend.internal.clang_VarDecl_has_init
import krapper.cppfrontend.internal.clang_VarDecl_has_init_with_side_effects
import krapper.cppfrontend.internal.clang_VarDecl_has_local_storage
import krapper.cppfrontend.internal.clang_VarDecl_is_arc_pseudo_strong
import krapper.cppfrontend.internal.clang_VarDecl_is_constexpr
import krapper.cppfrontend.internal.clang_VarDecl_is_cxx_cond_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_cxx_for_range_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_cxx_for_range_implicit_var
import krapper.cppfrontend.internal.clang_VarDecl_is_direct_init
import krapper.cppfrontend.internal.clang_VarDecl_is_escaping_byref
import krapper.cppfrontend.internal.clang_VarDecl_is_exception_variable
import krapper.cppfrontend.internal.clang_VarDecl_is_extern_c
import krapper.cppfrontend.internal.clang_VarDecl_is_file_var_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_function_or_method_var_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_in_extern_c_context
import krapper.cppfrontend.internal.clang_VarDecl_is_in_extern_cxx_context
import krapper.cppfrontend.internal.clang_VarDecl_is_init_capture
import krapper.cppfrontend.internal.clang_VarDecl_is_inline
import krapper.cppfrontend.internal.clang_VarDecl_is_inline_specified
import krapper.cppfrontend.internal.clang_VarDecl_is_known_to_be_defined
import krapper.cppfrontend.internal.clang_VarDecl_is_local_var_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_local_var_decl_or_parm
import krapper.cppfrontend.internal.clang_VarDecl_is_no_destroy
import krapper.cppfrontend.internal.clang_VarDecl_is_non_escaping_byref
import krapper.cppfrontend.internal.clang_VarDecl_is_nrvo_variable
import krapper.cppfrontend.internal.clang_VarDecl_is_obj_c_for_decl
import krapper.cppfrontend.internal.clang_VarDecl_is_out_of_line
import krapper.cppfrontend.internal.clang_VarDecl_is_previous_decl_in_same_block_scope
import krapper.cppfrontend.internal.clang_VarDecl_is_static_data_member
import krapper.cppfrontend.internal.clang_VarDecl_is_static_local
import krapper.cppfrontend.internal.clang_VarDecl_is_this_declaration_a_definition
import krapper.cppfrontend.internal.clang_VarDecl_is_this_declaration_a_demoted_definition
import krapper.cppfrontend.internal.clang_VarDecl_is_usable_in_constant_expressions
import krapper.cppfrontend.internal.clang_VarDecl_might_be_usable_in_constant_expressions
import krapper.cppfrontend.internal.clang_VarDecl_needs_destruction
import krapper.cppfrontend.internal.clang_VarDecl_set_arc_pseudo_strong
import krapper.cppfrontend.internal.clang_VarDecl_set_constexpr
import krapper.cppfrontend.internal.clang_VarDecl_set_cxx_cond_decl
import krapper.cppfrontend.internal.clang_VarDecl_set_cxx_for_range_decl
import krapper.cppfrontend.internal.clang_VarDecl_set_cxx_for_range_implicit_var
import krapper.cppfrontend.internal.clang_VarDecl_set_escaping_byref
import krapper.cppfrontend.internal.clang_VarDecl_set_exception_variable
import krapper.cppfrontend.internal.clang_VarDecl_set_implicitly_inline
import krapper.cppfrontend.internal.clang_VarDecl_set_init_capture
import krapper.cppfrontend.internal.clang_VarDecl_set_init_style
import krapper.cppfrontend.internal.clang_VarDecl_set_inline_specified
import krapper.cppfrontend.internal.clang_VarDecl_set_instantiation_of_static_data_member
import krapper.cppfrontend.internal.clang_VarDecl_set_nrvo_variable
import krapper.cppfrontend.internal.clang_VarDecl_set_obj_c_for_decl
import krapper.cppfrontend.internal.clang_VarDecl_set_previous_decl_in_same_block_scope
import krapper.cppfrontend.internal.clang_VarDecl_set_storage_class
import krapper.cppfrontend.internal.clang_VarDecl_set_template_specialization_kind
import krapper.cppfrontend.internal.clang_VarDecl_set_tsc_spec
import platform.linux.free
import std.Vector__Decl_P
import std.Vector__Decl_P.Companion.Vector__Decl_P_Holder

// BEGIN KRAPPER GEN for clang::ParmVarDecl

@krapper.CppBinding("clang::ParmVarDecl")
class ParmVarDecl(
    override val ptr: COpaquePointer,
    val memScope: MemScope,
) : clang.VarDeclApi, clang.DeclaratorDeclApi, clang.ValueDeclApi, clang.NamedDeclApi, clang.DeclApi {
    inline fun getStorageClass(): StorageClass {
        return fromValue(clang_VarDecl_get_storage_class(ptr))
    }
    inline fun setStorageClass(SC: StorageClass): Unit {
        return clang_VarDecl_set_storage_class(ptr, SC.value)
    }
    inline fun setTSCSpec(TSC: ThreadStorageClassSpecifier): Unit {
        return clang_VarDecl_set_tsc_spec(ptr, TSC.value)
    }
    inline fun getTSCSpec(): ThreadStorageClassSpecifier {
        return CompanionfromValue(clang_VarDecl_get_tsc_spec(ptr))
    }
    inline fun getTLSKind(): TLSKind {
        return TLSKindCompanionfromValue(clang_VarDecl_get_tls_kind(ptr))
    }
    inline fun hasLocalStorage(): Boolean {
        return clang_VarDecl_has_local_storage(ptr)
    }
    inline fun isStaticLocal(): Boolean {
        return clang_VarDecl_is_static_local(ptr)
    }
    inline fun hasExternalStorage(): Boolean {
        return clang_VarDecl_has_external_storage(ptr)
    }
    inline fun hasGlobalStorage(): Boolean {
        return clang_VarDecl_has_global_storage(ptr)
    }
    inline fun getStorageDuration(): StorageDuration {
        return StorageDurationCompanionfromValue(clang_VarDecl_get_storage_duration(ptr))
    }
    inline fun getLanguageLinkage(): LanguageLinkage {
        return LanguageLinkageCompanionfromValue(clang_VarDecl_get_language_linkage(ptr))
    }
    inline fun isExternC(): Boolean {
        return clang_VarDecl_is_extern_c(ptr)
    }
    inline fun isInExternCContext(): Boolean {
        return clang_VarDecl_is_in_extern_c_context(ptr)
    }
    inline fun isInExternCXXContext(): Boolean {
        return clang_VarDecl_is_in_extern_cxx_context(ptr)
    }
    inline fun isLocalVarDecl(): Boolean {
        return clang_VarDecl_is_local_var_decl(ptr)
    }
    inline fun isLocalVarDeclOrParm(): Boolean {
        return clang_VarDecl_is_local_var_decl_or_parm(ptr)
    }
    inline fun isFunctionOrMethodVarDecl(): Boolean {
        return clang_VarDecl_is_function_or_method_var_decl(ptr)
    }
    inline fun isStaticDataMember(): Boolean {
        return clang_VarDecl_is_static_data_member(ptr)
    }
    inline fun varDeclGetCanonicalDecl(): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    inline fun isThisDeclarationADefinition__clang_ASTContext(_arg_0: ASTContext): DefinitionKind {
        return DefinitionKindCompanionfromValue(clang_VarDecl_is_this_declaration_a_definition(ptr, _arg_0.ptr))
    }
    inline fun isThisDeclarationADefinition(): DefinitionKind {
        return DefinitionKindCompanionfromValue(_clang_VarDecl_is_this_declaration_a_definition(ptr))
    }
    inline fun hasDefinition__clang_ASTContext(_arg_0: ASTContext): DefinitionKind {
        return DefinitionKindCompanionfromValue(clang_VarDecl_has_definition(ptr, _arg_0.ptr))
    }
    inline fun hasDefinition(): DefinitionKind {
        return DefinitionKindCompanionfromValue(_clang_VarDecl_has_definition(ptr))
    }
    inline fun getActingDefinition(): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_acting_definition(ptr) ?: return null), memScope)
    }
    inline fun getDefinition__clang_ASTContext(C: ASTContext): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_definition(ptr, C.ptr) ?: return null), memScope)
    }
    inline fun getDefinition(): VarDeclApi? {
        return VarDecl((_clang_VarDecl_get_definition(ptr) ?: return null), memScope)
    }
    override fun isOutOfLine(): Boolean {
        return clang_VarDecl_is_out_of_line(ptr)
    }
    inline fun isFileVarDecl(): Boolean {
        return clang_VarDecl_is_file_var_decl(ptr)
    }
    inline fun hasInit(): Boolean {
        return clang_VarDecl_has_init(ptr)
    }
    inline fun getInitializingDeclaration(): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_initializing_declaration(ptr) ?: return null), memScope)
    }
    inline fun hasInitWithSideEffects(): Boolean {
        return clang_VarDecl_has_init_with_side_effects(ptr)
    }
    inline fun mightBeUsableInConstantExpressions(C: ASTContext): Boolean {
        return clang_VarDecl_might_be_usable_in_constant_expressions(ptr, C.ptr)
    }
    inline fun isUsableInConstantExpressions(C: ASTContext): Boolean {
        return clang_VarDecl_is_usable_in_constant_expressions(ptr, C.ptr)
    }
    inline fun hasConstantInitialization(): Boolean {
        return clang_VarDecl_has_constant_initialization(ptr)
    }
    inline fun hasICEInitializer(Context: ASTContext): Boolean {
        return clang_VarDecl_has_ice_initializer(ptr, Context.ptr)
    }
    inline fun setInitStyle(Style: InitializationStyle): Unit {
        return clang_VarDecl_set_init_style(ptr, Style.value)
    }
    inline fun getInitStyle(): InitializationStyle {
        return InitializationStyleCompanionfromValue(clang_VarDecl_get_init_style(ptr))
    }
    inline fun isDirectInit(): Boolean {
        return clang_VarDecl_is_direct_init(ptr)
    }
    inline fun isThisDeclarationADemotedDefinition(): Boolean {
        return clang_VarDecl_is_this_declaration_a_demoted_definition(ptr)
    }
    inline fun demoteThisDefinitionToDeclaration(): Unit {
        return clang_VarDecl_demote_this_definition_to_declaration(ptr)
    }
    inline fun isExceptionVariable(): Boolean {
        return clang_VarDecl_is_exception_variable(ptr)
    }
    inline fun setExceptionVariable(EV: Boolean): Unit {
        return clang_VarDecl_set_exception_variable(ptr, EV)
    }
    inline fun isNRVOVariable(): Boolean {
        return clang_VarDecl_is_nrvo_variable(ptr)
    }
    inline fun setNRVOVariable(NRVO: Boolean): Unit {
        return clang_VarDecl_set_nrvo_variable(ptr, NRVO)
    }
    inline fun isCXXForRangeDecl(): Boolean {
        return clang_VarDecl_is_cxx_for_range_decl(ptr)
    }
    inline fun setCXXForRangeDecl(FRD: Boolean): Unit {
        return clang_VarDecl_set_cxx_for_range_decl(ptr, FRD)
    }
    inline fun isObjCForDecl(): Boolean {
        return clang_VarDecl_is_obj_c_for_decl(ptr)
    }
    inline fun setObjCForDecl(FRD: Boolean): Unit {
        return clang_VarDecl_set_obj_c_for_decl(ptr, FRD)
    }
    inline fun isARCPseudoStrong(): Boolean {
        return clang_VarDecl_is_arc_pseudo_strong(ptr)
    }
    inline fun setARCPseudoStrong(PS: Boolean): Unit {
        return clang_VarDecl_set_arc_pseudo_strong(ptr, PS)
    }
    inline fun isInline(): Boolean {
        return clang_VarDecl_is_inline(ptr)
    }
    inline fun isInlineSpecified(): Boolean {
        return clang_VarDecl_is_inline_specified(ptr)
    }
    inline fun setInlineSpecified(): Unit {
        return clang_VarDecl_set_inline_specified(ptr)
    }
    inline fun setImplicitlyInline(): Unit {
        return clang_VarDecl_set_implicitly_inline(ptr)
    }
    inline fun isConstexpr(): Boolean {
        return clang_VarDecl_is_constexpr(ptr)
    }
    inline fun setConstexpr(IC: Boolean): Unit {
        return clang_VarDecl_set_constexpr(ptr, IC)
    }
    inline fun varDeclIsInitCapture(): Boolean {
        return clang_VarDecl_is_init_capture(ptr)
    }
    inline fun setInitCapture(IC: Boolean): Unit {
        return clang_VarDecl_set_init_capture(ptr, IC)
    }
    inline fun isPreviousDeclInSameBlockScope(): Boolean {
        return clang_VarDecl_is_previous_decl_in_same_block_scope(ptr)
    }
    inline fun setPreviousDeclInSameBlockScope(Same: Boolean): Unit {
        return clang_VarDecl_set_previous_decl_in_same_block_scope(ptr, Same)
    }
    inline fun isEscapingByref(): Boolean {
        return clang_VarDecl_is_escaping_byref(ptr)
    }
    inline fun isNonEscapingByref(): Boolean {
        return clang_VarDecl_is_non_escaping_byref(ptr)
    }
    inline fun setEscapingByref(): Unit {
        return clang_VarDecl_set_escaping_byref(ptr)
    }
    inline fun isCXXCondDecl(): Boolean {
        return clang_VarDecl_is_cxx_cond_decl(ptr)
    }
    inline fun setCXXCondDecl(): Unit {
        return clang_VarDecl_set_cxx_cond_decl(ptr)
    }
    inline fun isCXXForRangeImplicitVar(): Boolean {
        return clang_VarDecl_is_cxx_for_range_implicit_var(ptr)
    }
    inline fun setCXXForRangeImplicitVar(FRV: Boolean): Unit {
        return clang_VarDecl_set_cxx_for_range_implicit_var(ptr, FRV)
    }
    inline fun hasDependentAlignment(): Boolean {
        return clang_VarDecl_has_dependent_alignment(ptr)
    }
    inline fun getTemplateInstantiationPattern(): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_template_instantiation_pattern(ptr) ?: return null), memScope)
    }
    inline fun getInstantiatedFromStaticDataMember(): VarDeclApi? {
        return VarDecl((clang_VarDecl_get_instantiated_from_static_data_member(ptr) ?: return null), memScope)
    }
    inline fun getTemplateSpecializationKind(): TemplateSpecializationKind {
        return TemplateSpecializationKindCompanionfromValue(clang_VarDecl_get_template_specialization_kind(ptr))
    }
    inline fun getTemplateSpecializationKindForInstantiation(): TemplateSpecializationKind {
        return TemplateSpecializationKindCompanionfromValue(clang_VarDecl_get_template_specialization_kind_for_instantiation(ptr))
    }
    inline fun setTemplateSpecializationKind(TSK: TemplateSpecializationKind): Unit {
        return clang_VarDecl_set_template_specialization_kind(ptr, TSK.value)
    }
    inline fun setInstantiationOfStaticDataMember(VD: VarDeclApi?, TSK: TemplateSpecializationKind): Unit {
        return clang_VarDecl_set_instantiation_of_static_data_member(ptr, VD?.ptr, TSK.value)
    }
    inline fun isKnownToBeDefined(): Boolean {
        return clang_VarDecl_is_known_to_be_defined(ptr)
    }
    inline fun isNoDestroy(_arg_0: ASTContext): Boolean {
        return clang_VarDecl_is_no_destroy(ptr, _arg_0.ptr)
    }
    inline fun needsDestruction(Ctx: ASTContext): DestructionKind {
        return DestructionKindCompanionfromValue(clang_VarDecl_needs_destruction(ptr, Ctx.ptr))
    }
    inline fun hasFlexibleArrayInit(Ctx: ASTContext): Boolean {
        return clang_VarDecl_has_flexible_array_init(ptr, Ctx.ptr)
    }
    inline fun getNumTemplateParameterLists(): UInt {
        return clang_DeclaratorDecl_get_num_template_parameter_lists(ptr)
    }
    inline fun getTemplateParameterList(index: UInt): TemplateParameterList? {
        return TemplateParameterList((clang_DeclaratorDecl_get_template_parameter_list(ptr, index) ?: return null), memScope)
    }
    inline fun getType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ValueDecl_get_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setType(newType: QualType): Unit {
        return clang_ValueDecl_set_type(ptr, newType.ptr)
    }
    inline fun isWeak(): Boolean {
        return clang_ValueDecl_is_weak(ptr)
    }
    inline fun getPotentiallyDecomposedVarDecl(): VarDeclApi? {
        return VarDecl((clang_ValueDecl_get_potentially_decomposed_var_decl(ptr) ?: return null), memScope)
    }
    inline fun valueDeclIsParameterPack(): Boolean {
        return clang_ValueDecl_is_parameter_pack(ptr)
    }
    inline fun getNameAsString(): String? {
        val str: CPointer<ByteVar>? = clang_NamedDecl_get_name_as_string(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getQualifiedNameAsString(): String? {
        val str: CPointer<ByteVar>? = clang_NamedDecl_get_qualified_name_as_string(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun declarationReplaces(OldD: NamedDeclApi?, IsKnownNewer: Boolean = true): Boolean {
        return clang_NamedDecl_declaration_replaces(ptr, OldD?.ptr, IsKnownNewer)
    }
    inline fun hasLinkage(): Boolean {
        return clang_NamedDecl_has_linkage(ptr)
    }
    inline fun isCXXClassMember(): Boolean {
        return clang_NamedDecl_is_cxx_class_member(ptr)
    }
    inline fun isCXXInstanceMember(): Boolean {
        return clang_NamedDecl_is_cxx_instance_member(ptr)
    }
    inline fun getLinkageInternal(): Linkage {
        return LinkageCompanionfromValue(clang_NamedDecl_get_linkage_internal(ptr))
    }
    inline fun getFormalLinkage(): Linkage {
        return LinkageCompanionfromValue(clang_NamedDecl_get_formal_linkage(ptr))
    }
    inline fun hasExternalFormalLinkage(): Boolean {
        return clang_NamedDecl_has_external_formal_linkage(ptr)
    }
    inline fun isExternallyVisible(): Boolean {
        return clang_NamedDecl_is_externally_visible(ptr)
    }
    inline fun isExternallyDeclarable(): Boolean {
        return clang_NamedDecl_is_externally_declarable(ptr)
    }
    inline fun getVisibility(): Visibility {
        return VisibilityCompanionfromValue(clang_NamedDecl_get_visibility(ptr))
    }
    inline fun isLinkageValid(): Boolean {
        return clang_NamedDecl_is_linkage_valid(ptr)
    }
    inline fun hasLinkageBeenComputed(): Boolean {
        return clang_NamedDecl_has_linkage_been_computed(ptr)
    }
    inline fun getUnderlyingDecl(): NamedDeclApi? {
        return NamedDecl((clang_NamedDecl_get_underlying_decl(ptr) ?: return null), memScope)
    }
    inline fun namedDeclGetMostRecentDecl(): NamedDeclApi? {
        return NamedDecl((clang_NamedDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun getObjCFStringFormattingFamily(): ObjCStringFormatFamily {
        return ObjCStringFormatFamilyCompanionfromValue(clang_NamedDecl_get_obj_cf_string_formatting_family(ptr))
    }
    inline fun getName(): String? {
        val str: CPointer<ByteVar>? = clang_NamedDecl_get_name(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getKind(): Kind {
        return KindCompanionfromValue(clang_Decl_get_kind(ptr))
    }
    inline fun getDeclKindName(): String? {
        val str: CPointer<ByteVar>? = clang_Decl_get_decl_kind_name(ptr)
        val ret: String? = str?.toKString()
        return ret
    }
    inline fun getNextDeclInContext(): DeclApi? {
        return Decl((clang_Decl_get_next_decl_in_context(ptr) ?: return null), memScope)
    }
    inline fun getDeclContext(): DeclContext? {
        return DeclContext((clang_Decl_get_decl_context(ptr) ?: return null), memScope)
    }
    inline fun getNonTransparentDeclContext(): DeclContext? {
        return DeclContext((clang_Decl_get_non_transparent_decl_context(ptr) ?: return null), memScope)
    }
    inline fun getNonClosureContext(): DeclApi? {
        return Decl((clang_Decl_get_non_closure_context(ptr) ?: return null), memScope)
    }
    inline fun getTranslationUnitDecl(): TranslationUnitDecl? {
        return TranslationUnitDecl((clang_Decl_get_translation_unit_decl(ptr) ?: return null), memScope)
    }
    inline fun isInAnonymousNamespace(): Boolean {
        return clang_Decl_is_in_anonymous_namespace(ptr)
    }
    inline fun isInStdNamespace(): Boolean {
        return clang_Decl_is_in_std_namespace(ptr)
    }
    inline fun isFileContextDecl(): Boolean {
        return clang_Decl_is_file_context_decl(ptr)
    }
    inline fun getASTContext(): ASTContext? {
        return ASTContext((clang_Decl_get_ast_context(ptr) ?: return null), memScope)
    }
    inline fun setAccess(AS: AccessSpecifier): Unit {
        return clang_Decl_set_access(ptr, AS.value)
    }
    inline fun getAccess(): AccessSpecifier {
        return AccessSpecifierCompanionfromValue(clang_Decl_get_access(ptr))
    }
    inline fun getAccessUnsafe(): AccessSpecifier {
        return AccessSpecifierCompanionfromValue(clang_Decl_get_access_unsafe(ptr))
    }
    inline fun hasAttrs(): Boolean {
        return clang_Decl_has_attrs(ptr)
    }
    inline fun dropAttrs(): Unit {
        return clang_Decl_drop_attrs(ptr)
    }
    inline fun getMaxAlignment(): UInt {
        return clang_Decl_get_max_alignment(ptr)
    }
    inline fun setInvalidDecl(Invalid: Boolean = true): Unit {
        return clang_Decl_set_invalid_decl(ptr, Invalid)
    }
    inline fun isInvalidDecl(): Boolean {
        return clang_Decl_is_invalid_decl(ptr)
    }
    inline fun isImplicit(): Boolean {
        return clang_Decl_is_implicit(ptr)
    }
    inline fun setImplicit(I: Boolean = true): Unit {
        return clang_Decl_set_implicit(ptr, I)
    }
    inline fun isUsed(CheckUsedAttr: Boolean = true): Boolean {
        return clang_Decl_is_used(ptr, CheckUsedAttr)
    }
    inline fun setIsUsed(): Unit {
        return clang_Decl_set_is_used(ptr)
    }
    inline fun markUsed(C: ASTContext): Unit {
        return clang_Decl_mark_used(ptr, C.ptr)
    }
    inline fun isReferenced(): Boolean {
        return clang_Decl_is_referenced(ptr)
    }
    inline fun isThisDeclarationReferenced(): Boolean {
        return clang_Decl_is_this_declaration_referenced(ptr)
    }
    inline fun setReferenced(R: Boolean = true): Unit {
        return clang_Decl_set_referenced(ptr, R)
    }
    inline fun invalidateCachedLinkage(): Unit {
        return clang_Decl_invalidate_cached_linkage(ptr)
    }
    inline fun isTopLevelDeclInObjCContainer(): Boolean {
        return clang_Decl_is_top_level_decl_in_obj_c_container(ptr)
    }
    inline fun setTopLevelDeclInObjCContainer(V: Boolean = true): Unit {
        return clang_Decl_set_top_level_decl_in_obj_c_container(ptr, V)
    }
    inline fun isModulePrivate(): Boolean {
        return clang_Decl_is_module_private(ptr)
    }
    inline fun isModuleLocal(): Boolean {
        return clang_Decl_is_module_local(ptr)
    }
    inline fun isInExportDeclContext(): Boolean {
        return clang_Decl_is_in_export_decl_context(ptr)
    }
    inline fun isInvisibleOutsideTheOwningModule(): Boolean {
        return clang_Decl_is_invisible_outside_the_owning_module(ptr)
    }
    inline fun isInAnotherModuleUnit(): Boolean {
        return clang_Decl_is_in_another_module_unit(ptr)
    }
    inline fun isInCurrentModuleUnit(): Boolean {
        return clang_Decl_is_in_current_module_unit(ptr)
    }
    inline fun shouldEmitInExternalSource(): Boolean {
        return clang_Decl_should_emit_in_external_source(ptr)
    }
    inline fun isFromExplicitGlobalModule(): Boolean {
        return clang_Decl_is_from_explicit_global_module(ptr)
    }
    inline fun isFromGlobalModule(): Boolean {
        return clang_Decl_is_from_global_module(ptr)
    }
    inline fun isInNamedModule(): Boolean {
        return clang_Decl_is_in_named_module(ptr)
    }
    inline fun isFromHeaderUnit(): Boolean {
        return clang_Decl_is_from_header_unit(ptr)
    }
    inline fun hasDefiningAttr(): Boolean {
        return clang_Decl_has_defining_attr(ptr)
    }
    inline fun setFromASTFile(): Unit {
        return clang_Decl_set_from_ast_file(ptr)
    }
    inline fun setOwningModuleID(ID: UInt): Unit {
        return clang_Decl_set_owning_module_id(ptr, ID)
    }
    inline fun getAvailability(): AvailabilityResult {
        return AvailabilityResultCompanionfromValue(clang_Decl_get_availability(ptr))
    }
    inline fun isDeprecated(): Boolean {
        return clang_Decl_is_deprecated(ptr)
    }
    inline fun isUnavailable(): Boolean {
        return clang_Decl_is_unavailable(ptr)
    }
    inline fun isWeakImported(): Boolean {
        return clang_Decl_is_weak_imported(ptr)
    }
    inline fun canBeWeakImported(IsDefinition: Boolean): Boolean {
        return clang_Decl_can_be_weak_imported(ptr, IsDefinition)
    }
    inline fun isFromASTFile(): Boolean {
        return clang_Decl_is_from_ast_file(ptr)
    }
    inline fun getOwningModuleID(): UInt {
        return clang_Decl_get_owning_module_id(ptr)
    }
    inline fun hasOwningModule(): Boolean {
        return clang_Decl_has_owning_module(ptr)
    }
    inline fun isUnconditionallyVisible(): Boolean {
        return clang_Decl_is_unconditionally_visible(ptr)
    }
    inline fun isReachable(): Boolean {
        return clang_Decl_is_reachable(ptr)
    }
    inline fun setVisibleDespiteOwningModule(): Unit {
        return clang_Decl_set_visible_despite_owning_module(ptr)
    }
    inline fun setVisiblePromoted(): Unit {
        return clang_Decl_set_visible_promoted(ptr)
    }
    inline fun getModuleOwnershipKind(): ModuleOwnershipKind {
        return ModuleOwnershipKindCompanionfromValue(clang_Decl_get_module_ownership_kind(ptr))
    }
    inline fun setModuleOwnershipKind(MOK: ModuleOwnershipKind): Unit {
        return clang_Decl_set_module_ownership_kind(ptr, MOK.value)
    }
    inline fun getIdentifierNamespace(): UInt {
        return clang_Decl_get_identifier_namespace(ptr)
    }
    inline fun isInIdentifierNamespace(NS: UInt): Boolean {
        return clang_Decl_is_in_identifier_namespace(ptr, NS)
    }
    inline fun hasTagIdentifierNamespace(): Boolean {
        return clang_Decl_has_tag_identifier_namespace(ptr)
    }
    inline fun getLexicalDeclContext(): DeclContext? {
        return DeclContext((clang_Decl_get_lexical_decl_context(ptr) ?: return null), memScope)
    }
    inline fun setDeclContext(DC: DeclContext?): Unit {
        return clang_Decl_set_decl_context(ptr, DC?.ptr)
    }
    inline fun setLexicalDeclContext(DC: DeclContext?): Unit {
        return clang_Decl_set_lexical_decl_context(ptr, DC?.ptr)
    }
    inline fun isTemplated(): Boolean {
        return clang_Decl_is_templated(ptr)
    }
    inline fun getTemplateDepth(): UInt {
        return clang_Decl_get_template_depth(ptr)
    }
    inline fun isDefinedOutsideFunctionOrMethod(): Boolean {
        return clang_Decl_is_defined_outside_function_or_method(ptr)
    }
    inline fun isInLocalScopeForInstantiation(): Boolean {
        return clang_Decl_is_in_local_scope_for_instantiation(ptr)
    }
    inline fun getParentFunctionOrMethod(LexicalParent: Boolean = false): DeclContext? {
        return DeclContext((clang_Decl_get_parent_function_or_method(ptr, LexicalParent) ?: return null), memScope)
    }
    inline fun isCanonicalDecl(): Boolean {
        return clang_Decl_is_canonical_decl(ptr)
    }
    inline fun redecls(): Vector__Decl_P {
        val retValue: Vector__Decl_P = memScope.Vector__Decl_P_Holder()
        clang_Decl_redecls(ptr, retValue.ptr)
        return retValue
    }
    inline fun getPreviousDecl(): DeclApi? {
        return Decl((clang_Decl_get_previous_decl(ptr) ?: return null), memScope)
    }
    inline fun isFirstDecl(): Boolean {
        return clang_Decl_is_first_decl(ptr)
    }
    override fun hasBody(): Boolean {
        return clang_Decl_has_body(ptr)
    }
    inline fun isTemplateParameter(): Boolean {
        return clang_Decl_is_template_parameter(ptr)
    }
    inline fun isTemplateParameterPack(): Boolean {
        return clang_Decl_is_template_parameter_pack(ptr)
    }
    inline fun isTemplateDecl(): Boolean {
        return clang_Decl_is_template_decl(ptr)
    }
    inline fun isFunctionOrFunctionTemplate(): Boolean {
        return clang_Decl_is_function_or_function_template(ptr)
    }
    inline fun getDescribedTemplate(): TemplateDecl? {
        return TemplateDecl((clang_Decl_get_described_template(ptr) ?: return null), memScope)
    }
    inline fun getDescribedTemplateParams(): TemplateParameterList? {
        return TemplateParameterList((clang_Decl_get_described_template_params(ptr) ?: return null), memScope)
    }
    inline fun getAsFunction(): FunctionDeclApi? {
        return FunctionDecl((clang_Decl_get_as_function(ptr) ?: return null), memScope)
    }
    inline fun setLocalExternDecl(): Unit {
        return clang_Decl_set_local_extern_decl(ptr)
    }
    inline fun isLocalExternDecl(): Boolean {
        return clang_Decl_is_local_extern_decl(ptr)
    }
    inline fun setObjectOfFriendDecl(PerformFriendInjection: Boolean = false): Unit {
        return clang_Decl_set_object_of_friend_decl(ptr, PerformFriendInjection)
    }
    inline fun clearIdentifierNamespace(): Unit {
        return clang_Decl_clear_identifier_namespace(ptr)
    }
    inline fun getFriendObjectKind(): FriendObjectKind {
        return FriendObjectKindCompanionfromValue(clang_Decl_get_friend_object_kind(ptr))
    }
    inline fun setNonMemberOperator(): Unit {
        return clang_Decl_set_non_member_operator(ptr)
    }
    inline fun dump(): Unit {
        return clang_Decl_dump(ptr)
    }
    inline fun dumpColor(): Unit {
        return clang_Decl_dump_color(ptr)
    }
    inline fun getID(): Long {
        return clang_Decl_get_id(ptr)
    }
    inline fun getFunctionType(BlocksToo: Boolean = true): FunctionTypeApi? {
        return FunctionType((clang_Decl_get_function_type(ptr, BlocksToo) ?: return null), memScope)
    }
    inline fun isFunctionPointerType(): Boolean {
        return clang_Decl_is_function_pointer_type(ptr)
    }
    inline fun setObjCMethodScopeInfo(parameterIndex: UInt): Unit {
        return clang_ParmVarDecl_set_obj_c_method_scope_info(ptr, parameterIndex)
    }
    inline fun setScopeInfo(scopeDepth: UInt, parameterIndex: UInt): Unit {
        return clang_ParmVarDecl_set_scope_info(ptr, scopeDepth, parameterIndex)
    }
    inline fun isObjCMethodParameter(): Boolean {
        return clang_ParmVarDecl_is_obj_c_method_parameter(ptr)
    }
    inline fun isDestroyedInCallee(): Boolean {
        return clang_ParmVarDecl_is_destroyed_in_callee(ptr)
    }
    inline fun getFunctionScopeDepth(): UInt {
        return clang_ParmVarDecl_get_function_scope_depth(ptr)
    }
    inline fun getFunctionScopeIndex(): UInt {
        return clang_ParmVarDecl_get_function_scope_index(ptr)
    }
    inline fun getObjCDeclQualifier(): ObjCDeclQualifier {
        return ObjCDeclQualifierCompanionfromValue(clang_ParmVarDecl_get_obj_c_decl_qualifier(ptr))
    }
    inline fun setObjCDeclQualifier(QTVal: ObjCDeclQualifier): Unit {
        return clang_ParmVarDecl_set_obj_c_decl_qualifier(ptr, QTVal.value)
    }
    inline fun isKNRPromoted(): Boolean {
        return clang_ParmVarDecl_is_knr_promoted(ptr)
    }
    inline fun setKNRPromoted(promoted: Boolean): Unit {
        return clang_ParmVarDecl_set_knr_promoted(ptr, promoted)
    }
    inline fun isExplicitObjectParameter(): Boolean {
        return clang_ParmVarDecl_is_explicit_object_parameter(ptr)
    }
    inline fun hasDefaultArg(): Boolean {
        return clang_ParmVarDecl_has_default_arg(ptr)
    }
    inline fun hasUnparsedDefaultArg(): Boolean {
        return clang_ParmVarDecl_has_unparsed_default_arg(ptr)
    }
    inline fun hasUninstantiatedDefaultArg(): Boolean {
        return clang_ParmVarDecl_has_uninstantiated_default_arg(ptr)
    }
    inline fun setUnparsedDefaultArg(): Unit {
        return clang_ParmVarDecl_set_unparsed_default_arg(ptr)
    }
    inline fun hasInheritedDefaultArg(): Boolean {
        return clang_ParmVarDecl_has_inherited_default_arg(ptr)
    }
    inline fun setHasInheritedDefaultArg(I: Boolean = true): Unit {
        return clang_ParmVarDecl_set_has_inherited_default_arg(ptr, I)
    }
    inline fun getOriginalType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ParmVarDecl_get_original_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setOwningFunction(FD: DeclContext?): Unit {
        return clang_ParmVarDecl_set_owning_function(ptr, FD?.ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ParmVarDecl_size_of()
            }

        val align: Int
            inline get() {
                return clang_ParmVarDecl_align_of()
            }

        inline fun MemScope.getMaxFunctionScopeDepth(): UInt {
            return clang_ParmVarDecl_get_max_function_scope_depth()
        }
        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_ParmVarDecl_classof(D?.ptr)
        }
        inline fun MemScope.classofKind(K: Kind): Boolean {
            return clang_ParmVarDecl_classof_kind(K.value)
        }
        fun MemScope.ParmVarDecl_Holder(): ParmVarDecl {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return ParmVarDecl(memory, this)
        }
    }
    inline fun asVarDecl(): VarDecl {
        return VarDecl(clang_ParmVarDecl_as_clang_VarDecl(ptr)!!, memScope)
    }
    inline fun asDeclaratorDecl(): DeclaratorDecl {
        return DeclaratorDecl(clang_ParmVarDecl_as_clang_DeclaratorDecl(ptr)!!, memScope)
    }
    inline fun asValueDecl(): ValueDecl {
        return ValueDecl(clang_ParmVarDecl_as_clang_ValueDecl(ptr)!!, memScope)
    }
    inline fun asNamedDecl(): NamedDecl {
        return NamedDecl(clang_ParmVarDecl_as_clang_NamedDecl(ptr)!!, memScope)
    }
    inline fun asDecl(): Decl {
        return Decl(clang_ParmVarDecl_as_clang_Decl(ptr)!!, memScope)
    }
}

// END KRAPPER GEN for clang::ParmVarDecl


