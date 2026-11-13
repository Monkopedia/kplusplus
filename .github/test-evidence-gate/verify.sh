#!/usr/bin/env bash
# Executable evidence for gate.sh, the "assert the tests actually ran" gate every
# workflow calls -- run on the fixtures beside it, plus a check that every workflow
# site still calls it.
#
# WHY THIS FILE EXISTS. Three reviewers read that arithmetic and found it obviously
# right. It was not: it gated on `tests=`, which COUNTS SKIPPED TESTCASES, so a suite
# that was entirely @Ignore'd reported a full, healthy tests="N" with no failures and
# passed the gate having executed nothing -- the exact hole the gate was written to
# plug (Refs #198, Refs #222, Refs #230). A correct gate and a broken gate are
# indistinguishable while everything is green, which is how it survived. Prose in a
# comment cannot be executed; these fixtures can. Run this, do not re-reason it.
#
#   bash .github/test-evidence-gate/verify.sh
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1
F=fixtures
rc=0
ok() { printf '  ok    %s\n' "$1"; }
bad() { printf '  FAIL  %s\n' "$1"; rc=1; }
want() { # <label> <expected> <actual>
  if [ "$2" = "$3" ]; then ok "$1 = $2"; else bad "$1: expected $2, got $3"; fi
}

# The gate itself, sourced for its sum_attr (#247) -- not a copy. The broken variants
# under NEGATIVE CONTROLS are local on purpose: they prove the fixtures discriminate.
# shellcheck source-path=SCRIPTDIR source=gate.sh
. ./gate.sh
set +e
nospace() { grep -o "<testsuite[^>]*>" "$1" | grep -o " $2=\"[0-9]*\"" \
              | grep -o '[0-9]*' | awk '{ n += $1 } END { print n + 0 }' || true; }
head1()   { grep -o "$2=\"[0-9]*\"" "$1" | head -1 | tr -dc '0-9'; }    # pre-fix: first suite only

# The real `gate.sh`, run as the workflows run it, on module trees built from fixtures.
TMPD=$(mktemp -d) || exit 1
trap 'rm -rf "$TMPD"' EXIT
GATE=$PWD/gate.sh
tree() { # <module[:fixture,...]>... -> a fresh tree in $T; a bare module has src/test, no XML
  T=$(mktemp -d -p "$TMPD"); local spec m fx fxs
  for spec in "$@"; do
    m=${spec%%:*}; mkdir -p "$T/$m/src/test"
    [ "$spec" = "$m" ] && continue
    mkdir -p "$T/$m/build/test-results/nativeTest"
    IFS=, read -ra fxs <<< "${spec#*:}"
    for fx in "${fxs[@]}"; do cp "$F/$fx.xml" "$T/$m/build/test-results/nativeTest/"; done
  done
}
gate() { (cd "$T" && bash "$GATE" "$@") > "$T.log" 2>&1; echo $?; }
executed() { # <fixture...> -> the executed count gate.sh logs for one module holding them
  tree "a:$(IFS=,; echo "$*")"; gate assert a >/dev/null
  sed -n 's/^a: \([0-9-]*\) test(s) executed.*/\1/p' "$T.log"
}

echo "== executed counts as gate.sh computes them (tests - skipped, every <testsuite>)"
want "healthy.xml executed"              9  "$(executed healthy)"
want "all-skipped.xml executed"          0  "$(executed all-skipped)"
want "boundary.xml executed"             1  "$(executed boundary)"
want "nested.xml executed"               6  "$(executed nested)"
want "multi-suite-failure.xml executed"  10 "$(executed multi-suite-failure)"
want "decoy-system-out.xml executed"     2  "$(executed decoy-system-out)"
want "errored.xml executed"              6  "$(executed errored)"
want "three report files in one module"  10 "$(executed healthy all-skipped boundary)"

