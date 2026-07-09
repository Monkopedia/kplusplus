package clang

import clang.ArraySizeModifier.Companion.fromValue as ArraySizeModifierCompanionfromValue
import clang.Linkage.Companion.fromValue as LinkageCompanionfromValue
import clang.QualType.Companion.QualType_Holder
import clang.Visibility.Companion.fromValue as VisibilityCompanionfromValue
import clang.attr.Kind
import clang.qualifiers.ObjCLifetime
import clang.qualifiers.ObjCLifetime.Companion.fromValue as CompanionfromValue
import clang.type.ScalarTypeKind
import clang.type.ScalarTypeKind.Companion.fromValue as ScalarTypeKindCompanionfromValue
import clang.type.TypeClass
import clang.type.TypeClass.Companion.fromValue
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.UInt
import kotlin.Unit
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.toKString
import krapper.krapper_parse.internal.clang_ArrayType_align_of
import krapper.krapper_parse.internal.clang_ArrayType_as_clang_Type
import krapper.krapper_parse.internal.clang_ArrayType_classof
import krapper.krapper_parse.internal.clang_ArrayType_dyncast_clang_ConstantArrayType
import krapper.krapper_parse.internal.clang_ArrayType_get_element_type
import krapper.krapper_parse.internal.clang_ArrayType_get_index_type_cvr_qualifiers
import krapper.krapper_parse.internal.clang_ArrayType_get_size_modifier
import krapper.krapper_parse.internal.clang_ArrayType_size_of
import krapper.krapper_parse.internal.clang_Type_accepts_obj_c_type_params
import krapper.krapper_parse.internal.clang_Type_can_decay_to_pointer_type
import krapper.krapper_parse.internal.clang_Type_can_have_nullability
import krapper.krapper_parse.internal.clang_Type_cast_as_array_type_unsafe
import krapper.krapper_parse.internal.clang_Type_cast_as_cxx_record_decl
import krapper.krapper_parse.internal.clang_Type_cast_as_enum_decl
import krapper.krapper_parse.internal.clang_Type_cast_as_record_decl
import krapper.krapper_parse.internal.clang_Type_cast_as_tag_decl
import krapper.krapper_parse.internal.clang_Type_contains_errors
import krapper.krapper_parse.internal.clang_Type_contains_unexpanded_parameter_pack
import krapper.krapper_parse.internal.clang_Type_dump
import krapper.krapper_parse.internal.clang_Type_get_array_element_type_no_type_qual
import krapper.krapper_parse.internal.clang_Type_get_as_array_type_unsafe
import krapper.krapper_parse.internal.clang_Type_get_as_cxx_record_decl
import krapper.krapper_parse.internal.clang_Type_get_as_enum_decl
import krapper.krapper_parse.internal.clang_Type_get_as_record_decl
import krapper.krapper_parse.internal.clang_Type_get_as_tag_decl
import krapper.krapper_parse.internal.clang_Type_get_base_element_type_unsafe
import krapper.krapper_parse.internal.clang_Type_get_canonical_type_internal
import krapper.krapper_parse.internal.clang_Type_get_linkage
import krapper.krapper_parse.internal.clang_Type_get_locally_unqualified_single_step_desugared_type
import krapper.krapper_parse.internal.clang_Type_get_obj_carc_implicit_lifetime
import krapper.krapper_parse.internal.clang_Type_get_pointee_cxx_record_decl
import krapper.krapper_parse.internal.clang_Type_get_pointee_or_array_element_type
import krapper.krapper_parse.internal.clang_Type_get_pointee_type
import krapper.krapper_parse.internal.clang_Type_get_rvv_elt_type
import krapper.krapper_parse.internal.clang_Type_get_scalar_type_kind
import krapper.krapper_parse.internal.clang_Type_get_sizeless_vector_elt_type
import krapper.krapper_parse.internal.clang_Type_get_sve_elt_type
import krapper.krapper_parse.internal.clang_Type_get_type_class
import krapper.krapper_parse.internal.clang_Type_get_type_class_name
import krapper.krapper_parse.internal.clang_Type_get_unqualified_desugared_type
import krapper.krapper_parse.internal.clang_Type_get_visibility
import krapper.krapper_parse.internal.clang_Type_has_attr
import krapper.krapper_parse.internal.clang_Type_has_auto_for_trailing_return_type
import krapper.krapper_parse.internal.clang_Type_has_boolean_representation
import krapper.krapper_parse.internal.clang_Type_has_floating_representation
import krapper.krapper_parse.internal.clang_Type_has_integer_representation
import krapper.krapper_parse.internal.clang_Type_has_obj_c_pointer_representation
import krapper.krapper_parse.internal.clang_Type_has_pointee_to_cfi_unchecked_callee_function_type
import krapper.krapper_parse.internal.clang_Type_has_pointer_representation
import krapper.krapper_parse.internal.clang_Type_has_signed_integer_representation
import krapper.krapper_parse.internal.clang_Type_has_sized_vla_type
import krapper.krapper_parse.internal.clang_Type_has_unnamed_or_local_type
import krapper.krapper_parse.internal.clang_Type_has_unsigned_integer_representation
import krapper.krapper_parse.internal.clang_Type_is_aggregate_type
import krapper.krapper_parse.internal.clang_Type_is_align_val_t
import krapper.krapper_parse.internal.clang_Type_is_always_incomplete_type
import krapper.krapper_parse.internal.clang_Type_is_any_character_type
import krapper.krapper_parse.internal.clang_Type_is_any_complex_type
import krapper.krapper_parse.internal.clang_Type_is_any_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_arithmetic_type
import krapper.krapper_parse.internal.clang_Type_is_array_parameter_type
import krapper.krapper_parse.internal.clang_Type_is_array_type
import krapper.krapper_parse.internal.clang_Type_is_atomic_type
import krapper.krapper_parse.internal.clang_Type_is_b_float16type
import krapper.krapper_parse.internal.clang_Type_is_bit_int_type
import krapper.krapper_parse.internal.clang_Type_is_block_compatible_obj_c_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_block_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_boolean_type
import krapper.krapper_parse.internal.clang_Type_is_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_canonical_unqualified
import krapper.krapper_parse.internal.clang_Type_is_carc_bridgable_type
import krapper.krapper_parse.internal.clang_Type_is_cfi_unchecked_callee_function_type
import krapper.krapper_parse.internal.clang_Type_is_char16type
import krapper.krapper_parse.internal.clang_Type_is_char32type
import krapper.krapper_parse.internal.clang_Type_is_char8type
import krapper.krapper_parse.internal.clang_Type_is_char_type
import krapper.krapper_parse.internal.clang_Type_is_class_type
import krapper.krapper_parse.internal.clang_Type_is_clk_event_t
import krapper.krapper_parse.internal.clang_Type_is_complex_integer_type
import krapper.krapper_parse.internal.clang_Type_is_complex_type
import krapper.krapper_parse.internal.clang_Type_is_compound_type
import krapper.krapper_parse.internal.clang_Type_is_constant_array_type
import krapper.krapper_parse.internal.clang_Type_is_constant_matrix_bool_type
import krapper.krapper_parse.internal.clang_Type_is_constant_matrix_type
import krapper.krapper_parse.internal.clang_Type_is_constant_size_type
import krapper.krapper_parse.internal.clang_Type_is_convertible_to_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_count_attributed_type
import krapper.krapper_parse.internal.clang_Type_is_cuda_device_builtin_surface_type
import krapper.krapper_parse.internal.clang_Type_is_cuda_device_builtin_texture_type
import krapper.krapper_parse.internal.clang_Type_is_decltype_type
import krapper.krapper_parse.internal.clang_Type_is_dependent_address_space_type
import krapper.krapper_parse.internal.clang_Type_is_dependent_sized_array_type
import krapper.krapper_parse.internal.clang_Type_is_dependent_type
import krapper.krapper_parse.internal.clang_Type_is_double_type
import krapper.krapper_parse.internal.clang_Type_is_elaborated_type_specifier
import krapper.krapper_parse.internal.clang_Type_is_enumeral_type
import krapper.krapper_parse.internal.clang_Type_is_event_t
import krapper.krapper_parse.internal.clang_Type_is_ext_vector_bool_type
import krapper.krapper_parse.internal.clang_Type_is_ext_vector_type
import krapper.krapper_parse.internal.clang_Type_is_fixed_point_or_integer_type
import krapper.krapper_parse.internal.clang_Type_is_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_float128type
import krapper.krapper_parse.internal.clang_Type_is_float16type
import krapper.krapper_parse.internal.clang_Type_is_float32type
import krapper.krapper_parse.internal.clang_Type_is_floating_type
import krapper.krapper_parse.internal.clang_Type_is_from_ast
import krapper.krapper_parse.internal.clang_Type_is_function_no_proto_type
import krapper.krapper_parse.internal.clang_Type_is_function_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_function_proto_type
import krapper.krapper_parse.internal.clang_Type_is_function_reference_type
import krapper.krapper_parse.internal.clang_Type_is_function_type
import krapper.krapper_parse.internal.clang_Type_is_fundamental_type
import krapper.krapper_parse.internal.clang_Type_is_half_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_attributed_resource_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_builtin_intangible_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_inline_spirv_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_intangible_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_resource_record
import krapper.krapper_parse.internal.clang_Type_is_hlsl_resource_record_array
import krapper.krapper_parse.internal.clang_Type_is_hlsl_resource_type
import krapper.krapper_parse.internal.clang_Type_is_hlsl_specific_type
import krapper.krapper_parse.internal.clang_Type_is_ibm128type
import krapper.krapper_parse.internal.clang_Type_is_image_type
import krapper.krapper_parse.internal.clang_Type_is_incomplete_array_type
import krapper.krapper_parse.internal.clang_Type_is_incomplete_or_object_type
import krapper.krapper_parse.internal.clang_Type_is_incomplete_type
import krapper.krapper_parse.internal.clang_Type_is_instantiation_dependent_type
import krapper.krapper_parse.internal.clang_Type_is_integer_type
import krapper.krapper_parse.internal.clang_Type_is_integral_or_enumeration_type
import krapper.krapper_parse.internal.clang_Type_is_integral_or_unscoped_enumeration_type
import krapper.krapper_parse.internal.clang_Type_is_integral_type
import krapper.krapper_parse.internal.clang_Type_is_interface_type
import krapper.krapper_parse.internal.clang_Type_is_l_value_reference_type
import krapper.krapper_parse.internal.clang_Type_is_linkage_valid
import krapper.krapper_parse.internal.clang_Type_is_literal_type
import krapper.krapper_parse.internal.clang_Type_is_m_float8type
import krapper.krapper_parse.internal.clang_Type_is_matrix_type
import krapper.krapper_parse.internal.clang_Type_is_member_data_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_member_function_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_member_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_non_overload_placeholder_type
import krapper.krapper_parse.internal.clang_Type_is_nothrow_t
import krapper.krapper_parse.internal.clang_Type_is_null_ptr_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_boxable_record_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_class_or_class_kind_of_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_class_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_id_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_independent_class_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_indirect_lifetime_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_inert_unsafe_unretained_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_lifetime_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_object_or_interface_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_object_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_object_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_qualified_class_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_qualified_id_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_qualified_interface_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_retainable_type
import krapper.krapper_parse.internal.clang_Type_is_obj_c_sel_type
import krapper.krapper_parse.internal.clang_Type_is_obj_carc_bridgable_type
import krapper.krapper_parse.internal.clang_Type_is_obj_carc_implicitly_unretained_type
import krapper.krapper_parse.internal.clang_Type_is_obj_cns_object_type
import krapper.krapper_parse.internal.clang_Type_is_object_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_object_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_ext_opaque_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_array_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_array_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_array_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_buffer_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_buffer_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_buffer_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image1d_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_depth_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_depth_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_depth_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaa_depth_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaa_depth_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaa_depth_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaaro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaarw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_msaawo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_array_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_depth_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_depth_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_depth_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaa_depth_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaa_depth_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaa_depth_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaaro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaarw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_msaawo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image2d_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image3d_ro_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image3d_rw_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_image3d_wo_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_dual_reference_streamin_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_payload_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_result_dual_reference_streamout_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_result_single_reference_streamout_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_result_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ime_single_reference_streamin_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_mce_payload_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_mce_result_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ref_payload_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_ref_result_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_sic_payload_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_sic_result_type
import krapper.krapper_parse.internal.clang_Type_is_ocl_intel_subgroup_avc_type
import krapper.krapper_parse.internal.clang_Type_is_open_cl_specific_type
import krapper.krapper_parse.internal.clang_Type_is_overloadable_type
import krapper.krapper_parse.internal.clang_Type_is_packed_vector_bool_type
import krapper.krapper_parse.internal.clang_Type_is_pipe_type
import krapper.krapper_parse.internal.clang_Type_is_placeholder_type
import krapper.krapper_parse.internal.clang_Type_is_pointer_or_reference_type
import krapper.krapper_parse.internal.clang_Type_is_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_queue_t
import krapper.krapper_parse.internal.clang_Type_is_r_value_reference_type
import krapper.krapper_parse.internal.clang_Type_is_real_floating_type
import krapper.krapper_parse.internal.clang_Type_is_real_type
import krapper.krapper_parse.internal.clang_Type_is_record_type
import krapper.krapper_parse.internal.clang_Type_is_reference_type
import krapper.krapper_parse.internal.clang_Type_is_reserve_idt
import krapper.krapper_parse.internal.clang_Type_is_rvv_sizeless_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_rvvvls_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_sampler_t
import krapper.krapper_parse.internal.clang_Type_is_saturated_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_scalar_type
import krapper.krapper_parse.internal.clang_Type_is_scoped_enumeral_type
import krapper.krapper_parse.internal.clang_Type_is_signable_integer_type
import krapper.krapper_parse.internal.clang_Type_is_signable_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_signable_type
import krapper.krapper_parse.internal.clang_Type_is_signed_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_signed_integer_or_enumeration_type
import krapper.krapper_parse.internal.clang_Type_is_signed_integer_type
import krapper.krapper_parse.internal.clang_Type_is_sizeless_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_sizeless_type
import krapper.krapper_parse.internal.clang_Type_is_sizeless_vector_type
import krapper.krapper_parse.internal.clang_Type_is_specific_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_specific_placeholder_type
import krapper.krapper_parse.internal.clang_Type_is_specifier_type
import krapper.krapper_parse.internal.clang_Type_is_standard_layout_type
import krapper.krapper_parse.internal.clang_Type_is_std_byte_type
import krapper.krapper_parse.internal.clang_Type_is_structural_type
import krapper.krapper_parse.internal.clang_Type_is_structure_or_class_type
import krapper.krapper_parse.internal.clang_Type_is_structure_type
import krapper.krapper_parse.internal.clang_Type_is_structure_type_with_flexible_array_member
import krapper.krapper_parse.internal.clang_Type_is_subscriptable_vector_type
import krapper.krapper_parse.internal.clang_Type_is_sve_sizeless_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_sve_vls_builtin_type
import krapper.krapper_parse.internal.clang_Type_is_template_type_parm_type
import krapper.krapper_parse.internal.clang_Type_is_typedef_name_type
import krapper.krapper_parse.internal.clang_Type_is_undeduced_auto_type
import krapper.krapper_parse.internal.clang_Type_is_undeduced_type
import krapper.krapper_parse.internal.clang_Type_is_unicode_character_type
import krapper.krapper_parse.internal.clang_Type_is_union_type
import krapper.krapper_parse.internal.clang_Type_is_unsaturated_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_unscoped_enumeration_type
import krapper.krapper_parse.internal.clang_Type_is_unsigned_fixed_point_type
import krapper.krapper_parse.internal.clang_Type_is_unsigned_integer_or_enumeration_type
import krapper.krapper_parse.internal.clang_Type_is_unsigned_integer_type
import krapper.krapper_parse.internal.clang_Type_is_variable_array_type
import krapper.krapper_parse.internal.clang_Type_is_variably_modified_type
import krapper.krapper_parse.internal.clang_Type_is_vector_type
import krapper.krapper_parse.internal.clang_Type_is_visibility_explicit
import krapper.krapper_parse.internal.clang_Type_is_void_pointer_type
import krapper.krapper_parse.internal.clang_Type_is_void_type
import krapper.krapper_parse.internal.clang_Type_is_web_assembly_externref_type
import krapper.krapper_parse.internal.clang_Type_is_web_assembly_table_type
import krapper.krapper_parse.internal.clang_Type_is_wide_char_type

