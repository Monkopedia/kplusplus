package clang

import clang.AccessSpecifier.Companion.fromValue as AccessSpecifierCompanionfromValue
import clang.AvailabilityResult.Companion.fromValue as AvailabilityResultCompanionfromValue
import clang.ConstexprSpecKind.Companion.fromValue as CompanionfromValue
import clang.ExceptionSpecificationType.Companion.fromValue as ExceptionSpecificationTypeCompanionfromValue
import clang.LanguageLinkage.Companion.fromValue as LanguageLinkageCompanionfromValue
import clang.Linkage.Companion.fromValue as LinkageCompanionfromValue
import clang.MultiVersionKind.Companion.fromValue as MultiVersionKindCompanionfromValue
import clang.ObjCStringFormatFamily.Companion.fromValue as ObjCStringFormatFamilyCompanionfromValue
import clang.OverloadedOperatorKind.Companion.fromValue as OverloadedOperatorKindCompanionfromValue
import clang.QualType.Companion.QualType_Holder
import clang.RefQualifierKind.Companion.fromValue
import clang.StorageClass.Companion.fromValue as StorageClassCompanionfromValue
import clang.TemplateSpecializationKind.Companion.fromValue as TemplateSpecializationKindCompanionfromValue
import clang.Visibility.Companion.fromValue as VisibilityCompanionfromValue
import clang.decl.FriendObjectKind
import clang.decl.FriendObjectKind.Companion.fromValue as FriendObjectKindCompanionfromValue
import clang.decl.Kind
import clang.decl.Kind.Companion.fromValue as KindCompanionfromValue
import clang.decl.ModuleOwnershipKind
import clang.decl.ModuleOwnershipKind.Companion.fromValue as ModuleOwnershipKindCompanionfromValue
import clang.functionDecl.TemplatedKind
import clang.functionDecl.TemplatedKind.Companion.fromValue as TemplatedKindCompanionfromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.UInt
import kotlin.ULong
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.krapper_parse.internal._clang_FunctionDecl_has_body
import krapper.krapper_parse.internal._clang_FunctionDecl_is_defined
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_align_of
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_CXXMethodDecl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_Decl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_DeclContext
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_DeclaratorDecl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_FunctionDecl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_NamedDecl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_as_clang_ValueDecl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_classof
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_classof_kind
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_get_array_operator_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_get_canonical_decl
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_get_global_array_operator_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_get_operator_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_get_operator_global_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_is_called_by_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_set_global_operator_array_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_set_operator_array_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_set_operator_global_delete
import krapper.krapper_parse.internal.clang_CXXDestructorDecl_size_of
import krapper.krapper_parse.internal.clang_CXXMethodDecl_add_overridden_method
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_canonical_decl
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_corresponding_method_declared_in_class
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_corresponding_method_in_class
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_function_object_parameter_reference_type
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_function_object_parameter_type
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_most_recent_decl
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_num_explicit_params
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_parent
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_ref_qualifier
import krapper.krapper_parse.internal.clang_CXXMethodDecl_get_this_type
import krapper.krapper_parse.internal.clang_CXXMethodDecl_has_inline_body
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_const
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_copy_assignment_operator
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_explicit_object_member_function
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_implicit_object_member_function
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_instance
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_lambda_static_invoker
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_move_assignment_operator
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_static
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_virtual
import krapper.krapper_parse.internal.clang_CXXMethodDecl_is_volatile
import krapper.krapper_parse.internal.clang_CXXMethodDecl_size_overridden_methods
import krapper.krapper_parse.internal.clang_Decl_can_be_weak_imported
import krapper.krapper_parse.internal.clang_Decl_clear_identifier_namespace
import krapper.krapper_parse.internal.clang_Decl_drop_attrs
import krapper.krapper_parse.internal.clang_Decl_dump
import krapper.krapper_parse.internal.clang_Decl_dump_color
import krapper.krapper_parse.internal.clang_Decl_get_access
import krapper.krapper_parse.internal.clang_Decl_get_access_unsafe
import krapper.krapper_parse.internal.clang_Decl_get_as_function
import krapper.krapper_parse.internal.clang_Decl_get_ast_context
import krapper.krapper_parse.internal.clang_Decl_get_availability
import krapper.krapper_parse.internal.clang_Decl_get_decl_context
import krapper.krapper_parse.internal.clang_Decl_get_decl_kind_name
import krapper.krapper_parse.internal.clang_Decl_get_described_template
import krapper.krapper_parse.internal.clang_Decl_get_described_template_params
import krapper.krapper_parse.internal.clang_Decl_get_friend_object_kind
import krapper.krapper_parse.internal.clang_Decl_get_function_type
import krapper.krapper_parse.internal.clang_Decl_get_id
import krapper.krapper_parse.internal.clang_Decl_get_identifier_namespace
import krapper.krapper_parse.internal.clang_Decl_get_kind
import krapper.krapper_parse.internal.clang_Decl_get_lexical_decl_context
import krapper.krapper_parse.internal.clang_Decl_get_max_alignment
import krapper.krapper_parse.internal.clang_Decl_get_module_ownership_kind
import krapper.krapper_parse.internal.clang_Decl_get_next_decl_in_context
import krapper.krapper_parse.internal.clang_Decl_get_non_closure_context
import krapper.krapper_parse.internal.clang_Decl_get_non_transparent_decl_context
import krapper.krapper_parse.internal.clang_Decl_get_owning_module_id
import krapper.krapper_parse.internal.clang_Decl_get_parent_function_or_method
import krapper.krapper_parse.internal.clang_Decl_get_previous_decl
import krapper.krapper_parse.internal.clang_Decl_get_template_depth
import krapper.krapper_parse.internal.clang_Decl_get_translation_unit_decl
import krapper.krapper_parse.internal.clang_Decl_has_attrs
import krapper.krapper_parse.internal.clang_Decl_has_defining_attr
import krapper.krapper_parse.internal.clang_Decl_has_owning_module
import krapper.krapper_parse.internal.clang_Decl_has_tag_identifier_namespace
import krapper.krapper_parse.internal.clang_Decl_invalidate_cached_linkage
import krapper.krapper_parse.internal.clang_Decl_is_canonical_decl
import krapper.krapper_parse.internal.clang_Decl_is_defined_outside_function_or_method
import krapper.krapper_parse.internal.clang_Decl_is_deprecated
import krapper.krapper_parse.internal.clang_Decl_is_file_context_decl
import krapper.krapper_parse.internal.clang_Decl_is_first_decl
import krapper.krapper_parse.internal.clang_Decl_is_from_ast_file
import krapper.krapper_parse.internal.clang_Decl_is_from_explicit_global_module
import krapper.krapper_parse.internal.clang_Decl_is_from_global_module
import krapper.krapper_parse.internal.clang_Decl_is_from_header_unit
import krapper.krapper_parse.internal.clang_Decl_is_function_or_function_template
import krapper.krapper_parse.internal.clang_Decl_is_function_pointer_type
import krapper.krapper_parse.internal.clang_Decl_is_implicit
import krapper.krapper_parse.internal.clang_Decl_is_in_anonymous_namespace
import krapper.krapper_parse.internal.clang_Decl_is_in_another_module_unit
import krapper.krapper_parse.internal.clang_Decl_is_in_current_module_unit
import krapper.krapper_parse.internal.clang_Decl_is_in_export_decl_context
import krapper.krapper_parse.internal.clang_Decl_is_in_identifier_namespace
import krapper.krapper_parse.internal.clang_Decl_is_in_local_scope_for_instantiation
import krapper.krapper_parse.internal.clang_Decl_is_in_named_module
import krapper.krapper_parse.internal.clang_Decl_is_in_std_namespace
import krapper.krapper_parse.internal.clang_Decl_is_invalid_decl
import krapper.krapper_parse.internal.clang_Decl_is_invisible_outside_the_owning_module
import krapper.krapper_parse.internal.clang_Decl_is_local_extern_decl
import krapper.krapper_parse.internal.clang_Decl_is_module_local
import krapper.krapper_parse.internal.clang_Decl_is_module_private
import krapper.krapper_parse.internal.clang_Decl_is_reachable
import krapper.krapper_parse.internal.clang_Decl_is_referenced
import krapper.krapper_parse.internal.clang_Decl_is_template_decl
import krapper.krapper_parse.internal.clang_Decl_is_template_parameter
import krapper.krapper_parse.internal.clang_Decl_is_template_parameter_pack
import krapper.krapper_parse.internal.clang_Decl_is_templated
import krapper.krapper_parse.internal.clang_Decl_is_this_declaration_referenced
import krapper.krapper_parse.internal.clang_Decl_is_top_level_decl_in_obj_c_container
import krapper.krapper_parse.internal.clang_Decl_is_unavailable
import krapper.krapper_parse.internal.clang_Decl_is_unconditionally_visible
import krapper.krapper_parse.internal.clang_Decl_is_used
import krapper.krapper_parse.internal.clang_Decl_is_weak_imported
import krapper.krapper_parse.internal.clang_Decl_mark_used
import krapper.krapper_parse.internal.clang_Decl_redecls
import krapper.krapper_parse.internal.clang_Decl_set_access
import krapper.krapper_parse.internal.clang_Decl_set_decl_context
import krapper.krapper_parse.internal.clang_Decl_set_from_ast_file
import krapper.krapper_parse.internal.clang_Decl_set_implicit
import krapper.krapper_parse.internal.clang_Decl_set_invalid_decl
import krapper.krapper_parse.internal.clang_Decl_set_is_used
import krapper.krapper_parse.internal.clang_Decl_set_lexical_decl_context
import krapper.krapper_parse.internal.clang_Decl_set_local_extern_decl
import krapper.krapper_parse.internal.clang_Decl_set_module_ownership_kind
import krapper.krapper_parse.internal.clang_Decl_set_non_member_operator
import krapper.krapper_parse.internal.clang_Decl_set_object_of_friend_decl
import krapper.krapper_parse.internal.clang_Decl_set_owning_module_id
import krapper.krapper_parse.internal.clang_Decl_set_referenced
import krapper.krapper_parse.internal.clang_Decl_set_top_level_decl_in_obj_c_container
import krapper.krapper_parse.internal.clang_Decl_set_visible_despite_owning_module
import krapper.krapper_parse.internal.clang_Decl_set_visible_promoted
import krapper.krapper_parse.internal.clang_Decl_should_emit_in_external_source
import krapper.krapper_parse.internal.clang_DeclaratorDecl_get_num_template_parameter_lists
import krapper.krapper_parse.internal.clang_DeclaratorDecl_get_template_parameter_list
import krapper.krapper_parse.internal.clang_FunctionDecl_body_contains_immediate_escalating_expressions
import krapper.krapper_parse.internal.clang_FunctionDecl_does_declaration_force_externally_visible_definition
import krapper.krapper_parse.internal.clang_FunctionDecl_does_this_declaration_have_a_body
import krapper.krapper_parse.internal.clang_FunctionDecl_friend_constraint_refers_to_enclosing_template
import krapper.krapper_parse.internal.clang_FunctionDecl_get_builtin_id
import krapper.krapper_parse.internal.clang_FunctionDecl_get_call_result_type
import krapper.krapper_parse.internal.clang_FunctionDecl_get_constexpr_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_get_declared_return_type
import krapper.krapper_parse.internal.clang_FunctionDecl_get_definition
import krapper.krapper_parse.internal.clang_FunctionDecl_get_exception_spec_type
import krapper.krapper_parse.internal.clang_FunctionDecl_get_instantiated_from_decl
import krapper.krapper_parse.internal.clang_FunctionDecl_get_instantiated_from_member_function
import krapper.krapper_parse.internal.clang_FunctionDecl_get_language_linkage
import krapper.krapper_parse.internal.clang_FunctionDecl_get_memory_function_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_get_min_required_arguments
import krapper.krapper_parse.internal.clang_FunctionDecl_get_min_required_explicit_arguments
import krapper.krapper_parse.internal.clang_FunctionDecl_get_multi_version_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_get_non_object_parameter
import krapper.krapper_parse.internal.clang_FunctionDecl_get_num_non_object_params
import krapper.krapper_parse.internal.clang_FunctionDecl_get_num_params
import krapper.krapper_parse.internal.clang_FunctionDecl_get_odr_hash
import krapper.krapper_parse.internal.clang_FunctionDecl_get_overloaded_operator
import krapper.krapper_parse.internal.clang_FunctionDecl_get_param_decl
import krapper.krapper_parse.internal.clang_FunctionDecl_get_return_type
import krapper.krapper_parse.internal.clang_FunctionDecl_get_storage_class
import krapper.krapper_parse.internal.clang_FunctionDecl_get_template_instantiation_pattern
import krapper.krapper_parse.internal.clang_FunctionDecl_get_template_specialization_args
import krapper.krapper_parse.internal.clang_FunctionDecl_get_template_specialization_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_get_template_specialization_kind_for_instantiation
import krapper.krapper_parse.internal.clang_FunctionDecl_get_templated_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_has_body
import krapper.krapper_parse.internal.clang_FunctionDecl_has_cxx_explicit_function_object_parameter
import krapper.krapper_parse.internal.clang_FunctionDecl_has_implicit_return_zero
import krapper.krapper_parse.internal.clang_FunctionDecl_has_inherited_prototype
import krapper.krapper_parse.internal.clang_FunctionDecl_has_one_param_or_default_args
import krapper.krapper_parse.internal.clang_FunctionDecl_has_prototype
import krapper.krapper_parse.internal.clang_FunctionDecl_has_skipped_body
import krapper.krapper_parse.internal.clang_FunctionDecl_has_trivial_body
import krapper.krapper_parse.internal.clang_FunctionDecl_has_written_prototype
import krapper.krapper_parse.internal.clang_FunctionDecl_instantiation_is_pending
import krapper.krapper_parse.internal.clang_FunctionDecl_is_analyzer_no_return
import krapper.krapper_parse.internal.clang_FunctionDecl_is_consteval
import krapper.krapper_parse.internal.clang_FunctionDecl_is_constexpr
import krapper.krapper_parse.internal.clang_FunctionDecl_is_constexpr_specified
import krapper.krapper_parse.internal.clang_FunctionDecl_is_cpu_dispatch_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_cpu_specific_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_defaulted
import krapper.krapper_parse.internal.clang_FunctionDecl_is_defined
import krapper.krapper_parse.internal.clang_FunctionDecl_is_deleted
import krapper.krapper_parse.internal.clang_FunctionDecl_is_deleted_as_written
import krapper.krapper_parse.internal.clang_FunctionDecl_is_destroying_operator_delete
import krapper.krapper_parse.internal.clang_FunctionDecl_is_explicitly_defaulted
import krapper.krapper_parse.internal.clang_FunctionDecl_is_extern_c
import krapper.krapper_parse.internal.clang_FunctionDecl_is_function_template_specialization
import krapper.krapper_parse.internal.clang_FunctionDecl_is_global
import krapper.krapper_parse.internal.clang_FunctionDecl_is_immediate_escalating
import krapper.krapper_parse.internal.clang_FunctionDecl_is_immediate_function
import krapper.krapper_parse.internal.clang_FunctionDecl_is_implicitly_instantiable
import krapper.krapper_parse.internal.clang_FunctionDecl_is_in_extern_c_context
import krapper.krapper_parse.internal.clang_FunctionDecl_is_in_extern_cxx_context
import krapper.krapper_parse.internal.clang_FunctionDecl_is_ineligible_or_not_selected
import krapper.krapper_parse.internal.clang_FunctionDecl_is_inline_builtin_declaration
import krapper.krapper_parse.internal.clang_FunctionDecl_is_inline_definition_externally_visible
import krapper.krapper_parse.internal.clang_FunctionDecl_is_inline_specified
import krapper.krapper_parse.internal.clang_FunctionDecl_is_inlined
import krapper.krapper_parse.internal.clang_FunctionDecl_is_instantiated_from_member_template
import krapper.krapper_parse.internal.clang_FunctionDecl_is_late_template_parsed
import krapper.krapper_parse.internal.clang_FunctionDecl_is_main
import krapper.krapper_parse.internal.clang_FunctionDecl_is_member_like_constrained_friend
import krapper.krapper_parse.internal.clang_FunctionDecl_is_ms_extern_inline
import krapper.krapper_parse.internal.clang_FunctionDecl_is_msvcrt_entry_point
import krapper.krapper_parse.internal.clang_FunctionDecl_is_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_no_return
import krapper.krapper_parse.internal.clang_FunctionDecl_is_out_of_line
import krapper.krapper_parse.internal.clang_FunctionDecl_is_overloaded_operator
import krapper.krapper_parse.internal.clang_FunctionDecl_is_pure_virtual
import krapper.krapper_parse.internal.clang_FunctionDecl_is_referenceable_kernel
import krapper.krapper_parse.internal.clang_FunctionDecl_is_replaceable_global_allocation_function
import krapper.krapper_parse.internal.clang_FunctionDecl_is_reserved_global_placement_operator
import krapper.krapper_parse.internal.clang_FunctionDecl_is_target_clones_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_target_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_target_multi_version_default
import krapper.krapper_parse.internal.clang_FunctionDecl_is_target_version_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_is_template_instantiation
import krapper.krapper_parse.internal.clang_FunctionDecl_is_this_declaration_a_definition
import krapper.krapper_parse.internal.clang_FunctionDecl_is_this_declaration_instantiated_from_a_friend_definition
import krapper.krapper_parse.internal.clang_FunctionDecl_is_trivial
import krapper.krapper_parse.internal.clang_FunctionDecl_is_trivial_for_call
import krapper.krapper_parse.internal.clang_FunctionDecl_is_type_aware_operator_new_or_delete
import krapper.krapper_parse.internal.clang_FunctionDecl_is_usable_as_global_allocation_function_in_constant_evaluation
import krapper.krapper_parse.internal.clang_FunctionDecl_is_user_provided
import krapper.krapper_parse.internal.clang_FunctionDecl_is_variadic
import krapper.krapper_parse.internal.clang_FunctionDecl_is_virtual_as_written
import krapper.krapper_parse.internal.clang_FunctionDecl_param_empty
import krapper.krapper_parse.internal.clang_FunctionDecl_param_size
import krapper.krapper_parse.internal.clang_FunctionDecl_set_body_contains_immediate_escalating_expressions
import krapper.krapper_parse.internal.clang_FunctionDecl_set_constexpr_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_set_defaulted
import krapper.krapper_parse.internal.clang_FunctionDecl_set_deleted_as_written
import krapper.krapper_parse.internal.clang_FunctionDecl_set_explicitly_defaulted
import krapper.krapper_parse.internal.clang_FunctionDecl_set_friend_constraint_refers_to_enclosing_template
import krapper.krapper_parse.internal.clang_FunctionDecl_set_has_implicit_return_zero
import krapper.krapper_parse.internal.clang_FunctionDecl_set_has_inherited_prototype
import krapper.krapper_parse.internal.clang_FunctionDecl_set_has_skipped_body
import krapper.krapper_parse.internal.clang_FunctionDecl_set_has_written_prototype
import krapper.krapper_parse.internal.clang_FunctionDecl_set_implicitly_inline
import krapper.krapper_parse.internal.clang_FunctionDecl_set_ineligible_or_not_selected
import krapper.krapper_parse.internal.clang_FunctionDecl_set_inline_specified
import krapper.krapper_parse.internal.clang_FunctionDecl_set_instantiated_from_decl
import krapper.krapper_parse.internal.clang_FunctionDecl_set_instantiated_from_member_template
import krapper.krapper_parse.internal.clang_FunctionDecl_set_instantiation_is_pending
import krapper.krapper_parse.internal.clang_FunctionDecl_set_instantiation_of_member_function
import krapper.krapper_parse.internal.clang_FunctionDecl_set_is_destroying_operator_delete
import krapper.krapper_parse.internal.clang_FunctionDecl_set_is_multi_version
import krapper.krapper_parse.internal.clang_FunctionDecl_set_is_pure_virtual
import krapper.krapper_parse.internal.clang_FunctionDecl_set_is_type_aware_operator_new_or_delete
import krapper.krapper_parse.internal.clang_FunctionDecl_set_late_template_parsed
import krapper.krapper_parse.internal.clang_FunctionDecl_set_lazy_body
import krapper.krapper_parse.internal.clang_FunctionDecl_set_previous_declaration
import krapper.krapper_parse.internal.clang_FunctionDecl_set_storage_class
import krapper.krapper_parse.internal.clang_FunctionDecl_set_template_specialization_kind
import krapper.krapper_parse.internal.clang_FunctionDecl_set_trivial
import krapper.krapper_parse.internal.clang_FunctionDecl_set_trivial_for_call
import krapper.krapper_parse.internal.clang_FunctionDecl_set_uses_fp_intrin
import krapper.krapper_parse.internal.clang_FunctionDecl_set_uses_seh_try
import krapper.krapper_parse.internal.clang_FunctionDecl_set_virtual_as_written
import krapper.krapper_parse.internal.clang_FunctionDecl_set_will_have_body
import krapper.krapper_parse.internal.clang_FunctionDecl_uses_fp_intrin
import krapper.krapper_parse.internal.clang_FunctionDecl_uses_seh_try
import krapper.krapper_parse.internal.clang_FunctionDecl_will_have_body
import krapper.krapper_parse.internal.clang_NamedDecl_declaration_replaces
import krapper.krapper_parse.internal.clang_NamedDecl_get_formal_linkage
import krapper.krapper_parse.internal.clang_NamedDecl_get_linkage_internal
import krapper.krapper_parse.internal.clang_NamedDecl_get_name
import krapper.krapper_parse.internal.clang_NamedDecl_get_name_as_string
import krapper.krapper_parse.internal.clang_NamedDecl_get_obj_cf_string_formatting_family
import krapper.krapper_parse.internal.clang_NamedDecl_get_qualified_name_as_string
import krapper.krapper_parse.internal.clang_NamedDecl_get_underlying_decl
import krapper.krapper_parse.internal.clang_NamedDecl_get_visibility
import krapper.krapper_parse.internal.clang_NamedDecl_has_external_formal_linkage
import krapper.krapper_parse.internal.clang_NamedDecl_has_linkage
import krapper.krapper_parse.internal.clang_NamedDecl_has_linkage_been_computed
import krapper.krapper_parse.internal.clang_NamedDecl_is_cxx_class_member
import krapper.krapper_parse.internal.clang_NamedDecl_is_cxx_instance_member
import krapper.krapper_parse.internal.clang_NamedDecl_is_externally_declarable
import krapper.krapper_parse.internal.clang_NamedDecl_is_externally_visible
import krapper.krapper_parse.internal.clang_NamedDecl_is_linkage_valid
import krapper.krapper_parse.internal.clang_ValueDecl_get_potentially_decomposed_var_decl
import krapper.krapper_parse.internal.clang_ValueDecl_get_type
import krapper.krapper_parse.internal.clang_ValueDecl_is_init_capture
import krapper.krapper_parse.internal.clang_ValueDecl_is_parameter_pack
import krapper.krapper_parse.internal.clang_ValueDecl_is_weak
import krapper.krapper_parse.internal.clang_ValueDecl_set_type
import platform.linux.free
import platform.posix.size_t
import std.Vector__Decl_P
import std.Vector__Decl_P.Companion.Vector__Decl_P_Holder

