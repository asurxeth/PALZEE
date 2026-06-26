# PAL Architectural Migration Package

## 1. PROJECT TREE
```text

PAL/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/
│               └── com/
│                   └── finrein/
│                       └── pals/
│                           ├── MainActivity.kt
│                           ├── PalApplication.kt
│                           ├── BackgroundSyncService.kt
│                           ├── di/
│                           │   └── NetworkModule.kt
│                           ├── data/
│                           │   └── repository/
│                           │       ├── AuthRepositoryImpl.kt
│                           │       └── GroupRepositoryImpl.kt
│                           │   └── local/
│                           │       └── SessionManager.kt
│                           ├── domain/
│                           │   ├── repository/
│                           │   │   ├── AuthRepository.kt
│                           │   │   └── GroupRepository.kt
│                           │   └── model/
│                           │       └── User.kt
│                           └── presentation/
│                               ├── auth/
│                               │   ├── AuthViewModel.kt
│                               │   ├── AuthUiState.kt
│                               │   ├── AuthUiEvent.kt
│                               │   ├── SimpleAuthScreen.kt
│                               │   └── OnboardingScreen.kt
│                               ├── home/
│                               │   ├── HomeScreen.kt
│                               │   ├── PalGroupGridScreen.kt
│                               │   └── VideoProcessor.kt
│                               └── theme/
│                                   ├── Color.kt
│                                   ├── Spacing.kt
│                                   ├── Theme.kt
│                                   └── Type.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml

```

## 2. SUPABASE INITIALIZATION & NETWORK

### [PalApplication.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/PalApplication.kt)
```kotlin
package com.finrein.pals

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

import io.ktor.client.plugins.HttpTimeout

@HiltAndroidApp
class PalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PalApplication
            private set

        @kotlin.OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
        val supabase: SupabaseClient by lazy {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                httpConfig {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                        connectTimeoutMillis = 30000
                        socketTimeoutMillis = 30000
                    }
                }
                install(Auth)
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID)
                }
                install(Postgrest)
                install(Storage)
                install(Realtime)
            }
        }
    }
}

typealias PALApplication = PalApplication

```

### [NetworkModule.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/di/NetworkModule.kt)
```kotlin
package com.finrein.pals.di

import com.finrein.pals.data.repository.AuthRepositoryImpl
import com.finrein.pals.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupModule {

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: com.finrein.pals.data.repository.GroupRepositoryImpl
    ): com.finrein.pals.domain.repository.GroupRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): io.github.jan.supabase.SupabaseClient {
        return com.finrein.pals.PalApplication.supabase
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(WebSockets)
        }
    }
}

```

## 3. VIEWMODELS

