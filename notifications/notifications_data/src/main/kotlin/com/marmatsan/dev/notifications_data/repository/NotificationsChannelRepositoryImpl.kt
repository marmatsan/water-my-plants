package com.marmatsan.dev.notifications_data.repository

import android.app.NotificationChannel
import com.marmatsan.dev.notifications_domain.repository.NotificationsChannelRepository
import me.tatarka.inject.annotations.Inject

@Inject
class NotificationsChannelRepositoryImpl : NotificationsChannelRepository {
    override fun createNotificationsChannel(
        id: String,
        name: String,
        importance: Int
    ) = NotificationChannel(
        id,
        name,
        importance
    )
}