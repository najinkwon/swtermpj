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
        val normalized = normalizeIngredientFast(ingredient)
        val existing = ingredientDao.getIngredientByName(normalized.name)

        if (existing == null) {
            val similar = findSimilarIngredientByName(normalized.name)

            if (similar != null) {
                mergeIngredientWithExisting(
                    existingId = similar.id,
                    incoming = normalized
                )
                return similar.id
            }

            return ingredientDao.insertIngredient(normalized.toEntity())
        }

        val merged = mergeIngredient(
            existing = existing.toDomain(),
            incoming = normalized,
            displayName = normalized.name.ifBlank { existing.name }
        )

        ingredientDao.updateIngredient(merged.toEntity())
        return existing.id
    }

    suspend fun addIngredientAsNew(ingredient: BIngredient): Long {
        val normalized = normalizeIngredientFast(ingredient)
        return ingredientDao.insertIngredient(normalized.toEntity())
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

        val normalizedIncoming = normalizeIngredientFast(incoming)

        val merged = mergeIngredient(
            existing = existing,
            incoming = normalizedIncoming,
            displayName = normalizedIncoming.name.ifBlank {
                canonicalIngredientKey(incoming.name).ifBlank { existing.name }
            }
        )

        ingredientDao.updateIngredient(merged.toEntity())
    }

    suspend fun updateIngredient(ingredient: BIngredient) {
        val normalized = normalizeIngredientFast(ingredient)
        ingredientDao.updateIngredient(normalized.toEntity())
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

    private fun normalizeIngredientFast(ingredient: BIngredient): BIngredient {
        val cleanName = normalizeDisplayName(ingredient.name)
        val finalCategory =
            if (ingredient.category.isBlank() || ingredient.category == "기타") {
                categoryForName(cleanName)
            } else {
                ingredient.category
            }

        return ingredient.copy(
            name = cleanName,
            category = finalCategory
        )
    }

    private fun mergeIngredient(
        existing: BIngredient,
        incoming: BIngredient,
        displayName: String
    ): BIngredient {
        val sameUnit = normalizeUnit(existing.unit) == normalizeUnit(incoming.unit)

        val mergedInitialAmount =
            if (sameUnit) {
                existing.initialAmount + incoming.initialAmount
            } else {
                existing.initialAmount
            }

        val mergedCurrentAmount =
            if (sameUnit) {
                existing.currentAmount + incoming.currentAmount
            } else {
                existing.currentAmount
            }

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
        if (existingCategory.isBlank()) return incomingCategory
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
            listOf("체다치즈", "모짜렐라치즈", "모차렐라치즈", "치즈"),
            listOf("요구르트", "요거트"),
            listOf("양파", "적양파"),
            listOf("대파", "쪽파", "실파", "파"),
            listOf("다진마늘", "깐마늘", "통마늘", "마늘"),
            listOf("상추", "양상추", "로메인"),
            listOf("배추", "알배추"),
            listOf("양배추", "적양배추"),
            listOf("방울토마토", "토마토"),
            listOf("브로커리", "브로컬리", "브로코리", "브로콜리"),
            listOf("파프리카", "피망"),
            listOf("오이"),
            listOf("당근"),
            listOf("감자"),
            listOf("고구마"),
            listOf("두부", "부침두부", "찌개두부"),
            listOf("순두부", "연두부"),
            listOf("닭가슴살", "닭고기", "닭"),
            listOf("소고기", "쇠고기", "한우"),
            listOf("돼지고기", "삼겹살", "목살"),
            listOf("참치", "참치캔"),
            listOf("스팸", "햄"),
            listOf("새우", "칵테일새우", "대하"),
            listOf("오징어"),
            listOf("고등어"),
            listOf("연어"),
            listOf("멸치", "국물멸치", "볶음멸치", "다시멸치"),
            listOf("김치", "배추김치"),
            listOf("깍두기"),
            listOf("즉석밥", "햇반", "밥"),
            listOf("스파게티면", "파스타면", "파스타"),
            listOf("식빵", "빵"),
            listOf("라면"),
            listOf("우동", "우동면"),
            listOf("간장", "진간장", "양조간장", "국간장"),
            listOf("고추장"),
            listOf("된장"),
            listOf("쌈장"),
            listOf("케찹", "케첩"),
            listOf("마요네즈", "마요"),
            listOf("드레싱"),
            listOf("참기름"),
            listOf("들기름"),
            listOf("올리브유", "올리브오일", "식용유", "카놀라유", "포도씨유"),
            listOf("소금", "맛소금"),
            listOf("후추", "통후추"),
            listOf("설탕"),
            listOf("고춧가루"),
            listOf("땅콩", "볶음땅콩"),
            listOf("아몬드"),
            listOf("호두")
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

    private fun normalizeDisplayName(name: String): String {
        val key = canonicalIngredientKey(name)

        return normalizeDisplayKey(key)
            .ifBlank { name.trim() }
    }

    private fun normalizeDisplayKey(value: String): String {
        return when (value) {
            "달걀" -> "계란"
            "요구르트" -> "요거트"
            "케찹" -> "케첩"
            "쇠고기" -> "소고기"
            "브로커리" -> "브로콜리"
            "브로컬리" -> "브로콜리"
            "브로코리" -> "브로콜리"
            "모짜렐라치즈" -> "모차렐라치즈"
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
            .replace("수입", "")
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

    private fun categoryForName(name: String): String {
        val compact = name.replace(" ", "")

        return when {
            listOf("우유", "치즈", "요거트", "요구르트").any {
                compact.contains(it)
            } -> "유제품"

            listOf(
                "계란", "달걀", "고기", "닭", "돼지", "소고기", "쇠고기",
                "참치", "두부", "가슴살", "햄", "스팸", "새우", "연어"
            ).any { compact.contains(it) } -> "단백질"

            listOf(
                "양파", "대파", "파", "마늘", "상추", "양상추", "양배추",
                "적양배추", "브로콜리", "파프리카", "오이", "당근", "감자",
                "토마토", "채소", "야채"
            ).any { compact.contains(it) } -> "채소"

            listOf(
                "간장", "고추장", "된장", "쌈장", "소스", "드레싱",
                "소금", "후추", "설탕", "식용유", "참기름", "올리브유"
            ).any { compact.contains(it) } -> "조미료/소스"

            else -> "기타"
        }
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