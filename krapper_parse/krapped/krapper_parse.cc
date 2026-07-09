#include "krapper_parse.h"
#include "../include/clang_slice.h"
#include "KrapperForce_std_vector_clang_CXXBaseSpecifierPtr.h"
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

void* clang_NamedDecl_dyncast_clang_DeclaratorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::DeclaratorDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_VarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::VarDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_ValueDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ValueDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_EnumConstantDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumConstantDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_EnumDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TypeDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypeDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TypedefNameDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypedefNameDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_ParmVarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ParmVarDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_NamespaceBaseDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceBaseDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_NamespaceDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TemplateTypeParmDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateTypeParmDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}

void* clang_NamedDecl_dyncast_clang_TemplateDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateDecl>(reinterpret_cast<clang::NamedDecl*>(p));
}


// END KRAPPER GEN for clang::NamedDecl


// BEGIN KRAPPER GEN for clang::QualType

void* clang_QualType_new(void* location) {
    return new (location) clang::QualType();
}

void* clang_QualType_new__const_clang_Type_P_unsigned_int(void* location, void* Ptr, unsigned int Quals) {
    const clang::Type* Ptr_cast = reinterpret_cast<const clang::Type*>(Ptr);
    return new (location) clang::QualType(Ptr_cast, Quals);
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

void* clang_QualType_get_type_ptr(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (void*)thiz_cast->getTypePtr();
}

void* clang_QualType_get_type_ptr_or_null(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (void*)thiz_cast->getTypePtrOrNull();
}

const void* clang_QualType_get_as_opaque_ptr(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return thiz_cast->getAsOpaquePtr();
}

void clang_QualType_get_from_opaque_ptr(const void* Ptr, void* ret_value) {
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(clang::QualType::getFromOpaquePtr(Ptr));
}

void* clang_QualType_op_reference(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (void*)&(thiz_cast->operator*());
}

void* clang_QualType_op_pointer_reference(void* thiz) {
    clang::QualType* thiz_cast = reinterpret_cast<clang::QualType*>(thiz);
    return (void*)thiz_cast->operator->();
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


// BEGIN KRAPPER GEN for llvm::APSInt

void* llvm_APSInt_new(void* location) {
    return new (location) llvm::APSInt();
}

void* llvm_APSInt_new__unsigned_int_bool(void* location, unsigned int BitWidth, bool isUnsigned) {
    return new (location) llvm::APSInt(BitWidth, isUnsigned);
}

void* llvm_APSInt_new__llvm_StringRef(void* location, const char* Str) {
    llvm::StringRef Str_cast = llvm::StringRef(Str);
    return new (location) llvm::APSInt(Str_cast);
}

bool llvm_APSInt_is_negative(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isNegative();
}

bool llvm_APSInt_is_non_negative(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isNonNegative();
}

bool llvm_APSInt_is_strictly_positive(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isStrictlyPositive();
}

void* llvm_APSInt_op_assign__unsigned_long(void* thiz, unsigned long RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return (void*)&((*thiz_cast = RHS));
}

bool llvm_APSInt_is_signed(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isSigned();
}

bool llvm_APSInt_is_unsigned(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isUnsigned();
}

void llvm_APSInt_set_is_unsigned(void* thiz, bool Val) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    thiz_cast->setIsUnsigned(Val);
}

void llvm_APSInt_set_is_signed(void* thiz, bool Val) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    thiz_cast->setIsSigned(Val);
}

bool llvm_APSInt_is_representable_by_int64(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->isRepresentableByInt64();
}

long llvm_APSInt_get_ext_value(void* thiz) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return thiz_cast->getExtValue();
}

void llvm_APSInt_trunc(void* thiz, unsigned int width, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->trunc(width));
}

void llvm_APSInt_extend(void* thiz, unsigned int width, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->extend(width));
}

void llvm_APSInt_ext_or_trunc(void* thiz, unsigned int width, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->extOrTrunc(width));
}

void* llvm_APSInt_operator_modeq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator%=(*RHS_cast));
}

void* llvm_APSInt_operator_diveq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator/=(*RHS_cast));
}

void llvm_APSInt_op_mod(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast % *RHS_cast);
}

void llvm_APSInt_op_divide(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast / *RHS_cast);
}

void llvm_APSInt_op_shr(void* thiz, unsigned int Amt, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast >> Amt);
}

void* llvm_APSInt_operator_shreq(void* thiz, unsigned int Amt) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return (void*)&(thiz_cast->operator>>=(Amt));
}

void llvm_APSInt_relative_shr(void* thiz, unsigned int Amt, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->relativeShr(Amt));
}

bool llvm_APSInt_op_lt(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast < *RHS_cast;
}

bool llvm_APSInt_op_gt(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast > *RHS_cast;
}

bool llvm_APSInt_op_lteq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast <= *RHS_cast;
}

bool llvm_APSInt_op_gteq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast >= *RHS_cast;
}

bool llvm_APSInt_op_eq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast == *RHS_cast;
}

bool llvm_APSInt_op_neq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return *thiz_cast != *RHS_cast;
}

bool llvm_APSInt_op_neq__long(void* thiz, long RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return *thiz_cast != RHS;
}

bool llvm_APSInt_op_lteq__long(void* thiz, long RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return *thiz_cast <= RHS;
}

bool llvm_APSInt_op_gteq__long(void* thiz, long RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return *thiz_cast >= RHS;
}

bool llvm_APSInt_op_gt__long(void* thiz, long RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return *thiz_cast > RHS;
}

void llvm_APSInt_op_shl(void* thiz, unsigned int Bits, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast << Bits);
}

void* llvm_APSInt_operator_shleq(void* thiz, unsigned int Amt) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    return (void*)&(thiz_cast->operator<<=(Amt));
}

void llvm_APSInt_relative_shl(void* thiz, unsigned int Amt, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->relativeShl(Amt));
}

void llvm_APSInt_op_post_increment(void* thiz, int _arg_0, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->operator++(_arg_0));
}

void llvm_APSInt_op_post_decrement(void* thiz, int _arg_0, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->operator--(_arg_0));
}

void llvm_APSInt_op_unary_minus(void* thiz, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->operator-());
}

void* llvm_APSInt_op_plus_equals(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&((*thiz_cast += *RHS_cast));
}

void* llvm_APSInt_operator_minuseq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator-=(*RHS_cast));
}

void* llvm_APSInt_operator_timeseq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator*=(*RHS_cast));
}

void* llvm_APSInt_operator_andeq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator&=(*RHS_cast));
}

void* llvm_APSInt_operator_oreq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator|=(*RHS_cast));
}

void* llvm_APSInt_operator_xoreq(void* thiz, void* RHS) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    return (void*)&(thiz_cast->operator^=(*RHS_cast));
}

void llvm_APSInt_op_and(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast & *RHS_cast);
}

void llvm_APSInt_op_or(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast | *RHS_cast);
}

void llvm_APSInt_op_xor(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast ^ *RHS_cast);
}

void llvm_APSInt_op_times(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast * *RHS_cast);
}

void llvm_APSInt_op_plus(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast + *RHS_cast);
}

void llvm_APSInt_op_minus(void* thiz, void* RHS, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    const llvm::APSInt* RHS_cast = reinterpret_cast<const llvm::APSInt*>(RHS);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(*thiz_cast - *RHS_cast);
}

void llvm_APSInt_op_inv(void* thiz, void* ret_value) {
    llvm::APSInt* thiz_cast = reinterpret_cast<llvm::APSInt*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->operator~());
}

void llvm_APSInt_get_max_value(unsigned int numBits, bool Unsigned, void* ret_value) {
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(llvm::APSInt::getMaxValue(numBits, Unsigned));
}

void llvm_APSInt_get_min_value(unsigned int numBits, bool Unsigned, void* ret_value) {
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(llvm::APSInt::getMinValue(numBits, Unsigned));
}

bool llvm_APSInt_is_same_value(void* I1, void* I2) {
    const llvm::APSInt* I1_cast = reinterpret_cast<const llvm::APSInt*>(I1);
    const llvm::APSInt* I2_cast = reinterpret_cast<const llvm::APSInt*>(I2);
    return llvm::APSInt::isSameValue(*I1_cast, *I2_cast);
}

int llvm_APSInt_compare_values(void* I1, void* I2) {
    const llvm::APSInt* I1_cast = reinterpret_cast<const llvm::APSInt*>(I1);
    const llvm::APSInt* I2_cast = reinterpret_cast<const llvm::APSInt*>(I2);
    return llvm::APSInt::compareValues(*I1_cast, *I2_cast);
}

void llvm_APSInt_get(long X, void* ret_value) {
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(llvm::APSInt::get(X));
}

void llvm_APSInt_get_unsigned(unsigned long X, void* ret_value) {
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(llvm::APSInt::getUnsigned(X));
}

int llvm_APSInt_size_of() {
    return sizeof(llvm::APSInt);
}

int llvm_APSInt_align_of() {
    return alignof(llvm::APSInt);
}


// END KRAPPER GEN for llvm::APSInt


// BEGIN KRAPPER GEN for clang::TemplateArgument

void* clang_TemplateArgument_new(void* location) {
    return new (location) clang::TemplateArgument();
}

void* clang_TemplateArgument_new__clang_QualType_bool_bool(void* location, void* T, bool isNullPtr, bool IsDefaulted) {
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return new (location) clang::TemplateArgument(*T_cast, isNullPtr, IsDefaulted);
}

void* clang_TemplateArgument_new__clang_ValueDecl_P_clang_QualType_bool(void* location, void* D, void* QT, bool IsDefaulted) {
    clang::ValueDecl* D_cast = reinterpret_cast<clang::ValueDecl*>(D);
    clang::QualType* QT_cast = reinterpret_cast<clang::QualType*>(QT);
    return new (location) clang::TemplateArgument(D_cast, *QT_cast, IsDefaulted);
}

void* clang_TemplateArgument_new__const_clang_ASTContext_and_const_llvm_APSInt_and_clang_QualType_bool(void* location, void* Ctx, void* Value, void* Type, bool IsDefaulted) {
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    const llvm::APSInt* Value_cast = reinterpret_cast<const llvm::APSInt*>(Value);
    clang::QualType* Type_cast = reinterpret_cast<clang::QualType*>(Type);
    return new (location) clang::TemplateArgument(*Ctx_cast, *Value_cast, *Type_cast, IsDefaulted);
}

void* clang_TemplateArgument_new__const_clang_TemplateArgument_and_clang_QualType(void* location, void* Other, void* Type) {
    const clang::TemplateArgument* Other_cast = reinterpret_cast<const clang::TemplateArgument*>(Other);
    clang::QualType* Type_cast = reinterpret_cast<clang::QualType*>(Type);
    return new (location) clang::TemplateArgument(*Other_cast, *Type_cast);
}

void clang_TemplateArgument_get_empty_pack(void* ret_value) {
    clang::TemplateArgument* ret_value_cast = reinterpret_cast<clang::TemplateArgument*>(ret_value);
    new (ret_value_cast) clang::TemplateArgument(clang::TemplateArgument::getEmptyPack());
}

unsigned int clang_TemplateArgument_get_kind(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return (unsigned int)thiz_cast->getKind();
}

bool clang_TemplateArgument_is_null(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isNull();
}

bool clang_TemplateArgument_is_dependent(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isDependent();
}

bool clang_TemplateArgument_is_instantiation_dependent(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isInstantiationDependent();
}

bool clang_TemplateArgument_contains_unexpanded_parameter_pack(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->containsUnexpandedParameterPack();
}

bool clang_TemplateArgument_is_pack_expansion(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isPackExpansion();
}

bool clang_TemplateArgument_is_concept_or_concept_template_parameter(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isConceptOrConceptTemplateParameter();
}

void clang_TemplateArgument_get_as_type(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAsType());
}

const void* clang_TemplateArgument_get_as_decl(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return (void*)thiz_cast->getAsDecl();
}

void clang_TemplateArgument_get_param_type_for_decl(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getParamTypeForDecl());
}

void clang_TemplateArgument_get_null_ptr_type(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getNullPtrType());
}

void clang_TemplateArgument_get_as_integral(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->getAsIntegral());
}

void clang_TemplateArgument_get_integral_type(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getIntegralType());
}

void clang_TemplateArgument_set_integral_type(void* thiz, void* T) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    thiz_cast->setIntegralType(*T_cast);
}

void clang_TemplateArgument_set_is_defaulted(void* thiz, bool v) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    thiz_cast->setIsDefaulted(v);
}

bool clang_TemplateArgument_get_is_defaulted(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->getIsDefaulted();
}

void clang_TemplateArgument_get_structural_value_type(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getStructuralValueType());
}

void clang_TemplateArgument_get_non_type_template_argument_type(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getNonTypeTemplateArgumentType());
}

bool clang_TemplateArgument_is_canonical_expr(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->isCanonicalExpr();
}

unsigned int clang_TemplateArgument_pack_size(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    return thiz_cast->pack_size();
}

bool clang_TemplateArgument_structurally_equals(void* thiz, void* Other) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    const clang::TemplateArgument* Other_cast = reinterpret_cast<const clang::TemplateArgument*>(Other);
    return thiz_cast->structurallyEquals(*Other_cast);
}

void clang_TemplateArgument_get_pack_expansion_pattern(void* thiz, void* ret_value) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    clang::TemplateArgument* ret_value_cast = reinterpret_cast<clang::TemplateArgument*>(ret_value);
    new (ret_value_cast) clang::TemplateArgument(thiz_cast->getPackExpansionPattern());
}

void _clang_TemplateArgument_dump(void* thiz) {
    clang::TemplateArgument* thiz_cast = reinterpret_cast<clang::TemplateArgument*>(thiz);
    thiz_cast->dump();
}

int clang_TemplateArgument_size_of() {
    return sizeof(clang::TemplateArgument);
}

int clang_TemplateArgument_align_of() {
    return alignof(clang::TemplateArgument);
}


// END KRAPPER GEN for clang::TemplateArgument


// BEGIN KRAPPER GEN for clang::TemplateParameterList

unsigned int clang_TemplateParameterList_size(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->size();
}

bool clang_TemplateParameterList_empty(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->empty();
}

void* clang_TemplateParameterList_get_param(void* thiz, unsigned int Idx) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return (void*)thiz_cast->getParam(Idx);
}

unsigned int clang_TemplateParameterList_get_min_required_arguments(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->getMinRequiredArguments();
}

unsigned int clang_TemplateParameterList_get_depth(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->getDepth();
}

bool clang_TemplateParameterList_contains_unexpanded_parameter_pack(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->containsUnexpandedParameterPack();
}

bool clang_TemplateParameterList_has_parameter_pack(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->hasParameterPack();
}

bool clang_TemplateParameterList_has_associated_constraints(void* thiz) {
    clang::TemplateParameterList* thiz_cast = reinterpret_cast<clang::TemplateParameterList*>(thiz);
    return thiz_cast->hasAssociatedConstraints();
}

int clang_TemplateParameterList_size_of() {
    return sizeof(clang::TemplateParameterList);
}

int clang_TemplateParameterList_align_of() {
    return alignof(clang::TemplateParameterList);
}


// END KRAPPER GEN for clang::TemplateParameterList


// BEGIN KRAPPER GEN for clang::DeclaratorDecl

unsigned int clang_DeclaratorDecl_get_num_template_parameter_lists(void* thiz) {
    clang::DeclaratorDecl* thiz_cast = reinterpret_cast<clang::DeclaratorDecl*>(thiz);
    return thiz_cast->getNumTemplateParameterLists();
}

