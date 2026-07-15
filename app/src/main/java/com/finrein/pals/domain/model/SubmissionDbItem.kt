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
            val timePart = when {
                dateStr.contains('T') -> dateStr.substringAfter('T')
                dateStr.contains(' ') -> dateStr.substringAfter(' ')
                dateStr.length >= 19 -> dateStr.substring(11)
                else -> return 0
            }
            val hourStr = timePart.take(2)
            hourStr.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
