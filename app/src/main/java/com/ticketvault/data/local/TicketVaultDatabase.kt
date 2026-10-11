package com.ticketvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ticketvault.data.local.dao.CategoryDao
import com.ticketvault.data.local.dao.StoreDao
import com.ticketvault.data.local.dao.TicketDao
import com.ticketvault.data.local.entity.CategoryEntity
import com.ticketvault.data.local.entity.StoreEntity
import com.ticketvault.data.local.entity.SubcategoryEntity
import com.ticketvault.data.local.entity.TicketEntity
import com.ticketvault.data.local.entity.TicketItemEntity

@Database(
    entities = [
        StoreEntity::class,
        TicketEntity::class,
        TicketItemEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TicketVaultDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun ticketDao(): TicketDao
    abstract fun categoryDao(): CategoryDao
}
