package com.example.swtermproject.ocr

data class BReceiptItemCandidate(
    val name: String,
    val amountText: String? = null
)

object BReceiptItemExtractor {
    private val ignoredKeywords = listOf(
        "합계", "카드", "승인", "영수증", "부가세", "과세", "면세", "거스름돈",
        "전화", "사업자", "대표", "주소", "일시", "금액", "수량"
    )

    fun extractCandidates(lines: List<String>): List<BReceiptItemCandidate> {
        return lines
            .filterNot { line -> ignoredKeywords.any { line.contains(it) } }
            .mapNotNull { line -> extractCandidate(line) }
            .distinctBy { it.name }
    }

    private fun extractCandidate(line: String): BReceiptItemCandidate? {
        val removedPrice = line.replace(Regex("[0-9,]+\\s*원?"), " ")
        val cleaned = removedPrice
            .replace(Regex("[^가-힣a-zA-Z0-9\\s]"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")

        if (cleaned.length < 2) return null

        val amount = Regex("(\\d+\\s?(g|kg|ml|l|개|구|봉))", RegexOption.IGNORE_CASE)
            .find(line)
            ?.value

        val name = cleaned
            .split(" ")
            .filter { it.length >= 2 }
            .joinToString(" ")
            .trim()

        if (name.isBlank()) return null

        return BReceiptItemCandidate(
            name = name,
            amountText = amount
        )
    }
}
