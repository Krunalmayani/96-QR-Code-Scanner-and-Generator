package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import timber.log.Timber
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd as SdkBannerAd

/*
 * The ONLY file that touches the Google Mobile Ads Next Gen SDK (com.google.android.libraries.ads.mobile.sdk).
 * Everything else in the app talks to the small wrapper types and functions below, so an SDK API change is
 * fixed here and nowhere else. Call shapes follow Google's official samples (googleads/gma-next-gen-sdk-android-examples).
 */

/** A loaded banner. Opaque outside this file. */
class LoadedBanner internal constructor(internal val ad: SdkBannerAd) {
    fun destroy() {
        ad.destroy()
    }
}

/** A loaded native ad. Opaque outside this file. */
class LoadedNative internal constructor(
    internal val ad: NativeAd,
    val loadedAtMs: Long = System.currentTimeMillis(),
) {
    fun destroy() {
        ad.destroy()
    }
}

/** A loaded interstitial. Opaque outside this file. */
internal class LoadedInterstitial(val ad: InterstitialAd)

/** A loaded app open ad. Opaque outside this file. */
internal class LoadedAppOpen(val ad: AppOpenAd)

/** Lifecycle of a full-screen ad. May be called from a background thread. */
internal interface FullScreenCallbacks {
    fun onShown()

    fun onDismissed()

    fun onFailedToShow(message: String)
}

/** ARGB colors and texts for the native ad card, taken from the app theme by the composable. */
class NativeCardStyle(
    val background: Int,
    val outline: Int,
    val headline: Int,
    val body: Int,
    val label: Int,
    val ctaBackground: Int,
    val ctaText: Int,
    val adLabel: String,
    val adLabelDescription: String,
)

internal object NextGenAdsGateway {
    private const val MEDIA_HEIGHT_DP = 180
    private const val ICON_SIZE_DP = 40
    private const val CARD_PADDING_DP = 12
    private const val CARD_RADIUS_DP = 16
    private const val CTA_MIN_HEIGHT_DP = 48
    private const val HEADLINE_SP = 16f
    private const val BODY_SP = 14f
    private const val LABEL_SP = 11f

