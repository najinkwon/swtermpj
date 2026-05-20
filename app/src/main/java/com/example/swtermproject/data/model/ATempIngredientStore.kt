package com.example.swtermproject.data.model

object ATempIngredientStore {

    val ingredients = mutableListOf(
        Ingredient("간장", "조미료/소스", 18, false, 30),
        Ingredient("우유", "유제품", 15, false, 2),
        Ingredient("대파", "채소", 12, false, 3),
        Ingredient("계란", "단백질", 65, false, 5),
        Ingredient("치즈", "유제품", 42, false, 10)
    )

    fun addIngredient(newIngredient: Ingredient): Boolean {

        val existingIngredient =
            ingredients.find {
                it.name.equals(
                    newIngredient.name,
                    ignoreCase = true
                )
            }

        return if (existingIngredient != null) {

            val index =
                ingredients.indexOf(existingIngredient)

            ingredients[index] =
                existingIngredient.copy(
                    percent = 100,
                    expireDay = 7
                )

            false

        } else {

            ingredients.add(0, newIngredient)

            true
        }
    }

    fun removeIngredient(position: Int) {

        if (position in ingredients.indices) {

            ingredients.removeAt(position)
        }
    }
}