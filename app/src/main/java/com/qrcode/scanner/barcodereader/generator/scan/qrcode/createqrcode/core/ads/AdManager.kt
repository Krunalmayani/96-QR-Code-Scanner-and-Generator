package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Screens never touch the ads SDK. They use only: [BannerAd], [NativeAdCard] and [LocalAdController].
 * The real implementation (Next Gen SDK, UMP consent, frequency caps, remote kill-switch) lives in this package.
 */
interface AdManager {
    /** Called once from Application.onCreate. */
    fun initialize(application: Application)

    /** Controller bound to the foreground [activity] (needed to show full-screen ads). */
    fun controller(activity: Activity): AdController
}

/** Per-activity entry point for full-screen ad triggers. */
interface AdController {
    /** Call when a scan/create result is visible on screen: counts it and may show an interstitial. */
    fun onResultShown()

    /** Call when the app returns to the foreground (app-open ad rules are applied inside). */
    fun onAppForegrounded()
}

object NoOpAdController : AdController {
    override fun onResultShown() = Unit

    override fun onAppForegrounded() = Unit
}

val LocalAdController = staticCompositionLocalOf<AdController> { NoOpAdController }

/** Adaptive banner for History and Settings. Renders nothing when ads are disabled or not loaded. */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    // Replaced by the real implementation in the monetization milestone.
}

/** Native ad row shown every 8 history items. Renders nothing when ads are disabled or not loaded. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    // Replaced by the real implementation in the monetization milestone.
}
