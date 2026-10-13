package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.content.Context
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * Thin wrapper over Google's User Messaging Platform. The UMP SDK is process-wide static state, so this
 * needs no injection. [canRequestAds] is what gates every ad: the Mobile Ads SDK is never initialised
 * while it is false.
 */
internal object UmpConsent {
    private val canRequest = MutableStateFlow(false)

    /** Mirrors ConsentInformation.canRequestAds(); updates after consent gathering and the privacy form. */
    val canRequestAds: StateFlow<Boolean> = canRequest.asStateFlow()

    /** Reads the consent state UMP persisted in an earlier session (valid before any network call). */
    fun refresh(context: Context) {
        canRequest.value = UserMessagingPlatform.getConsentInformation(context).canRequestAds()
    }

    /**
     * Updates consent info and shows the consent form when required. Call on every app launch, on the main
     * thread. [onDone] receives an error message or null; the state flow is already refreshed by then.
     */
    fun gather(activity: Activity, onDone: (errorMessage: String?) -> Unit) {
        val information = UserMessagingPlatform.getConsentInformation(activity)
        information.requestConsentInfoUpdate(
            activity,
            requestParameters(activity),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    refresh(activity)
                    onDone(formError?.message)
                }
            },
            { requestError ->
                refresh(activity)
                onDone(requestError.message)
            },
        )
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean =
        UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity, onDone: (errorMessage: String?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            refresh(activity)
            onDone(formError?.message)
        }
    }

    private fun requestParameters(activity: Activity): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
        if (BuildConfig.DEBUG) builder.setConsentDebugSettings(debugSettings(activity))
        return builder.build()
    }

    /** Test devices and a forced geography come from string resources, see docs/ADS_SETUP.md. */
    private fun debugSettings(activity: Activity): ConsentDebugSettings {
        val builder = ConsentDebugSettings.Builder(activity)
        testDeviceIds(activity).forEach { builder.addTestDeviceHashedId(it) }
        when (activity.getString(R.string.ads_debug_geography).trim().uppercase()) {
            "EEA" -> builder.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            "NOT_EEA" -> builder.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA)
            else -> Timber.d("UMP debug geography not forced")
        }
        return builder.build()
    }

    /** Hashed test device ids from the ads_test_device_ids resource (comma separated, may be empty). */
    fun testDeviceIds(context: Context): List<String> =
        context.getString(R.string.ads_test_device_ids)
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
