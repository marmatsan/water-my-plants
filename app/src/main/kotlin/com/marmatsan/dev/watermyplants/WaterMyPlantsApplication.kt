package com.marmatsan.dev.watermyplants

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import com.marmatsan.dev.notifications_domain.usecase.CreateNotificationsChannelUseCaseParameters
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.di.ApplicationComponentProvider
import com.marmatsan.dev.watermyplants.di.create
import com.marmatsan.dev.watermyplants.notifications.NotificationsChannelService

class WaterMyPlantsApplication : Application(), ApplicationComponentProvider {
    override val component by lazy(LazyThreadSafetyMode.NONE) {
        ApplicationComponent::class.create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationsChannel()
    }

    private fun createNotificationsChannel() {
        val notificationsChannel = component
            .notificationsDomainUseCases
            .createNotificationsChannelUseCase(
                CreateNotificationsChannelUseCaseParameters(
                    id = NotificationsChannelService.NOTIFICATIONS_CHANNEL_ID,
                    name = "Watering notifications",
                    importance = NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        notificationsChannel.description =
            "Notifies you about the plants need to water in the near future"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationsChannel)
    }
}