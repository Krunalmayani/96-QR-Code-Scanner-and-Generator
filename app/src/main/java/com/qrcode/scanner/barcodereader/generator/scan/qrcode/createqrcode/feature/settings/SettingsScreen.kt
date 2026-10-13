package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.BannerAd
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.isPrivacyOptionsRequired
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.showPrivacyOptionsForm
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBarHeight
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsNavRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsSwitchRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ProductSiteOrder

/** Everything the Settings list can trigger outside its own state: navigation and system intents. */
data class SettingsActions(
    val onTheme: () -> Unit = {},
    val onLanguage: () -> Unit = {},
    val onHelp: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    val onAbout: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onPrivacySummary: () -> Unit = {},
    val onPrivacyPolicyWeb: () -> Unit = {},
    val onManagePermissions: () -> Unit = {},
    val onRate: () -> Unit = {},
    val onShareApp: () -> Unit = {},
    /** Debug builds only: null hides the "Component gallery" row. */
    val onGallery: (() -> Unit)? = null,
)

/**
 * Settings tab root (s54, s55, s56, s77): one scrolling list in four sections, banner ad above the bottom bar.
 * [versionName] is shown on the About row.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    actions: SettingsActions,
    snackbarHostState: SnackbarHostState,
    versionName: String,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val bottomInset = LocalBottomBarInset.current
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_title)) },
        snackbarHost = { AppSnackbarHost(snackbarHostState, Modifier.padding(bottom = bottomInset)) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                ScanningSection(state.settings, onEvent)
                Spacer(Modifier.height(SectionGap))
                AppearanceSection(state.settings, actions)
                Spacer(Modifier.height(SectionGap))
                PrivacySection(state.settings, onEvent, actions)
                Spacer(Modifier.height(SectionGap))
                AboutSection(versionName, actions)
                Spacer(Modifier.height(SectionGap))
            }
            BannerAd(Modifier.fillMaxWidth())
            Spacer(Modifier.height(bottomInset))
        }
    }
}

private val SectionGap = 16.dp

@Composable
private fun ScanningSection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit) {
    SectionLabel(stringResource(R.string.settings_section_scanning))
    CardGroup {
        SettingsSwitchRow(
            title = stringResource(R.string.settings_vibrate),
            checked = settings.vibrate,
            onCheckedChange = { onEvent(SettingsEvent.SetVibrate(it)) },
            icon = AppIcons.Vibrate,
        )
        AppDivider()
        SettingsSwitchRow(
            title = stringResource(R.string.settings_beep),
            checked = settings.beep,
            onCheckedChange = { onEvent(SettingsEvent.SetBeep(it)) },
            icon = AppIcons.Sound,
        )
        AppDivider()
        SettingsSwitchRow(
            title = stringResource(R.string.settings_flash_on_start),
            checked = settings.flashOnStart,
            onCheckedChange = { onEvent(SettingsEvent.SetFlashOnStart(it)) },
            icon = AppIcons.FlashOn,
        )
        AppDivider()
        SettingsSwitchRow(
            title = stringResource(R.string.settings_auto_open),
            subtitle = stringResource(R.string.settings_auto_open_hint),
            checked = settings.autoOpenLinks,
            onCheckedChange = { onEvent(SettingsEvent.SetAutoOpenLinks(it)) },
            icon = AppIcons.Link,
        )
        AppDivider()
        SettingsSwitchRow(
            title = stringResource(R.string.settings_continuous),
            subtitle = stringResource(R.string.settings_continuous_hint),
            checked = settings.continuousScan,
            onCheckedChange = { onEvent(SettingsEvent.SetContinuousScan(it)) },
            icon = AppIcons.Apps,
        )
        AppDivider()
        ScanAreaRow(scanAreaOnly = settings.scanAreaOnly, onChange = { onEvent(SettingsEvent.SetScanAreaOnly(it)) })
    }
    Spacer(Modifier.height(12.dp))
    ProductSitesCard(order = settings.productSiteOrder, onMove = { site, up -> onEvent(SettingsEvent.MoveSite(site, up)) })
}

@Composable
private fun ScanAreaRow(scanAreaOnly: Boolean, onChange: (Boolean) -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(AppIcons.Scan)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_scan_area), style = AppTheme.type.title, color = c.ink)
                Text(stringResource(R.string.settings_scan_area_hint), style = AppTheme.type.caption, color = c.inkSecondary)
            }
        }
        SegmentedControl(
            options = listOf(stringResource(R.string.settings_scan_area_frame), stringResource(R.string.settings_scan_area_full)),
            selectedIndex = if (scanAreaOnly) 0 else 1,
            onSelect = { onChange(it == 0) },
        )
    }
}

@Composable
private fun ProductSitesCard(order: List<ProductSite>, onMove: (ProductSite, Boolean) -> Unit) {
    val c = AppTheme.colors
    val sites = ProductSiteOrder.normalize(order)
    CardGroup {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(AppIcons.Product)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_sites_title), style = AppTheme.type.title, color = c.ink)
                Text(stringResource(R.string.settings_sites_hint), style = AppTheme.type.caption, color = c.inkSecondary)
            }
        }
        sites.forEachIndexed { index, site ->
            AppDivider()
            ProductSiteRow(
                position = index + 1,
                site = site,
                canMoveUp = ProductSiteOrder.canMoveUp(sites, site),
                canMoveDown = ProductSiteOrder.canMoveDown(sites, site),
                onMove = onMove,
            )
        }
    }
}

@Composable
private fun ProductSiteRow(
    position: Int,
    site: ProductSite,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (ProductSite, Boolean) -> Unit,
) {
    val name = stringResource(siteLabelRes(site))
    ListRow(
        title = name,
        leading = {
            Text(
                text = position.toString(),
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.primary,
                modifier = Modifier.width(24.dp),
            )
        },
        trailing = {
            Row {
                IconButtonRounded(
                    icon = AppIcons.ExpandLess,
                    contentDescription = stringResource(R.string.settings_site_move_up, name),
                    onClick = { onMove(site, true) },
                    enabled = canMoveUp,
                    size = 40.dp,
                )
                IconButtonRounded(
                    icon = AppIcons.ExpandMore,
                    contentDescription = stringResource(R.string.settings_site_move_down, name),
                    onClick = { onMove(site, false) },
                    enabled = canMoveDown,
                    size = 40.dp,
                )
            }
        },
    )
}

@Composable
private fun AppearanceSection(settings: AppSettings, actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_section_appearance))
    CardGroup {
        SettingsNavRow(
            title = stringResource(R.string.settings_theme),
            value = stringResource(themeLabelRes(settings.theme)),
            onClick = actions.onTheme,
            icon = AppIcons.Palette,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_language),
            value = languageLabel(settings.languageTag),
            onClick = actions.onLanguage,
            icon = AppIcons.Language,
        )
    }
}

@Composable
private fun PrivacySection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit, actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_section_privacy))
    PrivacyBanner()
    Spacer(Modifier.height(8.dp))
    CardGroup {
        PermissionRow(AppIcons.Camera, R.string.settings_perm_camera, R.string.settings_perm_camera_hint)
        AppDivider()
        PermissionRow(AppIcons.Image, R.string.settings_perm_photos, R.string.settings_perm_photos_hint)
        AppDivider()
        PermissionRow(AppIcons.Location, R.string.settings_perm_location, R.string.settings_perm_location_hint)
        AppDivider()
        PermissionRow(AppIcons.Social, R.string.settings_perm_internet, R.string.settings_perm_internet_hint)
    }
    Spacer(Modifier.height(12.dp))
    TonalButton(
        text = stringResource(R.string.settings_manage_android),
        onClick = actions.onManagePermissions,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = AppIcons.OpenInNew,
    )
    Spacer(Modifier.height(12.dp))
    CardGroup {
        SettingsSwitchRow(
            title = stringResource(R.string.settings_save_history),
            subtitle = stringResource(R.string.settings_save_history_hint),
            checked = settings.saveHistory,
            onCheckedChange = { onEvent(SettingsEvent.SetSaveHistory(it)) },
            icon = AppIcons.History,
        )
        AppDivider()
        SettingsSwitchRow(
            title = stringResource(R.string.settings_analytics),
            subtitle = stringResource(R.string.settings_analytics_hint),
            checked = settings.analyticsEnabled,
            onCheckedChange = { onEvent(SettingsEvent.SetAnalytics(it)) },
            icon = AppIcons.Info,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_clear_history),
            onClick = { onEvent(SettingsEvent.ClearHistoryClicked) },
            icon = AppIcons.Delete,
            tone = Tone.Error,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_export_history),
            onClick = actions.onExport,
            icon = AppIcons.Document,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_privacy_summary),
            onClick = actions.onPrivacySummary,
            icon = AppIcons.Shield,
        )
        AppDivider()
        ExternalLinkRow(
            title = stringResource(R.string.settings_privacy_policy_web),
            icon = AppIcons.Lock,
            onClick = actions.onPrivacyPolicyWeb,
        )
        AdPrivacyOptionsRow()
    }
}

/** UMP "Privacy options" entry; only shown where the consent SDK says users must be able to revisit their choice. */
@Composable
private fun AdPrivacyOptionsRow() {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    if (!isPrivacyOptionsRequired(context)) return
    AppDivider()
    SettingsNavRow(
        title = stringResource(R.string.ads_privacy_options_title),
        subtitle = stringResource(R.string.ads_privacy_options_summary),
        onClick = { context.findHostActivity()?.let { showPrivacyOptionsForm(it) } },
        icon = AppIcons.Shield,
    )
}

