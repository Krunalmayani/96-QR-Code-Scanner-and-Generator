package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.About
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.ComponentGalleryRoute
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.ExportHistory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Feedback
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Help
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.LanguagePicker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.PrivacyPolicy
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Settings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.ThemePicker

/** Registers the Settings tab root and its sub-screens (theme, language, help, privacy, feedback, about, export). */
fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    val back: () -> Unit = { navController.popBackStack() }

    composable<Settings> {
        SettingsRoute(
            navigation = SettingsNavigation(
                toTheme = { navController.navigate(ThemePicker) },
                toLanguage = { navController.navigate(LanguagePicker) },
                toHelp = { navController.navigate(Help) },
                toFeedback = { navController.navigate(Feedback) },
                toAbout = { navController.navigate(About) },
                toExport = { navController.navigate(ExportHistory) },
                toPrivacySummary = { navController.navigate(PrivacyPolicy) },
                toComponentGallery = { navController.navigate(ComponentGalleryRoute) },
            ),
        )
    }
    composable<ThemePicker> { ThemePickerRoute(onBack = back) }
    composable<LanguagePicker> { LanguagePickerRoute(onBack = back) }
    composable<Help> { HelpRoute(onBack = back) }
    composable<PrivacyPolicy> { PrivacyPolicyRoute(onBack = back) }
    composable<Feedback> { FeedbackRoute(onBack = back) }
    composable<About> { AboutRoute(onBack = back) }
    composable<ExportHistory> { ExportHistoryRoute(onBack = back) }
}
