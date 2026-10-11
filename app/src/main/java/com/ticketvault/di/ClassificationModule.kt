package com.ticketvault.di

import com.ticketvault.classification.AssetCategoryDictionarySource
import com.ticketvault.classification.CategoryDictionarySource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassificationModule {

    @Binds
    @Singleton
    abstract fun bindCategoryDictionarySource(
        impl: AssetCategoryDictionarySource,
    ): CategoryDictionarySource
}
