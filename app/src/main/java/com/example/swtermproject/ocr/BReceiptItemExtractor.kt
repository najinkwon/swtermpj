package com.example.swtermproject.ocr

object BReceiptItemExtractor {

    private val ignoredKeywords = listOf(
        "합계", "총액", "총 합계", "결제", "결제금액", "받을금액", "받은금액",
        "카드", "신용", "체크", "승인", "승인번호", "카드번호", "일시불",
        "영수증", "전자영수증", "현금영수증", "교환", "환불",
        "부가세", "부가가치세", "과세", "면세", "공급가", "세액",
        "거스름돈", "거래", "거래일시", "판매", "판매자",
        "전화", "tel", "사업자", "사업자번호", "대표", "주소", "점포", "매장",
        "일시", "날짜", "시간", "금액", "수량", "단가", "품명",
        "포인트", "적립", "쿠폰", "할인", "행사", "멤버십",
        "봉투", "배달", "주문", "테이블", "고객", "문의",
        "subtotal", "total", "amount", "card", "cash", "change", "tax"
    )

    private val foodKeywords = listOf(
        "우유", "치즈", "요거트", "요구르트", "버터", "크림",
        "계란", "달걀", "두부", "고기", "닭", "닭가슴살", "소고기", "쇠고기", "돼지고기", "참치", "햄", "스팸",
        "양파", "대파", "쪽파", "파", "마늘", "다진마늘", "상추", "양배추", "토마토", "오이", "당근", "감자", "고구마", "버섯", "채소", "야채",
        "김치", "밥", "햇반", "즉석밥", "면", "라면", "파스타", "스파게티", "식빵", "빵",
        "간장", "고추장", "된장", "소스", "케첩", "케찹", "마요네즈", "드레싱", "참기름", "식용유", "올리브유", "소금", "후추", "설탕", "고춧가루"
    )

    private val unitRegex =
        Regex("(\\d+(\\.\\d+)?\\s?(g|kg|ml|l|L|개|봉|팩|입|단|병|캔))")

    private val priceRegex =
        Regex("(^|\\s)[0-9]{1,3}(,[0-9]{3})+(\\s*원?)?($|\\s)|(^|\\s)[0-9]{3,7}\\s*원?($|\\s)")

    fun extractCandidates(lines: List<String>): List<BReceiptItemCandidate> {
        return lines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { isDefinitelyNotItemLine(it) }
            .mapNotNull { extractCandidate(it) }
            .filter { isUsableCandidate(it.name) }
            .distinctBy { normalizeForDistinct(it.name) }
            .take(20)
    }

    private fun extractCandidate(line: String): BReceiptItemCandidate? {
        val amountText = unitRegex.find(line)?.value

        val withoutPrice = removePriceAndReceiptNoise(line)
        val cleaned = cleanNameText(withoutPrice)

        if (cleaned.isBlank()) return null

        val candidateName = chooseCandidateName(cleaned)

        if (candidateName.isBlank()) return null

        return BReceiptItemCandidate(
            name = candidateName,
            amountText = amountText
        )
    }

    private fun isDefinitelyNotItemLine(line: String): Boolean {
        val lower = line.lowercase()
        val compact = line.replace(" ", "")

        if (ignoredKeywords.any { lower.contains(it.lowercase()) }) {
            return true
        }

        if (compact.length <= 1) {
            return true
        }

        if (compact.length > 28 && foodKeywords.none { compact.contains(it) }) {
            return true
        }

        val digitCount = compact.count { it.isDigit() }
        val letterCount = compact.count { it.isLetter() }

        if (digitCount >= 6 && letterCount <= 2) {
            return true
        }

        if (Regex("^[-=*_]+$").matches(compact)) {
            return true
        }

        if (Regex("^[0-9,./:-]+$").matches(compact)) {
            return true
        }

        if (Regex("^\\d{2,4}[./-]\\d{1,2}[./-]\\d{1,2}").containsMatchIn(compact)) {
            return true
        }

        if (Regex("\\d{2}:\\d{2}").containsMatchIn(compact)) {
            return true
        }

        return false
    }

    private fun removePriceAndReceiptNoise(line: String): String {
        return line
            .replace(priceRegex, " ")
            .replace(Regex("\\b[0-9]{8,}\\b"), " ")
            .replace(Regex("\\b[0-9]{2,4}-[0-9]{2,4}-[0-9]{4}\\b"), " ")
            .replace(Regex("\\b[0-9]{3}-[0-9]{2}-[0-9]{5}\\b"), " ")
    }

    private fun cleanNameText(value: String): String {
        return value
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("\\[[^]]*]"), " ")
            .replace(Regex("[*#@%~+=|<>]"), " ")
            .replace(Regex("[^가-힣a-zA-Z0-9\\s./-]"), " ")
            .replace(unitRegex, " ")
            .replace(Regex("\\b[0-9]+\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun chooseCandidateName(cleaned: String): String {
        val tokens = cleaned
            .split(" ")
            .map { it.trim() }
            .filter { it.length >= 2 }
            .filterNot { token -> ignoredKeywords.any { token.contains(it, ignoreCase = true) } }

        if (tokens.isEmpty()) return ""

        val joined = tokens.joinToString(" ").trim()

        val matchedFoodKeyword = foodKeywords.firstOrNull { keyword ->
            joined.contains(keyword, ignoreCase = true)
        }

        if (matchedFoodKeyword != null) {
            return shortenAroundKeyword(
                text = joined,
                keyword = matchedFoodKeyword
            )
        }

        return joined
    }

    private fun shortenAroundKeyword(
        text: String,
        keyword: String
    ): String {
        val tokens = text.split(" ").filter { it.isNotBlank() }

        val keywordIndex = tokens.indexOfFirst { it.contains(keyword, ignoreCase = true) }

        if (keywordIndex < 0) {
            return text.take(20).trim()
        }

        val start = (keywordIndex - 1).coerceAtLeast(0)
        val end = (keywordIndex + 2).coerceAtMost(tokens.size)

        return tokens
            .subList(start, end)
            .joinToString(" ")
            .trim()
    }

    private fun isUsableCandidate(name: String): Boolean {
        val compact = name.replace(" ", "")

        if (compact.length < 2) return false
        if (compact.length > 22) return false

        val koreanCount = compact.count { it in '가'..'힣' }
        val englishCount = compact.count { it.isLetter() && it !in '가'..'힣' }

        if (koreanCount == 0 && englishCount < 2) return false

        if (ignoredKeywords.any { compact.contains(it, ignoreCase = true) }) {
            return false
        }

        if (Regex("^[0-9]+$").matches(compact)) {
            return false
        }

        return true
    }

    private fun normalizeForDistinct(name: String): String {
        return name
            .lowercase()
            .replace(Regex("[^가-힣a-zA-Z0-9]"), "")
            .trim()
    }
}
