package com.marmatsan.dev.notifications_domain.di

import com.marmatsan.dev.notifications_domain.repository.NotificationsChannelRepository
import com.marmatsan.dev.notifications_domain.usecase.CreateNotificationsChannelUseCase
import com.marmatsan.dev.notifications_domain.usecase.NotificationsDomainUseCases
import me.tatarka.inject.annotations.Provides

interface NotificationsDomainComponent {
    @Provides
    fun provideNotificationsDomainUseCases(
        repository: NotificationsChannelRepository
    ): NotificationsDomainUseCases = NotificationsDomainUseCases(
        createNotificationsChannelUseCase = CreateNotificationsChannelUseCase(repository)
    )
}