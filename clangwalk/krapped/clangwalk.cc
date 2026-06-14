#include "clangwalk.h"
#include "../include/clang_slice.h"
#include "KrapperForce_std_vector_clang_CXXBaseSpecifierPtr.h"
#include "KrapperForce_std_vector_clang_CXXMethodDeclPtr.h"
#include "KrapperForce_std_vector_clang_DeclPtr.h"
#include <vector>
#include <string>
#include <iterator>
#include <llvm/Support/Casting.h>

template <class E> static E* kpp_to_elem_ptr(E* p) { return p; };
template <class E> static E* kpp_to_elem_ptr(E& r) { return &r; };
template <class E> static E* kpp_to_elem_ptr(const E* p) { return (E*)p; };
template <class E> static E* kpp_to_elem_ptr(const E& r) { return (E*)&r; };

extern "C" {

typedef void (*StackConstructorCallback)(void*, void*);

// BEGIN KRAPPER GEN for clang::NamedDecl

const char* clang_NamedDecl_get_name_as_string(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    std::string ret_value = thiz_cast->getNameAsString();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

const char* clang_NamedDecl_get_qualified_name_as_string(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    std::string ret_value = thiz_cast->getQualifiedNameAsString();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

bool clang_NamedDecl_declaration_replaces(void* thiz, void* OldD, bool IsKnownNewer) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    const clang::NamedDecl* OldD_cast = reinterpret_cast<const clang::NamedDecl*>(OldD);
    return thiz_cast->declarationReplaces(OldD_cast, IsKnownNewer);
}

bool clang_NamedDecl_has_linkage(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->hasLinkage();
}

bool clang_NamedDecl_is_cxx_class_member(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->isCXXClassMember();
}

bool clang_NamedDecl_is_cxx_instance_member(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->isCXXInstanceMember();
}

unsigned char clang_NamedDecl_get_linkage_internal(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (unsigned char)thiz_cast->getLinkageInternal();
}

unsigned char clang_NamedDecl_get_formal_linkage(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (unsigned char)thiz_cast->getFormalLinkage();
}

bool clang_NamedDecl_has_external_formal_linkage(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->hasExternalFormalLinkage();
}

bool clang_NamedDecl_is_externally_visible(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->isExternallyVisible();
}

bool clang_NamedDecl_is_externally_declarable(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->isExternallyDeclarable();
}

unsigned int clang_NamedDecl_get_visibility(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (unsigned int)thiz_cast->getVisibility();
}

bool clang_NamedDecl_is_linkage_valid(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->isLinkageValid();
}

bool clang_NamedDecl_has_linkage_been_computed(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return thiz_cast->hasLinkageBeenComputed();
}

void* clang_NamedDecl_get_underlying_decl(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (void*)thiz_cast->getUnderlyingDecl();
}

void* clang_NamedDecl_get_most_recent_decl(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

unsigned int clang_NamedDecl_get_obj_cf_string_formatting_family(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    return (unsigned int)thiz_cast->getObjCFStringFormattingFamily();
}

bool clang_NamedDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::NamedDecl::classof(D_cast);
}

bool clang_NamedDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::NamedDecl::classofKind(K_cast);
}

const char* clang_NamedDecl_get_name(void* thiz) {
    clang::NamedDecl* thiz_cast = reinterpret_cast<clang::NamedDecl*>(thiz);
    std::string ret_value = thiz_cast->getName().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

int clang_NamedDecl_size_of() {
    return sizeof(clang::NamedDecl);
}

int clang_NamedDecl_align_of() {
    return alignof(clang::NamedDecl);
}

void* clang_NamedDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TypeDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypeDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_ValueDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ValueDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_DeclaratorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::DeclaratorDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}


// END KRAPPER GEN for clang::NamedDecl


// BEGIN KRAPPER GEN for clang::TypeDecl

bool clang_TypeDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TypeDecl::classof(D_cast);
}

bool clang_TypeDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TypeDecl::classofKind(K_cast);
}

int clang_TypeDecl_size_of() {
    return sizeof(clang::TypeDecl);
}

int clang_TypeDecl_align_of() {
    return alignof(clang::TypeDecl);
}

void* clang_TypeDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}


// END KRAPPER GEN for clang::TypeDecl


// BEGIN KRAPPER GEN for clang::TagDecl

void* clang_TagDecl_get_canonical_decl(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_TagDecl_is_this_declaration_a_definition(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isThisDeclarationADefinition();
}

bool clang_TagDecl_is_complete_definition(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isCompleteDefinition();
}

void clang_TagDecl_set_complete_definition(void* thiz, bool V) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->setCompleteDefinition(V);
}

bool clang_TagDecl_is_complete_definition_required(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isCompleteDefinitionRequired();
}

void clang_TagDecl_set_complete_definition_required(void* thiz, bool V) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->setCompleteDefinitionRequired(V);
}

bool clang_TagDecl_is_being_defined(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isBeingDefined();
}

bool clang_TagDecl_is_embedded_in_declarator(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isEmbeddedInDeclarator();
}

void clang_TagDecl_set_embedded_in_declarator(void* thiz, bool isInDeclarator) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->setEmbeddedInDeclarator(isInDeclarator);
}

bool clang_TagDecl_is_free_standing(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isFreeStanding();
}

void clang_TagDecl_set_free_standing(void* thiz, bool isFreeStanding) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->setFreeStanding(isFreeStanding);
}

bool clang_TagDecl_is_dependent_type(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isDependentType();
}

bool clang_TagDecl_is_this_declaration_a_demoted_definition(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isThisDeclarationADemotedDefinition();
}

void clang_TagDecl_demote_this_definition_to_declaration(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->demoteThisDefinitionToDeclaration();
}

void clang_TagDecl_start_definition(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    thiz_cast->startDefinition();
}

const void* clang_TagDecl_get_definition(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

const void* clang_TagDecl_get_definition_or_self(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (void*)thiz_cast->getDefinitionOrSelf();
}

bool clang_TagDecl_is_entity_being_defined(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isEntityBeingDefined();
}

int clang_TagDecl_get_tag_kind(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (int)thiz_cast->getTagKind();
}

void clang_TagDecl_set_tag_kind(void* thiz, int TK) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    clang::TagTypeKind TK_cast = (clang::TagTypeKind)TK;
    thiz_cast->setTagKind(TK_cast);
}

bool clang_TagDecl_is_struct(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isStruct();
}

bool clang_TagDecl_is_interface(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isInterface();
}

bool clang_TagDecl_is_class(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isClass();
}

bool clang_TagDecl_is_union(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isUnion();
}

bool clang_TagDecl_is_enum(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isEnum();
}

bool clang_TagDecl_is_structure_or_class(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->isStructureOrClass();
}

bool clang_TagDecl_has_name_for_linkage(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->hasNameForLinkage();
}

unsigned int clang_TagDecl_get_num_template_parameter_lists(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->getNumTemplateParameterLists();
}

bool clang_TagDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TagDecl::classof(D_cast);
}

bool clang_TagDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TagDecl::classofKind(K_cast);
}

void* clang_TagDecl_cast_to_decl_context(void* D) {
    const clang::TagDecl* D_cast = reinterpret_cast<const clang::TagDecl*>(D);
    return (void*)clang::TagDecl::castToDeclContext(D_cast);
}

void* clang_TagDecl_cast_from_decl_context(void* DC) {
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return (void*)clang::TagDecl::castFromDeclContext(DC_cast);
}

const char* clang_TagDecl_get_kind_name(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    std::string ret_value = thiz_cast->getKindName().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

int clang_TagDecl_size_of() {
    return sizeof(clang::TagDecl);
}

int clang_TagDecl_align_of() {
    return alignof(clang::TagDecl);
}

void* clang_TagDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::TagDecl*>(p));
}


// END KRAPPER GEN for clang::TagDecl


// BEGIN KRAPPER GEN for clang::QualType

void* clang_QualType_new(void* location) {
    return new (location) clang::QualType();
}

unsigned int clang_QualType_get_local_fast_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->getLocalFastQualifiers();
}

void clang_QualType_set_local_fast_qualifiers(void* thiz, unsigned int Quals) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->setLocalFastQualifiers(Quals);
}

bool clang_QualType_use_excess_precision(void* thiz, void* Ctx) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->UseExcessPrecision(*Ctx_cast);
}

const void* clang_QualType_get_as_opaque_ptr(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->getAsOpaquePtr();
}

void clang_QualType_get_from_opaque_ptr(const void* Ptr, void* ret_value) {
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(clang::QualType::getFromOpaquePtr(Ptr));
}

bool clang_QualType_is_canonical(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isCanonical();
}

bool clang_QualType_is_canonical_as_param(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isCanonicalAsParam();
}

bool clang_QualType_is_null(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isNull();
}

bool clang_QualType_is_referenceable(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isReferenceable();
}

bool clang_QualType_is_local_const_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isLocalConstQualified();
}

bool clang_QualType_is_const_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isConstQualified();
}

bool clang_QualType_is_constant_storage(void* thiz, void* Ctx, bool ExcludeCtor, bool ExcludeDtor) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isConstantStorage(*Ctx_cast, ExcludeCtor, ExcludeDtor);
}

bool clang_QualType_is_local_restrict_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isLocalRestrictQualified();
}

bool clang_QualType_is_restrict_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isRestrictQualified();
}

bool clang_QualType_is_local_volatile_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isLocalVolatileQualified();
}

bool clang_QualType_is_volatile_qualified(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isVolatileQualified();
}

bool clang_QualType_has_local_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasLocalQualifiers();
}

bool clang_QualType_has_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasQualifiers();
}

bool clang_QualType_has_local_non_fast_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasLocalNonFastQualifiers();
}

unsigned int clang_QualType_get_local_cvr_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->getLocalCVRQualifiers();
}

unsigned int clang_QualType_get_cvr_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->getCVRQualifiers();
}

bool clang_QualType_is_constant(void* thiz, void* Ctx) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isConstant(*Ctx_cast);
}

bool clang_QualType_is_pod_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isPODType(*Context_cast);
}

bool clang_QualType_is_cxx98pod_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isCXX98PODType(*Context_cast);
}

bool clang_QualType_is_cxx11pod_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isCXX11PODType(*Context_cast);
}

bool clang_QualType_is_trivial_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isTrivialType(*Context_cast);
}

bool clang_QualType_is_trivially_copyable_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isTriviallyCopyableType(*Context_cast);
}

bool clang_QualType_is_bitwise_cloneable_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isBitwiseCloneableType(*Context_cast);
}

bool clang_QualType_is_trivially_copy_constructible_type(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isTriviallyCopyConstructibleType(*Context_cast);
}

bool clang_QualType_may_be_dynamic_class(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->mayBeDynamicClass();
}

bool clang_QualType_may_be_not_dynamic_class(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->mayBeNotDynamicClass();
}

bool clang_QualType_is_web_assembly_reference_type(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isWebAssemblyReferenceType();
}

bool clang_QualType_is_web_assembly_externref_type(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isWebAssemblyExternrefType();
}

bool clang_QualType_is_web_assembly_funcref_type(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isWebAssemblyFuncrefType();
}

void clang_QualType_add_const(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->addConst();
}

void clang_QualType_with_const(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withConst());
}

void clang_QualType_add_volatile(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->addVolatile();
}

void clang_QualType_with_volatile(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withVolatile());
}

void clang_QualType_add_restrict(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->addRestrict();
}

void clang_QualType_with_restrict(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withRestrict());
}

void clang_QualType_with_cvr_qualifiers(void* thiz, unsigned int CVR, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withCVRQualifiers(CVR));
}

void clang_QualType_add_fast_qualifiers(void* thiz, unsigned int TQs) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->addFastQualifiers(TQs);
}

void clang_QualType_remove_local_const(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->removeLocalConst();
}

void clang_QualType_remove_local_volatile(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->removeLocalVolatile();
}

void clang_QualType_remove_local_restrict(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->removeLocalRestrict();
}

void clang_QualType_remove_local_fast_qualifiers(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->removeLocalFastQualifiers();
}

void clang_QualType_remove_local_fast_qualifiers__unsigned_int(void* thiz, unsigned int Mask) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->removeLocalFastQualifiers(Mask);
}

void clang_QualType_with_fast_qualifiers(void* thiz, unsigned int TQs, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withFastQualifiers(TQs));
}

void clang_QualType_with_exact_local_fast_qualifiers(void* thiz, unsigned int TQs, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withExactLocalFastQualifiers(TQs));
}

void clang_QualType_without_local_fast_qualifiers(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->withoutLocalFastQualifiers());
}

void clang_QualType_get_canonical_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCanonicalType());
}

void clang_QualType_get_local_unqualified_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getLocalUnqualifiedType());
}

void clang_QualType_get_unqualified_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnqualifiedType());
}

bool clang_QualType_is_more_qualified_than(void* thiz, void* Other, void* Ctx) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* Other_cast = reinterpret_cast<clang::QualType*>(Other);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isMoreQualifiedThan(*Other_cast, *Ctx_cast);
}

bool clang_QualType_is_at_least_as_qualified_as(void* thiz, void* Other, void* Ctx) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* Other_cast = reinterpret_cast<clang::QualType*>(Other);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isAtLeastAsQualifiedAs(*Other_cast, *Ctx_cast);
}

void clang_QualType_get_non_reference_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getNonReferenceType());
}

void clang_QualType_get_non_l_value_expr_type(void* thiz, void* Context, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getNonLValueExprType(*Context_cast));
}

void clang_QualType_get_non_pack_expansion_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getNonPackExpansionType());
}

void clang_QualType_get_desugared_type(void* thiz, void* Context, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getDesugaredType(*Context_cast));
}

void clang_QualType_get_single_step_desugared_type(void* thiz, void* Context, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSingleStepDesugaredType(*Context_cast));
}

void clang_QualType_ignore_parens(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->IgnoreParens());
}

const char* _clang_QualType_get_as_string(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    std::string ret_value = thiz_cast->getAsString();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

void clang_QualType_dump(void* thiz, const char* s) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->dump(s);
}

void _clang_QualType_dump(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    thiz_cast->dump();
}

bool clang_QualType_has_address_space(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasAddressSpace();
}

unsigned int clang_QualType_get_address_space(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->getAddressSpace();
}

bool clang_QualType_is_address_space_overlapping(void* thiz, void* T, void* Ctx) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isAddressSpaceOverlapping(*T_cast, *Ctx_cast);
}

unsigned int clang_QualType_get_obj_cgc_attr(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->getObjCGCAttr();
}

bool clang_QualType_is_obj_cgc_weak(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isObjCGCWeak();
}

bool clang_QualType_is_obj_cgc_strong(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isObjCGCStrong();
}

unsigned int clang_QualType_get_obj_c_lifetime(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->getObjCLifetime();
}

bool clang_QualType_has_non_trivial_obj_c_lifetime(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasNonTrivialObjCLifetime();
}

bool clang_QualType_has_strong_or_weak_obj_c_lifetime(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasStrongOrWeakObjCLifetime();
}

bool clang_QualType_is_non_weak_in_mrr_with_obj_c_weak(void* thiz, void* Context) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->isNonWeakInMRRWithObjCWeak(*Context_cast);
}

bool clang_QualType_has_address_discriminated_pointer_auth(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasAddressDiscriminatedPointerAuth();
}

unsigned int clang_QualType_is_non_trivial_to_primitive_default_initialize(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->isNonTrivialToPrimitiveDefaultInitialize();
}

unsigned int clang_QualType_is_non_trivial_to_primitive_copy(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->isNonTrivialToPrimitiveCopy();
}

unsigned int clang_QualType_is_non_trivial_to_primitive_destructive_move(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->isNonTrivialToPrimitiveDestructiveMove();
}

unsigned int clang_QualType_is_destructed_type(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (unsigned int)thiz_cast->isDestructedType();
}

bool clang_QualType_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveDefaultInitializeCUnion();
}

bool clang_QualType_has_non_trivial_to_primitive_destruct_c_union(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveDestructCUnion();
}

bool clang_QualType_has_non_trivial_to_primitive_copy_c_union(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveCopyCUnion();
}

bool clang_QualType_is_c_forbidden_l_value_type(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->isCForbiddenLValueType();
}

void clang_QualType_subst_obj_c_member_type(void* thiz, void* objectType, void* dc, int context, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* objectType_cast = reinterpret_cast<clang::QualType*>(objectType);
    const clang::DeclContext* dc_cast = reinterpret_cast<const clang::DeclContext*>(dc);
    clang::ObjCSubstitutionContext context_cast = (clang::ObjCSubstitutionContext)context;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->substObjCMemberType(*objectType_cast, dc_cast, context_cast));
}

void clang_QualType_strip_obj_c_kind_of_type(void* thiz, void* ctx, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    const clang::ASTContext* ctx_cast = reinterpret_cast<const clang::ASTContext*>(ctx);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->stripObjCKindOfType(*ctx_cast));
}

void clang_QualType_get_atomic_unqualified_type(void* thiz, void* ret_value) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAtomicUnqualifiedType());
}

int clang_QualType_size_of() {
    return sizeof(clang::QualType);
}

int clang_QualType_align_of() {
    return alignof(clang::QualType);
}


// END KRAPPER GEN for clang::QualType


// BEGIN KRAPPER GEN for clang::ValueDecl

void clang_ValueDecl_get_type(void* thiz, void* ret_value) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getType());
}

void clang_ValueDecl_set_type(void* thiz, void* newType) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    clang::QualType* newType_cast = reinterpret_cast<clang::QualType*>(newType);
    thiz_cast->setType(*newType_cast);
}

bool clang_ValueDecl_is_weak(void* thiz) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    return thiz_cast->isWeak();
}

bool clang_ValueDecl_is_init_capture(void* thiz) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    return thiz_cast->isInitCapture();
}

bool clang_ValueDecl_is_parameter_pack(void* thiz) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    return thiz_cast->isParameterPack();
}

bool clang_ValueDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::ValueDecl::classof(D_cast);
}

bool clang_ValueDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::ValueDecl::classofKind(K_cast);
}

int clang_ValueDecl_size_of() {
    return sizeof(clang::ValueDecl);
}

int clang_ValueDecl_align_of() {
    return alignof(clang::ValueDecl);
}

void* clang_ValueDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_DeclaratorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::DeclaratorDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}


// END KRAPPER GEN for clang::ValueDecl


// BEGIN KRAPPER GEN for clang::DeclaratorDecl

unsigned int clang_DeclaratorDecl_get_num_template_parameter_lists(void* thiz) {
    clang::DeclaratorDecl* thiz_cast = reinterpret_cast<clang::DeclaratorDecl*>(thiz);
    return thiz_cast->getNumTemplateParameterLists();
}

bool clang_DeclaratorDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::DeclaratorDecl::classof(D_cast);
}

bool clang_DeclaratorDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::DeclaratorDecl::classofKind(K_cast);
}

int clang_DeclaratorDecl_size_of() {
    return sizeof(clang::DeclaratorDecl);
}

int clang_DeclaratorDecl_align_of() {
    return alignof(clang::DeclaratorDecl);
}

void* clang_DeclaratorDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}


// END KRAPPER GEN for clang::DeclaratorDecl


// BEGIN KRAPPER GEN for clang::FieldDecl

unsigned int clang_FieldDecl_get_field_index(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->getFieldIndex();
}

bool clang_FieldDecl_is_mutable(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isMutable();
}