### [AuthViewModel.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/presentation/auth/AuthViewModel.kt)
```kotlin
package com.finrein.pals.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.gotrue.providers.Google
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.finrein.pals.PalApplication
import com.finrein.pals.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: com.finrein.pals.domain.repository.AuthRepository
) : ViewModel() {
 
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    suspend fun authenticateAndRouteUser(rawIdToken: String): com.finrein.pals.domain.repository.UserRouteState {
        return authRepository.authenticateAndRouteUser(rawIdToken)
    }

    suspend fun verifyOtpAndRouteUser(userEmail: String, otpToken: String): com.finrein.pals.domain.repository.UserRouteState {
        return authRepository.verifyOtpAndRouteUser(userEmail, otpToken)
    }

    suspend fun softDeleteAccount(userId: String) {
        authRepository.softDeleteAccount(userId)
    }

    fun setLoading() {
        _uiState.value = AuthUiState.Loading
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun setSuccess(message: String, user: com.finrein.pals.domain.model.User) {
        _uiState.value = AuthUiState.Success(message, user)
    }
 
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun getStableUserId(email: String): String {
        return UUID.nameUUIDFromBytes(email.trim().lowercase(java.util.Locale.ROOT).toByteArray()).toString()
    }
 
    // Global handle to our initialized Supabase client instance
    private val supabaseAuth = PalApplication.supabase.auth
 
    // Step A: Request the 6-Digit code to be delivered to the user's email inbox
    fun sendEmailVerificationCode(emailAddress: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val trimmedEmail = emailAddress.trim().lowercase(java.util.Locale.ROOT)
                supabaseAuth.signInWith(OTP) {
                    email = trimmedEmail
                    createUser = true // Automatically signs up new temporary accounts
                }
                _uiState.value = AuthUiState.Success("Verification code dispatched to $trimmedEmail")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Failed to transmit OTP")
            }
        }
    }
 
    // Step B: Collect the user's 6-digit text input and authenticate the session
    fun verifyEmailCode(emailAddress: String, baseCode: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val trimmedEmail = emailAddress.trim().lowercase(java.util.Locale.ROOT)
                val trimmedCode = baseCode.trim()
                supabaseAuth.verifyEmailOtp(
                    type = OtpType.Email.MAGIC_LINK,
                    email = trimmedEmail,
                    token = trimmedCode
                )
                // For backward compatibility, let's create a User model and pass it in Success
                val user = com.finrein.pals.domain.model.User(
                    id = supabaseAuth.currentUserOrNull()?.id ?: getStableUserId(trimmedEmail),
                    email = trimmedEmail,
                    displayName = trimmedEmail.substringBefore("@"),
                    isPasskeyRegistered = false
                )
                _uiState.value = AuthUiState.Success("Authenticated successfully!", user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Invalid code verification execution")
            }
        }
    }
 
    fun handleGoogleLoginResult(result: NativeSignInResult) {
        viewModelScope.launch {
            when (result) {
                is NativeSignInResult.Success -> {
                    val user = com.finrein.pals.domain.model.User(
                        id = supabaseAuth.currentUserOrNull()?.id ?: getStableUserId("google.user@gmail.com"),
                        email = "google.user@gmail.com",
                        displayName = "Google User",
                        isPasskeyRegistered = false
                    )
                    _uiState.value = AuthUiState.Success("Google handshake complete!", user)
                }
                is NativeSignInResult.ClosedByUser -> {
                    _uiState.value = AuthUiState.Idle
                }
                is NativeSignInResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }
 
    fun loginWithGoogleIdToken(idTokenStr: String, emailAddress: String, displayNameStr: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                supabaseAuth.signInWith(IDToken) {
                    idToken = idTokenStr
                    provider = Google
                    nonce = null
                }
                val user = com.finrein.pals.domain.model.User(
                    id = supabaseAuth.currentUserOrNull()?.id ?: getStableUserId(emailAddress),
                    email = emailAddress,
                    displayName = displayNameStr,
                    isPasskeyRegistered = false
                )
                _uiState.value = AuthUiState.Success("Google handshake complete!", user)
            } catch (e: Exception) {
                // Return local success if online handshake fails to guarantee demo operation
                val user = com.finrein.pals.domain.model.User(
                    id = getStableUserId(emailAddress),
                    email = emailAddress,
                    displayName = displayNameStr,
                    isPasskeyRegistered = false
                )
                _uiState.value = AuthUiState.Success("Google handshake completed locally.", user)
            }
        }
    }
 
    fun loginWithLocalPasskey(user: com.finrein.pals.domain.model.User) {
        _uiState.value = AuthUiState.Success("Logged in via device Passkey", user)
    }
 
    fun registerTemporaryPasskeyUser(name: String, emailAddress: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // 1. Establish unique anonymous system identities
                val fallbackSecureSystemPassword = UUID.randomUUID().toString() + "PassKey123!"
 
                // 2. Create the shell record containing the local user metadata parameters
                supabaseAuth.signUpWith(Email) {
                    email = emailAddress
                    password = fallbackSecureSystemPassword
                    data = buildJsonObject {
                        put("first_name", name)
                        put("is_temporary", true)
                    }
                }
 
                val user = com.finrein.pals.domain.model.User(
                    id = supabaseAuth.currentUserOrNull()?.id ?: getStableUserId(emailAddress),
                    email = emailAddress,
                    displayName = name,
                    isPasskeyRegistered = true
                )
                _uiState.value = AuthUiState.Success("Temporary profile registered via biometrics.", user)
            } catch (e: Exception) {
                // Return local success if online signup fails to guarantee temporary device passkey operation
                val user = com.finrein.pals.domain.model.User(
                    id = getStableUserId(emailAddress),
                    email = emailAddress,
                    displayName = name,
                    isPasskeyRegistered = true
                )
                _uiState.value = AuthUiState.Success("Temporary profile registered locally.", user)
            }
        }
    }
}

```

## 4. REPOSITORIES

### [AuthRepository.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/repository/AuthRepository.kt)
```kotlin
package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.User

enum class UserRouteState { NEW_USER, RETURNING_USER, ERROR }

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    
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

```

### [GroupRepository.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/repository/GroupRepository.kt)
```kotlin
package com.finrein.pals.domain.repository

interface GroupRepository {
    /**
     * Deletes the single row inside the 'user_pals' join table where both [userId] and [groupId] (palCode) match.
     */
    suspend fun leaveGroup(userId: String, groupId: String): Result<Unit>

    /**
     * Deletes the core record from the 'pals' table using [groupId] (palCode) which
     * automatically cascades and clears out all memberships (user_pals), submissions, and messages.
     */
    suspend fun deleteGroup(groupId: String): Result<Unit>
}

```

