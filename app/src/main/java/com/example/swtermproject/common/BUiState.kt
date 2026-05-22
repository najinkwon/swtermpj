package com.example.swtermproject.common

data class BUiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val message: String? = null
)
