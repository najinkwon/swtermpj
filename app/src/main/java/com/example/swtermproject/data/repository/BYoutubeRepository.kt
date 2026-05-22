package com.example.swtermproject.data.repository

import android.net.Uri
import com.example.swtermproject.common.BConstants
import com.example.swtermproject.data.remote.youtube.BYoutubeApi

class BYoutubeRepository(
    private val youtubeApi: BYoutubeApi? = null
) {
    suspend fun getYoutubeUrl(keyword: String): String {
        val api = youtubeApi

        if (api == null || BConstants.YOUTUBE_API_KEY.isBlank()) {
            return createYoutubeSearchUrl(keyword)
        }

        return runCatching {
            val response = api.searchVideos(
                query = keyword,
                apiKey = BConstants.YOUTUBE_API_KEY
            )
            val videoId = response.items.firstOrNull()?.id?.videoId
            if (videoId.isNullOrBlank()) {
                createYoutubeSearchUrl(keyword)
            } else {
                "https://www.youtube.com/watch?v=$videoId"
            }
        }.getOrElse {
            createYoutubeSearchUrl(keyword)
        }
    }

    private fun createYoutubeSearchUrl(keyword: String): String {
        return "https://www.youtube.com/results?search_query=${Uri.encode(keyword)}"
    }
}