### [AuthRepositoryImpl.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/data/repository/AuthRepositoryImpl.kt)
```kotlin
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
            // Atomically delete group from parent pals table, letting ON DELETE CASCADE handle child tables
            supabaseClient.postgrest.from("pals").delete {
                filter { eq("pal_code", palCode) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun leavePalsGroup(palCode: String, userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Atomically delete user mapping, letting database constraints cascade/clean up related rows
            supabaseClient.postgrest.from("user_pals").delete {
                filter {
                    eq("pal_code", palCode)
                    eq("user_id", userId)
                }
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

```

### [GroupRepositoryImpl.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/data/repository/GroupRepositoryImpl.kt)
```kotlin
package com.finrein.pals.data.repository

import com.finrein.pals.domain.repository.GroupRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : GroupRepository {

    override suspend fun leaveGroup(userId: String, groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("user_pals").delete {
                filter {
                    eq("user_id", userId)
                    eq("pal_code", groupId)
                }
            }
            Unit
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("pals").delete {
                filter {
                    eq("pal_code", groupId)
                }
            }
            Unit
        }
    }
}

```

## 5. DOMAIN MODELS

### [User.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/model/User.kt)
```kotlin
package com.finrein.pals.domain.model

data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isPasskeyRegistered: Boolean
)

```

### Classes declared in [HomeScreen.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt)
```kotlin
class PalItem(
    val name: String,
    val size: String,
    val code: String,
    val isVlog: Boolean = false,
    val isCreator: Boolean = true
)

class UserItem(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null
)

class UserPalMapping(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_display_name") val userDisplayName: String? = null,
    @SerialName("user_avatar_url") val userAvatarUrl: String? = null,
    @SerialName("joined_at") val createdAt: String? = null
)

class SubmissionDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_display_name") val userDisplayName: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("created_at") val createdAt: String? = null
)

class MessageDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_id") val userId: String,
    @SerialName("message_text") val messageText: String,
    @SerialName("created_at") val createdAt: String? = null
)

class PalDbItem(
    @SerialName("pal_code") val code: String,
    val name: String,
    val size: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

class VlogRecord(
    @SerialName("user_id") val user_id: String,
    @SerialName("pal_code") val pal_code: String,
    @SerialName("video_url") val video_url: String,
    @SerialName("captured_at") val captured_at: String
)
```

## 6. DATABASE FUNCTIONS IN HOMESCREEN

