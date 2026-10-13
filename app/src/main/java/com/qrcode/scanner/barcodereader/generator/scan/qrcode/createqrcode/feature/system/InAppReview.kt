package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Shows the Play In-App Review flow over [activity]. Play decides whether a dialog actually appears (quota),
 * so a normal return does not mean the user rated. Returns false when the flow could not be started.
 */
internal suspend fun launchInAppReview(activity: Activity): Boolean {
    return try {
        val manager = ReviewManagerFactory.create(activity)
        val info = requestReviewInfo(manager) ?: return false
        launchReviewFlow(manager, activity, info)
        true
    } catch (error: RuntimeException) {
        Timber.w(error, "In-app review failed")
        false
    }
}

private suspend fun requestReviewInfo(manager: ReviewManager): ReviewInfo? =
    suspendCancellableCoroutine { continuation ->
        manager.requestReviewFlow().addOnCompleteListener { task ->
            continuation.resume(if (task.isSuccessful) task.result else null)
        }
    }

private suspend fun launchReviewFlow(manager: ReviewManager, activity: Activity, info: ReviewInfo) {
    suspendCancellableCoroutine<Unit> { continuation ->
        manager.launchReviewFlow(activity, info).addOnCompleteListener {
            continuation.resume(Unit)
        }
    }
}
