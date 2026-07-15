package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.User

enum class UserRouteState { NEW_USER, RETURNING_USER, ERROR }

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    
    suspend fun signInWithIdToken(idToken: String, provider: String, nonce: String? = null): Result<User>
    
    suspend fun authenticateAndRouteUserIdToken(idToken: String, provider: String, nonce: String? = null): UserRouteState
    
    suspend fun sendEmailOtp(email: String): Result<Unit>
    
    suspend fun verifyEmailOtp(email: String, token: String): Result<User>
    
    suspend fun registerTemporaryPasskey(firstName: String, lastName: String): Result<Unit>
    
    suspend fun signInWithPasskey(): Result<User>

    suspend fun authenticateAndRouteUser(rawIdToken: String): UserRouteState

    suspend fun verifyOtpAndRouteUser(userEmail: String, otpToken: String): UserRouteState

    suspend fun softDeleteAccount(userId: String)

    suspend fun checkAndReinstateAccount(userId: String): Boolean

    suspend fun deletePalsGroupForever(palCode: String)

    suspend fun leavePalsGroup(palCode: String, userId: String)

    suspend fun deleteSpecificPalItem(submissionId: String)
}
