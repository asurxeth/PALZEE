package com.finrein.pals.feature.auth

import com.finrein.pals.core.domain.model.User

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String, val user: User? = null) : AuthUiState
    data class Error(val exceptionMessage: String) : AuthUiState
}
