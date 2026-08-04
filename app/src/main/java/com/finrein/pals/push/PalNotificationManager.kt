package com.finrein.pals.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.finrein.pals.MainActivity
import com.finrein.pals.R

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HOURLY_CHANNEL_ID,
                HOURLY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to capture your pal"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.pal_circular_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification)
        } catch (e: Exception) {
            null
        }

        val publicNotificationBuilder = NotificationCompat.Builder(context, HOURLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Palzee")
            .setContentText("Notification")
            .setAutoCancel(true)

        if (largeIconBitmap != null) {
            publicNotificationBuilder.setLargeIcon(largeIconBitmap)
        }

        val notificationBuilder = NotificationCompat.Builder(context, HOURLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotificationBuilder.build())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }

        notificationManager.notify(1002, notificationBuilder.build())
    }

    /**
     * Shows notification when a user joins a pals group (Image 1 style):
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
     * Shows notification when a user sends a pal in a pals group (Image 2 style):
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GROUP_CHANNEL_ID,
                GROUP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for group activity and new pals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.pal_circular_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification)
        } catch (e: Exception) {
            null
        }

        val groupKey = "com.finrein.pals.NOTIFICATION_GROUP_$groupName"
        val notificationId = ((System.currentTimeMillis() % 1000000).toInt() + (0..1000).random())

        val publicNotificationBuilder = NotificationCompat.Builder(context, GROUP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setGroup(groupKey)
            .setAutoCancel(true)

        if (largeIconBitmap != null) {
            publicNotificationBuilder.setLargeIcon(largeIconBitmap)
        }

        val notificationBuilder = NotificationCompat.Builder(context, GROUP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotificationBuilder.build())
            .setGroup(groupKey)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
