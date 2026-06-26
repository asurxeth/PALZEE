package com.finrein.pals.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SubmissionDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_display_name") val userDisplayName: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun getHourBucket(): Int {
        val dateStr = createdAt ?: return 0
        return try {
            val instant = java.time.Instant.parse(dateStr)
            instant.atZone(java.time.ZoneId.systemDefault()).hour
        } catch (e: Exception) {
            try {
                java.time.ZonedDateTime.parse(dateStr).withZoneSameInstant(java.time.ZoneId.systemDefault()).hour
            } catch (e2: Exception) {
                try {
                    java.time.LocalDateTime.parse(dateStr).hour
                } catch (e3: Exception) {
                    0
                }
            }
        }
    }
}
