package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.Level
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ResultStates
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.rememberTestSnackbarHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Result screen for a website: the safe path and the unsafe-link warning sheet with its three actions. */
class ResultScreenWebsiteTest : ThemedComposeTest() {
    private val events = mutableListOf<ResultEvent>()
    private var backs = 0

    private fun show(state: ResultUiState) {
        setThemedContent {
            ResultScreen(
                state = state,
                snackbarHostState = rememberTestSnackbarHost(),
                onEvent = { events += it },
                onBack = { backs++ },
            )
        }
    }

    @Test
    fun theUnsafeSampleUrlReallyIsFlaggedByTheDomainChecker() {
        val level = ResultStates.website(ResultStates.UNSAFE_URL).safety?.level
        assertNotEquals(Level.SAFE, level)
    }

    @Test
    fun safeWebsiteShowsTitleAndOpenLinkSendsPrimaryAction() {
        show(ResultStates.website(ResultStates.SAFE_URL))

        composeRule.onNodeWithText(Res.string(R.string.result_title_website)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_title_unsafe)).assertDoesNotExist()
        composeRule.onNodeWithText(Res.string(R.string.result_action_open_link)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.PrimaryAction), events)
    }

    @Test
    fun unsafeSheetShowsWarningAndGoBackToSafetyAction() {
        show(ResultStates.website(ResultStates.UNSAFE_URL, showUnsafeSheet = true))

        composeRule.onNodeWithText(unsafeOrCautionTitle()).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_open_anyway)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_go_back)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.UnsafeGoBack), events)
    }

    @Test
    fun unsafeSheetCopyLinkInsteadAction() {
        show(ResultStates.website(ResultStates.UNSAFE_URL, showUnsafeSheet = true))

        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_copy)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.UnsafeCopyLink), events)
    }

    @Test
    fun unsafeSheetOpenAnywayIsAnExplicitSeparateAction() {
        show(ResultStates.website(ResultStates.UNSAFE_URL, showUnsafeSheet = true))

        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_open_anyway)).performClick()

        assertEquals(listOf<ResultEvent>(ResultEvent.UnsafeOpenAnyway), events)
    }

    @Test
    fun noSheetUntilTheStateAsksForIt() {
        show(ResultStates.website(ResultStates.UNSAFE_URL, showUnsafeSheet = false))

        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_go_back)).assertDoesNotExist()
        composeRule.onNodeWithText(Res.string(R.string.result_unsafe_open_anyway)).assertDoesNotExist()
    }

    @Test
    fun topBarBackCallsOnBack() {
        show(ResultStates.website(ResultStates.SAFE_URL))

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_back)).performClick()

        assertEquals(1, backs)
    }

    private fun unsafeOrCautionTitle(): String {
        val level = ResultStates.website(ResultStates.UNSAFE_URL).safety?.level
        return Res.string(
            if (level == Level.UNSAFE) R.string.result_unsafe_title_unsafe else R.string.result_unsafe_title_caution,
        )
    }
}
