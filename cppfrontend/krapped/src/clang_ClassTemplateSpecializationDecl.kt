package clang

import clang.AccessSpecifier.Companion.fromValue as AccessSpecifierCompanionfromValue
import clang.AvailabilityResult.Companion.fromValue as AvailabilityResultCompanionfromValue
import clang.LambdaCaptureDefault.Companion.fromValue
import clang.Linkage.Companion.fromValue as LinkageCompanionfromValue
import clang.MSInheritanceModel.Companion.fromValue as MSInheritanceModelCompanionfromValue
import clang.ObjCStringFormatFamily.Companion.fromValue as ObjCStringFormatFamilyCompanionfromValue
import clang.RecordArgPassingKind.Companion.fromValue as RecordArgPassingKindCompanionfromValue
import clang.TagTypeKind.Companion.fromValue as TagTypeKindCompanionfromValue
import clang.TemplateSpecializationKind.Companion.fromValue as CompanionfromValue
import clang.Visibility.Companion.fromValue as VisibilityCompanionfromValue
import clang.decl.FriendObjectKind
import clang.decl.FriendObjectKind.Companion.fromValue as FriendObjectKindCompanionfromValue
import clang.decl.Kind
import clang.decl.Kind.Companion.fromValue as KindCompanionfromValue
import clang.decl.ModuleOwnershipKind
import clang.decl.ModuleOwnershipKind.Companion.fromValue as ModuleOwnershipKindCompanionfromValue
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
import krapper.cppfrontend.internal.clang_CXXRecordDecl_added_eligible_special_member_function
import krapper.cppfrontend.internal.clang_CXXRecordDecl_added_selected_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_allow_const_default_init
import krapper.cppfrontend.internal.clang_CXXRecordDecl_bases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_calculate_inheritance_model
import krapper.cppfrontend.internal.clang_CXXRecordDecl_capture_size
import krapper.cppfrontend.internal.clang_CXXRecordDecl_complete_definition
import krapper.cppfrontend.internal.clang_CXXRecordDecl_defaulted_copy_constructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_defaulted_default_constructor_is_constexpr
import krapper.cppfrontend.internal.clang_CXXRecordDecl_defaulted_destructor_is_constexpr
import krapper.cppfrontend.internal.clang_CXXRecordDecl_defaulted_destructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_defaulted_move_constructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_finished_defaulted_or_deleted_member
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_canonical_decl
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_definition
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_definition_or_self
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_described_class_template
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_device_lambda_mangling_number
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_generic_lambda_template_parameter_list
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_instantiated_from_member_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_call_operator
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_capture_default
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_context_decl
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_dependency_kind
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_index_in_context
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_mangling_number
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_static_invoker
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_lambda_static_invoker__clang_CallingConv
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_most_recent_decl
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_ms_inheritance_model
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_num_bases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_num_v_bases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_odr_hash
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_previous_decl
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_standard_layout_base_with_fields
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_template_instantiation_pattern
import krapper.cppfrontend.internal.clang_CXXRecordDecl_get_template_specialization_kind
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_any_dependent_bases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_constexpr_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_constexpr_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_constexpr_non_copy_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_copy_assignment_with_const_param
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_copy_constructor_with_const_param
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_definition
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_deleted_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_direct_fields
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_friends
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_in_class_initializer
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_inherited_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_inherited_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_init_method
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_injected_class_type
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_irrelevant_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_known_lambda_internal_linkage
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_mutable_fields
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_literal_type_fields_or_bases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_copy_constructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_destructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_non_trivial_move_constructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_private_fields
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_protected_fields
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_simple_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_simple_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_simple_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_simple_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_simple_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_copy_constructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_destructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_trivial_move_constructor_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_uninitialized_reference_member
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_declared_move_operation
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_user_provided_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_has_variant_members
import krapper.cppfrontend.internal.clang_CXXRecordDecl_implicit_copy_assignment_has_const_param
import krapper.cppfrontend.internal.clang_CXXRecordDecl_implicit_copy_constructor_has_const_param
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_abstract
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_aggregate
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_any_destructor_no_return
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_c_like
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_captureless_lambda
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_current_instantiation
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_cxx11standard_layout
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_dependent_lambda
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_derived_from
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_dynamic_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_effectively_final
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_empty
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_generic_lambda
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_hlsl_intangible
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_injected_class_name
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_interface_like
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_lambda
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_literal
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_local_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_never_dependent_lambda
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_parsing_base_specifiers
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_pod
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_polymorphic
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_provably_not_derived_from
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_standard_layout
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_structural
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_trivial
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_trivially_copy_constructible
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_trivially_copyable
import krapper.cppfrontend.internal.clang_CXXRecordDecl_is_virtually_derived_from
import krapper.cppfrontend.internal.clang_CXXRecordDecl_lambda_is_default_constructible_and_assignable
import krapper.cppfrontend.internal.clang_CXXRecordDecl_mark_abstract
import krapper.cppfrontend.internal.clang_CXXRecordDecl_may_be_abstract
import krapper.cppfrontend.internal.clang_CXXRecordDecl_may_be_dynamic_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_may_be_non_dynamic_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_default_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_implicit_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_overload_resolution_for_copy_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_overload_resolution_for_copy_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_overload_resolution_for_destructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_overload_resolution_for_move_assignment
import krapper.cppfrontend.internal.clang_CXXRecordDecl_needs_overload_resolution_for_move_constructor
import krapper.cppfrontend.internal.clang_CXXRecordDecl_null_field_offset_is_zero
import krapper.cppfrontend.internal.clang_CXXRecordDecl_remove_conversion
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_described_class_template
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_has_trivial_special_member_for_call
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_implicit_copy_assignment_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_implicit_copy_constructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_implicit_destructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_implicit_move_assignment_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_implicit_move_constructor_is_deleted
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_init_method
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_instantiation_of_member_class
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_is_parsing_base_specifiers
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_lambda_dependency_kind
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_lambda_is_generic
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_template_specialization_kind
import krapper.cppfrontend.internal.clang_CXXRecordDecl_set_trivial_for_call_flags
import krapper.cppfrontend.internal.clang_CXXRecordDecl_vbases
import krapper.cppfrontend.internal.clang_CXXRecordDecl_view_inheritance
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_align_of
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_CXXRecordDecl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_Decl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_DeclContext
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_NamedDecl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_RecordDecl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_TagDecl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_as_clang_TypeDecl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_classof
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_classof_kind
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_definition_or_self
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_most_recent_decl
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_specialization_kind
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_specialized_template
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_template_args
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_get_template_instantiation_args
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_has_strict_pack_match
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_is_class_scope_explicit_specialization
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_is_explicit_instantiation_or_specialization
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_is_explicit_specialization
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_set_instantiation_of__clang_ClassTemplateDecl_P
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_set_specialization_kind
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_set_specialized_template
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_set_strict_pack_match
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_set_template_args
import krapper.cppfrontend.internal.clang_ClassTemplateSpecializationDecl_size_of
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
import krapper.cppfrontend.internal.clang_Decl_is_out_of_line
import krapper.cppfrontend.internal.clang_Decl_is_parameter_pack
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
import krapper.cppfrontend.internal.clang_NamedDecl_declaration_replaces
import krapper.cppfrontend.internal.clang_NamedDecl_get_formal_linkage
import krapper.cppfrontend.internal.clang_NamedDecl_get_linkage_internal
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
import krapper.cppfrontend.internal.clang_RecordDecl_can_pass_in_registers
import krapper.cppfrontend.internal.clang_RecordDecl_field_empty
import krapper.cppfrontend.internal.clang_RecordDecl_find_first_named_data_member
import krapper.cppfrontend.internal.clang_RecordDecl_get_arg_passing_restrictions
import krapper.cppfrontend.internal.clang_RecordDecl_get_num_fields
import krapper.cppfrontend.internal.clang_RecordDecl_has_flexible_array_member
import krapper.cppfrontend.internal.clang_RecordDecl_has_loaded_fields_from_external_storage
import krapper.cppfrontend.internal.clang_RecordDecl_has_non_trivial_to_primitive_copy_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_has_non_trivial_to_primitive_default_initialize_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_has_non_trivial_to_primitive_destruct_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_has_object_member
import krapper.cppfrontend.internal.clang_RecordDecl_has_uninitialized_explicit_init_fields
import krapper.cppfrontend.internal.clang_RecordDecl_has_volatile_member
import krapper.cppfrontend.internal.clang_RecordDecl_is_anonymous_struct_or_union
import krapper.cppfrontend.internal.clang_RecordDecl_is_captured_record
import krapper.cppfrontend.internal.clang_RecordDecl_is_ms_struct
import krapper.cppfrontend.internal.clang_RecordDecl_is_non_trivial_to_primitive_copy
import krapper.cppfrontend.internal.clang_RecordDecl_is_non_trivial_to_primitive_default_initialize
import krapper.cppfrontend.internal.clang_RecordDecl_is_non_trivial_to_primitive_destroy
import krapper.cppfrontend.internal.clang_RecordDecl_is_or_contains_union
import krapper.cppfrontend.internal.clang_RecordDecl_is_param_destroyed_in_callee
import krapper.cppfrontend.internal.clang_RecordDecl_is_randomized
import krapper.cppfrontend.internal.clang_RecordDecl_may_insert_extra_padding
import krapper.cppfrontend.internal.clang_RecordDecl_noload_field_empty
import krapper.cppfrontend.internal.clang_RecordDecl_set_anonymous_struct_or_union
import krapper.cppfrontend.internal.clang_RecordDecl_set_arg_passing_restrictions
import krapper.cppfrontend.internal.clang_RecordDecl_set_captured_record
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_flexible_array_member
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_loaded_fields_from_external_storage
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_non_trivial_to_primitive_copy_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_non_trivial_to_primitive_default_initialize_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_non_trivial_to_primitive_destruct_c_union
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_object_member
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_uninitialized_explicit_init_fields
import krapper.cppfrontend.internal.clang_RecordDecl_set_has_volatile_member
import krapper.cppfrontend.internal.clang_RecordDecl_set_is_randomized
import krapper.cppfrontend.internal.clang_RecordDecl_set_non_trivial_to_primitive_copy
import krapper.cppfrontend.internal.clang_RecordDecl_set_non_trivial_to_primitive_default_initialize
import krapper.cppfrontend.internal.clang_RecordDecl_set_non_trivial_to_primitive_destroy
import krapper.cppfrontend.internal.clang_RecordDecl_set_param_destroyed_in_callee
import krapper.cppfrontend.internal.clang_TagDecl_demote_this_definition_to_declaration
import krapper.cppfrontend.internal.clang_TagDecl_get_kind_name
import krapper.cppfrontend.internal.clang_TagDecl_get_num_template_parameter_lists
import krapper.cppfrontend.internal.clang_TagDecl_get_tag_kind
import krapper.cppfrontend.internal.clang_TagDecl_get_template_parameter_list
import krapper.cppfrontend.internal.clang_TagDecl_get_typedef_name_for_anon_decl
import krapper.cppfrontend.internal.clang_TagDecl_has_name_for_linkage
import krapper.cppfrontend.internal.clang_TagDecl_is_being_defined
import krapper.cppfrontend.internal.clang_TagDecl_is_class
import krapper.cppfrontend.internal.clang_TagDecl_is_complete_definition
import krapper.cppfrontend.internal.clang_TagDecl_is_complete_definition_required
import krapper.cppfrontend.internal.clang_TagDecl_is_dependent_type
import krapper.cppfrontend.internal.clang_TagDecl_is_embedded_in_declarator
import krapper.cppfrontend.internal.clang_TagDecl_is_entity_being_defined
import krapper.cppfrontend.internal.clang_TagDecl_is_enum
import krapper.cppfrontend.internal.clang_TagDecl_is_free_standing
import krapper.cppfrontend.internal.clang_TagDecl_is_interface
import krapper.cppfrontend.internal.clang_TagDecl_is_struct
import krapper.cppfrontend.internal.clang_TagDecl_is_structure_or_class
import krapper.cppfrontend.internal.clang_TagDecl_is_this_declaration_a_definition
import krapper.cppfrontend.internal.clang_TagDecl_is_this_declaration_a_demoted_definition
import krapper.cppfrontend.internal.clang_TagDecl_is_union
import krapper.cppfrontend.internal.clang_TagDecl_set_complete_definition
import krapper.cppfrontend.internal.clang_TagDecl_set_complete_definition_required
import krapper.cppfrontend.internal.clang_TagDecl_set_embedded_in_declarator
import krapper.cppfrontend.internal.clang_TagDecl_set_free_standing
import krapper.cppfrontend.internal.clang_TagDecl_set_tag_kind
import krapper.cppfrontend.internal.clang_TagDecl_set_typedef_name_for_anon_decl
import krapper.cppfrontend.internal.clang_TagDecl_start_definition
import krapper.cppfrontend.internal.clang_TypeDecl_get_type_for_decl
import krapper.cppfrontend.internal.clang_TypeDecl_set_type_for_decl
import platform.linux.free
import std.Vector__CXXBaseSpecifier_P
import std.Vector__CXXBaseSpecifier_P.Companion.Vector__CXXBaseSpecifier_P_Holder
import std.Vector__Decl_P
import std.Vector__Decl_P.Companion.Vector__Decl_P_Holder

