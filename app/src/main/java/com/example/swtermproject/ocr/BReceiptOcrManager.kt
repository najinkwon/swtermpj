package com.example.swtermproject.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class BReceiptOcrManager(
    private val context: Context
) {
    suspend fun recognizeTextFromImage(uri: Uri): Result<String> {
        return runCatching {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(
                KoreanTextRecognizerOptions.Builder().build()
            )

            suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        if (continuation.isActive) {
                            continuation.resume(visionText.text)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWith(
                                Result.failure(exception)
                            )
                        }
                    }
            }
        }
    }

    fun extractItemsFromText(rawText: String): List<BReceiptItemCandidate> {
        val lines = BReceiptParser.parseLines(rawText)
        return BReceiptItemExtractor.extractCandidates(lines)
    }
}
