package com.example.swtermproject.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
    private lateinit var emptyLayout: LinearLayout
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var recyclerView: RecyclerView

    private val shoppingList = mutableListOf<ShoppingItem>()
    private lateinit var adapter: AShoppingAdapter

    private var recipeName: String? = null
    private var recipeMissingIngredients: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recipeName = arguments?.getString(ARG_RECIPE_NAME)
        recipeMissingIngredients =
            arguments?.getStringArrayList(ARG_MISSING_INGREDIENTS).orEmpty()
    }

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
        emptyLayout = view.findViewById(R.id.layoutShoppingEmpty)
        emptyTitle = view.findViewById(R.id.textShoppingEmptyTitle)
        emptyMessage = view.findViewById(R.id.textShoppingEmptyMessage)
        recyclerView = view.findViewById(R.id.recyclerShopping)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AShoppingAdapter(shoppingList)
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isRecipeMode()) {
            updateRecipeShoppingList()
        } else {
            observeIngredients()
        }
    }

    private fun isRecipeMode(): Boolean {
        return recipeName != null
    }

    private fun observeIngredients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ingredients.collect { ingredients ->
                    updateLowStockShoppingList(ingredients)
                }
            }
        }
    }

    private fun updateLowStockShoppingList(ingredients: List<BIngredient>) {
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
            if (shoppingList.isEmpty()) {
                "현재 구매가 필요한 재료가 없어요"
            } else {
                "현재 ${shoppingList.size}개의 재료 구매를 추천해요"
            }

        mainMessage.text =
            if (shoppingList.isEmpty()) {
                "지금은 구매할 재료가 없어요"
            } else {
                "총 ${shoppingList.size}개의 부족 재료가 있어요"
            }

        subMessage.text =
            if (shoppingList.isEmpty()) {
                "재고가 충분해요. 부족해지면 여기에서 바로 확인할 수 있어요"
            } else {
                "부족한 재료만 모아서 보여드릴게요"
            }

        updateEmptyState(
            title = "구매할 재료가 없어요",
            message = "재고가 충분해요. 부족해지면 여기에서 알려드릴게요."
        )

        adapter.notifyDataSetChanged()
    }

    private fun updateRecipeShoppingList() {
        shoppingList.clear()

        recipeMissingIngredients
            .distinct()
            .filter { it.isNotBlank() }
            .forEach { name ->
                shoppingList.add(
                    ShoppingItem(
                        emoji = emojiForIngredientName(name),
                        name = name,
                        percent = RECIPE_MISSING_PERCENT
                    )
                )
            }

        val name = recipeName ?: "선택한 레시피"

        summary.text =
            if (shoppingList.isEmpty()) {
                "$name 레시피에 부족한 재료가 없어요"
            } else {
                "${name}에 필요한 부족 재료 ${shoppingList.size}개"
            }

        mainMessage.text =
            if (shoppingList.isEmpty()) {
                "바로 만들 수 있는 레시피예요"
            } else {
                "레시피에 필요한 재료를 모았어요"
            }

        subMessage.text =
            if (shoppingList.isEmpty()) {
                "현재 냉장고 재료만으로 조리할 수 있어요"
            } else {
                "부족 재료를 구매하면 이 레시피를 만들 수 있어요"
            }

        updateEmptyState(
            title = "바로 만들 수 있어요",
            message = "이 레시피는 현재 냉장고 재료만으로 조리할 수 있어요."
        )

        adapter.notifyDataSetChanged()
    }

    private fun updateEmptyState(
        title: String,
        message: String
    ) {
        val isEmpty = shoppingList.isEmpty()

        emptyLayout.visibility =
            if (isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }

        recyclerView.visibility =
            if (isEmpty) {
                View.GONE
            } else {
                View.VISIBLE
            }

        emptyTitle.text = title
        emptyMessage.text = message
    }

    private fun emojiForCategory(category: String): String {
        return when (category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🛒"
        }
    }

    private fun emojiForIngredientName(name: String): String {
        return when {
            name.contains("계란") ||
                name.contains("달걀") ||
                name.contains("고기") ||
                name.contains("닭") ||
                name.contains("소고기") ||
                name.contains("두부") ||
                name.contains("햄") -> "🥚"

            name.contains("우유") ||
                name.contains("치즈") ||
                name.contains("요거트") ||
                name.contains("버터") -> "🥛"

            name.contains("양파") ||
                name.contains("대파") ||
                name.contains("파") ||
                name.contains("마늘") ||
                name.contains("당근") ||
                name.contains("상추") ||
                name.contains("토마토") ||
                name.contains("감자") ||
                name.contains("오이") ||
                name.contains("양배추") -> "🥬"

            name.contains("간장") ||
                name.contains("소금") ||
                name.contains("후추") ||
                name.contains("설탕") ||
                name.contains("소스") ||
                name.contains("케첩") ||
                name.contains("마요네즈") ||
                name.contains("드레싱") ||
                name.contains("참기름") ||
                name.contains("고춧가루") ||
                name.contains("된장") -> "🥫"

            name.contains("🍚") ||
                name.contains("🍝") ||
                name.contains("파스타") ||
                name.contains("식빵") -> "🍚"

            else -> "🛒"
        }
    }

    companion object {
        private const val ARG_RECIPE_NAME = "recipe_name"
        private const val ARG_MISSING_INGREDIENTS = "missing_ingredients"
        const val RECIPE_MISSING_PERCENT = -1

        fun newRecipeMode(
            recipeName: String,
            missingIngredients: ArrayList<String>
        ): AShoppingFragment {
            val fragment = AShoppingFragment()
            val bundle = Bundle()
            bundle.putString(ARG_RECIPE_NAME, recipeName)
            bundle.putStringArrayList(ARG_MISSING_INGREDIENTS, missingIngredients)
            fragment.arguments = bundle
            return fragment
        }
    }
}
