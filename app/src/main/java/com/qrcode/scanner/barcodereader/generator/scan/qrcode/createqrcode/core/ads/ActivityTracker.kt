package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Counts started activities across the whole process, ad activities included, so opening a full-screen ad
 * never looks like "the app went to the background". [onBackgrounded] fires when the last started activity
 * stops (not for a configuration change).
 */
internal class ActivityTracker(
    private val onBackgrounded: () -> Unit,
) : Application.ActivityLifecycleCallbacks {
    @Volatile
    private var startedCount = 0

    @Volatile
    private var current: WeakReference<Activity>? = null

    val isForeground: Boolean get() = startedCount > 0

    val currentActivity: Activity? get() = current?.get()

    override fun onActivityStarted(activity: Activity) {
        startedCount += 1
        current = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        startedCount = maxOf(startedCount - 1, 0)
        if (startedCount == 0 && !activity.isChangingConfigurations) onBackgrounded()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
