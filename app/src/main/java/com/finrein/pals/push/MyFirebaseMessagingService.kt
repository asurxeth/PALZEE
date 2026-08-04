package com.finrein.pals.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data["type"] ?: remoteMessage.data["notification_type"] ?: ""
        val groupName = remoteMessage.data["group_name"] ?: remoteMessage.data["pals_group_name"] ?: ""
        val userName = remoteMessage.data["user_name"] ?: remoteMessage.data["person_name"] ?: remoteMessage.data["sender_name"] ?: ""

        val rawTitle = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: ""
        val rawBody = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""

        when {
            type == "group_join" || rawBody.contains("joined", ignoreCase = true) -> {
                // Image 1 format: Title = <pals_group_name>, Body = <user_name> joined <pals_group_name>
                val gName = groupName.ifBlank { rawTitle.ifBlank { "Pals" } }
                val uName = userName.ifBlank { rawBody.substringBefore("joined").trim() }
                PalNotificationManager.showGroupJoinNotification(applicationContext, uName, gName)
            }
            type == "new_pal" || type == "vlog_post" || rawBody.contains("new pal", ignoreCase = true) || rawBody.contains("new log", ignoreCase = true) -> {
                // Image 2 format: Title = <person_name>, Body = "new pal"
                val pName = userName.ifBlank { rawTitle.ifBlank { "Someone" } }
                val gName = groupName.ifBlank { "Pals" }
                PalNotificationManager.showNewPalNotification(applicationContext, pName, gName)
            }
            else -> {
                // Fallback for general notifications
                val title = rawTitle.ifBlank { "Palzee" }
                val body = rawBody.ifBlank { "Time to capture your pal" }
                val gName = groupName.ifBlank { "Pals" }
                PalNotificationManager.showNotification(applicationContext, title, body, gName)
            }
        }
    }
}
