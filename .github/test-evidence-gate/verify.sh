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
want "errored.xml executed"              6  "$(executed $F/errored.xml)"

echo "== failures AND errors must be seen in EVERY suite, not just the first"
want "multi-suite-failure.xml failures"  1  "$(sum_attr $F/multi-suite-failure.xml failures)"
want "decoy-system-out.xml failures"     0  "$(sum_attr $F/decoy-system-out.xml failures)"
# A test that BLEW UP is recorded as an error, not a failure. Until errored.xml every
# fixture here carried errors="0", so the gates' `errors` term was indistinguishable
# from its absence (Refs #243) -- the same shape as the single data point Refs #240 fixed.
want "errored.xml errors"                2  "$(sum_attr $F/errored.xml errors)"
want "errored.xml failures"              0  "$(sum_attr $F/errored.xml failures)"

echo "== gate verdicts"
for f in healthy boundary nested multi-suite-failure decoy-system-out errored; do
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
want "head -1 misses the ERRORS in the second suite"    0  "$(head1 $F/errored.xml errors)"
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
# its own guards. Nothing in THIS block is a reimplementation; the local `executed()`
# near the top of the file still is one, and is labelled as such. This tests
# semantics, not spelling: reformat the lines freely, but a gate that stops
# subtracting `skipped` reads 9 instead of 0 on all-skipped.xml.
#
# Two properties this deliberately has, both learned the hard way (Refs #240):
#
#  * SEVERAL data points, not one. The earlier version of this block ran only
#    `reported=9 skipped=9 -> 0`, which `executed=$((reported - 9))` also
#    satisfies while reporting 91 executed on a real all-skipped 100-test suite.
#    One sample can be hit by a constant; the eight run here cannot.
#  * The ACCUMULATION LOOP, not just the subtraction. `sum_attr "$f" skips` -- a
#    one-letter typo -- sums nothing, so `skipped` stays 0 and every suite reports
#    its full `tests` count as executed. That is the original bug restored, and
#    checking the `executed=` line alone cannot see it.
#
# And one property added after the fact (Refs #243), because everything above and
# every check before it inspects the COMPUTED NUMBER and nothing inspected the
# VERDICT. Deleting the bare `exit 1` from a gate's zero-executed branch left the
# arithmetic right, the condition right, the branch taken, the `::error::` printed --
# and the step GREEN, because a `::error::` annotation does not fail anything. The
# whole class of "arithmetic right, consequence removed" was invisible. So each site
# yields a SECOND, longer block that runs on past `executed=` through both guards and
# their `exit 1`s, and is judged on its exit status rather than on a variable.
# ---------------------------------------------------------------------------
echo "== each gate's own arithmetic (accumulation AND subtraction), run on the fixtures"
TMPD=$(mktemp -d) || exit 1
trap 'rm -rf "$TMPD"' EXIT

