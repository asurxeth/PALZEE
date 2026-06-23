package com.finrein.pals

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BackgroundSyncService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Build a professional, persistent status bar notification
        val notification: Notification = NotificationCompat.Builder(this, "PAL_CHANNEL_ID")
            .setContentTitle("Pals is Active")
            .setContentText("Syncing background timelines securely...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true) // Prevents the user from swiping it away
            .build()

        // Elevates your app priority to level 1 instantly
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY tells Android to automatically restart this service if it ever runs out of memory
        return START_STICKY 
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "PAL_CHANNEL_ID",
                "Background Synchronization",
                NotificationManager.IMPORTANCE_LOW // Low importance prevents an annoying sound alert on loop
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
