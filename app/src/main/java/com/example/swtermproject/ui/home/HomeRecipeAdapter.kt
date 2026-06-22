package com.example.swtermproject.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.Recipe

class HomeRecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<HomeRecipeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.textHomeRecipeEmoji)
        val title: TextView = view.findViewById(R.id.textHomeRecipeTitle)
        val reason: TextView = view.findViewById(R.id.textHomeRecipeReason)
        val match: TextView = view.findViewById(R.id.textHomeRecipeMatch)
        val time: TextView = view.findViewById(R.id.textHomeRecipeTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_recipe, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int =
        recipeList.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val recipe = recipeList[position]

        holder.emoji.text = recipe.emoji
        holder.title.text = recipe.title
        holder.reason.text =
            if (recipe.reason.startsWith("부족 재료:")) {
                recipe.reason
                    .replace("부족 재료:", "부족:")
                    .take(22)
            } else {
                "바로 만들기 좋아요"
            }

        holder.match.text = "재료 ${recipe.matchPercent}%"
        holder.time.text = recipe.cookTime

        holder.itemView.setOnClickListener {
            onClick(recipe)
        }

        holder.itemView.alpha = 0f
        holder.itemView.translationY = 16f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180)
            .start()
    }
}
