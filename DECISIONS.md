# Decisions

Non-obvious choices made while building (append new entries at the end).

1. **Package id** stays `com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode` (set by the owner when the template was copied); the PRD's suggested `com.app.vv.qrscanner` is not used.
2. **Gradle wrapper 8.10.2**: the template shipped Gradle 9.0.0 with AGP 8.7.3, which is incompatible. 8.10.2 is what its README named and is supported by AGP 8.7.3.
3. **Domain stays pure Kotlin** inside the single `app` module (packages, not Gradle modules, per the PRD) so parsers, validators, link safety and generators run as plain JVM unit tests.
4. **History off = transient records.** When "Save history" is off, scans/creations still get a `ScanRecord` with a negative id held in memory so Result/Generated screens work uniformly; nothing is written to disk.
5. **Created codes are inserted into history immediately** on "Generate" (source CREATED) and the style editor updates that row (`updateStyle`); `Generated(recordId)` is the only argument passed between Create screens.
6. **Parsing works on the raw payload text** (not ML Kit value types) so it is testable on the JVM and identical for scanned and created codes; ML Kit only supplies `rawValue` + format.
7. **Per-feature string files** (`strings_<feature>.xml`, prefixed names) to avoid merge conflicts while features were built in parallel.
8. **Icons**: Material Symbols Rounded equivalents from `material-icons-extended` (`Icons.Rounded.*`) rather than hand-converted SVG; the app launcher icon is a hand-written vector based on the Brand artboard.
9. **Per-app language** uses `AppCompatDelegate.setApplicationLocales` (works on API 24+ via AppCompat) with `res/xml/locales_config.xml` for the Android 13 system picker; `MainActivity` extends `AppCompatActivity`.
10. **System prompts host** hides all prompts on Result, Onboarding, CameraRationaleOnboarding and Feedback; the rate sheet always goes before the update dialog, and the update dialog is held back while scanning. Prompts appear after a 1.5 s settle delay.
11. **Play review/update** use the Java `Task` APIs (not the ktx extensions) to avoid depending on extension package names that could not be verified offline.
12. **Parser choices**: map links become `Location` only when the coordinates are the point; unusual schemes (`javascript:`, `data:`) parse as Text (no Open action) while the safety checker still flags them UNSAFE; a valid IANA `TZID` in iCalendar is honoured; all-day `DTEND` is treated as exclusive.
13. **Link-safety brand matching** is stricter than a plain edit distance of 2 for short brand names (0 typos up to 3 letters, 1 up to 6) to avoid false positives such as `finance.com`.
14. **Core library desugaring** is enabled (desugar_jdk_libs 2.1.3) so java.time works on minSdk 24.
15. **Ads**: Google Mobile Ads Next Gen SDK 1.5.0 (version confirmed from Google's sample repo, release notes could not be opened); every SDK call is isolated in `core/ads/NextGenAdsGateway.kt`. UMP consent is gathered from `AdManager.controller(activity)`; the SDK is never initialised before consent. App-open bookkeeping uses `ActivityLifecycleCallbacks` (no lifecycle-process dependency). `Analytics.setEnabled` also toggles Crashlytics.
16. **Release ad ids** come from `local.properties` (`ads.*`) or `QR_ADS_*` environment variables and fall back to Google's test ids so CI release builds still run; the release checklist in `PLAY_STORE.md` must be completed before publishing.
17. **Build-compat risk**: Google's SDK sample builds with Kotlin 2.1.0 / compileSdk 36 while this repo is on Kotlin 2.0.21 / compileSdk 35 / AGP 8.7.3. If the first real build complains, raise those together (AGP 8.9+, Kotlin 2.1.x with matching KSP).
18. **Scan area only** filters detections by the centre of their bounding box against the viewfinder frame instead of cropping the camera buffer (simpler, no per-frame bitmap work). The duplicate-suppression window slides (a code held in view stays suppressed until it has been gone for 2 s).
19. **Batch scan** saves each code as it is collected so rows can open Result; "Finish batch" clears the list. The photos rationale is shown once per process (the Photo Picker itself needs no permission).