bool clang_FieldDecl_is_bit_field(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isBitField();
}

bool clang_FieldDecl_is_unnamed_bit_field(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isUnnamedBitField();
}

bool clang_FieldDecl_is_anonymous_struct_or_union(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isAnonymousStructOrUnion();
}

bool clang_FieldDecl_has_constant_integer_bit_width(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->hasConstantIntegerBitWidth();
}

unsigned int clang_FieldDecl_get_bit_width_value(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->getBitWidthValue();
}

void clang_FieldDecl_remove_bit_width(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    thiz_cast->removeBitWidth();
}

bool clang_FieldDecl_is_zero_length_bit_field(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isZeroLengthBitField();
}

bool clang_FieldDecl_is_zero_size(void* thiz, void* Ctx) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isZeroSize(*Ctx_cast);
}

bool clang_FieldDecl_is_potentially_overlapping(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->isPotentiallyOverlapping();
}

unsigned int clang_FieldDecl_get_in_class_init_style(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return (unsigned int)thiz_cast->getInClassInitStyle();
}

bool clang_FieldDecl_has_in_class_initializer(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->hasInClassInitializer();
}

bool clang_FieldDecl_has_non_null_in_class_initializer(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->hasNonNullInClassInitializer();
}

void* clang_FieldDecl_find_counted_by_field(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return (void*)thiz_cast->findCountedByField();
}

void clang_FieldDecl_remove_in_class_initializer(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    thiz_cast->removeInClassInitializer();
}

bool clang_FieldDecl_has_captured_vla_type(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return thiz_cast->hasCapturedVLAType();
}

void* clang_FieldDecl_get_parent(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return (void*)thiz_cast->getParent();
}

void* clang_FieldDecl_get_canonical_decl(void* thiz) {
    clang::FieldDecl* thiz_cast = reinterpret_cast<clang::FieldDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_FieldDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::FieldDecl::classof(D_cast);
}

bool clang_FieldDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::FieldDecl::classofKind(K_cast);
}

int clang_FieldDecl_size_of() {
    return sizeof(clang::FieldDecl);
}

int clang_FieldDecl_align_of() {
    return alignof(clang::FieldDecl);
}

void* clang_FieldDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::FieldDecl*>(p));
}

void* clang_FieldDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::FieldDecl*>(p));
}

void* clang_FieldDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::FieldDecl*>(p));
}

void* clang_FieldDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::FieldDecl*>(p));
}


// END KRAPPER GEN for clang::FieldDecl


// BEGIN KRAPPER GEN for clang::RecordDecl

void* clang_RecordDecl_get_previous_decl(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (void*)thiz_cast->getPreviousDecl();
}

void* clang_RecordDecl_get_most_recent_decl(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

bool clang_RecordDecl_has_flexible_array_member(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasFlexibleArrayMember();
}

void clang_RecordDecl_set_has_flexible_array_member(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasFlexibleArrayMember(V);
}

bool clang_RecordDecl_is_anonymous_struct_or_union(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isAnonymousStructOrUnion();
}

void clang_RecordDecl_set_anonymous_struct_or_union(void* thiz, bool Anon) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setAnonymousStructOrUnion(Anon);
}

bool clang_RecordDecl_has_object_member(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasObjectMember();
}

void clang_RecordDecl_set_has_object_member(void* thiz, bool val) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasObjectMember(val);
}

bool clang_RecordDecl_has_volatile_member(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasVolatileMember();
}

void clang_RecordDecl_set_has_volatile_member(void* thiz, bool val) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasVolatileMember(val);
}

bool clang_RecordDecl_has_loaded_fields_from_external_storage(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasLoadedFieldsFromExternalStorage();
}

void clang_RecordDecl_set_has_loaded_fields_from_external_storage(void* thiz, bool val) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasLoadedFieldsFromExternalStorage(val);
}

bool clang_RecordDecl_is_non_trivial_to_primitive_default_initialize(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isNonTrivialToPrimitiveDefaultInitialize();
}

void clang_RecordDecl_set_non_trivial_to_primitive_default_initialize(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setNonTrivialToPrimitiveDefaultInitialize(V);
}

bool clang_RecordDecl_is_non_trivial_to_primitive_copy(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isNonTrivialToPrimitiveCopy();
}

void clang_RecordDecl_set_non_trivial_to_primitive_copy(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setNonTrivialToPrimitiveCopy(V);
}

bool clang_RecordDecl_is_non_trivial_to_primitive_destroy(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isNonTrivialToPrimitiveDestroy();
}

void clang_RecordDecl_set_non_trivial_to_primitive_destroy(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setNonTrivialToPrimitiveDestroy(V);
}

bool clang_RecordDecl_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveDefaultInitializeCUnion();
}

void clang_RecordDecl_set_has_non_trivial_to_primitive_default_initialize_c_union(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasNonTrivialToPrimitiveDefaultInitializeCUnion(V);
}

bool clang_RecordDecl_has_non_trivial_to_primitive_destruct_c_union(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveDestructCUnion();
}

void clang_RecordDecl_set_has_non_trivial_to_primitive_destruct_c_union(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasNonTrivialToPrimitiveDestructCUnion(V);
}

bool clang_RecordDecl_has_non_trivial_to_primitive_copy_c_union(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialToPrimitiveCopyCUnion();
}

void clang_RecordDecl_set_has_non_trivial_to_primitive_copy_c_union(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasNonTrivialToPrimitiveCopyCUnion(V);
}

bool clang_RecordDecl_has_uninitialized_explicit_init_fields(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->hasUninitializedExplicitInitFields();
}

void clang_RecordDecl_set_has_uninitialized_explicit_init_fields(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setHasUninitializedExplicitInitFields(V);
}

bool clang_RecordDecl_can_pass_in_registers(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->canPassInRegisters();
}

int clang_RecordDecl_get_arg_passing_restrictions(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (int)thiz_cast->getArgPassingRestrictions();
}

void clang_RecordDecl_set_arg_passing_restrictions(void* thiz, int Kind) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    clang::RecordArgPassingKind Kind_cast = (clang::RecordArgPassingKind)Kind;
    thiz_cast->setArgPassingRestrictions(Kind_cast);
}

bool clang_RecordDecl_is_param_destroyed_in_callee(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isParamDestroyedInCallee();
}

void clang_RecordDecl_set_param_destroyed_in_callee(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setParamDestroyedInCallee(V);
}

bool clang_RecordDecl_is_randomized(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isRandomized();
}

void clang_RecordDecl_set_is_randomized(void* thiz, bool V) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setIsRandomized(V);
}

bool clang_RecordDecl_is_lambda(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isLambda();
}

bool clang_RecordDecl_is_captured_record(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isCapturedRecord();
}

void clang_RecordDecl_set_captured_record(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->setCapturedRecord();
}

const void* clang_RecordDecl_get_definition(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

const void* clang_RecordDecl_get_definition_or_self(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (void*)thiz_cast->getDefinitionOrSelf();
}

bool clang_RecordDecl_is_or_contains_union(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->isOrContainsUnion();
}

bool clang_RecordDecl_field_empty(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->field_empty();
}

unsigned int clang_RecordDecl_get_num_fields(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->getNumFields();
}

bool clang_RecordDecl_noload_field_empty(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->noload_field_empty();
}

void clang_RecordDecl_complete_definition(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    thiz_cast->completeDefinition();
}

bool clang_RecordDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::RecordDecl::classof(D_cast);
}

bool clang_RecordDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::RecordDecl::classofKind(K_cast);
}

bool clang_RecordDecl_is_ms_struct(void* thiz, void* C) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    const clang::ASTContext* C_cast = reinterpret_cast<const clang::ASTContext*>(C);
    return thiz_cast->isMsStruct(*C_cast);
}

bool clang_RecordDecl_may_insert_extra_padding(void* thiz, bool EmitRemark) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->mayInsertExtraPadding(EmitRemark);
}

void* clang_RecordDecl_find_first_named_data_member(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return (void*)thiz_cast->findFirstNamedDataMember();
}

unsigned int clang_RecordDecl_get_odr_hash(void* thiz) {
    clang::RecordDecl* thiz_cast = reinterpret_cast<clang::RecordDecl*>(thiz);
    return thiz_cast->getODRHash();
}

int clang_RecordDecl_size_of() {
    return sizeof(clang::RecordDecl);
}

int clang_RecordDecl_align_of() {
    return alignof(clang::RecordDecl);
}

void* clang_RecordDecl_as_clang_TagDecl(void* p) {
    return static_cast<clang::TagDecl*>(reinterpret_cast<clang::RecordDecl*>(p));
}

void* clang_RecordDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::RecordDecl*>(p));
}

void* clang_RecordDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::RecordDecl*>(p));
}

void* clang_RecordDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::RecordDecl*>(p));
}

void* clang_RecordDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::RecordDecl*>(p));
}

void* clang_RecordDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::RecordDecl*>(p));
}


// END KRAPPER GEN for clang::RecordDecl


// BEGIN KRAPPER GEN for clang::CXXBaseSpecifier

void* clang_CXXBaseSpecifier_new(void* location) {
    return new (location) clang::CXXBaseSpecifier();
}

bool clang_CXXBaseSpecifier_is_virtual(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return thiz_cast->isVirtual();
}

bool clang_CXXBaseSpecifier_is_base_of_class(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return thiz_cast->isBaseOfClass();
}

bool clang_CXXBaseSpecifier_is_pack_expansion(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return thiz_cast->isPackExpansion();
}

bool clang_CXXBaseSpecifier_get_inherit_constructors(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return thiz_cast->getInheritConstructors();
}

void clang_CXXBaseSpecifier_set_inherit_constructors(void* thiz, bool Inherit) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    thiz_cast->setInheritConstructors(Inherit);
}

unsigned char clang_CXXBaseSpecifier_get_access_specifier(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return (unsigned char)thiz_cast->getAccessSpecifier();
}

unsigned char clang_CXXBaseSpecifier_get_access_specifier_as_written(void* thiz) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    return (unsigned char)thiz_cast->getAccessSpecifierAsWritten();
}

void clang_CXXBaseSpecifier_get_type(void* thiz, void* ret_value) {
    clang::CXXBaseSpecifier* thiz_cast = reinterpret_cast<clang::CXXBaseSpecifier*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getType());
}

int clang_CXXBaseSpecifier_size_of() {
    return sizeof(clang::CXXBaseSpecifier);
}

int clang_CXXBaseSpecifier_align_of() {
    return alignof(clang::CXXBaseSpecifier);
}


// END KRAPPER GEN for clang::CXXBaseSpecifier


// BEGIN KRAPPER GEN for clang::FunctionDecl

bool clang_FunctionDecl_has_body(void* thiz, void* Definition) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    const clang::FunctionDecl* Definition_cast = reinterpret_cast<const clang::FunctionDecl*>(Definition);
    return thiz_cast->hasBody(Definition_cast);
}

bool _clang_FunctionDecl_has_body(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasBody();
}

bool clang_FunctionDecl_has_trivial_body(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasTrivialBody();
}

bool clang_FunctionDecl_is_defined(void* thiz, void* Definition, bool CheckForPendingFriendDefinition) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    const clang::FunctionDecl* Definition_cast = reinterpret_cast<const clang::FunctionDecl*>(Definition);
    return thiz_cast->isDefined(Definition_cast, CheckForPendingFriendDefinition);
}

bool _clang_FunctionDecl_is_defined(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isDefined();
}

void* clang_FunctionDecl_get_definition(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

bool clang_FunctionDecl_is_this_declaration_a_definition(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isThisDeclarationADefinition();
}

bool clang_FunctionDecl_is_this_declaration_instantiated_from_a_friend_definition(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isThisDeclarationInstantiatedFromAFriendDefinition();
}

bool clang_FunctionDecl_does_this_declaration_have_a_body(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->doesThisDeclarationHaveABody();
}

void clang_FunctionDecl_set_lazy_body(void* thiz, unsigned long Offset) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setLazyBody(Offset);
}

bool clang_FunctionDecl_is_variadic(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isVariadic();
}

bool clang_FunctionDecl_is_virtual_as_written(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isVirtualAsWritten();
}

void clang_FunctionDecl_set_virtual_as_written(void* thiz, bool V) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setVirtualAsWritten(V);
}

bool clang_FunctionDecl_is_pure_virtual(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isPureVirtual();
}

void clang_FunctionDecl_set_is_pure_virtual(void* thiz, bool P) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setIsPureVirtual(P);
}

bool clang_FunctionDecl_is_late_template_parsed(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isLateTemplateParsed();
}

void clang_FunctionDecl_set_late_template_parsed(void* thiz, bool ILT) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setLateTemplateParsed(ILT);
}

bool clang_FunctionDecl_is_instantiated_from_member_template(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInstantiatedFromMemberTemplate();
}

void clang_FunctionDecl_set_instantiated_from_member_template(void* thiz, bool Val) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setInstantiatedFromMemberTemplate(Val);
}

bool clang_FunctionDecl_is_trivial(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTrivial();
}

void clang_FunctionDecl_set_trivial(void* thiz, bool IT) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setTrivial(IT);
}

bool clang_FunctionDecl_is_trivial_for_call(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTrivialForCall();
}

void clang_FunctionDecl_set_trivial_for_call(void* thiz, bool IT) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setTrivialForCall(IT);
}

bool clang_FunctionDecl_is_defaulted(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isDefaulted();
}

void clang_FunctionDecl_set_defaulted(void* thiz, bool D) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setDefaulted(D);
}

bool clang_FunctionDecl_is_explicitly_defaulted(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isExplicitlyDefaulted();
}

void clang_FunctionDecl_set_explicitly_defaulted(void* thiz, bool ED) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setExplicitlyDefaulted(ED);
}

bool clang_FunctionDecl_is_user_provided(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isUserProvided();
}

bool clang_FunctionDecl_is_ineligible_or_not_selected(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isIneligibleOrNotSelected();
}

void clang_FunctionDecl_set_ineligible_or_not_selected(void* thiz, bool II) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setIneligibleOrNotSelected(II);
}

bool clang_FunctionDecl_has_implicit_return_zero(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasImplicitReturnZero();
}

void clang_FunctionDecl_set_has_implicit_return_zero(void* thiz, bool IRZ) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setHasImplicitReturnZero(IRZ);
}

bool clang_FunctionDecl_has_prototype(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasPrototype();
}

bool clang_FunctionDecl_has_written_prototype(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasWrittenPrototype();
}

void clang_FunctionDecl_set_has_written_prototype(void* thiz, bool P) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setHasWrittenPrototype(P);
}

bool clang_FunctionDecl_has_inherited_prototype(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasInheritedPrototype();
}

void clang_FunctionDecl_set_has_inherited_prototype(void* thiz, bool P) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setHasInheritedPrototype(P);
}

bool clang_FunctionDecl_is_constexpr(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isConstexpr();
}

void clang_FunctionDecl_set_constexpr_kind(void* thiz, int CSK) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::ConstexprSpecKind CSK_cast = (clang::ConstexprSpecKind)CSK;
    thiz_cast->setConstexprKind(CSK_cast);
}

int clang_FunctionDecl_get_constexpr_kind(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (int)thiz_cast->getConstexprKind();
}

bool clang_FunctionDecl_is_constexpr_specified(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isConstexprSpecified();
}

bool clang_FunctionDecl_is_consteval(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isConsteval();
}

void clang_FunctionDecl_set_body_contains_immediate_escalating_expressions(void* thiz, bool Set) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setBodyContainsImmediateEscalatingExpressions(Set);
}

bool clang_FunctionDecl_body_contains_immediate_escalating_expressions(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->BodyContainsImmediateEscalatingExpressions();
}

bool clang_FunctionDecl_is_immediate_escalating(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isImmediateEscalating();
}

bool clang_FunctionDecl_is_immediate_function(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isImmediateFunction();
}

bool clang_FunctionDecl_instantiation_is_pending(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->instantiationIsPending();
}

void clang_FunctionDecl_set_instantiation_is_pending(void* thiz, bool IC) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setInstantiationIsPending(IC);
}

bool clang_FunctionDecl_uses_seh_try(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->usesSEHTry();
}

void clang_FunctionDecl_set_uses_seh_try(void* thiz, bool UST) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setUsesSEHTry(UST);
}

bool clang_FunctionDecl_is_deleted(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isDeleted();
}

bool clang_FunctionDecl_is_deleted_as_written(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isDeletedAsWritten();
}

void clang_FunctionDecl_set_deleted_as_written(void* thiz, bool D) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setDeletedAsWritten(D);
}

bool clang_FunctionDecl_is_main(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isMain();
}

bool clang_FunctionDecl_is_msvcrt_entry_point(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isMSVCRTEntryPoint();
}

bool clang_FunctionDecl_is_reserved_global_placement_operator(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isReservedGlobalPlacementOperator();
}

bool clang_FunctionDecl_is_replaceable_global_allocation_function(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isReplaceableGlobalAllocationFunction();
}

bool clang_FunctionDecl_is_usable_as_global_allocation_function_in_constant_evaluation(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isUsableAsGlobalAllocationFunctionInConstantEvaluation();
}

bool clang_FunctionDecl_is_inline_builtin_declaration(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInlineBuiltinDeclaration();
}

bool clang_FunctionDecl_is_destroying_operator_delete(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isDestroyingOperatorDelete();
}

void clang_FunctionDecl_set_is_destroying_operator_delete(void* thiz, bool IsDestroyingDelete) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setIsDestroyingOperatorDelete(IsDestroyingDelete);
}

bool clang_FunctionDecl_is_type_aware_operator_new_or_delete(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTypeAwareOperatorNewOrDelete();
}

void clang_FunctionDecl_set_is_type_aware_operator_new_or_delete(void* thiz, bool IsTypeAwareOperator) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setIsTypeAwareOperatorNewOrDelete(IsTypeAwareOperator);
}

unsigned int clang_FunctionDecl_get_language_linkage(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getLanguageLinkage();
}

bool clang_FunctionDecl_is_extern_c(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isExternC();
}

bool clang_FunctionDecl_is_in_extern_c_context(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInExternCContext();
}

bool clang_FunctionDecl_is_in_extern_cxx_context(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInExternCXXContext();
}

bool clang_FunctionDecl_is_global(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isGlobal();
}

bool clang_FunctionDecl_is_no_return(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isNoReturn();
}

bool clang_FunctionDecl_is_analyzer_no_return(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isAnalyzerNoReturn();
}

bool clang_FunctionDecl_has_skipped_body(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasSkippedBody();
}

void clang_FunctionDecl_set_has_skipped_body(void* thiz, bool Skipped) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setHasSkippedBody(Skipped);
}

bool clang_FunctionDecl_will_have_body(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->willHaveBody();
}

void clang_FunctionDecl_set_will_have_body(void* thiz, bool V) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setWillHaveBody(V);
}

bool clang_FunctionDecl_is_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isMultiVersion();
}

void clang_FunctionDecl_set_is_multi_version(void* thiz, bool V) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setIsMultiVersion(V);
}

void clang_FunctionDecl_set_friend_constraint_refers_to_enclosing_template(void* thiz, bool V) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setFriendConstraintRefersToEnclosingTemplate(V);
}

bool clang_FunctionDecl_friend_constraint_refers_to_enclosing_template(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->FriendConstraintRefersToEnclosingTemplate();
}

bool clang_FunctionDecl_is_member_like_constrained_friend(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isMemberLikeConstrainedFriend();
}

