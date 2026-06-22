package com.example.swtermproject.data.repository

import com.example.swtermproject.BuildConfig
import com.example.swtermproject.data.remote.barcode.BBarcodeProduct
import com.example.swtermproject.data.remote.barcode.BFoodsafetyBarcodeApi

class BBarcodeRepository(
    private val api: BFoodsafetyBarcodeApi
) {
    suspend fun searchProduct(barcode: String): BBarcodeProduct? {
        val apiKey = BuildConfig.FOODSAFETY_API_KEY

        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "식품안전나라 API 키가 없습니다. local.properties에 FOODSAFETY_API_KEY를 추가하세요."
            )
        }

        val response = api.searchBarcode(
            apiKey = apiKey,
            barcode = barcode
        )

        val row = response.body?.rows?.firstOrNull()
            ?: return null

        val productName = row.productName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val companyName = row.companyName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "제조사 정보 없음"

        val rawFoodType = row.foodTypeName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "기타"

        return BBarcodeProduct(
            barcode = row.barcode ?: barcode,
            name = productName,
            company = companyName,
            category = mapFoodTypeToIngredientCategory(rawFoodType, productName),
            rawFoodType = rawFoodType
        )
    }

    private fun mapFoodTypeToIngredientCategory(
        foodType: String,
        productName: String
    ): String {
        val source = "$foodType $productName"

        return when {
            source.contains("우유") ||
                source.contains("치즈") ||
                source.contains("요구르트") ||
                source.contains("요거트") ||
                source.contains("유제품") -> "유제품"

            source.contains("계란") ||
                source.contains("달걀") ||
                source.contains("육류") ||
                source.contains("닭") ||
                source.contains("돼지") ||
                source.contains("소고기") ||
                source.contains("참치") ||
                source.contains("어육") -> "단백질"

            source.contains("채소") ||
                source.contains("야채") ||
                source.contains("양파") ||
                source.contains("대파") ||
                source.contains("마늘") ||
                source.contains("상추") -> "채소"

            source.contains("소스") ||
                source.contains("간장") ||
                source.contains("고추장") ||
                source.contains("된장") ||
                source.contains("조미") ||
                source.contains("드레싱") -> "조미료/소스"

            else -> "기타"
        }
    }
}
