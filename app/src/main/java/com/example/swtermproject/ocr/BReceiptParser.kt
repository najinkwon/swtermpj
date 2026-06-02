package com.example.swtermproject.ocr

object BReceiptParser {
    fun parseLines(rawText: String): List<String> {
        return rawText
            .lines()
            .map { it.trim() }
            .map { normalizeSpaces(it) }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun normalizeSpaces(value: String): String {
        return value
            .replace("\t", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
