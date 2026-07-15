package com.finrein.pals.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PalDbInsertionItem(
    @SerialName("name") val name: String
    // We send ONLY the group name to allow the server to generate the key safely!
)
