package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.CameraColors

@Composable
private fun ButtonBase(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    container: Color,
    content: Color,
    border: BorderStroke?,
    height: Dp,
    horizontalPadding: Dp,
) {
    val shape = RoundedCornerShape(height / 2)
    val colors = AppTheme.colors
    val bg = if (enabled) container else colors.surfaceVariant
    val fg = if (enabled) content else colors.inkSecondary.copy(alpha = 0.75f)
    Row(
        modifier = modifier
            .heightIn(min = height)
            .clip(shape)
            .background(bg)
            .then(if (border != null && enabled) Modifier.border(border, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        }
        Text(text, style = AppTheme.type.button, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Filled primary button, 48dp high, pill shaped. Pass `Modifier.fillMaxWidth()` for the full-width variant. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, c.primary, c.onPrimary, null, 48.dp, 24.dp)
}

/** Tonal (primary container) button. */
@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, c.primaryContainer, c.onPrimaryContainer, null, 48.dp, 24.dp)
}

/** Outlined button (named App* to avoid clashing with the Material3 one). */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, Color.Transparent, c.primary, BorderStroke(1.5.dp, c.outline), 48.dp, 24.dp)
}

/** Text button with no container. */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, Color.Transparent, c.primary, null, 48.dp, 12.dp)
}

/** Filled red button for destructive confirmations. */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, c.error, c.onError, null, 48.dp, 24.dp)
}

/** Compact 40dp button for inline use (cards, banners). */
@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val c = AppTheme.colors
    val container = if (tonal) c.primaryContainer else c.primary
    val content = if (tonal) c.onPrimaryContainer else c.onPrimary
    ButtonBase(text, onClick, modifier, enabled, leadingIcon, container, content, null, 40.dp, 16.dp)
}

enum class IconButtonStyle { Plain, Filled, Tonal, Primary, OnCamera, OnCameraActive }

/**
 * 48dp round icon button. [contentDescription] is mandatory for accessibility; pass null only when
 * the icon sits next to its own label.
 */
@Composable
fun IconButtonRounded(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconButtonStyle = IconButtonStyle.Plain,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    tint: Color? = null,
) {
    val c = AppTheme.colors
    val bg: Color
    val fg: Color
    when (style) {
        IconButtonStyle.Plain -> { bg = Color.Transparent; fg = c.ink }
        IconButtonStyle.Filled -> { bg = c.surfaceVariant; fg = c.ink }
        IconButtonStyle.Tonal -> { bg = c.primaryContainer; fg = c.onPrimaryContainer }
        IconButtonStyle.Primary -> { bg = c.primary; fg = c.onPrimary }
        IconButtonStyle.OnCamera -> { bg = CameraColors.glass; fg = CameraColors.onGlass }
        IconButtonStyle.OnCameraActive -> { bg = Color.White; fg = Color(0xFF13142B) }
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = (tint ?: fg).let { if (enabled) it else it.copy(alpha = 0.4f) },
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(name = "Buttons")
@Composable
private fun ButtonsPreview() {
    AppTheme(dark = false) { ButtonsPreviewContent() }
}

@Preview(name = "Buttons dark")
@Composable
private fun ButtonsPreviewDark() {
    AppTheme(dark = true) { ButtonsPreviewContent() }
}

@Composable
private fun ButtonsPreviewContent() {
    androidx.compose.foundation.layout.Column(
        Modifier.background(AppTheme.colors.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PrimaryButton("Generate", {}, leadingIcon = AppIcons.QrCode)
        PrimaryButton("Disabled", {}, enabled = false)
        TonalButton("Tonal", {})
        AppOutlinedButton("Outlined", {})
        AppTextButton("Text", {})
        DestructiveButton("Delete", {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButtonRounded(AppIcons.Copy, "Copy", {}, style = IconButtonStyle.Filled)
            IconButtonRounded(AppIcons.Share, "Share", {}, style = IconButtonStyle.Tonal)
            IconButtonRounded(AppIcons.Star, "Favorite", {}, style = IconButtonStyle.Primary)
        }
    }
}
