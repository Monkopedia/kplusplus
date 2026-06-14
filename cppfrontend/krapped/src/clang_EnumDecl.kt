package clang

import clang.AccessSpecifier.Companion.fromValue as AccessSpecifierCompanionfromValue
import clang.AvailabilityResult.Companion.fromValue as AvailabilityResultCompanionfromValue
import clang.Linkage.Companion.fromValue as CompanionfromValue
import clang.ObjCStringFormatFamily.Companion.fromValue as ObjCStringFormatFamilyCompanionfromValue
import clang.QualType.Companion.QualType_Holder
import clang.TagTypeKind.Companion.fromValue
import clang.TemplateSpecializationKind.Companion.fromValue as TemplateSpecializationKindCompanionfromValue
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
import krapper.cppfrontend.internal.clang_EnumDecl_align_of
import krapper.cppfrontend.internal.clang_EnumDecl_as_clang_Decl
import krapper.cppfrontend.internal.clang_EnumDecl_as_clang_DeclContext
import krapper.cppfrontend.internal.clang_EnumDecl_as_clang_NamedDecl
import krapper.cppfrontend.internal.clang_EnumDecl_as_clang_TagDecl
import krapper.cppfrontend.internal.clang_EnumDecl_as_clang_TypeDecl
import krapper.cppfrontend.internal.clang_EnumDecl_classof
import krapper.cppfrontend.internal.clang_EnumDecl_classof_kind
import krapper.cppfrontend.internal.clang_EnumDecl_complete_definition
import krapper.cppfrontend.internal.clang_EnumDecl_get_canonical_decl
import krapper.cppfrontend.internal.clang_EnumDecl_get_definition
import krapper.cppfrontend.internal.clang_EnumDecl_get_definition_or_self
import krapper.cppfrontend.internal.clang_EnumDecl_get_instantiated_from_member_enum
import krapper.cppfrontend.internal.clang_EnumDecl_get_integer_type
import krapper.cppfrontend.internal.clang_EnumDecl_get_most_recent_decl
import krapper.cppfrontend.internal.clang_EnumDecl_get_num_negative_bits
import krapper.cppfrontend.internal.clang_EnumDecl_get_num_positive_bits
import krapper.cppfrontend.internal.clang_EnumDecl_get_odr_hash
import krapper.cppfrontend.internal.clang_EnumDecl_get_previous_decl
import krapper.cppfrontend.internal.clang_EnumDecl_get_promotion_type
import krapper.cppfrontend.internal.clang_EnumDecl_get_template_instantiation_pattern
import krapper.cppfrontend.internal.clang_EnumDecl_get_template_specialization_kind
import krapper.cppfrontend.internal.clang_EnumDecl_is_closed
import krapper.cppfrontend.internal.clang_EnumDecl_is_closed_flag
import krapper.cppfrontend.internal.clang_EnumDecl_is_closed_non_flag
import krapper.cppfrontend.internal.clang_EnumDecl_is_complete
import krapper.cppfrontend.internal.clang_EnumDecl_is_fixed
import krapper.cppfrontend.internal.clang_EnumDecl_is_scoped
import krapper.cppfrontend.internal.clang_EnumDecl_is_scoped_using_class_tag
import krapper.cppfrontend.internal.clang_EnumDecl_set_fixed
import krapper.cppfrontend.internal.clang_EnumDecl_set_instantiation_of_member_enum
import krapper.cppfrontend.internal.clang_EnumDecl_set_integer_type
import krapper.cppfrontend.internal.clang_EnumDecl_set_promotion_type
import krapper.cppfrontend.internal.clang_EnumDecl_set_scoped
import krapper.cppfrontend.internal.clang_EnumDecl_set_scoped_using_class_tag
import krapper.cppfrontend.internal.clang_EnumDecl_set_template_specialization_kind
import krapper.cppfrontend.internal.clang_EnumDecl_size_of
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
import krapper.cppfrontend.internal.clang_TagDecl_demote_this_definition_to_declaration
import krapper.cppfrontend.internal.clang_TagDecl_get_canonical_decl
import krapper.cppfrontend.internal.clang_TagDecl_get_definition
import krapper.cppfrontend.internal.clang_TagDecl_get_definition_or_self
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
import std.Vector__Decl_P
import std.Vector__Decl_P.Companion.Vector__Decl_P_Holder

// BEGIN KRAPPER GEN for clang::EnumDecl

