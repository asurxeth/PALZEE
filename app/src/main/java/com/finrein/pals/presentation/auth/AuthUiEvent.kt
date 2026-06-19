package com.finrein.pals.presentation.auth

sealed interface AuthUiEvent {
    data class SignInWithGoogle(val idToken: String) : AuthUiEvent
    
    data class SendEmailOtp(val email: String) : AuthUiEvent
    
    data class VerifyEmailOtp(val email: String, val otp: String) : AuthUiEvent
    
    data class RegisterPasskey(val firstName: String, val lastName: String) : AuthUiEvent
    
    object SignInWithPasskey : AuthUiEvent
    
    object ResetState : AuthUiEvent
}
