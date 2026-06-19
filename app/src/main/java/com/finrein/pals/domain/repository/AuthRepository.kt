package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    
    suspend fun sendEmailOtp(email: String): Result<Unit>
    
    suspend fun verifyEmailOtp(email: String, token: String): Result<User>
    
    suspend fun registerTemporaryPasskey(firstName: String, lastName: String): Result<Unit>
    
    suspend fun signInWithPasskey(): Result<User>
}
