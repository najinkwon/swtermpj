package com.example.swtermproject.data.model

data class Recipe(
    val emoji: String,
    val title: String,
    val reason: String,
    val matchPercent: Int,
    val cookTime: String,
    val difficulty: String,
    val ingredients: List<String>,
    val steps: List<String>
)