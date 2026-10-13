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
