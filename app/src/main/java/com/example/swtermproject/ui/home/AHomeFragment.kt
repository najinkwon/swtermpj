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
import com.example.swtermproject.data.model.RecipeDummyStore
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.ui.ingredient.AIngredientListFragment
import com.example.swtermproject.ui.recipe.RecipeAdapter
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
        recyclerRecipe.adapter = RecipeAdapter(
            RecipeDummyStore.recipes.take(2)
        ) { recipe ->
            (activity as MainActivity).openRecipeDetail(recipe.title)
        }

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
