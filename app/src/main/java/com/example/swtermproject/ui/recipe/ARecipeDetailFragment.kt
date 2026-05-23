package com.example.swtermproject.ui.recipe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.recipe.BRecipeDataSource
import com.example.swtermproject.recipe.BRecipeScorer
import kotlinx.coroutines.launch

class ARecipeDetailFragment : Fragment() {

    private var recipeName: String = "계란볶음밥"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipeName = arguments?.getString("recipeName") ?: "계란볶음밥"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_recipe_detail,
            container,
            false
        )

        val recipe = BRecipeDataSource.recipes.find { it.title == recipeName }
            ?: BRecipeDataSource.recipes.first()

        val textImageEmoji = view.findViewById<TextView>(R.id.textRecipeImageEmoji)
        val textTitle = view.findViewById<TextView>(R.id.textRecipeTitle)
        val textReason = view.findViewById<TextView>(R.id.textRecipeReason)
        val textMatch = view.findViewById<TextView>(R.id.textMatch)
        val textCookTime = view.findViewById<TextView>(R.id.textCookTime)
        val textDifficulty = view.findViewById<TextView>(R.id.textDifficulty)
        val textIngredients = view.findViewById<TextView>(R.id.textIngredients)
        val textSteps = view.findViewById<TextView>(R.id.textSteps)
        val btnYoutube = view.findViewById<Button>(R.id.btnYoutube)
        val btnShopping = view.findViewById<Button>(R.id.btnShopping)

        textImageEmoji.text = emojiForRecipe(recipe)
        textTitle.text = "${emojiForRecipe(recipe)} ${recipe.title}"
        textReason.text = "냉장고 재료를 기준으로 매칭률을 계산하고 있어요."
        textMatch.text = "계산 중"
        textCookTime.text = cookTimeForRecipe(recipe)
        textDifficulty.text = difficultyForRecipe(recipe)

        textIngredients.text = (recipe.mainIngredients + recipe.subIngredients + recipe.seasonings)
            .joinToString("\n") { "• $it" }

        textSteps.text = recipe.description

        val ingredientRepository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val ingredients = ingredientRepository.getAllIngredients()
            val scoredRecipe = BRecipeScorer.scoreRecipe(recipe, ingredients)
            bindRecipeMatch(
                recipe = scoredRecipe,
                ownedIngredients = ingredients,
                textReason = textReason,
                textMatch = textMatch,
                textIngredients = textIngredients
            )
        }

        btnYoutube.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.youtube.com/results?search_query=${recipe.youtubeKeyword}"
                )
            )

            startActivity(intent)
        }

        btnShopping.setOnClickListener {
            (activity as MainActivity).openShopping()
        }

        return view
    }

    private fun bindRecipeMatch(
        recipe: BRecipe,
        ownedIngredients: List<BIngredient>,
        textReason: TextView,
        textMatch: TextView,
        textIngredients: TextView
    ) {
        val requiredIngredients =
            recipe.mainIngredients + recipe.subIngredients + recipe.seasonings

        val requiredForPercent =
            recipe.mainIngredients + recipe.subIngredients

        val missingSet = recipe.missingIngredients.toSet()

        val matchPercent =
            if (requiredForPercent.isEmpty()) {
                100
            } else {
                val missingCount = recipe.missingIngredients
                    .distinct()
                    .count { it in requiredForPercent }

                val ownedCount = (requiredForPercent.size - missingCount).coerceAtLeast(0)

                ((ownedCount.toDouble() / requiredForPercent.size.toDouble()) * 100.0)
                    .toInt()
                    .coerceIn(0, 100)
            }

        textMatch.text = "재료 ${matchPercent}%"

        textReason.text =
            if (recipe.missingIngredients.isEmpty()) {
                "현재 냉장고 재료만으로 만들기 좋은 레시피예요."
            } else {
                "부족 재료: ${recipe.missingIngredients.joinToString(", ")}"
            }

        textIngredients.text = requiredIngredients.joinToString("\n") { required ->
            val owned = ownedIngredients.any { ingredient ->
                ingredient.name.contains(required, ignoreCase = true) ||
                    required.contains(ingredient.name, ignoreCase = true)
            }

            when {
                required in missingSet -> "• $required  ❌ 부족"
                owned -> "• $required  ✅ 보유"
                else -> "• $required  ◻ 선택"
            }
        }
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

    companion object {
        fun newInstance(recipeName: String): ARecipeDetailFragment {
            val fragment = ARecipeDetailFragment()
            val bundle = Bundle()
            bundle.putString("recipeName", recipeName)
            fragment.arguments = bundle
            return fragment
        }
    }
}
