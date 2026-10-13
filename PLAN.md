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
