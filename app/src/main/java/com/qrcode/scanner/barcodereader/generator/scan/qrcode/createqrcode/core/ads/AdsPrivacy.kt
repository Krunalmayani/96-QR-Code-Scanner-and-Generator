package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.content.Context

/**
 * True when the user's region requires an entry point for changing ad privacy choices (EEA, UK, ...).
 * Settings shows the "Privacy options" row only when this is true.
 */
fun isPrivacyOptionsRequired(context: Context): Boolean = UmpConsent.isPrivacyOptionsRequired(context)

/**
 * Opens the UMP privacy options form so the user can review or withdraw ad consent. [onDismissed] gets an
 * error message or null once the form closes. If consent is withdrawn, no further ads are requested.
 */
fun showPrivacyOptionsForm(activity: Activity, onDismissed: (errorMessage: String?) -> Unit = {}) {
    UmpConsent.showPrivacyOptions(activity, onDismissed)
}
