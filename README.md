# QR Code Scanner and Generator

Offline-first Android app: scan QR codes and barcodes with the camera or from an image, act on the result by type
(links, Wi-Fi, contacts, calendar events, phone, email, SMS, locations, products), create styled QR codes and
barcodes, and keep a searchable history with favorites. No account, no backend. Free, supported by ads.

Package: `com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode` · min SDK 24 · target/compile SDK 35.

## Stack
Kotlin, Jetpack Compose + Material 3, single Activity, Navigation Compose (type-safe routes), MVVM with
`StateFlow<UiState>` + one-shot effects, Hilt, Room, Preferences DataStore, CameraX + ML Kit Barcode Scanning,
ZXing (encoding), kotlinx.serialization, Firebase (Analytics, Crashlytics, Remote Config), Google Mobile Ads
Next Gen SDK + UMP consent, Play In-App Review / Updates.

## Layout
```
app/src/main/java/<package>/
  core/           navigation (Routes, AppNav), ads, analytics, config, billing scaffold, permissions UI, DI
  designsystem/   theme (tokens, Inter), components, icons, debug Component Gallery
  domain/         PURE Kotlin: models, parser, link safety, create (payloads, validators, encoders, vector renderer),
                  history/scan/result/system/settings/ads logic, repositories (interfaces), use cases
  data/           Room (history), DataStore (settings), repository implementations
  feature/        onboarding, scan, result, create, history, settings, system (rate/update prompts)
```
Design source of truth: the "QR Code Scanner and Generator" Design artifact (77 phone artboards). Product spec: `PRD.md`.
Repo conventions: `CLAUDE.md`. Non-obvious choices: `DECISIONS.md`. Store listing: `PLAY_STORE.md`. Ads setup: `docs/ADS_SETUP.md`.

## Build
```
./gradlew assembleDebug          # debug APK (applicationId has a .debug suffix)
./gradlew testDebugUnitTest      # JVM unit tests (parsers, validators, ViewModels, ...)
./gradlew ktlintCheck detekt lintDebug
./gradlew bundleRelease          # R8 + (optionally signed) release bundle
```
Requires JDK 17 and the Android SDK (API 35). The Gradle wrapper is 8.10.2 with AGP 8.7.3.

## Before publishing
- Replace `app/google-services.json` with the file from your Firebase project (package must match, plus `.debug`).
- Real AdMob ids: `local.properties` keys `ads.appId`, `ads.bannerId`, `ads.interstitialId`, `ads.appOpenId`, `ads.nativeId`
  (or `QR_ADS_*` environment variables). Without them release builds use Google's TEST ids. See `docs/ADS_SETUP.md`.
- Hosted privacy policy URL in `privacy_policy_url`; support e-mail and terms URL in `strings_settings.xml`.
- Signing: `keystore.properties` (see `keystore.properties.example`) or `QR_KEYSTORE_FILE`, `QR_KEYSTORE_PASSWORD`,
  `QR_KEY_ALIAS`, `QR_KEY_PASSWORD`. Never commit keystores.
- Complete the checklist in `PLAY_STORE.md` (Data safety, content rating, screenshots).

## Languages
English, Hindi, Spanish, Portuguese (BR), French, German, Indonesian, Arabic (per-app language picker, `locales_config.xml`).
