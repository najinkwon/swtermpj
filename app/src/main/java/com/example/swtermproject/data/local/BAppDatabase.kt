package com.example.swtermproject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.swtermproject.common.BConstants
import com.example.swtermproject.data.local.dao.BIngredientDao
import com.example.swtermproject.data.local.dao.BNotificationDao
import com.example.swtermproject.data.local.entity.BIngredientEntity
import com.example.swtermproject.data.local.entity.BNotificationEntity

@Database(
    entities = [
        BIngredientEntity::class,
        BNotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BAppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): BIngredientDao
    abstract fun notificationDao(): BNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: BAppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE ingredients ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): BAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BAppDatabase::class.java,
                    BConstants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
