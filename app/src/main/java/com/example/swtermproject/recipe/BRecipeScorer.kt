package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.domain.model.BRecipeIngredientRequirement
import com.example.swtermproject.domain.model.BRecipeIngredientStatus
import java.text.Normalizer
import java.util.Locale

object BRecipeScorer {

    fun scoreRecipe(recipe: BRecipe, ingredients: List<BIngredient>): BRecipe {
        val requirements = getRequirements(recipe)

        val normalizedIngredients = ingredients.map { ingredient ->
            NormalizedIngredient(
                id = ingredient.id,
                name = ingredient.name,
                key = canonicalKey(ingredient.name),
                amount = ingredient.currentAmount,
                unit = normalizeUnit(ingredient.unit)
            )
        }

        val statuses = requirements.map { requirement ->
            buildStatus(
                requirement = requirement,
                ingredients = normalizedIngredients
            )
        }

        val mainMatchCount = statuses.count {
            it.requirement.group == "main" && it.isEnough
        }

        val subMatchCount = statuses.count {
            it.requirement.group == "sub" && it.isEnough
        }

        val seasoningMatchCount = statuses.count {
            it.requirement.group == "seasoning" && it.isEnough
        }

        val missingEssentialMainCount = statuses.count {
            it.requirement.group == "main" &&
                    it.requirement.essential &&
                    !it.isEnough
        }

        val missingEssentialSubCount = statuses.count {
            it.requirement.group == "sub" &&
                    it.requirement.essential &&
                    !it.isEnough
        }

        val score =
            mainMatchCount * 50 +
                    subMatchCount * 15 +
                    seasoningMatchCount * 5 -
                    missingEssentialMainCount * 40 -
                    missingEssentialSubCount * 10

        val missingIngredients = statuses
            .filter { it.requirement.essential && !it.isEnough }
            .map { it.requirement.name }
            .distinct()

        return recipe.copy(
            score = score.coerceAtLeast(0),
            missingIngredients = missingIngredients,
            ingredientStatuses = statuses
        )
    }

    fun getRequirements(recipe: BRecipe): List<BRecipeIngredientRequirement> {
        if (recipe.requiredIngredients.isNotEmpty()) {
            return recipe.requiredIngredients
        }

        val main = recipe.mainIngredients.map {
            BRecipeIngredientRequirement(
                name = it,
                amount = 1.0,
                unit = defaultUnitForName(it),
                group = "main",
                essential = true
            )
        }

        val sub = recipe.subIngredients.map {
            BRecipeIngredientRequirement(
                name = it,
                amount = 1.0,
                unit = defaultUnitForName(it),
                group = "sub",
                essential = true
            )
        }

        val seasonings = recipe.seasonings.map {
            BRecipeIngredientRequirement(
                name = it,
                amount = 1.0,
                unit = defaultUnitForName(it),
                group = "seasoning",
                essential = false
            )
        }

        return main + sub + seasonings
    }

    private fun buildStatus(
        requirement: BRecipeIngredientRequirement,
        ingredients: List<NormalizedIngredient>
    ): BRecipeIngredientStatus {
        val requiredKey = canonicalKey(requirement.name)
        val requiredUnit = normalizeUnit(requirement.unit)

        val owned = ingredients.firstOrNull { ingredient ->
            ingredient.key == requiredKey ||
                    ingredient.key.contains(requiredKey) ||
                    requiredKey.contains(ingredient.key)
        }

        if (owned == null) {
            return BRecipeIngredientStatus(
                requirement = requirement,
                isOwned = false,
                isUnitCompatible = false,
                isEnough = false,
                missingAmount = requirement.amount
            )
        }

        val isUnitCompatible = owned.unit == requiredUnit
        val ownedAmount = owned.amount

        val isEnough =
            isUnitCompatible &&
                    ownedAmount >= requirement.amount

        val missingAmount =
            if (isEnough) {
                0.0
            } else {
                (requirement.amount - ownedAmount).coerceAtLeast(0.0)
            }

        return BRecipeIngredientStatus(
            requirement = requirement,
            ownedIngredientId = owned.id,
            ownedName = owned.name,
            ownedAmount = ownedAmount,
            ownedUnit = owned.unit,
            isOwned = true,
            isUnitCompatible = isUnitCompatible,
            isEnough = isEnough,
            missingAmount = missingAmount
        )
    }

