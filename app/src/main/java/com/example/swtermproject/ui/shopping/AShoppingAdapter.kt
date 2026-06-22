package com.example.swtermproject.ui.shopping

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ShoppingItem

class AShoppingAdapter(
    private val shoppingList: List<ShoppingItem>
) : RecyclerView.Adapter<AShoppingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val emoji: TextView =
            view.findViewById(R.id.textShoppingEmoji)

        val name: TextView =
            view.findViewById(R.id.textShoppingName)

        val percent: TextView =
            view.findViewById(R.id.textShoppingPercent)

        val message: TextView =
            view.findViewById(R.id.textShoppingMessage)

        val badge: TextView =
            view.findViewById(R.id.textShoppingBadge)

        val button: Button =
            view.findViewById(R.id.btnShoppingSearch)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.item_shopping,
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int =
        shoppingList.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = shoppingList[position]
        val isRecipeMissingItem =
            item.percent == AShoppingFragment.RECIPE_MISSING_PERCENT

        holder.emoji.text = item.emoji
        holder.name.text = item.name

        holder.percent.text =
            if (isRecipeMissingItem) {
                "레시피에 필요한 재료"
            } else {
                "현재 재고 ${item.percent}%"
            }

        holder.message.text =
            if (isRecipeMissingItem) {
                "구매하면 바로 만들 수 있어요"
            } else {
                "재고가 부족해요"
            }

        if (isRecipeMissingItem) {
            holder.badge.visibility = View.GONE
        } else {
            holder.badge.visibility = View.VISIBLE
            holder.badge.text =
                if (item.percent <= 10) {
                    "매우 부족"
                } else {
                    "부족"
                }
        }

        holder.button.backgroundTintList = null
        holder.button.setBackgroundResource(R.drawable.bg_primary_button)
        holder.button.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.white
            )
        )

        holder.button.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.coupang.com/np/search?q=${Uri.encode(item.name)}"
                )
            )

            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.alpha = 0f
        holder.itemView.translationY = 18f

        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .start()
    }
}
