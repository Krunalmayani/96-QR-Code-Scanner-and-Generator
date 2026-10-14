# Google Play Store Description (draft for preparation)

> Draft text based on the current code. It is **not** published and nothing here has been submitted.
> Review every claim against the final release build before using it. Items marked **Unknown** need your input.

## App name (max 30 characters)
QR Code Scanner and Generator

(29 characters.)

## Package name
`com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode`

## Short description (max 80 characters)
Scan any QR code or barcode and create your own. No account needed.

(67 characters.)

## Full description (max 4000 characters)

Scan any QR code or barcode, understand it at a glance, and act on it with one tap. Then create your own codes with the colors, shapes and logo you want.

SCAN FAST
- Point your camera at a QR code or barcode
- Flash, zoom slider and pinch to zoom for dark or distant codes
- Scan QR codes, barcodes, or everything with one mode switch
- Scan a code from a photo or screenshot in your gallery
- Batch mode for scanning several codes in a row
- Reads QR, EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar, PDF417, Data Matrix and Aztec

DO MORE WITH EVERY RESULT
- Websites: open in a browser tab, with a warning when a link looks suspicious
- Wi-Fi: connect from the result screen, or copy the password
- Contacts and calendar events: add them to your phone
- Phone numbers, email, SMS and map locations: start the call, message or navigation
- Product barcodes: search on Google, Open Food Facts or Amazon
- Copy, share and favorite any result

CREATE ANY CODE
- Website, text, contact, Wi-Fi, email, phone, SMS, location, event, social profile and app link
- Barcodes: EAN-13, EAN-8, UPC-A, Code 128, Code 39, ITF, PDF417, Data Matrix and Aztec
- Choose colors and gradients, dot and corner shapes, add a center logo and a "Scan me" frame
- A warning tells you when your colors may be hard to scan
- Save to your gallery or share as PNG, JPG, SVG or PDF

HISTORY THAT STAYS ON YOUR DEVICE
- Separate Scanned and Created lists with search and filters
- Favorites, notes, swipe to delete with undo
- Export your history as CSV, JSON or text
- Turn history off or clear it whenever you like

MADE TO BE COMFORTABLE
- No account or sign-up
- Light and dark themes
- Available in English, Hindi, Spanish, Portuguese, French, German, Indonesian and Arabic

The app is free and supported by ads.

(1810 characters.)

## Suggested listing fields
| Field | Value |
|---|---|
| Category | Tools (suggestion from `PLAY_STORE.md`; **Unknown** until chosen in Play Console) |
| Tags | QR code scanner, Barcode scanner, QR generator |
| Contains ads | Yes (ads are implemented; real ad IDs still needed) |
| Target age | **Unknown** (not decided in code; `PLAY_STORE.md` assumes 13+) |
| Languages in the app | English, Hindi, Spanish, Portuguese (Brazil wording), French, German, Indonesian, Arabic |

## Before you publish
- Replace the placeholder `app/google-services.json` (currently `project_id: template-placeholder`), the Google test ad IDs, and the `example.com` privacy policy URL, terms URL and support email.
- Complete the Data safety form: the code includes Firebase Analytics, Crashlytics, Remote Config, Google Mobile Ads and the UMP consent flow, so advertising ID, diagnostics and analytics events are likely to need declaring. Final answers are **Unknown** until the production configuration exists. `PLAY_STORE.md` has a draft.
- Run the real build and test on devices; the listing text above describes implemented code, not tested behavior.
- Screenshots, icon export (512 px) and feature graphic are not generated here (the launcher icon vectors and a design board exist).
