package com.example.swtermproject.ui.home

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.RecipeDummyStore
import com.example.swtermproject.ui.ingredient.AIngredientListFragment
import com.example.swtermproject.ui.recipe.RecipeAdapter

class AHomeFragment : Fragment() {

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

        val textTotal = view.findViewById<TextView>(R.id.textTotalCount)
        val textFavorite = view.findViewById<TextView>(R.id.textFavoriteCount)
        val textLowStock = view.findViewById<TextView>(R.id.textLowStockCount)
        val textExpire = view.findViewById<TextView>(R.id.textExpireCount)

        val cardTotal = view.findViewById<LinearLayout>(R.id.cardTotal)
        val cardFavorite = view.findViewById<LinearLayout>(R.id.cardFavorite)
        val cardLowStock = view.findViewById<LinearLayout>(R.id.cardLowStock)
        val cardExpire = view.findViewById<LinearLayout>(R.id.cardExpire)

        val buttonMoreRecipe = view.findViewById<TextView>(R.id.buttonMoreRecipe)
        val recyclerRecipe = view.findViewById<RecyclerView>(R.id.recyclerRecipe)

        val ingredients = ATempIngredientStore.ingredients

        animateCount(textTotal, ingredients.size)
        animateCount(textFavorite, ingredients.count { it.favorite })
        animateCount(textLowStock, ingredients.count { it.percent <= 20 })
        animateCount(textExpire, ingredients.count { it.expireDay <= 3 })

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
            (activity as MainActivity).openShopping()
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

    private fun animateCount(
        textView: TextView,
        target: Int
    ) {
        val animator = ValueAnimator.ofInt(0, target)

        animator.duration = 650

        animator.addUpdateListener {
            textView.text = it.animatedValue.toString()
        }

        animator.start()
    }
}