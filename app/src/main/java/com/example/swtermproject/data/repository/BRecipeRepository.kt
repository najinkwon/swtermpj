package com.example.swtermproject.data.repository

import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.recipe.BRecipeRecommender

class BRecipeRepository(
    private val recommender: BRecipeRecommender = BRecipeRecommender()
) {
    fun recommendRecipes(
        ingredients: List<BIngredient>,
        category: String,
        fridgeCleanMode: Boolean = false
    ): List<BRecipe> {
        return recommender.recommend(
            ingredients = ingredients,
            category = category,
            fridgeCleanMode = fridgeCleanMode
        )
    }
}
