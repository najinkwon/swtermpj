package com.example.swtermproject.ml

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BIngredientClassifier(
    private val context: Context
) {
    data class Prediction(
        val category: String,
        val confidence: Float
    )

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var vocabulary: Map<String, Int> = emptyMap()

    init {
        loadModelIfExists()
    }

    fun classify(ingredientName: String): Prediction {
        val model = interpreter
        if (model != null && labels.isNotEmpty() && vocabulary.isNotEmpty()) {
            return runTfliteModel(model, ingredientName)
        }

        return fallbackClassify(ingredientName)
    }

    private fun loadModelIfExists() {
        runCatching {
            val assetManager = context.assets

            labels = assetManager.open("ml/labels.txt")
                .bufferedReader()
                .readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val vocabJson = assetManager.open("ml/vocabulary.json")
                .bufferedReader()
                .readText()

            vocabulary = parseVocabulary(vocabJson)

            val modelBytes = assetManager.open("ml/ingredient_classifier.tflite").use { input ->
                input.readBytes()
            }

            val buffer = ByteBuffer.allocateDirect(modelBytes.size)
                .order(ByteOrder.nativeOrder())
            buffer.put(modelBytes)
            buffer.rewind()

            interpreter = Interpreter(buffer)
        }.onFailure {
            interpreter = null
        }
    }

    private fun parseVocabulary(json: String): Map<String, Int> {
        if (json.isBlank() || json.trim() == "{}") return emptyMap()

        val obj = JSONObject(json)
        val result = mutableMapOf<String, Int>()

        obj.keys().forEach { key ->
            result[key] = obj.getInt(key)
        }

        return result
    }

    private fun runTfliteModel(model: Interpreter, ingredientName: String): Prediction {
        val inputVector = FloatArray(vocabulary.size)
        val tokens = BTextPreprocessor.extractTokens(ingredientName)

        tokens.forEach { token ->
            val index = vocabulary[token]
            if (index != null && index in inputVector.indices) {
                inputVector[index] = 1f
            }
        }

        val input = arrayOf(inputVector)
        val output = Array(1) { FloatArray(labels.size) }

        model.run(input, output)

        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0

        val confidence = probabilities.getOrElse(maxIndex) { 0f }

        if (confidence < 0.55f) {
            return fallbackClassify(ingredientName)
        }

        return Prediction(
            category = labels.getOrElse(maxIndex) { "기타" },
            confidence = confidence
        )
    }

    private fun fallbackClassify(ingredientName: String): Prediction {
        val name = BTextPreprocessor.clean(ingredientName)

        val category = when {
            listOf("계란", "달걀", "두부", "콩", "닭가슴살").any { name.contains(it) } -> "단백질"
            listOf("우유", "치즈", "요거트", "버터", "크림").any { name.contains(it) } -> "유제품"
            listOf("양파", "대파", "파", "감자", "당근", "마늘", "상추", "배추", "토마토", "버섯").any { name.contains(it) } -> "채소"
            listOf("소고기", "돼지고기", "삼겹살", "목살", "불고기", "닭", "햄", "베이컨").any { name.contains(it) } -> "육류"
            listOf("새우", "오징어", "고등어", "참치", "연어", "멸치", "조개").any { name.contains(it) } -> "해산물"
            listOf("밥", "쌀", "파스타", "면", "라면", "우동", "식빵", "빵", "또띠아").any { name.contains(it) } -> "탄수화물"
            listOf("간장", "고추장", "된장", "소금", "설탕", "식용유", "참기름", "다시다", "미원", "후추", "케첩", "마요네즈").any { name.contains(it) } -> "조미료"
            else -> "기타"
        }

        return Prediction(
            category = category,
            confidence = 0.6f
        )
    }
}
