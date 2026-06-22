package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BIngredient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object BLocalFridgeRecipeGenerator {

    fun generate(ingredients: List<BIngredient>): String {
        if (ingredients.isEmpty()) {
            return """
AI 냉털 추천

아직 냉장고에 등록된 재료가 없어요.
재료를 먼저 추가하면 현재 냉장고 상태에 맞춰 냉털 레시피를 추천할 수 있어요.

추천 준비:
- 재료 탭에서 직접 추가
- 바코드 인식으로 추가
- 영수증 OCR로 추가
""".trimIndent()
        }

        val sorted = ingredients
            .sortedWith(
                compareBy<BIngredient> { calculateExpireDay(it.expiryDate) }
                    .thenBy { it.stockPercent }
                    .thenBy { it.name }
            )

        val urgent = sorted.filter {
            calculateExpireDay(it.expiryDate) <= 3 || it.stockPercent <= 30
        }

        val priority = if (urgent.isNotEmpty()) urgent else sorted

        val proteins = sorted.filter { it.category == "단백질" }
        val vegetables = sorted.filter { it.category == "채소" }
        val dairy = sorted.filter { it.category == "유제품" }
        val sauces = sorted.filter { it.category == "조미료/소스" }
        val etc = sorted.filter { it.category == "기타" }

        val recipe1 = buildStirFryRecipe(
            main = proteins.firstOrNull() ?: priority.first(),
            vegetable = vegetables.firstOrNull(),
            sauce = sauces.firstOrNull()
        )

        val recipe2 = buildRiceBowlRecipe(
            main = proteins.getOrNull(1) ?: proteins.firstOrNull() ?: priority.first(),
            vegetable = vegetables.getOrNull(1) ?: vegetables.firstOrNull(),
            sauce = sauces.firstOrNull()
        )

        val recipe3 =
            if (dairy.isNotEmpty()) {
                buildCreamRecipe(
                    dairy = dairy.first(),
                    vegetable = vegetables.firstOrNull(),
                    extra = etc.firstOrNull()
                )
            } else {
                buildSoupRecipe(
                    main = priority.getOrNull(1) ?: priority.first(),
                    vegetable = vegetables.firstOrNull(),
                    sauce = sauces.firstOrNull()
                )
            }

        val fridgeSummary = sorted.joinToString("\n") { ingredient ->
            val dDay = calculateExpireDay(ingredient.expiryDate)
            val expireText =
                if (ingredient.expiryDate.isBlank()) {
                    "유통기한 미입력"
                } else {
                    "D-$dDay"
                }

            "- ${ingredient.name}: ${ingredient.category}, 재고 ${ingredient.stockPercent}%, $expireText"
        }

        return """
AI 냉털 추천 데모

현재 냉장고 상태
$fridgeSummary

$recipe1

$recipe2

$recipe3

추천 기준
- 유통기한이 임박한 재료를 우선 사용했어요.
- 재고가 적은 재료를 먼저 소비하도록 구성했어요.
- 장보기 없이 만들 수 있는 조합을 우선 추천했어요.
""".trimIndent()
    }

    private fun buildStirFryRecipe(
        main: BIngredient,
        vegetable: BIngredient?,
        sauce: BIngredient?
    ): String {
        val used = listOfNotNull(main.name, vegetable?.name, sauce?.name)
        val title =
            if (vegetable != null) {
                "${main.name} ${vegetable.name} 볶음"
            } else {
                "${main.name} 간단 볶음"
            }

        val additional =
            if (sauce == null) "간장, 소금, 후추" else "밥 또는 면"

        return """
1. $title
추천 이유: ${main.name}을 중심으로 냉장고 재료를 빠르게 소비할 수 있는 메뉴예요.
사용 재료: ${used.joinToString(", ")}
있으면 좋은 추가 재료: $additional
조리 순서:
1) 재료를 먹기 좋은 크기로 손질해요.
2) 팬에 기름을 두르고 향이 나는 재료부터 볶아요.
3) ${main.name}을 넣고 충분히 익혀요.
4) 간을 맞추고 한 번 더 볶아 마무리해요.
난이도: 쉬움 / 예상 시간: 10~15분
""".trimIndent()
    }

    private fun buildRiceBowlRecipe(
        main: BIngredient,
        vegetable: BIngredient?,
        sauce: BIngredient?
    ): String {
        val used = listOfNotNull(main.name, vegetable?.name, sauce?.name)
        val title =
            if (vegetable != null) {
                "${main.name} ${vegetable.name} 덮밥"
            } else {
                "${main.name} 냉털 덮밥"
            }

        val additional =
            if (sauce == null) "밥, 간장, 참기름" else "밥, 계란"

        return """
2. $title
추천 이유: 남은 재료를 밥 위에 올려 한 끼로 처리하기 좋아요.
사용 재료: ${used.joinToString(", ")}
있으면 좋은 추가 재료: $additional
조리 순서:
1) ${main.name}과 채소를 작게 썰어요.
2) 팬에 재료를 넣고 볶거나 졸여요.
3) 간장이나 소스로 간을 맞춰요.
4) 따뜻한 밥 위에 올려 완성해요.
난이도: 쉬움 / 예상 시간: 10분
""".trimIndent()
    }

    private fun buildCreamRecipe(
        dairy: BIngredient,
        vegetable: BIngredient?,
        extra: BIngredient?
    ): String {
        val used = listOfNotNull(dairy.name, vegetable?.name, extra?.name)
        val title =
            if (vegetable != null) {
                "${dairy.name} ${vegetable.name} 크림 요리"
            } else {
                "${dairy.name} 크림 냉털 요리"
            }

        return """
3. $title
추천 이유: 유제품을 활용해서 부드럽고 든든한 메뉴로 만들 수 있어요.
사용 재료: ${used.joinToString(", ")}
있으면 좋은 추가 재료: 파스타면, 후추, 치즈
조리 순서:
1) 채소나 부재료를 먼저 볶아요.
2) ${dairy.name}을 넣고 약불에서 끓여요.
3) 소금과 후추로 간을 맞춰요.
4) 면이나 밥과 함께 먹으면 좋아요.
난이도: 보통 / 예상 시간: 15~20분
""".trimIndent()
    }

    private fun buildSoupRecipe(
        main: BIngredient,
        vegetable: BIngredient?,
        sauce: BIngredient?
    ): String {
        val used = listOfNotNull(main.name, vegetable?.name, sauce?.name)
        val title =
            if (vegetable != null) {
                "${main.name} ${vegetable.name} 간단 국물요리"
            } else {
                "${main.name} 냉털 국물요리"
            }

        return """
3. $title
추천 이유: 남은 재료를 한 번에 넣고 끓이면 빠르게 소비할 수 있어요.
사용 재료: ${used.joinToString(", ")}
있으면 좋은 추가 재료: 물, 소금, 후추, 다진 마늘
조리 순서:
1) 냄비에 물을 넣고 끓여요.
2) ${main.name}과 준비한 재료를 넣어요.
3) 중불에서 충분히 익혀요.
4) 간을 맞추고 따뜻하게 먹어요.
난이도: 쉬움 / 예상 시간: 15분
""".trimIndent()
    }

    private fun calculateExpireDay(expiryDate: String): Int {
        if (expiryDate.isBlank()) return 999

        return runCatching {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = formatter.parse(expiryDate) ?: return 999
            val now = Date()
            val diff = targetDate.time - now.time

            TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
        }.getOrDefault(999)
    }
}