# Each gate site contributes two runnable blocks, one a prefix of the other:
#   site-NN.sh    -- its `sum_attr` definition, then the contiguous run from
#                    `reported=0` (or `total_reported=0`) through the `executed=`
#                    subtraction. The arithmetic; judged on the values it leaves set.
#   verdict-NN.sh -- all of the above PLUS the contiguous run on through the
#                    zero-executed guard and the failures/errors guard, closing on
#                    the second `fi`. The consequence; judged on its exit status.
# The outer per-module loop is not part of either, and at every site both runs are
# contiguous, so nothing has to be stitched or rewritten.
read -r nblocks nverdicts < <(awk -v outdir="$TMPD" '
  FNR == 1 { st = 0; blk = "" }
  st == 0 && /^[[:space:]]*sum_attr\(\) \{/ { st = 1; blk = $0 "\n"; next }
  st == 1 { blk = blk $0 "\n"; if ($0 ~ /^[[:space:]]*\}[[:space:]]*$/) st = 2; next }
  st == 2 && /^[[:space:]]*(total_)?reported=0/ { st = 3; blk = blk $0 "\n"; next }
  st == 3 { blk = blk $0 "\n"
            if ($0 ~ /^[[:space:]]*(total_)?executed=\$\(\(/) {
              n++; f = sprintf("%s/site-%02d.sh", outdir, n)
              printf "%s", blk > f; close(f); st = 4; nfi = 0 }
            next }
  st == 4 { blk = blk $0 "\n"
            if ($0 ~ /^[[:space:]]*fi[[:space:]]*$/ && ++nfi == 2) {
              m++; f = sprintf("%s/verdict-%02d.sh", outdir, m)
              printf "%s", blk > f; close(f); st = 0; blk = "" }
            next }
  END { print n + 0, m + 0 }' ../workflows/*.yml)
# Vacuity guard: a run over zero extracted blocks would report a serene OK, which is
# this repo's signature defect. Rename the variable at all five sites and this is the
# check that goes red. Same for the verdict blocks: make the tail stop matching and
# this reads 0, not OK. (An unmatched glob once iterated as the literal `site-*.sh`,
# hence the `[ -f ]` filter into a real array below rather than a bare `for b in`.)
want "gate arithmetic blocks extracted from the workflows" 5 "${nblocks:-0}"
want "gate verdict blocks extracted from the workflows"    5 "${nverdicts:-0}"
blocks=(); for b in "$TMPD"/site-*.sh; do [ -f "$b" ] && blocks+=("$b"); done
vblocks=(); for b in "$TMPD"/verdict-*.sh; do [ -f "$b" ] && vblocks+=("$b"); done

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
at_sites executed  6 "a suite whose tests ERRORED"    $F/errored.xml
at_sites executed 10 "three report files at once"     $F/healthy.xml $F/all-skipped.xml $F/boundary.xml
# The same loop accumulates failures/errors, and a typo there drops a real failure
# and turns a red build green -- strictly worse than miscounting passes. Both halves
# of that sum need a fixture that can tell them from zero: multi-suite-failure.xml
# pins the `failures` term, errored.xml the `errors` one. Before errored.xml existed
# every fixture reported errors="0", and deleting the `errors` term from a gate
# changed nothing anywhere in this file (Refs #243).
at_sites failed    1 "a failure in the SECOND suite"  $F/multi-suite-failure.xml
at_sites failed    2 "ERRORS in the SECOND suite"     $F/errored.xml

# The gate must also ACT on that number. Computing `executed` correctly and then
# testing `reported` would pass everything above and still ship the defect.
echo "== each gate fails on the EXECUTED count, never the reported one"
ng=$(grep -cE '^ *if \[ "\$(total_)?executed" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
nr=$(grep -cE '^ *if \[ "\$(total_)?reported" -eq 0 \]' ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
want "gate sites failing when zero tests EXECUTED"      5 "$ng"
want "gate sites failing on the REPORTED count instead" 0 "$nr"

# ---------------------------------------------------------------------------
# THE VERDICT, NOT THE VALUE (Refs #243).
#
# Everything above this line -- every fixture, every drift check, every in-situ run --
# inspects a NUMBER the gate computed. None of it inspected whether the job actually
# fails when that number is bad. Delete the bare `exit 1` from a gate's zero-executed
# branch and the gate still computes 0, still takes the branch, still prints
# `::error::Zero tests EXECUTED` -- and the step passes, because a `::error::`
# annotation is an annotation, not a failure. Measured before the fix: rc=0, OK.
#
# So run each gate's own condition AND its own exit, and judge the EXIT STATUS.
# ---------------------------------------------------------------------------
echo "== each gate's own guards, judged on the EXIT STATUS they produce"
verdict_at() { # <verdict-block> <report.xml...> -> the status that gate step would exit with
  ( mod=fixtures                                # the per-module name three sites log
    xml=("${@:2}"); xml_files=("${@:2}")        # the two names the gates glob into
    . "$1" >/dev/null ) ; echo $?
}
at_verdict() { # <pass|fail> <label> <report.xml...>
  local expect=$1 label=$2; shift 2
  local n=0 blk st
  for blk in ${vblocks[@]+"${vblocks[@]}"}; do
    st=$(verdict_at "$blk" "$@")
    if { [ "$expect" = fail ] && [ "$st" -ne 0 ]; } || { [ "$expect" = pass ] && [ "$st" -eq 0 ]; }
    then n=$((n + 1))
    else bad "in situ ${blk##*/}: exited $st on $label, expected to $expect"; fi
  done
  want "gate sites that $expect on $label" 5 "$n"
}
# The zero-executed branch...
at_verdict fail "an all-skipped 9-of-9 suite"        $F/all-skipped.xml
# ...the failures/errors branch, reached by a failure and by an error independently...
at_verdict fail "a failure in the SECOND suite"      $F/multi-suite-failure.xml
at_verdict fail "ERRORS in the SECOND suite"         $F/errored.xml
# ...and the positive controls, without which a gate that exits 1 unconditionally --
# or one that dies on an unbound variable -- would satisfy all three above.
at_verdict pass "a healthy suite"                    $F/healthy.xml
at_verdict pass "a mostly-skipped suite"             $F/boundary.xml
at_verdict pass "a chatty <system-out> file"         $F/decoy-system-out.xml

# The six remaining `exit 1`s guard "no JUnit XML at all" / "no test source tree at
# all", conditions this harness cannot reach because it SUPPLIES the file list. They
# get the same invariant statically instead, with its denominator on the line: every
# `::error::` in the workflows is a diagnosis whose consequence is the very next line.
# `::error::` also appears in the PROSE of these workflows, and a grep hit in a comment
# is not a code reference -- so match the emitting line, not the string.
E='^[[:space:]]*echo "::error::'
ne=$(grep -cE "$E" ../workflows/*.yml | awk -F: '{n+=$2} END{print n+0}')
np=$(awk -v e="$E" '$0 ~ e { if ((getline nxt) > 0 && nxt ~ /^[[:space:]]*exit 1[[:space:]]*$/) n++ }
          END { print n + 0 }' ../workflows/*.yml)
want "::error:: annotations across the four workflows" 16 "$ne"
want "...each one immediately followed by 'exit 1'"    16 "$np"

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

# Same treatment for the two assertions added in Refs #243. Same rule: mutate each
# gate's REAL extracted text, and a mutant that comes back byte-identical is reported
# as a failure rather than credited.
echo "== negative control: a dropped 'errors' term MUST be caught above"
ne1=0
for blk in ${blocks[@]+"${blocks[@]}"}; do
  # Two shapes, one meaning: four sites add the term inline to `failed=$((...))`,
  # samples-minimal.yml accumulates it on its own `total_errors=` line.
  sed -E -e 's/[[:space:]]*\+[[:space:]]*\$\(sum_attr "\$f" errors\)//' \
         -e '/^[[:space:]]*total_errors=\$\(\(total_errors \+ er\)\)[[:space:]]*$/d' \
         "$blk" > "$blk.noerr"
  if cmp -s "$blk" "$blk.noerr"; then
    bad "negative control ${blk##*/}: no 'errors' term was removed, so this proves nothing"
    continue
  fi
  read -r _ fl < <(run_block "$blk.noerr" $F/errored.xml)
  if [ "$fl" != 2 ]; then ne1=$((ne1 + 1))
  else bad "negative control ${blk##*/}: dropping the 'errors' term still reads 2 -- errored.xml does not discriminate"; fi
done
want "gate blocks where a dropped 'errors' term IS caught" 5 "$ne1"

echo "== negative control: a deleted 'exit 1' MUST be caught above"
nv=0
for blk in ${vblocks[@]+"${vblocks[@]}"}; do
  sed -E '/^[[:space:]]*exit 1[[:space:]]*$/d' "$blk" > "$blk.noexit"
  if cmp -s "$blk" "$blk.noexit"; then
    bad "negative control ${blk##*/}: no 'exit 1' was removed, so this proves nothing"
    continue
  fi
  # One fixture per guard, so each branch is isolated: all-skipped.xml takes only the
  # zero-executed branch, errored.xml only the failures/errors one. Arithmetic and
  # condition are untouched in both; only the consequence is gone.
  z=$(verdict_at "$blk.noexit" $F/all-skipped.xml)
  e=$(verdict_at "$blk.noexit" $F/errored.xml)
  if [ "$z" -eq 0 ] && [ "$e" -eq 0 ]; then nv=$((nv + 1))
  else bad "negative control ${blk##*/}: deleting 'exit 1' still exits $z/$e -- the verdict run does not discriminate"; fi
done
want "gate blocks where a deleted 'exit 1' IS caught" 5 "$nv"

echo
[ $rc -eq 0 ] && echo "test-evidence gate arithmetic: OK" || echo "test-evidence gate arithmetic: FAILED"
exit $rc
