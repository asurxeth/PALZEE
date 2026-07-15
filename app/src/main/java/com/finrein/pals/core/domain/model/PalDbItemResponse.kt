package com.finrein.pals.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PalDbItemResponse(
    @SerialName("pal_code") val palCode: String
)