```kotlin
fun refreshPals() {
        if (currentUserId.isEmpty()) return
        coroutineScope.launch {
            try {
                isRefreshing = true
                
                // 🚀 Replaces all sequential client queries with one single database transaction
                val rawResponseString = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.rpc(
                        function = "get_clean_homescreen_dashboard",
                        parameters = mapOf("current_user_uuid" to currentUserId)
                    ).data
                }

                // Parse unified payload array
                val jsonObject = kotlinx.serialization.json.Json.parseToJsonElement(rawResponseString).jsonObject
                val vlogBoxSize = jsonObject["vlog_box_size"]?.jsonPrimitive?.content ?: ""
                val groupsArray = jsonObject["groups"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())

                val defaultVlog = PalItem(
                    name = "vlog",
                    size = vlogBoxSize, 
                    code = "vlog",
                    isVlog = true,
                    isCreator = false
                )

                val mappedPals = groupsArray.map { element ->
                    val obj = element.jsonObject
                    PalItem(
                        name = obj["name"]?.jsonPrimitive?.content ?: "",
                        size = obj["size"]?.jsonPrimitive?.content ?: "1",
                        code = obj["code"]?.jsonPrimitive?.content ?: "",
                        isVlog = false,
                        isCreator = obj["is_creator"]?.jsonPrimitive?.boolean ?: false
                    )
                }

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    createdPals = (listOf(defaultVlog) + mappedPals)
                        .distinctBy { it.code }
                        .filterNot { locallyDeletedPals.containsKey(it.code) }
                }

            } catch (e: Exception) {
                android.util.Log.e("RPC_Dashboard_Error", "Transaction request bypassed: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

fun refreshActivePalDetails(palCode: String) {
        if (currentUserId.isEmpty() || palCode == "vlog" || palCode.isBlank()) return
        coroutineScope.launch {
            try {
                // Groups strictly operate within the absolute, real-time 4am to 4am day window
                val systemNow = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
                val targetDay = if (systemNow.hour < 4) {
                    systemNow.minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                } else {
                    systemNow.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                }
                
                val currentSystemHour = systemNow.hour

                // 1. Fetch only submissions belonging strictly to this custom palCode group table row
                val dbSubmissions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.from("submissions")
                        .select {
                            filter {
                                eq("pal_code", palCode)
                                gte("created_at", "${targetDay}T04:00:00Z")
                                lt("created_at", "${java.time.ZonedDateTime.parse(targetDay + "T04:00:00Z").plusDays(1).toInstant()}")
                            }
                        }
                        .decodeList<SubmissionDbItem>()
                }
                val filteredSubmissions = dbSubmissions.filterNot { sub ->
                    sub.imageUrl == "PROFILE_AVATAR" || sub.imageUrl.startsWith("PROFILE_AVATAR") ||
                    locallyDeletedSubmissions.containsKey(sub.imageUrl.split("|||").firstOrNull()) ||
                    (sub.id != null && locallyDeletedSubmissions.containsKey(sub.id.toString()))
                }
                val oldSubs = allPalsSubmissions[palCode] ?: emptyList()
                if (oldSubs != filteredSubmissions) {
                    allPalsSubmissions[palCode] = filteredSubmissions
                }
                
                val dbMessages = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.from("messages")
                        .select {
                            filter {
                                eq("pal_code", palCode)
                            }
                        }
                        .decodeList<MessageDbItem>()
                }
                val oldMsgs = allPalsMessages[palCode] ?: emptyList()
                if (oldMsgs != dbMessages) {
                    allPalsMessages[palCode] = dbMessages
                }

                // If user doesn't have a submission (either profile or active vlog) in dbSubmissions, insert a profile submission in the background
                val hasUserSub = dbSubmissions.any { it.userId == currentUserId }
                if (!hasUserSub && pendingProfileInserts[palCode] != true) {
                    pendingProfileInserts[palCode] = true
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            var avatarUrl = ""
                            customAvatarUriString?.let { uriStr ->
                                if (uriStr.startsWith("http")) {
                                    avatarUrl = uriStr
                                } else if (uriStr.isNotEmpty()) {
                                    val uploaded = uploadFileToSupabase(context, uriStr, "avatars")
                                    if (uploaded.startsWith("http")) {
                                        avatarUrl = uploaded
                                        sessionManager.saveAvatarUri(uploaded)
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            customAvatarUriString = uploaded
                                        }
                                    }
                                }
                            }
                            val cleanCode = palCode.trim()
                            if (cleanCode.isBlank()) {
                                android.util.Log.e("SubmissionError", "Aborting upload: pal_code is empty.")
                                pendingProfileInserts.remove(palCode)
                                return@launch
                            }
                            val profileDisplayName = if (avatarUrl.isNotEmpty()) "$firstName|||$avatarUrl" else firstName
                            val profileSub = SubmissionDbItem(
                                palCode = cleanCode,
                                userId = currentUserId,
                                userDisplayName = profileDisplayName,
                                imageUrl = "PROFILE_AVATAR",
                                createdAt = java.time.Instant.now().toString()
                            )

                            // Use the mutex lock here so profile generation never cross-fires with real-time events
                            if (!globalSyncMutex.isLocked) {
                                globalSyncMutex.withLock {
                                    // Recreate group if deleted or missing using insert (no pre-check select)
                                    try {
                                        supabaseClient.postgrest.from("pals")
                                            .insert(PalDbItem(code = cleanCode, name = "Pals Group"))
                                    } catch (e: Exception) {
                                        // Ignore conflict to preserve original group name
                                    }

                                    // Insert profile token row safely
                                    supabaseClient.postgrest.from("submissions").insert(profileSub)
                                    
                                    // ✅ THE FIX: Do NOT recall refreshActivePalDetails() recursively!
                                    // Instead, update the local map memory directly on the main thread to break the loop.
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        val currentList = allPalsSubmissions[cleanCode] ?: emptyList()
                                        allPalsSubmissions[cleanCode] = currentList + profileSub
                                        pendingProfileInserts.remove(cleanCode)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WarpProfileFix", "Profile setup stalled safely: ${e.message}")
                            pendingProfileInserts.remove(palCode)
                        }
                    }
                }
                
                val mappings = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.from("user_pals")
                        .select {
                            filter {
                                eq("pal_code", palCode)
                            }
                        }
                        .decodeList<UserPalMapping>()
                        .sortedWith(compareBy({ it.createdAt ?: "" }, { it.id ?: "" }))
                }

                val userFirstName = currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                
                val memberList = mutableListOf<String>()
                val addedUserIds = mutableSetOf<String>()

                mappings.forEach { mapping ->
                    if (mapping.userId.isNotEmpty() && !addedUserIds.contains(mapping.userId)) {
                        val sub = dbSubmissions.firstOrNull { it.userId == mapping.userId }
                        val (displayName, avatarUrl) = if (sub != null) {
                            parseUserDisplayName(sub.userDisplayName)
                        } else {
                            if (mapping.userId == currentUserId) {
                                val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
                                Pair(userFirstName, localAvatar)
                            } else {
                                Pair("Pal", null)
                            }
                        }
                        val formatted = "${mapping.userId}|||$displayName|||${avatarUrl ?: ""}"
                        memberList.add(formatted)
                        addedUserIds.add(mapping.userId)
                    }
                }

                dbSubmissions.forEach { sub ->
                    if (sub.userId.isNotEmpty() && !addedUserIds.contains(sub.userId)) {
                        val (displayName, avatarUrl) = parseUserDisplayName(sub.userDisplayName)
                        val formatted = "${sub.userId}|||$displayName|||${avatarUrl ?: ""}"
                        memberList.add(formatted)
                        addedUserIds.add(sub.userId)
                    }
                }

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    // Classify all historical group updates into separate 0-23 hour slots
                    dailyHourHistoryMap = filteredSubmissions.groupBy { it.getHourBucket() }

                    // Extract group items submitted during the *current* active hourly frame
                    val itemsInThisHour = dailyHourHistoryMap[currentSystemHour] ?: emptyList()
                    activeHourSubmissions = itemsInThisHour.associateBy { it.userId }
                    
                    // Update group export matrices cleanly 
                    exportMenuDataState = dailyHourHistoryMap.toSortedMap()

                    allPalsMembers[palCode] = memberList
                    palPalsCount[palCode] = filteredSubmissions.map { it.userId }.distinct().size
                }
            } catch (e: Exception) {
                android.util.Log.e("PalsGroupRefresh", "Group sync engine error: ${e.message}")
            }
        }
    }

fun refreshMessages(palCode: String) {
        if (currentUserId.isEmpty() || palCode == "vlog") return
        coroutineScope.launch {
            try {
                val dbMessages = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.from("messages")
                        .select {
                            filter {
                                eq("pal_code", palCode)
                            }
                            order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                        }
                        .decodeList<MessageDbItem>()
                }
                val oldMsgs = palMessages[palCode] ?: emptyList()
                if (oldMsgs != dbMessages) {
                    palMessages[palCode] = dbMessages
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

suspend fun uploadFileToSupabase(context: android.content.Context, uriString: String, bucketName: String): String {
    try {
        val uri = android.net.Uri.parse(uriString)
        val inputStream = if (uri.scheme == "content" || uri.scheme == "file") {
            context.contentResolver.openInputStream(uri)
        } else {
            val cleanPath = if (uriString.startsWith("file://")) uriString.substring(7) else uriString
            val file = java.io.File(cleanPath)
            if (file.exists()) {
                java.io.FileInputStream(file)
            } else {
                context.contentResolver.openInputStream(uri)
            }
        }
        if (inputStream == null) {
            android.util.Log.e("SupabaseUpload", "Could not open input stream for $uriString")
            return uriString
        }
        var bytes = inputStream.use { it.readBytes() }
        val extension = if (bucketName == "pals" || bucketName == "pals_vlogs") "mp4" else "jpg"
        
        // Compress images to keep payload under 200 KB
        if (extension == "jpg") {
            bytes = compressImageBytes(bytes)
        }

        val fileName = "${java.util.UUID.randomUUID()}.$extension"
        val storageBucket = com.finrein.pals.PalApplication.supabase.storage.from(bucketName)
        storageBucket.upload(fileName, bytes, upsert = true)
        val publicUrl = storageBucket.publicUrl(fileName)
        android.util.Log.d("SupabaseUpload", "Uploaded successfully! Public URL: $publicUrl")
        return publicUrl
    } catch (e: Exception) {
        e.printStackTrace()
        android.util.Log.e("SupabaseUpload", "Upload failed for $uriString: ${e.message}")
        return uriString
    }
}

suspend fun uploadPalVideoAndGetUrl(context: android.content.Context, localUri: android.net.Uri, userId: String): String? {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(localUri)
            val bytes = inputStream?.readBytes() ?: return@withContext null
            inputStream.close()
            
            // Generate a unique filename with a timestamp and proper extension
            val fileName = "${userId}_${System.currentTimeMillis()}.mp4"
            
            val bucket = com.finrein.pals.PalApplication.supabase.storage.from("pals_vlogs")
            bucket.upload(fileName, bytes, upsert = true)
            
            return@withContext bucket.publicUrl(fileName)
        } catch (e: Exception) {
            android.util.Log.e("VIDEO_STORAGE_ERR", "Video upload failed: ${e.localizedMessage}")
            null
        }
    }
}

suspend fun ensureVideoCached(context: android.content.Context, videoPath: String): String {
    if (!videoPath.startsWith("http")) {
        return videoPath
    }
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val palPrefs = context.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
        val vlogPrefs = context.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE)
        val cachedLocal = palPrefs.getString("local_path_$videoPath", null)
            ?: vlogPrefs.getString("local_path_$videoPath", null)
        if (cachedLocal != null && java.io.File(cachedLocal).exists()) {
            cachedLocal
        } else {
            val fileName = videoPath.substringAfterLast("/")
            val cacheFile = java.io.File(context.cacheDir, "cached_pal_$fileName")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                cacheFile.absolutePath
            } else {
                try {
                    val bucketName = if (videoPath.contains("pals_vlogs")) "pals_vlogs" else "pals"
                    val storage = com.finrein.pals.PalApplication.supabase.storage.from(bucketName)
                    val bytes = try {
                        storage.downloadPublic(fileName)
                    } catch (e1: Exception) {
                        storage.downloadAuthenticated(fileName)
                    }
                    cacheFile.writeBytes(bytes)
                    palPrefs.edit().putString("local_path_$videoPath", cacheFile.absolutePath).apply()
                    vlogPrefs.edit().putString("local_path_$videoPath", cacheFile.absolutePath).apply()
                    cacheFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                    videoPath
                }
            }
        }
    }
}

suspend fun sendVideoPalToVlog(context: android.content.Context, localUri: android.net.Uri, userId: String, palCode: String) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val permanentVideoUrl = uploadPalVideoAndGetUrl(context, localUri, userId) ?: return@withContext
        
        val newVlogRecord = VlogRecord(
            user_id = userId,
            pal_code = palCode,
            video_url = permanentVideoUrl, // 💡 Save the permanent cloud video web link!
            captured_at = java.time.Instant.now().toString()
        )
        
        com.finrein.pals.PalApplication.supabase.postgrest.from("user_pals").insert(newVlogRecord)
    }
}
```

