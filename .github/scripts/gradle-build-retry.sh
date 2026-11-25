#!/usr/bin/env bash
set -euo pipefail

# Retry only a recognized remote artifact-resolution failure; any actual code,
# lint, test or signing failure is returned immediately. No check is skipped.
log=$(mktemp)
trap 'rm -f -- "$log"' EXIT
for attempt in 1 2 3; do
  if gradle "$@" 2>&1 | tee "$log"; then exit 0; fi
  if [[ "$attempt" == 3 ]] || ! grep -Eq 'Plugin \[id: .org\.jetbrains\.kotlin\.plugin\.compose.*was not found|could not resolve plugin artifact.*org\.jetbrains\.kotlin\.plugin\.compose' "$log"; then
    exit 1
  fi
  printf 'Retrying official Kotlin Compose artifact resolution (%s/3)\n' "$attempt"
  sleep 10
done
