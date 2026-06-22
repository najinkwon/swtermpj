package com.example.swtermproject.data.remote.shopping

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BShoppingApi {
    @GET("v1/search/shop.json")
    suspend fun searchShoppingItems(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10
    ): BShoppingResponse
}
