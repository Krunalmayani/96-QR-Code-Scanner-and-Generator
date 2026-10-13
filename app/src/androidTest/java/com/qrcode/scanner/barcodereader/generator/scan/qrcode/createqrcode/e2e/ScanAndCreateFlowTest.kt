package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.e2e

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.HiltTestActivity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Result
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * End-to-end flows through the real navigation graphs, ViewModels, use cases and screens, with in-memory
 * repositories (see TestModules.kt). The camera cannot be automated, so the scanner is simulated: the test saves
 * a record through [SaveScanUseCase] (exactly what the scan screen does on a detection) and navigates to it.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScanAndCreateFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var saveScan: SaveScanUseCase

    @Inject lateinit var history: FakeHistoryRepository

    @Inject lateinit var settings: FakeSettingsRepository

    private lateinit var navController: NavHostController

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            val controller = rememberNavController()
            SideEffect { navController = controller }
            E2eAppHost(controller)
        }
        composeRule.onNodeWithContentDescription(SCAN_PLACEHOLDER).assertIsDisplayed()
    }

    private fun tab(label: Int) = composeRule.onNode(hasText(Res.string(label)) and hasClickAction())

    private fun await(matcher: SemanticsMatcher) {
        composeRule.waitUntil(AWAIT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun awaitText(text: String) = await(hasText(text, substring = true, ignoreCase = true))

    @Test
    fun scanToResultToHistory() {
        val url = "https://example.com/menu?table=12"
        val saved =
            runBlocking {
                saveScan(ScanContent.Website(url, CodeFormat.QR_CODE, url), ScanSource.CAMERA)
            }
        assertTrue("a saved scan gets a stored (positive) id", saved.id > 0)

        composeRule.runOnUiThread { navController.navigate(Result(recordId = saved.id)) }
        awaitText(Res.string(R.string.result_title_website))
        awaitText("example.com/menu")
        composeRule.onNodeWithText(Res.string(R.string.result_action_open_link)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_back)).performClick()
        composeRule.onNodeWithContentDescription(SCAN_PLACEHOLDER).assertIsDisplayed()

        tab(R.string.common_nav_history).performClick()
        awaitText(saved.displayTitle)
        composeRule.onNodeWithText(saved.displayTitle).assertIsDisplayed()

        assertEquals(1, settings.value.successfulScans)
    }

    @Test
    fun savedScanRowOpensTheResultAgainFromHistory() {
        val url = "https://example.com/menu"
        val saved = runBlocking { saveScan(ScanContent.Website(url, CodeFormat.QR_CODE, url), ScanSource.CAMERA) }

        tab(R.string.common_nav_history).performClick()
        awaitText(saved.displayTitle)
        composeRule.onNodeWithText(saved.displayTitle).performClick()

        awaitText(Res.string(R.string.result_title_website))
        composeRule.onNodeWithText(Res.string(R.string.result_action_open_link)).assertIsDisplayed()
    }

    @Test
    fun emptyHistoryOffersToStartScanning() {
        tab(R.string.common_nav_history).performClick()

        awaitText(Res.string(R.string.hist_empty_scanned_title))
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_scanned_action)).performClick()

        composeRule.onNodeWithContentDescription(SCAN_PLACEHOLDER).assertIsDisplayed()
    }

    @Test
    fun createWebsiteToGeneratedToHistoryCreatedTab() {
        val url = "https://brewhouse.coffee/menu"

        tab(R.string.common_nav_create).performClick()
        awaitText(Res.string(R.string.create_hub_all_types))
        composeRule.onNodeWithText(Res.string(R.string.create_type_website)).performClick()

        await(hasSetTextAction())
        composeRule.onNode(hasSetTextAction()).performTextInput(url)
        composeRule.onNodeWithText(Res.string(R.string.create_generate_qr)).performClick()

        awaitText(Res.string(R.string.create_generated_title_qr))
        composeRule.onNodeWithText(Res.string(R.string.create_save_to_gallery)).assertIsDisplayed()

        val created = history.persistedRecords.single()
        assertEquals(ScanSource.CREATED, created.source)
        assertEquals(url, (created.content as ScanContent.Website).url)
        assertEquals(1, settings.value.completedActions)

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_back)).performClick()
        tab(R.string.common_nav_history).performClick()
        composeRule.onNode(hasText(Res.string(R.string.hist_tab_created)) and hasClickAction()).performClick()
        awaitText(created.displayTitle)
        composeRule.onNodeWithText(created.displayTitle).assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText(Res.string(R.string.hist_empty_created_title)).fetchSemanticsNodes().size)
    }

    @Test
    fun invalidWebsiteKeepsGenerateDisabledSoNothingIsCreated() {
        tab(R.string.common_nav_create).performClick()
        awaitText(Res.string(R.string.create_hub_all_types))
        composeRule.onNodeWithText(Res.string(R.string.create_type_website)).performClick()
        await(hasSetTextAction())

        composeRule.onNode(hasSetTextAction()).performTextInput("brewhouse coffee")
        composeRule.onNodeWithText(Res.string(R.string.create_generate_qr)).performClick()
        composeRule.waitForIdle()

        assertTrue(history.persistedRecords.isEmpty())
        assertEquals(0, settings.value.completedActions)
    }

    private companion object {
        const val AWAIT_MILLIS = 8_000L
    }
}
