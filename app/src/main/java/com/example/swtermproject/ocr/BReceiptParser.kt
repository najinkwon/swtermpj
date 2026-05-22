package com.example.swtermproject.ocr

object BReceiptParser {
    fun parseLines(rawText: String): List<String> {
        return rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
