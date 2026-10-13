package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.di

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image.ImageCodeReader
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image.MlKitImageCodeReader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanBindingsModule {
    @Binds
    abstract fun bindImageCodeReader(impl: MlKitImageCodeReader): ImageCodeReader
}