// BEGIN KRAPPER GEN for clang::ArrayType
// WARNING: polymorphic class with a non-virtual destructor — deleting a ArrayType through a base pointer is undefined in C++ (the derived destructor will not run). Give the base a `virtual ~...()` to fix.

@krapper.CppBinding("clang::ArrayType")
class ArrayType(
    override val ptr: COpaquePointer,
    val memScope: MemScope,
) : clang.ArrayTypeApi, clang.TypeApi {
    inline fun getTypeClass(): TypeClass {
        return fromValue(clang_Type_get_type_class(ptr))
    }
    inline fun isFromAST(): Boolean {
        return clang_Type_is_from_ast(ptr)
    }
    inline fun containsUnexpandedParameterPack(): Boolean {
        return clang_Type_contains_unexpanded_parameter_pack(ptr)
    }
    inline fun isCanonicalUnqualified(): Boolean {
        return clang_Type_is_canonical_unqualified(ptr)
    }
    inline fun getLocallyUnqualifiedSingleStepDesugaredType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_locally_unqualified_single_step_desugared_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun isSizelessType(): Boolean {
        return clang_Type_is_sizeless_type(ptr)
    }
    inline fun isSizelessBuiltinType(): Boolean {
        return clang_Type_is_sizeless_builtin_type(ptr)
    }
    inline fun isSizelessVectorType(): Boolean {
        return clang_Type_is_sizeless_vector_type(ptr)
    }
    inline fun isSVESizelessBuiltinType(): Boolean {
        return clang_Type_is_sve_sizeless_builtin_type(ptr)
    }
    inline fun isRVVSizelessBuiltinType(): Boolean {
        return clang_Type_is_rvv_sizeless_builtin_type(ptr)
    }
    inline fun isWebAssemblyExternrefType(): Boolean {
        return clang_Type_is_web_assembly_externref_type(ptr)
    }
    inline fun isWebAssemblyTableType(): Boolean {
        return clang_Type_is_web_assembly_table_type(ptr)
    }
    inline fun isSveVLSBuiltinType(): Boolean {
        return clang_Type_is_sve_vls_builtin_type(ptr)
    }
    inline fun getSveEltType(Ctx: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_sve_elt_type(ptr, Ctx.ptr, retValue.ptr)
        return retValue
    }
    inline fun isRVVVLSBuiltinType(): Boolean {
        return clang_Type_is_rvvvls_builtin_type(ptr)
    }
    inline fun getRVVEltType(Ctx: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_rvv_elt_type(ptr, Ctx.ptr, retValue.ptr)
        return retValue
    }
    inline fun getSizelessVectorEltType(Ctx: ASTContext): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_sizeless_vector_elt_type(ptr, Ctx.ptr, retValue.ptr)
        return retValue
    }
    inline fun isIncompleteType(Def: NamedDeclApi? = null): Boolean {
        return clang_Type_is_incomplete_type(ptr, Def?.ptr)
    }
    inline fun isIncompleteOrObjectType(): Boolean {
        return clang_Type_is_incomplete_or_object_type(ptr)
    }
    inline fun isAlwaysIncompleteType(): Boolean {
        return clang_Type_is_always_incomplete_type(ptr)
    }
    inline fun isObjectType(): Boolean {
        return clang_Type_is_object_type(ptr)
    }
    inline fun isLiteralType(Ctx: ASTContext): Boolean {
        return clang_Type_is_literal_type(ptr, Ctx.ptr)
    }
    inline fun isStructuralType(): Boolean {
        return clang_Type_is_structural_type(ptr)
    }
    inline fun isStandardLayoutType(): Boolean {
        return clang_Type_is_standard_layout_type(ptr)
    }
    inline fun isBuiltinType(): Boolean {
        return clang_Type_is_builtin_type(ptr)
    }
    inline fun isSpecificBuiltinType(K: UInt): Boolean {
        return clang_Type_is_specific_builtin_type(ptr, K)
    }
    inline fun isPlaceholderType(): Boolean {
        return clang_Type_is_placeholder_type(ptr)
    }
    inline fun isSpecificPlaceholderType(K: UInt): Boolean {
        return clang_Type_is_specific_placeholder_type(ptr, K)
    }
    inline fun isNonOverloadPlaceholderType(): Boolean {
        return clang_Type_is_non_overload_placeholder_type(ptr)
    }
    inline fun isIntegerType(): Boolean {
        return clang_Type_is_integer_type(ptr)
    }
    inline fun isEnumeralType(): Boolean {
        return clang_Type_is_enumeral_type(ptr)
    }
    inline fun isScopedEnumeralType(): Boolean {
        return clang_Type_is_scoped_enumeral_type(ptr)
    }
    inline fun isBooleanType(): Boolean {
        return clang_Type_is_boolean_type(ptr)
    }
    inline fun isCharType(): Boolean {
        return clang_Type_is_char_type(ptr)
    }
    inline fun isWideCharType(): Boolean {
        return clang_Type_is_wide_char_type(ptr)
    }
    inline fun isChar8Type(): Boolean {
        return clang_Type_is_char8type(ptr)
    }
    inline fun isChar16Type(): Boolean {
        return clang_Type_is_char16type(ptr)
    }
    inline fun isChar32Type(): Boolean {
        return clang_Type_is_char32type(ptr)
    }
    inline fun isAnyCharacterType(): Boolean {
        return clang_Type_is_any_character_type(ptr)
    }
    inline fun isUnicodeCharacterType(): Boolean {
        return clang_Type_is_unicode_character_type(ptr)
    }
    inline fun isIntegralType(Ctx: ASTContext): Boolean {
        return clang_Type_is_integral_type(ptr, Ctx.ptr)
    }
    inline fun isIntegralOrEnumerationType(): Boolean {
        return clang_Type_is_integral_or_enumeration_type(ptr)
    }
    inline fun isIntegralOrUnscopedEnumerationType(): Boolean {
        return clang_Type_is_integral_or_unscoped_enumeration_type(ptr)
    }
    inline fun isUnscopedEnumerationType(): Boolean {
        return clang_Type_is_unscoped_enumeration_type(ptr)
    }
    inline fun isRealFloatingType(): Boolean {
        return clang_Type_is_real_floating_type(ptr)
    }
    inline fun isComplexType(): Boolean {
        return clang_Type_is_complex_type(ptr)
    }
    inline fun isAnyComplexType(): Boolean {
        return clang_Type_is_any_complex_type(ptr)
    }
    inline fun isFloatingType(): Boolean {
        return clang_Type_is_floating_type(ptr)
    }
    inline fun isHalfType(): Boolean {
        return clang_Type_is_half_type(ptr)
    }
    inline fun isFloat16Type(): Boolean {
        return clang_Type_is_float16type(ptr)
    }
    inline fun isFloat32Type(): Boolean {
        return clang_Type_is_float32type(ptr)
    }
    inline fun isDoubleType(): Boolean {
        return clang_Type_is_double_type(ptr)
    }
    inline fun isBFloat16Type(): Boolean {
        return clang_Type_is_b_float16type(ptr)
    }
    inline fun isMFloat8Type(): Boolean {
        return clang_Type_is_m_float8type(ptr)
    }
    inline fun isFloat128Type(): Boolean {
        return clang_Type_is_float128type(ptr)
    }
    inline fun isIbm128Type(): Boolean {
        return clang_Type_is_ibm128type(ptr)
    }
    inline fun isRealType(): Boolean {
        return clang_Type_is_real_type(ptr)
    }
    inline fun isArithmeticType(): Boolean {
        return clang_Type_is_arithmetic_type(ptr)
    }
    inline fun isVoidType(): Boolean {
        return clang_Type_is_void_type(ptr)
    }
    inline fun isScalarType(): Boolean {
        return clang_Type_is_scalar_type(ptr)
    }
    inline fun isAggregateType(): Boolean {
        return clang_Type_is_aggregate_type(ptr)
    }
    inline fun isFundamentalType(): Boolean {
        return clang_Type_is_fundamental_type(ptr)
    }
    inline fun isCompoundType(): Boolean {
        return clang_Type_is_compound_type(ptr)
    }
    inline fun isFunctionType(): Boolean {
        return clang_Type_is_function_type(ptr)
    }
    inline fun isFunctionNoProtoType(): Boolean {
        return clang_Type_is_function_no_proto_type(ptr)
    }
    inline fun isFunctionProtoType(): Boolean {
        return clang_Type_is_function_proto_type(ptr)
    }
    inline fun isPointerType(): Boolean {
        return clang_Type_is_pointer_type(ptr)
    }
    inline fun isPointerOrReferenceType(): Boolean {
        return clang_Type_is_pointer_or_reference_type(ptr)
    }
    inline fun isSignableType(Ctx: ASTContext): Boolean {
        return clang_Type_is_signable_type(ptr, Ctx.ptr)
    }
    inline fun isSignablePointerType(): Boolean {
        return clang_Type_is_signable_pointer_type(ptr)
    }
    inline fun isSignableIntegerType(Ctx: ASTContext): Boolean {
        return clang_Type_is_signable_integer_type(ptr, Ctx.ptr)
    }
    inline fun isAnyPointerType(): Boolean {
        return clang_Type_is_any_pointer_type(ptr)
    }
    inline fun isCountAttributedType(): Boolean {
        return clang_Type_is_count_attributed_type(ptr)
    }
    inline fun isCFIUncheckedCalleeFunctionType(): Boolean {
        return clang_Type_is_cfi_unchecked_callee_function_type(ptr)
    }
    inline fun hasPointeeToCFIUncheckedCalleeFunctionType(): Boolean {
        return clang_Type_has_pointee_to_cfi_unchecked_callee_function_type(ptr)
    }
    inline fun isBlockPointerType(): Boolean {
        return clang_Type_is_block_pointer_type(ptr)
    }
    inline fun isVoidPointerType(): Boolean {
        return clang_Type_is_void_pointer_type(ptr)
    }
    inline fun isReferenceType(): Boolean {
        return clang_Type_is_reference_type(ptr)
    }
    inline fun isLValueReferenceType(): Boolean {
        return clang_Type_is_l_value_reference_type(ptr)
    }
    inline fun isRValueReferenceType(): Boolean {
        return clang_Type_is_r_value_reference_type(ptr)
    }
    inline fun isObjectPointerType(): Boolean {
        return clang_Type_is_object_pointer_type(ptr)
    }
    inline fun isFunctionPointerType(): Boolean {
        return clang_Type_is_function_pointer_type(ptr)
    }
    inline fun isFunctionReferenceType(): Boolean {
        return clang_Type_is_function_reference_type(ptr)
    }
    inline fun isMemberPointerType(): Boolean {
        return clang_Type_is_member_pointer_type(ptr)
    }
    inline fun isMemberFunctionPointerType(): Boolean {
        return clang_Type_is_member_function_pointer_type(ptr)
    }
    inline fun isMemberDataPointerType(): Boolean {
        return clang_Type_is_member_data_pointer_type(ptr)
    }
    inline fun isArrayType(): Boolean {
        return clang_Type_is_array_type(ptr)
    }
    inline fun isConstantArrayType(): Boolean {
        return clang_Type_is_constant_array_type(ptr)
    }
    inline fun isIncompleteArrayType(): Boolean {
        return clang_Type_is_incomplete_array_type(ptr)
    }
    inline fun isVariableArrayType(): Boolean {
        return clang_Type_is_variable_array_type(ptr)
    }
    inline fun isArrayParameterType(): Boolean {
        return clang_Type_is_array_parameter_type(ptr)
    }
    inline fun isDependentSizedArrayType(): Boolean {
        return clang_Type_is_dependent_sized_array_type(ptr)
    }
    inline fun isRecordType(): Boolean {
        return clang_Type_is_record_type(ptr)
    }
    inline fun isClassType(): Boolean {
        return clang_Type_is_class_type(ptr)
    }
    inline fun isStructureType(): Boolean {
        return clang_Type_is_structure_type(ptr)
    }
    inline fun isStructureTypeWithFlexibleArrayMember(): Boolean {
        return clang_Type_is_structure_type_with_flexible_array_member(ptr)
    }
    inline fun isObjCBoxableRecordType(): Boolean {
        return clang_Type_is_obj_c_boxable_record_type(ptr)
    }
    inline fun isInterfaceType(): Boolean {
        return clang_Type_is_interface_type(ptr)
    }
    inline fun isStructureOrClassType(): Boolean {
        return clang_Type_is_structure_or_class_type(ptr)
    }
    inline fun isUnionType(): Boolean {
        return clang_Type_is_union_type(ptr)
    }
    inline fun isComplexIntegerType(): Boolean {
        return clang_Type_is_complex_integer_type(ptr)
    }
    inline fun isVectorType(): Boolean {
        return clang_Type_is_vector_type(ptr)
    }
    inline fun isExtVectorType(): Boolean {
        return clang_Type_is_ext_vector_type(ptr)
    }
    inline fun isExtVectorBoolType(): Boolean {
        return clang_Type_is_ext_vector_bool_type(ptr)
    }
    inline fun isConstantMatrixBoolType(): Boolean {
        return clang_Type_is_constant_matrix_bool_type(ptr)
    }
    inline fun isPackedVectorBoolType(ctx: ASTContext): Boolean {
        return clang_Type_is_packed_vector_bool_type(ptr, ctx.ptr)
    }
    inline fun isSubscriptableVectorType(): Boolean {
        return clang_Type_is_subscriptable_vector_type(ptr)
    }
    inline fun isMatrixType(): Boolean {
        return clang_Type_is_matrix_type(ptr)
    }
    inline fun isConstantMatrixType(): Boolean {
        return clang_Type_is_constant_matrix_type(ptr)
    }
    inline fun isDependentAddressSpaceType(): Boolean {
        return clang_Type_is_dependent_address_space_type(ptr)
    }
    inline fun isObjCObjectPointerType(): Boolean {
        return clang_Type_is_obj_c_object_pointer_type(ptr)
    }
    inline fun isObjCRetainableType(): Boolean {
        return clang_Type_is_obj_c_retainable_type(ptr)
    }
    inline fun isObjCLifetimeType(): Boolean {
        return clang_Type_is_obj_c_lifetime_type(ptr)
    }
    inline fun isObjCIndirectLifetimeType(): Boolean {
        return clang_Type_is_obj_c_indirect_lifetime_type(ptr)
    }
    inline fun isObjCNSObjectType(): Boolean {
        return clang_Type_is_obj_cns_object_type(ptr)
    }
    inline fun isObjCIndependentClassType(): Boolean {
        return clang_Type_is_obj_c_independent_class_type(ptr)
    }
    inline fun isObjCObjectType(): Boolean {
        return clang_Type_is_obj_c_object_type(ptr)
    }
    inline fun isObjCQualifiedInterfaceType(): Boolean {
        return clang_Type_is_obj_c_qualified_interface_type(ptr)
    }
    inline fun isObjCQualifiedIdType(): Boolean {
        return clang_Type_is_obj_c_qualified_id_type(ptr)
    }
    inline fun isObjCQualifiedClassType(): Boolean {
        return clang_Type_is_obj_c_qualified_class_type(ptr)
    }
    inline fun isObjCObjectOrInterfaceType(): Boolean {
        return clang_Type_is_obj_c_object_or_interface_type(ptr)
    }
    inline fun isObjCIdType(): Boolean {
        return clang_Type_is_obj_c_id_type(ptr)
    }
    inline fun isDecltypeType(): Boolean {
        return clang_Type_is_decltype_type(ptr)
    }
    inline fun isObjCInertUnsafeUnretainedType(): Boolean {
        return clang_Type_is_obj_c_inert_unsafe_unretained_type(ptr)
    }
    inline fun isObjCClassType(): Boolean {
        return clang_Type_is_obj_c_class_type(ptr)
    }
    inline fun isObjCClassOrClassKindOfType(): Boolean {
        return clang_Type_is_obj_c_class_or_class_kind_of_type(ptr)
    }
    inline fun isBlockCompatibleObjCPointerType(ctx: ASTContext): Boolean {
        return clang_Type_is_block_compatible_obj_c_pointer_type(ptr, ctx.ptr)
    }
    inline fun isObjCSelType(): Boolean {
        return clang_Type_is_obj_c_sel_type(ptr)
    }
    inline fun isObjCBuiltinType(): Boolean {
        return clang_Type_is_obj_c_builtin_type(ptr)
    }
    inline fun isObjCARCBridgableType(): Boolean {
        return clang_Type_is_obj_carc_bridgable_type(ptr)
    }
    inline fun isCARCBridgableType(): Boolean {
        return clang_Type_is_carc_bridgable_type(ptr)
    }
    inline fun isTemplateTypeParmType(): Boolean {
        return clang_Type_is_template_type_parm_type(ptr)
    }
    inline fun isNullPtrType(): Boolean {
        return clang_Type_is_null_ptr_type(ptr)
    }
    inline fun isNothrowT(): Boolean {
        return clang_Type_is_nothrow_t(ptr)
    }
    inline fun isAlignValT(): Boolean {
        return clang_Type_is_align_val_t(ptr)
    }
    inline fun isStdByteType(): Boolean {
        return clang_Type_is_std_byte_type(ptr)
    }
    inline fun isAtomicType(): Boolean {
        return clang_Type_is_atomic_type(ptr)
    }
    inline fun isUndeducedAutoType(): Boolean {
        return clang_Type_is_undeduced_auto_type(ptr)
    }
    inline fun isTypedefNameType(): Boolean {
        return clang_Type_is_typedef_name_type(ptr)
    }
    inline fun isOCLImage1dROType(): Boolean {
        return clang_Type_is_ocl_image1d_ro_type(ptr)
    }
    inline fun isOCLImage1dArrayROType(): Boolean {
        return clang_Type_is_ocl_image1d_array_ro_type(ptr)
    }
    inline fun isOCLImage1dBufferROType(): Boolean {
        return clang_Type_is_ocl_image1d_buffer_ro_type(ptr)
    }
    inline fun isOCLImage2dROType(): Boolean {
        return clang_Type_is_ocl_image2d_ro_type(ptr)
    }
    inline fun isOCLImage2dArrayROType(): Boolean {
        return clang_Type_is_ocl_image2d_array_ro_type(ptr)
    }
    inline fun isOCLImage2dDepthROType(): Boolean {
        return clang_Type_is_ocl_image2d_depth_ro_type(ptr)
    }
    inline fun isOCLImage2dArrayDepthROType(): Boolean {
        return clang_Type_is_ocl_image2d_array_depth_ro_type(ptr)
    }
    inline fun isOCLImage2dMSAAROType(): Boolean {
        return clang_Type_is_ocl_image2d_msaaro_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAAROType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaaro_type(ptr)
    }
    inline fun isOCLImage2dMSAADepthROType(): Boolean {
        return clang_Type_is_ocl_image2d_msaa_depth_ro_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAADepthROType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaa_depth_ro_type(ptr)
    }
    inline fun isOCLImage3dROType(): Boolean {
        return clang_Type_is_ocl_image3d_ro_type(ptr)
    }
    inline fun isOCLImage1dWOType(): Boolean {
        return clang_Type_is_ocl_image1d_wo_type(ptr)
    }
    inline fun isOCLImage1dArrayWOType(): Boolean {
        return clang_Type_is_ocl_image1d_array_wo_type(ptr)
    }
    inline fun isOCLImage1dBufferWOType(): Boolean {
        return clang_Type_is_ocl_image1d_buffer_wo_type(ptr)
    }
    inline fun isOCLImage2dWOType(): Boolean {
        return clang_Type_is_ocl_image2d_wo_type(ptr)
    }
    inline fun isOCLImage2dArrayWOType(): Boolean {
        return clang_Type_is_ocl_image2d_array_wo_type(ptr)
    }
    inline fun isOCLImage2dDepthWOType(): Boolean {
        return clang_Type_is_ocl_image2d_depth_wo_type(ptr)
    }
    inline fun isOCLImage2dArrayDepthWOType(): Boolean {
        return clang_Type_is_ocl_image2d_array_depth_wo_type(ptr)
    }
    inline fun isOCLImage2dMSAAWOType(): Boolean {
        return clang_Type_is_ocl_image2d_msaawo_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAAWOType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaawo_type(ptr)
    }
    inline fun isOCLImage2dMSAADepthWOType(): Boolean {
        return clang_Type_is_ocl_image2d_msaa_depth_wo_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAADepthWOType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaa_depth_wo_type(ptr)
    }
    inline fun isOCLImage3dWOType(): Boolean {
        return clang_Type_is_ocl_image3d_wo_type(ptr)
    }
    inline fun isOCLImage1dRWType(): Boolean {
        return clang_Type_is_ocl_image1d_rw_type(ptr)
    }
    inline fun isOCLImage1dArrayRWType(): Boolean {
        return clang_Type_is_ocl_image1d_array_rw_type(ptr)
    }
    inline fun isOCLImage1dBufferRWType(): Boolean {
        return clang_Type_is_ocl_image1d_buffer_rw_type(ptr)
    }
    inline fun isOCLImage2dRWType(): Boolean {
        return clang_Type_is_ocl_image2d_rw_type(ptr)
    }
    inline fun isOCLImage2dArrayRWType(): Boolean {
        return clang_Type_is_ocl_image2d_array_rw_type(ptr)
    }
    inline fun isOCLImage2dDepthRWType(): Boolean {
        return clang_Type_is_ocl_image2d_depth_rw_type(ptr)
    }
    inline fun isOCLImage2dArrayDepthRWType(): Boolean {
        return clang_Type_is_ocl_image2d_array_depth_rw_type(ptr)
    }
    inline fun isOCLImage2dMSAARWType(): Boolean {
        return clang_Type_is_ocl_image2d_msaarw_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAARWType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaarw_type(ptr)
    }
    inline fun isOCLImage2dMSAADepthRWType(): Boolean {
        return clang_Type_is_ocl_image2d_msaa_depth_rw_type(ptr)
    }
    inline fun isOCLImage2dArrayMSAADepthRWType(): Boolean {
        return clang_Type_is_ocl_image2d_array_msaa_depth_rw_type(ptr)
    }
    inline fun isOCLImage3dRWType(): Boolean {
        return clang_Type_is_ocl_image3d_rw_type(ptr)
    }
    inline fun isImageType(): Boolean {
        return clang_Type_is_image_type(ptr)
    }
    inline fun isSamplerT(): Boolean {
        return clang_Type_is_sampler_t(ptr)
    }
    inline fun isEventT(): Boolean {
        return clang_Type_is_event_t(ptr)
    }
    inline fun isClkEventT(): Boolean {
        return clang_Type_is_clk_event_t(ptr)
    }
    inline fun isQueueT(): Boolean {
        return clang_Type_is_queue_t(ptr)
    }
    inline fun isReserveIDT(): Boolean {
        return clang_Type_is_reserve_idt(ptr)
    }
    inline fun isOCLIntelSubgroupAVCMcePayloadType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_mce_payload_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImePayloadType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_payload_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCRefPayloadType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ref_payload_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCSicPayloadType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_sic_payload_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCMceResultType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_mce_result_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImeResultType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_result_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCRefResultType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ref_result_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCSicResultType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_sic_result_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImeResultSingleReferenceStreamoutType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_result_single_reference_streamout_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImeResultDualReferenceStreamoutType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_result_dual_reference_streamout_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImeSingleReferenceStreaminType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_single_reference_streamin_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCImeDualReferenceStreaminType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_ime_dual_reference_streamin_type(ptr)
    }
    inline fun isOCLIntelSubgroupAVCType(): Boolean {
        return clang_Type_is_ocl_intel_subgroup_avc_type(ptr)
    }
    inline fun isOCLExtOpaqueType(): Boolean {
        return clang_Type_is_ocl_ext_opaque_type(ptr)
    }
    inline fun isPipeType(): Boolean {
        return clang_Type_is_pipe_type(ptr)
    }
    inline fun isBitIntType(): Boolean {
        return clang_Type_is_bit_int_type(ptr)
    }
    inline fun isOpenCLSpecificType(): Boolean {
        return clang_Type_is_open_cl_specific_type(ptr)
    }
    inline fun isHLSLResourceType(): Boolean {
        return clang_Type_is_hlsl_resource_type(ptr)
    }
    inline fun isHLSLSpecificType(): Boolean {
        return clang_Type_is_hlsl_specific_type(ptr)
    }
    inline fun isHLSLBuiltinIntangibleType(): Boolean {
        return clang_Type_is_hlsl_builtin_intangible_type(ptr)
    }
    inline fun isHLSLAttributedResourceType(): Boolean {
        return clang_Type_is_hlsl_attributed_resource_type(ptr)
    }
    inline fun isHLSLInlineSpirvType(): Boolean {
        return clang_Type_is_hlsl_inline_spirv_type(ptr)
    }
    inline fun isHLSLResourceRecord(): Boolean {
        return clang_Type_is_hlsl_resource_record(ptr)
    }
    inline fun isHLSLResourceRecordArray(): Boolean {
        return clang_Type_is_hlsl_resource_record_array(ptr)
    }
    inline fun isHLSLIntangibleType(): Boolean {
        return clang_Type_is_hlsl_intangible_type(ptr)
    }
    inline fun isObjCARCImplicitlyUnretainedType(): Boolean {
        return clang_Type_is_obj_carc_implicitly_unretained_type(ptr)
    }
    inline fun isCUDADeviceBuiltinSurfaceType(): Boolean {
        return clang_Type_is_cuda_device_builtin_surface_type(ptr)
    }
    inline fun isCUDADeviceBuiltinTextureType(): Boolean {
        return clang_Type_is_cuda_device_builtin_texture_type(ptr)
    }
    inline fun getObjCARCImplicitLifetime(): ObjCLifetime {
        return CompanionfromValue(clang_Type_get_obj_carc_implicit_lifetime(ptr))
    }
    inline fun getScalarTypeKind(): ScalarTypeKind {
        return ScalarTypeKindCompanionfromValue(clang_Type_get_scalar_type_kind(ptr))
    }
    inline fun containsErrors(): Boolean {
        return clang_Type_contains_errors(ptr)
    }
    inline fun isDependentType(): Boolean {
        return clang_Type_is_dependent_type(ptr)
    }
    inline fun isInstantiationDependentType(): Boolean {
        return clang_Type_is_instantiation_dependent_type(ptr)
    }
    inline fun isUndeducedType(): Boolean {
        return clang_Type_is_undeduced_type(ptr)
    }
    inline fun isVariablyModifiedType(): Boolean {
        return clang_Type_is_variably_modified_type(ptr)
    }
    inline fun hasSizedVLAType(): Boolean {
        return clang_Type_has_sized_vla_type(ptr)
    }
    inline fun hasUnnamedOrLocalType(): Boolean {
        return clang_Type_has_unnamed_or_local_type(ptr)
    }
    inline fun isOverloadableType(): Boolean {
        return clang_Type_is_overloadable_type(ptr)
    }
    inline fun isElaboratedTypeSpecifier(): Boolean {
        return clang_Type_is_elaborated_type_specifier(ptr)
    }
    inline fun canDecayToPointerType(): Boolean {
        return clang_Type_can_decay_to_pointer_type(ptr)
    }
    inline fun hasPointerRepresentation(): Boolean {
        return clang_Type_has_pointer_representation(ptr)
    }
    inline fun hasObjCPointerRepresentation(): Boolean {
        return clang_Type_has_obj_c_pointer_representation(ptr)
    }
    inline fun hasIntegerRepresentation(): Boolean {
        return clang_Type_has_integer_representation(ptr)
    }
    inline fun hasSignedIntegerRepresentation(): Boolean {
        return clang_Type_has_signed_integer_representation(ptr)
    }
    inline fun hasUnsignedIntegerRepresentation(): Boolean {
        return clang_Type_has_unsigned_integer_representation(ptr)
    }
    inline fun hasFloatingRepresentation(): Boolean {
        return clang_Type_has_floating_representation(ptr)
    }
    inline fun hasBooleanRepresentation(): Boolean {
        return clang_Type_has_boolean_representation(ptr)
    }
    inline fun getAsCXXRecordDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_Type_get_as_cxx_record_decl(ptr) ?: return null), memScope)
    }
    inline fun castAsCXXRecordDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_Type_cast_as_cxx_record_decl(ptr) ?: return null), memScope)
    }
    inline fun getAsRecordDecl(): RecordDeclApi? {
        return RecordDecl((clang_Type_get_as_record_decl(ptr) ?: return null), memScope)
    }
    inline fun castAsRecordDecl(): RecordDeclApi? {
        return RecordDecl((clang_Type_cast_as_record_decl(ptr) ?: return null), memScope)
    }
    inline fun getAsEnumDecl(): EnumDecl? {
        return EnumDecl((clang_Type_get_as_enum_decl(ptr) ?: return null), memScope)
    }
    inline fun castAsEnumDecl(): EnumDecl? {
        return EnumDecl((clang_Type_cast_as_enum_decl(ptr) ?: return null), memScope)
    }
    inline fun getAsTagDecl(): TagDeclApi? {
        return TagDecl((clang_Type_get_as_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun castAsTagDecl(): TagDeclApi? {
        return TagDecl((clang_Type_cast_as_tag_decl(ptr) ?: return null), memScope)
    }
    inline fun getPointeeCXXRecordDecl(): CXXRecordDeclApi? {
        return CXXRecordDecl((clang_Type_get_pointee_cxx_record_decl(ptr) ?: return null), memScope)
    }
    inline fun hasAutoForTrailingReturnType(): Boolean {
        return clang_Type_has_auto_for_trailing_return_type(ptr)
    }
    inline fun getAsArrayTypeUnsafe(): ArrayTypeApi? {
        return ArrayType((clang_Type_get_as_array_type_unsafe(ptr) ?: return null), memScope)
    }
    inline fun castAsArrayTypeUnsafe(): ArrayTypeApi? {
        return ArrayType((clang_Type_cast_as_array_type_unsafe(ptr) ?: return null), memScope)
    }
    inline fun hasAttr(AK: Kind): Boolean {
        return clang_Type_has_attr(ptr, AK.value)
    }
    inline fun getBaseElementTypeUnsafe(): TypeApi? {
        return Type((clang_Type_get_base_element_type_unsafe(ptr) ?: return null), memScope)
    }
    inline fun getArrayElementTypeNoTypeQual(): TypeApi? {
        return Type((clang_Type_get_array_element_type_no_type_qual(ptr) ?: return null), memScope)
    }
    inline fun getPointeeOrArrayElementType(): TypeApi? {
        return Type((clang_Type_get_pointee_or_array_element_type(ptr) ?: return null), memScope)
    }
    inline fun getPointeeType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_pointee_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getUnqualifiedDesugaredType(): TypeApi? {
        return Type((clang_Type_get_unqualified_desugared_type(ptr) ?: return null), memScope)
    }
    inline fun isSignedIntegerType(): Boolean {
        return clang_Type_is_signed_integer_type(ptr)
    }
    inline fun isUnsignedIntegerType(): Boolean {
        return clang_Type_is_unsigned_integer_type(ptr)
    }
    inline fun isSignedIntegerOrEnumerationType(): Boolean {
        return clang_Type_is_signed_integer_or_enumeration_type(ptr)
    }
    inline fun isUnsignedIntegerOrEnumerationType(): Boolean {
        return clang_Type_is_unsigned_integer_or_enumeration_type(ptr)
    }
    inline fun isFixedPointType(): Boolean {
        return clang_Type_is_fixed_point_type(ptr)
    }
    inline fun isFixedPointOrIntegerType(): Boolean {
        return clang_Type_is_fixed_point_or_integer_type(ptr)
    }
    inline fun isConvertibleToFixedPointType(): Boolean {
        return clang_Type_is_convertible_to_fixed_point_type(ptr)
    }
    inline fun isSaturatedFixedPointType(): Boolean {
        return clang_Type_is_saturated_fixed_point_type(ptr)
    }
    inline fun isUnsaturatedFixedPointType(): Boolean {
        return clang_Type_is_unsaturated_fixed_point_type(ptr)
    }
    inline fun isSignedFixedPointType(): Boolean {
        return clang_Type_is_signed_fixed_point_type(ptr)
    }
    inline fun isUnsignedFixedPointType(): Boolean {
        return clang_Type_is_unsigned_fixed_point_type(ptr)
    }
    inline fun isConstantSizeType(): Boolean {
        return clang_Type_is_constant_size_type(ptr)
    }
    inline fun isSpecifierType(): Boolean {
        return clang_Type_is_specifier_type(ptr)
    }
    inline fun getLinkage(): Linkage {
        return LinkageCompanionfromValue(clang_Type_get_linkage(ptr))
    }
    inline fun getVisibility(): Visibility {
        return VisibilityCompanionfromValue(clang_Type_get_visibility(ptr))
    }
    inline fun isVisibilityExplicit(): Boolean {
        return clang_Type_is_visibility_explicit(ptr)
    }
    inline fun isLinkageValid(): Boolean {
        return clang_Type_is_linkage_valid(ptr)
    }
    inline fun canHaveNullability(ResultIfUnknown: Boolean = true): Boolean {
        return clang_Type_can_have_nullability(ptr, ResultIfUnknown)
    }
    inline fun acceptsObjCTypeParams(): Boolean {
        return clang_Type_accepts_obj_c_type_params(ptr)
    }
    inline fun getTypeClassName(): String? {
        val str: CPointer<ByteVar>? = clang_Type_get_type_class_name(ptr)
        val ret: String? = str?.toKString()
        return ret
    }
    inline fun getCanonicalTypeInternal(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_Type_get_canonical_type_internal(ptr, retValue.ptr)
        return retValue
    }
    inline fun dump(): Unit {
        return clang_Type_dump(ptr)
    }
    inline fun getElementType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_ArrayType_get_element_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun getSizeModifier(): ArraySizeModifier {
        return ArraySizeModifierCompanionfromValue(clang_ArrayType_get_size_modifier(ptr))
    }
    inline fun getIndexTypeCVRQualifiers(): UInt {
        return clang_ArrayType_get_index_type_cvr_qualifiers(ptr)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_ArrayType_size_of()
            }

        val align: Int
            inline get() {
                return clang_ArrayType_align_of()
            }

        inline fun MemScope.classof(T: TypeApi?): Boolean {
            return clang_ArrayType_classof(T?.ptr)
        }
        fun MemScope.ArrayType_Holder(): ArrayType {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return ArrayType(memory, this)
        }
    }
    inline fun asType(): Type {
        return Type(clang_ArrayType_as_clang_Type(ptr)!!, memScope)
    }
    inline fun asConstantArrayType(): ConstantArrayType? {
        val raw: COpaquePointer = (clang_ArrayType_dyncast_clang_ConstantArrayType(ptr) ?: return null)
        return ConstantArrayType(raw, memScope)
    }
}

// END KRAPPER GEN for clang::ArrayType


