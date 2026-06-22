package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe

class BRecipeRecommender {
    fun recommend(
        ingredients: List<BIngredient>,
        category: String,
        fridgeCleanMode: Boolean = false
    ): List<BRecipe> {
        val categoryRecipes =
            BRecipeDataSource.recipes
                .filter { it.category == category }

        val scoredRecipes =
            categoryRecipes.map { recipe ->
                BRecipeScorer.scoreRecipe(recipe, ingredients)
            }

        return if (fridgeCleanMode) {
            scoredRecipes
                .filter { it.missingIngredients.size <= 1 }
                .sortedByDescending { it.score }
        } else {
            // 기본 레시피 화면에서는 매칭 점수가 0이어도 보여준다.
            // 사용자가 가진 재료가 없어도 기본 레시피 데이터셋을 제공해야 하기 때문.
            scoredRecipes
                .sortedWith(
                    compareByDescending<BRecipe> { it.score }
                        .thenBy { it.missingIngredients.size }
                        .thenBy { it.title }
                )
        }
    }
}
