#!/usr/bin/env bash
set -euo pipefail

# Run the same real AndroidJUnitRunner suite, with direct APK installation.
# The dedicated upgrade test requires pre-existing legacy data and runs separately.
package_id=com.aistudio.privacylock.pnvxld
adb shell df -h /data
adb install test-fixture/build/outputs/apk/debug/test-fixture-debug.apk
adb install app/build/outputs/apk/opensource/debug/app-opensource-debug.apk
adb install app/build/outputs/apk/androidTest/opensource/debug/app-opensource-debug-androidTest.apk
trap 'adb logcat -d -s ForegroundProtectionTest:I > foreground-test-log.txt || true' EXIT
adb shell am instrument -w -e notClass com.example.LegacyApkUpgradeTest \
  "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee authentication-test-results.txt
# Android's instrumentation command may return zero even when a test fails.
# Require the complete expected suite, no failure output, and a successful summary.
if grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED' authentication-test-results.txt; then exit 1; fi
grep -Eq '^OK \(20 tests\)' authentication-test-results.txt
