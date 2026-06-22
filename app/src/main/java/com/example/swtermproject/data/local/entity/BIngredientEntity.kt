package com.example.swtermproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.swtermproject.common.BConstants

@Entity(tableName = "ingredients")
data class BIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val initialAmount: Double,
    val currentAmount: Double,
    val unit: String,
    val expiryDate: String,
    val storageType: String,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun stockRate(): Double {
        if (initialAmount <= 0.0) return 0.0
        return currentAmount / initialAmount
    }

    fun isLowStock(): Boolean {
        return stockRate() <= BConstants.LOW_STOCK_RATE
    }
}
