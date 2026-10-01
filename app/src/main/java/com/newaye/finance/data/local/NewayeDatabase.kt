package com.newaye.finance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.newaye.finance.data.local.dao.AccountDao
import com.newaye.finance.data.local.entity.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NewayeDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    companion object {

        @Volatile
        private var INSTANCE: NewayeDatabase? = null

        fun getDatabase(context: Context): NewayeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewayeDatabase::class.java,
                    "newaye_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
