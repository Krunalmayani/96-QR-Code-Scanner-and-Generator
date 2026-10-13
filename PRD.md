# QR Code Scanner and Generator — PRD + Claude Code build prompt

Copy everything below the line into Claude Code, at the root of your new project (created from your Android template repo). Put a copy of this file in the repo as `PRD.md` and a `CLAUDE.md` with the "Rules" section so every session follows it.

---

## ROLE

You are a senior Android engineer. Build the production-ready Android app **"QR Code Scanner and Generator"** end to end, in the order of the milestones below. After each milestone: build, run unit tests, fix errors, commit with a clear message, then continue. Do not stop to ask questions; choose the sensible default and note it in `DECISIONS.md`.

## 1. Product summary

Offline-first Android app to scan QR codes and barcodes with the camera or from an image, act on the result by type, create styled QR codes and barcodes, and keep a searchable history with favorites. No account, no backend. Free, supported by ads via Google Mobile Ads Next Gen SDK (see section 9).

Target users: everyday users (menus, Wi-Fi, payments links, contacts) and shoppers (barcode lookup).

## 2. Tech stack and architecture

- Language: Kotlin only. Min SDK 24, target/compile SDK latest stable.
- UI: Jetpack Compose + Material 3, single Activity, Navigation Compose (type-safe routes with kotlinx.serialization).
- Architecture: MVVM + unidirectional data flow (UiState / UiEvent / UiEffect per screen), Clean Architecture layers: `ui` → `domain` (use cases, models) → `data` (repositories, Room, DataStore, ML Kit).
- DI: Hilt. Async: Coroutines + Flow. Build: Gradle version catalog (`libs.versions.toml`), KSP.
- Camera: CameraX (Preview + ImageAnalysis). Decoding: ML Kit Barcode Scanning (bundled model, works offline). Generation: ZXing core for encoding; custom Compose/Canvas renderer for styled QR (dot shapes, colors, logo).
- Storage: Room (history), Preferences DataStore (settings). Images: Coil only if needed.
- Testing: JUnit5/Turbine/MockK for ViewModels and use cases, Room in-memory tests, Compose UI tests for key flows.
- Package: `com.app.vv.qrscanner` (change if the template already sets one). Modules: single `app` module with packages `core`, `feature/{onboarding,scan,result,create,history,settings}`, `data`, `domain`, `designsystem`.

## 3. Design source of truth

The full design is the Design artifact "QR Code Scanner and Generator": one canvas page, "All screens", with 77 phone artboards (360x800, named `s01` to `s77`, listed in section 13) plus a design system sheet and an app icon / Play graphic board. Implement it faithfully:

- Material 3, font Inter (bundle via downloadable fonts or res/font), 8dp grid, 16 to 24dp corner radius, rounded 24dp outline icons (Material Symbols Rounded).
- Light tokens: background #F5F6FC, surface #FFFFFF, surface variant #ECEEF9, ink #13142B, secondary text #575B78, outline #E1E3F1, primary #4F46E5, primary container #E7E5FF (on-container #2B2594), success/teal #0F9F8E (container #D3F4EE), scan-hit #34E0C5, error #C62828 (container #FDE4E4), warning #9A5B00 (container #FFF0D1).
- Dark tokens: background #0D0E1B, surface #171829, surface-2 #212340, ink #F1F2FF, secondary #A6AAC8, outline #2A2C4A, primary #A5A0FF (on-primary #14123F), primary container #2D2A78, success #2DD4BF (container #10403B), error #FF8A80, warning #FFC56B.
- Build a `designsystem` package first: theme (light/dark/system), typography, shapes, and reusable components: PrimaryButton, TonalButton, OutlinedButton, TextButton, IconButtonRounded, ChipRow, SegmentedControl, SearchBar, ListItem with leading icon tile, Card, SettingsRow (switch/radio/chevron), TextField with error/helper, BottomNavBar with raised center Scan button, BottomSheet, ConfirmDialog, Snackbar/Toast host, EmptyState, ViewfinderOverlay.
- Bottom nav: Create, **Scan (center, raised, start destination)**, History, Settings. Favorites is a filter chip inside History.

## 4. Screens and behavior (build every one)

### 4.1 Onboarding
- Splash (logo, 1s, Android 12 SplashScreen API). 3 onboarding slides (Scan fast, Create anything, Private by design) with page dots, Skip, Next/Get started. Shown once (DataStore flag). Camera permission rationale screen after onboarding.

