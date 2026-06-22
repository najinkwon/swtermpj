package com.example.swtermproject.ml

import android.content.Context
import com.example.swtermproject.ocr.BIngredientDictionary
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
        val dictionaryPrediction = classifyByDictionary(ingredientName)
        if (dictionaryPrediction != null) {
            return dictionaryPrediction
        }

        val model = interpreter
        if (model != null && labels.isNotEmpty() && vocabulary.isNotEmpty()) {
            return runTfliteModel(model, ingredientName)
        }

        return fallbackClassify(ingredientName)
    }

    private fun classifyByDictionary(ingredientName: String): Prediction? {
        val entry = BIngredientDictionary.findBest(ingredientName) ?: return null

        return Prediction(
            category = normalizeCategory(entry.category),
            confidence = 0.95f
        )
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
            labels = emptyList()
            vocabulary = emptyMap()
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

        val hasKnownToken = inputVector.any { it > 0f }
        if (!hasKnownToken) {
            return fallbackClassify(ingredientName)
        }

        val input = arrayOf(inputVector)
        val output = Array(1) { FloatArray(labels.size) }

        return runCatching {
            model.run(input, output)

            val probabilities = output[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities.getOrElse(maxIndex) { 0f }
            val predictedCategory = labels.getOrElse(maxIndex) { "기타" }

            if (confidence < 0.55f) {
                fallbackClassify(ingredientName)
            } else {
                Prediction(
                    category = normalizeCategory(predictedCategory),
                    confidence = confidence
                )
            }
        }.getOrElse {
            fallbackClassify(ingredientName)
        }
    }

    private fun fallbackClassify(ingredientName: String): Prediction {
        BIngredientDictionary.findBest(ingredientName)?.let { entry ->
            return Prediction(
                category = normalizeCategory(entry.category),
                confidence = 0.95f
            )
        }

        val name = BTextPreprocessor.clean(ingredientName)

        val category = when {
            listOf(
                "계란", "달걀", "왕란", "특란", "메추리알",
                "두부", "순두부", "연두부", "콩", "검은콩", "완두콩",
                "닭가슴살", "땅콩", "볶음땅콩", "아몬드", "호두", "견과"
            ).any { name.contains(it) } -> "단백질"

            listOf(
                "우유", "저지방우유", "초코우유", "딸기우유", "두유",
                "치즈", "모짜렐라", "모차렐라", "체다", "크림치즈",
                "요거트", "요구르트", "그릭요거트",
                "버터", "생크림", "휘핑크림", "연유"
            ).any { name.contains(it) } -> "유제품"

            listOf(
                "양파", "적양파", "대파", "쪽파", "파", "마늘", "생강",
                "감자", "고구마", "당근", "무", "배추", "알배추",
                "양배추", "적양배추", "상추", "양상추", "로메인", "깻잎",
                "시금치", "청경채", "부추", "미나리", "숙주", "콩나물",
                "오이", "애호박", "단호박", "호박", "가지", "토마토",
                "방울토마토", "브로콜리", "브로커리", "브로컬리", "브로코리",
                "파프리카", "피망", "고추", "청양고추", "버섯", "새송이버섯",
                "팽이버섯", "느타리버섯", "표고버섯", "양송이버섯", "연근",
                "우엉", "도라지", "고사리", "샐러드", "케일", "비트", "셀러리"
            ).any { name.contains(it) } -> "채소"

            listOf(
                "소고기", "한우", "불고기", "양지", "사태", "등심", "안심",
                "채끝", "갈비", "차돌박이", "우삼겹",
                "돼지고기", "삼겹살", "목살", "앞다리살", "뒷다리살",
                "항정살", "가브리살", "갈매기살", "돼지갈비", "제육",
                "닭", "닭고기", "닭가슴살", "닭다리살", "닭봉", "닭날개",
                "오리", "훈제오리", "햄", "스팸", "베이컨", "소시지", "소세지",
                "비엔나", "미트볼", "돈까스"
            ).any { name.contains(it) } -> "육류"

            listOf(
                "새우", "오징어", "한치", "낙지", "주꾸미", "쭈꾸미", "문어",
                "고등어", "삼치", "갈치", "꽁치", "참치", "연어", "광어", "우럭",
                "대구", "명태", "동태", "코다리", "조기", "굴비", "가자미",
                "멸치", "건새우", "황태", "북어",
                "조개", "바지락", "홍합", "굴", "전복", "소라", "꼬막",
                "어묵", "오뎅", "맛살", "크래미", "명란", "젓갈",
                "미역", "다시마", "김", "파래", "매생이"
            ).any { name.contains(it) } -> "해산물"

            listOf(
                "밥", "쌀", "백미", "현미", "찹쌀", "흑미", "잡곡", "보리", "귀리",
                "오트밀", "밀가루", "부침가루", "튀김가루", "전분", "빵가루",
                "식빵", "빵", "모닝빵", "베이글", "바게트", "또띠아",
                "즉석밥", "햇반", "라면", "우동", "소면", "중면", "칼국수",
                "당면", "쌀국수", "파스타", "스파게티", "펜네", "마카로니",
                "떡", "떡국떡", "떡볶이떡", "누룽지", "시리얼", "그래놀라"
            ).any { name.contains(it) } -> "탄수화물"

            listOf(
                "간장", "진간장", "양조간장", "국간장", "고추장", "된장", "쌈장",
                "초고추장", "춘장", "소금", "맛소금", "설탕", "올리고당", "물엿",
                "꿀", "식초", "맛술", "미림", "식용유", "카놀라유", "포도씨유",
                "올리브유", "참기름", "들기름", "고춧가루", "후추", "깨",
                "다시다", "미원", "치킨스톡", "액젓", "참치액", "굴소스",
                "돈까스소스", "데리야끼소스", "케첩", "케찹", "마요네즈",
                "머스타드", "칠리소스", "핫소스", "고추기름", "카레가루",
                "월계수", "바질", "오레가노", "파슬리", "계피", "시나몬", "와사비"
            ).any { name.contains(it) } -> "조미료"

            listOf(
                "김치", "깍두기", "단무지", "피클", "장아찌", "만두", "순대",
                "유부", "참치캔", "골뱅이캔", "옥수수캔", "토마토소스",
                "파스타소스", "로제소스", "크림소스"
            ).any { name.contains(it) } -> "가공식품"

            listOf(
                "사과", "배", "바나나", "귤", "오렌지", "레몬", "키위", "딸기",
                "블루베리", "포도", "복숭아", "자두", "체리", "망고", "파인애플",
                "멜론", "수박", "참외", "감", "아보카도"
            ).any { name.contains(it) } -> "과일"

            else -> "기타"
        }

        return Prediction(
            category = normalizeCategory(category),
            confidence = if (category == "기타") 0.4f else 0.6f
        )
    }

    private fun normalizeCategory(category: String): String {
        return when (category.trim()) {
            "가공식품" -> "기타"
            "과일" -> "기타"
            else -> category.trim().ifBlank { "기타" }
        }
    }
}