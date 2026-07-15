package com.finrein.pals.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.finrein.pals.PalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BackgroundSyncService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        // Silent background sync loop
        serviceScope.launch {
            while (true) {
                try {
                    val supabase = PalApplication.supabase
                    val sessionManager = com.finrein.pals.core.data.local.SessionManager(applicationContext)
                    val currentUser = sessionManager.getUser()
                    if (currentUser != null) {
                        val currentUserId = currentUser.id
                        // Periodic background check/sync here if needed
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY 
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
