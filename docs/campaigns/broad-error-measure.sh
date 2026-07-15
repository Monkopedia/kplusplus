#!/usr/bin/env bash
# Broad-surface codegen error measurement harness (issue #10).
#
# Forces the FULL transitive Clang slice under DefaultFilter + INCLUDE_MISSING (the broadest
# case krapper_gen can be asked to bind), then compiles the generated C++ with clang++ and
# buckets the errors. This is the "measure" half of the #10 measure-fix-remeasure loop that
# PR #162 first made reachable (base-bind collapsed 250s -> 4.3s).
#
# Prerequisites (run once, on an LLVM box):
#   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
#   export GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx12g -XX:MaxMetaspaceSize=512m"
#   ./gradlew :krapper_gen:linkReleaseExecutableNative -PenableClang
#   ./gradlew :clangwalk:kplusplusSync -PenableClang   # produces the clang-slice base_model
#
# Usage:  docs/campaigns/broad-error-measure.sh
# Output: prints the total error count + a per-family bucket table.
set -euo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
KEXE="$REPO/krapper_gen/build/bin/native/releaseExecutable/krapper_gen.kexe"
CM="$REPO/clangwalk/build/krapped-cpp/cpp-models"
HDR="$REPO/krapper_parse/include/clang_slice.h"
OUT="${1:-/tmp/kpp10-broad-measure}"

[ -x "$KEXE" ] || { echo "missing $KEXE — build it first (see header)"; exit 1; }
[ -f "$CM/base_model.json" ] || { echo "missing $CM/base_model.json — run :clangwalk:kplusplusSync"; exit 1; }

rm -rf "$OUT"; mkdir -p "$OUT"

# DefaultFilter (no --only) + INCLUDE_MISSING = the broad transitive surface. The generator
# runs its OWN final compile and exits 134 on the remaining errors, but the .cc/.h are fully
# written before that, which is all we need. So the non-zero exit here is EXPECTED.
"$KEXE" \
  --header "$HDR" \
  --parsedModel "$CM/base_model.json" \
  --referencePolicy INCLUDE_MISSING \
  --compiler clang++ --std c++17 -o "$OUT" \
  --instantiate "std::vector<clang::Decl*>" \
  --instantiate "std::vector<clang::CXXMethodDecl*>" \
  --instantiate "std::vector<clang::CXXBaseSpecifier*>" \
  --forcingModel "std::vector<clang::Decl*>=$CM/KrapperForce_std_vector_clang_DeclPtr.json" \
  --forcingModel "std::vector<clang::CXXMethodDecl*>=$CM/KrapperForce_std_vector_clang_CXXMethodDeclPtr.json" \
  --forcingModel "std::vector<clang::CXXBaseSpecifier*>=$CM/KrapperForce_std_vector_clang_CXXBaseSpecifierPtr.json" \
  > "$OUT/gen.log" 2>&1 || true

[ -f "$OUT/clang_slice.h.cc" ] || { echo "generation produced no .cc — see $OUT/gen.log"; exit 1; }

# Standalone compile with all errors surfaced (-ferror-limit=0).
clang++ -std=c++17 -fsyntax-only -ferror-limit=0 \
  -I"$OUT" -I"$REPO/krapper_parse/include" -I/usr/include \
  "$OUT/clang_slice.h.cc" 2> "$OUT/errors.txt" || true

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
