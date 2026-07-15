package com.finrein.pals.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MessageDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_id") val userId: String,
    @SerialName("message_text") val messageText: String,
    @SerialName("created_at") val createdAt: String? = null
) {
    val senderName: String get() = userId
    val content: String get() = messageText
}
