#!/usr/bin/env bash
# Runs inside android-emulator-runner (emulator already booted).
# Runs the instrumentation tests, then launches the debug app and captures screenshots.
# Output goes to ci-artifacts/ (screenshots/*.png, *.txt, *.log) for the workflow to upload.
set -u

APP_ID="com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.debug"
OUT="ci-artifacts"
SHOTS="$OUT/screenshots"
mkdir -p "$SHOTS"

shot() {
  local name="$1"
  adb exec-out screencap -p > "$SHOTS/$name.png" || echo "screencap $name failed"
}

adb devices -l | tee "$OUT/adb-devices.txt"
adb shell getprop ro.build.version.sdk | tee "$OUT/device-sdk.txt"

echo "== Instrumentation tests"
./gradlew connectedDebugAndroidTest --stacktrace 2>&1 | tee "$OUT/instrumentation.log"
TEST_STATUS=${PIPESTATUS[0]}

echo "== Launch debug app and capture screenshots"
adb install -r -g app/build/outputs/apk/debug/*.apk 2>&1 | tee "$OUT/install.log" || true
adb logcat -c || true
adb shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 > "$OUT/launch.log" 2>&1 || true
sleep 8
shot 01-launch
adb shell input tap 540 2000 || true
sleep 3
shot 02-after-tap
adb logcat -d -t 2000 > "$OUT/logcat.txt" 2>&1 || true

echo "instrumentation exit status: $TEST_STATUS" | tee "$OUT/status.txt"
exit "$TEST_STATUS"