echo "== failures AND errors must be seen in EVERY suite, not just the first"
want "multi-suite-failure.xml failures"  1  "$(sum_attr $F/multi-suite-failure.xml failures)"
want "decoy-system-out.xml failures"     0  "$(sum_attr $F/decoy-system-out.xml failures)"
# A test that BLEW UP is recorded as an error, not a failure. Until errored.xml every
# fixture here carried errors="0", so the gate's `errors` term was indistinguishable
# from its absence (Refs #243) -- the same shape as the single data point Refs #240 fixed.
want "errored.xml errors"                2  "$(sum_attr $F/errored.xml errors)"
want "errored.xml failures"              0  "$(sum_attr $F/errored.xml failures)"

# ---------------------------------------------------------------------------
# NEGATIVE CONTROLS. If these ever stop failing, this script has stopped
# discriminating and every check above is worthless.
# ---------------------------------------------------------------------------
echo "== negative controls (each is a real bug; each MUST still be wrong here)"
want "gating on 'tests' passes the all-skipped suite"   9  "$(sum_attr $F/all-skipped.xml tests)"
want "dropping the space after 'testsuite' double-counts nested" \
     12 "$(( $(nospace $F/nested.xml tests) - $(nospace $F/nested.xml skipped) ))"
want "head -1 misses the failure in the second suite"   0  "$(head1 $F/multi-suite-failure.xml failures)"
want "head -1 misses the ERRORS in the second suite"    0  "$(head1 $F/errored.xml errors)"
want "head -1 undercounts the multi-suite total"        4  "$(head1 $F/multi-suite-failure.xml tests)"

# ---------------------------------------------------------------------------
# THE VERDICT, NOT THE VALUE. A `::error::` with no `exit 1` is an annotation, not a
# failure (Refs #243), so these judge gate.sh's EXIT STATUS. A `fail` must be the gate's
# own verdict -- exit 1 after an `::error::` -- not a crash; the positive controls stop a
# gate that exits 1 unconditionally from satisfying the rest.
# ---------------------------------------------------------------------------
echo "== gate.sh assert, judged on its exit status"
verdict() { # <pass|fail> <label> <gate args...>
  local expect=$1 label=$2 st; shift 2
  st=$(gate "$@")
  if { [ "$expect" = pass ] && [ "$st" -eq 0 ]; } \
     || { [ "$expect" = fail ] && [ "$st" -eq 1 ] && grep -q '^::error::' "$T.log"; }
  then ok "$expect: $label"; else bad "expected to $expect, exited $st: $label"; sed 's/^/        /' "$T.log"; fi
}
tree a;                         verdict fail "zero JUnit XML"                            assert a
tree a:all-skipped;             verdict fail "an all-skipped 9-of-9 suite"               assert a
tree a:multi-suite-failure;     verdict fail "a failure in the SECOND suite"             assert a
tree a:errored;                 verdict fail "ERRORS in the SECOND suite"                assert a
# The summed-gate bug (#230): a healthy module must not cover for a silent one.
tree a:healthy b;               verdict fail "b has no XML while a has 9 tests"          assert a b
tree a:healthy b:all-skipped;   verdict fail "b all-skipped while a has 9 tests"         assert a b
tree a:healthy;                 verdict fail "a listed module that does not exist"       assert a b
tree a:healthy;                 verdict fail "a module glob matching nothing"            assert 'x/*/src/test'
tree a:healthy;                 verdict fail "no modules at all"                         assert
tree a:healthy b:boundary,decoy-system-out,nested
verdict pass "two healthy modules, four fixtures"     assert a b
want "  ...and it counted them" "TOTAL: 18 test(s) executed across 2 module(s)" "$(tail -1 "$T.log")"
tree a:healthy b:nested c;      verdict fail "glob discovery still requires every module" assert '*/src/test'
# The workflows pass "$TEST_MODULES" as ONE space-joined argument; every word must match.
tree a:healthy b:healthy;       verdict pass "one joined argument, every word a module"  assert 'a b'
tree a:healthy b:healthy;       verdict fail "one joined argument with a typo'd word"    assert 'a bb b'
tree a:healthy;                 verdict fail "one joined argument, a glob matching nothing" assert 'a x/*/src/test'
tree a:healthy;                 verdict fail "an empty TEST_MODULES"                     assert ''
tree a:healthy b:healthy;       verdict fail "clear with a typo'd word in a joined argument" clear 'a bb'
tree a:healthy b:nested;        verdict pass "glob discovery of every */src/test"        assert '*/src/test'
want "  ...discovered both" "gating on 2 module(s): a b" "$(head -1 "$T.log")"
echo "== gate.sh clear: a stale report must not satisfy a later assert"
tree a:healthy b:healthy;       verdict pass "clear"                                     clear a b
verdict fail "assert after clear, nothing re-run" assert a
verdict fail "...for every module cleared"        assert b
if [ -d "$T/a/src/test" ]; then ok "clear leaves the module itself alone"; else bad "clear deleted more than test-results"; fi

