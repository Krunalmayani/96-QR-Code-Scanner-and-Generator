package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.Validators
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The stateless Create form driven by a tiny reducer that mirrors the ViewModel's rules (validate on every edit,
 * errors visible only after blur): Generate stays disabled until valid and the inline error appears after blur.
 */
class CreateFormScreenTest : ThemedComposeTest() {
    private val events = mutableListOf<CreateFormEvent>()
    private var generated = 0
    private var focusManager: FocusManager? = null

    private fun formState(spec: CreateSpec): CreateFormUiState = CreateFormUiState(spec = spec, errors = Validators.validate(spec))

    @Composable
    private fun FormHost(initial: CreateSpec) {
        var state by remember { mutableStateOf(formState(initial)) }
        val focus = LocalFocusManager.current
        SideEffect { focusManager = focus }
        CreateFormScreen(
            state = state,
            onEvent = { event ->
                events += event
                when (event) {
                    is CreateFormEvent.Edit -> state = state.copy(spec = event.spec, errors = Validators.validate(event.spec))
                    is CreateFormEvent.Blur -> state = state.copy(touched = state.touched + event.field)
                    CreateFormEvent.Generate -> if (state.isValid) generated++ else state = state.copy(showAllErrors = true)
                    else -> Unit
                }
            },
            onBack = {},
            onSwitchToQr = {},
        )
    }

    private fun show(type: CreateType) {
        setThemedContent { FormHost(CreateSpec.blank(type)) }
    }

    private fun generateLabel(): String = Res.string(R.string.create_generate_qr)

    private fun blur() {
        composeRule.runOnIdle { focusManager?.clearFocus() }
        composeRule.waitForIdle()
    }

    @Test
    fun generateIsDisabledOnAnEmptyFormAndNoErrorIsShownYet() {
        show(CreateType.WEBSITE)

        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()
        composeRule.onNodeWithText(Res.string(R.string.create_error_required)).assertDoesNotExist()
        composeRule.onNodeWithText(Res.string(R.string.create_error_invalid_url)).assertDoesNotExist()
    }

    @Test
    fun invalidUrlShowsItsInlineErrorOnlyAfterBlur() {
        show(CreateType.WEBSITE)
        val error = Res.string(R.string.create_error_invalid_url)

        composeRule.onNode(hasSetTextAction()).performTextInput("brewhouse coffee")
        composeRule.onNodeWithText(error).assertDoesNotExist()
        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()

        blur()

        composeRule.onNodeWithText(error).assertIsDisplayed()
        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()
        assertTrue(events.contains(CreateFormEvent.Blur(FormField.URL)))
    }

    @Test
    fun correctingTheUrlClearsTheErrorAndEnablesGenerate() {
        show(CreateType.WEBSITE)
        val error = Res.string(R.string.create_error_invalid_url)
        composeRule.onNode(hasSetTextAction()).performTextInput("brewhouse coffee")
        blur()
        composeRule.onNodeWithText(error).assertIsDisplayed()

        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("https://brewhouse.coffee/menu")

        composeRule.onNodeWithText(error).assertDoesNotExist()
        composeRule.onNodeWithText(generateLabel()).assertIsEnabled()
    }

    @Test
    fun validFormGeneratesExactlyOnce() {
        show(CreateType.WEBSITE)
        composeRule.onNode(hasSetTextAction()).performTextInput("https://brewhouse.coffee/menu")

        composeRule.onNodeWithText(generateLabel()).performClick()

        assertEquals(1, generated)
        assertEquals(CreateFormEvent.Generate, events.last())
    }

    @Test
    fun disabledGenerateDoesNotFire() {
        show(CreateType.WEBSITE)

        composeRule.onNodeWithText(generateLabel()).performClick()

        assertEquals(0, generated)
        assertTrue(events.none { it == CreateFormEvent.Generate })
    }

    @Test
    fun wifiNeedsBothNetworkNameAndPassword() {
        show(CreateType.WIFI)
        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("CafeNova_Guest")
        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()

        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("guestpass123")
        composeRule.onNodeWithText(generateLabel()).assertIsEnabled()
    }

    @Test
    fun shortWifiPasswordStaysInvalidAndShowsTheLengthErrorAfterBlur() {
        show(CreateType.WIFI)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("CafeNova_Guest")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("short")

        blur()

        composeRule.onNodeWithText(generateLabel()).assertIsNotEnabled()
        composeRule.onNodeWithText(Res.string(R.string.create_error_password_too_short, 8)).assertIsDisplayed()
    }
}
