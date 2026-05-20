package com.example.swtermproject.data.model

data class Ingredient(
    val name: String,
    val category: String,
    var percent: Int,
    var favorite: Boolean = false,
    var expireDay: Int = 7
)