## 7. NAVIGATION & ENTRY POINT

### [MainActivity.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/MainActivity.kt)
```kotlin
package com.finrein.pals

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import com.finrein.pals.presentation.auth.OnboardingScreen
import com.finrein.pals.presentation.auth.AuthViewModel
import com.finrein.pals.presentation.home.HomeScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.auth
import com.finrein.pals.presentation.theme.PalTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import javax.inject.Inject
import com.finrein.pals.domain.repository.AuthRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        super.onCreate(savedInstanceState)
        


        val sessionManager = com.finrein.pals.data.local.SessionManager(applicationContext)

        setContent {
            PalTheme {
                var currentUser by remember { 
                    mutableStateOf<com.finrein.pals.domain.model.User?>(sessionManager.getUser()) 
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.finrein.pals.presentation.theme.PalBackground
                ) {
                    if (currentUser == null) {
                        OnboardingScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { user ->
                                sessionManager.saveUser(user)
                                currentUser = user
                            }
                        )
                    } else {
                        HomeScreen(
                            user = currentUser,
                            authRepository = authRepository,
                            onSignOut = {
                                lifecycleScope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(this@MainActivity)
                                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                    } catch (e: Exception) {
                                        // Ignore credential manager clearing exception
                                    }
                                    try {
                                        PalApplication.supabase.auth.signOut()
                                    } catch (e: Exception) {
                                        // Ignore session/network exception on signout
                                    }
                                }
                                authViewModel.resetState()
                                sessionManager.clearUser()
                                val sharedPrefs = applicationContext.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE)
                                sharedPrefs.edit().clear().apply()
                                val palPrefs = applicationContext.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
                                palPrefs.edit().clear().apply()
                                try {
                                    applicationContext.cacheDir.deleteRecursively()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                currentUser = null
                            },
                            onDeleteAccount = {
                                val userId = currentUser?.id
                                lifecycleScope.launch {
                                    if (userId != null) {
                                        try {
                                            authViewModel.softDeleteAccount(userId)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    try {
                                        val credentialManager = CredentialManager.create(this@MainActivity)
                                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                    } catch (e: Exception) {
                                        // Ignore credential manager clearing exception
                                    }
                                    try {
                                        PalApplication.supabase.auth.signOut()
                                    } catch (e: Exception) {
                                        // Ignore session/network exception on signout
                                    }
                                    authViewModel.resetState()
                                    sessionManager.clearUser()
                                    val sharedPrefs = applicationContext.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE)
                                    sharedPrefs.edit().clear().apply()
                                    val palPrefs = applicationContext.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
                                    palPrefs.edit().clear().apply()
                                    try {
                                        applicationContext.cacheDir.deleteRecursively()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    currentUser = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


```

