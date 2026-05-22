package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BNotificationRepository
import com.example.swtermproject.domain.model.BAppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BNotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BNotificationRepository(
        BAppDatabase.getDatabase(application).notificationDao()
    )

    private val _notifications = MutableStateFlow<List<BAppNotification>>(emptyList())
    val notifications: StateFlow<List<BAppNotification>> = _notifications

    init {
        viewModelScope.launch {
            repository.observeNotifications().collect {
                _notifications.value = it
            }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
