package com.example.swtermproject.data.repository

import com.example.swtermproject.data.local.dao.BIngredientDao
import com.example.swtermproject.data.local.entity.BIngredientEntity
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BIngredientRepository(
    private val ingredientDao: BIngredientDao
) {
    fun observeIngredients(): Flow<List<BIngredient>> {
        return ingredientDao.observeAllIngredients().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getAllIngredients(): List<BIngredient> {
        return ingredientDao.getAllIngredients().map { it.toDomain() }
    }

    suspend fun addIngredient(ingredient: BIngredient): Long {
        return ingredientDao.insertIngredient(ingredient.toEntity())
    }

    suspend fun updateIngredient(ingredient: BIngredient) {
        ingredientDao.updateIngredient(ingredient.toEntity())
    }

    suspend fun updateCurrentAmount(id: Long, currentAmount: Double) {
        ingredientDao.updateCurrentAmount(id, currentAmount)
    }

    suspend fun deleteIngredient(id: Long) {
        ingredientDao.deleteIngredientById(id)
    }

    suspend fun getLowStockIngredients(): List<BIngredient> {
        return ingredientDao.getLowStockIngredients().map { it.toDomain() }
    }
}

fun BIngredientEntity.toDomain(): BIngredient {
    return BIngredient(
        id = id,
        name = name,
        category = category,
        initialAmount = initialAmount,
        currentAmount = currentAmount,
        unit = unit,
        expiryDate = expiryDate,
        storageType = storageType,
        createdAt = createdAt
    )
}

fun BIngredient.toEntity(): BIngredientEntity {
    return BIngredientEntity(
        id = id,
        name = name,
        category = category,
        initialAmount = initialAmount,
        currentAmount = currentAmount,
        unit = unit,
        expiryDate = expiryDate,
        storageType = storageType,
        createdAt = createdAt
    )
}
