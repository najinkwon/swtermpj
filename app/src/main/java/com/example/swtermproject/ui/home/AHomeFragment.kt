package com.example.swtermproject.ui.home

import android.animation.ValueAnimator
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.Recipe
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.recipe.BRecipeDataSource
import com.example.swtermproject.recipe.BRecipeScorer
import com.example.swtermproject.ui.ingredient.AIngredientListFragment
import com.example.swtermproject.viewmodel.BHomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AHomeFragment : Fragment() {

    private val viewModel: BHomeViewModel by viewModels()

    private lateinit var textTotal: TextView
    private lateinit var textFavorite: TextView
    private lateinit var textLowStock: TextView
    private lateinit var textExpire: TextView

    private lateinit var recipeAdapter: HomeRecipeAdapter
    private val homeRecipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )

        textTotal = view.findViewById(R.id.textTotalCount)
        textFavorite = view.findViewById(R.id.textFavoriteCount)
        textLowStock = view.findViewById(R.id.textLowStockCount)
        textExpire = view.findViewById(R.id.textExpireCount)

        val cardTotal = view.findViewById<LinearLayout>(R.id.cardTotal)
        val cardFavorite = view.findViewById<LinearLayout>(R.id.cardFavorite)
        val cardLowStock = view.findViewById<LinearLayout>(R.id.cardLowStock)
        val cardExpire = view.findViewById<LinearLayout>(R.id.cardExpire)

        val buttonMoreRecipe = view.findViewById<TextView>(R.id.buttonMoreRecipe)
        val recyclerRecipe = view.findViewById<RecyclerView>(R.id.recyclerRecipe)

        recyclerRecipe.layoutManager = GridLayoutManager(requireContext(), 2)

        recipeAdapter = HomeRecipeAdapter(homeRecipes) { recipe ->
            (activity as MainActivity).openRecipeDetail(recipe.title)
        }

        recyclerRecipe.adapter = recipeAdapter

        cardTotal.setOnClickListener {
            (activity as MainActivity).openIngredientListWithFilter(
                AIngredientListFragment.FILTER_ALL
            )
        }

        cardFavorite.setOnClickListener {
            (activity as MainActivity).openIngredientListWithFilter(
                AIngredientListFragment.FILTER_FAVORITE
            )
        }

        cardLowStock.setOnClickListener {
            (activity as MainActivity).openIngredientListWithFilter(
                AIngredientListFragment.FILTER_LOW
            )
        }

        cardExpire.setOnClickListener {
            (activity as MainActivity).openIngredientListWithFilter(
                AIngredientListFragment.FILTER_EXPIRE
            )
        }

        buttonMoreRecipe.setOnClickListener {
            (activity as MainActivity).openRecipeCategory()
        }

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
                    updateDashboard(ingredients)
                    updateHomeRecipes(ingredients)
                }
            }
        }
    }

    private fun updateDashboard(ingredients: List<BIngredient>) {
        val totalCount = ingredients.size
        val lowStockCount = ingredients.count { it.stockPercent <= 20 }
        val expireCount = ingredients.count {
            calculateExpireDay(it.expiryDate) <= 3
        }
        val favoriteCount = ingredients.count { it.favorite }

        animateCount(textTotal, totalCount)
        animateCount(textFavorite, favoriteCount)
        animateCount(textLowStock, lowStockCount)
        animateCount(textExpire, expireCount)
    }

    private fun updateHomeRecipes(ingredients: List<BIngredient>) {
        val recommended = BRecipeDataSource.recipes
            .map { recipe ->
                BRecipeScorer.scoreRecipe(recipe, ingredients)
            }
            .sortedWith(
                compareByDescending<BRecipe> { it.score }
                    .thenBy { it.missingIngredients.size }
                    .thenBy { it.title }
            )
            .take(2)
            .map { it.toUiRecipe() }

        homeRecipes.clear()
        homeRecipes.addAll(recommended)
        recipeAdapter.notifyDataSetChanged()
    }

    private fun BRecipe.toUiRecipe(): Recipe {
        val matchPercent = calculateRecipeMatchPercent(this)

        val reasonText =
            if (missingIngredients.isEmpty()) {
                "현재 냉장고 재료만으로 만들기 좋아요."
            } else {
                "부족 재료: ${missingIngredients.joinToString(", ")}"
            }

        return Recipe(
            emoji = emojiForRecipe(this),
            title = title,
            reason = reasonText,
            matchPercent = matchPercent,
            cookTime = cookTimeForRecipe(this),
            difficulty = difficultyForRecipe(this),
            ingredients = mainIngredients + subIngredients + seasonings,
            steps = description
                .lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
        )
    }

    private fun calculateRecipeMatchPercent(recipe: BRecipe): Int {
        val required = recipe.mainIngredients + recipe.subIngredients

        if (required.isEmpty()) return 100

        val missingCount = recipe.missingIngredients
            .distinct()
            .count { it in required }

        val ownedCount = (required.size - missingCount).coerceAtLeast(0)

        return ((ownedCount.toDouble() / required.size.toDouble()) * 100.0)
            .toInt()
            .coerceIn(0, 100)
    }

    private fun emojiForRecipe(recipe: BRecipe): String {
        val source = "${recipe.title} ${recipe.category}"

        return when {
            source.contains("볶음밥") -> "🍳"
            source.contains("밥") -> "🍚"
            source.contains("파스타") -> "🍝"
            source.contains("두부") -> "🥘"
            source.contains("규동") -> "🍱"
            source.contains("오므라이스") -> "🍳"
            else -> "🍽️"
        }
    }

    private fun cookTimeForRecipe(recipe: BRecipe): String {
        return when {
            recipe.title.contains("간장계란밥") -> "5분"
            recipe.title.contains("볶음밥") -> "10분"
            recipe.title.contains("두부") -> "15분"
            recipe.title.contains("파스타") -> "20분"
            recipe.title.contains("오므라이스") -> "20분"
            recipe.title.contains("규동") -> "20분"
            else -> "15분"
        }
    }

    private fun difficultyForRecipe(recipe: BRecipe): String {
        return when {
            recipe.title.contains("간장계란밥") -> "쉬움"
            recipe.title.contains("계란볶음밥") -> "쉬움"
            else -> "보통"
        }
    }

    private fun calculateExpireDay(expiryDate: String): Int {
        if (expiryDate.isBlank()) return 7

        return runCatching {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = formatter.parse(expiryDate) ?: return 7

            val now = Date()
            val diff = targetDate.time - now.time

            TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
        }.getOrDefault(7)
    }

    private fun animateCount(
        textView: TextView,
        target: Int
    ) {
        val current = textView.text.toString().toIntOrNull() ?: 0

        val animator = ValueAnimator.ofInt(current, target)
        animator.duration = 450

        animator.addUpdateListener {
            textView.text = it.animatedValue.toString()
        }

        animator.start()
    }
}
