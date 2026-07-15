package com.finrein.pals.core.domain.model

data class PalItem(
    val name: String,
    val size: String,
    val code: String,
    val isVlog: Boolean = false,
    val isCreator: Boolean = true
)