int clang_FunctionDecl_get_multi_version_kind(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (int)thiz_cast->getMultiVersionKind();
}

bool clang_FunctionDecl_is_cpu_dispatch_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isCPUDispatchMultiVersion();
}

bool clang_FunctionDecl_is_cpu_specific_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isCPUSpecificMultiVersion();
}

bool clang_FunctionDecl_is_target_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTargetMultiVersion();
}

bool clang_FunctionDecl_is_target_multi_version_default(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTargetMultiVersionDefault();
}

bool clang_FunctionDecl_is_target_clones_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTargetClonesMultiVersion();
}

bool clang_FunctionDecl_is_target_version_multi_version(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTargetVersionMultiVersion();
}

void clang_FunctionDecl_set_previous_declaration(void* thiz, void* PrevDecl) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::FunctionDecl* PrevDecl_cast = reinterpret_cast<clang::FunctionDecl*>(PrevDecl);
    thiz_cast->setPreviousDeclaration(PrevDecl_cast);
}

void* clang_FunctionDecl_get_canonical_decl(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

unsigned int clang_FunctionDecl_get_builtin_id(void* thiz, bool ConsiderWrapperFunctions) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getBuiltinID(ConsiderWrapperFunctions);
}

bool clang_FunctionDecl_param_empty(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->param_empty();
}

size_t clang_FunctionDecl_param_size(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->param_size();
}

unsigned int clang_FunctionDecl_get_num_params(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getNumParams();
}

unsigned int clang_FunctionDecl_get_min_required_arguments(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getMinRequiredArguments();
}

unsigned int clang_FunctionDecl_get_min_required_explicit_arguments(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getMinRequiredExplicitArguments();
}

bool clang_FunctionDecl_has_cxx_explicit_function_object_parameter(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasCXXExplicitFunctionObjectParameter();
}

unsigned int clang_FunctionDecl_get_num_non_object_params(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getNumNonObjectParams();
}

bool clang_FunctionDecl_has_one_param_or_default_args(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->hasOneParamOrDefaultArgs();
}

void clang_FunctionDecl_get_return_type(void* thiz, void* ret_value) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getReturnType());
}

void clang_FunctionDecl_get_declared_return_type(void* thiz, void* ret_value) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getDeclaredReturnType());
}

unsigned int clang_FunctionDecl_get_exception_spec_type(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getExceptionSpecType();
}

void clang_FunctionDecl_get_call_result_type(void* thiz, void* ret_value) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCallResultType());
}

unsigned int clang_FunctionDecl_get_storage_class(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getStorageClass();
}

void clang_FunctionDecl_set_storage_class(void* thiz, unsigned int SClass) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::StorageClass SClass_cast = (clang::StorageClass)SClass;
    thiz_cast->setStorageClass(SClass_cast);
}

bool clang_FunctionDecl_is_inline_specified(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInlineSpecified();
}

void clang_FunctionDecl_set_inline_specified(void* thiz, bool I) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setInlineSpecified(I);
}

bool clang_FunctionDecl_uses_fp_intrin(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->UsesFPIntrin();
}

void clang_FunctionDecl_set_uses_fp_intrin(void* thiz, bool I) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setUsesFPIntrin(I);
}

void clang_FunctionDecl_set_implicitly_inline(void* thiz, bool I) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    thiz_cast->setImplicitlyInline(I);
}

bool clang_FunctionDecl_is_inlined(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInlined();
}

bool clang_FunctionDecl_is_inline_definition_externally_visible(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isInlineDefinitionExternallyVisible();
}

bool clang_FunctionDecl_is_ms_extern_inline(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isMSExternInline();
}

bool clang_FunctionDecl_does_declaration_force_externally_visible_definition(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->doesDeclarationForceExternallyVisibleDefinition();
}

bool clang_FunctionDecl_is_static(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isStatic();
}

bool clang_FunctionDecl_is_overloaded_operator(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isOverloadedOperator();
}

int clang_FunctionDecl_get_overloaded_operator(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (int)thiz_cast->getOverloadedOperator();
}

const void* clang_FunctionDecl_get_instantiated_from_member_function(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromMemberFunction();
}

unsigned int clang_FunctionDecl_get_templated_kind(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplatedKind();
}

void clang_FunctionDecl_set_instantiation_of_member_function(void* thiz, void* FD, unsigned int TSK) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setInstantiationOfMemberFunction(FD_cast, TSK_cast);
}

void clang_FunctionDecl_set_instantiated_from_decl(void* thiz, void* FD) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    thiz_cast->setInstantiatedFromDecl(FD_cast);
}

const void* clang_FunctionDecl_get_instantiated_from_decl(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromDecl();
}

bool clang_FunctionDecl_is_function_template_specialization(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isFunctionTemplateSpecialization();
}

bool clang_FunctionDecl_is_implicitly_instantiable(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isImplicitlyInstantiable();
}

bool clang_FunctionDecl_is_template_instantiation(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isTemplateInstantiation();
}

const void* clang_FunctionDecl_get_template_instantiation_pattern(void* thiz, bool ForDefinition) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getTemplateInstantiationPattern(ForDefinition);
}

unsigned int clang_FunctionDecl_get_template_specialization_kind(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKind();
}

unsigned int clang_FunctionDecl_get_template_specialization_kind_for_instantiation(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKindForInstantiation();
}

void clang_FunctionDecl_set_template_specialization_kind(void* thiz, unsigned int TSK) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setTemplateSpecializationKind(TSK_cast);
}

bool clang_FunctionDecl_is_out_of_line(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isOutOfLine();
}

unsigned int clang_FunctionDecl_get_memory_function_kind(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getMemoryFunctionKind();
}

unsigned int clang_FunctionDecl_get_odr_hash(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->getODRHash();
}

bool clang_FunctionDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::FunctionDecl::classof(D_cast);
}

bool clang_FunctionDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::FunctionDecl::classofKind(K_cast);
}

void* clang_FunctionDecl_cast_to_decl_context(void* D) {
    const clang::FunctionDecl* D_cast = reinterpret_cast<const clang::FunctionDecl*>(D);
    return (void*)clang::FunctionDecl::castToDeclContext(D_cast);
}

void* clang_FunctionDecl_cast_from_decl_context(void* DC) {
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return (void*)clang::FunctionDecl::castFromDeclContext(DC_cast);
}

bool clang_FunctionDecl_is_referenceable_kernel(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return thiz_cast->isReferenceableKernel();
}

int clang_FunctionDecl_size_of() {
    return sizeof(clang::FunctionDecl);
}

int clang_FunctionDecl_align_of() {
    return alignof(clang::FunctionDecl);
}

void* clang_FunctionDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::FunctionDecl*>(p));
}


// END KRAPPER GEN for clang::FunctionDecl


// BEGIN KRAPPER GEN for clang::CXXMethodDecl

bool clang_CXXMethodDecl_is_static(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isStatic();
}

bool clang_CXXMethodDecl_is_instance(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isInstance();
}

bool clang_CXXMethodDecl_is_explicit_object_member_function(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isExplicitObjectMemberFunction();
}

bool clang_CXXMethodDecl_is_implicit_object_member_function(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isImplicitObjectMemberFunction();
}

bool clang_CXXMethodDecl_is_static_overloaded_operator(int OOK) {
    clang::OverloadedOperatorKind OOK_cast = (clang::OverloadedOperatorKind)OOK;
    return clang::CXXMethodDecl::isStaticOverloadedOperator(OOK_cast);
}

bool clang_CXXMethodDecl_is_const(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isConst();
}

bool clang_CXXMethodDecl_is_volatile(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isVolatile();
}

bool clang_CXXMethodDecl_is_virtual(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isVirtual();
}

bool clang_CXXMethodDecl_is_copy_assignment_operator(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isCopyAssignmentOperator();
}

bool clang_CXXMethodDecl_is_move_assignment_operator(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isMoveAssignmentOperator();
}

void* clang_CXXMethodDecl_get_canonical_decl(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

void* clang_CXXMethodDecl_get_most_recent_decl(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

void clang_CXXMethodDecl_add_overridden_method(void* thiz, void* MD) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    const clang::CXXMethodDecl* MD_cast = reinterpret_cast<const clang::CXXMethodDecl*>(MD);
    thiz_cast->addOverriddenMethod(MD_cast);
}

unsigned int clang_CXXMethodDecl_size_overridden_methods(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->size_overridden_methods();
}

void clang_CXXMethodDecl_overridden_methods(void* thiz, void* ret_value) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    std::vector<clang::CXXMethodDecl*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(ret_value);
    auto __range_0 = thiz_cast->overridden_methods();
    std::vector<clang::CXXMethodDecl*> __vec_0;
    for (auto __it_0 = __range_0.begin(); __it_0 != __range_0.end(); ++__it_0) __vec_0.push_back(kpp_to_elem_ptr<clang::CXXMethodDecl>(*__it_0));
    new (ret_value_cast) std::vector<clang::CXXMethodDecl*>(__vec_0);
}

void* clang_CXXMethodDecl_get_parent(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return (void*)thiz_cast->getParent();
}

void clang_CXXMethodDecl_get_this_type(void* thiz, void* ret_value) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getThisType());
}

void clang_CXXMethodDecl_get_function_object_parameter_reference_type(void* thiz, void* ret_value) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFunctionObjectParameterReferenceType());
}

void clang_CXXMethodDecl_get_function_object_parameter_type(void* thiz, void* ret_value) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFunctionObjectParameterType());
}

unsigned int clang_CXXMethodDecl_get_num_explicit_params(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->getNumExplicitParams();
}

unsigned int clang_CXXMethodDecl_get_ref_qualifier(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return (unsigned int)thiz_cast->getRefQualifier();
}

bool clang_CXXMethodDecl_has_inline_body(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->hasInlineBody();
}

bool clang_CXXMethodDecl_is_lambda_static_invoker(void* thiz) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    return thiz_cast->isLambdaStaticInvoker();
}

void* clang_CXXMethodDecl_get_corresponding_method_in_class(void* thiz, void* RD, bool MayBeBase) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return (void*)thiz_cast->getCorrespondingMethodInClass(RD_cast, MayBeBase);
}

void* clang_CXXMethodDecl_get_corresponding_method_declared_in_class(void* thiz, void* RD, bool MayBeBase) {
    clang::CXXMethodDecl* thiz_cast = reinterpret_cast<clang::CXXMethodDecl*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return (void*)thiz_cast->getCorrespondingMethodDeclaredInClass(RD_cast, MayBeBase);
}

bool clang_CXXMethodDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::CXXMethodDecl::classof(D_cast);
}

bool clang_CXXMethodDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::CXXMethodDecl::classofKind(K_cast);
}

int clang_CXXMethodDecl_size_of() {
    return sizeof(clang::CXXMethodDecl);
}

int clang_CXXMethodDecl_align_of() {
    return alignof(clang::CXXMethodDecl);
}

void* clang_CXXMethodDecl_as_clang_FunctionDecl(void* p) {
    return static_cast<clang::FunctionDecl*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}


// END KRAPPER GEN for clang::CXXMethodDecl


// BEGIN KRAPPER GEN for clang::CXXRecordDecl

void* clang_CXXRecordDecl_get_canonical_decl(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

void* clang_CXXRecordDecl_get_previous_decl(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getPreviousDecl();
}

void* clang_CXXRecordDecl_get_most_recent_decl(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

const void* clang_CXXRecordDecl_get_definition(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

const void* clang_CXXRecordDecl_get_definition_or_self(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getDefinitionOrSelf();
}

bool clang_CXXRecordDecl_has_definition(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasDefinition();
}

bool clang_CXXRecordDecl_is_dynamic_class(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isDynamicClass();
}

bool clang_CXXRecordDecl_may_be_dynamic_class(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->mayBeDynamicClass();
}

bool clang_CXXRecordDecl_may_be_non_dynamic_class(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->mayBeNonDynamicClass();
}

void clang_CXXRecordDecl_set_is_parsing_base_specifiers(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setIsParsingBaseSpecifiers();
}

bool clang_CXXRecordDecl_is_parsing_base_specifiers(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isParsingBaseSpecifiers();
}

unsigned int clang_CXXRecordDecl_get_odr_hash(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getODRHash();
}

unsigned int clang_CXXRecordDecl_get_num_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getNumBases();
}

void clang_CXXRecordDecl_bases(void* thiz, void* ret_value) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    std::vector<clang::CXXBaseSpecifier*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(ret_value);
    auto __range_1 = thiz_cast->bases();
    std::vector<clang::CXXBaseSpecifier*> __vec_1;
    for (auto __it_1 = __range_1.begin(); __it_1 != __range_1.end(); ++__it_1) __vec_1.push_back(kpp_to_elem_ptr<clang::CXXBaseSpecifier>(*__it_1));
    new (ret_value_cast) std::vector<clang::CXXBaseSpecifier*>(__vec_1);
}

unsigned int clang_CXXRecordDecl_get_num_v_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getNumVBases();
}

void clang_CXXRecordDecl_vbases(void* thiz, void* ret_value) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    std::vector<clang::CXXBaseSpecifier*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(ret_value);
    auto __range_2 = thiz_cast->vbases();
    std::vector<clang::CXXBaseSpecifier*> __vec_2;
    for (auto __it_2 = __range_2.begin(); __it_2 != __range_2.end(); ++__it_2) __vec_2.push_back(kpp_to_elem_ptr<clang::CXXBaseSpecifier>(*__it_2));
    new (ret_value_cast) std::vector<clang::CXXBaseSpecifier*>(__vec_2);
}

bool clang_CXXRecordDecl_has_any_dependent_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasAnyDependentBases();
}

void clang_CXXRecordDecl_methods(void* thiz, void* ret_value) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    std::vector<clang::CXXMethodDecl*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(ret_value);
    auto __range_3 = thiz_cast->methods();
    std::vector<clang::CXXMethodDecl*> __vec_3;
    for (auto __it_3 = __range_3.begin(); __it_3 != __range_3.end(); ++__it_3) __vec_3.push_back(kpp_to_elem_ptr<clang::CXXMethodDecl>(*__it_3));
    new (ret_value_cast) std::vector<clang::CXXMethodDecl*>(__vec_3);
}

bool clang_CXXRecordDecl_has_friends(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasFriends();
}

bool clang_CXXRecordDecl_defaulted_copy_constructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->defaultedCopyConstructorIsDeleted();
}

bool clang_CXXRecordDecl_defaulted_move_constructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->defaultedMoveConstructorIsDeleted();
}

bool clang_CXXRecordDecl_defaulted_destructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->defaultedDestructorIsDeleted();
}

bool clang_CXXRecordDecl_has_simple_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasSimpleCopyConstructor();
}

bool clang_CXXRecordDecl_has_simple_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasSimpleMoveConstructor();
}

bool clang_CXXRecordDecl_has_simple_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasSimpleCopyAssignment();
}

bool clang_CXXRecordDecl_has_simple_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasSimpleMoveAssignment();
}

bool clang_CXXRecordDecl_has_simple_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasSimpleDestructor();
}

bool clang_CXXRecordDecl_has_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasDefaultConstructor();
}

bool clang_CXXRecordDecl_needs_implicit_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitDefaultConstructor();
}

bool clang_CXXRecordDecl_has_user_declared_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredConstructor();
}

bool clang_CXXRecordDecl_has_user_provided_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserProvidedDefaultConstructor();
}

bool clang_CXXRecordDecl_has_user_declared_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredCopyConstructor();
}

bool clang_CXXRecordDecl_needs_implicit_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitCopyConstructor();
}

bool clang_CXXRecordDecl_needs_overload_resolution_for_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsOverloadResolutionForCopyConstructor();
}

bool clang_CXXRecordDecl_implicit_copy_constructor_has_const_param(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->implicitCopyConstructorHasConstParam();
}

bool clang_CXXRecordDecl_has_copy_constructor_with_const_param(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasCopyConstructorWithConstParam();
}

bool clang_CXXRecordDecl_has_user_declared_move_operation(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredMoveOperation();
}

bool clang_CXXRecordDecl_has_user_declared_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredMoveConstructor();
}

bool clang_CXXRecordDecl_has_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasMoveConstructor();
}

void clang_CXXRecordDecl_set_implicit_copy_constructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setImplicitCopyConstructorIsDeleted();
}

void clang_CXXRecordDecl_set_implicit_move_constructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setImplicitMoveConstructorIsDeleted();
}

void clang_CXXRecordDecl_set_implicit_destructor_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setImplicitDestructorIsDeleted();
}

bool clang_CXXRecordDecl_needs_implicit_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitMoveConstructor();
}

bool clang_CXXRecordDecl_needs_overload_resolution_for_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsOverloadResolutionForMoveConstructor();
}

bool clang_CXXRecordDecl_has_user_declared_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredCopyAssignment();
}

void clang_CXXRecordDecl_set_implicit_copy_assignment_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setImplicitCopyAssignmentIsDeleted();
}

bool clang_CXXRecordDecl_needs_implicit_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitCopyAssignment();
}

bool clang_CXXRecordDecl_needs_overload_resolution_for_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsOverloadResolutionForCopyAssignment();
}

bool clang_CXXRecordDecl_implicit_copy_assignment_has_const_param(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->implicitCopyAssignmentHasConstParam();
}

bool clang_CXXRecordDecl_has_copy_assignment_with_const_param(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasCopyAssignmentWithConstParam();
}

bool clang_CXXRecordDecl_has_user_declared_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredMoveAssignment();
}

bool clang_CXXRecordDecl_has_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasMoveAssignment();
}

void clang_CXXRecordDecl_set_implicit_move_assignment_is_deleted(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setImplicitMoveAssignmentIsDeleted();
}

bool clang_CXXRecordDecl_needs_implicit_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitMoveAssignment();
}

bool clang_CXXRecordDecl_needs_overload_resolution_for_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsOverloadResolutionForMoveAssignment();
}

bool clang_CXXRecordDecl_has_user_declared_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUserDeclaredDestructor();
}

bool clang_CXXRecordDecl_needs_implicit_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsImplicitDestructor();
}

bool clang_CXXRecordDecl_needs_overload_resolution_for_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->needsOverloadResolutionForDestructor();
}

bool clang_CXXRecordDecl_is_lambda(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isLambda();
}

bool clang_CXXRecordDecl_is_generic_lambda(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isGenericLambda();
}

bool clang_CXXRecordDecl_lambda_is_default_constructible_and_assignable(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->lambdaIsDefaultConstructibleAndAssignable();
}

const void* clang_CXXRecordDecl_get_lambda_call_operator(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getLambdaCallOperator();
}

const void* clang_CXXRecordDecl_get_lambda_static_invoker(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getLambdaStaticInvoker();
}

const void* clang_CXXRecordDecl_get_lambda_static_invoker__clang_CallingConv(void* thiz, unsigned int CC) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::CallingConv CC_cast = (clang::CallingConv)CC;
    return (void*)thiz_cast->getLambdaStaticInvoker(CC_cast);
}

unsigned int clang_CXXRecordDecl_get_lambda_capture_default(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (unsigned int)thiz_cast->getLambdaCaptureDefault();
}

bool clang_CXXRecordDecl_is_captureless_lambda(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isCapturelessLambda();
}

unsigned int clang_CXXRecordDecl_capture_size(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->capture_size();
}

