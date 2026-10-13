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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.BannerAd
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.isPrivacyOptionsRequired
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.showPrivacyOptionsForm
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBarHeight
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
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
 * [versionName] and [versionCode] are shown in the About header and on the About row.
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
    versionCode: Int = 1,
) {
    val bottomInset = LocalBottomBarInset.current
    AppScaffold(
        modifier = modifier,
        topBar = { SettingsTitleBar(stringResource(R.string.settings_title)) },
        snackbarHost = { AppSnackbarHost(snackbarHostState, Modifier.padding(bottom = bottomInset)) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                ScanningSection(state.settings, onEvent)
                AppearanceSection(state.settings, actions)
                PrivacySection(state.settings, onEvent, actions)
                AboutSection(versionName, versionCode, actions)
            }
            BannerAd(Modifier.fillMaxWidth())
            Spacer(Modifier.height(bottomInset))
        }
    }
}

/**
 * Tab-root title bar of the design: 24/32 extra-bold title indented 24dp, 12dp above the content line
 * (AppTopBar only offers the 20sp title of sub-screens).
 */
@Composable
private fun SettingsTitleBar(title: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 24.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = AppTheme.type.topBar.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = AppTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun ScanningSection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit) {
    SettingsSectionLabel(stringResource(R.string.settings_section_scanning), topPadding = 0.dp)
    CardGroup {
        SettingsSwitchItem(
            title = stringResource(R.string.settings_beep),
            checked = settings.beep,
            onCheckedChange = { onEvent(SettingsEvent.SetBeep(it)) },
            icon = AppIcons.Sound,
        )
        AppDivider()
        SettingsSwitchItem(
            title = stringResource(R.string.settings_vibrate),
            checked = settings.vibrate,
            onCheckedChange = { onEvent(SettingsEvent.SetVibrate(it)) },
            icon = AppIcons.Vibrate,
        )
        AppDivider()
        SettingsSwitchItem(
            title = stringResource(R.string.settings_flash_on_start),
            checked = settings.flashOnStart,
            onCheckedChange = { onEvent(SettingsEvent.SetFlashOnStart(it)) },
            icon = AppIcons.FlashOn,
        )
        AppDivider()
        SettingsSwitchItem(
            title = stringResource(R.string.settings_auto_open),
            subtitle = stringResource(R.string.settings_auto_open_hint),
            checked = settings.autoOpenLinks,
            onCheckedChange = { onEvent(SettingsEvent.SetAutoOpenLinks(it)) },
            icon = AppIcons.Link,
        )
        AppDivider()
        SettingsSwitchItem(
            title = stringResource(R.string.settings_continuous),
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
            SettingsTile(AppIcons.Scan)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_scan_area), style = settingsTitleStyle(), color = c.ink)
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
            SettingsTile(AppIcons.Product)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_sites_title), style = settingsTitleStyle(), color = c.ink)
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
    SettingsRow(
        title = name,
        verticalPadding = 4.dp,
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
                )
                IconButtonRounded(
                    icon = AppIcons.ExpandMore,
                    contentDescription = stringResource(R.string.settings_site_move_down, name),
                    onClick = { onMove(site, false) },
                    enabled = canMoveDown,
                )
            }
        },
    )
}

@Composable
private fun AppearanceSection(settings: AppSettings, actions: SettingsActions) {
    SettingsSectionLabel(stringResource(R.string.settings_section_appearance))
    CardGroup {
        SettingsNavItem(
            title = stringResource(R.string.settings_theme),
            value = stringResource(themeLabelRes(settings.theme)),
            onClick = actions.onTheme,
            icon = AppIcons.Palette,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_language),
            value = languageLabel(settings.languageTag),
            onClick = actions.onLanguage,
            icon = AppIcons.Language,
        )
    }
}

