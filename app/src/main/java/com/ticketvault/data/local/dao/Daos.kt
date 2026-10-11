package com.ticketvault.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM stores")
    suspend fun getAll(): List<StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getById(id: Long): StoreEntity?

    @Query("SELECT * FROM stores WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): StoreEntity?

    @Query("SELECT templateId FROM stores WHERE templateId IS NOT NULL")
    suspend fun getTemplateIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(store: StoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringDuplicates(stores: List<StoreEntity>)

    @Update
    suspend fun update(store: StoreEntity)
}

data class TicketWithStoreName(
    @Embedded val ticket: TicketEntity,
    val storeName: String?,
)

@Dao
interface TicketDao {
    @Query(
        """
        SELECT tickets.*, stores.name AS storeName
        FROM tickets LEFT JOIN stores ON stores.id = tickets.storeId
        ORDER BY tickets.capturedAt DESC
        """,
    )
    fun observeAllWithStoreName(): Flow<List<TicketWithStoreName>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getById(id: Long): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity): Long

    @Query("UPDATE tickets SET storeId = :storeId WHERE id = :ticketId")
    suspend fun updateStore(ticketId: Long, storeId: Long?)

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
