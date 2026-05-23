package com.example.swtermproject.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swtermproject.R
import com.example.swtermproject.ui.recipe.ARecipeDetailFragment

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragment)

        val recipeName = intent.getStringExtra(EXTRA_RECIPE_NAME)
            ?: "계란볶음밥"

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.singleFragmentContainer,
                    ARecipeDetailFragment.newInstance(recipeName)
                )
                .commit()
        }
    }

    companion object {
        const val EXTRA_RECIPE_NAME = "extra_recipe_name"
    }
}
