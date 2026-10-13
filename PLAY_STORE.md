# Google Play listing: QR Code Scanner and Generator

Package: `com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode`

## Store listing
**Title (30 max):** QR Code Scanner and Generator

**Short description (80 max):** Scan QR codes and barcodes fast, create your own. Free, private, offline.

**Full description (4000 max):**
Scan any QR code or barcode in a blink, and create your own with colors, shapes and a logo. No account, no sign-up, and everything works offline.

SCAN FAST
- Point the camera at a QR code or barcode: the result opens right away
- Flash, zoom and pinch to zoom for dark or distant codes
- Batch mode to scan many codes in a row
- Scan from a photo or screenshot in your gallery
- Reads QR, EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar, PDF417, Data Matrix and Aztec

DO MORE WITH EVERY CODE
- Websites: open safely, with warnings for suspicious links
- Wi-Fi: connect in one tap, or copy the password
- Contacts, calendar events, phone numbers, email, SMS and map locations
- Product barcodes: look them up on Google, Open Food Facts or Amazon

CREATE ANYTHING
- Website, text, contact, Wi-Fi, email, phone, SMS, location, event, social profile and app links
- Barcodes: EAN-13, EAN-8, UPC-A, Code 128, Code 39, ITF, PDF417, Data Matrix, Aztec
- Pick colors and gradients, dot and corner shapes, add your logo and a "Scan me" frame
- Save to your gallery or share as PNG, JPG, SVG or PDF

HISTORY THAT STAYS ON YOUR DEVICE
- Search, favorites, filters, swipe to delete with undo
- Export your history as CSV, JSON or text
- Turn history off any time

PRIVATE BY DESIGN
- Scans and creations never leave your phone
- No account needed
- Light and dark themes, 8 languages

Free, with ads.

## Graphics
- App icon 512x512 and feature graphic 1024x500: Design artifact board "App icon and Play feature graphic" (export from the board; replace the phone content with the real scan screen).
- Phone screenshots (min 2, 1080x1920 or similar): Scan, Result (website), Create hub, Customize, History, Settings. Light and dark.

## Category and tags
Category: Tools. Tags: QR code scanner, Barcode scanner, QR generator.

## Content rating (IARC questionnaire answers)
Violence: none. Sexual content: none. Language: none. Controlled substances: none. User interaction/UGC: none. Shares location: no. Digital purchases: none. Ads: yes. Expected rating: Everyone / PEGI 3.

## Data safety form
| Question | Answer |
|---|---|
| Collects or shares data? | Yes (device or other IDs via ads and analytics; diagnostics via Crashlytics) |
| Personal info, financial info, health, messages, contacts, photos/videos, audio, files | **Not collected.** Scanned and created content, images and history stay on the device |
| Location | Not collected (permission used only to join a scanned Wi-Fi network on devices that require it; value never leaves the device) |
| App activity | Collected: app interactions (event names only, no scan content) for analytics |
| App info and performance | Collected: crash logs, diagnostics (Firebase Crashlytics) |
| Device or other IDs | Collected: advertising ID (AdMob) |
| Data encrypted in transit | Yes |
| Users can request data deletion | Not applicable for on-device data; analytics/ad data per Google policies |
| Data shared with third parties | Advertising ID and diagnostics are shared with Google (AdMob, Firebase) |
| Required for the app to work? | Optional (analytics and ads can be limited via consent/settings) |

Ads declaration: the app contains ads. Target audience: 13+ (not designed for children). News app: no. Government app: no.

## Permissions rationale
| Permission | Why |
|---|---|
| CAMERA | Scan codes with the live camera. Asked in context, the app still works for scan-from-image and create without it. |
| INTERNET, ACCESS_NETWORK_STATE | Ads, remote config, product search links (opened in the browser). |
| VIBRATE | Haptic feedback on a successful scan (can be turned off). |
| ACCESS_FINE_LOCATION | Only requested when Android requires it to connect to a scanned Wi-Fi network. |
| CHANGE_WIFI_STATE, ACCESS_WIFI_STATE, CHANGE_NETWORK_STATE | Connect to a Wi-Fi network from a scanned Wi-Fi QR code. |
| AD_ID | Advertising ID for AdMob. |

No storage permissions: images are picked with the system Photo Picker and saved with MediaStore.

## Release checklist
1. Create the app in Play Console with the package above, enrol in Play App Signing.
2. Put real AdMob ids in `local.properties`/BuildConfig (see `docs/ADS_SETUP.md`) and the real AdMob app id in `resValue`.
3. Replace `app/google-services.json` with the file from your Firebase project (package must match, plus the `.debug` client if used).
4. Set `privacy_policy_url` in `strings.xml` to the hosted policy and add the URL in Play Console.
5. Provide a keystore through `keystore.properties` or `QR_KEYSTORE_FILE`, `QR_KEYSTORE_PASSWORD`, `QR_KEY_ALIAS`, `QR_KEY_PASSWORD`.
6. `./gradlew bundleRelease` and upload `app/build/outputs/bundle/release/app-release.aab` to an internal testing track first.