void clang_CXXRecordDecl_remove_conversion(void* thiz, void* Old) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::NamedDecl* Old_cast = reinterpret_cast<const clang::NamedDecl*>(Old);
    thiz_cast->removeConversion(Old_cast);
}

bool clang_CXXRecordDecl_is_aggregate(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isAggregate();
}

bool clang_CXXRecordDecl_has_in_class_initializer(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasInClassInitializer();
}

bool clang_CXXRecordDecl_has_uninitialized_reference_member(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasUninitializedReferenceMember();
}

bool clang_CXXRecordDecl_is_pod(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isPOD();
}

bool clang_CXXRecordDecl_is_c_like(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isCLike();
}

bool clang_CXXRecordDecl_is_empty(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isEmpty();
}

void clang_CXXRecordDecl_set_init_method(void* thiz, bool Val) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setInitMethod(Val);
}

bool clang_CXXRecordDecl_has_init_method(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasInitMethod();
}

bool clang_CXXRecordDecl_has_private_fields(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasPrivateFields();
}

bool clang_CXXRecordDecl_has_protected_fields(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasProtectedFields();
}

bool clang_CXXRecordDecl_has_direct_fields(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasDirectFields();
}

void* clang_CXXRecordDecl_get_standard_layout_base_with_fields(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getStandardLayoutBaseWithFields();
}

bool clang_CXXRecordDecl_is_polymorphic(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isPolymorphic();
}

bool clang_CXXRecordDecl_is_abstract(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isAbstract();
}

bool clang_CXXRecordDecl_is_standard_layout(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isStandardLayout();
}

bool clang_CXXRecordDecl_is_cxx11standard_layout(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isCXX11StandardLayout();
}

bool clang_CXXRecordDecl_has_mutable_fields(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasMutableFields();
}

bool clang_CXXRecordDecl_has_variant_members(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasVariantMembers();
}

bool clang_CXXRecordDecl_has_trivial_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialDefaultConstructor();
}

bool clang_CXXRecordDecl_has_non_trivial_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialDefaultConstructor();
}

bool clang_CXXRecordDecl_has_constexpr_non_copy_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasConstexprNonCopyMoveConstructor();
}

bool clang_CXXRecordDecl_defaulted_default_constructor_is_constexpr(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->defaultedDefaultConstructorIsConstexpr();
}

bool clang_CXXRecordDecl_has_constexpr_default_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasConstexprDefaultConstructor();
}

bool clang_CXXRecordDecl_has_trivial_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialCopyConstructor();
}

bool clang_CXXRecordDecl_has_trivial_copy_constructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialCopyConstructorForCall();
}

bool clang_CXXRecordDecl_has_non_trivial_copy_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialCopyConstructor();
}

bool clang_CXXRecordDecl_has_non_trivial_copy_constructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialCopyConstructorForCall();
}

bool clang_CXXRecordDecl_has_trivial_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialMoveConstructor();
}

bool clang_CXXRecordDecl_has_trivial_move_constructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialMoveConstructorForCall();
}

bool clang_CXXRecordDecl_has_non_trivial_move_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialMoveConstructor();
}

bool clang_CXXRecordDecl_has_non_trivial_move_constructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialMoveConstructorForCall();
}

bool clang_CXXRecordDecl_has_trivial_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialCopyAssignment();
}

bool clang_CXXRecordDecl_has_non_trivial_copy_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialCopyAssignment();
}

bool clang_CXXRecordDecl_has_trivial_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialMoveAssignment();
}

bool clang_CXXRecordDecl_has_non_trivial_move_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialMoveAssignment();
}

bool clang_CXXRecordDecl_defaulted_destructor_is_constexpr(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->defaultedDestructorIsConstexpr();
}

bool clang_CXXRecordDecl_has_constexpr_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasConstexprDestructor();
}

bool clang_CXXRecordDecl_has_trivial_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialDestructor();
}

bool clang_CXXRecordDecl_has_trivial_destructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasTrivialDestructorForCall();
}

bool clang_CXXRecordDecl_has_non_trivial_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialDestructor();
}

bool clang_CXXRecordDecl_has_non_trivial_destructor_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonTrivialDestructorForCall();
}

void clang_CXXRecordDecl_set_has_trivial_special_member_for_call(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setHasTrivialSpecialMemberForCall();
}

bool clang_CXXRecordDecl_allow_const_default_init(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->allowConstDefaultInit();
}

bool clang_CXXRecordDecl_has_irrelevant_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasIrrelevantDestructor();
}

bool clang_CXXRecordDecl_has_non_literal_type_fields_or_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasNonLiteralTypeFieldsOrBases();
}

bool clang_CXXRecordDecl_has_inherited_constructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasInheritedConstructor();
}

bool clang_CXXRecordDecl_has_inherited_assignment(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasInheritedAssignment();
}

bool clang_CXXRecordDecl_is_trivially_copyable(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isTriviallyCopyable();
}

bool clang_CXXRecordDecl_is_trivially_copy_constructible(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isTriviallyCopyConstructible();
}

bool clang_CXXRecordDecl_is_trivial(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isTrivial();
}

bool clang_CXXRecordDecl_is_literal(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isLiteral();
}

bool clang_CXXRecordDecl_is_structural(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isStructural();
}

void clang_CXXRecordDecl_added_eligible_special_member_function(void* thiz, void* MD, unsigned int SMKind) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::CXXMethodDecl* MD_cast = reinterpret_cast<const clang::CXXMethodDecl*>(MD);
    thiz_cast->addedEligibleSpecialMemberFunction(MD_cast, SMKind);
}

const void* clang_CXXRecordDecl_get_instantiated_from_member_class(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromMemberClass();
}

void clang_CXXRecordDecl_set_instantiation_of_member_class(void* thiz, void* RD, unsigned int TSK) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::CXXRecordDecl* RD_cast = reinterpret_cast<clang::CXXRecordDecl*>(RD);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setInstantiationOfMemberClass(RD_cast, TSK_cast);
}

unsigned int clang_CXXRecordDecl_get_template_specialization_kind(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKind();
}

void clang_CXXRecordDecl_set_template_specialization_kind(void* thiz, unsigned int TSK) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setTemplateSpecializationKind(TSK_cast);
}

void* clang_CXXRecordDecl_get_template_instantiation_pattern(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getTemplateInstantiationPattern();
}

bool clang_CXXRecordDecl_has_deleted_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasDeletedDestructor();
}

bool clang_CXXRecordDecl_is_any_destructor_no_return(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isAnyDestructorNoReturn();
}

bool clang_CXXRecordDecl_is_hlsl_intangible(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isHLSLIntangible();
}

void* clang_CXXRecordDecl_is_local_class(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->isLocalClass();
}

bool clang_CXXRecordDecl_is_current_instantiation(void* thiz, void* CurContext) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::DeclContext* CurContext_cast = reinterpret_cast<const clang::DeclContext*>(CurContext);
    return thiz_cast->isCurrentInstantiation(CurContext_cast);
}

bool clang_CXXRecordDecl_is_derived_from(void* thiz, void* Base) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::CXXRecordDecl* Base_cast = reinterpret_cast<const clang::CXXRecordDecl*>(Base);
    return thiz_cast->isDerivedFrom(Base_cast);
}

bool clang_CXXRecordDecl_is_virtually_derived_from(void* thiz, void* Base) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::CXXRecordDecl* Base_cast = reinterpret_cast<const clang::CXXRecordDecl*>(Base);
    return thiz_cast->isVirtuallyDerivedFrom(Base_cast);
}

bool clang_CXXRecordDecl_is_provably_not_derived_from(void* thiz, void* Base) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    const clang::CXXRecordDecl* Base_cast = reinterpret_cast<const clang::CXXRecordDecl*>(Base);
    return thiz_cast->isProvablyNotDerivedFrom(Base_cast);
}

void clang_CXXRecordDecl_view_inheritance(void* thiz, void* Context) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::ASTContext* Context_cast = reinterpret_cast<clang::ASTContext*>(Context);
    thiz_cast->viewInheritance(*Context_cast);
}

unsigned char clang_CXXRecordDecl_merge_access(unsigned char PathAccess, unsigned char DeclAccess) {
    clang::AccessSpecifier PathAccess_cast = (clang::AccessSpecifier)PathAccess;
    clang::AccessSpecifier DeclAccess_cast = (clang::AccessSpecifier)DeclAccess;
    return (unsigned char)clang::CXXRecordDecl::MergeAccess(PathAccess_cast, DeclAccess_cast);
}

void clang_CXXRecordDecl_finished_defaulted_or_deleted_member(void* thiz, void* MD) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::CXXMethodDecl* MD_cast = reinterpret_cast<clang::CXXMethodDecl*>(MD);
    thiz_cast->finishedDefaultedOrDeletedMember(MD_cast);
}

void clang_CXXRecordDecl_set_trivial_for_call_flags(void* thiz, void* MD) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::CXXMethodDecl* MD_cast = reinterpret_cast<clang::CXXMethodDecl*>(MD);
    thiz_cast->setTrivialForCallFlags(MD_cast);
}

void clang_CXXRecordDecl_complete_definition(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->completeDefinition();
}

bool clang_CXXRecordDecl_may_be_abstract(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->mayBeAbstract();
}

bool clang_CXXRecordDecl_is_effectively_final(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isEffectivelyFinal();
}

unsigned int clang_CXXRecordDecl_get_lambda_mangling_number(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getLambdaManglingNumber();
}

bool clang_CXXRecordDecl_has_known_lambda_internal_linkage(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasKnownLambdaInternalLinkage();
}

const void* clang_CXXRecordDecl_get_lambda_context_decl(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getLambdaContextDecl();
}

unsigned int clang_CXXRecordDecl_get_lambda_index_in_context(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getLambdaIndexInContext();
}

unsigned int clang_CXXRecordDecl_get_device_lambda_mangling_number(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getDeviceLambdaManglingNumber();
}

int clang_CXXRecordDecl_get_ms_inheritance_model(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (int)thiz_cast->getMSInheritanceModel();
}

int clang_CXXRecordDecl_calculate_inheritance_model(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (int)thiz_cast->calculateInheritanceModel();
}

bool clang_CXXRecordDecl_null_field_offset_is_zero(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->nullFieldOffsetIsZero();
}

bool clang_CXXRecordDecl_is_dependent_lambda(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isDependentLambda();
}

bool clang_CXXRecordDecl_is_never_dependent_lambda(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isNeverDependentLambda();
}

unsigned int clang_CXXRecordDecl_get_lambda_dependency_kind(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getLambdaDependencyKind();
}

void clang_CXXRecordDecl_set_lambda_dependency_kind(void* thiz, unsigned int Kind) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setLambdaDependencyKind(Kind);
}

void clang_CXXRecordDecl_set_lambda_is_generic(void* thiz, bool IsGeneric) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->setLambdaIsGeneric(IsGeneric);
}

bool clang_CXXRecordDecl_is_injected_class_name(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isInjectedClassName();
}

bool clang_CXXRecordDecl_has_injected_class_type(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasInjectedClassType();
}

bool clang_CXXRecordDecl_is_interface_like(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->isInterfaceLike();
}

bool clang_CXXRecordDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::CXXRecordDecl::classof(D_cast);
}

bool clang_CXXRecordDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::CXXRecordDecl::classofKind(K_cast);
}

void clang_CXXRecordDecl_mark_abstract(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    thiz_cast->markAbstract();
}

int clang_CXXRecordDecl_size_of() {
    return sizeof(clang::CXXRecordDecl);
}

int clang_CXXRecordDecl_align_of() {
    return alignof(clang::CXXRecordDecl);
}

void* clang_CXXRecordDecl_as_clang_RecordDecl(void* p) {
    return static_cast<clang::RecordDecl*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}

void* clang_CXXRecordDecl_as_clang_TagDecl(void* p) {
    return static_cast<clang::TagDecl*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}

void* clang_CXXRecordDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}

void* clang_CXXRecordDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}

void* clang_CXXRecordDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}

void* clang_CXXRecordDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}


// END KRAPPER GEN for clang::CXXRecordDecl


// BEGIN KRAPPER GEN for clang::TranslationUnitDecl

const void* clang_TranslationUnitDecl_get_ast_context(void* thiz) {
    clang::TranslationUnitDecl* thiz_cast = reinterpret_cast<clang::TranslationUnitDecl*>(thiz);
    return (void*)&(thiz_cast->getASTContext());
}

void* clang_TranslationUnitDecl_create(void* C) {
    clang::ASTContext* C_cast = reinterpret_cast<clang::ASTContext*>(C);
    return (void*)clang::TranslationUnitDecl::Create(*C_cast);
}

bool clang_TranslationUnitDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TranslationUnitDecl::classof(D_cast);
}

bool clang_TranslationUnitDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TranslationUnitDecl::classofKind(K_cast);
}

void* clang_TranslationUnitDecl_cast_to_decl_context(void* D) {
    const clang::TranslationUnitDecl* D_cast = reinterpret_cast<const clang::TranslationUnitDecl*>(D);
    return (void*)clang::TranslationUnitDecl::castToDeclContext(D_cast);
}

void* clang_TranslationUnitDecl_cast_from_decl_context(void* DC) {
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return (void*)clang::TranslationUnitDecl::castFromDeclContext(DC_cast);
}

void* clang_TranslationUnitDecl_get_canonical_decl(void* thiz) {
    clang::TranslationUnitDecl* thiz_cast = reinterpret_cast<clang::TranslationUnitDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

int clang_TranslationUnitDecl_size_of() {
    return sizeof(clang::TranslationUnitDecl);
}

int clang_TranslationUnitDecl_align_of() {
    return alignof(clang::TranslationUnitDecl);
}

void* clang_TranslationUnitDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TranslationUnitDecl*>(p));
}

void* clang_TranslationUnitDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::TranslationUnitDecl*>(p));
}


// END KRAPPER GEN for clang::TranslationUnitDecl


// BEGIN KRAPPER GEN for clang::ASTContext

bool clang_ASTContext_contains_address_discriminated_pointer_auth(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->containsAddressDiscriminatedPointerAuth(*T_cast);
}

bool clang_ASTContext_contains_non_relocatable_pointer_auth(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->containsNonRelocatablePointerAuth(*T_cast);
}

void clang_ASTContext_set_traversal_scope(void* thiz, void* _arg_0) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const std::vector<clang::Decl*>* _arg_0_cast = reinterpret_cast<const std::vector<clang::Decl*>*>(_arg_0);
    thiz_cast->setTraversalScope(*_arg_0_cast);
}

void clang_ASTContext_cleanup(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    thiz_cast->cleanup();
}

const void* clang_ASTContext_allocate(void* thiz, size_t Size, unsigned int Align) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->Allocate(Size, Align);
}

void clang_ASTContext_deallocate(void* thiz, void* Ptr) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    thiz_cast->Deallocate(Ptr);
}

size_t clang_ASTContext_get_ast_allocated_memory(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getASTAllocatedMemory();
}

size_t clang_ASTContext_get_side_table_allocated_memory(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getSideTableAllocatedMemory();
}

void clang_ASTContext_get_higher_precision_fp_type(void* thiz, void* ElementType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ElementType_cast = reinterpret_cast<clang::QualType*>(ElementType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->GetHigherPrecisionFPType(*ElementType_cast));
}

void clang_ASTContext_get_int_type_for_bitwidth(void* thiz, unsigned int DestWidth, unsigned int Signed, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getIntTypeForBitwidth(DestWidth, Signed));
}

void clang_ASTContext_get_real_type_for_bitwidth(void* thiz, unsigned int DestWidth, int ExplicitType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FloatModeKind ExplicitType_cast = (clang::FloatModeKind)ExplicitType;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getRealTypeForBitwidth(DestWidth, ExplicitType_cast));
}

bool clang_ASTContext_is_dependence_allowed(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->isDependenceAllowed();
}

unsigned int clang_ASTContext_get_cxxabi_kind(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (unsigned int)thiz_cast->getCXXABIKind();
}

void clang_ASTContext_erase_decl_attrs(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    thiz_cast->eraseDeclAttrs(D_cast);
}

void* clang_ASTContext_get_instantiated_from_using_decl(void* thiz, void* Inst) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::NamedDecl* Inst_cast = reinterpret_cast<clang::NamedDecl*>(Inst);
    return (void*)thiz_cast->getInstantiatedFromUsingDecl(Inst_cast);
}

void clang_ASTContext_set_instantiated_from_using_decl(void* thiz, void* Inst, void* Pattern) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::NamedDecl* Inst_cast = reinterpret_cast<clang::NamedDecl*>(Inst);
    clang::NamedDecl* Pattern_cast = reinterpret_cast<clang::NamedDecl*>(Pattern);
    thiz_cast->setInstantiatedFromUsingDecl(Inst_cast, Pattern_cast);
}

const void* clang_ASTContext_get_instantiated_from_unnamed_field_decl(void* thiz, void* Field) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FieldDecl* Field_cast = reinterpret_cast<clang::FieldDecl*>(Field);
    return (void*)thiz_cast->getInstantiatedFromUnnamedFieldDecl(Field_cast);
}

void clang_ASTContext_set_instantiated_from_unnamed_field_decl(void* thiz, void* Inst, void* Tmpl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FieldDecl* Inst_cast = reinterpret_cast<clang::FieldDecl*>(Inst);
    clang::FieldDecl* Tmpl_cast = reinterpret_cast<clang::FieldDecl*>(Tmpl);
    thiz_cast->setInstantiatedFromUnnamedFieldDecl(Inst_cast, Tmpl_cast);
}

unsigned int clang_ASTContext_overridden_methods_size(void* thiz, void* Method) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXMethodDecl* Method_cast = reinterpret_cast<const clang::CXXMethodDecl*>(Method);
    return thiz_cast->overridden_methods_size(Method_cast);
}

void clang_ASTContext_overridden_methods(void* thiz, void* Method, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXMethodDecl* Method_cast = reinterpret_cast<const clang::CXXMethodDecl*>(Method);
    std::vector<clang::CXXMethodDecl*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(ret_value);
    auto __range_4 = thiz_cast->overridden_methods(Method_cast);
    std::vector<clang::CXXMethodDecl*> __vec_4;
    for (auto __it_4 = __range_4.begin(); __it_4 != __range_4.end(); ++__it_4) __vec_4.push_back(kpp_to_elem_ptr<clang::CXXMethodDecl>(*__it_4));
    new (ret_value_cast) std::vector<clang::CXXMethodDecl*>(__vec_4);
}

void clang_ASTContext_add_overridden_method(void* thiz, void* Method, void* Overridden) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXMethodDecl* Method_cast = reinterpret_cast<const clang::CXXMethodDecl*>(Method);
    const clang::CXXMethodDecl* Overridden_cast = reinterpret_cast<const clang::CXXMethodDecl*>(Overridden);
    thiz_cast->addOverriddenMethod(Method_cast, Overridden_cast);
}

void* clang_ASTContext_get_primary_merged_decl(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    return (void*)thiz_cast->getPrimaryMergedDecl(D_cast);
}

void clang_ASTContext_set_primary_merged_decl(void* thiz, void* D, void* Primary) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    clang::Decl* Primary_cast = reinterpret_cast<clang::Decl*>(Primary);
    thiz_cast->setPrimaryMergedDecl(D_cast, Primary_cast);
}

