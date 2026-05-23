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

    private val _similarIngredient = MutableStateFlow<SimilarIngredientState?>(null)
    val similarIngredient: StateFlow<SimilarIngredientState?> = _similarIngredient

    data class SimilarIngredientState(
        val existing: BIngredient,
        val incoming: BIngredient
    )

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

    fun addIngredientManually(
        name: String,
        category: String,
        initialAmount: Double,
        currentAmount: Double,
        unit: String,
        expiryDate: String,
        storageType: String
    ) {
        viewModelScope.launch {
            runCatching {
                val incoming = BIngredient(
                    name = name,
                    category = category,
                    initialAmount = initialAmount,
                    currentAmount = currentAmount,
                    unit = unit,
                    expiryDate = expiryDate,
                    storageType = storageType
                )

                val similar = repository.findSimilarIngredientByName(name)

                if (similar != null) {
                    _similarIngredient.value = SimilarIngredientState(
                        existing = similar,
                        incoming = incoming
                    )
                    return@launch
                }

                repository.addIngredient(incoming)
                _message.value = "재료가 추가되었습니다."
            }.onFailure {
                _message.value = it.message ?: "재료 추가에 실패했습니다."
            }
        }
    }

    fun resolveSimilarIngredient(merge: Boolean) {
        val state = _similarIngredient.value ?: return
        _similarIngredient.value = null

        viewModelScope.launch {
            runCatching {
                if (merge) {
                    repository.mergeIngredientWithExisting(
                        existingId = state.existing.id,
                        incoming = state.incoming
                    )
                } else {
                    repository.addIngredientAsNew(state.incoming)
                }
            }.onSuccess {
                _message.value =
                    if (merge) {
                        "기존 재료와 병합했습니다."
                    } else {
                        "새 재료로 추가했습니다."
                    }
            }.onFailure {
                _message.value = it.message ?: "재료 추가에 실패했습니다."
            }
        }
    }

    fun clearSimilarIngredient() {
        _similarIngredient.value = null
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
