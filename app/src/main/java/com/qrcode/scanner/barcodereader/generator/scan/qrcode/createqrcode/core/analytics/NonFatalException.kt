package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics

/**
 * Privacy-safe stand-in for an exception reported to Crashlytics. Keeps the original stack trace and class
 * name but drops its message and cause, which can echo scanned or created content.
 */
class NonFatalException(tag: String, original: Throwable) : Exception("$tag: ${original.javaClass.name}") {
    init {
        stackTrace = original.stackTrace
    }
}