private tailrec fun Context.findHostActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findHostActivity()
        else -> null
    }

@Composable
private fun PrivacyBanner() {
    val c = AppTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(c.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(AppIcons.Shield, contentDescription = null, tint = c.onPrimaryContainer)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.settings_privacy_banner_title),
                style = AppTheme.type.bodyStrong,
                color = c.onPrimaryContainer,
            )
            Text(
                stringResource(R.string.settings_privacy_banner_text),
                style = AppTheme.type.bodySmall,
                color = c.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    @StringRes title: Int,
    @StringRes hint: Int,
) {
    ListRow(
        title = stringResource(title),
        subtitle = stringResource(hint),
        leading = { IconTile(icon) },
    )
}

@Composable
internal fun ExternalLinkRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListRow(
        title = title,
        leading = { IconTile(icon) },
        trailing = { Icon(AppIcons.OpenInNew, contentDescription = null, tint = AppTheme.colors.inkSecondary) },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun AboutSection(versionName: String, actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_section_about))
    CardGroup {
        SettingsNavRow(
            title = stringResource(R.string.settings_help),
            onClick = actions.onHelp,
            icon = AppIcons.Help,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_feedback),
            onClick = actions.onFeedback,
            icon = AppIcons.Email,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_rate),
            onClick = actions.onRate,
            icon = AppIcons.Star,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_share_app),
            onClick = actions.onShareApp,
            icon = AppIcons.Share,
        )
        AppDivider()
        SettingsNavRow(
            title = stringResource(R.string.settings_about),
            value = versionName,
            onClick = actions.onAbout,
            icon = AppIcons.Info,
        )
        val gallery = actions.onGallery
        if (gallery != null) {
            AppDivider()
            SettingsNavRow(
                title = stringResource(R.string.settings_debug_gallery),
                onClick = gallery,
                icon = AppIcons.Brush,
            )
        }
    }
}

