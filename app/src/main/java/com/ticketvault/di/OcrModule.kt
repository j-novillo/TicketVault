package com.ticketvault.di

import com.ticketvault.ocr.MlKitTextRecognitionGateway
import com.ticketvault.ocr.TextRecognitionGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    @Singleton
    abstract fun bindTextRecognitionGateway(
        impl: MlKitTextRecognitionGateway,
    ): TextRecognitionGateway
}
