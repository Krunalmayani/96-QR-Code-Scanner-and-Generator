package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.app.Application
import javax.inject.Inject
import javax.inject.Singleton

/** Placeholder binding until the monetization milestone swaps in the SDK-backed manager. */
@Singleton
class NoOpAdManager @Inject constructor() : AdManager {
    override fun initialize(application: Application) = Unit

    override fun controller(activity: Activity): AdController = NoOpAdController
}
