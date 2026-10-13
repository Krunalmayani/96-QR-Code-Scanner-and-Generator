package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Empty activity that instrumented Compose tests launch with `createAndroidComposeRule<HiltTestActivity>()`.
 * `hiltViewModel()` needs an `@AndroidEntryPoint` host; the plain ComponentActivity from ui-test-manifest is not one.
 * Lives in the debug source set (not androidTest) because ActivityScenario resolves activities in the app package.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
