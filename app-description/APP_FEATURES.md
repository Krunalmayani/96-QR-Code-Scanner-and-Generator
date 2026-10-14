# App Features: implemented vs. incomplete

Source: the code in this repository (`app/src/main/java/com/qrcode/scanner/barcodereader/generator/scan/qrcode/createqrcode/`, called `<P>` below).

**Status legend**
- **Implemented**: code exists, was type-checked and covered by JVM unit tests where the logic is pure Kotlin. Not run on a device and not built with the real Gradle/Android toolchain.
- **Partial**: works in part, or differs from the design.
- **Placeholder**: present only as a stub or sample value.
- **Unknown**: cannot be established from the code.

## A. Implemented features

### Onboarding and permissions (`feature/onboarding`, `core/permissions`, `feature/system`)
| Feature | Status |
|---|---|
| System splash screen, minimum display time 700 ms | Implemented |
| Three intro slides with page dots, Skip, Next / Get started | Implemented |
| Camera permission explanation screen after onboarding; rationale dialog afterwards | Implemented |
| Photos and Location rationale UI (components exist and are used by the image and Wi-Fi flows) | Implemented |
| Rate-us sheet (after 5 successful scans, at most once per 60 days, never while scanning or on result/onboarding/feedback screens); 4 to 5 stars go to Play In-App Review, 1 to 3 stars open the Feedback screen | Implemented |
| In-app update prompt (Play flexible update; "Later" hides it for 3 days; shows nothing in debug builds) | Implemented |

### Scanning (`feature/scan`, `domain/scan`)
| Feature | Status |
|---|---|
| Live camera preview (CameraX) with ML Kit barcode detection, animated viewfinder, flash, zoom slider and pinch, camera switch | Implemented |
| Modes: QR, Barcode, All; formats: QR, EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar, PDF417, Data Matrix, Aztec | Implemented |
| Duplicate suppression (same value within 2 s), optional vibration and beep, "flash on start" | Implemented |
| "Scan area only" option (filters by the viewfinder frame) | Implemented |
| Camera permission states: first-time rationale, denied, permanently denied (opens app settings), camera failure | Implemented |
| Quick result bottom sheet when "continuous scan" is on | Implemented |
| Batch mode with count badge and list sheet; each code is saved when scanned | Implemented |
| Scan from image via the system photo picker; processing, found (single or several codes), not-found and unreadable states; retries with contrast stretch, inversion and rotations | Implemented |

### Results (`feature/result`, `domain/parser`, `domain/safety`, `domain/result`)
| Feature | Status |
|---|---|
| Parsing into: website, text, contact (vCard, MECARD), phone, email, SMS, location (geo and coordinate map links), calendar event (iCalendar), Wi-Fi, product barcode, unknown | Implemented |
| Website: open in Custom Tab, copy, share, favorite; local safety checks with a warning sheet (go back / copy link / open anyway); optional auto-open for links rated safe | Implemented |
| Phone: dial, send SMS, add contact; Email: compose; SMS: compose; Location: open in maps app; Event: add to calendar; Contact: add to contacts, call, email | Implemented (all require a tap) |
| Wi-Fi: connecting / connected / could not connect states, copy password; Android 10+ network specifier with suggestion fallback, legacy path on Android 7 to 9 | Implemented |
| Product: number plus generated barcode picture, search on Google / Open Food Facts / Amazon in the user's order, offline state with Try again | Implemented |
| Unknown formats: raw text and hex view | Implemented |
| Share sheet (text, image, save, email, message, more apps) | Implemented |

### Create (`feature/create`, `domain/create`)
| Feature | Status |
|---|---|
| Create hub with search, recent types, grid of 11 content types and 9 barcode formats | Implemented |
| Forms with validation and inline errors: website, text, contact, Wi-Fi, email, phone, SMS, location, event (date and time pickers), social profile (Instagram, Facebook, X, LinkedIn, YouTube, WhatsApp, Telegram), app store link, barcodes (length and check-digit rules) | Implemented |
| Generated code screen: preview, details, save to gallery, share, copy, favorite | Implemented |
| Editor: colors and gradients, dot shapes, eye shapes (incl. leaf), error correction, center logo from the photo picker, frame text, export size slider and format (PNG, JPG, SVG, PDF) | Implemented |
| Scan-reliability warning for low contrast or inverted colors | Implemented |
| Barcode options: show number, bar height | Implemented |
| Every created code is stored in history (Created tab) | Implemented |

### History (`feature/history`, `data`)
| Feature | Status |
|---|---|
| Scanned and Created tabs, filter chips (favorites, links, Wi-Fi, contacts, text, phone, email, location, events, products), live search with 250 ms debounce and highlighting | Implemented |
| Day grouping with sticky headers; multi-select (favorite, delete, share, export CSV); swipe left to delete with 5 s undo | Implemented |
| Item detail (shares the result screen): note, favorite, delete with confirmation, style preview and edit for created codes | Implemented |
| Empty states for each tab, favorites and search; Room database with indices; temporary in-memory records when history is turned off | Implemented |

