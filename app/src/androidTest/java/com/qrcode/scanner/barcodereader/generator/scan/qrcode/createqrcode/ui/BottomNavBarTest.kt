package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** The raised-centre bottom bar: which tab shows as selected and which callback each tap fires. */
class BottomNavBarTest : ThemedComposeTest() {
    private fun label(tab: BottomTab): String =
        Res.string(
            when (tab) {
                BottomTab.Create -> R.string.common_nav_create
                BottomTab.Scan -> R.string.common_nav_scan
                BottomTab.History -> R.string.common_nav_history
                BottomTab.Settings -> R.string.common_nav_settings
            },
        )

    @Test
    fun selectedTabIsMarkedSelectedAndTheOthersAreNot() {
        var selectedTab by mutableStateOf<BottomTab?>(BottomTab.Create)
        setThemedContent { BottomNavBar(selected = selectedTab, onSelect = {}) }

        for (tab in BottomTab.entries) {
            composeRule.runOnIdle { selectedTab = tab }
            BottomTab.entries.forEach { other ->
                val node = composeRule.onNodeWithText(label(other))
                if (other == tab) node.assertIsSelected() else node.assertIsNotSelected()
            }
        }
    }

    @Test
    fun noTabIsSelectedWhenSelectionIsNull() {
        setThemedContent { BottomNavBar(selected = null, onSelect = {}) }
        BottomTab.entries.forEach { composeRule.onNodeWithText(label(it)).assertIsNotSelected() }
    }

    @Test
    fun tappingATabReportsExactlyThatTab() {
        val taps = mutableListOf<BottomTab>()
        setThemedContent { BottomNavBar(selected = BottomTab.Scan, onSelect = { taps += it }) }

        BottomTab.entries.forEach { composeRule.onNodeWithText(label(it)).performClick() }

        assertEquals(BottomTab.entries.toList(), taps)
    }

    @Test
    fun selectionFollowsTheCallbackWhenTheHostUpdatesState() {
        setThemedContent {
            var current by remember { mutableStateOf(BottomTab.Scan) }
            BottomNavBar(selected = current, onSelect = { current = it })
        }
        composeRule.onNodeWithText(label(BottomTab.Scan)).assertIsSelected()

        composeRule.onNodeWithText(label(BottomTab.History)).performClick()

        composeRule.onNodeWithText(label(BottomTab.History)).assertIsSelected()
        composeRule.onNodeWithText(label(BottomTab.Scan)).assertIsNotSelected()
    }
}
