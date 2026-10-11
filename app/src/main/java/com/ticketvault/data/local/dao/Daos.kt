package com.ticketvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ticketvault.data.local.entity.CategoryEntity
import com.ticketvault.data.local.entity.StoreEntity
import com.ticketvault.data.local.entity.SubcategoryEntity
import com.ticketvault.data.local.entity.TicketEntity
import com.ticketvault.data.local.entity.TicketItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY name ASC")
    fun observeAll(): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(store: StoreEntity): Long
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TicketItemEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name ASC")
    fun observeSubcategories(categoryId: Long): Flow<List<SubcategoryEntity>>
}
