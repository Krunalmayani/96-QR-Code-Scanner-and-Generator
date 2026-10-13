package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** Type scale from the design (qr.css): d1, h1, h2, tt, bd, cap, lb. */
@Immutable
data class AppType(
    /** 32/40 extra-bold: splash and onboarding headlines. */
    val display: TextStyle = style(32, 40, FontWeight.ExtraBold, -0.02f),
    /** 22/28 bold: screen headlines. */
    val headline: TextStyle = style(22, 28, FontWeight.Bold, -0.01f),
    /** 20/28 bold: top bar title. */
    val topBar: TextStyle = style(20, 28, FontWeight.Bold),
    /** 18/24 bold: section titles, dialog titles. */
    val titleLarge: TextStyle = style(18, 24, FontWeight.Bold),
    /** 16/24 semi-bold: list titles. */
    val title: TextStyle = style(16, 24, FontWeight.SemiBold),
    /** 14/20 regular: body. */
    val body: TextStyle = style(14, 20, FontWeight.Normal),
    /** 14/20 semi-bold: emphasised body, values. */
    val bodyStrong: TextStyle = style(14, 20, FontWeight.SemiBold),
    /** 15/20 semi-bold: buttons. */
    val button: TextStyle = style(15, 20, FontWeight.SemiBold),
    /** 11/16 semi-bold: bottom navigation labels. */
    val navLabel: TextStyle = style(11, 16, FontWeight.SemiBold),
    /** 15/20 regular: text typed into fields and the search bar. */
    val input: TextStyle = style(15, 20, FontWeight.Normal),
    /** 11/14 semi-bold: the label inside a text field. */
    val fieldLabel: TextStyle = style(11, 14, FontWeight.SemiBold),
    /** 13/20 regular: secondary body text and key labels. */
    val bodySmall: TextStyle = style(13, 20, FontWeight.Normal),
    /** 12/16 semi-bold: captions under round action buttons. */
    val captionStrong: TextStyle = style(12, 16, FontWeight.SemiBold),
    /** 13/20 medium: chips. */
    val chip: TextStyle = style(13, 20, FontWeight.Medium),
    /** 13/20 semi-bold: segmented control and selected chips. */
    val chipStrong: TextStyle = style(13, 20, FontWeight.SemiBold),
    /** 12/16 medium: captions and helper text. */
    val caption: TextStyle = style(12, 16, FontWeight.Medium),
    /** 11/16 bold, tracked: overline labels (use with uppercase text). */
    val label: TextStyle = style(11, 16, FontWeight.Bold, 0.06f),
    val mono: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Float = 0f) = TextStyle(
    fontFamily = InterFamily,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.em,
)

/** Material typography mapped onto the design scale so stock M3 widgets also use Inter. */
internal fun materialTypography(type: AppType): Typography {
    val base = Typography()
    fun TextStyle.inter() = copy(fontFamily = InterFamily)
    return base.copy(
        displayLarge = base.displayLarge.inter(),
        displayMedium = base.displayMedium.inter(),
        displaySmall = type.display,
        headlineLarge = base.headlineLarge.inter(),
        headlineMedium = type.headline,
        headlineSmall = base.headlineSmall.inter(),
        titleLarge = type.topBar,
        titleMedium = type.title,
        titleSmall = type.bodyStrong,
        bodyLarge = type.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = type.body,
        bodySmall = type.caption,
        labelLarge = type.button,
        labelMedium = type.caption,
        labelSmall = type.label,
    )
}

val SingleLine = TextOverflow.Ellipsis