> [!IMPORTANT]
> There is NO `NavHost` or `App()` function inside the project. Navigation is manually operated via `currentUser` nullability check in `MainActivity` content scope.

## 8. ARCHITECTURAL DEPENDENCY DIAGRAM

```mermaid
graph TD
  subgraph Current_Architecture [Current Realized Architecture (Highly Coupled)]
    UI[HomeScreen.kt / Composables] -->|Direct Query Violation| Supabase[Supabase Client API]
    UI -->|Direct DB Mutation Violation| Supabase
    UI -->|Google signout/auth directly| Supabase
    MainActivity -->|Direct signout/auth directly| Supabase
    AuthViewModel -->|Bypasses Repo for Auth OTP| Supabase
    AuthViewModel -->|Google auth logic| Supabase
    AuthViewModel -->|Call Repo| AuthRepo[AuthRepositoryImpl]
    AuthRepo -->|Database query| Supabase
  end

  subgraph Clean_Architecture_Target [Target Architecture]
    TargetUI[UI Layer] -->|ReadOnly state observe| TargetVM[ViewModel Layer]
    TargetVM -->|Commands / Intent triggers| TargetRepo[Repository Layer]
    TargetRepo -->|Postgrest / Storage / Realtime| TargetSupabase[Supabase Client / Data Sources]
  end

  style UI fill:#ff9999,stroke:#333,stroke-width:2px
  style AuthViewModel fill:#ffffcc,stroke:#333,stroke-width:1px
```

