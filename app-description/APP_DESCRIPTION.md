# QR Code Scanner and Generator: App Description

> Generated from the current source code of this repository (branch `claude/compassionate-mccarthy-wqkct3`).
> Status labels used below: **Implemented** = the code exists and was type-checked and unit-tested in a build harness;
> it has **not** been run on a device or built with the real Android toolchain. Anything not provable from the code is marked **Unknown**.

## 1. App Name
QR Code Scanner and Generator (`app_name` in `res/values/strings.xml`, not translated).

## 2. Package Name
`com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode`
(debug builds use the suffix `.debug`). Version in the code: `1.0.0` (`versionCode` 1). Min SDK 24, target/compile SDK 35.

## 3. App Category
Suggested Google Play category: **Tools** (taken from the project's own `PLAY_STORE.md`; no category is set in code). The final choice is **Unknown** until the listing is created.

## 4. Short Description (max 80 characters)
> Scan any QR code or barcode and create your own. No account needed.

(67 characters.)

## 5. Full App Description
Scan a QR code or barcode, see what is inside, and act on it with one tap. Then turn around and create your own codes, styled the way you like.

QR Code Scanner and Generator reads codes with your camera or from a picture in your gallery and recognizes what they contain: web links, Wi-Fi networks, contacts, calendar events, phone numbers, email, text messages, map locations and product barcodes. Each result has actions that fit its type, such as opening a link, connecting to Wi-Fi, adding a contact or event, or searching for a product. Nothing is opened automatically unless you turn on the option for links.

Need a code of your own? Pick from website, text, contact, Wi-Fi, email, phone, SMS, location, event, social profile and app links, or generate a standard barcode such as EAN-13, UPC-A or Code 128. Change colors and gradients, dot and corner shapes, add your own logo or a "Scan me" frame, then save it to your gallery or share it as PNG, JPG, SVG or PDF.

Every scan and creation goes into a searchable history on your device. Mark favorites, filter by type, swipe to delete with undo, or export the list as CSV, JSON or text. There is no sign-up and no account. The app supports light and dark themes and eight languages.

The app is supported by ads. Whether real ads are shown depends on ad IDs that still have to be configured (see section 12).

## 6. Main Features
Scanning
- Live camera scanning with a viewfinder, flash, zoom (slider and pinch) and camera switching
- Three scan modes: QR codes, barcodes, or all supported symbologies
- Supported formats: QR, EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar, PDF417, Data Matrix, Aztec
- Scan from an image picked with the system photo picker, with retries for rotated or low-contrast pictures and a chooser when several codes are found
- Batch scanning that collects several codes in a row
- Optional vibration and beep on a successful scan, flash on start, and "continuous scan" with a quick result sheet

Understanding results (parsed from the code text)
- Website, plain text, contact (vCard and MECARD), phone, email, SMS, location, calendar event, Wi-Fi, product barcode, and unknown/other
- Website safety check before opening: warns about plain http, look-alike brand domains, punycode, suspicious domain endings, URL shorteners, IP-address hosts and embedded credentials
- Wi-Fi connect (with a rationale for location where the OS needs it) and a copy-password fallback
- Product barcodes can be searched on Google, Open Food Facts or Amazon in your preferred order (opened in the browser)
- Copy, share and favorite actions on every result

Creating codes
- 11 content types plus 9 barcode formats, with field validation (URL, email, phone, Wi-Fi password length, coordinates, event times, barcode length and check digits)
- Customization: colors, gradients, dot shapes, eye shapes, error correction level, center logo, frame text, export size 512 to 2048 px
- A warning when a color choice may make the code hard to scan
- Save to gallery, share, and export as PNG, JPG, SVG or PDF

History
- Scanned and Created tabs, filters (favorites, links, Wi-Fi, contacts and more), search, day grouping
- Multi-select, favorites, notes, swipe-to-delete with undo, clear-all with an option to keep favorites
- Export as CSV, JSON or text

Settings and polish
- Theme (system, light, dark), per-app language (English, Hindi, Spanish, Portuguese, French, German, Indonesian, Arabic)
- Help and FAQ, privacy summary, feedback form, rate and share app, open-source licenses
- Onboarding with camera-permission explanation, rate-us and update prompts

## 7. Benefits for Users
- One app for both reading and making QR codes and barcodes.
- Results are explained and turned into the right action, so there is less copying and typing.
- Links are checked before they are opened, which helps users notice suspicious addresses (it is a set of local heuristics, not a guarantee).
- No account to create; history is stored on the device and can be turned off, cleared or exported.
- Codes can be styled to match a brand or event, and exported in print-friendly formats.

## 8. How It Works
1. First launch: three short intro screens, then an explanation of why the camera is needed.
2. The app opens on **Scan**. Point the camera at a code; when it is found the result screen opens (or a quick sheet if continuous scan is on).
3. On the result screen use the main action (open, connect, call, add to contacts...) or Copy, Share, Favorite.
4. **Create** tab: choose a type, fill in the form, tap Generate, then adjust colors, shapes, logo and size and save or share.
5. **History** tab: find, favorite, delete or export earlier scans and creations.
6. **Settings** tab: scanning options, theme, language, privacy and data tools, help and about.

## 9. Target Audience
Everyday smartphone users who scan menus, tickets, Wi-Fi codes and links; shoppers checking product barcodes; and small businesses, event organizers or individuals who need to make QR codes for links, Wi-Fi, contacts, events or social profiles.

## 10. Unique Selling Points (supported by the code)
- Scanner and generator in the same app, with a shared history.
- Local link-safety checks with a warning sheet before an unsafe-looking link opens.
- Wide barcode support for both scanning and creating (standard retail and industrial formats).
- Detailed code styling (gradients, shapes, logo, frame) with a scan-reliability warning.
- Eight interface languages and a design with light and dark modes.
- No backend or user accounts in the code; history lives on the device.

## 11. Technical Overview
Kotlin, Jetpack Compose with Material 3, single-activity app with type-safe Navigation Compose, MVVM, Hilt, Room, DataStore, CameraX, ML Kit Barcode Scanning (bundled model), ZXing for encoding, Firebase (Analytics, Crashlytics, Remote Config), Google Mobile Ads Next Gen SDK with the UMP consent flow, Play In-App Review and In-App Updates. There is no custom backend. Details: `APP_TECHNICAL_SUMMARY.md`.

## 12. Incomplete, Placeholder or Unverified Items
Full list in `APP_FEATURES.md`. The most important ones:
- **Never built with the real Android toolchain.** The code was checked in a harness only; the first real Gradle build, lint, R8 release build and on-device behavior (camera, ML Kit, Wi-Fi connect, ads) are **Unknown**.
- **Firebase is a placeholder:** `app/google-services.json` has `project_id: template-placeholder`.
- **Ads use Google's test ad IDs** unless real IDs are supplied; the Next Gen Ads SDK call shapes and version `1.5.0` are unverified.
- **"Remove ads" purchase is not implemented** (a no-op billing placeholder only).
- **Placeholder links and addresses:** privacy policy URL, terms URL and support email point to `example.com`.
- The map on the location result is a decorative placeholder (the real map opens in the maps app).
- Translations were written without native-speaker review.
- Not published; Play listing, store assets and Data Safety answers are **Unknown / not done**.
