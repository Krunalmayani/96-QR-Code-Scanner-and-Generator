package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.billing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    abstract fun bindBillingManager(impl: NoOpBillingManager): BillingManager
}
