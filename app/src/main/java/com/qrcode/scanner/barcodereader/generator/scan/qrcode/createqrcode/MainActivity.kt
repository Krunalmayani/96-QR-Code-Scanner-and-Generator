package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdManager
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LocalAdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.app.MainViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.AppNav
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.isDarkFor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !viewModel.ready.value }

        val adController = adManager.controller(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { adController.onAppForegrounded() }
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val current = settings ?: return@setContent
            val dark = isDarkFor(current.theme)

            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose { }
            }
            LaunchedEffect(current.languageTag) {
                val wanted = LocaleListCompat.forLanguageTags(current.languageTag)
                if (AppCompatDelegate.getApplicationLocales() != wanted) AppCompatDelegate.setApplicationLocales(wanted)
            }

            val onboardingDone = remember { current.onboardingDone }
            CompositionLocalProvider(LocalAdController provides adController) {
                AppTheme(themeMode = current.theme) {
                    AppNav(onboardingDone = onboardingDone)
                }
            }
        }
    }
}