void clang_ASTContext_deduplicate_merged_definitions_for(void* thiz, void* ND) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::NamedDecl* ND_cast = reinterpret_cast<clang::NamedDecl*>(ND);
    thiz_cast->deduplicateMergedDefinitionsFor(ND_cast);
}

const void* clang_ASTContext_get_translation_unit_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getTranslationUnitDecl();
}

void clang_ASTContext_add_translation_unit_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    thiz_cast->addTranslationUnitDecl();
}

void clang_ASTContext_dispose(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    thiz_cast->~ASTContext();
}

void clang_ASTContext_print_stats(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    thiz_cast->PrintStats();
}

const void* clang_ASTContext_build_implicit_record(void* thiz, const char* Name, int TK) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    llvm::StringRef Name_cast = llvm::StringRef(Name);
    clang::TagTypeKind TK_cast = (clang::TagTypeKind)TK;
    return (void*)thiz_cast->buildImplicitRecord(Name_cast, TK_cast);
}

void clang_ASTContext_get_addr_space_qual_type(void* thiz, void* T, unsigned int AddressSpace, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::LangAS AddressSpace_cast = (clang::LangAS)AddressSpace;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAddrSpaceQualType(*T_cast, AddressSpace_cast));
}

void clang_ASTContext_remove_addr_space_qual_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->removeAddrSpaceQualType(*T_cast));
}

unsigned short clang_ASTContext_get_pointer_auth_v_table_pointer_discriminator(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return thiz_cast->getPointerAuthVTablePointerDiscriminator(RD_cast);
}

unsigned short clang_ASTContext_get_pointer_auth_type_discriminator(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getPointerAuthTypeDiscriminator(*T_cast);
}

void clang_ASTContext_get_obj_cgc_qual_type(void* thiz, void* T, unsigned int gcAttr, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::Qualifiers::GC gcAttr_cast = (clang::Qualifiers::GC)gcAttr;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCGCQualType(*T_cast, gcAttr_cast));
}

void clang_ASTContext_remove_ptr_size_addr_space(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->removePtrSizeAddrSpace(*T_cast));
}

void clang_ASTContext_get_restrict_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getRestrictType(*T_cast));
}

void clang_ASTContext_get_volatile_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getVolatileType(*T_cast));
}

void clang_ASTContext_get_const_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getConstType(*T_cast));
}

void clang_ASTContext_adjust_function_result_type(void* thiz, void* FunctionType, void* NewResultType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* FunctionType_cast = reinterpret_cast<clang::QualType*>(FunctionType);
    clang::QualType* NewResultType_cast = reinterpret_cast<clang::QualType*>(NewResultType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->adjustFunctionResultType(*FunctionType_cast, *NewResultType_cast));
}

void clang_ASTContext_adjust_deduced_function_result_type(void* thiz, void* FD, void* ResultType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    clang::QualType* ResultType_cast = reinterpret_cast<clang::QualType*>(ResultType);
    thiz_cast->adjustDeducedFunctionResultType(FD_cast, *ResultType_cast);
}

bool clang_ASTContext_has_same_function_type_ignoring_exception_spec(void* thiz, void* T, void* U) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* U_cast = reinterpret_cast<clang::QualType*>(U);
    return thiz_cast->hasSameFunctionTypeIgnoringExceptionSpec(*T_cast, *U_cast);
}

void clang_ASTContext_get_function_type_without_ptr_sizes(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFunctionTypeWithoutPtrSizes(*T_cast));
}

bool clang_ASTContext_has_same_function_type_ignoring_ptr_sizes(void* thiz, void* T, void* U) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* U_cast = reinterpret_cast<clang::QualType*>(U);
    return thiz_cast->hasSameFunctionTypeIgnoringPtrSizes(*T_cast, *U_cast);
}

void clang_ASTContext_get_function_type_without_param_ab_is(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFunctionTypeWithoutParamABIs(*T_cast));
}

bool clang_ASTContext_has_same_function_type_ignoring_param_abi(void* thiz, void* T, void* U) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* U_cast = reinterpret_cast<clang::QualType*>(U);
    return thiz_cast->hasSameFunctionTypeIgnoringParamABI(*T_cast, *U_cast);
}

void clang_ASTContext_get_complex_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getComplexType(*T_cast));
}

void clang_ASTContext_get_pointer_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getPointerType(*T_cast));
}

void clang_ASTContext_get_adjusted_type(void* thiz, void* Orig, void* New, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Orig_cast = reinterpret_cast<clang::QualType*>(Orig);
    clang::QualType* New_cast = reinterpret_cast<clang::QualType*>(New);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAdjustedType(*Orig_cast, *New_cast));
}

void clang_ASTContext_get_decayed_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getDecayedType(*T_cast));
}

void clang_ASTContext_get_decayed_type__clang_QualType_clang_QualType(void* thiz, void* Orig, void* Decayed, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Orig_cast = reinterpret_cast<clang::QualType*>(Orig);
    clang::QualType* Decayed_cast = reinterpret_cast<clang::QualType*>(Decayed);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getDecayedType(*Orig_cast, *Decayed_cast));
}

void clang_ASTContext_get_array_parameter_type(void* thiz, void* Ty, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getArrayParameterType(*Ty_cast));
}

void clang_ASTContext_get_atomic_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAtomicType(*T_cast));
}

void clang_ASTContext_get_block_pointer_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBlockPointerType(*T_cast));
}

void clang_ASTContext_get_block_descriptor_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBlockDescriptorType());
}

void clang_ASTContext_get_read_pipe_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getReadPipeType(*T_cast));
}

void clang_ASTContext_get_write_pipe_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getWritePipeType(*T_cast));
}

void clang_ASTContext_get_bit_int_type(void* thiz, bool Unsigned, unsigned int NumBits, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBitIntType(Unsigned, NumBits));
}

void clang_ASTContext_get_block_descriptor_extended_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBlockDescriptorExtendedType());
}

unsigned int clang_ASTContext_get_default_open_cl_pointee_addr_space(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (unsigned int)thiz_cast->getDefaultOpenCLPointeeAddrSpace();
}

void clang_ASTContext_setcuda_configure_call_decl(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    thiz_cast->setcudaConfigureCallDecl(FD_cast);
}

void* clang_ASTContext_getcuda_configure_call_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getcudaConfigureCallDecl();
}

void clang_ASTContext_setcuda_get_parameter_buffer_decl(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    thiz_cast->setcudaGetParameterBufferDecl(FD_cast);
}

void* clang_ASTContext_getcuda_get_parameter_buffer_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getcudaGetParameterBufferDecl();
}

void clang_ASTContext_setcuda_launch_device_decl(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    thiz_cast->setcudaLaunchDeviceDecl(FD_cast);
}

void* clang_ASTContext_getcuda_launch_device_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getcudaLaunchDeviceDecl();
}

bool clang_ASTContext_get_byref_lifetime(void* thiz, void* Ty, unsigned int Lifetime, bool HasByrefExtendedLayout) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::Qualifiers::ObjCLifetime Lifetime_cast = (clang::Qualifiers::ObjCLifetime)Lifetime;
    return thiz_cast->getByrefLifetime(*Ty_cast, Lifetime_cast, HasByrefExtendedLayout);
}

void clang_ASTContext_get_l_value_reference_type(void* thiz, void* T, bool SpelledAsLValue, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getLValueReferenceType(*T_cast, SpelledAsLValue));
}

void clang_ASTContext_get_r_value_reference_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getRValueReferenceType(*T_cast));
}

void clang_ASTContext_get_incomplete_array_type(void* thiz, void* EltTy, int ASM, unsigned int IndexTypeQuals, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* EltTy_cast = reinterpret_cast<clang::QualType*>(EltTy);
    clang::ArraySizeModifier ASM_cast = (clang::ArraySizeModifier)ASM;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getIncompleteArrayType(*EltTy_cast, ASM_cast, IndexTypeQuals));
}

void clang_ASTContext_get_string_literal_array_type(void* thiz, void* EltTy, unsigned int Length, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* EltTy_cast = reinterpret_cast<clang::QualType*>(EltTy);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getStringLiteralArrayType(*EltTy_cast, Length));
}

void clang_ASTContext_get_variable_array_decayed_type(void* thiz, void* Ty, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getVariableArrayDecayedType(*Ty_cast));
}

void clang_ASTContext_get_scalable_vector_type(void* thiz, void* EltTy, unsigned int NumElts, unsigned int NumFields, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* EltTy_cast = reinterpret_cast<clang::QualType*>(EltTy);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getScalableVectorType(*EltTy_cast, NumElts, NumFields));
}

void clang_ASTContext_get_web_assembly_externref_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getWebAssemblyExternrefType());
}

void clang_ASTContext_get_vector_type(void* thiz, void* VectorType, unsigned int NumElts, int VecKind, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* VectorType_cast = reinterpret_cast<clang::QualType*>(VectorType);
    clang::VectorKind VecKind_cast = (clang::VectorKind)VecKind;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getVectorType(*VectorType_cast, NumElts, VecKind_cast));
}

void clang_ASTContext_get_ext_vector_type(void* thiz, void* VectorType, unsigned int NumElts, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* VectorType_cast = reinterpret_cast<clang::QualType*>(VectorType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getExtVectorType(*VectorType_cast, NumElts));
}

void clang_ASTContext_get_constant_matrix_type(void* thiz, void* ElementType, unsigned int NumRows, unsigned int NumColumns, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ElementType_cast = reinterpret_cast<clang::QualType*>(ElementType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getConstantMatrixType(*ElementType_cast, NumRows, NumColumns));
}

void clang_ASTContext_get_function_no_proto_type__clang_QualType(void* thiz, void* ResultTy, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ResultTy_cast = reinterpret_cast<clang::QualType*>(ResultTy);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFunctionNoProtoType(*ResultTy_cast));
}

void clang_ASTContext_adjust_string_literal_base_type(void* thiz, void* StrLTy, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* StrLTy_cast = reinterpret_cast<clang::QualType*>(StrLTy);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->adjustStringLiteralBaseType(*StrLTy_cast));
}

void clang_ASTContext_get_type_decl_type__const_clang_TypeDecl_P(void* thiz, void* Decl, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TypeDecl* Decl_cast = reinterpret_cast<const clang::TypeDecl*>(Decl);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getTypeDeclType(Decl_cast));
}

bool clang_ASTContext_compute_best_enum_types(void* thiz, bool IsPacked, unsigned int NumNegativeBits, unsigned int NumPositiveBits, void* BestType, void* BestPromotionType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* BestType_cast = reinterpret_cast<clang::QualType*>(BestType);
    clang::QualType* BestPromotionType_cast = reinterpret_cast<clang::QualType*>(BestPromotionType);
    return thiz_cast->computeBestEnumTypes(IsPacked, NumNegativeBits, NumPositiveBits, *BestType_cast, *BestPromotionType_cast);
}

void clang_ASTContext_get_attributed_type(void* thiz, unsigned int attrKind, void* modifiedType, void* equivalentType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::attr::Kind attrKind_cast = (clang::attr::Kind)attrKind;
    clang::QualType* modifiedType_cast = reinterpret_cast<clang::QualType*>(modifiedType);
    clang::QualType* equivalentType_cast = reinterpret_cast<clang::QualType*>(equivalentType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAttributedType(attrKind_cast, *modifiedType_cast, *equivalentType_cast));
}

void clang_ASTContext_get_attributed_type__clang_NullabilityKind_clang_QualType_clang_QualType(void* thiz, unsigned char nullability, void* modifiedType, void* equivalentType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::NullabilityKind nullability_cast = (clang::NullabilityKind)nullability;
    clang::QualType* modifiedType_cast = reinterpret_cast<clang::QualType*>(modifiedType);
    clang::QualType* equivalentType_cast = reinterpret_cast<clang::QualType*>(equivalentType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAttributedType(nullability_cast, *modifiedType_cast, *equivalentType_cast));
}

void clang_ASTContext_get_template_type_parm_type(void* thiz, unsigned int Depth, unsigned int Index, bool ParameterPack, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getTemplateTypeParmType(Depth, Index, ParameterPack));
}

void clang_ASTContext_get_paren_type(void* thiz, void* NamedType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* NamedType_cast = reinterpret_cast<clang::QualType*>(NamedType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getParenType(*NamedType_cast));
}

void clang_ASTContext_get_obj_c_object_pointer_type(void* thiz, void* OIT, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* OIT_cast = reinterpret_cast<clang::QualType*>(OIT);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCObjectPointerType(*OIT_cast));
}

void clang_ASTContext_get_type_of_type(void* thiz, void* QT, unsigned char Kind, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* QT_cast = reinterpret_cast<clang::QualType*>(QT);
    clang::TypeOfKind Kind_cast = (clang::TypeOfKind)Kind;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getTypeOfType(*QT_cast, Kind_cast));
}

void clang_ASTContext_get_unary_transform_type(void* thiz, void* BaseType, void* UnderlyingType, unsigned int UKind, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* BaseType_cast = reinterpret_cast<clang::QualType*>(BaseType);
    clang::QualType* UnderlyingType_cast = reinterpret_cast<clang::QualType*>(UnderlyingType);
    clang::UnaryTransformType::UTTKind UKind_cast = (clang::UnaryTransformType::UTTKind)UKind;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnaryTransformType(*BaseType_cast, *UnderlyingType_cast, UKind_cast));
}

void clang_ASTContext_get_auto_type(void* thiz, void* DeducedType, int Keyword, bool IsDependent, bool IsPack, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* DeducedType_cast = reinterpret_cast<clang::QualType*>(DeducedType);
    clang::AutoTypeKeyword Keyword_cast = (clang::AutoTypeKeyword)Keyword;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAutoType(*DeducedType_cast, Keyword_cast, IsDependent, IsPack));
}

void clang_ASTContext_get_auto_deduct_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAutoDeductType());
}

void clang_ASTContext_get_auto_r_ref_deduct_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAutoRRefDeductType());
}

void clang_ASTContext_get_unconstrained_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnconstrainedType(*T_cast));
}

void clang_ASTContext_get_size_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSizeType());
}

void clang_ASTContext_get_signed_size_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSignedSizeType());
}

void clang_ASTContext_get_w_char_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getWCharType());
}

void clang_ASTContext_get_wide_char_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getWideCharType());
}

void clang_ASTContext_get_signed_w_char_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSignedWCharType());
}

void clang_ASTContext_get_unsigned_w_char_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnsignedWCharType());
}

void clang_ASTContext_get_w_int_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getWIntType());
}

void clang_ASTContext_get_int_ptr_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getIntPtrType());
}

void clang_ASTContext_get_u_int_ptr_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUIntPtrType());
}

void clang_ASTContext_get_pointer_diff_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getPointerDiffType());
}

void clang_ASTContext_get_unsigned_pointer_diff_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnsignedPointerDiffType());
}

void clang_ASTContext_get_process_id_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getProcessIDType());
}

void clang_ASTContext_get_cf_constant_string_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCFConstantStringType());
}

void clang_ASTContext_get_obj_c_super_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCSuperType());
}

void clang_ASTContext_set_obj_c_super_type(void* thiz, void* ST) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ST_cast = reinterpret_cast<clang::QualType*>(ST);
    thiz_cast->setObjCSuperType(*ST_cast);
}

void clang_ASTContext_get_raw_cf_constant_string_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getRawCFConstantStringType());
}

void clang_ASTContext_set_cf_constant_string_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    thiz_cast->setCFConstantStringType(*T_cast);
}

const void* clang_ASTContext_get_cf_constant_string_tag_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getCFConstantStringTagDecl();
}

void clang_ASTContext_get_obj_c_constant_string_interface(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCConstantStringInterface());
}

void clang_ASTContext_get_obj_cns_string_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCNSStringType());
}

void clang_ASTContext_set_obj_cns_string_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    thiz_cast->setObjCNSStringType(*T_cast);
}

void clang_ASTContext_get_obj_c_id_redefinition_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCIdRedefinitionType());
}

void clang_ASTContext_set_obj_c_id_redefinition_type(void* thiz, void* RedefType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* RedefType_cast = reinterpret_cast<clang::QualType*>(RedefType);
    thiz_cast->setObjCIdRedefinitionType(*RedefType_cast);
}

void clang_ASTContext_get_obj_c_class_redefinition_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCClassRedefinitionType());
}

void clang_ASTContext_set_obj_c_class_redefinition_type(void* thiz, void* RedefType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* RedefType_cast = reinterpret_cast<clang::QualType*>(RedefType);
    thiz_cast->setObjCClassRedefinitionType(*RedefType_cast);
}

void clang_ASTContext_get_obj_c_sel_redefinition_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCSelRedefinitionType());
}

void clang_ASTContext_set_obj_c_sel_redefinition_type(void* thiz, void* RedefType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* RedefType_cast = reinterpret_cast<clang::QualType*>(RedefType);
    thiz_cast->setObjCSelRedefinitionType(*RedefType_cast);
}

void clang_ASTContext_get_obj_c_instance_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCInstanceType());
}

void clang_ASTContext_set_file_decl(void* thiz, void* FILEDecl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TypeDecl* FILEDecl_cast = reinterpret_cast<clang::TypeDecl*>(FILEDecl);
    thiz_cast->setFILEDecl(FILEDecl_cast);
}

void clang_ASTContext_get_file_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getFILEType());
}

void clang_ASTContext_setjmp_buf_decl(void* thiz, void* jmp_bufDecl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TypeDecl* jmp_bufDecl_cast = reinterpret_cast<clang::TypeDecl*>(jmp_bufDecl);
    thiz_cast->setjmp_bufDecl(jmp_bufDecl_cast);
}

void clang_ASTContext_getjmp_buf_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getjmp_bufType());
}

void clang_ASTContext_setsigjmp_buf_decl(void* thiz, void* sigjmp_bufDecl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TypeDecl* sigjmp_bufDecl_cast = reinterpret_cast<clang::TypeDecl*>(sigjmp_bufDecl);
    thiz_cast->setsigjmp_bufDecl(sigjmp_bufDecl_cast);
}

void clang_ASTContext_getsigjmp_buf_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getsigjmp_bufType());
}

void clang_ASTContext_setucontext_t_decl(void* thiz, void* ucontext_tDecl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TypeDecl* ucontext_tDecl_cast = reinterpret_cast<clang::TypeDecl*>(ucontext_tDecl);
    thiz_cast->setucontext_tDecl(ucontext_tDecl_cast);
}

void clang_ASTContext_getucontext_t_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getucontext_tType());
}

void clang_ASTContext_get_obj_c_encoding_for_type(void* thiz, void* T, const char* S, void* Field, void* NotEncodedT) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    std::string S_cast = std::string(S);
    const clang::FieldDecl* Field_cast = reinterpret_cast<const clang::FieldDecl*>(Field);
    clang::QualType* NotEncodedT_cast = reinterpret_cast<clang::QualType*>(NotEncodedT);
    thiz_cast->getObjCEncodingForType(*T_cast, S_cast, Field_cast, NotEncodedT_cast);
}

void clang_ASTContext_get_obj_c_encoding_for_property_type(void* thiz, void* T, const char* S) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    std::string S_cast = std::string(S);
    thiz_cast->getObjCEncodingForPropertyType(*T_cast, S_cast);
}

void clang_ASTContext_get_legacy_integral_type_encoding(void* thiz, void* t) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* t_cast = reinterpret_cast<clang::QualType*>(t);
    thiz_cast->getLegacyIntegralTypeEncoding(*t_cast);
}

