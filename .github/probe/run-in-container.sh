#!/bin/sh
# Runs INSIDE each distro container (via docker run) for the portability probe.
# Installs an LLVM (per $LLVM_INSTALL), then checks whether the prebuilt
# cppfrontend binary at /probe/cppfrontend can dynamically load + start.
#
# Emits exactly one machine-readable line: `RESULT=<verdict> ...` where verdict is
#   PASS-loads        binary resolves all NEEDED libs and starts (no loader error)
#   FAIL-load         a NEEDED shared lib is unresolved (SONAME/version mismatch)
#   FAIL-runtime      resolved by ldd but the loader still errors at exec
#   install-failed    couldn't install the requested LLVM on this image
# plus diagnostic context (which libLLVM/libclang-cpp SONAMEs the image provides).
set -u
BIN=/probe/cppfrontend

echo "== image: $(. /etc/os-release 2>/dev/null; echo "${PRETTY_NAME:-unknown}") =="
echo "== installing LLVM: ${LLVM_INSTALL} =="
if ! sh -c "${LLVM_INSTALL}"; then
  echo "RESULT=install-failed image=${IMAGE_NAME}"
  exit 0
fi

echo "== glibc / libstdc++ floor on this image =="
ldd --version 2>&1 | head -1 || true
strings "$(find /usr/lib* -name 'libstdc++.so.6*' 2>/dev/null | head -1)" 2>/dev/null \
  | grep -oE 'GLIBCXX_[0-9.]+' | sort -V | tail -1 || true

echo "== libLLVM / libclang-cpp SONAMEs this image provides =="
found_dir=""
for d in /usr/lib /usr/lib64 /usr/lib/x86_64-linux-gnu $(ls -d /usr/lib/llvm*/lib 2>/dev/null); do
  hits=$(ls -1 "$d"/libLLVM*.so* "$d"/libclang-cpp*.so* 2>/dev/null || true)
  if [ -n "$hits" ]; then echo "$hits"; [ -z "$found_dir" ] && found_dir="$d"; fi
done
# Put the discovered LLVM libdir on the loader path (versioned dirs like
# /usr/lib/llvm-22/lib aren't on the default search path).
export LD_LIBRARY_PATH="${found_dir}:${LD_LIBRARY_PATH:-}"

echo "== ldd =="
ldd "$BIN" 2>&1 | grep -iE 'llvm|clang|not found' || true
if ldd "$BIN" 2>&1 | grep -qiE 'not found'; then
  echo "RESULT=FAIL-load image=${IMAGE_NAME} (unresolved NEEDED — SONAME/version mismatch)"
  exit 0
fi

# Resolved by ldd — now confirm the loader actually starts it.
out=$("$BIN" --help 2>&1 || true)
if echo "$out" | grep -qi 'error while loading shared libraries'; then
  echo "RESULT=FAIL-runtime image=${IMAGE_NAME}"
  echo "$out" | grep -i 'error while loading' | head -1
else
  echo "RESULT=PASS-loads image=${IMAGE_NAME}"
fi
