package com.example.swtermproject.data.remote.youtube

data class BYoutubeDto(
    val id: BYoutubeVideoId? = null,
    val snippet: BYoutubeSnippet? = null
)

data class BYoutubeVideoId(
    val videoId: String? = null
)

data class BYoutubeSnippet(
    val title: String? = null,
    val description: String? = null,
    val thumbnails: BYoutubeThumbnails? = null
)

data class BYoutubeThumbnails(
    val default: BYoutubeThumbnail? = null,
    val medium: BYoutubeThumbnail? = null,
    val high: BYoutubeThumbnail? = null
)

data class BYoutubeThumbnail(
    val url: String? = null
)
