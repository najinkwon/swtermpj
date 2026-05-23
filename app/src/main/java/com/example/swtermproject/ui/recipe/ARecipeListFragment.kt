package com.example.swtermproject.ui.recipe

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
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.Recipe
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.viewmodel.BRecipeViewModel
import kotlinx.coroutines.launch

class ARecipeListFragment : Fragment() {

    private val viewModel: BRecipeViewModel by viewModels()

    private var category: String = "한식"

    private lateinit var title: TextView
    private lateinit var recyclerView: RecyclerView

    private val recipes = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString("category") ?: "한식"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_recipe_list,
            container,
            false
        )

        title = view.findViewById(R.id.textRecipeListTitle)
        recyclerView = view.findViewById(R.id.recyclerRecipe)

        title.text = titleForCategory(category)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = RecipeAdapter(recipes) { recipe ->
            (activity as MainActivity).openRecipeDetail(recipe.title)
        }

        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeRecipes()
        viewModel.recommend(category)
    }

    private fun observeRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipes.collect { recommendedRecipes ->
                    recipes.clear()

                    title.text = titleForCategory(category)

                    recipes.addAll(
                        recommendedRecipes.map { it.toUiRecipe() }
                    )

                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun BRecipe.toUiRecipe(): Recipe {
        val matchPercent = calculateMatchPercent(this)

        val reasonText =
            if (missingIngredients.isEmpty()) {
                "냉장고 재료만으로 만들기 좋아요."
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

    private fun calculateMatchPercent(recipe: BRecipe): Int {
        val required = recipe.mainIngredients + recipe.subIngredients

        if (required.isEmpty()) return 100

        val missing = recipe.missingIngredients
            .distinct()
            .count { it in required }

        val owned = (required.size - missing).coerceAtLeast(0)

        return ((owned.toDouble() / required.size.toDouble()) * 100.0).toInt()
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

    private fun titleForCategory(category: String): String {
        return when (category) {
            "한식" -> "오늘은 집밥 어때요?"
            "양식" -> "가볍게 즐기는 양식"
            "일식" -> "깔끔한 일식 한 끼"
            "기타" -> "간단하게 냉털하기"
            else -> "$category 추천 레시피"
        }
    }

    companion object {
        fun newInstance(category: String): ARecipeListFragment {
            val fragment = ARecipeListFragment()
            val bundle = Bundle()
            bundle.putString("category", category)
            fragment.arguments = bundle
            return fragment
        }
    }
}
