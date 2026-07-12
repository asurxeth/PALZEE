package com.finrein.pals.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.finrein.pals.PalApplication
import com.finrein.pals.data.local.SessionManager
import com.finrein.pals.domain.model.PalDbItem
import com.finrein.pals.domain.model.SubmissionDbItem
import com.finrein.pals.presentation.home.uploadFileToSupabase
import com.finrein.pals.presentation.home.uploadPalVideoAndGetUrl
import com.finrein.pals.presentation.home.getVlogPrefs
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VlogUploadService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val caption = intent.getStringExtra("CAPTION") ?: ""
        val targetPalCodes = intent.getStringArrayExtra("TARGET_PAL_CODES") ?: emptyArray()
        val targetPalNames = intent.getStringArrayExtra("TARGET_PAL_NAMES") ?: emptyArray()
        val capturedVideoPath = intent.getStringExtra("CAPTURED_VIDEO_PATH")
        val capturedVideoDuration = intent.getLongExtra("CAPTURED_VIDEO_DURATION", 0L)
        val capturedVideoTimeText = intent.getStringExtra("CAPTURED_VIDEO_TIME_TEXT") ?: ""
        val capturedVideoInstantStr = intent.getStringExtra("CAPTURED_VIDEO_INSTANT") ?: ""
        val currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""
        val firstName = intent.getStringExtra("FIRST_NAME") ?: ""
        val customAvatarUriString = intent.getStringExtra("CUSTOM_AVATAR_URI_STRING")
        val zoomFactor = intent.getFloatExtra("ZOOM_FACTOR", 1.0f)
        val rotation = intent.getIntExtra("ROTATION", 0)

        if (!capturedVideoPath.isNullOrBlank()) {
            if (!activeUploads.add(capturedVideoPath)) {
                android.util.Log.d("VlogUploadService", "Already uploading: $capturedVideoPath, skipping duplicate onStartCommand.")
                return START_NOT_STICKY
            }
        }

        val channelId = "vlog_upload_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Uploading Vlog...")
            .setContentText("Your vlog is being uploaded securely.")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(43, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(43, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("VlogUploadService", "Failed to start foreground service: ${e.localizedMessage}")
            if (!capturedVideoPath.isNullOrBlank()) {
                activeUploads.remove(capturedVideoPath)
            }
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            try {
                val sessionManager = SessionManager(applicationContext)
                val supabaseClient = PalApplication.supabase
                
                val localVideoPath = capturedVideoPath
                var finalAvatarUrl = customAvatarUriString ?: ""

                val pairs = targetPalCodes.zip(targetPalNames)
                for ((targetPalCode, targetPalName) in pairs) {
                    val uploadedVideoUrl = if (!localVideoPath.isNullOrBlank()) {
                        if (targetPalCode == "vlog") {
                            val cleanPath = if (localVideoPath.startsWith("file://")) localVideoPath.substring(7) else localVideoPath
                            val uri = android.net.Uri.fromFile(java.io.File(cleanPath))
                            uploadPalVideoAndGetUrl(applicationContext, uri, currentUserId) ?: ""
                        } else {
                            uploadFileToSupabase(applicationContext, localVideoPath, "PALS")
                        }
                    } else {
                        ""
                    }

                    if (uploadedVideoUrl.isBlank() || !uploadedVideoUrl.startsWith("http")) {
                        android.util.Log.e("VlogUploadService", "Upload failed for code: $targetPalCode")
                        continue
                    }

                    if (uploadedVideoUrl.startsWith("http") && !localVideoPath.isNullOrBlank()) {
                        val palPrefs = applicationContext.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
                        palPrefs.edit().putString("local_path_$uploadedVideoUrl", localVideoPath).apply()

                        val vlogPrefs = getVlogPrefs(applicationContext)
                        vlogPrefs.edit().apply {
                            putString("local_path_$uploadedVideoUrl", localVideoPath)
                            putInt("rotation_$uploadedVideoUrl", rotation)
                            putInt("rotation_$localVideoPath", rotation)
                        }.apply()
                        
                        // Map the thumbnail to the remoteUrl so that the 0ms thumbnail loading keeps working when using remote URLs
                        val localThumb = vlogPrefs.getString("thumb_path_$localVideoPath", null)
                        if (localThumb != null) {
                            vlogPrefs.edit().putString("thumb_path_$uploadedVideoUrl", localThumb).apply()
                        }

                        // Atomic Handoff: Replace local file path in vlog_paths with remote URL
                        if (targetPalCode == "vlog") {
                            val savedPaths = vlogPrefs.getString("vlog_paths", "") ?: ""
                            if (savedPaths.isNotEmpty()) {
                                val pathsList = savedPaths.split(";;;").toMutableList()
                                val index = pathsList.indexOf(localVideoPath)
                                if (index != -1) {
                                    pathsList[index] = uploadedVideoUrl
                                    vlogPrefs.edit().putString("vlog_paths", pathsList.joinToString(";;;")).apply()
                                }
                            }
                        }
                    }

                    var avatarUrl = ""
                    val checkAvatar = finalAvatarUrl
                    if (checkAvatar.isNotEmpty()) {
                        if (checkAvatar.startsWith("http")) {
                            avatarUrl = checkAvatar
                        } else {
                            val uploaded = uploadFileToSupabase(applicationContext, checkAvatar, "AVATARS")
                            if (uploaded.startsWith("http")) {
                                avatarUrl = uploaded
                                sessionManager.saveAvatarUri(uploaded)
                                finalAvatarUrl = uploaded
                            }
                        }
                    }

                    val formattedName = if (avatarUrl.isNotEmpty()) "$firstName|||$avatarUrl" else firstName
                    val delimiterString = "$uploadedVideoUrl|||${caption}|||${capturedVideoDuration}|||$zoomFactor|||$rotation"
                    val cleanCode = targetPalCode.trim()

                    if (cleanCode.isBlank()) continue

                    val newSubmission = SubmissionDbItem(
                        palCode = cleanCode,
                        userId = currentUserId,
                        userDisplayName = formattedName,
                        imageUrl = delimiterString,
                        createdAt = capturedVideoInstantStr
                    )

                    try {
                        try {
                            supabaseClient.postgrest.from("pals")
                                .upsert(PalDbItem(code = cleanCode, name = targetPalName), onConflict = "pal_code")
                        } catch (e: Exception) {
                            // Ignore conflict
                        }
                        supabaseClient.postgrest.from("submissions").insert(newSubmission)
                        
                        if (targetPalCode != "vlog") {
                            val pendingPrefs = applicationContext.getSharedPreferences("pending_submissions_prefs", android.content.Context.MODE_PRIVATE)
                            val allPending = pendingPrefs.all
                            for ((k, v) in allPending) {
                                val valueStr = v as? String ?: continue
                                if (valueStr.contains(targetPalCode) && valueStr.contains(currentUserId) && (capturedVideoPath != null && valueStr.contains(capturedVideoPath))) {
                                    pendingPrefs.edit().remove(k).apply()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (!capturedVideoPath.isNullOrBlank()) {
                    val vlogPrefs = getVlogPrefs(applicationContext)
                    vlogPrefs.edit().remove("uploading_$capturedVideoPath").apply()
                    activeUploads.remove(capturedVideoPath)
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "vlog_upload_channel"
            val channelName = "Vlog Uploading Service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Handles uploading of captured vlogs in an isolated process"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private val activeUploads = java.util.Collections.synchronizedSet(HashSet<String>())
    }
}