const void* clang_DeclaratorDecl_get_template_parameter_list(void* thiz, unsigned int index) {
    clang::DeclaratorDecl* thiz_cast = reinterpret_cast<clang::DeclaratorDecl*>(thiz);
    return (void*)thiz_cast->getTemplateParameterList(index);
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

void* clang_DeclaratorDecl_dyncast_clang_VarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::VarDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_ParmVarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ParmVarDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}

void* clang_DeclaratorDecl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::DeclaratorDecl*>(p));
}


// END KRAPPER GEN for clang::DeclaratorDecl


// BEGIN KRAPPER GEN for clang::VarDecl

const char* clang_VarDecl_get_storage_class_specifier_string(unsigned int SC) {
    clang::StorageClass SC_cast = (clang::StorageClass)SC;
    return clang::VarDecl::getStorageClassSpecifierString(SC_cast);
}

unsigned int clang_VarDecl_get_storage_class(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getStorageClass();
}

void clang_VarDecl_set_storage_class(void* thiz, unsigned int SC) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::StorageClass SC_cast = (clang::StorageClass)SC;
    thiz_cast->setStorageClass(SC_cast);
}

void clang_VarDecl_set_tsc_spec(void* thiz, unsigned int TSC) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::ThreadStorageClassSpecifier TSC_cast = (clang::ThreadStorageClassSpecifier)TSC;
    thiz_cast->setTSCSpec(TSC_cast);
}

unsigned int clang_VarDecl_get_tsc_spec(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getTSCSpec();
}

unsigned int clang_VarDecl_get_tls_kind(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getTLSKind();
}

bool clang_VarDecl_has_local_storage(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasLocalStorage();
}

bool clang_VarDecl_is_static_local(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isStaticLocal();
}

bool clang_VarDecl_has_external_storage(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasExternalStorage();
}

bool clang_VarDecl_has_global_storage(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasGlobalStorage();
}

unsigned int clang_VarDecl_get_storage_duration(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getStorageDuration();
}

unsigned int clang_VarDecl_get_language_linkage(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getLanguageLinkage();
}

bool clang_VarDecl_is_extern_c(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isExternC();
}

bool clang_VarDecl_is_in_extern_c_context(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isInExternCContext();
}

bool clang_VarDecl_is_in_extern_cxx_context(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isInExternCXXContext();
}

bool clang_VarDecl_is_local_var_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isLocalVarDecl();
}

bool clang_VarDecl_is_local_var_decl_or_parm(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isLocalVarDeclOrParm();
}

bool clang_VarDecl_is_function_or_method_var_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isFunctionOrMethodVarDecl();
}

bool clang_VarDecl_is_static_data_member(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isStaticDataMember();
}

void* clang_VarDecl_get_canonical_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

unsigned int clang_VarDecl_is_this_declaration_a_definition(void* thiz, void* _arg_0) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::ASTContext* _arg_0_cast = reinterpret_cast<clang::ASTContext*>(_arg_0);
    return (unsigned int)thiz_cast->isThisDeclarationADefinition(*_arg_0_cast);
}

unsigned int _clang_VarDecl_is_this_declaration_a_definition(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->isThisDeclarationADefinition();
}

unsigned int clang_VarDecl_has_definition(void* thiz, void* _arg_0) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::ASTContext* _arg_0_cast = reinterpret_cast<clang::ASTContext*>(_arg_0);
    return (unsigned int)thiz_cast->hasDefinition(*_arg_0_cast);
}

unsigned int _clang_VarDecl_has_definition(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->hasDefinition();
}

void* clang_VarDecl_get_acting_definition(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getActingDefinition();
}

void* clang_VarDecl_get_definition(void* thiz, void* C) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::ASTContext* C_cast = reinterpret_cast<clang::ASTContext*>(C);
    return (void*)thiz_cast->getDefinition(*C_cast);
}

void* _clang_VarDecl_get_definition(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

bool clang_VarDecl_is_out_of_line(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isOutOfLine();
}

bool clang_VarDecl_is_file_var_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isFileVarDecl();
}

bool clang_VarDecl_has_init(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasInit();
}

void* clang_VarDecl_get_initializing_declaration(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getInitializingDeclaration();
}

bool clang_VarDecl_has_init_with_side_effects(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasInitWithSideEffects();
}

bool clang_VarDecl_might_be_usable_in_constant_expressions(void* thiz, void* C) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* C_cast = reinterpret_cast<const clang::ASTContext*>(C);
    return thiz_cast->mightBeUsableInConstantExpressions(*C_cast);
}

bool clang_VarDecl_is_usable_in_constant_expressions(void* thiz, void* C) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* C_cast = reinterpret_cast<const clang::ASTContext*>(C);
    return thiz_cast->isUsableInConstantExpressions(*C_cast);
}

bool clang_VarDecl_has_constant_initialization(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasConstantInitialization();
}

bool clang_VarDecl_has_ice_initializer(void* thiz, void* Context) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->hasICEInitializer(*Context_cast);
}

void clang_VarDecl_set_init_style(void* thiz, unsigned int Style) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::VarDecl::InitializationStyle Style_cast = (clang::VarDecl::InitializationStyle)Style;
    thiz_cast->setInitStyle(Style_cast);
}

unsigned int clang_VarDecl_get_init_style(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getInitStyle();
}

bool clang_VarDecl_is_direct_init(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isDirectInit();
}

bool clang_VarDecl_is_this_declaration_a_demoted_definition(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isThisDeclarationADemotedDefinition();
}

void clang_VarDecl_demote_this_definition_to_declaration(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->demoteThisDefinitionToDeclaration();
}

bool clang_VarDecl_is_exception_variable(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isExceptionVariable();
}

void clang_VarDecl_set_exception_variable(void* thiz, bool EV) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setExceptionVariable(EV);
}

bool clang_VarDecl_is_nrvo_variable(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isNRVOVariable();
}

void clang_VarDecl_set_nrvo_variable(void* thiz, bool NRVO) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setNRVOVariable(NRVO);
}

bool clang_VarDecl_is_cxx_for_range_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isCXXForRangeDecl();
}

void clang_VarDecl_set_cxx_for_range_decl(void* thiz, bool FRD) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setCXXForRangeDecl(FRD);
}

bool clang_VarDecl_is_obj_c_for_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isObjCForDecl();
}

void clang_VarDecl_set_obj_c_for_decl(void* thiz, bool FRD) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setObjCForDecl(FRD);
}

bool clang_VarDecl_is_arc_pseudo_strong(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isARCPseudoStrong();
}

void clang_VarDecl_set_arc_pseudo_strong(void* thiz, bool PS) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setARCPseudoStrong(PS);
}

bool clang_VarDecl_is_inline(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isInline();
}

bool clang_VarDecl_is_inline_specified(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isInlineSpecified();
}

void clang_VarDecl_set_inline_specified(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setInlineSpecified();
}

void clang_VarDecl_set_implicitly_inline(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setImplicitlyInline();
}

bool clang_VarDecl_is_constexpr(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isConstexpr();
}

void clang_VarDecl_set_constexpr(void* thiz, bool IC) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setConstexpr(IC);
}

bool clang_VarDecl_is_init_capture(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isInitCapture();
}

void clang_VarDecl_set_init_capture(void* thiz, bool IC) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setInitCapture(IC);
}

bool clang_VarDecl_is_previous_decl_in_same_block_scope(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isPreviousDeclInSameBlockScope();
}

void clang_VarDecl_set_previous_decl_in_same_block_scope(void* thiz, bool Same) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setPreviousDeclInSameBlockScope(Same);
}

bool clang_VarDecl_is_escaping_byref(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isEscapingByref();
}

bool clang_VarDecl_is_non_escaping_byref(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isNonEscapingByref();
}

void clang_VarDecl_set_escaping_byref(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setEscapingByref();
}

bool clang_VarDecl_is_cxx_cond_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isCXXCondDecl();
}

void clang_VarDecl_set_cxx_cond_decl(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setCXXCondDecl();
}

bool clang_VarDecl_is_cxx_for_range_implicit_var(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isCXXForRangeImplicitVar();
}

void clang_VarDecl_set_cxx_for_range_implicit_var(void* thiz, bool FRV) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    thiz_cast->setCXXForRangeImplicitVar(FRV);
}

bool clang_VarDecl_has_dependent_alignment(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->hasDependentAlignment();
}

const void* clang_VarDecl_get_template_instantiation_pattern(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getTemplateInstantiationPattern();
}

const void* clang_VarDecl_get_instantiated_from_static_data_member(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromStaticDataMember();
}

unsigned int clang_VarDecl_get_template_specialization_kind(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKind();
}

unsigned int clang_VarDecl_get_template_specialization_kind_for_instantiation(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKindForInstantiation();
}

void clang_VarDecl_set_template_specialization_kind(void* thiz, unsigned int TSK) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setTemplateSpecializationKind(TSK_cast);
}

void clang_VarDecl_set_instantiation_of_static_data_member(void* thiz, void* VD, unsigned int TSK) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    clang::VarDecl* VD_cast = reinterpret_cast<clang::VarDecl*>(VD);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setInstantiationOfStaticDataMember(VD_cast, TSK_cast);
}

bool clang_VarDecl_is_known_to_be_defined(void* thiz) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    return thiz_cast->isKnownToBeDefined();
}

bool clang_VarDecl_is_no_destroy(void* thiz, void* _arg_0) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* _arg_0_cast = reinterpret_cast<const clang::ASTContext*>(_arg_0);
    return thiz_cast->isNoDestroy(*_arg_0_cast);
}

unsigned int clang_VarDecl_needs_destruction(void* thiz, void* Ctx) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return (unsigned int)thiz_cast->needsDestruction(*Ctx_cast);
}

bool clang_VarDecl_has_flexible_array_init(void* thiz, void* Ctx) {
    clang::VarDecl* thiz_cast = reinterpret_cast<clang::VarDecl*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->hasFlexibleArrayInit(*Ctx_cast);
}

bool clang_VarDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::VarDecl::classof(D_cast);
}

bool clang_VarDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::VarDecl::classofKind(K_cast);
}

int clang_VarDecl_size_of() {
    return sizeof(clang::VarDecl);
}

int clang_VarDecl_align_of() {
    return alignof(clang::VarDecl);
}

void* clang_VarDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::VarDecl*>(p));
}

void* clang_VarDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::VarDecl*>(p));
}

void* clang_VarDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::VarDecl*>(p));
}

void* clang_VarDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::VarDecl*>(p));
}

void* clang_VarDecl_dyncast_clang_ParmVarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ParmVarDecl>(reinterpret_cast<clang::VarDecl*>(p));
}


// END KRAPPER GEN for clang::VarDecl


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

void* clang_ValueDecl_get_potentially_decomposed_var_decl(void* thiz) {
    clang::ValueDecl* thiz_cast = reinterpret_cast<clang::ValueDecl*>(thiz);
    return (void*)thiz_cast->getPotentiallyDecomposedVarDecl();
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

void* clang_ValueDecl_dyncast_clang_VarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::VarDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_EnumConstantDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumConstantDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_ParmVarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ParmVarDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}

void* clang_ValueDecl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::ValueDecl*>(p));
}


// END KRAPPER GEN for clang::ValueDecl


// BEGIN KRAPPER GEN for clang::EnumConstantDecl

void clang_EnumConstantDecl_get_init_val(void* thiz, void* ret_value) {
    clang::EnumConstantDecl* thiz_cast = reinterpret_cast<clang::EnumConstantDecl*>(thiz);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->getInitVal());
}

void clang_EnumConstantDecl_set_init_val(void* thiz, void* C, void* V) {
    clang::EnumConstantDecl* thiz_cast = reinterpret_cast<clang::EnumConstantDecl*>(thiz);
    const clang::ASTContext* C_cast = reinterpret_cast<const clang::ASTContext*>(C);
    const llvm::APSInt* V_cast = reinterpret_cast<const llvm::APSInt*>(V);
    thiz_cast->setInitVal(*C_cast, *V_cast);
}

void* clang_EnumConstantDecl_get_canonical_decl(void* thiz) {
    clang::EnumConstantDecl* thiz_cast = reinterpret_cast<clang::EnumConstantDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_EnumConstantDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::EnumConstantDecl::classof(D_cast);
}

bool clang_EnumConstantDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::EnumConstantDecl::classofKind(K_cast);
}

int clang_EnumConstantDecl_size_of() {
    return sizeof(clang::EnumConstantDecl);
}

int clang_EnumConstantDecl_align_of() {
    return alignof(clang::EnumConstantDecl);
}

void* clang_EnumConstantDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::EnumConstantDecl*>(p));
}

void* clang_EnumConstantDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::EnumConstantDecl*>(p));
}

void* clang_EnumConstantDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::EnumConstantDecl*>(p));
}


// END KRAPPER GEN for clang::EnumConstantDecl


// BEGIN KRAPPER GEN for clang::EnumDecl

void clang_EnumDecl_set_scoped(void* thiz, bool Scoped) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    thiz_cast->setScoped(Scoped);
}

void clang_EnumDecl_set_scoped_using_class_tag(void* thiz, bool ScopedUCT) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    thiz_cast->setScopedUsingClassTag(ScopedUCT);
}

void clang_EnumDecl_set_fixed(void* thiz, bool Fixed) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    thiz_cast->setFixed(Fixed);
}

void* clang_EnumDecl_get_canonical_decl(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

void* clang_EnumDecl_get_previous_decl(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getPreviousDecl();
}

void* clang_EnumDecl_get_most_recent_decl(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

const void* clang_EnumDecl_get_definition(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getDefinition();
}

const void* clang_EnumDecl_get_definition_or_self(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getDefinitionOrSelf();
}

void clang_EnumDecl_complete_definition(void* thiz, void* NewType, void* PromotionType, unsigned int NumPositiveBits, unsigned int NumNegativeBits) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::QualType* NewType_cast = reinterpret_cast<clang::QualType*>(NewType);
    clang::QualType* PromotionType_cast = reinterpret_cast<clang::QualType*>(PromotionType);
    thiz_cast->completeDefinition(*NewType_cast, *PromotionType_cast, NumPositiveBits, NumNegativeBits);
}

void clang_EnumDecl_get_promotion_type(void* thiz, void* ret_value) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getPromotionType());
}

void clang_EnumDecl_set_promotion_type(void* thiz, void* T) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    thiz_cast->setPromotionType(*T_cast);
}

void clang_EnumDecl_get_integer_type(void* thiz, void* ret_value) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getIntegerType());
}

void clang_EnumDecl_set_integer_type(void* thiz, void* T) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    thiz_cast->setIntegerType(*T_cast);
}

unsigned int clang_EnumDecl_get_num_positive_bits(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->getNumPositiveBits();
}

unsigned int clang_EnumDecl_get_num_negative_bits(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->getNumNegativeBits();
}

bool clang_EnumDecl_is_scoped(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isScoped();
}

bool clang_EnumDecl_is_scoped_using_class_tag(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isScopedUsingClassTag();
}

bool clang_EnumDecl_is_fixed(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isFixed();
}

unsigned int clang_EnumDecl_get_odr_hash(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->getODRHash();
}

bool clang_EnumDecl_is_complete(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isComplete();
}

bool clang_EnumDecl_is_closed(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isClosed();
}

bool clang_EnumDecl_is_closed_flag(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isClosedFlag();
}

bool clang_EnumDecl_is_closed_non_flag(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return thiz_cast->isClosedNonFlag();
}

const void* clang_EnumDecl_get_template_instantiation_pattern(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getTemplateInstantiationPattern();
}

