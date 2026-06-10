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
