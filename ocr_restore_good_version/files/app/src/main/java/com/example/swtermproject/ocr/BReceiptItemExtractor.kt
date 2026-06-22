package com.example.swtermproject.ocr

object BReceiptItemExtractor {

    private val ignoredKeywords = listOf(
        "합계", "카드", "승인", "영수증", "부가세", "과세", "면세", "거스름돈",
        "전화", "사업자", "대표", "주소", "일시", "금액", "수량", "단가", "상품코드",
        "포인트", "회원", "현금", "결제", "취소", "교환", "환불", "매장", "점포",
        "고객", "발행", "계산", "공급가액", "받기", "구매", "부가", "잔액",
        "메트로", "장안점", "남양주점", "청암점", "창동점", "강남점",
        "emart", "e-mart", "이마트", "http", "https", "www", ".com", ".co.kr", "url",
        "정부", "방침", "7월1일", "취소시", "소비자", "분실", "보관", "바랍니다",
        "현금결제", "현금 결제", "영수증이 없으면", "교환/환불",
        "pos", "cashier", "tel", "fax", "no.", "카드번호", "신세계포인트"
    )

    private val removeMarketingWords = listOf(
        "국산", "국내산", "수입산", "수입", "냉장", "냉동", "신선", "친환경",
        "유기농", "무농약", "손질", "세척", "깐", "햇", "특", "대용량", "소포장",
        "행사", "특가", "할인", "프리미엄", "소단량", "한팩", "한봉",
        "이마트", "노브랜드", "피코크", "초이스"
    )

    private val receiptEndKeywords = listOf(
        "부가세", "과세", "면세", "합계", "받기", "카드", "포인트", "회원"
    )

    fun extractCandidates(lines: List<String>): List<BReceiptItemCandidate> {
        val productLines = cropProductArea(lines)

        return productLines
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { isClearlyNotFoodLine(it) }
            .mapNotNull { line -> extractCandidate(line) }
            .distinctBy { BIngredientDictionary.normalizeForMatch(it.name) }
            .toList()
    }

    private fun cropProductArea(lines: List<String>): List<String> {
        val cleanedLines = lines
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val headerIndex = cleanedLines.indexOfFirst { line ->
            val compact = compact(line)
            compact.contains("상품코드") ||
                    compact.contains("단가수량금액") ||
                    compact.contains("상품명")
        }

        val targetLines =
            if (headerIndex >= 0 && headerIndex + 1 < cleanedLines.size) {
                cleanedLines.drop(headerIndex + 1)
            } else {
                cleanedLines
            }

        val result = mutableListOf<String>()
        var foundAtLeastOneFood = false

        for (line in targetLines) {
            val compactLine = compact(line)

            if (foundAtLeastOneFood && receiptEndKeywords.any { compactLine.contains(compact(it)) }) {
                break
            }

            val cleaned = cleanReceiptLine(line)
            val entry = BIngredientDictionary.findBest(cleaned)

            if (entry != null) {
                foundAtLeastOneFood = true
                result.add(line)
            }
        }

        return result
    }

    private fun extractCandidate(line: String): BReceiptItemCandidate? {
        val normalizedAmount = BAmountNormalizer.extractFromText(line)

        val cleaned = cleanReceiptLine(line)
        if (cleaned.length < 2) return null

        val entry = BIngredientDictionary.findBest(cleaned) ?: return null

        val unit =
            normalizedAmount?.unit
                ?: BAmountNormalizer.defaultUnitForIngredient(entry.canonical)

        val amount =
            normalizedAmount?.amount
                ?: BAmountNormalizer.defaultAmountForUnit(unit)

        return BReceiptItemCandidate(
            name = entry.canonical,
            amountText = normalizedAmount?.amountText,
            amount = amount,
            unit = unit,
            amountGram = if (unit == "g") amount else null,
            category = entry.category,
            amountSource = normalizedAmount?.source ?: "default"
        )
    }

    private fun cleanReceiptLine(line: String): String {
        var text = line

        text = text.replace(Regex("[0-9]{8,}"), " ")
        text = text.replace(Regex("[0-9,]+\\s*원"), " ")
        text = text.replace(Regex("\\b[0-9,]{3,}\\b"), " ")
        text = text.replace(Regex("\\([^)]*\\)"), " ")
        text = text.replace(Regex("\\[[^]]*]"), " ")
        text = text.replace(Regex("^[A-Za-z]{0,5}\\d{1,6}\\s*"), " ")
        text = text.replace(Regex("\\d+\\s*/\\s*\\d*"), " ")
        text = text.replace(
            Regex("\\d+(\\.\\d+)?\\s*(g|kg|ml|l|L|개|입|봉|팩|묶음|통|병|캔|ea|EA|단|매|구|알|근)"),
            " "
        )
        text = text.replace(Regex("[^가-힣a-zA-Z0-9\\s]"), " ")

        removeMarketingWords.forEach { word ->
            text = text.replace(word, " ")
        }

        return text
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isClearlyNotFoodLine(line: String): Boolean {
        val original = line.trim()
        val lower = original.lowercase()
        val compactLine = compact(original)

        if (original.isBlank()) return true

        if (ignoredKeywords.any { keyword ->
                lower.contains(keyword.lowercase()) ||
                        compactLine.contains(compact(keyword))
            }
        ) {
            return true
        }

        if (Regex("(http|https|www|\\.com|\\.co\\.kr)", RegexOption.IGNORE_CASE).containsMatchIn(original)) {
            return true
        }

        if (Regex("^[가-힣\\s]{2,15}점$").containsMatchIn(original)) {
            return true
        }

        if (Regex("\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}").containsMatchIn(original)) {
            return true
        }

        if (Regex("\\d{1,2}:\\d{2}").containsMatchIn(original)) {
            return true
        }

        val digitCount = original.count { it.isDigit() }
        if (original.isNotBlank() && digitCount >= original.length * 0.55) {
            return true
        }

        if (Regex("^[0-9,\\s]+$").matches(original)) {
            return true
        }

        return false
    }

    private fun compact(text: String): String {
        return text
            .lowercase()
            .replace(Regex("\\s+"), "")
            .replace(Regex("[^가-힣a-zA-Z0-9.]"), "")
    }
}