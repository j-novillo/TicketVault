package com.tickettrack.`data`.local.dao

import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.sqlite.SQLiteStatement
import com.tickettrack.`data`.local.entity.CategoryEntity
import com.tickettrack.`data`.local.entity.SubcategoryEntity
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
public class CategoryDao_Impl(
  __db: RoomDatabase,
) : CategoryDao {
  private val __db: RoomDatabase
  init {
    this.__db = __db
  }

  public override fun observeCategories(): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM categories ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = CategoryEntity(_tmpId,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSubcategories(categoryId: Long): Flow<List<SubcategoryEntity>> {
    val _sql: String = "SELECT * FROM subcategories WHERE categoryId = ? ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("subcategories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, categoryId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<SubcategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubcategoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCategoryId: Long
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = SubcategoryEntity(_tmpId,_tmpCategoryId,_tmpName)
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
