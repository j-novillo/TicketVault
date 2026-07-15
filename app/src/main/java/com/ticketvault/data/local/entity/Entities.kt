package com.ticketvault.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val recognitionPatterns: String,
    val parsingTemplate: String,
)

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("storeId")],
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long?,
    val date: String,
    val time: String?,
    val total: Double,
    val originalImageUri: String?,
    val cleanedImageUri: String?,
    val rawOcrText: String?,
)

@Entity(
    tableName = "ticket_items",
    foreignKeys = [
        ForeignKey(
            entity = TicketEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("ticketId")],
)
data class TicketItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double?,
    val totalPrice: Double,
    val category: String?,
    val subcategory: String?,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class SubcategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
)
