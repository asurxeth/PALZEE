package com.finrein.pals.data.repository

import com.finrein.pals.domain.model.User
import com.finrein.pals.domain.repository.AuthRepository
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient
) : AuthRepository {

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