void clang_ASTContext_get_obj_c_encoding_for_type_qualifier(void* thiz, unsigned int QT, const char* S) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl::ObjCDeclQualifier QT_cast = (clang::Decl::ObjCDeclQualifier)QT;
    std::string S_cast = std::string(S);
    thiz_cast->getObjCEncodingForTypeQualifier(QT_cast, S_cast);
}

const char* clang_ASTContext_get_obj_c_encoding_for_function_decl(void* thiz, void* Decl) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* Decl_cast = reinterpret_cast<const clang::FunctionDecl*>(Decl);
    std::string ret_value = thiz_cast->getObjCEncodingForFunctionDecl(Decl_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

void clang_ASTContext_get_obj_c_id_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCIdType());
}

void clang_ASTContext_get_obj_c_sel_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCSelType());
}

void clang_ASTContext_get_obj_c_class_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCClassType());
}

void clang_ASTContext_get_bool_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBOOLType());
}

void clang_ASTContext_get_obj_c_proto_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getObjCProtoType());
}

void clang_ASTContext_get_builtin_va_list_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBuiltinVaListType());
}

const void* clang_ASTContext_get_va_list_tag_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getVaListTagDecl();
}

void clang_ASTContext_get_builtin_ms_va_list_type(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBuiltinMSVaListType());
}

const void* clang_ASTContext_get_ms_guid_tag_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getMSGuidTagDecl();
}

const void* clang_ASTContext_get_ms_type_info_tag_decl(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->getMSTypeInfoTagDecl();
}

bool clang_ASTContext_can_builtin_be_redeclared(void* thiz, void* _arg_0) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* _arg_0_cast = reinterpret_cast<const clang::FunctionDecl*>(_arg_0);
    return thiz_cast->canBuiltinBeRedeclared(_arg_0_cast);
}

void clang_ASTContext_get_cvr_qualified_type(void* thiz, void* T, unsigned int CVR, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCVRQualifiedType(*T_cast, CVR));
}

void clang_ASTContext_get_lifetime_qualified_type(void* thiz, void* type, unsigned int lifetime, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* type_cast = reinterpret_cast<clang::QualType*>(type);
    clang::Qualifiers::ObjCLifetime lifetime_cast = (clang::Qualifiers::ObjCLifetime)lifetime;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getLifetimeQualifiedType(*type_cast, lifetime_cast));
}

void clang_ASTContext_get_unqualified_obj_c_pointer_type(void* thiz, void* type, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* type_cast = reinterpret_cast<clang::QualType*>(type);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnqualifiedObjCPointerType(*type_cast));
}

unsigned char clang_ASTContext_get_fixed_point_scale(void* thiz, void* Ty) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return thiz_cast->getFixedPointScale(*Ty_cast);
}

unsigned char clang_ASTContext_get_fixed_point_i_bits(void* thiz, void* Ty) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return thiz_cast->getFixedPointIBits(*Ty_cast);
}

void clang_ASTContext_decode_type_str(void* thiz, const char* Str, void* Context, unsigned int Error, bool RequireICE, bool AllowTypeModifiers, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    clang::ASTContext::GetBuiltinTypeError Error_cast = (clang::ASTContext::GetBuiltinTypeError)Error;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->DecodeTypeStr(Str, *Context_cast, Error_cast, RequireICE, AllowTypeModifiers));
}

void clang_ASTContext_get_builtin_type(void* thiz, unsigned int ID, unsigned int Error, unsigned int* IntegerConstantArgs, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::ASTContext::GetBuiltinTypeError Error_cast = (clang::ASTContext::GetBuiltinTypeError)Error;
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->GetBuiltinType(ID, Error_cast, IntegerConstantArgs));
}

unsigned int clang_ASTContext_get_obj_cgc_attr_kind(void* thiz, void* Ty) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return (unsigned int)thiz_cast->getObjCGCAttrKind(*Ty_cast);
}

bool clang_ASTContext_are_compatible_vector_types(void* thiz, void* FirstVec, void* SecondVec) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* FirstVec_cast = reinterpret_cast<clang::QualType*>(FirstVec);
    clang::QualType* SecondVec_cast = reinterpret_cast<clang::QualType*>(SecondVec);
    return thiz_cast->areCompatibleVectorTypes(*FirstVec_cast, *SecondVec_cast);
}

bool clang_ASTContext_are_compatible_rvv_types(void* thiz, void* FirstType, void* SecondType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* FirstType_cast = reinterpret_cast<clang::QualType*>(FirstType);
    clang::QualType* SecondType_cast = reinterpret_cast<clang::QualType*>(SecondType);
    return thiz_cast->areCompatibleRVVTypes(*FirstType_cast, *SecondType_cast);
}

bool clang_ASTContext_are_lax_compatible_rvv_types(void* thiz, void* FirstType, void* SecondType) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* FirstType_cast = reinterpret_cast<clang::QualType*>(FirstType);
    clang::QualType* SecondType_cast = reinterpret_cast<clang::QualType*>(SecondType);
    return thiz_cast->areLaxCompatibleRVVTypes(*FirstType_cast, *SecondType_cast);
}

bool clang_ASTContext_has_direct_ownership_qualifier(void* thiz, void* Ty) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return thiz_cast->hasDirectOwnershipQualifier(*Ty_cast);
}

bool clang_ASTContext_is_obj_cns_object_type(void* Ty) {
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return clang::ASTContext::isObjCNSObjectType(*Ty_cast);
}

unsigned int clang_ASTContext_get_open_mp_default_simd_align(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getOpenMPDefaultSimdAlign(*T_cast);
}

unsigned long clang_ASTContext_get_type_size(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeSize(*T_cast);
}

unsigned long clang_ASTContext_get_char_width(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getCharWidth();
}

unsigned int clang_ASTContext_get_type_align(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeAlign(*T_cast);
}

unsigned int clang_ASTContext_get_type_unadjusted_align(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeUnadjustedAlign(*T_cast);
}

unsigned int clang_ASTContext_get_type_align_if_known(void* thiz, void* T, bool NeedsPreferredAlignment) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeAlignIfKnown(*T_cast, NeedsPreferredAlignment);
}

bool clang_ASTContext_is_alignment_required__clang_QualType(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isAlignmentRequired(*T_cast);
}

bool clang_ASTContext_is_promotable_integer_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isPromotableIntegerType(*T_cast);
}

unsigned int clang_ASTContext_get_preferred_type_align(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getPreferredTypeAlign(*T_cast);
}

unsigned int clang_ASTContext_get_target_default_align_for_attribute_aligned(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getTargetDefaultAlignForAttributeAligned();
}

bool clang_ASTContext_defaults_to_ms_struct(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->defaultsToMsStruct();
}

void* clang_ASTContext_get_current_key_function(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return (void*)thiz_cast->getCurrentKeyFunction(RD_cast);
}

void clang_ASTContext_set_non_key_function(void* thiz, void* method) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXMethodDecl* method_cast = reinterpret_cast<const clang::CXXMethodDecl*>(method);
    thiz_cast->setNonKeyFunction(method_cast);
}

unsigned long clang_ASTContext_get_field_offset(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ValueDecl* FD_cast = reinterpret_cast<const clang::ValueDecl*>(FD);
    return thiz_cast->getFieldOffset(FD_cast);
}

bool clang_ASTContext_is_nearly_empty(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return thiz_cast->isNearlyEmpty(RD_cast);
}

bool clang_ASTContext_has_unique_object_representations(void* thiz, void* Ty, bool CheckIfTriviallyCopyable) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    return thiz_cast->hasUniqueObjectRepresentations(*Ty_cast, CheckIfTriviallyCopyable);
}

bool clang_ASTContext_has_same_type(void* T1, void* T2) {
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return clang::ASTContext::hasSameType(*T1_cast, *T2_cast);
}

void clang_ASTContext_get_unqualified_array_type__clang_QualType(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnqualifiedArrayType(*T_cast));
}

bool clang_ASTContext_has_same_unqualified_type(void* T1, void* T2) {
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return clang::ASTContext::hasSameUnqualifiedType(*T1_cast, *T2_cast);
}

bool clang_ASTContext_has_same_nullability_type_qualifier(void* thiz, void* SubT, void* SuperT, bool IsParam) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* SubT_cast = reinterpret_cast<clang::QualType*>(SubT);
    clang::QualType* SuperT_cast = reinterpret_cast<clang::QualType*>(SuperT);
    return thiz_cast->hasSameNullabilityTypeQualifier(*SubT_cast, *SuperT_cast, IsParam);
}

bool clang_ASTContext_unwrap_similar_types(void* thiz, void* T1, void* T2, bool AllowPiMismatch) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return thiz_cast->UnwrapSimilarTypes(*T1_cast, *T2_cast, AllowPiMismatch);
}

void clang_ASTContext_unwrap_similar_array_types(void* thiz, void* T1, void* T2, bool AllowPiMismatch) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    thiz_cast->UnwrapSimilarArrayTypes(*T1_cast, *T2_cast, AllowPiMismatch);
}

bool clang_ASTContext_has_similar_type(void* thiz, void* T1, void* T2) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return thiz_cast->hasSimilarType(*T1_cast, *T2_cast);
}

bool clang_ASTContext_has_cvr_similar_type(void* thiz, void* T1, void* T2) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return thiz_cast->hasCvrSimilarType(*T1_cast, *T2_cast);
}

unsigned int clang_ASTContext_get_default_calling_convention(void* thiz, bool IsVariadic, bool IsCXXMethod) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (unsigned int)thiz_cast->getDefaultCallingConvention(IsVariadic, IsCXXMethod);
}

bool clang_ASTContext_is_same_entity(void* thiz, void* X, void* Y) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::NamedDecl* X_cast = reinterpret_cast<const clang::NamedDecl*>(X);
    const clang::NamedDecl* Y_cast = reinterpret_cast<const clang::NamedDecl*>(Y);
    return thiz_cast->isSameEntity(X_cast, Y_cast);
}

bool clang_ASTContext_is_same_template_parameter(void* thiz, void* X, void* Y) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::NamedDecl* X_cast = reinterpret_cast<const clang::NamedDecl*>(X);
    const clang::NamedDecl* Y_cast = reinterpret_cast<const clang::NamedDecl*>(Y);
    return thiz_cast->isSameTemplateParameter(X_cast, Y_cast);
}

bool clang_ASTContext_is_same_default_template_argument(void* thiz, void* X, void* Y) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::NamedDecl* X_cast = reinterpret_cast<const clang::NamedDecl*>(X);
    const clang::NamedDecl* Y_cast = reinterpret_cast<const clang::NamedDecl*>(Y);
    return thiz_cast->isSameDefaultTemplateArgument(X_cast, Y_cast);
}

void clang_ASTContext_get_base_element_type__clang_QualType(void* thiz, void* QT, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* QT_cast = reinterpret_cast<clang::QualType*>(QT);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBaseElementType(*QT_cast));
}

void clang_ASTContext_get_adjusted_parameter_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAdjustedParameterType(*T_cast));
}

void clang_ASTContext_get_signature_parameter_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSignatureParameterType(*T_cast));
}

void clang_ASTContext_get_exception_object_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getExceptionObjectType(*T_cast));
}

void clang_ASTContext_get_array_decayed_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getArrayDecayedType(*T_cast));
}

void clang_ASTContext_get_promoted_integer_type(void* thiz, void* PromotableType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* PromotableType_cast = reinterpret_cast<clang::QualType*>(PromotableType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getPromotedIntegerType(*PromotableType_cast));
}

unsigned int clang_ASTContext_get_inner_obj_c_ownership(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return (unsigned int)thiz_cast->getInnerObjCOwnership(*T_cast);
}

int clang_ASTContext_get_integer_type_order(void* thiz, void* LHS, void* RHS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* LHS_cast = reinterpret_cast<clang::QualType*>(LHS);
    clang::QualType* RHS_cast = reinterpret_cast<clang::QualType*>(RHS);
    return thiz_cast->getIntegerTypeOrder(*LHS_cast, *RHS_cast);
}

int clang_ASTContext_get_floating_type_order(void* thiz, void* LHS, void* RHS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* LHS_cast = reinterpret_cast<clang::QualType*>(LHS);
    clang::QualType* RHS_cast = reinterpret_cast<clang::QualType*>(RHS);
    return thiz_cast->getFloatingTypeOrder(*LHS_cast, *RHS_cast);
}

int clang_ASTContext_get_floating_type_semantic_order(void* thiz, void* LHS, void* RHS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* LHS_cast = reinterpret_cast<clang::QualType*>(LHS);
    clang::QualType* RHS_cast = reinterpret_cast<clang::QualType*>(RHS);
    return thiz_cast->getFloatingTypeSemanticOrder(*LHS_cast, *RHS_cast);
}

unsigned int clang_ASTContext_get_target_address_space(void* thiz, unsigned int AS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::LangAS AS_cast = (clang::LangAS)AS;
    return thiz_cast->getTargetAddressSpace(AS_cast);
}

unsigned int clang_ASTContext_get_lang_as_for_builtin_address_space(void* thiz, unsigned int AS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (unsigned int)thiz_cast->getLangASForBuiltinAddressSpace(AS);
}

unsigned long clang_ASTContext_get_target_null_pointer_value(void* thiz, void* QT) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* QT_cast = reinterpret_cast<clang::QualType*>(QT);
    return thiz_cast->getTargetNullPointerValue(*QT_cast);
}

bool clang_ASTContext_address_space_map_mangling_for(void* thiz, unsigned int AS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::LangAS AS_cast = (clang::LangAS)AS;
    return thiz_cast->addressSpaceMapManglingFor(AS_cast);
}

bool clang_ASTContext_has_any_function_effects(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->hasAnyFunctionEffects();
}

void clang_ASTContext_get_common_sugared_type(void* thiz, void* X, void* Y, bool Unqualified, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* X_cast = reinterpret_cast<clang::QualType*>(X);
    clang::QualType* Y_cast = reinterpret_cast<clang::QualType*>(Y);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCommonSugaredType(*X_cast, *Y_cast, Unqualified));
}

bool clang_ASTContext_types_are_compatible(void* thiz, void* T1, void* T2, bool CompareUnqualified) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return thiz_cast->typesAreCompatible(*T1_cast, *T2_cast, CompareUnqualified);
}

bool clang_ASTContext_property_types_are_compatible(void* thiz, void* _arg_0, void* _arg_1) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    return thiz_cast->propertyTypesAreCompatible(*_arg_0_cast, *_arg_1_cast);
}

bool clang_ASTContext_types_are_block_pointer_compatible(void* thiz, void* _arg_0, void* _arg_1) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    return thiz_cast->typesAreBlockPointerCompatible(*_arg_0_cast, *_arg_1_cast);
}

bool clang_ASTContext_is_obj_c_id_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isObjCIdType(*T_cast);
}

bool clang_ASTContext_is_obj_c_class_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isObjCClassType(*T_cast);
}

bool clang_ASTContext_is_obj_c_sel_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isObjCSelType(*T_cast);
}

bool clang_ASTContext_are_comparable_obj_c_pointer_types(void* thiz, void* LHS, void* RHS) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* LHS_cast = reinterpret_cast<clang::QualType*>(LHS);
    clang::QualType* RHS_cast = reinterpret_cast<clang::QualType*>(RHS);
    return thiz_cast->areComparableObjCPointerTypes(*LHS_cast, *RHS_cast);
}

bool clang_ASTContext_can_bind_obj_c_object_type(void* thiz, void* To, void* From) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* To_cast = reinterpret_cast<clang::QualType*>(To);
    clang::QualType* From_cast = reinterpret_cast<clang::QualType*>(From);
    return thiz_cast->canBindObjCObjectType(*To_cast, *From_cast);
}

void clang_ASTContext_merge_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, bool BlockReturnType, bool IsConditionalOperator, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeTypes(*_arg_0_cast, *_arg_1_cast, OfBlockPointer, Unqualified, BlockReturnType, IsConditionalOperator));
}

void clang_ASTContext_merge_function_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, bool AllowCXX, bool IsConditionalOperator, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeFunctionTypes(*_arg_0_cast, *_arg_1_cast, OfBlockPointer, Unqualified, AllowCXX, IsConditionalOperator));
}

void clang_ASTContext_merge_function_parameter_types(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeFunctionParameterTypes(*_arg_0_cast, *_arg_1_cast, OfBlockPointer, Unqualified));
}

void clang_ASTContext_merge_transparent_union_type(void* thiz, void* _arg_0, void* _arg_1, bool OfBlockPointer, bool Unqualified, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeTransparentUnionType(*_arg_0_cast, *_arg_1_cast, OfBlockPointer, Unqualified));
}

void clang_ASTContext_merge_tag_definitions(void* thiz, void* _arg_0, void* _arg_1, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeTagDefinitions(*_arg_0_cast, *_arg_1_cast));
}

void clang_ASTContext_merge_obj_cgc_qualifiers(void* thiz, void* _arg_0, void* _arg_1, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* _arg_0_cast = reinterpret_cast<clang::QualType*>(_arg_0);
    clang::QualType* _arg_1_cast = reinterpret_cast<clang::QualType*>(_arg_1);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->mergeObjCGCQualifiers(*_arg_0_cast, *_arg_1_cast));
}

unsigned int clang_ASTContext_get_int_width(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getIntWidth(*T_cast);
}

void clang_ASTContext_get_corresponding_unsigned_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCorrespondingUnsignedType(*T_cast));
}

void clang_ASTContext_get_corresponding_signed_type(void* thiz, void* T, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCorrespondingSignedType(*T_cast));
}

void clang_ASTContext_get_corresponding_saturated_type(void* thiz, void* Ty, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCorrespondingSaturatedType(*Ty_cast));
}

void clang_ASTContext_get_corresponding_unsaturated_type(void* thiz, void* Ty, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCorrespondingUnsaturatedType(*Ty_cast));
}

void clang_ASTContext_get_corresponding_signed_fixed_point_type(void* thiz, void* Ty, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCorrespondingSignedFixedPointType(*Ty_cast));
}

bool clang_ASTContext_any_obj_c_implementation(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->AnyObjCImplementation();
}

unsigned int clang_ASTContext_get_gva_linkage_for_function(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* FD_cast = reinterpret_cast<const clang::FunctionDecl*>(FD);
    return (unsigned int)thiz_cast->GetGVALinkageForFunction(FD_cast);
}

bool clang_ASTContext_decl_must_be_emitted(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return thiz_cast->DeclMustBeEmitted(D_cast);
}

void clang_ASTContext_add_declarator_for_unnamed_tag_decl(void* thiz, void* TD, void* DD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TagDecl* TD_cast = reinterpret_cast<clang::TagDecl*>(TD);
    clang::DeclaratorDecl* DD_cast = reinterpret_cast<clang::DeclaratorDecl*>(DD);
    thiz_cast->addDeclaratorForUnnamedTagDecl(TD_cast, DD_cast);
}

void* clang_ASTContext_get_declarator_for_unnamed_tag_decl(void* thiz, void* TD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TagDecl* TD_cast = reinterpret_cast<const clang::TagDecl*>(TD);
    return (void*)thiz_cast->getDeclaratorForUnnamedTagDecl(TD_cast);
}

void clang_ASTContext_set_mangling_number(void* thiz, void* ND, unsigned int Number) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::NamedDecl* ND_cast = reinterpret_cast<const clang::NamedDecl*>(ND);
    thiz_cast->setManglingNumber(ND_cast, Number);
}

