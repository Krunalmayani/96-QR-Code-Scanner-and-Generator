package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.SettingsActions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.SettingsEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.SettingsScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.SettingsUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.rememberTestSnackbarHost
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Settings list: every switch row toggles through [SettingsEvent]s and the navigation rows call their action. */
class SettingsScreenTest : ThemedComposeTest() {
    private val events = mutableListOf<SettingsEvent>()
    private var themeOpened = 0
    private var languageOpened = 0

    @Composable
    private fun Host(initial: AppSettings) {
        var settings by remember { mutableStateOf(initial) }
        CompositionLocalProvider(LocalBottomBarInset provides 80.dp) {
            SettingsScreen(
                state = SettingsUiState(settings = settings),
                onEvent = { event ->
                    events += event
                    settings =
                        when (event) {
                            is SettingsEvent.SetVibrate -> settings.copy(vibrate = event.on)
                            is SettingsEvent.SetBeep -> settings.copy(beep = event.on)
                            is SettingsEvent.SetFlashOnStart -> settings.copy(flashOnStart = event.on)
                            is SettingsEvent.SetAutoOpenLinks -> settings.copy(autoOpenLinks = event.on)
                            is SettingsEvent.SetContinuousScan -> settings.copy(continuousScan = event.on)
                            is SettingsEvent.SetScanAreaOnly -> settings.copy(scanAreaOnly = event.on)
                            is SettingsEvent.SetSaveHistory -> settings.copy(saveHistory = event.on)
                            is SettingsEvent.SetAnalytics -> settings.copy(analyticsEnabled = event.on)
                            else -> settings
                        }
                },
                actions =
                    SettingsActions(
                        onTheme = { themeOpened++ },
                        onLanguage = { languageOpened++ },
                    ),
                snackbarHostState = rememberTestSnackbarHost(),
                versionName = "1.2.3",
            )
        }
    }

    private fun show(settings: AppSettings = AppSettings()) {
        setThemedContent { Host(settings) }
    }

    private fun toggle(titleRes: Int) = composeRule.onNodeWithText(Res.string(titleRes))

    @Test
    fun vibrateIsOnByDefaultAndSwitchesOffAndOn() {
        show()

        toggle(R.string.settings_vibrate).assertIsOn()
        toggle(R.string.settings_vibrate).performClick()
        toggle(R.string.settings_vibrate).assertIsOff()
        toggle(R.string.settings_vibrate).performClick()
        toggle(R.string.settings_vibrate).assertIsOn()

        assertEquals(listOf(SettingsEvent.SetVibrate(false), SettingsEvent.SetVibrate(true)), events)
    }

    @Test
    fun scanningSwitchesToggleTheirOwnSetting() {
        show()

        toggle(R.string.settings_beep).assertIsOff()
        toggle(R.string.settings_beep).performClick()
        toggle(R.string.settings_beep).assertIsOn()

        toggle(R.string.settings_flash_on_start).performClick()
        toggle(R.string.settings_flash_on_start).assertIsOn()

        toggle(R.string.settings_auto_open).performScrollTo().performClick()
        toggle(R.string.settings_auto_open).assertIsOn()

        toggle(R.string.settings_continuous).performScrollTo().performClick()
        toggle(R.string.settings_continuous).assertIsOn()

        assertEquals(
            listOf(
                SettingsEvent.SetBeep(true),
                SettingsEvent.SetFlashOnStart(true),
                SettingsEvent.SetAutoOpenLinks(true),
                SettingsEvent.SetContinuousScan(true),
            ),
            events,
        )
    }

    @Test
    fun autoOpenLinksIsOffByDefaultAsThePrdRequires() {
        show()

        toggle(R.string.settings_auto_open).performScrollTo().assertIsOff()
    }

    @Test
    fun privacySwitchesToggle() {
        show()

        toggle(R.string.settings_save_history).performScrollTo().assertIsOn()
        toggle(R.string.settings_save_history).performClick()
        toggle(R.string.settings_save_history).assertIsOff()

        toggle(R.string.settings_analytics).performScrollTo().performClick()
        toggle(R.string.settings_analytics).assertIsOff()

        assertEquals(listOf(SettingsEvent.SetSaveHistory(false), SettingsEvent.SetAnalytics(false)), events)
    }

    @Test
    fun themeAndLanguageRowsOpenTheirPickers() {
        show()

        toggle(R.string.settings_theme).performScrollTo().assertIsDisplayed()
        toggle(R.string.settings_theme).performClick()
        toggle(R.string.settings_language).performScrollTo().performClick()

        assertEquals(1, themeOpened)
        assertEquals(1, languageOpened)
    }
}
