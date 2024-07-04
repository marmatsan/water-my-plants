package com.marmatsan.dev.notifications_data.di

import com.marmatsan.dev.notifications_data.repository.NotificationsRepositoryImpl
import com.marmatsan.dev.notifications_domain.repository.NotificationsRepository
import me.tatarka.inject.annotations.Provides

interface NotificationsDataComponent {
    @Provides
    fun NotificationsRepositoryImpl.bind(): NotificationsRepository = this
}