package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/** JUnit 5 extension that swaps Dispatchers.Main for a test dispatcher: `@RegisterExtension val main = MainDispatcherExtension()`. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherExtension(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) = Dispatchers.setMain(dispatcher)

    override fun afterEach(context: ExtensionContext?) = Dispatchers.resetMain()
}