### 4.2 Scan
- Live CameraX preview with viewfinder overlay, animated scan line, corner brackets that turn teal on detection.
- Controls: flash toggle, zoom slider/pinch, switch camera, scan-from-image button, mode chip (QR / Barcode / All).
- On detect: vibrate (if enabled), beep (if enabled), freeze frame, navigate to Result (or show quick bottom sheet if "Continue scanning" is on).
- Batch scan mode: collects multiple codes into a list with count badge, "Done" opens a list sheet.
- Permission states: first-time rationale dialog, denied (explain + Request again), permanently denied (Open settings). Handle lifecycle, rotation locked to portrait.
- Debounce duplicate detections (same value within 2s).

### 4.3 Scan from image
- Use the Android Photo Picker (no storage permission on API 33+; READ_MEDIA_IMAGES fallback not needed because the picker works back to API 21 via backport). Show picked image, "Scanning…" state, success (go to Result), and "No code found" with Try another image. Support multiple codes in one image (list to choose from).

### 4.4 Result screens (by parsed type)
Parse with ML Kit value types into a sealed `ScanContent`:
- **Website:** show URL, secure/not secure badge, actions Open link (Custom Tabs), Copy, Share, Favorite. Run local safety checks (http not https, lookalike of well-known brands via edit distance, punycode, suspicious TLDs, URL shorteners) and show the Unsafe link sheet with "Go back to safety / Copy link instead / Open anyway".
- **Text**, **Contact (vCard/MECARD)** with Add to contacts, Call, Email, **Phone** (Dial, Add contact, Message), **Email** (Compose), **SMS** (Compose), **Geo** (Open in Maps), **Calendar event** (Add to calendar), **Wi-Fi** (Connect using WifiNetworkSuggestion/ WifiNetworkSpecifier, states: connecting, connected, could not connect with copy password, location explainer sheet when required), **Product barcode** (EAN/UPC: show number, "Search on" list: Google, Open Food Facts, Amazon; disclaimer "Results come from external websites"; offline state with Try again), **Unknown/raw** (hex/text view, format shown).
- Every result auto-saves to history unless history is off. Header shows type badge + timestamp.

### 4.5 Create
- Hub: search + grid/list of types: Website, Text, Contact, Wi-Fi, Email, Phone, SMS, Location, Event, Social profile (Instagram, Facebook, X, LinkedIn, YouTube, WhatsApp), App Store link, and Barcode formats (EAN-13, EAN-8, UPC-A, Code 128, Code 39, ITF, PDF417, Data Matrix, Aztec).
- Each form: validated fields with inline errors (URL format, E.164-ish phone, email, SSID/password/security WEP/WPA/none with hidden toggle, date-time pickers for events, lat/long picker). Primary "Generate" disabled until valid. Barcode forms validate length and check digit.

### 4.6 Generated code and editor
- Large preview, content summary, actions: Save to gallery (MediaStore, no permission on API 29+), Share image, Copy content, Add to favorites.
- Customize tabs: **Colors** (foreground/background/gradient presets, contrast warning when scan reliability is low), **Shapes** (square, rounded, dots, eye shapes), **Extras** (center logo from gallery, frame with text like "Scan me", error correction L/M/Q/H, export size 512 to 2048, PNG/JPG/SVG/PDF).
- Barcode variant: show number under bars toggle, height.
- Created codes saved in History › Created.

### 4.7 History
- Tabs Scanned / Created; filter chips All, Favorites, Links, Wi-Fi, Contacts, etc.; search; grouped by day.
- Item: icon tile by type, title, subtype, time, star. Tap opens Detail. Long-press enters multi-select (delete, favorite, share, export). Swipe to delete with Undo snackbar.
- Detail: the full result actions again, edit label/note, favorite, delete with confirm dialog. Empty states for each tab and for Favorites and search.
- Clear all history in Settings (dialog states counts, "Keep my favorites" checkbox).

