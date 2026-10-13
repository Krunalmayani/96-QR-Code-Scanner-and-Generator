package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding.OnboardingRoute
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** The three onboarding slides: Next advances, Back returns, Skip and the last "Get started" both finish. */
class OnboardingTest : ThemedComposeTest() {
    private var finished = 0

    private fun show() {
        setThemedContent { OnboardingRoute(onFinished = { finished++ }) }
    }

    private val next get() = Res.string(R.string.common_next)
    private val skip get() = Res.string(R.string.common_skip)
    private val back get() = Res.string(R.string.common_back)
    private val getStarted get() = Res.string(R.string.onb_get_started)

    private fun pageIndicator(page: Int) = Res.string(R.string.onb_page_indicator, page, 3)

    @Test
    fun firstSlideShowsSkipAndNextButNoBack() {
        show()

        composeRule.onNodeWithText(Res.string(R.string.onb_slide1_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(pageIndicator(1)).assertIsDisplayed()
        composeRule.onNodeWithText(skip).assertIsDisplayed()
        composeRule.onNodeWithText(next).assertIsDisplayed()
        composeRule.onNodeWithText(back).assertDoesNotExist()
    }

    @Test
    fun nextWalksThroughTheSlidesAndGetStartedFinishes() {
        show()

        composeRule.onNodeWithText(next).performClick()
        composeRule.onNodeWithText(Res.string(R.string.onb_slide2_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(pageIndicator(2)).assertIsDisplayed()

        composeRule.onNodeWithText(next).performClick()
        composeRule.onNodeWithText(Res.string(R.string.onb_slide3_title)).assertIsDisplayed()
        composeRule.onNodeWithText(skip).assertDoesNotExist()
        assertEquals(0, finished)

        composeRule.onNodeWithText(getStarted).performClick()
        assertEquals(1, finished)
    }

    @Test
    fun backReturnsToThePreviousSlide() {
        show()
        composeRule.onNodeWithText(next).performClick()
        composeRule.onNodeWithText(Res.string(R.string.onb_slide2_title)).assertIsDisplayed()

        composeRule.onNodeWithText(back).performClick()

        composeRule.onNodeWithText(Res.string(R.string.onb_slide1_title)).assertIsDisplayed()
        assertEquals(0, finished)
    }

    @Test
    fun skipFinishesImmediately() {
        show()

        composeRule.onNodeWithText(skip).performClick()

        assertEquals(1, finished)
    }
}
