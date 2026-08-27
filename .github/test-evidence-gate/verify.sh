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
# So: pull each gate's OWN arithmetic out of the workflow text and RUN it on the
# fixtures -- its own `sum_attr`, its own accumulation loop, its own subtraction,
# nothing reimplemented here. This tests semantics, not spelling: reformat the
# lines freely, but a gate that stops subtracting `skipped` reads 9 instead of 0
# on all-skipped.xml.
#
# Two properties this deliberately has, both learned the hard way (Refs #240):
#
#  * SEVERAL data points, not one. The earlier version of this block ran only
#    `reported=9 skipped=9 -> 0`, which `executed=$((reported - 9))` also
#    satisfies while reporting 91 executed on a real all-skipped 100-test suite.
#    One sample can be hit by a constant; six cannot.
#  * The ACCUMULATION LOOP, not just the subtraction. `sum_attr "$f" skips` -- a
#    one-letter typo -- sums nothing, so `skipped` stays 0 and every suite reports
#    its full `tests` count as executed. That is the original bug restored, and
#    checking the `executed=` line alone cannot see it.
# ---------------------------------------------------------------------------
echo "== each gate's own arithmetic (accumulation AND subtraction), run on the fixtures"
TMPD=$(mktemp -d) || exit 1
trap 'rm -rf "$TMPD"' EXIT

# Each gate site contributes one runnable block: its `sum_attr` definition, then the
# contiguous run from `reported=0` (or `total_reported=0`) through the `executed=`
# subtraction. The outer per-module loop is not part of the arithmetic, and at every
# site the arithmetic is contiguous, so nothing has to be stitched or rewritten.
nblocks=$(awk -v outdir="$TMPD" '
  FNR == 1 { st = 0; blk = "" }
  st == 0 && /^[[:space:]]*sum_attr\(\) \{/ { st = 1; blk = $0 "\n"; next }
  st == 1 { blk = blk $0 "\n"; if ($0 ~ /^[[:space:]]*\}[[:space:]]*$/) st = 2; next }
  st == 2 && /^[[:space:]]*(total_)?reported=0/ { st = 3; blk = blk $0 "\n"; next }
  st == 3 { blk = blk $0 "\n"
            if ($0 ~ /^[[:space:]]*(total_)?executed=\$\(\(/) {
              n++; f = sprintf("%s/site-%02d.sh", outdir, n)
              printf "%s", blk > f; close(f); st = 0; blk = "" }
            next }
  END { print n + 0 }' ../workflows/*.yml)
# Vacuity guard: a run over zero extracted blocks would report a serene OK, which is
# this repo's signature defect. Rename the variable at all five sites and this is the
# check that goes red.
want "gate arithmetic blocks extracted from the workflows" 5 "${nblocks:-0}"
blocks=(); for b in "$TMPD"/site-*.sh; do [ -f "$b" ] && blocks+=("$b"); done

run_block() { # <block-file> <report.xml...> -> "<executed> <failed>" as that block computes them
  local blk=$1; shift
  ( xml=("$@"); xml_files=("$@")                # the two names the gates glob into
    unset -v executed total_executed failed total_failures total_errors
    . "$blk" >/dev/null                         # one site logs per file; keep it out of the answer
    printf '%s %s\n' "${executed-${total_executed-UNSET}}" \
                     "${failed-$(( ${total_failures:-0} + ${total_errors:-0} ))}" )
}
at_sites() { # <executed|failed> <expected> <label> <report.xml...>
  local fld=$1 expect=$2 label=$3; shift 3
  local n=0 blk ex fl v
  for blk in ${blocks[@]+"${blocks[@]}"}; do
    read -r ex fl < <(run_block "$blk" "$@")
    case $fld in executed) v=$ex ;; failed) v=$fl ;; esac
    if [ "$v" = "$expect" ]; then n=$((n + 1))
    else bad "in situ ${blk##*/}: $fld = $v on $label, expected $expect"; fi
  done
  want "gate sites computing $fld=$expect on $label" 5 "$n"
}
at_sites executed  0 "an all-skipped 9-of-9 suite"    $F/all-skipped.xml
at_sites executed  9 "a healthy suite"                $F/healthy.xml
at_sites executed  1 "a mostly-skipped suite"         $F/boundary.xml
at_sites executed  6 "a <testsuites>-wrapped file"    $F/nested.xml
at_sites executed 10 "a two-suite file"               $F/multi-suite-failure.xml
at_sites executed  2 "a chatty <system-out> file"     $F/decoy-system-out.xml
at_sites executed 10 "three report files at once"     $F/healthy.xml $F/all-skipped.xml $F/boundary.xml
# The same loop accumulates failures/errors, and a typo there drops a real failure
# and turns a red build green -- strictly worse than miscounting passes.
at_sites failed    1 "a failure in the SECOND suite"  $F/multi-suite-failure.xml

# The gate must also ACT on that number. Computing `executed` correctly and then
# testing `reported` would pass everything above and still ship the defect.
echo "== each gate fails on the EXECUTED count, never the reported one"
ng=$(grep -cE '^ *if \[ "\$(total_)?executed" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
nr=$(grep -cE '^ *if \[ "\$(total_)?reported" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
want "gate sites failing when zero tests EXECUTED"      5 "$ng"
want "gate sites failing on the REPORTED count instead" 0 "$nr"

# Negative control for the in-situ block. A guard whose failure path has never been
# observed is exactly what this whole sequence is about, so watch it fail: take each
# gate's REAL extracted text -- not a hand-written stand-in -- delete its `skipped`
# accumulation line, and require every site to stop reading 0 on the all-skipped
# suite. If a mutant ever comes back byte-identical the mutation matched nothing and
# proves nothing, which is itself reported as a failure.
echo "== negative control: a broken accumulation loop MUST be caught above"
nc=0
for blk in ${blocks[@]+"${blocks[@]}"}; do
  sed -E '/^[[:space:]]*(total_)?skipped=\$\(\(/d' "$blk" > "$blk.mutant"
  if cmp -s "$blk" "$blk.mutant"; then
    bad "negative control ${blk##*/}: no 'skipped' accumulation was removed, so this proves nothing"
    continue
  fi
  read -r ex _ < <(run_block "$blk.mutant" $F/all-skipped.xml)
  if [ "$ex" != 0 ]; then nc=$((nc + 1))
  else bad "negative control ${blk##*/}: dropping the 'skipped' accumulation still reads 0 -- the in-situ run does not discriminate"; fi
done
want "gate blocks where a deleted 'skipped' accumulation IS caught" 5 "$nc"

echo
[ $rc -eq 0 ] && echo "test-evidence gate arithmetic: OK" || echo "test-evidence gate arithmetic: FAILED"
exit $rc
