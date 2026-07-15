package com.tickettrack.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.tickettrack.`data`.local.dao.CategoryDao
import com.tickettrack.`data`.local.dao.CategoryDao_Impl
import com.tickettrack.`data`.local.dao.StoreDao
import com.tickettrack.`data`.local.dao.StoreDao_Impl
import com.tickettrack.`data`.local.dao.TicketDao
import com.tickettrack.`data`.local.dao.TicketDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TicketTrackDatabase_Impl : TicketTrackDatabase() {
  private val _storeDao: Lazy<StoreDao> = lazy {
    StoreDao_Impl(this)
  }

  private val _ticketDao: Lazy<TicketDao> = lazy {
    TicketDao_Impl(this)
  }

  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "93c32578ad04202cdf5754a885efc8a8", "974f5f33af5d19b44bd995bb410eab84") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stores` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `recognitionPatterns` TEXT NOT NULL, `parsingTemplate` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tickets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storeId` INTEGER, `date` TEXT NOT NULL, `time` TEXT, `total` REAL NOT NULL, `originalImageUri` TEXT, `cleanedImageUri` TEXT, `rawOcrText` TEXT, FOREIGN KEY(`storeId`) REFERENCES `stores`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tickets_storeId` ON `tickets` (`storeId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ticket_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ticketId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `quantity` REAL NOT NULL, `unitPrice` REAL, `totalPrice` REAL NOT NULL, `category` TEXT, `subcategory` TEXT, FOREIGN KEY(`ticketId`) REFERENCES `tickets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_ticket_items_ticketId` ON `ticket_items` (`ticketId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subcategories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` INTEGER NOT NULL, `name` TEXT NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_subcategories_categoryId` ON `subcategories` (`categoryId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '93c32578ad04202cdf5754a885efc8a8')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `stores`")
        connection.execSQL("DROP TABLE IF EXISTS `tickets`")
        connection.execSQL("DROP TABLE IF EXISTS `ticket_items`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `subcategories`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsStores: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStores.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStores.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStores.put("recognitionPatterns", TableInfo.Column("recognitionPatterns", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStores.put("parsingTemplate", TableInfo.Column("parsingTemplate", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStores: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStores: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoStores: TableInfo = TableInfo("stores", _columnsStores, _foreignKeysStores,
            _indicesStores)
        val _existingStores: TableInfo = read(connection, "stores")
        if (!_infoStores.equals(_existingStores)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |stores(com.tickettrack.data.local.entity.StoreEntity).
              | Expected:
              |""".trimMargin() + _infoStores + """
              |
              | Found:
              |""".trimMargin() + _existingStores)
        }
        val _columnsTickets: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTickets.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("storeId", TableInfo.Column("storeId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("date", TableInfo.Column("date", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("time", TableInfo.Column("time", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("total", TableInfo.Column("total", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("originalImageUri", TableInfo.Column("originalImageUri", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("cleanedImageUri", TableInfo.Column("cleanedImageUri", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTickets.put("rawOcrText", TableInfo.Column("rawOcrText", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTickets: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTickets.add(TableInfo.ForeignKey("stores", "SET NULL", "NO ACTION",
            listOf("storeId"), listOf("id")))
        val _indicesTickets: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTickets.add(TableInfo.Index("index_tickets_storeId", false, listOf("storeId"),
            listOf("ASC")))
        val _infoTickets: TableInfo = TableInfo("tickets", _columnsTickets, _foreignKeysTickets,
            _indicesTickets)
        val _existingTickets: TableInfo = read(connection, "tickets")
        if (!_infoTickets.equals(_existingTickets)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tickets(com.tickettrack.data.local.entity.TicketEntity).
              | Expected:
              |""".trimMargin() + _infoTickets + """
              |
              | Found:
              |""".trimMargin() + _existingTickets)
        }
        val _columnsTicketItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTicketItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("ticketId", TableInfo.Column("ticketId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("productName", TableInfo.Column("productName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("quantity", TableInfo.Column("quantity", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("unitPrice", TableInfo.Column("unitPrice", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("totalPrice", TableInfo.Column("totalPrice", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("category", TableInfo.Column("category", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTicketItems.put("subcategory", TableInfo.Column("subcategory", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTicketItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTicketItems.add(TableInfo.ForeignKey("tickets", "CASCADE", "NO ACTION",
            listOf("ticketId"), listOf("id")))
        val _indicesTicketItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTicketItems.add(TableInfo.Index("index_ticket_items_ticketId", false,
            listOf("ticketId"), listOf("ASC")))
        val _infoTicketItems: TableInfo = TableInfo("ticket_items", _columnsTicketItems,
            _foreignKeysTicketItems, _indicesTicketItems)
        val _existingTicketItems: TableInfo = read(connection, "ticket_items")
        if (!_infoTicketItems.equals(_existingTicketItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ticket_items(com.tickettrack.data.local.entity.TicketItemEntity).
              | Expected:
              |""".trimMargin() + _infoTicketItems + """
              |
              | Found:
              |""".trimMargin() + _existingTicketItems)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories,
            _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.tickettrack.data.local.entity.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsSubcategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubcategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubcategories.put("categoryId", TableInfo.Column("categoryId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubcategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubcategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSubcategories.add(TableInfo.ForeignKey("categories", "CASCADE", "NO ACTION",
            listOf("categoryId"), listOf("id")))
        val _indicesSubcategories: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSubcategories.add(TableInfo.Index("index_subcategories_categoryId", false,
            listOf("categoryId"), listOf("ASC")))
        val _infoSubcategories: TableInfo = TableInfo("subcategories", _columnsSubcategories,
            _foreignKeysSubcategories, _indicesSubcategories)
        val _existingSubcategories: TableInfo = read(connection, "subcategories")
        if (!_infoSubcategories.equals(_existingSubcategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |subcategories(com.tickettrack.data.local.entity.SubcategoryEntity).
              | Expected:
              |""".trimMargin() + _infoSubcategories + """
              |
              | Found:
              |""".trimMargin() + _existingSubcategories)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "stores", "tickets",
        "ticket_items", "categories", "subcategories")
  }

  public override fun clearAllTables() {
    super.performClear(true, "stores", "tickets", "ticket_items", "categories", "subcategories")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(StoreDao::class, StoreDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TicketDao::class, TicketDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun storeDao(): StoreDao = _storeDao.value

  public override fun ticketDao(): TicketDao = _ticketDao.value

  public override fun categoryDao(): CategoryDao = _categoryDao.value
}
