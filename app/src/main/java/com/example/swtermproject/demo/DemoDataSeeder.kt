package com.example.swtermproject.demo

import android.content.Context
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DemoDataSeeder {

    suspend fun seedIfNeeded(context: Context) {
        withContext(Dispatchers.IO) {
            val repository = BIngredientRepository(
                BAppDatabase.getDatabase(context.applicationContext).ingredientDao()
            )

            val existing = repository.getAllIngredients()

            if (existing.isNotEmpty()) {
                return@withContext
            }

            val demoIngredients = listOf(
                BIngredient(
                    name = "계란",
                    category = "단백질",
                    initialAmount = 10.0,
                    currentAmount = 6.0,
                    unit = "개",
                    expiryDate = dateAfter(1),
                    storageType = "냉장"
                ),
                BIngredient(
                    name = "우유",
                    category = "유제품",
                    initialAmount = 1000.0,
                    currentAmount = 150.0,
                    unit = "ml",
                    expiryDate = dateAfter(3),
                    storageType = "냉장"
                ),
                BIngredient(
                    name = "양파",
                    category = "채소",
                    initialAmount = 3.0,
                    currentAmount = 2.0,
                    unit = "개",
                    expiryDate = dateAfter(7),
                    storageType = "실온"
                ),
                BIngredient(
                    name = "대파",
                    category = "채소",
                    initialAmount = 2.0,
                    currentAmount = 1.0,
                    unit = "개",
                    expiryDate = dateAfter(2),
                    storageType = "냉장"
                ),
                BIngredient(
                    name = "밥",
                    category = "기타",
                    initialAmount = 2.0,
                    currentAmount = 2.0,
                    unit = "개",
                    expiryDate = dateAfter(5),
                    storageType = "냉장"
                ),
                BIngredient(
                    name = "간장",
                    category = "조미료/소스",
                    initialAmount = 500.0,
                    currentAmount = 500.0,
                    unit = "ml",
                    expiryDate = dateAfter(30),
                    storageType = "실온"
                ),
                BIngredient(
                    name = "참기름",
                    category = "조미료/소스",
                    initialAmount = 300.0,
                    currentAmount = 250.0,
                    unit = "ml",
                    expiryDate = dateAfter(30),
                    storageType = "실온"
                ),
                BIngredient(
                    name = "바나나",
                    category = "기타",
                    initialAmount = 5.0,
                    currentAmount = 1.0,
                    unit = "개",
                    expiryDate = dateAfter(2),
                    storageType = "실온"
                )
            )

            demoIngredients.forEach { ingredient ->
                repository.addIngredientAsNew(ingredient)
            }
        }
    }

    private fun dateAfter(days: Int): String {
        val calendar = Calendar.getInstance(Locale.KOREA)
        calendar.add(Calendar.DAY_OF_MONTH, days)

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.KOREA
        ).format(calendar.time)
    }
}
