package com.example.swtermproject.data.repository

import android.net.Uri
import com.example.swtermproject.common.BConstants
import com.example.swtermproject.data.remote.shopping.BShoppingApi
import com.example.swtermproject.domain.model.BShoppingItem

class BShoppingRepository(
    private val shoppingApi: BShoppingApi? = null
) {
    suspend fun searchShoppingItems(keyword: String): List<BShoppingItem> {
        val api = shoppingApi

        if (
            api == null ||
            BConstants.NAVER_CLIENT_ID.isBlank() ||
            BConstants.NAVER_CLIENT_SECRET.isBlank()
        ) {
            return listOf(createFallbackShoppingItem(keyword))
        }

        return runCatching {
            api.searchShoppingItems(
                clientId = BConstants.NAVER_CLIENT_ID,
                clientSecret = BConstants.NAVER_CLIENT_SECRET,
                query = keyword
            ).items.map {
                BShoppingItem(
                    title = it.title.orEmpty().replace("<b>", "").replace("</b>", ""),
                    imageUrl = it.image,
                    price = it.lprice,
                    link = it.link.orEmpty()
                )
            }
        }.getOrElse {
            listOf(createFallbackShoppingItem(keyword))
        }
    }

    private fun createFallbackShoppingItem(keyword: String): BShoppingItem {
        val encoded = Uri.encode(keyword)
        return BShoppingItem(
            title = "$keyword 구매 검색",
            imageUrl = null,
            price = null,
            link = "https://search.shopping.naver.com/search/all?query=$encoded"
        )
    }
}