// Previews --------------------------------------------------------------------------------------------------------

private val PreviewSettings =
    AppSettings(
        beep = true,
        productSiteOrder = listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE, ProductSite.AMAZON),
        theme = ThemeMode.DARK,
        languageTag = "hi",
    )

@Composable
private fun SettingsPreview(dark: Boolean, scroll: Int, clearDialog: ClearDialogState? = null) {
    AppTheme(dark = dark) {
        CompositionLocalProvider(LocalBottomBarInset provides BottomNavBarHeight) {
            Box(Modifier.fillMaxSize()) {
                SettingsScreen(
                    state = SettingsUiState(settings = PreviewSettings, clearDialog = clearDialog),
                    onEvent = {},
                    actions = SettingsActions(onGallery = {}),
                    snackbarHostState = SnackbarHostState(),
                    versionName = "1.0.0",
                    scrollState = rememberScrollState(initial = scroll),
                )
                BottomNavBar(selected = BottomTab.Settings, onSelect = {}, modifier = Modifier.align(Alignment.BottomCenter))
                if (clearDialog != null) {
                    ClearHistoryDialog(state = clearDialog, onKeepFavoritesChange = {}, onConfirm = {}, onDismiss = {})
                }
            }
        }
    }
}

@Preview(name = "S54_ScanAndAppearance_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S54_ScanAndAppearance_Light() = SettingsPreview(dark = false, scroll = 0)

@Preview(name = "S54_ScanAndAppearance_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S54_ScanAndAppearance_Dark() = SettingsPreview(dark = true, scroll = 0)

@Preview(name = "S55_PrivacyAndData_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S55_PrivacyAndData_Light() = SettingsPreview(dark = false, scroll = 2200)

@Preview(name = "S55_PrivacyAndData_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S55_PrivacyAndData_Dark() = SettingsPreview(dark = true, scroll = 2200)

@Preview(name = "S56_AboutRow_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S56_AboutRow_Light() = SettingsPreview(dark = false, scroll = 100_000)

@Preview(name = "S56_AboutRow_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S56_AboutRow_Dark() = SettingsPreview(dark = true, scroll = 100_000)

@Preview(name = "S68_ClearHistory_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S68_ClearHistory_Light() =
    SettingsPreview(dark = false, scroll = 2200, clearDialog = ClearDialogState(scanned = 124, created = 18))

@Preview(name = "S68_ClearHistory_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S68_ClearHistory_Dark() =
    SettingsPreview(dark = true, scroll = 2200, clearDialog = ClearDialogState(scanned = 124, created = 18))

@Preview(name = "S77_Settings_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S77_Settings_Light() = SettingsPreview(dark = false, scroll = 0)

@Preview(name = "S77_Settings_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S77_Settings_Dark() = SettingsPreview(dark = true, scroll = 0)
