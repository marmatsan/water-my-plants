package com.marmatsan.dev.notifications_domain.repository

interface NotificationsManagerRepository {
    fun showNotification(
        smallIconResId : Int,
        contentTitle: String,
        contentText : String
    )
}