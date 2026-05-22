package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.data.repository.BRecipeRepository
import com.example.swtermproject.domain.model.BRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BRecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientRepository = BIngredientRepository(
        BAppDatabase.getDatabase(application).ingredientDao()
    )
    private val recipeRepository = BRecipeRepository()

    private val _recipes = MutableStateFlow<List<BRecipe>>(emptyList())
    val recipes: StateFlow<List<BRecipe>> = _recipes

    fun recommend(category: String, fridgeCleanMode: Boolean = false) {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllIngredients()
            _recipes.value = recipeRepository.recommendRecipes(
                ingredients = ingredients,
                category = category,
                fridgeCleanMode = fridgeCleanMode
            )
        }
    }
}
