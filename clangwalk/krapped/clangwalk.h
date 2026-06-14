#ifndef __CLANGWALK__
    #define __CLANGWALK__

    #include <stdlib.h>
    #include <stdint.h>
    #include <stdbool.h>
    #include <stddef.h>

    #ifdef __cplusplus
        extern "C" {
    #endif //__cplusplus

    // BEGIN KRAPPER GEN for clang::NamedDecl

    const char* clang_NamedDecl_get_name_as_string(void* thiz);

    const char* clang_NamedDecl_get_qualified_name_as_string(void* thiz);

    bool clang_NamedDecl_declaration_replaces(void* thiz, void* OldD, bool IsKnownNewer);

    bool clang_NamedDecl_has_linkage(void* thiz);

    bool clang_NamedDecl_is_cxx_class_member(void* thiz);

    bool clang_NamedDecl_is_cxx_instance_member(void* thiz);

    unsigned char clang_NamedDecl_get_linkage_internal(void* thiz);

    unsigned char clang_NamedDecl_get_formal_linkage(void* thiz);

    bool clang_NamedDecl_has_external_formal_linkage(void* thiz);

    bool clang_NamedDecl_is_externally_visible(void* thiz);

    bool clang_NamedDecl_is_externally_declarable(void* thiz);

    unsigned int clang_NamedDecl_get_visibility(void* thiz);

    bool clang_NamedDecl_is_linkage_valid(void* thiz);

    bool clang_NamedDecl_has_linkage_been_computed(void* thiz);

    void* clang_NamedDecl_get_underlying_decl(void* thiz);

    void* clang_NamedDecl_get_most_recent_decl(void* thiz);

    unsigned int clang_NamedDecl_get_obj_cf_string_formatting_family(void* thiz);

    bool clang_NamedDecl_classof(void* D);

    bool clang_NamedDecl_classof_kind(unsigned int K);

    const char* clang_NamedDecl_get_name(void* thiz);

    int clang_NamedDecl_size_of();

    int clang_NamedDecl_align_of();

    void* clang_NamedDecl_as_clang_Decl(void* p);

    void* clang_NamedDecl_dyncast_clang_TypeDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_TagDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_ValueDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_DeclaratorDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_FieldDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_RecordDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_FunctionDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_CXXMethodDecl(void* p);

    void* clang_NamedDecl_dyncast_clang_CXXRecordDecl(void* p);


    // END KRAPPER GEN for clang::NamedDecl


    // BEGIN KRAPPER GEN for clang::TypeDecl

    bool clang_TypeDecl_classof(void* D);

    bool clang_TypeDecl_classof_kind(unsigned int K);

    int clang_TypeDecl_size_of();

    int clang_TypeDecl_align_of();

    void* clang_TypeDecl_as_clang_NamedDecl(void* p);

    void* clang_TypeDecl_as_clang_Decl(void* p);

    void* clang_TypeDecl_dyncast_clang_TagDecl(void* p);

    void* clang_TypeDecl_dyncast_clang_RecordDecl(void* p);

    void* clang_TypeDecl_dyncast_clang_CXXRecordDecl(void* p);


    // END KRAPPER GEN for clang::TypeDecl


    // BEGIN KRAPPER GEN for clang::TagDecl

    void* clang_TagDecl_get_canonical_decl(void* thiz);

    bool clang_TagDecl_is_this_declaration_a_definition(void* thiz);

    bool clang_TagDecl_is_complete_definition(void* thiz);

    void clang_TagDecl_set_complete_definition(void* thiz, bool V);

    bool clang_TagDecl_is_complete_definition_required(void* thiz);

    void clang_TagDecl_set_complete_definition_required(void* thiz, bool V);

    bool clang_TagDecl_is_being_defined(void* thiz);

    bool clang_TagDecl_is_embedded_in_declarator(void* thiz);

    void clang_TagDecl_set_embedded_in_declarator(void* thiz, bool isInDeclarator);

    bool clang_TagDecl_is_free_standing(void* thiz);

    void clang_TagDecl_set_free_standing(void* thiz, bool isFreeStanding);

    bool clang_TagDecl_is_dependent_type(void* thiz);

    bool clang_TagDecl_is_this_declaration_a_demoted_definition(void* thiz);

    void clang_TagDecl_demote_this_definition_to_declaration(void* thiz);

    void clang_TagDecl_start_definition(void* thiz);

    const void* clang_TagDecl_get_definition(void* thiz);

    const void* clang_TagDecl_get_definition_or_self(void* thiz);

    bool clang_TagDecl_is_entity_being_defined(void* thiz);

    int clang_TagDecl_get_tag_kind(void* thiz);

    void clang_TagDecl_set_tag_kind(void* thiz, int TK);

    bool clang_TagDecl_is_struct(void* thiz);

    bool clang_TagDecl_is_interface(void* thiz);

    bool clang_TagDecl_is_class(void* thiz);

    bool clang_TagDecl_is_union(void* thiz);

    bool clang_TagDecl_is_enum(void* thiz);

    bool clang_TagDecl_is_structure_or_class(void* thiz);

    bool clang_TagDecl_has_name_for_linkage(void* thiz);

    unsigned int clang_TagDecl_get_num_template_parameter_lists(void* thiz);

    bool clang_TagDecl_classof(void* D);

    bool clang_TagDecl_classof_kind(unsigned int K);

    void* clang_TagDecl_cast_to_decl_context(void* D);

    void* clang_TagDecl_cast_from_decl_context(void* DC);

    const char* clang_TagDecl_get_kind_name(void* thiz);

    int clang_TagDecl_size_of();

    int clang_TagDecl_align_of();

    void* clang_TagDecl_as_clang_TypeDecl(void* p);

    void* clang_TagDecl_as_clang_DeclContext(void* p);

    void* clang_TagDecl_as_clang_NamedDecl(void* p);

    void* clang_TagDecl_as_clang_Decl(void* p);

    void* clang_TagDecl_dyncast_clang_RecordDecl(void* p);

    void* clang_TagDecl_dyncast_clang_CXXRecordDecl(void* p);


    // END KRAPPER GEN for clang::TagDecl


    // BEGIN KRAPPER GEN for clang::QualType

    void* clang_QualType_new(void* location);

    unsigned int clang_QualType_get_local_fast_qualifiers(void* thiz);

    void clang_QualType_set_local_fast_qualifiers(void* thiz, unsigned int Quals);

    bool clang_QualType_use_excess_precision(void* thiz, void* Ctx);

    const void* clang_QualType_get_as_opaque_ptr(void* thiz);

    void clang_QualType_get_from_opaque_ptr(const void* Ptr, void* ret_value);

    bool clang_QualType_is_canonical(void* thiz);

    bool clang_QualType_is_canonical_as_param(void* thiz);

    bool clang_QualType_is_null(void* thiz);

    bool clang_QualType_is_referenceable(void* thiz);

    bool clang_QualType_is_local_const_qualified(void* thiz);

    bool clang_QualType_is_const_qualified(void* thiz);

    bool clang_QualType_is_constant_storage(void* thiz, void* Ctx, bool ExcludeCtor, bool ExcludeDtor);

    bool clang_QualType_is_local_restrict_qualified(void* thiz);

    bool clang_QualType_is_restrict_qualified(void* thiz);

    bool clang_QualType_is_local_volatile_qualified(void* thiz);

    bool clang_QualType_is_volatile_qualified(void* thiz);

    bool clang_QualType_has_local_qualifiers(void* thiz);

    bool clang_QualType_has_qualifiers(void* thiz);

    bool clang_QualType_has_local_non_fast_qualifiers(void* thiz);

    unsigned int clang_QualType_get_local_cvr_qualifiers(void* thiz);

    unsigned int clang_QualType_get_cvr_qualifiers(void* thiz);

    bool clang_QualType_is_constant(void* thiz, void* Ctx);

    bool clang_QualType_is_pod_type(void* thiz, void* Context);

    bool clang_QualType_is_cxx98pod_type(void* thiz, void* Context);

    bool clang_QualType_is_cxx11pod_type(void* thiz, void* Context);

    bool clang_QualType_is_trivial_type(void* thiz, void* Context);

    bool clang_QualType_is_trivially_copyable_type(void* thiz, void* Context);

    bool clang_QualType_is_bitwise_cloneable_type(void* thiz, void* Context);

    bool clang_QualType_is_trivially_copy_constructible_type(void* thiz, void* Context);

    bool clang_QualType_may_be_dynamic_class(void* thiz);

    bool clang_QualType_may_be_not_dynamic_class(void* thiz);

    bool clang_QualType_is_web_assembly_reference_type(void* thiz);

    bool clang_QualType_is_web_assembly_externref_type(void* thiz);

    bool clang_QualType_is_web_assembly_funcref_type(void* thiz);

    void clang_QualType_add_const(void* thiz);

    void clang_QualType_with_const(void* thiz, void* ret_value);

    void clang_QualType_add_volatile(void* thiz);

    void clang_QualType_with_volatile(void* thiz, void* ret_value);

    void clang_QualType_add_restrict(void* thiz);

    void clang_QualType_with_restrict(void* thiz, void* ret_value);

    void clang_QualType_with_cvr_qualifiers(void* thiz, unsigned int CVR, void* ret_value);

    void clang_QualType_add_fast_qualifiers(void* thiz, unsigned int TQs);

    void clang_QualType_remove_local_const(void* thiz);

    void clang_QualType_remove_local_volatile(void* thiz);

    void clang_QualType_remove_local_restrict(void* thiz);

    void clang_QualType_remove_local_fast_qualifiers(void* thiz);

    void clang_QualType_remove_local_fast_qualifiers__unsigned_int(void* thiz, unsigned int Mask);

    void clang_QualType_with_fast_qualifiers(void* thiz, unsigned int TQs, void* ret_value);

    void clang_QualType_with_exact_local_fast_qualifiers(void* thiz, unsigned int TQs, void* ret_value);

    void clang_QualType_without_local_fast_qualifiers(void* thiz, void* ret_value);

    void clang_QualType_get_canonical_type(void* thiz, void* ret_value);

    void clang_QualType_get_local_unqualified_type(void* thiz, void* ret_value);

    void clang_QualType_get_unqualified_type(void* thiz, void* ret_value);

    bool clang_QualType_is_more_qualified_than(void* thiz, void* Other, void* Ctx);

    bool clang_QualType_is_at_least_as_qualified_as(void* thiz, void* Other, void* Ctx);

    void clang_QualType_get_non_reference_type(void* thiz, void* ret_value);

    void clang_QualType_get_non_l_value_expr_type(void* thiz, void* Context, void* ret_value);

    void clang_QualType_get_non_pack_expansion_type(void* thiz, void* ret_value);

    void clang_QualType_get_desugared_type(void* thiz, void* Context, void* ret_value);

    void clang_QualType_get_single_step_desugared_type(void* thiz, void* Context, void* ret_value);

    void clang_QualType_ignore_parens(void* thiz, void* ret_value);

    const char* _clang_QualType_get_as_string(void* thiz);

    void clang_QualType_dump(void* thiz, const char* s);

    void _clang_QualType_dump(void* thiz);

    bool clang_QualType_has_address_space(void* thiz);

    unsigned int clang_QualType_get_address_space(void* thiz);

    bool clang_QualType_is_address_space_overlapping(void* thiz, void* T, void* Ctx);

    unsigned int clang_QualType_get_obj_cgc_attr(void* thiz);

    bool clang_QualType_is_obj_cgc_weak(void* thiz);

    bool clang_QualType_is_obj_cgc_strong(void* thiz);

    unsigned int clang_QualType_get_obj_c_lifetime(void* thiz);

    bool clang_QualType_has_non_trivial_obj_c_lifetime(void* thiz);

    bool clang_QualType_has_strong_or_weak_obj_c_lifetime(void* thiz);

    bool clang_QualType_is_non_weak_in_mrr_with_obj_c_weak(void* thiz, void* Context);

    bool clang_QualType_has_address_discriminated_pointer_auth(void* thiz);

    unsigned int clang_QualType_is_non_trivial_to_primitive_default_initialize(void* thiz);

    unsigned int clang_QualType_is_non_trivial_to_primitive_copy(void* thiz);

    unsigned int clang_QualType_is_non_trivial_to_primitive_destructive_move(void* thiz);

    unsigned int clang_QualType_is_destructed_type(void* thiz);

    bool clang_QualType_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz);

    bool clang_QualType_has_non_trivial_to_primitive_destruct_c_union(void* thiz);

    bool clang_QualType_has_non_trivial_to_primitive_copy_c_union(void* thiz);

    bool clang_QualType_is_c_forbidden_l_value_type(void* thiz);

    void clang_QualType_subst_obj_c_member_type(void* thiz, void* objectType, void* dc, int context, void* ret_value);

    void clang_QualType_strip_obj_c_kind_of_type(void* thiz, void* ctx, void* ret_value);

    void clang_QualType_get_atomic_unqualified_type(void* thiz, void* ret_value);

    int clang_QualType_size_of();

    int clang_QualType_align_of();


    // END KRAPPER GEN for clang::QualType


    // BEGIN KRAPPER GEN for clang::ValueDecl

    void clang_ValueDecl_get_type(void* thiz, void* ret_value);

    void clang_ValueDecl_set_type(void* thiz, void* newType);

    bool clang_ValueDecl_is_weak(void* thiz);

    bool clang_ValueDecl_is_init_capture(void* thiz);

    bool clang_ValueDecl_is_parameter_pack(void* thiz);

    bool clang_ValueDecl_classof(void* D);

    bool clang_ValueDecl_classof_kind(unsigned int K);

    int clang_ValueDecl_size_of();

    int clang_ValueDecl_align_of();

    void* clang_ValueDecl_as_clang_NamedDecl(void* p);

    void* clang_ValueDecl_as_clang_Decl(void* p);

    void* clang_ValueDecl_dyncast_clang_DeclaratorDecl(void* p);

    void* clang_ValueDecl_dyncast_clang_FieldDecl(void* p);

    void* clang_ValueDecl_dyncast_clang_FunctionDecl(void* p);

    void* clang_ValueDecl_dyncast_clang_CXXMethodDecl(void* p);


    // END KRAPPER GEN for clang::ValueDecl


    // BEGIN KRAPPER GEN for clang::DeclaratorDecl

    unsigned int clang_DeclaratorDecl_get_num_template_parameter_lists(void* thiz);

    bool clang_DeclaratorDecl_classof(void* D);

    bool clang_DeclaratorDecl_classof_kind(unsigned int K);

    int clang_DeclaratorDecl_size_of();

    int clang_DeclaratorDecl_align_of();

    void* clang_DeclaratorDecl_as_clang_ValueDecl(void* p);

    void* clang_DeclaratorDecl_as_clang_NamedDecl(void* p);

    void* clang_DeclaratorDecl_as_clang_Decl(void* p);

    void* clang_DeclaratorDecl_dyncast_clang_FieldDecl(void* p);

    void* clang_DeclaratorDecl_dyncast_clang_FunctionDecl(void* p);

    void* clang_DeclaratorDecl_dyncast_clang_CXXMethodDecl(void* p);


    // END KRAPPER GEN for clang::DeclaratorDecl


    // BEGIN KRAPPER GEN for clang::FieldDecl

    unsigned int clang_FieldDecl_get_field_index(void* thiz);

    bool clang_FieldDecl_is_mutable(void* thiz);

    bool clang_FieldDecl_is_bit_field(void* thiz);

    bool clang_FieldDecl_is_unnamed_bit_field(void* thiz);

    bool clang_FieldDecl_is_anonymous_struct_or_union(void* thiz);

    bool clang_FieldDecl_has_constant_integer_bit_width(void* thiz);

    unsigned int clang_FieldDecl_get_bit_width_value(void* thiz);

    void clang_FieldDecl_remove_bit_width(void* thiz);

    bool clang_FieldDecl_is_zero_length_bit_field(void* thiz);

    bool clang_FieldDecl_is_zero_size(void* thiz, void* Ctx);

    bool clang_FieldDecl_is_potentially_overlapping(void* thiz);

    unsigned int clang_FieldDecl_get_in_class_init_style(void* thiz);

    bool clang_FieldDecl_has_in_class_initializer(void* thiz);

    bool clang_FieldDecl_has_non_null_in_class_initializer(void* thiz);

    void* clang_FieldDecl_find_counted_by_field(void* thiz);

    void clang_FieldDecl_remove_in_class_initializer(void* thiz);

    bool clang_FieldDecl_has_captured_vla_type(void* thiz);

    void* clang_FieldDecl_get_parent(void* thiz);

    void* clang_FieldDecl_get_canonical_decl(void* thiz);

    bool clang_FieldDecl_classof(void* D);

    bool clang_FieldDecl_classof_kind(unsigned int K);

    int clang_FieldDecl_size_of();

    int clang_FieldDecl_align_of();

    void* clang_FieldDecl_as_clang_DeclaratorDecl(void* p);

    void* clang_FieldDecl_as_clang_ValueDecl(void* p);

    void* clang_FieldDecl_as_clang_NamedDecl(void* p);

    void* clang_FieldDecl_as_clang_Decl(void* p);


    // END KRAPPER GEN for clang::FieldDecl


    // BEGIN KRAPPER GEN for clang::RecordDecl

    void* clang_RecordDecl_get_previous_decl(void* thiz);

    void* clang_RecordDecl_get_most_recent_decl(void* thiz);

    bool clang_RecordDecl_has_flexible_array_member(void* thiz);

    void clang_RecordDecl_set_has_flexible_array_member(void* thiz, bool V);

    bool clang_RecordDecl_is_anonymous_struct_or_union(void* thiz);

    void clang_RecordDecl_set_anonymous_struct_or_union(void* thiz, bool Anon);

    bool clang_RecordDecl_has_object_member(void* thiz);

    void clang_RecordDecl_set_has_object_member(void* thiz, bool val);

    bool clang_RecordDecl_has_volatile_member(void* thiz);

    void clang_RecordDecl_set_has_volatile_member(void* thiz, bool val);

    bool clang_RecordDecl_has_loaded_fields_from_external_storage(void* thiz);

    void clang_RecordDecl_set_has_loaded_fields_from_external_storage(void* thiz, bool val);

    bool clang_RecordDecl_is_non_trivial_to_primitive_default_initialize(void* thiz);

    void clang_RecordDecl_set_non_trivial_to_primitive_default_initialize(void* thiz, bool V);

    bool clang_RecordDecl_is_non_trivial_to_primitive_copy(void* thiz);

    void clang_RecordDecl_set_non_trivial_to_primitive_copy(void* thiz, bool V);

    bool clang_RecordDecl_is_non_trivial_to_primitive_destroy(void* thiz);

    void clang_RecordDecl_set_non_trivial_to_primitive_destroy(void* thiz, bool V);

    bool clang_RecordDecl_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz);

    void clang_RecordDecl_set_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz, bool V);

    bool clang_RecordDecl_has_non_trivial_to_primitive_destruct_c_union(void* thiz);

    void clang_RecordDecl_set_has_non_trivial_to_primitive_destruct_c_union(void* thiz, bool V);

    bool clang_RecordDecl_has_non_trivial_to_primitive_copy_c_union(void* thiz);

    void clang_RecordDecl_set_has_non_trivial_to_primitive_copy_c_union(void* thiz, bool V);

    bool clang_RecordDecl_has_uninitialized_explicit_init_fields(void* thiz);

    void clang_RecordDecl_set_has_uninitialized_explicit_init_fields(void* thiz, bool V);

    bool clang_RecordDecl_can_pass_in_registers(void* thiz);

    int clang_RecordDecl_get_arg_passing_restrictions(void* thiz);

    void clang_RecordDecl_set_arg_passing_restrictions(void* thiz, int Kind);

    bool clang_RecordDecl_is_param_destroyed_in_callee(void* thiz);

    void clang_RecordDecl_set_param_destroyed_in_callee(void* thiz, bool V);

    bool clang_RecordDecl_is_randomized(void* thiz);

    void clang_RecordDecl_set_is_randomized(void* thiz, bool V);

    bool clang_RecordDecl_is_lambda(void* thiz);

    bool clang_RecordDecl_is_captured_record(void* thiz);

    void clang_RecordDecl_set_captured_record(void* thiz);

    const void* clang_RecordDecl_get_definition(void* thiz);

    const void* clang_RecordDecl_get_definition_or_self(void* thiz);

    bool clang_RecordDecl_is_or_contains_union(void* thiz);

    bool clang_RecordDecl_field_empty(void* thiz);

    unsigned int clang_RecordDecl_get_num_fields(void* thiz);

    bool clang_RecordDecl_noload_field_empty(void* thiz);

    void clang_RecordDecl_complete_definition(void* thiz);

    bool clang_RecordDecl_classof(void* D);

    bool clang_RecordDecl_classof_kind(unsigned int K);

    bool clang_RecordDecl_is_ms_struct(void* thiz, void* C);

    bool clang_RecordDecl_may_insert_extra_padding(void* thiz, bool EmitRemark);

    void* clang_RecordDecl_find_first_named_data_member(void* thiz);

    unsigned int clang_RecordDecl_get_odr_hash(void* thiz);

    int clang_RecordDecl_size_of();

    int clang_RecordDecl_align_of();

    void* clang_RecordDecl_as_clang_TagDecl(void* p);

    void* clang_RecordDecl_as_clang_TypeDecl(void* p);

    void* clang_RecordDecl_as_clang_DeclContext(void* p);

    void* clang_RecordDecl_as_clang_NamedDecl(void* p);

    void* clang_RecordDecl_as_clang_Decl(void* p);

    void* clang_RecordDecl_dyncast_clang_CXXRecordDecl(void* p);


    // END KRAPPER GEN for clang::RecordDecl


    // BEGIN KRAPPER GEN for clang::CXXBaseSpecifier

    void* clang_CXXBaseSpecifier_new(void* location);

    bool clang_CXXBaseSpecifier_is_virtual(void* thiz);

    bool clang_CXXBaseSpecifier_is_base_of_class(void* thiz);

    bool clang_CXXBaseSpecifier_is_pack_expansion(void* thiz);

    bool clang_CXXBaseSpecifier_get_inherit_constructors(void* thiz);

    void clang_CXXBaseSpecifier_set_inherit_constructors(void* thiz, bool Inherit);

    unsigned char clang_CXXBaseSpecifier_get_access_specifier(void* thiz);

    unsigned char clang_CXXBaseSpecifier_get_access_specifier_as_written(void* thiz);

    void clang_CXXBaseSpecifier_get_type(void* thiz, void* ret_value);

    int clang_CXXBaseSpecifier_size_of();

    int clang_CXXBaseSpecifier_align_of();


    // END KRAPPER GEN for clang::CXXBaseSpecifier


    // BEGIN KRAPPER GEN for clang::FunctionDecl

    bool clang_FunctionDecl_has_body(void* thiz, void* Definition);

    bool _clang_FunctionDecl_has_body(void* thiz);

    bool clang_FunctionDecl_has_trivial_body(void* thiz);

    bool clang_FunctionDecl_is_defined(void* thiz, void* Definition, bool CheckForPendingFriendDefinition);

    bool _clang_FunctionDecl_is_defined(void* thiz);

    void* clang_FunctionDecl_get_definition(void* thiz);

    bool clang_FunctionDecl_is_this_declaration_a_definition(void* thiz);

    bool clang_FunctionDecl_is_this_declaration_instantiated_from_a_friend_definition(void* thiz);

    bool clang_FunctionDecl_does_this_declaration_have_a_body(void* thiz);

    void clang_FunctionDecl_set_lazy_body(void* thiz, unsigned long Offset);

    bool clang_FunctionDecl_is_variadic(void* thiz);

    bool clang_FunctionDecl_is_virtual_as_written(void* thiz);

    void clang_FunctionDecl_set_virtual_as_written(void* thiz, bool V);

    bool clang_FunctionDecl_is_pure_virtual(void* thiz);

    void clang_FunctionDecl_set_is_pure_virtual(void* thiz, bool P);

    bool clang_FunctionDecl_is_late_template_parsed(void* thiz);

    void clang_FunctionDecl_set_late_template_parsed(void* thiz, bool ILT);

    bool clang_FunctionDecl_is_instantiated_from_member_template(void* thiz);

    void clang_FunctionDecl_set_instantiated_from_member_template(void* thiz, bool Val);

    bool clang_FunctionDecl_is_trivial(void* thiz);

    void clang_FunctionDecl_set_trivial(void* thiz, bool IT);

    bool clang_FunctionDecl_is_trivial_for_call(void* thiz);

    void clang_FunctionDecl_set_trivial_for_call(void* thiz, bool IT);

    bool clang_FunctionDecl_is_defaulted(void* thiz);

    void clang_FunctionDecl_set_defaulted(void* thiz, bool D);

    bool clang_FunctionDecl_is_explicitly_defaulted(void* thiz);

    void clang_FunctionDecl_set_explicitly_defaulted(void* thiz, bool ED);

    bool clang_FunctionDecl_is_user_provided(void* thiz);

    bool clang_FunctionDecl_is_ineligible_or_not_selected(void* thiz);

    void clang_FunctionDecl_set_ineligible_or_not_selected(void* thiz, bool II);

    bool clang_FunctionDecl_has_implicit_return_zero(void* thiz);

    void clang_FunctionDecl_set_has_implicit_return_zero(void* thiz, bool IRZ);

    bool clang_FunctionDecl_has_prototype(void* thiz);

    bool clang_FunctionDecl_has_written_prototype(void* thiz);

    void clang_FunctionDecl_set_has_written_prototype(void* thiz, bool P);

    bool clang_FunctionDecl_has_inherited_prototype(void* thiz);

    void clang_FunctionDecl_set_has_inherited_prototype(void* thiz, bool P);

    bool clang_FunctionDecl_is_constexpr(void* thiz);

    void clang_FunctionDecl_set_constexpr_kind(void* thiz, int CSK);

    int clang_FunctionDecl_get_constexpr_kind(void* thiz);

    bool clang_FunctionDecl_is_constexpr_specified(void* thiz);

    bool clang_FunctionDecl_is_consteval(void* thiz);

    void clang_FunctionDecl_set_body_contains_immediate_escalating_expressions(void* thiz, bool Set);

    bool clang_FunctionDecl_body_contains_immediate_escalating_expressions(void* thiz);

    bool clang_FunctionDecl_is_immediate_escalating(void* thiz);

    bool clang_FunctionDecl_is_immediate_function(void* thiz);

    bool clang_FunctionDecl_instantiation_is_pending(void* thiz);

    void clang_FunctionDecl_set_instantiation_is_pending(void* thiz, bool IC);

    bool clang_FunctionDecl_uses_seh_try(void* thiz);

    void clang_FunctionDecl_set_uses_seh_try(void* thiz, bool UST);

    bool clang_FunctionDecl_is_deleted(void* thiz);

    bool clang_FunctionDecl_is_deleted_as_written(void* thiz);

    void clang_FunctionDecl_set_deleted_as_written(void* thiz, bool D);

    bool clang_FunctionDecl_is_main(void* thiz);

    bool clang_FunctionDecl_is_msvcrt_entry_point(void* thiz);

    bool clang_FunctionDecl_is_reserved_global_placement_operator(void* thiz);

    bool clang_FunctionDecl_is_replaceable_global_allocation_function(void* thiz);

    bool clang_FunctionDecl_is_usable_as_global_allocation_function_in_constant_evaluation(void* thiz);

    bool clang_FunctionDecl_is_inline_builtin_declaration(void* thiz);

    bool clang_FunctionDecl_is_destroying_operator_delete(void* thiz);

    void clang_FunctionDecl_set_is_destroying_operator_delete(void* thiz, bool IsDestroyingDelete);

    bool clang_FunctionDecl_is_type_aware_operator_new_or_delete(void* thiz);

    void clang_FunctionDecl_set_is_type_aware_operator_new_or_delete(void* thiz, bool IsTypeAwareOperator);

    unsigned int clang_FunctionDecl_get_language_linkage(void* thiz);

    bool clang_FunctionDecl_is_extern_c(void* thiz);

    bool clang_FunctionDecl_is_in_extern_c_context(void* thiz);

    bool clang_FunctionDecl_is_in_extern_cxx_context(void* thiz);

    bool clang_FunctionDecl_is_global(void* thiz);

    bool clang_FunctionDecl_is_no_return(void* thiz);

    bool clang_FunctionDecl_is_analyzer_no_return(void* thiz);

    bool clang_FunctionDecl_has_skipped_body(void* thiz);

    void clang_FunctionDecl_set_has_skipped_body(void* thiz, bool Skipped);

    bool clang_FunctionDecl_will_have_body(void* thiz);

    void clang_FunctionDecl_set_will_have_body(void* thiz, bool V);

    bool clang_FunctionDecl_is_multi_version(void* thiz);

    void clang_FunctionDecl_set_is_multi_version(void* thiz, bool V);

    void clang_FunctionDecl_set_friend_constraint_refers_to_enclosing_template(void* thiz, bool V);

    bool clang_FunctionDecl_friend_constraint_refers_to_enclosing_template(void* thiz);

    bool clang_FunctionDecl_is_member_like_constrained_friend(void* thiz);

    int clang_FunctionDecl_get_multi_version_kind(void* thiz);

    bool clang_FunctionDecl_is_cpu_dispatch_multi_version(void* thiz);

    bool clang_FunctionDecl_is_cpu_specific_multi_version(void* thiz);

    bool clang_FunctionDecl_is_target_multi_version(void* thiz);

    bool clang_FunctionDecl_is_target_multi_version_default(void* thiz);

    bool clang_FunctionDecl_is_target_clones_multi_version(void* thiz);

    bool clang_FunctionDecl_is_target_version_multi_version(void* thiz);

    void clang_FunctionDecl_set_previous_declaration(void* thiz, void* PrevDecl);

    void* clang_FunctionDecl_get_canonical_decl(void* thiz);

    unsigned int clang_FunctionDecl_get_builtin_id(void* thiz, bool ConsiderWrapperFunctions);

    bool clang_FunctionDecl_param_empty(void* thiz);

    size_t clang_FunctionDecl_param_size(void* thiz);

    unsigned int clang_FunctionDecl_get_num_params(void* thiz);

    unsigned int clang_FunctionDecl_get_min_required_arguments(void* thiz);

    unsigned int clang_FunctionDecl_get_min_required_explicit_arguments(void* thiz);

    bool clang_FunctionDecl_has_cxx_explicit_function_object_parameter(void* thiz);

    unsigned int clang_FunctionDecl_get_num_non_object_params(void* thiz);

    bool clang_FunctionDecl_has_one_param_or_default_args(void* thiz);

    void clang_FunctionDecl_get_return_type(void* thiz, void* ret_value);

    void clang_FunctionDecl_get_declared_return_type(void* thiz, void* ret_value);

    unsigned int clang_FunctionDecl_get_exception_spec_type(void* thiz);

    void clang_FunctionDecl_get_call_result_type(void* thiz, void* ret_value);

    unsigned int clang_FunctionDecl_get_storage_class(void* thiz);

    void clang_FunctionDecl_set_storage_class(void* thiz, unsigned int SClass);

    bool clang_FunctionDecl_is_inline_specified(void* thiz);

    void clang_FunctionDecl_set_inline_specified(void* thiz, bool I);

    bool clang_FunctionDecl_uses_fp_intrin(void* thiz);

    void clang_FunctionDecl_set_uses_fp_intrin(void* thiz, bool I);

    void clang_FunctionDecl_set_implicitly_inline(void* thiz, bool I);

    bool clang_FunctionDecl_is_inlined(void* thiz);

    bool clang_FunctionDecl_is_inline_definition_externally_visible(void* thiz);

    bool clang_FunctionDecl_is_ms_extern_inline(void* thiz);

    bool clang_FunctionDecl_does_declaration_force_externally_visible_definition(void* thiz);

    bool clang_FunctionDecl_is_static(void* thiz);

    bool clang_FunctionDecl_is_overloaded_operator(void* thiz);

    int clang_FunctionDecl_get_overloaded_operator(void* thiz);

    const void* clang_FunctionDecl_get_instantiated_from_member_function(void* thiz);

    unsigned int clang_FunctionDecl_get_templated_kind(void* thiz);

    void clang_FunctionDecl_set_instantiation_of_member_function(void* thiz, void* FD, unsigned int TSK);

    void clang_FunctionDecl_set_instantiated_from_decl(void* thiz, void* FD);

    const void* clang_FunctionDecl_get_instantiated_from_decl(void* thiz);

    bool clang_FunctionDecl_is_function_template_specialization(void* thiz);

    bool clang_FunctionDecl_is_implicitly_instantiable(void* thiz);

    bool clang_FunctionDecl_is_template_instantiation(void* thiz);

    const void* clang_FunctionDecl_get_template_instantiation_pattern(void* thiz, bool ForDefinition);

    unsigned int clang_FunctionDecl_get_template_specialization_kind(void* thiz);

    unsigned int clang_FunctionDecl_get_template_specialization_kind_for_instantiation(void* thiz);

    void clang_FunctionDecl_set_template_specialization_kind(void* thiz, unsigned int TSK);

    bool clang_FunctionDecl_is_out_of_line(void* thiz);

    unsigned int clang_FunctionDecl_get_memory_function_kind(void* thiz);

    unsigned int clang_FunctionDecl_get_odr_hash(void* thiz);

    bool clang_FunctionDecl_classof(void* D);

    bool clang_FunctionDecl_classof_kind(unsigned int K);

    void* clang_FunctionDecl_cast_to_decl_context(void* D);

    void* clang_FunctionDecl_cast_from_decl_context(void* DC);

    bool clang_FunctionDecl_is_referenceable_kernel(void* thiz);

    int clang_FunctionDecl_size_of();

    int clang_FunctionDecl_align_of();

    void* clang_FunctionDecl_as_clang_DeclaratorDecl(void* p);

    void* clang_FunctionDecl_as_clang_DeclContext(void* p);

    void* clang_FunctionDecl_as_clang_ValueDecl(void* p);

    void* clang_FunctionDecl_as_clang_NamedDecl(void* p);

    void* clang_FunctionDecl_as_clang_Decl(void* p);

    void* clang_FunctionDecl_dyncast_clang_CXXMethodDecl(void* p);


    // END KRAPPER GEN for clang::FunctionDecl


    // BEGIN KRAPPER GEN for clang::CXXMethodDecl

    bool clang_CXXMethodDecl_is_static(void* thiz);

    bool clang_CXXMethodDecl_is_instance(void* thiz);

    bool clang_CXXMethodDecl_is_explicit_object_member_function(void* thiz);

    bool clang_CXXMethodDecl_is_implicit_object_member_function(void* thiz);

    bool clang_CXXMethodDecl_is_static_overloaded_operator(int OOK);

    bool clang_CXXMethodDecl_is_const(void* thiz);

    bool clang_CXXMethodDecl_is_volatile(void* thiz);

    bool clang_CXXMethodDecl_is_virtual(void* thiz);

    bool clang_CXXMethodDecl_is_copy_assignment_operator(void* thiz);

    bool clang_CXXMethodDecl_is_move_assignment_operator(void* thiz);

    void* clang_CXXMethodDecl_get_canonical_decl(void* thiz);

    void* clang_CXXMethodDecl_get_most_recent_decl(void* thiz);

    void clang_CXXMethodDecl_add_overridden_method(void* thiz, void* MD);

    unsigned int clang_CXXMethodDecl_size_overridden_methods(void* thiz);

    void clang_CXXMethodDecl_overridden_methods(void* thiz, void* ret_value);

    void* clang_CXXMethodDecl_get_parent(void* thiz);

    void clang_CXXMethodDecl_get_this_type(void* thiz, void* ret_value);

    void clang_CXXMethodDecl_get_function_object_parameter_reference_type(void* thiz, void* ret_value);

    void clang_CXXMethodDecl_get_function_object_parameter_type(void* thiz, void* ret_value);

    unsigned int clang_CXXMethodDecl_get_num_explicit_params(void* thiz);

    unsigned int clang_CXXMethodDecl_get_ref_qualifier(void* thiz);

    bool clang_CXXMethodDecl_has_inline_body(void* thiz);

    bool clang_CXXMethodDecl_is_lambda_static_invoker(void* thiz);

    void* clang_CXXMethodDecl_get_corresponding_method_in_class(void* thiz, void* RD, bool MayBeBase);

    void* clang_CXXMethodDecl_get_corresponding_method_declared_in_class(void* thiz, void* RD, bool MayBeBase);

    bool clang_CXXMethodDecl_classof(void* D);

    bool clang_CXXMethodDecl_classof_kind(unsigned int K);

    int clang_CXXMethodDecl_size_of();

    int clang_CXXMethodDecl_align_of();

    void* clang_CXXMethodDecl_as_clang_FunctionDecl(void* p);

    void* clang_CXXMethodDecl_as_clang_DeclaratorDecl(void* p);

    void* clang_CXXMethodDecl_as_clang_DeclContext(void* p);

    void* clang_CXXMethodDecl_as_clang_ValueDecl(void* p);

    void* clang_CXXMethodDecl_as_clang_NamedDecl(void* p);

    void* clang_CXXMethodDecl_as_clang_Decl(void* p);


    // END KRAPPER GEN for clang::CXXMethodDecl


    // BEGIN KRAPPER GEN for clang::CXXRecordDecl

    void* clang_CXXRecordDecl_get_canonical_decl(void* thiz);

    void* clang_CXXRecordDecl_get_previous_decl(void* thiz);

    void* clang_CXXRecordDecl_get_most_recent_decl(void* thiz);

    const void* clang_CXXRecordDecl_get_definition(void* thiz);

    const void* clang_CXXRecordDecl_get_definition_or_self(void* thiz);

    bool clang_CXXRecordDecl_has_definition(void* thiz);

    bool clang_CXXRecordDecl_is_dynamic_class(void* thiz);

    bool clang_CXXRecordDecl_may_be_dynamic_class(void* thiz);

    bool clang_CXXRecordDecl_may_be_non_dynamic_class(void* thiz);

    void clang_CXXRecordDecl_set_is_parsing_base_specifiers(void* thiz);

    bool clang_CXXRecordDecl_is_parsing_base_specifiers(void* thiz);

    unsigned int clang_CXXRecordDecl_get_odr_hash(void* thiz);

    unsigned int clang_CXXRecordDecl_get_num_bases(void* thiz);

    void clang_CXXRecordDecl_bases(void* thiz, void* ret_value);

    unsigned int clang_CXXRecordDecl_get_num_v_bases(void* thiz);

    void clang_CXXRecordDecl_vbases(void* thiz, void* ret_value);

    bool clang_CXXRecordDecl_has_any_dependent_bases(void* thiz);

    void clang_CXXRecordDecl_methods(void* thiz, void* ret_value);

    bool clang_CXXRecordDecl_has_friends(void* thiz);

    bool clang_CXXRecordDecl_defaulted_copy_constructor_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_defaulted_move_constructor_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_defaulted_destructor_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_has_simple_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_simple_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_simple_copy_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_simple_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_simple_destructor(void* thiz);

    bool clang_CXXRecordDecl_has_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_user_provided_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_needs_overload_resolution_for_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_implicit_copy_constructor_has_const_param(void* thiz);

    bool clang_CXXRecordDecl_has_copy_constructor_with_const_param(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_move_operation(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_move_constructor(void* thiz);

    void clang_CXXRecordDecl_set_implicit_copy_constructor_is_deleted(void* thiz);

    void clang_CXXRecordDecl_set_implicit_move_constructor_is_deleted(void* thiz);

    void clang_CXXRecordDecl_set_implicit_destructor_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_needs_overload_resolution_for_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_copy_assignment(void* thiz);

    void clang_CXXRecordDecl_set_implicit_copy_assignment_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_copy_assignment(void* thiz);

    bool clang_CXXRecordDecl_needs_overload_resolution_for_copy_assignment(void* thiz);

    bool clang_CXXRecordDecl_implicit_copy_assignment_has_const_param(void* thiz);

    bool clang_CXXRecordDecl_has_copy_assignment_with_const_param(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_move_assignment(void* thiz);

    void clang_CXXRecordDecl_set_implicit_move_assignment_is_deleted(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_needs_overload_resolution_for_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_user_declared_destructor(void* thiz);

    bool clang_CXXRecordDecl_needs_implicit_destructor(void* thiz);

    bool clang_CXXRecordDecl_needs_overload_resolution_for_destructor(void* thiz);

    bool clang_CXXRecordDecl_is_lambda(void* thiz);

    bool clang_CXXRecordDecl_is_generic_lambda(void* thiz);

    bool clang_CXXRecordDecl_lambda_is_default_constructible_and_assignable(void* thiz);

    const void* clang_CXXRecordDecl_get_lambda_call_operator(void* thiz);

    const void* clang_CXXRecordDecl_get_lambda_static_invoker(void* thiz);

    const void* clang_CXXRecordDecl_get_lambda_static_invoker__clang_CallingConv(void* thiz, unsigned int CC);

    unsigned int clang_CXXRecordDecl_get_lambda_capture_default(void* thiz);

    bool clang_CXXRecordDecl_is_captureless_lambda(void* thiz);

    unsigned int clang_CXXRecordDecl_capture_size(void* thiz);

    void clang_CXXRecordDecl_remove_conversion(void* thiz, void* Old);

    bool clang_CXXRecordDecl_is_aggregate(void* thiz);

    bool clang_CXXRecordDecl_has_in_class_initializer(void* thiz);

    bool clang_CXXRecordDecl_has_uninitialized_reference_member(void* thiz);

    bool clang_CXXRecordDecl_is_pod(void* thiz);

    bool clang_CXXRecordDecl_is_c_like(void* thiz);

    bool clang_CXXRecordDecl_is_empty(void* thiz);

    void clang_CXXRecordDecl_set_init_method(void* thiz, bool Val);

    bool clang_CXXRecordDecl_has_init_method(void* thiz);

    bool clang_CXXRecordDecl_has_private_fields(void* thiz);

    bool clang_CXXRecordDecl_has_protected_fields(void* thiz);

    bool clang_CXXRecordDecl_has_direct_fields(void* thiz);

    void* clang_CXXRecordDecl_get_standard_layout_base_with_fields(void* thiz);

    bool clang_CXXRecordDecl_is_polymorphic(void* thiz);

    bool clang_CXXRecordDecl_is_abstract(void* thiz);

    bool clang_CXXRecordDecl_is_standard_layout(void* thiz);

    bool clang_CXXRecordDecl_is_cxx11standard_layout(void* thiz);

    bool clang_CXXRecordDecl_has_mutable_fields(void* thiz);

    bool clang_CXXRecordDecl_has_variant_members(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_constexpr_non_copy_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_defaulted_default_constructor_is_constexpr(void* thiz);

    bool clang_CXXRecordDecl_has_constexpr_default_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_copy_constructor_for_call(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_copy_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_copy_constructor_for_call(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_move_constructor_for_call(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_move_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_move_constructor_for_call(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_copy_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_copy_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_move_assignment(void* thiz);

    bool clang_CXXRecordDecl_defaulted_destructor_is_constexpr(void* thiz);

    bool clang_CXXRecordDecl_has_constexpr_destructor(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_destructor(void* thiz);

    bool clang_CXXRecordDecl_has_trivial_destructor_for_call(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_destructor(void* thiz);

    bool clang_CXXRecordDecl_has_non_trivial_destructor_for_call(void* thiz);

    void clang_CXXRecordDecl_set_has_trivial_special_member_for_call(void* thiz);

    bool clang_CXXRecordDecl_allow_const_default_init(void* thiz);

    bool clang_CXXRecordDecl_has_irrelevant_destructor(void* thiz);

    bool clang_CXXRecordDecl_has_non_literal_type_fields_or_bases(void* thiz);

    bool clang_CXXRecordDecl_has_inherited_constructor(void* thiz);

    bool clang_CXXRecordDecl_has_inherited_assignment(void* thiz);

    bool clang_CXXRecordDecl_is_trivially_copyable(void* thiz);

    bool clang_CXXRecordDecl_is_trivially_copy_constructible(void* thiz);

    bool clang_CXXRecordDecl_is_trivial(void* thiz);

    bool clang_CXXRecordDecl_is_literal(void* thiz);

    bool clang_CXXRecordDecl_is_structural(void* thiz);

    void clang_CXXRecordDecl_added_eligible_special_member_function(void* thiz, void* MD, unsigned int SMKind);

    const void* clang_CXXRecordDecl_get_instantiated_from_member_class(void* thiz);

    void clang_CXXRecordDecl_set_instantiation_of_member_class(void* thiz, void* RD, unsigned int TSK);

    unsigned int clang_CXXRecordDecl_get_template_specialization_kind(void* thiz);

    void clang_CXXRecordDecl_set_template_specialization_kind(void* thiz, unsigned int TSK);

    void* clang_CXXRecordDecl_get_template_instantiation_pattern(void* thiz);

    bool clang_CXXRecordDecl_has_deleted_destructor(void* thiz);

    bool clang_CXXRecordDecl_is_any_destructor_no_return(void* thiz);

    bool clang_CXXRecordDecl_is_hlsl_intangible(void* thiz);

    void* clang_CXXRecordDecl_is_local_class(void* thiz);

    bool clang_CXXRecordDecl_is_current_instantiation(void* thiz, void* CurContext);

    bool clang_CXXRecordDecl_is_derived_from(void* thiz, void* Base);

    bool clang_CXXRecordDecl_is_virtually_derived_from(void* thiz, void* Base);

    bool clang_CXXRecordDecl_is_provably_not_derived_from(void* thiz, void* Base);

    void clang_CXXRecordDecl_view_inheritance(void* thiz, void* Context);

    unsigned char clang_CXXRecordDecl_merge_access(unsigned char PathAccess, unsigned char DeclAccess);

    void clang_CXXRecordDecl_finished_defaulted_or_deleted_member(void* thiz, void* MD);

    void clang_CXXRecordDecl_set_trivial_for_call_flags(void* thiz, void* MD);

    void clang_CXXRecordDecl_complete_definition(void* thiz);

    bool clang_CXXRecordDecl_may_be_abstract(void* thiz);

    bool clang_CXXRecordDecl_is_effectively_final(void* thiz);

    unsigned int clang_CXXRecordDecl_get_lambda_mangling_number(void* thiz);

    bool clang_CXXRecordDecl_has_known_lambda_internal_linkage(void* thiz);

    const void* clang_CXXRecordDecl_get_lambda_context_decl(void* thiz);

    unsigned int clang_CXXRecordDecl_get_lambda_index_in_context(void* thiz);

    unsigned int clang_CXXRecordDecl_get_device_lambda_mangling_number(void* thiz);

    int clang_CXXRecordDecl_get_ms_inheritance_model(void* thiz);

    int clang_CXXRecordDecl_calculate_inheritance_model(void* thiz);

    bool clang_CXXRecordDecl_null_field_offset_is_zero(void* thiz);

    bool clang_CXXRecordDecl_is_dependent_lambda(void* thiz);

    bool clang_CXXRecordDecl_is_never_dependent_lambda(void* thiz);

    unsigned int clang_CXXRecordDecl_get_lambda_dependency_kind(void* thiz);

    void clang_CXXRecordDecl_set_lambda_dependency_kind(void* thiz, unsigned int Kind);

    void clang_CXXRecordDecl_set_lambda_is_generic(void* thiz, bool IsGeneric);

    bool clang_CXXRecordDecl_is_injected_class_name(void* thiz);

    bool clang_CXXRecordDecl_has_injected_class_type(void* thiz);

    bool clang_CXXRecordDecl_is_interface_like(void* thiz);

    bool clang_CXXRecordDecl_classof(void* D);

    bool clang_CXXRecordDecl_classof_kind(unsigned int K);

    void clang_CXXRecordDecl_mark_abstract(void* thiz);

    int clang_CXXRecordDecl_size_of();

    int clang_CXXRecordDecl_align_of();

    void* clang_CXXRecordDecl_as_clang_RecordDecl(void* p);

    void* clang_CXXRecordDecl_as_clang_TagDecl(void* p);

    void* clang_CXXRecordDecl_as_clang_TypeDecl(void* p);

    void* clang_CXXRecordDecl_as_clang_DeclContext(void* p);

    void* clang_CXXRecordDecl_as_clang_NamedDecl(void* p);

    void* clang_CXXRecordDecl_as_clang_Decl(void* p);


    // END KRAPPER GEN for clang::CXXRecordDecl


    // BEGIN KRAPPER GEN for clang::TranslationUnitDecl

    const void* clang_TranslationUnitDecl_get_ast_context(void* thiz);

    void* clang_TranslationUnitDecl_create(void* C);

    bool clang_TranslationUnitDecl_classof(void* D);

    bool clang_TranslationUnitDecl_classof_kind(unsigned int K);

    void* clang_TranslationUnitDecl_cast_to_decl_context(void* D);

    void* clang_TranslationUnitDecl_cast_from_decl_context(void* DC);

    void* clang_TranslationUnitDecl_get_canonical_decl(void* thiz);

    int clang_TranslationUnitDecl_size_of();

    int clang_TranslationUnitDecl_align_of();

    void* clang_TranslationUnitDecl_as_clang_Decl(void* p);

    void* clang_TranslationUnitDecl_as_clang_DeclContext(void* p);


    // END KRAPPER GEN for clang::TranslationUnitDecl


    // BEGIN KRAPPER GEN for clang::ASTContext

    bool clang_ASTContext_contains_address_discriminated_pointer_auth(void* thiz, void* T);

    bool clang_ASTContext_contains_non_relocatable_pointer_auth(void* thiz, void* T);

    void clang_ASTContext_set_traversal_scope(void* thiz, void* _arg_0);

    void clang_ASTContext_cleanup(void* thiz);

    const void* clang_ASTContext_allocate(void* thiz, size_t Size, unsigned int Align);

    void clang_ASTContext_deallocate(void* thiz, void* Ptr);

    size_t clang_ASTContext_get_ast_allocated_memory(void* thiz);

    size_t clang_ASTContext_get_side_table_allocated_memory(void* thiz);

    void clang_ASTContext_get_higher_precision_fp_type(void* thiz, void* ElementType, void* ret_value);

    void clang_ASTContext_get_int_type_for_bitwidth(void* thiz, unsigned int DestWidth, unsigned int Signed, void* ret_value);

    void clang_ASTContext_get_real_type_for_bitwidth(void* thiz, unsigned int DestWidth, int ExplicitType, void* ret_value);

    bool clang_ASTContext_is_dependence_allowed(void* thiz);

    unsigned int clang_ASTContext_get_cxxabi_kind(void* thiz);

    void clang_ASTContext_erase_decl_attrs(void* thiz, void* D);

    void* clang_ASTContext_get_instantiated_from_using_decl(void* thiz, void* Inst);

    void clang_ASTContext_set_instantiated_from_using_decl(void* thiz, void* Inst, void* Pattern);

    const void* clang_ASTContext_get_instantiated_from_unnamed_field_decl(void* thiz, void* Field);

    void clang_ASTContext_set_instantiated_from_unnamed_field_decl(void* thiz, void* Inst, void* Tmpl);

    unsigned int clang_ASTContext_overridden_methods_size(void* thiz, void* Method);

    void clang_ASTContext_overridden_methods(void* thiz, void* Method, void* ret_value);

    void clang_ASTContext_add_overridden_method(void* thiz, void* Method, void* Overridden);

    void* clang_ASTContext_get_primary_merged_decl(void* thiz, void* D);

    void clang_ASTContext_set_primary_merged_decl(void* thiz, void* D, void* Primary);

    void clang_ASTContext_deduplicate_merged_definitions_for(void* thiz, void* ND);

    const void* clang_ASTContext_get_translation_unit_decl(void* thiz);

    void clang_ASTContext_add_translation_unit_decl(void* thiz);

    void clang_ASTContext_dispose(void* thiz);

    void clang_ASTContext_print_stats(void* thiz);

    const void* clang_ASTContext_build_implicit_record(void* thiz, const char* Name, int TK);

    void clang_ASTContext_get_addr_space_qual_type(void* thiz, void* T, unsigned int AddressSpace, void* ret_value);

    void clang_ASTContext_remove_addr_space_qual_type(void* thiz, void* T, void* ret_value);

    unsigned short clang_ASTContext_get_pointer_auth_v_table_pointer_discriminator(void* thiz, void* RD);

    unsigned short clang_ASTContext_get_pointer_auth_type_discriminator(void* thiz, void* T);

    void clang_ASTContext_get_obj_cgc_qual_type(void* thiz, void* T, unsigned int gcAttr, void* ret_value);

    void clang_ASTContext_remove_ptr_size_addr_space(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_restrict_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_volatile_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_const_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_adjust_function_result_type(void* thiz, void* FunctionType, void* NewResultType, void* ret_value);

    void clang_ASTContext_adjust_deduced_function_result_type(void* thiz, void* FD, void* ResultType);

    bool clang_ASTContext_has_same_function_type_ignoring_exception_spec(void* thiz, void* T, void* U);

    void clang_ASTContext_get_function_type_without_ptr_sizes(void* thiz, void* T, void* ret_value);

    bool clang_ASTContext_has_same_function_type_ignoring_ptr_sizes(void* thiz, void* T, void* U);

    void clang_ASTContext_get_function_type_without_param_ab_is(void* thiz, void* T, void* ret_value);

    bool clang_ASTContext_has_same_function_type_ignoring_param_abi(void* thiz, void* T, void* U);

    void clang_ASTContext_get_complex_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_pointer_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_adjusted_type(void* thiz, void* Orig, void* New, void* ret_value);

    void clang_ASTContext_get_decayed_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_decayed_type__clang_QualType_clang_QualType(void* thiz, void* Orig, void* Decayed, void* ret_value);

    void clang_ASTContext_get_array_parameter_type(void* thiz, void* Ty, void* ret_value);

    void clang_ASTContext_get_atomic_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_block_pointer_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_block_descriptor_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_read_pipe_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_write_pipe_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_bit_int_type(void* thiz, bool Unsigned, unsigned int NumBits, void* ret_value);

    void clang_ASTContext_get_block_descriptor_extended_type(void* thiz, void* ret_value);

    unsigned int clang_ASTContext_get_default_open_cl_pointee_addr_space(void* thiz);

    void clang_ASTContext_setcuda_configure_call_decl(void* thiz, void* FD);

    void* clang_ASTContext_getcuda_configure_call_decl(void* thiz);

    void clang_ASTContext_setcuda_get_parameter_buffer_decl(void* thiz, void* FD);

    void* clang_ASTContext_getcuda_get_parameter_buffer_decl(void* thiz);

    void clang_ASTContext_setcuda_launch_device_decl(void* thiz, void* FD);

    void* clang_ASTContext_getcuda_launch_device_decl(void* thiz);

    bool clang_ASTContext_get_byref_lifetime(void* thiz, void* Ty, unsigned int Lifetime, bool HasByrefExtendedLayout);

    void clang_ASTContext_get_l_value_reference_type(void* thiz, void* T, bool SpelledAsLValue, void* ret_value);

    void clang_ASTContext_get_r_value_reference_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_incomplete_array_type(void* thiz, void* EltTy, int ASM, unsigned int IndexTypeQuals, void* ret_value);

    void clang_ASTContext_get_string_literal_array_type(void* thiz, void* EltTy, unsigned int Length, void* ret_value);

    void clang_ASTContext_get_variable_array_decayed_type(void* thiz, void* Ty, void* ret_value);

    void clang_ASTContext_get_scalable_vector_type(void* thiz, void* EltTy, unsigned int NumElts, unsigned int NumFields, void* ret_value);

    void clang_ASTContext_get_web_assembly_externref_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_vector_type(void* thiz, void* VectorType, unsigned int NumElts, int VecKind, void* ret_value);

    void clang_ASTContext_get_ext_vector_type(void* thiz, void* VectorType, unsigned int NumElts, void* ret_value);

    void clang_ASTContext_get_constant_matrix_type(void* thiz, void* ElementType, unsigned int NumRows, unsigned int NumColumns, void* ret_value);

    void clang_ASTContext_get_function_no_proto_type__clang_QualType(void* thiz, void* ResultTy, void* ret_value);

    void clang_ASTContext_adjust_string_literal_base_type(void* thiz, void* StrLTy, void* ret_value);

    void clang_ASTContext_get_type_decl_type__const_clang_TypeDecl_P(void* thiz, void* Decl, void* ret_value);

    bool clang_ASTContext_compute_best_enum_types(void* thiz, bool IsPacked, unsigned int NumNegativeBits, unsigned int NumPositiveBits, void* BestType, void* BestPromotionType);

    void clang_ASTContext_get_attributed_type(void* thiz, unsigned int attrKind, void* modifiedType, void* equivalentType, void* ret_value);

    void clang_ASTContext_get_attributed_type__clang_NullabilityKind_clang_QualType_clang_QualType(void* thiz, unsigned char nullability, void* modifiedType, void* equivalentType, void* ret_value);

    void clang_ASTContext_get_template_type_parm_type(void* thiz, unsigned int Depth, unsigned int Index, bool ParameterPack, void* ret_value);

    void clang_ASTContext_get_paren_type(void* thiz, void* NamedType, void* ret_value);

    void clang_ASTContext_get_obj_c_object_pointer_type(void* thiz, void* OIT, void* ret_value);

    void clang_ASTContext_get_type_of_type(void* thiz, void* QT, unsigned char Kind, void* ret_value);

    void clang_ASTContext_get_unary_transform_type(void* thiz, void* BaseType, void* UnderlyingType, unsigned int UKind, void* ret_value);

    void clang_ASTContext_get_auto_type(void* thiz, void* DeducedType, int Keyword, bool IsDependent, bool IsPack, void* ret_value);

    void clang_ASTContext_get_auto_deduct_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_auto_r_ref_deduct_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_unconstrained_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_size_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_signed_size_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_w_char_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_wide_char_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_signed_w_char_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_unsigned_w_char_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_w_int_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_int_ptr_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_u_int_ptr_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_pointer_diff_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_unsigned_pointer_diff_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_process_id_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_cf_constant_string_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_c_super_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_obj_c_super_type(void* thiz, void* ST);

    void clang_ASTContext_get_raw_cf_constant_string_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_cf_constant_string_type(void* thiz, void* T);

    const void* clang_ASTContext_get_cf_constant_string_tag_decl(void* thiz);

    void clang_ASTContext_get_obj_c_constant_string_interface(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_cns_string_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_obj_cns_string_type(void* thiz, void* T);

    void clang_ASTContext_get_obj_c_id_redefinition_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_obj_c_id_redefinition_type(void* thiz, void* RedefType);

    void clang_ASTContext_get_obj_c_class_redefinition_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_obj_c_class_redefinition_type(void* thiz, void* RedefType);

    void clang_ASTContext_get_obj_c_sel_redefinition_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_obj_c_sel_redefinition_type(void* thiz, void* RedefType);

    void clang_ASTContext_get_obj_c_instance_type(void* thiz, void* ret_value);

    void clang_ASTContext_set_file_decl(void* thiz, void* FILEDecl);

    void clang_ASTContext_get_file_type(void* thiz, void* ret_value);

    void clang_ASTContext_setjmp_buf_decl(void* thiz, void* jmp_bufDecl);

    void clang_ASTContext_getjmp_buf_type(void* thiz, void* ret_value);

    void clang_ASTContext_setsigjmp_buf_decl(void* thiz, void* sigjmp_bufDecl);

    void clang_ASTContext_getsigjmp_buf_type(void* thiz, void* ret_value);

    void clang_ASTContext_setucontext_t_decl(void* thiz, void* ucontext_tDecl);

    void clang_ASTContext_getucontext_t_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_c_encoding_for_type(void* thiz, void* T, const char* S, void* Field, void* NotEncodedT);

    void clang_ASTContext_get_obj_c_encoding_for_property_type(void* thiz, void* T, const char* S);

    void clang_ASTContext_get_legacy_integral_type_encoding(void* thiz, void* t);

    void clang_ASTContext_get_obj_c_encoding_for_type_qualifier(void* thiz, unsigned int QT, const char* S);

    const char* clang_ASTContext_get_obj_c_encoding_for_function_decl(void* thiz, void* Decl);

    void clang_ASTContext_get_obj_c_id_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_c_sel_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_c_class_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_bool_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_obj_c_proto_type(void* thiz, void* ret_value);

    void clang_ASTContext_get_builtin_va_list_type(void* thiz, void* ret_value);

    const void* clang_ASTContext_get_va_list_tag_decl(void* thiz);

    void clang_ASTContext_get_builtin_ms_va_list_type(void* thiz, void* ret_value);

    const void* clang_ASTContext_get_ms_guid_tag_decl(void* thiz);

    const void* clang_ASTContext_get_ms_type_info_tag_decl(void* thiz);

    bool clang_ASTContext_can_builtin_be_redeclared(void* thiz, void* _arg_0);

    void clang_ASTContext_get_cvr_qualified_type(void* thiz, void* T, unsigned int CVR, void* ret_value);

    void clang_ASTContext_get_lifetime_qualified_type(void* thiz, void* type, unsigned int lifetime, void* ret_value);

    void clang_ASTContext_get_unqualified_obj_c_pointer_type(void* thiz, void* type, void* ret_value);

    unsigned char clang_ASTContext_get_fixed_point_scale(void* thiz, void* Ty);

    unsigned char clang_ASTContext_get_fixed_point_i_bits(void* thiz, void* Ty);

    void clang_ASTContext_decode_type_str(void* thiz, const char* Str, void* Context, unsigned int Error, bool RequireICE, bool AllowTypeModifiers, void* ret_value);

    void clang_ASTContext_get_builtin_type(void* thiz, unsigned int ID, unsigned int Error, unsigned int* IntegerConstantArgs, void* ret_value);

    unsigned int clang_ASTContext_get_obj_cgc_attr_kind(void* thiz, void* Ty);

    bool clang_ASTContext_are_compatible_vector_types(void* thiz, void* FirstVec, void* SecondVec);

    bool clang_ASTContext_are_compatible_rvv_types(void* thiz, void* FirstType, void* SecondType);

    bool clang_ASTContext_are_lax_compatible_rvv_types(void* thiz, void* FirstType, void* SecondType);

    bool clang_ASTContext_has_direct_ownership_qualifier(void* thiz, void* Ty);

    bool clang_ASTContext_is_obj_cns_object_type(void* Ty);

    unsigned int clang_ASTContext_get_open_mp_default_simd_align(void* thiz, void* T);

    unsigned long clang_ASTContext_get_type_size(void* thiz, void* T);

    unsigned long clang_ASTContext_get_char_width(void* thiz);

    unsigned int clang_ASTContext_get_type_align(void* thiz, void* T);

    unsigned int clang_ASTContext_get_type_unadjusted_align(void* thiz, void* T);

    unsigned int clang_ASTContext_get_type_align_if_known(void* thiz, void* T, bool NeedsPreferredAlignment);

    bool clang_ASTContext_is_alignment_required__clang_QualType(void* thiz, void* T);

    bool clang_ASTContext_is_promotable_integer_type(void* thiz, void* T);

    unsigned int clang_ASTContext_get_preferred_type_align(void* thiz, void* T);

    unsigned int clang_ASTContext_get_target_default_align_for_attribute_aligned(void* thiz);

    bool clang_ASTContext_defaults_to_ms_struct(void* thiz);

    void* clang_ASTContext_get_current_key_function(void* thiz, void* RD);

    void clang_ASTContext_set_non_key_function(void* thiz, void* method);

    unsigned long clang_ASTContext_get_field_offset(void* thiz, void* FD);

    bool clang_ASTContext_is_nearly_empty(void* thiz, void* RD);

    bool clang_ASTContext_has_unique_object_representations(void* thiz, void* Ty, bool CheckIfTriviallyCopyable);

    bool clang_ASTContext_has_same_type(void* T1, void* T2);

    void clang_ASTContext_get_unqualified_array_type__clang_QualType(void* thiz, void* T, void* ret_value);

    bool clang_ASTContext_has_same_unqualified_type(void* T1, void* T2);

    bool clang_ASTContext_has_same_nullability_type_qualifier(void* thiz, void* SubT, void* SuperT, bool IsParam);

    bool clang_ASTContext_unwrap_similar_types(void* thiz, void* T1, void* T2, bool AllowPiMismatch);

    void clang_ASTContext_unwrap_similar_array_types(void* thiz, void* T1, void* T2, bool AllowPiMismatch);

    bool clang_ASTContext_has_similar_type(void* thiz, void* T1, void* T2);

    bool clang_ASTContext_has_cvr_similar_type(void* thiz, void* T1, void* T2);

    unsigned int clang_ASTContext_get_default_calling_convention(void* thiz, bool IsVariadic, bool IsCXXMethod);

    bool clang_ASTContext_is_same_entity(void* thiz, void* X, void* Y);

    bool clang_ASTContext_is_same_template_parameter(void* thiz, void* X, void* Y);

    bool clang_ASTContext_is_same_default_template_argument(void* thiz, void* X, void* Y);

    void clang_ASTContext_get_base_element_type__clang_QualType(void* thiz, void* QT, void* ret_value);

    void clang_ASTContext_get_adjusted_parameter_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_signature_parameter_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_exception_object_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_array_decayed_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_promoted_integer_type(void* thiz, void* PromotableType, void* ret_value);

    unsigned int clang_ASTContext_get_inner_obj_c_ownership(void* thiz, void* T);

    int clang_ASTContext_get_integer_type_order(void* thiz, void* LHS, void* RHS);

    int clang_ASTContext_get_floating_type_order(void* thiz, void* LHS, void* RHS);

    int clang_ASTContext_get_floating_type_semantic_order(void* thiz, void* LHS, void* RHS);

    unsigned int clang_ASTContext_get_target_address_space(void* thiz, unsigned int AS);

    unsigned int clang_ASTContext_get_lang_as_for_builtin_address_space(void* thiz, unsigned int AS);

    unsigned long clang_ASTContext_get_target_null_pointer_value(void* thiz, void* QT);

    bool clang_ASTContext_address_space_map_mangling_for(void* thiz, unsigned int AS);

    bool clang_ASTContext_has_any_function_effects(void* thiz);

    void clang_ASTContext_get_common_sugared_type(void* thiz, void* X, void* Y, bool Unqualified, void* ret_value);

    bool clang_ASTContext_types_are_compatible(void* thiz, void* T1, void* T2, bool CompareUnqualified);

    bool clang_ASTContext_property_types_are_compatible(void* thiz, void* _arg_0, void* _arg_1);

    bool clang_ASTContext_types_are_block_pointer_compatible(void* thiz, void* _arg_0, void* _arg_1);

    bool clang_ASTContext_is_obj_c_id_type(void* thiz, void* T);

    bool clang_ASTContext_is_obj_c_class_type(void* thiz, void* T);

    bool clang_ASTContext_is_obj_c_sel_type(void* thiz, void* T);

    bool clang_ASTContext_are_comparable_obj_c_pointer_types(void* thiz, void* LHS, void* RHS);

    bool clang_ASTContext_can_bind_obj_c_object_type(void* thiz, void* To, void* From);

    void clang_ASTContext_merge_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, bool BlockReturnType, bool IsConditionalOperator, void* ret_value);

    void clang_ASTContext_merge_function_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, bool AllowCXX, bool IsConditionalOperator, void* ret_value);

    void clang_ASTContext_merge_function_parameter_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, void* ret_value);

    void clang_ASTContext_merge_transparent_union_type(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, void* ret_value);

    void clang_ASTContext_merge_tag_definitions(void* thiz, void* _arg_0, void* _arg_1, void* ret_value);

    void clang_ASTContext_merge_obj_cgc_qualifiers(void* thiz, void* _arg_0, void* _arg_1, void* ret_value);

    unsigned int clang_ASTContext_get_int_width(void* thiz, void* T);

    void clang_ASTContext_get_corresponding_unsigned_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_corresponding_signed_type(void* thiz, void* T, void* ret_value);

    void clang_ASTContext_get_corresponding_saturated_type(void* thiz, void* Ty, void* ret_value);

    void clang_ASTContext_get_corresponding_unsaturated_type(void* thiz, void* Ty, void* ret_value);

    void clang_ASTContext_get_corresponding_signed_fixed_point_type(void* thiz, void* Ty, void* ret_value);

    bool clang_ASTContext_any_obj_c_implementation(void* thiz);

    unsigned int clang_ASTContext_get_gva_linkage_for_function(void* thiz, void* FD);

    bool clang_ASTContext_decl_must_be_emitted(void* thiz, void* D);

    void clang_ASTContext_add_declarator_for_unnamed_tag_decl(void* thiz, void* TD, void* DD);

    void* clang_ASTContext_get_declarator_for_unnamed_tag_decl(void* thiz, void* TD);

    void clang_ASTContext_set_mangling_number(void* thiz, void* ND, unsigned int Number);

    unsigned int clang_ASTContext_get_mangling_number(void* thiz, void* ND, bool ForAuxTarget);

    bool clang_ASTContext_has_seen_type_aware_operator_new_or_delete(void* thiz);

    void clang_ASTContext_set_is_destroying_operator_delete(void* thiz, void* FD, bool IsDestroying);

    bool clang_ASTContext_is_destroying_operator_delete(void* thiz, void* FD);

    void clang_ASTContext_set_is_type_aware_operator_new_or_delete(void* thiz, void* FD, bool IsTypeAware);

    bool clang_ASTContext_is_type_aware_operator_new_or_delete(void* thiz, void* FD);

    void clang_ASTContext_set_class_needs_vector_deleting_destructor(void* thiz, void* RD);

    bool clang_ASTContext_class_needs_vector_deleting_destructor(void* thiz, void* RD);

    unsigned int clang_ASTContext_get_next_string_literal_version(void* thiz);

    void clang_ASTContext_register_sycl_entry_point_function(void* thiz, void* FD);

    void clang_ASTContext_get_obj_c_encoding_for_method_parameter(void* thiz, unsigned int QT, void* T, const char* S, bool Extended);

    bool clang_ASTContext_may_externalize(void* thiz, void* D);

    bool clang_ASTContext_should_externalize(void* thiz, void* D);

    void* clang_ASTContext_base_for_v_table_authentication(void* thiz, void* ThisClass);

    const char* clang_ASTContext_backup_str(void* thiz, const char* S);

    const char* clang_ASTContext_get_cuid_hash(void* thiz);

    int clang_ASTContext_size_of();

    int clang_ASTContext_align_of();

    const unsigned int clang_ASTContext_TUKind_get(void* thiz);

    bool clang_ASTContext_CommentsLoaded_get(void* thiz);

    void clang_ASTContext_CommentsLoaded_set(void* thiz, bool value);

    void clang_ASTContext_AutoDeductTy_get(void* thiz, void* ret_value);

    void clang_ASTContext_AutoDeductTy_set(void* thiz, void* value);

    void clang_ASTContext_AutoRRefDeductTy_get(void* thiz, void* ret_value);

    void clang_ASTContext_AutoRRefDeductTy_set(void* thiz, void* value);

    void* clang_ASTContext_VaListTagDecl_get(void* thiz);

    void clang_ASTContext_VaListTagDecl_set(void* thiz, void* value);

    void* clang_ASTContext_MSGuidTagDecl_get(void* thiz);

    void clang_ASTContext_MSGuidTagDecl_set(void* thiz, void* value);

    void* clang_ASTContext_MSTypeInfoTagDecl_get(void* thiz);

    void clang_ASTContext_MSTypeInfoTagDecl_set(void* thiz, void* value);

    unsigned int clang_ASTContext_NumImplicitDefaultConstructors_get(void* thiz);

    void clang_ASTContext_NumImplicitDefaultConstructors_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitDefaultConstructorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitDefaultConstructorsDeclared_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitCopyConstructors_get(void* thiz);

    void clang_ASTContext_NumImplicitCopyConstructors_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitCopyConstructorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitCopyConstructorsDeclared_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitMoveConstructors_get(void* thiz);

    void clang_ASTContext_NumImplicitMoveConstructors_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitMoveConstructorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitMoveConstructorsDeclared_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitCopyAssignmentOperators_get(void* thiz);

    void clang_ASTContext_NumImplicitCopyAssignmentOperators_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitMoveAssignmentOperators_get(void* thiz);

    void clang_ASTContext_NumImplicitMoveAssignmentOperators_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitDestructors_get(void* thiz);

    void clang_ASTContext_NumImplicitDestructors_set(void* thiz, unsigned int value);

    unsigned int clang_ASTContext_NumImplicitDestructorsDeclared_get(void* thiz);

    void clang_ASTContext_NumImplicitDestructorsDeclared_set(void* thiz, unsigned int value);


    // END KRAPPER GEN for clang::ASTContext


    // BEGIN KRAPPER GEN for clang::DeclContext

    void clang_DeclContext_dispose(void* thiz);

    bool clang_DeclContext_has_valid_decl_kind(void* thiz);

    unsigned int clang_DeclContext_get_decl_kind(void* thiz);

    const char* clang_DeclContext_get_decl_kind_name(void* thiz);

    void* clang_DeclContext_get_parent(void* thiz);

    void* clang_DeclContext_get_lexical_parent(void* thiz);

    void* clang_DeclContext_get_lookup_parent(void* thiz);

    const void* clang_DeclContext_get_parent_ast_context(void* thiz);

    bool clang_DeclContext_is_closure(void* thiz);

    bool clang_DeclContext_is_obj_c_container(void* thiz);

    bool clang_DeclContext_is_function_or_method(void* thiz);

    bool clang_DeclContext_is_lookup_context(void* thiz);

    bool clang_DeclContext_is_file_context(void* thiz);

    bool clang_DeclContext_is_translation_unit(void* thiz);

    bool clang_DeclContext_is_record(void* thiz);

    bool clang_DeclContext_is_requires_expr_body(void* thiz);

    bool clang_DeclContext_is_namespace(void* thiz);

    bool clang_DeclContext_is_std_namespace(void* thiz);

    bool clang_DeclContext_is_inline_namespace(void* thiz);

    bool clang_DeclContext_is_dependent_context(void* thiz);

    bool clang_DeclContext_is_transparent_context(void* thiz);

    bool clang_DeclContext_is_extern_c_context(void* thiz);

    bool clang_DeclContext_is_extern_cxx_context(void* thiz);

    bool clang_DeclContext_equals(void* thiz, void* DC);

    bool clang_DeclContext_encloses(void* thiz, void* DC);

    bool clang_DeclContext_lexically_encloses(void* thiz, void* DC);

    void* clang_DeclContext_get_non_closure_ancestor(void* thiz);

    void* clang_DeclContext_get_non_transparent_context(void* thiz);

    void* clang_DeclContext_get_primary_context(void* thiz);

    void* clang_DeclContext_get_redecl_context(void* thiz);

    void* clang_DeclContext_get_enclosing_namespace_context(void* thiz);

    void* clang_DeclContext_get_outer_lexical_record_context(void* thiz);

    bool clang_DeclContext_in_enclosing_namespace_set_of(void* thiz, void* NS);

    void clang_DeclContext_decls(void* thiz, void* ret_value);

    bool clang_DeclContext_decls_empty(void* thiz);

    void clang_DeclContext_noload_decls(void* thiz, void* ret_value);

    void clang_DeclContext_add_decl(void* thiz, void* D);

    void clang_DeclContext_add_decl_internal(void* thiz, void* D);

    void clang_DeclContext_add_hidden_decl(void* thiz, void* D);

    void clang_DeclContext_remove_decl(void* thiz, void* D);

    bool clang_DeclContext_contains_decl(void* thiz, void* D);

    bool clang_DeclContext_contains_decl_and_load(void* thiz, void* D);

    void clang_DeclContext_make_decl_visible_in_context(void* thiz, void* D);

    void clang_DeclContext_set_must_build_lookup_table(void* thiz);

    bool clang_DeclContext_has_external_lexical_storage(void* thiz);

    void clang_DeclContext_set_has_external_lexical_storage(void* thiz, bool ES);

    bool clang_DeclContext_has_external_visible_storage(void* thiz);

    void clang_DeclContext_set_has_external_visible_storage(void* thiz, bool ES);

    bool clang_DeclContext_is_decl_in_lexical_traversal(void* thiz, void* D);

    void clang_DeclContext_set_use_qualified_lookup(void* thiz, bool use);

    bool clang_DeclContext_should_use_qualified_lookup(void* thiz);

    bool clang_DeclContext_classof(void* D);

    bool clang_DeclContext_classof__const_clang_DeclContext_P(void* D);

    void clang_DeclContext_dump_as_decl(void* thiz);

    void clang_DeclContext_dump_as_decl__const_clang_ASTContext_P(void* thiz, void* Ctx);

    void clang_DeclContext_dump_decl_context(void* thiz);

    void clang_DeclContext_dump_lookups(void* thiz);

    int clang_DeclContext_size_of();

    int clang_DeclContext_align_of();

    void* clang_DeclContext_dyncast_clang_TagDecl(void* p);

    void* clang_DeclContext_dyncast_clang_RecordDecl(void* p);

    void* clang_DeclContext_dyncast_clang_FunctionDecl(void* p);

    void* clang_DeclContext_dyncast_clang_CXXMethodDecl(void* p);

    void* clang_DeclContext_dyncast_clang_CXXRecordDecl(void* p);

    void* clang_DeclContext_dyncast_clang_TranslationUnitDecl(void* p);


    // END KRAPPER GEN for clang::DeclContext


    // BEGIN KRAPPER GEN for clang::Decl

    unsigned int clang_Decl_get_kind(void* thiz);

    const char* clang_Decl_get_decl_kind_name(void* thiz);

    void* clang_Decl_get_next_decl_in_context(void* thiz);

    void* clang_Decl_get_decl_context(void* thiz);

    void* clang_Decl_get_non_transparent_decl_context(void* thiz);

    void* clang_Decl_get_non_closure_context(void* thiz);

    void* clang_Decl_get_translation_unit_decl(void* thiz);

    bool clang_Decl_is_in_anonymous_namespace(void* thiz);

    bool clang_Decl_is_in_std_namespace(void* thiz);

    bool clang_Decl_is_file_context_decl(void* thiz);

    bool clang_Decl_is_flexible_array_member_like(void* Context, void* D, void* Ty, int StrictFlexArraysLevel, bool IgnoreTemplateOrMacroSubstitution);

    const void* clang_Decl_get_ast_context(void* thiz);

    void clang_Decl_set_access(void* thiz, unsigned char AS);

    unsigned char clang_Decl_get_access(void* thiz);

    unsigned char clang_Decl_get_access_unsafe(void* thiz);

    bool clang_Decl_has_attrs(void* thiz);

    void clang_Decl_drop_attrs(void* thiz);

    unsigned int clang_Decl_get_max_alignment(void* thiz);

    void clang_Decl_set_invalid_decl(void* thiz, bool Invalid);

    bool clang_Decl_is_invalid_decl(void* thiz);

    bool clang_Decl_is_implicit(void* thiz);

    void clang_Decl_set_implicit(void* thiz, bool I);

    bool clang_Decl_is_used(void* thiz, bool CheckUsedAttr);

    void clang_Decl_set_is_used(void* thiz);

    void clang_Decl_mark_used(void* thiz, void* C);

    bool clang_Decl_is_referenced(void* thiz);

    bool clang_Decl_is_this_declaration_referenced(void* thiz);

    void clang_Decl_set_referenced(void* thiz, bool R);

    void clang_Decl_invalidate_cached_linkage(void* thiz);

    bool clang_Decl_is_top_level_decl_in_obj_c_container(void* thiz);

    void clang_Decl_set_top_level_decl_in_obj_c_container(void* thiz, bool V);

    bool clang_Decl_is_module_private(void* thiz);

    bool clang_Decl_is_module_local(void* thiz);

    bool clang_Decl_is_in_export_decl_context(void* thiz);

    bool clang_Decl_is_invisible_outside_the_owning_module(void* thiz);

    bool clang_Decl_is_in_another_module_unit(void* thiz);

    bool clang_Decl_is_in_current_module_unit(void* thiz);

    bool clang_Decl_should_emit_in_external_source(void* thiz);

    bool clang_Decl_is_from_explicit_global_module(void* thiz);

    bool clang_Decl_is_from_global_module(void* thiz);

    bool clang_Decl_is_in_named_module(void* thiz);

    bool clang_Decl_is_from_header_unit(void* thiz);

    bool clang_Decl_has_defining_attr(void* thiz);

    void clang_Decl_set_from_ast_file(void* thiz);

    void clang_Decl_set_owning_module_id(void* thiz, unsigned int ID);

    unsigned int clang_Decl_get_availability(void* thiz);

    bool clang_Decl_is_deprecated(void* thiz);

    bool clang_Decl_is_unavailable(void* thiz);

    bool clang_Decl_is_weak_imported(void* thiz);

    bool clang_Decl_can_be_weak_imported(void* thiz, bool IsDefinition);

    bool clang_Decl_is_from_ast_file(void* thiz);

    unsigned int clang_Decl_get_owning_module_id(void* thiz);

    bool clang_Decl_has_owning_module(void* thiz);

    bool clang_Decl_is_unconditionally_visible(void* thiz);

    bool clang_Decl_is_reachable(void* thiz);

    void clang_Decl_set_visible_despite_owning_module(void* thiz);

    void clang_Decl_set_visible_promoted(void* thiz);

    unsigned char clang_Decl_get_module_ownership_kind(void* thiz);

    void clang_Decl_set_module_ownership_kind(void* thiz, unsigned char MOK);

    unsigned int clang_Decl_get_identifier_namespace(void* thiz);

    bool clang_Decl_is_in_identifier_namespace(void* thiz, unsigned int NS);

    unsigned int clang_Decl_get_identifier_namespace_for_kind(unsigned int DK);

    bool clang_Decl_has_tag_identifier_namespace(void* thiz);

    bool clang_Decl_is_tag_identifier_namespace(unsigned int NS);

    void* clang_Decl_get_lexical_decl_context(void* thiz);

    bool clang_Decl_is_out_of_line(void* thiz);

    void clang_Decl_set_decl_context(void* thiz, void* DC);

    void clang_Decl_set_lexical_decl_context(void* thiz, void* DC);

    bool clang_Decl_is_templated(void* thiz);

    unsigned int clang_Decl_get_template_depth(void* thiz);

    bool clang_Decl_is_defined_outside_function_or_method(void* thiz);

    bool clang_Decl_is_in_local_scope_for_instantiation(void* thiz);

    void* clang_Decl_get_parent_function_or_method(void* thiz, bool LexicalParent);

    void* clang_Decl_get_canonical_decl(void* thiz);

    bool clang_Decl_is_canonical_decl(void* thiz);

    void clang_Decl_redecls(void* thiz, void* ret_value);

    void* clang_Decl_get_previous_decl(void* thiz);

    bool clang_Decl_is_first_decl(void* thiz);

    void* clang_Decl_get_most_recent_decl(void* thiz);

    bool clang_Decl_has_body(void* thiz);

    void clang_Decl_add(unsigned int k);

    void clang_Decl_enable_statistics();

    void clang_Decl_print_stats();

    bool clang_Decl_is_template_parameter(void* thiz);

    bool clang_Decl_is_template_parameter_pack(void* thiz);

    bool clang_Decl_is_parameter_pack(void* thiz);

    bool clang_Decl_is_template_decl(void* thiz);

    bool clang_Decl_is_function_or_function_template(void* thiz);

    void* clang_Decl_get_as_function(void* thiz);

    void clang_Decl_set_local_extern_decl(void* thiz);

    bool clang_Decl_is_local_extern_decl(void* thiz);

    void clang_Decl_set_object_of_friend_decl(void* thiz, bool PerformFriendInjection);

    void clang_Decl_clear_identifier_namespace(void* thiz);

    unsigned int clang_Decl_get_friend_object_kind(void* thiz);

    void clang_Decl_set_non_member_operator(void* thiz);

    bool clang_Decl_classof_kind(unsigned int K);

    void* clang_Decl_cast_to_decl_context(void* _arg_0);

    void* clang_Decl_cast_from_decl_context(void* _arg_0);

    void clang_Decl_dump(void* thiz);

    void clang_Decl_dump_color(void* thiz);

    long clang_Decl_get_id(void* thiz);

    bool clang_Decl_is_function_pointer_type(void* thiz);

    int clang_Decl_size_of();

    int clang_Decl_align_of();

    void* clang_Decl_dyncast_clang_NamedDecl(void* p);

    void* clang_Decl_dyncast_clang_TypeDecl(void* p);

    void* clang_Decl_dyncast_clang_TagDecl(void* p);

    void* clang_Decl_dyncast_clang_ValueDecl(void* p);

    void* clang_Decl_dyncast_clang_DeclaratorDecl(void* p);

    void* clang_Decl_dyncast_clang_FieldDecl(void* p);

    void* clang_Decl_dyncast_clang_RecordDecl(void* p);

    void* clang_Decl_dyncast_clang_FunctionDecl(void* p);

    void* clang_Decl_dyncast_clang_CXXMethodDecl(void* p);

    void* clang_Decl_dyncast_clang_CXXRecordDecl(void* p);

    void* clang_Decl_dyncast_clang_TranslationUnitDecl(void* p);


    // END KRAPPER GEN for clang::Decl


    // BEGIN KRAPPER GEN for clang::ASTUnit

    void clang_ASTUnit_dispose(void* thiz);

    bool clang_ASTUnit_is_main_file_ast(void* thiz);

    bool clang_ASTUnit_is_unsafe_to_free(void* thiz);

    void clang_ASTUnit_set_unsafe_to_free(void* thiz, bool Value);

    void* clang_ASTUnit_get_ast_context(void* thiz);

    void clang_ASTUnit_enable_source_file_diagnostics(void* thiz);

    bool clang_ASTUnit_has_sema(void* thiz);

    bool clang_ASTUnit_get_only_local_decls(void* thiz);

    bool clang_ASTUnit_get_owns_remapped_file_buffers(void* thiz);

    void clang_ASTUnit_set_owns_remapped_file_buffers(void* thiz, bool val);

    size_t clang_ASTUnit_top_level_size(void* thiz);

    bool clang_ASTUnit_top_level_empty(void* thiz);

    void clang_ASTUnit_add_top_level_decl(void* thiz, void* D);

    void clang_ASTUnit_add_file_level_decl(void* thiz, void* D);

    unsigned int* clang_ASTUnit_get_current_top_level_hash_value(void* thiz);

    unsigned int clang_ASTUnit_get_preamble_counter_for_tests(void* thiz);

    unsigned int clang_ASTUnit_stored_diag_size(void* thiz);

    unsigned int clang_ASTUnit_cached_completion_size(void* thiz);

    bool clang_ASTUnit_is_module_file(void* thiz);

    unsigned int clang_ASTUnit_get_translation_unit_kind(void* thiz);

    void clang_ASTUnit_reset_for_parse(void* thiz);

    bool clang_ASTUnit_save(void* thiz, const char* File);

    const char* clang_ASTUnit_get_original_source_file_name(void* thiz);

    const char* clang_ASTUnit_get_main_file_name(void* thiz);

    const char* clang_ASTUnit_get_ast_file_name(void* thiz);

    int clang_ASTUnit_size_of();

    int clang_ASTUnit_align_of();


    // END KRAPPER GEN for clang::ASTUnit


    // BEGIN KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>

    void* std_vector_clang_CXXBaseSpecifier_P_new(void* location);

    void* std_vector_clang_CXXBaseSpecifier_P_new__size_t_const_allocator_type_and(void* location, size_t __n);

    void* std_vector_clang_CXXBaseSpecifier_P_new__const_std_vector_and(void* location, void* __x);

    void std_vector_clang_CXXBaseSpecifier_P_dispose(void* thiz);

    void* std_vector_clang_CXXBaseSpecifier_P_op_assign(void* thiz, void* __x);

    size_t std_vector_clang_CXXBaseSpecifier_P_size(void* thiz);

    size_t std_vector_clang_CXXBaseSpecifier_P_max_size(void* thiz);

    void std_vector_clang_CXXBaseSpecifier_P_resize(void* thiz, size_t __new_size);

    void std_vector_clang_CXXBaseSpecifier_P_shrink_to_fit(void* thiz);

    size_t std_vector_clang_CXXBaseSpecifier_P_capacity(void* thiz);

    bool std_vector_clang_CXXBaseSpecifier_P_empty(void* thiz);

    void std_vector_clang_CXXBaseSpecifier_P_reserve(void* thiz, size_t __n);

    void* std_vector_clang_CXXBaseSpecifier_P_op_ind(void* thiz, size_t __n);

    void* std_vector_clang_CXXBaseSpecifier_P_at(void* thiz, size_t __n);

    void* std_vector_clang_CXXBaseSpecifier_P_front(void* thiz);

    void* std_vector_clang_CXXBaseSpecifier_P_back(void* thiz);

    void* std_vector_clang_CXXBaseSpecifier_P_data(void* thiz);

    void std_vector_clang_CXXBaseSpecifier_P_pop_back(void* thiz);

    void std_vector_clang_CXXBaseSpecifier_P_swap(void* thiz, void* __x);

    void std_vector_clang_CXXBaseSpecifier_P_clear(void* thiz);

    int std_vector_clang_CXXBaseSpecifier_P_size_of();

    int std_vector_clang_CXXBaseSpecifier_P_align_of();


    // END KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>


    // BEGIN KRAPPER GEN for std::vector<clang::CXXMethodDecl*>

    void* std_vector_clang_CXXMethodDecl_P_new(void* location);

    void* std_vector_clang_CXXMethodDecl_P_new__size_t_const_allocator_type_and(void* location, size_t __n);

    void* std_vector_clang_CXXMethodDecl_P_new__const_std_vector_and(void* location, void* __x);

    void std_vector_clang_CXXMethodDecl_P_dispose(void* thiz);

    void* std_vector_clang_CXXMethodDecl_P_op_assign(void* thiz, void* __x);

    size_t std_vector_clang_CXXMethodDecl_P_size(void* thiz);

    size_t std_vector_clang_CXXMethodDecl_P_max_size(void* thiz);

    void std_vector_clang_CXXMethodDecl_P_resize(void* thiz, size_t __new_size);

    void std_vector_clang_CXXMethodDecl_P_shrink_to_fit(void* thiz);

    size_t std_vector_clang_CXXMethodDecl_P_capacity(void* thiz);

    bool std_vector_clang_CXXMethodDecl_P_empty(void* thiz);

    void std_vector_clang_CXXMethodDecl_P_reserve(void* thiz, size_t __n);

    void* std_vector_clang_CXXMethodDecl_P_op_ind(void* thiz, size_t __n);

    void* std_vector_clang_CXXMethodDecl_P_at(void* thiz, size_t __n);

    void* std_vector_clang_CXXMethodDecl_P_front(void* thiz);

    void* std_vector_clang_CXXMethodDecl_P_back(void* thiz);

    void* std_vector_clang_CXXMethodDecl_P_data(void* thiz);

    void std_vector_clang_CXXMethodDecl_P_pop_back(void* thiz);

    void std_vector_clang_CXXMethodDecl_P_swap(void* thiz, void* __x);

    void std_vector_clang_CXXMethodDecl_P_clear(void* thiz);

    int std_vector_clang_CXXMethodDecl_P_size_of();

    int std_vector_clang_CXXMethodDecl_P_align_of();


    // END KRAPPER GEN for std::vector<clang::CXXMethodDecl*>


    // BEGIN KRAPPER GEN for std::vector<clang::Decl*>

    void* std_vector_clang_Decl_P_new(void* location);

    void* std_vector_clang_Decl_P_new__size_t_const_allocator_type_and(void* location, size_t __n);

    void* std_vector_clang_Decl_P_new__const_std_vector_and(void* location, void* __x);

    void std_vector_clang_Decl_P_dispose(void* thiz);

    void* std_vector_clang_Decl_P_op_assign(void* thiz, void* __x);

    size_t std_vector_clang_Decl_P_size(void* thiz);

    size_t std_vector_clang_Decl_P_max_size(void* thiz);

    void std_vector_clang_Decl_P_resize(void* thiz, size_t __new_size);

    void std_vector_clang_Decl_P_shrink_to_fit(void* thiz);

    size_t std_vector_clang_Decl_P_capacity(void* thiz);

    bool std_vector_clang_Decl_P_empty(void* thiz);

    void std_vector_clang_Decl_P_reserve(void* thiz, size_t __n);

    void* std_vector_clang_Decl_P_op_ind(void* thiz, size_t __n);

    void* std_vector_clang_Decl_P_at(void* thiz, size_t __n);

    void* std_vector_clang_Decl_P_front(void* thiz);

    void* std_vector_clang_Decl_P_back(void* thiz);

    void* std_vector_clang_Decl_P_data(void* thiz);

    void std_vector_clang_Decl_P_pop_back(void* thiz);

    void std_vector_clang_Decl_P_swap(void* thiz, void* __x);

    void std_vector_clang_Decl_P_clear(void* thiz);

    int std_vector_clang_Decl_P_size_of();

    int std_vector_clang_Decl_P_align_of();


    // END KRAPPER GEN for std::vector<clang::Decl*>


    void* clang_tooling_build_ast_from_code(const char* Code, const char* FileName);

    void* clang_tooling_build_ast_from_code(const char* Code, const char* FileName);

    void* clang_tooling_build_ast_from_code(const char* Code, const char* FileName);

    void* clang_tooling_build_ast_from_code(const char* Code, const char* FileName);


    #ifdef __cplusplus
        }
    #endif //__cplusplus

#endif //__CLANGWALK__
