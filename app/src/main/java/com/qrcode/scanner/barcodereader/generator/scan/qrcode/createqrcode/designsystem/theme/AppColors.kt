package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Semantic color roles of the design (see Design artifact "Design system" board). */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    /** "surface-2": inputs, segmented tracks, disabled fills. */
    val surfaceVariant: Color,
    val ink: Color,
    val inkSecondary: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    /** Teal accent: success and "code detected". */
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val scrim: Color,
)

val ScanHitColor = Color(0xFF34E0C5)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF5F6FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFECEEF9),
    ink = Color(0xFF13142B),
    inkSecondary = Color(0xFF575B78),
    outline = Color(0xFFE1E3F1),
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7E5FF),
    onPrimaryContainer = Color(0xFF2B2594),
    success = Color(0xFF0F9F8E),
    successContainer = Color(0xFFD3F4EE),
    onSuccessContainer = Color(0xFF0B5F55),
    error = Color(0xFFC62828),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDE4E4),
    warning = Color(0xFF9A5B00),
    warningContainer = Color(0xFFFFF0D1),
    scrim = Color(0x8013142B),
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0D0E1B),
    surface = Color(0xFF171829),
    surfaceVariant = Color(0xFF212340),
    ink = Color(0xFFF1F2FF),
    inkSecondary = Color(0xFFA6AAC8),
    outline = Color(0xFF2A2C4A),
    primary = Color(0xFFA5A0FF),
    onPrimary = Color(0xFF14123F),
    primaryContainer = Color(0xFF2D2A78),
    onPrimaryContainer = Color(0xFFE2E0FF),
    success = Color(0xFF2DD4BF),
    successContainer = Color(0xFF10403B),
    onSuccessContainer = Color(0xFF9BF0E4),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF2B0A0A),
    errorContainer = Color(0xFF4A1F24),
    warning = Color(0xFFFFC56B),
    warningContainer = Color(0xFF43320F),
    scrim = Color(0x9E000000),
)

/** Colors used on top of the camera preview; identical in light and dark. */
object CameraColors {
    val glass = Color(0x24FFFFFF)
    val glassStrong = Color(0x3DFFFFFF)
    val onGlass = Color(0xFFFFFFFF)
    val sheetHandle = Color(0x66FFFFFF)
    val scanLine = Color(0xFF8F88FF)
    val hit = ScanHitColor
    val dim = Color(0x8C05060C)
}

/** Visual weight of an icon tile, badge or banner. */
enum class Tone { Primary, Success, Warning, Error }

fun AppColors.containerOf(tone: Tone): Color = when (tone) {
    Tone.Primary -> primaryContainer
    Tone.Success -> successContainer
    Tone.Warning -> warningContainer
    Tone.Error -> errorContainer
}

fun AppColors.contentOf(tone: Tone): Color = when (tone) {
    Tone.Primary -> primary
    Tone.Success -> onSuccessContainer
    Tone.Warning -> warning
    Tone.Error -> error
}

/** Text colour for badges (slightly stronger than the icon colour for the primary tone). */
fun AppColors.badgeContentOf(tone: Tone): Color = when (tone) {
    Tone.Primary -> onPrimaryContainer
    else -> contentOf(tone)
}
