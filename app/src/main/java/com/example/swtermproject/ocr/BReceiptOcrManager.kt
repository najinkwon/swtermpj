package com.example.swtermproject.ocr

import android.content.Context
import android.graphics.Bitmap
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
            recognize(image)
        }
    }

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): Result<String> {
        return runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognize(image)
        }
    }

    private suspend fun recognize(image: InputImage): String {
        val recognizer = TextRecognition.getClient(
            KoreanTextRecognizerOptions.Builder().build()
        )

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    recognizer.close()

                    if (continuation.isActive) {
                        continuation.resume(visionText.text)
                    }
                }
                .addOnFailureListener { exception ->
                    recognizer.close()

                    if (continuation.isActive) {
                        continuation.resumeWith(
                            Result.failure(exception)
                        )
                    }
                }
        }
    }

    fun extractItemsFromText(rawText: String): List<BReceiptItemCandidate> {
        val lines = BReceiptParser.parseLines(rawText)
        return BReceiptItemExtractor.extractCandidates(lines)
    }
}
