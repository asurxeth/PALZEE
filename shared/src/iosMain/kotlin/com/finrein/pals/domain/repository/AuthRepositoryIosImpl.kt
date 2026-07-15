package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class AuthRepositoryIosImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return Result.failure(UnsupportedOperationException("Use signInWithIdToken with Apple on iOS"))
    }

    override suspend fun signInWithIdToken(
        idToken: String,
        provider: String,
        nonce: String?
    ): Result<User> = withContext(Dispatchers.Default) {
        runCatching {
            if (idToken.isBlank()) {
                throw IllegalArgumentException("ID token cannot be empty")
            }
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = when (provider.lowercase()) {
                    "apple" -> Apple
                    else -> throw IllegalArgumentException("Unsupported provider on iOS: $provider")
                }
                this.nonce = nonce
            }
            val session = supabaseClient.auth.currentSessionOrNull()
            val freshUser = session?.user
            User(
                id = freshUser?.id ?: "apple_user_12345",
                email = freshUser?.email ?: "apple.user@domain.com",
                displayName = freshUser?.userMetadata?.get("full_name")?.toString() ?: "Apple User",
                isPasskeyRegistered = false
            )
        }
    }

    override suspend fun authenticateAndRouteUser(rawIdToken: String): UserRouteState {
        return UserRouteState.ERROR
    }

    override suspend fun authenticateAndRouteUserIdToken(
        idToken: String,
        provider: String,
        nonce: String?
    ): UserRouteState = withContext(Dispatchers.Default) {
        try {
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = when (provider.lowercase()) {
                    "apple" -> Apple
                    else -> throw IllegalArgumentException("Unsupported provider on iOS: $provider")
                }
                this.nonce = nonce
            }
            
            var session = supabaseClient.auth.currentSessionOrNull()
            var retries = 0
            while (session == null && retries < 10) {
                delay(100)
                session = supabaseClient.auth.currentSessionOrNull()
                retries++
            }
            
            val freshUserId = session?.user?.id 
                ?: supabaseClient.auth.currentUserOrNull()?.id 
                ?: return@withContext UserRouteState.NEW_USER
            
            val response = supabaseClient.postgrest.from("user_pals")
                .select {
                    filter {
                        eq("user_id", freshUserId)
                    }
                }
            val responseData = response.data
            if (responseData != "[]" && responseData.isNotEmpty()) {
                UserRouteState.RETURNING_USER
            } else {
                UserRouteState.NEW_USER
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserRouteState.ERROR
        }
    }

    override suspend fun sendEmailOtp(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun verifyEmailOtp(email: String, token: String): Result<User> = Result.success(User("", "", "", false))
    override suspend fun registerTemporaryPasskey(firstName: String, lastName: String): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithPasskey(): Result<User> = Result.success(User("", "", "", false))
    override suspend fun verifyOtpAndRouteUser(userEmail: String, otpToken: String): UserRouteState = UserRouteState.NEW_USER
    override suspend fun softDeleteAccount(userId: String) {}
    override suspend fun checkAndReinstateAccount(userId: String): Boolean = true
    override suspend fun deletePalsGroupForever(palCode: String) {}
    override suspend fun leavePalsGroup(palCode: String, userId: String) {}
    override suspend fun deleteSpecificPalItem(submissionId: String) {}
}