unsigned int clang_ASTContext_get_mangling_number(void* thiz, void* ND, bool ForAuxTarget) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::NamedDecl* ND_cast = reinterpret_cast<const clang::NamedDecl*>(ND);
    return thiz_cast->getManglingNumber(ND_cast, ForAuxTarget);
}

bool clang_ASTContext_has_seen_type_aware_operator_new_or_delete(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->hasSeenTypeAwareOperatorNewOrDelete();
}

void clang_ASTContext_set_is_destroying_operator_delete(void* thiz, void* FD, bool IsDestroying) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* FD_cast = reinterpret_cast<const clang::FunctionDecl*>(FD);
    thiz_cast->setIsDestroyingOperatorDelete(FD_cast, IsDestroying);
}

bool clang_ASTContext_is_destroying_operator_delete(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* FD_cast = reinterpret_cast<const clang::FunctionDecl*>(FD);
    return thiz_cast->isDestroyingOperatorDelete(FD_cast);
}

void clang_ASTContext_set_is_type_aware_operator_new_or_delete(void* thiz, void* FD, bool IsTypeAware) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* FD_cast = reinterpret_cast<const clang::FunctionDecl*>(FD);
    thiz_cast->setIsTypeAwareOperatorNewOrDelete(FD_cast, IsTypeAware);
}

bool clang_ASTContext_is_type_aware_operator_new_or_delete(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::FunctionDecl* FD_cast = reinterpret_cast<const clang::FunctionDecl*>(FD);
    return thiz_cast->isTypeAwareOperatorNewOrDelete(FD_cast);
}

void clang_ASTContext_set_class_needs_vector_deleting_destructor(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    thiz_cast->setClassNeedsVectorDeletingDestructor(RD_cast);
}

bool clang_ASTContext_class_needs_vector_deleting_destructor(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* RD_cast = reinterpret_cast<const clang::CXXRecordDecl*>(RD);
    return thiz_cast->classNeedsVectorDeletingDestructor(RD_cast);
}

unsigned int clang_ASTContext_get_next_string_literal_version(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getNextStringLiteralVersion();
}

void clang_ASTContext_register_sycl_entry_point_function(void* thiz, void* FD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::FunctionDecl* FD_cast = reinterpret_cast<clang::FunctionDecl*>(FD);
    thiz_cast->registerSYCLEntryPointFunction(FD_cast);
}

void clang_ASTContext_get_obj_c_encoding_for_method_parameter(void* thiz, unsigned int QT, void* T, const char* S, bool Extended) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl::ObjCDeclQualifier QT_cast = (clang::Decl::ObjCDeclQualifier)QT;
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    std::string S_cast = std::string(S);
    thiz_cast->getObjCEncodingForMethodParameter(QT_cast, *T_cast, S_cast, Extended);
}

bool clang_ASTContext_may_externalize(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return thiz_cast->mayExternalize(D_cast);
}

bool clang_ASTContext_should_externalize(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return thiz_cast->shouldExternalize(D_cast);
}

void* clang_ASTContext_base_for_v_table_authentication(void* thiz, void* ThisClass) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXRecordDecl* ThisClass_cast = reinterpret_cast<const clang::CXXRecordDecl*>(ThisClass);
    return (void*)thiz_cast->baseForVTableAuthentication(ThisClass_cast);
}

const char* clang_ASTContext_backup_str(void* thiz, const char* S) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    llvm::StringRef S_cast = llvm::StringRef(S);
    std::string ret_value = thiz_cast->backupStr(S_cast).str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

const char* clang_ASTContext_get_cuid_hash(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    std::string ret_value = thiz_cast->getCUIDHash().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

int clang_ASTContext_size_of() {
    return sizeof(clang::ASTContext);
}

int clang_ASTContext_align_of() {
    return alignof(clang::ASTContext);
}

const unsigned int clang_ASTContext_TUKind_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (const unsigned int)thiz_cast->TUKind;
}

bool clang_ASTContext_CommentsLoaded_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->CommentsLoaded;
}

void clang_ASTContext_CommentsLoaded_set(void* thiz, bool value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->CommentsLoaded = value);
}

void clang_ASTContext_AutoDeductTy_get(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->AutoDeductTy);
}

void clang_ASTContext_AutoDeductTy_set(void* thiz, void* value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* value_cast = reinterpret_cast<clang::QualType*>(value);
    (thiz_cast->AutoDeductTy = *value_cast);
}

void clang_ASTContext_AutoRRefDeductTy_get(void* thiz, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->AutoRRefDeductTy);
}

void clang_ASTContext_AutoRRefDeductTy_set(void* thiz, void* value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* value_cast = reinterpret_cast<clang::QualType*>(value);
    (thiz_cast->AutoRRefDeductTy = *value_cast);
}

void* clang_ASTContext_VaListTagDecl_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->VaListTagDecl;
}

void clang_ASTContext_VaListTagDecl_set(void* thiz, void* value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl* value_cast = reinterpret_cast<clang::Decl*>(value);
    (thiz_cast->VaListTagDecl = value_cast);
}

void* clang_ASTContext_MSGuidTagDecl_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->MSGuidTagDecl;
}

void clang_ASTContext_MSGuidTagDecl_set(void* thiz, void* value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TagDecl* value_cast = reinterpret_cast<clang::TagDecl*>(value);
    (thiz_cast->MSGuidTagDecl = value_cast);
}

void* clang_ASTContext_MSTypeInfoTagDecl_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return (void*)thiz_cast->MSTypeInfoTagDecl;
}

void clang_ASTContext_MSTypeInfoTagDecl_set(void* thiz, void* value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TagDecl* value_cast = reinterpret_cast<clang::TagDecl*>(value);
    (thiz_cast->MSTypeInfoTagDecl = value_cast);
}

unsigned int clang_ASTContext_NumImplicitDefaultConstructors_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitDefaultConstructors;
}

void clang_ASTContext_NumImplicitDefaultConstructors_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitDefaultConstructors = value);
}

unsigned int clang_ASTContext_NumImplicitDefaultConstructorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitDefaultConstructorsDeclared;
}

void clang_ASTContext_NumImplicitDefaultConstructorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitDefaultConstructorsDeclared = value);
}

unsigned int clang_ASTContext_NumImplicitCopyConstructors_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitCopyConstructors;
}

void clang_ASTContext_NumImplicitCopyConstructors_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitCopyConstructors = value);
}

unsigned int clang_ASTContext_NumImplicitCopyConstructorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitCopyConstructorsDeclared;
}

void clang_ASTContext_NumImplicitCopyConstructorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitCopyConstructorsDeclared = value);
}

unsigned int clang_ASTContext_NumImplicitMoveConstructors_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitMoveConstructors;
}

void clang_ASTContext_NumImplicitMoveConstructors_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitMoveConstructors = value);
}

unsigned int clang_ASTContext_NumImplicitMoveConstructorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitMoveConstructorsDeclared;
}

void clang_ASTContext_NumImplicitMoveConstructorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitMoveConstructorsDeclared = value);
}

unsigned int clang_ASTContext_NumImplicitCopyAssignmentOperators_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitCopyAssignmentOperators;
}

void clang_ASTContext_NumImplicitCopyAssignmentOperators_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitCopyAssignmentOperators = value);
}

unsigned int clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitCopyAssignmentOperatorsDeclared;
}

void clang_ASTContext_NumImplicitCopyAssignmentOperatorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitCopyAssignmentOperatorsDeclared = value);
}

unsigned int clang_ASTContext_NumImplicitMoveAssignmentOperators_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitMoveAssignmentOperators;
}

void clang_ASTContext_NumImplicitMoveAssignmentOperators_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitMoveAssignmentOperators = value);
}

unsigned int clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitMoveAssignmentOperatorsDeclared;
}

void clang_ASTContext_NumImplicitMoveAssignmentOperatorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitMoveAssignmentOperatorsDeclared = value);
}

unsigned int clang_ASTContext_NumImplicitDestructors_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitDestructors;
}

void clang_ASTContext_NumImplicitDestructors_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitDestructors = value);
}

unsigned int clang_ASTContext_NumImplicitDestructorsDeclared_get(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->NumImplicitDestructorsDeclared;
}

void clang_ASTContext_NumImplicitDestructorsDeclared_set(void* thiz, unsigned int value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    (thiz_cast->NumImplicitDestructorsDeclared = value);
}


// END KRAPPER GEN for clang::ASTContext


// BEGIN KRAPPER GEN for clang::DeclContext

void clang_DeclContext_dispose(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->~DeclContext();
}

bool clang_DeclContext_has_valid_decl_kind(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->hasValidDeclKind();
}

unsigned int clang_DeclContext_get_decl_kind(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (unsigned int)thiz_cast->getDeclKind();
}

const char* clang_DeclContext_get_decl_kind_name(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->getDeclKindName();
}

void* clang_DeclContext_get_parent(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getParent();
}

void* clang_DeclContext_get_lexical_parent(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getLexicalParent();
}

void* clang_DeclContext_get_lookup_parent(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getLookupParent();
}

const void* clang_DeclContext_get_parent_ast_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)&(thiz_cast->getParentASTContext());
}

bool clang_DeclContext_is_closure(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isClosure();
}

bool clang_DeclContext_is_obj_c_container(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isObjCContainer();
}

bool clang_DeclContext_is_function_or_method(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isFunctionOrMethod();
}

bool clang_DeclContext_is_lookup_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isLookupContext();
}

bool clang_DeclContext_is_file_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isFileContext();
}

bool clang_DeclContext_is_translation_unit(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isTranslationUnit();
}

bool clang_DeclContext_is_record(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isRecord();
}

bool clang_DeclContext_is_requires_expr_body(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isRequiresExprBody();
}

bool clang_DeclContext_is_namespace(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isNamespace();
}

bool clang_DeclContext_is_std_namespace(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isStdNamespace();
}

bool clang_DeclContext_is_inline_namespace(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isInlineNamespace();
}

bool clang_DeclContext_is_dependent_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isDependentContext();
}

bool clang_DeclContext_is_transparent_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isTransparentContext();
}

bool clang_DeclContext_is_extern_c_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isExternCContext();
}

bool clang_DeclContext_is_extern_cxx_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->isExternCXXContext();
}

bool clang_DeclContext_equals(void* thiz, void* DC) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return thiz_cast->Equals(DC_cast);
}

bool clang_DeclContext_encloses(void* thiz, void* DC) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return thiz_cast->Encloses(DC_cast);
}

bool clang_DeclContext_lexically_encloses(void* thiz, void* DC) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return thiz_cast->LexicallyEncloses(DC_cast);
}

void* clang_DeclContext_get_non_closure_ancestor(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getNonClosureAncestor();
}

void* clang_DeclContext_get_non_transparent_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getNonTransparentContext();
}

void* clang_DeclContext_get_primary_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getPrimaryContext();
}

void* clang_DeclContext_get_redecl_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getRedeclContext();
}

void* clang_DeclContext_get_enclosing_namespace_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getEnclosingNamespaceContext();
}

void* clang_DeclContext_get_outer_lexical_record_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return (void*)thiz_cast->getOuterLexicalRecordContext();
}

bool clang_DeclContext_in_enclosing_namespace_set_of(void* thiz, void* NS) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::DeclContext* NS_cast = reinterpret_cast<const clang::DeclContext*>(NS);
    return thiz_cast->InEnclosingNamespaceSetOf(NS_cast);
}

void clang_DeclContext_decls(void* thiz, void* ret_value) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    std::vector<clang::Decl*>* ret_value_cast = reinterpret_cast<std::vector<clang::Decl*>*>(ret_value);
    auto __range_5 = thiz_cast->decls();
    std::vector<clang::Decl*> __vec_5;
    for (auto __it_5 = __range_5.begin(); __it_5 != __range_5.end(); ++__it_5) __vec_5.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_5));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_5);
}

bool clang_DeclContext_decls_empty(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->decls_empty();
}

void clang_DeclContext_noload_decls(void* thiz, void* ret_value) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    std::vector<clang::Decl*>* ret_value_cast = reinterpret_cast<std::vector<clang::Decl*>*>(ret_value);
    auto __range_6 = thiz_cast->noload_decls();
    std::vector<clang::Decl*> __vec_6;
    for (auto __it_6 = __range_6.begin(); __it_6 != __range_6.end(); ++__it_6) __vec_6.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_6));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_6);
}

void clang_DeclContext_add_decl(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->addDecl(D_cast);
}

void clang_DeclContext_add_decl_internal(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->addDeclInternal(D_cast);
}

void clang_DeclContext_add_hidden_decl(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->addHiddenDecl(D_cast);
}

void clang_DeclContext_remove_decl(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->removeDecl(D_cast);
}

bool clang_DeclContext_contains_decl(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    return thiz_cast->containsDecl(D_cast);
}

bool clang_DeclContext_contains_decl_and_load(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    return thiz_cast->containsDeclAndLoad(D_cast);
}

void clang_DeclContext_make_decl_visible_in_context(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    clang::NamedDecl* D_cast = reinterpret_cast<clang::NamedDecl*>(D);
    thiz_cast->makeDeclVisibleInContext(D_cast);
}

void clang_DeclContext_set_must_build_lookup_table(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->setMustBuildLookupTable();
}

bool clang_DeclContext_has_external_lexical_storage(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->hasExternalLexicalStorage();
}

void clang_DeclContext_set_has_external_lexical_storage(void* thiz, bool ES) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->setHasExternalLexicalStorage(ES);
}

bool clang_DeclContext_has_external_visible_storage(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->hasExternalVisibleStorage();
}

void clang_DeclContext_set_has_external_visible_storage(void* thiz, bool ES) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->setHasExternalVisibleStorage(ES);
}

bool clang_DeclContext_is_decl_in_lexical_traversal(void* thiz, void* D) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return thiz_cast->isDeclInLexicalTraversal(D_cast);
}

void clang_DeclContext_set_use_qualified_lookup(void* thiz, bool use) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->setUseQualifiedLookup(use);
}

bool clang_DeclContext_should_use_qualified_lookup(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->shouldUseQualifiedLookup();
}

bool clang_DeclContext_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::DeclContext::classof(D_cast);
}

bool clang_DeclContext_classof__const_clang_DeclContext_P(void* D) {
    const clang::DeclContext* D_cast = reinterpret_cast<const clang::DeclContext*>(D);
    return clang::DeclContext::classof(D_cast);
}

void clang_DeclContext_dump_as_decl(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->dumpAsDecl();
}

void clang_DeclContext_dump_as_decl__const_clang_ASTContext_P(void* thiz, void* Ctx) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    thiz_cast->dumpAsDecl(Ctx_cast);
}

void clang_DeclContext_dump_decl_context(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->dumpDeclContext();
}

void clang_DeclContext_dump_lookups(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    thiz_cast->dumpLookups();
}

int clang_DeclContext_size_of() {
    return sizeof(clang::DeclContext);
}

int clang_DeclContext_align_of() {
    return alignof(clang::DeclContext);
}

void* clang_DeclContext_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_TranslationUnitDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TranslationUnitDecl>(reinterpret_cast<clang::DeclContext*>(p));
}


// END KRAPPER GEN for clang::DeclContext


// BEGIN KRAPPER GEN for clang::Decl

unsigned int clang_Decl_get_kind(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned int)thiz_cast->getKind();
}

const char* clang_Decl_get_decl_kind_name(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getDeclKindName();
}

void* clang_Decl_get_next_decl_in_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getNextDeclInContext();
}

void* clang_Decl_get_decl_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getDeclContext();
}

void* clang_Decl_get_non_transparent_decl_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getNonTransparentDeclContext();
}

void* clang_Decl_get_non_closure_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getNonClosureContext();
}

void* clang_Decl_get_translation_unit_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getTranslationUnitDecl();
}

bool clang_Decl_is_in_anonymous_namespace(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInAnonymousNamespace();
}

bool clang_Decl_is_in_std_namespace(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInStdNamespace();
}

bool clang_Decl_is_file_context_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFileContextDecl();
}

bool clang_Decl_is_flexible_array_member_like(void* Context, void* D, void* Ty, int StrictFlexArraysLevel, bool IgnoreTemplateOrMacroSubstitution) {
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    clang::LangOptionsBase::StrictFlexArraysLevelKind StrictFlexArraysLevel_cast = (clang::LangOptionsBase::StrictFlexArraysLevelKind)StrictFlexArraysLevel;
    return clang::Decl::isFlexibleArrayMemberLike(*Context_cast, D_cast, *Ty_cast, StrictFlexArraysLevel_cast, IgnoreTemplateOrMacroSubstitution);
}

const void* clang_Decl_get_ast_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)&(thiz_cast->getASTContext());
}

void clang_Decl_set_access(void* thiz, unsigned char AS) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    clang::AccessSpecifier AS_cast = (clang::AccessSpecifier)AS;
    thiz_cast->setAccess(AS_cast);
}

unsigned char clang_Decl_get_access(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned char)thiz_cast->getAccess();
}

unsigned char clang_Decl_get_access_unsafe(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned char)thiz_cast->getAccessUnsafe();
}

bool clang_Decl_has_attrs(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->hasAttrs();
}

void clang_Decl_drop_attrs(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->dropAttrs();
}

unsigned int clang_Decl_get_max_alignment(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getMaxAlignment();
}

void clang_Decl_set_invalid_decl(void* thiz, bool Invalid) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setInvalidDecl(Invalid);
}

bool clang_Decl_is_invalid_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInvalidDecl();
}

bool clang_Decl_is_implicit(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isImplicit();
}

void clang_Decl_set_implicit(void* thiz, bool I) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setImplicit(I);
}

bool clang_Decl_is_used(void* thiz, bool CheckUsedAttr) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isUsed(CheckUsedAttr);
}

void clang_Decl_set_is_used(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setIsUsed();
}

void clang_Decl_mark_used(void* thiz, void* C) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    clang::ASTContext* C_cast = reinterpret_cast<clang::ASTContext*>(C);
    thiz_cast->markUsed(*C_cast);
}

bool clang_Decl_is_referenced(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isReferenced();
}

bool clang_Decl_is_this_declaration_referenced(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isThisDeclarationReferenced();
}

void clang_Decl_set_referenced(void* thiz, bool R) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setReferenced(R);
}

void clang_Decl_invalidate_cached_linkage(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->invalidateCachedLinkage();
}

bool clang_Decl_is_top_level_decl_in_obj_c_container(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isTopLevelDeclInObjCContainer();
}

void clang_Decl_set_top_level_decl_in_obj_c_container(void* thiz, bool V) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setTopLevelDeclInObjCContainer(V);
}

bool clang_Decl_is_module_private(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isModulePrivate();
}

bool clang_Decl_is_module_local(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isModuleLocal();
}

bool clang_Decl_is_in_export_decl_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInExportDeclContext();
}

bool clang_Decl_is_invisible_outside_the_owning_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInvisibleOutsideTheOwningModule();
}

bool clang_Decl_is_in_another_module_unit(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInAnotherModuleUnit();
}

bool clang_Decl_is_in_current_module_unit(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInCurrentModuleUnit();
}

bool clang_Decl_should_emit_in_external_source(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->shouldEmitInExternalSource();
}

bool clang_Decl_is_from_explicit_global_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFromExplicitGlobalModule();
}

