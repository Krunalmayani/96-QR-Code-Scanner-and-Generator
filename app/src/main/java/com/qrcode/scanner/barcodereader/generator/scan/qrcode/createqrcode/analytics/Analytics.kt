package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val fa = FirebaseAnalytics.getInstance(context)

    fun log(event: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle().apply { params.forEach { (k, v) -> putString(k, v) } }
        fa.logEvent(event, bundle)
    }
}
