#!/usr/bin/env bash
set -euo pipefail

package_id=com.aistudio.privacylock.pnvxld
adb install legacy-fixture/build/outputs/apk/debug/legacy-fixture-debug.apk
adb shell am start -W -n "$package_id/com.example.legacyfixture.LegacyFixtureActivity"
for attempt in $(seq 1 30); do
  if adb shell run-as "$package_id" ls databases/privacy_lock_db >/dev/null 2>&1; then break; fi
  sleep 1
done
adb shell run-as "$package_id" ls databases/privacy_lock_db
adb shell am force-stop "$package_id"
adb install -r app/build/outputs/apk/opensource/debug/app-opensource-debug.apk
adb shell am start -W -n "$package_id/com.example.MainActivity"
adb install app/build/outputs/apk/androidTest/opensource/debug/app-opensource-debug-androidTest.apk
adb shell am instrument -w -e class com.example.LegacyApkUpgradeTest "$package_id.test/androidx.test.runner.AndroidJUnitRunner" | tee upgrade-test-results.txt
grep -F 'OK (1 test)' upgrade-test-results.txt
