package com.example.swtermproject.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.R
import com.example.swtermproject.data.model.NotificationItem

class ANotificationAdapter(
    private val notificationList: MutableList<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ANotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: LinearLayout = view.findViewById(R.id.notificationRoot)
        val icon: TextView = view.findViewById(R.id.textNotificationIcon)
        val title: TextView = view.findViewById(R.id.textNotificationTitle)
        val message: TextView = view.findViewById(R.id.textNotificationMessage)
        val time: TextView = view.findViewById(R.id.textNotificationTime)
        val btnDelete: TextView = view.findViewById(R.id.btnDeleteNotification)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notificationList.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = notificationList[position]
        val context = holder.itemView.context

        val textMain = ContextCompat.getColor(context, R.color.text_main)
        val textSub = ContextCompat.getColor(context, R.color.text_sub)
        val textHint = ContextCompat.getColor(context, R.color.text_hint)
        val primaryDark = ContextCompat.getColor(context, R.color.primary_green_dark)

        holder.title.text = item.title
        holder.message.text = item.message
        holder.time.text = if (item.isRead) "읽음" else item.time

        holder.icon.text = when {
            item.title.contains("유통기한") -> "⏰"
            item.title.contains("부족") || item.title.contains("재고") -> "⚠️"
            else -> "✅"
        }

        val isEmptyState = item.title == "냉장고 상태가 좋아요"

        holder.btnDelete.visibility =
            if (isEmptyState) View.GONE else View.VISIBLE

        if (item.isRead) {
            holder.root.alpha = 0.72f
            holder.title.setTextColor(textSub)
            holder.message.setTextColor(textHint)
            holder.time.setTextColor(textHint)
        } else {
            holder.root.alpha = 1f
            holder.title.setTextColor(textMain)
            holder.message.setTextColor(textSub)
            holder.time.setTextColor(primaryDark)
        }

        holder.root.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            item.isRead = true
            notifyItemChanged(adapterPosition)
            onItemClick(item)
        }

        holder.btnDelete.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            onDeleteClick(adapterPosition)
        }

        holder.itemView.alpha = 0f
        holder.itemView.translationY = 24f

        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(240)
            .start()
    }
}
