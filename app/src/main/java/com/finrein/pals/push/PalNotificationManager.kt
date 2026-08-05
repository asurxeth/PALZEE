package com.finrein.pals.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.finrein.pals.MainActivity
import com.finrein.pals.utils.NotificationHelper

object PalNotificationManager {

    private const val GROUP_CHANNEL_ID = "palzee_group_notifications"
    private const val GROUP_CHANNEL_NAME = "Pals Group Notifications"

    private const val HOURLY_CHANNEL_ID = "palzee_hourly_reminders"
    private const val HOURLY_CHANNEL_NAME = "Hourly Pal Reminders"

    /**
     * Preserved original hourly pal reminder notification (channel: palzee_hourly_reminders, ID: 1002)
     */
    fun showHourlyNotification(context: Context, title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotificationBuilder = NotificationHelper.getBaseBuilder(context, HOURLY_CHANNEL_ID, HOURLY_CHANNEL_NAME)
            .setContentTitle("Palzee")
            .setContentText("Notification")

        val notificationBuilder = NotificationHelper.getBaseBuilder(context, HOURLY_CHANNEL_ID, HOURLY_CHANNEL_NAME)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotificationBuilder.build())
            .setContentIntent(pendingIntent)

        notificationManager.notify(1002, notificationBuilder.build())
    }

    /**
     * Shows notification when a user joins a pals group:
     * Title: <pals_group_name>
     * Body: <user_name> joined <pals_group_name>
     */
    fun showGroupJoinNotification(
        context: Context,
        userName: String,
        groupName: String
    ) {
        val title = groupName
        val body = "$userName joined $groupName"
        showGroupNotification(context, title, body, groupName)
    }

    /**
     * Shows notification when a user sends a pal in a pals group:
     * Title: <person_name>
     * Body: new pal
     */
    fun showNewPalNotification(
        context: Context,
        personName: String,
        groupName: String = "Pals"
    ) {
        val title = personName
        val body = "new pal"
        showGroupNotification(context, title, body, groupName)
    }

    fun showGroupNotification(
        context: Context,
        title: String,
        body: String,
        groupName: String = "Pals"
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val groupKey = "com.finrein.pals.NOTIFICATION_GROUP_$groupName"
        val notificationId = ((System.currentTimeMillis() % 1000000).toInt() + (0..1000).random())

        val publicNotificationBuilder = NotificationHelper.getBaseBuilder(context, GROUP_CHANNEL_ID, GROUP_CHANNEL_NAME)
            .setContentTitle(title)
            .setContentText(body)
            .setGroup(groupKey)

        val notificationBuilder = NotificationHelper.getBaseBuilder(context, GROUP_CHANNEL_ID, GROUP_CHANNEL_NAME)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotificationBuilder.build())
            .setGroup(groupKey)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
