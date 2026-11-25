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
adb shell am instrument -w -e class 'com.example.RestartPersistenceTest#prepareFailedAttempt' \
  "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee restart-prepare-results.txt
grep -Eq '^OK \(1 test\)' restart-prepare-results.txt
adb shell am force-stop "$package_id"
adb shell am instrument -w -e class 'com.example.RestartPersistenceTest#verifyAfterProcessRestart' \
  "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee restart-verify-results.txt
grep -Eq '^OK \(1 test\)' restart-verify-results.txt
# Reboot the disposable guest and require a genuinely changed kernel boot ID.
adb shell am instrument -w -e class 'com.example.RestartPersistenceTest#prepareFailedAttempt' \
  "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee reboot-prepare-results.txt
grep -Eq '^OK \(1 test\)' reboot-prepare-results.txt
boot_before=$(adb shell cat /proc/sys/kernel/random/boot_id | tr -d '\r\n')
test -n "$boot_before"
adb reboot
timeout 180 adb wait-for-device
for attempt in $(seq 1 90); do
  if [[ "$(adb shell getprop sys.boot_completed | tr -d '\r\n')" == 1 ]]; then break; fi
  sleep 2
done
test "$(adb shell getprop sys.boot_completed | tr -d '\r\n')" = 1
boot_after=$(adb shell cat /proc/sys/kernel/random/boot_id | tr -d '\r\n')
test -n "$boot_after" && test "$boot_before" != "$boot_after"
adb shell input keyevent KEYCODE_WAKEUP
adb shell input keyevent KEYCODE_MENU
adb shell am instrument -w -e class 'com.example.RestartPersistenceTest#verifyAfterProcessRestart' \
  -e reboot true "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee reboot-verify-results.txt
grep -Eq '^OK \(1 test\)' reboot-verify-results.txt
adb shell am instrument -w -e notClass com.example.LegacyApkUpgradeTest,com.example.RestartPersistenceTest \
  "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee authentication-test-results.txt
# Android's instrumentation command may return zero even when a test fails.
# Require the complete expected suite, no failure output, and a successful summary.
if grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED' authentication-test-results.txt; then exit 1; fi
grep -Eq '^OK \(20 tests\)' authentication-test-results.txt
