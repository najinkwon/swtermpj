package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.ml.BIngredientClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BIngredientViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BAppDatabase.getDatabase(application)
    private val repository = BIngredientRepository(database.ingredientDao())
    private val classifier = BIngredientClassifier(application)

    private val _ingredients = MutableStateFlow<List<BIngredient>>(emptyList())
    val ingredients: StateFlow<List<BIngredient>> = _ingredients

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        observeIngredients()
    }

    private fun observeIngredients() {
        viewModelScope.launch {
            repository.observeIngredients().collect {
                _ingredients.value = it
            }
        }
    }

    fun addIngredient(
        name: String,
        initialAmount: Double,
        currentAmount: Double,
        unit: String,
        expiryDate: String,
        storageType: String
    ) {
        viewModelScope.launch {
            runCatching {
                val prediction = classifier.classify(name)

                repository.addIngredient(
                    BIngredient(
                        name = name,
                        category = prediction.category,
                        initialAmount = initialAmount,
                        currentAmount = currentAmount,
                        unit = unit,
                        expiryDate = expiryDate,
                        storageType = storageType
                    )
                )
            }.onSuccess {
                _message.value = "재료가 추가되었습니다."
            }.onFailure {
                _message.value = it.message ?: "재료 추가에 실패했습니다."
            }
        }
    }

    fun updateCurrentAmount(id: Long, currentAmount: Double) {
        viewModelScope.launch {
            repository.updateCurrentAmount(id, currentAmount)
        }
    }

    fun updateIngredient(ingredient: BIngredient) {
        viewModelScope.launch {
            runCatching {
                repository.updateIngredient(ingredient)
            }.onSuccess {
                _message.value = "재료 정보가 수정되었습니다."
            }.onFailure {
                _message.value = it.message ?: "재료 정보 수정에 실패했습니다."
            }
        }
    }

    fun deleteIngredient(id: Long) {
        viewModelScope.launch {
            repository.deleteIngredient(id)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
