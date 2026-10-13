package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf

/** Max content width on tablets; phones are narrower so it has no effect there. */
val MaxContentWidth: Dp = 600.dp

/** Centers [content] and caps its width at [MaxContentWidth]. The nav shell wraps the NavHost in this. */
@Composable
fun MaxWidthContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize().background(AppTheme.colors.background), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = MaxContentWidth).fillMaxSize()) { content() }
    }
}

/**
 * Standard screen frame: background color, optional [topBar]/[bottomBar]/[snackbarHost].
 * Status-bar inset is applied by [AppTopBar]; navigation-bar inset is added here when there is no [bottomBar]
 * ([BottomNavBar] and [ActionBar] handle their own). Apply `Modifier.imePadding()` inside forms.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = AppTheme.colors.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = { bottomBar?.invoke() },
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentColor = AppTheme.colors.ink,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        val bottom = if (bottomBar == null) padding.calculateBottomPadding() + navInset else padding.calculateBottomPadding()
        content(PaddingValues(top = padding.calculateTopPadding(), bottom = bottom))
    }
}

/** 64dp top bar: optional back button, title, trailing [actions]. Handles the status-bar inset. */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.Transparent,
    contentColor: Color = AppTheme.colors.ink,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(64.dp)
            .padding(start = if (onBack != null) 8.dp else 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            IconButtonRounded(AppIcons.Back, stringResource(R.string.common_back), onBack, tint = contentColor)
        }
        Text(
            text = title,
            style = AppTheme.type.topBar,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
    }
}

/** Bordered surface card, radius 20. Clickable when [onClick] is given. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    containerColor: Color = AppTheme.colors.surface,
    borderColor: Color = AppTheme.colors.outline,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = AppTheme.shapes.card
    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Card without padding that clips its children: stack [ListRow]s and [AppDivider]s inside. */
@Composable
fun CardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = AppTheme.shapes.card
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.outline, shape),
        content = content,
    )
}

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline))
}

/** Rounded tile holding an icon: history rows, settings rows, form type grid. */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    size: Dp = 40.dp,
    iconSize: Dp = size / 2,
    contentDescription: String? = null,
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(if (size >= 56.dp) RoundedCornerShape(20.dp) else AppTheme.shapes.tile)
            .background(c.containerOf(tone)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = c.contentOf(tone), modifier = Modifier.size(iconSize))
    }
}

/**
 * Generic list row (min height 64dp). Put it inside a [CardGroup]; use [AppDivider] between rows.
 * [selected] tints the row for multi-select; [onLongClick] enters multi-select.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    titleMaxLines: Int = 1,
) {
    val c = AppTheme.colors
    val clickable = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(role = Role.Button, onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(if (selected) c.primaryContainer.copy(alpha = 0.6f) else Color.Transparent)
            .then(clickable)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppTheme.type.title, color = c.ink, maxLines = titleMaxLines, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = AppTheme.type.caption, color = c.inkSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

/** Label : value pair used in result details. */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueMaxLines: Int = Int.MAX_VALUE,
) {
    Row(modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = AppTheme.type.bodySmall, color = AppTheme.colors.inkSecondary, modifier = Modifier.width(84.dp))
        Text(value, style = AppTheme.type.bodyStrong, color = AppTheme.colors.ink, maxLines = valueMaxLines, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

/** Small uppercase section label ("SCANNING", "APPEARANCE"). Pass the text in normal case; it is uppercased here. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AppTheme.type.label,
        color = AppTheme.colors.inkSecondary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp).semantics { heading() },
    )
}

/** Round quick-action button with a caption beneath (Copy / Share / Favorite on results). */
@Composable
fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    enabled: Boolean = true,
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(c.containerOf(tone)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (tone == Tone.Primary) c.onPrimaryContainer else c.contentOf(tone), modifier = Modifier.size(24.dp))
        }
        Text(label, style = AppTheme.type.captionStrong, color = c.ink, textAlign = TextAlign.Center, maxLines = 1)
    }
}

/** Pinned bottom action area (surface with top border). Handles the navigation-bar inset. */
@Composable
fun ActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().background(AppTheme.colors.surface).navigationBarsPadding()) {
        AppDivider()
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Snackbar host styled like the design (dark pill, lilac action). Pair with a [SnackbarHostState]. */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2B2D45))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(data.visuals.message, style = AppTheme.type.body, color = Color(0xFFF1F2FF), modifier = Modifier.weight(1f))
            val action = data.visuals.actionLabel
            if (action != null) {
                Text(
                    action,
                    style = AppTheme.type.bodyStrong,
                    color = Color(0xFFB7B3FF),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { data.performAction() }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview(name = "Surfaces")
@Composable
private fun SurfacesPreview() {
    AppTheme(dark = false) { SurfacesPreviewContent() }
}

@Preview(name = "Surfaces dark")
@Composable
private fun SurfacesPreviewDark() {
    AppTheme(dark = true) { SurfacesPreviewContent() }
}

@Composable
private fun SurfacesPreviewContent() {
    Column(Modifier.background(AppTheme.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardGroup {
            ListRow("example.com/menu", subtitle = "Website  ·  10:42", leading = { IconTile(AppIcons.Link) }, trailing = { Icon(AppIcons.Chevron, null) }, onClick = {})
            AppDivider()
            ListRow("Home Wi-Fi", subtitle = "Wi-Fi  ·  Yesterday", leading = { IconTile(AppIcons.Wifi, tone = Tone.Success) }, selected = true, onClick = {})
        }
        AppCard { KeyValueRow("Network", "Home Wi-Fi"); AppDivider(); KeyValueRow("Security", "WPA2") }
        Row(Modifier.fillMaxWidth()) {
            QuickAction(AppIcons.Copy, "Copy", {}, Modifier.weight(1f))
            QuickAction(AppIcons.Share, "Share", {}, Modifier.weight(1f))
            QuickAction(AppIcons.Star, "Favorite", {}, Modifier.weight(1f))
        }
    }
}
