#!/usr/bin/env bash
# THE "assert the tests actually ran" gate -- the one implementation every workflow calls
# (#247; it used to be five hand-copied blocks that had drifted three ways). verify.sh runs
# this file on the fixtures beside it, and checks that every workflow site still calls it.
#
#   gate.sh clear  MODULE...   before the Gradle step: delete each module's JUnit XML
#   gate.sh assert MODULE...   after it: each module must have EXECUTED >0 tests, none failed
#
# Each word of each argument is a module directory, or a glob of paths inside modules
# (`compiler/*/src/test`): the module is everything before `/src/`. A glob is DISCOVERY --
# a new module is gated without anyone listing it (#231) -- and a word matching nothing
# fails closed.
#
# Delete-before-measure (#198): a task that does not run leaves the previous run's report
# exactly where `assert` looks, so "never ran" and "ran and passed" read the same on disk.
#
# PER MODULE, never summed (#230): a grand total stays green when one module's report
# vanishes, as long as another module still reports.
set -euo pipefail
shopt -s globstar nullglob

die() { echo "::error::$*"; exit 1; }

# `tests=` COUNTS SKIPPED TESTCASES: an all-skipped suite reports a healthy tests="N" with
# failures=0 errors=0, having measured nothing (Refs #198, #222, #230). So executed is
# `tests - skipped`, both read off the same <testsuite> element.
#
# Sum EVERY <testsuite> rather than `head -1`, which dropped a failure in a second suite and
# turned a red build green. Reading only the `<testsuite ...>` start tag keeps attribute-like
# text in <system-out> from counting, and the literal space after `testsuite` excludes a
# `<testsuites>` wrapper, whose totals would be added on top of the children they summarise.
sum_attr() { # <file> <attr> -> summed over every <testsuite> element in the file
  grep -o '<testsuite [^>]*>' "$1" | grep -o " $2=\"[0-9]*\"" \
    | grep -o '[0-9]*' | awk '{ n += $1 } END { print n + 0 }' || true
}

modules() { # <MODULE...> -> the module directories in MODS, fail-closed on an empty match
  local arg p d n words
  MODS=()
  # Split into WORDS first: the workflows pass "$TEST_MODULES" as one space-joined argument,
  # and every word in it must match on its own, or a typo'd one would be silently dropped.
  for arg in "$@"; do
    read -ra words <<< "$arg"
    for p in "${words[@]}"; do
      n=0
      # shellcheck disable=SC2086 # deliberately unquoted: $p is a glob
      for d in $p; do [ -d "$d" ] && { MODS+=("${d%%/src/*}"); n=$((n + 1)); }; done
      [ "$n" -gt 0 ] || die "No module matches '$p'. Either the layout moved (update the workflow) or the tests are gone."
    done
  done
  [ ${#MODS[@]} -gt 0 ] || die "test-evidence gate: no modules given."
}

clear_reports() {
  local mod
  modules "$@"
  for mod in "${MODS[@]}"; do rm -rf "$mod/build/test-results"; done
}

assert_ran() {
  local mod f xml reported skipped failed executed grand=0
  modules "$@"
  echo "gating on ${#MODS[@]} module(s): ${MODS[*]}"
  for mod in "${MODS[@]}"; do
    xml=("$mod"/build/test-results/**/*.xml)
    [ ${#xml[@]} -gt 0 ] || die "No JUnit XML under $mod/build/test-results. That module's tests cannot have run, so this job would be green having skipped them."
    reported=0; skipped=0; failed=0
    for f in "${xml[@]}"; do
      reported=$((reported + $(sum_attr "$f" tests)))
      skipped=$((skipped + $(sum_attr "$f" skipped)))
      failed=$((failed + $(sum_attr "$f" failures) + $(sum_attr "$f" errors)))
    done
    executed=$((reported - skipped))
    echo "$mod: $executed test(s) executed ($reported reported, $skipped skipped), $failed failure(s)/error(s) across ${#xml[@]} report file(s)"
    [ "$executed" -gt 0 ] || die "$mod EXECUTED zero tests ($reported reported, $skipped skipped). This job would otherwise be green having run nothing."
    [ "$failed" -eq 0 ] || die "$mod reported $failed test failure(s)/error(s) in its JUnit XML."
    grand=$((grand + executed))
  done
  echo "TOTAL: $grand test(s) executed across ${#MODS[@]} module(s)"
}

# Sourced (by verify.sh, for sum_attr), define only; executed, dispatch.
if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  cmd=${1-}; shift || true
  case $cmd in
    clear) clear_reports "$@" ;;
    assert) assert_ran "$@" ;;
    *) die "usage: gate.sh clear|assert MODULE..." ;;
  esac
fi