const void* clang_EnumDecl_get_instantiated_from_member_enum(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromMemberEnum();
}

unsigned int clang_EnumDecl_get_template_specialization_kind(void* thiz) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    return (unsigned int)thiz_cast->getTemplateSpecializationKind();
}

void clang_EnumDecl_set_template_specialization_kind(void* thiz, unsigned int TSK) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setTemplateSpecializationKind(TSK_cast);
}

void clang_EnumDecl_set_instantiation_of_member_enum(void* thiz, void* ED, unsigned int TSK) {
    clang::EnumDecl* thiz_cast = reinterpret_cast<clang::EnumDecl*>(thiz);
    clang::EnumDecl* ED_cast = reinterpret_cast<clang::EnumDecl*>(ED);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setInstantiationOfMemberEnum(ED_cast, TSK_cast);
}

bool clang_EnumDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::EnumDecl::classof(D_cast);
}

bool clang_EnumDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::EnumDecl::classofKind(K_cast);
}

int clang_EnumDecl_size_of() {
    return sizeof(clang::EnumDecl);
}

int clang_EnumDecl_align_of() {
    return alignof(clang::EnumDecl);
}

void* clang_EnumDecl_as_clang_TagDecl(void* p) {
    return static_cast<clang::TagDecl*>(reinterpret_cast<clang::EnumDecl*>(p));
}

void* clang_EnumDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::EnumDecl*>(p));
}

void* clang_EnumDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::EnumDecl*>(p));
}

void* clang_EnumDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::EnumDecl*>(p));
}

void* clang_EnumDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::EnumDecl*>(p));
}


// END KRAPPER GEN for clang::EnumDecl


// BEGIN KRAPPER GEN for clang::ArrayType

void clang_ArrayType_get_element_type(void* thiz, void* ret_value) {
    clang::ArrayType* thiz_cast = reinterpret_cast<clang::ArrayType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getElementType());
}

int clang_ArrayType_get_size_modifier(void* thiz) {
    clang::ArrayType* thiz_cast = reinterpret_cast<clang::ArrayType*>(thiz);
    return (int)thiz_cast->getSizeModifier();
}

unsigned int clang_ArrayType_get_index_type_cvr_qualifiers(void* thiz) {
    clang::ArrayType* thiz_cast = reinterpret_cast<clang::ArrayType*>(thiz);
    return thiz_cast->getIndexTypeCVRQualifiers();
}

bool clang_ArrayType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::ArrayType::classof(T_cast);
}

int clang_ArrayType_size_of() {
    return sizeof(clang::ArrayType);
}

int clang_ArrayType_align_of() {
    return alignof(clang::ArrayType);
}

void* clang_ArrayType_as_clang_Type(void* p) {
    return static_cast<clang::Type*>(reinterpret_cast<clang::ArrayType*>(p));
}

void* clang_ArrayType_dyncast_clang_ConstantArrayType(void* p) {
    return llvm::dyn_cast_or_null<clang::ConstantArrayType>(reinterpret_cast<clang::ArrayType*>(p));
}


// END KRAPPER GEN for clang::ArrayType


// BEGIN KRAPPER GEN for clang::Type

unsigned int clang_Type_get_type_class(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (unsigned int)thiz_cast->getTypeClass();
}

bool clang_Type_is_from_ast(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFromAST();
}

bool clang_Type_contains_unexpanded_parameter_pack(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->containsUnexpandedParameterPack();
}

bool clang_Type_is_canonical_unqualified(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCanonicalUnqualified();
}

void clang_Type_get_locally_unqualified_single_step_desugared_type(void* thiz, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getLocallyUnqualifiedSingleStepDesugaredType());
}

bool clang_Type_is_sizeless_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSizelessType();
}

bool clang_Type_is_sizeless_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSizelessBuiltinType();
}

bool clang_Type_is_sizeless_vector_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSizelessVectorType();
}

bool clang_Type_is_sve_sizeless_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSVESizelessBuiltinType();
}

bool clang_Type_is_rvv_sizeless_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRVVSizelessBuiltinType();
}

bool clang_Type_is_web_assembly_externref_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isWebAssemblyExternrefType();
}

bool clang_Type_is_web_assembly_table_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isWebAssemblyTableType();
}

bool clang_Type_is_sve_vls_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSveVLSBuiltinType();
}

void clang_Type_get_sve_elt_type(void* thiz, void* Ctx, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSveEltType(*Ctx_cast));
}

bool clang_Type_is_rvvvls_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRVVVLSBuiltinType();
}

void clang_Type_get_rvv_elt_type(void* thiz, void* Ctx, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getRVVEltType(*Ctx_cast));
}

void clang_Type_get_sizeless_vector_elt_type(void* thiz, void* Ctx, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSizelessVectorEltType(*Ctx_cast));
}

bool clang_Type_is_incomplete_type(void* thiz, void* Def) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::NamedDecl** Def_cast = reinterpret_cast<clang::NamedDecl**>(Def);
    return thiz_cast->isIncompleteType(Def_cast);
}

bool clang_Type_is_incomplete_or_object_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIncompleteOrObjectType();
}

bool clang_Type_is_always_incomplete_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAlwaysIncompleteType();
}

bool clang_Type_is_object_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjectType();
}

bool clang_Type_is_literal_type(void* thiz, void* Ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isLiteralType(*Ctx_cast);
}

bool clang_Type_is_structural_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStructuralType();
}

bool clang_Type_is_standard_layout_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStandardLayoutType();
}

bool clang_Type_is_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isBuiltinType();
}

bool clang_Type_is_specific_builtin_type(void* thiz, unsigned int K) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSpecificBuiltinType(K);
}

bool clang_Type_is_placeholder_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isPlaceholderType();
}

bool clang_Type_is_specific_placeholder_type(void* thiz, unsigned int K) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSpecificPlaceholderType(K);
}

bool clang_Type_is_non_overload_placeholder_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isNonOverloadPlaceholderType();
}

bool clang_Type_is_integer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIntegerType();
}

bool clang_Type_is_enumeral_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isEnumeralType();
}

bool clang_Type_is_scoped_enumeral_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isScopedEnumeralType();
}

bool clang_Type_is_boolean_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isBooleanType();
}

bool clang_Type_is_char_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCharType();
}

bool clang_Type_is_wide_char_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isWideCharType();
}

bool clang_Type_is_char8type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isChar8Type();
}

bool clang_Type_is_char16type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isChar16Type();
}

bool clang_Type_is_char32type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isChar32Type();
}

bool clang_Type_is_any_character_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAnyCharacterType();
}

bool clang_Type_is_unicode_character_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnicodeCharacterType();
}

bool clang_Type_is_integral_type(void* thiz, void* Ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isIntegralType(*Ctx_cast);
}

bool clang_Type_is_integral_or_enumeration_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIntegralOrEnumerationType();
}

bool clang_Type_is_integral_or_unscoped_enumeration_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIntegralOrUnscopedEnumerationType();
}

bool clang_Type_is_unscoped_enumeration_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnscopedEnumerationType();
}

bool clang_Type_is_real_floating_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRealFloatingType();
}

bool clang_Type_is_complex_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isComplexType();
}

bool clang_Type_is_any_complex_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAnyComplexType();
}

bool clang_Type_is_floating_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFloatingType();
}

bool clang_Type_is_half_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHalfType();
}

bool clang_Type_is_float16type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFloat16Type();
}

bool clang_Type_is_float32type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFloat32Type();
}

bool clang_Type_is_double_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isDoubleType();
}

bool clang_Type_is_b_float16type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isBFloat16Type();
}

bool clang_Type_is_m_float8type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isMFloat8Type();
}

bool clang_Type_is_float128type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFloat128Type();
}

bool clang_Type_is_ibm128type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIbm128Type();
}

bool clang_Type_is_real_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRealType();
}

bool clang_Type_is_arithmetic_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isArithmeticType();
}

bool clang_Type_is_void_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVoidType();
}

bool clang_Type_is_scalar_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isScalarType();
}

bool clang_Type_is_aggregate_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAggregateType();
}

bool clang_Type_is_fundamental_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFundamentalType();
}

bool clang_Type_is_compound_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCompoundType();
}

bool clang_Type_is_function_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFunctionType();
}

bool clang_Type_is_function_no_proto_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFunctionNoProtoType();
}

bool clang_Type_is_function_proto_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFunctionProtoType();
}

bool clang_Type_is_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isPointerType();
}

bool clang_Type_is_pointer_or_reference_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isPointerOrReferenceType();
}

bool clang_Type_is_signable_type(void* thiz, void* Ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isSignableType(*Ctx_cast);
}

bool clang_Type_is_signable_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSignablePointerType();
}

bool clang_Type_is_signable_integer_type(void* thiz, void* Ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* Ctx_cast = reinterpret_cast<const clang::ASTContext*>(Ctx);
    return thiz_cast->isSignableIntegerType(*Ctx_cast);
}

bool clang_Type_is_any_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAnyPointerType();
}

bool clang_Type_is_count_attributed_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCountAttributedType();
}

bool clang_Type_is_cfi_unchecked_callee_function_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCFIUncheckedCalleeFunctionType();
}

bool clang_Type_has_pointee_to_cfi_unchecked_callee_function_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasPointeeToCFIUncheckedCalleeFunctionType();
}

bool clang_Type_is_block_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isBlockPointerType();
}

bool clang_Type_is_void_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVoidPointerType();
}

bool clang_Type_is_reference_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isReferenceType();
}

bool clang_Type_is_l_value_reference_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isLValueReferenceType();
}

bool clang_Type_is_r_value_reference_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRValueReferenceType();
}

bool clang_Type_is_object_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjectPointerType();
}

bool clang_Type_is_function_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFunctionPointerType();
}

bool clang_Type_is_function_reference_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFunctionReferenceType();
}

bool clang_Type_is_member_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isMemberPointerType();
}

bool clang_Type_is_member_function_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isMemberFunctionPointerType();
}

bool clang_Type_is_member_data_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isMemberDataPointerType();
}

bool clang_Type_is_array_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isArrayType();
}

bool clang_Type_is_constant_array_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isConstantArrayType();
}

bool clang_Type_is_incomplete_array_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isIncompleteArrayType();
}

bool clang_Type_is_variable_array_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVariableArrayType();
}

bool clang_Type_is_array_parameter_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isArrayParameterType();
}

bool clang_Type_is_dependent_sized_array_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isDependentSizedArrayType();
}

bool clang_Type_is_record_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isRecordType();
}

bool clang_Type_is_class_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isClassType();
}

bool clang_Type_is_structure_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStructureType();
}

bool clang_Type_is_structure_type_with_flexible_array_member(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStructureTypeWithFlexibleArrayMember();
}

bool clang_Type_is_obj_c_boxable_record_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCBoxableRecordType();
}

bool clang_Type_is_interface_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isInterfaceType();
}

bool clang_Type_is_structure_or_class_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStructureOrClassType();
}

bool clang_Type_is_union_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnionType();
}

bool clang_Type_is_complex_integer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isComplexIntegerType();
}

bool clang_Type_is_vector_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVectorType();
}

bool clang_Type_is_ext_vector_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isExtVectorType();
}

bool clang_Type_is_ext_vector_bool_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isExtVectorBoolType();
}

bool clang_Type_is_constant_matrix_bool_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isConstantMatrixBoolType();
}

bool clang_Type_is_packed_vector_bool_type(void* thiz, void* ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    const clang::ASTContext* ctx_cast = reinterpret_cast<const clang::ASTContext*>(ctx);
    return thiz_cast->isPackedVectorBoolType(*ctx_cast);
}

bool clang_Type_is_subscriptable_vector_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSubscriptableVectorType();
}

bool clang_Type_is_matrix_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isMatrixType();
}

bool clang_Type_is_constant_matrix_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isConstantMatrixType();
}

bool clang_Type_is_dependent_address_space_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isDependentAddressSpaceType();
}

bool clang_Type_is_obj_c_object_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCObjectPointerType();
}

bool clang_Type_is_obj_c_retainable_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCRetainableType();
}

bool clang_Type_is_obj_c_lifetime_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCLifetimeType();
}

bool clang_Type_is_obj_c_indirect_lifetime_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCIndirectLifetimeType();
}

bool clang_Type_is_obj_cns_object_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCNSObjectType();
}

bool clang_Type_is_obj_c_independent_class_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCIndependentClassType();
}

bool clang_Type_is_obj_c_object_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCObjectType();
}

bool clang_Type_is_obj_c_qualified_interface_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCQualifiedInterfaceType();
}

bool clang_Type_is_obj_c_qualified_id_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCQualifiedIdType();
}

bool clang_Type_is_obj_c_qualified_class_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCQualifiedClassType();
}

bool clang_Type_is_obj_c_object_or_interface_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCObjectOrInterfaceType();
}

bool clang_Type_is_obj_c_id_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCIdType();
}

bool clang_Type_is_decltype_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isDecltypeType();
}

bool clang_Type_is_obj_c_inert_unsafe_unretained_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCInertUnsafeUnretainedType();
}

bool clang_Type_is_obj_c_class_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCClassType();
}

bool clang_Type_is_obj_c_class_or_class_kind_of_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCClassOrClassKindOfType();
}

bool clang_Type_is_block_compatible_obj_c_pointer_type(void* thiz, void* ctx) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::ASTContext* ctx_cast = reinterpret_cast<clang::ASTContext*>(ctx);
    return thiz_cast->isBlockCompatibleObjCPointerType(*ctx_cast);
}

bool clang_Type_is_obj_c_sel_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCSelType();
}

bool clang_Type_is_obj_c_builtin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCBuiltinType();
}

bool clang_Type_is_obj_carc_bridgable_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCARCBridgableType();
}

bool clang_Type_is_carc_bridgable_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCARCBridgableType();
}

bool clang_Type_is_template_type_parm_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isTemplateTypeParmType();
}

bool clang_Type_is_null_ptr_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isNullPtrType();
}

bool clang_Type_is_nothrow_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isNothrowT();
}

bool clang_Type_is_align_val_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAlignValT();
}

bool clang_Type_is_std_byte_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isStdByteType();
}

bool clang_Type_is_atomic_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isAtomicType();
}

bool clang_Type_is_undeduced_auto_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUndeducedAutoType();
}

bool clang_Type_is_typedef_name_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isTypedefNameType();
}

bool clang_Type_is_ocl_image1d_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dROType();
}

bool clang_Type_is_ocl_image1d_array_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dArrayROType();
}

bool clang_Type_is_ocl_image1d_buffer_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dBufferROType();
}

bool clang_Type_is_ocl_image2d_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dROType();
}

bool clang_Type_is_ocl_image2d_array_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayROType();
}

bool clang_Type_is_ocl_image2d_depth_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dDepthROType();
}

bool clang_Type_is_ocl_image2d_array_depth_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayDepthROType();
}

bool clang_Type_is_ocl_image2d_msaaro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAAROType();
}

bool clang_Type_is_ocl_image2d_array_msaaro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAAROType();
}

bool clang_Type_is_ocl_image2d_msaa_depth_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAADepthROType();
}

bool clang_Type_is_ocl_image2d_array_msaa_depth_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAADepthROType();
}

bool clang_Type_is_ocl_image3d_ro_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage3dROType();
}

bool clang_Type_is_ocl_image1d_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dWOType();
}

bool clang_Type_is_ocl_image1d_array_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dArrayWOType();
}

bool clang_Type_is_ocl_image1d_buffer_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dBufferWOType();
}

bool clang_Type_is_ocl_image2d_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dWOType();
}

