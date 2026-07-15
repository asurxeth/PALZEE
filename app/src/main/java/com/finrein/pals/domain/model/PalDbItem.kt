package com.finrein.pals.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PalDbItem(
    @SerialName("pal_code") val code: String,
    val name: String,
    val size: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
