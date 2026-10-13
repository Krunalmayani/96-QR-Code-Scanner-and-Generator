package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode

import android.app.Application
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ads.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()
        adManager.initialize()
    }
}
