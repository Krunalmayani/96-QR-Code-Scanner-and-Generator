# Android Template (com.app.vv)

Kotlin + Jetpack Compose + Material 3 + Hilt + Firebase (Analytics, Crashlytics, Remote Config) + Google Mobile Ads Next Gen SDK.
Package: `com.app.vv.template` (renamed per app by `scripts/rename.sh`).

## One-time setup (do this once, then never again)

1. Open this folder in Android Studio and let Gradle sync.
   - If there is no `gradlew`, run `gradle wrapper --gradle-version 8.10.2` in this folder
     (or let Android Studio create it), then commit `gradlew`, `gradlew.bat`, `gradle/wrapper/`.
   - Bump versions in `gradle/libs.versions.toml` to the latest stable if Studio suggests it.
2. Fill in the Next Gen ads calls in `ads/AdManager.kt` once, using Google's Next Gen docs.
3. Run the app on a device/emulator and confirm it builds.
4. Push to GitHub as `android-template`, then: Settings -> check **Template repository**.

## Every new app (about 30 seconds)

Install GitHub CLI (`gh`) and run `gh auth login` once. Then:

```
./new-app.sh quiz-cash com.app.vv.quizcash "Quiz Cash"
```

This creates the GitHub repo from the template, clones it, renames the package and app name,
commits and pushes. Open the new folder in Android Studio.

Or manually: click **Use this template** on GitHub, clone, run `./scripts/rename.sh <package> "<App Name>"`.

## Going live checklist (per app)

- Replace `app/google-services.json` with the real file from Firebase (package must match).
- Replace the AdMob **test** ids in `app/build.gradle.kts` (`admob_app_id`, `AD_*_ID`) with real ones.
- Set `privacy_policy_url` in `strings.xml`.
- Copy `keystore.properties.example` to `keystore.properties` and point it at your upload keystore.
  Never commit the keystore or `keystore.properties` (already in `.gitignore`).
- Replace the launcher icon and splash.
- Bump `versionCode` / `versionName` for each release.

## Structure

```
app/src/main/java/com/app/vv/template/
  App.kt, MainActivity.kt
  ads/AdManager.kt          single place for all ads
  analytics/Analytics.kt    Firebase event helper
  data/SettingsRepository   DataStore preferences
  ui/{theme,navigation,home,settings}
```
