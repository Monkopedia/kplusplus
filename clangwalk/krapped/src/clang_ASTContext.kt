package clang

import clang.CallingConv.Companion.fromValue as CallingConvCompanionfromValue
import clang.GVALinkage.Companion.fromValue as GVALinkageCompanionfromValue
import clang.LangAS.Companion.fromValue as CompanionfromValue
import clang.QualType.Companion.QualType_Holder
import clang.TranslationUnitKind.Companion.fromValue as TranslationUnitKindCompanionfromValue
import clang.aSTContext.GetBuiltinTypeError
import clang.attr.Kind as AttrKind
import clang.decl.ObjCDeclQualifier
import clang.qualifiers.GC
import clang.qualifiers.GC.Companion.fromValue as GCCompanionfromValue
import clang.qualifiers.ObjCLifetime
import clang.qualifiers.ObjCLifetime.Companion.fromValue as ObjCLifetimeCompanionfromValue
import clang.targetCXXABI.Kind
import clang.targetCXXABI.Kind.Companion.fromValue
import clang.unaryTransformType.UTTKind
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.UByte
import kotlin.UInt
import kotlin.ULong
import kotlin.UShort
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.clangwalk.internal.clang_ASTContext_AutoDeductTy_get
import krapper.clangwalk.internal.clang_ASTContext_AutoDeductTy_set
import krapper.clangwalk.internal.clang_ASTContext_AutoRRefDeductTy_get
import krapper.clangwalk.internal.clang_ASTContext_AutoRRefDeductTy_set
import krapper.clangwalk.internal.clang_ASTContext_CommentsLoaded_get
import krapper.clangwalk.internal.clang_ASTContext_CommentsLoaded_set
import krapper.clangwalk.internal.clang_ASTContext_MSGuidTagDecl_get
import krapper.clangwalk.internal.clang_ASTContext_MSGuidTagDecl_set
import krapper.clangwalk.internal.clang_ASTContext_MSTypeInfoTagDecl_get
import krapper.clangwalk.internal.clang_ASTContext_MSTypeInfoTagDecl_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyAssignmentOperators_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyAssignmentOperators_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyConstructorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyConstructorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyConstructors_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitCopyConstructors_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDefaultConstructorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDefaultConstructorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDefaultConstructors_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDefaultConstructors_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDestructorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDestructorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDestructors_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitDestructors_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveAssignmentOperators_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveAssignmentOperators_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveConstructorsDeclared_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveConstructorsDeclared_set
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveConstructors_get
import krapper.clangwalk.internal.clang_ASTContext_NumImplicitMoveConstructors_set
import krapper.clangwalk.internal.clang_ASTContext_TUKind_get
import krapper.clangwalk.internal.clang_ASTContext_VaListTagDecl_get
import krapper.clangwalk.internal.clang_ASTContext_VaListTagDecl_set
import krapper.clangwalk.internal.clang_ASTContext_add_declarator_for_unnamed_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_add_overridden_method
import krapper.clangwalk.internal.clang_ASTContext_add_translation_unit_decl
import krapper.clangwalk.internal.clang_ASTContext_address_space_map_mangling_for
import krapper.clangwalk.internal.clang_ASTContext_adjust_deduced_function_result_type
import krapper.clangwalk.internal.clang_ASTContext_adjust_function_result_type
import krapper.clangwalk.internal.clang_ASTContext_adjust_string_literal_base_type
import krapper.clangwalk.internal.clang_ASTContext_align_of
import krapper.clangwalk.internal.clang_ASTContext_allocate
import krapper.clangwalk.internal.clang_ASTContext_any_obj_c_implementation
import krapper.clangwalk.internal.clang_ASTContext_are_comparable_obj_c_pointer_types
import krapper.clangwalk.internal.clang_ASTContext_are_compatible_rvv_types
import krapper.clangwalk.internal.clang_ASTContext_are_compatible_vector_types
import krapper.clangwalk.internal.clang_ASTContext_are_lax_compatible_rvv_types
import krapper.clangwalk.internal.clang_ASTContext_backup_str
import krapper.clangwalk.internal.clang_ASTContext_base_for_v_table_authentication
import krapper.clangwalk.internal.clang_ASTContext_build_implicit_record
import krapper.clangwalk.internal.clang_ASTContext_can_bind_obj_c_object_type
import krapper.clangwalk.internal.clang_ASTContext_can_builtin_be_redeclared
import krapper.clangwalk.internal.clang_ASTContext_class_needs_vector_deleting_destructor
import krapper.clangwalk.internal.clang_ASTContext_cleanup
import krapper.clangwalk.internal.clang_ASTContext_compute_best_enum_types
import krapper.clangwalk.internal.clang_ASTContext_contains_address_discriminated_pointer_auth
import krapper.clangwalk.internal.clang_ASTContext_contains_non_relocatable_pointer_auth
import krapper.clangwalk.internal.clang_ASTContext_deallocate
import krapper.clangwalk.internal.clang_ASTContext_decl_must_be_emitted
import krapper.clangwalk.internal.clang_ASTContext_decode_type_str
import krapper.clangwalk.internal.clang_ASTContext_deduplicate_merged_definitions_for
import krapper.clangwalk.internal.clang_ASTContext_defaults_to_ms_struct
import krapper.clangwalk.internal.clang_ASTContext_dispose
import krapper.clangwalk.internal.clang_ASTContext_erase_decl_attrs
import krapper.clangwalk.internal.clang_ASTContext_get_addr_space_qual_type
import krapper.clangwalk.internal.clang_ASTContext_get_adjusted_parameter_type
import krapper.clangwalk.internal.clang_ASTContext_get_adjusted_type
import krapper.clangwalk.internal.clang_ASTContext_get_array_decayed_type
import krapper.clangwalk.internal.clang_ASTContext_get_array_parameter_type
import krapper.clangwalk.internal.clang_ASTContext_get_ast_allocated_memory
import krapper.clangwalk.internal.clang_ASTContext_get_atomic_type
import krapper.clangwalk.internal.clang_ASTContext_get_attributed_type
import krapper.clangwalk.internal.clang_ASTContext_get_attributed_type__clang_NullabilityKind_clang_QualType_clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_get_auto_deduct_type
import krapper.clangwalk.internal.clang_ASTContext_get_auto_r_ref_deduct_type
import krapper.clangwalk.internal.clang_ASTContext_get_auto_type
import krapper.clangwalk.internal.clang_ASTContext_get_base_element_type__clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_get_bit_int_type
import krapper.clangwalk.internal.clang_ASTContext_get_block_descriptor_extended_type
import krapper.clangwalk.internal.clang_ASTContext_get_block_descriptor_type
import krapper.clangwalk.internal.clang_ASTContext_get_block_pointer_type
import krapper.clangwalk.internal.clang_ASTContext_get_bool_type
import krapper.clangwalk.internal.clang_ASTContext_get_builtin_ms_va_list_type
import krapper.clangwalk.internal.clang_ASTContext_get_builtin_type
import krapper.clangwalk.internal.clang_ASTContext_get_builtin_va_list_type
import krapper.clangwalk.internal.clang_ASTContext_get_byref_lifetime
import krapper.clangwalk.internal.clang_ASTContext_get_cf_constant_string_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_get_cf_constant_string_type
import krapper.clangwalk.internal.clang_ASTContext_get_char_width
import krapper.clangwalk.internal.clang_ASTContext_get_common_sugared_type
import krapper.clangwalk.internal.clang_ASTContext_get_complex_type
import krapper.clangwalk.internal.clang_ASTContext_get_const_type
import krapper.clangwalk.internal.clang_ASTContext_get_constant_matrix_type
import krapper.clangwalk.internal.clang_ASTContext_get_corresponding_saturated_type
import krapper.clangwalk.internal.clang_ASTContext_get_corresponding_signed_fixed_point_type
import krapper.clangwalk.internal.clang_ASTContext_get_corresponding_signed_type
import krapper.clangwalk.internal.clang_ASTContext_get_corresponding_unsaturated_type
import krapper.clangwalk.internal.clang_ASTContext_get_corresponding_unsigned_type
import krapper.clangwalk.internal.clang_ASTContext_get_cuid_hash
import krapper.clangwalk.internal.clang_ASTContext_get_current_key_function
import krapper.clangwalk.internal.clang_ASTContext_get_cvr_qualified_type
import krapper.clangwalk.internal.clang_ASTContext_get_cxxabi_kind
import krapper.clangwalk.internal.clang_ASTContext_get_decayed_type
import krapper.clangwalk.internal.clang_ASTContext_get_decayed_type__clang_QualType_clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_get_declarator_for_unnamed_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_get_default_calling_convention
import krapper.clangwalk.internal.clang_ASTContext_get_default_open_cl_pointee_addr_space
import krapper.clangwalk.internal.clang_ASTContext_get_exception_object_type
import krapper.clangwalk.internal.clang_ASTContext_get_ext_vector_type
import krapper.clangwalk.internal.clang_ASTContext_get_field_offset
import krapper.clangwalk.internal.clang_ASTContext_get_file_type
import krapper.clangwalk.internal.clang_ASTContext_get_fixed_point_i_bits
import krapper.clangwalk.internal.clang_ASTContext_get_fixed_point_scale
import krapper.clangwalk.internal.clang_ASTContext_get_floating_type_order
import krapper.clangwalk.internal.clang_ASTContext_get_floating_type_semantic_order
import krapper.clangwalk.internal.clang_ASTContext_get_function_no_proto_type__clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_get_function_type_without_param_ab_is
import krapper.clangwalk.internal.clang_ASTContext_get_function_type_without_ptr_sizes
import krapper.clangwalk.internal.clang_ASTContext_get_gva_linkage_for_function
import krapper.clangwalk.internal.clang_ASTContext_get_higher_precision_fp_type
import krapper.clangwalk.internal.clang_ASTContext_get_incomplete_array_type
import krapper.clangwalk.internal.clang_ASTContext_get_inner_obj_c_ownership
import krapper.clangwalk.internal.clang_ASTContext_get_instantiated_from_unnamed_field_decl
import krapper.clangwalk.internal.clang_ASTContext_get_instantiated_from_using_decl
import krapper.clangwalk.internal.clang_ASTContext_get_int_ptr_type
import krapper.clangwalk.internal.clang_ASTContext_get_int_type_for_bitwidth
import krapper.clangwalk.internal.clang_ASTContext_get_int_width
import krapper.clangwalk.internal.clang_ASTContext_get_integer_type_order
import krapper.clangwalk.internal.clang_ASTContext_get_l_value_reference_type
import krapper.clangwalk.internal.clang_ASTContext_get_lang_as_for_builtin_address_space
import krapper.clangwalk.internal.clang_ASTContext_get_legacy_integral_type_encoding
import krapper.clangwalk.internal.clang_ASTContext_get_lifetime_qualified_type
import krapper.clangwalk.internal.clang_ASTContext_get_mangling_number
import krapper.clangwalk.internal.clang_ASTContext_get_ms_guid_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_get_ms_type_info_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_get_next_string_literal_version
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_class_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_class_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_constant_string_interface
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_encoding_for_function_decl
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_encoding_for_method_parameter
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_encoding_for_property_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_encoding_for_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_encoding_for_type_qualifier
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_id_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_id_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_instance_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_object_pointer_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_proto_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_sel_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_sel_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_c_super_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_cgc_attr_kind
import krapper.clangwalk.internal.clang_ASTContext_get_obj_cgc_qual_type
import krapper.clangwalk.internal.clang_ASTContext_get_obj_cns_string_type
import krapper.clangwalk.internal.clang_ASTContext_get_open_mp_default_simd_align
import krapper.clangwalk.internal.clang_ASTContext_get_paren_type
import krapper.clangwalk.internal.clang_ASTContext_get_pointer_auth_type_discriminator
import krapper.clangwalk.internal.clang_ASTContext_get_pointer_auth_v_table_pointer_discriminator
import krapper.clangwalk.internal.clang_ASTContext_get_pointer_diff_type
import krapper.clangwalk.internal.clang_ASTContext_get_pointer_type
import krapper.clangwalk.internal.clang_ASTContext_get_preferred_type_align
import krapper.clangwalk.internal.clang_ASTContext_get_primary_merged_decl
import krapper.clangwalk.internal.clang_ASTContext_get_process_id_type
import krapper.clangwalk.internal.clang_ASTContext_get_promoted_integer_type
import krapper.clangwalk.internal.clang_ASTContext_get_r_value_reference_type
import krapper.clangwalk.internal.clang_ASTContext_get_raw_cf_constant_string_type
import krapper.clangwalk.internal.clang_ASTContext_get_read_pipe_type
import krapper.clangwalk.internal.clang_ASTContext_get_real_type_for_bitwidth
import krapper.clangwalk.internal.clang_ASTContext_get_restrict_type
import krapper.clangwalk.internal.clang_ASTContext_get_scalable_vector_type
import krapper.clangwalk.internal.clang_ASTContext_get_side_table_allocated_memory
import krapper.clangwalk.internal.clang_ASTContext_get_signature_parameter_type
import krapper.clangwalk.internal.clang_ASTContext_get_signed_size_type
import krapper.clangwalk.internal.clang_ASTContext_get_signed_w_char_type
import krapper.clangwalk.internal.clang_ASTContext_get_size_type
import krapper.clangwalk.internal.clang_ASTContext_get_string_literal_array_type
import krapper.clangwalk.internal.clang_ASTContext_get_target_address_space
import krapper.clangwalk.internal.clang_ASTContext_get_target_default_align_for_attribute_aligned
import krapper.clangwalk.internal.clang_ASTContext_get_target_null_pointer_value
import krapper.clangwalk.internal.clang_ASTContext_get_template_type_parm_type
import krapper.clangwalk.internal.clang_ASTContext_get_translation_unit_decl
import krapper.clangwalk.internal.clang_ASTContext_get_type_align
import krapper.clangwalk.internal.clang_ASTContext_get_type_align_if_known
import krapper.clangwalk.internal.clang_ASTContext_get_type_decl_type__const_clang_TypeDecl_P
import krapper.clangwalk.internal.clang_ASTContext_get_type_of_type
import krapper.clangwalk.internal.clang_ASTContext_get_type_size
import krapper.clangwalk.internal.clang_ASTContext_get_type_unadjusted_align
import krapper.clangwalk.internal.clang_ASTContext_get_u_int_ptr_type
import krapper.clangwalk.internal.clang_ASTContext_get_unary_transform_type
import krapper.clangwalk.internal.clang_ASTContext_get_unconstrained_type
import krapper.clangwalk.internal.clang_ASTContext_get_unqualified_array_type__clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_get_unqualified_obj_c_pointer_type
import krapper.clangwalk.internal.clang_ASTContext_get_unsigned_pointer_diff_type
import krapper.clangwalk.internal.clang_ASTContext_get_unsigned_w_char_type
import krapper.clangwalk.internal.clang_ASTContext_get_va_list_tag_decl
import krapper.clangwalk.internal.clang_ASTContext_get_variable_array_decayed_type
import krapper.clangwalk.internal.clang_ASTContext_get_vector_type
import krapper.clangwalk.internal.clang_ASTContext_get_volatile_type
import krapper.clangwalk.internal.clang_ASTContext_get_w_char_type
import krapper.clangwalk.internal.clang_ASTContext_get_w_int_type
import krapper.clangwalk.internal.clang_ASTContext_get_web_assembly_externref_type
import krapper.clangwalk.internal.clang_ASTContext_get_wide_char_type
import krapper.clangwalk.internal.clang_ASTContext_get_write_pipe_type
import krapper.clangwalk.internal.clang_ASTContext_getcuda_configure_call_decl
import krapper.clangwalk.internal.clang_ASTContext_getcuda_get_parameter_buffer_decl
import krapper.clangwalk.internal.clang_ASTContext_getcuda_launch_device_decl
import krapper.clangwalk.internal.clang_ASTContext_getjmp_buf_type
import krapper.clangwalk.internal.clang_ASTContext_getsigjmp_buf_type
import krapper.clangwalk.internal.clang_ASTContext_getucontext_t_type
import krapper.clangwalk.internal.clang_ASTContext_has_any_function_effects
import krapper.clangwalk.internal.clang_ASTContext_has_cvr_similar_type
import krapper.clangwalk.internal.clang_ASTContext_has_direct_ownership_qualifier
import krapper.clangwalk.internal.clang_ASTContext_has_same_function_type_ignoring_exception_spec
import krapper.clangwalk.internal.clang_ASTContext_has_same_function_type_ignoring_param_abi
import krapper.clangwalk.internal.clang_ASTContext_has_same_function_type_ignoring_ptr_sizes
import krapper.clangwalk.internal.clang_ASTContext_has_same_nullability_type_qualifier
import krapper.clangwalk.internal.clang_ASTContext_has_same_type
import krapper.clangwalk.internal.clang_ASTContext_has_same_unqualified_type
import krapper.clangwalk.internal.clang_ASTContext_has_seen_type_aware_operator_new_or_delete
import krapper.clangwalk.internal.clang_ASTContext_has_similar_type
import krapper.clangwalk.internal.clang_ASTContext_has_unique_object_representations
import krapper.clangwalk.internal.clang_ASTContext_is_alignment_required__clang_QualType
import krapper.clangwalk.internal.clang_ASTContext_is_dependence_allowed
import krapper.clangwalk.internal.clang_ASTContext_is_destroying_operator_delete
import krapper.clangwalk.internal.clang_ASTContext_is_nearly_empty
import krapper.clangwalk.internal.clang_ASTContext_is_obj_c_class_type
import krapper.clangwalk.internal.clang_ASTContext_is_obj_c_id_type
import krapper.clangwalk.internal.clang_ASTContext_is_obj_c_sel_type
import krapper.clangwalk.internal.clang_ASTContext_is_obj_cns_object_type
import krapper.clangwalk.internal.clang_ASTContext_is_promotable_integer_type
import krapper.clangwalk.internal.clang_ASTContext_is_same_default_template_argument
import krapper.clangwalk.internal.clang_ASTContext_is_same_entity
import krapper.clangwalk.internal.clang_ASTContext_is_same_template_parameter
import krapper.clangwalk.internal.clang_ASTContext_is_type_aware_operator_new_or_delete
import krapper.clangwalk.internal.clang_ASTContext_may_externalize
import krapper.clangwalk.internal.clang_ASTContext_merge_function_parameter_types
import krapper.clangwalk.internal.clang_ASTContext_merge_function_types
import krapper.clangwalk.internal.clang_ASTContext_merge_obj_cgc_qualifiers
import krapper.clangwalk.internal.clang_ASTContext_merge_tag_definitions
import krapper.clangwalk.internal.clang_ASTContext_merge_transparent_union_type
import krapper.clangwalk.internal.clang_ASTContext_merge_types
import krapper.clangwalk.internal.clang_ASTContext_overridden_methods
import krapper.clangwalk.internal.clang_ASTContext_overridden_methods_size
import krapper.clangwalk.internal.clang_ASTContext_print_stats
import krapper.clangwalk.internal.clang_ASTContext_property_types_are_compatible
import krapper.clangwalk.internal.clang_ASTContext_register_sycl_entry_point_function
import krapper.clangwalk.internal.clang_ASTContext_remove_addr_space_qual_type
import krapper.clangwalk.internal.clang_ASTContext_remove_ptr_size_addr_space
import krapper.clangwalk.internal.clang_ASTContext_set_cf_constant_string_type
import krapper.clangwalk.internal.clang_ASTContext_set_class_needs_vector_deleting_destructor
import krapper.clangwalk.internal.clang_ASTContext_set_file_decl
import krapper.clangwalk.internal.clang_ASTContext_set_instantiated_from_unnamed_field_decl
import krapper.clangwalk.internal.clang_ASTContext_set_instantiated_from_using_decl
import krapper.clangwalk.internal.clang_ASTContext_set_is_destroying_operator_delete
import krapper.clangwalk.internal.clang_ASTContext_set_is_type_aware_operator_new_or_delete
import krapper.clangwalk.internal.clang_ASTContext_set_mangling_number
import krapper.clangwalk.internal.clang_ASTContext_set_non_key_function
import krapper.clangwalk.internal.clang_ASTContext_set_obj_c_class_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_set_obj_c_id_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_set_obj_c_sel_redefinition_type
import krapper.clangwalk.internal.clang_ASTContext_set_obj_c_super_type
import krapper.clangwalk.internal.clang_ASTContext_set_obj_cns_string_type
import krapper.clangwalk.internal.clang_ASTContext_set_primary_merged_decl
import krapper.clangwalk.internal.clang_ASTContext_set_traversal_scope
import krapper.clangwalk.internal.clang_ASTContext_setcuda_configure_call_decl
import krapper.clangwalk.internal.clang_ASTContext_setcuda_get_parameter_buffer_decl
import krapper.clangwalk.internal.clang_ASTContext_setcuda_launch_device_decl
import krapper.clangwalk.internal.clang_ASTContext_setjmp_buf_decl
import krapper.clangwalk.internal.clang_ASTContext_setsigjmp_buf_decl
import krapper.clangwalk.internal.clang_ASTContext_setucontext_t_decl
import krapper.clangwalk.internal.clang_ASTContext_should_externalize
import krapper.clangwalk.internal.clang_ASTContext_size_of
import krapper.clangwalk.internal.clang_ASTContext_types_are_block_pointer_compatible
import krapper.clangwalk.internal.clang_ASTContext_types_are_compatible
import krapper.clangwalk.internal.clang_ASTContext_unwrap_similar_array_types
import krapper.clangwalk.internal.clang_ASTContext_unwrap_similar_types
import platform.linux.free
import platform.posix.size_t
import std.Vector__CXXMethodDecl_P
import std.Vector__CXXMethodDecl_P.Companion.Vector__CXXMethodDecl_P_Holder
import std.Vector__Decl_P