### 4.8 Settings
- Scanning: vibrate, beep, flash on start, auto-open links (off by default), continuous scan, scan area/full-screen, product site order.
- Appearance: theme (System/Light/Dark), language (per-app locales via `LocaleManager`/AppCompat, list at least: English, Hindi, Spanish, Portuguese, French, German, Indonesian, Arabic).
- Privacy and data: save history toggle, clear history, export history (CSV/JSON/Text via SAF/share), privacy policy summary screen.
- About and support: Help and FAQ (accordion), Send feedback (stars, topic chips, text, optional email → mailto intent), Rate on Google Play (Play In-App Review), Share app, Open source licenses (play-services-oss-licenses), Terms, version.

### 4.9 System states and dialogs
Camera/photo/location rationale dialogs, delete and clear confirmations, no-internet inline state, rate-us sheet (after the 5th successful scan, max once per 60 days, never during scanning; 4 to 5 stars → in-app review, 1 to 3 → feedback form), app update prompt (Play In-App Updates flexible), loading and error states everywhere, snackbar/toast host.

## 5. Data model (Room)

`ScanEntity(id, rawValue, format, type, displayTitle, parsedJson, createdAt, source[CAMERA|IMAGE|CREATED], isFavorite, note, styleJson?)`. Indices on createdAt, type, isFavorite. DAO with Flow queries: all, by source, by type, search (LIKE on title/raw/note), favorites. Migration tests from v1.

## 6. Non-functional requirements

- Cold start under 1.5 s; scan detection under 500 ms in good light; smooth 60 fps UI; APK/AAB under 25 MB.
- Fully offline for scanning, creating and history. Network only for optional product search (external browser) and ads.
- Accessibility: TalkBack labels, 48dp targets, 4.5:1 contrast, font scale to 200%, no color-only meaning.
- Privacy: no images or scan content leave the device; no analytics of scan contents. Data Safety form notes in `PLAY_STORE.md`.
- Security: no `usesCleartextTraffic`, R8/ProGuard on, no exported components except launcher, safe handling of intents from scanned content (never auto-execute; require tap).
- Orientation: portrait phones; tablets use centered max-width 600dp.

## 7. Permissions

CAMERA (runtime). VIBRATE. INTERNET, ACCESS_NETWORK_STATE (ads/lookup). ACCESS_FINE_LOCATION only when needed for Wi-Fi connect on affected versions (request in context). No storage permissions (Photo Picker, MediaStore insert). Declare `<queries>` for intents used.

## 8. Analytics and quality

- Firebase Crashlytics + Analytics with **event names only** (scan_success, create_success, share_tap, etc.), never content. Add a consent-friendly toggle placeholder.
- Timber logging in debug. Detekt/ktlint and Android Lint in CI. GitHub Actions: build, unit tests, lint on PR.

## 9. Monetization (Google Mobile Ads Next Gen SDK)

- Adaptive banner on History and Settings only; never over the camera.
- Interstitial after every 4th completed scan or create, with a 90s minimum interval, never on first session, never before the result is visible.
- App-open ad on return from background after 30 min, not on cold start before onboarding is done.
- Native ad in History list every 8 items. Wrap in an `AdManager` behind an interface so ads can be disabled by a build flag and a remote flag. Use test ad unit IDs in debug and put real IDs in `local.properties`/BuildConfig placeholders. Include UMP consent flow.
- Optional later: "Remove ads" one-time purchase screen (Play Billing) — scaffold only, off by default.

## 10. Milestones (do in order, commit after each)

1. **Project setup:** check the template, package name, version catalog, Hilt, Navigation, theme, CI, lint config. App launches to an empty Scan placeholder with bottom nav.
2. **Design system:** tokens, typography, all shared components, a debug-only Component Gallery screen, light/dark previews.
3. **Core scan:** CameraX + ML Kit, permission flow, scan overlay, flash/zoom, duplicate debounce, basic Result.
4. **Parsing and result screens:** sealed `ScanContent`, parsers + unit tests (a table of at least 40 sample payloads), all result UIs and actions, link safety checker + tests, Wi-Fi connect.
5. **Scan from image + batch mode.**
6. **Database and History:** Room, repositories, history UI with search, filters, multi-select, favorites, swipe delete + undo, detail.
7. **Create:** hub, all forms with validation, generator domain (ZXing + styled renderer), editor tabs, save/share/export.
8. **Settings and sub-screens:** DataStore settings, theme, per-app language, privacy, export, help, feedback, about, licenses.
9. **System polish:** onboarding, rationale dialogs, empty/error/offline states, rate sheet, in-app update, haptics/sound, animations noted in the design.
10. **Monetization and analytics:** ads behind `AdManager`, UMP, Firebase, remote flags.
11. **Hardening:** accessibility pass, R8 release build, baseline profile, Compose UI tests for scan→result→history and create→save, instrumented tests on API 24 and 35 emulators, screenshot tests for key screens in light/dark.
12. **Design parity check:** go through the section 13 inventory one artboard at a time, compare each screen to its artboard in light and dark, and fix differences in spacing, color, copy and states.
13. **Release prep:** icon (adaptive + monochrome; design reference "App icon" board), feature graphic notes, `PLAY_STORE.md` (title, short and full description, Data Safety answers, content rating), signing config via env vars, versioning, release AAB.

