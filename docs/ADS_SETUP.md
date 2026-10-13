# Ads, consent and analytics setup

Everything ad related lives in `core/ads` (Google Mobile Ads **Next Gen** SDK, artifact
`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk`), the pure frequency rules in `domain/ads`,
remote flags in `core/config`, and the "Remove ads" seam in `core/billing`. Screens only use `BannerAd()`,
`NativeAdCard()` and `LocalAdController.current.onResultShown()`.

## What shows where (PRD 9)

| Format | Rule | Code |
|---|---|---|
| Anchored adaptive banner | History and Settings only, never on the camera. Takes zero height until loaded. | `BannerAd()` |
| Interstitial | After every 4th completed scan/create, at least 90 s apart, never in the first 10 minutes after first launch, never before the result is visible (triggered by `onResultShown()`). Preloaded, reloaded after each show. | `InterstitialPolicy`, `FullScreenAds` |
| App open | On return after 30 min or more in the background; never before onboarding is done; never on a fresh install's first cold start. | `AppOpenPolicy`, `FullScreenAds` |
| Native | One card every 8 history rows. | `NativeAdCard()`, `NativeAdPlacement` |

Ads run only when ALL of these hold (`AdsGate`):

1. `BuildConfig.ADS_ENABLED` is true (build kill switch in `app/build.gradle.kts`).
2. Firebase Remote Config `ads_enabled` is true (default true).
3. UMP consent allows ads (`canRequestAds()`). The Mobile Ads SDK is **not initialised** before this.
4. The user has not bought "Remove ads" (`BillingManager.isAdFree`, always false today).

## Ad unit ids

`app/build.gradle.kts` currently holds Google's public **test** ids:

| BuildConfig field | Test id |
|---|---|
| `AD_BANNER_ID` | `ca-app-pub-3940256099942544/9214589741` |
| `AD_INTERSTITIAL_ID` | `ca-app-pub-3940256099942544/1033173712` |
| `AD_APP_OPEN_ID` | `ca-app-pub-3940256099942544/9257395921` |
| `AD_NATIVE_ID` | `ca-app-pub-3940256099942544/2247696110` |
| `AD_REWARDED_ID` | `ca-app-pub-3940256099942544/5224354917` (unused) |
| `admob_app_id` (resValue) | `ca-app-pub-3940256099942544~3347511713` |

Never ship the test ids and never click your own live ads.

### Going live

1. In AdMob create the app, then one ad unit per format above (banner, interstitial, app open, native advanced).
2. Put the real ids in `local.properties` (never committed; it is in `.gitignore`):

   ```properties
   # placeholders, replace with your real ids
   ads.appId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ads.bannerId=ca-app-pub-XXXXXXXXXXXXXXXX/1111111111
   ads.interstitialId=ca-app-pub-XXXXXXXXXXXXXXXX/2222222222
   ads.appOpenId=ca-app-pub-XXXXXXXXXXXXXXXX/3333333333
   ads.nativeId=ca-app-pub-XXXXXXXXXXXXXXXX/4444444444
   ```

3. `app/build.gradle.kts` must read these for **release** builds and keep the test ids for debug. The Gradle
   change is listed as a request in the monetization report (the file is shared, so it is not edited here):
   in `buildTypes.release`, override the `buildConfigField`s and `resValue("string", "admob_app_id", ...)`
   from `local.properties` (fail the release build when a value is missing so test ids can never ship).
4. Until that change lands, edit the `buildConfigField` / `resValue` lines in `defaultConfig` by hand.

## Consent (UMP)

- Dependency: `com.google.android.ump:user-messaging-platform:3.1.0`.
- The consent flow starts when `AdManager.controller(activity)` is first called (MainActivity.onCreate):
  `requestConsentInfoUpdate`, then `loadAndShowConsentFormIfRequired`. It runs once per app launch.
