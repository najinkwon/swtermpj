package com.example.swtermproject.ml

object BTextPreprocessor {
    fun clean(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace(Regex("[^가-힣a-zA-Z0-9]"), "")
    }

    fun extractTokens(text: String): List<String> {
        val cleanText = clean(text)
        if (cleanText.isBlank()) return emptyList()

        val chars = cleanText.map { it.toString() }
        val bigrams = cleanText.windowed(size = 2, step = 1, partialWindows = false)
        return (chars + bigrams + cleanText).distinct()
    }
}