@krapper.CppBinding("clang::EnumDecl")
class EnumDecl(
    override val ptr: COpaquePointer,
    val memScope: MemScope,
) : clang.TagDeclApi, clang.TypeDeclApi, clang.NamedDeclApi, clang.DeclApi {
    inline fun tagDeclGetCanonicalDecl(): TagDeclApi? {
        return TagDecl((clang_TagDecl_get_canonical_decl(ptr) ?: return null), memScope)
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
    inline fun getDefinition(): TagDeclApi? {
        return TagDecl((clang_TagDecl_get_definition(ptr) ?: return null), memScope)
    }
    inline fun getDefinitionOrSelf(): TagDeclApi? {
        return TagDecl((clang_TagDecl_get_definition_or_self(ptr) ?: return null), memScope)
    }
    inline fun isEntityBeingDefined(): Boolean {
        return clang_TagDecl_is_entity_being_defined(ptr)
    }
    inline fun getTagKind(): TagTypeKind {
        return fromValue(clang_TagDecl_get_tag_kind(ptr))
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
        return CompanionfromValue(clang_NamedDecl_get_linkage_internal(ptr))
    }
    inline fun getFormalLinkage(): Linkage {
        return CompanionfromValue(clang_NamedDecl_get_formal_linkage(ptr))
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
    inline fun setScoped(Scoped: Boolean = true): Unit {
        return clang_EnumDecl_set_scoped(ptr, Scoped)
    }
    inline fun setScopedUsingClassTag(ScopedUCT: Boolean = true): Unit {
        return clang_EnumDecl_set_scoped_using_class_tag(ptr, ScopedUCT)
    }
    inline fun setFixed(Fixed: Boolean = true): Unit {
        return clang_EnumDecl_set_fixed(ptr, Fixed)
    }
    inline fun enumDeclGetCanonicalDecl(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_canonical_decl(ptr) ?: return null), memScope)
    }
    inline fun enumDeclGetPreviousDecl(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_previous_decl(ptr) ?: return null), memScope)
    }
    inline fun enumDeclGetMostRecentDecl(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_most_recent_decl(ptr) ?: return null), memScope)
    }
    inline fun enumDeclGetDefinition(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_definition(ptr) ?: return null), memScope)
    }
    inline fun enumDeclGetDefinitionOrSelf(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_definition_or_self(ptr) ?: return null), memScope)
    }
    inline fun completeDefinition(NewType: QualType, PromotionType: QualType, NumPositiveBits: UInt, NumNegativeBits: UInt): Unit {
        return clang_EnumDecl_complete_definition(ptr, NewType.ptr, PromotionType.ptr, NumPositiveBits, NumNegativeBits)
    }
    inline fun getPromotionType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_EnumDecl_get_promotion_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setPromotionType(T: QualType): Unit {
        return clang_EnumDecl_set_promotion_type(ptr, T.ptr)
    }
    inline fun getIntegerType(): QualType {
        val retValue: QualType = memScope.QualType_Holder()
        clang_EnumDecl_get_integer_type(ptr, retValue.ptr)
        return retValue
    }
    inline fun setIntegerType(T: QualType): Unit {
        return clang_EnumDecl_set_integer_type(ptr, T.ptr)
    }
    inline fun getNumPositiveBits(): UInt {
        return clang_EnumDecl_get_num_positive_bits(ptr)
    }
    inline fun getNumNegativeBits(): UInt {
        return clang_EnumDecl_get_num_negative_bits(ptr)
    }
    inline fun isScoped(): Boolean {
        return clang_EnumDecl_is_scoped(ptr)
    }
    inline fun isScopedUsingClassTag(): Boolean {
        return clang_EnumDecl_is_scoped_using_class_tag(ptr)
    }
    inline fun isFixed(): Boolean {
        return clang_EnumDecl_is_fixed(ptr)
    }
    inline fun getODRHash(): UInt {
        return clang_EnumDecl_get_odr_hash(ptr)
    }
    inline fun isComplete(): Boolean {
        return clang_EnumDecl_is_complete(ptr)
    }
    inline fun isClosed(): Boolean {
        return clang_EnumDecl_is_closed(ptr)
    }
    inline fun isClosedFlag(): Boolean {
        return clang_EnumDecl_is_closed_flag(ptr)
    }
    inline fun isClosedNonFlag(): Boolean {
        return clang_EnumDecl_is_closed_non_flag(ptr)
    }
    inline fun getTemplateInstantiationPattern(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_template_instantiation_pattern(ptr) ?: return null), memScope)
    }
    inline fun getInstantiatedFromMemberEnum(): EnumDecl? {
        return EnumDecl((clang_EnumDecl_get_instantiated_from_member_enum(ptr) ?: return null), memScope)
    }
    inline fun getTemplateSpecializationKind(): TemplateSpecializationKind {
        return TemplateSpecializationKindCompanionfromValue(clang_EnumDecl_get_template_specialization_kind(ptr))
    }
    inline fun setTemplateSpecializationKind(TSK: TemplateSpecializationKind): Unit {
        return clang_EnumDecl_set_template_specialization_kind(ptr, TSK.value)
    }
    inline fun setInstantiationOfMemberEnum(ED: EnumDecl?, TSK: TemplateSpecializationKind): Unit {
        return clang_EnumDecl_set_instantiation_of_member_enum(ptr, ED?.ptr, TSK.value)
    }
    companion object {
        val size: Int
            inline get() {
                return clang_EnumDecl_size_of()
            }

        val align: Int
            inline get() {
                return clang_EnumDecl_align_of()
            }

        inline fun MemScope.classof(D: DeclApi?): Boolean {
            return clang_EnumDecl_classof(D?.ptr)
        }
        inline fun MemScope.classofKind(K: Kind): Boolean {
            return clang_EnumDecl_classof_kind(K.value)
        }
        fun MemScope.EnumDecl_Holder(): EnumDecl {
            val memory: COpaquePointer = (interpretCPointer(alloc(size, align).rawPtr) ?: error("Allocation failed"))
            return EnumDecl(memory, this)
        }
    }
    inline fun asTagDecl(): TagDecl {
        return TagDecl(clang_EnumDecl_as_clang_TagDecl(ptr)!!, memScope)
    }
    inline fun asTypeDecl(): TypeDecl {
        return TypeDecl(clang_EnumDecl_as_clang_TypeDecl(ptr)!!, memScope)
    }
    inline fun asDeclContext(): DeclContext {
        return DeclContext(clang_EnumDecl_as_clang_DeclContext(ptr)!!, memScope)
    }
    inline fun asNamedDecl(): NamedDecl {
        return NamedDecl(clang_EnumDecl_as_clang_NamedDecl(ptr)!!, memScope)
    }
    inline fun asDecl(): Decl {
        return Decl(clang_EnumDecl_as_clang_Decl(ptr)!!, memScope)
    }
}

// END KRAPPER GEN for clang::EnumDecl


