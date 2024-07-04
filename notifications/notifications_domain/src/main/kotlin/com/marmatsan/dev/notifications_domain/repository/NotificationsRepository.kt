package com.marmatsan.dev.notifications_domain.repository

import android.app.NotificationChannel

interface NotificationsRepository {
    fun createNotificationsChannel(
        id: String,
        name: String,
        importance: Int
    ): NotificationChannel
}