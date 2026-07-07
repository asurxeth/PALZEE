package com.finrein.pals.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.finrein.pals.MainActivity
import com.finrein.pals.data.local.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionManager = SessionManager(context.applicationContext)
        val interval = sessionManager.getNotificationInterval()

        if (interval == "off" || interval.isBlank()) {
            PalAlarmScheduler.cancelAlarm(context)
            return
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val scheduledHour = intent.getIntExtra("EXTRA_SCHEDULED_HOUR", -1)
        val hourToUse = if (scheduledHour in 0..23) scheduledHour else currentHour

        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                // User just unlocked their phone!
                if (!isPalSentOrNotifiedForHour(context, hourToUse)) {
                    if (isAfterTargetTimeForHour(hourToUse)) {
                        showNativeNotification(context, hourToUse)
                        markAsNotifiedForHour(context, hourToUse)
                    }
                }
            }

            PalAlarmScheduler.ACTION_PAL_ALARM, Intent.ACTION_BOOT_COMPLETED -> {
                // Top of the hour fallback check / System Reboot
                if (intent.action == PalAlarmScheduler.ACTION_PAL_ALARM) {
                    if (!isPalSentOrNotifiedForHour(context, hourToUse)) {
                        showNativeNotification(context, hourToUse)
                        markAsNotifiedForHour(context, hourToUse)
                    }
                }
                // Schedule the next hourly fallback window
                PalAlarmScheduler.updateScheduling(context, interval)
            }
        }
    }

    private fun isAfterTargetTimeForHour(hour: Int): Boolean {
        val relativeHour = (hour - 4 + 24) % 24
        val totalSeconds = relativeHour * 150
        val targetMinute = totalSeconds / 60
        val targetSecond = totalSeconds % 60
        
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, targetSecond)
            set(Calendar.MILLISECOND, 0)
        }
        return System.currentTimeMillis() >= targetCal.timeInMillis
    }

    private fun isPalSentOrNotifiedForHour(context: Context, hour: Int): Boolean {
        val sharedPrefs = context.getSharedPreferences("palzee_prefs", Context.MODE_PRIVATE)
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val palSentKey = "pal_logged_${dateStamp}_$hour"
        val alertSentKey = "pal_notified_${dateStamp}_$hour"
        
        val alreadySentPal = sharedPrefs.getBoolean(palSentKey, false)
        val alreadyNotified = sharedPrefs.getBoolean(alertSentKey, false)
        
        return alreadySentPal || alreadyNotified
    }

    private fun markAsNotifiedForHour(context: Context, hour: Int) {
        val sharedPrefs = context.getSharedPreferences("palzee_prefs", Context.MODE_PRIVATE)
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val alertSentKey = "pal_notified_${dateStamp}_$hour"
        
        sharedPrefs.edit().putBoolean(alertSentKey, true).apply()
    }

    private fun showNativeNotification(context: Context, hour: Int) {
        val channelId = "palzee_hourly_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hourly Pal Reminders",
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

        val timeString = String.format(Locale.getDefault(), "%02d:00", hour)

        val publicNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.finrein.pals.R.drawable.pal_logo)
            .setContentTitle("Palzee")
            .setContentText("Notification")
            .setAutoCancel(true)
            .build()

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.finrein.pals.R.drawable.pal_logo)
            .setContentTitle("Time for your $timeString pal")
            .setContentText("Capture this hour before it passes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1002, notificationBuilder.build())
    }
}
