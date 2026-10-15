package com.newaye.finance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.newaye.finance.data.local.dao.AccountDao
import com.newaye.finance.data.local.dao.CategoryDao
import com.newaye.finance.data.local.dao.TransactionDao
import com.newaye.finance.data.local.entity.AccountEntity
import com.newaye.finance.data.local.entity.CategoryEntity
import com.newaye.finance.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NewayeDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "isDefault INTEGER NOT NULL)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name_type " +
                        "ON categories(name, type)"
                )
            }
        }

        @Volatile
        private var INSTANCE: NewayeDatabase? = null

        fun getDatabase(context: Context): NewayeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewayeDatabase::class.java,
                    "newaye_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
