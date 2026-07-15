package com.ticketvault.di

import android.content.Context
import androidx.room.Room
import com.ticketvault.data.local.TicketVaultDatabase
import com.ticketvault.data.local.dao.CategoryDao
import com.ticketvault.data.local.dao.StoreDao
import com.ticketvault.data.local.dao.TicketDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): TicketVaultDatabase =
        Room.databaseBuilder(
            context,
            TicketVaultDatabase::class.java,
            "ticketvault.db",
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideStoreDao(database: TicketVaultDatabase): StoreDao = database.storeDao()

    @Provides
    fun provideTicketDao(database: TicketVaultDatabase): TicketDao = database.ticketDao()

    @Provides
    fun provideCategoryDao(database: TicketVaultDatabase): CategoryDao = database.categoryDao()
}
