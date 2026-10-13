package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.badgeContentOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf

/**
 * Notice box of the Create screens, drawn the way the artboards do it (the shared InfoBanner is a little tighter).
 * The default size is the tip box under the forms (radius 20, 14/16 padding, 13/18 text, icon top-aligned);
 * [compact] is the status line of the editor (radius 16, 12/14 padding, semi-bold text). The text carries the whole
 * meaning, the tone only tints it. [live] announces changes to screen readers (editor warnings that appear while editing).
 */
@Composable
internal fun NoticeBanner(
    text: String,
    tone: Tone,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    live: Boolean = false,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(if (compact) 16.dp else 20.dp)
    val content = colors.badgeContentOf(tone)
    val base = AppTheme.type.bodySmall.copy(lineHeight = 18.sp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.containerOf(tone))
                .then(if (live) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier)
                .padding(horizontal = if (compact) 14.dp else 16.dp, vertical = if (compact) 12.dp else 14.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        Text(
            text = text,
            style = if (compact) base.copy(fontWeight = FontWeight.SemiBold) else base,
            color = content,
            modifier = Modifier.weight(1f),
        )
    }
}
