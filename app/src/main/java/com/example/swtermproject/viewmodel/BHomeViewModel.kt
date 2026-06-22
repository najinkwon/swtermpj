package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BIngredientRepository(
        BAppDatabase.getDatabase(application).ingredientDao()
    )

    private val _ingredients = MutableStateFlow<List<BIngredient>>(emptyList())
    val ingredients: StateFlow<List<BIngredient>> = _ingredients

    val totalCount: Int
        get() = _ingredients.value.size

    val lowStockCount: Int
        get() = _ingredients.value.count { it.isLowStock }

    init {
        viewModelScope.launch {
            repository.observeIngredients().collect {
                _ingredients.value = it
            }
        }
    }
}
