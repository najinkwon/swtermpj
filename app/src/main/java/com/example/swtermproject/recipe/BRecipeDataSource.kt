package com.example.swtermproject.recipe

import com.example.swtermproject.domain.model.BRecipe

object BRecipeDataSource {
    val recipes = listOf(
        BRecipe(
            id = 1,
            title = "계란볶음밥",
            category = "한식",
            mainIngredients = listOf("밥", "계란"),
            subIngredients = listOf("대파", "양파"),
            seasonings = listOf("간장", "소금", "후추", "식용유"),
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
            title = "토마토 파스타",
            category = "양식",
            mainIngredients = listOf("파스타면"),
            subIngredients = listOf("양파", "마늘", "토마토"),
            seasonings = listOf("소금", "후추", "올리브유"),
            description = """
                1. 파스타면을 삶습니다.
                2. 팬에 올리브유를 두르고 마늘과 양파를 볶습니다.
                3. 토마토 또는 토마토소스를 넣고 끓입니다.
                4. 삶은 면을 넣고 섞으면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "토마토 파스타 레시피"
        ),
        BRecipe(
            id = 5,
            title = "오므라이스",
            category = "양식",
            mainIngredients = listOf("밥", "계란"),
            subIngredients = listOf("양파", "당근", "햄"),
            seasonings = listOf("케첩", "소금", "후추"),
            description = """
                1. 양파, 당근, 햄을 잘게 썰어 볶습니다.
                2. 밥과 케첩을 넣고 볶음밥을 만듭니다.
                3. 계란지단을 만들어 볶음밥 위에 올립니다.
                4. 케첩을 뿌리면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "오므라이스 레시피"
        ),
        BRecipe(
            id = 6,
            title = "규동",
            category = "일식",
            mainIngredients = listOf("밥", "소고기"),
            subIngredients = listOf("양파", "계란"),
            seasonings = listOf("간장", "설탕"),
            description = """
                1. 양파를 채 썰고 소고기를 준비합니다.
                2. 간장과 설탕을 넣은 소스에 양파와 소고기를 졸입니다.
                3. 밥 위에 올리고 계란을 곁들이면 완성입니다.
            """.trimIndent(),
            youtubeKeyword = "규동 레시피"
        )
    )
}
