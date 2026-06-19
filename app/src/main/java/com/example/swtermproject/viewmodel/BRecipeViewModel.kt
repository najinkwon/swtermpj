package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.data.repository.BRecipeRepository
import com.example.swtermproject.domain.model.BRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val ingredientRepository = BIngredientRepository(
        BAppDatabase.getDatabase(application).ingredientDao()
    )

    private val recipeRepository = BRecipeRepository()

    private val _recipes = MutableStateFlow<List<BRecipe>>(emptyList())
    val recipes: StateFlow<List<BRecipe>> = _recipes

    private var recommendJob: Job? = null

    fun recommend(category: String, fridgeCleanMode: Boolean = false) {
        recommendJob?.cancel()

        recommendJob = viewModelScope.launch {
            val recommendedRecipes = withContext(Dispatchers.IO) {
                val ingredients = ingredientRepository.getAllIngredients()

                recipeRepository.recommendRecipes(
                    ingredients = ingredients,
                    category = category,
                    fridgeCleanMode = fridgeCleanMode
                )
            }

            _recipes.value = recommendedRecipes
        }
    }
}