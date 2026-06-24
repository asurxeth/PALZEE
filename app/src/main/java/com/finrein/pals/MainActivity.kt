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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

