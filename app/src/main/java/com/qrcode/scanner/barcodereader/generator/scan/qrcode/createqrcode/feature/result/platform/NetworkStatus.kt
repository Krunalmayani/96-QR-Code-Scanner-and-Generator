package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform

/** Whether the device currently has a working internet connection (product lookup offline state, s70). */
interface NetworkStatus {
    fun isOnline(): Boolean
}
