package com.finrein.pals.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object PalAlarmScheduler {
    private const val REQUEST_CODE = 1001
    const val ACTION_PAL_ALARM = "com.finrein.pals.ACTION_HOURLY_PAL_ALARM"

    fun updateScheduling(context: Context, interval: String) {
        if (interval == "off" || interval.isBlank()) {
            cancelAlarm(context)
        } else {
            scheduleNextAlarm(context, interval)
        }
    }

    private fun scheduleNextAlarm(context: Context, interval: String) {
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

        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 50)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            val hoursToAdd = if (interval == "every 3hrs") 3 else 1
            calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd)
        } else if (interval == "every 3hrs") {
            calendar.add(Calendar.HOUR_OF_DAY, 2)
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
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
        }
    }
}
