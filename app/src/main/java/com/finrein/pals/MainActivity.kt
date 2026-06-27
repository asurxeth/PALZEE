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
import com.finrein.pals.presentation.home.HomeViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.auth
import com.finrein.pals.presentation.theme.PalTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import javax.inject.Inject
import com.finrein.pals.domain.repository.AuthRepository
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.os.Build
import android.view.RoundedCorner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

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
            var selectedThemeColor by remember { 
                mutableStateOf(sessionManager.getThemeColor()) 
            }

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
                        DynamicGlowScreenContainer(selectedThemeColor = selectedThemeColor) {
                            HomeScreen(
                                user = currentUser,
                                authRepository = authRepository,
                                viewModel = homeViewModel,
                                selectedThemeColor = selectedThemeColor,
                                onSelectedThemeColorChange = { color ->
                                    selectedThemeColor = color
                                    sessionManager.saveThemeColor(color)
                                },
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
}

@Composable
fun DynamicGlowScreenContainer(
    selectedThemeColor: String,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var cornerRadiusDp by remember { mutableStateOf(0.dp) }

    DisposableEffect(view) {
        val listener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val insets = view.rootWindowInsets
                if (insets != null) {
                    val topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                    val topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
                    val radiusPx = topLeft?.radius ?: topRight?.radius ?: 0
                    if (radiusPx > 0) {
                        cornerRadiusDp = (radiusPx / density.density).dp
                    }
                }
            }
        }
        view.addOnLayoutChangeListener(listener)
        // Check immediately in case view is already attached and laid out
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val insets = view.rootWindowInsets
            if (insets != null) {
                val topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                val topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
                val radiusPx = topLeft?.radius ?: topRight?.radius ?: 0
                if (radiusPx > 0) {
                    cornerRadiusDp = (radiusPx / density.density).dp
                }
            }
        }
        onDispose {
            view.removeOnLayoutChangeListener(listener)
        }
    }

    val themeColorOption = when (selectedThemeColor) {
        "yellow" -> Color(0xFFFFE600) // Neon Yellow
        "orange" -> Color(0xFFFF6700) // Neon Orange
        "pink" -> Color(0xFFFF007F)   // Neon Pink
        "blue" -> Color(0xFF00F0FF)   // Neon Cyan/Blue
        "purple" -> Color(0xFFB000FF) // Neon Purple
        "red" -> Color(0xFFFF073A)    // Neon Red
        else -> Color(0xFFFFE600)
    }

    val deviceSmoothRadius = RoundedCornerShape(cornerRadiusDp)

    if (cornerRadiusDp > 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 🌟 1. DRAW THE INNER NEON GLOW BLUR EXTENSION EFFECT USING CANVAS CLIPPING
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val path = android.graphics.Path().apply {
                            val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
                            val rx = cornerRadiusDp.toPx()
                            addRoundRect(rect, rx, rx, android.graphics.Path.Direction.CW)
                        }
                        
                        // Clip the canvas to prevent the glow from bleeding outside the screen boundaries
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.clipPath(path)

                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = themeColorOption.toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 4.dp.toPx()
                            setShadowLayer(
                                24.dp.toPx(),  // Ambient blur radius
                                0f, 0f,
                                themeColorOption.toArgb()
                            )
                        }
                        canvas.nativeCanvas.drawPath(path, paint)
                        canvas.nativeCanvas.restore()
                    }
                }
        ) {
            // Content container Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(deviceSmoothRadius)
                    .background(Color.Black)
            ) {
                content()
            }

            // Overlay Border Box: drawn strictly on top so its lines are never clipped and have exact width of 2.dp allthrough the wrapper!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = themeColorOption,
                        shape = deviceSmoothRadius
                    )
            )
        }
    } else {
        // Plain fullscreen layout when no physical rounded corners are detected (square screen / API fallback)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            content()
        }
    }
}

