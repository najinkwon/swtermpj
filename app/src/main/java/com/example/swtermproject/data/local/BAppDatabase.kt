package com.example.swtermproject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class BAppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): BIngredientDao
    abstract fun notificationDao(): BNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: BAppDatabase? = null

        fun getDatabase(context: Context): BAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BAppDatabase::class.java,
                    BConstants.DATABASE_NAME
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
