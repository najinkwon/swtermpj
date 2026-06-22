package com.example.swtermproject.ocr

import java.util.Locale

object BAmountNormalizer {

    data class NormalizedAmount(
        val amountText: String,
        val amount: Double,
        val unit: String,
        val source: String = "receipt"
    ) {
        val amountGram: Double?
            get() = if (unit == "g") amount else null
    }

    fun extractFromText(rawText: String): NormalizedAmount? {
        val text = rawText
            .replace(",", "")
            .replace(" ", "")
            .lowercase(Locale.getDefault())

        if (text.isBlank()) return null

        parseCountPack(text)?.let { return it }
        parseCount(text)?.let { return it }
        parseFractionGeun(text)?.let { return it }
        parseKoreanGeun(text)?.let { return it }
        parseKg(text)?.let { return it }
        parseGram(text)?.let { return it }
        parseLiter(text)?.let { return it }
        parseMilliLiter(text)?.let { return it }
        parsePack(text)?.let { return it }

        return null
    }

    private fun parseCountPack(text: String): NormalizedAmount? {
        // 예: 2입/봉, 3입/팩
        val match = Regex("(\\d+(\\.\\d+)?)(입|개|알)/(봉|팩|묶음)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value,
            unit = "개"
        )
    }

    private fun parseCount(text: String): NormalizedAmount? {
        // 예: 10입, 2개, 계란30구, 메추리알20알
        val match = Regex("(\\d+(\\.\\d+)?)(개|입|알|구|마리)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value,
            unit = "개"
        )
    }

    private fun parseKg(text: String): NormalizedAmount? {
        val match = Regex("(\\d+(\\.\\d+)?)(kg|킬로|키로)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value * 1000.0,
            unit = "g"
        )
    }

    private fun parseGram(text: String): NormalizedAmount? {
        val match = Regex("(\\d+(\\.\\d+)?)(g|그램)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value,
            unit = "g"
        )
    }

    private fun parseLiter(text: String): NormalizedAmount? {
        val match = Regex("(\\d+(\\.\\d+)?)(l|리터)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value * 1000.0,
            unit = "ml"
        )
    }

    private fun parseMilliLiter(text: String): NormalizedAmount? {
        val match = Regex("(\\d+(\\.\\d+)?)(ml|밀리리터|미리)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value,
            unit = "ml"
        )
    }

    private fun parseKoreanGeun(text: String): NormalizedAmount? {
        if (text.contains("한근")) {
            return NormalizedAmount(
                amountText = "한근",
                amount = 600.0,
                unit = "g"
            )
        }

        if (text.contains("반근")) {
            return NormalizedAmount(
                amountText = "반근",
                amount = 300.0,
                unit = "g"
            )
        }

        val match = Regex("(\\d+(\\.\\d+)?)근").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null

        return NormalizedAmount(
            amountText = match.value,
            amount = value * 600.0,
            unit = "g"
        )
    }

    private fun parseFractionGeun(text: String): NormalizedAmount? {
        val match = Regex("(\\d+)/(\\d+)근").find(text)
            ?: return null

        val numerator = match.groupValues[1].toDoubleOrNull() ?: return null
        val denominator = match.groupValues[2].toDoubleOrNull() ?: return null

        if (denominator == 0.0) return null

        return NormalizedAmount(
            amountText = match.value,
            amount = numerator / denominator * 600.0,
            unit = "g"
        )
    }

    private fun parsePack(text: String): NormalizedAmount? {
        // 예: 1봉, 2팩. 정확한 중량은 모르므로 봉/팩 그대로 저장
        val match = Regex("(\\d+(\\.\\d+)?)(봉|팩|묶음|통|병|캔)").find(text)
            ?: return null

        val value = match.groupValues[1].toDoubleOrNull() ?: return null
        val rawUnit = match.groupValues[3]

        val unit = when (rawUnit) {
            "봉" -> "봉"
            "팩" -> "팩"
            "묶음" -> "팩"
            "통" -> "개"
            "병" -> "개"
            "캔" -> "개"
            else -> rawUnit
        }

        return NormalizedAmount(
            amountText = match.value,
            amount = value,
            unit = unit
        )
    }

    fun defaultUnitForIngredient(name: String): String {
        val compact = name.replace(" ", "")

        val countBasedKeywords = listOf(
            "계란", "달걀", "메추리알",
            "브로콜리", "브로커리", "브로컬리", "브로코리",
            "파프리카", "피망",
            "양상추", "양배추", "적양배추",
            "상추", "깻잎",
            "오이", "애호박", "단호박",
            "토마토", "방울토마토",
            "사과", "배", "바나나", "귤", "오렌지", "키위",
            "두부", "순두부", "연두부"
        )

        val mlBasedKeywords = listOf(
            "우유", "두유", "생수", "물", "주스",
            "간장", "식초", "식용유", "올리브유", "참기름", "들기름",
            "액젓", "소스", "드레싱"
        )

        return when {
            countBasedKeywords.any { compact.contains(it) } -> "개"
            mlBasedKeywords.any { compact.contains(it) } -> "ml"
            else -> "g"
        }
    }

    fun defaultAmountForUnit(unit: String): Double {
        return when (unit) {
            "g" -> 100.0
            "ml" -> 100.0
            else -> 1.0
        }
    }

    fun formatAmount(amount: Double, unit: String): String {
        val amountText =
            if (amount % 1.0 == 0.0) {
                amount.toInt().toString()
            } else {
                String.format(Locale.KOREA, "%.1f", amount)
                    .trimEnd('0')
                    .trimEnd('.')
            }

        return "$amountText$unit"
    }
}