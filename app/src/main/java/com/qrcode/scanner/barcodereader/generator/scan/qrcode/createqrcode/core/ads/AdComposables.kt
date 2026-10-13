package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import dagger.hilt.android.EntryPointAccessors

/**
 * Anchored adaptive banner for History and Settings. Never use it on the camera screen.
 * Takes no height until an ad has loaded, and none at all when ads are disabled, unavailable or in previews.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val source = rememberAdContentSource() ?: return
    val available by source.adsAvailable.collectAsStateWithLifecycle()
    if (!available) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = if (maxWidth.value.isFinite()) maxWidth.value.toInt() else 0
        BannerContent(source = source, widthDp = widthDp)
    }
}

@Composable
private fun BannerContent(source: AdContentSource, widthDp: Int) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var banner by remember { mutableStateOf<LoadedBanner?>(null) }

    LaunchedEffect(source, widthDp) {
        val previous = banner
        banner = null
        previous?.destroy()
        banner = source.loadBanner(context, widthDp)
    }
    DisposableEffect(Unit) {
        onDispose {
            val last = banner
            banner = null
            last?.destroy()
        }
    }

    val loaded = banner
    if (loaded != null && activity != null) {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { viewContext -> NextGenAdsGateway.createBannerView(viewContext) },
            update = { view -> NextGenAdsGateway.bindBanner(view, loaded, activity) },
        )
    }
}

/**
 * Native ad row for the History list (one per N items, see NativeAdPlacement). Draws nothing until an ad is
 * ready, and nothing at all when ads are disabled, unavailable or in previews.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val source = rememberAdContentSource() ?: return
    val available by source.adsAvailable.collectAsStateWithLifecycle()
    var native by remember { mutableStateOf<LoadedNative?>(null) }

    LaunchedEffect(source, available) {
        if (available && native == null) native = source.takeNative()
    }
    DisposableEffect(Unit) {
        onDispose {
            val last = native
            native = null
            last?.destroy()
        }
    }

    val loaded = native
    if (available && loaded != null) NativeAdContent(loaded = loaded, modifier = modifier)
}

@Composable
private fun NativeAdContent(loaded: LoadedNative, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val style = NativeCardStyle(
        background = colors.surface.toArgb(),
        outline = colors.outlineVariant.toArgb(),
        headline = colors.onSurface.toArgb(),
        body = colors.onSurfaceVariant.toArgb(),
        label = colors.onSurfaceVariant.toArgb(),
        ctaBackground = colors.primary.toArgb(),
        ctaText = colors.onPrimary.toArgb(),
        adLabel = stringResource(R.string.ads_label_ad),
        adLabelDescription = stringResource(R.string.ads_native_content_description),
    )
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { viewContext -> NextGenAdsGateway.createNativeView(viewContext, loaded, style) },
    )
}

/** Null in previews and in tests without Hilt, so the ad composables simply draw nothing there. */
@Composable
private fun rememberAdContentSource(): AdContentSource? {
    if (LocalInspectionMode.current) return null
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        runCatching { EntryPointAccessors.fromApplication(appContext, AdsEntryPoint::class.java).adContentSource() }.getOrNull()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
