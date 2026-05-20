package com.example.swtermproject.ui.notification

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
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.NotificationItem
import com.example.swtermproject.ui.ingredient.AIngredientListFragment

class ANotificationFragment : Fragment() {

    private lateinit var summary: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ANotificationAdapter

    private val notifications = mutableListOf<NotificationItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_notification,
            container,
            false
        )

        summary = view.findViewById(R.id.textNotificationSummary)
        recyclerView = view.findViewById(R.id.recyclerNotification)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        makeNotifications()

        adapter = ANotificationAdapter(
            notifications,
            { item ->
                handleNotificationClick(item)
            },
            { position ->
                deleteNotification(position)
            }
        )

        recyclerView.adapter = adapter

        updateSummary()

        return view
    }

    private fun makeNotifications() {
        notifications.clear()

        ATempIngredientStore.ingredients.forEach { ingredient ->
            if (ingredient.percent <= 20) {
                notifications.add(
                    NotificationItem(
                        title = "${ingredient.name} 재고 부족",
                        message = "현재 ${ingredient.percent}% 남아있어요. 재구매를 추천해요.",
                        time = "방금 전"
                    )
                )
            }

            if (ingredient.expireDay <= 3) {
                notifications.add(
                    NotificationItem(
                        title = "${ingredient.name} 유통기한 임박",
                        message = "D-${ingredient.expireDay} 남았어요. 빨리 소비하는 걸 추천해요.",
                        time = "방금 전"
                    )
                )
            }
        }

        if (notifications.isEmpty()) {
            notifications.add(
                NotificationItem(
                    title = "냉장고 상태 안정",
                    message = "현재 부족하거나 유통기한이 임박한 재료가 없어요.",
                    time = "현재",
                    isRead = true
                )
            )
        }
    }

    private fun handleNotificationClick(item: NotificationItem) {
        when {
            item.title.contains("재고") || item.title.contains("부족") -> {
                (activity as MainActivity).openShopping()
            }

            item.title.contains("유통기한") -> {
                (activity as MainActivity).openIngredientListWithFilter(
                    AIngredientListFragment.FILTER_EXPIRE
                )
            }

            else -> {
                (activity as MainActivity).openIngredientList()
            }
        }
    }

    private fun deleteNotification(position: Int) {
        if (position !in notifications.indices) return

        notifications.removeAt(position)
        adapter.notifyItemRemoved(position)
        updateSummary()
    }

    private fun updateSummary() {
        val unreadCount = notifications.count { !it.isRead }

        summary.text = if (notifications.isEmpty()) {
            "알림이 없습니다"
        } else {
            "총 ${notifications.size}개 · 안 읽은 알림 ${unreadCount}개"
        }
    }
}