// BEGIN KRAPPER GEN for clang::CXXDestructorDecl

@krapper.CppBinding("clang::CXXDestructorDecl")
class CXXDestructorDecl(
    override val ptr: COpaquePointer,
    val memScope: MemScope,
) : clang.CXXMethodDeclApi, clang.FunctionDeclApi, clang.DeclaratorDeclApi, clang.ValueDeclApi, clang.NamedDeclApi, clang.DeclApi {
    inline fun cXXMethodDeclIsStatic(): Boolean {
        return clang_CXXMethodDecl_is_static(ptr)
    }
    inline fun isInstance(): Boolean {
        return clang_CXXMethodDecl_is_instance(ptr)
    }
    inline fun isExplicitObjectMemberFunction(): Boolean {
        return clang_CXXMethodDecl_is_explicit_object_member_function(ptr)
    }
    inline fun isImplicitObjectMemberFunction(): Boolean {
        return clang_CXXMethodDecl_is_implicit_object_member_function(ptr)
    }
    inline fun isConst(): Boolean {
        return clang_CXXMethodDecl_is_const(ptr)
    }
    inline fun isVolatile(): Boolean {
        return clang_CXXMethodDecl_is_volatile(ptr)
    }
    inline fun isVirtual(): Boolean {
        return clang_CXXMethodDecl_is_virtual(ptr)
    }
    inline fun isCopyAssignmentOperator(): Boolean {
        return clang_CXXMethodDecl_is_copy_assignment_operator(ptr)
    }
    inline fun isMoveAssignmentOperator(): Boolean {
        return clang_CXXMethodDecl_is_move_assignment_operator(ptr)
    }
    inline fun cXXMethodDeclGetCanonicalDecl(): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXMethodDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    inline fun cXXMethodDeclGetMostRecentDecl(): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXMethodDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun addOverriddenMethod(MD: CXXMethodDeclApi?): Unit {
        return clang_CXXMethodDecl_add_overridden_method(ptr, MD?.ptr)
    }
    inline fun size_overridden_methods(): UInt {
        return clang_CXXMethodDecl_size_overridden_methods(ptr)
    }
    inline fun getParent(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXMethodDecl_get_parent(ptr) ?: return null), memScope)
    }
    inline fun getThisType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_CXXMethodDecl_get_this_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getFunctionObjectParameterReferenceType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_CXXMethodDecl_get_function_object_parameter_reference_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getFunctionObjectParameterType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_CXXMethodDecl_get_function_object_parameter_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getNumExplicitParams(): UInt {
        return clang_CXXMethodDecl_get_num_explicit_params(ptr)
    }
    inline fun getRefQualifier(): RefQualifierKind {
        return fromValue(clang_CXXMethodDecl_get_ref_qualifier(ptr))
    }
    inline fun hasInlineBody(): Boolean {
        return clang_CXXMethodDecl_has_inline_body(ptr)
    }
    inline fun isLambdaStaticInvoker(): Boolean {
        return clang_CXXMethodDecl_is_lambda_static_invoker(ptr)
    }
    inline fun getCorrespondingMethodInClass(RD: CXXRecordDeclApi?, MayBeBase: Boolean = false): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXMethodDecl_get_corresponding_method_in_class(ptr, RD?.ptr, MayBeBase) ?: return null), memScope)
    }
    inline fun getCorrespondingMethodDeclaredInClass(RD: CXXRecordDeclApi?, MayBeBase: Boolean = false): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXMethodDecl_get_corresponding_method_declared_in_class(ptr, RD?.ptr, MayBeBase) ?: return null), memScope)
    }
    inline fun hasBody__const_clang_FunctionDecl_P(Definition: FunctionDeclApi?): Boolean {
        return clang_FunctionDecl_has_body(ptr, Definition?.ptr)
    }
    override fun hasBody(): Boolean {
        return _clang_FunctionDecl_has_body(ptr)
    }
    inline fun hasTrivialBody(): Boolean {
        return clang_FunctionDecl_has_trivial_body(ptr)
    }
    inline fun isDefined__const_clang_FunctionDecl_P_bool(Definition: FunctionDeclApi?, CheckForPendingFriendDefinition: Boolean = false): Boolean {
        return clang_FunctionDecl_is_defined(ptr, Definition?.ptr, CheckForPendingFriendDefinition)
    }
    inline fun isDefined(): Boolean {
        return _clang_FunctionDecl_is_defined(ptr)
    }
    inline fun getDefinition(): FunctionDeclApi? {
        return FunctionDecl((clang_FunctionDecl_get_definition(ptr) ?: return null), memScope)
    }
    inline fun isThisDeclarationADefinition(): Boolean {
        return clang_FunctionDecl_is_this_declaration_a_definition(ptr)
    }
    inline fun isThisDeclarationInstantiatedFromAFriendDefinition(): Boolean {
        return clang_FunctionDecl_is_this_declaration_instantiated_from_a_friend_definition(ptr)
    }
    inline fun doesThisDeclarationHaveABody(): Boolean {
        return clang_FunctionDecl_does_this_declaration_have_a_body(ptr)
    }
    inline fun setLazyBody(Offset: ULong): Unit {
        return clang_FunctionDecl_set_lazy_body(ptr, Offset)
    }
    inline fun isVariadic(): Boolean {
        return clang_FunctionDecl_is_variadic(ptr)
    }
    inline fun isVirtualAsWritten(): Boolean {
        return clang_FunctionDecl_is_virtual_as_written(ptr)
    }
    inline fun setVirtualAsWritten(V: Boolean): Unit {
        return clang_FunctionDecl_set_virtual_as_written(ptr, V)
    }
    inline fun isPureVirtual(): Boolean {
        return clang_FunctionDecl_is_pure_virtual(ptr)
    }
    inline fun setIsPureVirtual(P: Boolean = true): Unit {
        return clang_FunctionDecl_set_is_pure_virtual(ptr, P)
    }
    inline fun isLateTemplateParsed(): Boolean {
        return clang_FunctionDecl_is_late_template_parsed(ptr)
    }
    inline fun setLateTemplateParsed(ILT: Boolean = true): Unit {
        return clang_FunctionDecl_set_late_template_parsed(ptr, ILT)
    }
    inline fun isInstantiatedFromMemberTemplate(): Boolean {
        return clang_FunctionDecl_is_instantiated_from_member_template(ptr)
    }
    inline fun setInstantiatedFromMemberTemplate(Val: Boolean = true): Unit {
        return clang_FunctionDecl_set_instantiated_from_member_template(ptr, Val)
    }
    inline fun isTrivial(): Boolean {
        return clang_FunctionDecl_is_trivial(ptr)
    }
    inline fun setTrivial(IT: Boolean): Unit {
        return clang_FunctionDecl_set_trivial(ptr, IT)
    }
    inline fun isTrivialForCall(): Boolean {
        return clang_FunctionDecl_is_trivial_for_call(ptr)
    }
    inline fun setTrivialForCall(IT: Boolean): Unit {
        return clang_FunctionDecl_set_trivial_for_call(ptr, IT)
    }
    inline fun isDefaulted(): Boolean {
        return clang_FunctionDecl_is_defaulted(ptr)
    }
    inline fun setDefaulted(D: Boolean = true): Unit {
        return clang_FunctionDecl_set_defaulted(ptr, D)
    }
    inline fun isExplicitlyDefaulted(): Boolean {
        return clang_FunctionDecl_is_explicitly_defaulted(ptr)
    }
    inline fun setExplicitlyDefaulted(ED: Boolean = true): Unit {
        return clang_FunctionDecl_set_explicitly_defaulted(ptr, ED)
    }
    inline fun isUserProvided(): Boolean {
        return clang_FunctionDecl_is_user_provided(ptr)
    }
    inline fun isIneligibleOrNotSelected(): Boolean {
        return clang_FunctionDecl_is_ineligible_or_not_selected(ptr)
    }
    inline fun setIneligibleOrNotSelected(II: Boolean): Unit {
        return clang_FunctionDecl_set_ineligible_or_not_selected(ptr, II)
    }
    inline fun hasImplicitReturnZero(): Boolean {
        return clang_FunctionDecl_has_implicit_return_zero(ptr)
    }
    inline fun setHasImplicitReturnZero(IRZ: Boolean): Unit {
        return clang_FunctionDecl_set_has_implicit_return_zero(ptr, IRZ)
    }
    inline fun hasPrototype(): Boolean {
        return clang_FunctionDecl_has_prototype(ptr)
    }
    inline fun hasWrittenPrototype(): Boolean {
        return clang_FunctionDecl_has_written_prototype(ptr)
    }
    inline fun setHasWrittenPrototype(P: Boolean = true): Unit {
        return clang_FunctionDecl_set_has_written_prototype(ptr, P)
    }
    inline fun hasInheritedPrototype(): Boolean {
        return clang_FunctionDecl_has_inherited_prototype(ptr)
    }
    inline fun setHasInheritedPrototype(P: Boolean = true): Unit {
        return clang_FunctionDecl_set_has_inherited_prototype(ptr, P)
    }
    inline fun isConstexpr(): Boolean {
        return clang_FunctionDecl_is_constexpr(ptr)
    }
    inline fun setConstexprKind(CSK: ConstexprSpecKind): Unit {
        return clang_FunctionDecl_set_constexpr_kind(ptr, CSK.value)
    }
    inline fun getConstexprKind(): ConstexprSpecKind {
        return CompanionfromValue(clang_FunctionDecl_get_constexpr_kind(ptr))
    }
    inline fun isConstexprSpecified(): Boolean {
        return clang_FunctionDecl_is_constexpr_specified(ptr)
    }
    inline fun isConsteval(): Boolean {
        return clang_FunctionDecl_is_consteval(ptr)
    }
    inline fun setBodyContainsImmediateEscalatingExpressions(Set: Boolean): Unit {
        return clang_FunctionDecl_set_body_contains_immediate_escalating_expressions(ptr, Set)
    }
    inline fun BodyContainsImmediateEscalatingExpressions(): Boolean {
        return clang_FunctionDecl_body_contains_immediate_escalating_expressions(ptr)
    }
    inline fun isImmediateEscalating(): Boolean {
        return clang_FunctionDecl_is_immediate_escalating(ptr)
    }
    inline fun isImmediateFunction(): Boolean {
        return clang_FunctionDecl_is_immediate_function(ptr)
    }
    inline fun instantiationIsPending(): Boolean {
        return clang_FunctionDecl_instantiation_is_pending(ptr)
    }
    inline fun setInstantiationIsPending(IC: Boolean): Unit {
        return clang_FunctionDecl_set_instantiation_is_pending(ptr, IC)
    }
    inline fun usesSEHTry(): Boolean {
        return clang_FunctionDecl_uses_seh_try(ptr)
    }
    inline fun setUsesSEHTry(UST: Boolean): Unit {
        return clang_FunctionDecl_set_uses_seh_try(ptr, UST)
    }
    inline fun isDeleted(): Boolean {
        return clang_FunctionDecl_is_deleted(ptr)
    }
    inline fun isDeletedAsWritten(): Boolean {
        return clang_FunctionDecl_is_deleted_as_written(ptr)
    }
    inline fun setDeletedAsWritten(D: Boolean = true): Unit {
        return clang_FunctionDecl_set_deleted_as_written(ptr, D)
    }
    inline fun isMain(): Boolean {
        return clang_FunctionDecl_is_main(ptr)
    }
    inline fun isMSVCRTEntryPoint(): Boolean {
        return clang_FunctionDecl_is_msvcrt_entry_point(ptr)
    }
    inline fun isReservedGlobalPlacementOperator(): Boolean {
        return clang_FunctionDecl_is_reserved_global_placement_operator(ptr)
    }
    inline fun isReplaceableGlobalAllocationFunction(): Boolean {
        return clang_FunctionDecl_is_replaceable_global_allocation_function(ptr)
    }
    inline fun isUsableAsGlobalAllocationFunctionInConstantEvaluation(): Boolean {
        return clang_FunctionDecl_is_usable_as_global_allocation_function_in_constant_evaluation(ptr)
    }
    inline fun isInlineBuiltinDeclaration(): Boolean {
        return clang_FunctionDecl_is_inline_builtin_declaration(ptr)
    }
    inline fun isDestroyingOperatorDelete(): Boolean {
        return clang_FunctionDecl_is_destroying_operator_delete(ptr)
    }
    inline fun setIsDestroyingOperatorDelete(IsDestroyingDelete: Boolean): Unit {
        return clang_FunctionDecl_set_is_destroying_operator_delete(ptr, IsDestroyingDelete)
    }
    inline fun isTypeAwareOperatorNewOrDelete(): Boolean {
        return clang_FunctionDecl_is_type_aware_operator_new_or_delete(ptr)
    }
    inline fun setIsTypeAwareOperatorNewOrDelete(IsTypeAwareOperator: Boolean = true): Unit {
        return clang_FunctionDecl_set_is_type_aware_operator_new_or_delete(ptr, IsTypeAwareOperator)
    }
    inline fun getLanguageLinkage(): LanguageLinkage {
        return LanguageLinkageCompanionfromValue(clang_FunctionDecl_get_language_linkage(ptr))
    }
    inline fun isExternC(): Boolean {
        return clang_FunctionDecl_is_extern_c(ptr)
    }
    inline fun isInExternCContext(): Boolean {
        return clang_FunctionDecl_is_in_extern_c_context(ptr)
    }
    inline fun isInExternCXXContext(): Boolean {
        return clang_FunctionDecl_is_in_extern_cxx_context(ptr)
    }
    inline fun isGlobal(): Boolean {
        return clang_FunctionDecl_is_global(ptr)
    }
    inline fun isNoReturn(): Boolean {
        return clang_FunctionDecl_is_no_return(ptr)
    }
    inline fun isAnalyzerNoReturn(): Boolean {
        return clang_FunctionDecl_is_analyzer_no_return(ptr)
    }
    inline fun hasSkippedBody(): Boolean {
        return clang_FunctionDecl_has_skipped_body(ptr)
    }
    inline fun setHasSkippedBody(Skipped: Boolean = true): Unit {
        return clang_FunctionDecl_set_has_skipped_body(ptr, Skipped)
    }
    inline fun willHaveBody(): Boolean {
        return clang_FunctionDecl_will_have_body(ptr)
    }
    inline fun setWillHaveBody(V: Boolean = true): Unit {
        return clang_FunctionDecl_set_will_have_body(ptr, V)
    }
    inline fun isMultiVersion(): Boolean {
        return clang_FunctionDecl_is_multi_version(ptr)
    }
    inline fun setIsMultiVersion(V: Boolean = true): Unit {
        return clang_FunctionDecl_set_is_multi_version(ptr, V)
    }
    inline fun setFriendConstraintRefersToEnclosingTemplate(V: Boolean = true): Unit {
        return clang_FunctionDecl_set_friend_constraint_refers_to_enclosing_template(ptr, V)
    }
    inline fun FriendConstraintRefersToEnclosingTemplate(): Boolean {
        return clang_FunctionDecl_friend_constraint_refers_to_enclosing_template(ptr)
    }
    inline fun isMemberLikeConstrainedFriend(): Boolean {
        return clang_FunctionDecl_is_member_like_constrained_friend(ptr)
    }
    inline fun getMultiVersionKind(): MultiVersionKind {
        return MultiVersionKindCompanionfromValue(clang_FunctionDecl_get_multi_version_kind(ptr))
    }
    inline fun isCPUDispatchMultiVersion(): Boolean {
        return clang_FunctionDecl_is_cpu_dispatch_multi_version(ptr)
    }
    inline fun isCPUSpecificMultiVersion(): Boolean {
        return clang_FunctionDecl_is_cpu_specific_multi_version(ptr)
    }
    inline fun isTargetMultiVersion(): Boolean {
        return clang_FunctionDecl_is_target_multi_version(ptr)
    }
    inline fun isTargetMultiVersionDefault(): Boolean {
        return clang_FunctionDecl_is_target_multi_version_default(ptr)
    }
    inline fun isTargetClonesMultiVersion(): Boolean {
        return clang_FunctionDecl_is_target_clones_multi_version(ptr)
    }
    inline fun isTargetVersionMultiVersion(): Boolean {
        return clang_FunctionDecl_is_target_version_multi_version(ptr)
    }
    inline fun setPreviousDeclaration(PrevDecl: FunctionDeclApi?): Unit {
        return clang_FunctionDecl_set_previous_declaration(ptr, PrevDecl?.ptr)
    }
    inline fun getBuiltinID(ConsiderWrapperFunctions: Boolean = false): UInt {
        return clang_FunctionDecl_get_builtin_id(ptr, ConsiderWrapperFunctions)
    }
    inline fun param_empty(): Boolean {
        return clang_FunctionDecl_param_empty(ptr)
    }
    inline fun param_size(): size_t {
        return clang_FunctionDecl_param_size(ptr)
    }
    inline fun getNumParams(): UInt {
        return clang_FunctionDecl_get_num_params(ptr)
    }
    inline fun getParamDecl(i: UInt): ParmVarDecl? {
        return ParmVarDecl((clang_FunctionDecl_get_param_decl(ptr, i) ?: return null), memScope)
    }
    inline fun getMinRequiredArguments(): UInt {
        return clang_FunctionDecl_get_min_required_arguments(ptr)
    }
    inline fun getMinRequiredExplicitArguments(): UInt {
        return clang_FunctionDecl_get_min_required_explicit_arguments(ptr)
    }
    inline fun hasCXXExplicitFunctionObjectParameter(): Boolean {
        return clang_FunctionDecl_has_cxx_explicit_function_object_parameter(ptr)
    }
    inline fun getNumNonObjectParams(): UInt {
        return clang_FunctionDecl_get_num_non_object_params(ptr)
    }
    inline fun getNonObjectParameter(I: UInt): ParmVarDecl? {
        return ParmVarDecl((clang_FunctionDecl_get_non_object_parameter(ptr, I) ?: return null), memScope)
    }
    inline fun hasOneParamOrDefaultArgs(): Boolean {
        return clang_FunctionDecl_has_one_param_or_default_args(ptr)
    }
    inline fun getReturnType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_FunctionDecl_get_return_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getDeclaredReturnType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_FunctionDecl_get_declared_return_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getExceptionSpecType(): ExceptionSpecificationType {
        return ExceptionSpecificationTypeCompanionfromValue(clang_FunctionDecl_get_exception_spec_type(ptr))
    }
    inline fun getCallResultType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_FunctionDecl_get_call_result_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getStorageClass(): StorageClass {
        return StorageClassCompanionfromValue(clang_FunctionDecl_get_storage_class(ptr))
    }
    inline fun setStorageClass(SClass: StorageClass): Unit {
        return clang_FunctionDecl_set_storage_class(ptr, SClass.value)
    }
    inline fun isInlineSpecified(): Boolean {
        return clang_FunctionDecl_is_inline_specified(ptr)
    }
    inline fun setInlineSpecified(I: Boolean): Unit {
        return clang_FunctionDecl_set_inline_specified(ptr, I)
    }
    inline fun UsesFPIntrin(): Boolean {
        return clang_FunctionDecl_uses_fp_intrin(ptr)
    }
    inline fun setUsesFPIntrin(I: Boolean): Unit {
        return clang_FunctionDecl_set_uses_fp_intrin(ptr, I)
    }
    inline fun setImplicitlyInline(I: Boolean = true): Unit {
        return clang_FunctionDecl_set_implicitly_inline(ptr, I)
    }
    inline fun isInlined(): Boolean {
        return clang_FunctionDecl_is_inlined(ptr)
    }
    inline fun isInlineDefinitionExternallyVisible(): Boolean {
        return clang_FunctionDecl_is_inline_definition_externally_visible(ptr)
    }
    inline fun isMSExternInline(): Boolean {
        return clang_FunctionDecl_is_ms_extern_inline(ptr)
    }
    inline fun doesDeclarationForceExternallyVisibleDefinition(): Boolean {
        return clang_FunctionDecl_does_declaration_force_externally_visible_definition(ptr)
    }
    inline fun isOverloadedOperator(): Boolean {
        return clang_FunctionDecl_is_overloaded_operator(ptr)
    }
    inline fun getOverloadedOperator(): OverloadedOperatorKind {
        return OverloadedOperatorKindCompanionfromValue(clang_FunctionDecl_get_overloaded_operator(ptr))
    }
    inline fun getInstantiatedFromMemberFunction(): FunctionDeclApi? {
        return FunctionDecl((clang_FunctionDecl_get_instantiated_from_member_function(ptr) ?: return null), memScope)
    }
    inline fun getTemplatedKind(): TemplatedKind {
        return TemplatedKindCompanionfromValue(clang_FunctionDecl_get_templated_kind(ptr))
    }
    inline fun setInstantiationOfMemberFunction(FD: FunctionDeclApi?, TSK: TemplateSpecializationKind): Unit {
        return clang_FunctionDecl_set_instantiation_of_member_function(ptr, FD?.ptr, TSK.value)
    }
    inline fun setInstantiatedFromDecl(FD: FunctionDeclApi?): Unit {
        return clang_FunctionDecl_set_instantiated_from_decl(ptr, FD?.ptr)
    }
    inline fun getInstantiatedFromDecl(): FunctionDeclApi? {
        return FunctionDecl((clang_FunctionDecl_get_instantiated_from_decl(ptr) ?: return null), memScope)
    }
    inline fun isFunctionTemplateSpecialization(): Boolean {
        return clang_FunctionDecl_is_function_template_specialization(ptr)
    }
    inline fun isImplicitlyInstantiable(): Boolean {
        return clang_FunctionDecl_is_implicitly_instantiable(ptr)
    }
    inline fun isTemplateInstantiation(): Boolean {
        return clang_FunctionDecl_is_template_instantiation(ptr)
    }
    inline fun getTemplateInstantiationPattern(ForDefinition: Boolean = true): FunctionDeclApi? {
        return FunctionDecl((clang_FunctionDecl_get_template_instantiation_pattern(ptr, ForDefinition) ?: return null), memScope)
    }
    inline fun getTemplateSpecializationArgs(): TemplateArgumentList? {
        return TemplateArgumentList((clang_FunctionDecl_get_template_specialization_args(ptr) ?: return null), memScope)
    }
    inline fun getTemplateSpecializationKind(): TemplateSpecializationKind {
        return TemplateSpecializationKindCompanionfromValue(clang_FunctionDecl_get_template_specialization_kind(ptr))
    }
    inline fun getTemplateSpecializationKindForInstantiation(): TemplateSpecializationKind {
        return TemplateSpecializationKindCompanionfromValue(clang_FunctionDecl_get_template_specialization_kind_for_instantiation(ptr))
    }
    inline fun setTemplateSpecializationKind(TSK: TemplateSpecializationKind): Unit {
        return clang_FunctionDecl_set_template_specialization_kind(ptr, TSK.value)
    }
    override fun isOutOfLine(): Boolean {
        return clang_FunctionDecl_is_out_of_line(ptr)
    }
    inline fun getMemoryFunctionKind(): UInt {
        return clang_FunctionDecl_get_memory_function_kind(ptr)
    }
    inline fun getODRHash(): UInt {
        return clang_FunctionDecl_get_odr_hash(ptr)
    }
    inline fun isReferenceableKernel(): Boolean {
        return clang_FunctionDecl_is_referenceable_kernel(ptr)
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
    inline fun isInitCapture(): Boolean {
        return clang_ValueDecl_is_init_capture(ptr)
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
    inline fun setOperatorGlobalDelete(OD: FunctionDeclApi?): Unit {
        return clang_CXXDestructorDecl_set_operator_global_delete(ptr, OD?.ptr)
    }
    inline fun setOperatorArrayDelete(OD: FunctionDeclApi?): Unit {
        return clang_CXXDestructorDecl_set_operator_array_delete(ptr, OD?.ptr)
    }
    inline fun setGlobalOperatorArrayDelete(OD: FunctionDeclApi?): Unit {
        return clang_CXXDestructorDecl_set_global_operator_array_delete(ptr, OD?.ptr)
    }
    inline fun getOperatorDelete(): FunctionDeclApi? {
        return FunctionDecl((clang_CXXDestructorDecl_get_operator_delete(ptr) ?: return null), memScope)
    }
    inline fun getOperatorGlobalDelete(): FunctionDeclApi? {
        return FunctionDecl((clang_CXXDestructorDecl_get_operator_global_delete(ptr) ?: return null), memScope)
    }
    inline fun getArrayOperatorDelete(): FunctionDeclApi? {
        return FunctionDecl((clang_CXXDestructorDecl_get_array_operator_delete(ptr) ?: return null), memScope)
    }
    inline fun getGlobalArrayOperatorDelete(): FunctionDeclApi? {
        return FunctionDecl((clang_CXXDestructorDecl_get_global_array_operator_delete(ptr) ?: return null), memScope)
    }
    inline fun isCalledByDelete(OpDel: FunctionDeclApi? = null): Boolean {
        return clang_CXXDestructorDecl_is_called_by_delete(ptr, OpDel?.ptr)
    }
    inline fun cXXDestructorDeclGetCanonicalDecl(): CXXDestructorDecl? {
        return CXXDestructorDecl((clang_CXXDestructorDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_CXXDestructorDecl_size_of()
            }

        val align: Int
            inline get() {
                return clang_CXXDestructorDecl_align_of()
            }

        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_CXXDestructorDecl_classof(D?.ptr)
        }
        inline fun MemScope.classofKind(K: Kind): Boolean {
            return clang_CXXDestructorDecl_classof_kind(K.value)
        }
        fun MemScope.CXXDestructorDecl_Holder(): CXXDestructorDecl {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return CXXDestructorDecl(memory, this)
        }
    }
    inline fun asCXXMethodDecl(): CXXMethodDecl {
        return CXXMethodDecl(clang_CXXDestructorDecl_as_clang_CXXMethodDecl(ptr)!!, memScope)
    }
    inline fun asFunctionDecl(): FunctionDecl {
        return FunctionDecl(clang_CXXDestructorDecl_as_clang_FunctionDecl(ptr)!!, memScope)
    }
    inline fun asDeclaratorDecl(): DeclaratorDecl {
        return DeclaratorDecl(clang_CXXDestructorDecl_as_clang_DeclaratorDecl(ptr)!!, memScope)
    }
    inline fun asDeclContext(): DeclContext {
        return DeclContext(clang_CXXDestructorDecl_as_clang_DeclContext(ptr)!!, memScope)
    }
    inline fun asValueDecl(): ValueDecl {
        return ValueDecl(clang_CXXDestructorDecl_as_clang_ValueDecl(ptr)!!, memScope)
    }
    inline fun asNamedDecl(): NamedDecl {
        return NamedDecl(clang_CXXDestructorDecl_as_clang_NamedDecl(ptr)!!, memScope)
    }
    inline fun asDecl(): Decl {
        return Decl(clang_CXXDestructorDecl_as_clang_Decl(ptr)!!, memScope)
    }
}

// END KRAPPER GEN for clang::CXXDestructorDecl


