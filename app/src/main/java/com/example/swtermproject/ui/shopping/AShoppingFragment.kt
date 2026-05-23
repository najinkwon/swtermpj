package com.example.swtermproject.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ShoppingItem
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.viewmodel.BIngredientViewModel
import kotlinx.coroutines.launch

class AShoppingFragment : Fragment() {

    private val viewModel: BIngredientViewModel by viewModels()

    private lateinit var summary: TextView
    private lateinit var mainMessage: TextView
    private lateinit var subMessage: TextView
    private lateinit var recyclerView: RecyclerView

    private val shoppingList = mutableListOf<ShoppingItem>()
    private lateinit var adapter: AShoppingAdapter

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

        summary = view.findViewById(R.id.textShoppingSummary)
        mainMessage = view.findViewById(R.id.textShoppingMainMessage)
        subMessage = view.findViewById(R.id.textShoppingSubMessage)
        recyclerView = view.findViewById(R.id.recyclerShopping)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AShoppingAdapter(shoppingList)
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIngredients()
    }

    private fun observeIngredients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ingredients.collect { ingredients ->
                    updateShoppingList(ingredients)
                }
            }
        }
    }

    private fun updateShoppingList(ingredients: List<BIngredient>) {
        shoppingList.clear()

        ingredients
            .filter { it.stockPercent <= 20 }
            .forEach { ingredient ->
                shoppingList.add(
                    ShoppingItem(
                        emoji = emojiForCategory(ingredient.category),
                        name = ingredient.name,
                        percent = ingredient.stockPercent.coerceIn(0, 100)
                    )
                )
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

        adapter.notifyDataSetChanged()
    }

    private fun emojiForCategory(category: String): String {
        return when (category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🍖"
            "조미료/소스" -> "🥫"
            else -> "🛒"
        }
    }
}