// BEGIN KRAPPER GEN for clang::ClassTemplateSpecializationDecl

@krapper.CppBinding("clang::ClassTemplateSpecializationDecl")
class ClassTemplateSpecializationDecl(
    override val ptr: COpaquePointer,
    val memScope: MemScope,
) : clang.CXXRecordDeclApi, clang.RecordDeclApi, clang.TagDeclApi, clang.TypeDeclApi, clang.NamedDeclApi, clang.DeclApi {
    inline fun cXXRecordDeclGetCanonicalDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    inline fun cXXRecordDeclGetPreviousDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_previous_decl(ptr) ?: return null), memScope)
    }
    inline fun cXXRecordDeclGetMostRecentDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun cXXRecordDeclGetDefinition(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_definition(ptr) ?: return null), memScope)
    }
    inline fun cXXRecordDeclGetDefinitionOrSelf(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_definition_or_self(ptr) ?: return null), memScope)
    }
    inline fun hasDefinition(): Boolean {
        return clang_CXXRecordDecl_has_definition(ptr)
    }
    inline fun isDynamicClass(): Boolean {
        return clang_CXXRecordDecl_is_dynamic_class(ptr)
    }
    inline fun mayBeDynamicClass(): Boolean {
        return clang_CXXRecordDecl_may_be_dynamic_class(ptr)
    }
    inline fun mayBeNonDynamicClass(): Boolean {
        return clang_CXXRecordDecl_may_be_non_dynamic_class(ptr)
    }
    inline fun setIsParsingBaseSpecifiers(): Unit {
        return clang_CXXRecordDecl_set_is_parsing_base_specifiers(ptr)
    }
    inline fun isParsingBaseSpecifiers(): Boolean {
        return clang_CXXRecordDecl_is_parsing_base_specifiers(ptr)
    }
    inline fun cXXRecordDeclGetODRHash(): UInt {
        return clang_CXXRecordDecl_get_odr_hash(ptr)
    }
    inline fun getNumBases(): UInt {
        return clang_CXXRecordDecl_get_num_bases(ptr)
    }
    inline fun bases(): Vector__CXXBaseSpecifier_P {
        val retValue: Vector__CXXBaseSpecifier_P = memScope.Vector__CXXBaseSpecifier_P_Holder()
        clang_CXXRecordDecl_bases(ptr, retValue.ptr)
        return retValue
    }
    inline fun getNumVBases(): UInt {
        return clang_CXXRecordDecl_get_num_v_bases(ptr)
    }
    inline fun vbases(): Vector__CXXBaseSpecifier_P {
        val retValue: Vector__CXXBaseSpecifier_P = memScope.Vector__CXXBaseSpecifier_P_Holder()
        clang_CXXRecordDecl_vbases(ptr, retValue.ptr)
        return retValue
    }
    inline fun hasAnyDependentBases(): Boolean {
        return clang_CXXRecordDecl_has_any_dependent_bases(ptr)
    }
    inline fun hasFriends(): Boolean {
        return clang_CXXRecordDecl_has_friends(ptr)
    }
    inline fun defaultedCopyConstructorIsDeleted(): Boolean {
        return clang_CXXRecordDecl_defaulted_copy_constructor_is_deleted(ptr)
    }
    inline fun defaultedMoveConstructorIsDeleted(): Boolean {
        return clang_CXXRecordDecl_defaulted_move_constructor_is_deleted(ptr)
    }
    inline fun defaultedDestructorIsDeleted(): Boolean {
        return clang_CXXRecordDecl_defaulted_destructor_is_deleted(ptr)
    }
    inline fun hasSimpleCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_has_simple_copy_constructor(ptr)
    }
    inline fun hasSimpleMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_simple_move_constructor(ptr)
    }
    inline fun hasSimpleCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_has_simple_copy_assignment(ptr)
    }
    inline fun hasSimpleMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_has_simple_move_assignment(ptr)
    }
    inline fun hasSimpleDestructor(): Boolean {
        return clang_CXXRecordDecl_has_simple_destructor(ptr)
    }
    inline fun hasDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_has_default_constructor(ptr)
    }
    inline fun needsImplicitDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_default_constructor(ptr)
    }
    inline fun hasUserDeclaredConstructor(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_constructor(ptr)
    }
    inline fun hasUserProvidedDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_has_user_provided_default_constructor(ptr)
    }
    inline fun hasUserDeclaredCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_copy_constructor(ptr)
    }
    inline fun needsImplicitCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_copy_constructor(ptr)
    }
    inline fun needsOverloadResolutionForCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_needs_overload_resolution_for_copy_constructor(ptr)
    }
    inline fun implicitCopyConstructorHasConstParam(): Boolean {
        return clang_CXXRecordDecl_implicit_copy_constructor_has_const_param(ptr)
    }
    inline fun hasCopyConstructorWithConstParam(): Boolean {
        return clang_CXXRecordDecl_has_copy_constructor_with_const_param(ptr)
    }
    inline fun hasUserDeclaredMoveOperation(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_move_operation(ptr)
    }
    inline fun hasUserDeclaredMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_move_constructor(ptr)
    }
    inline fun hasMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_move_constructor(ptr)
    }
    inline fun setImplicitCopyConstructorIsDeleted(): Unit {
        return clang_CXXRecordDecl_set_implicit_copy_constructor_is_deleted(ptr)
    }
    inline fun setImplicitMoveConstructorIsDeleted(): Unit {
        return clang_CXXRecordDecl_set_implicit_move_constructor_is_deleted(ptr)
    }
    inline fun setImplicitDestructorIsDeleted(): Unit {
        return clang_CXXRecordDecl_set_implicit_destructor_is_deleted(ptr)
    }
    inline fun needsImplicitMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_move_constructor(ptr)
    }
    inline fun needsOverloadResolutionForMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_needs_overload_resolution_for_move_constructor(ptr)
    }
    inline fun hasUserDeclaredCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_copy_assignment(ptr)
    }
    inline fun setImplicitCopyAssignmentIsDeleted(): Unit {
        return clang_CXXRecordDecl_set_implicit_copy_assignment_is_deleted(ptr)
    }
    inline fun needsImplicitCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_copy_assignment(ptr)
    }
    inline fun needsOverloadResolutionForCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_needs_overload_resolution_for_copy_assignment(ptr)
    }
    inline fun implicitCopyAssignmentHasConstParam(): Boolean {
        return clang_CXXRecordDecl_implicit_copy_assignment_has_const_param(ptr)
    }
    inline fun hasCopyAssignmentWithConstParam(): Boolean {
        return clang_CXXRecordDecl_has_copy_assignment_with_const_param(ptr)
    }
    inline fun hasUserDeclaredMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_move_assignment(ptr)
    }
    inline fun hasMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_has_move_assignment(ptr)
    }
    inline fun setImplicitMoveAssignmentIsDeleted(): Unit {
        return clang_CXXRecordDecl_set_implicit_move_assignment_is_deleted(ptr)
    }
    inline fun needsImplicitMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_move_assignment(ptr)
    }
    inline fun needsOverloadResolutionForMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_needs_overload_resolution_for_move_assignment(ptr)
    }
    inline fun hasUserDeclaredDestructor(): Boolean {
        return clang_CXXRecordDecl_has_user_declared_destructor(ptr)
    }
    inline fun needsImplicitDestructor(): Boolean {
        return clang_CXXRecordDecl_needs_implicit_destructor(ptr)
    }
    inline fun needsOverloadResolutionForDestructor(): Boolean {
        return clang_CXXRecordDecl_needs_overload_resolution_for_destructor(ptr)
    }
    inline fun cXXRecordDeclIsLambda(): Boolean {
        return clang_CXXRecordDecl_is_lambda(ptr)
    }
    inline fun isGenericLambda(): Boolean {
        return clang_CXXRecordDecl_is_generic_lambda(ptr)
    }
    inline fun lambdaIsDefaultConstructibleAndAssignable(): Boolean {
        return clang_CXXRecordDecl_lambda_is_default_constructible_and_assignable(ptr)
    }
    inline fun getLambdaCallOperator(): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXRecordDecl_get_lambda_call_operator(ptr) ?: return null), memScope)
    }
    inline fun getLambdaStaticInvoker(): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXRecordDecl_get_lambda_static_invoker(ptr) ?: return null), memScope)
    }
    inline fun getLambdaStaticInvoker__clang_CallingConv(CC: CallingConv): CXXMethodDeclApi? {
        return CXXMethodDecl((clang_CXXRecordDecl_get_lambda_static_invoker__clang_CallingConv(ptr, CC.value) ?: return null), memScope)
    }
    inline fun getGenericLambdaTemplateParameterList(): TemplateParameterList? {
        return TemplateParameterList((clang_CXXRecordDecl_get_generic_lambda_template_parameter_list(ptr) ?: return null), memScope)
    }
    inline fun getLambdaCaptureDefault(): LambdaCaptureDefault {
        return fromValue(clang_CXXRecordDecl_get_lambda_capture_default(ptr))
    }
    inline fun isCapturelessLambda(): Boolean {
        return clang_CXXRecordDecl_is_captureless_lambda(ptr)
    }
    inline fun capture_size(): UInt {
        return clang_CXXRecordDecl_capture_size(ptr)
    }
    inline fun removeConversion(Old: NamedDeclApi?): Unit {
        return clang_CXXRecordDecl_remove_conversion(ptr, Old?.ptr)
    }
    inline fun isAggregate(): Boolean {
        return clang_CXXRecordDecl_is_aggregate(ptr)
    }
    inline fun hasInClassInitializer(): Boolean {
        return clang_CXXRecordDecl_has_in_class_initializer(ptr)
    }
    inline fun hasUninitializedReferenceMember(): Boolean {
        return clang_CXXRecordDecl_has_uninitialized_reference_member(ptr)
    }
    inline fun isPOD(): Boolean {
        return clang_CXXRecordDecl_is_pod(ptr)
    }
    inline fun isCLike(): Boolean {
        return clang_CXXRecordDecl_is_c_like(ptr)
    }
    inline fun isEmpty(): Boolean {
        return clang_CXXRecordDecl_is_empty(ptr)
    }
    inline fun setInitMethod(Val: Boolean): Unit {
        return clang_CXXRecordDecl_set_init_method(ptr, Val)
    }
    inline fun hasInitMethod(): Boolean {
        return clang_CXXRecordDecl_has_init_method(ptr)
    }
    inline fun hasPrivateFields(): Boolean {
        return clang_CXXRecordDecl_has_private_fields(ptr)
    }
    inline fun hasProtectedFields(): Boolean {
        return clang_CXXRecordDecl_has_protected_fields(ptr)
    }
    inline fun hasDirectFields(): Boolean {
        return clang_CXXRecordDecl_has_direct_fields(ptr)
    }
    inline fun getStandardLayoutBaseWithFields(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_standard_layout_base_with_fields(ptr) ?: return null), memScope)
    }
    inline fun isPolymorphic(): Boolean {
        return clang_CXXRecordDecl_is_polymorphic(ptr)
    }
    inline fun isAbstract(): Boolean {
        return clang_CXXRecordDecl_is_abstract(ptr)
    }
    inline fun isStandardLayout(): Boolean {
        return clang_CXXRecordDecl_is_standard_layout(ptr)
    }
    inline fun isCXX11StandardLayout(): Boolean {
        return clang_CXXRecordDecl_is_cxx11standard_layout(ptr)
    }
    inline fun hasMutableFields(): Boolean {
        return clang_CXXRecordDecl_has_mutable_fields(ptr)
    }
    inline fun hasVariantMembers(): Boolean {
        return clang_CXXRecordDecl_has_variant_members(ptr)
    }
    inline fun hasTrivialDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_has_trivial_default_constructor(ptr)
    }
    inline fun hasNonTrivialDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_default_constructor(ptr)
    }
    inline fun hasConstexprNonCopyMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_constexpr_non_copy_move_constructor(ptr)
    }
    inline fun defaultedDefaultConstructorIsConstexpr(): Boolean {
        return clang_CXXRecordDecl_defaulted_default_constructor_is_constexpr(ptr)
    }
    inline fun hasConstexprDefaultConstructor(): Boolean {
        return clang_CXXRecordDecl_has_constexpr_default_constructor(ptr)
    }
    inline fun hasTrivialCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_has_trivial_copy_constructor(ptr)
    }
    inline fun hasTrivialCopyConstructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_trivial_copy_constructor_for_call(ptr)
    }
    inline fun hasNonTrivialCopyConstructor(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_copy_constructor(ptr)
    }
    inline fun hasNonTrivialCopyConstructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_copy_constructor_for_call(ptr)
    }
    inline fun hasTrivialMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_trivial_move_constructor(ptr)
    }
    inline fun hasTrivialMoveConstructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_trivial_move_constructor_for_call(ptr)
    }
    inline fun hasNonTrivialMoveConstructor(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_move_constructor(ptr)
    }
    inline fun hasNonTrivialMoveConstructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_move_constructor_for_call(ptr)
    }
    inline fun hasTrivialCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_has_trivial_copy_assignment(ptr)
    }
    inline fun hasNonTrivialCopyAssignment(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_copy_assignment(ptr)
    }
    inline fun hasTrivialMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_has_trivial_move_assignment(ptr)
    }
    inline fun hasNonTrivialMoveAssignment(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_move_assignment(ptr)
    }
    inline fun defaultedDestructorIsConstexpr(): Boolean {
        return clang_CXXRecordDecl_defaulted_destructor_is_constexpr(ptr)
    }
    inline fun hasConstexprDestructor(): Boolean {
        return clang_CXXRecordDecl_has_constexpr_destructor(ptr)
    }
    inline fun hasTrivialDestructor(): Boolean {
        return clang_CXXRecordDecl_has_trivial_destructor(ptr)
    }
    inline fun hasTrivialDestructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_trivial_destructor_for_call(ptr)
    }
    inline fun hasNonTrivialDestructor(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_destructor(ptr)
    }
    inline fun hasNonTrivialDestructorForCall(): Boolean {
        return clang_CXXRecordDecl_has_non_trivial_destructor_for_call(ptr)
    }
    inline fun setHasTrivialSpecialMemberForCall(): Unit {
        return clang_CXXRecordDecl_set_has_trivial_special_member_for_call(ptr)
    }
    inline fun allowConstDefaultInit(): Boolean {
        return clang_CXXRecordDecl_allow_const_default_init(ptr)
    }
    inline fun hasIrrelevantDestructor(): Boolean {
        return clang_CXXRecordDecl_has_irrelevant_destructor(ptr)
    }
    inline fun hasNonLiteralTypeFieldsOrBases(): Boolean {
        return clang_CXXRecordDecl_has_non_literal_type_fields_or_bases(ptr)
    }
    inline fun hasInheritedConstructor(): Boolean {
        return clang_CXXRecordDecl_has_inherited_constructor(ptr)
    }
    inline fun hasInheritedAssignment(): Boolean {
        return clang_CXXRecordDecl_has_inherited_assignment(ptr)
    }
    inline fun isTriviallyCopyable(): Boolean {
        return clang_CXXRecordDecl_is_trivially_copyable(ptr)
    }
    inline fun isTriviallyCopyConstructible(): Boolean {
        return clang_CXXRecordDecl_is_trivially_copy_constructible(ptr)
    }
    inline fun isTrivial(): Boolean {
        return clang_CXXRecordDecl_is_trivial(ptr)
    }
    inline fun isLiteral(): Boolean {
        return clang_CXXRecordDecl_is_literal(ptr)
    }
    inline fun isStructural(): Boolean {
        return clang_CXXRecordDecl_is_structural(ptr)
    }
    inline fun addedSelectedDestructor(DD: CXXDestructorDecl?): Unit {
        return clang_CXXRecordDecl_added_selected_destructor(ptr, DD?.ptr)
    }
    inline fun addedEligibleSpecialMemberFunction(MD: CXXMethodDeclApi?, SMKind: UInt): Unit {
        return clang_CXXRecordDecl_added_eligible_special_member_function(ptr, MD?.ptr, SMKind)
    }
    inline fun getInstantiatedFromMemberClass(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_instantiated_from_member_class(ptr) ?: return null), memScope)
    }
    inline fun setInstantiationOfMemberClass(RD: CXXRecordDeclApi?, TSK: TemplateSpecializationKind): Unit {
        return clang_CXXRecordDecl_set_instantiation_of_member_class(ptr, RD?.ptr, TSK.value)
    }
    inline fun getDescribedClassTemplate(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_CXXRecordDecl_get_described_class_template(ptr) ?: return null), memScope)
    }
    inline fun setDescribedClassTemplate(Template: ClassTemplateDecl?): Unit {
        return clang_CXXRecordDecl_set_described_class_template(ptr, Template?.ptr)
    }
    inline fun getTemplateSpecializationKind(): TemplateSpecializationKind {
        return CompanionfromValue(clang_CXXRecordDecl_get_template_specialization_kind(ptr))
    }
    inline fun setTemplateSpecializationKind(TSK: TemplateSpecializationKind): Unit {
        return clang_CXXRecordDecl_set_template_specialization_kind(ptr, TSK.value)
    }
    inline fun getTemplateInstantiationPattern(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_CXXRecordDecl_get_template_instantiation_pattern(ptr) ?: return null), memScope)
    }
    inline fun getDestructor(): CXXDestructorDecl? {
        return CXXDestructorDecl((clang_CXXRecordDecl_get_destructor(ptr) ?: return null), memScope)
    }
    inline fun hasDeletedDestructor(): Boolean {
        return clang_CXXRecordDecl_has_deleted_destructor(ptr)
    }
    inline fun isAnyDestructorNoReturn(): Boolean {
        return clang_CXXRecordDecl_is_any_destructor_no_return(ptr)
    }
    inline fun isHLSLIntangible(): Boolean {
        return clang_CXXRecordDecl_is_hlsl_intangible(ptr)
    }
    inline fun isLocalClass(): FunctionDeclApi? {
        return FunctionDecl((clang_CXXRecordDecl_is_local_class(ptr) ?: return null), memScope)
    }
    inline fun isCurrentInstantiation(CurContext: DeclContext?): Boolean {
        return clang_CXXRecordDecl_is_current_instantiation(ptr, CurContext?.ptr)
    }
    inline fun isDerivedFrom(Base: CXXRecordDeclApi?): Boolean {
        return clang_CXXRecordDecl_is_derived_from(ptr, Base?.ptr)
    }
    inline fun isVirtuallyDerivedFrom(Base: CXXRecordDeclApi?): Boolean {
        return clang_CXXRecordDecl_is_virtually_derived_from(ptr, Base?.ptr)
    }
    inline fun isProvablyNotDerivedFrom(Base: CXXRecordDeclApi?): Boolean {
        return clang_CXXRecordDecl_is_provably_not_derived_from(ptr, Base?.ptr)
    }
    inline fun viewInheritance(Context: ASTContext): Unit {
        return clang_CXXRecordDecl_view_inheritance(ptr, Context.ptr)
    }
    inline fun finishedDefaultedOrDeletedMember(MD: CXXMethodDeclApi?): Unit {
        return clang_CXXRecordDecl_finished_defaulted_or_deleted_member(ptr, MD?.ptr)
    }
    inline fun setTrivialForCallFlags(MD: CXXMethodDeclApi?): Unit {
        return clang_CXXRecordDecl_set_trivial_for_call_flags(ptr, MD?.ptr)
    }
    override fun completeDefinition(): Unit {
        return clang_CXXRecordDecl_complete_definition(ptr)
    }
    inline fun mayBeAbstract(): Boolean {
        return clang_CXXRecordDecl_may_be_abstract(ptr)
    }
    inline fun isEffectivelyFinal(): Boolean {
        return clang_CXXRecordDecl_is_effectively_final(ptr)
    }
    inline fun getLambdaManglingNumber(): UInt {
        return clang_CXXRecordDecl_get_lambda_mangling_number(ptr)
    }
    inline fun hasKnownLambdaInternalLinkage(): Boolean {
        return clang_CXXRecordDecl_has_known_lambda_internal_linkage(ptr)
    }
    inline fun getLambdaContextDecl(): DeclApi? {
        return Decl((clang_CXXRecordDecl_get_lambda_context_decl(ptr) ?: return null), memScope)
    }
    inline fun getLambdaIndexInContext(): UInt {
        return clang_CXXRecordDecl_get_lambda_index_in_context(ptr)
    }
    inline fun getDeviceLambdaManglingNumber(): UInt {
        return clang_CXXRecordDecl_get_device_lambda_mangling_number(ptr)
    }
    inline fun getMSInheritanceModel(): MSInheritanceModel {
        return MSInheritanceModelCompanionfromValue(clang_CXXRecordDecl_get_ms_inheritance_model(ptr))
    }
    inline fun calculateInheritanceModel(): MSInheritanceModel {
        return MSInheritanceModelCompanionfromValue(clang_CXXRecordDecl_calculate_inheritance_model(ptr))
    }
    inline fun nullFieldOffsetIsZero(): Boolean {
        return clang_CXXRecordDecl_null_field_offset_is_zero(ptr)
    }
    inline fun isDependentLambda(): Boolean {
        return clang_CXXRecordDecl_is_dependent_lambda(ptr)
    }
    inline fun isNeverDependentLambda(): Boolean {
        return clang_CXXRecordDecl_is_never_dependent_lambda(ptr)
    }
    inline fun getLambdaDependencyKind(): UInt {
        return clang_CXXRecordDecl_get_lambda_dependency_kind(ptr)
    }
    inline fun setLambdaDependencyKind(Kind: UInt): Unit {
        return clang_CXXRecordDecl_set_lambda_dependency_kind(ptr, Kind)
    }
    inline fun setLambdaIsGeneric(IsGeneric: Boolean): Unit {
        return clang_CXXRecordDecl_set_lambda_is_generic(ptr, IsGeneric)
    }
    inline fun isInjectedClassName(): Boolean {
        return clang_CXXRecordDecl_is_injected_class_name(ptr)
    }
    inline fun hasInjectedClassType(): Boolean {
        return clang_CXXRecordDecl_has_injected_class_type(ptr)
    }
    inline fun isInterfaceLike(): Boolean {
        return clang_CXXRecordDecl_is_interface_like(ptr)
    }
    inline fun markAbstract(): Unit {
        return clang_CXXRecordDecl_mark_abstract(ptr)
    }
    inline fun hasFlexibleArrayMember(): Boolean {
        return clang_RecordDecl_has_flexible_array_member(ptr)
    }
    inline fun setHasFlexibleArrayMember(V: Boolean): Unit {
        return clang_RecordDecl_set_has_flexible_array_member(ptr, V)
    }
    inline fun isAnonymousStructOrUnion(): Boolean {
        return clang_RecordDecl_is_anonymous_struct_or_union(ptr)
    }
    inline fun setAnonymousStructOrUnion(Anon: Boolean): Unit {
        return clang_RecordDecl_set_anonymous_struct_or_union(ptr, Anon)
    }
    inline fun hasObjectMember(): Boolean {
        return clang_RecordDecl_has_object_member(ptr)
    }
    inline fun setHasObjectMember(`val`: Boolean): Unit {
        return clang_RecordDecl_set_has_object_member(ptr, `val`)
    }
    inline fun hasVolatileMember(): Boolean {
        return clang_RecordDecl_has_volatile_member(ptr)
    }
    inline fun setHasVolatileMember(`val`: Boolean): Unit {
        return clang_RecordDecl_set_has_volatile_member(ptr, `val`)
    }
    inline fun hasLoadedFieldsFromExternalStorage(): Boolean {
        return clang_RecordDecl_has_loaded_fields_from_external_storage(ptr)
    }
    inline fun setHasLoadedFieldsFromExternalStorage(`val`: Boolean): Unit {
        return clang_RecordDecl_set_has_loaded_fields_from_external_storage(ptr, `val`)
    }
    inline fun isNonTrivialToPrimitiveDefaultInitialize(): Boolean {
        return clang_RecordDecl_is_non_trivial_to_primitive_default_initialize(ptr)
    }
    inline fun setNonTrivialToPrimitiveDefaultInitialize(V: Boolean): Unit {
        return clang_RecordDecl_set_non_trivial_to_primitive_default_initialize(ptr, V)
    }
    inline fun isNonTrivialToPrimitiveCopy(): Boolean {
        return clang_RecordDecl_is_non_trivial_to_primitive_copy(ptr)
    }
    inline fun setNonTrivialToPrimitiveCopy(V: Boolean): Unit {
        return clang_RecordDecl_set_non_trivial_to_primitive_copy(ptr, V)
    }
    inline fun isNonTrivialToPrimitiveDestroy(): Boolean {
        return clang_RecordDecl_is_non_trivial_to_primitive_destroy(ptr)
    }
    inline fun setNonTrivialToPrimitiveDestroy(V: Boolean): Unit {
        return clang_RecordDecl_set_non_trivial_to_primitive_destroy(ptr, V)
    }
    inline fun hasNonTrivialToPrimitiveDefaultInitializeCUnion(): Boolean {
        return clang_RecordDecl_has_non_trivial_to_primitive_default_initialize_c_union(ptr)
    }
    inline fun setHasNonTrivialToPrimitiveDefaultInitializeCUnion(V: Boolean): Unit {
        return clang_RecordDecl_set_has_non_trivial_to_primitive_default_initialize_c_union(ptr, V)
    }
    inline fun hasNonTrivialToPrimitiveDestructCUnion(): Boolean {
        return clang_RecordDecl_has_non_trivial_to_primitive_destruct_c_union(ptr)
    }
    inline fun setHasNonTrivialToPrimitiveDestructCUnion(V: Boolean): Unit {
        return clang_RecordDecl_set_has_non_trivial_to_primitive_destruct_c_union(ptr, V)
    }
    inline fun hasNonTrivialToPrimitiveCopyCUnion(): Boolean {
        return clang_RecordDecl_has_non_trivial_to_primitive_copy_c_union(ptr)
    }
    inline fun setHasNonTrivialToPrimitiveCopyCUnion(V: Boolean): Unit {
        return clang_RecordDecl_set_has_non_trivial_to_primitive_copy_c_union(ptr, V)
    }
    inline fun hasUninitializedExplicitInitFields(): Boolean {
        return clang_RecordDecl_has_uninitialized_explicit_init_fields(ptr)
    }
    inline fun setHasUninitializedExplicitInitFields(V: Boolean): Unit {
        return clang_RecordDecl_set_has_uninitialized_explicit_init_fields(ptr, V)
    }
    inline fun canPassInRegisters(): Boolean {
        return clang_RecordDecl_can_pass_in_registers(ptr)
    }
    inline fun getArgPassingRestrictions(): RecordArgPassingKind {
        return RecordArgPassingKindCompanionfromValue(clang_RecordDecl_get_arg_passing_restrictions(ptr))
    }
    inline fun setArgPassingRestrictions(Kind: RecordArgPassingKind): Unit {
        return clang_RecordDecl_set_arg_passing_restrictions(ptr, Kind.value)
    }
    inline fun isParamDestroyedInCallee(): Boolean {
        return clang_RecordDecl_is_param_destroyed_in_callee(ptr)
    }
    inline fun setParamDestroyedInCallee(V: Boolean): Unit {
        return clang_RecordDecl_set_param_destroyed_in_callee(ptr, V)
    }
    inline fun isRandomized(): Boolean {
        return clang_RecordDecl_is_randomized(ptr)
    }
    inline fun setIsRandomized(V: Boolean): Unit {
        return clang_RecordDecl_set_is_randomized(ptr, V)
    }
    inline fun isCapturedRecord(): Boolean {
        return clang_RecordDecl_is_captured_record(ptr)
    }
    inline fun setCapturedRecord(): Unit {
        return clang_RecordDecl_set_captured_record(ptr)
    }
    inline fun isOrContainsUnion(): Boolean {
        return clang_RecordDecl_is_or_contains_union(ptr)
    }
    inline fun field_empty(): Boolean {
        return clang_RecordDecl_field_empty(ptr)
    }
    inline fun getNumFields(): UInt {
        return clang_RecordDecl_get_num_fields(ptr)
    }
    inline fun noload_field_empty(): Boolean {
        return clang_RecordDecl_noload_field_empty(ptr)
    }
    inline fun isMsStruct(C: ASTContext): Boolean {
        return clang_RecordDecl_is_ms_struct(ptr, C.ptr)
    }
    inline fun mayInsertExtraPadding(EmitRemark: Boolean = false): Boolean {
        return clang_RecordDecl_may_insert_extra_padding(ptr, EmitRemark)
    }
    inline fun findFirstNamedDataMember(): FieldDecl? {
        return FieldDecl((clang_RecordDecl_find_first_named_data_member(ptr) ?: return null), memScope)
    }
    inline fun isThisDeclarationADefinition(): Boolean {
        return clang_TagDecl_is_this_declaration_a_definition(ptr)
    }
    inline fun isCompleteDefinition(): Boolean {
        return clang_TagDecl_is_complete_definition(ptr)
    }
    inline fun setCompleteDefinition(V: Boolean = true): Unit {
        return clang_TagDecl_set_complete_definition(ptr, V)
    }
    inline fun isCompleteDefinitionRequired(): Boolean {
        return clang_TagDecl_is_complete_definition_required(ptr)
    }
    inline fun setCompleteDefinitionRequired(V: Boolean = true): Unit {
        return clang_TagDecl_set_complete_definition_required(ptr, V)
    }
    inline fun isBeingDefined(): Boolean {
        return clang_TagDecl_is_being_defined(ptr)
    }
    inline fun isEmbeddedInDeclarator(): Boolean {
        return clang_TagDecl_is_embedded_in_declarator(ptr)
    }
    inline fun setEmbeddedInDeclarator(isInDeclarator: Boolean): Unit {
        return clang_TagDecl_set_embedded_in_declarator(ptr, isInDeclarator)
    }
    inline fun isFreeStanding(): Boolean {
        return clang_TagDecl_is_free_standing(ptr)
    }
    inline fun setFreeStanding(_isFreeStanding: Boolean = true): Unit {
        return clang_TagDecl_set_free_standing(ptr, _isFreeStanding)
    }
    inline fun isDependentType(): Boolean {
        return clang_TagDecl_is_dependent_type(ptr)
    }
    inline fun isThisDeclarationADemotedDefinition(): Boolean {
        return clang_TagDecl_is_this_declaration_a_demoted_definition(ptr)
    }
    inline fun demoteThisDefinitionToDeclaration(): Unit {
        return clang_TagDecl_demote_this_definition_to_declaration(ptr)
    }
    inline fun startDefinition(): Unit {
        return clang_TagDecl_start_definition(ptr)
    }
    inline fun isEntityBeingDefined(): Boolean {
        return clang_TagDecl_is_entity_being_defined(ptr)
    }
    inline fun getTagKind(): TagTypeKind {
        return TagTypeKindCompanionfromValue(clang_TagDecl_get_tag_kind(ptr))
    }
    inline fun setTagKind(TK: TagTypeKind): Unit {
        return clang_TagDecl_set_tag_kind(ptr, TK.value)
    }
    inline fun isStruct(): Boolean {
        return clang_TagDecl_is_struct(ptr)
    }
    inline fun isInterface(): Boolean {
        return clang_TagDecl_is_interface(ptr)
    }
    inline fun isClass(): Boolean {
        return clang_TagDecl_is_class(ptr)
    }
    inline fun isUnion(): Boolean {
        return clang_TagDecl_is_union(ptr)
    }
    inline fun isEnum(): Boolean {
        return clang_TagDecl_is_enum(ptr)
    }
    inline fun isStructureOrClass(): Boolean {
        return clang_TagDecl_is_structure_or_class(ptr)
    }
    inline fun hasNameForLinkage(): Boolean {
        return clang_TagDecl_has_name_for_linkage(ptr)
    }
    inline fun getTypedefNameForAnonDecl(): TypedefNameDecl? {
        return TypedefNameDecl((clang_TagDecl_get_typedef_name_for_anon_decl(ptr) ?: return null), memScope)
    }
    inline fun setTypedefNameForAnonDecl(TDD: TypedefNameDecl?): Unit {
        return clang_TagDecl_set_typedef_name_for_anon_decl(ptr, TDD?.ptr)
    }
    inline fun getNumTemplateParameterLists(): UInt {
        return clang_TagDecl_get_num_template_parameter_lists(ptr)
    }
    inline fun getTemplateParameterList(i: UInt): TemplateParameterList? {
        return TemplateParameterList((clang_TagDecl_get_template_parameter_list(ptr, i) ?: return null), memScope)
    }
    inline fun getKindName(): String? {
        val str: CPointer<ByteVar>? = clang_TagDecl_get_kind_name(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getTypeForDecl(): TypeApi? {
        return Type((clang_TypeDecl_get_type_for_decl(ptr) ?: return null), memScope)
    }
    inline fun setTypeForDecl(TD: TypeApi?): Unit {
        return clang_TypeDecl_set_type_for_decl(ptr, TD?.ptr)
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
    override fun isOutOfLine(): Boolean {
        return clang_Decl_is_out_of_line(ptr)
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
    inline fun isParameterPack(): Boolean {
        return clang_Decl_is_parameter_pack(ptr)
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
    inline fun classTemplateSpecializationDeclGetMostRecentDecl(): ClassTemplateSpecializationDecl? {
        return ClassTemplateSpecializationDecl((clang_ClassTemplateSpecializationDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun classTemplateSpecializationDeclGetDefinitionOrSelf(): ClassTemplateSpecializationDecl? {
        return ClassTemplateSpecializationDecl((clang_ClassTemplateSpecializationDecl_get_definition_or_self(ptr) ?: return null), memScope)
    }
    inline fun getSpecializedTemplate(): ClassTemplateDecl? {
        return ClassTemplateDecl((clang_ClassTemplateSpecializationDecl_get_specialized_template(ptr) ?: return null), memScope)
    }
    inline fun getTemplateArgs(): TemplateArgumentList? {
        return TemplateArgumentList((clang_ClassTemplateSpecializationDecl_get_template_args(ptr) ?: return null), memScope)
    }
    inline fun setTemplateArgs(Args: TemplateArgumentList?): Unit {
        return clang_ClassTemplateSpecializationDecl_set_template_args(ptr, Args?.ptr)
    }
    inline fun getSpecializationKind(): TemplateSpecializationKind {
        return CompanionfromValue(clang_ClassTemplateSpecializationDecl_get_specialization_kind(ptr))
    }
    inline fun isExplicitSpecialization(): Boolean {
        return clang_ClassTemplateSpecializationDecl_is_explicit_specialization(ptr)
    }
    inline fun isClassScopeExplicitSpecialization(): Boolean {
        return clang_ClassTemplateSpecializationDecl_is_class_scope_explicit_specialization(ptr)
    }
    inline fun isExplicitInstantiationOrSpecialization(): Boolean {
        return clang_ClassTemplateSpecializationDecl_is_explicit_instantiation_or_specialization(ptr)
    }
    inline fun setSpecializedTemplate(Specialized: ClassTemplateDecl?): Unit {
        return clang_ClassTemplateSpecializationDecl_set_specialized_template(ptr, Specialized?.ptr)
    }
    inline fun setSpecializationKind(TSK: TemplateSpecializationKind): Unit {
        return clang_ClassTemplateSpecializationDecl_set_specialization_kind(ptr, TSK.value)
    }
    inline fun hasStrictPackMatch(): Boolean {
        return clang_ClassTemplateSpecializationDecl_has_strict_pack_match(ptr)
    }
    inline fun setStrictPackMatch(Val: Boolean): Unit {
        return clang_ClassTemplateSpecializationDecl_set_strict_pack_match(ptr, Val)
    }
    inline fun getTemplateInstantiationArgs(): TemplateArgumentList? {
        return TemplateArgumentList((clang_ClassTemplateSpecializationDecl_get_template_instantiation_args(ptr) ?: return null), memScope)
    }
    inline fun setInstantiationOf(TemplDecl: ClassTemplateDecl?): Unit {
        return clang_ClassTemplateSpecializationDecl_set_instantiation_of__clang_ClassTemplateDecl_P(ptr, TemplDecl?.ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ClassTemplateSpecializationDecl_size_of()
            }

        val align: Int
            inline get() {
                return clang_ClassTemplateSpecializationDecl_align_of()
            }

        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_ClassTemplateSpecializationDecl_classof(D?.ptr)
        }
        inline fun MemScope.classofKind(K: Kind): Boolean {
            return clang_ClassTemplateSpecializationDecl_classof_kind(K.value)
        }
        fun MemScope.ClassTemplateSpecializationDecl_Holder(): ClassTemplateSpecializationDecl {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return ClassTemplateSpecializationDecl(memory, this)
        }
    }
    inline fun asCXXRecordDecl(): CXXRecordDecl {
        return CXXRecordDecl(clang_ClassTemplateSpecializationDecl_as_clang_CXXRecordDecl(ptr)!!, memScope)
    }
    inline fun asRecordDecl(): RecordDecl {
        return RecordDecl(clang_ClassTemplateSpecializationDecl_as_clang_RecordDecl(ptr)!!, memScope)
    }
    inline fun asTagDecl(): TagDecl {
        return TagDecl(clang_ClassTemplateSpecializationDecl_as_clang_TagDecl(ptr)!!, memScope)
    }
    inline fun asTypeDecl(): TypeDecl {
        return TypeDecl(clang_ClassTemplateSpecializationDecl_as_clang_TypeDecl(ptr)!!, memScope)
    }
    inline fun asDeclContext(): DeclContext {
        return DeclContext(clang_ClassTemplateSpecializationDecl_as_clang_DeclContext(ptr)!!, memScope)
    }
    inline fun asNamedDecl(): NamedDecl {
        return NamedDecl(clang_ClassTemplateSpecializationDecl_as_clang_NamedDecl(ptr)!!, memScope)
    }
    inline fun asDecl(): Decl {
        return Decl(clang_ClassTemplateSpecializationDecl_as_clang_Decl(ptr)!!, memScope)
    }
}

// END KRAPPER GEN for clang::ClassTemplateSpecializationDecl