## 11. Acceptance criteria (definition of done)

- Every screen in section 4 and every artboard in section 13 exists, matches the design in light and dark, and has loading, empty, error and permission states where applicable.
- Scanning works for QR, EAN-13/8, UPC-A/E, Code 128/39/93, ITF, Codabar, PDF417, Data Matrix, Aztec on a real device; from image works for rotated and low-contrast images.
- Every result type triggers the correct system intent and never auto-runs without a tap (unless the user enabled auto-open links).
- Generated codes scan back correctly with the app itself (add an automated round-trip test for each type).
- History persists across restarts; delete, undo, favorite, export and clear-all behave as specified.
- App is fully usable in airplane mode (except external lookups and ads).
- Unit test coverage on parsers, safety checker, validators and use cases ≥ 80%; CI green; `./gradlew assembleRelease` succeeds with R8; no lint errors.
- No crashes or ANRs in a 30-minute monkey run.

## 12. Rules for this repo (put these in CLAUDE.md)

- Kotlin only, Compose only (no XML layouts except manifest/themes). Prefer stateless composables with hoisted state; previews for every component.
- Never put business logic in composables; ViewModels expose `StateFlow<UiState>` and one-shot `UiEffect`s via `Channel`.
- No hard-coded strings or colors in UI code: use string resources (so localization works) and theme tokens.
- Each parser, validator and use case gets unit tests before the UI that depends on it.
- Keep functions small, name by intent, no TODOs left in merged code; record non-obvious choices in `DECISIONS.md`.
- Commit after every milestone with a conventional message; keep the build green.

## 13. Screen inventory (maps 1:1 to the design canvas "All screens")

Every artboard in the design is named `NN Section · Screen`. Implement each as a Compose screen or sheet/dialog with a `@Preview` (light and dark) named after the artboard number. Use the artboard titles as the source of truth for state coverage; dark-mode artboards (section 08) are previews of the same screens, not separate screens.


**01 Onboarding**
- `s01` Splash
- `s02` Slide 1, scan
- `s03` Slide 2, create
- `s04` Slide 3, privacy
- `s05` Camera permission rationale

**02 Scan**
- `s06` Live camera, QR mode
- `s07` Barcode mode, flash on
- `s08` Scan success, quick result sheet
- `s09` Camera permission denied
- `s10` Continuous (batch) scan
- `s11` Pick an image
- `s12` Processing
- `s13` Code found in image
- `s14` No code found

**03 Results**
- `s15` Website link
- `s16` Plain text
- `s17` Contact (vCard)
- `s18` Wi-Fi network
- `s19` Calendar event
- `s20` Phone number
- `s21` Email
- `s22` SMS
- `s23` Location
- `s24` Product barcode
- `s25` Unknown / other format
- `s26` Wi-Fi: connecting
- `s27` Wi-Fi: connected
- `s28` Wi-Fi: could not connect
- `s29` Share sheet
- `s30` Product search results

**04 Create**
- `s31` Create hub
- `s32` Website form, validation error
- `s33` Text form
- `s34` Phone number form
- `s35` Contact form
- `s36` Wi-Fi form
- `s37` Event form
- `s38` Social profile form
- `s39` Custom data and barcode types
- `s40` Generated QR, saved
- `s41` Customize, Colors tab
- `s42` Customize, Shapes tab
- `s43` Customize, Extras tab
- `s44` Barcode variant (EAN-13)

**05 History**
- `s45` Scanned list
- `s46` Created tab
- `s47` Multi-select
- `s48` Favorites filter
- `s49` Empty state
- `s50` History item detail (created Wi-Fi)
- `s51` Swipe to delete, undo snackbar
- `s52` Favorites, empty
- `s53` Search results

