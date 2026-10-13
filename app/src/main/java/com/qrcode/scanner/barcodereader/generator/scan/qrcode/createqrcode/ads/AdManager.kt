package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ads

import android.content.Context
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for all ads. Screens never touch the SDK directly,
 * so swapping SDKs or adding frequency caps happens in one place.
 *
 * Ad unit IDs come from BuildConfig (Google TEST ids by default, see app/build.gradle.kts).
 *
 * TODO(per app): wire the Google Mobile Ads Next Gen SDK calls here following
 * https://developers.google.com/admob/android/next-gen (initialize, load/show banner,
 * interstitial, rewarded, app open). The SDK dependency is already in the version catalog.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val bannerId: String = BuildConfig.AD_BANNER_ID
    val interstitialId: String = BuildConfig.AD_INTERSTITIAL_ID
    val rewardedId: String = BuildConfig.AD_REWARDED_ID
    val appOpenId: String = BuildConfig.AD_APP_OPEN_ID

    fun initialize() {
        // TODO: initialize the Next Gen SDK here (once, from App.onCreate).
    }
}
