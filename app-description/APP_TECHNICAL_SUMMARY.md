# Technical Summary

Based on the current repository contents. Counts are from the working tree at the time of writing.

## Identity and build configuration
| Item | Value (source) |
|---|---|
| Application ID / namespace | `com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode` (`app/build.gradle.kts`) |
| Version | `1.0.0`, versionCode 1 |
| SDK levels | minSdk 24, targetSdk 35, compileSdk 35 |
| Build tools | Gradle wrapper 8.10.2, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.28, Java 17 target, core library desugaring (java.time on API 24+) |
| Build types | `debug` (application ID suffix `.debug`); `release` (R8 minify and resource shrinking, signing from `keystore.properties` or `QR_KEYSTORE_*` variables, ad IDs from `local.properties` or `QR_ADS_*`) |
| Modules | Single `app` module |
| Size | 267 main Kotlin files, 102 unit-test files, 20 instrumented-test files |
| Orientation | Portrait (manifest); content width capped at 600 dp on tablets |

## Language and UI
- Kotlin only; UI is 100% Jetpack Compose with Material 3. The only XML is the manifest, themes/strings, vector drawables, locale and file-provider configuration.
- Custom design system (`designsystem/`): theme tokens for light and dark, the Inter font (bundled TTFs), about 40 reusable components, semantic icon map, debug-only component gallery.
- Single `MainActivity` (extends `AppCompatActivity` for per-app locales), edge-to-edge, Android SplashScreen API.

## Architecture
- MVVM with unidirectional data flow: `StateFlow<UiState>` plus one-shot effects through a `Channel`.
- Layers: `feature` (UI + ViewModels) → `domain` (pure Kotlin: models, parsers, validators, encoders, policies, use cases, repository interfaces) → `data` (Room, DataStore, repository implementations).
- Dependency injection: Hilt (14 `@HiltViewModel`s, modules for database, repositories, dispatchers, use cases, ads, billing, platform helpers).
- Navigation: Navigation Compose with type-safe `@Serializable` routes (`core/navigation/Routes.kt`); bottom bar with Create, Scan (center, start destination), History, Settings.
- Packages: `core` (navigation, ads, analytics, config, billing, permissions, di), `designsystem`, `domain`, `data`, `feature/{onboarding,scan,result,create,history,settings,system}`.

## Data and storage
- **Room** (`qr_history.db`, version 1, table `scans`): raw value, format, type, title, parsed JSON, timestamp, source (camera, image, created), favorite, note, style JSON; indices on createdAt, type, isFavorite. No destructive migration fallback.
- **Preferences DataStore**: user settings (scanning options, theme, language tag, privacy toggles) and bookkeeping (onboarding done, scan counters, prompt timestamps).
- Records are kept in memory only (negative ids) when "Save history" is off.
- Files: FileProvider cache for sharing; created-code logos copied to the app's private storage; gallery saves via MediaStore (public folder plus permission on Android 9 and older).

## Scanning and code generation
- **CameraX 1.4.1** (preview + image analysis), **ML Kit Barcode Scanning 17.3.0** (bundled model).
- **ZXing core 3.5.3** for encoding QR and barcodes; a custom pure-Kotlin vector model draws styled codes, used for the Compose preview, bitmap/PDF export and SVG export.
- Parsers for URLs, Wi-Fi, vCard/MECARD, iCalendar, geo/maps links, mailto/MATMSG, SMS, tel and product numbers; a local link-safety checker (a built-in list of more than 60 well-known brand names, punycode, TLD, shortener, IP-host and credential checks).

## Third-party services and integrations
| Integration | Use | State |
|---|---|---|
| Firebase Analytics | Event names and short enum parameters only (code contents are not logged by design) | Needs a real `google-services.json` (currently placeholder) |
| Firebase Crashlytics | Crash and non-fatal reporting | Same |
| Firebase Remote Config | Flags: `ads_enabled`, `interstitial_every_n`, `interstitial_min_interval_sec`, `native_every_n_items`, `app_open_min_away_min` | Defaults in code; needs a real project |
| Google Mobile Ads **Next Gen SDK 1.5.0** | Banner (History, Settings), interstitial, app-open, native ads | Test IDs by default; SDK use unverified |
| Google UMP | Consent form before ads; privacy options entry in Settings | Implemented; unverified on device |
| Play In-App Review 2.0.2 / In-App Update 2.1.0 | Rate and update prompts | Implemented; unverified on device |
| play-services-oss-licenses | Open-source licenses screen | Configured with plugin |
| Android Custom Tabs | Opens links and product searches | Implemented |
| External websites (opened in the browser, not called by the app) | Google, Open Food Facts, Amazon product search; scanned links | By user tap |
| Own backend / REST API / user accounts | None | Not present |
| In-app purchases | Scaffold only (no Play Billing dependency) | Placeholder |

## Network behavior (from code)
Scanning, parsing, creating, exporting and history do not require a network connection in the code. The app uses the network for ads and consent, Firebase, Play review/update, and for opening web pages the user taps. Cleartext traffic is disabled in the manifest.

## Permissions (`AndroidManifest.xml`)
| Permission | Purpose |
|---|---|
| `CAMERA` | Live scanning (the camera feature is declared optional) |
| `VIBRATE` | Haptic feedback on scan |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Ads, consent, Firebase, offline check before product search |
| `com.google.android.gms.permission.AD_ID` | Advertising ID for ads |
| `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`, `CHANGE_NETWORK_STATE` | Connect to a scanned Wi-Fi network |
| `ACCESS_FINE_LOCATION` | Requested only where Android requires it to connect to Wi-Fi (Android 10 and below paths) |
| `WRITE_EXTERNAL_STORAGE` (max SDK 28) | Save created codes to the gallery on Android 9 and older |
No storage permissions are used on newer Android versions (Photo Picker, MediaStore). The manifest also declares `<queries>` for web, phone, mail, SMS, maps, calendar, contacts, share and Custom Tabs intents, a non-exported FileProvider, and exports only `MainActivity`.

## Localization
Default English plus `values-hi`, `-es`, `-pt`, `-fr`, `-de`, `-in`, `-ar` (819 string resources, 800 of them translatable, plus 13 plurals; every translatable one is translated in all 7 languages); `locales_config.xml` enables the Android 13 per-app language picker, and `AppCompatDelegate.setApplicationLocales` handles older versions.

## Testing and verification evidence
- JVM tests: 1,097 passing in the verification harness (domain logic, parsers, validators, encoders with decode-back round trips, ViewModels, data layer, architecture rules); 827 of them run by the pure-domain Gradle check.
- Static audits all clean: DI graph, navigation routes, Room SQL against SQLite, resources/manifest, packages, resource types. ktlint clean.
- Instrumented and Hilt end-to-end tests (20 files) compile in the harness; **never executed**.
- Not done: real Gradle build, lint, R8, screenshot tests, on-device tests, monkey run, coverage measurement.
- CI (`.github/workflows/build.yml`): static analysis, unit tests, lint, debug APK, release bundle on push, non-blocking emulator tests on pull requests. **Never run.**

## Known technical risks (most likely first)
1. First real Gradle build may expose errors the harness could not (annotation processing, API drift in stubbed libraries).
2. Ads SDK 1.5.0 compatibility and API shape.
3. Lint findings are unknown.
4. Runtime behavior of CameraX/ML Kit, Wi-Fi connection, MediaStore saves and Compose layouts on real devices.
