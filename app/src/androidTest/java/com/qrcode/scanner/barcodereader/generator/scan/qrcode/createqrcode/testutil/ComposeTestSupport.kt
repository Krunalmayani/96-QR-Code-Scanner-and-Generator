package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Reads app string resources the same way the composables do, so assertions follow the device language. */
object Res {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    fun string(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    fun plural(
        @PluralsRes id: Int,
        quantity: Int,
        vararg args: Any,
    ): String = context.resources.getQuantityString(id, quantity, *args)
}

/**
 * Base for stateless-composable tests: every test runs twice, in the light and in the dark theme.
 * Subclasses call [setThemedContent] once and then drive and assert through [composeRule].
 */
@RunWith(Parameterized::class)
abstract class ThemedComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @JvmField
    @Parameterized.Parameter
    var dark: Boolean = false

    protected fun setThemedContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            AppTheme(dark = dark) {
                Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) { content() }
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "dark={0}")
        fun themes(): List<Boolean> = listOf(false, true)
    }
}

/** A snackbar host state that survives recomposition, for screens that require one. */
@Composable
fun rememberTestSnackbarHost(): SnackbarHostState = remember { SnackbarHostState() }
