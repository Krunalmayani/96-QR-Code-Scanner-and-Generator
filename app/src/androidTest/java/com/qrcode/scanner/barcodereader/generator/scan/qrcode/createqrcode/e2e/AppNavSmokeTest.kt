package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.e2e

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.HiltTestActivity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.AppNav
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Launches the real `AppNav` (including the live Scan destination, whatever state the camera permission is in) and
 * checks the shell: first-run onboarding and switching between the four tabs.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppNavSmokeTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var settings: FakeSettingsRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun tab(label: Int) = composeRule.onNode(hasText(Res.string(label)) and hasClickAction())

    private fun await(matcher: SemanticsMatcher) {
        composeRule.waitUntil(AWAIT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun awaitText(text: String) = await(hasText(text, substring = true, ignoreCase = true))

    @Test
    fun startsOnScanWithAllFourTabsAndSwitchesBetweenThem() {
        composeRule.setContent { AppTheme(dark = false) { AppNav(onboardingDone = true) } }

        awaitText(Res.string(R.string.common_nav_scan))
        tab(R.string.common_nav_scan).assertIsSelected()

        tab(R.string.common_nav_history).performClick()
        awaitText(Res.string(R.string.hist_empty_scanned_title))
        tab(R.string.common_nav_history).assertIsSelected()

        tab(R.string.common_nav_settings).performClick()
        awaitText(Res.string(R.string.settings_section_scanning))
        tab(R.string.common_nav_settings).assertIsSelected()

        tab(R.string.common_nav_create).performClick()
        awaitText(Res.string(R.string.create_hub_all_types))
        tab(R.string.common_nav_create).assertIsSelected()

        tab(R.string.common_nav_scan).performClick()
        tab(R.string.common_nav_scan).assertIsSelected()
    }

    @Test
    fun firstRunSkipsThroughOnboardingToTheCameraRationaleAndEndsOnScan() {
        settings.value = settings.value.copy(onboardingDone = false)
        composeRule.setContent { AppTheme(dark = false) { AppNav(onboardingDone = false) } }

        awaitText(Res.string(R.string.onb_slide1_title))
        composeRule.onNode(hasText(Res.string(R.string.common_skip))).performClick()

        awaitText(Res.string(R.string.perm_camera_screen_title))
        composeRule.onNode(hasText(Res.string(R.string.perm_not_now))).performClick()

        awaitText(Res.string(R.string.common_nav_history))
        tab(R.string.common_nav_scan).assertIsSelected()
        composeRule.waitUntil(AWAIT_MILLIS) { settings.value.onboardingDone }
        assertTrue(settings.value.onboardingDone)
    }

    private companion object {
        const val AWAIT_MILLIS = 8_000L
    }
}