- The AdMob app id is already in the manifest (`com.google.android.gms.ads.APPLICATION_ID`), which UMP needs.
- In AdMob: **Privacy & messaging** -> create a **GDPR** message (and a US states regulations message if you
  serve the US), assign it to this app, and publish it. Without a published message no form is shown and
  `canRequestAds()` is true, which is correct outside regulated regions.
- Add the privacy policy URL in AdMob and in `privacy_policy_url` (`strings.xml`).
- **Privacy options entry point** (required where the form says so). Settings should show a row only when
  `isPrivacyOptionsRequired(context)` is true and call `showPrivacyOptionsForm(activity)` on tap
  (`core/ads/AdsPrivacy.kt`). Strings `ads_privacy_options_title` / `ads_privacy_options_summary` exist.
  Withdrawing consent stops all further ad requests immediately.

## Test devices and testing consent

Find your hashed device id in logcat after the first run on the device:
`Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))` and
`Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")`.

Create `app/src/debug/res/values/ads_test_devices.xml` (debug only, do not commit your device id):

```xml
<resources>
    <string name="ads_test_device_ids" translatable="false">ABCDEF012345</string>
    <!-- "" (real geography), "EEA" or "NOT_EEA" forces the UMP geography to test the consent form -->
    <string name="ads_debug_geography" translatable="false">EEA</string>
</resources>
```

Both values are only read in debug builds. To see the consent form again, clear app data
(or call `UserMessagingPlatform.getConsentInformation(context).reset()` from a debug-only snippet).
Test ads show with the Google test ids even without a registered device; registering the device is needed when
you use real ids during development.

## Remote Config flags

Defaults live in code (`AdsConfig.DEFAULT`), so the app works offline and before the first fetch. Values are
fetched at app start (minimum fetch interval 12 h, 1 h in debug) and clamped to safe ranges so a typo in the
console cannot create an ad flood.

| Key | Type | Default | Allowed range |
|---|---|---|---|
| `ads_enabled` | boolean | `true` | remote kill switch |
| `interstitial_every_n` | number | `4` | 2 to 50 |
| `interstitial_min_interval_sec` | number | `90` | 30 to 3600 |
| `native_every_n_items` | number | `8` | 4 to 50 |
| `app_open_min_away_min` | number | `30` | 5 to 1440 |

Create the same keys in Firebase console -> Remote Config and publish. A changed `ads_enabled` takes effect
in the running session once the fetch succeeds.

## Analytics and crashes

- Events are names only (`core/analytics/Analytics.kt`): `ad_interstitial_shown`, `ad_app_open_shown`,
  `ad_load_failed` (param `ad_format` = `native`, `interstitial`, `app_open`). Never scanned content.
- The Settings "analytics" toggle (`AppSettings.analyticsEnabled`) turns Firebase Analytics **and**
  Crashlytics collection off (`Analytics.setEnabled` calls `CrashReporter.setEnabled`).
- `CrashReporter.logNonFatal(tag, throwable)` strips the exception message and cause, because messages can
  contain what the user scanned.
- Replace `app/google-services.json` with the real Firebase file before release, otherwise Remote Config and
  Crashlytics cannot reach a project (the code falls back to defaults).

## "Remove ads" (scaffold, off)

`core/billing/BillingManager` with `NoOpBillingManager` bound by Hilt (`isAdFree` always false,
`isPurchaseAvailable` false, `purchaseRemoveAds()` does nothing). There is no Play Billing dependency. A future
implementation only has to emit `true` from `isAdFree`; the ad manager then hides banners and native cards,
drops preloaded ads and stops full-screen ads. Rebind it in `BillingModule`.

## Release checklist

- [ ] Real ad unit ids and app id wired for release builds, no test ids in the release APK.
- [ ] AdMob app linked to the Play listing; `app-ads.txt` published on the developer website.
- [ ] GDPR (and US states) message published; privacy options row visible in Settings where required.
- [ ] Data safety form in Play Console declares advertising id use (`AD_ID` permission is in the manifest).
- [ ] Remote Config keys created and published.
- [ ] Verify with the Ad Inspector on a test device (`MobileAds.openAdInspector`) before shipping.
