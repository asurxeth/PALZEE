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
        
        val (nextAlarmTime, targetHour) = getNextAlarmTimeAndHour(interval)
        
        val intent = Intent(context, PalNotificationReceiver::class.java).apply {
            action = ACTION_PAL_ALARM
            putExtra("EXTRA_SCHEDULED_HOUR", targetHour)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
            android.util.Log.d("PalAlarmScheduler", "Scheduled next alarm at: ${java.util.Date(nextAlarmTime)} (Hour: $targetHour)")
        } catch (e: Exception) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            } catch (ex: Exception) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
        }
    }

    fun getNextAlarmTimeAndHour(interval: String): Pair<Long, Int> {
        val now = System.currentTimeMillis()
        val isThreeHours = interval == "every 3hrs"
        
        for (i in 0..48) {
            val testCal = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, i)
            }
            val testHour = testCal.get(Calendar.HOUR_OF_DAY)
            val relativeHour = (testHour - 4 + 24) % 24
            
            if (isThreeHours && (relativeHour % 3 != 0)) {
                continue
            }
            
            val totalSeconds = relativeHour * 150
            val targetMinute = totalSeconds / 60
            val targetSecond = totalSeconds % 60
            
            testCal.set(Calendar.MINUTE, targetMinute)
            testCal.set(Calendar.SECOND, targetSecond)
            testCal.set(Calendar.MILLISECOND, 0)
            
            if (testCal.timeInMillis > now) {
                return Pair(testCal.timeInMillis, testHour)
            }
        }
        
        val fallbackCal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }
        return Pair(fallbackCal.timeInMillis, fallbackCal.get(Calendar.HOUR_OF_DAY))
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