# ---------------------------------------------------------------------------
# EVERY SITE STILL CALLS IT. A workflow that stops calling the gate is the failure mode
# a single implementation leaves, so pin each workflow's sequence of gate calls (a site
# not in SITES must make none). Every call names the job's own "$TEST_MODULES", so a
# `clear` and its `assert` cannot cover different modules. An inline copy of the
# arithmetic coming back (`sum_attr` in a workflow) fails too.
# ---------------------------------------------------------------------------
echo "== every workflow site calls the gate, clear before assert"
declare -A SITES=(
  [compiler-check.yml]="clear assert"
  [release.yml]="clear assert"
  [root-build.yml]="clear assert clear assert"
  [samples-minimal.yml]="clear assert"
)
# shellcheck disable=SC2016 # a literal $TEST_MODULES, matched in the workflow text
CALL='^[[:space:]]*(run: )?bash \.github/test-evidence-gate/gate\.sh (clear|assert) "\$TEST_MODULES"[[:space:]]*$'
for wf in ../workflows/*.yml; do
  name=${wf##*/}
  got=$(grep -E "$CALL" "$wf" | grep -oE '(clear|assert)' | paste -sd' ')
  want "$name gate calls" "${SITES[$name]-}" "$got"
done
# A gate step that cannot fail is no gate: `continue-on-error`, or an `if:` other than
# `always()` (which only makes it run after a failed build too), turns it off quietly.
read -r ngate nsoft < <(CALL=$CALL awk '
  function close_step() { if (gate) { g++; if (soft) n++ }; gate = 0; soft = 0 }
  FNR == 1 || /^      - / { close_step() }
  $0 ~ ENVIRON["CALL"] { gate = 1 }
  /^        continue-on-error:/ || (/^        if:/ && !/^        if: always\(\)[[:space:]]*$/) { soft = 1 }
  END { close_step(); print g + 0, n + 0 }' ../workflows/*.yml)
want "gate steps found (vacuity guard)"                     10 "$ngate"
want "gate steps with continue-on-error or a skipping if:" 0  "$nsoft"
want "workflows defining their own sum_attr" 0 "$(grep -c 'sum_attr' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')"

# Every `::error::` a workflow or the gate emits is a diagnosis whose consequence is the
# very next line (Refs #243). Matching the emitting line, not prose, and pinning the
# denominator so a guard that vanishes changes a number here. The gate's own guards all go
# through die(), whose `exit 1` is exercised by every `fail` verdict above; one-line helpers
# like it (samples-multiproject's fail()) count when their `exit 1` closes the same line.
E='^[[:space:]]*([a-z_]+[(][)] [{] )?echo "::error::'
ne=$(grep -cE "$E" ../workflows/*.yml gate.sh | awk -F: '{n+=$2} END{print n+0}')
np=$(awk -v e="$E" '$0 ~ e { if (index($0, "; exit 1; }")) n++; else if ((getline nxt) > 0 && nxt ~ /^[[:space:]]*exit 1[[:space:]]*$/) n++ }
          END { print n + 0 }' ../workflows/*.yml gate.sh)
want "::error:: emitters across the workflows and gate.sh" 7 "$ne"
want "...each one immediately followed by 'exit 1'"       7 "$np"

echo
[ $rc -eq 0 ] && echo "test-evidence gate: OK" || echo "test-evidence gate: FAILED"
exit $rc
