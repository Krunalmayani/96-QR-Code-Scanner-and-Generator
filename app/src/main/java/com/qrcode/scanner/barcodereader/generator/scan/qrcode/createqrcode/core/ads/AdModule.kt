package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {
    @Binds
    abstract fun bindAdManager(impl: NoOpAdManager): AdManager
}
