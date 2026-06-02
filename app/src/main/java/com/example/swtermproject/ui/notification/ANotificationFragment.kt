package com.example.swtermproject.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.NotificationItem
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.ui.ingredient.AIngredientListFragment
import com.example.swtermproject.viewmodel.BIngredientViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ANotificationFragment : Fragment() {

    private val viewModel: BIngredientViewModel by viewModels()

    private lateinit var summary: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ANotificationAdapter
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnMarkAllRead: Button
    private lateinit var btnResetDeleted: Button

    private val notifications = mutableListOf<NotificationItem>()
    private val deletedNotificationKeys = mutableSetOf<String>()
    private val readNotificationKeys = mutableSetOf<String>()

    private var latestIngredients: List<BIngredient> = emptyList()

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
        layoutActions = view.findViewById(R.id.layoutNotificationActions)
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead)
        btnResetDeleted = view.findViewById(R.id.btnResetDeletedNotifications)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }

        btnResetDeleted.setOnClickListener {
            deletedNotificationKeys.clear()
            makeNotifications(latestIngredients)
            adapter.notifyDataSetChanged()
            updateSummary()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIngredients()
    }

    private fun observeIngredients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ingredients.collect { ingredients ->
                    latestIngredients = ingredients
                    makeNotifications(ingredients)
                    adapter.notifyDataSetChanged()
                    updateSummary()
                }
            }
        }
    }

    private fun makeNotifications(ingredients: List<BIngredient>) {
        notifications.clear()

        ingredients.forEach { ingredient ->
            val stockPercent = ingredient.stockPercent.coerceIn(0, 100)
            val expireDay = calculateExpireDay(ingredient.expiryDate)

            val lowStockKey = notificationKey(
                type = "low_stock",
                ingredient = ingredient
            )

            val expireKey = notificationKey(
                type = "expire",
                ingredient = ingredient
            )

            val lowStockTitle = "${ingredient.name} 재고 부족"
            val expireTitle = "${ingredient.name} 유통기한 임박"

            if (stockPercent <= 20 && !deletedNotificationKeys.contains(lowStockKey)) {
                notifications.add(
                    NotificationItem(
                        title = lowStockTitle,
                        message = "현재 ${stockPercent}% 남아있어요. 재구매를 추천해요.",
                        time = "방금 전",
                        isRead = readNotificationKeys.contains(lowStockKey)
                    )
                )
            }

            if (
                ingredient.expiryDate.isNotBlank() &&
                expireDay <= 3 &&
                !deletedNotificationKeys.contains(expireKey)
            ) {
                notifications.add(
                    NotificationItem(
                        title = expireTitle,
                        message = "D-${expireDay} 남았어요. 빨리 소비하는 걸 추천해요.",
                        time = "방금 전",
                        isRead = readNotificationKeys.contains(expireKey)
                    )
                )
            }
        }

        if (notifications.isEmpty()) {
            notifications.add(
                NotificationItem(
                    title = "냉장고 상태가 좋아요",
                    message = "현재 부족하거나 유통기한이 임박한 재료가 없어요.",
                    time = "현재",
                    isRead = true
                )
            )
        }
    }

    private fun handleNotificationClick(item: NotificationItem) {
        val key = keyForItem(item)
        if (key != null) {
            readNotificationKeys.add(key)
        }

        item.isRead = true
        adapter.notifyDataSetChanged()
        updateSummary()

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

        val item = notifications[position]
        val key = keyForItem(item) ?: return

        deletedNotificationKeys.add(key)

        notifications.removeAt(position)
        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun markAllAsRead() {
        notifications.forEach { item ->
            val key = keyForItem(item)
            if (key != null) {
                readNotificationKeys.add(key)
            }

            item.isRead = true
        }

        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun updateSummary() {
        val realNotifications =
            notifications.filter { it.title != "냉장고 상태가 좋아요" }

        val unreadCount = realNotifications.count { !it.isRead }

        layoutActions.visibility =
            if (realNotifications.isEmpty()) View.GONE else View.VISIBLE

        summary.text =
            when {
                realNotifications.isEmpty() -> {
                    "알림이 없습니다"
                }

                unreadCount == 0 -> {
                    "총 ${realNotifications.size}개 · 모두 읽음"
                }

                else -> {
                    "총 ${realNotifications.size}개 · 안 읽은 알림 ${unreadCount}개"
                }
            }
    }

    private fun keyForItem(item: NotificationItem): String? {
        val ingredientName = item.title
            .replace(" 재고 부족", "")
            .replace(" 유통기한 임박", "")

        return when {
            item.title.contains("재고") || item.title.contains("부족") -> {
                "low_stock:$ingredientName"
            }

            item.title.contains("유통기한") -> {
                "expire:$ingredientName"
            }

            else -> null
        }
    }

    private fun notificationKey(
        type: String,
        ingredient: BIngredient
    ): String {
        return "$type:${ingredient.name}"
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
