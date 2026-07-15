package com.tickettrack.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.tickettrack.`data`.local.entity.StoreEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StoreDao_Impl(
  __db: RoomDatabase,
) : StoreDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfStoreEntity: EntityInsertAdapter<StoreEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfStoreEntity = object : EntityInsertAdapter<StoreEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `stores` (`id`,`name`,`recognitionPatterns`,`parsingTemplate`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StoreEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.recognitionPatterns)
        statement.bindText(4, entity.parsingTemplate)
      }
    }
  }

  public override suspend fun insert(store: StoreEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfStoreEntity.insertAndReturnId(_connection, store)
    _result
  }

  public override fun observeAll(): Flow<List<StoreEntity>> {
    val _sql: String = "SELECT * FROM stores ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("stores")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfRecognitionPatterns: Int = getColumnIndexOrThrow(_stmt,
            "recognitionPatterns")
        val _columnIndexOfParsingTemplate: Int = getColumnIndexOrThrow(_stmt, "parsingTemplate")
        val _result: MutableList<StoreEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: StoreEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpRecognitionPatterns: String
          _tmpRecognitionPatterns = _stmt.getText(_columnIndexOfRecognitionPatterns)
          val _tmpParsingTemplate: String
          _tmpParsingTemplate = _stmt.getText(_columnIndexOfParsingTemplate)
          _item = StoreEntity(_tmpId,_tmpName,_tmpRecognitionPatterns,_tmpParsingTemplate)
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
