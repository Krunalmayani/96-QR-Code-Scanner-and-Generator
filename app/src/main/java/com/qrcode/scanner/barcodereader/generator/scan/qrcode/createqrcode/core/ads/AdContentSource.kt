package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * What the ad composables ([BannerAd], [NativeAdCard]) read. Screens never use this directly.
 */
interface AdContentSource {
    /** True when ads may be shown right now: SDK ready, build and remote flags on, consent given, not ad-free. */
    val adsAvailable: StateFlow<Boolean>

    /** Loads an anchored adaptive banner for a container [widthDp] wide, or null when none is available. */
    suspend fun loadBanner(context: Context, widthDp: Int): LoadedBanner?

    /** Takes a preloaded native ad (waiting briefly for one), or null. The caller must destroy it. */
    suspend fun takeNative(): LoadedNative?
}
