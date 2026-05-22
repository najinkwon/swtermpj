package com.example.swtermproject.domain.model

data class BAppNotification(
    val id: Long = 0,
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
