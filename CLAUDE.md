# QR Code Scanner and Generator: repo rules

Product spec: `PRD.md`. Design source of truth: the Design artifact https://claude.ai/artifact/2r1htWfHVjccRdWA4do3m5
(77 phone artboards `s01`..`s77`; read with the Artifact tool, `project/<file>.dc.html`, shared styles in `project/qr.css`).
Plan: `PLAN.md`. Non-obvious choices: `DECISIONS.md` (append, never rewrite others' lines).

## Hard rules
- Kotlin only, Compose only (no XML layouts; XML only for manifest, themes, resources, vectors).
- Package root `com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode` (call it `<P>`), source in `app/src/main/java/<P>/`.
- Layers: `ui`/`feature` -> `domain` -> `data`. `domain` is **pure Kotlin** (no `android.*`, no Compose, no Room, no ML Kit) so it unit-tests on the JVM.
- ViewModels expose `StateFlow<UiState>` plus one-shot `UiEffect` via `Channel` (`receiveAsFlow()`); composables are stateless with hoisted state; no business logic in composables.
- No hard-coded user-visible strings or colors in UI code: strings in resources, colors from `AppTheme.colors`/`MaterialTheme`.
- Every screen has `@Preview`s for light and dark named after the artboard, e.g. `S18_WifiResult_Light`, `S18_WifiResult_Dark`.
- No TODOs left in code. Keep functions small. Unit tests for each parser, validator and use case.
- Never auto-execute intents from scanned content: always require a tap.

## There is NO Android SDK or Google Maven in the build sandbox, so nothing Android can be compiled here
Write Android/Compose code defensively:
- Use only APIs you are certain exist in the pinned versions (`gradle/libs.versions.toml`): Compose BOM 2024.12.01 (Material3 1.3.x), Navigation 2.8.5, Lifecycle 2.8.7, CameraX 1.4.1, ML Kit barcode 17.3.0, Room 2.6.1, Hilt 2.52, Kotlin 2.0.21.
- Explicit imports only (no wildcards); double-check every import path and every named parameter. Prefer stable APIs; if you must use an experimental one add `@OptIn(...)`.
- Re-read your own files once for syntax, imports, and signature mismatches against the files you call (read the callee, do not guess).
- Pure domain code IS compiled and tested here: the JVM check script whose path is given in your task prompt (usage: `jvmcheck.sh <name> model <your domain subpackages...>`). Keep domain code compiling there.

## Parallel-work conventions (several people edit this repo at once)
- Only create/edit files inside the folders your task owns. Do not edit shared files (build.gradle.kts, libs.versions.toml, Manifest, `Routes.kt`, `designsystem/`) - if you need a change there, write the exact request at the end of your report.
- Strings: put yours in `app/src/main/res/values/strings_<feature>.xml`, every name prefixed `<feature>_` (e.g. `scan_flash_on`). Never edit `strings.xml` / `strings_common.xml`. Shared strings (OK, Cancel, Done, Copy, Share, Back, ...) live in `strings_common.xml` with prefix `common_`.
- No git commits by workers: the lead commits. Do not run git commands that change state.
- Hilt: each feature injects through constructors; DI bindings for repositories are in `data/di/`.

## Navigation
Type-safe routes in `core/navigation/Routes.kt`. Each feature exposes `fun NavGraphBuilder.<feature>Graph(nav: NavHostController)` that registers its destinations with `composable<Route> { ... }` and navigates only via those route objects.

## Shared domain contract (already written, read before coding)
`domain/model/*` (CodeFormat, ContentType, ScanSource, ScanContent, QrStyle, ScanRecord),
`domain/repository/HistoryRepository.kt`, `SettingsRepository.kt` (AppSettings, ThemeMode, ProductSite).

## Design system (already written, use it, do not re-create widgets)
`designsystem/theme`: `AppTheme { }` (+ `AppTheme.colors`, `AppTheme.type`, `AppTheme.shapes`), `Tone`, `CameraColors`, `ScanHitColor`.
Preview pattern: wrap in `AppTheme(dark = false/true) { ... }`.
`designsystem/icons/AppIcons` (semantic icon names; add missing icons there only via a request).
`designsystem/components/*`: `PrimaryButton, TonalButton, AppOutlinedButton, AppTextButton, DestructiveButton, SmallButton, IconButtonRounded(IconButtonStyle), AppChip, ChipRow(ChipItem), AppBadge, SegmentedControl, AppTextField, PasswordVisibilityToggle, AppSearchBar, AppSwitch, AppCheckbox, CheckboxRow, AppRadio, SettingsSwitchRow/RadioRow/NavRow, ColorSwatch, AppSlider, AppScaffold, AppTopBar, AppCard, CardGroup, AppDivider, IconTile, ListRow, KeyValueRow, SectionLabel, QuickAction, ActionBar, AppSnackbarHost, BottomNavBar(BottomTab), AppBottomSheet, ConfirmDialog, ToastState/AppToastHost, EmptyState, LoadingSpinner, IndeterminateBar, PageDots, InfoBanner, ViewfinderOverlay (+ viewfinderRect), MaxWidthContainer`. Read the file before calling a component (exact parameters).
Tab-root screens must add `LocalBottomBarInset.current` as bottom content padding (bottom bar overlays the screen). Forms use `Modifier.imePadding()`.
Open `Design artifact` boards for pixel details; CSS classes in `project/qr.css` map to components (`.bt` buttons, `.cp` chips, `.cd` cards, `.li` list rows, `.tf` fields, `.sht` sheets, `.dlg` dialogs, `.snk` snackbar, `.tst` toast).

## Cross-feature contracts
- Feature graph entry points (called by `core/navigation/AppNav.kt`): `fun NavGraphBuilder.onboardingGraph(navController: NavHostController)` in `feature/onboarding`, `scanGraph` in `feature/scan`, `resultGraph` in `feature/result`, `createGraph` in `feature/create`, `historyGraph` in `feature/history`, `settingsGraph` in `feature/settings`; plus `@Composable fun SystemPromptsHost(navController: NavHostController, scanningInProgress: Boolean)` in `feature/system`.
- Permission/rationale UI shared across features lives in `core/permissions`: `CameraRationaleContent(onAllow, onNotNow, modifier)` (full screen, onboarding s05), `CameraRationaleDialog(onAllow, onDismiss)` (s64), `PhotosRationaleDialog(onContinue, onDismiss)` (s65), `LocationRationaleSheet(onContinue, onDismiss)` (s66).
- Ads (call only these): `BannerAd()`, `NativeAdCard()` and `LocalAdController.current.onResultShown()` from `core/ads`. Analytics: inject `core/analytics/Analytics`, use `Events.*`/`Params.*`, never log content.
- History/records: always through `domain/repository/HistoryRepository` (and `domain/usecase/*`). `SaveScanUseCase` persists a scan/creation and returns the `ScanRecord` (id may be negative when history is off).
- Tests: JUnit 5 + Turbine + the fakes in `app/src/test/java/<P>/domain/testing/` (`FakeHistoryRepository`, `FakeSettingsRepository`, `Fixtures`) + `MainDispatcherExtension` in `<P>/testing`.
- Scanned tab = sources CAMERA and IMAGE (the repository filters one source at a time: observe with `source = null` and drop CREATED in the ViewModel).
