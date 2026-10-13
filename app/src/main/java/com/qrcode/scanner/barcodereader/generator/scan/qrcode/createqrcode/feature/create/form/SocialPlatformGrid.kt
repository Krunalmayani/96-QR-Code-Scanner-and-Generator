package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.SocialPlatform

private const val PLATFORM_COLUMNS = 4
private const val PLATFORM_COLUMNS_LARGE_TEXT = 3
private const val LARGE_FONT_SCALE = 1.3f
private val TileSize = 48.dp
private val TileSlot = 56.dp
private val TileRadius = 16.dp
private val RingInner = 3.dp
private val RingOuter = 5.dp

/** Brand colors of the platform marks (design s38). They identify the platforms, so they are the same in light and dark. */
private val InstagramBrush = Brush.linearGradient(listOf(Color(0xFF8E5CF6), Color(0xFFD6489A)))
private val FacebookColor = Color(0xFF3B6FD8)
private val XColor = Color(0xFF13142B)
private val LinkedInColor = Color(0xFF1E6FA8)
private val YouTubeColor = Color(0xFFD63A3A)
private val WhatsAppColor = Color(0xFF1FA064)
private val TelegramColor = Color(0xFF2E9BD6)
private val MarkColor = Color.White

private fun SocialPlatform.brand(): Brush =
    when (this) {
        SocialPlatform.INSTAGRAM -> InstagramBrush
        SocialPlatform.FACEBOOK -> SolidColor(FacebookColor)
        SocialPlatform.X -> SolidColor(XColor)
        SocialPlatform.LINKEDIN -> SolidColor(LinkedInColor)
        SocialPlatform.YOUTUBE -> SolidColor(YouTubeColor)
        SocialPlatform.WHATSAPP -> SolidColor(WhatsAppColor)
        SocialPlatform.TELEGRAM -> SolidColor(TelegramColor)
    }

@StringRes
private fun SocialPlatform.labelRes(): Int =
    when (this) {
        SocialPlatform.INSTAGRAM -> R.string.create_social_instagram
        SocialPlatform.FACEBOOK -> R.string.create_social_facebook
        SocialPlatform.X -> R.string.create_social_x
        SocialPlatform.LINKEDIN -> R.string.create_social_linkedin
        SocialPlatform.YOUTUBE -> R.string.create_social_youtube
        SocialPlatform.WHATSAPP -> R.string.create_social_whatsapp
        SocialPlatform.TELEGRAM -> R.string.create_social_telegram
    }

@StringRes
private fun SocialPlatform.markRes(): Int =
    when (this) {
        SocialPlatform.INSTAGRAM -> R.string.create_social_mark_instagram
        SocialPlatform.FACEBOOK -> R.string.create_social_mark_facebook
        SocialPlatform.X -> R.string.create_social_mark_x
        SocialPlatform.LINKEDIN -> R.string.create_social_mark_linkedin
        SocialPlatform.YOUTUBE -> R.string.create_social_mark_youtube
        SocialPlatform.WHATSAPP -> R.string.create_social_mark_whatsapp
        SocialPlatform.TELEGRAM -> R.string.create_social_mark_telegram
    }

/**
 * Platform picker of the Social form (design s38): rounded brand tiles with the platform name below, the chosen one
 * ringed and named in bold. Each tile is one radio button read out by its name; the two-letter mark is decoration.
 */
@Composable
internal fun SocialPlatformGrid(
    selected: SocialPlatform,
    onSelect: (SocialPlatform) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = if (LocalDensity.current.fontScale > LARGE_FONT_SCALE) PLATFORM_COLUMNS_LARGE_TEXT else PLATFORM_COLUMNS
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (rowItems in SocialPlatform.entries.chunked(columns)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (platform in rowItems) {
                    PlatformTile(
                        platform = platform,
                        label = stringResource(platform.labelRes()),
                        selected = platform == selected,
                        onClick = { onSelect(platform) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PlatformTile(
    platform: SocialPlatform,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(TileRadius)
    val ringInner = colors.primaryContainer
    val ringOuter = colors.primary
    Column(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(TileSlot)
                    .drawBehind {
                        if (selected) {
                            drawRing(RingOuter.toPx(), TileRadius.toPx() + RingOuter.toPx(), ringOuter)
                            drawRing(RingInner.toPx(), TileRadius.toPx() + RingInner.toPx(), ringInner)
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(TileSize)
                        .clip(shape)
                        .background(platform.brand())
                        .border(1.dp, colors.outline, shape)
                        .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(platform.markRes()),
                    style = AppTheme.type.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MarkColor,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = label,
            style = if (selected) AppTheme.type.captionStrong else AppTheme.type.caption,
            color = if (selected) colors.ink else colors.inkSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A filled rounded square [grow] larger than the tile on every side (the ring is drawn as two stacked squares). */
private fun DrawScope.drawRing(grow: Float, radius: Float, color: Color) {
    val tile = TileSize.toPx()
    val offset = (size.width - tile) / 2f - grow
    drawRoundRect(
        color = color,
        topLeft = Offset(offset, offset),
        size = Size(tile + grow * 2f, tile + grow * 2f),
        cornerRadius = CornerRadius(radius, radius),
    )
}
