package com.finrein.pals.push

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging

object PalAlarmScheduler {
    
    const val ACTION_PAL_ALARM = "com.finrein.pals.ACTION_HOURLY_PAL_ALARM"

    fun updateScheduling(context: Context, interval: String) {
        // Cancel any legacy local AlarmManager alarms first
        try {
            cancelAlarm(context)
        } catch (e: Exception) {
            // ignore
        }

        val messaging = FirebaseMessaging.getInstance()

        // Unsubscribe from all topics first to clean up state
        try {
            messaging.unsubscribeFromTopic("pals_first_time")
            messaging.unsubscribeFromTopic("pals_hourly")
            messaging.unsubscribeFromTopic("pals_three_hourly")
            android.util.Log.d("PalAlarmScheduler", "Unsubscribed from FCM topics")
        } catch (e: Exception) {
            android.util.Log.e("PalAlarmScheduler", "Error unsubscribing: ${e.message}")
        }

        if (interval == "off" || interval.isBlank()) {
            return
        }

        try {
            if (interval == "every 1hr") {
                messaging.subscribeToTopic("pals_hourly")
                messaging.subscribeToTopic("pals_first_time")
                android.util.Log.d("PalAlarmScheduler", "Subscribed to pals_hourly and pals_first_time FCM topics")
            } else if (interval == "every 3hrs") {
                messaging.subscribeToTopic("pals_three_hourly")
                messaging.subscribeToTopic("pals_first_time")
                android.util.Log.d("PalAlarmScheduler", "Subscribed to pals_three_hourly and pals_first_time FCM topics")
            }
        } catch (e: Exception) {
            android.util.Log.e("PalAlarmScheduler", "Error subscribing to FCM topics: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context) {
        // Stub: cancel actual AlarmManager alarm if it was scheduled previously
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent().apply {
                component = android.content.ComponentName(context, "com.finrein.pals.push.PalNotificationReceiver")
                action = ACTION_PAL_ALARM
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                android.util.Log.d("PalAlarmScheduler", "Cancelled existing client-side AlarmManager alarm")
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun scheduleBootAlarm(context: Context) {
        // No-op for server-side notification model
    }
}


