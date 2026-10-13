package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.billing

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class NoOpBillingManagerTest {
    private val billing: BillingManager = NoOpBillingManager()

    @Test
    fun `nobody is ad free by default`() = runTest {
        assertFalse(billing.isAdFree.first())
    }

    @Test
    fun `ad free flow emits a single false and completes`() = runTest {
        billing.isAdFree.test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `purchase is not available and does nothing`() = runTest {
        assertFalse(billing.isPurchaseAvailable)
        billing.purchaseRemoveAds()
        assertFalse(billing.isAdFree.first())
    }
}
