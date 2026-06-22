package com.example.swtermproject.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.Recipe

class RecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageEmoji: TextView = view.findViewById(R.id.textRecipeImageEmoji)
        val title: TextView = view.findViewById(R.id.textRecipeTitle)
        val reason: TextView = view.findViewById(R.id.textRecipeReason)
        val match: TextView = view.findViewById(R.id.textRecipeMatch)
        val time: TextView = view.findViewById(R.id.textRecipeTime)
        val difficulty: TextView = view.findViewById(R.id.textRecipeDifficulty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.imageEmoji.text = recipe.emoji
        holder.title.text = recipe.title
        holder.reason.text = recipe.reason
        holder.match.text = "재료 ${recipe.matchPercent}%"
        holder.time.text = recipe.cookTime
        holder.difficulty.text = recipe.difficulty

        holder.itemView.setOnClickListener {
            onClick(recipe)
        }
    }
}