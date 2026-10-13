package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.CameraRationaleContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.CameraRationaleDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.LocationRationaleSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.PhotosRationaleDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Camera (onboarding screen and dialog), photos and location rationales: each button calls exactly its callback. */
class PermissionRationaleTest : ThemedComposeTest() {
    private var allowed = 0
    private var dismissed = 0

    @Test
    fun onboardingCameraRationaleAllowAndNotNow() {
        setThemedContent { CameraRationaleContent(onAllow = { allowed++ }, onNotNow = { dismissed++ }) }

        composeRule.onNodeWithText(Res.string(R.string.perm_camera_screen_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.perm_camera_allow)).performClick()
        assertEquals(1 to 0, allowed to dismissed)

        composeRule.onNodeWithText(Res.string(R.string.perm_not_now)).performClick()
        assertEquals(1 to 1, allowed to dismissed)
    }

    @Test
    fun cameraRationaleDialogContinueAndNotNow() {
        setThemedContent { CameraRationaleDialog(onAllow = { allowed++ }, onDismiss = { dismissed++ }) }

        composeRule.onNodeWithText(Res.string(R.string.perm_camera_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.perm_continue)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.perm_not_now)).performClick()

        assertEquals(1 to 1, allowed to dismissed)
    }

    @Test
    fun photosRationaleDialogChooseAndCancel() {
        setThemedContent { PhotosRationaleDialog(onContinue = { allowed++ }, onDismiss = { dismissed++ }) }

        composeRule.onNodeWithText(Res.string(R.string.perm_photos_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.perm_photos_choose)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.perm_photos_cancel)).performClick()

        assertEquals(1 to 1, allowed to dismissed)
    }

    @Test
    fun locationRationaleSheetContinueAndNotNow() {
        setThemedContent { LocationRationaleSheet(onContinue = { allowed++ }, onDismiss = { dismissed++ }) }

        composeRule.onNodeWithText(Res.string(R.string.perm_location_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.perm_continue)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.perm_not_now)).performClick()

        assertEquals(1 to 1, allowed to dismissed)
    }
}
