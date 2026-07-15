package com.finrein.pals.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class VlogRecord(
    @SerialName("user_id") val user_id: String,
    @SerialName("pal_code") val pal_code: String,
    @SerialName("video_url") val video_url: String,
    @SerialName("captured_at") val captured_at: String
)
