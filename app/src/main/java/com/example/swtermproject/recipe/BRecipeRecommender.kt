package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe

class BRecipeRecommender {
    fun recommend(
        ingredients: List<BIngredient>,
        category: String,
        fridgeCleanMode: Boolean = false
    ): List<BRecipe> {
        return BRecipeDataSource.recipes
            .filter { it.category == category }
            .map { recipe -> BRecipeScorer.scoreRecipe(recipe, ingredients) }
            .filter { scoredRecipe ->
                if (fridgeCleanMode) {
                    scoredRecipe.missingIngredients.size <= 1
                } else {
                    scoredRecipe.score > 0
                }
            }
            .sortedByDescending { it.score }
    }
}