bool clang_Type_is_ocl_image2d_array_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayWOType();
}

bool clang_Type_is_ocl_image2d_depth_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dDepthWOType();
}

bool clang_Type_is_ocl_image2d_array_depth_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayDepthWOType();
}

bool clang_Type_is_ocl_image2d_msaawo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAAWOType();
}

bool clang_Type_is_ocl_image2d_array_msaawo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAAWOType();
}

bool clang_Type_is_ocl_image2d_msaa_depth_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAADepthWOType();
}

bool clang_Type_is_ocl_image2d_array_msaa_depth_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAADepthWOType();
}

bool clang_Type_is_ocl_image3d_wo_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage3dWOType();
}

bool clang_Type_is_ocl_image1d_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dRWType();
}

bool clang_Type_is_ocl_image1d_array_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dArrayRWType();
}

bool clang_Type_is_ocl_image1d_buffer_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage1dBufferRWType();
}

bool clang_Type_is_ocl_image2d_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dRWType();
}

bool clang_Type_is_ocl_image2d_array_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayRWType();
}

bool clang_Type_is_ocl_image2d_depth_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dDepthRWType();
}

bool clang_Type_is_ocl_image2d_array_depth_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayDepthRWType();
}

bool clang_Type_is_ocl_image2d_msaarw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAARWType();
}

bool clang_Type_is_ocl_image2d_array_msaarw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAARWType();
}

bool clang_Type_is_ocl_image2d_msaa_depth_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dMSAADepthRWType();
}

bool clang_Type_is_ocl_image2d_array_msaa_depth_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage2dArrayMSAADepthRWType();
}

bool clang_Type_is_ocl_image3d_rw_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLImage3dRWType();
}

bool clang_Type_is_image_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isImageType();
}

bool clang_Type_is_sampler_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSamplerT();
}

bool clang_Type_is_event_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isEventT();
}

bool clang_Type_is_clk_event_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isClkEventT();
}

bool clang_Type_is_queue_t(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isQueueT();
}

bool clang_Type_is_reserve_idt(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isReserveIDT();
}

bool clang_Type_is_ocl_intel_subgroup_avc_mce_payload_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCMcePayloadType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_payload_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImePayloadType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ref_payload_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCRefPayloadType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_sic_payload_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCSicPayloadType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_mce_result_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCMceResultType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_result_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImeResultType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ref_result_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCRefResultType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_sic_result_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCSicResultType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_result_single_reference_streamout_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImeResultSingleReferenceStreamoutType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_result_dual_reference_streamout_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImeResultDualReferenceStreamoutType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_single_reference_streamin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImeSingleReferenceStreaminType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_ime_dual_reference_streamin_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCImeDualReferenceStreaminType();
}

bool clang_Type_is_ocl_intel_subgroup_avc_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLIntelSubgroupAVCType();
}

bool clang_Type_is_ocl_ext_opaque_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOCLExtOpaqueType();
}

bool clang_Type_is_pipe_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isPipeType();
}

bool clang_Type_is_bit_int_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isBitIntType();
}

bool clang_Type_is_open_cl_specific_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOpenCLSpecificType();
}

bool clang_Type_is_hlsl_resource_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLResourceType();
}

bool clang_Type_is_hlsl_specific_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLSpecificType();
}

bool clang_Type_is_hlsl_builtin_intangible_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLBuiltinIntangibleType();
}

bool clang_Type_is_hlsl_attributed_resource_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLAttributedResourceType();
}

bool clang_Type_is_hlsl_inline_spirv_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLInlineSpirvType();
}

bool clang_Type_is_hlsl_resource_record(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLResourceRecord();
}

bool clang_Type_is_hlsl_resource_record_array(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLResourceRecordArray();
}

bool clang_Type_is_hlsl_intangible_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isHLSLIntangibleType();
}

bool clang_Type_is_obj_carc_implicitly_unretained_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isObjCARCImplicitlyUnretainedType();
}

bool clang_Type_is_cuda_device_builtin_surface_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCUDADeviceBuiltinSurfaceType();
}

bool clang_Type_is_cuda_device_builtin_texture_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isCUDADeviceBuiltinTextureType();
}

unsigned int clang_Type_get_obj_carc_implicit_lifetime(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (unsigned int)thiz_cast->getObjCARCImplicitLifetime();
}

unsigned int clang_Type_get_scalar_type_kind(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (unsigned int)thiz_cast->getScalarTypeKind();
}

bool clang_Type_contains_errors(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->containsErrors();
}

bool clang_Type_is_dependent_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isDependentType();
}

bool clang_Type_is_instantiation_dependent_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isInstantiationDependentType();
}

bool clang_Type_is_undeduced_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUndeducedType();
}

bool clang_Type_is_variably_modified_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVariablyModifiedType();
}

bool clang_Type_has_sized_vla_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasSizedVLAType();
}

bool clang_Type_has_unnamed_or_local_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasUnnamedOrLocalType();
}

bool clang_Type_is_overloadable_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isOverloadableType();
}

bool clang_Type_is_elaborated_type_specifier(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isElaboratedTypeSpecifier();
}

bool clang_Type_can_decay_to_pointer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->canDecayToPointerType();
}

bool clang_Type_has_pointer_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasPointerRepresentation();
}

bool clang_Type_has_obj_c_pointer_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasObjCPointerRepresentation();
}

bool clang_Type_has_integer_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasIntegerRepresentation();
}

bool clang_Type_has_signed_integer_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasSignedIntegerRepresentation();
}

bool clang_Type_has_unsigned_integer_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasUnsignedIntegerRepresentation();
}

bool clang_Type_has_floating_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasFloatingRepresentation();
}

bool clang_Type_has_boolean_representation(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasBooleanRepresentation();
}

const void* clang_Type_get_as_cxx_record_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getAsCXXRecordDecl();
}

const void* clang_Type_cast_as_cxx_record_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->castAsCXXRecordDecl();
}

const void* clang_Type_get_as_record_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getAsRecordDecl();
}

const void* clang_Type_cast_as_record_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->castAsRecordDecl();
}

const void* clang_Type_get_as_enum_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getAsEnumDecl();
}

const void* clang_Type_cast_as_enum_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->castAsEnumDecl();
}

const void* clang_Type_get_as_tag_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getAsTagDecl();
}

const void* clang_Type_cast_as_tag_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->castAsTagDecl();
}

void* clang_Type_get_pointee_cxx_record_decl(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getPointeeCXXRecordDecl();
}

bool clang_Type_has_auto_for_trailing_return_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->hasAutoForTrailingReturnType();
}

void* clang_Type_get_as_array_type_unsafe(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getAsArrayTypeUnsafe();
}

void* clang_Type_cast_as_array_type_unsafe(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->castAsArrayTypeUnsafe();
}

bool clang_Type_has_attr(void* thiz, unsigned int AK) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::attr::Kind AK_cast = (clang::attr::Kind)AK;
    return thiz_cast->hasAttr(AK_cast);
}

void* clang_Type_get_base_element_type_unsafe(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getBaseElementTypeUnsafe();
}

void* clang_Type_get_array_element_type_no_type_qual(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getArrayElementTypeNoTypeQual();
}

void* clang_Type_get_pointee_or_array_element_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getPointeeOrArrayElementType();
}

void clang_Type_get_pointee_type(void* thiz, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getPointeeType());
}

void* clang_Type_get_unqualified_desugared_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (void*)thiz_cast->getUnqualifiedDesugaredType();
}

bool clang_Type_is_signed_integer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSignedIntegerType();
}

bool clang_Type_is_unsigned_integer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnsignedIntegerType();
}

bool clang_Type_is_signed_integer_or_enumeration_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSignedIntegerOrEnumerationType();
}

bool clang_Type_is_unsigned_integer_or_enumeration_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnsignedIntegerOrEnumerationType();
}

bool clang_Type_is_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFixedPointType();
}

bool clang_Type_is_fixed_point_or_integer_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isFixedPointOrIntegerType();
}

bool clang_Type_is_convertible_to_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isConvertibleToFixedPointType();
}

bool clang_Type_is_saturated_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSaturatedFixedPointType();
}

bool clang_Type_is_unsaturated_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnsaturatedFixedPointType();
}

bool clang_Type_is_signed_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSignedFixedPointType();
}

bool clang_Type_is_unsigned_fixed_point_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isUnsignedFixedPointType();
}

bool clang_Type_is_constant_size_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isConstantSizeType();
}

bool clang_Type_is_specifier_type(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isSpecifierType();
}

unsigned char clang_Type_get_linkage(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (unsigned char)thiz_cast->getLinkage();
}

unsigned int clang_Type_get_visibility(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return (unsigned int)thiz_cast->getVisibility();
}

bool clang_Type_is_visibility_explicit(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isVisibilityExplicit();
}

bool clang_Type_is_linkage_valid(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->isLinkageValid();
}

bool clang_Type_can_have_nullability(void* thiz, bool ResultIfUnknown) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->canHaveNullability(ResultIfUnknown);
}

bool clang_Type_accepts_obj_c_type_params(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->acceptsObjCTypeParams();
}

const char* clang_Type_get_type_class_name(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    return thiz_cast->getTypeClassName();
}

void clang_Type_get_canonical_type_internal(void* thiz, void* ret_value) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCanonicalTypeInternal());
}

void clang_Type_dump(void* thiz) {
    clang::Type* thiz_cast = reinterpret_cast<clang::Type*>(thiz);
    thiz_cast->dump();
}

int clang_Type_size_of() {
    return sizeof(clang::Type);
}

int clang_Type_align_of() {
    return alignof(clang::Type);
}

void* clang_Type_dyncast_clang_ArrayType(void* p) {
    return llvm::dyn_cast_or_null<clang::ArrayType>(reinterpret_cast<clang::Type*>(p));
}

void* clang_Type_dyncast_clang_FunctionType(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionType>(reinterpret_cast<clang::Type*>(p));
}

void* clang_Type_dyncast_clang_FunctionProtoType(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionProtoType>(reinterpret_cast<clang::Type*>(p));
}

void* clang_Type_dyncast_clang_ConstantArrayType(void* p) {
    return llvm::dyn_cast_or_null<clang::ConstantArrayType>(reinterpret_cast<clang::Type*>(p));
}

void* clang_Type_dyncast_clang_TemplateTypeParmType(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateTypeParmType>(reinterpret_cast<clang::Type*>(p));
}


// END KRAPPER GEN for clang::Type


// BEGIN KRAPPER GEN for clang::TypeDecl

void* clang_TypeDecl_get_type_for_decl(void* thiz) {
    clang::TypeDecl* thiz_cast = reinterpret_cast<clang::TypeDecl*>(thiz);
    return (void*)thiz_cast->getTypeForDecl();
}

void clang_TypeDecl_set_type_for_decl(void* thiz, void* TD) {
    clang::TypeDecl* thiz_cast = reinterpret_cast<clang::TypeDecl*>(thiz);
    const clang::Type* TD_cast = reinterpret_cast<const clang::Type*>(TD);
    thiz_cast->setTypeForDecl(TD_cast);
}

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

void* clang_TypeDecl_dyncast_clang_EnumDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_TypedefNameDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypedefNameDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}

void* clang_TypeDecl_dyncast_clang_TemplateTypeParmDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateTypeParmDecl>(reinterpret_cast<clang::TypeDecl*>(p));
}


// END KRAPPER GEN for clang::TypeDecl


// BEGIN KRAPPER GEN for clang::TypedefNameDecl

bool clang_TypedefNameDecl_is_moded(void* thiz) {
    clang::TypedefNameDecl* thiz_cast = reinterpret_cast<clang::TypedefNameDecl*>(thiz);
    return thiz_cast->isModed();
}

void clang_TypedefNameDecl_get_underlying_type(void* thiz, void* ret_value) {
    clang::TypedefNameDecl* thiz_cast = reinterpret_cast<clang::TypedefNameDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getUnderlyingType());
}

void* clang_TypedefNameDecl_get_canonical_decl(void* thiz) {
    clang::TypedefNameDecl* thiz_cast = reinterpret_cast<clang::TypedefNameDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

const void* clang_TypedefNameDecl_get_anon_decl_with_typedef_name(void* thiz, bool AnyRedecl) {
    clang::TypedefNameDecl* thiz_cast = reinterpret_cast<clang::TypedefNameDecl*>(thiz);
    return (void*)thiz_cast->getAnonDeclWithTypedefName(AnyRedecl);
}

bool clang_TypedefNameDecl_is_transparent_tag(void* thiz) {
    clang::TypedefNameDecl* thiz_cast = reinterpret_cast<clang::TypedefNameDecl*>(thiz);
    return thiz_cast->isTransparentTag();
}

bool clang_TypedefNameDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TypedefNameDecl::classof(D_cast);
}

bool clang_TypedefNameDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TypedefNameDecl::classofKind(K_cast);
}

int clang_TypedefNameDecl_size_of() {
    return sizeof(clang::TypedefNameDecl);
}

int clang_TypedefNameDecl_align_of() {
    return alignof(clang::TypedefNameDecl);
}

void* clang_TypedefNameDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::TypedefNameDecl*>(p));
}

void* clang_TypedefNameDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::TypedefNameDecl*>(p));
}

void* clang_TypedefNameDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TypedefNameDecl*>(p));
}


// END KRAPPER GEN for clang::TypedefNameDecl


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

const void* clang_TagDecl_get_typedef_name_for_anon_decl(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (void*)thiz_cast->getTypedefNameForAnonDecl();
}

void clang_TagDecl_set_typedef_name_for_anon_decl(void* thiz, void* TDD) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    clang::TypedefNameDecl* TDD_cast = reinterpret_cast<clang::TypedefNameDecl*>(TDD);
    thiz_cast->setTypedefNameForAnonDecl(TDD_cast);
}

unsigned int clang_TagDecl_get_num_template_parameter_lists(void* thiz) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return thiz_cast->getNumTemplateParameterLists();
}

const void* clang_TagDecl_get_template_parameter_list(void* thiz, unsigned int i) {
    clang::TagDecl* thiz_cast = reinterpret_cast<clang::TagDecl*>(thiz);
    return (void*)thiz_cast->getTemplateParameterList(i);
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

void* clang_TagDecl_dyncast_clang_EnumDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumDecl>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::TagDecl*>(p));
}

void* clang_TagDecl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::TagDecl*>(p));
}


// END KRAPPER GEN for clang::TagDecl


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

void* clang_RecordDecl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::RecordDecl*>(p));
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


// BEGIN KRAPPER GEN for clang::ParmVarDecl

void clang_ParmVarDecl_set_obj_c_method_scope_info(void* thiz, unsigned int parameterIndex) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    thiz_cast->setObjCMethodScopeInfo(parameterIndex);
}

void clang_ParmVarDecl_set_scope_info(void* thiz, unsigned int scopeDepth, unsigned int parameterIndex) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    thiz_cast->setScopeInfo(scopeDepth, parameterIndex);
}

bool clang_ParmVarDecl_is_obj_c_method_parameter(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->isObjCMethodParameter();
}

bool clang_ParmVarDecl_is_destroyed_in_callee(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->isDestroyedInCallee();
}

unsigned int clang_ParmVarDecl_get_function_scope_depth(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->getFunctionScopeDepth();
}

unsigned int clang_ParmVarDecl_get_max_function_scope_depth() {
    return clang::ParmVarDecl::getMaxFunctionScopeDepth();
}

unsigned int clang_ParmVarDecl_get_function_scope_index(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->getFunctionScopeIndex();
}

