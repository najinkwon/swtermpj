package com.example.swtermproject.data.remote

import com.example.swtermproject.common.BConstants
import com.example.swtermproject.data.remote.shopping.BShoppingApi
import com.example.swtermproject.data.remote.youtube.BYoutubeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BRetrofitClient {
    val shoppingApi: BShoppingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BConstants.NAVER_SHOPPING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BShoppingApi::class.java)
    }

    val youtubeApi: BYoutubeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BConstants.YOUTUBE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BYoutubeApi::class.java)
    }
}
