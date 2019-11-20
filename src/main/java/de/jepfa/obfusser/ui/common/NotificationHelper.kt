package de.jepfa.obfusser.ui.common

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    val CHANNEL_ID_BACKUP = "de.jepfa.notificationchannel.backup"
    val NOTIFICATION_ID_BACKUP_SUCCESS = 1001

    fun createNotificationChannel(context: Context, channelId: String, name: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}
