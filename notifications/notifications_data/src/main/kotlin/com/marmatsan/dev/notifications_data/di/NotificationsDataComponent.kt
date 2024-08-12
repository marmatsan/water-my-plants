package com.marmatsan.dev.notifications_data.di

import com.marmatsan.dev.notifications_data.repository.NotificationsChannelRepositoryImpl
import com.marmatsan.dev.notifications_domain.repository.NotificationsChannelRepository
import me.tatarka.inject.annotations.Provides

interface NotificationsDataComponent {
    @Provides
    fun NotificationsChannelRepositoryImpl.bind(): NotificationsChannelRepository = this
}