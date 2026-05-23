package com.example.swtermproject.ui.ingredient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.domain.model.BIngredient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AIngredientAdapter(
    private val ingredientList: MutableList<BIngredient>,
    private val activity: FragmentActivity,
    private val onDeleteClick: (Int) -> Unit,
    private val onAmountChanged: (ingredientId: Long, currentAmount: Double) -> Unit,
    private val onIngredientUpdated: (BIngredient) -> Unit,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<AIngredientAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textIcon: TextView = view.findViewById(R.id.textIngredientIcon)
        val textName: TextView = view.findViewById(R.id.textIngredientName)
        val textCategory: TextView = view.findViewById(R.id.textIngredientCategory)
        val textPercent: TextView = view.findViewById(R.id.textIngredientPercent)
        val textExpireDay: TextView = view.findViewById(R.id.textExpireDay)
        val btnDelete: TextView = view.findViewById(R.id.btnDeleteIngredient)
        val btnFavorite: TextView = view.findViewById(R.id.btnFavorite)
        val textFavorite: TextView = view.findViewById(R.id.textFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredientList[position]
        val context = holder.itemView.context

        val percent = ingredient.stockPercent.coerceIn(0, 100)
        val expireDay = calculateExpireDay(ingredient.expiryDate)

        holder.textName.text = ingredient.name
        holder.textCategory.text = ingredient.category
        holder.textPercent.text = "$percent%"
        holder.textExpireDay.text = "D-$expireDay"

        val warningColor = ContextCompat.getColor(context, R.color.accent_red)
        val safeColor = ContextCompat.getColor(context, R.color.primary_green_dark)

        holder.textExpireDay.setTextColor(
            if (expireDay <= 3) warningColor else safeColor
        )

        holder.textIcon.text = when (ingredient.category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🍽️"
        }

        holder.textPercent.setTextColor(
            if (percent <= 20) warningColor else safeColor
        )

        holder.textFavorite.visibility =
            if (ingredient.favorite) View.VISIBLE else View.GONE

        holder.btnFavorite.text =
            if (ingredient.favorite) "★" else "☆"

        holder.btnFavorite.setOnClickListener {
            val updated = ingredient.copy(favorite = !ingredient.favorite)
            onIngredientUpdated(updated)
            onDataChanged()
        }

        holder.itemView.setOnClickListener {
            AIngredientDetailBottomSheet(
                ingredient = ingredient,
                expireDay = expireDay,
                onAmountChanged = { newCurrentAmount ->
                    onAmountChanged(ingredient.id, newCurrentAmount)
                },
                onIngredientUpdated = { updatedIngredient ->
                    onIngredientUpdated(updatedIngredient)
                },
                onChanged = {
                    notifyDataSetChanged()
                    onDataChanged()
                }
            ).show(
                activity.supportFragmentManager,
                "ingredient_detail"
            )
        }

        holder.btnDelete.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClick(adapterPosition)
            }
        }

        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
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
}
