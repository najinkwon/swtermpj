package com.example.swtermproject.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.swtermproject.data.local.entity.BIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BIngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY createdAt DESC")
    fun observeAllIngredients(): Flow<List<BIngredientEntity>>

    @Query("SELECT * FROM ingredients ORDER BY createdAt DESC")
    suspend fun getAllIngredients(): List<BIngredientEntity>

    @Query("SELECT * FROM ingredients WHERE id = :id LIMIT 1")
    suspend fun getIngredientById(id: Long): BIngredientEntity?

    @Query("SELECT * FROM ingredients WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getIngredientByName(name: String): BIngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: BIngredientEntity): Long

    @Update
    suspend fun updateIngredient(ingredient: BIngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: BIngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: Long)

    @Query("UPDATE ingredients SET currentAmount = :currentAmount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Long, currentAmount: Double)

    @Query("SELECT * FROM ingredients WHERE initialAmount > 0 AND currentAmount <= initialAmount * 0.2 ORDER BY currentAmount ASC")
    suspend fun getLowStockIngredients(): List<BIngredientEntity>
}
