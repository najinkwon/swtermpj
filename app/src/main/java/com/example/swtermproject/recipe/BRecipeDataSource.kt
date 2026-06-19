package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.domain.model.BRecipeIngredientRequirement

object BRecipeDataSource {

    private fun main(name: String, amount: Double, unit: String): BRecipeIngredientRequirement {
        return BRecipeIngredientRequirement(
            name = name,
            amount = amount,
            unit = unit,
            group = "main",
            essential = true
        )
    }

    private fun sub(name: String, amount: Double, unit: String): BRecipeIngredientRequirement {
        return BRecipeIngredientRequirement(
            name = name,
            amount = amount,
            unit = unit,
            group = "sub",
            essential = true
        )
    }

    private fun seasoning(name: String, amount: Double, unit: String): BRecipeIngredientRequirement {
        return BRecipeIngredientRequirement(
            name = name,
            amount = amount,
            unit = unit,
            group = "seasoning",
            essential = false
        )
    }

    val recipes = listOf(
        BRecipe(
            id = 1,
            title = "계란볶음밥",
            category = "한식",
            mainIngredients = listOf("밥", "계란"),
            subIngredients = listOf("대파", "양파"),
            seasonings = listOf("간장", "소금", "후추", "식용유"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("계란", 1.0, "개"),
                sub("대파", 20.0, "g"),
                sub("양파", 50.0, "g"),
                seasoning("간장", 15.0, "ml"),
                seasoning("소금", 2.0, "g"),
                seasoning("후추", 1.0, "g"),
                seasoning("식용유", 15.0, "ml")
            ),
            description = """
                1. 대파와 양파를 잘게 썰어주세요.
                2. 팬에 식용유를 두르고 대파를 먼저 볶아 파기름을 냅니다.
                3. 밥과 계란을 넣고 함께 볶아주세요.
                4. 간장, 소금, 후추로 간을 맞추면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "계란볶음밥 레시피"
        ),
        BRecipe(
            id = 2,
            title = "간장계란밥",
            category = "한식",
            mainIngredients = listOf("밥", "계란"),
            subIngredients = listOf(),
            seasonings = listOf("간장", "참기름"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("계란", 1.0, "개"),
                seasoning("간장", 15.0, "ml"),
                seasoning("참기름", 5.0, "ml")
            ),
            description = """
                1. 따뜻한 밥을 그릇에 담습니다.
                2. 계란후라이를 올립니다.
                3. 간장과 참기름을 넣고 비벼 먹으면 됩니다.
            """.trimIndent(),
            youtubeKeyword = "간장계란밥 레시피"
        ),
        BRecipe(
            id = 3,
            title = "두부조림",
            category = "한식",
            mainIngredients = listOf("두부"),
            subIngredients = listOf("대파", "양파"),
            seasonings = listOf("간장", "고춧가루", "설탕", "다진마늘"),
            requiredIngredients = listOf(
                main("두부", 1.0, "개"),
                sub("대파", 20.0, "g"),
                sub("양파", 50.0, "g"),
                seasoning("간장", 30.0, "ml"),
                seasoning("고춧가루", 5.0, "g"),
                seasoning("설탕", 5.0, "g"),
                seasoning("다진마늘", 10.0, "g")
            ),
            description = """
                1. 두부를 먹기 좋은 크기로 자릅니다.
                2. 팬에 두부를 살짝 굽습니다.
                3. 간장, 고춧가루, 설탕, 다진마늘로 양념장을 만듭니다.
                4. 양념장을 넣고 졸이면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "두부조림 레시피"
        ),
        BRecipe(
            id = 4,
            title = "김치볶음밥",
            category = "한식",
            mainIngredients = listOf("밥", "김치"),
            subIngredients = listOf("계란", "대파", "양파"),
            seasonings = listOf("간장", "참기름", "고춧가루"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("김치", 100.0, "g"),
                sub("계란", 1.0, "개"),
                sub("대파", 20.0, "g"),
                sub("양파", 50.0, "g"),
                seasoning("간장", 10.0, "ml"),
                seasoning("참기름", 5.0, "ml"),
                seasoning("고춧가루", 5.0, "g")
            ),
            description = """
                1. 김치와 대파를 먹기 좋은 크기로 썰어주세요.
                2. 팬에 기름을 두르고 김치와 대파를 먼저 볶습니다.
                3. 밥을 넣고 골고루 섞어 볶습니다.
                4. 간장과 참기름으로 간을 맞추고 계란을 올리면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "김치볶음밥 레시피"
        ),
        BRecipe(
            id = 5,
            title = "계란찜",
            category = "한식",
            mainIngredients = listOf("계란"),
            subIngredients = listOf("대파", "당근"),
            seasonings = listOf("소금", "참기름"),
            requiredIngredients = listOf(
                main("계란", 2.0, "개"),
                sub("대파", 15.0, "g"),
                sub("당근", 30.0, "g"),
                seasoning("소금", 2.0, "g"),
                seasoning("참기름", 3.0, "ml")
            ),
            description = """
                1. 계란을 풀고 물을 조금 섞어주세요.
                2. 대파와 당근을 잘게 썰어 넣습니다.
                3. 소금으로 간을 맞춘 뒤 약불에서 익힙니다.
                4. 부드럽게 익으면 참기름을 살짝 둘러 완성합니다.
            """.trimIndent(),
            youtubeKeyword = "계란찜 레시피"
        ),
        BRecipe(
            id = 6,
            title = "된장국",
            category = "한식",
            mainIngredients = listOf("된장"),
            subIngredients = listOf("두부", "대파", "양파", "감자"),
            seasonings = listOf("다진마늘", "고춧가루"),
            requiredIngredients = listOf(
                main("된장", 30.0, "g"),
                sub("두부", 0.5, "개"),
                sub("대파", 20.0, "g"),
                sub("양파", 50.0, "g"),
                sub("감자", 1.0, "개"),
                seasoning("다진마늘", 10.0, "g"),
                seasoning("고춧가루", 5.0, "g")
            ),
            description = """
                1. 냄비에 물을 넣고 된장을 풀어주세요.
                2. 감자와 양파를 먼저 넣고 끓입니다.
                3. 두부와 대파를 넣고 한 번 더 끓입니다.
                4. 다진마늘과 고춧가루로 맛을 조절하면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "된장국 레시피"
        ),
        BRecipe(
            id = 7,
            title = "토마토 파스타",
            category = "양식",
            mainIngredients = listOf("파스타면"),
            subIngredients = listOf("양파", "마늘", "토마토"),
            seasonings = listOf("소금", "후추", "올리브유"),
            requiredIngredients = listOf(
                main("파스타면", 100.0, "g"),
                sub("양파", 50.0, "g"),
                sub("마늘", 10.0, "g"),
                sub("토마토", 1.0, "개"),
                seasoning("소금", 2.0, "g"),
                seasoning("후추", 1.0, "g"),
                seasoning("올리브유", 15.0, "ml")
            ),
            description = """
                1. 파스타면을 삶습니다.
                2. 팬에 올리브유를 두르고 마늘과 양파를 볶습니다.
                3. 토마토 또는 토마토소스를 넣고 끓입니다.
                4. 삶은 면을 넣고 섞으면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "토마토 파스타 레시피"
        ),
        BRecipe(
            id = 8,
            title = "크림 파스타",
            category = "양식",
            mainIngredients = listOf("파스타면", "우유"),
            subIngredients = listOf("양파", "치즈", "버섯"),
            seasonings = listOf("소금", "후추", "버터"),
            requiredIngredients = listOf(
                main("파스타면", 100.0, "g"),
                main("우유", 200.0, "ml"),
                sub("양파", 50.0, "g"),
                sub("치즈", 1.0, "개"),
                sub("버섯", 50.0, "g"),
                seasoning("소금", 2.0, "g"),
                seasoning("후추", 1.0, "g"),
                seasoning("버터", 10.0, "g")
            ),
            description = """
                1. 파스타면을 삶아 준비합니다.
                2. 팬에 버터를 녹이고 양파와 버섯을 볶습니다.
                3. 우유와 치즈를 넣어 크림 소스를 만듭니다.
                4. 삶은 면을 넣고 소금, 후추로 간을 맞추면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "크림 파스타 레시피"
        ),
        BRecipe(
            id = 9,
            title = "오므라이스",
            category = "양식",
            mainIngredients = listOf("밥", "계란"),
            subIngredients = listOf("양파", "당근", "햄"),
            seasonings = listOf("케첩", "소금", "후추"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("계란", 2.0, "개"),
                sub("양파", 50.0, "g"),
                sub("당근", 30.0, "g"),
                sub("햄", 50.0, "g"),
                seasoning("케첩", 30.0, "g"),
                seasoning("소금", 2.0, "g"),
                seasoning("후추", 1.0, "g")
            ),
            description = """
                1. 양파, 당근, 햄을 잘게 썰어 볶습니다.
                2. 밥과 케첩을 넣고 볶음밥을 만듭니다.
                3. 계란지단을 만들어 볶음밥 위에 올립니다.
                4. 케첩을 뿌리면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "오므라이스 레시피"
        ),
        BRecipe(
            id = 10,
            title = "프렌치토스트",
            category = "양식",
            mainIngredients = listOf("식빵", "계란", "우유"),
            subIngredients = listOf("치즈", "버터"),
            seasonings = listOf("설탕", "소금"),
            requiredIngredients = listOf(
                main("식빵", 2.0, "개"),
                main("계란", 1.0, "개"),
                main("우유", 100.0, "ml"),
                sub("치즈", 1.0, "개"),
                sub("버터", 10.0, "g"),
                seasoning("설탕", 10.0, "g"),
                seasoning("소금", 1.0, "g")
            ),
            description = """
                1. 계란과 우유를 섞어 달걀물을 만듭니다.
                2. 식빵을 달걀물에 적셔주세요.
                3. 팬에 버터를 녹이고 식빵을 앞뒤로 굽습니다.
                4. 설탕이나 치즈를 곁들이면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "프렌치토스트 레시피"
        ),
        BRecipe(
            id = 11,
            title = "감자샐러드",
            category = "양식",
            mainIngredients = listOf("감자"),
            subIngredients = listOf("계란", "양파", "당근", "오이"),
            seasonings = listOf("마요네즈", "소금", "후추"),
            requiredIngredients = listOf(
                main("감자", 2.0, "개"),
                sub("계란", 1.0, "개"),
                sub("양파", 50.0, "g"),
                sub("당근", 50.0, "g"),
                sub("오이", 1.0, "개"),
                seasoning("마요네즈", 30.0, "g"),
                seasoning("소금", 2.0, "g"),
                seasoning("후추", 1.0, "g")
            ),
            description = """
                1. 감자와 계란을 삶아 으깨주세요.
                2. 양파, 당근, 오이를 잘게 썹니다.
                3. 재료를 한데 섞고 마요네즈를 넣습니다.
                4. 소금과 후추로 간을 맞추면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "감자샐러드 레시피"
        ),
        BRecipe(
            id = 12,
            title = "규동",
            category = "일식",
            mainIngredients = listOf("밥", "소고기"),
            subIngredients = listOf("양파", "계란"),
            seasonings = listOf("간장", "설탕"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("소고기", 120.0, "g"),
                sub("양파", 80.0, "g"),
                sub("계란", 1.0, "개"),
                seasoning("간장", 30.0, "ml"),
                seasoning("설탕", 10.0, "g")
            ),
            description = """
                1. 양파를 채 썰고 소고기를 준비합니다.
                2. 간장과 설탕을 넣은 소스에 양파와 소고기를 졸입니다.
                3. 밥 위에 올리고 계란을 곁들이면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "규동 레시피"
        ),
        BRecipe(
            id = 13,
            title = "오야코동",
            category = "일식",
            mainIngredients = listOf("밥", "계란", "닭고기"),
            subIngredients = listOf("양파", "대파"),
            seasonings = listOf("간장", "설탕"),
            requiredIngredients = listOf(
                main("밥", 200.0, "g"),
                main("계란", 2.0, "개"),
                main("닭고기", 120.0, "g"),
                sub("양파", 80.0, "g"),
                sub("대파", 20.0, "g"),
                seasoning("간장", 30.0, "ml"),
                seasoning("설탕", 10.0, "g")
            ),
            description = """
                1. 닭고기와 양파를 먹기 좋은 크기로 준비합니다.
                2. 간장과 설탕을 넣은 국물에 닭고기와 양파를 익힙니다.
                3. 계란을 풀어 위에 붓고 반숙으로 익힙니다.
                4. 밥 위에 올리면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "오야코동 레시피"
        ),
        BRecipe(
            id = 14,
            title = "야키소바",
            category = "일식",
            mainIngredients = listOf("면"),
            subIngredients = listOf("양배추", "양파", "당근", "햄"),
            seasonings = listOf("간장", "소스", "후추"),
            requiredIngredients = listOf(
                main("면", 1.0, "개"),
                sub("양배추", 150.0, "g"),
                sub("양파", 50.0, "g"),
                sub("당근", 30.0, "g"),
                sub("햄", 50.0, "g"),
                seasoning("간장", 15.0, "ml"),
                seasoning("소스", 30.0, "g"),
                seasoning("후추", 1.0, "g")
            ),
            description = """
                1. 채소와 햄을 먹기 좋은 크기로 썰어주세요.
                2. 팬에 재료를 볶다가 면을 넣습니다.
                3. 간장이나 소스로 간을 맞춥니다.
                4. 후추를 살짝 뿌려 마무리합니다.
            """.trimIndent(),
            youtubeKeyword = "야키소바 레시피"
        ),
        BRecipe(
            id = 15,
            title = "냉털 샐러드",
            category = "기타",
            mainIngredients = listOf("상추"),
            subIngredients = listOf("계란", "토마토", "오이", "치즈"),
            seasonings = listOf("드레싱", "소금", "후추"),
            requiredIngredients = listOf(
                main("상추", 5.0, "개"),
                sub("계란", 1.0, "개"),
                sub("토마토", 1.0, "개"),
                sub("오이", 1.0, "개"),
                sub("치즈", 1.0, "개"),
                seasoning("드레싱", 30.0, "ml"),
                seasoning("소금", 1.0, "g"),
                seasoning("후추", 1.0, "g")
            ),
            description = """
                1. 채소를 깨끗하게 씻고 먹기 좋게 자릅니다.
                2. 계란이나 치즈 같은 단백질 재료를 곁들입니다.
                3. 드레싱을 뿌리고 가볍게 섞어주세요.
                4. 소금과 후추로 마무리하면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "냉털 샐러드 레시피"
        ),
        BRecipe(
            id = 16,
            title = "간단 샌드위치",
            category = "기타",
            mainIngredients = listOf("식빵"),
            subIngredients = listOf("계란", "치즈", "상추", "햄"),
            seasonings = listOf("마요네즈", "소금", "후추"),
            requiredIngredients = listOf(
                main("식빵", 2.0, "개"),
                sub("계란", 1.0, "개"),
                sub("치즈", 1.0, "개"),
                sub("상추", 2.0, "개"),
                sub("햄", 50.0, "g"),
                seasoning("마요네즈", 20.0, "g"),
                seasoning("소금", 1.0, "g"),
                seasoning("후추", 1.0, "g")
            ),
            description = """
                1. 식빵을 준비하고 속재료를 손질합니다.
                2. 계란이나 햄을 익혀주세요.
                3. 빵 위에 재료를 차곡차곡 올립니다.
                4. 마요네즈나 소스를 발라 덮으면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "간단 샌드위치 레시피"
        )
    )
}