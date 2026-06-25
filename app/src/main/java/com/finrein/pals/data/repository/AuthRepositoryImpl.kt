package com.finrein.pals.data.repository

import com.finrein.pals.domain.model.User
import com.finrein.pals.domain.repository.AuthRepository
import com.finrein.pals.domain.repository.UserRouteState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import com.finrein.pals.presentation.home.SubmissionDbItem
import com.finrein.pals.presentation.home.UserPalMapping
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun verifyOtpAndRouteUser(userEmail: String, otpToken: String): UserRouteState = withContext(Dispatchers.IO) {
        try {
            // Step 1: Verify the Email OTP with Supabase
            supabaseClient.auth.verifyEmailOtp(
                type = OtpType.Email.MAGIC_LINK,
                email = userEmail,
                token = otpToken
            )
            
            // Step 2: Poll for session to ensure Ktor headers/JWT propagate
            var session = supabaseClient.auth.currentSessionOrNull()
            var retries = 0
            while (session == null && retries < 10) {
                delay(100)
                session = supabaseClient.auth.currentSessionOrNull()
                retries++
            }
            
            // Step 3: Extract the fresh user ID directly from immediate session/auth state
            val freshUserId = session?.user?.id 
                ?: supabaseClient.auth.currentUserOrNull()?.id 
                ?: return@withContext UserRouteState.NEW_USER
            
            checkAndReinstateAccount(freshUserId)
            
            // Step 4: Check if account exists and is older than 60 seconds
            val userCreatedAt = session?.user?.createdAt 
                ?: supabaseClient.auth.currentUserOrNull()?.createdAt
            val isReturningUserByAge = if (userCreatedAt != null) {
                val ageMs = System.currentTimeMillis() - userCreatedAt.toEpochMilliseconds()
                ageMs > 60000
            } else {
                false
            }

            if (isReturningUserByAge) {
                UserRouteState.RETURNING_USER
            } else {
                // Step 5: Instantly check user_pals using the explicit freshUserId
                val response = supabaseClient.postgrest.from("user_pals")
                    .select {
                        filter {
                            eq("user_id", freshUserId)
                        }
                    }
                val responseData = response.data
                if (responseData != "[]" && responseData.isNotEmpty()) {
                    UserRouteState.RETURNING_USER // Destined for Screen 4 (Permissions)
                } else {
                    UserRouteState.NEW_USER       // Destined for Welcome to Pal Screen
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserRouteState.ERROR
        }
    }

    override suspend fun authenticateAndRouteUser(rawIdToken: String): UserRouteState = withContext(Dispatchers.IO) {
        try {
            // Step 1: Initialize the Supabase ID Token Handshake
            supabaseClient.auth.signInWith(IDToken) {
                idToken = rawIdToken
                provider = Google
                nonce = null
            }
            
            // Step 2: Poll for session to ensure Ktor headers/JWT propagate
            var session = supabaseClient.auth.currentSessionOrNull()
            var retries = 0
            while (session == null && retries < 10) {
                delay(100)
                session = supabaseClient.auth.currentSessionOrNull()
                retries++
            }
            
            // Step 3: Extract the fresh user ID directly from immediate session/auth state
            val freshUserId = session?.user?.id 
                ?: supabaseClient.auth.currentUserOrNull()?.id 
                ?: return@withContext UserRouteState.NEW_USER
            
            checkAndReinstateAccount(freshUserId)
            
            // Step 4: Check if account exists and is older than 60 seconds
            val userCreatedAt = session?.user?.createdAt 
                ?: supabaseClient.auth.currentUserOrNull()?.createdAt
            val isReturningUserByAge = if (userCreatedAt != null) {
                val ageMs = System.currentTimeMillis() - userCreatedAt.toEpochMilliseconds()
                ageMs > 60000
            } else {
                false
            }

            if (isReturningUserByAge) {
                UserRouteState.RETURNING_USER
            } else {
                // Step 5: Query the user_pals membership table immediately using the explicit freshUserId
                val response = supabaseClient.postgrest.from("user_pals")
                    .select {
                        filter {
                            eq("user_id", freshUserId)
                        }
                    }
                val responseData = response.data
                if (responseData != "[]" && responseData.isNotEmpty()) {
                    UserRouteState.RETURNING_USER // Destined for Screen 4 (Permissions)
                } else {
                    UserRouteState.NEW_USER       // Destined for Welcome to Pal Screen
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserRouteState.ERROR
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            // Simulate network call latency
            delay(1500)
            
            // Real network structure commented for compilation/mock testing
            /*
            val response: UserResponse = httpClient.post("https://api.pal.com/v1/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleAuthRequest(idToken))
            }.body()
            User(response.id, response.email, response.displayName, response.isPasskeyRegistered)
            */

            // Production-grade mock result for onboarding test
            if (idToken.isBlank()) {
                throw IllegalArgumentException("ID token cannot be empty")
            }
            User(
                id = "google_user_12345",
                email = "user@gmail.com",
                displayName = "Google User",
                isPasskeyRegistered = false
            )
        }
    }

    override suspend fun sendEmailOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            delay(1000)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                throw IllegalArgumentException("Invalid email format")
            }
            // Mock server request
            Unit
        }
    }

    override suspend fun verifyEmailOtp(email: String, token: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            delay(1500)
            if (token != "123456" && token.length != 6) {
                throw IllegalArgumentException("Invalid OTP token. Try '123456'")
            }
            User(
                id = "email_user_67890",
                email = email,
                displayName = email.substringBefore("@"),
                isPasskeyRegistered = false
            )
        }
    }

    override suspend fun registerTemporaryPasskey(firstName: String, lastName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            delay(1500)
            if (firstName.isBlank() || lastName.isBlank()) {
                throw IllegalArgumentException("Name fields cannot be empty")
            }
            // Passkey registration sequence mock
            Unit
        }
    }

    override suspend fun signInWithPasskey(): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            delay(2000)
            // Simulated passkey biometric prompt success
            User(
                id = "passkey_user_abcde",
                email = "passkey.user@pal.com",
                displayName = "Passkey User",
                isPasskeyRegistered = true
            )
        }
    }

    override suspend fun softDeleteAccount(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest.from("submissions").delete {
                filter {
                    eq("user_id", userId)
                }
            }
            supabaseClient.postgrest.from("user_pals").delete {
                filter {
                    eq("user_id", userId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun checkAndReinstateAccount(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext false
    }

    override suspend fun deletePalsGroupForever(palCode: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Hard delete in reverse order of foreign keys (child tables first, then parent table)
            try {
                supabaseClient.postgrest.from("messages")
                    .delete {
                        filter { eq("pal_code", palCode) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                supabaseClient.postgrest.from("submissions")
                    .delete {
                        filter { eq("pal_code", palCode) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                supabaseClient.postgrest.from("user_pals")
                    .delete {
                        filter { eq("pal_code", palCode) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                supabaseClient.postgrest.from("pals")
                    .delete {
                        filter { eq("pal_code", palCode) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun leavePalsGroup(palCode: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Hard delete mappings and submissions for this user in this group
            try {
                supabaseClient.postgrest.from("user_pals")
                    .delete {
                        filter {
                            eq("pal_code", palCode)
                            eq("user_id", userId)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                supabaseClient.postgrest.from("submissions")
                    .delete {
                        filter {
                            eq("pal_code", palCode)
                            eq("user_id", userId)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteSpecificPalItem(submissionId: String): Unit = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest.from("submissions")
                .delete {
                    filter { eq("id", submissionId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Serializable
private data class GoogleAuthRequest(val idToken: String)

@Serializable
private data class UserResponse(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isPasskeyRegistered: Boolean
)
