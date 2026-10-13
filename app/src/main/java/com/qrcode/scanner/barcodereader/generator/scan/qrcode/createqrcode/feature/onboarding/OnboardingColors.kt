package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Fixed brand colors of the onboarding hero (design class `.hero`). The hero looks the same in light and
 * dark, so these are not theme tokens.
 */
internal object OnboardingColors {
    val heroBrush: Brush =
        Brush.linearGradient(
            colors = listOf(Color(0xFF7A70FF), Color(0xFF4F46E5), Color(0xFF352CB8)),
        )
    val glow = Color(0x14FFFFFF)
    val onHero = Color.White
    val chipSurface = Color.White
    val chipSuccessText = Color(0xFF0B5F55)
    val chipSuccessIcon = Color(0xFF0F9F8E)
    val chipPrimaryText = Color(0xFF2B2594)
    val tileIcon = Color(0xFF4F46E5)
    val detectedBracket = Color(0xFF34E0C5)
    val qrInk = Color(0xFF13142B)
    val qrAccent = Color(0xFF4F46E5)
    val cardShadow = Color(0x80140A64)
}