unsigned int clang_ParmVarDecl_get_obj_c_decl_qualifier(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return (unsigned int)thiz_cast->getObjCDeclQualifier();
}

void clang_ParmVarDecl_set_obj_c_decl_qualifier(void* thiz, unsigned int QTVal) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    clang::Decl::ObjCDeclQualifier QTVal_cast = (clang::Decl::ObjCDeclQualifier)QTVal;
    thiz_cast->setObjCDeclQualifier(QTVal_cast);
}

bool clang_ParmVarDecl_is_knr_promoted(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->isKNRPromoted();
}

void clang_ParmVarDecl_set_knr_promoted(void* thiz, bool promoted) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    thiz_cast->setKNRPromoted(promoted);
}

bool clang_ParmVarDecl_is_explicit_object_parameter(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->isExplicitObjectParameter();
}

bool clang_ParmVarDecl_has_default_arg(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->hasDefaultArg();
}

bool clang_ParmVarDecl_has_unparsed_default_arg(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->hasUnparsedDefaultArg();
}

bool clang_ParmVarDecl_has_uninstantiated_default_arg(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->hasUninstantiatedDefaultArg();
}

void clang_ParmVarDecl_set_unparsed_default_arg(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    thiz_cast->setUnparsedDefaultArg();
}

bool clang_ParmVarDecl_has_inherited_default_arg(void* thiz) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    return thiz_cast->hasInheritedDefaultArg();
}

void clang_ParmVarDecl_set_has_inherited_default_arg(void* thiz, bool I) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    thiz_cast->setHasInheritedDefaultArg(I);
}

void clang_ParmVarDecl_get_original_type(void* thiz, void* ret_value) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getOriginalType());
}

void clang_ParmVarDecl_set_owning_function(void* thiz, void* FD) {
    clang::ParmVarDecl* thiz_cast = reinterpret_cast<clang::ParmVarDecl*>(thiz);
    clang::DeclContext* FD_cast = reinterpret_cast<clang::DeclContext*>(FD);
    thiz_cast->setOwningFunction(FD_cast);
}

bool clang_ParmVarDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::ParmVarDecl::classof(D_cast);
}

bool clang_ParmVarDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::ParmVarDecl::classofKind(K_cast);
}

int clang_ParmVarDecl_size_of() {
    return sizeof(clang::ParmVarDecl);
}

int clang_ParmVarDecl_align_of() {
    return alignof(clang::ParmVarDecl);
}

void* clang_ParmVarDecl_as_clang_VarDecl(void* p) {
    return static_cast<clang::VarDecl*>(reinterpret_cast<clang::ParmVarDecl*>(p));
}

void* clang_ParmVarDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::ParmVarDecl*>(p));
}

void* clang_ParmVarDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::ParmVarDecl*>(p));
}

void* clang_ParmVarDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::ParmVarDecl*>(p));
}

void* clang_ParmVarDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::ParmVarDecl*>(p));
}


// END KRAPPER GEN for clang::ParmVarDecl


// BEGIN KRAPPER GEN for clang::TemplateArgumentList

void* clang_TemplateArgumentList_get(void* thiz, unsigned int Idx) {
    clang::TemplateArgumentList* thiz_cast = reinterpret_cast<clang::TemplateArgumentList*>(thiz);
    return (void*)&(thiz_cast->get(Idx));
}

void* clang_TemplateArgumentList_op_ind(void* thiz, unsigned int Idx) {
    clang::TemplateArgumentList* thiz_cast = reinterpret_cast<clang::TemplateArgumentList*>(thiz);
    return (void*)&(thiz_cast->operator[](Idx));
}

unsigned int clang_TemplateArgumentList_size(void* thiz) {
    clang::TemplateArgumentList* thiz_cast = reinterpret_cast<clang::TemplateArgumentList*>(thiz);
    return thiz_cast->size();
}

void* clang_TemplateArgumentList_data(void* thiz) {
    clang::TemplateArgumentList* thiz_cast = reinterpret_cast<clang::TemplateArgumentList*>(thiz);
    return (void*)thiz_cast->data();
}

int clang_TemplateArgumentList_size_of() {
    return sizeof(clang::TemplateArgumentList);
}

int clang_TemplateArgumentList_align_of() {
    return alignof(clang::TemplateArgumentList);
}


// END KRAPPER GEN for clang::TemplateArgumentList


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

void* clang_FunctionDecl_get_param_decl(void* thiz, unsigned int i) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getParamDecl(i);
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

void* clang_FunctionDecl_get_non_object_parameter(void* thiz, unsigned int I) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getNonObjectParameter(I);
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

void* clang_FunctionDecl_get_template_specialization_args(void* thiz) {
    clang::FunctionDecl* thiz_cast = reinterpret_cast<clang::FunctionDecl*>(thiz);
    return (void*)thiz_cast->getTemplateSpecializationArgs();
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

void* clang_FunctionDecl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::FunctionDecl*>(p));
}

void* clang_FunctionDecl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::FunctionDecl*>(p));
}


// END KRAPPER GEN for clang::FunctionDecl


// BEGIN KRAPPER GEN for clang::FunctionType

unsigned int clang_FunctionType_get_arm_za_state(unsigned int AttrBits) {
    return (unsigned int)clang::FunctionType::getArmZAState(AttrBits);
}

unsigned int clang_FunctionType_get_arm_zt0state(unsigned int AttrBits) {
    return (unsigned int)clang::FunctionType::getArmZT0State(AttrBits);
}

void clang_FunctionType_get_return_type(void* thiz, void* ret_value) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getReturnType());
}

bool clang_FunctionType_get_has_reg_parm(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->getHasRegParm();
}

unsigned int clang_FunctionType_get_reg_parm_type(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->getRegParmType();
}

bool clang_FunctionType_get_no_return_attr(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->getNoReturnAttr();
}

bool clang_FunctionType_get_cfi_unchecked_callee_attr(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->getCFIUncheckedCalleeAttr();
}

bool clang_FunctionType_get_cmse_ns_call_attr(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->getCmseNSCallAttr();
}

unsigned int clang_FunctionType_get_call_conv(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return (unsigned int)thiz_cast->getCallConv();
}

bool clang_FunctionType_is_const(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->isConst();
}

bool clang_FunctionType_is_volatile(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->isVolatile();
}

bool clang_FunctionType_is_restrict(void* thiz) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    return thiz_cast->isRestrict();
}

void clang_FunctionType_get_call_result_type(void* thiz, void* Context, void* ret_value) {
    clang::FunctionType* thiz_cast = reinterpret_cast<clang::FunctionType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getCallResultType(*Context_cast));
}

bool clang_FunctionType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::FunctionType::classof(T_cast);
}

const char* clang_FunctionType_get_name_for_call_conv(unsigned int CC) {
    clang::CallingConv CC_cast = (clang::CallingConv)CC;
    std::string ret_value = clang::FunctionType::getNameForCallConv(CC_cast).str();
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

int clang_FunctionType_size_of() {
    return sizeof(clang::FunctionType);
}

int clang_FunctionType_align_of() {
    return alignof(clang::FunctionType);
}

void* clang_FunctionType_as_clang_Type(void* p) {
    return static_cast<clang::Type*>(reinterpret_cast<clang::FunctionType*>(p));
}

void* clang_FunctionType_dyncast_clang_FunctionProtoType(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionProtoType>(reinterpret_cast<clang::FunctionType*>(p));
}


// END KRAPPER GEN for clang::FunctionType


// BEGIN KRAPPER GEN for clang::FunctionProtoType

unsigned int clang_FunctionProtoType_get_num_params(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->getNumParams();
}

void clang_FunctionProtoType_get_param_type(void* thiz, unsigned int i, void* ret_value) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getParamType(i));
}

unsigned int clang_FunctionProtoType_get_exception_spec_type(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (unsigned int)thiz_cast->getExceptionSpecType();
}

bool clang_FunctionProtoType_has_exception_spec(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasExceptionSpec();
}

bool clang_FunctionProtoType_has_dynamic_exception_spec(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasDynamicExceptionSpec();
}

bool clang_FunctionProtoType_has_noexcept_exception_spec(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasNoexceptExceptionSpec();
}

bool clang_FunctionProtoType_has_dependent_exception_spec(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasDependentExceptionSpec();
}

bool clang_FunctionProtoType_has_instantiation_dependent_exception_spec(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasInstantiationDependentExceptionSpec();
}

unsigned int clang_FunctionProtoType_get_num_exceptions(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->getNumExceptions();
}

void clang_FunctionProtoType_get_exception_type(void* thiz, unsigned int i, void* ret_value) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getExceptionType(i));
}

const void* clang_FunctionProtoType_get_exception_spec_decl(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (void*)thiz_cast->getExceptionSpecDecl();
}

const void* clang_FunctionProtoType_get_exception_spec_template(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (void*)thiz_cast->getExceptionSpecTemplate();
}

unsigned int clang_FunctionProtoType_can_throw(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (unsigned int)thiz_cast->canThrow();
}

bool clang_FunctionProtoType_is_nothrow(void* thiz, bool ResultIfDependent) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->isNothrow(ResultIfDependent);
}

bool clang_FunctionProtoType_is_variadic(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->isVariadic();
}

bool clang_FunctionProtoType_is_template_variadic(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->isTemplateVariadic();
}

bool clang_FunctionProtoType_has_trailing_return(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasTrailingReturn();
}

bool clang_FunctionProtoType_has_cfi_unchecked_callee(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasCFIUncheckedCallee();
}

unsigned int clang_FunctionProtoType_get_ref_qualifier(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (unsigned int)thiz_cast->getRefQualifier();
}

bool clang_FunctionProtoType_has_ext_parameter_infos(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->hasExtParameterInfos();
}

unsigned int clang_FunctionProtoType_get_a_arch64sme_attributes(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->getAArch64SMEAttributes();
}

int clang_FunctionProtoType_get_parameter_abi(void* thiz, unsigned int I) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return (int)thiz_cast->getParameterABI(I);
}

bool clang_FunctionProtoType_is_param_consumed(void* thiz, unsigned int I) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->isParamConsumed(I);
}

unsigned int clang_FunctionProtoType_get_num_function_effects(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->getNumFunctionEffects();
}

unsigned int clang_FunctionProtoType_get_num_function_effect_conditions(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->getNumFunctionEffectConditions();
}

bool clang_FunctionProtoType_is_sugared(void* thiz) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    return thiz_cast->isSugared();
}

void clang_FunctionProtoType_desugar(void* thiz, void* ret_value) {
    clang::FunctionProtoType* thiz_cast = reinterpret_cast<clang::FunctionProtoType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->desugar());
}

bool clang_FunctionProtoType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::FunctionProtoType::classof(T_cast);
}

int clang_FunctionProtoType_size_of() {
    return sizeof(clang::FunctionProtoType);
}

int clang_FunctionProtoType_align_of() {
    return alignof(clang::FunctionProtoType);
}

void* clang_FunctionProtoType_as_clang_FunctionType(void* p) {
    return static_cast<clang::FunctionType*>(reinterpret_cast<clang::FunctionProtoType*>(p));
}

void* clang_FunctionProtoType_as_clang_Type(void* p) {
    return static_cast<clang::Type*>(reinterpret_cast<clang::FunctionProtoType*>(p));
}


// END KRAPPER GEN for clang::FunctionProtoType


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

void clang_CXXMethodDecl_get_this_type__const_clang_FunctionProtoType_P_const_clang_CXXRecordDecl_P(void* FPT, void* Decl, void* ret_value) {
    const clang::FunctionProtoType* FPT_cast = reinterpret_cast<const clang::FunctionProtoType*>(FPT);
    const clang::CXXRecordDecl* Decl_cast = reinterpret_cast<const clang::CXXRecordDecl*>(Decl);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(clang::CXXMethodDecl::getThisType(FPT_cast, Decl_cast));
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

void* clang_CXXMethodDecl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}

void* clang_CXXMethodDecl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::CXXMethodDecl*>(p));
}


// END KRAPPER GEN for clang::CXXMethodDecl


// BEGIN KRAPPER GEN for clang::CXXConstructorDecl

bool clang_CXXConstructorDecl_is_explicit(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isExplicit();
}

unsigned int clang_CXXConstructorDecl_get_num_ctor_initializers(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->getNumCtorInitializers();
}

void clang_CXXConstructorDecl_set_num_ctor_initializers(void* thiz, unsigned int numCtorInitializers) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    thiz_cast->setNumCtorInitializers(numCtorInitializers);
}

bool clang_CXXConstructorDecl_is_delegating_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isDelegatingConstructor();
}

const void* clang_CXXConstructorDecl_get_target_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return (void*)thiz_cast->getTargetConstructor();
}

bool clang_CXXConstructorDecl_is_default_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isDefaultConstructor();
}

bool clang_CXXConstructorDecl_is_copy_constructor(void* thiz, unsigned int TypeQuals) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isCopyConstructor(TypeQuals);
}

bool _clang_CXXConstructorDecl_is_copy_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isCopyConstructor();
}

bool clang_CXXConstructorDecl_is_move_constructor(void* thiz, unsigned int TypeQuals) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isMoveConstructor(TypeQuals);
}

bool _clang_CXXConstructorDecl_is_move_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isMoveConstructor();
}

bool clang_CXXConstructorDecl_is_copy_or_move_constructor(void* thiz, unsigned int TypeQuals) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isCopyOrMoveConstructor(TypeQuals);
}

bool _clang_CXXConstructorDecl_is_copy_or_move_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isCopyOrMoveConstructor();
}

bool clang_CXXConstructorDecl_is_converting_constructor(void* thiz, bool AllowExplicit) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isConvertingConstructor(AllowExplicit);
}

bool clang_CXXConstructorDecl_is_specialization_copying_object(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isSpecializationCopyingObject();
}

bool clang_CXXConstructorDecl_is_inheriting_constructor(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return thiz_cast->isInheritingConstructor();
}

void clang_CXXConstructorDecl_set_inheriting_constructor(void* thiz, bool isIC) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    thiz_cast->setInheritingConstructor(isIC);
}

void* clang_CXXConstructorDecl_get_canonical_decl(void* thiz) {
    clang::CXXConstructorDecl* thiz_cast = reinterpret_cast<clang::CXXConstructorDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_CXXConstructorDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::CXXConstructorDecl::classof(D_cast);
}

bool clang_CXXConstructorDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::CXXConstructorDecl::classofKind(K_cast);
}

int clang_CXXConstructorDecl_size_of() {
    return sizeof(clang::CXXConstructorDecl);
}

int clang_CXXConstructorDecl_align_of() {
    return alignof(clang::CXXConstructorDecl);
}

void* clang_CXXConstructorDecl_as_clang_CXXMethodDecl(void* p) {
    return static_cast<clang::CXXMethodDecl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_FunctionDecl(void* p) {
    return static_cast<clang::FunctionDecl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}

void* clang_CXXConstructorDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::CXXConstructorDecl*>(p));
}


// END KRAPPER GEN for clang::CXXConstructorDecl


// BEGIN KRAPPER GEN for clang::CXXDestructorDecl

void clang_CXXDestructorDecl_set_operator_global_delete(void* thiz, void* OD) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    clang::FunctionDecl* OD_cast = reinterpret_cast<clang::FunctionDecl*>(OD);
    thiz_cast->setOperatorGlobalDelete(OD_cast);
}

void clang_CXXDestructorDecl_set_operator_array_delete(void* thiz, void* OD) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    clang::FunctionDecl* OD_cast = reinterpret_cast<clang::FunctionDecl*>(OD);
    thiz_cast->setOperatorArrayDelete(OD_cast);
}

