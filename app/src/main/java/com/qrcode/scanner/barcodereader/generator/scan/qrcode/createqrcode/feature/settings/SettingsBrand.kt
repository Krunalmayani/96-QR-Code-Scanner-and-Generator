package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Fixed brand colors of the app mark and the privacy hero (design class `.hero` and the About logo). They look the
 * same in light and dark, so they are not theme tokens.
 */
internal object SettingsBrand {
    val heroBrush: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF7A70FF), Color(0xFF4F46E5), Color(0xFF352CB8)),
        )

    /** Gradient behind the launcher mark on the About screen (same stops as `ic_launcher_background`). */
    val logoBrush: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF6D65F5), Color(0xFF4F46E5), Color(0xFF3A33B8)),
        )

    val onHero = Color.White
    val onHeroMuted = Color(0xE0FFFFFF)
    val heroTile = Color(0x33FFFFFF)
}