// BEGIN KRAPPER GEN for clang::ASTContext

@krapper.CppBinding("clang::ASTContext")
class ASTContext(
    val ptr: COpaquePointer,
    val memScope: MemScope,
) {
    inline fun containsAddressDiscriminatedPointerAuth(T: QualType): Boolean {
        return clang_ASTContext_contains_address_discriminated_pointer_auth(ptr, T.ptr)
    }
    inline fun containsNonRelocatablePointerAuth(T: QualType): Boolean {
        return clang_ASTContext_contains_non_relocatable_pointer_auth(ptr, T.ptr)
    }
    inline fun setTraversalScope(_arg_0: Vector__Decl_P): Unit {
        return clang_ASTContext_set_traversal_scope(ptr, _arg_0.ptr)
    }
    inline fun cleanup(): Unit {
        return clang_ASTContext_cleanup(ptr)
    }
    inline fun Allocate(Size: size_t, Align: UInt = 8u): COpaquePointer? {
        return clang_ASTContext_allocate(ptr, Size, Align)
    }
    inline fun Deallocate(Ptr: COpaquePointer?): Unit {
        return clang_ASTContext_deallocate(ptr, Ptr)
    }
    inline fun getASTAllocatedMemory(): size_t {
        return clang_ASTContext_get_ast_allocated_memory(ptr)
    }
    inline fun getSideTableAllocatedMemory(): size_t {
        return clang_ASTContext_get_side_table_allocated_memory(ptr)
    }
    inline fun GetHigherPrecisionFPType(ElementType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_higher_precision_fp_type(ptr, ElementType.ptr, retValue.ptr)
        return retValue
    }
    inline fun getIntTypeForBitwidth(DestWidth: UInt, Signed: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_int_type_for_bitwidth(ptr, DestWidth, Signed, retValue.ptr)
        return retValue
    }
    inline fun getRealTypeForBitwidth(DestWidth: UInt, ExplicitType: Int): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_real_type_for_bitwidth(ptr, DestWidth, ExplicitType, retValue.ptr)
        return retValue
    }
    inline fun isDependenceAllowed(): Boolean {
        return clang_ASTContext_is_dependence_allowed(ptr)
    }
    inline fun getCXXABIKind(): Kind {
        return fromValue(clang_ASTContext_get_cxxabi_kind(ptr))
    }
    inline fun eraseDeclAttrs(D: DeclApi?): Unit {
        return clang_ASTContext_erase_decl_attrs(ptr, D?.ptr)
    }
    inline fun getInstantiatedFromUsingDecl(Inst: NamedDeclApi?): NamedDeclApi? {
        return NamedDecl((clang_ASTContext_get_instantiated_from_using_decl(ptr, Inst?.ptr) ?: return null), memScope)
    }
    inline fun setInstantiatedFromUsingDecl(Inst: NamedDeclApi?, Pattern: NamedDeclApi?): Unit {
        return clang_ASTContext_set_instantiated_from_using_decl(ptr, Inst?.ptr, Pattern?.ptr)
    }
    inline fun getInstantiatedFromUnnamedFieldDecl(Field: FieldDecl?): FieldDecl? {
        return FieldDecl((clang_ASTContext_get_instantiated_from_unnamed_field_decl(ptr, Field?.ptr) ?: return null), memScope)
    }
    inline fun setInstantiatedFromUnnamedFieldDecl(Inst: FieldDecl?, Tmpl: FieldDecl?): Unit {
        return clang_ASTContext_set_instantiated_from_unnamed_field_decl(ptr, Inst?.ptr, Tmpl?.ptr)
    }
    inline fun overridden_methods_size(Method: CXXMethodDecl?): UInt {
        return clang_ASTContext_overridden_methods_size(ptr, Method?.ptr)
    }
    inline fun overridden_methods(Method: CXXMethodDecl?): Vector__CXXMethodDecl_P {
        val retValue: Vector__CXXMethodDecl_P = memScope.Vector__CXXMethodDecl_P_Holder()
        clang_ASTContext_overridden_methods(ptr, Method?.ptr, retValue.ptr)
        return retValue
    }
    inline fun addOverriddenMethod(Method: CXXMethodDecl?, Overridden: CXXMethodDecl?): Unit {
        return clang_ASTContext_add_overridden_method(ptr, Method?.ptr, Overridden?.ptr)
    }
    inline fun getPrimaryMergedDecl(D: DeclApi?): DeclApi? {
        return Decl((clang_ASTContext_get_primary_merged_decl(ptr, D?.ptr) ?: return null), memScope)
    }
    inline fun setPrimaryMergedDecl(D: DeclApi?, Primary: DeclApi?): Unit {
        return clang_ASTContext_set_primary_merged_decl(ptr, D?.ptr, Primary?.ptr)
    }
    inline fun deduplicateMergedDefinitionsFor(ND: NamedDeclApi?): Unit {
        return clang_ASTContext_deduplicate_merged_definitions_for(ptr, ND?.ptr)
    }
    inline fun getTranslationUnitDecl(): TranslationUnitDecl? {
        return TranslationUnitDecl((clang_ASTContext_get_translation_unit_decl(ptr) ?: return null), memScope)
    }
    inline fun addTranslationUnitDecl(): Unit {
        return clang_ASTContext_add_translation_unit_decl(ptr)
    }
    inline fun PrintStats(): Unit {
        return clang_ASTContext_print_stats(ptr)
    }
    inline fun buildImplicitRecord(Name: String?, TK: TagTypeKind): RecordDeclApi? {
        return RecordDecl((clang_ASTContext_build_implicit_record(ptr, Name, TK.value) ?: return null), memScope)
    }
    inline fun getAddrSpaceQualType(T: QualType, AddressSpace: LangAS): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_addr_space_qual_type(ptr, T.ptr, AddressSpace.value, retValue.ptr)
        return retValue
    }
    inline fun removeAddrSpaceQualType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_remove_addr_space_qual_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getPointerAuthVTablePointerDiscriminator(RD: CXXRecordDecl?): UShort {
        return clang_ASTContext_get_pointer_auth_v_table_pointer_discriminator(ptr, RD?.ptr)
    }
    inline fun getPointerAuthTypeDiscriminator(T: QualType): UShort {
        return clang_ASTContext_get_pointer_auth_type_discriminator(ptr, T.ptr)
    }
    inline fun getObjCGCQualType(T: QualType, gcAttr: GC): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_cgc_qual_type(ptr, T.ptr, gcAttr.value, retValue.ptr)
        return retValue
    }
    inline fun removePtrSizeAddrSpace(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_remove_ptr_size_addr_space(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getRestrictType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_restrict_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getVolatileType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_volatile_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getConstType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_const_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun adjustFunctionResultType(FunctionType: QualType, NewResultType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_adjust_function_result_type(ptr, FunctionType.ptr, NewResultType.ptr, retValue.ptr)
        return retValue
    }
    inline fun adjustDeducedFunctionResultType(FD: FunctionDeclApi?, ResultType: QualType): Unit {
        return clang_ASTContext_adjust_deduced_function_result_type(ptr, FD?.ptr, ResultType.ptr)
    }
    inline fun hasSameFunctionTypeIgnoringExceptionSpec(T: QualType, U: QualType): Boolean {
        return clang_ASTContext_has_same_function_type_ignoring_exception_spec(ptr, T.ptr, U.ptr)
    }
    inline fun getFunctionTypeWithoutPtrSizes(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_function_type_without_ptr_sizes(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun hasSameFunctionTypeIgnoringPtrSizes(T: QualType, U: QualType): Boolean {
        return clang_ASTContext_has_same_function_type_ignoring_ptr_sizes(ptr, T.ptr, U.ptr)
    }
    inline fun getFunctionTypeWithoutParamABIs(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_function_type_without_param_ab_is(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun hasSameFunctionTypeIgnoringParamABI(T: QualType, U: QualType): Boolean {
        return clang_ASTContext_has_same_function_type_ignoring_param_abi(ptr, T.ptr, U.ptr)
    }
    inline fun getComplexType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_complex_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getPointerType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_pointer_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getAdjustedType(Orig: QualType, New: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_adjusted_type(ptr, Orig.ptr, New.ptr, retValue.ptr)
        return retValue
    }
    inline fun getDecayedType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_decayed_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getDecayedType__clang_QualType_clang_QualType(Orig: QualType, Decayed: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_decayed_type__clang_QualType_clang_QualType(ptr, Orig.ptr, Decayed.ptr, retValue.ptr)
        return retValue
    }
    inline fun getArrayParameterType(Ty: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_array_parameter_type(ptr, Ty.ptr, retValue.ptr)
        return retValue
    }
    inline fun getAtomicType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_atomic_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getBlockPointerType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_block_pointer_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getBlockDescriptorType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_block_descriptor_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getReadPipeType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_read_pipe_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getWritePipeType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_write_pipe_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getBitIntType(Unsigned: Boolean, NumBits: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_bit_int_type(ptr, Unsigned, NumBits, retValue.ptr)
        return retValue
    }
    inline fun getBlockDescriptorExtendedType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_block_descriptor_extended_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getDefaultOpenCLPointeeAddrSpace(): LangAS {
        return CompanionfromValue(clang_ASTContext_get_default_open_cl_pointee_addr_space(ptr))
    }
    inline fun setcudaConfigureCallDecl(FD: FunctionDeclApi?): Unit {
        return clang_ASTContext_setcuda_configure_call_decl(ptr, FD?.ptr)
    }
    inline fun getcudaConfigureCallDecl(): FunctionDeclApi? {
        return FunctionDecl((clang_ASTContext_getcuda_configure_call_decl(ptr) ?: return null), memScope)
    }
    inline fun setcudaGetParameterBufferDecl(FD: FunctionDeclApi?): Unit {
        return clang_ASTContext_setcuda_get_parameter_buffer_decl(ptr, FD?.ptr)
    }
    inline fun getcudaGetParameterBufferDecl(): FunctionDeclApi? {
        return FunctionDecl((clang_ASTContext_getcuda_get_parameter_buffer_decl(ptr) ?: return null), memScope)
    }
    inline fun setcudaLaunchDeviceDecl(FD: FunctionDeclApi?): Unit {
        return clang_ASTContext_setcuda_launch_device_decl(ptr, FD?.ptr)
    }
    inline fun getcudaLaunchDeviceDecl(): FunctionDeclApi? {
        return FunctionDecl((clang_ASTContext_getcuda_launch_device_decl(ptr) ?: return null), memScope)
    }
    inline fun getByrefLifetime(Ty: QualType, Lifetime: ObjCLifetime, HasByrefExtendedLayout: Boolean): Boolean {
        return clang_ASTContext_get_byref_lifetime(ptr, Ty.ptr, Lifetime.value, HasByrefExtendedLayout)
    }
    inline fun getLValueReferenceType(T: QualType, SpelledAsLValue: Boolean = true): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_l_value_reference_type(ptr, T.ptr, SpelledAsLValue, retValue.ptr)
        return retValue
    }
    inline fun getRValueReferenceType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_r_value_reference_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getIncompleteArrayType(EltTy: QualType, ASM: ArraySizeModifier, IndexTypeQuals: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_incomplete_array_type(ptr, EltTy.ptr, ASM.value, IndexTypeQuals, retValue.ptr)
        return retValue
    }
    inline fun getStringLiteralArrayType(EltTy: QualType, Length: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_string_literal_array_type(ptr, EltTy.ptr, Length, retValue.ptr)
        return retValue
    }
    inline fun getVariableArrayDecayedType(Ty: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_variable_array_decayed_type(ptr, Ty.ptr, retValue.ptr)
        return retValue
    }
    inline fun getScalableVectorType(EltTy: QualType, NumElts: UInt, NumFields: UInt = 1u): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_scalable_vector_type(ptr, EltTy.ptr, NumElts, NumFields, retValue.ptr)
        return retValue
    }
    inline fun getWebAssemblyExternrefType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_web_assembly_externref_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getVectorType(VectorType: QualType, NumElts: UInt, VecKind: VectorKind): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_vector_type(ptr, VectorType.ptr, NumElts, VecKind.value, retValue.ptr)
        return retValue
    }
    inline fun getExtVectorType(VectorType: QualType, NumElts: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_ext_vector_type(ptr, VectorType.ptr, NumElts, retValue.ptr)
        return retValue
    }
    inline fun getConstantMatrixType(ElementType: QualType, NumRows: UInt, NumColumns: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_constant_matrix_type(ptr, ElementType.ptr, NumRows, NumColumns, retValue.ptr)
        return retValue
    }
    inline fun getFunctionNoProtoType(ResultTy: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_function_no_proto_type__clang_QualType(ptr, ResultTy.ptr, retValue.ptr)
        return retValue
    }
    inline fun adjustStringLiteralBaseType(StrLTy: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_adjust_string_literal_base_type(ptr, StrLTy.ptr, retValue.ptr)
        return retValue
    }
    inline fun getTypeDeclType(Decl: TypeDeclApi?): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_type_decl_type__const_clang_TypeDecl_P(ptr, Decl?.ptr, retValue.ptr)
        return retValue
    }
    inline fun computeBestEnumTypes(IsPacked: Boolean, NumNegativeBits: UInt, NumPositiveBits: UInt, BestType: QualType, BestPromotionType: QualType): Boolean {
        return clang_ASTContext_compute_best_enum_types(ptr, IsPacked, NumNegativeBits, NumPositiveBits, BestType.ptr, BestPromotionType.ptr)
    }
    inline fun getAttributedType__clang_attr_Kind_clang_QualType_clang_QualType(attrKind: AttrKind, modifiedType: QualType, equivalentType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_attributed_type(ptr, attrKind.value, modifiedType.ptr, equivalentType.ptr, retValue.ptr)
        return retValue
    }
    inline fun getAttributedType(nullability: NullabilityKind, modifiedType: QualType, equivalentType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_attributed_type__clang_NullabilityKind_clang_QualType_clang_QualType(ptr, nullability.value, modifiedType.ptr, equivalentType.ptr, retValue.ptr)
        return retValue
    }
    inline fun getTemplateTypeParmType(Depth: UInt, Index: UInt, ParameterPack: Boolean): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_template_type_parm_type(ptr, Depth, Index, ParameterPack, retValue.ptr)
        return retValue
    }
    inline fun getParenType(NamedType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_paren_type(ptr, NamedType.ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCObjectPointerType(OIT: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_object_pointer_type(ptr, OIT.ptr, retValue.ptr)
        return retValue
    }
    inline fun getTypeOfType(QT: QualType, Kind: TypeOfKind): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_type_of_type(ptr, QT.ptr, Kind.value, retValue.ptr)
        return retValue
    }
    inline fun getUnaryTransformType(BaseType: QualType, UnderlyingType: QualType, UKind: UTTKind): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unary_transform_type(ptr, BaseType.ptr, UnderlyingType.ptr, UKind.value, retValue.ptr)
        return retValue
    }
    inline fun getAutoType(DeducedType: QualType, Keyword: AutoTypeKeyword, IsDependent: Boolean, IsPack: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_auto_type(ptr, DeducedType.ptr, Keyword.value, IsDependent, IsPack, retValue.ptr)
        return retValue
    }
    inline fun getAutoDeductType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_auto_deduct_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getAutoRRefDeductType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_auto_r_ref_deduct_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUnconstrainedType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unconstrained_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getSizeType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_size_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getSignedSizeType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_signed_size_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getWCharType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_w_char_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getWideCharType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_wide_char_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getSignedWCharType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_signed_w_char_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUnsignedWCharType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unsigned_w_char_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getWIntType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_w_int_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getIntPtrType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_int_ptr_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUIntPtrType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_u_int_ptr_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getPointerDiffType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_pointer_diff_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUnsignedPointerDiffType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unsigned_pointer_diff_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getProcessIDType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_process_id_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getCFConstantStringType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_cf_constant_string_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCSuperType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_super_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setObjCSuperType(ST: QualType): Unit {
        return clang_ASTContext_set_obj_c_super_type(ptr, ST.ptr)
    }
    inline fun getRawCFConstantStringType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_raw_cf_constant_string_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setCFConstantStringType(T: QualType): Unit {
        return clang_ASTContext_set_cf_constant_string_type(ptr, T.ptr)
    }
    inline fun getCFConstantStringTagDecl(): RecordDeclApi? {
        return RecordDecl((clang_ASTContext_get_cf_constant_string_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun getObjCConstantStringInterface(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_constant_string_interface(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCNSStringType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_cns_string_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setObjCNSStringType(T: QualType): Unit {
        return clang_ASTContext_set_obj_cns_string_type(ptr, T.ptr)
    }
    inline fun getObjCIdRedefinitionType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_id_redefinition_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setObjCIdRedefinitionType(RedefType: QualType): Unit {
        return clang_ASTContext_set_obj_c_id_redefinition_type(ptr, RedefType.ptr)
    }
    inline fun getObjCClassRedefinitionType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_class_redefinition_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setObjCClassRedefinitionType(RedefType: QualType): Unit {
        return clang_ASTContext_set_obj_c_class_redefinition_type(ptr, RedefType.ptr)
    }
    inline fun getObjCSelRedefinitionType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_sel_redefinition_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setObjCSelRedefinitionType(RedefType: QualType): Unit {
        return clang_ASTContext_set_obj_c_sel_redefinition_type(ptr, RedefType.ptr)
    }
    inline fun getObjCInstanceType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_instance_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setFILEDecl(FILEDecl: TypeDeclApi?): Unit {
        return clang_ASTContext_set_file_decl(ptr, FILEDecl?.ptr)
    }
    inline fun getFILEType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_file_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setjmp_bufDecl(jmp_bufDecl: TypeDeclApi?): Unit {
        return clang_ASTContext_setjmp_buf_decl(ptr, jmp_bufDecl?.ptr)
    }
    inline fun getjmp_bufType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_getjmp_buf_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setsigjmp_bufDecl(sigjmp_bufDecl: TypeDeclApi?): Unit {
        return clang_ASTContext_setsigjmp_buf_decl(ptr, sigjmp_bufDecl?.ptr)
    }
    inline fun getsigjmp_bufType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_getsigjmp_buf_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setucontext_tDecl(ucontext_tDecl: TypeDeclApi?): Unit {
        return clang_ASTContext_setucontext_t_decl(ptr, ucontext_tDecl?.ptr)
    }
    inline fun getucontext_tType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_getucontext_t_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCEncodingForType(T: QualType, S: String?, Field: FieldDecl? = null, NotEncodedT: QualType? = null): Unit {
        return clang_ASTContext_get_obj_c_encoding_for_type(ptr, T.ptr, S, Field?.ptr, NotEncodedT?.ptr)
    }
    inline fun getObjCEncodingForPropertyType(T: QualType, S: String?): Unit {
        return clang_ASTContext_get_obj_c_encoding_for_property_type(ptr, T.ptr, S)
    }
    inline fun getLegacyIntegralTypeEncoding(t: QualType): Unit {
        return clang_ASTContext_get_legacy_integral_type_encoding(ptr, t.ptr)
    }
    inline fun getObjCEncodingForTypeQualifier(QT: ObjCDeclQualifier, S: String?): Unit {
        return clang_ASTContext_get_obj_c_encoding_for_type_qualifier(ptr, QT.value, S)
    }
    inline fun getObjCEncodingForFunctionDecl(Decl: FunctionDeclApi?): String? {
        val str: CPointer<ByteVar>? = clang_ASTContext_get_obj_c_encoding_for_function_decl(ptr, Decl?.ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getObjCIdType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_id_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCSelType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_sel_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCClassType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_class_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getBOOLType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_bool_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getObjCProtoType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_obj_c_proto_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getBuiltinVaListType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_builtin_va_list_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getVaListTagDecl(): DeclApi? {
        return Decl((clang_ASTContext_get_va_list_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun getBuiltinMSVaListType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_builtin_ms_va_list_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getMSGuidTagDecl(): TagDeclApi? {
        return TagDecl((clang_ASTContext_get_ms_guid_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun getMSTypeInfoTagDecl(): TagDeclApi? {
        return TagDecl((clang_ASTContext_get_ms_type_info_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun canBuiltinBeRedeclared(_arg_0: FunctionDeclApi?): Boolean {
        return clang_ASTContext_can_builtin_be_redeclared(ptr, _arg_0?.ptr)
    }
    inline fun getCVRQualifiedType(T: QualType, CVR: UInt): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_cvr_qualified_type(ptr, T.ptr, CVR, retValue.ptr)
        return retValue
    }
    inline fun getLifetimeQualifiedType(type: QualType, lifetime: ObjCLifetime): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_lifetime_qualified_type(ptr, type.ptr, lifetime.value, retValue.ptr)
        return retValue
    }
    inline fun getUnqualifiedObjCPointerType(type: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unqualified_obj_c_pointer_type(ptr, type.ptr, retValue.ptr)
        return retValue
    }
    inline fun getFixedPointScale(Ty: QualType): UByte {
        return clang_ASTContext_get_fixed_point_scale(ptr, Ty.ptr)
    }
    inline fun getFixedPointIBits(Ty: QualType): UByte {
        return clang_ASTContext_get_fixed_point_i_bits(ptr, Ty.ptr)
    }
    inline fun DecodeTypeStr(Str: String?, Context: ASTContext, Error: GetBuiltinTypeError, RequireICE: Boolean, AllowTypeModifiers: Boolean): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_decode_type_str(ptr, Str, Context.ptr, Error.value, RequireICE, AllowTypeModifiers, retValue.ptr)
        return retValue
    }
    inline fun GetBuiltinType(ID: UInt, Error: GetBuiltinTypeError, IntegerConstantArgs: CValuesRef<UIntVar>? = null): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_builtin_type(ptr, ID, Error.value, IntegerConstantArgs, retValue.ptr)
        return retValue
    }
    inline fun getObjCGCAttrKind(Ty: QualType): GC {
        return GCCompanionfromValue(clang_ASTContext_get_obj_cgc_attr_kind(ptr, Ty.ptr))
    }
    inline fun areCompatibleVectorTypes(FirstVec: QualType, SecondVec: QualType): Boolean {
        return clang_ASTContext_are_compatible_vector_types(ptr, FirstVec.ptr, SecondVec.ptr)
    }
    inline fun areCompatibleRVVTypes(FirstType: QualType, SecondType: QualType): Boolean {
        return clang_ASTContext_are_compatible_rvv_types(ptr, FirstType.ptr, SecondType.ptr)
    }
    inline fun areLaxCompatibleRVVTypes(FirstType: QualType, SecondType: QualType): Boolean {
        return clang_ASTContext_are_lax_compatible_rvv_types(ptr, FirstType.ptr, SecondType.ptr)
    }
    inline fun hasDirectOwnershipQualifier(Ty: QualType): Boolean {
        return clang_ASTContext_has_direct_ownership_qualifier(ptr, Ty.ptr)
    }
    inline fun getOpenMPDefaultSimdAlign(T: QualType): UInt {
        return clang_ASTContext_get_open_mp_default_simd_align(ptr, T.ptr)
    }
    inline fun getTypeSize(T: QualType): ULong {
        return clang_ASTContext_get_type_size(ptr, T.ptr)
    }
    inline fun getCharWidth(): ULong {
        return clang_ASTContext_get_char_width(ptr)
    }
    inline fun getTypeAlign(T: QualType): UInt {
        return clang_ASTContext_get_type_align(ptr, T.ptr)
    }
    inline fun getTypeUnadjustedAlign(T: QualType): UInt {
        return clang_ASTContext_get_type_unadjusted_align(ptr, T.ptr)
    }
    inline fun getTypeAlignIfKnown(T: QualType, NeedsPreferredAlignment: Boolean = false): UInt {
        return clang_ASTContext_get_type_align_if_known(ptr, T.ptr, NeedsPreferredAlignment)
    }
    inline fun isAlignmentRequired(T: QualType): Boolean {
        return clang_ASTContext_is_alignment_required__clang_QualType(ptr, T.ptr)
    }
    inline fun isPromotableIntegerType(T: QualType): Boolean {
        return clang_ASTContext_is_promotable_integer_type(ptr, T.ptr)
    }
    inline fun getPreferredTypeAlign(T: QualType): UInt {
        return clang_ASTContext_get_preferred_type_align(ptr, T.ptr)
    }
    inline fun getTargetDefaultAlignForAttributeAligned(): UInt {
        return clang_ASTContext_get_target_default_align_for_attribute_aligned(ptr)
    }
    inline fun defaultsToMsStruct(): Boolean {
        return clang_ASTContext_defaults_to_ms_struct(ptr)
    }
    inline fun getCurrentKeyFunction(RD: CXXRecordDecl?): CXXMethodDecl? {
        return CXXMethodDecl((clang_ASTContext_get_current_key_function(ptr, RD?.ptr) ?: return null), memScope)
    }
    inline fun setNonKeyFunction(method: CXXMethodDecl?): Unit {
        return clang_ASTContext_set_non_key_function(ptr, method?.ptr)
    }
    inline fun getFieldOffset(FD: ValueDeclApi?): ULong {
        return clang_ASTContext_get_field_offset(ptr, FD?.ptr)
    }
    inline fun isNearlyEmpty(RD: CXXRecordDecl?): Boolean {
        return clang_ASTContext_is_nearly_empty(ptr, RD?.ptr)
    }
    inline fun hasUniqueObjectRepresentations(Ty: QualType, CheckIfTriviallyCopyable: Boolean = true): Boolean {
        return clang_ASTContext_has_unique_object_representations(ptr, Ty.ptr, CheckIfTriviallyCopyable)
    }
    inline fun getUnqualifiedArrayType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_unqualified_array_type__clang_QualType(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun hasSameNullabilityTypeQualifier(SubT: QualType, SuperT: QualType, IsParam: Boolean): Boolean {
        return clang_ASTContext_has_same_nullability_type_qualifier(ptr, SubT.ptr, SuperT.ptr, IsParam)
    }
    inline fun UnwrapSimilarTypes(T1: QualType, T2: QualType, AllowPiMismatch: Boolean = true): Boolean {
        return clang_ASTContext_unwrap_similar_types(ptr, T1.ptr, T2.ptr, AllowPiMismatch)
    }
    inline fun UnwrapSimilarArrayTypes(T1: QualType, T2: QualType, AllowPiMismatch: Boolean = true): Unit {
        return clang_ASTContext_unwrap_similar_array_types(ptr, T1.ptr, T2.ptr, AllowPiMismatch)
    }
    inline fun hasSimilarType(T1: QualType, T2: QualType): Boolean {
        return clang_ASTContext_has_similar_type(ptr, T1.ptr, T2.ptr)
    }
    inline fun hasCvrSimilarType(T1: QualType, T2: QualType): Boolean {
        return clang_ASTContext_has_cvr_similar_type(ptr, T1.ptr, T2.ptr)
    }
    inline fun getDefaultCallingConvention(IsVariadic: Boolean, IsCXXMethod: Boolean): CallingConv {
        return CallingConvCompanionfromValue(clang_ASTContext_get_default_calling_convention(ptr, IsVariadic, IsCXXMethod))
    }
    inline fun isSameEntity(X: NamedDeclApi?, Y: NamedDeclApi?): Boolean {
        return clang_ASTContext_is_same_entity(ptr, X?.ptr, Y?.ptr)
    }
    inline fun isSameTemplateParameter(X: NamedDeclApi?, Y: NamedDeclApi?): Boolean {
        return clang_ASTContext_is_same_template_parameter(ptr, X?.ptr, Y?.ptr)
    }
    inline fun isSameDefaultTemplateArgument(X: NamedDeclApi?, Y: NamedDeclApi?): Boolean {
        return clang_ASTContext_is_same_default_template_argument(ptr, X?.ptr, Y?.ptr)
    }
    inline fun getBaseElementType(QT: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_base_element_type__clang_QualType(ptr, QT.ptr, retValue.ptr)
        return retValue
    }
    inline fun getAdjustedParameterType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_adjusted_parameter_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getSignatureParameterType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_signature_parameter_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getExceptionObjectType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_exception_object_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getArrayDecayedType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_array_decayed_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getPromotedIntegerType(PromotableType: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_promoted_integer_type(ptr, PromotableType.ptr, retValue.ptr)
        return retValue
    }
    inline fun getInnerObjCOwnership(T: QualType): ObjCLifetime {
        return ObjCLifetimeCompanionfromValue(clang_ASTContext_get_inner_obj_c_ownership(ptr, T.ptr))
    }
    inline fun getIntegerTypeOrder(LHS: QualType, RHS: QualType): Int {
        return clang_ASTContext_get_integer_type_order(ptr, LHS.ptr, RHS.ptr)
    }
    inline fun getFloatingTypeOrder(LHS: QualType, RHS: QualType): Int {
        return clang_ASTContext_get_floating_type_order(ptr, LHS.ptr, RHS.ptr)
    }
    inline fun getFloatingTypeSemanticOrder(LHS: QualType, RHS: QualType): Int {
        return clang_ASTContext_get_floating_type_semantic_order(ptr, LHS.ptr, RHS.ptr)
    }
    inline fun getTargetAddressSpace(AS: LangAS): UInt {
        return clang_ASTContext_get_target_address_space(ptr, AS.value)
    }
    inline fun getLangASForBuiltinAddressSpace(AS: UInt): LangAS {
        return CompanionfromValue(clang_ASTContext_get_lang_as_for_builtin_address_space(ptr, AS))
    }
    inline fun getTargetNullPointerValue(QT: QualType): ULong {
        return clang_ASTContext_get_target_null_pointer_value(ptr, QT.ptr)
    }
    inline fun addressSpaceMapManglingFor(AS: LangAS): Boolean {
        return clang_ASTContext_address_space_map_mangling_for(ptr, AS.value)
    }
    inline fun hasAnyFunctionEffects(): Boolean {
        return clang_ASTContext_has_any_function_effects(ptr)
    }
    inline fun getCommonSugaredType(X: QualType, Y: QualType, Unqualified: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_common_sugared_type(ptr, X.ptr, Y.ptr, Unqualified, retValue.ptr)
        return retValue
    }
    inline fun typesAreCompatible(T1: QualType, T2: QualType, CompareUnqualified: Boolean = false): Boolean {
        return clang_ASTContext_types_are_compatible(ptr, T1.ptr, T2.ptr, CompareUnqualified)
    }
    inline fun propertyTypesAreCompatible(_arg_0: QualType, _arg_1: QualType): Boolean {
        return clang_ASTContext_property_types_are_compatible(ptr, _arg_0.ptr, _arg_1.ptr)
    }
    inline fun typesAreBlockPointerCompatible(_arg_0: QualType, _arg_1: QualType): Boolean {
        return clang_ASTContext_types_are_block_pointer_compatible(ptr, _arg_0.ptr, _arg_1.ptr)
    }
    inline fun isObjCIdType(T: QualType): Boolean {
        return clang_ASTContext_is_obj_c_id_type(ptr, T.ptr)
    }
    inline fun isObjCClassType(T: QualType): Boolean {
        return clang_ASTContext_is_obj_c_class_type(ptr, T.ptr)
    }
    inline fun isObjCSelType(T: QualType): Boolean {
        return clang_ASTContext_is_obj_c_sel_type(ptr, T.ptr)
    }
    inline fun areComparableObjCPointerTypes(LHS: QualType, RHS: QualType): Boolean {
        return clang_ASTContext_are_comparable_obj_c_pointer_types(ptr, LHS.ptr, RHS.ptr)
    }
    inline fun canBindObjCObjectType(To: QualType, From: QualType): Boolean {
        return clang_ASTContext_can_bind_obj_c_object_type(ptr, To.ptr, From.ptr)
    }
    inline fun mergeTypes(_arg_0: QualType, _arg_1: QualType, OfBlockPointer: Boolean = false, Unqualified: Boolean = false, BlockReturnType: Boolean = false, IsConditionalOperator: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_types(ptr, _arg_0.ptr, _arg_1.ptr, OfBlockPointer, Unqualified, BlockReturnType, IsConditionalOperator, retValue.ptr)
        return retValue
    }
    inline fun mergeFunctionTypes(_arg_0: QualType, _arg_1: QualType, OfBlockPointer: Boolean = false, Unqualified: Boolean = false, AllowCXX: Boolean = false, IsConditionalOperator: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_function_types(ptr, _arg_0.ptr, _arg_1.ptr, OfBlockPointer, Unqualified, AllowCXX, IsConditionalOperator, retValue.ptr)
        return retValue
    }
    inline fun mergeFunctionParameterTypes(_arg_0: QualType, _arg_1: QualType, OfBlockPointer: Boolean = false, Unqualified: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_function_parameter_types(ptr, _arg_0.ptr, _arg_1.ptr, OfBlockPointer, Unqualified, retValue.ptr)
        return retValue
    }
    inline fun mergeTransparentUnionType(_arg_0: QualType, _arg_1: QualType, OfBlockPointer: Boolean = false, Unqualified: Boolean = false): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_transparent_union_type(ptr, _arg_0.ptr, _arg_1.ptr, OfBlockPointer, Unqualified, retValue.ptr)
        return retValue
    }
    inline fun mergeTagDefinitions(_arg_0: QualType, _arg_1: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_tag_definitions(ptr, _arg_0.ptr, _arg_1.ptr, retValue.ptr)
        return retValue
    }
    inline fun mergeObjCGCQualifiers(_arg_0: QualType, _arg_1: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_merge_obj_cgc_qualifiers(ptr, _arg_0.ptr, _arg_1.ptr, retValue.ptr)
        return retValue
    }
    inline fun getIntWidth(T: QualType): UInt {
        return clang_ASTContext_get_int_width(ptr, T.ptr)
    }
    inline fun getCorrespondingUnsignedType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_corresponding_unsigned_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getCorrespondingSignedType(T: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_corresponding_signed_type(ptr, T.ptr, retValue.ptr)
        return retValue
    }
    inline fun getCorrespondingSaturatedType(Ty: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_corresponding_saturated_type(ptr, Ty.ptr, retValue.ptr)
        return retValue
    }
    inline fun getCorrespondingUnsaturatedType(Ty: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_corresponding_unsaturated_type(ptr, Ty.ptr, retValue.ptr)
        return retValue
    }
    inline fun getCorrespondingSignedFixedPointType(Ty: QualType): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ASTContext_get_corresponding_signed_fixed_point_type(ptr, Ty.ptr, retValue.ptr)
        return retValue
    }
    inline fun AnyObjCImplementation(): Boolean {
        return clang_ASTContext_any_obj_c_implementation(ptr)
    }
    inline fun GetGVALinkageForFunction(FD: FunctionDeclApi?): GVALinkage {
        return GVALinkageCompanionfromValue(clang_ASTContext_get_gva_linkage_for_function(ptr, FD?.ptr))
    }
    inline fun DeclMustBeEmitted(D: DeclApi?): Boolean {
        return clang_ASTContext_decl_must_be_emitted(ptr, D?.ptr)
    }
    inline fun addDeclaratorForUnnamedTagDecl(TD: TagDeclApi?, DD: DeclaratorDeclApi?): Unit {
        return clang_ASTContext_add_declarator_for_unnamed_tag_decl(ptr, TD?.ptr, DD?.ptr)
    }
    inline fun getDeclaratorForUnnamedTagDecl(TD: TagDeclApi?): DeclaratorDeclApi? {
        return DeclaratorDecl((clang_ASTContext_get_declarator_for_unnamed_tag_decl(ptr, TD?.ptr) ?: return null), memScope)
    }
    inline fun setManglingNumber(ND: NamedDeclApi?, Number: UInt): Unit {
        return clang_ASTContext_set_mangling_number(ptr, ND?.ptr, Number)
    }
    inline fun getManglingNumber(ND: NamedDeclApi?, ForAuxTarget: Boolean = false): UInt {
        return clang_ASTContext_get_mangling_number(ptr, ND?.ptr, ForAuxTarget)
    }
    inline fun hasSeenTypeAwareOperatorNewOrDelete(): Boolean {
        return clang_ASTContext_has_seen_type_aware_operator_new_or_delete(ptr)
    }
    inline fun setIsDestroyingOperatorDelete(FD: FunctionDeclApi?, IsDestroying: Boolean): Unit {
        return clang_ASTContext_set_is_destroying_operator_delete(ptr, FD?.ptr, IsDestroying)
    }
    inline fun isDestroyingOperatorDelete(FD: FunctionDeclApi?): Boolean {
        return clang_ASTContext_is_destroying_operator_delete(ptr, FD?.ptr)
    }
    inline fun setIsTypeAwareOperatorNewOrDelete(FD: FunctionDeclApi?, IsTypeAware: Boolean): Unit {
        return clang_ASTContext_set_is_type_aware_operator_new_or_delete(ptr, FD?.ptr, IsTypeAware)
    }
    inline fun isTypeAwareOperatorNewOrDelete(FD: FunctionDeclApi?): Boolean {
        return clang_ASTContext_is_type_aware_operator_new_or_delete(ptr, FD?.ptr)
    }
    inline fun setClassNeedsVectorDeletingDestructor(RD: CXXRecordDecl?): Unit {
        return clang_ASTContext_set_class_needs_vector_deleting_destructor(ptr, RD?.ptr)
    }
    inline fun classNeedsVectorDeletingDestructor(RD: CXXRecordDecl?): Boolean {
        return clang_ASTContext_class_needs_vector_deleting_destructor(ptr, RD?.ptr)
    }
    inline fun getNextStringLiteralVersion(): UInt {
        return clang_ASTContext_get_next_string_literal_version(ptr)
    }
    inline fun registerSYCLEntryPointFunction(FD: FunctionDeclApi?): Unit {
        return clang_ASTContext_register_sycl_entry_point_function(ptr, FD?.ptr)
    }
    inline fun getObjCEncodingForMethodParameter(QT: ObjCDeclQualifier, T: QualType, S: String?, Extended: Boolean): Unit {
        return clang_ASTContext_get_obj_c_encoding_for_method_parameter(ptr, QT.value, T.ptr, S, Extended)
    }
    inline fun mayExternalize(D: DeclApi?): Boolean {
        return clang_ASTContext_may_externalize(ptr, D?.ptr)
    }
    inline fun shouldExternalize(D: DeclApi?): Boolean {
        return clang_ASTContext_should_externalize(ptr, D?.ptr)
    }
    inline fun baseForVTableAuthentication(ThisClass: CXXRecordDecl?): CXXRecordDecl? {
        return CXXRecordDecl((clang_ASTContext_base_for_v_table_authentication(ptr, ThisClass?.ptr) ?: return null), memScope)
    }
    inline fun backupStr(S: String?): String? {
        val str: CPointer<ByteVar>? = clang_ASTContext_backup_str(ptr, S)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    inline fun getCUIDHash(): String? {
        val str: CPointer<ByteVar>? = clang_ASTContext_get_cuid_hash(ptr)
        val ret: String? = str?.toKString()
        free(str)
        return ret
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ASTContext_size_of()
            }

        val align: Int
            inline get() {
                return clang_ASTContext_align_of()
            }

        inline fun MemScope.isObjCNSObjectType(Ty: QualType): Boolean {
            return clang_ASTContext_is_obj_cns_object_type(Ty.ptr)
        }
        inline fun MemScope.hasSameType(T1: QualType, T2: QualType): Boolean {
            return clang_ASTContext_has_same_type(T1.ptr, T2.ptr)
        }
        inline fun MemScope.hasSameUnqualifiedType(T1: QualType, T2: QualType): Boolean {
            return clang_ASTContext_has_same_unqualified_type(T1.ptr, T2.ptr)
        }
        fun MemScope.ASTContext_Holder(): ASTContext {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            defer {
                clang_ASTContext_dispose(memory)
            }
            return ASTContext(memory, this)
        }
    }
    val TUKind: TranslationUnitKind
        inline get() {
            return TranslationUnitKindCompanionfromValue(clang_ASTContext_TUKind_get(ptr))
        }

    var CommentsLoaded: Boolean
        inline get() {
            return clang_ASTContext_CommentsLoaded_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_CommentsLoaded_set(ptr, value)
        }

    var AutoDeductTy: QualType?
        inline get() {
            val retValue: QualType? = memScope.QualType_Holder()
            clang_ASTContext_AutoDeductTy_get(ptr, retValue?.ptr)
            return retValue
        }
        inline set(value) {
            clang_ASTContext_AutoDeductTy_set(ptr, value?.ptr)
        }

    var AutoRRefDeductTy: QualType?
        inline get() {
            val _retValue: QualType? = memScope.QualType_Holder()
            clang_ASTContext_AutoRRefDeductTy_get(ptr, _retValue?.ptr)
            return _retValue
        }
        inline set(value) {
            clang_ASTContext_AutoRRefDeductTy_set(ptr, value?.ptr)
        }

    var VaListTagDecl: Decl?
        inline get() {
            return Decl((clang_ASTContext_VaListTagDecl_get(ptr) ?: return null), memScope)
        }
        inline set(value) {
            clang_ASTContext_VaListTagDecl_set(ptr, value?.ptr)
        }

    var MSGuidTagDecl: TagDecl?
        inline get() {
            return TagDecl((clang_ASTContext_MSGuidTagDecl_get(ptr) ?: return null), memScope)
        }
        inline set(value) {
            clang_ASTContext_MSGuidTagDecl_set(ptr, value?.ptr)
        }

    var MSTypeInfoTagDecl: TagDecl?
        inline get() {
            return TagDecl((clang_ASTContext_MSTypeInfoTagDecl_get(ptr) ?: return null), memScope)
        }
        inline set(value) {
            clang_ASTContext_MSTypeInfoTagDecl_set(ptr, value?.ptr)
        }

    var NumImplicitDefaultConstructors: UInt
        inline get() {
            return clang_ASTContext_NumImplicitDefaultConstructors_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitDefaultConstructors_set(ptr, value)
        }

    var NumImplicitDefaultConstructorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitDefaultConstructorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitDefaultConstructorsDeclared_set(ptr, value)
        }

    var NumImplicitCopyConstructors: UInt
        inline get() {
            return clang_ASTContext_NumImplicitCopyConstructors_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitCopyConstructors_set(ptr, value)
        }

    var NumImplicitCopyConstructorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitCopyConstructorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitCopyConstructorsDeclared_set(ptr, value)
        }

    var NumImplicitMoveConstructors: UInt
        inline get() {
            return clang_ASTContext_NumImplicitMoveConstructors_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitMoveConstructors_set(ptr, value)
        }

    var NumImplicitMoveConstructorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitMoveConstructorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitMoveConstructorsDeclared_set(ptr, value)
        }

    var NumImplicitCopyAssignmentOperators: UInt
        inline get() {
            return clang_ASTContext_NumImplicitCopyAssignmentOperators_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitCopyAssignmentOperators_set(ptr, value)
        }

    var NumImplicitCopyAssignmentOperatorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_set(ptr, value)
        }

    var NumImplicitMoveAssignmentOperators: UInt
        inline get() {
            return clang_ASTContext_NumImplicitMoveAssignmentOperators_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitMoveAssignmentOperators_set(ptr, value)
        }

    var NumImplicitMoveAssignmentOperatorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_set(ptr, value)
        }

    var NumImplicitDestructors: UInt
        inline get() {
            return clang_ASTContext_NumImplicitDestructors_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitDestructors_set(ptr, value)
        }

    var NumImplicitDestructorsDeclared: UInt
        inline get() {
            return clang_ASTContext_NumImplicitDestructorsDeclared_get(ptr)
        }
        inline set(value) {
            clang_ASTContext_NumImplicitDestructorsDeclared_set(ptr, value)
        }

    inline fun dispose(): Unit {
        clang_ASTContext_dispose(ptr)
    }
    inline fun owned(): ASTContext {
        memScope.defer {
            clang_ASTContext_dispose(ptr)
        }
        return this
    }
}

// END KRAPPER GEN for clang::ASTContext


