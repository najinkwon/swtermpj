package com.example.swtermproject.data.remote.shopping

data class BShoppingResponse(
    val total: Int = 0,
    val start: Int = 0,
    val display: Int = 0,
    val items: List<BShoppingDto> = emptyList()
)
