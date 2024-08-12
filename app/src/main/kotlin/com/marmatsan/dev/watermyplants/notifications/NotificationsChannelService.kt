package com.marmatsan.dev.watermyplants.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.marmatsan.dev.notifications_domain.repository.NotificationsManagerRepository
import com.marmatsan.dev.watermyplants.MainActivity

class NotificationsChannelService(
    private val context: Context
) : NotificationsManagerRepository {
    companion object {
        const val NOTIFICATIONS_CHANNEL_ID = "water_plants_notification_channel"
    }

    override fun showNotification(
        smallIconResId: Int,
        contentTitle: String,
        contentText: String
    ) {
        val activityIntent = Intent(
            context,
            MainActivity::class.java
        )
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            context,
            NOTIFICATIONS_CHANNEL_ID
        ).apply {
            setSmallIcon(smallIconResId)
            setContentTitle(contentTitle)
            setContentText(contentText)
            setContentIntent(activityPendingIntent)
        }

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_CHANNEL_ID, notification)
    }
}