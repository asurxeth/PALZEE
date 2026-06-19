package com.finrein.pals.domain.model

data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isPasskeyRegistered: Boolean
)