## 9. BUILD CONFIGURATION

### [build.gradle.kts (Project-Level)](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/build.gradle.kts)
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
}

```

### [build.gradle.kts (App-Level)](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/build.gradle.kts)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.0"
}

import java.util.Properties

val envProperties = Properties().apply {
    val envFile = project.rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

val localProperties = Properties().apply {
    val localFile = project.rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

val supabaseUrl = (envProperties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "https://placeholder-url.supabase.co").trim('"')
val supabaseAnonKey = (envProperties.getProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: "placeholder-anon-key").trim('"')
val googleWebClientId = (localProperties.getProperty("google.web.client.id") ?: envProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: System.getenv("GOOGLE_WEB_CLIENT_ID") ?: "").trim('"')

android {
    namespace = "com.finrein.pals"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = project.rootProject.file("Palls.jks")
            storePassword = "11223344"
            keyAlias = "pals-release"
            keyPassword = "11223344"
        }
    }

    defaultConfig {
        applicationId = "com.finrein.pals"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        javaCompileOptions {
            annotationProcessorOptions {
                argument("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
            }
        }
        ksp {
            arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            isMinifyEnabled = false      // Turn this OFF to stop R8 from stripping code
            isShrinkResources = false   // Turn this OFF to keep all assets intact
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release") // Keeps your working Palls.jks signature
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM & UI Elements
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room local storage
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore & Crypto
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto.ktx)

    // Ktor client & network capabilities
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.websockets)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.compose.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)

    // Core Credential Manager Library
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // CameraX dependencies
    val cameraVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-video:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // Media3 ExoPlayer dependencies for loop playback in preview
    val media3Version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Tooling dependencies for development
    debugImplementation(libs.androidx.compose.ui.tooling)
}



tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-deprecation")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.register("readCrash") {
    doLast {
        val destFile = file("${projectDir}/../crash_log_real.txt")
        try {
            val process = ProcessBuilder("adb", "logcat", "-d", "-b", "crash")
                .redirectOutput(ProcessBuilder.Redirect.to(destFile))
                .start()
            process.waitFor()
            println("Successfully read crash log and wrote to: ${destFile.absolutePath}")
        } catch (e: Exception) {
            println("Error reading crash log: ${e.message}")
        }
    }
}

tasks.register("readLogcat") {
    doLast {
        val destFile = file("${projectDir}/../logcat_real.txt")
        try {
            // Retrieve logcat with main, system, and crash buffers
            val process = ProcessBuilder("adb", "logcat", "-d")
                .redirectOutput(ProcessBuilder.Redirect.to(destFile))
                .start()
            process.waitFor()
            println("Successfully read logcat and wrote to: ${destFile.absolutePath}")
        } catch (e: Exception) {
            println("Error reading logcat: ${e.message}")
        }
    }
}



```

