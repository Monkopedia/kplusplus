#!/usr/bin/env bash
# Executable evidence for the JUnit-XML arithmetic used by every "assert the tests
# actually ran" gate in .github/workflows/.
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
cd "$(dirname "${BASH_SOURCE[0]}")"
F=fixtures
rc=0
ok() { printf '  ok    %s\n' "$1"; }
bad() { printf '  FAIL  %s\n' "$1"; rc=1; }
want() { # <label> <expected> <actual>
  if [ "$2" = "$3" ]; then ok "$1 = $2"; else bad "$1: expected $2, got $3"; fi
}

# ---------------------------------------------------------------------------
# THE CANONICAL FORM. Byte-identical to the helper defined in all five gate steps;
# the drift check at the bottom fails if any site stops matching it.
# ---------------------------------------------------------------------------
sum_attr() { # <file> <attr> -> summed over every <testsuite> element in the file
  grep -o '<testsuite [^>]*>' "$1" | grep -o " $2=\"[0-9]*\"" \
    | grep -o '[0-9]*' | awk '{ n += $1 } END { print n + 0 }' || true
}
executed() { echo $(( $(sum_attr "$1" tests) - $(sum_attr "$1" skipped) )); }
broken()  { echo $(( $(sum_attr "$1" tests) )); }                       # pre-fix: gated on `tests`
nospace() { grep -o "<testsuite[^>]*>" "$1" | grep -o " $2=\"[0-9]*\"" \
              | grep -o '[0-9]*' | awk '{ n += $1 } END { print n + 0 }' || true; }
head1()   { grep -o "$2=\"[0-9]*\"" "$1" | head -1 | tr -dc '0-9'; }    # pre-fix: first suite only

echo "== executed counts (tests - skipped, summed over every <testsuite>)"
want "healthy.xml executed"              9  "$(executed $F/healthy.xml)"
want "all-skipped.xml executed"          0  "$(executed $F/all-skipped.xml)"
want "boundary.xml executed"             1  "$(executed $F/boundary.xml)"
want "nested.xml executed"               6  "$(executed $F/nested.xml)"
want "multi-suite-failure.xml executed"  10 "$(executed $F/multi-suite-failure.xml)"
want "decoy-system-out.xml executed"     2  "$(executed $F/decoy-system-out.xml)"

echo "== failures must be seen in EVERY suite, not just the first"
want "multi-suite-failure.xml failures"  1  "$(sum_attr $F/multi-suite-failure.xml failures)"
want "decoy-system-out.xml failures"     0  "$(sum_attr $F/decoy-system-out.xml failures)"

echo "== gate verdicts"
for f in healthy boundary nested multi-suite-failure decoy-system-out; do
  [ "$(executed $F/$f.xml)" -gt 0 ] && ok "$f.xml executes something -> gate passes" \
    || bad "$f.xml wrongly reads as having executed nothing"
done
[ "$(executed $F/all-skipped.xml)" -eq 0 ] \
  && ok "all-skipped.xml executes nothing -> gate goes RED" \
  || bad "all-skipped.xml must read as zero executed"

# ---------------------------------------------------------------------------
# NEGATIVE CONTROLS. If these ever stop failing, this script has stopped
# discriminating and every check above is worthless.
# ---------------------------------------------------------------------------
echo "== negative controls (each is a real bug; each MUST still be wrong here)"
want "gating on 'tests' passes the all-skipped suite"   9  "$(broken $F/all-skipped.xml)"
want "dropping the space after 'testsuite' double-counts nested" \
     12 "$(( $(nospace $F/nested.xml tests) - $(nospace $F/nested.xml skipped) ))"
want "head -1 misses the failure in the second suite"   0  "$(head1 $F/multi-suite-failure.xml failures)"
want "head -1 undercounts the multi-suite total"        4  "$(head1 $F/multi-suite-failure.xml tests)"

# ---------------------------------------------------------------------------
# ANTI-DRIFT. Pin every gate site to the form tested above, so a site cannot be
# edited back into a shape these fixtures never see.
# ---------------------------------------------------------------------------
echo "== all five gate sites still use the canonical form"
L1='grep -o '"'"'<testsuite [^>]*>'"'"' "$1" | grep -o " $2=\"[0-9]*\"" \'
L2='| grep -o '"'"'[0-9]*'"'"' | awk '"'"'{ n += $1 } END { print n + 0 }'"'"' || true'
n1=$(grep -Fc "$L1" ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
n2=$(grep -Fc "$L2" ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
want "gate sites matching the canonical pattern line" 5 "$n1"
want "gate sites matching the canonical sum line"     5 "$n2"
nh=$(grep -n 'head -1' ../workflows/*.yml | grep -v 'clang++ --version' | grep -vc '^\S*:[0-9]*: *#')
want "XML attribute reads still using head -1"        0 "$nh"

# ---------------------------------------------------------------------------
# THE ARITHMETIC AS SHIPPED -- and the reason this block exists.
#
# Everything above proves a LOCAL copy of `executed = tests - skipped`. Proving a
# copy is not proving the gates. The five workflow copies of that subtraction were
# untested in situ, so deleting `- skipped` from a gate left every fixture, every
# drift check and all of CI green: the guard against the bug had the SAME blind
# spot as the bug, one level up. Checking a control against a representation of
# the thing instead of the thing is how this defect survived three reviews; it is
# not allowed to be how its guard fails too.
#
# So: pull each gate's OWN assignment out of the workflow text and RUN it on an
# all-skipped suite. This tests semantics, not spelling -- reformat the line
# freely, but a gate that stops subtracting `skipped` reads 9 instead of 0 here.
# ---------------------------------------------------------------------------
echo "== each gate's own executed-count line, run on a 9-of-9-skipped suite"
mapfile -t exec_lines < <(grep -hoE '^ *(total_)?executed=\$\(\([^)]*\)\)' ../workflows/*.yml)
want "gate sites computing an executed count" 5 "${#exec_lines[@]}"
zero=0
for line in "${exec_lines[@]}"; do
  reported=9 skipped=9 total_reported=9 total_skipped=9
  unset executed total_executed
  eval "$line"
  v=${executed:-${total_executed:-UNSET}}
  if [ "$v" = 0 ]; then zero=$((zero + 1))
  else bad "in situ: '${line#"${line%%[![:space:]]*}"}' yields $v on an all-skipped suite, must be 0"; fi
done
want "gate sites reading ZERO executed for an all-skipped suite" 5 "$zero"

# The gate must also ACT on that number. Computing `executed` correctly and then
# testing `reported` would pass everything above and still ship the defect.
echo "== each gate fails on the EXECUTED count, never the reported one"
ng=$(grep -cE '^ *if \[ "\$(total_)?executed" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
nr=$(grep -cE '^ *if \[ "\$(total_)?reported" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
want "gate sites failing when zero tests EXECUTED"      5 "$ng"
want "gate sites failing on the REPORTED count instead" 0 "$nr"

# Negative control for the two checks above: the broken forms must still be broken
# here, or the in-situ test has stopped discriminating and says nothing.
reported=9 skipped=9; unset executed; eval 'executed=$((reported))'
want "a gate that dropped '- skipped' reads nonzero (so it IS caught)" 9 "$executed"

echo
[ $rc -eq 0 ] && echo "test-evidence gate arithmetic: OK" || echo "test-evidence gate arithmetic: FAILED"
exit $rc
