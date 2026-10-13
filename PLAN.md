# Build plan (PRD section 10 -> files)

| # | Milestone | Where |
|---|-----------|-------|
| 1 | Project setup | `build.gradle.kts`, `gradle/libs.versions.toml`, `config/detekt.yml`, `.editorconfig`, `.github/workflows/build.yml`, `core/`, `App.kt`, `MainActivity.kt`, `core/navigation/` |
| 2 | Design system | `designsystem/` (theme, tokens, components, `ComponentGallery` debug screen) |
| 3 | Core scan | `feature/scan/` (CameraX + ML Kit, overlay, flash/zoom, debounce, permission flow) |
| 4 | Parsing + results | `domain/parser`, `domain/safety`, `domain/product`, `feature/result/` |
| 5 | Image scan + batch | `feature/scan/image`, `feature/scan/batch` |
| 6 | Database + history | `data/db`, `data/repository`, `feature/history/` |
| 7 | Create | `domain/create`, `feature/create/` (hub, forms, generated, editor, export) |
| 8 | Settings | `data/settings`, `feature/settings/` |
| 9 | Polish | `feature/onboarding/`, `core/permissions`, rate sheet, in-app update |
| 10 | Ads + analytics | `core/ads` (`AdManager` interface), `core/analytics`, UMP, remote flags |
| 11 | Hardening | tests, R8, baseline profile |
| 12 | Design parity | per-artboard pass |
| 13 | Release prep | icon, `PLAY_STORE.md`, signing via env |

## Status (all milestones implemented)
Milestones 1-13 are implemented. Verified without an Android SDK: every main, unit-test and androidTest source type-checks
against a harness (real Kotlin/Compose/Dagger/ZXing/Android-framework artifacts + stubs for Google-Maven-only libraries),
1097 JVM tests pass, and static audits for DI, navigation, Room SQL, resources, packages and manifest are clean.
NOT verified (needs a machine with the Android SDK): the real Gradle/AGP/KSP/Hilt/Room build, R8 release build, lint,
any on-device behaviour (camera, ML Kit, Wi-Fi connect, ads SDK 1.5.0 call shapes), instrumented tests, screenshot tests.
