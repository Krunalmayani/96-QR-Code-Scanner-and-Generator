package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Crashlytics helper. Privacy rule (PRD 8): never pass scanned or created content, URLs, names or any user
 * text. [logNonFatal] strips the exception message for that reason; [log] and [setKey] take only short
 * constants chosen by the code, never data.
 */
@Singleton
class CrashReporter @Inject constructor() {

    /** Turns crash and non-fatal collection on or off (the Settings privacy toggle). */
    fun setEnabled(enabled: Boolean) {
        runSafely { FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled) }
    }

    /** Records a handled exception. [tag] is a short constant such as "ads_init" naming where it happened. */
    fun logNonFatal(tag: String, throwable: Throwable) {
        Timber.w(throwable, "non-fatal in %s", tag)
        runSafely { FirebaseCrashlytics.getInstance().recordException(NonFatalException(tag, throwable)) }
    }

    /** Adds a breadcrumb to the next crash report. Constants only. */
    fun log(breadcrumb: String) {
        runSafely { FirebaseCrashlytics.getInstance().log(breadcrumb) }
    }

    /** Attaches a small enum-like key to crash reports, such as theme or build flavour. Never content. */
    fun setKey(key: String, value: String) {
        runSafely { FirebaseCrashlytics.getInstance().setCustomKey(key, value.take(MAX_VALUE_LENGTH)) }
    }

    private inline fun runSafely(block: () -> Unit) {
        try {
            block()
        } catch (error: IllegalStateException) {
            Timber.w(error, "Crashlytics unavailable")
        }
    }

    private companion object {
        const val MAX_VALUE_LENGTH = 36
    }
}