void clang_CXXDestructorDecl_set_global_operator_array_delete(void* thiz, void* OD) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    clang::FunctionDecl* OD_cast = reinterpret_cast<clang::FunctionDecl*>(OD);
    thiz_cast->setGlobalOperatorArrayDelete(OD_cast);
}

void* clang_CXXDestructorDecl_get_operator_delete(void* thiz) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    return (void*)thiz_cast->getOperatorDelete();
}

void* clang_CXXDestructorDecl_get_operator_global_delete(void* thiz) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    return (void*)thiz_cast->getOperatorGlobalDelete();
}

void* clang_CXXDestructorDecl_get_array_operator_delete(void* thiz) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    return (void*)thiz_cast->getArrayOperatorDelete();
}

void* clang_CXXDestructorDecl_get_global_array_operator_delete(void* thiz) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    return (void*)thiz_cast->getGlobalArrayOperatorDelete();
}

bool clang_CXXDestructorDecl_is_called_by_delete(void* thiz, void* OpDel) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    const clang::FunctionDecl* OpDel_cast = reinterpret_cast<const clang::FunctionDecl*>(OpDel);
    return thiz_cast->isCalledByDelete(OpDel_cast);
}

void* clang_CXXDestructorDecl_get_canonical_decl(void* thiz) {
    clang::CXXDestructorDecl* thiz_cast = reinterpret_cast<clang::CXXDestructorDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_CXXDestructorDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::CXXDestructorDecl::classof(D_cast);
}

bool clang_CXXDestructorDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::CXXDestructorDecl::classofKind(K_cast);
}

int clang_CXXDestructorDecl_size_of() {
    return sizeof(clang::CXXDestructorDecl);
}

int clang_CXXDestructorDecl_align_of() {
    return alignof(clang::CXXDestructorDecl);
}

void* clang_CXXDestructorDecl_as_clang_CXXMethodDecl(void* p) {
    return static_cast<clang::CXXMethodDecl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_FunctionDecl(void* p) {
    return static_cast<clang::FunctionDecl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_DeclaratorDecl(void* p) {
    return static_cast<clang::DeclaratorDecl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_ValueDecl(void* p) {
    return static_cast<clang::ValueDecl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}

void* clang_CXXDestructorDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::CXXDestructorDecl*>(p));
}


// END KRAPPER GEN for clang::CXXDestructorDecl


// BEGIN KRAPPER GEN for clang::ClassTemplateSpecializationDecl

void* clang_ClassTemplateSpecializationDecl_get_most_recent_decl(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

const void* clang_ClassTemplateSpecializationDecl_get_definition_or_self(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (void*)thiz_cast->getDefinitionOrSelf();
}

const void* clang_ClassTemplateSpecializationDecl_get_specialized_template(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (void*)thiz_cast->getSpecializedTemplate();
}

void* clang_ClassTemplateSpecializationDecl_get_template_args(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (void*)&(thiz_cast->getTemplateArgs());
}

void clang_ClassTemplateSpecializationDecl_set_template_args(void* thiz, void* Args) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    clang::TemplateArgumentList* Args_cast = reinterpret_cast<clang::TemplateArgumentList*>(Args);
    thiz_cast->setTemplateArgs(Args_cast);
}

unsigned int clang_ClassTemplateSpecializationDecl_get_specialization_kind(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (unsigned int)thiz_cast->getSpecializationKind();
}

bool clang_ClassTemplateSpecializationDecl_is_explicit_specialization(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return thiz_cast->isExplicitSpecialization();
}

bool clang_ClassTemplateSpecializationDecl_is_class_scope_explicit_specialization(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return thiz_cast->isClassScopeExplicitSpecialization();
}

bool clang_ClassTemplateSpecializationDecl_is_explicit_instantiation_or_specialization(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return thiz_cast->isExplicitInstantiationOrSpecialization();
}

void clang_ClassTemplateSpecializationDecl_set_specialized_template(void* thiz, void* Specialized) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    clang::ClassTemplateDecl* Specialized_cast = reinterpret_cast<clang::ClassTemplateDecl*>(Specialized);
    thiz_cast->setSpecializedTemplate(Specialized_cast);
}

void clang_ClassTemplateSpecializationDecl_set_specialization_kind(void* thiz, unsigned int TSK) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setSpecializationKind(TSK_cast);
}

bool clang_ClassTemplateSpecializationDecl_has_strict_pack_match(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return thiz_cast->hasStrictPackMatch();
}

void clang_ClassTemplateSpecializationDecl_set_strict_pack_match(void* thiz, bool Val) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    thiz_cast->setStrictPackMatch(Val);
}

void* clang_ClassTemplateSpecializationDecl_get_template_instantiation_args(void* thiz) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    return (void*)&(thiz_cast->getTemplateInstantiationArgs());
}

void clang_ClassTemplateSpecializationDecl_set_instantiation_of__clang_ClassTemplateDecl_P(void* thiz, void* TemplDecl) {
    clang::ClassTemplateSpecializationDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(thiz);
    clang::ClassTemplateDecl* TemplDecl_cast = reinterpret_cast<clang::ClassTemplateDecl*>(TemplDecl);
    thiz_cast->setInstantiationOf(TemplDecl_cast);
}

bool clang_ClassTemplateSpecializationDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::ClassTemplateSpecializationDecl::classof(D_cast);
}

bool clang_ClassTemplateSpecializationDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::ClassTemplateSpecializationDecl::classofKind(K_cast);
}

int clang_ClassTemplateSpecializationDecl_size_of() {
    return sizeof(clang::ClassTemplateSpecializationDecl);
}

int clang_ClassTemplateSpecializationDecl_align_of() {
    return alignof(clang::ClassTemplateSpecializationDecl);
}

void* clang_ClassTemplateSpecializationDecl_as_clang_CXXRecordDecl(void* p) {
    return static_cast<clang::CXXRecordDecl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_RecordDecl(void* p) {
    return static_cast<clang::RecordDecl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_TagDecl(void* p) {
    return static_cast<clang::TagDecl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}

void* clang_ClassTemplateSpecializationDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(p));
}


// END KRAPPER GEN for clang::ClassTemplateSpecializationDecl


// BEGIN KRAPPER GEN for clang::ClassTemplateDecl

void clang_ClassTemplateDecl_load_lazy_specializations(void* thiz, bool OnlyPartial) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    thiz_cast->LoadLazySpecializations(OnlyPartial);
}

const void* clang_ClassTemplateDecl_get_templated_decl(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return (void*)thiz_cast->getTemplatedDecl();
}

bool clang_ClassTemplateDecl_is_this_declaration_a_definition(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return thiz_cast->isThisDeclarationADefinition();
}

void clang_ClassTemplateDecl_add_specialization(void* thiz, void* D, void* InsertPos) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    clang::ClassTemplateSpecializationDecl* D_cast = reinterpret_cast<clang::ClassTemplateSpecializationDecl*>(D);
    thiz_cast->AddSpecialization(D_cast, InsertPos);
}

void* clang_ClassTemplateDecl_get_canonical_decl(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

void* clang_ClassTemplateDecl_get_previous_decl(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return (void*)thiz_cast->getPreviousDecl();
}

void* clang_ClassTemplateDecl_get_most_recent_decl(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return (void*)thiz_cast->getMostRecentDecl();
}

const void* clang_ClassTemplateDecl_get_instantiated_from_member_template(void* thiz) {
    clang::ClassTemplateDecl* thiz_cast = reinterpret_cast<clang::ClassTemplateDecl*>(thiz);
    return (void*)thiz_cast->getInstantiatedFromMemberTemplate();
}

bool clang_ClassTemplateDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::ClassTemplateDecl::classof(D_cast);
}

bool clang_ClassTemplateDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::ClassTemplateDecl::classofKind(K_cast);
}

int clang_ClassTemplateDecl_size_of() {
    return sizeof(clang::ClassTemplateDecl);
}

int clang_ClassTemplateDecl_align_of() {
    return alignof(clang::ClassTemplateDecl);
}


// END KRAPPER GEN for clang::ClassTemplateDecl


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
    auto __range_0 = thiz_cast->bases();
    std::vector<clang::CXXBaseSpecifier*> __vec_0;
    for (auto __it_0 = __range_0.begin(); __it_0 != __range_0.end(); ++__it_0) __vec_0.push_back(kpp_to_elem_ptr<clang::CXXBaseSpecifier>(*__it_0));
    new (ret_value_cast) std::vector<clang::CXXBaseSpecifier*>(__vec_0);
}

unsigned int clang_CXXRecordDecl_get_num_v_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->getNumVBases();
}

void clang_CXXRecordDecl_vbases(void* thiz, void* ret_value) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    std::vector<clang::CXXBaseSpecifier*>* ret_value_cast = reinterpret_cast<std::vector<clang::CXXBaseSpecifier*>*>(ret_value);
    auto __range_1 = thiz_cast->vbases();
    std::vector<clang::CXXBaseSpecifier*> __vec_1;
    for (auto __it_1 = __range_1.begin(); __it_1 != __range_1.end(); ++__it_1) __vec_1.push_back(kpp_to_elem_ptr<clang::CXXBaseSpecifier>(*__it_1));
    new (ret_value_cast) std::vector<clang::CXXBaseSpecifier*>(__vec_1);
}

bool clang_CXXRecordDecl_has_any_dependent_bases(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return thiz_cast->hasAnyDependentBases();
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

const void* clang_CXXRecordDecl_get_generic_lambda_template_parameter_list(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getGenericLambdaTemplateParameterList();
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

void clang_CXXRecordDecl_added_selected_destructor(void* thiz, void* DD) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::CXXDestructorDecl* DD_cast = reinterpret_cast<clang::CXXDestructorDecl*>(DD);
    thiz_cast->addedSelectedDestructor(DD_cast);
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

const void* clang_CXXRecordDecl_get_described_class_template(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getDescribedClassTemplate();
}

void clang_CXXRecordDecl_set_described_class_template(void* thiz, void* Template) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    clang::ClassTemplateDecl* Template_cast = reinterpret_cast<clang::ClassTemplateDecl*>(Template);
    thiz_cast->setDescribedClassTemplate(Template_cast);
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

const void* clang_CXXRecordDecl_get_destructor(void* thiz) {
    clang::CXXRecordDecl* thiz_cast = reinterpret_cast<clang::CXXRecordDecl*>(thiz);
    return (void*)thiz_cast->getDestructor();
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

void* clang_CXXRecordDecl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::CXXRecordDecl*>(p));
}


// END KRAPPER GEN for clang::CXXRecordDecl


// BEGIN KRAPPER GEN for clang::NamespaceBaseDecl

void* clang_NamespaceBaseDecl_get_namespace(void* thiz) {
    clang::NamespaceBaseDecl* thiz_cast = reinterpret_cast<clang::NamespaceBaseDecl*>(thiz);
    return (void*)thiz_cast->getNamespace();
}

bool clang_NamespaceBaseDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::NamespaceBaseDecl::classof(D_cast);
}

bool clang_NamespaceBaseDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::NamespaceBaseDecl::classofKind(K_cast);
}

int clang_NamespaceBaseDecl_size_of() {
    return sizeof(clang::NamespaceBaseDecl);
}

int clang_NamespaceBaseDecl_align_of() {
    return alignof(clang::NamespaceBaseDecl);
}

void* clang_NamespaceBaseDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::NamespaceBaseDecl*>(p));
}

void* clang_NamespaceBaseDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::NamespaceBaseDecl*>(p));
}

void* clang_NamespaceBaseDecl_dyncast_clang_NamespaceDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceDecl>(reinterpret_cast<clang::NamespaceBaseDecl*>(p));
}


// END KRAPPER GEN for clang::NamespaceBaseDecl


// BEGIN KRAPPER GEN for clang::NamespaceDecl

bool clang_NamespaceDecl_is_anonymous_namespace(void* thiz) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    return thiz_cast->isAnonymousNamespace();
}

bool clang_NamespaceDecl_is_inline(void* thiz) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    return thiz_cast->isInline();
}

void clang_NamespaceDecl_set_inline(void* thiz, bool Inline) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    thiz_cast->setInline(Inline);
}

bool clang_NamespaceDecl_is_nested(void* thiz) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    return thiz_cast->isNested();
}

void clang_NamespaceDecl_set_nested(void* thiz, bool Nested) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    thiz_cast->setNested(Nested);
}

const void* clang_NamespaceDecl_get_anonymous_namespace(void* thiz) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    return (void*)thiz_cast->getAnonymousNamespace();
}

void clang_NamespaceDecl_set_anonymous_namespace(void* thiz, void* D) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    clang::NamespaceDecl* D_cast = reinterpret_cast<clang::NamespaceDecl*>(D);
    thiz_cast->setAnonymousNamespace(D_cast);
}

void* clang_NamespaceDecl_get_canonical_decl(void* thiz) {
    clang::NamespaceDecl* thiz_cast = reinterpret_cast<clang::NamespaceDecl*>(thiz);
    return (void*)thiz_cast->getCanonicalDecl();
}

bool clang_NamespaceDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::NamespaceDecl::classof(D_cast);
}

bool clang_NamespaceDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::NamespaceDecl::classofKind(K_cast);
}

void* clang_NamespaceDecl_cast_to_decl_context(void* D) {
    const clang::NamespaceDecl* D_cast = reinterpret_cast<const clang::NamespaceDecl*>(D);
    return (void*)clang::NamespaceDecl::castToDeclContext(D_cast);
}

void* clang_NamespaceDecl_cast_from_decl_context(void* DC) {
    const clang::DeclContext* DC_cast = reinterpret_cast<const clang::DeclContext*>(DC);
    return (void*)clang::NamespaceDecl::castFromDeclContext(DC_cast);
}

int clang_NamespaceDecl_size_of() {
    return sizeof(clang::NamespaceDecl);
}

int clang_NamespaceDecl_align_of() {
    return alignof(clang::NamespaceDecl);
}

void* clang_NamespaceDecl_as_clang_NamespaceBaseDecl(void* p) {
    return static_cast<clang::NamespaceBaseDecl*>(reinterpret_cast<clang::NamespaceDecl*>(p));
}

void* clang_NamespaceDecl_as_clang_DeclContext(void* p) {
    return static_cast<clang::DeclContext*>(reinterpret_cast<clang::NamespaceDecl*>(p));
}

void* clang_NamespaceDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::NamespaceDecl*>(p));
}

void* clang_NamespaceDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::NamespaceDecl*>(p));
}


// END KRAPPER GEN for clang::NamespaceDecl


// BEGIN KRAPPER GEN for clang::TranslationUnitDecl

const void* clang_TranslationUnitDecl_get_ast_context(void* thiz) {
    clang::TranslationUnitDecl* thiz_cast = reinterpret_cast<clang::TranslationUnitDecl*>(thiz);
    return (void*)&(thiz_cast->getASTContext());
}

const void* clang_TranslationUnitDecl_get_anonymous_namespace(void* thiz) {
    clang::TranslationUnitDecl* thiz_cast = reinterpret_cast<clang::TranslationUnitDecl*>(thiz);
    return (void*)thiz_cast->getAnonymousNamespace();
}

