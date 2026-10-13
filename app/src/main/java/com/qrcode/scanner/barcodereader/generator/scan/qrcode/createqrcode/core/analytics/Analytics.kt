package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Names of the only events the app logs. Privacy rule (PRD 8): event names and small enum-like params
 * (type, source, format) only, never scanned or created content.
 */
object Events {
    const val SCAN_SUCCESS = "scan_success"
    const val SCAN_FROM_IMAGE = "scan_from_image"
    const val SCAN_NO_CODE = "scan_no_code"
    const val CREATE_SUCCESS = "create_success"
    const val SAVE_TO_GALLERY = "save_to_gallery"
    const val SHARE_TAP = "share_tap"
    const val COPY_TAP = "copy_tap"
    const val OPEN_LINK = "open_link"
    const val UNSAFE_LINK_SHOWN = "unsafe_link_shown"
    const val WIFI_CONNECT = "wifi_connect"
    const val FAVORITE_TOGGLE = "favorite_toggle"
    const val HISTORY_EXPORT = "history_export"
    const val HISTORY_CLEAR = "history_clear"
    const val ONBOARDING_DONE = "onboarding_done"
    const val RATE_PROMPT_SHOWN = "rate_prompt_shown"
    const val FEEDBACK_SENT = "feedback_sent"
    const val THEME_CHANGED = "theme_changed"
    const val LANGUAGE_CHANGED = "language_changed"
    const val UPDATE_PROMPT_SHOWN = "update_prompt_shown"
    const val AD_INTERSTITIAL_SHOWN = "ad_interstitial_shown"
    const val AD_APP_OPEN_SHOWN = "ad_app_open_shown"
    const val AD_LOAD_FAILED = "ad_load_failed"
}

/** Param keys; values must be short enum names such as "WEBSITE" or "CAMERA". */
object Params {
    const val TYPE = "type"
    const val SOURCE = "source"
    const val FORMAT = "format"
    const val RESULT = "result"

    /** Values: "banner", "native", "interstitial", "app_open". */
    const val AD_FORMAT = "ad_format"
}

/**
 * Thin wrapper over Firebase Analytics. Collection can be switched off from Settings; the same switch also
 * turns Crashlytics collection off ([CrashReporter]).
 */
@Singleton
class Analytics @Inject constructor(
    @ApplicationContext context: Context,
    private val crashReporter: CrashReporter,
) {
    private val firebase = FirebaseAnalytics.getInstance(context)

    fun setEnabled(enabled: Boolean) {
        firebase.setAnalyticsCollectionEnabled(enabled)
        crashReporter.setEnabled(enabled)
    }

    fun log(event: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle().apply { params.forEach { (key, value) -> putString(key, value.take(MAX_PARAM_LENGTH)) } }
        firebase.logEvent(event, bundle)
    }

    private companion object {
        const val MAX_PARAM_LENGTH = 36
    }
}
