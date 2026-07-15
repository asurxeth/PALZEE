package com.finrein.pals.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserPalMapping(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_display_name") val userDisplayName: String? = null,
    @SerialName("user_avatar_url") val userAvatarUrl: String? = null,
    @SerialName("joined_at") val createdAt: String? = null
)