void clang_TranslationUnitDecl_set_anonymous_namespace(void* thiz, void* D) {
    clang::TranslationUnitDecl* thiz_cast = reinterpret_cast<clang::TranslationUnitDecl*>(thiz);
    clang::NamespaceDecl* D_cast = reinterpret_cast<clang::NamespaceDecl*>(D);
    thiz_cast->setAnonymousNamespace(D_cast);
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


// BEGIN KRAPPER GEN for clang::TemplateTypeParmDecl

bool clang_TemplateTypeParmDecl_was_declared_with_typename(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->wasDeclaredWithTypename();
}

bool clang_TemplateTypeParmDecl_has_default_argument(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->hasDefaultArgument();
}

bool clang_TemplateTypeParmDecl_default_argument_was_inherited(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->defaultArgumentWasInherited();
}

void clang_TemplateTypeParmDecl_set_inherited_default_argument(void* thiz, void* C, void* Prev) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    const clang::ASTContext* C_cast = reinterpret_cast<const clang::ASTContext*>(C);
    clang::TemplateTypeParmDecl* Prev_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(Prev);
    thiz_cast->setInheritedDefaultArgument(*C_cast, Prev_cast);
}

void clang_TemplateTypeParmDecl_remove_default_argument(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    thiz_cast->removeDefaultArgument();
}

void clang_TemplateTypeParmDecl_set_declared_with_typename(void* thiz, bool withTypename) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    thiz_cast->setDeclaredWithTypename(withTypename);
}

unsigned int clang_TemplateTypeParmDecl_get_depth(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->getDepth();
}

unsigned int clang_TemplateTypeParmDecl_get_index(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->getIndex();
}

bool clang_TemplateTypeParmDecl_is_parameter_pack(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->isParameterPack();
}

bool clang_TemplateTypeParmDecl_is_pack_expansion(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->isPackExpansion();
}

bool clang_TemplateTypeParmDecl_has_type_constraint(void* thiz) {
    clang::TemplateTypeParmDecl* thiz_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(thiz);
    return thiz_cast->hasTypeConstraint();
}

bool clang_TemplateTypeParmDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TemplateTypeParmDecl::classof(D_cast);
}

bool clang_TemplateTypeParmDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TemplateTypeParmDecl::classofKind(K_cast);
}

int clang_TemplateTypeParmDecl_size_of() {
    return sizeof(clang::TemplateTypeParmDecl);
}

int clang_TemplateTypeParmDecl_align_of() {
    return alignof(clang::TemplateTypeParmDecl);
}

void* clang_TemplateTypeParmDecl_as_clang_TypeDecl(void* p) {
    return static_cast<clang::TypeDecl*>(reinterpret_cast<clang::TemplateTypeParmDecl*>(p));
}

void* clang_TemplateTypeParmDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::TemplateTypeParmDecl*>(p));
}

void* clang_TemplateTypeParmDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TemplateTypeParmDecl*>(p));
}


// END KRAPPER GEN for clang::TemplateTypeParmDecl


// BEGIN KRAPPER GEN for clang::TemplateDecl

const void* clang_TemplateDecl_get_template_parameters(void* thiz) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    return (void*)thiz_cast->getTemplateParameters();
}

bool clang_TemplateDecl_has_associated_constraints(void* thiz) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    return thiz_cast->hasAssociatedConstraints();
}

const void* clang_TemplateDecl_get_templated_decl(void* thiz) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    return (void*)thiz_cast->getTemplatedDecl();
}

bool clang_TemplateDecl_is_type_alias(void* thiz) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    return thiz_cast->isTypeAlias();
}

bool clang_TemplateDecl_classof(void* D) {
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return clang::TemplateDecl::classof(D_cast);
}

bool clang_TemplateDecl_classof_kind(unsigned int K) {
    clang::Decl::Kind K_cast = (clang::Decl::Kind)K;
    return clang::TemplateDecl::classofKind(K_cast);
}

void clang_TemplateDecl_set_template_parameters(void* thiz, void* TParams) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    clang::TemplateParameterList* TParams_cast = reinterpret_cast<clang::TemplateParameterList*>(TParams);
    thiz_cast->setTemplateParameters(TParams_cast);
}

void clang_TemplateDecl_init(void* thiz, void* NewTemplatedDecl) {
    clang::TemplateDecl* thiz_cast = reinterpret_cast<clang::TemplateDecl*>(thiz);
    clang::NamedDecl* NewTemplatedDecl_cast = reinterpret_cast<clang::NamedDecl*>(NewTemplatedDecl);
    thiz_cast->init(NewTemplatedDecl_cast);
}

int clang_TemplateDecl_size_of() {
    return sizeof(clang::TemplateDecl);
}

int clang_TemplateDecl_align_of() {
    return alignof(clang::TemplateDecl);
}

void* clang_TemplateDecl_as_clang_NamedDecl(void* p) {
    return static_cast<clang::NamedDecl*>(reinterpret_cast<clang::TemplateDecl*>(p));
}

void* clang_TemplateDecl_as_clang_Decl(void* p) {
    return static_cast<clang::Decl*>(reinterpret_cast<clang::TemplateDecl*>(p));
}


// END KRAPPER GEN for clang::TemplateDecl


// BEGIN KRAPPER GEN for clang::ConstantArrayType

unsigned int clang_ConstantArrayType_get_size_bit_width(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->getSizeBitWidth();
}

bool clang_ConstantArrayType_is_zero_size(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->isZeroSize();
}

unsigned long clang_ConstantArrayType_get_z_ext_size(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->getZExtSize();
}

long clang_ConstantArrayType_get_s_ext_size(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->getSExtSize();
}

unsigned long clang_ConstantArrayType_get_limited_size(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->getLimitedSize();
}

bool clang_ConstantArrayType_is_sugared(void* thiz) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    return thiz_cast->isSugared();
}

void clang_ConstantArrayType_desugar(void* thiz, void* ret_value) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->desugar());
}

unsigned int clang_ConstantArrayType_get_num_addressing_bits__const_clang_ASTContext_and(void* thiz, void* Context) {
    clang::ConstantArrayType* thiz_cast = reinterpret_cast<clang::ConstantArrayType*>(thiz);
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return thiz_cast->getNumAddressingBits(*Context_cast);
}

unsigned int clang_ConstantArrayType_get_max_size_bits(void* Context) {
    const clang::ASTContext* Context_cast = reinterpret_cast<const clang::ASTContext*>(Context);
    return clang::ConstantArrayType::getMaxSizeBits(*Context_cast);
}

bool clang_ConstantArrayType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::ConstantArrayType::classof(T_cast);
}

int clang_ConstantArrayType_size_of() {
    return sizeof(clang::ConstantArrayType);
}

int clang_ConstantArrayType_align_of() {
    return alignof(clang::ConstantArrayType);
}

void* clang_ConstantArrayType_as_clang_ArrayType(void* p) {
    return static_cast<clang::ArrayType*>(reinterpret_cast<clang::ConstantArrayType*>(p));
}

void* clang_ConstantArrayType_as_clang_Type(void* p) {
    return static_cast<clang::Type*>(reinterpret_cast<clang::ConstantArrayType*>(p));
}


// END KRAPPER GEN for clang::ConstantArrayType


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

void clang_ASTContext_set_instantiated_from_static_data_member(void* thiz, void* Inst, void* Tmpl, unsigned int TSK) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::VarDecl* Inst_cast = reinterpret_cast<clang::VarDecl*>(Inst);
    clang::VarDecl* Tmpl_cast = reinterpret_cast<clang::VarDecl*>(Tmpl);
    clang::TemplateSpecializationKind TSK_cast = (clang::TemplateSpecializationKind)TSK;
    thiz_cast->setInstantiatedFromStaticDataMember(Inst_cast, Tmpl_cast, TSK_cast);
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

unsigned char clang_ASTContext_get_open_cl_type_kind(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return (unsigned char)thiz_cast->getOpenCLTypeKind(T_cast);
}

unsigned int clang_ASTContext_get_open_cl_type_addr_space(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return (unsigned int)thiz_cast->getOpenCLTypeAddrSpace(T_cast);
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

bool clang_ASTContext_block_requires_copying(void* thiz, void* Ty, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Ty_cast = reinterpret_cast<clang::QualType*>(Ty);
    const clang::VarDecl* D_cast = reinterpret_cast<const clang::VarDecl*>(D);
    return thiz_cast->BlockRequiresCopying(*Ty_cast, D_cast);
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

bool clang_ASTContext_is_representable_integer_value(void* thiz, void* Value, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    llvm::APSInt* Value_cast = reinterpret_cast<llvm::APSInt*>(Value);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->isRepresentableIntegerValue(*Value_cast, *T_cast);
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

void clang_ASTContext_get_subst_template_type_parm_pack_type(void* thiz, void* AssociatedDecl, unsigned int Index, bool Final, void* ArgPack, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::Decl* AssociatedDecl_cast = reinterpret_cast<clang::Decl*>(AssociatedDecl);
    const clang::TemplateArgument* ArgPack_cast = reinterpret_cast<const clang::TemplateArgument*>(ArgPack);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSubstTemplateTypeParmPackType(AssociatedDecl_cast, Index, Final, *ArgPack_cast));
}

void clang_ASTContext_get_subst_builtin_template_pack(void* thiz, void* ArgPack, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TemplateArgument* ArgPack_cast = reinterpret_cast<const clang::TemplateArgument*>(ArgPack);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getSubstBuiltinTemplatePack(*ArgPack_cast));
}

void clang_ASTContext_get_template_type_parm_type(void* thiz, unsigned int Depth, unsigned int Index, bool ParameterPack, void* ParmDecl, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TemplateTypeParmDecl* ParmDecl_cast = reinterpret_cast<clang::TemplateTypeParmDecl*>(ParmDecl);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getTemplateTypeParmType(Depth, Index, ParameterPack, ParmDecl_cast));
}

void clang_ASTContext_get_paren_type(void* thiz, void* NamedType, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* NamedType_cast = reinterpret_cast<clang::QualType*>(NamedType);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getParenType(*NamedType_cast));
}

void clang_ASTContext_get_injected_template_arg(void* thiz, void* ParamDecl, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::NamedDecl* ParamDecl_cast = reinterpret_cast<clang::NamedDecl*>(ParamDecl);
    clang::TemplateArgument* ret_value_cast = reinterpret_cast<clang::TemplateArgument*>(ret_value);
    new (ret_value_cast) clang::TemplateArgument(thiz_cast->getInjectedTemplateArg(ParamDecl_cast));
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

void clang_ASTContext_get_auto_type(void* thiz, void* DeducedType, int Keyword, bool IsDependent, bool IsPack, void* TypeConstraintConcept, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* DeducedType_cast = reinterpret_cast<clang::QualType*>(DeducedType);
    clang::AutoTypeKeyword Keyword_cast = (clang::AutoTypeKeyword)Keyword;
    clang::TemplateDecl* TypeConstraintConcept_cast = reinterpret_cast<clang::TemplateDecl*>(TypeConstraintConcept);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getAutoType(*DeducedType_cast, Keyword_cast, IsDependent, IsPack, TypeConstraintConcept_cast));
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

unsigned long clang_ASTContext_get_type_size__const_clang_Type_P(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return thiz_cast->getTypeSize(T_cast);
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

unsigned int clang_ASTContext_get_type_align__const_clang_Type_P(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return thiz_cast->getTypeAlign(T_cast);
}

unsigned int clang_ASTContext_get_type_unadjusted_align(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeUnadjustedAlign(*T_cast);
}

unsigned int clang_ASTContext_get_type_unadjusted_align__const_clang_Type_P(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return thiz_cast->getTypeUnadjustedAlign(T_cast);
}

unsigned int clang_ASTContext_get_type_align_if_known(void* thiz, void* T, bool NeedsPreferredAlignment) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return thiz_cast->getTypeAlignIfKnown(*T_cast, NeedsPreferredAlignment);
}

bool clang_ASTContext_is_alignment_required(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return thiz_cast->isAlignmentRequired(T_cast);
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

unsigned int clang_ASTContext_get_preferred_type_align__const_clang_Type_P(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return thiz_cast->getPreferredTypeAlign(T_cast);
}

unsigned int clang_ASTContext_get_target_default_align_for_attribute_aligned(void* thiz) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    return thiz_cast->getTargetDefaultAlignForAttributeAligned();
}

unsigned int clang_ASTContext_get_align_of_global_var(void* thiz, void* T, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return thiz_cast->getAlignOfGlobalVar(*T_cast, VD_cast);
}

unsigned int clang_ASTContext_get_min_global_align_of_var(void* thiz, unsigned long Size, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return thiz_cast->getMinGlobalAlignOfVar(Size, VD_cast);
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

void* clang_ASTContext_get_canonical_type(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return (void*)clang::ASTContext::getCanonicalType(T_cast);
}

bool clang_ASTContext_has_same_type(void* T1, void* T2) {
    clang::QualType* T1_cast = reinterpret_cast<clang::QualType*>(T1);
    clang::QualType* T2_cast = reinterpret_cast<clang::QualType*>(T2);
    return clang::ASTContext::hasSameType(*T1_cast, *T2_cast);
}

bool clang_ASTContext_has_same_type__const_clang_Type_P_const_clang_Type_P(void* T1, void* T2) {
    const clang::Type* T1_cast = reinterpret_cast<const clang::Type*>(T1);
    const clang::Type* T2_cast = reinterpret_cast<const clang::Type*>(T2);
    return clang::ASTContext::hasSameType(T1_cast, T2_cast);
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

bool clang_ASTContext_is_same_template_parameter_list(void* thiz, void* X, void* Y) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TemplateParameterList* X_cast = reinterpret_cast<const clang::TemplateParameterList*>(X);
    const clang::TemplateParameterList* Y_cast = reinterpret_cast<const clang::TemplateParameterList*>(Y);
    return thiz_cast->isSameTemplateParameterList(X_cast, Y_cast);
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

void clang_ASTContext_get_canonical_template_argument(void* thiz, void* Arg, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TemplateArgument* Arg_cast = reinterpret_cast<const clang::TemplateArgument*>(Arg);
    clang::TemplateArgument* ret_value_cast = reinterpret_cast<clang::TemplateArgument*>(ret_value);
    new (ret_value_cast) clang::TemplateArgument(thiz_cast->getCanonicalTemplateArgument(*Arg_cast));
}

bool clang_ASTContext_is_same_template_argument(void* thiz, void* Arg1, void* Arg2) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TemplateArgument* Arg1_cast = reinterpret_cast<const clang::TemplateArgument*>(Arg1);
    const clang::TemplateArgument* Arg2_cast = reinterpret_cast<const clang::TemplateArgument*>(Arg2);
    return thiz_cast->isSameTemplateArgument(*Arg1_cast, *Arg2_cast);
}

void* clang_ASTContext_get_as_array_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return (void*)thiz_cast->getAsArrayType(*T_cast);
}

void* clang_ASTContext_get_as_constant_array_type(void* thiz, void* T) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* T_cast = reinterpret_cast<clang::QualType*>(T);
    return (void*)thiz_cast->getAsConstantArrayType(*T_cast);
}

void clang_ASTContext_get_base_element_type(void* thiz, void* VAT, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ArrayType* VAT_cast = reinterpret_cast<const clang::ArrayType*>(VAT);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBaseElementType(VAT_cast));
}

void clang_ASTContext_get_base_element_type__clang_QualType(void* thiz, void* QT, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* QT_cast = reinterpret_cast<clang::QualType*>(QT);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->getBaseElementType(*QT_cast));
}

unsigned long clang_ASTContext_get_constant_array_element_count(void* thiz, void* CA) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ConstantArrayType* CA_cast = reinterpret_cast<const clang::ConstantArrayType*>(CA);
    return thiz_cast->getConstantArrayElementCount(CA_cast);
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

