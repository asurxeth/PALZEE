package com.finrein.pals.presentation.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finrein.pals.MainActivity
import com.finrein.pals.PalApplication
import com.finrein.pals.data.local.SessionManager
import com.finrein.pals.presentation.home.CapturedPreviewScreen
import com.finrein.pals.presentation.home.HomeViewModel
import com.finrein.pals.presentation.home.PalThemes
import com.finrein.pals.presentation.home.handleVlogSubmission
import com.finrein.pals.presentation.theme.PalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun finish() {
        android.util.Log.e("PreviewClose", "finish() called!", Throwable("Activity finish() Stack Trace"))
        super.finish()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        android.util.Log.e("PreviewClose", "onBackPressed called!")
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            android.util.Log.e("PreviewActivity", "Activity is finishing! Check your logic.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.e("PreviewClose", "Activity finishing!", Throwable("Activity onDestroy Stack Trace"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inspect the entire raw bundle keys
        val bundleKeys = intent.extras?.keySet()
        if (bundleKeys != null) {
            for (key in bundleKeys) {
                @Suppress("DEPRECATION")
                val value = intent.extras?.get(key)
                android.util.Log.e("PreviewActivity", "Found Intent Extra Key: '$key' -> Value: $value")
            }
        } else {
            android.util.Log.e("PreviewActivity", "Bundle extras object itself is completely null!")
        }

        // 2. Test your specific expected targets
        val pathString = intent.getStringExtra("video_path") 
        val uriExtra = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("video_uri", android.net.Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<android.net.Uri>("video_uri")
        } 

        android.util.Log.e("PreviewActivity", "Target Path String evaluation: '$pathString'")
        android.util.Log.e("PreviewActivity", "Target Uri Extra evaluation: '$uriExtra'")

        val capturedVideoPath = (intent.getStringExtra("VLOG_PATH")
            ?: intent.getStringExtra("CAPTURED_VIDEO_PATH")
            ?: intent.getStringExtra("VIDEO_URI_PATH")
            ?: pathString
            ?: uriExtra?.toString()) ?: ""

        android.util.Log.e("PreviewActivity", "Resolved capturedVideoPath = '$capturedVideoPath'")

        if (capturedVideoPath.isEmpty()) {
            android.util.Log.e("PreviewActivity", "Video path missing from intent extras!")
            finish()
            return
        }

        val capturedVideoDuration = intent.getLongExtra("VLOG_DURATION", 2000L)
        val capturedVideoZoomFactor = intent.getFloatExtra("VLOG_ZOOM_FACTOR", 1.0f)
        val activePalCode = intent.getStringExtra("ACTIVE_PAL_CODE")
        val activePalName = intent.getStringExtra("ACTIVE_PAL_NAME")
        val activeVlogPal = if (activePalCode != null && activePalName != null) {
            com.finrein.pals.domain.model.PalItem(
                name = activePalName,
                code = activePalCode,
                size = "0",
                isVlog = activePalCode == "vlog",
                isCreator = true
            )
        } else {
            null
        }

        val sessionManager = SessionManager(applicationContext)
        val currentUser = sessionManager.getUser()
        val currentUserId = currentUser?.id ?: ""
        val currentDisplayName = currentUser?.displayName ?: "Pal"
        val customAvatarUriString = sessionManager.getAvatarUri()

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isDark = isSystemInDarkTheme()
            val selectedThemeColor = remember { sessionManager.getThemeColor() }
            val themeConfig = remember(selectedThemeColor) { PalThemes[selectedThemeColor] ?: PalThemes["blue"]!! }

            val accentColor = themeConfig.accentColor
            val textColor = if (isDark) Color.White else Color.Black
            val mutedTextColor = if (isDark) Color(0xFFA3A3A3) else Color(0xFF737373)
            val palTextLogoColor = themeConfig.logoColor

            val createdPalsState = homeViewModel.createdPals.collectAsState()
            val createdPals = createdPalsState.value

            LaunchedEffect(createdPals) {
                android.util.Log.e("PreviewDataAudit", "Pals list size in Preview: ${createdPals.size}")
            }

            val vlogPrefs = remember { com.finrein.pals.presentation.home.getVlogPrefs(context) }

            LaunchedEffect(Unit) {
                val saved = vlogPrefs.getString("created_pals", "") ?: ""
                val initialList = if (saved.isEmpty()) {
                    listOf(com.finrein.pals.domain.model.PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true))
                } else {
                    saved.split(";;;").mapNotNull { s ->
                        val parts = s.split(":")
                        if (parts.size < 3) null else {
                            com.finrein.pals.domain.model.PalItem(
                                name = parts[0].replace("\\:", ":"),
                                size = parts.getOrNull(1) ?: "4",
                                code = parts.getOrNull(2) ?: "",
                                isVlog = parts.getOrNull(3)?.toBoolean() ?: false,
                                isCreator = parts.getOrNull(4)?.toBoolean() ?: false
                            )
                        }
                    }
                }
                homeViewModel.updateCreatedPals(initialList)
            }

            val capturedVlogsPaths = remember {
                val savedPaths = vlogPrefs.getString("vlog_paths", "") ?: ""
                if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
            }

            val capturedVlogsTimes = remember {
                val savedTimes = vlogPrefs.getString("vlog_times", "") ?: ""
                if (savedTimes.isEmpty()) emptyList() else savedTimes.split(";;;")
            }

            val capturedVlogsCaptions = remember {
                val savedCaptions = vlogPrefs.getString("vlog_captions", "") ?: ""
                if (savedCaptions.isEmpty()) emptyList() else savedCaptions.split(";;;")
            }

            val capturedVlogsDurations = remember {
                val savedDurations = vlogPrefs.getString("vlog_durations", "") ?: ""
                if (savedDurations.isEmpty()) emptyList() else savedDurations.split(";;;")
            }

            val capturedVlogsZoomed = remember {
                val savedZoomed = vlogPrefs.getString("vlog_zoomed", "") ?: ""
                val zoomed = if (savedZoomed.isEmpty()) emptyList() else savedZoomed.split(";;;")
                zoomed.map { token ->
                    if (token.trim().equals("true", ignoreCase = true)) 2.5f
                    else if (token.trim().equals("false", ignoreCase = true)) 1.0f
                    else token.trim().toFloatOrNull() ?: 1.0f
                }
            }

            val allPalsSubmissions = remember {
                val submissionsJson = vlogPrefs.getString("cached_all_pals_submissions", "") ?: ""
                val initialMap = if (submissionsJson.isNotEmpty()) {
                    try {
                        kotlinx.serialization.json.Json.decodeFromString<Map<String, List<com.finrein.pals.domain.model.SubmissionDbItem>>>(submissionsJson)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }
                initialMap
            }

            val allPalsMembers = remember {
                val membersJson = vlogPrefs.getString("cached_all_pals_members", "") ?: ""
                val initialMap = if (membersJson.isNotEmpty()) {
                    try {
                        kotlinx.serialization.json.Json.decodeFromString<Map<String, List<String>>>(membersJson)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }
                initialMap
            }

            val palPalsCount = remember {
                val countsJson = vlogPrefs.getString("cached_pal_pals_count", "") ?: ""
                val initialMap = if (countsJson.isNotEmpty()) {
                    try {
                        kotlinx.serialization.json.Json.decodeFromString<Map<String, Int>>(countsJson)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }
                initialMap
            }

            val refreshActivePalDetailsHelper = remember(currentUserId, currentDisplayName, customAvatarUriString) {
                { palCode: String ->
                    if (currentUserId.isNotEmpty() && palCode != "vlog" && palCode.isNotBlank()) {
                        homeViewModel.refreshActivePalDetails(
                            palCode = palCode,
                            currentUserId = currentUserId,
                            currentDisplayName = currentDisplayName,
                            firstName = currentDisplayName.split(" ").firstOrNull() ?: "Pal",
                            currentAvatarUrl = customAvatarUriString,
                            locallyDeletedSubmissions = emptySet(),
                            resolveAvatarUrl = { customAvatarUriString ?: "" }
                        )
                    }
                }
            }

            fun isVideoFileValid(context: android.content.Context, pathString: String): Boolean {
                if (pathString.isEmpty()) return false
                try {
                    if (pathString.startsWith("content://") || pathString.startsWith("android.resource://")) {
                        val uri = android.net.Uri.parse(pathString)
                        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                            return afd.length > 0L
                        }
                        return false
                    } else {
                        val cleanPath = if (pathString.startsWith("file://")) pathString.substring(7) else pathString
                        val file = java.io.File(cleanPath)
                        return file.exists() && file.length() > 0L
                    }
                } catch (e: Exception) {
                    return false
                }
            }

            val isReady = remember(capturedVideoPath) { capturedVideoPath.isNotEmpty() }

            PalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.finrein.pals.presentation.theme.PalBackground
                ) {
                    if (isReady) {
                        CapturedPreviewScreen(
                            isDark = isDark,
                            accentColor = accentColor,
                            textColor = textColor,
                            mutedTextColor = mutedTextColor,
                            palTextLogoColor = palTextLogoColor,
                            activeVlogPal = activeVlogPal,
                            createdPals = createdPals,
                            rotationAngle = 270f,
                            capturedVideoPath = capturedVideoPath,
                            capturedVlogsPaths = capturedVlogsPaths,
                            zoomFactor = capturedVideoZoomFactor,
                             onClose = {
                                 finish()
                             },
                            onSend = { caption, targetPals, isMuted ->
                                val time = java.time.LocalTime.now()
                                val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", time.hour, time.minute)
                                val finalInstant = java.time.Instant.now()

                                val applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

                                handleVlogSubmission(
                                    caption = caption,
                                    targetPals = targetPals,
                                    capturedVideoPath = capturedVideoPath,
                                    capturedVideoDuration = capturedVideoDuration,
                                    capturedVideoTimeText = formattedTime,
                                    capturedVideoInstant = finalInstant,
                                    currentUserId = currentUserId,
                                    firstName = currentDisplayName.split(" ").firstOrNull() ?: "Pal",
                                    customAvatarUriString = customAvatarUriString,
                                    capturedVlogsPaths = capturedVlogsPaths,
                                    capturedVlogsTimes = capturedVlogsTimes,
                                    capturedVlogsCaptions = capturedVlogsCaptions,
                                    capturedVlogsDurations = capturedVlogsDurations,
                                    capturedVlogsZoomed = capturedVlogsZoomed,
                                    allPalsSubmissions = allPalsSubmissions.toMutableMap(),
                                    palPalsCount = palPalsCount.toMutableMap(),
                                    onUpdateVlogLists = { _, _, _, _, _ ->
                                        val intent = Intent(this@PreviewActivity, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            putExtra("TARGET_TAB", "pals")
                                        }
                                        startActivity(intent)
                                        finish()
                                    },
                                    context = context,
                                    coroutineScope = applicationScope,
                                    supabaseClient = PalApplication.supabase,
                                    sessionManager = sessionManager,
                                    refreshActivePalDetails = refreshActivePalDetailsHelper,
                                    refreshVlogs = {},
                                    onActiveVlogPalChange = {},
                                    onShowingCapturedPreviewChange = {},
                                    onSelectedTabChange = {},
                                    onUpdateAvatarUrl = {},
                                    zoomFactor = capturedVideoZoomFactor,
                                    isMuted = isMuted
                                )
                            },
                            currentUserId = currentUserId,
                            currentDisplayName = currentDisplayName,
                            allPalsSubmissions = allPalsSubmissions,
                            customAvatarUriString = customAvatarUriString,
                            allPalsMembers = allPalsMembers
                        )
                    } else {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
