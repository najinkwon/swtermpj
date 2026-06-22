package com.example.swtermproject.data.model

object RecipeDummyStore {

    val recipes = listOf(
        Recipe(
            emoji = "🍳",
            title = "계란볶음밥",
            reason = "계란과 밥만 있어도 빠르게 만들 수 있는 냉털 메뉴예요.",
            matchPercent = 90,
            cookTime = "10분",
            difficulty = "쉬움",
            ingredients = listOf("계란", "밥", "간장", "대파"),
            steps = listOf(
                "팬에 기름을 두르고 대파를 먼저 볶아요.",
                "계란을 넣고 스크램블처럼 익혀요.",
                "밥을 넣고 골고루 섞어요.",
                "간장으로 간을 맞추고 한 번 더 볶아요."
            )
        ),
        Recipe(
            emoji = "🥘",
            title = "김치볶음밥",
            reason = "남은 밥과 김치를 활용하기 좋아요.",
            matchPercent = 78,
            cookTime = "15분",
            difficulty = "보통",
            ingredients = listOf("김치", "밥", "계란", "대파"),
            steps = listOf(
                "김치를 먹기 좋은 크기로 잘라요.",
                "팬에 김치와 대파를 먼저 볶아요.",
                "밥을 넣고 골고루 섞어요.",
                "계란 프라이를 올리면 완성돼요."
            )
        ),
        Recipe(
            emoji = "🍝",
            title = "크림 파스타",
            reason = "우유와 치즈 같은 유제품을 소비하기 좋은 메뉴예요.",
            matchPercent = 65,
            cookTime = "20분",
            difficulty = "보통",
            ingredients = listOf("우유", "치즈", "파스타면", "양파"),
            steps = listOf(
                "파스타면을 삶아요.",
                "양파를 볶다가 우유를 넣어요.",
                "치즈를 넣어 소스를 걸쭉하게 만들어요.",
                "삶은 면을 넣고 잘 섞어요."
            )
        )
    )

    fun findRecipe(title: String): Recipe {
        return recipes.find { it.title == title } ?: recipes.first()
    }
}