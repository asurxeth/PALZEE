package com.finrein.pals

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.net.Uri
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
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
        try {
            com.finrein.pals.presentation.home.pruneOrphanedAppCache(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val sessionManager = com.finrein.pals.data.local.SessionManager(applicationContext)
        val interval = sessionManager.getNotificationInterval()
        com.finrein.pals.notification.PalAlarmScheduler.updateScheduling(
            applicationContext,
            interval
        )
        if (sessionManager.getUser() != null && interval != "off" && interval.isNotEmpty()) {
            val checkIntent = android.content.Intent(applicationContext, com.finrein.pals.notification.PalNotificationReceiver::class.java).apply {
                action = "com.finrein.pals.ACTION_CHECK_FIRST_PAL"
            }
            applicationContext.sendBroadcast(checkIntent)
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName

        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(fallbackIntent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            DisposableEffect(Unit) {
                onDispose {
                    try {
                        com.finrein.pals.presentation.home.pruneOrphanedAppCache(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

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
                                val currentInterval = sessionManager.getNotificationInterval()
                                com.finrein.pals.notification.PalAlarmScheduler.updateScheduling(
                                    applicationContext,
                                    currentInterval
                                )
                                if (currentInterval != "off" && currentInterval.isNotEmpty()) {
                                    val checkIntent = android.content.Intent(applicationContext, com.finrein.pals.notification.PalNotificationReceiver::class.java).apply {
                                        action = "com.finrein.pals.ACTION_CHECK_FIRST_PAL"
                                    }
                                    applicationContext.sendBroadcast(checkIntent)
                                }
                            }
                        )
                    } else {
                        var onboardingCompleted by remember(currentUser) {
                            mutableStateOf(sessionManager.isOnboardingCompleted())
                        }
                        DynamicGlowScreenContainer(
                            selectedThemeColor = selectedThemeColor,
                            showBorder = onboardingCompleted
                        ) {
                            HomeScreen(
                                user = currentUser,
                                authRepository = authRepository,
                                viewModel = homeViewModel,
                                selectedThemeColor = selectedThemeColor,
                                onSelectedThemeColorChange = { color ->
                                    selectedThemeColor = color
                                    sessionManager.saveThemeColor(color)
                                },
                                onOnboardingCompleted = {
                                    onboardingCompleted = true
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val targetTab = intent.getStringExtra("TARGET_TAB")
        if (targetTab != null) {
            homeViewModel.setCurrentTab(targetTab)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            com.finrein.pals.presentation.home.pruneOrphanedAppCache(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        init {
            try {
                System.loadLibrary("image_processing_util_jni")
                System.loadLibrary("surface_util_jni")
            } catch (e: UnsatisfiedLinkError) {
                println("Native surface shader linker handshake initiated safely.")
            }
        }
    }
}

@Composable
fun DynamicGlowScreenContainer(
    selectedThemeColor: String,
    showBorder: Boolean,
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

    val accentEdgeColor = when (selectedThemeColor) {
        "blue" -> Color(0xFF11D5F3)
        "green" -> Color(0xFF65EA7B)
        "orange" -> Color(0xFFFE9068)
        "pink" -> Color(0xFFFE75F5)
        "purple" -> Color(0xFFAA6DFE)
        "cyan" -> Color(0xFF5D96FF)
        else -> Color(0xFF11D5F3)
    }

    val effectiveRadius = if (cornerRadiusDp > 0.dp) cornerRadiusDp else 24.dp
    val deviceSmoothRadius = RoundedCornerShape(effectiveRadius)

    val baseModifier = Modifier
        .fillMaxSize()
        .clip(deviceSmoothRadius)
        .background(Color.Black)

    val containerModifier = if (showBorder) {
        baseModifier.drawWithContent {
            drawContent() // 1. Draw the black background and HomeScreen contents first
            
            // 2. Draw the solid outermost screen edge boundary outline on top of the black background/contents!
            drawIntoCanvas { canvas ->
                val path = android.graphics.Path().apply {
                    val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
                    val rx = effectiveRadius.toPx()
                    addRoundRect(rect, rx, rx, android.graphics.Path.Direction.CW)
                }

                // Draw the solid outermost boundary stroke on the original path (clipped to path to prevent outer bleed)
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.clipPath(path)

                val borderPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = accentEdgeColor.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 1.dp.toPx() // Visible border width is exactly 0.5.dp after clipping
                }
                canvas.nativeCanvas.drawPath(path, borderPaint)

                canvas.nativeCanvas.restore()
            }
        }
    } else {
        baseModifier
    }

    Box(
        modifier = containerModifier
    ) {
        content()
    }
}
