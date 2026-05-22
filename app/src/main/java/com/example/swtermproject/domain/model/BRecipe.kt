package com.example.swtermproject.domain.model

data class BRecipe(
    val id: Long,
    val title: String,
    val category: String,
    val mainIngredients: List<String>,
    val subIngredients: List<String>,
    val seasonings: List<String>,
    val description: String,
    val youtubeKeyword: String,
    val score: Int = 0,
    val missingIngredients: List<String> = emptyList()
)
