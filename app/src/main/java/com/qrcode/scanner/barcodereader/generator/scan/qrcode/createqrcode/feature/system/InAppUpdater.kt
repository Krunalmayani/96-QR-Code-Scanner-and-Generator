package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/** Where the flexible in-app update stands. */
enum class UpdateStatus {
    /** Nothing to offer: up to date, debug build, Play unavailable or the check failed. */
    NONE,

    /** A flexible update can be started. */
    AVAILABLE,

    /** The user accepted; Play is downloading it in the background. */
    DOWNLOADING,

    /** Downloaded; the app must restart to install it. */
    DOWNLOADED,
}

/** Flexible Play In-App Updates behind an interface so the prompt logic can be tested without Play. */
interface InAppUpdater {
    val status: StateFlow<UpdateStatus>

    /** Asks Play whether an update exists. Never throws; failures leave [status] at NONE. */
    fun check()

    /** Starts the flexible flow for the update found by the last [check]. */
    fun start(activity: Activity)

    /** Installs a downloaded update and restarts the app. */
    fun complete()
}