void clang_ASTContext_make_int_value(void* thiz, unsigned long Value, void* Type, void* ret_value) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::QualType* Type_cast = reinterpret_cast<clang::QualType*>(Type);
    llvm::APSInt* ret_value_cast = reinterpret_cast<llvm::APSInt*>(ret_value);
    new (ret_value_cast) llvm::APSInt(thiz_cast->MakeIntValue(Value, *Type_cast));
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

unsigned int clang_ASTContext_get_gva_linkage_for_variable(void* thiz, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return (unsigned int)thiz_cast->GetGVALinkageForVariable(VD_cast);
}

bool clang_ASTContext_decl_must_be_emitted(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::Decl* D_cast = reinterpret_cast<const clang::Decl*>(D);
    return thiz_cast->DeclMustBeEmitted(D_cast);
}

void* clang_ASTContext_get_copy_constructor_for_exception_object(void* thiz, void* RD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::CXXRecordDecl* RD_cast = reinterpret_cast<clang::CXXRecordDecl*>(RD);
    return (void*)thiz_cast->getCopyConstructorForExceptionObject(RD_cast);
}

void clang_ASTContext_add_copy_constructor_for_exception_object(void* thiz, void* RD, void* CD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::CXXRecordDecl* RD_cast = reinterpret_cast<clang::CXXRecordDecl*>(RD);
    clang::CXXConstructorDecl* CD_cast = reinterpret_cast<clang::CXXConstructorDecl*>(CD);
    thiz_cast->addCopyConstructorForExceptionObject(RD_cast, CD_cast);
}

void clang_ASTContext_add_typedef_name_for_unnamed_tag_decl(void* thiz, void* TD, void* TND) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    clang::TagDecl* TD_cast = reinterpret_cast<clang::TagDecl*>(TD);
    clang::TypedefNameDecl* TND_cast = reinterpret_cast<clang::TypedefNameDecl*>(TND);
    thiz_cast->addTypedefNameForUnnamedTagDecl(TD_cast, TND_cast);
}

void* clang_ASTContext_get_typedef_name_for_unnamed_tag_decl(void* thiz, void* TD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::TagDecl* TD_cast = reinterpret_cast<const clang::TagDecl*>(TD);
    return (void*)thiz_cast->getTypedefNameForUnnamedTagDecl(TD_cast);
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

void clang_ASTContext_set_static_local_number(void* thiz, void* VD, unsigned int Number) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    thiz_cast->setStaticLocalNumber(VD_cast, Number);
}

unsigned int clang_ASTContext_get_static_local_number(void* thiz, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return thiz_cast->getStaticLocalNumber(VD_cast);
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

void clang_ASTContext_add_operator_delete_for_v_dtor(void* thiz, void* Dtor, void* OperatorDelete, unsigned int K) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXDestructorDecl* Dtor_cast = reinterpret_cast<const clang::CXXDestructorDecl*>(Dtor);
    clang::FunctionDecl* OperatorDelete_cast = reinterpret_cast<clang::FunctionDecl*>(OperatorDelete);
    clang::ASTContext::OperatorDeleteKind K_cast = (clang::ASTContext::OperatorDeleteKind)K;
    thiz_cast->addOperatorDeleteForVDtor(Dtor_cast, OperatorDelete_cast, K_cast);
}

const void* clang_ASTContext_get_operator_delete_for_v_dtor(void* thiz, void* Dtor, unsigned int K) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXDestructorDecl* Dtor_cast = reinterpret_cast<const clang::CXXDestructorDecl*>(Dtor);
    clang::ASTContext::OperatorDeleteKind K_cast = (clang::ASTContext::OperatorDeleteKind)K;
    return (void*)thiz_cast->getOperatorDeleteForVDtor(Dtor_cast, K_cast);
}

bool clang_ASTContext_dtor_has_operator_delete(void* thiz, void* Dtor, unsigned int K) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::CXXDestructorDecl* Dtor_cast = reinterpret_cast<const clang::CXXDestructorDecl*>(Dtor);
    clang::ASTContext::OperatorDeleteKind K_cast = (clang::ASTContext::OperatorDeleteKind)K;
    return thiz_cast->dtorHasOperatorDelete(Dtor_cast, K_cast);
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

void clang_ASTContext_set_parameter_index(void* thiz, void* D, unsigned int index) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ParmVarDecl* D_cast = reinterpret_cast<const clang::ParmVarDecl*>(D);
    thiz_cast->setParameterIndex(D_cast, index);
}

unsigned int clang_ASTContext_get_parameter_index(void* thiz, void* D) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::ParmVarDecl* D_cast = reinterpret_cast<const clang::ParmVarDecl*>(D);
    return thiz_cast->getParameterIndex(D_cast);
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

bool clang_ASTContext_is_ms_static_data_member_inline_definition(void* thiz, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return thiz_cast->isMSStaticDataMemberInlineDefinition(VD_cast);
}

int clang_ASTContext_get_inline_variable_definition_kind(void* thiz, void* VD) {
    clang::ASTContext* thiz_cast = reinterpret_cast<clang::ASTContext*>(thiz);
    const clang::VarDecl* VD_cast = reinterpret_cast<const clang::VarDecl*>(VD);
    return (int)thiz_cast->getInlineVariableDefinitionKind(VD_cast);
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
    auto __range_2 = thiz_cast->decls();
    std::vector<clang::Decl*> __vec_2;
    for (auto __it_2 = __range_2.begin(); __it_2 != __range_2.end(); ++__it_2) __vec_2.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_2));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_2);
}

bool clang_DeclContext_decls_empty(void* thiz) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    return thiz_cast->decls_empty();
}

void clang_DeclContext_noload_decls(void* thiz, void* ret_value) {
    clang::DeclContext* thiz_cast = reinterpret_cast<clang::DeclContext*>(thiz);
    std::vector<clang::Decl*>* ret_value_cast = reinterpret_cast<std::vector<clang::Decl*>*>(ret_value);
    auto __range_3 = thiz_cast->noload_decls();
    std::vector<clang::Decl*> __vec_3;
    for (auto __it_3 = __range_3.begin(); __it_3 != __range_3.end(); ++__it_3) __vec_3.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_3));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_3);
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

void* clang_DeclContext_dyncast_clang_EnumDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumDecl>(reinterpret_cast<clang::DeclContext*>(p));
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

void* clang_DeclContext_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::DeclContext*>(p));
}

void* clang_DeclContext_dyncast_clang_NamespaceDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceDecl>(reinterpret_cast<clang::DeclContext*>(p));
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
    auto __range_4 = thiz_cast->redecls();
    std::vector<clang::Decl*> __vec_4;
    for (auto __it_4 = __range_4.begin(); __it_4 != __range_4.end(); ++__it_4) __vec_4.push_back(kpp_to_elem_ptr<clang::Decl>(*__it_4));
    new (ret_value_cast) std::vector<clang::Decl*>(__vec_4);
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

const void* clang_Decl_get_described_template(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getDescribedTemplate();
}

void* clang_Decl_get_described_template_params(void* thiz) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getDescribedTemplateParams();
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

void* clang_Decl_get_function_type(void* thiz, bool BlocksToo) {
    clang::Decl* thiz_cast = reinterpret_cast<clang::Decl*>(thiz);
    return (void*)thiz_cast->getFunctionType(BlocksToo);
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

void* clang_Decl_dyncast_clang_DeclaratorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::DeclaratorDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_VarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::VarDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_ValueDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ValueDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_EnumConstantDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumConstantDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_EnumDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::EnumDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TypeDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypeDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TypedefNameDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TypedefNameDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TagDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TagDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_FieldDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FieldDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_RecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::RecordDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_ParmVarDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ParmVarDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_FunctionDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::FunctionDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXMethodDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXMethodDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXConstructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXConstructorDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXDestructorDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXDestructorDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_ClassTemplateSpecializationDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::ClassTemplateSpecializationDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_CXXRecordDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::CXXRecordDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_NamespaceBaseDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceBaseDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_NamespaceDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::NamespaceDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TranslationUnitDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TranslationUnitDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TemplateTypeParmDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateTypeParmDecl>(reinterpret_cast<clang::Decl*>(p));
}

void* clang_Decl_dyncast_clang_TemplateDecl(void* p) {
    return llvm::dyn_cast_or_null<clang::TemplateDecl>(reinterpret_cast<clang::Decl*>(p));
}


// END KRAPPER GEN for clang::Decl


// BEGIN KRAPPER GEN for clang::TemplateTypeParmType

unsigned int clang_TemplateTypeParmType_get_depth(void* thiz) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    return thiz_cast->getDepth();
}

unsigned int clang_TemplateTypeParmType_get_index(void* thiz) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    return thiz_cast->getIndex();
}

bool clang_TemplateTypeParmType_is_parameter_pack(void* thiz) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    return thiz_cast->isParameterPack();
}

const void* clang_TemplateTypeParmType_get_decl(void* thiz) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    return (void*)thiz_cast->getDecl();
}

bool clang_TemplateTypeParmType_is_sugared(void* thiz) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    return thiz_cast->isSugared();
}

void clang_TemplateTypeParmType_desugar(void* thiz, void* ret_value) {
    clang::TemplateTypeParmType* thiz_cast = reinterpret_cast<clang::TemplateTypeParmType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->desugar());
}

bool clang_TemplateTypeParmType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::TemplateTypeParmType::classof(T_cast);
}

int clang_TemplateTypeParmType_size_of() {
    return sizeof(clang::TemplateTypeParmType);
}

int clang_TemplateTypeParmType_align_of() {
    return alignof(clang::TemplateTypeParmType);
}

void* clang_TemplateTypeParmType_as_clang_Type(void* p) {
    return static_cast<clang::Type*>(reinterpret_cast<clang::TemplateTypeParmType*>(p));
}


// END KRAPPER GEN for clang::TemplateTypeParmType


// BEGIN KRAPPER GEN for clang::TypedefType

const void* clang_TypedefType_get_decl(void* thiz) {
    clang::TypedefType* thiz_cast = reinterpret_cast<clang::TypedefType*>(thiz);
    return (void*)thiz_cast->getDecl();
}

bool clang_TypedefType_is_sugared(void* thiz) {
    clang::TypedefType* thiz_cast = reinterpret_cast<clang::TypedefType*>(thiz);
    return thiz_cast->isSugared();
}

void clang_TypedefType_desugar(void* thiz, void* ret_value) {
    clang::TypedefType* thiz_cast = reinterpret_cast<clang::TypedefType*>(thiz);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(thiz_cast->desugar());
}

bool clang_TypedefType_type_matches_decl(void* thiz) {
    clang::TypedefType* thiz_cast = reinterpret_cast<clang::TypedefType*>(thiz);
    return thiz_cast->typeMatchesDecl();
}

bool clang_TypedefType_classof(void* T) {
    const clang::Type* T_cast = reinterpret_cast<const clang::Type*>(T);
    return clang::TypedefType::classof(T_cast);
}

int clang_TypedefType_size_of() {
    return sizeof(clang::TypedefType);
}

int clang_TypedefType_align_of() {
    return alignof(clang::TypedefType);
}


// END KRAPPER GEN for clang::TypedefType


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
void* kppbridge_build_ast_with_args(const char* code, const char* filename, const char* joinedArgs) {
    return (void*)kppbridge::buildASTWithArgs(code, filename, joinedArgs);
}
int kppbridge_num_template_args(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    return kppbridge::numTemplateArgs(*type_cast);
}
void kppbridge_template_arg_as_type(void* type, unsigned int index, void* ret_value) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::templateArgAsType(*type_cast, index));
}
const char* kppbridge_qualified_name(void* decl) {
    const clang::NamedDecl* decl_cast = reinterpret_cast<const clang::NamedDecl*>(decl);
    std::string ret_value = kppbridge::qualifiedName(decl_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
const char* kppbridge_template_base_name(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    std::string ret_value = kppbridge::templateBaseName(*type_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
void kppbridge_default_arg_type(void* parm, void* ret_value) {
    const clang::TemplateTypeParmDecl* parm_cast = reinterpret_cast<const clang::TemplateTypeParmDecl*>(parm);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::defaultArgType(parm_cast));
}
const char* kppbridge_default_arg_text(void* parm) {
    clang::ParmVarDecl* parm_cast = reinterpret_cast<clang::ParmVarDecl*>(parm);
    std::string ret_value = kppbridge::defaultArgText(parm_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
void* _clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}
void* _kppbridge_build_ast_with_args(const char* code, const char* filename, const char* joinedArgs) {
    return (void*)kppbridge::buildASTWithArgs(code, filename, joinedArgs);
}
int _kppbridge_num_template_args(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    return kppbridge::numTemplateArgs(*type_cast);
}
void _kppbridge_template_arg_as_type(void* type, unsigned int index, void* ret_value) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::templateArgAsType(*type_cast, index));
}
const char* _kppbridge_qualified_name(void* decl) {
    const clang::NamedDecl* decl_cast = reinterpret_cast<const clang::NamedDecl*>(decl);
    std::string ret_value = kppbridge::qualifiedName(decl_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
const char* _kppbridge_template_base_name(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    std::string ret_value = kppbridge::templateBaseName(*type_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
void _kppbridge_default_arg_type(void* parm, void* ret_value) {
    const clang::TemplateTypeParmDecl* parm_cast = reinterpret_cast<const clang::TemplateTypeParmDecl*>(parm);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::defaultArgType(parm_cast));
}
const char* _kppbridge_default_arg_text(void* parm) {
    clang::ParmVarDecl* parm_cast = reinterpret_cast<clang::ParmVarDecl*>(parm);
    std::string ret_value = kppbridge::defaultArgText(parm_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
void* __clang_tooling_build_ast_from_code(const char* Code, const char* FileName) {
    llvm::StringRef Code_cast = llvm::StringRef(Code);
    llvm::StringRef FileName_cast = llvm::StringRef(FileName);
    return (void*)clang::tooling::buildASTFromCode(Code_cast, FileName_cast).release();
}
void* __kppbridge_build_ast_with_args(const char* code, const char* filename, const char* joinedArgs) {
    return (void*)kppbridge::buildASTWithArgs(code, filename, joinedArgs);
}
int __kppbridge_num_template_args(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    return kppbridge::numTemplateArgs(*type_cast);
}
void __kppbridge_template_arg_as_type(void* type, unsigned int index, void* ret_value) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::templateArgAsType(*type_cast, index));
}
const char* __kppbridge_qualified_name(void* decl) {
    const clang::NamedDecl* decl_cast = reinterpret_cast<const clang::NamedDecl*>(decl);
    std::string ret_value = kppbridge::qualifiedName(decl_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
const char* __kppbridge_template_base_name(void* type) {
    const clang::QualType* type_cast = reinterpret_cast<const clang::QualType*>(type);
    std::string ret_value = kppbridge::templateBaseName(*type_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}
void __kppbridge_default_arg_type(void* parm, void* ret_value) {
    const clang::TemplateTypeParmDecl* parm_cast = reinterpret_cast<const clang::TemplateTypeParmDecl*>(parm);
    clang::QualType* ret_value_cast = reinterpret_cast<clang::QualType*>(ret_value);
    new (ret_value_cast) clang::QualType(kppbridge::defaultArgType(parm_cast));
}
const char* __kppbridge_default_arg_text(void* parm) {
    clang::ParmVarDecl* parm_cast = reinterpret_cast<clang::ParmVarDecl*>(parm);
    std::string ret_value = kppbridge::defaultArgText(parm_cast);
    char* ret_value_cast = new char[ret_value.length() + 1];
    ret_value.copy(ret_value_cast, ret_value.length(), 0);
    ret_value_cast[ret_value.length()] = '\0';
    return ret_value_cast;
}

}

