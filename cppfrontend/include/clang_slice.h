// Include guard required: the generated wrapper includes this header both directly and
// through each KrapperForce_*.h (via differently-spelled relative paths, so #pragma once
// can't be trusted to dedupe) — without a guard the inline kppbridge helper below is a
// same-TU redefinition. The clang/LLVM includes self-guard, which is why the header
// didn't need one before it carried a definition of its own.
#ifndef KPLUSPLUS_CPPFRONTEND_CLANG_SLICE_H_
#define KPLUSPLUS_CPPFRONTEND_CLANG_SLICE_H_

#include <clang/Frontend/ASTUnit.h>
#include <clang/AST/DeclCXX.h>
#include <clang/AST/DeclTemplate.h>
#include <clang/AST/ASTContext.h>
#include <clang/AST/Type.h>
#include <clang/Tooling/Tooling.h>
#include <clang/Lex/Lexer.h>

#include <string>
#include <vector>

// brick-6 BRIDGE (#44, documented): the libclang front-end recovers a parameter default's
// VALUE as source text — ModelFactories.defaultValue tokenizes the default sub-expression
// cursor's extent and joins the spellings. The C++-AST equivalent is
// Lexer::getSourceText(CharSourceRange::getTokenRange(ParmVarDecl::getDefaultArgRange())),
// but binding that call through only() drags in clang::Lexer + clang::SourceManager +
// clang::LangOptions — three very large classes bound for ONE static call, each a likely
// source of APSInt-style operator/bitfield fixup discovery (a gap to close iteratively,
// recorded on #44). Until that surface is proven, this inline helper is the smallest
// bridge: one bound free function from an already-bound ParmVarDecl* to the exact text.
// hasDefaultArg() itself IS bound and stays the authoritative flag (ModelBuilder).
//
// String contract vs libclang: tokenSpellings().joinToString("") concatenates tokens with
// NO separator, while getSourceText preserves the source's inter-token whitespace. The
// two agree for every default written without internal spaces ("5", "-1", "RED",
// "nullptr", "Palette()"); a spaced default (`= Palette ( )`) diverges — Phase C
// normalizer entry: compare default values with whitespace stripped.
namespace kppbridge {
// brick-3 BRIDGE (#45, instantiation forcing): the forcing-parse fixture #includes std
// headers (<vector>), which clang::tooling can only resolve with real driver arguments —
// at minimum `-resource-dir` (the tool name "clang-tool" defeats the relative resource-dir
// computation) and the language standard. The bound `buildASTFromCode(code, filename)`
// overload takes no args, and the args-taking overload's `const std::vector<std::string>&`
// parameter is not a bindable surface yet (the std::vector<std::string> instantiation +
// the by-value-vector marshalling). Until it is, this inline helper is the smallest
// bridge: args arrive '\n'-joined in one string (no escaping — driver args never contain
// newlines), and the returned ASTUnit* transfers ownership via .release(), exactly the
// raw-pointer contract the unique_ptr-return rewrite gives the bound buildASTFromCode.
inline clang::ASTUnit *buildASTWithArgs(const char *code, const char *filename,
                                        const char *joinedArgs) {
    std::vector<std::string> args;
    std::string current;
    for (const char *c = joinedArgs; *c; ++c) {
        if (*c == '\n') {
            if (!current.empty()) args.push_back(current);
            current.clear();
        } else {
            current.push_back(*c);
        }
    }
    if (!current.empty()) args.push_back(current);
    return clang::tooling::buildASTFromCodeWithArgs(code, args, filename).release();
}

// brick-3 BRIDGE (#45, instantiation forcing): mirror libclang's GetTemplateArguments
// (CXType.cpp) — the template-argument read that PREFERS the sugared
// TemplateSpecializationType's WRITTEN arguments (so `std::vector<Item*>` carries ONE
// arg, not the canonical two with the defaulted allocator) and falls back to the
// canonical specialization decl's full list. The TST half also catches DEPENDENT
// specializations (`vector<_Tp, _Alloc>` inside the template's own member signatures),
// which have no canonical record at all. TemplateName / ArrayRef<TemplateArgument>
// aren't bindable surfaces yet, hence the bridge. -1 = not a template specialization.
inline int numTemplateArgs(const clang::QualType &type) {
    if (type.isNull()) return -1;
    if (const auto *spec = type->getAs<clang::TemplateSpecializationType>())
        return (int)spec->template_arguments().size();
    if (const auto *record = type->getAsCXXRecordDecl())
        if (const auto *decl =
                llvm::dyn_cast<clang::ClassTemplateSpecializationDecl>(record))
            return (int)decl->getTemplateArgs().size();
    return -1;
}

// args[index] when it is a TYPE argument; a null QualType otherwise — mirroring
// clang_Type_getTemplateArgumentAsType's CXType_Invalid for value/template args,
// which TypeFactories drops via filterNotNull.
inline clang::QualType templateArgAsType(const clang::QualType &type, unsigned index) {
    if (const auto *spec = type->getAs<clang::TemplateSpecializationType>()) {
        auto args = spec->template_arguments();
        if (index < args.size() && args[index].getKind() == clang::TemplateArgument::Type)
            return args[index].getAsType();
        return clang::QualType();
    }
    if (const auto *record = type->getAsCXXRecordDecl())
        if (const auto *decl =
                llvm::dyn_cast<clang::ClassTemplateSpecializationDecl>(record)) {
            const auto &args = decl->getTemplateArgs();
            if (index < args.size() &&
                args[index].getKind() == clang::TemplateArgument::Type)
                return args[index].getAsType();
        }
    return clang::QualType();
}

// The specialization's TEMPLATE name, qualified — the decl-side equivalent of
// createForType(forTemplateBase=true)'s referencedDecl.fullyQualified (libclang's
// clang_getTypeDeclaration resolves a TST to its ClassTemplateDecl and a resolved
// specialization to its ClassTemplateSpecializationDecl; both spell the "::"-joined
// semantic-parent chain).
inline std::string templateBaseName(const clang::QualType &type) {
    if (type.isNull()) return std::string();
    if (const auto *spec = type->getAs<clang::TemplateSpecializationType>())
        if (const auto *decl = spec->getTemplateName().getAsTemplateDecl())
            return decl->getQualifiedNameAsString();
    if (const auto *record = type->getAsCXXRecordDecl())
        return record->getQualifiedNameAsString();
    return std::string();
}

// Phase D BRIDGE (#46): a template type parameter's default TYPE argument
// (`typename _Alloc = std::allocator<_Tp>`). hasDefaultArgument() itself IS bound;
// getDefaultArgument() returns a TemplateArgumentLoc (clang 19+), which isn't a bindable
// surface yet — this helper unwraps it to the QualType the model decode consumes. A null
// QualType signals "no default / not a type default" (mirroring templateArgAsType's
// convention above).
inline clang::QualType defaultArgType(const clang::TemplateTypeParmDecl *parm) {
    if (!parm || !parm->hasDefaultArgument()) return clang::QualType();
    const clang::TemplateArgument &arg = parm->getDefaultArgument().getArgument();
    if (arg.getKind() != clang::TemplateArgument::Type) return clang::QualType();
    return arg.getAsType();
}

inline std::string defaultArgText(clang::ParmVarDecl *parm) {
    if (!parm || !parm->hasDefaultArg()) {
        return std::string();
    }
    // getDefaultArgRange (not getDefaultArg()->getSourceRange()): it self-guards the
    // unparsed/uninstantiated default states getDefaultArg() asserts on.
    const clang::ASTContext &ctx = parm->getASTContext();
    return clang::Lexer::getSourceText(
               clang::CharSourceRange::getTokenRange(parm->getDefaultArgRange()),
               ctx.getSourceManager(), ctx.getLangOpts())
        .str();
}
} // namespace kppbridge

#endif // KPLUSPLUS_CPPFRONTEND_CLANG_SLICE_H_
