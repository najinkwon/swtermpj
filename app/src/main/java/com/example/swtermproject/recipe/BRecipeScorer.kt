package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe

object BRecipeScorer {
    fun scoreRecipe(recipe: BRecipe, ingredients: List<BIngredient>): BRecipe {
        val ownedNames = ingredients.map { it.name.trim() }

        val mainMatchCount = recipe.mainIngredients.count { required ->
            ownedNames.any { owned -> owned.contains(required) || required.contains(owned) }
        }

        val subMatchCount = recipe.subIngredients.count { required ->
            ownedNames.any { owned -> owned.contains(required) || required.contains(owned) }
        }

        val seasoningMatchCount = recipe.seasonings.count { required ->
            ownedNames.any { owned -> owned.contains(required) || required.contains(owned) }
        }

        val missingMainIngredients = recipe.mainIngredients.filter { required ->
            ownedNames.none { owned -> owned.contains(required) || required.contains(owned) }
        }

        val missingSubIngredients = recipe.subIngredients.filter { required ->
            ownedNames.none { owned -> owned.contains(required) || required.contains(owned) }
        }

        val score =
            mainMatchCount * 50 +
            subMatchCount * 15 +
            seasoningMatchCount * 5 -
            missingMainIngredients.size * 40

        return recipe.copy(
            score = score.coerceAtLeast(0),
            missingIngredients = missingMainIngredients + missingSubIngredients
        )
    }
}
