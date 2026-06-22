package com.example.swtermproject.domain.model

data class BRecipeIngredientRequirement(
    val name: String,
    val amount: Double,
    val unit: String,
    val group: String = "main",
    val essential: Boolean = true
)

data class BRecipeIngredientStatus(
    val requirement: BRecipeIngredientRequirement,
    val ownedIngredientId: Long? = null,
    val ownedName: String? = null,
    val ownedAmount: Double = 0.0,
    val ownedUnit: String = "",
    val isOwned: Boolean = false,
    val isUnitCompatible: Boolean = false,
    val isEnough: Boolean = false,
    val missingAmount: Double = 0.0
)

data class BRecipe(
    val id: Long,
    val title: String,
    val category: String,
    val mainIngredients: List<String>,
    val subIngredients: List<String>,
    val seasonings: List<String>,
    val description: String,
    val youtubeKeyword: String,
    val requiredIngredients: List<BRecipeIngredientRequirement> = emptyList(),
    val score: Int = 0,
    val missingIngredients: List<String> = emptyList(),
    val ingredientStatuses: List<BRecipeIngredientStatus> = emptyList()
)