    /** Initialises the SDK. Call from a background thread, and only once consent allows ads. */
    fun initialize(context: Context, appId: String, testDeviceIds: List<String>, onComplete: () -> Unit) {
        MobileAds.initialize(context, InitializationConfig.Builder(appId).build()) { onComplete() }
        if (testDeviceIds.isNotEmpty()) {
            MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build())
        }
    }

    // ---- Banner ------------------------------------------------------------------------------------------

    /** Loads a large anchored adaptive banner for a container [widthDp] wide. Call from the main thread. */
    suspend fun loadBanner(context: Context, adUnitId: String, widthDp: Int): LoadedBanner? {
        val size = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
        return when (val result = SdkBannerAd.load(BannerAdRequest.Builder(adUnitId, size).build())) {
            is AdLoadResult.Success -> LoadedBanner(result.ad)
            is AdLoadResult.Failure -> {
                Timber.w("Banner ad failed to load: %s", result)
                null
            }
        }
    }

    fun createBannerView(context: Context): View = AdView(context)

    fun bindBanner(view: View, banner: LoadedBanner, activity: Activity) {
        (view as? AdView)?.registerBannerAd(banner.ad, activity)
    }

    // ---- Interstitial ------------------------------------------------------------------------------------

    fun loadInterstitial(adUnitId: String, onLoaded: (LoadedInterstitial) -> Unit, onFailed: (String) -> Unit) {
        InterstitialAd.load(
            AdRequest.Builder(adUnitId).build(),
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    onLoaded(LoadedInterstitial(ad))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onFailed(adError.message.orEmpty())
                }
            },
        )
    }

    /** Shows [loaded] on the main thread. */
    fun showInterstitial(loaded: LoadedInterstitial, activity: Activity, callbacks: FullScreenCallbacks) {
        loaded.ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                callbacks.onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                callbacks.onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                callbacks.onFailedToShow(fullScreenContentError.message.orEmpty())
            }
        }
        loaded.ad.show(activity)
    }

    // ---- App open ----------------------------------------------------------------------------------------

    fun loadAppOpen(adUnitId: String, onLoaded: (LoadedAppOpen) -> Unit, onFailed: (String) -> Unit) {
        AppOpenAd.load(
            AdRequest.Builder(adUnitId).build(),
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    onLoaded(LoadedAppOpen(ad))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onFailed(adError.message.orEmpty())
                }
            },
        )
    }

    /** Shows [loaded] on the main thread. */
    fun showAppOpen(loaded: LoadedAppOpen, activity: Activity, callbacks: FullScreenCallbacks) {
        loaded.ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                callbacks.onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                callbacks.onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                callbacks.onFailedToShow(fullScreenContentError.message.orEmpty())
            }
        }
        loaded.ad.show(activity)
    }

    // ---- Native ------------------------------------------------------------------------------------------

    fun loadNative(adUnitId: String, onLoaded: (LoadedNative) -> Unit, onFailed: (String) -> Unit) {
        val request = NativeAdRequest.Builder(adUnitId, listOf(NativeAd.NativeAdType.NATIVE)).build()
        NativeAdLoader.load(
            request,
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    onLoaded(LoadedNative(nativeAd))
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onFailed(adError.message.orEmpty())
                }
            },
        )
    }

    /** Builds the card as plain views (the SDK needs real views for its native ad assets). Main thread. */
    fun createNativeView(context: Context, loaded: LoadedNative, style: NativeCardStyle): View {
        val ad = loaded.ad
        val density = context.resources.displayMetrics.density
        val dp = { value: Int -> (value * density).toInt() }

        val adView = NativeAdView(context)
        adView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        adView.background = roundedRect(style.background, style.outline, dp(CARD_RADIUS_DP), dp(1))
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(CARD_PADDING_DP), dp(CARD_PADDING_DP), dp(CARD_PADDING_DP), dp(CARD_PADDING_DP))
        }
        adView.addView(column, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val icon = ImageView(context).apply { setImageDrawable(ad.icon?.drawable) }
        val headline = TextView(context).apply {
            text = ad.headline.orEmpty()
            setTextColor(style.headline)
            textSize = HEADLINE_SP
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 2
        }
        val label = TextView(context).apply {
            text = style.adLabel
            contentDescription = style.adLabelDescription
            setTextColor(style.label)
            textSize = LABEL_SP
            background = roundedRect(0, style.label, dp(4), dp(1))
            setPadding(dp(4), dp(1), dp(4), dp(1))
        }
        column.addView(headerRow(context, icon, headline, label, dp(ICON_SIZE_DP), dp(8)))

        val media = MediaView(context)
        column.addView(media, linearParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(MEDIA_HEIGHT_DP), dp(8)))

        val body = TextView(context).apply {
            text = ad.body.orEmpty()
            setTextColor(style.body)
            textSize = BODY_SP
            maxLines = 3
            visibility = if (ad.body.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        column.addView(body, linearParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, dp(8)))

        val cta = Button(context).apply {
            text = ad.callToAction.orEmpty()
            isAllCaps = false
            setTextColor(style.ctaText)
            background = roundedRect(style.ctaBackground, style.ctaBackground, dp(CTA_MIN_HEIGHT_DP), 0)
            minHeight = dp(CTA_MIN_HEIGHT_DP)
            visibility = if (ad.callToAction.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        column.addView(cta, linearParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, dp(8)))

        adView.iconView = icon
        adView.headlineView = headline
        adView.bodyView = body
        adView.callToActionView = cta
        // Register after layout so the MediaView already has its 120x120dp minimum size.
        media.post { adView.registerNativeAd(ad, media) }
        return adView
    }

    private fun headerRow(context: Context, icon: ImageView, headline: TextView, label: TextView, iconPx: Int, gapPx: Int): View {
        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headline)
            addView(label, linearParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gapPx / 2))
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(icon, LinearLayout.LayoutParams(iconPx, iconPx).apply { marginEnd = gapPx })
            addView(textColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun linearParams(width: Int, height: Int, topMarginPx: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(width, height).apply { topMargin = topMarginPx }

    private fun roundedRect(fill: Int, stroke: Int, radiusPx: Int, strokePx: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radiusPx.toFloat()
            if (strokePx > 0) setStroke(strokePx, stroke)
        }
}
