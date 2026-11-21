#!/usr/bin/env bash
# Broad-surface codegen error measurement harness (issue #10).
#
# Forces the FULL transitive Clang slice under DefaultFilter + INCLUDE_MISSING (the broadest
# case krapper can be asked to bind), then compiles the generated C++ with clang++ and
# buckets the errors. This is the "measure" half of the #10 measure-fix-remeasure loop that
# PR #162 first made reachable (base-bind collapsed 250s -> 4.3s).
#
# Prerequisites (run once, on an LLVM 22 box):
#   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
#   ./gradlew --no-daemon :krapper:linkReleaseExecutableKlinker
# The single `krapper` binary parses the header in-process (#184), so there is no separate
# model to produce first: clangwalk builds from its committed seed and has no parse to borrow.
#
# STATUS (#316, 2026-11-20): under DefaultFilter + INCLUDE_MISSING the run gets through parse,
# resolve, forcing and code generation, and writes the full .cc/.h plus ~3760 Kotlin files. The
# anonymous enum that used to abort it (clang/Sema/ScopeInfo.h:190, named `(unnamed enum at
# ...)` and used as a file name, #231/#316) now binds through `decltype` of its field. The
# generator's own final compile then fails on the remaining errors (176 on LLVM 22 at #316),
# which is the expected exit this script measures past.
#
# Usage:  docs/campaigns/broad-error-measure.sh [outdir]
# Output: prints the total error count + a per-family bucket table.
set -euo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
KEXE="$REPO/krapper/build/bin/klinker/krapperRelease/krapper"
HDR="$REPO/clangwalk/include/clang_slice.h"
OUT="${1:-/tmp/kpp10-broad-measure}"
LLVM_CONFIG="${LLVM_CONFIG:-llvm-config}"  # LLVM 22: put /usr/lib/llvm22/bin first on PATH
LLVM_INC="$("$LLVM_CONFIG" --includedir)"
export CPATH="$LLVM_INC${CPATH:+:$CPATH}"
LLVM_LIB="$("$LLVM_CONFIG" --libdir)"
export LD_LIBRARY_PATH="$LLVM_LIB${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"

[ -x "$KEXE" ] || { echo "missing $KEXE — build it first (see header)"; exit 1; }

rm -rf "$OUT"; mkdir -p "$OUT"

# DefaultFilter (no --only) + INCLUDE_MISSING = the broad transitive surface. The generator
# runs its OWN final compile and fails on the remaining errors, but the .cc/.h are fully
# written before that, which is all we need. So the non-zero exit here is EXPECTED.
"$KEXE" clang_slice \
  --header "$HDR" \
  --referencePolicy INCLUDE_MISSING \
  --compiler clang++ --std c++17 -o "$OUT" \
  --instantiate "std::vector<clang::Decl*>" \
  --instantiate "std::vector<clang::CXXMethodDecl*>" \
  --instantiate "std::vector<clang::CXXBaseSpecifier*>" \
  > "$OUT/gen.log" 2>&1 || true

[ -f "$OUT/clang_slice.cc" ] || { echo "generation produced no .cc — see $OUT/gen.log"; exit 1; }

# Standalone compile with all errors surfaced (-ferror-limit=0).
clang++ -std=c++17 -fsyntax-only -ferror-limit=0 \
  -I"$OUT" -I"$REPO/clangwalk/include" -I"$LLVM_INC" \
  "$OUT/clang_slice.cc" 2> "$OUT/errors.txt" || true

E="$OUT/errors.txt"
count() { grep -c "$1" "$E" || true; }
echo "=== broad-surface C++ error buckets ($OUT/errors.txt) ==="
printf '%-42s %s\n' "TOTAL"                     "$(count 'error:')"
printf '%-42s %s\n' "rvalue-param-init"         "$(count "cannot initialize a parameter of type .* with an rvalue")"
printf '%-42s %s\n' "no-matching-function"      "$(count 'no matching function for call')"
printf '%-42s %s\n' "no-matching-constructor"   "$(count 'no matching constructor')"
printf '%-42s %s\n' "requires-template-args"    "$(count 'requires template arguments')"
printf '%-42s %s\n' "deduction-not-allowed"     "$(count 'argument deduction not allowed')"
printf '%-42s %s\n' "invalid-binary-operands"   "$(count 'invalid operands to binary')"
printf '%-42s %s\n' "pointer-to-deduced"        "$(count 'cannot form pointer to deduced')"
printf '%-42s %s\n' "deleted-ctor"              "$(count 'implicitly-deleted\|call to deleted')"
