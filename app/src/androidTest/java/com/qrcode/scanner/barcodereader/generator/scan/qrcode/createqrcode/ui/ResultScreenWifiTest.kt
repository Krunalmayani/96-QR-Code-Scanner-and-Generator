package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ResultStates
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.rememberTestSnackbarHost
import org.junit.Assert.assertEquals
import org.junit.Test

/** Wi-Fi result (design s18) and the connecting, connected and could-not-connect states (s26 to s28, s66). */
class ResultScreenWifiTest : ThemedComposeTest() {
    private val events = mutableListOf<ResultEvent>()

    private fun show(state: ResultUiState) {
        setThemedContent {
            ResultScreen(
                state = state,
                snackbarHostState = rememberTestSnackbarHost(),
                onEvent = { events += it },
                onBack = {},
            )
        }
    }

    @Test
    fun idleResultShowsNetworkAndConnectStartsTheFlow() {
        show(ResultStates.wifi())

        composeRule.onNodeWithText(ResultStates.WIFI_SSID).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_action_connect)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiConnectClicked), events)
    }

    @Test
    fun passwordIsMaskedUntilTheEyeIsTapped() {
        show(ResultStates.wifi())

        composeRule.onNodeWithText(ResultStates.WIFI_PASSWORD).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(Res.string(R.string.common_show_password)).performClick()

        composeRule.onNodeWithText(ResultStates.WIFI_PASSWORD).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(Res.string(R.string.common_hide_password)).assertIsDisplayed()
    }

    @Test
    fun copyPasswordReportsWifiCopyPassword() {
        show(ResultStates.wifi())

        composeRule.onAllNodesWithContentDescription(Res.string(R.string.result_action_copy_password)).onFirst().performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiCopyPassword), events)
    }

    @Test
    fun openNetworkHasNoPasswordCopyAction() {
        show(ResultStates.wifi(security = WifiSecurity.NONE, password = ""))

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_no_password)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_action_copy_password)).assertDoesNotExist()
        composeRule.onNodeWithText(Res.string(R.string.result_action_connect)).assertIsDisplayed()
    }

    @Test
    fun whyLocationLinkReportsTheExplainerRequest() {
        show(ResultStates.wifi())

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_why_location)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WhyLocationClicked), events)
    }

    @Test
    fun connectingShowsProgressAndCancel() {
        show(ResultStates.wifi(WifiConnectState(phase = WifiPhase.CONNECTING)))

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_connecting_title, ResultStates.WIFI_SSID)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_action_connect)).assertDoesNotExist()
        composeRule.onNodeWithText(Res.string(R.string.common_cancel)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiCancel), events)
    }

    @Test
    fun connectedShowsSuccessAndDoneLeavesTheFlow() {
        show(ResultStates.wifi(WifiConnectState(phase = WifiPhase.CONNECTED)))

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_connected_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.common_done)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiDone), events)
    }

    @Test
    fun savedSuggestionUsesItsOwnTitle() {
        show(ResultStates.wifi(WifiConnectState(phase = WifiPhase.CONNECTED, viaSuggestion = true)))

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_saved_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_wifi_connected_title)).assertDoesNotExist()
    }

    @Test
    fun failedKeepsThePasswordCopyableAndOffersRetry() {
        show(ResultStates.wifi(WifiConnectState(phase = WifiPhase.FAILED, failure = WifiFailure.UNAVAILABLE)))

        composeRule.onNodeWithText(Res.string(R.string.result_wifi_failed_title)).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription(Res.string(R.string.result_action_copy_password)).onFirst().performClick()
        composeRule.onNodeWithText(Res.string(R.string.common_retry)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiCopyPassword, ResultEvent.WifiRetry), events)
    }

    @Test
    fun topBarBackInsideTheFlowReturnsToTheResult() {
        show(ResultStates.wifi(WifiConnectState(phase = WifiPhase.FAILED, failure = WifiFailure.WIFI_DISABLED)))

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_back)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.WifiBack), events)
    }

    @Test
    fun locationRationaleSheetContinueAndNotNow() {
        show(ResultStates.wifi(locationRationaleVisible = true))

        composeRule.onNodeWithText(Res.string(R.string.perm_location_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.perm_continue)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.perm_not_now)).performClick()

        assertEquals(
            listOf(ResultEvent.LocationRationaleContinue, ResultEvent.LocationRationaleDismissed),
            events,
        )
    }
}
