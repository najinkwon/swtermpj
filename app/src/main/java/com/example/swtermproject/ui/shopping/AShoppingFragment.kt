package com.example.swtermproject.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.ShoppingItem

class AShoppingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_shopping,
            container,
            false
        )

        val summary =
            view.findViewById<TextView>(
                R.id.textShoppingSummary
            )

        val mainMessage =
            view.findViewById<TextView>(
                R.id.textShoppingMainMessage
            )

        val subMessage =
            view.findViewById<TextView>(
                R.id.textShoppingSubMessage
            )

        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.recyclerShopping
            )

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        val shoppingList =
            mutableListOf<ShoppingItem>()

        ATempIngredientStore.ingredients.forEach { ingredient ->

            if (ingredient.percent <= 20) {

                val emoji = when (ingredient.category) {

                    "채소" -> "🥬"

                    "유제품" -> "🥛"

                    "단백질" -> "🍖"

                    "조미료/소스" -> "🥫"

                    else -> "🛒"
                }

                shoppingList.add(
                    ShoppingItem(
                        emoji = emoji,
                        name = ingredient.name,
                        percent = ingredient.percent
                    )
                )
            }
        }

        summary.text =
            "현재 ${shoppingList.size}개의 재료 구매를 추천해요"

        mainMessage.text =
            if (shoppingList.isEmpty()) {
                "지금은 구매할 재료가 없어요"
            } else {
                "총 ${shoppingList.size}개의 부족 재료가 있어요"
            }

        subMessage.text =
            if (shoppingList.isEmpty()) {
                "냉장고 상태가 안정적이에요"
            } else {
                "부족한 재료만 모아서 보여드릴게요"
            }

        recyclerView.adapter =
            AShoppingAdapter(shoppingList)

        return view
    }
}