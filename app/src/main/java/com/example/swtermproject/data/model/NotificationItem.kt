package com.example.swtermproject.data.model

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean = false
)