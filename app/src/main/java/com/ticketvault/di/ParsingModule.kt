package com.ticketvault.di

import com.ticketvault.parsing.store.AssetStoreTemplateSource
import com.ticketvault.parsing.store.StoreTemplateSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParsingModule {

    @Binds
    @Singleton
    abstract fun bindStoreTemplateSource(
        impl: AssetStoreTemplateSource,
    ): StoreTemplateSource
}
