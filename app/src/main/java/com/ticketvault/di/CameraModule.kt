package com.ticketvault.di

import com.ticketvault.camera.ColorMatrixTicketImageCleaner
import com.ticketvault.camera.DocumentScannerGateway
import com.ticketvault.camera.GmsDocumentScannerGateway
import com.ticketvault.camera.TicketImageCleaner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {

    @Binds
    @Singleton
    abstract fun bindDocumentScannerGateway(
        impl: GmsDocumentScannerGateway,
    ): DocumentScannerGateway

    @Binds
    @Singleton
    abstract fun bindTicketImageCleaner(
        impl: ColorMatrixTicketImageCleaner,
    ): TicketImageCleaner
}