### [libs.versions.toml](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/gradle/libs.versions.toml)
```toml
[versions]
agp = "8.4.0"
kotlin = "2.0.0"
composeBom = "2024.05.00"
hilt = "2.51.1"
room = "2.6.1"
ktor = "2.3.11"
coroutines = "1.8.1"
datastore = "1.1.1"
lifecycle = "2.8.0"
activityCompose = "1.9.0"
coreKtx = "1.13.1"
securityCrypto = "1.1.0-alpha06"
ksp = "2.0.0-1.0.21"
supabase = "2.6.1"


[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-ui-text-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }

# Hilt Dependency Injection
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# Room local database
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# DataStore user preferences
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
security-crypto-ktx = { group = "androidx.security", name = "security-crypto-ktx", version.ref = "securityCrypto" }

# Ktor Network Client
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-cio = { group = "io.ktor", name = "ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
ktor-client-websockets = { group = "io.ktor", name = "ktor-client-websockets", version.ref = "ktor" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Supabase
supabase-bom = { group = "io.github.jan-tennert.supabase", name = "bom", version.ref = "supabase" }
supabase-gotrue = { group = "io.github.jan-tennert.supabase", name = "gotrue-kt" }
supabase-compose-auth = { group = "io.github.jan-tennert.supabase", name = "compose-auth" }
supabase-postgrest = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt" }
supabase-storage = { group = "io.github.jan-tennert.supabase", name = "storage-kt" }
supabase-realtime = { group = "io.github.jan-tennert.supabase", name = "realtime-kt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

```

## 10. DATABASE SCHEMAS, CONSTRAINTS & ROUTINES

### Table Definitions
```sql
-- 1. pals Table
CREATE TABLE public.pals (
    pal_code VARCHAR PRIMARY KEY NOT NULL UNIQUE,
    name VARCHAR NOT NULL,
    size VARCHAR DEFAULT '3',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- 2. user_pals (Group Memberships & Vlogs Tracker)
CREATE TABLE public.user_pals (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,
    video_url VARCHAR NULL, -- Used to store individual vlog uploads
    user_display_name VARCHAR NULL,
    user_avatar_url VARCHAR NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- 3. submissions (Hour buckets submissions)
CREATE TABLE public.submissions (
    id BIGSERIAL PRIMARY KEY,
    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    user_display_name VARCHAR NOT NULL,
    image_url VARCHAR NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- 4. messages (Chat overlay data)
CREATE TABLE public.messages (
    id BIGSERIAL PRIMARY KEY,
    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);
```

### RPC Definitions
```sql
-- RPC 1: generate_unique_pal_code
CREATE OR REPLACE FUNCTION public.generate_unique_pal_code()
RETURNS VARCHAR AS $$
DECLARE
    new_code VARCHAR;
    exists_flag BOOLEAN;
BEGIN
    LOOP
        new_code := upper(substring(md5(random()::text) from 1 for 6));
        SELECT EXISTS(SELECT 1 FROM public.pals WHERE pal_code = new_code) INTO exists_flag;
        IF NOT exists_flag THEN
            RETURN new_code;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- RPC 2: get_clean_homescreen_dashboard
CREATE OR REPLACE FUNCTION public.get_clean_homescreen_dashboard(current_user_uuid UUID)
RETURNS JSONB AS $$
DECLARE
    vlog_size INT;
    groups_json JSONB;
BEGIN
    -- Count user's personal vlog entries
    SELECT count(*) INTO vlog_size FROM public.submissions WHERE pal_code = 'vlog' AND user_id = current_user_uuid;
    
    -- Fetch and map pals groups user belongs to
    SELECT jsonb_agg(jsonb_build_object(
        'code', p.pal_code,
        'name', p.name,
        'size', p.size,
        'is_creator', EXISTS(SELECT 1 FROM public.user_pals up WHERE up.pal_code = p.pal_code AND up.user_id = current_user_uuid)
    )) INTO groups_json
    FROM public.pals p
    JOIN public.user_pals up ON p.pal_code = up.pal_code
    WHERE up.user_id = current_user_uuid;

    RETURN jsonb_build_object(
        'vlog_box_size', COALESCE(vlog_size::text, '0'),
        'groups', COALESCE(groups_json, '[]'::jsonb)
    );
END;
$$ LANGUAGE plpgsql;
```

### RLS, Indexes & Foreign Keys
```sql
-- RLS enabling
ALTER TABLE public.pals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_pals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Enable realtime replication for updates stream listening
alter publication supabase_realtime add table public.user_pals;
alter publication supabase_realtime add table public.submissions;
alter publication supabase_realtime add table public.messages;

-- Indexes
CREATE INDEX idx_user_pals_user ON public.user_pals(user_id);
CREATE INDEX idx_user_pals_code ON public.user_pals(pal_code);
CREATE INDEX idx_submissions_code ON public.submissions(pal_code);
CREATE INDEX idx_messages_code ON public.messages(pal_code);
```
