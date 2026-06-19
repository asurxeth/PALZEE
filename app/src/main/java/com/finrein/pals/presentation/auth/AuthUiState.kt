package com.finrein.pals.presentation.auth

import com.finrein.pals.domain.model.User

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String, val user: User? = null) : AuthUiState
    data class Error(val exceptionMessage: String) : AuthUiState
}
