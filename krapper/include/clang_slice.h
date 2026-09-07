// Include guard required: the generated wrapper includes this header both directly and
// through each KrapperForce_*.h (via differently-spelled relative paths, so #pragma once
// can't be trusted to dedupe) — without a guard the inline kppbridge helper below is a
// same-TU redefinition. The clang/LLVM includes self-guard, which is why the header
// didn't need one before it carried a definition of its own.
#ifndef KPLUSPLUS_KRAPPER_PARSE_CLANG_SLICE_H_
#define KPLUSPLUS_KRAPPER_PARSE_CLANG_SLICE_H_

#include <clang/Frontend/ASTUnit.h>
#include <clang/AST/DeclCXX.h>
#include <clang/AST/DeclTemplate.h>
#include <clang/AST/ASTContext.h>
#include <clang/AST/Type.h>
#include <clang/Tooling/Tooling.h>
#include <clang/Lex/Lexer.h>
#include <clang/Basic/Diagnostic.h>
#include <clang/Basic/SourceManager.h>
#include <llvm/ADT/SmallString.h>

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
// Collector for the parse diagnostics the bridge below hands back to Kotlin (#224).
// Internal to the bridge — nothing here is bound; only kppbridge::lastParseDiagnostics is.
namespace detail {
class ParseDiagCollector : public clang::DiagnosticConsumer {
public:
    // Accumulated records in the wire format documented on lastParseDiagnostics().
    std::string records;

    // Overrides DiagnosticConsumer::clear(), which zeroes the base tallies; keep that
    // behaviour and drop the previous parse's records with it.
    void clear() override {
        clang::DiagnosticConsumer::clear();
        records.clear();
    }

    void HandleDiagnostic(clang::DiagnosticsEngine::Level level,
                          const clang::Diagnostic &info) override {
        // Keep the base class's error/warning tallies accurate for anything that reads them.
        clang::DiagnosticConsumer::HandleDiagnostic(level, info);
        if (level < clang::DiagnosticsEngine::Error) return;
        llvm::SmallString<256> text;
        info.FormatDiagnostic(text);
        std::string message(text.str());
        for (char &c : message) {
            if (c == '\n' || c == '\r' || c == '\t') c = ' ';
        }
        std::string loc;
        if (info.hasSourceManager() && info.getLocation().isValid()) {
            clang::PresumedLoc presumed =
                info.getSourceManager().getPresumedLoc(info.getLocation());
            if (presumed.isValid() && presumed.getFilename()) {
                loc = std::string(presumed.getFilename()) + ":" +
                      std::to_string(presumed.getLine()) + ":" +
                      std::to_string(presumed.getColumn());
            }
        }
        records += (level == clang::DiagnosticsEngine::Fatal) ? "fatal" : "error";
        records += '\t';
        records += loc;
        records += '\t';
        records += message;
        records += '\n';
    }
};

// One collector per process, CLEARED at the start of every buildASTWithArgs. A run parses the
// root header plus one forcing TU per --instantiate, strictly one at a time on one thread, and
// the records are drained immediately after each parse returns — so "last parse" is
// unambiguous. A function-local static (not a namespace-scope object) so the header stays
// header-only across the TUs that include it.
inline ParseDiagCollector &parseDiagCollector() {
    static ParseDiagCollector instance;
    return instance;
}
} // namespace detail

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
    // The parse's own error diagnostics are COLLECTED (see lastParseDiagnostics below)
    // rather than printed and forgotten: passing the collector as the DiagnosticConsumer
    // is what lets --strict-diagnostics be a real gate. Everything before it is
    // buildASTFromCodeWithArgs' own default argument, respelled because C++ has no way to
    // skip to the last parameter.
    detail::parseDiagCollector().clear();
    return clang::tooling::buildASTFromCodeWithArgs(
               code, args, filename, "clang-tool",
               std::make_shared<clang::PCHContainerOperations>(),
               clang::tooling::getClangStripDependencyFileAdjuster(),
               clang::tooling::FileContentMappings(), &detail::parseDiagCollector())
        .release();
}

