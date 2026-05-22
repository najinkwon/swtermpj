package com.example.swtermproject.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.RecipeDummyStore

class ARecipeListFragment : Fragment() {

    private var category: String = "한식"

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

        val title = view.findViewById<TextView>(R.id.textRecipeListTitle)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerRecipe)

        title.text = "$category 추천 레시피"

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = RecipeAdapter(RecipeDummyStore.recipes) { recipe ->
            (activity as MainActivity).openRecipeDetail(recipe.title)
        }

        return view
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