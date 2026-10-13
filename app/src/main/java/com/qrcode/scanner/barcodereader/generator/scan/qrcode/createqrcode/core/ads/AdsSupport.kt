package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.CrashReporter

internal fun nowMs(): Long = System.currentTimeMillis()

/**
 * Runs SDK-facing [block]; any failure is reported as a non-fatal (no message, see CrashReporter) and
 * [onError] restores bookkeeping, so a misbehaving ad call can never crash the app.
 */
internal inline fun guardedAds(crashReporter: CrashReporter, tag: String, onError: () -> Unit = {}, block: () -> Unit) {
    val failure = runCatching { block() }.exceptionOrNull() ?: return
    crashReporter.logNonFatal(tag, failure)
    onError()
}
