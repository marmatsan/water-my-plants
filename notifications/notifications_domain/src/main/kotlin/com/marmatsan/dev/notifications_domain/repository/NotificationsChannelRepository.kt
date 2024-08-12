package com.marmatsan.dev.notifications_domain.repository

import android.app.NotificationChannel

interface NotificationsChannelRepository {
    fun createNotificationsChannel(
        id: String,
        name: String,
        importance: Int
    ): NotificationChannel
}