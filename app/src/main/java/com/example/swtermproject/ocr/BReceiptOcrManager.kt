package com.example.swtermproject.ocr

import android.content.Context
import android.net.Uri

class BReceiptOcrManager(
    private val context: Context
) {
    suspend fun recognizeTextFromImage(uri: Uri): Result<String> {
        return Result.failure(
            NotImplementedError(
                "OCR 실제 인식은 ML Kit 연결 단계에서 구현 예정입니다. 현재는 ReceiptParser/Extractor 테스트용 구조만 준비되었습니다."
            )
        )
    }

    fun extractItemsFromText(rawText: String): List<BReceiptItemCandidate> {
        val lines = BReceiptParser.parseLines(rawText)
        return BReceiptItemExtractor.extractCandidates(lines)
    }
}
