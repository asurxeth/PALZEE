package com.finrein.pals.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessaging

object PalAlarmScheduler {
    private const val REQUEST_CODE = 1001
    const val ACTION_PAL_ALARM = "com.finrein.pals.ACTION_HOURLY_PAL_ALARM"

    fun updateScheduling(context: Context, interval: String) {
        // 1. Permanently cancel any existing client-side AlarmManager reminders
        cancelAlarm(context)

        // 2. Sync FCM topic subscriptions
        val hasPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        try {
            val messaging = FirebaseMessaging.getInstance()
            if (interval == "off" || interval.isBlank() || !hasPermission) {
                messaging.unsubscribeFromTopic("pals_first_time")
                messaging.unsubscribeFromTopic("pals_hourly")
                messaging.unsubscribeFromTopic("pals_three_hourly")
                android.util.Log.d("PalAlarmScheduler", "FCM: Unsubscribed from all topics (notifications off/disabled)")
            } else {
                when (interval) {
                    "first time" -> {
                        messaging.subscribeToTopic("pals_first_time")
                        messaging.unsubscribeFromTopic("pals_hourly")
                        messaging.unsubscribeFromTopic("pals_three_hourly")
                        android.util.Log.d("PalAlarmScheduler", "FCM: Subscribed to pals_first_time, unsubscribed from others")
                    }
                    "every 1hr" -> {
                        messaging.subscribeToTopic("pals_hourly")
                        messaging.unsubscribeFromTopic("pals_first_time")
                        messaging.unsubscribeFromTopic("pals_three_hourly")
                        android.util.Log.d("PalAlarmScheduler", "FCM: Subscribed to pals_hourly, unsubscribed from others")
                    }
                    "every 3hrs" -> {
                        messaging.subscribeToTopic("pals_three_hourly")
                        messaging.unsubscribeFromTopic("pals_first_time")
                        messaging.unsubscribeFromTopic("pals_hourly")
                        android.util.Log.d("PalAlarmScheduler", "FCM: Subscribed to pals_three_hourly, unsubscribed from others")
                    }
                    else -> {
                        messaging.unsubscribeFromTopic("pals_first_time")
                        messaging.unsubscribeFromTopic("pals_hourly")
                        messaging.unsubscribeFromTopic("pals_three_hourly")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PalAlarmScheduler", "Failed to update FCM topic subscriptions", e)
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PalNotificationReceiver::class.java).apply {
            action = ACTION_PAL_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            android.util.Log.d("PalAlarmScheduler", "Cancelled existing client-side AlarmManager alarm")
        }
    }
}

