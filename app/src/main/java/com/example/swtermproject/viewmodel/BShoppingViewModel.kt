package com.example.swtermproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.remote.BRetrofitClient
import com.example.swtermproject.data.repository.BShoppingRepository
import com.example.swtermproject.domain.model.BShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BShoppingViewModel : ViewModel() {
    private val repository = BShoppingRepository(BRetrofitClient.shoppingApi)

    private val _items = MutableStateFlow<List<BShoppingItem>>(emptyList())
    val items: StateFlow<List<BShoppingItem>> = _items

    fun search(keyword: String) {
        viewModelScope.launch {
            _items.value = repository.searchShoppingItems(keyword)
        }
    }
}
