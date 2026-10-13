package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalAppType = staticCompositionLocalOf { AppType() }

/** Accessor for design tokens: `AppTheme.colors.primary`, `AppTheme.type.body`, `AppTheme.shapes.card`. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val type: AppType
        @Composable @ReadOnlyComposable
        get() = LocalAppType.current

    val shapes: AppShapes get() = AppShapes
}

/** True when [mode] resolves to dark right now (SYSTEM follows the OS). */
@Composable
fun isDarkFor(mode: ThemeMode): Boolean =
    when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

/** App theme. Colors cross-fade over 250 ms when the user switches theme. */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isDarkFor(themeMode)
    AppTheme(dark = dark, content = content)
}

@Composable
fun AppTheme(
    dark: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = animatedColors(if (dark) DarkAppColors else LightAppColors)
    val type = remember { AppType() }
    val scheme =
        if (dark) {
            darkColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                primaryContainer = colors.primaryContainer,
                onPrimaryContainer = colors.onPrimaryContainer,
                secondary = colors.success,
                onSecondary = colors.onPrimary,
                secondaryContainer = colors.successContainer,
                onSecondaryContainer = colors.onSuccessContainer,
                background = colors.background,
                onBackground = colors.ink,
                surface = colors.surface,
                onSurface = colors.ink,
                surfaceVariant = colors.surfaceVariant,
                onSurfaceVariant = colors.inkSecondary,
                outline = colors.inkSecondary,
                outlineVariant = colors.outline,
                error = colors.error,
                onError = colors.onError,
                errorContainer = colors.errorContainer,
                scrim = Color.Black,
            )
        } else {
            lightColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                primaryContainer = colors.primaryContainer,
                onPrimaryContainer = colors.onPrimaryContainer,
                secondary = colors.success,
                onSecondary = colors.onPrimary,
                secondaryContainer = colors.successContainer,
                onSecondaryContainer = colors.onSuccessContainer,
                background = colors.background,
                onBackground = colors.ink,
                surface = colors.surface,
                onSurface = colors.ink,
                surfaceVariant = colors.surfaceVariant,
                onSurfaceVariant = colors.inkSecondary,
                outline = colors.inkSecondary,
                outlineVariant = colors.outline,
                error = colors.error,
                onError = colors.onError,
                errorContainer = colors.errorContainer,
                scrim = Color.Black,
            )
        }
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppType provides type,
    ) {
        MaterialTheme(colorScheme = scheme, typography = materialTypography(type)) {
            CompositionLocalProvider(LocalContentColor provides colors.ink, content = content)
        }
    }
}

@Composable
private fun animatedColors(target: AppColors): AppColors {
    @Composable
    fun anim(c: Color): Color = animateColorAsState(c, tween(250), label = "themeColor").value
    return AppColors(
        isDark = target.isDark,
        background = anim(target.background),
        surface = anim(target.surface),
        surfaceVariant = anim(target.surfaceVariant),
        ink = anim(target.ink),
        inkSecondary = anim(target.inkSecondary),
        outline = anim(target.outline),
        primary = anim(target.primary),
        onPrimary = anim(target.onPrimary),
        primaryContainer = anim(target.primaryContainer),
        onPrimaryContainer = anim(target.onPrimaryContainer),
        success = anim(target.success),
        successContainer = anim(target.successContainer),
        onSuccessContainer = anim(target.onSuccessContainer),
        error = anim(target.error),
        onError = anim(target.onError),
        errorContainer = anim(target.errorContainer),
        warning = anim(target.warning),
        warningContainer = anim(target.warningContainer),
        scrim = anim(target.scrim),
    )
}