bool clang_Decl_is_from_global_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFromGlobalModule();
}

bool clang_Decl_is_in_named_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInNamedModule();
}

bool clang_Decl_is_from_header_unit(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFromHeaderUnit();
}

bool clang_Decl_has_defining_attr(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->hasDefiningAttr();
}

void clang_Decl_set_from_ast_file(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setFromASTFile();
}

void clang_Decl_set_owning_module_id(void* thiz, unsigned int ID) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setOwningModuleID(ID);
}

unsigned int clang_Decl_get_availability(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned int)thiz_cast->getAvailability();
}

bool clang_Decl_is_deprecated(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isDeprecated();
}

bool clang_Decl_is_unavailable(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isUnavailable();
}

bool clang_Decl_is_weak_imported(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isWeakImported();
}

bool clang_Decl_can_be_weak_imported(void* thiz, bool IsDefinition) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->canBeWeakImported(IsDefinition);
}

bool clang_Decl_is_from_ast_file(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFromASTFile();
}

unsigned int clang_Decl_get_owning_module_id(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getOwningModuleID();
}

bool clang_Decl_has_owning_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->hasOwningModule();
}

bool clang_Decl_is_unconditionally_visible(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isUnconditionallyVisible();
}

bool clang_Decl_is_reachable(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isReachable();
}

void clang_Decl_set_visible_despite_owning_module(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setVisibleDespiteOwningModule();
}

void clang_Decl_set_visible_promoted(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setVisiblePromoted();
}

unsigned char clang_Decl_get_module_ownership_kind(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned char)thiz_cast->getModuleOwnershipKind();
}

void clang_Decl_set_module_ownership_kind(void* thiz, unsigned char MOK) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    clang::Decl::ModuleOwnershipKind MOK_cast = (clang::Decl::ModuleOwnershipKind)MOK;
    thiz_cast->setModuleOwnershipKind(MOK_cast);
}

unsigned int clang_Decl_get_identifier_namespace(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getIdentifierNamespace();
}

bool clang_Decl_is_in_identifier_namespace(void* thiz, unsigned int NS) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInIdentifierNamespace(NS);
}

unsigned int clang_Decl_get_identifier_namespace_for_kind(unsigned int DK) {
    clang::Decl::Kind DK_cast = (clang::Decl::Kind)DK;
    return clang::Decl::getIdentifierNamespaceForKind(DK_cast);
}

bool clang_Decl_has_tag_identifier_namespace(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->hasTagIdentifierNamespace();
}

bool clang_Decl_is_tag_identifier_namespace(unsigned int NS) {
    return clang::Decl::isTagIdentifierNamespace(NS);
}

void* clang_Decl_get_lexical_decl_context(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getLexicalDeclContext();
}

bool clang_Decl_is_out_of_line(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isOutOfLine();
}

void clang_Decl_set_decl_context(void* thiz, void* DC) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    clang::DeclContext* DC_cast = reinterpret_cast<clang::DeclContext*>(DC);
    thiz_cast->setDeclContext(DC_cast);
}

void clang_Decl_set_lexical_decl_context(void* thiz, void* DC) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    clang::DeclContext* DC_cast = reinterpret_cast<clang::DeclContext*>(DC);
    thiz_cast->setLexicalDeclContext(DC_cast);
}

bool clang_Decl_is_templated(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isTemplated();
}

unsigned int clang_Decl_get_template_depth(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getTemplateDepth();
}

bool clang_Decl_is_defined_outside_function_or_method(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isDefinedOutsideFunctionOrMethod();
}

bool clang_Decl_is_in_local_scope_for_instantiation(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isInLocalScopeForInstantiation();
}

void* clang_Decl_get_parent_function_or_method(void* thiz, bool LexicalParent) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getParentFunctionOrMethod(LexicalParent);
}

void* clang_Decl_get_canonical_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_Decl_is_canonical_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isCanonicalDecl();
}

void clang_Decl_redecls(void* thiz, void* ret_value) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    std::vector<clang::Decl*>* ret_value_cast = reinterpret_cast<std::vector<clang::Decl*>*>(ret_value);
    auto __range_7 = thiz_cast->redecls();
    std::vector<clang::Decl*> __vec_7;
    for (auto __it_7 = __range_7.begin(); __it_7 != __range_7.end(); ++__it_7) __vec_7.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_7));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_7);
}

void* clang_Decl_get_previous_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getPreviousDecl();
}

bool clang_Decl_is_first_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFirstDecl();
}

void* clang_Decl_get_most_recent_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

bool clang_Decl_has_body(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->hasBody();
}

void clang_Decl_add(unsigned int k) {
    clang::Decl::Kind k_cast = (clang::Decl::Kind)k;
    clang::Decl::add(k_cast);
}

void clang_Decl_enable_statistics() {
    clang::Decl::EnableStatistics();
}

void clang_Decl_print_stats() {
    clang::Decl::PrintStats();
}

bool clang_Decl_is_template_parameter(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isTemplateParameter();
}

bool clang_Decl_is_template_parameter_pack(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isTemplateParameterPack();
}

bool clang_Decl_is_parameter_pack(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isParameterPack();
}

bool clang_Decl_is_template_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isTemplateDecl();
}

bool clang_Decl_is_function_or_function_template(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFunctionOrFunctionTemplate();
}

void* clang_Decl_get_as_function(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getAsFunction();
}

void clang_Decl_set_local_extern_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setLocalExternDecl();
}

bool clang_Decl_is_local_extern_decl(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isLocalExternDecl();
}

void clang_Decl_set_object_of_friend_decl(void* thiz, bool PerformFriendInjection) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setObjectOfFriendDecl(PerformFriendInjection);
}

void clang_Decl_clear_identifier_namespace(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->clearIdentifierNamespace();
}

unsigned int clang_Decl_get_friend_object_kind(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (unsigned int)thiz_cast->getFriendObjectKind();
}

void clang_Decl_set_non_member_operator(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->setNonMemberOperator();
}

bool clang_Decl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::Decl::classofKind(K_cast);
}

void* clang_Decl_cast_to_decl_context(void* _arg_0) {
    const clang::Decl* _arg_0_cast = reinterpret_cast<const clang::Decl*>(_arg_0);
    return (void*)clang::Decl::castToDeclContext(_arg_0_cast);
}

void* clang_Decl_cast_from_decl_context(void* _arg_0) {
    const clang::DeclContext* _arg_0_cast = reinterpret_cast<const clang::DeclContext*>(_arg_0);
    return (void*)clang::Decl::castFromDeclContext(_arg_0_cast);
}

void clang_Decl_dump(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->dump();
}

void clang_Decl_dump_color(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    thiz_cast->dumpColor();
}

long clang_Decl_get_id(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->getID();
}

bool clang_Decl_is_function_pointer_type(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return thiz_cast->isFunctionPointerType();
}

int clang_Decl_size_of() {
    return sizeof(clang::Decl);
}

int clang_Decl_align_of() {
    return alignof(clang::Decl);
}

void* clang_Decl_dyncast_clang_NamedDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamedDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TypeDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypeDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_ValueDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ValueDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_DeclaratorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::DeclaratorDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TranslationUnitDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TranslationUnitDecl>(reinterpret_cast<clang::Decl*>(p));
}


// END KRAPPER GEN for clang::Decl


// BEGIN KRAPPER GEN for clang::ASTUnit

void clang_ASTUnit_dispose(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    thiz_cast->~ASTUnit();
}

bool clang_ASTUnit_is_main_file_ast(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->isMainFileAST();
}

bool clang_ASTUnit_is_unsafe_to_free(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->isUnsafeToFree();
}

void clang_ASTUnit_set_unsafe_to_free(void* thiz, bool Value) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    thiz_cast->setUnsafeToFree(Value);
}

void* clang_ASTUnit_get_ast_context(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return (void*)&(thiz_cast->getASTContext());
}

void clang_ASTUnit_enable_source_file_diagnostics(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    thiz_cast->enableSourceFileDiagnostics();
}

bool clang_ASTUnit_has_sema(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->hasSema();
}

bool clang_ASTUnit_get_only_local_decls(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->getOnlyLocalDecls();
}

bool clang_ASTUnit_get_owns_remapped_file_buffers(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->getOwnsRemappedFileBuffers();
}

void clang_ASTUnit_set_owns_remapped_file_buffers(void* thiz, bool val) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    thiz_cast->setOwnsRemappedFileBuffers(val);
}

size_t clang_ASTUnit_top_level_size(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->top_level_size();
}

bool clang_ASTUnit_top_level_empty(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->top_level_empty();
}

void clang_ASTUnit_add_top_level_decl(void* thiz, void* D) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->addTopLevelDecl(D_cast);
}

void clang_ASTUnit_add_file_level_decl(void* thiz, void* D) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    clang::Decl* D_cast = reinterpret_cast<clang::Decl*>(D);
    thiz_cast->addFileLevelDecl(D_cast);
}

unsigned int* clang_ASTUnit_get_current_top_level_hash_value(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return &(thiz_cast->getCurrentTopLevelHashValue());
}

unsigned int clang_ASTUnit_get_preamble_counter_for_tests(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->getPreambleCounterForTests();
}

unsigned int clang_ASTUnit_stored_diag_size(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->stored_diag_size();
}

unsigned int clang_ASTUnit_cached_completion_size(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->cached_completion_size();
}

bool clang_ASTUnit_is_module_file(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return thiz_cast->isModuleFile();
}

unsigned int clang_ASTUnit_get_translation_unit_kind(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    return (unsigned int)thiz_cast->getTranslationUnitKind();
}

void clang_ASTUnit_reset_for_parse(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    thiz_cast->ResetForParse();
}

bool clang_ASTUnit_save(void* thiz, const char* File) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    llvm::StringRef File_cast = llvm::StringRef(File);
    return thiz_cast->Save(File_cast);
}

const char* clang_ASTUnit_get_original_source_file_name(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    std::string ret_value = thiz_cast->getOriginalSourceFileName().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

const char* clang_ASTUnit_get_main_file_name(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    std::string ret_value = thiz_cast->getMainFileName().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

const char* clang_ASTUnit_get_ast_file_name(void* thiz) {
    clang::ASTUnit* thiz_cast = reinterpret_cast<clang::ASTUnit*>(thiz);
    std::string ret_value = thiz_cast->getASTFileName().str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

int clang_ASTUnit_size_of() {
    return sizeof(clang::ASTUnit);
}

int clang_ASTUnit_align_of() {
    return alignof(clang::ASTUnit);
}


// END KRAPPER GEN for clang::ASTUnit


// BEGIN KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>

void* std_vector_clang_CXXBaseSpecifier_P_new(void* location) {
    return new (location) std::vector<clang::CXXBaseSpecifier*>();
}

void* std_vector_clang_CXXBaseSpecifier_P_new__size_t_const_allocator_type_and(void* location, size_t __n) {
    return new (location) std::vector<clang::CXXBaseSpecifier*>(__n);
}

void* std_vector_clang_CXXBaseSpecifier_P_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<clang::CXXBaseSpecifier*>* __x_cast = reinterpret_cast<const std::vector<clang::CXXBaseSpecifier*>*>(__x);
    return new (location) std::vector<clang::CXXBaseSpecifier*>(*__x_cast);
}

void std_vector_clang_CXXBaseSpecifier_P_dispose(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_clang_CXXBaseSpecifier_P_op_assign(void* thiz, void* __x) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    const std::vector<clang::CXXBaseSpecifier*>* __x_cast = reinterpret_cast<const std::vector<clang::CXXBaseSpecifier*>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

size_t std_vector_clang_CXXBaseSpecifier_P_size(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_clang_CXXBaseSpecifier_P_max_size(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_clang_CXXBaseSpecifier_P_resize(void* thiz, size_t __new_size) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_clang_CXXBaseSpecifier_P_shrink_to_fit(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_clang_CXXBaseSpecifier_P_capacity(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_clang_CXXBaseSpecifier_P_empty(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_clang_CXXBaseSpecifier_P_reserve(void* thiz, size_t __n) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->reserve(__n);
}

void* std_vector_clang_CXXBaseSpecifier_P_op_ind(void* thiz, size_t __n) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return (void*)thiz_cast->operator[](__n);
}

void* std_vector_clang_CXXBaseSpecifier_P_at(void* thiz, size_t __n) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return (void*)thiz_cast->at(__n);
}

void* std_vector_clang_CXXBaseSpecifier_P_front(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return (void*)thiz_cast->front();
}

void* std_vector_clang_CXXBaseSpecifier_P_back(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return (void*)thiz_cast->back();
}

void* std_vector_clang_CXXBaseSpecifier_P_data(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    return (void*)thiz_cast->data();
}

void std_vector_clang_CXXBaseSpecifier_P_pop_back(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_clang_CXXBaseSpecifier_P_swap(void* thiz, void* __x) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    std::vector<clang::CXXBaseSpecifier*>* __x_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_clang_CXXBaseSpecifier_P_clear(void* thiz) {
    std::vector<clang::CXXBaseSpecifier*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(thiz);
    thiz_cast->clear();
}

int std_vector_clang_CXXBaseSpecifier_P_size_of() {
    return sizeof(std::vector<clang::CXXBaseSpecifier*>);
}

int std_vector_clang_CXXBaseSpecifier_P_align_of() {
    return alignof(std::vector<clang::CXXBaseSpecifier*>);
}


// END KRAPPER GEN for std::vector<clang::CXXBaseSpecifier*>


// BEGIN KRAPPER GEN for std::vector<clang::CXXMethodDecl*>

void* std_vector_clang_CXXMethodDecl_P_new(void* location) {
    return new (location) std::vector<clang::CXXMethodDecl*>();
}

void* std_vector_clang_CXXMethodDecl_P_new__size_t_const_allocator_type_and(void* location, size_t __n) {
    return new (location) std::vector<clang::CXXMethodDecl*>(__n);
}

void* std_vector_clang_CXXMethodDecl_P_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<clang::CXXMethodDecl*>* __x_cast = reinterpret_cast<const std::vector<clang::CXXMethodDecl*>*>(__x);
    return new (location) std::vector<clang::CXXMethodDecl*>(*__x_cast);
}

void std_vector_clang_CXXMethodDecl_P_dispose(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_clang_CXXMethodDecl_P_op_assign(void* thiz, void* __x) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    const std::vector<clang::CXXMethodDecl*>* __x_cast = reinterpret_cast<const std::vector<clang::CXXMethodDecl*>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

size_t std_vector_clang_CXXMethodDecl_P_size(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_clang_CXXMethodDecl_P_max_size(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_clang_CXXMethodDecl_P_resize(void* thiz, size_t __new_size) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_clang_CXXMethodDecl_P_shrink_to_fit(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_clang_CXXMethodDecl_P_capacity(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_clang_CXXMethodDecl_P_empty(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_clang_CXXMethodDecl_P_reserve(void* thiz, size_t __n) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->reserve(__n);
}

void* std_vector_clang_CXXMethodDecl_P_op_ind(void* thiz, size_t __n) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return (void*)thiz_cast->operator[](__n);
}

void* std_vector_clang_CXXMethodDecl_P_at(void* thiz, size_t __n) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return (void*)thiz_cast->at(__n);
}

void* std_vector_clang_CXXMethodDecl_P_front(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return (void*)thiz_cast->front();
}

void* std_vector_clang_CXXMethodDecl_P_back(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return (void*)thiz_cast->back();
}

void* std_vector_clang_CXXMethodDecl_P_data(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    return (void*)thiz_cast->data();
}

void std_vector_clang_CXXMethodDecl_P_pop_back(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_clang_CXXMethodDecl_P_swap(void* thiz, void* __x) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    std::vector<clang::CXXMethodDecl*>* __x_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_clang_CXXMethodDecl_P_clear(void* thiz) {
    std::vector<clang::CXXMethodDecl*>* thiz_cast = reinterpret_cast<std::vector<clang::CXXMethodDecl*>*>(thiz);
    thiz_cast->clear();
}

int std_vector_clang_CXXMethodDecl_P_size_of() {
    return sizeof(std::vector<clang::CXXMethodDecl*>);
}

int std_vector_clang_CXXMethodDecl_P_align_of() {
    return alignof(std::vector<clang::CXXMethodDecl*>);
}


// END KRAPPER GEN for std::vector<clang::CXXMethodDecl*>


// BEGIN KRAPPER GEN for std::vector<clang::Decl*>

void* std_vector_clang_Decl_P_new(void* location) {
    return new (location) std::vector<clang::Decl*>();
}

void* std_vector_clang_Decl_P_new__size_t_const_allocator_type_and(void* location, size_t __n) {
    return new (location) std::vector<clang::Decl*>(__n);
}

void* std_vector_clang_Decl_P_new__const_std_vector_and(void* location, void* __x) {
    const std::vector<clang::Decl*>* __x_cast = reinterpret_cast<const std::vector<clang::Decl*>*>(__x);
    return new (location) std::vector<clang::Decl*>(*__x_cast);
}

void std_vector_clang_Decl_P_dispose(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->~vector();
}

void* std_vector_clang_Decl_P_op_assign(void* thiz, void* __x) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    const std::vector<clang::Decl*>* __x_cast = reinterpret_cast<const std::vector<clang::Decl*>*>(__x);
    return (void*)&((*thiz_cast = *__x_cast));
}

size_t std_vector_clang_Decl_P_size(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return thiz_cast->size();
}

size_t std_vector_clang_Decl_P_max_size(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return thiz_cast->max_size();
}

void std_vector_clang_Decl_P_resize(void* thiz, size_t __new_size) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->resize(__new_size);
}

void std_vector_clang_Decl_P_shrink_to_fit(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->shrink_to_fit();
}

size_t std_vector_clang_Decl_P_capacity(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return thiz_cast->capacity();
}

bool std_vector_clang_Decl_P_empty(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return thiz_cast->empty();
}

void std_vector_clang_Decl_P_reserve(void* thiz, size_t __n) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->reserve(__n);
}

void* std_vector_clang_Decl_P_op_ind(void* thiz, size_t __n) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return (void*)thiz_cast->operator[](__n);
}

void* std_vector_clang_Decl_P_at(void* thiz, size_t __n) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return (void*)thiz_cast->at(__n);
}

void* std_vector_clang_Decl_P_front(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return (void*)thiz_cast->front();
}

void* std_vector_clang_Decl_P_back(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return (void*)thiz_cast->back();
}

void* std_vector_clang_Decl_P_data(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    return (void*)thiz_cast->data();
}

void std_vector_clang_Decl_P_pop_back(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->pop_back();
}

void std_vector_clang_Decl_P_swap(void* thiz, void* __x) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    std::vector<clang::Decl*>* __x_cast = reinterpret_cast<std::vector<clang::Decl*>*>(__x);
    thiz_cast->swap(*__x_cast);
}

void std_vector_clang_Decl_P_clear(void* thiz) {
    std::vector<clang::Decl*>* thiz_cast = reinterpret_cast<std::vector<clang::Decl*>*>(thiz);
    thiz_cast->clear();
}

int std_vector_clang_Decl_P_size_of() {
    return sizeof(std::vector<clang::Decl*>);
}

int std_vector_clang_Decl_P_align_of() {
    return alignof(std::vector<clang::Decl*>);
}


// END KRAPPER GEN for std::vector<clang::Decl*>


void* clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}
void* _clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}
void* __clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}
void* ___clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}

}

