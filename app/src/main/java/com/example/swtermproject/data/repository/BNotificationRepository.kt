package com.example.swtermproject.data.repository

import com.example.swtermproject.data.local.dao.BNotificationDao
import com.example.swtermproject.data.local.entity.BNotificationEntity
import com.example.swtermproject.domain.model.BAppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BNotificationRepository(
    private val notificationDao: BNotificationDao
) {
    fun observeNotifications(): Flow<List<BAppNotification>> {
        return notificationDao.observeNotifications().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addNotification(title: String, message: String): Long {
        return notificationDao.insertNotification(
            BNotificationEntity(
                title = title,
                message = message
            )
        )
    }

    suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearAll() {
        notificationDao.clearAll()
    }
}

fun BNotificationEntity.toDomain(): BAppNotification {
    return BAppNotification(
        id = id,
        title = title,
        message = message,
        createdAt = createdAt,
        isRead = isRead
    )
}
