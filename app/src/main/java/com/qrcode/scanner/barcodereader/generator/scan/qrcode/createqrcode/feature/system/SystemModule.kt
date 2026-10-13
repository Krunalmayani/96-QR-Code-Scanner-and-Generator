package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {
    @Binds
    abstract fun bindInAppUpdater(impl: PlayInAppUpdater): InAppUpdater
}
