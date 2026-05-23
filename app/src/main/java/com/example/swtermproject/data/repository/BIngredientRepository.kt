package com.example.swtermproject.data.repository

import com.example.swtermproject.data.local.dao.BIngredientDao
import com.example.swtermproject.data.local.entity.BIngredientEntity
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Locale

class BIngredientRepository(
    private val ingredientDao: BIngredientDao
) {
    fun observeIngredients(): Flow<List<BIngredient>> {
        return ingredientDao.observeAllIngredients().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getAllIngredients(): List<BIngredient> {
        return ingredientDao.getAllIngredients().map { it.toDomain() }
    }

    suspend fun addIngredient(ingredient: BIngredient): Long {
        val normalized = ingredient.copy(
            name = ingredient.name.trim()
        )

        val existing = ingredientDao.getIngredientByName(normalized.name)

        if (existing == null) {
            return ingredientDao.insertIngredient(normalized.toEntity())
        }

        val merged = mergeIngredient(
            existing = existing.toDomain(),
            incoming = normalized,
            displayName = existing.name
        )

        ingredientDao.updateIngredient(merged.toEntity())
        return existing.id
    }

    suspend fun addIngredientAsNew(ingredient: BIngredient): Long {
        return ingredientDao.insertIngredient(
            ingredient.copy(name = ingredient.name.trim()).toEntity()
        )
    }

    suspend fun findSimilarIngredientByName(name: String): BIngredient? {
        val incomingNormalized = normalizeName(name)
        val incomingKey = canonicalIngredientKey(name)

        if (incomingKey.isBlank()) return null

        return ingredientDao.getAllIngredients()
            .map { it.toDomain() }
            .firstOrNull { existing ->
                val existingNormalized = normalizeName(existing.name)
                val existingKey = canonicalIngredientKey(existing.name)

                existingNormalized != incomingNormalized &&
                    existingKey == incomingKey
            }
    }

    suspend fun mergeIngredientWithExisting(
        existingId: Long,
        incoming: BIngredient
    ) {
        val existing = ingredientDao.getIngredientById(existingId)?.toDomain()
            ?: return

        val merged = mergeIngredient(
            existing = existing,
            incoming = incoming.copy(name = incoming.name.trim()),
            displayName = canonicalIngredientKey(incoming.name)
                .ifBlank { existing.name }
        )

        ingredientDao.updateIngredient(merged.toEntity())
    }

    suspend fun updateIngredient(ingredient: BIngredient) {
        ingredientDao.updateIngredient(ingredient.toEntity())
    }

    suspend fun updateCurrentAmount(id: Long, currentAmount: Double) {
        ingredientDao.updateCurrentAmount(id, currentAmount)
    }

    suspend fun deleteIngredient(id: Long) {
        ingredientDao.deleteIngredientById(id)
    }

    suspend fun getLowStockIngredients(): List<BIngredient> {
        return ingredientDao.getLowStockIngredients().map { it.toDomain() }
    }

    private fun mergeIngredient(
        existing: BIngredient,
        incoming: BIngredient,
        displayName: String
    ): BIngredient {
        val mergedInitialAmount =
            existing.initialAmount + incoming.initialAmount

        val mergedCurrentAmount =
            existing.currentAmount + incoming.currentAmount

        return existing.copy(
            name = displayName.ifBlank { existing.name },
            category = chooseCategory(existing.category, incoming.category),
            initialAmount = mergedInitialAmount,
            currentAmount = mergedCurrentAmount.coerceAtMost(mergedInitialAmount),
            unit = chooseUnit(existing.unit, incoming.unit),
            expiryDate = chooseEarlierDate(
                existing.expiryDate,
                incoming.expiryDate
            ),
            storageType = incoming.storageType.ifBlank { existing.storageType },
            favorite = existing.favorite || incoming.favorite,
            createdAt = existing.createdAt
        )
    }

    private fun chooseCategory(
        existingCategory: String,
        incomingCategory: String
    ): String {
        if (incomingCategory.isBlank()) return existingCategory
        if (existingCategory == "기타" && incomingCategory != "기타") return incomingCategory
        return existingCategory
    }

    private fun chooseUnit(
        existingUnit: String,
        incomingUnit: String
    ): String {
        if (existingUnit.isBlank()) return incomingUnit
        return existingUnit
    }

    private fun chooseEarlierDate(
        first: String,
        second: String
    ): String {
        if (first.isBlank()) return second
        if (second.isBlank()) return first

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return runCatching {
            val firstDate = formatter.parse(first)
            val secondDate = formatter.parse(second)

            when {
                firstDate == null -> second
                secondDate == null -> first
                firstDate.time <= secondDate.time -> first
                else -> second
            }
        }.getOrDefault(first)
    }

    private fun canonicalIngredientKey(name: String): String {
        val normalized = normalizeName(name)

        val keywordGroups = listOf(
            listOf("달걀", "계란"),
            listOf("서울우유", "매일우유", "남양우유", "우유"),
            listOf("체다치즈", "모짜렐라치즈", "치즈"),
            listOf("요구르트", "요거트"),
            listOf("양파"),
            listOf("대파", "쪽파", "실파"),
            listOf("다진마늘", "마늘"),
            listOf("상추"),
            listOf("양배추"),
            listOf("방울토마토", "토마토"),
            listOf("오이"),
            listOf("당근"),
            listOf("감자"),
            listOf("고구마"),
            listOf("두부"),
            listOf("닭가슴살", "닭고기"),
            listOf("소고기", "쇠고기"),
            listOf("돼지고기"),
            listOf("참치"),
            listOf("스팸", "햄"),
            listOf("김치"),
            listOf("즉석밥", "햇반", "밥"),
            listOf("스파게티면", "파스타면"),
            listOf("식빵"),
            listOf("간장"),
            listOf("고추장"),
            listOf("된장"),
            listOf("케찹", "케첩"),
            listOf("마요네즈"),
            listOf("드레싱"),
            listOf("참기름"),
            listOf("올리브유", "식용유"),
            listOf("소금"),
            listOf("후추"),
            listOf("설탕"),
            listOf("고춧가루")
        )

        keywordGroups.forEach { group ->
            val matched = group.firstOrNull { keyword ->
                normalized.contains(normalizeName(keyword))
            }

            if (matched != null) {
                return normalizeDisplayKey(group.last())
            }
        }

        return normalized
    }

    private fun normalizeDisplayKey(value: String): String {
        return when (value) {
            "달걀" -> "계란"
            "요구르트" -> "요거트"
            "케찹" -> "케첩"
            "쇠고기" -> "소고기"
            else -> value
        }
    }

    private fun normalizeName(name: String): String {
        val normalized = Normalizer.normalize(
            name.trim().lowercase(Locale.getDefault()),
            Normalizer.Form.NFKC
        )

        return normalized
            .replace(Regex("\\([^)]*\\)"), "")
            .replace(Regex("\\[[^]]*]"), "")
            .replace(Regex("[0-9]+(\\.[0-9]+)?\\s*(g|kg|ml|l|개|봉|팩|입|매|단|병|캔)"), "")
            .replace(Regex("[^가-힣a-zA-Z0-9]"), "")
            .replace("국산", "")
            .replace("국내산", "")
            .replace("수입산", "")
            .replace("냉장", "")
            .replace("냉동", "")
            .replace("대용량", "")
            .replace("친환경", "")
            .replace("무농약", "")
            .replace("유기농", "")
            .replace("프리미엄", "")
            .trim()
    }
}

fun BIngredientEntity.toDomain(): BIngredient {
    return BIngredient(
        id = id,
        name = name,
        category = category,
        initialAmount = initialAmount,
        currentAmount = currentAmount,
        unit = unit,
        expiryDate = expiryDate,
        storageType = storageType,
        favorite = favorite,
        createdAt = createdAt
    )
}

fun BIngredient.toEntity(): BIngredientEntity {
    return BIngredientEntity(
        id = id,
        name = name,
        category = category,
        initialAmount = initialAmount,
        currentAmount = currentAmount,
        unit = unit,
        expiryDate = expiryDate,
        storageType = storageType,
        favorite = favorite,
        createdAt = createdAt
    )
}
