package com.marmatsan.dev.notifications_domain.usecase

import android.app.NotificationChannel
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase
import com.marmatsan.dev.notifications_domain.repository.NotificationsRepository

class CreateNotificationsChannelUseCase(
    private val repository: NotificationsRepository
) : NonSuspendingUseCase<CreateNotificationsChannelUseCaseParameters, NotificationChannel>() {
    override fun invoke(
        input: CreateNotificationsChannelUseCaseParameters
    ) = repository.createNotificationsChannel(
        id = input.id,
        name = input.name,
        importance = input.importance
    )
}

data class CreateNotificationsChannelUseCaseParameters(
    val id: String,
    val name: String,
    val importance: Int
)