**06 Settings**
- `s54` Scan and appearance
- `s55` Privacy, permissions and data
- `s56` About
- `s57` Theme picker
- `s58` Language picker
- `s59` Help and FAQ
- `s60` Privacy policy
- `s61` Send feedback
- `s62` About
- `s63` Export history

**07 Dialogs and system states**
- `s64` Camera permission rationale
- `s65` Photos permission rationale
- `s66` Location for Wi-Fi connect
- `s67` Delete confirmation
- `s68` Clear all history
- `s69` Unsafe link warning
- `s70` No internet (product lookup)
- `s71` Rate the app sheet
- `s72` App update prompt

**08 Dark mode**
- `s73` Scan
- `s74` Result: website
- `s75` Create hub
- `s76` History
- `s77` Settings

Notes on the inventory:
- `s05` (onboarding rationale screen) and `s64` (system rationale dialog) are the same permission flow: build one `CameraRationale` component and show it as a full screen in onboarding and as a dialog afterwards.
- `s56` is the About row inside the Settings list; `s62` is the About screen it opens. `s54` and `s55` are two scroll positions of the one Settings screen.
- `s13` and `s14` are results of the same Scan-from-image flow; `s26` to `s28` are states of one Wi-Fi connect screen; `s41` to `s43` are tabs of one editor screen; `s46`, `s48`, `s49`, `s52`, `s53` are states of the one History screen.
- Every screen needs a Compose `@Preview` for light and dark using the artboard number in its name, e.g. `S18_WifiResult_Light`.

## 14. Interaction and motion notes (from the design annotations)

- **Splash:** logo scales in over 400 ms, then fades to onboarding or Scan. Onboarding pages swipe horizontally with a 300 ms ease-out; page dots animate width.
- **Scan:** scan line sweeps top to bottom every 1.8 s; on detection the brackets turn teal (150 ms), a short haptic fires, and the frame freezes for 250 ms before the result opens. Flash and mode chips toggle instantly with a state-layer ripple. Pinch to zoom, slider mirrors zoom.
- **Quick result sheet (s08):** slides up from the bottom with a drag handle; swipe down to keep scanning, tap the sheet body to open the full result.
- **Batch scan (s10):** each new code adds a row with a short slide-in and increments the badge; duplicates are ignored.
- **Result screens:** primary action is a full-width button pinned to the bottom; secondary actions are the round Copy / Share / Favorite buttons. Favorite fills the star with a small pop animation.
- **Wi-Fi (s26 to s28):** connecting shows an indeterminate bar; success uses the teal accent and Done returns to Scan; failure keeps the password copyable and offers Try again.
- **Create forms:** Generate stays disabled until all required fields validate; errors appear on blur with helper text turning red; keyboard type and IME action match the field (url, email, phone, next/done).
- **Editor:** changing any style option re-renders the preview within 100 ms; a low-contrast color pair shows a warning chip "May be hard to scan"; Save to gallery shows the "Saved to gallery" toast; export size uses a slider with snap points.
- **History:** long-press enters multi-select with a top action bar; swipe left deletes with an Undo snackbar for 5 s; day headers are sticky; search filters live with 250 ms debounce.
- **Dialogs and sheets:** scrim at 50% (62% dark); dialogs fade/scale in, sheets slide; back button and outside tap dismiss non-destructive ones, destructive confirms require a button tap.
- **Theme switch (s57):** applies immediately with a short cross-fade; "System" is the default.
- **Rate prompt (s71):** shown after the 5th successful scan, max once per 60 days, never during scanning.
- **Update prompt (s72):** flexible update; "Later" hides it for 3 days.

## 15. Revision history

- v2: aligned the PRD to the final design (77 artboards on one canvas); added screen inventory (section 13), interaction notes (section 14), Wi-Fi connect states, share sheet, product search results, help, privacy summary, feedback, about and export-history screens.

## START NOW

Read this whole document, inspect the existing project, then write a short plan in `PLAN.md` mapping milestones to files. Begin with **Milestone 1** and continue through all milestones without waiting for approval. Use sections 13 and 14 as the screen-by-screen checklist. At the end, print a checklist of section 11 with pass/fail for each item and anything that needs my manual input (real ad unit IDs, signing key, Firebase config file, store listing assets).
