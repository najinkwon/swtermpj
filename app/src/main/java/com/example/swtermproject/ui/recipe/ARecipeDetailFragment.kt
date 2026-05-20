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
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.RecipeDummyStore

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

        val recipe = RecipeDummyStore.findRecipe(recipeName)

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

        textImageEmoji.text = recipe.emoji
        textTitle.text = "${recipe.emoji} ${recipe.title}"
        textReason.text = recipe.reason
        textMatch.text = "재료 ${recipe.matchPercent}%"
        textCookTime.text = recipe.cookTime
        textDifficulty.text = recipe.difficulty

        textIngredients.text = recipe.ingredients.joinToString("\n") {
            "• $it"
        }

        textSteps.text = recipe.steps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")

        btnYoutube.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=${recipe.title} 레시피")
            )

            startActivity(intent)
        }

        btnShopping.setOnClickListener {
            (activity as MainActivity).openShopping()
        }

        return view
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