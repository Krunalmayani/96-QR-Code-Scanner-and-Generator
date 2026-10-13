package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSearchBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSlider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppToastHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ColorSwatch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ConfirmDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.DestructiveButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IndeterminateBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.InfoBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.KeyValueRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LoadingSpinner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PageDots
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.QuickAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsNavRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsRadioRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsSwitchRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.rememberToastState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import kotlinx.coroutines.launch

/**
 * Debug-only catalogue of every design-system component (opened from Settings in debug builds).
 * A theme switch at the top flips light/dark so both can be reviewed on a device.
 */
@Composable
fun ComponentGallery(onBack: () -> Unit) {
    var dark by rememberSaveable { mutableStateOf(false) }
    AppTheme(dark = dark) {
        GalleryContent(dark = dark, onToggleDark = { dark = !dark }, onBack = onBack)
    }
}

@Composable
private fun GalleryContent(dark: Boolean, onToggleDark: () -> Unit, onBack: () -> Unit) {
    val toast = rememberToastState()
    val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    var segment by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var switch by remember { mutableStateOf(true) }
    var radio by remember { mutableIntStateOf(0) }
    var slider by remember { mutableStateOf(0.4f) }
    var swatch by remember { mutableIntStateOf(0) }
    val sections = remember { listOf("Buttons", "Chips and badges", "Inputs", "Lists and settings", "Feedback", "Overlays", "Navigation") }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Component gallery",
                onBack = onBack,
                actions = {
                    IconButtonRounded(
                        if (dark) AppIcons.LightMode else AppIcons.DarkMode,
                        if (dark) "Switch to light" else "Switch to dark",
                        onToggleDark,
                    )
                },
            )
        },
        bottomBar = { BottomNavBar(BottomTab.Settings, {}) },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sections) { section ->
                    SectionLabel(section)
                    when (section) {
                        "Buttons" ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PrimaryButton("Primary", {}, Modifier.fillMaxWidth(), leadingIcon = AppIcons.QrCode)
                                PrimaryButton("Disabled", {}, Modifier.fillMaxWidth(), enabled = false)
                                TonalButton("Tonal", {}, Modifier.fillMaxWidth())
                                AppOutlinedButton("Outlined", {}, Modifier.fillMaxWidth())
                                AppTextButton("Text button", {})
                                DestructiveButton("Delete", {}, Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButtonRounded(AppIcons.Copy, "Copy", {}, style = IconButtonStyle.Filled)
                                    IconButtonRounded(AppIcons.Share, "Share", {}, style = IconButtonStyle.Tonal)
                                    IconButtonRounded(AppIcons.Star, "Favorite", {}, style = IconButtonStyle.Primary)
                                    IconButtonRounded(AppIcons.FlashOn, "Flash", {}, style = IconButtonStyle.Plain)
                                }
                            }
                        "Chips and badges" ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ChipRow(
                                    listOf(
                                        ChipItem("All", true),
                                        ChipItem("Favorites", false, AppIcons.Star),
                                        ChipItem("Links", false),
                                        ChipItem("Wi-Fi", false),
                                    ),
                                    {
                                    },
                                    contentPadding =
                                        androidx.compose.foundation.layout
                                            .PaddingValues(0.dp),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppBadge("Website", icon = AppIcons.Link)
                                    AppBadge("Secure", tone = Tone.Success)
                                    AppBadge("Not secure", tone = Tone.Warning)
                                    AppBadge("Unsafe", tone = Tone.Error)
                                }
                                SegmentedControl(listOf("QR", "Barcode", "All"), segment, { segment = it })
                            }
                        "Inputs" ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                AppSearchBar(query, { query = it }, "Search history")
                                AppTextField(
                                    text,
                                    { text = it },
                                    "Website URL",
                                    helper = "We add https:// for you",
                                    placeholder = "example.com",
                                )
                                AppTextField("exa mple", {}, "Website URL", error = "Enter a valid web address")
                                AppSlider(slider, { slider = it })
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0xFF13142B, 0xFF4F46E5, 0xFF0F9F8E, 0xFFC62828).forEachIndexed { i, argb ->
                                        ColorSwatch(Color(argb), swatch == i, { swatch = i }, "Color ${i + 1}")
                                    }
                                }
                            }
                        "Lists and settings" ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CardGroup {
                                    ListRow("example.com/menu", subtitle = "Website  ·  10:42", leading = {
                                        IconTile(AppIcons.Link)
                                    }, trailing = { Icon(AppIcons.Star, null, tint = AppTheme.colors.primary) }, onClick = {})
                                    AppDivider()
                                    ListRow(
                                        "Home Wi-Fi",
                                        subtitle = "Wi-Fi  ·  Yesterday",
                                        leading = { IconTile(AppIcons.Wifi, tone = Tone.Success) },
                                        onClick = {},
                                    )
                                }
                                CardGroup {
                                    SettingsSwitchRow(
                                        "Vibrate",
                                        switch,
                                        { switch = it },
                                        subtitle = "Short buzz on every scan",
                                        icon = AppIcons.Vibrate,
                                    )
                                    AppDivider()
                                    SettingsRadioRow("System", radio == 0, { radio = 0 })
                                    AppDivider()
                                    SettingsRadioRow("Dark", radio == 1, { radio = 1 })
                                    AppDivider()
                                    SettingsNavRow("Language", {}, value = "English", icon = AppIcons.Language)
                                }
                                AppCard {
                                    KeyValueRow("Network", "Home Wi-Fi")
                                    AppDivider()
                                    KeyValueRow("Security", "WPA2")
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    QuickAction(AppIcons.Copy, "Copy", {}, Modifier.weight(1f))
                                    QuickAction(AppIcons.Share, "Share", {}, Modifier.weight(1f))
                                    QuickAction(AppIcons.Star, "Favorite", {}, Modifier.weight(1f))
                                }
                            }
                        "Feedback" ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBanner("This link uses http, so it is not encrypted.")
                                InfoBanner("Saved", tone = Tone.Success, icon = AppIcons.CheckCircle)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    LoadingSpinner()
                                    PageDots(3, 1)
                                }
                                IndeterminateBar()
                                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    EmptyState(AppIcons.History, "No scans yet", message = "Scan a code and it shows up here.")
                                }
                            }
                        "Overlays" ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TonalButton("Sheet", { sheet = true })
                                TonalButton("Dialog", { dialog = true })
                                TonalButton("Toast", { scope.launch { toast.show("Saved to gallery") } })
                            }
                        else ->
                            Text(
                                "Bottom navigation is pinned at the bottom of this screen.",
                                style = AppTheme.type.body,
                                color = AppTheme.colors.inkSecondary,
                            )
                    }
                }
            }
            AppToastHost(toast, Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
        }
    }

    if (sheet) {
        AppBottomSheet(onDismissRequest = { sheet = false }) {
            Text("Bottom sheet", style = AppTheme.type.titleLarge, color = AppTheme.colors.ink)
            Text(
                "Swipe down or tap outside to close.",
                style = AppTheme.type.body,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            PrimaryButton("Got it", { sheet = false }, Modifier.fillMaxWidth())
        }
    }
    if (dialog) {
        ConfirmDialog(
            title = "Delete this item?",
            message = "This can't be undone.",
            confirmText = "Delete",
            onConfirm = { dialog = false },
            onDismiss = { dialog = false },
            destructive = true,
            icon = AppIcons.Delete,
        )
    }
}

@Preview(name = "Gallery light", heightDp = 900)
@Composable
private fun GalleryPreviewLight() {
    AppTheme(dark = false) { GalleryContent(dark = false, onToggleDark = {}, onBack = {}) }
}

@Preview(name = "Gallery dark", heightDp = 900)
@Composable
private fun GalleryPreviewDark() {
    AppTheme(dark = true) { GalleryContent(dark = true, onToggleDark = {}, onBack = {}) }
}