// #224 BRIDGE: the ERROR-severity diagnostics of the most recent buildASTWithArgs call.
//
// With the default `DiagConsumer = nullptr`, clang::tooling prints every parse error through
// a TextDiagnosticPrinter to stderr and the tool keeps whatever AST clang recovered — so the
// generator cannot tell a clean parse from one that lost half a class to a bad include, and
// --strict-diagnostics had nothing to read. DiagnosticsEngine / clang::Diagnostic /
// PresumedLoc are not bindable surfaces (they drag in SourceManager and most of Basic/), so
// the collection happens C++-side and the result comes back as ONE string, in the same
// "smallest bridge" shape as declLocation above.
//
// WIRE FORMAT — one record per line, `severity \t file:line:col \t message`:
//   * severity is `fatal` or `error` (warnings and notes are the front-end's normal noise
//     and are not collected);
//   * the location is EMPTY when the diagnostic has none — a command-line-level or
//     otherwise unattributable error, which the Kotlin side treats as un-recoverable;
//   * tabs and newlines inside a message are folded to spaces so the framing holds.
// Decoded by com.monkopedia.krapper.generator.decodeParseDiagnostics.
inline std::string lastParseDiagnostics() { return detail::parseDiagCollector().records; }

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

// Phase D BRIDGE (#46): the "::"-joined qualified name INCLUDING inline namespaces.
// libclang's fullyQualified walk joins every named semantic parent — `std::string`'s
// template spells `std::__cxx11::basic_string` — but NamedDecl::getQualifiedNameAsString
// suppresses redundant inline namespaces by default (SuppressInlineNamespaceMode::
// Redundant), spelling `std::basic_string`. The mismatch breaks resolution on the cpp
// path: the basic_string ClassTemplate element's qualified name (built from the model's
// REAL namespace chain, which includes the inline __cxx11) never matches the type-use
// spelling, so std::string members silently drop. Print with None instead.
inline std::string qualifiedName(const clang::NamedDecl *decl) {
    if (!decl) return std::string();
    clang::PrintingPolicy policy(decl->getASTContext().getLangOpts());
    policy.SuppressInlineNamespace =
        llvm::to_underlying(clang::PrintingPolicy::SuppressInlineNamespaceMode::None);
    std::string out;
    llvm::raw_string_ostream os(out);
    decl->printQualifiedName(os, policy);
    return out;
}

// The specialization's TEMPLATE name, qualified — the decl-side equivalent of
// createForType(forTemplateBase=true)'s referencedDecl.fullyQualified (libclang's
// clang_getTypeDeclaration resolves a TST to its ClassTemplateDecl and a resolved
// specialization to its ClassTemplateSpecializationDecl; both spell the "::"-joined
// semantic-parent chain — inline namespaces included, hence qualifiedName above).
inline std::string templateBaseName(const clang::QualType &type) {
    if (type.isNull()) return std::string();
    if (const auto *spec = type->getAs<clang::TemplateSpecializationType>())
        if (const auto *decl = spec->getTemplateName().getAsTemplateDecl())
            return qualifiedName(decl);
    if (const auto *record = type->getAsCXXRecordDecl())
        return qualifiedName(record);
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

// #185 BRIDGE: the presumed `file:line:col` of a declaration, as one string.
//
// SourceLocation / PresumedLoc are value types with no bindable surface here (and binding
// SourceManager wholesale would drag in most of Basic/), so the whole lookup is done C++-side
// and the position comes back already spelled. An empty string means "no usable location"
// (an invalid loc, or a decl synthesized by the compiler) and the caller leaves the element
// unlocated. This is what lets a dropped binding be reported against real C++ source instead
// of a bare symbol name.
inline std::string declLocation(const clang::Decl *decl) {
    if (!decl) return std::string();
    const clang::SourceManager &sm = decl->getASTContext().getSourceManager();
    clang::PresumedLoc loc = sm.getPresumedLoc(decl->getLocation());
    if (loc.isInvalid() || !loc.getFilename()) return std::string();
    return std::string(loc.getFilename()) + ":" + std::to_string(loc.getLine()) + ":" +
           std::to_string(loc.getColumn());
}
} // namespace kppbridge

#endif // KPLUSPLUS_KRAPPER_PARSE_CLANG_SLICE_H_
