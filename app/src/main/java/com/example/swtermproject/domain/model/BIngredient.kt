package com.example.swtermproject.domain.model

import com.example.swtermproject.common.BConstants

data class BIngredient(
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
    val stockRate: Double
        get() = if (initialAmount <= 0.0) 0.0 else currentAmount / initialAmount

    val stockPercent: Int
        get() = (stockRate * 100).toInt()

    val isLowStock: Boolean
        get() = stockRate <= BConstants.LOW_STOCK_RATE
}
