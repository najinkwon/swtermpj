package com.example.swtermproject.ui.ingredient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.Ingredient

class AIngredientAdapter(
    private val ingredientList: MutableList<Ingredient>,
    private val activity: FragmentActivity,
    private val onDeleteClick: (Int) -> Unit,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<AIngredientAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textIcon: TextView =
            view.findViewById(R.id.textIngredientIcon)

        val textName: TextView =
            view.findViewById(R.id.textIngredientName)

        val textCategory: TextView =
            view.findViewById(R.id.textIngredientCategory)

        val textPercent: TextView =
            view.findViewById(R.id.textIngredientPercent)

        val textExpireDay: TextView =
            view.findViewById(R.id.textExpireDay)

        val btnDelete: TextView =
            view.findViewById(R.id.btnDeleteIngredient)

        val btnFavorite: TextView =
            view.findViewById(R.id.btnFavorite)

        val textFavorite: TextView =
            view.findViewById(R.id.textFavorite)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_ingredient,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val ingredient = ingredientList[position]
        val context = holder.itemView.context

        holder.textName.text = ingredient.name
        holder.textCategory.text = ingredient.category
        holder.textPercent.text = "${ingredient.percent}%"
        holder.textExpireDay.text = "D-${ingredient.expireDay}"

        val warningColor = ContextCompat.getColor(context, R.color.accent_red)
        val safeColor = ContextCompat.getColor(context, R.color.primary_green_dark)

        holder.textExpireDay.setTextColor(
            if (ingredient.expireDay <= 3) {
                warningColor
            } else {
                safeColor
            }
        )

        holder.textIcon.text = when (ingredient.category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🍽️"
        }

        holder.textPercent.setTextColor(
            if (ingredient.percent <= 20) {
                warningColor
            } else {
                safeColor
            }
        )

        holder.textFavorite.visibility =
            if (ingredient.favorite) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.btnFavorite.text =
            if (ingredient.favorite) {
                "★"
            } else {
                "☆"
            }

        holder.btnFavorite.setOnClickListener {
            ingredient.favorite = !ingredient.favorite

            notifyItemChanged(holder.adapterPosition)
            onDataChanged()
        }

        holder.itemView.setOnClickListener {
            AIngredientDetailBottomSheet(
                ingredient.name
            ) {
                notifyDataSetChanged()
                onDataChanged()
            }.show(
                activity.supportFragmentManager,
                "ingredient_detail"
            )
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(holder.adapterPosition)
        }

        holder.itemView.alpha = 0f

        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }
}