@Composable
private fun PrivacySection(settings: AppSettings, onEvent: (SettingsEvent) -> Unit, actions: SettingsActions) {
    SettingsSectionLabel(stringResource(R.string.settings_section_privacy))
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
    SettingsSectionLabel(stringResource(R.string.settings_section_data))
    CardGroup {
        SettingsSwitchItem(
            title = stringResource(R.string.settings_save_history),
            checked = settings.saveHistory,
            onCheckedChange = { onEvent(SettingsEvent.SetSaveHistory(it)) },
            icon = AppIcons.History,
        )
        AppDivider()
        SettingsSwitchItem(
            title = stringResource(R.string.settings_analytics),
            subtitle = stringResource(R.string.settings_analytics_hint),
            checked = settings.analyticsEnabled,
            onCheckedChange = { onEvent(SettingsEvent.SetAnalytics(it)) },
            icon = AppIcons.Info,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_clear_history),
            onClick = { onEvent(SettingsEvent.ClearHistoryClicked) },
            icon = AppIcons.Delete,
            tone = Tone.Error,
            titleColor = AppTheme.colors.error,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_export_history),
            onClick = actions.onExport,
            icon = AppIcons.Document,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_privacy_summary),
            onClick = actions.onPrivacySummary,
            icon = AppIcons.Shield,
        )
        AppDivider()
        SettingsLinkItem(
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
    SettingsNavItem(
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

/** Teal notice at the top of the privacy section (s55). */
@Composable
private fun PrivacyBanner() {
    val c = AppTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(c.successContainer)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(AppIcons.Shield, contentDescription = null, tint = c.onSuccessContainer)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.settings_privacy_banner_title),
                style = AppTheme.type.bodyStrong,
                color = c.onSuccessContainer,
            )
            Text(
                stringResource(R.string.settings_privacy_banner_text),
                style = AppTheme.type.bodySmall,
                color = c.onSuccessContainer,
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
    SettingsRow(
        title = stringResource(title),
        subtitle = stringResource(hint),
        leading = { SettingsTile(icon) },
    )
}

@Composable
private fun AboutSection(versionName: String, versionCode: Int, actions: SettingsActions) {
    AppIdentity(versionName, versionCode)
    SettingsSectionLabel(stringResource(R.string.settings_section_about), topPadding = 0.dp)
    CardGroup {
        SettingsNavItem(
            title = stringResource(R.string.settings_help),
            onClick = actions.onHelp,
            icon = AppIcons.Help,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_feedback),
            onClick = actions.onFeedback,
            icon = AppIcons.Email,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_rate),
            onClick = actions.onRate,
            icon = AppIcons.Star,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_share_app),
            onClick = actions.onShareApp,
            icon = AppIcons.Share,
        )
        AppDivider()
        SettingsNavItem(
            title = stringResource(R.string.settings_about),
            value = versionName,
            onClick = actions.onAbout,
            icon = AppIcons.Info,
        )
        val gallery = actions.onGallery
        if (gallery != null) {
            AppDivider()
            SettingsNavItem(
                title = stringResource(R.string.settings_debug_gallery),
                onClick = gallery,
                icon = AppIcons.Brush,
            )
        }
    }
    Disclaimer()
}

/** App mark, name and version above the About rows (s56). */
@Composable
private fun AppIdentity(versionName: String, versionCode: Int) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 22.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(SettingsBrand.heroBrush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Scan, contentDescription = null, tint = SettingsBrand.onHero, modifier = Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = AppTheme.type.title, color = c.ink)
            Text(
                stringResource(R.string.settings_about_version, versionName, versionCode),
                style = AppTheme.type.caption,
                color = c.inkSecondary,
            )
        }
    }
}

/** Neutral notice that the app offers no payment or financial services (end of the list in s56). */
@Composable
private fun Disclaimer() {
    val c = AppTheme.colors
    Row(
        modifier =
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(c.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(AppIcons.Info, contentDescription = null, tint = c.inkSecondary)
        Text(stringResource(R.string.settings_about_disclaimer), style = AppTheme.type.bodySmall, color = c.inkSecondary)
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
private fun S55_PrivacyAndData_Light() = SettingsPreview(dark = false, scroll = 830)

@Preview(name = "S55_PrivacyAndData_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S55_PrivacyAndData_Dark() = SettingsPreview(dark = true, scroll = 830)

@Preview(name = "S56_AboutRow_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S56_AboutRow_Light() = SettingsPreview(dark = false, scroll = 100_000)

@Preview(name = "S56_AboutRow_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S56_AboutRow_Dark() = SettingsPreview(dark = true, scroll = 100_000)

@Preview(name = "S68_ClearHistory_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S68_ClearHistory_Light() =
    SettingsPreview(dark = false, scroll = 1100, clearDialog = ClearDialogState(scanned = 124, created = 18))

@Preview(name = "S68_ClearHistory_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S68_ClearHistory_Dark() =
    SettingsPreview(dark = true, scroll = 1100, clearDialog = ClearDialogState(scanned = 124, created = 18))

@Preview(name = "S77_Settings_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S77_Settings_Light() = SettingsPreview(dark = false, scroll = 0)

@Preview(name = "S77_Settings_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S77_Settings_Dark() = SettingsPreview(dark = true, scroll = 0)