### Settings (`feature/settings`)
| Feature | Status |
|---|---|
| Scanning options: vibrate, beep, flash on start, auto-open links, continuous scan, scan area, product-site order | Implemented |
| Theme (system, light, dark) and per-app language (8 languages) | Implemented |
| Privacy and data: save history, analytics toggle, clear history dialog (counts, keep favorites), export history (CSV, JSON, text via save or share), privacy summary screen, link to the web privacy policy, ad privacy options (shown only when the consent SDK requires it) | Implemented |
| Help and FAQ (accordion), feedback form (stars, topic, text, optional email through a mail app), rate on Google Play, share app, open-source licenses, terms link, About screen | Implemented |
| Debug-build-only Component gallery | Implemented (debug only) |

### Cross-cutting
| Feature | Status |
|---|---|
| Light and dark themes, Inter font, shared design system | Implemented |
| Localization: English plus Hindi, Spanish, Portuguese, French, German, Indonesian, Arabic (819 string resources, of which 800 are translatable and translated in all 7 languages, plus 13 plurals) | Implemented (translation quality **Unknown**) |
| Ads: banner (History, Settings), interstitial rules, app-open rules, native ad in the history list, UMP consent | Implemented in code; behavior with the real SDK **Unknown** |
| Firebase Analytics (event names only), Crashlytics, Remote Config flags | Implemented in code; backend project **Unknown** |
| Accessibility work: content descriptions, 48dp targets, large-font safe heights, live regions | Implemented (not audited with Accessibility Scanner or TalkBack) |

## B. Incomplete, placeholder or unverified

### Blocking before release
1. **Never built with the real toolchain.** The Gradle/AGP/KSP/Hilt/Room code generation, manifest merge, lint, R8 release build and APK size are **Unknown**. Verification so far: a stand-in compile harness (stubs for Google-only libraries), 1,097 JVM unit tests passing there, and static audits of DI, navigation, Room SQL and resources.
2. **No on-device verification.** Camera, ML Kit decoding, Wi-Fi connect, Photo Picker, share/save flows, ads and performance are **Unknown**. The 20 instrumented-test files have never been executed.
3. **Firebase is a placeholder.** `app/google-services.json` contains `project_id: template-placeholder`. Analytics, Crashlytics and Remote Config will not report to a real project until it is replaced.
4. **Ad configuration.** Banner, interstitial, app-open and native IDs are Google's public test IDs unless `local.properties` / `QR_ADS_*` variables are provided. The Next Gen Ads SDK version `1.5.0` and its call shapes (isolated in `core/ads/NextGenAdsGateway.kt`) were not verified against the real SDK, and its compatibility with Kotlin 2.0.21 / compileSdk 35 is **Unknown**.
5. **Placeholder links and addresses.** `privacy_policy_url` and `settings_terms_url` point to `example.com`; `settings_support_email` is `support@example.com`.

### Features not implemented or only partly implemented
| Item | Status |
|---|---|
| "Remove ads" purchase | Placeholder: `NoOpBillingManager` only; no screen, no Play Billing dependency |
| Location result map | Placeholder: a decorative map graphic; the real map opens in the maps app |
| Splash screen from the design (gradient hero, logo tile, spinner) | Partial: only the Android system splash (solid color and icon) |
| Batch scan inline list panel and compact frame (design s10) | Partial: a list sheet is used instead |
| Red scan line in barcode mode (design s07) | Not implemented |
| Settings "Auto-copy" and "Default camera" rows (design s54) | Not implemented (no matching setting in code) |
| Latitude/longitude map picker (PRD 4.5) | Not implemented: text fields with validation |
| Phone form country dropdown and label fields; separate "eye color"; Barcode/Data switch (design s44); "More options" menus | Not implemented |
| Update-prompt release notes | Partial: generic text because the Play API gives no notes |
| Search-results and history thumbnails of created codes (design shows a QR thumbnail) | Partial: a type icon is shown |
| Theme and language pickers as dialog/sheet (design) | Partial: full-screen pages |
| Screenshot (light/dark) tests | Not implemented |
| 30-minute monkey run, JaCoCo coverage report, APK/AAB size check, cold-start and detection-time targets | Not done / **Unknown** |
| Baseline profile | Partial: hand-written broad rules, not generated from a benchmark |
| Translations | Machine-written, no native-speaker review: quality **Unknown** |
| Store assets (screenshots, 512 px icon export, feature graphic), Data safety answers, content rating | Not done; drafts in `PLAY_STORE.md` |
| Static analysis | ktlint passes; detekt is configured as advisory and reports findings without failing the build |
| `EnsureFirstLaunchUseCase` | Defined and provided, but not used (`App` sets the first-launch time itself) |
| Publication status | Not published (**Unknown** whether any store listing exists) |