    private fun canonicalKey(name: String): String {
        val normalized = normalizeSimple(name)

        val aliasGroups = listOf(
            listOf("달걀", "계란"),
            listOf("브로커리", "브로컬리", "브로코리", "브로콜리"),
            listOf("파프리카", "피망"),
            listOf("케찹", "케첩"),
            listOf("쇠고기", "소고기"),
            listOf("소고기", "한우", "불고기", "차돌박이", "양지", "사태"),
            listOf("돼지고기", "삼겹살", "목살", "앞다리살", "뒷다리살"),
            listOf("닭가슴살", "닭고기", "닭"),
            listOf("두부", "부침두부", "찌개두부", "순두부"),
            listOf("파스타면", "스파게티면", "파스타"),
            listOf("밥", "햇반", "즉석밥"),
            listOf("식빵", "빵"),
            listOf("양배추", "적양배추"),
            listOf("방울토마토", "토마토"),
            listOf("서울우유", "매일우유", "남양우유", "우유"),
            listOf("마요네즈", "마요"),
            listOf("식용유", "카놀라유", "포도씨유"),
            listOf("올리브유", "올리브오일"),
            listOf("참기름", "들기름"),
            listOf("대파", "파", "쪽파")
        )

        aliasGroups.forEach { group ->
            val canonical = normalizeSimple(group.last())

            if (group.any { alias ->
                    val aliasKey = normalizeSimple(alias)
                    normalized == aliasKey ||
                            normalized.contains(aliasKey) ||
                            aliasKey.contains(normalized)
                }
            ) {
                return canonical
            }
        }

        return normalized
    }

    private fun normalizeSimple(value: String): String {
        val normalized = Normalizer.normalize(
            value.trim().lowercase(Locale.getDefault()),
            Normalizer.Form.NFKC
        )

        return normalized
            .replace(Regex("\\([^)]*\\)"), "")
            .replace(Regex("\\[[^]]*]"), "")
            .replace(Regex("[0-9]{8,}"), "")
            .replace(Regex("\\d+\\s*/\\s*\\d+"), "")
            .replace(
                Regex("[0-9]+(\\.[0-9]+)?\\s*(g|kg|ml|l|개|봉|팩|입|매|단|병|캔|통|묶음|ea|구|알|근)"),
                ""
            )
            .replace(Regex("[^가-힣a-zA-Z0-9]"), "")
            .replace("국산", "")
            .replace("국내산", "")
            .replace("수입산", "")
            .replace("냉장", "")
            .replace("냉동", "")
            .replace("신선", "")
            .replace("손질", "")
            .replace("세척", "")
            .replace("깐", "")
            .replace("대용량", "")
            .replace("소포장", "")
            .replace("친환경", "")
            .replace("무농약", "")
            .replace("유기농", "")
            .replace("프리미엄", "")
            .replace("행사", "")
            .replace("특가", "")
            .replace("할인", "")
            .trim()
    }

    private fun normalizeUnit(unit: String): String {
        return when (unit.trim()) {
            "입", "알", "구", "마리" -> "개"
            "kg", "킬로", "키로" -> "g"
            "L", "l", "리터" -> "ml"
            "밀리리터", "미리" -> "ml"
            else -> unit.trim()
        }
    }

    private fun defaultUnitForName(name: String): String {
        val compact = name.replace(" ", "")

        return when {
            listOf(
                "계란", "달걀", "브로콜리", "파프리카", "양상추", "상추",
                "오이", "토마토", "감자", "식빵", "두부", "치즈"
            ).any { compact.contains(it) } -> "개"

            listOf(
                "우유", "간장", "참기름", "식용유", "올리브유", "드레싱"
            ).any { compact.contains(it) } -> "ml"

            else -> "g"
        }
    }

    private data class NormalizedIngredient(
        val id: Long,
        val name: String,
        val key: String,
        val amount: Double,
        val unit: String
    )
}