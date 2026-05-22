package com.example.swtermproject.data.remote.youtube

import retrofit2.http.GET
import retrofit2.http.Query

interface BYoutubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 1,
        @Query("key") apiKey: String
    ): BYoutubeResponse
}
