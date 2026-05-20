package com.example.swtermproject.ui.notification

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
        val btnRead: Button = view.findViewById(R.id.btnReadNotification)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteNotification)
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

        holder.title.text = item.title
        holder.message.text = item.message
        holder.time.text = if (item.isRead) "읽음" else item.time

        holder.icon.text = when {
            item.title.contains("유통기한") -> "⏰"
            item.title.contains("부족") || item.title.contains("재고") -> "⚠️"
            else -> "✅"
        }

        if (item.isRead) {
            holder.root.alpha = 0.55f
            holder.title.setTextColor(Color.parseColor("#999999"))
            holder.time.setTextColor(Color.parseColor("#999999"))
            holder.btnRead.text = "읽음 완료"
        } else {
            holder.root.alpha = 1f
            holder.title.setTextColor(Color.parseColor("#333333"))
            holder.time.setTextColor(Color.parseColor("#FF6B9D"))
            holder.btnRead.text = "읽음"
        }

        holder.root.setOnClickListener {
            item.isRead = true
            notifyItemChanged(holder.adapterPosition)
            onItemClick(item)
        }

        holder.btnRead.setOnClickListener {
            item.isRead = true
            notifyItemChanged(holder.adapterPosition)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(holder.adapterPosition)
        }

        holder.itemView.alpha = 0f
        holder.itemView.translationX = 40f
        holder.itemView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .start()
    }
}