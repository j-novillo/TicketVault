package com.tickettrack.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.tickettrack.`data`.local.entity.TicketEntity
import com.tickettrack.`data`.local.entity.TicketItemEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TicketDao_Impl(
  __db: RoomDatabase,
) : TicketDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTicketEntity: EntityInsertAdapter<TicketEntity>

  private val __insertAdapterOfTicketItemEntity: EntityInsertAdapter<TicketItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTicketEntity = object : EntityInsertAdapter<TicketEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `tickets` (`id`,`storeId`,`date`,`time`,`total`,`originalImageUri`,`cleanedImageUri`,`rawOcrText`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TicketEntity) {
        statement.bindLong(1, entity.id)
        val _tmpStoreId: Long? = entity.storeId
        if (_tmpStoreId == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmpStoreId)
        }
        statement.bindText(3, entity.date)
        val _tmpTime: String? = entity.time
        if (_tmpTime == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpTime)
        }
        statement.bindDouble(5, entity.total)
        val _tmpOriginalImageUri: String? = entity.originalImageUri
        if (_tmpOriginalImageUri == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpOriginalImageUri)
        }
        val _tmpCleanedImageUri: String? = entity.cleanedImageUri
        if (_tmpCleanedImageUri == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpCleanedImageUri)
        }
        val _tmpRawOcrText: String? = entity.rawOcrText
        if (_tmpRawOcrText == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpRawOcrText)
        }
      }
    }
    this.__insertAdapterOfTicketItemEntity = object : EntityInsertAdapter<TicketItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `ticket_items` (`id`,`ticketId`,`productName`,`quantity`,`unitPrice`,`totalPrice`,`category`,`subcategory`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TicketItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.ticketId)
        statement.bindText(3, entity.productName)
        statement.bindDouble(4, entity.quantity)
        val _tmpUnitPrice: Double? = entity.unitPrice
        if (_tmpUnitPrice == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpUnitPrice)
        }
        statement.bindDouble(6, entity.totalPrice)
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpCategory)
        }
        val _tmpSubcategory: String? = entity.subcategory
        if (_tmpSubcategory == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpSubcategory)
        }
      }
    }
  }

  public override suspend fun insert(ticket: TicketEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfTicketEntity.insertAndReturnId(_connection, ticket)
    _result
  }

  public override suspend fun insertItems(items: List<TicketItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTicketItemEntity.insert(_connection, items)
  }

  public override fun observeAll(): Flow<List<TicketEntity>> {
    val _sql: String = "SELECT * FROM tickets ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("tickets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStoreId: Int = getColumnIndexOrThrow(_stmt, "storeId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTime: Int = getColumnIndexOrThrow(_stmt, "time")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfOriginalImageUri: Int = getColumnIndexOrThrow(_stmt, "originalImageUri")
        val _columnIndexOfCleanedImageUri: Int = getColumnIndexOrThrow(_stmt, "cleanedImageUri")
        val _columnIndexOfRawOcrText: Int = getColumnIndexOrThrow(_stmt, "rawOcrText")
        val _result: MutableList<TicketEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TicketEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStoreId: Long?
          if (_stmt.isNull(_columnIndexOfStoreId)) {
            _tmpStoreId = null
          } else {
            _tmpStoreId = _stmt.getLong(_columnIndexOfStoreId)
          }
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTime: String?
          if (_stmt.isNull(_columnIndexOfTime)) {
            _tmpTime = null
          } else {
            _tmpTime = _stmt.getText(_columnIndexOfTime)
          }
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          val _tmpOriginalImageUri: String?
          if (_stmt.isNull(_columnIndexOfOriginalImageUri)) {
            _tmpOriginalImageUri = null
          } else {
            _tmpOriginalImageUri = _stmt.getText(_columnIndexOfOriginalImageUri)
          }
          val _tmpCleanedImageUri: String?
          if (_stmt.isNull(_columnIndexOfCleanedImageUri)) {
            _tmpCleanedImageUri = null
          } else {
            _tmpCleanedImageUri = _stmt.getText(_columnIndexOfCleanedImageUri)
          }
          val _tmpRawOcrText: String?
          if (_stmt.isNull(_columnIndexOfRawOcrText)) {
            _tmpRawOcrText = null
          } else {
            _tmpRawOcrText = _stmt.getText(_columnIndexOfRawOcrText)
          }
          _item =
              TicketEntity(_tmpId,_tmpStoreId,_tmpDate,_tmpTime,_tmpTotal,_tmpOriginalImageUri,_tmpCleanedImageUri,_tmpRawOcrText)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
