package com.finrein.pals.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.finrein.pals.data.local.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PalAlarmScheduler {
    private const val REQUEST_CODE = 1001
    private const val BOOT_REQUEST_CODE = 1002
    
    const val ACTION_PAL_ALARM = "com.finrein.pals.ACTION_HOURLY_PAL_ALARM"

    fun updateScheduling(context: Context, interval: String) {
        cancelAlarm(context)

        // Make sure we unsubscribe from FCM pub-sub topics since we handle notifications locally now
        try {
            val messaging = FirebaseMessaging.getInstance()
            messaging.unsubscribeFromTopic("pals_first_time")
            messaging.unsubscribeFromTopic("pals_hourly")
            messaging.unsubscribeFromTopic("pals_three_hourly")
        } catch (e: Exception) {
            // ignore
        }

        if (interval == "off" || interval.isBlank()) {
            return
        }

        val sharedPrefs = context.getSharedPreferences("palzee_prefs", Context.MODE_PRIVATE)
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var hasLoggedAnyToday = false
        for (h in 0..23) {
            if (sharedPrefs.getBoolean("pal_logged_${dateStamp}_$h", false)) {
                hasLoggedAnyToday = true
                break
            }
        }
        val firstNotifiedKey = "first_pal_notified_$dateStamp"
        val hasFirstPalOccurred = sharedPrefs.getBoolean(firstNotifiedKey, false) || hasLoggedAnyToday

        // If today's first pal hasn't been triggered yet, do not schedule subsequent hourly alarms.
        // They will be scheduled once the first pal notification is delivered.
        if (!hasFirstPalOccurred) {
            return
        }

        // Schedule next subsequent alarm
        if (interval == "every 1hr" || interval == "every 3hrs") {
            val intervalMs = if (interval == "every 1hr") 60 * 60 * 1000L else 3 * 60 * 60 * 1000L
            val lastSent = sharedPrefs.getLong("last_notification_sent_time", 0L)
            
            val nextTrigger = if (lastSent == 0L) {
                System.currentTimeMillis() + intervalMs
            } else {
                lastSent + intervalMs
            }
            
            // Fallback: if nextTrigger is in the past, schedule for 10 seconds from now
            val finalTrigger = if (nextTrigger < System.currentTimeMillis()) {
                System.currentTimeMillis() + 10 * 1000L
            } else {
                nextTrigger
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PalNotificationReceiver::class.java).apply {
                action = ACTION_PAL_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTrigger, pendingIntent)
            }
            android.util.Log.d("PalAlarmScheduler", "Scheduled next local alarm at: $finalTrigger")
        }
    }

    fun scheduleBootAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PalNotificationReceiver::class.java).apply {
            action = "com.finrein.pals.ACTION_BOOT_15MIN_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BOOT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + 15 * 60 * 1000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        android.util.Log.d("PalAlarmScheduler", "Scheduled 15-minute boot alarm")
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

