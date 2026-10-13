package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation runner for every androidTest: identical to AndroidJUnitRunner except that the application under
 * test is [HiltTestApplication], which Hilt tests need to replace modules with `@TestInstallIn`.
 * Tests that do not use Hilt (Compose component tests, Room tests) are unaffected by the different Application.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
