package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf

/** Centered empty/error state: big icon disc, title, message, optional action slot. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    tone: Tone = Tone.Primary,
    action: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(c.containerOf(tone)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(48.dp))
        }
        Text(title, style = AppTheme.type.titleLarge, color = c.ink, textAlign = TextAlign.Center)
        if (message != null) {
            Text(message, style = AppTheme.type.body, color = c.inkSecondary, textAlign = TextAlign.Center)
        }
        if (action != null) {
            Box(Modifier.padding(top = 8.dp)) { action() }
        }
    }
}

/** Circular progress in the primary color. */
@Composable
fun LoadingSpinner(modifier: Modifier = Modifier, size: Dp = 48.dp, onCamera: Boolean = false) {
    val c = AppTheme.colors
    val description = stringResource(R.string.common_loading)
    CircularProgressIndicator(
        modifier = modifier.size(size).semantics { contentDescription = description },
        color = if (onCamera) androidx.compose.ui.graphics.Color.White else c.primary,
        trackColor =
            if (onCamera) {
                androidx.compose.ui.graphics.Color.White
                    .copy(alpha = 0.3f)
            } else {
                c.primaryContainer
            },
        strokeWidth = 4.dp,
    )
}

/** Indeterminate linear bar (Wi-Fi connecting, processing). */
@Composable
fun IndeterminateBar(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        color = c.primary,
        trackColor = c.primaryContainer,
    )
}

/** Pager dots; the selected dot is 24dp wide and animates its width. */
@Composable
fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            val width by animateDpAsState(if (index == selected) 24.dp else 8.dp, label = "dotWidth")
            Box(
                Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index == selected) c.primary else c.outline),
            )
        }
    }
}

/** Inline banner for notices and warnings inside a screen. */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Warning,
    icon: ImageVector = AppIcons.Warning,
) {
    val c = AppTheme.colors
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(c.containerOf(tone))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(24.dp))
        Text(text, style = AppTheme.type.bodySmall, color = c.contentOf(tone), modifier = Modifier.weight(1f))
    }
}

@Preview(name = "EmptyState")
@Composable
private fun EmptyStatePreview() {
    AppTheme(dark = false) {
        Box(Modifier.background(AppTheme.colors.background)) {
            EmptyState(
                AppIcons.History,
                "No scans yet",
                message = "Scan a code and it shows up here.",
                action = { PrimaryButton("Scan a code", {}) },
            )
        }
    }
}

@Preview(name = "EmptyState dark")
@Composable
private fun EmptyStatePreviewDark() {
    AppTheme(dark = true) {
        Box(Modifier.background(AppTheme.colors.background)) {
            EmptyState(AppIcons.Star, "No favorites", message = "Tap the star on any result to keep it here.")
        }
    }
}
