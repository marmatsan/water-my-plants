package com.marmatsan.dev.watermyplants.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.marmatsan.app.R
import com.marmatsan.dev.watermyplants.MainActivity

class NotificationsChannelService(
    private val context: Context
) : NotificationsManager {
    companion object {
        const val NOTIFICATIONS_CHANNEL_ID = "water_plants_notification_channel"
    }

    override fun showNotification() {
        val activityIntent = Intent(context, MainActivity::class.java)
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
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("Test notification")
            setContentIntent(activityPendingIntent)
        }
    }
}