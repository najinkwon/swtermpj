package com.example.swtermproject.common

sealed class BResource<out T> {
    data class Success<T>(val data: T) : BResource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : BResource<Nothing>()
    object Loading : BResource<Nothing>()
}
