package com.example.swtermproject.ml

enum class BIngredientCategory(val label: String) {
    PROTEIN("단백질"),
    DAIRY("유제품"),
    VEGETABLE("채소"),
    MEAT("육류"),
    SEAFOOD("해산물"),
    CARBOHYDRATE("탄수화물"),
    SEASONING("조미료"),
    ETC("기타");

    companion object {
        fun fromLabel(label: String): BIngredientCategory {
            return values().firstOrNull { it.label == label } ?: ETC
        }
    }
}
