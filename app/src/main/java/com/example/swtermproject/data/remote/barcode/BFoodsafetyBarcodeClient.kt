package com.example.swtermproject.data.remote.barcode

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BFoodsafetyBarcodeClient {
    val api: BFoodsafetyBarcodeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openapi.foodsafetykorea.go.kr/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BFoodsafetyBarcodeApi::class.java)
    }
}
