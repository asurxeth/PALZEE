package com.finrein.pals.presentation.home

import com.finrein.pals.domain.model.PalItem
import com.finrein.pals.domain.model.MessageDbItem
import com.finrein.pals.domain.model.SubmissionDbItem
import com.finrein.pals.domain.model.UserPalMapping
import com.finrein.pals.domain.model.PalDbItem
import com.finrein.pals.domain.model.VlogRecord
import com.finrein.pals.domain.model.ActivePalState
import com.finrein.pals.domain.model.PalDbInsertionItem
import com.finrein.pals.domain.model.PalDbItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import android.os.Build
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.Mutex
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finrein.pals.R
import com.finrein.pals.presentation.theme.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import java.util.concurrent.ConcurrentHashMap
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.Camera
import android.annotation.SuppressLint
import androidx.camera.video.VideoCapture
import androidx.camera.video.Recorder
import androidx.camera.video.QualitySelector
import androidx.camera.video.Quality
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recording
import androidx.core.content.ContextCompat
import android.media.MediaPlayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Search
import java.time.LocalTime


fun getVlogPrefs(context: android.content.Context): android.content.SharedPreferences {
    return context.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE)
}

fun handleDeletePal(
    pal: PalItem?,
    currentUserId: String,
    locallyDeletedPals: MutableMap<String, Boolean>,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    groupDatabase: MutableMap<String, PalItem>,
    palPalsCount: MutableMap<String, Int>,
    allPalsSubmissions: MutableMap<String, List<SubmissionDbItem>>,
    allPalsMessages: MutableMap<String, List<MessageDbItem>>,
    allPalsMembers: MutableMap<String, List<String>>,
    viewModel: com.finrein.pals.presentation.home.HomeViewModel,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    onActiveVlogPalChange: (PalItem?) -> Unit,
    onUpdateVlogState: () -> Unit
) {
    val p = pal
    if (p != null) {
        locallyDeletedPals[p.code] = true
        onCreatedPalsChange(createdPals.filterNot { it.code == p.code })
        if (groupDatabase.containsKey(p.code)) {
            groupDatabase.remove(p.code)
        }
        palPalsCount.remove(p.code)
        viewModel.removePalMessages(p.code)
        allPalsSubmissions.remove(p.code)
        allPalsMessages.remove(p.code)
        allPalsMembers.remove(p.code)
        viewModel.removePendingProfileInsert(p.code)
        if (p.code == "vlog") {
            onUpdateVlogState()
            getVlogPrefs(context).edit().clear().apply()
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                supabaseClient.postgrest.from("pals")
                    .delete {
                        filter {
                            eq("pal_code", p.code)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onActiveVlogPalChange(null)
    }
}

fun handleLeavePal(
    pal: PalItem?,
    currentUserId: String,
    locallyDeletedPals: MutableMap<String, Boolean>,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    palPalsCount: MutableMap<String, Int>,
    allPalsSubmissions: MutableMap<String, List<SubmissionDbItem>>,
    allPalsMessages: MutableMap<String, List<MessageDbItem>>,
    allPalsMembers: MutableMap<String, List<String>>,
    viewModel: com.finrein.pals.presentation.home.HomeViewModel,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    onActiveVlogPalChange: (PalItem?) -> Unit,
    onUpdateVlogState: () -> Unit
) {
    val p = pal
    if (p != null) {
        locallyDeletedPals[p.code] = true
        onCreatedPalsChange(createdPals.filterNot { it.code == p.code })
        palPalsCount.remove(p.code)
        viewModel.removePalMessages(p.code)
        allPalsSubmissions.remove(p.code)
        allPalsMessages.remove(p.code)
        allPalsMembers.remove(p.code)
        viewModel.removePendingProfileInsert(p.code)
        if (p.code == "vlog") {
            onUpdateVlogState()
            getVlogPrefs(context).edit().clear().apply()
        }
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                supabaseClient.postgrest.from("user_pals")
                    .delete {
                        filter {
                            eq("pal_code", p.code)
                            eq("user_id", currentUserId)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onActiveVlogPalChange(null)
    }
}

fun handleDeleteVlog(
    indexToDelete: Int,
    filteredPaths: List<String>,
    filteredTimes: List<String>,
    filteredCaptions: List<String>,
    activeVlogPal: PalItem?,
    currentUserId: String,
    locallyDeletedSubmissions: MutableMap<String, Boolean>,
    allPalsSubmissions: MutableMap<String, List<SubmissionDbItem>>,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    authRepository: com.finrein.pals.domain.repository.AuthRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    capturedVlogsPaths: List<String>,
    capturedVlogsTimes: List<String>,
    capturedVlogsCaptions: List<String>,
    capturedVlogsDurations: List<String>,
    onUpdateVlogLists: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    vlogExoPlayer: androidx.media3.exoplayer.ExoPlayer,
    targetDate: java.time.LocalDate,
    onActiveVlogPalChange: (PalItem?) -> Unit,
    onCurrentPlayingIndexChange: (Int) -> Unit,
    refreshActivePalDetails: (String) -> Unit
) {
    if (indexToDelete in filteredPaths.indices) {
        val deletedPath = filteredPaths[indexToDelete]
        val palCode = activeVlogPal?.code ?: "vlog"
        
        locallyDeletedSubmissions[deletedPath] = true
        if (palCode != "vlog") {
            // GROUP Pal DELETION
            // 1. Remove from allPalsSubmissions
            val currentSubs = allPalsSubmissions[palCode]
            if (currentSubs != null) {
                val updatedSubs = currentSubs.filterNot { 
                    val pathPart = it.imageUrl.split("|||").firstOrNull() ?: ""
                    pathPart == deletedPath || it.imageUrl == deletedPath
                }
                allPalsSubmissions[palCode] = updatedSubs
            }
            
            // 2. Delete from Supabase
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val dbSubs = supabaseClient.postgrest.from("submissions")
                        .select {
                            filter {
                                eq("pal_code", palCode)
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<SubmissionDbItem>()
                    val targetSub = dbSubs.firstOrNull { 
                        val pathPart = it.imageUrl.split("|||").firstOrNull() ?: ""
                        pathPart == deletedPath || it.imageUrl == deletedPath 
                    }
                    if (targetSub != null && targetSub.id != null) {
                        authRepository.deleteSpecificPalItem(targetSub.id)
                    }
                    deleteVlogPostPermanently(currentUserId, deletedPath, palCode)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        refreshActivePalDetails(palCode)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // STANDARD VLOG DELETION
            val globalIndex = capturedVlogsPaths.indexOf(deletedPath)
            if (globalIndex != -1) {
                val updatedPaths = ArrayList(capturedVlogsPaths).apply { removeAt(globalIndex) }
                val updatedTimes = ArrayList(capturedVlogsTimes).apply { if (globalIndex < size) removeAt(globalIndex) }
                val updatedCaptions = ArrayList(capturedVlogsCaptions).apply { if (globalIndex < size) removeAt(globalIndex) }
                val updatedDurations = ArrayList(capturedVlogsDurations).apply { if (globalIndex < size) removeAt(globalIndex) }
                
                getVlogPrefs(context).edit().apply {
                    putString("vlog_paths", updatedPaths.joinToString(";;;"))
                    putString("vlog_times", updatedTimes.joinToString(";;;"))
                    putString("vlog_captions", updatedCaptions.joinToString(";;;"))
                    putString("vlog_durations", updatedDurations.joinToString(";;;"))
                    apply()
                }
                
                onUpdateVlogLists(updatedPaths, updatedTimes, updatedCaptions, updatedDurations)
                if (updatedPaths.isEmpty()) {
                    onActiveVlogPalChange(null)
                }
                
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val dbSubs = supabaseClient.postgrest.from("submissions")
                            .select {
                                filter {
                                    eq("pal_code", palCode)
                                    eq("user_id", currentUserId)
                                }
                            }
                            .decodeList<SubmissionDbItem>()
                        val targetSub = dbSubs.firstOrNull { 
                            val pathPart = it.imageUrl.split("|||").firstOrNull() ?: ""
                            pathPart == deletedPath || it.imageUrl == deletedPath 
                        }
                        if (targetSub != null && targetSub.id != null) {
                            supabaseClient.postgrest.from("submissions").delete {
                                filter {
                                    eq("id", targetSub.id)
                                }
                            }
                        }
                        deleteVlogPostPermanently(currentUserId, deletedPath, palCode)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                vlogExoPlayer.stop()
                vlogExoPlayer.clearMediaItems()
                val newFilteredPaths = updatedPaths.filter { getVlogLocalDate(it) == targetDate }
                newFilteredPaths.forEach { path ->
                    val cleanPath2 = when {
                        path.startsWith("file://") -> path.substring(7)
                        else -> path
                    }
                    val fileTarget = java.io.File(cleanPath2)
                    if (fileTarget.exists() && fileTarget.length() > 0) {
                        val targetUri = android.net.Uri.fromFile(fileTarget)
                        vlogExoPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                    }
                }
                if (newFilteredPaths.isNotEmpty()) {
                    val nextIndex = indexToDelete.coerceAtMost(newFilteredPaths.lastIndex)
                    if (nextIndex < vlogExoPlayer.mediaItemCount) {
                        vlogExoPlayer.seekTo(nextIndex, 0L)
                        vlogExoPlayer.prepare()
                        vlogExoPlayer.playWhenReady = true
                        vlogExoPlayer.play()
                    }
                    onCurrentPlayingIndexChange(nextIndex)
                } else {
                    onCurrentPlayingIndexChange(0)
                }
            }
        }
    }
}

fun handleUpdateVlogCaption(
    targetPath: String,
    newCaption: String,
    capturedVlogsPaths: List<String>,
    capturedVlogsDurations: List<String>,
    capturedVlogsCaptions: List<String>,
    onUpdateCaptionsState: (List<String>) -> Unit,
    activeVlogPal: PalItem?,
    currentUserId: String,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context
) {
    val globalIndex = capturedVlogsPaths.indexOf(targetPath)
    if (globalIndex != -1) {
        val targetDuration = capturedVlogsDurations.getOrNull(globalIndex) ?: "2000"
        val palCode = activeVlogPal?.code ?: "vlog"
        
        val updatedCaptions = ArrayList(capturedVlogsCaptions)
        updatedCaptions[globalIndex] = newCaption
        onUpdateCaptionsState(updatedCaptions)
        getVlogPrefs(context).edit().putString("vlog_captions", updatedCaptions.joinToString(";;;")).apply()
    
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbSubs = supabaseClient.postgrest.from("submissions").select {
                    filter {
                        eq("pal_code", palCode)
                        eq("user_id", currentUserId)
                    }
                }.decodeList<SubmissionDbItem>()
                val targetSub = dbSubs.firstOrNull { 
                    val pathPart = it.imageUrl.split("|||").firstOrNull() ?: ""
                    pathPart == targetPath || it.imageUrl == targetPath
                }
                if (targetSub != null && targetSub.id != null) {
                    val updatedDelimited = "$targetPath|||$newCaption|||$targetDuration"
                    supabaseClient.postgrest.from("submissions").update(
                        value = SubmissionDbItem(
                            id = targetSub.id,
                            palCode = targetSub.palCode,
                            userId = targetSub.userId,
                            userDisplayName = targetSub.userDisplayName,
                            imageUrl = updatedDelimited,
                            createdAt = targetSub.createdAt
                        )
                    ) {
                        filter {
                            eq("id", targetSub.id)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun handleVlogSubmission(
    caption: String,
    targetPals: List<PalItem>,
    capturedVideoPath: String?,
    capturedVideoDuration: Long,
    currentUserId: String,
    firstName: String,
    customAvatarUriString: String?,
    capturedVlogsPaths: List<String>,
    capturedVlogsTimes: List<String>,
    capturedVlogsCaptions: List<String>,
    capturedVlogsDurations: List<String>,
    allPalsSubmissions: MutableMap<String, List<SubmissionDbItem>>,
    palPalsCount: MutableMap<String, Int>,
    onUpdateVlogLists: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    sessionManager: com.finrein.pals.data.local.SessionManager,
    refreshActivePalDetails: (String) -> Unit,
    refreshVlogs: () -> Unit,
    onActiveVlogPalChange: (PalItem?) -> Unit,
    onShowingCapturedPreviewChange: (Boolean) -> Unit,
    onSelectedTabChange: (String) -> Unit,
    onUpdateAvatarUrl: (String) -> Unit
) {
    val localVideoPath = capturedVideoPath
    val time = java.time.LocalTime.now()
    val formattedTime = String.format("%02d:%02d", time.hour, time.minute)

    // Mark that a pal was successfully sent for this specific hour
    val sentHour = time.hour
    val sentPrefs = context.getSharedPreferences("palzee_prefs", android.content.Context.MODE_PRIVATE)
    val sentTodayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
    val sentLogKey = "pal_logged_${sentTodayStr}_$sentHour"
    sentPrefs.edit().putBoolean(sentLogKey, true).apply()

    var currentPaths = capturedVlogsPaths
    var currentTimes = capturedVlogsTimes
    var currentCaptions = capturedVlogsCaptions
    var currentDurations = capturedVlogsDurations
    var finalAvatarUrl = customAvatarUriString ?: ""

    targetPals.forEach { targetPal ->
        val targetPalCode = targetPal.code
        if (targetPalCode == "vlog") {
            localVideoPath?.let { path ->
                val cleanPath = when {
                    path.startsWith("file://") -> path.substring(7)
                    else -> path
                }
                val sourceFile = java.io.File(cleanPath)
                val targetFile = java.io.File(context.filesDir, sourceFile.name)
                if (sourceFile.exists()) {
                    try {
                        sourceFile.copyTo(targetFile, overwrite = true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val persistentPath = targetFile.absolutePath

                val updatedPaths = ArrayList(currentPaths)
                updatedPaths.add(0, persistentPath)
                currentPaths = updatedPaths
                getVlogPrefs(context).edit().putString("vlog_paths", updatedPaths.joinToString(";;;")).apply()

                val updatedTimes = ArrayList(currentTimes)
                updatedTimes.add(0, formattedTime)
                currentTimes = updatedTimes
                getVlogPrefs(context).edit().putString("vlog_times", updatedTimes.joinToString(";;;")).apply()

                val updatedCaptions = ArrayList(currentCaptions)
                updatedCaptions.add(0, caption)
                currentCaptions = updatedCaptions
                getVlogPrefs(context).edit().putString("vlog_captions", updatedCaptions.joinToString(";;;")).apply()

                val updatedDurations = ArrayList(currentDurations)
                updatedDurations.add(0, capturedVideoDuration.toString())
                currentDurations = updatedDurations
                getVlogPrefs(context).edit().putString("vlog_durations", updatedDurations.joinToString(";;;")).apply()
            }
        }

        if (targetPalCode != "vlog") {
            palPalsCount[targetPalCode] = (palPalsCount[targetPalCode] ?: 0) + 1
            val localSubmission = SubmissionDbItem(
                palCode = targetPalCode,
                userId = currentUserId,
                userDisplayName = if (finalAvatarUrl.isNotEmpty()) "$firstName|||$finalAvatarUrl" else firstName,
                imageUrl = "${localVideoPath ?: ""}|||${caption}|||${capturedVideoDuration}",
                createdAt = java.time.Instant.now().toString()
            )
            val currentList = allPalsSubmissions[targetPalCode] ?: emptyList()
            val newHour = (java.time.LocalTime.now().hour - 4 + 24) % 24
            val updatedList = currentList.filterNot { sub ->
                sub.userId == currentUserId && getSubmissionRelativeHour(sub) == newHour
            } + localSubmission
            allPalsSubmissions[targetPalCode] = updatedList
        }

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val uploadedVideoUrl = if (!localVideoPath.isNullOrBlank()) {
                    if (targetPalCode == "vlog") {
                        val cleanPath = if (localVideoPath.startsWith("file://")) localVideoPath.substring(7) else localVideoPath
                        val uri = android.net.Uri.fromFile(java.io.File(cleanPath))
                        uploadPalVideoAndGetUrl(context, uri, currentUserId) ?: ""
                    } else {
                        uploadFileToSupabase(context, localVideoPath, "pals")
                    }
                } else {
                    ""
                }
                
                if (uploadedVideoUrl.startsWith("http") && !localVideoPath.isNullOrBlank()) {
                    val palPrefs = context.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
                    palPrefs.edit().putString("local_path_$uploadedVideoUrl", localVideoPath).apply()
                    
                    val vlogPathValue = if (targetPalCode == "vlog") {
                        val cleanPath = if (localVideoPath.startsWith("file://")) localVideoPath.substring(7) else localVideoPath
                        java.io.File(context.filesDir, java.io.File(cleanPath).name).absolutePath
                    } else {
                        localVideoPath
                    }
                    getVlogPrefs(context).edit().putString("local_path_$uploadedVideoUrl", vlogPathValue).apply()
                }
                
                var avatarUrl = ""
                val checkAvatar = finalAvatarUrl
                if (checkAvatar.isNotEmpty()) {
                    if (checkAvatar.startsWith("http")) {
                        avatarUrl = checkAvatar
                    } else {
                        val uploaded = uploadFileToSupabase(context, checkAvatar, "avatars")
                        if (uploaded.startsWith("http")) {
                            avatarUrl = uploaded
                            sessionManager.saveAvatarUri(uploaded)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                finalAvatarUrl = uploaded
                                onUpdateAvatarUrl(uploaded)
                            }
                        }
                    }
                }
                
                val formattedName = if (avatarUrl.isNotEmpty()) "$firstName|||$avatarUrl" else firstName

                val delimiterString = "$uploadedVideoUrl|||${caption}|||${capturedVideoDuration}"
                val cleanCode = targetPalCode.trim()
                if (cleanCode.isBlank()) {
                    android.util.Log.e("SubmissionError", "Aborting upload: pal_code is empty.")
                    return@launch
                }
                val newSubmission = SubmissionDbItem(
                    palCode = cleanCode,
                    userId = currentUserId,
                    userDisplayName = formattedName,
                    imageUrl = delimiterString,
                    createdAt = java.time.Instant.now().toString()
                )
                try {
                    // Recreate group if deleted or missing using upsert (no pre-check select)
                    try {
                        supabaseClient.postgrest.from("pals")
                            .upsert(PalDbItem(code = cleanCode, name = "Pals Group"), onConflict = "pal_code")
                    } catch (e: Exception) {
                        // Ignore conflict to preserve original group name
                    }

                    supabaseClient.postgrest.from("submissions").insert(newSubmission)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (cleanCode != "vlog") {
                            refreshActivePalDetails(cleanCode)
                        } else {
                            refreshVlogs()
                        }
                    }
                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    android.util.Log.e("SubmissionError", "Postgres ForeignKey block: Group $cleanCode might have been deleted.")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onActiveVlogPalChange(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    onUpdateVlogLists(currentPaths, currentTimes, currentCaptions, currentDurations)
    onShowingCapturedPreviewChange(false)
    onSelectedTabChange("pals")
}

fun handleGlobalSearchTrigger(
    query: String,
    currentUserId: String,
    currentDisplayName: String,
    customAvatarUriString: String?,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    refreshPals: () -> Unit
) {
    val code = query.trim().removePrefix("#").trim().lowercase(java.util.Locale.ROOT)
    if (code.isNotEmpty()) {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val matchedPalDb = supabaseClient.postgrest.from("pals")
                    .select {
                        filter {
                            eq("pal_code", code)
                        }
                    }
                    .decodeSingleOrNull<PalDbItem>()

                if (matchedPalDb != null) {
                    val newMapping = UserPalMapping(
                        userId = currentUserId,
                        palCode = code,
                        userDisplayName = currentDisplayName,
                        userAvatarUrl = customAvatarUriString
                    )
                    supabaseClient.postgrest.from("user_pals").upsert(newMapping, onConflict = "pal_code,user_id")
                    val matchedItem = PalItem(
                        name = matchedPalDb.name,
                        size = "1",
                        code = matchedPalDb.code,
                        isVlog = false,
                        isCreator = false
                    )

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (!createdPals.any { it.code == code }) {
                            onCreatedPalsChange(createdPals + matchedItem)
                        }
                        refreshPals()
                        android.widget.Toast.makeText(context, "Joined group: ${matchedPalDb.name}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "No group found with code: $code", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Failed to search/join group: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

object VlogPlayerManager {
    private val playerCache = ConcurrentHashMap<String, androidx.media3.exoplayer.ExoPlayer>()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getOrCreatePlayer(context: android.content.Context, videoUrl: String): androidx.media3.exoplayer.ExoPlayer {
        return playerCache.getOrPut(videoUrl) {
            androidx.media3.exoplayer.ExoPlayer.Builder(context.applicationContext)
                .setRenderersFactory(
                    androidx.media3.exoplayer.DefaultRenderersFactory(context.applicationContext).apply {
                        setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                        setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                    }
                )
                .setMediaSourceFactory(
                    androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context.applicationContext)
                        .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context.applicationContext))
                )
                .build().apply {
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(videoUrl))
                    repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                    volume = 0f // Muted feed
                    prepare()
                    playWhenReady = true
                }
        }
    }

    fun releasePlayer(videoUrl: String) {
        playerCache.remove(videoUrl)?.apply {
            stop()
            release()
        }
    }

    fun clearAll() {
        playerCache.keys.forEach { releasePlayer(it) }
    }
}

fun parseUserDisplayName(userDisplayName: String): Pair<String, String?> {
    val parts = userDisplayName.split("|||")
    val rawName = parts.getOrNull(0) ?: ""
    val cleanName = rawName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
    val avatar = parts.getOrNull(1)
    return Pair(cleanName, if (avatar.isNullOrEmpty()) null else avatar)
}

fun compressImageBytes(bytes: ByteArray): ByteArray {
    try {
        // Decode dimensions first (out of memory safe)
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        
        // Downscale image if too large (max 1200px on either side)
        val maxDimension = 1200
        var scale = 1
        if (options.outWidth > maxDimension || options.outHeight > maxDimension) {
            val largest = maxOf(options.outWidth, options.outHeight)
            scale = (largest.toFloat() / maxDimension).toInt().coerceAtLeast(1)
        }
        
        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = scale
        }
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        if (bitmap != null) {
            val outputStream = java.io.ByteArrayOutputStream()
            // Compress with 75% quality JPEG
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val compressedBytes = outputStream.toByteArray()
            bitmap.recycle()
            return compressedBytes
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bytes
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

suspend fun deleteVlogPostPermanently(userId: String, videoUrl: String, palCode: String) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            // 1. Evict player from memory pool to kill the background stream instantly
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                VlogPlayerManager.releasePlayer(videoUrl)
            }

            // 2. Clear out physical storage assets from Supabase Bucket
            val fileName = videoUrl.substringAfterLast("/")
            com.finrein.pals.PalApplication.supabase.storage.from("pals_vlogs").delete(fileName)
        } catch (e: Exception) {
            android.util.Log.e("PURGE_ERROR", "Failed to clear asset: ${e.localizedMessage}")
        }
    }
}





data class UserItem(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null
)

val PalItemSaver = listSaver<PalItem, Any>(
    save = { listOf(it.name, it.size, it.code, it.isVlog, it.isCreator) },
    restore = { list ->
        PalItem(
            name = list[0] as String,
            size = list[1] as String,
            code = list[2] as String,
            isVlog = list[3] as Boolean,
            isCreator = list[4] as Boolean
        )
    }
)

val PalItemSaverNullable = Saver<PalItem?, Any>(
    save = { pal ->
        if (pal == null) "" else listOf(pal.name, pal.size, pal.code, pal.isVlog, pal.isCreator)
    },
    restore = { value ->
        if (value is String) null else {
            @Suppress("UNCHECKED_CAST")
            val list = value as List<Any>
            PalItem(
                name = list[0] as String,
                size = list[1] as String,
                code = list[2] as String,
                isVlog = list[3] as Boolean,
                isCreator = list[4] as Boolean
            )
        }
    }
)

val PalItemListSaver = listSaver<List<PalItem>, Any>(
    save = { list ->
        list.map { listOf(it.name, it.size, it.code, it.isVlog, it.isCreator) }
    },
    restore = { restoredList ->
        restoredList.map {
            @Suppress("UNCHECKED_CAST")
            val item = it as List<Any>
            PalItem(
                name = item[0] as String,
                size = item[1] as String,
                code = item[2] as String,
                isVlog = item[3] as Boolean,
                isCreator = item[4] as Boolean
            )
        }
    }
)

val PalItemListStateSaver = Saver<MutableState<List<PalItem>>, Any>(
    save = { state ->
        state.value.map { listOf(it.name, it.size, it.code, it.isVlog, it.isCreator) }
    },
    restore = { value ->
        @Suppress("UNCHECKED_CAST")
        val restoredList = value as List<Any>
        val list = restoredList.map {
            @Suppress("UNCHECKED_CAST")
            val item = it as List<Any>
            PalItem(
                name = item[0] as String,
                size = item[1] as String,
                code = item[2] as String,
                isVlog = item[3] as Boolean,
                isCreator = item[4] as Boolean
            )
        }
        mutableStateOf(list)
    }
)

val PalItemStateSaver = Saver<MutableState<PalItem?>, Any>(
    save = { state ->
        val pal = state.value
        if (pal == null) "" else listOf(pal.name, pal.size, pal.code, pal.isVlog, pal.isCreator)
    },
    restore = { value ->
        val pal = if (value is String) null else {
            @Suppress("UNCHECKED_CAST")
            val list = value as List<Any>
            PalItem(
                name = list[0] as String,
                size = list[1] as String,
                code = list[2] as String,
                isVlog = list[3] as Boolean,
                isCreator = list[4] as Boolean
            )
        }
        mutableStateOf(pal)
    }
)



@Composable
fun UriImage(
    uriString: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var imageBitmap by remember(uriString) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        try {
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                if (uriString.startsWith("http")) {
                    try {
                        val url = java.net.URL(uriString)
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        connection.inputStream.use { inputStream ->
                            android.graphics.BitmapFactory.decodeStream(inputStream)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    val uri = android.net.Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri).use { inputStream ->
                        android.graphics.BitmapFactory.decodeStream(inputStream)
                    }
                }
            }
            if (bitmap != null) {
                imageBitmap = bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Profile Photo",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(Color.Gray)
        )
    }
}

@Composable
fun CustomRadioButton(
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(1.5.dp, if (selected) color else color.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun VlogStarIcon(
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFFFDE792), // light yellow
    strokeColor: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val outerRadius = size.minDimension * 0.38f
        val innerRadius = outerRadius * 0.4f
        
        val path = Path()
        val numPoints = 5
        val angleStep = Math.PI / numPoints
        
        for (i in 0 until (2 * numPoints)) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * angleStep - Math.PI / 2
            val x = cx + r * Math.cos(angle).toFloat()
            val y = cy + r * Math.sin(angle).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        
        val strokePx = 1.5.dp.toPx()
        
        // Draw filled star
        drawPath(
            path = path,
            color = fillColor
        )
        
        // Draw star stroke outline
        drawPath(
            path = path,
            color = strokeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokePx,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        
        // Draw face inside the star
        val eyeRadius = 1.dp.toPx()
        val eyeOffset = outerRadius * 0.15f
        val eyeY = cy - outerRadius * 0.05f
        
        drawCircle(
            color = strokeColor,
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(cx - eyeOffset, eyeY)
        )
        drawCircle(
            color = strokeColor,
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(cx + eyeOffset, eyeY)
        )
        
        // Mouth
        val mouthRadius = 1.5.dp.toPx()
        drawCircle(
            color = strokeColor,
            radius = mouthRadius,
            center = androidx.compose.ui.geometry.Offset(cx, cy + outerRadius * 0.15f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
        
        // Sparkle lines radiating from star points
        val lineLen = 4.dp.toPx()
        val angles = listOf(0.0, 72.0, 144.0, 216.0, 288.0)
        angles.forEach { deg ->
            val rad = Math.toRadians(deg - 90)
            val startDist = outerRadius + 2.dp.toPx()
            val endDist = startDist + lineLen
            val sx = cx + startDist * Math.cos(rad).toFloat()
            val sy = cy + startDist * Math.sin(rad).toFloat()
            val ex = cx + endDist * Math.cos(rad).toFloat()
            val ey = cy + endDist * Math.sin(rad).toFloat()
            
            drawLine(
                color = strokeColor,
                start = androidx.compose.ui.geometry.Offset(sx, sy),
                end = androidx.compose.ui.geometry.Offset(ex, ey),
                strokeWidth = 1.2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

val CalendarMonthIcon = androidx.compose.ui.graphics.vector.ImageVector.Builder(
    name = "CalendarMonth",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = androidx.compose.ui.graphics.SolidColor(Color.White)
    ) {
        moveTo(19f, 4f)
        horizontalLineToRelative(-1f)
        verticalLineTo(2f)
        horizontalLineToRelative(-2f)
        verticalLineToRelative(2f)
        horizontalLineTo(8f)
        verticalLineTo(2f)
        horizontalLineTo(6f)
        verticalLineToRelative(2f)
        horizontalLineTo(5f)
        curveTo(3.89f, 4f, 3.01f, 4.9f, 3.01f, 6f)
        lineTo(3f, 20f)
        curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
        horizontalLineToRelative(14f)
        curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
        verticalLineTo(6f)
        curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
        close()
        moveTo(19f, 20f)
        horizontalLineTo(5f)
        verticalLineTo(9f)
        horizontalLineToRelative(14f)
        verticalLineTo(20f)
        close()
        moveTo(7f, 11f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(7f)
        close()
        moveTo(11f, 11f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(11f)
        close()
        moveTo(15f, 11f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(15f)
        close()
        moveTo(7f, 15f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(7f)
        close()
        moveTo(11f, 15f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(11f)
        close()
        moveTo(15f, 15f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineTo(15f)
        close()
    }
}.build()

data class PalThemeConfig(
    val name: String,
    val accentColor: Color,
    val logoColor: Color,
    val lightGradient: List<Color>,
    val darkGradient: List<Color>,
    val useDarkTextOnAccent: Boolean = false
)

enum class TripleDotScreen {
    MAIN,
    EDIT_PROFILE,
    COLOR_SELECTION,
    PAL_NOTIFICATIONS,
    SETTINGS,
    PHOTO_OPTIONS
}

class CardBoundsShape(
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float,
    private val cornerRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = Rect(left, top, right, bottom)
        val roundRect = RoundRect(rect, CornerRadius(cornerRadius))
        val path = Path().apply {
            addRoundRect(roundRect)
        }
        return Outline.Generic(path)
    }
}

val PalThemes = mapOf(
    "yellow" to PalThemeConfig(
        name = "yellow",
        accentColor = Color(0xFFFBC02D),
        logoColor = Color(0xFF007FFF), // Azure
        lightGradient = listOf(Color(0xFFFFFFF0).copy(alpha = 1.0f), Color(0xFFFFF9C4).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF2D2D16).copy(alpha = 1.0f), Color(0xFF1E1E0E).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    ),
    "orange" to PalThemeConfig(
        name = "orange",
        accentColor = Color(0xFFFF8F00),
        logoColor = Color(0xFF00E676), // Mint
        lightGradient = listOf(Color(0xFFFFEBEE).copy(alpha = 1.0f), Color(0xFFFFCCBC).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF2D1E16).copy(alpha = 1.0f), Color(0xFF1E140E).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    ),
    "green" to PalThemeConfig(
        name = "green",
        accentColor = Color(0xFF00E676),
        logoColor = Color(0xFFFF8F00), // Tangerine
        lightGradient = listOf(Color(0xFFE8F5E9).copy(alpha = 1.0f), Color(0xFFC8E6C9).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF162D1C).copy(alpha = 1.0f), Color(0xFF0E1E12).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    ),
    "blue" to PalThemeConfig(
        name = "blue",
        accentColor = Color(0xFF007FFF),
        logoColor = Color(0xFFFBC02D), // Sunflower Yellow
        lightGradient = listOf(Color(0xFFE3F2FD).copy(alpha = 1.0f), Color(0xFFBBDEFB).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF16242D).copy(alpha = 1.0f), Color(0xFF0E181E).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    ),
    "purple" to PalThemeConfig(
        name = "purple",
        accentColor = Color(0xFF8A2BE2),
        logoColor = Color(0xFFEA4335), // Google Red
        lightGradient = listOf(Color(0xFFF3E5F5).copy(alpha = 1.0f), Color(0xFFE1BEE7).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF1C1C2D).copy(alpha = 1.0f), Color(0xFF12121E).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    ),
    "red" to PalThemeConfig(
        name = "red",
        accentColor = Color(0xFFEA4335),
        logoColor = Color(0xFF8A2BE2), // Hyacinth
        lightGradient = listOf(Color(0xFFFFEBEE).copy(alpha = 1.0f), Color(0xFFFFCDD2).copy(alpha = 1.0f)),
        darkGradient = listOf(Color(0xFF2D1616).copy(alpha = 1.0f), Color(0xFF1E0E0E).copy(alpha = 1.0f)),
        useDarkTextOnAccent = false
    )
)

fun parseName(email: String?, displayName: String?): Pair<String, String> {
    val genericNames = listOf("google user", "apple user", "default user", "user")
    if (!displayName.isNullOrBlank() && !genericNames.contains(displayName.lowercase().trim())) {
        val parts = displayName.trim().split("\\s+".toRegex())
        val first = parts.firstOrNull() ?: ""
        val last = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
        if (first.isNotEmpty()) {
            return Pair(
                first.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                last.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            )
        }
    }
    
    if (!email.isNullOrBlank()) {
        val localPart = email.substringBefore("@")
        val clean = localPart.replace(Regex("[0-9]"), "")
        val separators = listOf(".", "_", "-")
        for (sep in separators) {
            if (clean.contains(sep)) {
                val parts = clean.split(sep)
                val first = parts.firstOrNull() ?: ""
                val last = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
                return Pair(
                    first.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    last.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                )
            }
        }
        
        if (clean.lowercase().endsWith("singh")) {
            val first = clean.substring(0, clean.length - 5)
            return Pair(
                first.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                "Singh"
            )
        }
        
        return Pair(clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, "")
    }
    
    return Pair("", "")
}

fun getActiveCycleLocalDate(instant: java.time.Instant): java.time.LocalDate {
    val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
    return if (zonedDateTime.hour < 4) {
        zonedDateTime.toLocalDate().minusDays(1)
    } else {
        zonedDateTime.toLocalDate()
    }
}

fun getVlogLocalDate(path: String): java.time.LocalDate? {
    val regex = Regex("\\d{13}")
    val match = regex.find(path)
    if (match != null) {
        try {
            val millis = match.value.toLong()
            val instant = java.time.Instant.ofEpochMilli(millis)
            return getActiveCycleLocalDate(instant)
        } catch (e: Exception) {
            // ignore
        }
    }
    val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
    val file = java.io.File(cleanPath)
    if (file.exists()) {
        val lastModified = file.lastModified()
        val instant = java.time.Instant.ofEpochMilli(lastModified)
        return getActiveCycleLocalDate(instant)
    }
    return null
}

fun safeParseInstant(dateStr: String?): java.time.Instant? {
    if (dateStr.isNullOrEmpty()) return null
    val cleaned = dateStr.trim().replace(" ", "T")
    try {
        return java.time.Instant.parse(cleaned)
    } catch (e: Exception) {}
    try {
        return java.time.OffsetDateTime.parse(cleaned).toInstant()
    } catch (e: Exception) {}
    try {
        return java.time.ZonedDateTime.parse(cleaned).toInstant()
    } catch (e: Exception) {}
    try {
        return java.time.LocalDateTime.parse(cleaned).atZone(java.time.ZoneId.systemDefault()).toInstant()
    } catch (e: Exception) {}
    return null
}

fun isInstantInActiveCycle(instant: java.time.Instant): Boolean {
    val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
    val target4AMToday = now.withHour(4).withMinute(0).withSecond(0).withNano(0)
    val startOfCycle = if (now.isBefore(target4AMToday)) {
        target4AMToday.minusDays(1).toInstant()
    } else {
        target4AMToday.toInstant()
    }
    val endOfCycle = startOfCycle.plus(24, java.time.temporal.ChronoUnit.HOURS)
    return !instant.isBefore(startOfCycle) && instant.isBefore(endOfCycle)
}

fun isInstantInDayCycle(instant: java.time.Instant, targetDate: java.time.LocalDate): Boolean {
    val systemZone = java.time.ZoneId.systemDefault()
    val startOfCycle = targetDate.atTime(4, 0).atZone(systemZone).toInstant()
    val endOfCycle = startOfCycle.plus(24, java.time.temporal.ChronoUnit.HOURS)
    return !instant.isBefore(startOfCycle) && instant.isBefore(endOfCycle)
}

fun isSubmissionInActiveCycle(sub: SubmissionDbItem): Boolean {
    val instant = safeParseInstant(sub.createdAt) ?: return false
    return isInstantInActiveCycle(instant)
}

fun getSubmissionLocalDate(sub: SubmissionDbItem): java.time.LocalDate? {
    val instant = safeParseInstant(sub.createdAt)
    if (instant != null) {
        return getActiveCycleLocalDate(instant)
    }
    val parts = sub.imageUrl.split("|||")
    val path = parts.getOrNull(0) ?: ""
    val localFileDate = getVlogLocalDate(path)
    if (localFileDate != null) {
        return localFileDate
    }
    return getActiveCycleLocalDate(java.time.Instant.now())
}

fun getSubmissionRelativeHour(sub: SubmissionDbItem): Int {
    val instant = safeParseInstant(sub.createdAt) ?: return 12
    val localDateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
    val rawHour = localDateTime.hour
    return (rawHour - 4 + 24) % 24
}

@Composable
fun OnboardingFlowContainer(
    onboardingFlowStep: Int,
    onOnboardingFlowStepChange: (Int) -> Unit,
    onboardingFirstName: String,
    onOnboardingFirstNameChange: (String) -> Unit,
    onboardingLastName: String,
    onOnboardingLastNameChange: (String) -> Unit,
    currentDisplayName: String,
    onCurrentDisplayNameChange: (String) -> Unit,
    user: com.finrein.pals.domain.model.User?,
    sessionManager: com.finrein.pals.data.local.SessionManager,
    onSignOut: () -> Unit,
    isDark: Boolean,
    textColor: Color,
    mutedTextColor: Color
) {
    when (onboardingFlowStep) {
        -1 -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF1C1C1C) else Color(0xFFFAF9F6)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_logo),
                        contentDescription = "Pal Yellow Cloud Logo",
                        modifier = Modifier.size(130.dp).offset(y = 30.dp),
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_envalope),
                        contentDescription = "Envelope with heart",
                        modifier = Modifier.offset(x = (-100).dp, y = 45.dp).size(50.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_moon),
                        contentDescription = "Crescent Moon",
                        modifier = Modifier.offset(x = 100.dp, y = 45.dp).size(55.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_star_1),
                        contentDescription = null,
                        modifier = Modifier.offset(x = (-115).dp, y = (-30).dp).size(36.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_star_2),
                        contentDescription = null,
                        modifier = Modifier.offset(x = (-55).dp, y = (-55).dp).size(45.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_star_3),
                        contentDescription = null,
                        modifier = Modifier.offset(x = 15.dp, y = (-65).dp).size(30.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_star_4),
                        contentDescription = null,
                        modifier = Modifier.offset(x = 85.dp, y = (-55).dp).size(45.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dm_star_5),
                        contentDescription = null,
                        modifier = Modifier.offset(x = 135.dp, y = (-20).dp).size(34.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                CircularProgressIndicator(
                    color = Color(0xFF00E676),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Checking account status...",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFFFCF6ED).copy(alpha = 0.6f) else Color(0xFF1E1C1A).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
        0 -> CreatingAccountScreen(
            firstName = "",
            lastName = "",
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
        1 -> NameInputScreen(
            firstName = onboardingFirstName,
            onFirstNameChange = onOnboardingFirstNameChange,
            lastName = onboardingLastName,
            onLastNameChange = onOnboardingLastNameChange,
            onNext = { onOnboardingFlowStepChange(2) },
            onCancel = { VlogPlayerManager.clearAll(); onSignOut() },
            isDark = isDark,
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
        2 -> NameConfirmScreen(
            firstName = onboardingFirstName,
            lastName = onboardingLastName,
            onContinue = {
                val newName = "$onboardingFirstName $onboardingLastName".trim()
                onCurrentDisplayNameChange(newName)
                user?.let {
                    sessionManager.saveUser(it.copy(displayName = newName))
                }
                onOnboardingFlowStepChange(3)
            },
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
        3 -> CreatingAccountScreen(
            firstName = onboardingFirstName,
            lastName = onboardingLastName,
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
        4 -> PermissionsScreen(
            onDone = {
                sessionManager.setHasLoggedInBefore(true)
                sessionManager.setOnboardingCompleted(true)
                onOnboardingFlowStepChange(6)
            },
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )
    }
}

@Composable
fun HomeScreen(
    user: com.finrein.pals.domain.model.User?,
    authRepository: com.finrein.pals.domain.repository.AuthRepository,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    selectedThemeColor: String = "yellow",
    onSelectedThemeColorChange: (String) -> Unit = {},
    onOnboardingCompleted: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: com.finrein.pals.presentation.home.HomeViewModel
) {
    val supabase = com.finrein.pals.PalApplication.supabase
    val isDark by rememberUpdatedState(isSystemInDarkTheme())
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    val currentUserId = remember(user) { user?.id ?: "" }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val deletedVlogsKey = "deleted_vlog_paths_$currentUserId"
    val sessionManager = remember { com.finrein.pals.data.local.SessionManager(context) }
    var onboardingFlowStep by remember {
        mutableStateOf(
            if (sessionManager.isOnboardingCompleted()) {
                6
            } else if (sessionManager.hasLoggedInBefore()) {
                4
            } else if (sessionManager.isFirstLogin()) {
                1
            } else {
                -1
            }
        )
    }
    val parsedName = remember(user) { parseName(user?.email, user?.displayName) }
    var onboardingFirstName by remember { mutableStateOf(parsedName.first) }
    var onboardingLastName by remember { mutableStateOf(parsedName.second) }

    var currentDisplayName by remember(user) {
        mutableStateOf(user?.displayName ?: user?.email?.substringBefore("@") ?: "apple_user")
    }

    val firstName = remember(currentDisplayName) {
        currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
    }

    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    val editNameFocusRequester = remember { FocusRequester() }

    var customAvatarUriString by remember { mutableStateOf(sessionManager.getAvatarUri()) }
    val avatarScope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            sessionManager.saveAvatarUri(uri.toString())
            customAvatarUriString = uri.toString()
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val uploadedUrl = uploadFileToSupabase(context, uri.toString(), "avatars")
                if (uploadedUrl.startsWith("http")) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        sessionManager.saveAvatarUri(uploadedUrl)
                        customAvatarUriString = uploadedUrl
                    }
                    try {
                        supabase.postgrest.from("user_pals")
                            .update(mapOf("user_avatar_url" to uploadedUrl)) {
                                filter {
                                    eq("user_id", currentUserId)
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    var isLoadingPals by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("pals") } // "camera" or "pals"
    var isRecordingCamera by remember { mutableStateOf(false) }
    var isCameraActiveState by remember { mutableStateOf(true) }

    val storagePermissionString = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    }

    val homePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val homeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(onboardingFlowStep) {
        if (onboardingFlowStep == 6) {
            val hasMicrophone = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val hasStorage = androidx.core.content.ContextCompat.checkSelfPermission(
                context, storagePermissionString
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val permissionsToRequest = mutableListOf<String>()
            if (!hasMicrophone) {
                permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
            }
            if (!hasStorage) {
                permissionsToRequest.add(storagePermissionString)
            }

            if (permissionsToRequest.isNotEmpty()) {
                homePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    LaunchedEffect(selectedTab, onboardingFlowStep) {
        if (onboardingFlowStep == 6 && selectedTab == "camera") {
            val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasCamera) {
                homeCameraLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }



    LaunchedEffect(selectedTab) {
        if (selectedTab == "pals" && sessionManager.isFirstLogin()) {
            sessionManager.setFirstLogin(false)
        }
        isLoadingPals = false
    }

    var showPlusMenu by remember { mutableStateOf(false) }
    var showTripleDotMenu by remember { mutableStateOf(false) }
    var showActivityScreen by remember { mutableStateOf(false) }
    var showCreatePalFlow by remember { mutableStateOf(false) }
    var showJoinPalFlow by remember { mutableStateOf(false) }

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val createPalFocusRequester = remember { FocusRequester() }

    var createPalTitleVisible by remember { mutableStateOf(false) }
    var createPalInputVisible by remember { mutableStateOf(false) }
    var createPalSizeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(showCreatePalFlow) {
        if (showCreatePalFlow) {
            createPalTitleVisible = true
            kotlinx.coroutines.delay(200)
            createPalInputVisible = true
            kotlinx.coroutines.delay(200)
            createPalSizeVisible = true
            // Request focus after progression finishes
            kotlinx.coroutines.delay(200)
            createPalFocusRequester.requestFocus()
        } else {
            createPalTitleVisible = false
            createPalInputVisible = false
            createPalSizeVisible = false
        }
    }

    LaunchedEffect(onboardingFlowStep, currentUserId) {
        if (onboardingFlowStep == 3) {
            kotlinx.coroutines.delay(2000)
            onboardingFlowStep = 4
        } else if ((onboardingFlowStep == -1 || onboardingFlowStep == 4) && currentUserId.isNotEmpty()) {
            try {
                val savedDeleted = getVlogPrefs(context).getString(deletedVlogsKey, "") ?: ""
                if (savedDeleted.isNotEmpty()) {
                    val currentDeleted = savedDeleted.split(";;;").toSet()
                    val cleanedDeleted = currentDeleted.filter { path ->
                        path.isNotEmpty() && !path.startsWith("|||")
                    }
                    getVlogPrefs(context).edit().putString(deletedVlogsKey, cleanedDeleted.joinToString(";;;")).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val supabase = com.finrein.pals.PalApplication.supabase
                val (mappings, dbSubmissions) = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val m = supabase.postgrest.from("user_pals")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<UserPalMapping>()

                    val s = supabase.postgrest.from("submissions")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<SubmissionDbItem>()
                    Pair(m, s)
                }

                val userCreatedAt = try {
                    supabase.auth.currentUserOrNull()?.createdAt
                } catch (e: Exception) {
                    null
                }
                val isReturningUserByAge = if (userCreatedAt != null) {
                    val ageMs = java.time.Instant.now().toEpochMilli() - userCreatedAt.toEpochMilliseconds()
                    ageMs > 60000
                } else {
                    false
                }

                if (mappings.isNotEmpty() || dbSubmissions.isNotEmpty() || isReturningUserByAge) {
                    var restoredName = ""
                    var restoredAvatar: String? = null
                    
                    dbSubmissions.forEach { sub ->
                        if (sub.userDisplayName.isNotEmpty()) {
                            val parsed = parseUserDisplayName(sub.userDisplayName)
                            if (parsed.first.isNotEmpty()) {
                                restoredName = parsed.first
                            }
                            if (!parsed.second.isNullOrEmpty()) {
                                restoredAvatar = parsed.second
                            }
                        }
                    }
                    
                    if (restoredName.isEmpty()) {
                        restoredName = user?.displayName ?: user?.email?.substringBefore("@") ?: "apple_user"
                    }
                    
                    currentDisplayName = restoredName
                    customAvatarUriString = restoredAvatar
                    
                    user?.let {
                        sessionManager.saveUser(it.copy(displayName = restoredName))
                    }
                    if (!restoredAvatar.isNullOrEmpty()) {
                        sessionManager.saveAvatarUri(restoredAvatar)
                    }
                    
                    sessionManager.setHasLoggedInBefore(true)
                    sessionManager.setFirstLogin(false)
                    if (onboardingFlowStep == -1) {
                        onboardingFlowStep = 4
                    }
                } else {
                    if (onboardingFlowStep == -1) {
                        onboardingFlowStep = 1
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (onboardingFlowStep == -1) {
                    onboardingFlowStep = 1
                }
            }
        }
    }

    androidx.activity.compose.BackHandler(enabled = onboardingFlowStep in 2..4) {
        if (onboardingFlowStep == 2) {
            onboardingFlowStep = 1
        }
    }

    var createPalStep by remember { mutableStateOf(1) }
    var newPalName by remember { mutableStateOf("") }
    var newPalSize by remember { mutableStateOf("3") }
    var generatedPalCode by remember { mutableStateOf("") }
    var joinPalCode by remember { mutableStateOf("") }

    var isCreatingPal by remember { mutableStateOf(false) }
    var creationDots by remember { mutableStateOf("") }



    val groupDatabase = remember {
        mutableStateMapOf<String, PalItem>().apply {
            put("hello9", PalItem(name = "Hi", size = "2", code = "hello9", isVlog = false, isCreator = false))
            put("abcd12", PalItem(name = "Cool Group", size = "4", code = "abcd12", isVlog = false, isCreator = false))
        }
    }

    var selectedThemeColor by remember(selectedThemeColor) {
        val state = mutableStateOf(selectedThemeColor)
        object : MutableState<String> by state {
            override var value: String
                get() = state.value
                set(newValue) {
                    state.value = newValue
                    onSelectedThemeColorChange(newValue)
                }
        }
    }
    val themeConfig = remember(selectedThemeColor) { PalThemes[selectedThemeColor] ?: PalThemes["yellow"]!! }
    val accentColor = themeConfig.accentColor
    val logoColor = themeConfig.logoColor
    val activeGradientColors = if (isDark) themeConfig.darkGradient else themeConfig.lightGradient

    val selectedProfileColor = remember(selectedThemeColor) {
        when (selectedThemeColor) {
            "yellow" -> Color(0xFFFFE600) // Neon Yellow
            "orange" -> Color(0xFFFF6700) // Neon Orange
            "pink" -> Color(0xFFFF007F)   // Neon Pink
            "blue" -> Color(0xFF00F0FF)   // Neon Cyan/Blue
            "purple" -> Color(0xFFB000FF) // Neon Purple
            "red" -> Color(0xFFFF073A)    // Neon Red
            else -> Color(0xFFFFE600)
        }
    }

    val palTextLogoColor = remember(selectedThemeColor) {
        val mappedColorName = when (selectedThemeColor) {
            "yellow" -> "blue"
            "blue" -> "yellow"
            "orange" -> "pink"
            "pink" -> "orange"
            "red" -> "purple"
            "purple" -> "red"
            else -> selectedThemeColor
        }
        when (mappedColorName) {
            "yellow" -> Color(0xFFFFE600)
            "blue" -> Color(0xFF00F0FF)
            "orange" -> Color(0xFFFF6700)
            "pink" -> Color(0xFFFF007F)
            "red" -> Color(0xFFFF073A)
            "purple" -> Color(0xFFB000FF)
            else -> Color(0xFFFFE600)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "smiley_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var tripleDotScreen by remember { mutableStateOf(TripleDotScreen.MAIN) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var notificationInterval by remember { mutableStateOf(sessionManager.getNotificationInterval()) }
    var userPin by remember { mutableStateOf("4A2D8B") }

    // Vlog and Group screen states
    var activeVlogPal by remember(currentUserId) { mutableStateOf<PalItem?>(null) }
    val isStateRestoredRef = remember(currentUserId) { mutableStateOf(false) }
    val saveGroupMutex = remember { Mutex() }
    var showingCapturedPreview by remember(currentUserId) { mutableStateOf(false) }
    var capturedVideoPath by remember(currentUserId) { mutableStateOf<String?>(null) }
    var capturedVideoDuration by remember(currentUserId) { mutableStateOf(2000L) }
    var capturedCaptionText by remember(currentUserId) { mutableStateOf("") }
    var capturedVideoTimeText by remember(currentUserId) { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    var initialSyncCompleted by remember { mutableStateOf(false) }

    val initialDeleted = remember(deletedVlogsKey) {
        emptySet<String>()
    }

    var capturedVlogsPaths by remember(deletedVlogsKey) {
        val savedPaths = getVlogPrefs(context).getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredPathsList = validIndices.map { paths[it] }
        mutableStateOf(ArrayList(filteredPathsList))
    }
    var capturedVlogsTimes by remember(deletedVlogsKey) {
        val savedPaths = getVlogPrefs(context).getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedTimes = getVlogPrefs(context).getString("vlog_times", "") ?: ""
        val times = if (savedTimes.isEmpty()) emptyList() else savedTimes.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredTimesList = validIndices.map { times.getOrNull(it) ?: "12:00" }
        mutableStateOf(ArrayList(filteredTimesList))
    }
    var capturedVlogsCaptions by remember(deletedVlogsKey) {
        val savedPaths = getVlogPrefs(context).getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedCaptions = getVlogPrefs(context).getString("vlog_captions", "") ?: ""
        val captions = if (savedCaptions.isEmpty()) emptyList() else savedCaptions.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredCaptionsList = validIndices.map { captions.getOrNull(it) ?: "" }
        mutableStateOf(ArrayList(filteredCaptionsList))
    }
    var capturedVlogsDurations by remember(deletedVlogsKey) {
        val savedPaths = getVlogPrefs(context).getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedDurations = getVlogPrefs(context).getString("vlog_durations", "") ?: ""
        val durations = if (savedDurations.isEmpty()) emptyList() else savedDurations.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredDurationsList = validIndices.map { durations.getOrNull(it) ?: "2000" }
        mutableStateOf(ArrayList(filteredDurationsList))
    }
    var currentPlayingIndex by remember { mutableStateOf(0) }
    var vlogPlaybackProgress by remember { mutableStateOf(0f) }
    var showVlogDropdownMenu by remember { mutableStateOf(false) }
    var showVlogChatScreen by remember { mutableStateOf(false) }
    var showEditPalFlow by remember { mutableStateOf(false) }
    var showDeletePalDialog by remember { mutableStateOf(false) }
    var showLeavePalDialog by remember { mutableStateOf(false) }
    var showVlogExportDialog by remember { mutableStateOf(false) }
     val palPalsCount = remember {
        val countsJson = getVlogPrefs(context).getString("cached_pal_pals_count", "") ?: ""
        val initialMap = if (countsJson.isNotEmpty()) {
            try {
                kotlinx.serialization.json.Json.decodeFromString<Map<String, Int>>(countsJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
        mutableStateMapOf<String, Int>().apply {
            putAll(initialMap)
        }
    }
    val palMessages by viewModel.palMessages.collectAsState()
    val allPalsSubmissions = remember {
        val submissionsJson = getVlogPrefs(context).getString("cached_all_pals_submissions", "") ?: ""
        val initialMap = if (submissionsJson.isNotEmpty()) {
            try {
                kotlinx.serialization.json.Json.decodeFromString<Map<String, List<SubmissionDbItem>>>(submissionsJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
        mutableStateMapOf<String, List<SubmissionDbItem>>().apply {
            putAll(initialMap)
        }
    }
    val allPalsMessages = remember { mutableStateMapOf<String, List<MessageDbItem>>() }
    val allPalsMembers = remember {
        val membersJson = getVlogPrefs(context).getString("cached_all_pals_members", "") ?: ""
        val initialMap = if (membersJson.isNotEmpty()) {
            try {
                kotlinx.serialization.json.Json.decodeFromString<Map<String, List<String>>>(membersJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
        mutableStateMapOf<String, List<String>>().apply {
            putAll(initialMap)
        }
    }
    val locallyDeletedPals = remember { mutableStateMapOf<String, Boolean>() }

    val createdPalsState = viewModel.createdPals.collectAsState()
    val filteredCreatedPals = remember(createdPalsState, locallyDeletedPals) {
        derivedStateOf {
            createdPalsState.value.filterNot { locallyDeletedPals.containsKey(it.code) }
        }
    }
    var createdPals by remember(viewModel) {
        object : MutableState<List<PalItem>> {
            override var value: List<PalItem>
                get() = filteredCreatedPals.value
                set(value) {
                    viewModel.updateCreatedPals(value)
                }
            override fun component1(): List<PalItem> = value
            override fun component2(): (List<PalItem>) -> Unit = { value = it }
        }
    }

    LaunchedEffect(createdPals.size) {
        val serialized = createdPals.joinToString(";;;") { "${it.name.replace(":", "\\:")}:${it.size}:${it.code}:${it.isVlog}:${it.isCreator}" }
        getVlogPrefs(context).edit().putString("created_pals", serialized).apply()
    }

    LaunchedEffect(createdPals.size, currentDisplayName, capturedVlogsPaths.size) {
        createdPals.forEach { pal ->
            launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    if (pal.isVlog) {
                        val vlogSubmissions = if (capturedVlogsPaths.isNotEmpty()) {
                            listOf(SubmissionDbItem(palCode = "vlog", userId = currentUserId, userDisplayName = currentDisplayName, imageUrl = ""))
                        } else {
                            emptyList()
                        }
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            allPalsMembers[pal.code] = listOf("only you")
                            allPalsSubmissions[pal.code] = vlogSubmissions
                        }
                    } else {
                        val dbSubs = com.finrein.pals.PalApplication.supabase.postgrest.from("submissions")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<SubmissionDbItem>()
                        
                        val todaySubs = dbSubs.filter { sub ->
                            if (sub.imageUrl == "PROFILE_AVATAR" || sub.imageUrl.startsWith("PROFILE_AVATAR")) {
                                false
                            } else {
                                var subDate: java.time.LocalDate? = null
                                if (!sub.createdAt.isNullOrEmpty()) {
                                    try {
                                        val instant = java.time.Instant.parse(sub.createdAt)
                                        subDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    } catch (e: Exception) {}
                                }
                                if (subDate == null) {
                                    val parts = sub.imageUrl.split("|||")
                                    val path = parts.getOrNull(0) ?: ""
                                    val regex = Regex("\\d{13}")
                                    val match = regex.find(path)
                                    if (match != null) {
                                        try {
                                            val millis = match.value.toLong()
                                            subDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                        } catch (e: Exception) {}
                                    }
                                }
                                subDate == java.time.LocalDate.now()
                            }
                        }

                        val mappings = com.finrein.pals.PalApplication.supabase.postgrest.from("user_pals")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<UserPalMapping>()
                            .sortedWith(compareBy({ it.createdAt ?: "" }, { it.id ?: "" }))

                        val memberList = mutableListOf<String>()
                        val addedUserIds = mutableSetOf<String>()

                        mappings.forEach { mapping ->
                            if (mapping.userId.isNotEmpty() && !addedUserIds.contains(mapping.userId)) {
                                val sub = dbSubs.firstOrNull { it.userId == mapping.userId }
                                val (displayName, avatarUrl) = if (sub != null) {
                                    parseUserDisplayName(sub.userDisplayName)
                                } else {
                                    if (mapping.userId == currentUserId) {
                                        val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
                                        Pair(firstName, localAvatar)
                                    } else {
                                        Pair("Pal", null)
                                    }
                                }
                                val formatted = "${mapping.userId}|||$displayName|||${avatarUrl ?: ""}"
                                memberList.add(formatted)
                                addedUserIds.add(mapping.userId)
                            }
                        }

                        dbSubs.forEach { sub ->
                            if (sub.userId.isNotEmpty() && !addedUserIds.contains(sub.userId)) {
                                val (displayName, avatarUrl) = parseUserDisplayName(sub.userDisplayName)
                                val formatted = "${sub.userId}|||$displayName|||${avatarUrl ?: ""}"
                                memberList.add(formatted)
                                addedUserIds.add(sub.userId)
                            }
                        }

                        if (!addedUserIds.contains(currentUserId)) {
                            val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
                            val formatted = "$currentUserId|||$firstName|||${localAvatar ?: ""}"
                            memberList.add(formatted)
                        }

                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            allPalsSubmissions[pal.code] = todaySubs
                            allPalsMembers[pal.code] = memberList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        val saved = getVlogPrefs(context).getString("created_pals", "") ?: ""
        val initialList = if (saved.isEmpty()) {
            listOf(PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true))
        } else {
            saved.split(";;;").mapNotNull { s ->
                val parts = s.split(":")
                if (parts.size < 5) null else {
                    PalItem(
                        name = parts[0].replace("\\:", ":"),
                        size = parts[1],
                        code = parts[2],
                        isVlog = parts[3].toBoolean(),
                        isCreator = parts[4].toBoolean()
                    )
                }
            }
        }
        viewModel.updateCreatedPals(initialList)
    }
    val locallyDeletedSubmissions = remember { mutableStateMapOf<String, Boolean>() }
    var selectedDayOffset by remember { mutableStateOf(0) }

    val activePalState by viewModel.activePalState.collectAsState()
    var dailyHourHistoryMap by remember { mutableStateOf<Map<Int, List<SubmissionDbItem>>>(emptyMap()) }
    var activeHourSubmissions by remember { mutableStateOf<Map<String, SubmissionDbItem>>(emptyMap()) }
    val exportMenuDataState = activePalState?.exportData ?: emptyMap()
    var activeGroupMembersList by remember { mutableStateOf<List<UserItem>>(emptyList()) }

    LaunchedEffect(activePalState) {
        activePalState?.let { state ->
            allPalsSubmissions[state.palCode] = state.submissions
            allPalsMembers[state.palCode] = state.members
            palPalsCount[state.palCode] = state.memberCount
            allPalsMessages[state.palCode] = state.messages
            dailyHourHistoryMap = state.dailyHourHistory
            activeHourSubmissions = state.activeHourSubmissions
            try {
                val jsonCount = kotlinx.serialization.json.Json.encodeToString(palPalsCount.toMap())
                getVlogPrefs(context).edit().putString("cached_pal_pals_count", jsonCount).apply()
                val jsonSubs = kotlinx.serialization.json.Json.encodeToString(allPalsSubmissions.toMap())
                getVlogPrefs(context).edit().putString("cached_all_pals_submissions", jsonSubs).apply()
                val jsonMembers = kotlinx.serialization.json.Json.encodeToString(allPalsMembers.toMap())
                getVlogPrefs(context).edit().putString("cached_all_pals_members", jsonMembers).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } ?: run {
            dailyHourHistoryMap = emptyMap()
            activeHourSubmissions = emptyMap()
        }
    }

    fun clearGroupMemoryCaches() {
        viewModel.clearActivePalState()
        allPalsMessages.clear()
    }

    // Persistent cache preserved across lifecycles and orientation changes.

    val palReactions = remember { mutableStateMapOf<String, String>() }
    var activeReplyPreviewPath by remember { mutableStateOf<String?>(null) }
    var activeReactionPreview by remember { mutableStateOf<Pair<String, String>?>(null) }

    val targetDate = remember(selectedDayOffset) {
        val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
        val activeLocalDate = if (now.hour < 4) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        activeLocalDate.minusDays(selectedDayOffset.toLong())
    }

    val activePalSubmissions = remember(activeVlogPal, capturedVlogsPaths, capturedVlogsTimes, capturedVlogsCaptions, capturedVlogsDurations, allPalsSubmissions) {
        val pal = activeVlogPal
        if (pal == null || pal.code == "vlog") {
            capturedVlogsPaths.mapIndexed { idx, path ->
                val caption = capturedVlogsCaptions.getOrNull(idx) ?: ""
                val duration = capturedVlogsDurations.getOrNull(idx) ?: "2000"
                val localPath = getVlogPrefs(context).getString("local_path_$path", null)
                val resolvedPath = localPath ?: path
                val cleanPath = if (resolvedPath.startsWith("file://")) resolvedPath.substring(7) else resolvedPath
                val file = java.io.File(cleanPath)
                val filename = java.io.File(path).name
                val matchingSub = allPalsSubmissions["vlog"]?.firstOrNull { sub ->
                    val subPath = sub.imageUrl.split("|||").firstOrNull() ?: ""
                    subPath == path || subPath.endsWith("/$filename") || (subPath.startsWith("http") && path.startsWith("http") && subPath == path)
                }
                val createdAtStr = matchingSub?.createdAt ?: if (file.exists()) {
                    java.time.Instant.ofEpochMilli(file.lastModified()).toString()
                } else {
                    java.time.Instant.now().toString()
                }
                SubmissionDbItem(
                    id = matchingSub?.id,
                    palCode = "vlog",
                    userId = currentUserId,
                    userDisplayName = currentDisplayName,
                    imageUrl = "$path|||$caption|||$duration",
                    createdAt = createdAtStr
                )
            }
        } else {
            allPalsSubmissions[pal.code] ?: emptyList()
        }
    }

    val filteredSubmissions = remember(activePalSubmissions, targetDate) {
        activePalSubmissions.filter { sub ->
            if (activeVlogPal == null || activeVlogPal?.code == "vlog") {
                val instant = safeParseInstant(sub.createdAt)
                instant != null && isInstantInDayCycle(instant, targetDate)
            } else {
                getSubmissionLocalDate(sub) == targetDate
            }
        }
    }

    val filteredPaths = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(0) ?: ""
        }
    }

    val todaySubmissionsMap = allPalsSubmissions.mapValues { (palCode, subs) ->
        val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
        val activeLocalDate = if (now.hour < 4) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        if (palCode == "vlog") {
            subs.filter { sub -> isSubmissionInActiveCycle(sub) }
        } else {
            subs.filter { sub -> getSubmissionLocalDate(sub) == activeLocalDate }
        }
    }

    val todayVlogPaths = remember(capturedVlogsPaths, capturedVlogsTimes, capturedVlogsCaptions, capturedVlogsDurations, allPalsSubmissions.toMap()) {
        capturedVlogsPaths.mapIndexedNotNull { idx, path ->
            val caption = capturedVlogsCaptions.getOrNull(idx) ?: ""
            val duration = capturedVlogsDurations.getOrNull(idx) ?: "2000"
            val localPath = getVlogPrefs(context).getString("local_path_$path", null)
            val resolvedPath = localPath ?: path
            val cleanPath = if (resolvedPath.startsWith("file://")) resolvedPath.substring(7) else resolvedPath
            val file = java.io.File(cleanPath)
            val filename = java.io.File(path).name
            val matchingSub = allPalsSubmissions["vlog"]?.firstOrNull { sub ->
                val subPath = sub.imageUrl.split("|||").firstOrNull() ?: ""
                subPath == path || subPath.endsWith("/$filename") || (subPath.startsWith("http") && path.startsWith("http") && subPath == path)
            }
            val createdAtStr = matchingSub?.createdAt ?: if (file.exists()) {
                java.time.Instant.ofEpochMilli(file.lastModified()).toString()
            } else {
                java.time.Instant.now().toString()
            }
            val sub = SubmissionDbItem(
                id = matchingSub?.id,
                palCode = "vlog",
                userId = currentUserId,
                userDisplayName = currentDisplayName,
                imageUrl = "$path|||$caption|||$duration",
                createdAt = createdAtStr
            )
            if (isSubmissionInActiveCycle(sub)) path else null
        }
    }

    val filteredTimes = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            if (!sub.createdAt.isNullOrEmpty()) {
                try {
                    val instant = safeParseInstant(sub.createdAt)
                    val zonedDateTime = instant?.atZone(java.time.ZoneId.systemDefault())
                    zonedDateTime?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US)) ?: "12:00"
                } catch (e: Exception) {
                    sub.createdAt.substringAfter("T").substringBefore(".").take(5)
                }
            } else {
                "12:00"
            }
        }
    }

    val filteredCaptions = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(1) ?: ""
        }
    }

    val filteredDurations = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(2)?.toLongOrNull() ?: 2000L
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    val vlogExoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context)
            .setRenderersFactory(
                androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                    setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                    setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                }
            )
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context))
            )
            .build().apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                volume = 0f
                playWhenReady = false
            }
    }

    LaunchedEffect(vlogExoPlayer) {
        vlogExoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                currentPlayingIndex = vlogExoPlayer.currentMediaItemIndex
            }
        })
    }

    LaunchedEffect(activeVlogPal) {
        currentPlayingIndex = 0
        if (vlogExoPlayer.mediaItemCount > 0) {
            vlogExoPlayer.seekTo(0, 0L)
        }
    }

    LaunchedEffect(filteredPaths) {
        vlogExoPlayer.stop()
        vlogExoPlayer.clearMediaItems()
        val resolved = filteredPaths.map { path ->
            ensureVideoCached(context, path)
        }
        resolved.forEach { resolvedPath ->
            if (resolvedPath.startsWith("http")) {
                vlogExoPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(resolvedPath)))
            } else {
                val cleanPath = when {
                    resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                    else -> resolvedPath
                }
                val fileTarget = java.io.File(cleanPath)
                if (fileTarget.exists() && fileTarget.length() > 0) {
                    val targetUri = android.net.Uri.fromFile(fileTarget)
                    vlogExoPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                }
            }
        }
        if (resolved.isNotEmpty()) {
            vlogExoPlayer.prepare()
        }
    }

    val isVlogTabVisible = selectedTab == "pals" && activeVlogPal == null && !showingCapturedPreview
    val shouldPlayVlogExoPlayer = isVlogTabVisible || (activeVlogPal != null && showVlogExportDialog)
    LaunchedEffect(shouldPlayVlogExoPlayer) {
        if (shouldPlayVlogExoPlayer) {
            vlogExoPlayer.playWhenReady = true
            vlogExoPlayer.play()
        } else {
            vlogExoPlayer.pause()
        }
    }

    LaunchedEffect(shouldPlayVlogExoPlayer, filteredPaths) {
        if (shouldPlayVlogExoPlayer && filteredPaths.isNotEmpty()) {
            while (true) {
                val duration = vlogExoPlayer.duration
                val position = vlogExoPlayer.currentPosition
                if (duration > 0) {
                    vlogPlaybackProgress = (position.toFloat() / duration).coerceIn(0f, 1f)
                } else {
                    vlogPlaybackProgress = 0f
                }
                delay(30L)
            }
        } else {
            vlogPlaybackProgress = 0f
        }
    }

    LaunchedEffect(showVlogExportDialog) {
        if (showVlogExportDialog) {
            currentPlayingIndex = 0
            if (vlogExoPlayer.mediaItemCount > 0) {
                vlogExoPlayer.seekTo(0, 0L)
            }
        }
    }

    DisposableEffect(vlogExoPlayer) {
        onDispose {
            vlogExoPlayer.release()
        }
    }



    var lastPhysicalIsRotated by remember { mutableStateOf<Boolean?>(null) }

    // Automatically transition selectedTab based on physical device rotation/orientation sensor
    DisposableEffect(
        onboardingFlowStep,
        selectedTab,
        showPlusMenu,
        showTripleDotMenu,
        showActivityScreen,
        showCreatePalFlow,
        showJoinPalFlow,
        showEditNameDialog,
        showDeletePalDialog,
        showLeavePalDialog,
        activeVlogPal,
        showVlogDropdownMenu,
        showVlogChatScreen,
        showEditPalFlow,
        isRecordingCamera,
        showingCapturedPreview
    ) {
        val isUserIdle = onboardingFlowStep >= 6 &&
                !showPlusMenu &&
                !showTripleDotMenu &&
                !showActivityScreen &&
                !showCreatePalFlow &&
                !showJoinPalFlow &&
                !showEditNameDialog &&
                !showDeletePalDialog &&
                !showLeavePalDialog &&
                activeVlogPal == null &&
                !showVlogDropdownMenu &&
                !showVlogChatScreen &&
                !showEditPalFlow &&
                !isRecordingCamera &&
                !showingCapturedPreview

        if (!isUserIdle) {
            return@DisposableEffect onDispose {}
        }

        val listener = object : android.view.OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val currentIsRotated = when {
                    (orientation in 60..120) || (orientation in 240..300) -> true
                    (orientation in 0..40) || (orientation in 320..359) || (orientation in 140..220) -> false
                    else -> null
                }

                if (currentIsRotated != null && currentIsRotated != lastPhysicalIsRotated) {
                    if (currentIsRotated && selectedTab == "pals") {
                        selectedTab = "camera"
                    } else if (!currentIsRotated && selectedTab == "camera") {
                        selectedTab = "pals"
                    }
                    lastPhysicalIsRotated = currentIsRotated
                }
            }
        }

        if (listener.canDetectOrientation()) {
            listener.enable()
        }

        onDispose {
            listener.disable()
        }
    }

    val supabaseClient = remember { com.finrein.pals.PalApplication.supabase }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshPals() {
        if (currentUserId.isEmpty()) return
        coroutineScope.launch {
            try {
                isRefreshing = true
                viewModel.refreshPals(currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("RPC_Dashboard_Error", "Transaction request bypassed: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refreshVlogs() {
        if (currentUserId.isEmpty()) return
        coroutineScope.launch {
            try {
                val dbSubmissions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabaseClient.postgrest.from("submissions")
                        .select {
                            filter {
                                eq("pal_code", "vlog")
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<SubmissionDbItem>()
                }
                
                val sorted = dbSubmissions
                    .filter { submission ->
                        val parts = submission.imageUrl.split("|||")
                        val path = parts.getOrNull(0) ?: ""
                        path.isNotEmpty() && !locallyDeletedSubmissions.containsKey(path) &&
                        (submission.id == null || !locallyDeletedSubmissions.containsKey(submission.id.toString()))
                    }
                    .sortedByDescending { it.createdAt ?: "" }
                
                val paths = ArrayList<String>()
                val times = ArrayList<String>()
                val captions = ArrayList<String>()
                val durations = ArrayList<String>()

                // Identify any local unsynced vlogs
                val unsyncedVlogs = capturedVlogsPaths.mapIndexedNotNull { idx, path ->
                    if (!path.startsWith("http")) {
                        val isSynced = sorted.any { sub ->
                            val subUrl = sub.imageUrl.split("|||").firstOrNull() ?: ""
                            val mappedLocal = getVlogPrefs(context).getString("local_path_$subUrl", null)
                            mappedLocal == path || (mappedLocal != null && java.io.File(mappedLocal).name == java.io.File(path).name)
                        }
                        if (!isSynced) {
                            Triple(
                                path,
                                capturedVlogsTimes.getOrNull(idx) ?: "12:00",
                                Triple(
                                    capturedVlogsCaptions.getOrNull(idx) ?: "",
                                    capturedVlogsDurations.getOrNull(idx) ?: "2000",
                                    idx
                                )
                            )
                        } else null
                    } else null
                }

                // Add unsynced vlogs first
                unsyncedVlogs.forEach { item ->
                    paths.add(item.first)
                    times.add(item.second)
                    captions.add(item.third.first)
                    durations.add(item.third.second)
                }

                sorted.forEach { sub ->
                    val parts = sub.imageUrl.split("|||")
                    val path = parts.getOrNull(0) ?: ""
                    val caption = parts.getOrNull(1) ?: ""
                    val duration = parts.getOrNull(2) ?: "2000"
                    
                    // If a database vlog is already represented by an unsynced local vlog in paths (via mapping), do not duplicate it
                    val isAlreadyAdded = paths.any { existingPath ->
                        existingPath == path || 
                        getVlogPrefs(context).getString("local_path_$path", null) == existingPath ||
                        (getVlogPrefs(context).getString("local_path_$path", null)?.let { mappedLocal ->
                            java.io.File(mappedLocal).name == java.io.File(existingPath).name
                        } ?: false)
                    }
                    if (!isAlreadyAdded) {
                        paths.add(path)
                        val timePart = if (!sub.createdAt.isNullOrEmpty()) {
                            try {
                                val instant = safeParseInstant(sub.createdAt)
                                val zonedDateTime = instant?.atZone(java.time.ZoneId.systemDefault())
                                zonedDateTime?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US)) ?: "12:00"
                            } catch (e: Exception) {
                                sub.createdAt.substringAfter("T").substringBefore(".").take(5)
                            }
                        } else {
                            "12:00"
                        }
                        times.add(timePart)
                        captions.add(caption)
                        durations.add(duration)
                    }
                }
                
                capturedVlogsPaths = paths
                capturedVlogsTimes = times
                capturedVlogsCaptions = captions
                capturedVlogsDurations = durations
                
                allPalsSubmissions["vlog"] = sorted
                
                getVlogPrefs(context).edit().apply {
                    putString("vlog_paths", paths.joinToString(";;;"))
                    putString("vlog_times", times.joinToString(";;;"))
                    putString("vlog_captions", captions.joinToString(";;;"))
                    putString("vlog_durations", durations.joinToString(";;;"))
                    apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshActivePalDetails(palCode: String) {
        if (currentUserId.isEmpty() || palCode == "vlog" || palCode.isBlank()) return
        viewModel.refreshActivePalDetails(
            palCode = palCode,
            currentUserId = currentUserId,
            currentDisplayName = currentDisplayName,
            firstName = firstName,
            currentAvatarUrl = customAvatarUriString,
            locallyDeletedSubmissions = locallyDeletedSubmissions.keys,
            resolveAvatarUrl = {
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
                avatarUrl
            }
        )
    }



    LaunchedEffect(currentUserId, lifecycleOwner) {
        if (currentUserId.isEmpty()) return@LaunchedEffect
        
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            var channel: io.github.jan.supabase.realtime.RealtimeChannel? = null
            try {
                channel = supabaseClient.channel("pals_realtime_channel")
                
                val userPalsFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "user_pals" }
                val submissionsFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "submissions" }
                val messagesFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "messages" }
                
                channel.subscribe()
                
                // USER_PALS STREAM WATCHDOG
                launch {
                    userPalsFlow.collect { action ->
                        // Drop internal structural echo events if a refresh is already processing
                        if (viewModel.globalSyncMutex.isLocked) return@collect
                        
                        try {
                            viewModel.globalSyncMutex.withLock {
                                when (action) {
                                    is PostgresAction.Insert, is PostgresAction.Delete -> {
                                        val record = when (action) {
                                            is PostgresAction.Insert -> action.record
                                            is PostgresAction.Delete -> action.oldRecord
                                            else -> null
                                        }
                                        val eventUserId = record?.get("user_id")?.jsonPrimitive?.content
                                        val eventPalCode = record?.get("pal_code")?.jsonPrimitive?.content
                                        
                                        if (eventUserId == currentUserId) {
                                            // Run sequentially under a single coroutine job to avoid thread racing
                                            refreshPals()
                                            refreshVlogs()
                                        }
                                        if (eventPalCode != null && eventPalCode == activeVlogPal?.code) {
                                            refreshActivePalDetails(eventPalCode)
                                        }
                                    }
                                    else -> android.util.Log.d("WarpGuard", "Suppressed user_pals update echo.")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WarpGuardError", "user_pals collection boundary caught: ${e.message}")
                        }
                    }
                }
                
                // SUBMISSIONS STREAM WATCHDOG
                launch {
                    submissionsFlow.collect { action ->
                        if (viewModel.globalSyncMutex.isLocked) return@collect
                        
                        try {
                            viewModel.globalSyncMutex.withLock {
                                when (action) {
                                    is PostgresAction.Insert, is PostgresAction.Delete -> {
                                        val record = when (action) {
                                            is PostgresAction.Insert -> action.record
                                            is PostgresAction.Delete -> action.oldRecord
                                            else -> null
                                        }
                                        val eventPalCode = record?.get("pal_code")?.jsonPrimitive?.content
                                        
                                        if (eventPalCode != null && eventPalCode == activeVlogPal?.code) {
                                            refreshActivePalDetails(eventPalCode)
                                        }
                                        refreshVlogs()
                                    }
                                    else -> android.util.Log.d("WarpGuard", "Suppressed submissions update echo.")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WarpGuardError", "submissions collection boundary caught: ${e.message}")
                        }
                    }
                }
                // MESSAGES STREAM WATCHDOG
                launch {
                    messagesFlow.collect { action ->
                        viewModel.handleMessageRealtimeAction(action, activeVlogPal?.code)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                channel?.let {
                    try {
                        supabaseClient.realtime.removeChannel(it)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && !initialSyncCompleted) {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val vlogPal = PalDbItem(code = "vlog", name = "vlog")
                    supabaseClient.postgrest.from("pals").upsert(vlogPal, onConflict = "pal_code")
                } catch (e: Exception) {
                    // Ignore if already exists or RLS blocks
                }
            }
            
            // ⚡ CLEAN STARTUP: No separate POST/GET/PATCH spams allowed
            try {
                // 1. Let the database handle the homescreen setup in 1 single network hop
                refreshPals() 
                
                // 2. Load your group-specific or user-specific details cleanly
                refreshVlogs()
                
                activeVlogPal?.code?.let { code ->
                    if (code != "vlog") {
                        refreshActivePalDetails(code)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StartupSync", "Clean launch bypassed: ${e.message}")
            }
            
            initialSyncCompleted = true
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val dbUserPals = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabase.postgrest.from("user_pals")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                            }
                        }
                        .decodeList<UserPalMapping>()
                }
                val dbProfile = dbUserPals.firstOrNull { !it.userDisplayName.isNullOrEmpty() || !it.userAvatarUrl.isNullOrEmpty() }
                if (dbProfile != null) {
                    val savedName = dbProfile.userDisplayName ?: ""
                    val savedAvatar = dbProfile.userAvatarUrl ?: ""
                    if (savedName.isNotEmpty()) {
                        currentDisplayName = savedName
                        sessionManager.updateDisplayName(savedName)
                    }
                    if (savedAvatar.isNotEmpty()) {
                        customAvatarUriString = savedAvatar
                        sessionManager.saveAvatarUri(savedAvatar)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val recentSub = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    supabase.postgrest.from("submissions")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                            }
                            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            limit(1)
                        }
                        .decodeSingleOrNull<SubmissionDbItem>()
                }
                if (recentSub != null) {
                    val parts = recentSub.userDisplayName.split("|||")
                    val name = parts.getOrNull(0) ?: ""
                    val avatar = parts.getOrNull(1) ?: ""
                    if (name.isNotEmpty() && (currentDisplayName.isEmpty() || currentDisplayName == "apple_user")) {
                        currentDisplayName = name
                        sessionManager.updateDisplayName(name)
                    }
                    if (avatar.isNotEmpty() && customAvatarUriString.isNullOrEmpty()) {
                        customAvatarUriString = avatar
                        sessionManager.saveAvatarUri(avatar)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                selectedTab = "pals"
                activeVlogPal = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isStateRestoredRef.value = true
            }
        }
    }

    // State saving Disabled to ensure user always starts clean on home screen
    LaunchedEffect(currentUserId, selectedTab, activeVlogPal) {
        // Disabled saving
    }

    LaunchedEffect(createdPals, activeVlogPal) {
        val currentActiveCode = activeVlogPal?.code
        if (currentActiveCode != null) {
            val matchingRealPal = createdPals.firstOrNull { it.code == currentActiveCode }
            if (matchingRealPal != null && (matchingRealPal.name != activeVlogPal?.name || matchingRealPal.size != activeVlogPal?.size)) {
                activeVlogPal = matchingRealPal
            }
        }
    }

    LaunchedEffect(activeVlogPal, showVlogChatScreen) {
        val pal = activeVlogPal
        if (pal != null) {
            if (pal.code != "vlog") {
                clearGroupMemoryCaches()
                refreshActivePalDetails(pal.code)
            }
            viewModel.refreshMessages(pal.code)
        }
    }

    var isCapturingPal by remember { mutableStateOf(false) }
    var capturingProgress by remember { mutableStateOf(0.0f) }
    var vlogMenuExpandedMembers by remember { mutableStateOf(false) }
    var vlogMenuExpandedSettings by remember { mutableStateOf(false) }
    var editPalName by remember { mutableStateOf("") }
    var editPalSize by remember { mutableStateOf("3") }
    var isEditingPalLoading by remember { mutableStateOf(false) }
    var editPalDots by remember { mutableStateOf("") }

    LaunchedEffect(isCapturingPal) {
        if (isCapturingPal) {
            capturingProgress = 0.0f
            val steps = 30
            val delayMs = 1500L / steps
            for (step in 1..steps) {
                kotlinx.coroutines.delay(delayMs)
                capturingProgress = step.toFloat() / steps
            }
            activeVlogPal?.let { pal ->
                palPalsCount[pal.code] = (palPalsCount[pal.code] ?: 0) + 1
                if (pal.code != "vlog") {
                    val localSubmission = SubmissionDbItem(
                        palCode = pal.code,
                        userId = currentUserId,
                        userDisplayName = if (!customAvatarUriString.isNullOrEmpty()) "$firstName|||$customAvatarUriString" else firstName,
                        imageUrl = "$capturedVideoPath|||||2000",
                        createdAt = java.time.Instant.now().toString()
                    )
                    val currentList = allPalsSubmissions[pal.code] ?: emptyList()
                    val newHour = (java.time.LocalTime.now().hour - 4 + 24) % 24
                    val updatedList = currentList.filterNot { sub ->
                        sub.userId == currentUserId && getSubmissionRelativeHour(sub) == newHour
                    } + localSubmission
                    allPalsSubmissions[pal.code] = updatedList
                }
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val uploadedVideoUrl = if (capturedVideoPath != null) {
                            if (pal.code == "vlog") {
                                val cleanPath = if (capturedVideoPath!!.startsWith("file://")) capturedVideoPath!!.substring(7) else capturedVideoPath!!
                                val uri = android.net.Uri.fromFile(java.io.File(cleanPath))
                                uploadPalVideoAndGetUrl(context, uri, currentUserId) ?: "dummy_image"
                            } else {
                                uploadFileToSupabase(context, capturedVideoPath!!, "pals")
                            }
                        } else {
                            "dummy_image"
                        }
                        
                        if (uploadedVideoUrl.startsWith("http") && capturedVideoPath != null) {
                            val palPrefs = context.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE)
                            palPrefs.edit().putString("local_path_$uploadedVideoUrl", capturedVideoPath).apply()
                            getVlogPrefs(context).edit().putString("local_path_$uploadedVideoUrl", capturedVideoPath).apply()
                        }
                        
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
                        
                        val formattedName = if (avatarUrl.isNotEmpty()) "$firstName|||$avatarUrl" else firstName
 
                        val cleanCode = pal.code.trim()
                        if (cleanCode.isBlank()) {
                            android.util.Log.e("SubmissionError", "Aborting upload: pal_code is empty.")
                            return@launch
                        }
                        val delimiterString = if (cleanCode == "vlog") {
                            "$uploadedVideoUrl|||$capturedCaptionText|||$capturedVideoDuration"
                        } else {
                            "$uploadedVideoUrl|||||2000"
                        }
                        val newSubmission = SubmissionDbItem(
                            palCode = cleanCode,
                            userId = currentUserId,
                            userDisplayName = formattedName,
                            imageUrl = delimiterString,
                            createdAt = java.time.Instant.now().toString()
                        )
                        try {
                            // Recreate group if deleted or missing using upsert (no pre-check select)
                            try {
                                supabaseClient.postgrest.from("pals")
                                    .upsert(PalDbItem(code = cleanCode, name = "Pals Group"), onConflict = "pal_code")
                            } catch (e: Exception) {
                                // Ignore conflict to preserve original group name
                            }

                            supabaseClient.postgrest.from("submissions").insert(newSubmission)
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (cleanCode != "vlog") {
                                    refreshActivePalDetails(cleanCode)
                                } else {
                                    refreshVlogs()
                                }
                            }
                        } catch (e: io.github.jan.supabase.exceptions.RestException) {
                            android.util.Log.e("SubmissionError", "Postgres ForeignKey block: Group $cleanCode might have been deleted.")
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                activeVlogPal = null
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            isCapturingPal = false
        }
    }

    LaunchedEffect(isEditingPalLoading) {
        if (isEditingPalLoading) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 1200) {
                editPalDots = ""
                kotlinx.coroutines.delay(300)
                editPalDots = "."
                kotlinx.coroutines.delay(300)
                editPalDots = ".."
                kotlinx.coroutines.delay(300)
                editPalDots = "..."
                kotlinx.coroutines.delay(300)
            }
            activeVlogPal?.let { oldPal ->
                val updatedPal = if (oldPal.isVlog) {
                    oldPal.copy(name = editPalName)
                } else {
                    oldPal.copy(name = editPalName, size = editPalSize, isVlog = editPalSize == "vlog")
                }
                createdPals = createdPals.map { if (it.code == oldPal.code) updatedPal else it }
                groupDatabase[oldPal.code] = updatedPal
                activeVlogPal = updatedPal

                if (oldPal.code != "vlog") {
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            supabaseClient.postgrest.from("pals").update(mapOf(
                                "name" to editPalName,
                                "size" to editPalSize
                            )) {
                                filter {
                                    eq("pal_code", oldPal.code)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            isEditingPalLoading = false
            showEditPalFlow = false
        }
    }


    var plusMenuBounds by remember { mutableStateOf<Rect?>(null) }
    var tripleDotMenuBounds by remember { mutableStateOf<Rect?>(null) }
    var joinPalBounds by remember { mutableStateOf<Rect?>(null) }
    var editNameBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(showEditNameDialog) {
        if (showEditNameDialog) {
            val parts = currentDisplayName.trim().split(" ", limit = 2)
            editFirstName = parts.getOrNull(0) ?: ""
            editLastName = parts.getOrNull(1) ?: ""
            kotlinx.coroutines.delay(150)
            editNameFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    androidx.activity.compose.BackHandler(enabled = activeVlogPal != null) {
        if (showVlogChatScreen) {
            showVlogChatScreen = false
        } else if (showVlogDropdownMenu) {
            showVlogDropdownMenu = false
        } else if (showEditPalFlow) {
            showEditPalFlow = false
        } else if (showDeletePalDialog) {
            showDeletePalDialog = false
        } else if (showLeavePalDialog) {
            showLeavePalDialog = false
        } else {
            activeVlogPal = null
            currentPlayingIndex = 0
            if (vlogExoPlayer.mediaItemCount > 0) {
                vlogExoPlayer.seekTo(0, 0L)
            }
        }
    }

    val density = LocalDensity.current
    var screenCornerRadius by remember { mutableStateOf(28.dp) }

    LaunchedEffect(context) {
        var radiusDp = 0.dp
        
        // 1. Try querying system resource dimensions (works on API 21+ and is extremely reliable on physical devices)
        try {
            val resourceId = context.resources.getIdentifier("rounded_corner_radius", "dimen", "android")
            if (resourceId > 0) {
                val radiusPx = context.resources.getDimensionPixelSize(resourceId)
                if (radiusPx > 0) {
                    radiusDp = (radiusPx / density.density).dp
                }
            }
            if (radiusDp == 0.dp) {
                val topResourceId = context.resources.getIdentifier("rounded_corner_radius_top", "dimen", "android")
                if (topResourceId > 0) {
                    val radiusPx = context.resources.getDimensionPixelSize(topResourceId)
                    if (radiusPx > 0) {
                        radiusDp = (radiusPx / density.density).dp
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore resource exception
        }

        // 2. Try window insets (API 31+) if resource dimensions weren't found or returned 0
        if (radiusDp == 0.dp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val activity = context as? android.app.Activity
            if (activity != null) {
                var rootWindowInsets = activity.window.decorView.rootWindowInsets
                var retries = 0
                while (rootWindowInsets == null && retries < 15) {
                    kotlinx.coroutines.delay(100)
                    rootWindowInsets = activity.window.decorView.rootWindowInsets
                    retries++
                }
                if (rootWindowInsets != null) {
                    val topLeftCorner = rootWindowInsets.getRoundedCorner(android.view.RoundedCorner.POSITION_TOP_LEFT)
                    if (topLeftCorner != null) {
                        val radiusPx = topLeftCorner.radius
                        if (radiusPx > 0) {
                            radiusDp = (radiusPx / density.density).dp
                        }
                    }
                }
            }
        }

        // 3. Fallback: If both system resources and insets query returned 0,
        // check if it's a typical mobile device screen (mobile aspect ratio).
        // If it is a portrait phone, default to a curved 28.dp edge, otherwise 0.dp.
        if (radiusDp == 0.dp) {
            val config = context.resources.configuration
            val isPortraitPhone = config.screenWidthDp < 600 && config.screenHeightDp > 600
            if (isPortraitPhone) {
                radiusDp = 28.dp
            } else {
                radiusDp = 0.dp
            }
        }

        screenCornerRadius = radiusDp
    }

    // Colors matching the dark AMOLED mockup and cozy light mode
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF3F4F6)
    val textColor = if (isDark) Color.White else Color.Black
    val mutedTextColor = if (isDark) Color(0xFFA3A3A3) else Color(0xFF737373)
    val circularButtonBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val circleNumBg = if (isDark) Color.White else Color.Black
    val circleNumText = if (isDark) Color.Black else Color.White
    val pillBg = if (isDark) Color(0xFF262626) else Color(0xFFE5E5E5)
    val opaqueMenuBg = if (isDark) Color(0xFF161616) else Color.White
    val navBarBgColor = if (isDark) Color(0xFF161616) else Color(0xFFE5E5E5)

    val isOverlayVisible = showPlusMenu || showTripleDotMenu || showJoinPalFlow || showEditNameDialog
    val overlayBackdropColor = if (Build.VERSION.SDK_INT >= 31) {
        if (isDark) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f)
    } else {
        backgroundColor.copy(alpha = 0.85f)
    }

    val currentBorderColor = if (onboardingFlowStep < 5) {
        Color(0xFF00E676)
    } else {
        selectedProfileColor
    }

    val currentBackgroundColor = if (onboardingFlowStep < 5) {
        if (isDark) Color(0xFF1C1C1C) else Color(0xFFFAF9F6)
    } else {
        if (isDark) Color.Black else PalBackground
    }

    val rootModifier = if (onboardingFlowStep >= 6) {
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(screenCornerRadius))
            .background(currentBackgroundColor)
            .border(2.5.dp, currentBorderColor, RoundedCornerShape(screenCornerRadius))
            .statusBarsPadding()
            .navigationBarsPadding()
    } else {
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(screenCornerRadius))
            .background(currentBackgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    }

    Box(
        modifier = rootModifier
    ) {
        val screenContent = @Composable {
            if (onboardingFlowStep < 6) {
                OnboardingFlowContainer(
                    onboardingFlowStep = onboardingFlowStep,
                    onOnboardingFlowStepChange = {
                        onboardingFlowStep = it
                        if (it == 6) {
                            onOnboardingCompleted()
                        }
                    },
                    onboardingFirstName = onboardingFirstName,
                    onOnboardingFirstNameChange = { onboardingFirstName = it },
                    onboardingLastName = onboardingLastName,
                    onOnboardingLastNameChange = { onboardingLastName = it },
                    currentDisplayName = currentDisplayName,
                    onCurrentDisplayNameChange = { currentDisplayName = it },
                    user = user,
                    sessionManager = sessionManager,
                    onSignOut = onSignOut,
                    isDark = isDark,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor
                )
            } else if (activeVlogPal != null) {
                VlogScreenContent(
                    params = VlogScreenContentParams(
                        pal = activeVlogPal!!,
                        onBack = {
                            activeVlogPal = null
                            currentPlayingIndex = 0
                            selectedDayOffset = 0
                            if (vlogExoPlayer.mediaItemCount > 0) {
                                vlogExoPlayer.seekTo(0, 0L)
                            }
                        },
                        isDark = isDark,
                        accentColor = accentColor,
                        palTextLogoColor = palTextLogoColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        activeGradientColors = activeGradientColors,
                        rotationAngle = rotationAngle,
                        palsCount = filteredPaths.size,
                        onStartCapture = { isCapturingPal = true },
                        isCapturing = isCapturingPal,
                        captureProgress = capturingProgress,
                        showDropdown = showVlogDropdownMenu,
                        onShowDropdownChange = { showVlogDropdownMenu = it },
                        showChat = showVlogChatScreen,
                        onShowChatChange = { showVlogChatScreen = it },
                        showEdit = showEditPalFlow,
                        onShowEditChange = {
                            showEditPalFlow = it
                            if (it) {
                                editPalName = activeVlogPal?.name ?: ""
                                editPalSize = activeVlogPal?.size ?: "3"
                            }
                        },
                        showDelete = showDeletePalDialog,
                        onShowDeleteChange = { showDeletePalDialog = it },
                        showLeave = showLeavePalDialog,
                        onShowLeaveChange = { showLeavePalDialog = it },
                        showExportDialog = showVlogExportDialog,
                        onShowExportDialogChange = { showVlogExportDialog = it },
                        expandedMembers = vlogMenuExpandedMembers,
                        onExpandedMembersChange = { vlogMenuExpandedMembers = it },
                        expandedSettings = vlogMenuExpandedSettings,
                        onExpandedSettingsChange = { vlogMenuExpandedSettings = it },
                        editName = editPalName,
                        onEditNameChange = { editPalName = it },
                        editSize = editPalSize,
                        onEditSizeChange = { editPalSize = it },
                        isEditingLoading = isEditingPalLoading,
                        onStartSaveEdit = { isEditingPalLoading = true },
                        editDots = editPalDots,
                        messages = palMessages[activeVlogPal!!.code] ?: emptyList(),
                        onSendMessage = { msg ->
                            val code = activeVlogPal!!.code
                            viewModel.sendMessage(code, currentUserId, msg)
                        },
                        currentDisplayName = currentDisplayName,
                        currentUserId = currentUserId,
                        onDeletePal = {
                            handleDeletePal(
                                pal = activeVlogPal,
                                currentUserId = currentUserId,
                                locallyDeletedPals = locallyDeletedPals,
                                createdPals = createdPals,
                                onCreatedPalsChange = { createdPals = it },
                                groupDatabase = groupDatabase,
                                palPalsCount = palPalsCount,
                                allPalsSubmissions = allPalsSubmissions,
                                allPalsMessages = allPalsMessages,
                                allPalsMembers = allPalsMembers,
                                viewModel = viewModel,
                                context = context,
                                coroutineScope = coroutineScope,
                                supabaseClient = supabaseClient,
                                onActiveVlogPalChange = { activeVlogPal = it },
                                onUpdateVlogState = {
                                    capturedVlogsPaths = arrayListOf()
                                    capturedVlogsTimes = arrayListOf()
                                    capturedVlogsCaptions = arrayListOf()
                                    capturedVlogsDurations = arrayListOf()
                                }
                            )
                        },
                        onLeavePal = {
                            handleLeavePal(
                                pal = activeVlogPal,
                                currentUserId = currentUserId,
                                locallyDeletedPals = locallyDeletedPals,
                                createdPals = createdPals,
                                onCreatedPalsChange = { createdPals = it },
                                palPalsCount = palPalsCount,
                                allPalsSubmissions = allPalsSubmissions,
                                allPalsMessages = allPalsMessages,
                                allPalsMembers = allPalsMembers,
                                viewModel = viewModel,
                                context = context,
                                coroutineScope = coroutineScope,
                                supabaseClient = supabaseClient,
                                onActiveVlogPalChange = { activeVlogPal = it },
                                onUpdateVlogState = {
                                    capturedVlogsPaths = arrayListOf()
                                    capturedVlogsTimes = arrayListOf()
                                    capturedVlogsCaptions = arrayListOf()
                                    capturedVlogsDurations = arrayListOf()
                                }
                            )
                        },
                        customAvatarUriString = customAvatarUriString,
                        capturedVlogsPaths = filteredPaths,
                        capturedVlogsTimes = filteredTimes,
                        capturedVlogsCaptions = filteredCaptions,
                        currentPlayingIndex = currentPlayingIndex,
                        vlogPlaybackProgress = vlogPlaybackProgress,
                        vlogExoPlayer = vlogExoPlayer,
                        onNavigateToCamera = {
                            selectedTab = "camera"
                            activeVlogPal = null
                            showVlogChatScreen = false
                        },
                        onDeleteVlog = { indexToDelete ->
                            handleDeleteVlog(
                                indexToDelete = indexToDelete,
                                filteredPaths = filteredPaths,
                                filteredTimes = filteredTimes,
                                filteredCaptions = filteredCaptions,
                                activeVlogPal = activeVlogPal,
                                currentUserId = currentUserId,
                                locallyDeletedSubmissions = locallyDeletedSubmissions,
                                allPalsSubmissions = allPalsSubmissions,
                                supabaseClient = supabaseClient,
                                authRepository = authRepository,
                                coroutineScope = coroutineScope,
                                context = context,
                                capturedVlogsPaths = capturedVlogsPaths,
                                capturedVlogsTimes = capturedVlogsTimes,
                                capturedVlogsCaptions = capturedVlogsCaptions,
                                capturedVlogsDurations = capturedVlogsDurations,
                                onUpdateVlogLists = { paths, times, captions, durations ->
                                    capturedVlogsPaths = java.util.ArrayList(paths)
                                    capturedVlogsTimes = java.util.ArrayList(times)
                                    capturedVlogsCaptions = java.util.ArrayList(captions)
                                    capturedVlogsDurations = java.util.ArrayList(durations)
                                },
                                vlogExoPlayer = vlogExoPlayer,
                                targetDate = targetDate,
                                onActiveVlogPalChange = { activeVlogPal = it },
                                onCurrentPlayingIndexChange = { currentPlayingIndex = it },
                                refreshActivePalDetails = { refreshActivePalDetails(it) }
                            )
                        },
                        onUpdateVlogCaption = { path, newCaption ->
                            handleUpdateVlogCaption(
                                targetPath = path,
                                newCaption = newCaption,
                                capturedVlogsPaths = capturedVlogsPaths,
                                capturedVlogsDurations = capturedVlogsDurations,
                                capturedVlogsCaptions = capturedVlogsCaptions,
                                onUpdateCaptionsState = { capturedVlogsCaptions = java.util.ArrayList(it) },
                                activeVlogPal = activeVlogPal,
                                currentUserId = currentUserId,
                                supabaseClient = supabaseClient,
                                coroutineScope = coroutineScope,
                                context = context
                            )
                            val palCode = activeVlogPal?.code ?: "vlog"
                            val currentSubs = allPalsSubmissions[palCode] ?: emptyList()
                            val updatedSubs = currentSubs.map { sub ->
                                if (sub.userId == currentUserId) {
                                    val parts = sub.imageUrl.split("|||")
                                    val duration = parts.getOrNull(2) ?: "2000"
                                    sub.copy(imageUrl = "$path|||$newCaption|||$duration")
                                } else {
                                    sub
                                }
                            }
                            allPalsSubmissions[palCode] = updatedSubs
                            try {
                                val jsonSubs = kotlinx.serialization.json.Json.encodeToString(allPalsSubmissions.toMap())
                                getVlogPrefs(context).edit().putString("cached_all_pals_submissions", jsonSubs).apply()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        selectedDayOffset = selectedDayOffset,
                        onSelectedDayOffsetChange = { selectedDayOffset = it },
                        allPalsSubmissions = allPalsSubmissions,
                        allPalsMembers = allPalsMembers,
                        palReactions = palReactions,
                        onEmojiReacted = { path, emoji ->
                            palReactions[path] = emoji
                            activeReactionPreview = Pair(path, emoji)
                            
                            val targetPalCode = activeVlogPal?.code
                            if (!targetPalCode.isNullOrEmpty() && targetPalCode != "vlog") {
                                val targetSub = allPalsSubmissions[targetPalCode]?.firstOrNull { sub ->
                                    sub.imageUrl.split("|||").firstOrNull() == path
                                }
                                val targetUserId = targetSub?.userId ?: ""
                                val targetUserDisplayName = targetSub?.userDisplayName?.let { parseUserDisplayName(it).first } ?: "Pal"
                                
                                val reactionContent = "REACTION|||$targetUserId|||$targetUserDisplayName|||$path|||$emoji"
                                viewModel.sendMessage(targetPalCode, currentUserId, reactionContent)
                            }
                        },
                        activeReplyPreviewPath = activeReplyPreviewPath,
                        onActiveReplyPreviewPathChange = { activeReplyPreviewPath = it },
                        activeReactionPreview = activeReactionPreview,
                        onActiveReactionPreviewChange = { activeReactionPreview = it },
                        onSendReply = { path, text ->
                            val targetPalCode = activeVlogPal?.code
                            if (!targetPalCode.isNullOrEmpty() && targetPalCode != "vlog") {
                                val targetSub = allPalsSubmissions[targetPalCode]?.firstOrNull { sub ->
                                    sub.imageUrl.split("|||").firstOrNull() == path
                                }
                                val targetUserId = targetSub?.userId ?: ""
                                val targetUserDisplayName = targetSub?.userDisplayName?.let { parseUserDisplayName(it).first } ?: "Pal"
                                
                                val replyContent = "REPLY|||$targetUserId|||$targetUserDisplayName|||$path|||$text"
                                viewModel.sendMessage(targetPalCode, currentUserId, replyContent)
                            }
                        },
                        onDeleteMessageLocal = { msgId ->
                            val activePalCode = activeVlogPal?.code
                            if (activePalCode != null) {
                                val currentMsgs = viewModel.palMessages.value[activePalCode] ?: emptyList()
                                viewModel.updatePalMessages(activePalCode, currentMsgs.filter { msgItem -> (msgItem.id?.toString() ?: "") != msgId })
                            }
                        }
                    )
            )
            } else if (selectedTab == "camera") {
                if (showingCapturedPreview) {
                    CapturedPreviewScreen(
                        isDark = isDark,
                        accentColor = accentColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        palTextLogoColor = palTextLogoColor,
                        activeVlogPal = activeVlogPal,
                        createdPals = createdPals,
                        rotationAngle = rotationAngle,
                        capturedVideoPath = capturedVideoPath,
                        capturedVlogsPaths = todayVlogPaths,
                        onClose = { showingCapturedPreview = false },
                        onSend = { caption, targetPals ->
                            val time = java.time.LocalTime.now()
                            val formattedTime = String.format("%02d:%02d", time.hour, time.minute)
                            capturedVideoTimeText = formattedTime
                            capturedCaptionText = caption

                            handleVlogSubmission(
                                caption = caption,
                                targetPals = targetPals,
                                capturedVideoPath = capturedVideoPath,
                                capturedVideoDuration = capturedVideoDuration,
                                currentUserId = currentUserId,
                                firstName = firstName,
                                customAvatarUriString = customAvatarUriString,
                                capturedVlogsPaths = capturedVlogsPaths,
                                capturedVlogsTimes = capturedVlogsTimes,
                                capturedVlogsCaptions = capturedVlogsCaptions,
                                capturedVlogsDurations = capturedVlogsDurations,
                                allPalsSubmissions = allPalsSubmissions,
                                palPalsCount = palPalsCount,
                                onUpdateVlogLists = { paths, times, captions, durations ->
                                    capturedVlogsPaths = java.util.ArrayList(paths)
                                    capturedVlogsTimes = java.util.ArrayList(times)
                                    capturedVlogsCaptions = java.util.ArrayList(captions)
                                    capturedVlogsDurations = java.util.ArrayList(durations)
                                },
                                context = context,
                                coroutineScope = coroutineScope,
                                supabaseClient = supabaseClient,
                                sessionManager = sessionManager,
                                refreshActivePalDetails = { refreshActivePalDetails(it) },
                                refreshVlogs = { refreshVlogs() },
                                onActiveVlogPalChange = { activeVlogPal = it },
                                onShowingCapturedPreviewChange = { showingCapturedPreview = it },
                                onSelectedTabChange = { selectedTab = it },
                                onUpdateAvatarUrl = { customAvatarUriString = it }
                            )
                        },
                        currentUserId = currentUserId,
                        currentDisplayName = currentDisplayName,
                        allPalsSubmissions = allPalsSubmissions,
                        customAvatarUriString = customAvatarUriString
                    )
                }
            } else {
                PalsTabScreenContent(
                    isDark = isDark,
                    palTextLogoColor = palTextLogoColor,
                    accentColor = accentColor,
                    customAvatarUriString = customAvatarUriString,
                    isLoadingPals = isLoadingPals,
                    mutedTextColor = mutedTextColor,
                    createdPals = createdPals,
                    rotationAngle = rotationAngle,
                    capturedVlogsPaths = filteredPaths,
                    currentPlayingIndex = currentPlayingIndex,
                    capturedVlogsCaptions = filteredCaptions,
                    capturedVlogsTimes = filteredTimes,
                    vlogPlaybackProgress = vlogPlaybackProgress,
                    vlogExoPlayer = vlogExoPlayer,
                    textColor = textColor,
                    allPalsMembers = allPalsMembers,
                    firstName = firstName,
                    allPalsSubmissions = todaySubmissionsMap,
                    currentUserId = currentUserId,
                    currentDisplayName = currentDisplayName,
                    circleNumBg = circleNumBg,
                    circleNumText = circleNumText,
                    onPlusClick = { showPlusMenu = !showPlusMenu },
                    onProfileClick = {
                        showTripleDotMenu = true
                        tripleDotScreen = TripleDotScreen.MAIN
                    },
                    onPalClick = { pal ->
                        currentPlayingIndex = 0
                        if (vlogExoPlayer.mediaItemCount > 0) {
                            vlogExoPlayer.seekTo(0, 0L)
                        }
                        activeVlogPal = pal
                    },
                    onCameraClick = { selectedTab = "camera" },
                    onGlobalSearchTrigger = { query ->
                        handleGlobalSearchTrigger(
                            query = query,
                            currentUserId = currentUserId,
                            currentDisplayName = currentDisplayName,
                            customAvatarUriString = customAvatarUriString,
                            createdPals = createdPals,
                            onCreatedPalsChange = { createdPals = it },
                            supabaseClient = supabaseClient,
                            coroutineScope = coroutineScope,
                            context = context,
                            refreshPals = { refreshPals() }
                        )
                    }
                )
            }

        // ----------------------------------------------------
        // 6. BOTTOM NAVIGATION PILL BAR
        // ----------------------------------------------------
        // 6. BOTTOM NAVIGATION (Capsule Bar)
        // ----------------------------------------------------
        val selectedOptionBgColor = if (isDark) Color(0xFF262626) else Color(0xFFFFFFFF)
        val navBarBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)

        if (activeVlogPal == null && onboardingFlowStep >= 6) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(navBarBgColor, RoundedCornerShape(24.dp))
                    .border(1.dp, navBarBorderColor, RoundedCornerShape(24.dp))
                    .padding(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Camera Option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == "camera") selectedOptionBgColor else Color.Transparent)
                            .clickable {
                                selectedTab = "camera"
                            }
                            .padding(horizontal = 11.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "camera",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (selectedTab == "camera") {
                                if (isDark) Color.White else Color.Black
                            } else {
                                if (isDark) Color(0xFF737373) else Color(0xFF8E8E93)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pals Option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == "pals") selectedOptionBgColor else Color.Transparent)
                            .clickable {
                                selectedTab = "pals"
                            }
                            .padding(horizontal = 11.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "pals",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (selectedTab == "pals") {
                                if (isDark) Color.White else Color.Black
                            } else {
                                if (isDark) Color(0xFF737373) else Color(0xFF8E8E93)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (onboardingFlowStep == 6) {
                val isCameraTabActive = selectedTab == "camera" && !showingCapturedPreview && activeVlogPal == null
                LaunchedEffect(isCameraTabActive) {
                    if (isCameraTabActive) {
                        isCameraActiveState = true
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isCameraTabActive) 1f else 0f)
                        .then(
                            if (isCameraTabActive) Modifier else Modifier.absoluteOffset(x = 10000.dp)
                        )
                ) {
                    CameraScreenContent(
                        isDark = isDark,
                        accentColor = accentColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        activeGradientColors = activeGradientColors,
                        selectedThemeColor = selectedThemeColor,
                        selectedProfileColor = selectedProfileColor,
                        rotationAngle = rotationAngle,
                        palTextLogoColor = palTextLogoColor,
                        isRecording = isRecordingCamera,
                        onRecordingChange = { isRecordingCamera = it },
                        onClose = { selectedTab = "pals" },
                        isCameraActive = isCameraActiveState && isCameraTabActive,
                        onCameraActiveChange = { isCameraActiveState = it },
                        cameraProviderFuture = cameraProviderFuture,
                        previewView = previewView,
                        onCaptureSuccess = { path, duration ->
                            capturedVideoPath = path
                            capturedVideoDuration = duration
                            showingCapturedPreview = true
                        }
                    )
                }
            }
            screenContent()
        }

        val activeOverlayBounds = when {
            showEditNameDialog -> editNameBounds
            else -> null
        }
        val activeCornerRadius = 24.dp

        if (activeOverlayBounds != null) {
            val density = LocalDensity.current
            val shape = remember(activeOverlayBounds, activeCornerRadius) {
                CardBoundsShape(
                    left = activeOverlayBounds.left,
                    top = activeOverlayBounds.top,
                    right = activeOverlayBounds.right,
                    bottom = activeOverlayBounds.bottom,
                    cornerRadius = with(density) { activeCornerRadius.toPx() }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(30.dp)
                    .graphicsLayer {
                        clip = true
                        this.shape = shape
                    }
            ) {
                screenContent()
            }
        }

        // ----------------------------------------------------
        // 7. FLOATING MENUS & OVERLAYS (Delegated to sub-composable to avoid JIT instruction limit crash)
        // ----------------------------------------------------
        if (initialSyncCompleted) {
            HomeScreenOverlays(
                showPlusMenu = showPlusMenu,
                onShowPlusMenuChange = { showPlusMenu = it },
                showTripleDotMenu = showTripleDotMenu,
                onShowTripleDotMenuChange = { showTripleDotMenu = it },
                showActivityScreen = showActivityScreen,
                onShowActivityScreenChange = { showActivityScreen = it },
                showCreatePalFlow = showCreatePalFlow,
                onShowCreatePalFlowChange = { showCreatePalFlow = it },
                showJoinPalFlow = showJoinPalFlow,
                onShowJoinPalFlowChange = { showJoinPalFlow = it },
                showEditNameDialog = showEditNameDialog,
                onShowEditNameDialogChange = { showEditNameDialog = it },
                tripleDotScreen = tripleDotScreen,
                onTripleDotScreenChange = { tripleDotScreen = it },
                editFirstName = editFirstName,
                onEditFirstNameChange = { editFirstName = it },
                editLastName = editLastName,
                onEditLastNameChange = { editLastName = it },
                editNameFocusRequester = editNameFocusRequester,
                tripleDotMenuBounds = tripleDotMenuBounds,
                onTripleDotMenuBoundsChange = { tripleDotMenuBounds = it },
                createPalStep = createPalStep,
                onCreatePalStepChange = { createPalStep = it },
                joinPalCode = joinPalCode,
                onJoinPalCodeChange = { joinPalCode = it },
                newPalName = newPalName,
                onNewPalNameChange = { newPalName = it },
                newPalSize = newPalSize,
                onNewPalSizeChange = { newPalSize = it },
                generatedPalCode = generatedPalCode,
                onGeneratedPalCodeChange = { generatedPalCode = it },
                creationDots = creationDots,
                onCreationDotsChange = { creationDots = it },
                isCreatingPal = isCreatingPal,
                onIsCreatingPalChange = { isCreatingPal = it },
                createPalFocusRequester = createPalFocusRequester,
                groupDatabase = groupDatabase,
                createdPals = createdPals,
                onCreatedPalsChange = { createdPals = it },
                activeVlogPal = activeVlogPal,
                onActiveVlogPalChange = { activeVlogPal = it },
                currentUserId = currentUserId,
                currentDisplayName = currentDisplayName,
                onCurrentDisplayNameChange = { newName ->
                    currentDisplayName = newName
                    sessionManager.updateDisplayName(newName)
                    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            supabase.postgrest.from("user_pals")
                                .update(mapOf("user_display_name" to newName)) {
                                    filter {
                                        eq("user_id", currentUserId)
                                    }
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                customAvatarUriString = customAvatarUriString,
                onCustomAvatarUriStringChange = { customAvatarUriString = it },
                notificationInterval = notificationInterval,
                onNotificationIntervalChange = { interval ->
                    notificationInterval = interval
                    sessionManager.saveNotificationInterval(interval)
                    com.finrein.pals.notification.PalAlarmScheduler.updateScheduling(context, interval)
                },
                selectedThemeColor = selectedThemeColor,
                onSelectedThemeColorChange = { selectedThemeColor = it },
                themeConfig = themeConfig,
                accentColor = accentColor,
                palTextLogoColor = palTextLogoColor,
                backgroundColor = backgroundColor,
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                navBarBgColor = navBarBgColor,
                overlayBackdropColor = overlayBackdropColor,
                isDark = isDark,
                photoPickerLauncher = photoPickerLauncher,
                sessionManager = sessionManager,
                refreshPals = { refreshPals() },
                refreshActivePalDetails = { refreshActivePalDetails(it) },
                onSignOut = onSignOut,
                onDeleteAccount = onDeleteAccount,
                context = context,
                coroutineScope = coroutineScope,
                supabaseClient = supabaseClient,
                allPalsMessages = allPalsMessages,
                editNameBounds = editNameBounds,
                onEditNameBoundsChange = { editNameBounds = it },
                onSaveGroupClick = { newGroupName, _ -> 
                    coroutineScope.launch {
                        if (!saveGroupMutex.tryLock()) return@launch
                        try {
                            isCreatingPal = true
                            activeHourSubmissions = emptyMap()
                            dailyHourHistoryMap = emptyMap()
                            allPalsMessages.clear()

                            withContext(Dispatchers.IO) {
                                // 1. Generate unique 6-character alphanumeric code on the client side
                                val allowedChars = ('a'..'z') + ('0'..'9')
                                val guaranteedServerCode = (1..6)
                                    .map { allowedChars.random() }
                                    .joinToString("")

                                // 2. Build the DB item with explicit code and size
                                val newPalPayload = PalDbItem(
                                    code = guaranteedServerCode,
                                    name = newGroupName,
                                    size = newPalSize
                                )
                                
                                // 3. Execute insert
                                supabaseClient.postgrest.from("pals").insert(newPalPayload)

                                // 4. Attach the creator mapping passing user_display_name and user_avatar_url to satisfy constraints
                                val newMapping = UserPalMapping(
                                    palCode = guaranteedServerCode, 
                                    userId = currentUserId,
                                    userDisplayName = if (currentDisplayName.isNotEmpty()) currentDisplayName else "Pal Member",
                                    userAvatarUrl = customAvatarUriString
                                )
                                supabaseClient.postgrest.from("user_pals").insert(newMapping)

                                withContext(Dispatchers.Main) {
                                    val freshPalItem = PalItem(
                                        name = newGroupName,
                                        size = newPalSize, 
                                        code = guaranteedServerCode,
                                        isVlog = false,
                                        isCreator = true
                                    )
                                    val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
                                    allPalsMembers[guaranteedServerCode] = listOf("$currentUserId|||${if (currentDisplayName.isNotEmpty()) currentDisplayName else "Pal Member"}|||${localAvatar ?: ""}")
                                    createdPals = createdPals + freshPalItem

                                    activeVlogPal = freshPalItem
                                    generatedPalCode = guaranteedServerCode
                                    createPalStep = 2
                                    isCreatingPal = false
                                    
                                    refreshPals() 
                                    refreshActivePalDetails(guaranteedServerCode)
                                }
                            }
                            
                        } catch (e: Exception) {
                            android.util.Log.e("TriggerGroupSync", "Pipeline creation error resolved: ${e.message}")
                            withContext(Dispatchers.Main) {
                                isCreatingPal = false
                            }
                        } finally {
                            saveGroupMutex.unlock()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DropdownMenuItem(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    rightIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    textColor: Color,
    mutedTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontFamily = FontFamily.SansSerif,
                color = textColor,
                fontWeight = FontWeight.Normal
            )
        }
        if (rightIcon != null) {
            rightIcon()
        }
    }
}

@Composable
fun CameraIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Camera body top notch
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.05f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
        
        // Camera main body border
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.75f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Camera lens circle
        drawCircle(
            color = tint,
            radius = h * 0.22f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.575f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 24.dp,
    blurRadius: Dp = 24.dp,
    isDark: Boolean = isSystemInDarkTheme(),
    gradientColors: List<Color>? = null,
    borderColor: Color? = null,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    onClickEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Glow Layer (renders behind)
        if (glowColor != null && glowColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp)
                    .background(glowColor, shape = RoundedCornerShape(borderRadius))
            )
        }

        // 2. Main Card Background, Border, Clickable & Clip
        val cardShape = RoundedCornerShape(borderRadius)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors ?: if (isDark) {
                            listOf(
                                Color(0xFF231427),
                                Color(0xFF170D1A)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFEEFA),
                                Color(0xFFFFDBF5)
                            )
                        }
                    ),
                    shape = cardShape
                )
                .clip(cardShape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(enabled = onClickEnabled, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .border(
                    width = 1.dp,
                    brush = if (borderColor != null) {
                        Brush.verticalGradient(
                            colors = listOf(
                                borderColor.copy(alpha = 0.55f),
                                Color.Transparent,
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.15f)
                            )
                        )
                    } else if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    },
                    shape = cardShape
                )
        )

        // 3. Crisp Content Layer (defines parent size if not constrained)
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun SadSmileyIcon(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF35F38),
    elementColor: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        
        // Draw background circle
        drawCircle(
            color = backgroundColor,
            radius = sizePx / 2f,
            center = centerOffset
        )
        
        // Eyes
        val eyeRadius = sizePx * 0.08f
        val eyeY = centerOffset.y - sizePx * 0.12f
        val leftEyeX = centerOffset.x - sizePx * 0.18f
        val rightEyeX = centerOffset.x + sizePx * 0.18f
        
        drawCircle(
            color = elementColor,
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(leftEyeX, eyeY)
        )
        drawCircle(
            color = elementColor,
            radius = eyeRadius,
            center = androidx.compose.ui.geometry.Offset(rightEyeX, eyeY)
        )
        
        // Sad Mouth (downturned arc)
        val mouthWidth = sizePx * 0.35f
        val mouthHeight = sizePx * 0.2f
        val mouthLeft = centerOffset.x - mouthWidth / 2f
        val mouthTop = centerOffset.y + sizePx * 0.05f
        
        drawArc(
            color = elementColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(mouthLeft, mouthTop),
            size = androidx.compose.ui.geometry.Size(mouthWidth, mouthHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = sizePx * 0.07f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
fun ChevronLeftIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.6f, h * 0.25f)
            lineTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.6f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun ChevronDownIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.4f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.4f)
        }
        drawPath(
            path = path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun ShareIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        val pathBox = Path().apply {
            moveTo(w * 0.2f, h * 0.4f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.8f, h * 0.85f)
            lineTo(w * 0.8f, h * 0.4f)
        }
        drawPath(
            path = pathBox,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        val arrowPath = Path().apply {
            moveTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.15f)
            moveTo(w * 0.3f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.7f, h * 0.35f)
        }
        drawPath(
            path = arrowPath,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun ChatBubbleIcon(tint: Color, modifier: Modifier = Modifier, filled: Boolean = false) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        val path = Path().apply {
            val r = w * 0.15f
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            quadraticBezierTo(w, 0f, w, r)
            lineTo(w, h * 0.65f)
            quadraticBezierTo(w, h * 0.8f, w - r, h * 0.8f)
            lineTo(w * 0.4f, h * 0.8f)
            lineTo(w * 0.2f, h)
            lineTo(w * 0.25f, h * 0.8f)
            lineTo(r, h * 0.8f)
            quadraticBezierTo(0f, h * 0.8f, 0f, h * 0.65f)
            lineTo(0f, r)
            quadraticBezierTo(0f, 0f, r, 0f)
        }
        drawPath(
            path = path,
            color = tint,
            style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun CalendarIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Main box
        val pathBox = Path().apply {
            moveTo(w * 0.15f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(
            path = pathBox,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Header line
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.45f),
            end = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.45f),
            strokeWidth = strokeWidth
        )
        
        // Two binders
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.15f),
            end = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.3f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.15f),
            end = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.3f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun BarcodeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Draw 4 vertical lines of varying thickness
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.8f),
            strokeWidth = strokeWidth * 2
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.8f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.8f),
            strokeWidth = strokeWidth * 3
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.2f),
            end = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.8f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun TrashIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Lid line
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.25f),
            end = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.25f),
            strokeWidth = strokeWidth
        )
        
        // Can body
        val pathBody = Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.3f, h * 0.85f)
            lineTo(w * 0.7f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.25f)
        }
        drawPath(
            path = pathBody,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Lid handle
        val pathLid = Path().apply {
            moveTo(w * 0.4f, h * 0.25f)
            lineTo(w * 0.4f, h * 0.15f)
            lineTo(w * 0.6f, h * 0.15f)
            lineTo(w * 0.6f, h * 0.25f)
        }
        drawPath(
            path = pathLid,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

fun saveVideoToGallery(context: android.content.Context, filePath: String): Boolean {
    try {
        val file = java.io.File(filePath)
        if (!file.exists()) return false

        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, "Pal_vlog_${System.currentTimeMillis()}.mp4")
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/Pal")
                put(android.provider.MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val videoUri = resolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false

        resolver.openOutputStream(videoUri).use { out ->
            if (out == null) return false
            java.io.FileInputStream(file).use { input ->
                input.copyTo(out)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(videoUri, contentValues, null, null)
        }
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

@Composable
fun ExportMenuButton(
    icon: @Composable () -> Unit,
    label: String,
    isPrimary: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val circleBg = if (isPrimary) {
            if (isDark) Color.White else Color.Black
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        }
        val labelColor = if (isPrimary) {
            if (isDark) Color.White else Color.Black
        } else {
            if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .size(49.dp)
                .clip(CircleShape)
                .background(circleBg)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            color = labelColor
        )
    }
}

@Composable
fun EditIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        val pathPen = Path().apply {
            moveTo(w * 0.7f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.3f)
            lineTo(w * 0.4f, h * 0.75f)
            lineTo(w * 0.2f, h * 0.8f)
            lineTo(w * 0.25f, h * 0.6f)
            close()
        }
        drawPath(
            path = pathPen,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun ExitIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Door frame
        val pathDoor = Path().apply {
            moveTo(w * 0.6f, h * 0.2f)
            lineTo(w * 0.2f, h * 0.2f)
            lineTo(w * 0.2f, h * 0.8f)
            lineTo(w * 0.6f, h * 0.8f)
        }
        drawPath(
            path = pathDoor,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Arrow
        val pathArrow = Path().apply {
            moveTo(w * 0.4f, h * 0.5f)
            lineTo(w * 0.8f, h * 0.5f)
            moveTo(w * 0.65f, h * 0.35f)
            lineTo(w * 0.8f, h * 0.5f)
            lineTo(w * 0.65f, h * 0.65f)
        }
        drawPath(
            path = pathArrow,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun PersonIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Head
        drawCircle(
            color = tint,
            radius = w * 0.15f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Body arc
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.75f)
            quadraticBezierTo(w * 0.5f, h * 0.55f, w * 0.75f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        
        // Center circle
        drawCircle(
            color = tint,
            radius = w * 0.15f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Outer teeth
        val numTeeth = 8
        for (i in 0 until numTeeth) {
            val angle = i * (2 * Math.PI / numTeeth)
            val cos = Math.cos(angle).toFloat()
            val sin = Math.sin(angle).toFloat()
            
            val innerX = w * 0.5f + (w * 0.22f) * cos
            val innerY = h * 0.5f + (h * 0.22f) * sin
            
            val outerX = w * 0.5f + (w * 0.38f) * cos
            val outerY = h * 0.5f + (h * 0.38f) * sin
            
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(innerX, innerY),
                end = androidx.compose.ui.geometry.Offset(outerX, outerY),
                strokeWidth = strokeWidth * 1.5f
            )
        }
    }
}

@Composable
fun LightningBoltIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8f * density
        val path = Path().apply {
            moveTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.25f, h * 0.55f)
            lineTo(w * 0.5f, h * 0.55f)
            lineTo(w * 0.45f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.45f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun FlipCameraIcon(tint: Color, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.swap_vertical_circle_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
        contentDescription = "Flip Camera",
        tint = tint,
        modifier = modifier
    )
}

enum class TimerMode {
    DEFAULT,
    TIMER_3S,
    TIMER_5S,
    TIMELAPSE
}

@Composable
fun TimerIcon(tint: Color, modifier: Modifier = Modifier, resId: Int = R.drawable.timer_24dp_1f1f1f_fill0_wght400_grad0_opsz24) {
    Icon(
        painter = painterResource(id = resId),
        contentDescription = "Timer",
        tint = tint,
        modifier = modifier
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isCameraFlipped: Boolean = false,
    isFlashOn: Boolean = false,
    linearZoom: Float = 0f,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    isCameraActive: Boolean = true,
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    onVideoCaptureCreated: (VideoCapture<Recorder>?) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    
    LaunchedEffect(scaleType) {
        previewView.scaleType = scaleType
    }
    
    var activeCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    
    LaunchedEffect(isCameraFlipped, isCameraActive) {
        val cameraProvider = withContext(kotlinx.coroutines.Dispatchers.IO) {
            cameraProviderFuture.get()
        }
        if (!isCameraActive) {
            try {
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            activeCamera = null
            onVideoCaptureCreated(null)
            return@LaunchedEffect
        }
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val videoCapture = VideoCapture.Builder(recorder)
            .setSurfaceProcessingForceEnabled()
            .build()
        onVideoCaptureCreated(videoCapture)
        
        val cameraSelector = if (isCameraFlipped) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            activeCamera = camera
        } catch (exc: Exception) {
            exc.printStackTrace()
            onVideoCaptureCreated(null)
        }
    }

    LaunchedEffect(activeCamera, isFlashOn) {
        val camera = activeCamera ?: return@LaunchedEffect
        try {
            if (camera.cameraInfo.hasFlashUnit()) {
                camera.cameraControl.enableTorch(isFlashOn)
            }
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    val lastZoomTime = remember { longArrayOf(0L) }
    LaunchedEffect(activeCamera, linearZoom) {
        val camera = activeCamera ?: return@LaunchedEffect
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastZoomTime[0]
        if (elapsed >= 32) {
            lastZoomTime[0] = currentTime
            try {
                camera.cameraControl.setLinearZoom(linearZoom)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        } else {
            delay(32 - elapsed)
            lastZoomTime[0] = System.currentTimeMillis()
            try {
                camera.cameraControl.setLinearZoom(linearZoom)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }
    }
    
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@SuppressLint("MissingPermission")
@Composable
fun CameraScreenContent(
    isDark: Boolean,
    accentColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    activeGradientColors: List<Color>,
    selectedThemeColor: String,
    selectedProfileColor: Color,
    rotationAngle: Float,
    palTextLogoColor: Color,
    isRecording: Boolean,
    onRecordingChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    isCameraActive: Boolean = true,
    onCameraActiveChange: (Boolean) -> Unit = {},
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    onCaptureSuccess: (String, Long) -> Unit
) {
    val context = LocalContext.current
    val darkShadeColor = remember(selectedThemeColor) {
        when (selectedThemeColor) {
            "yellow" -> Color(0xFF5E450A)
            "orange" -> Color(0xFF5E0B00)
            "green" -> Color(0xFF005E2B)
            "blue" -> Color(0xFF003366)
            "purple" -> Color(0xFF3B0B66)
            "red" -> Color(0xFF5E1B15)
            else -> Color(0xFF5E450A)
        }
    }
    val lighterShadeColor = remember(selectedThemeColor) {
        when (selectedThemeColor) {
            "yellow" -> Color(0xFFFFF176)
            "orange" -> Color(0xFFFFB74D)
            "green" -> Color(0xFF81C784)
            "blue" -> Color(0xFF64B5F6)
            "purple" -> Color(0xFFBA68C8)
            "red" -> Color(0xFFE57373)
            else -> Color(0xFFFFF176)
        }
    }
    var activeSlot by remember { mutableStateOf(1) }
    var linearZoom by remember { mutableStateOf(0f) }
    var activeTimerMode by remember { mutableStateOf(TimerMode.DEFAULT) }
    var flashMode by remember { mutableStateOf("off") } // "off", "on", "auto"
    var isCameraFlipped by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableStateOf(0.0f) }
    var countdownSeconds by remember { mutableStateOf(0) }
    var videoCaptureRef by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecordingSession by remember { mutableStateOf<Recording?>(null) }

    LaunchedEffect(isCameraActive) {
        if (isCameraActive) {
            linearZoom = 0f
            activeSlot = 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecordingSession?.stop()
        }
    }

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds -= 1
            if (countdownSeconds == 0) {
                onRecordingChange(true)
            }
        }
    }

    var currentTimeText by remember {
        val initialTime = java.time.LocalTime.now()
        mutableStateOf(String.format("%02d:%02d", initialTime.hour, initialTime.minute))
    }
    LaunchedEffect(Unit) {
        while (true) {
            val time = java.time.LocalTime.now()
            currentTimeText = String.format("%02d:%02d", time.hour, time.minute)
            delay(1000L)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val videoCapture = videoCaptureRef
            if (videoCapture == null) {
                android.util.Log.e("ProductionCamera", "VideoCapture is null! Reverting to fallback.")
                onRecordingChange(false)
                onCaptureSuccess("", 0L)
                return@LaunchedEffect
            }

            val durationMs = when (activeTimerMode) {
                TimerMode.DEFAULT -> 2000L
                TimerMode.TIMER_3S -> 3000L
                TimerMode.TIMER_5S -> 5000L
                TimerMode.TIMELAPSE -> 10000L
            }

            val outputFile = java.io.File(
                context.cacheDir,
                "Pal_REC_${System.currentTimeMillis()}.mp4"
            )
            val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()

            val mainExecutor = ContextCompat.getMainExecutor(context)
            var session: Recording? = null
            val recordingStarted = kotlinx.coroutines.CompletableDeferred<Unit>()
            try {
                val pendingRecording = videoCapture.output.prepareRecording(context, fileOutputOptions)
                val hasAudioPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val recording = if (hasAudioPermission) {
                    pendingRecording.withAudioEnabled()
                } else {
                    pendingRecording
                }
                session = recording.start(mainExecutor) { recordEvent ->
                        when (recordEvent) {
                            is VideoRecordEvent.Start -> {
                                recordingStarted.complete(Unit)
                            }
                            is VideoRecordEvent.Finalize -> {
                                val fileExists = outputFile.exists() && outputFile.length() > 0
                                if (fileExists && (!recordEvent.hasError() || recordEvent.error == VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE)) {
                                    android.util.Log.d("ProductionCamera", "Video encoding success: ${outputFile.absolutePath}")
                                    onCaptureSuccess(outputFile.absolutePath, durationMs)
                                } else {
                                    android.util.Log.e("ProductionCamera", "Encoder finalized with error: ${recordEvent.error}, fileExists: $fileExists")
                                    recordingStarted.completeExceptionally(java.lang.RuntimeException("Recording failed to start"))
                                    onCaptureSuccess("", 0L)
                                }
                            }
                        }
                    }
                activeRecordingSession = session
            } catch (e: Exception) {
                android.util.Log.e("ProductionCamera", "Error starting recording", e)
                onCaptureSuccess("", 0L)
                onRecordingChange(false)
                return@LaunchedEffect
            }

            try {
                recordingStarted.await()
                recordingProgress = 0.0f
                val steps = 50
                val delayMs = durationMs / steps
                for (step in 1..steps) {
                    delay(delayMs)
                    recordingProgress = step.toFloat() / steps
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductionCamera", "Error during recording progress", e)
            } finally {
                session.stop()
                activeRecordingSession = null
                onRecordingChange(false)
                recordingProgress = 0.0f
            }
        } else {
            activeRecordingSession?.stop()
            activeRecordingSession = null
            recordingProgress = 0.0f
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val scaleHeight = maxHeight.value / 620f
        val scaleWidth = (screenWidth.value * 0.85f) / 306f
        val scale = scaleHeight.coerceAtMost(scaleWidth).coerceAtMost(1.1f)


        val progressWidth = 7.5.dp

        // Precise positioning constants for taller layout
        val shutterBottomMargin = 67.5.dp
        val shutterSize = 59.dp * scale
        val cardBottomPadding = shutterBottomMargin + (shutterSize / 2f)
        val cameraFrameBottomPadding = cardBottomPadding - 2.5.dp

        // Dynamically calculate camera frame size to be exactly 7.5dp spaced from both ends of the screen
        var cameraWidth = screenWidth - 15.dp
        var cameraHeight = cameraWidth * (16f / 9f)

        val danceInnerColors = remember {
            listOf(
                Color(0xFFFF007F), // Neon Pink
                Color(0xFF00F0FF), // Neon Cyan
                Color(0xFF39FF14), // Neon Green
                Color(0xFFFF073A), // Neon Red
                Color(0xFFFFE600), // Neon Yellow
                Color(0xFFFF6700), // Neon Orange
                Color(0xFFB000FF)  // Neon Purple
            )
        }
        val colorIndex = if (isRecording) {
            val absAngle = if (rotationAngle < 0f) -rotationAngle else rotationAngle
            ((absAngle / 10f).toInt()) % 7
        } else {
            null
        }
        val currentInnerColor = if (colorIndex != null) danceInnerColors[colorIndex] else lighterShadeColor

        val startCaptureAction = {
            if (!isRecording && countdownSeconds == 0) {
                if (activeTimerMode == TimerMode.DEFAULT) {
                    onRecordingChange(true)
                } else {
                    countdownSeconds = 3
                }
            }
        }

        // Camera Viewfinder Box (9:16 rounded card) wrapped in a clean container (no glow/shadow)
        val cameraViewShape = RoundedCornerShape(32.dp * scale)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = cameraFrameBottomPadding)
                .width(cameraWidth)
                .height(cameraHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cameraViewShape)
                    .border(
                        BorderStroke(width = 1.5.dp, color = selectedProfileColor.copy(alpha = 0.5f)),
                        shape = cameraViewShape
                    )
            ) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxSize(),
                    borderRadius = 32.dp * scale,
                    isDark = isDark,
                    gradientColors = if (isDark) listOf(Color(0xFF161616), Color(0xFF161616)) else listOf(Color(0xFFEBEBEB), Color(0xFFEBEBEB)),
                    borderColor = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(cameraViewShape)
                    ) {
                     // Actual Camera Preview Feed!
                     CameraPreview(
                         modifier = Modifier.fillMaxSize(),
                         isCameraFlipped = isCameraFlipped,
                         isFlashOn = (flashMode == "on") || (flashMode == "auto" && isRecording),
                         linearZoom = linearZoom,
                         isCameraActive = isCameraActive,
                         cameraProviderFuture = cameraProviderFuture,
                         previewView = previewView,
                         onVideoCaptureCreated = { videoCaptureRef = it }
                     )
 
                     // Zoom Selector Options (1 to 5) inside the camera frame
                     Row(
                         modifier = Modifier
                             .align(Alignment.BottomCenter)
                             .padding(bottom = 59.dp * scale)
                             .fillMaxWidth()
                             .padding(horizontal = 48.dp * scale),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         (1..5).forEach { slot ->
                             val isSelected = activeSlot == slot
                             Box(
                                 modifier = Modifier
                                     .size(36.dp * scale)
                                     .clip(CircleShape)
                                     .clickable { 
                                         activeSlot = slot 
                                         linearZoom = (slot - 1) / 4f
                                     },
                                 contentAlignment = Alignment.Center
                             ) {
                                 Text(
                                     text = slot.toString(),
                                     color = if (isSelected) selectedProfileColor else Color.White,
                                     fontSize = (18 * scale).sp,
                                     fontWeight = FontWeight.Bold,
                                     modifier = Modifier.rotate(-90f)
                                 )
                             }
                         }
                     }

                    // Timelapse Recording Overlay
                    if (isRecording && activeTimerMode == TimerMode.TIMELAPSE) {
                        val flashTransition = rememberInfiniteTransition(label = "timelapse_flash")
                        val flashAlpha by flashTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "flash"
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp * scale),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp * scale)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = flashAlpha))
                                )
                                Text(
                                    text = "TIMELAPSE",
                                    color = Color.White,
                                    fontSize = (11 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            offset = androidx.compose.ui.geometry.Offset(1f * scale, 1f * scale),
                                            blurRadius = 2f * scale
                                        )
                                    )
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 54.dp * scale),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val currentFrame = ((recordingProgress * 10).toInt() + 1).coerceIn(1, 10)
                            Text(
                                text = "Capturing frame $currentFrame/10...",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = (12 * scale).sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        offset = androidx.compose.ui.geometry.Offset(1f * scale, 1f * scale),
                                        blurRadius = 3f * scale
                                    )
                                )
                            )
                        }
                    }

                    // Countdown Timer Overlay (displays 3 2 1 center Bricolage/Roboto regular text)
                    if (countdownSeconds > 0) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = countdownSeconds.toString(),
                                color = Color.White,
                                fontSize = (48 * scale).sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = RobotoFontFamily
                            )
                        }
                    }

                    // Vertical Present Time text (single text block rotated 90 degrees clockwise, centered, Bricolage font)
                    if (countdownSeconds == 0) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val parts = currentTimeText.split(":")
                            if (parts.size == 2) {
                                val hour = parts[0]
                                val minutes = parts[1]
                                val density = LocalDensity.current
                                Row(
                                    modifier = Modifier.rotate(90f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp * scale)
                                ) {
                                    Text(
                                        text = hour,
                                        fontFamily = DelaGothicOneFontFamily,
                                        fontSize = (28 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.4f),
                                                offset = androidx.compose.ui.geometry.Offset(2f * scale, 2f * scale),
                                                blurRadius = 4f * scale
                                            )
                                        )
                                    )
                                    Text(
                                        text = ":",
                                        fontFamily = DelaGothicOneFontFamily,
                                        fontSize = (28 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White,
                                        modifier = Modifier,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.4f),
                                                offset = androidx.compose.ui.geometry.Offset(2f * scale, 2f * scale),
                                                blurRadius = 4f * scale
                                            )
                                        )
                                    )
                                    Text(
                                        text = minutes,
                                        fontFamily = DelaGothicOneFontFamily,
                                        fontSize = (28 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.4f),
                                                offset = androidx.compose.ui.geometry.Offset(2f * scale, 2f * scale),
                                                blurRadius = 4f * scale
                                            )
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = currentTimeText,
                                    fontFamily = DelaGothicOneFontFamily,
                                    fontSize = (28 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    style = TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.4f),
                                            offset = androidx.compose.ui.geometry.Offset(2f * scale, 2f * scale),
                                            blurRadius = 4f * scale
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // Close Button (X) in Top-Right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp * scale)
                            .size(36.dp * scale)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close camera",
                            tint = if (isDark) Color.White else Color(0xFF1C1C1E),
                            modifier = Modifier.size(20.dp * scale)
                        )
                    }
                }
            }
        }
    }

        // Capture Button (R.drawable.capture_smile) centered on the bottom border of 9:16 frame exactly half-in, half-out
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = shutterBottomMargin)
                .size(shutterSize)
                .clip(CircleShape)
                .background(currentInnerColor)
                .pointerInput(isRecording, countdownSeconds) {
                    awaitPointerEventScope {
                        while (true) {
                            var downEvent: PointerInputChange? = null
                            while (downEvent == null) {
                                val event = awaitPointerEvent()
                                val candidate = event.changes.firstOrNull { it.pressed }
                                if (candidate != null) {
                                    downEvent = candidate
                                }
                            }
                            
                            val dragPointerId = downEvent.id
                            startCaptureAction()
                            
                            while (true) {
                                val event = awaitPointerEvent()
                                val dragEvent = event.changes.firstOrNull { it.id == dragPointerId }
                                if (dragEvent == null || !dragEvent.pressed) {
                                    break
                                }
                                
                                val diffY = dragEvent.position.y - dragEvent.previousPosition.y
                                if (diffY != 0f) {
                                    dragEvent.consume()
                                    val sensitivity = 300f * scale
                                    val newZoom = (linearZoom - (diffY / sensitivity)).coerceIn(0.0f, 1.0f)
                                    if (newZoom != linearZoom) {
                                        linearZoom = newZoom
                                        activeSlot = (1 + (newZoom * 4f + 0.5f).toInt()).coerceIn(1, 5)
                                    }
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.capture_smile),
                contentDescription = "Capture Button",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(180f + rotationAngle) // upside down baseline rotating anticlockwise continuously
            )
        }

        // Outer concentric ring for capture button (hollow, transparent fill, centered on the bottom border line)
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = shutterBottomMargin + (shutterSize / 2f) - (67.dp * scale / 2f))
                .size(67.dp * scale)
        ) {
            drawCircle(
                color = selectedProfileColor,
                radius = (size.minDimension - (2.5.dp * scale).toPx()) / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = (2.5.dp * scale).toPx())
            )
        }

        // Flash toggle (auto / off / on) positioned to the left of the capture button, centered on bottom border line
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-64).dp * scale)
                .padding(bottom = cameraFrameBottomPadding + 27.5.dp - (36.dp * scale / 2f))
                .size(36.dp * scale)
                .clip(CircleShape) // circular shape for soft click ripple!
                .clickable {
                    flashMode = when (flashMode) {
                        "off" -> "on"
                        "on" -> "auto"
                        else -> "off"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val flashIconRes = when (flashMode) {
                "on" -> R.drawable.ic_flash_on
                "auto" -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
            Icon(
                painter = painterResource(id = flashIconRes),
                contentDescription = "Flash Toggle",
                tint = if (flashMode == "on" || flashMode == "auto") Color(0xFFFFD600) else Color.White,
                modifier = Modifier
                    .size(24.dp * scale)
                    .rotate(-90f) // rotated 90 degrees anticlockwise!
            )
        }

        // Screen-Edge Anchored Vertical Progress Bar (parallel to card straight-edge, highest Z-index)
        if (isRecording && recordingProgress > 0.0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = cameraFrameBottomPadding + 28.dp * scale, end = 0.dp) // aligned with straight vertical edge of card
                    .width(progressWidth) // width
                    .height(cameraHeight - 56.dp * scale) // length of straight vertical edge (cameraHeight - 28.dp * 2)
                    .clip(RoundedCornerShape(3.25.dp * scale))
                    .background(Color.Transparent) // fully transparent track
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(recordingProgress)
                        .align(Alignment.TopCenter) // fills from top to bottom
                        .background(palTextLogoColor) // exact same color as pal text logo on top left!
                )
            }
        }

        // Timer stopwatch button at bottom left of the screen (aligned with nav bar capsule)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp * scale, bottom = 12.dp * scale)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp * scale)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF151515) else Color(0xFFE5E5E5), CircleShape)
                    .border(1.dp * scale, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                    .clickable {
                        activeTimerMode = when (activeTimerMode) {
                            TimerMode.DEFAULT -> TimerMode.TIMER_3S
                            TimerMode.TIMER_3S -> TimerMode.TIMER_5S
                            TimerMode.TIMER_5S -> TimerMode.TIMELAPSE
                            TimerMode.TIMELAPSE -> TimerMode.DEFAULT
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val timerResId = when (activeTimerMode) {
                    TimerMode.DEFAULT -> R.drawable.timer_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                    TimerMode.TIMER_3S -> R.drawable.timer_3_alt_1_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                    TimerMode.TIMER_5S -> R.drawable.timer_5_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                    TimerMode.TIMELAPSE -> R.drawable.timelapse_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                }
                TimerIcon(
                    tint = textColor,
                    modifier = Modifier.size(31.dp * scale),
                    resId = timerResId
                )
            }
        }

        // Flip camera button at bottom right of the screen (aligned with nav bar capsule)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp * scale, bottom = 12.dp * scale)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp * scale)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF151515) else Color(0xFFE5E5E5), CircleShape)
                    .border(1.dp * scale, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                    .clickable { isCameraFlipped = !isCameraFlipped },
                contentAlignment = Alignment.Center
            ) {
                FlipCameraIcon(
                    tint = textColor,
                    modifier = Modifier.size(31.dp * scale)
                )
            }
        }
    }
  }

@Composable
fun VoiceMuteIcon(isMuted: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val path = Path()
        val rectW = w * 0.25f
        val rectH = h * 0.35f
        val rectLeft = w * 0.15f
        val rectTop = (h - rectH) / 2f
        
        path.moveTo(rectLeft, rectTop)
        path.lineTo(rectLeft + rectW, rectTop)
        val hornW = w * 0.3f
        val hornH = h * 0.25f
        path.lineTo(rectLeft + rectW + hornW, rectTop - hornH)
        path.lineTo(rectLeft + rectW + hornW, rectTop + rectH + hornH)
        path.lineTo(rectLeft + rectW, rectTop + rectH)
        path.lineTo(rectLeft, rectTop + rectH)
        path.close()
        
        drawPath(path = path, color = tint)
        
        if (isMuted) {
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.1f),
                end = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.9f),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        } else {
            val arcRect = Rect(
                left = w * 0.65f,
                top = h * 0.25f,
                right = w * 0.95f,
                bottom = h * 0.75f
            )
            drawArc(
                color = tint,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun SaveIcon(tint: Color, modifier: Modifier = Modifier) {
    AnimatedSaveIcon(isSaved = false, tint = tint, modifier = modifier)
}

@Composable
fun AnimatedSaveIcon(
    isSaved: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSaved) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 500,
            easing = androidx.compose.animation.core.LinearOutSlowInEasing
        ),
        label = "save_icon_transition"
    )
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // 1. Draw the tray / bottom line
        val trayLeftX = lerp(0.25f, 5f / 24f, progress) * w
        val trayRightX = lerp(0.75f, 19f / 24f, progress) * w
        val trayY = lerp(0.8f, 19f / 24f, progress) * h
        val strokeW = 2.5.dp.toPx()
        
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(trayLeftX, trayY),
            end = androidx.compose.ui.geometry.Offset(trayRightX, trayY),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // 2. Draw the vertical stem
        val stemAlpha = (1f - progress).coerceIn(0f, 1f)
        if (stemAlpha > 0f) {
            val stemTopY = h * 0.2f
            val stemBottomY = lerp(h * 0.65f, 16f / 24f * h, progress)
            val stemX = lerp(w / 2f, 9.55f / 24f * w, progress)
            drawLine(
                color = tint.copy(alpha = stemAlpha),
                start = androidx.compose.ui.geometry.Offset(w / 2f, stemTopY),
                end = androidx.compose.ui.geometry.Offset(stemX, stemBottomY),
                strokeWidth = strokeW,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        // 3. Draw the left wing / left stroke of checkmark
        val leftStartPointX = lerp(w / 2f, 9.55f / 24f * w, progress)
        val leftStartPointY = lerp(h * 0.65f, 16f / 24f * h, progress)
        val leftEndPointX = lerp(w * 0.3f, 3.88f / 24f * w, progress)
        val leftEndPointY = lerp(h * 0.45f, 10.33f / 24f * h, progress)
        
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(leftStartPointX, leftStartPointY),
            end = androidx.compose.ui.geometry.Offset(leftEndPointX, leftEndPointY),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // 4. Draw the right wing / right stroke of checkmark
        val rightStartPointX = lerp(w / 2f, 9.55f / 24f * w, progress)
        val rightStartPointY = lerp(h * 0.65f, 16f / 24f * h, progress)
        val rightEndPointX = lerp(w * 0.7f, 18.7f / 24f * w, progress)
        val rightEndPointY = lerp(h * 0.45f, 4f / 24f * h, progress)
        
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(rightStartPointX, rightStartPointY),
            end = androidx.compose.ui.geometry.Offset(rightEndPointX, rightEndPointY),
            strokeWidth = strokeW,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun UpwardArrowIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Vertical stem of the arrow
        val stemTop = h * 0.2f
        val stemBottom = h * 0.8f
        val cx = w / 2f
        
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, stemTop),
            end = androidx.compose.ui.geometry.Offset(cx, stemBottom),
            strokeWidth = 2.5.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Left and right wings pointing down from the top
        val wingSize = w * 0.25f
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, stemTop),
            end = androidx.compose.ui.geometry.Offset(cx - wingSize, stemTop + wingSize),
            strokeWidth = 2.5.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, stemTop),
            end = androidx.compose.ui.geometry.Offset(cx + wingSize, stemTop + wingSize),
            strokeWidth = 2.5.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoLoopPlayer(
    exoPlayer: androidx.media3.exoplayer.ExoPlayer,
    resizeMode: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                this.resizeMode = resizeMode
                val videoSurfaceView = getVideoSurfaceView()
                videoSurfaceView?.rotation = 0f
            }
        },
        modifier = modifier
    )
}

@Composable
fun MemberSmiley(
    isLit: Boolean,
    smileySize: Dp,
    innerSize: Dp,
    palTextLogoColor: Color,
    isDark: Boolean,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .size(smileySize)
            .then(
                if (isLit) {
                    Modifier.border(
                        width = 1.2.dp,
                        color = accentColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(
                    if (isLit) palTextLogoColor else (if (isDark) Color(0xFF3A3A3C) else Color(0xFFC7C7CC))
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.smile_small),
                contentDescription = "Smiley",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .rotate(if (isLit) 0f else 180f)
            )
        }
    }
}

@Composable
fun SendToPalRow(
    pal: PalItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isDark: Boolean,
    accentColor: Color,
    palTextLogoColor: Color,
    currentUserId: String,
    members: List<String>,
    submissions: List<SubmissionDbItem>
) {
    val capitalizedName = pal.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val description = if (pal.isVlog) "only you" else {
        if (members.isEmpty()) "only you" else {
            members.take(3).joinToString(", ") { name ->
                name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }

    val totalMembers = if (pal.isVlog) 1 else 1 + members.size
    val currentAlreadySent = submissions.any { it.userId == currentUserId }
    val uniqueSendersCount = submissions.map { it.userId }.distinct().size
    val isCurrentUserLit = currentAlreadySent || (isSelected && pal.isVlog)
    val litCount = if (isCurrentUserLit && !currentAlreadySent) {
        (uniqueSendersCount + 1).coerceAtMost(totalMembers)
    } else {
        uniqueSendersCount.coerceAtMost(totalMembers)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Checkmark circle (vibrant accentColor if selected)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) accentColor else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.Transparent else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Stacked texts (following light/dark rules)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = capitalizedName,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = description,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Smiley Face List
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalMembers) {
                val isLit = i < litCount
                MemberSmiley(
                    isLit = isLit,
                    smileySize = 35.dp,
                    innerSize = 21.dp,
                    palTextLogoColor = palTextLogoColor,
                    isDark = isDark,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
fun GroupMembersSmileysRow(
    members: List<String>,
    submissions: List<SubmissionDbItem>,
    isDark: Boolean,
    accentColor: Color,
    palTextLogoColor: Color,
    currentUserId: String,
    userFirstName: String,
    smileySize: Dp = 24.dp,
    innerSize: Dp = 16.dp,
    unlitAlpha: Float = 0.25f,
    showOnlyLit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayCount = members.size.coerceIn(1, 10)
    val shufflingColors = listOf(
        Color(0xFFFFE600), // Yellow
        Color(0xFFFF6700), // Orange
        Color(0xFFFF007F), // Pink
        Color(0xFF00F0FF), // Blue
        Color(0xFFB000FF), // Purple
        Color(0xFFFF073A)  // Red
    )

    // Pre-calculate which members are lit so we can filter them if showOnlyLit is true
    val itemsToRender = mutableListOf<Pair<Int, Boolean>>() // index in members, isLit
    for (i in 0 until displayCount) {
        val memberInfo = members.getOrNull(i) ?: ""
        val memberParts = memberInfo.split("|||")
        val (memberId, memberName, _) = if (memberParts.size >= 2) {
            Triple(memberParts[0], memberParts[1], memberParts.getOrNull(2))
        } else {
            Triple(null, memberInfo, null)
        }
        val isLit = if (memberName.isEmpty()) {
            false
        } else {
            if (memberId != null && memberId != "legacy_id") {
                submissions.any { it.userId == memberId }
            } else {
                if (memberName == userFirstName || memberName.contains("(You)") || memberName == "only you") {
                    submissions.any { it.userId == currentUserId }
                } else {
                    submissions.any { sub ->
                        val cleanSubName = parseUserDisplayName(sub.userDisplayName).first.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        cleanSubName.equals(memberName, ignoreCase = true)
                    }
                }
            }
        }
        if (showOnlyLit && !isLit) {
            continue
        }
        itemsToRender.add(Pair(i, isLit))
    }

    val spacing = when {
        itemsToRender.size <= 4 -> 4.dp
        itemsToRender.size <= 6 -> 3.dp
        else -> 2.dp
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsToRender.forEach { (i, isLit) ->
            val outerColor = shufflingColors[i % 6]
            val innerColor = shufflingColors[(i + 3) % 6]
            
            Box(
                modifier = Modifier
                    .size(smileySize)
                    .then(
                        if (isLit) {
                            Modifier.border(
                                width = 1.dp,
                                color = outerColor,
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val currentInnerSize = if (isLit) innerSize else smileySize
                Box(
                    modifier = Modifier
                        .size(currentInnerSize)
                        .clip(CircleShape)
                        .background(
                            if (isLit) innerColor else (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.smile_small),
                        contentDescription = "Smiley",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isLit) 180f else 0f),
                        colorFilter = ColorFilter.tint(
                            if (isLit) {
                                Color.Black
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GroupHoursSmileysRow(
    submissions: List<SubmissionDbItem>,
    isDark: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val activeHours = remember(submissions) {
        submissions.filter { it.imageUrl != "PROFILE_AVATAR" && !it.imageUrl.startsWith("PROFILE_AVATAR") }
            .map { sub ->
                if (!sub.createdAt.isNullOrEmpty()) {
                    try {
                        val instant = java.time.Instant.parse(sub.createdAt)
                        val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                        zonedDateTime.hour
                    } catch (e: Exception) {
                        val parts = sub.imageUrl.split("|||")
                        val path = parts.getOrNull(0) ?: ""
                        val regex = Regex("\\d{13}")
                        val match = regex.find(path)
                        if (match != null) {
                            try {
                                val instant = java.time.Instant.ofEpochMilli(match.value.toLong())
                                val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                                zonedDateTime.hour
                            } catch (ex: Exception) { 0 }
                        } else { 0 }
                    }
                } else {
                    val parts = sub.imageUrl.split("|||")
                    val path = parts.getOrNull(0) ?: ""
                    val regex = Regex("\\d{13}")
                    val match = regex.find(path)
                    if (match != null) {
                        try {
                            val instant = java.time.Instant.ofEpochMilli(match.value.toLong())
                            val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                            zonedDateTime.hour
                        } catch (ex: Exception) { 0 }
                    } else { 0 }
                }
            }
            .distinct()
            .sorted()
    }

    val hourCount = activeHours.size
    if (hourCount == 0) return

    val smileySize = when {
        hourCount <= 6 -> 22.dp
        hourCount <= 12 -> 16.dp
        hourCount <= 18 -> 12.dp
        else -> 9.dp
    }
    val spacing = when {
        hourCount <= 6 -> 4.dp
        hourCount <= 12 -> 3.dp
        else -> 2.dp
    }

    val shufflingColors = listOf(
        Color(0xFFFFE600), // Yellow
        Color(0xFFFF6700), // Orange
        Color(0xFFFF007F), // Pink
        Color(0xFF00F0FF), // Blue
        Color(0xFFB000FF), // Purple
        Color(0xFFFF073A)  // Red
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        activeHours.forEachIndexed { i, hour ->
            val outerColor = shufflingColors[i % 6]
            val innerColor = shufflingColors[(i + 3) % 6]
            
            Box(
                modifier = Modifier
                    .size(smileySize)
                    .border(
                        width = 1.dp,
                        color = outerColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val innerCircleSize = smileySize * 0.72f
                Box(
                    modifier = Modifier
                        .size(innerCircleSize)
                        .clip(CircleShape)
                        .background(innerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.smile_small),
                        contentDescription = "Smiley",
                        modifier = Modifier.fillMaxSize().rotate(180f),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
fun CapturedPreviewScreen(
    isDark: Boolean,
    accentColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    palTextLogoColor: Color,
    activeVlogPal: PalItem?,
    createdPals: List<PalItem>,
    rotationAngle: Float,
    capturedVideoPath: String?,
    capturedVlogsPaths: List<String>,
    onClose: () -> Unit,
    onSend: (String, List<PalItem>) -> Unit,
    currentUserId: String,
    currentDisplayName: String,
    allPalsSubmissions: Map<String, List<SubmissionDbItem>>,
    customAvatarUriString: String?
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    var isPreviewSaving by remember { mutableStateOf(false) }
    var isPreviewSaved by remember { mutableStateOf(false) }
    
    val capturedTimeText = remember(capturedVideoPath) {
        val time = java.time.LocalTime.now()
        String.format("%02d:%02d", time.hour, time.minute)
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore focus error
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var selectedPals by remember { mutableStateOf<Set<String>>(emptySet()) }
    val groupMembersMap = remember { mutableStateMapOf<String, List<String>>() }
    
    val groupSubmissionsMap = createdPals.associate { pal ->
        val subs = allPalsSubmissions[pal.code] ?: emptyList()
        val todaySubs = subs.filter { sub ->
            var subDate: java.time.LocalDate? = null
            if (!sub.createdAt.isNullOrEmpty()) {
                try {
                    val instant = java.time.Instant.parse(sub.createdAt)
                    subDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) {}
            }
            if (subDate == null) {
                val parts = sub.imageUrl.split("|||")
                val path = parts.getOrNull(0) ?: ""
                val regex = Regex("\\d{13}")
                val match = regex.find(path)
                if (match != null) {
                    try {
                        val millis = match.value.toLong()
                        subDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    } catch (e: Exception) {}
                }
            }
            subDate == java.time.LocalDate.now()
        }
        pal.code to todaySubs
    }
    val userFirstName = remember(currentDisplayName) {
        currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
    }
    val sharedPrefs = remember(context) { context.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE) }
    val deletedVlogsKey = "deleted_vlog_paths_$currentUserId"

    LaunchedEffect(createdPals, currentDisplayName) {
        createdPals.forEach { pal ->
            if (pal.isVlog) {
                groupMembersMap[pal.code] = listOf("only you")
            } else {
                launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val dbSubs = com.finrein.pals.PalApplication.supabase.postgrest.from("submissions")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<SubmissionDbItem>()
                        val mappings = com.finrein.pals.PalApplication.supabase.postgrest.from("user_pals")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<UserPalMapping>()
                            .sortedWith(compareBy({ it.createdAt ?: "" }, { it.id ?: "" }))

                        val memberList = mutableListOf<String>()
                        val addedUserIds = mutableSetOf<String>()

                        mappings.forEach { mapping ->
                            if (mapping.userId.isNotEmpty() && !addedUserIds.contains(mapping.userId)) {
                                val sub = dbSubs.firstOrNull { it.userId == mapping.userId }
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

                        dbSubs.forEach { sub ->
                            if (sub.userId.isNotEmpty() && !addedUserIds.contains(sub.userId)) {
                                val (displayName, avatarUrl) = parseUserDisplayName(sub.userDisplayName)
                                val formatted = "${sub.userId}|||$displayName|||${avatarUrl ?: ""}"
                                memberList.add(formatted)
                                addedUserIds.add(sub.userId)
                            }
                        }

                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            groupMembersMap[pal.code] = memberList
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // 1. Initialize ExoPlayer safely
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context)
            .setRenderersFactory(
                androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                    setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                    setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                }
            )
            .build().apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL // Seamless infinite loop matching the vlog vibe
                playWhenReady = true
            }
    }

    // 2. FORCE RE-EVALUATION FLOW:
    // We listen to the raw capturedVideoPath. Whenever this string changes, we forcefully flush the player stack.
    LaunchedEffect(capturedVideoPath) {
        android.util.Log.d("PalPipeline", "Raw path string received: $capturedVideoPath")
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        if (!capturedVideoPath.isNullOrBlank()) {
            val cleanPath = when {
                capturedVideoPath.startsWith("file://") -> capturedVideoPath.substring(7)
                else -> capturedVideoPath
            }
            val fileTarget = java.io.File(cleanPath)
            // Wait 250ms to allow encoder to flush and unlock the file handle
            delay(250L)
            if (fileTarget.exists() && fileTarget.length() > 0) {
                val targetUri = android.net.Uri.fromFile(fileTarget)
                exoPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } else {
                android.util.Log.e("PalPipeline", "File path string exists but target file not found on disk!")
            }
        } else {
            android.util.Log.d("PalPipeline", "Path is completely empty. Retaining silent black display slate.")
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Buttons Background & Icon Tint following dark/light rules
    val buttonBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val buttonIconTint = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF2F2F7))
            .statusBarsPadding()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. The 16:9 Video Box with dynamic constraints for orientation rotation (no boundary line/border)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                        view.layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        view.apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                            setBackgroundColor(android.graphics.Color.BLACK)
                            
                            fun applyVideoScale() {
                                val videoSize = exoPlayer.videoSize
                                val videoWidth = videoSize.width
                                val videoHeight = videoSize.height
                                val textureView = getVideoSurfaceView() as? android.view.TextureView
                                if (textureView == null) {
                                    postDelayed({ applyVideoScale() }, 100)
                                    return
                                }
                                val containerWidth = width.toFloat()
                                val containerHeight = height.toFloat()
                                if (containerWidth > 0f && containerHeight > 0f && videoWidth > 0 && videoHeight > 0) {
                                    val isPortrait = videoHeight > videoWidth
                                    val rotatedWidth = if (isPortrait) videoHeight.toFloat() else videoWidth.toFloat()
                                    val rotatedHeight = if (isPortrait) videoWidth.toFloat() else videoHeight.toFloat()
                                    
                                    val scale = java.lang.Math.max(containerWidth / rotatedWidth, containerHeight / rotatedHeight)
                                    
                                    val calculatedScaleX: Float
                                    val calculatedScaleY: Float
                                    val calculatedRotation: Float
                                    if (isPortrait) {
                                        calculatedScaleX = (rotatedHeight * scale) / containerWidth
                                        calculatedScaleY = (rotatedWidth * scale) / containerHeight
                                        calculatedRotation = 270f
                                    } else {
                                        calculatedScaleX = (rotatedWidth * scale) / containerWidth
                                        calculatedScaleY = (rotatedHeight * scale) / containerHeight
                                        calculatedRotation = 0f
                                    }
                                    
                                    android.util.Log.d("PalVideoScale", "Reverted scale for exoPlayer: video=${videoWidth}x${videoHeight}, container=${containerWidth}x${containerHeight}, scaleX=${calculatedScaleX}, scaleY=${calculatedScaleY}, rotation=${calculatedRotation}")
                                    
                                    textureView.pivotX = containerWidth / 2f
                                    textureView.pivotY = containerHeight / 2f
                                    textureView.scaleX = calculatedScaleX
                                    textureView.scaleY = calculatedScaleY
                                    textureView.rotation = calculatedRotation
                                } else {
                                    postDelayed({ applyVideoScale() }, 100)
                                }
                            }

                            val layoutListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                                applyVideoScale()
                            }
                            addOnLayoutChangeListener(layoutListener)

                            exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                    super.onVideoSizeChanged(videoSize)
                                    applyVideoScale()
                                }
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    super.onPlaybackStateChanged(playbackState)
                                    applyVideoScale()
                                    if (playbackState == androidx.media3.common.Player.STATE_READY) {
                                        exoPlayer.play()
                                    }
                                }
                            })

                            applyVideoScale()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (view.player != exoPlayer) {
                            view.player = exoPlayer
                        }
                        if (!exoPlayer.isPlaying && exoPlayer.playbackState == androidx.media3.common.Player.STATE_READY) {
                            exoPlayer.play()
                        }
                    }
                )
            }

            // Top Middle Vlog Title Header Text ("vlog >" or selected group name + " >")
            val selectedPalNames = remember(selectedPals, createdPals) {
                createdPals.filter { selectedPals.contains(it.code) }
                    .joinToString(", ") { it.name.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
            }
            if (selectedPalNames.isNotEmpty()) {
                Text(
                    text = "$selectedPalNames >",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }

            // Centered Overlay for Caption vertical bar and Time Text
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Time text: increased by 5dp (20.sp), Dela Gothic One
                Text(
                    text = capturedTimeText,
                    fontFamily = DelaGothicOneFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(1.5.dp))

                // Caption vertical bar cursor (centered, width bound, sans-serif)
                androidx.compose.foundation.text.BasicTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(palTextLogoColor), // cursor color exactly matches top-left Pal color
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .width(180.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )
            }

            // Button border color for transparent buttons
            val transparentButtonBorderColor = Color.White.copy(alpha = 0.3f)

            // Top Left Close Cross Button (transparent bg, visible border, not much spaced from corners)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, transparentButtonBorderColor, CircleShape)
                    .background(Color.Transparent)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close preview",
                    tint = buttonIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Top Right Send Button (Glows up only when target is selected)
            val isButtonEnabled = selectedPals.isNotEmpty()
            val sendButtonBg = if (isButtonEnabled) {
                accentColor
            } else {
                accentColor.copy(alpha = 0.15f)
            }
            
            val sendButtonIconTint = if (isButtonEnabled) {
                if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White
            } else {
                accentColor.copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(sendButtonBg)
                    .then(
                        if (isButtonEnabled) {
                            Modifier.clickable {
                                val targets = createdPals.filter { selectedPals.contains(it.code) }
                                onSend(captionText, targets)
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                UpwardArrowIcon(
                    tint = sendButtonIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom Left Mute Toggle Button (transparent bg, visible border, not much spaced from corners)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, transparentButtonBorderColor, CircleShape)
                    .background(Color.Transparent)
                    .clickable { isMuted = !isMuted },
                contentAlignment = Alignment.Center
            ) {
                VoiceMuteIcon(
                    isMuted = isMuted,
                    tint = buttonIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom Right Save Button (transparent bg, visible border, not much spaced from corners)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, transparentButtonBorderColor, CircleShape)
                    .background(Color.Transparent)
                    .clickable {
                        if (!isPreviewSaving && !isPreviewSaved && !capturedVideoPath.isNullOrBlank()) {
                            isPreviewSaving = true
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val cleanPath = when {
                                    capturedVideoPath.startsWith("file://") -> capturedVideoPath.substring(7)
                                    else -> capturedVideoPath
                                }
                                val tempOut = java.io.File(context.cacheDir, "temp_preview_save_${System.currentTimeMillis()}.mp4")
                                VideoProcessor.processVideo(
                                    context = context,
                                    inputPath = cleanPath,
                                    outputPath = tempOut.absolutePath,
                                    vlogText = createdPals.filter { selectedPals.contains(it.code) }
                                        .joinToString(", ") { it.name }
                                        .ifEmpty { "vlog" },
                                    timeText = capturedTimeText,
                                    captionText = captionText,
                                    roundedCorners = true
                                ) { success ->
                                    if (success) {
                                        val saveSuccess = saveVideoToGallery(context, tempOut.absolutePath)
                                        if (saveSuccess) {
                                            isPreviewSaved = true
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(3000)
                                                isPreviewSaved = false
                                            }
                                        }
                                    }
                                    try { tempOut.delete() } catch(e: Exception) {}
                                    isPreviewSaving = false
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPreviewSaving) {
                    CircularProgressIndicator(
                        color = buttonIconTint,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    AnimatedSaveIcon(
                        isSaved = isPreviewSaved,
                        tint = buttonIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Send To Label (always white/grey in dark mode, black in light mode as per image)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "send to:",
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Options List (Column with spacedBy(10.dp) and vertical scroll)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val sortedPals = createdPals.sortedWith(compareByDescending { it.isVlog })
            val savedDeleted = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
            val currentDeleted = if (savedDeleted.isEmpty()) emptySet<String>() else savedDeleted.split(";;;").toSet()

            sortedPals.forEach { pal ->
                val isSelected = selectedPals.contains(pal.code)
                val groupName = pal.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                val members = groupMembersMap[pal.code] ?: emptyList()
                val allMemberFirstNames = members.map { entry ->
                    val parts = entry.split("|||")
                    val displayName = if (parts.size >= 2) parts[1] else entry
                    displayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                }
                
                val description = if (pal.isVlog) "only you" else {
                    if (allMemberFirstNames.isEmpty() || allMemberFirstNames.size <= 1) "only you" else {
                        allMemberFirstNames.joinToString(", ") { name ->
                            name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                    }
                }
                
                val descriptionLength = description.length
                val descriptionFontSize = if (pal.isVlog) 13.sp else {
                    when {
                        descriptionLength <= 15 -> 13.sp
                        descriptionLength <= 22 -> 13.sp
                        descriptionLength <= 30 -> 11.5.sp
                        descriptionLength <= 40 -> 10.sp
                        descriptionLength <= 50 -> 8.5.sp
                        else -> 7.5.sp
                    }
                }
                
                val lastHourSub = if (pal.isVlog) null else {
                    val groupSubs = groupSubmissionsMap[pal.code] ?: emptyList()
                    val oneHourAgo = System.currentTimeMillis() - 3600 * 1000
                    groupSubs.filter { it.userId == currentUserId }
                        .filter { sub ->
                            val path = sub.imageUrl.split("|||").firstOrNull() ?: ""
                            path !in currentDeleted && sub.imageUrl !in currentDeleted
                        }
                        .firstOrNull { sub ->
                            var subTime = 0L
                            if (!sub.createdAt.isNullOrEmpty()) {
                                try {
                                    subTime = java.time.Instant.parse(sub.createdAt).toEpochMilli()
                                } catch (e: Exception) {}
                            }
                            if (subTime == 0L) {
                                val parts = sub.imageUrl.split("|||")
                                val path = parts.getOrNull(0) ?: ""
                                val regex = Regex("\\d{13}")
                                val match = regex.find(path)
                                if (match != null) {
                                    try {
                                        subTime = match.value.toLong()
                                    } catch (e: Exception) {}
                                }
                            }
                            subTime > oneHourAgo
                        }
                }
                val isHourlyRestricted = lastHourSub != null
                
                val hourlySentText = if (lastHourSub != null) {
                    var subTime = 0L
                    if (!lastHourSub.createdAt.isNullOrEmpty()) {
                        try {
                            subTime = java.time.Instant.parse(lastHourSub.createdAt).toEpochMilli()
                        } catch (e: Exception) {}
                    }
                    if (subTime == 0L) {
                        val parts = lastHourSub.imageUrl.split("|||")
                        val path = parts.getOrNull(0) ?: ""
                        val regex = Regex("\\d{13}")
                        val match = regex.find(path)
                        if (match != null) {
                            try {
                                subTime = match.value.toLong()
                            } catch (e: Exception) {}
                        }
                    }
                    if (subTime > 0L) {
                        val instant = java.time.Instant.ofEpochMilli(subTime)
                        val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                        String.format("sent for %02d:00", zonedDateTime.hour)
                    } else {
                        "sent"
                    }
                } else {
                    ""
                }

                val cardAlpha = if (isHourlyRestricted) 0.5f else 1.0f

                // Card Box Container (Grey in light mode, Charcoal in dark mode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .alpha(cardAlpha)
                        .background(
                            if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA)
                        )
                        .then(
                            if (isHourlyRestricted) Modifier else Modifier.clickable {
                                selectedPals = if (isSelected) {
                                    selectedPals - pal.code
                                } else {
                                    selectedPals + pal.code
                                }
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Checkmark circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Description (White in dark mode, Black in light mode)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (pal.isVlog) "vlog" else groupName,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isHourlyRestricted && hourlySentText.isNotEmpty()) {
                                Text(
                                    text = "($hourlySentText)",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    color = Color.Red.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = description,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = descriptionFontSize,
                            fontWeight = FontWeight.Normal,
                            color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Smiley Face List
                    Box(
                        modifier = Modifier.wrapContentWidth(Alignment.End),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (pal.isVlog) {
                            val vlogSubmissions = if (capturedVlogsPaths.isNotEmpty() || isSelected) {
                                listOf(SubmissionDbItem(palCode = "vlog", userId = currentUserId, userDisplayName = currentDisplayName, imageUrl = ""))
                            } else {
                                emptyList()
                            }
                            GroupMembersSmileysRow(
                                members = listOf("only you"),
                                submissions = vlogSubmissions,
                                isDark = isDark,
                                accentColor = accentColor,
                                palTextLogoColor = palTextLogoColor,
                                currentUserId = currentUserId,
                                userFirstName = userFirstName,
                                smileySize = 24.dp,
                                innerSize = 18.dp
                            )
                        } else {
                            val groupMembersList = groupMembersMap[pal.code] ?: emptyList()
                            val memberCount = groupMembersList.size
                            val computedSmileySize = when {
                                memberCount <= 4 -> 24.dp
                                memberCount <= 6 -> 18.dp
                                memberCount <= 8 -> 14.dp
                                else -> 11.dp
                            }
                            val computedInnerSize = when {
                                memberCount <= 4 -> 16.dp
                                memberCount <= 6 -> 12.dp
                                memberCount <= 8 -> 10.dp
                                else -> 8.dp
                            }
                            val groupSubs = groupSubmissionsMap[pal.code] ?: emptyList()
                            val finalSubs = if (isSelected && !groupSubs.any { it.userId == currentUserId }) {
                                groupSubs + SubmissionDbItem(palCode = pal.code, userId = currentUserId, userDisplayName = currentDisplayName, imageUrl = "")
                            } else {
                                groupSubs
                            }
                            GroupMembersSmileysRow(
                                members = groupMembersList,
                                submissions = finalSubs,
                                isDark = isDark,
                                accentColor = accentColor,
                                palTextLogoColor = palTextLogoColor,
                                currentUserId = currentUserId,
                                userFirstName = userFirstName,
                                smileySize = computedSmileySize,
                                innerSize = computedInnerSize
                            )
                        }
                    }
                }
            }
        }
    }
}



data class VlogScreenContentParams(
    val pal: PalItem,
    val onBack: () -> Unit,
    val isDark: Boolean,
    val accentColor: Color,
    val palTextLogoColor: Color,
    val textColor: Color,
    val mutedTextColor: Color,
    val activeGradientColors: List<Color>,
    val rotationAngle: Float,
    val palsCount: Int,
    val onStartCapture: () -> Unit,
    val isCapturing: Boolean,
    val captureProgress: Float,
    val showDropdown: Boolean,
    val onShowDropdownChange: (Boolean) -> Unit,
    val showChat: Boolean,
    val onShowChatChange: (Boolean) -> Unit,
    val showEdit: Boolean,
    val onShowEditChange: (Boolean) -> Unit,
    val showDelete: Boolean,
    val onShowDeleteChange: (Boolean) -> Unit,
    val showLeave: Boolean,
    val onShowLeaveChange: (Boolean) -> Unit,
    val showExportDialog: Boolean,
    val onShowExportDialogChange: (Boolean) -> Unit,
    val expandedMembers: Boolean,
    val onExpandedMembersChange: (Boolean) -> Unit,
    val expandedSettings: Boolean,
    val onExpandedSettingsChange: (Boolean) -> Unit,
    val editName: String,
    val onEditNameChange: (String) -> Unit,
    val editSize: String,
    val onEditSizeChange: (String) -> Unit,
    val isEditingLoading: Boolean,
    val onStartSaveEdit: () -> Unit,
    val editDots: String,
    val messages: List<MessageDbItem>,
    val onSendMessage: (String) -> Unit,
    val currentDisplayName: String,
    val onDeletePal: () -> Unit,
    val onLeavePal: () -> Unit,
    val customAvatarUriString: String?,
    val capturedVlogsPaths: List<String>,
    val capturedVlogsTimes: List<String>,
    val capturedVlogsCaptions: List<String>,
    val currentPlayingIndex: Int,
    val vlogPlaybackProgress: Float,
    val vlogExoPlayer: androidx.media3.exoplayer.ExoPlayer,
    val onNavigateToCamera: () -> Unit = {},
    val onDeleteVlog: (Int) -> Unit = {},
    val onUpdateVlogCaption: (String, String) -> Unit = { _, _ -> },
    val selectedDayOffset: Int = 0,
    val onSelectedDayOffsetChange: (Int) -> Unit = {},
    val allPalsSubmissions: Map<String, List<SubmissionDbItem>> = emptyMap(),
    val allPalsMembers: Map<String, List<String>> = emptyMap(),
    val currentUserId: String = "",
    val palReactions: Map<String, String> = emptyMap(),
    val onEmojiReacted: (String, String) -> Unit = { _, _ -> },
    val activeReplyPreviewPath: String? = null,
    val onActiveReplyPreviewPathChange: (String?) -> Unit = {},
    val activeReactionPreview: Pair<String, String>? = null,
    val onActiveReactionPreviewChange: (Pair<String, String>?) -> Unit = {},
    val onSendReply: (String, String) -> Unit = { _, _ -> },
    val onDeleteMessageLocal: (String) -> Unit = {}
)

@Composable
fun GroupMemberCard(
    index: Int,
    isGrid: Boolean,
    cardHeightDp: Dp,
    groupMembers: List<String>,
    userFirstName: String,
    filteredSubmissions: List<SubmissionDbItem>,
    currentUserId: String,
    currentDisplayName: String,
    accentColor: Color,
    isDark: Boolean,
    textColor: Color,
    customAvatarUriString: String?,
    shufflingColors: List<Color>,
    selectedMemberIndex: Int,
    onSelectedMemberIndexChange: (Int) -> Unit,
    onNavigateToCamera: () -> Unit,
    onEditCaptionClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onInviteClick: () -> Unit,
    density: androidx.compose.ui.unit.Density,
    context: android.content.Context,
    palReactions: Map<String, String> = emptyMap(),
    onEmojiReacted: (String, String) -> Unit = { _, _ -> },
    onReplyClick: (String) -> Unit = {},
    messages: List<MessageDbItem> = emptyList(),
    isEditingCaption: Boolean = false,
    onIsEditingCaptionChange: (Boolean) -> Unit = {},
    editCaptionText: androidx.compose.ui.text.input.TextFieldValue = androidx.compose.ui.text.input.TextFieldValue(""),
    onEditCaptionTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit = {},
    onUpdateVlogCaption: (String, String) -> Unit = { _, _ -> },
    capturedVlogsPaths: List<String> = emptyList()
) {
    val isActualMember = index < groupMembers.size
    val cardShape = if (isGrid) androidx.compose.ui.graphics.RectangleShape else RoundedCornerShape(28.dp)
    val memberInfo = if (isActualMember) groupMembers[index] else null
    val memberParts = memberInfo?.split("|||")
    val (memberId, memberRawName, memberAvatar) = if (memberParts != null && memberParts.size >= 2) {
        Triple(memberParts[0], memberParts[1], memberParts.getOrNull(2))
    } else {
        Triple(null, memberInfo, null)
    }
    val memberName = remember(memberRawName) {
        memberRawName?.trim()?.substringBefore(" ")?.substringBefore("_")?.substringBefore(".")
    }
    val isUser = if (memberId != null) memberId == currentUserId else (memberName != null && (memberName.contains("(You)") || memberName == userFirstName))

    val memberReplies = remember(messages, memberId, memberName, isUser, currentUserId) {
        messages.filter { msg ->
            if (msg.content.startsWith("REPLY|||")) {
                val parts = msg.content.split("|||")
                val targetUserId = parts.getOrNull(1) ?: ""
                val targetUserDisplayName = parts.getOrNull(2) ?: ""
                if (memberId != null) {
                    targetUserId == memberId
                } else {
                    if (isUser) {
                        targetUserId == currentUserId
                    } else {
                        val cleanTargetName = targetUserDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        cleanTargetName.equals(memberName, ignoreCase = true)
                    }
                }
            } else {
                false
            }
        }.map { msg ->
            val senderMember = groupMembers.firstOrNull { it.startsWith("${msg.userId}|||") }
            val (senderName, senderAvatar) = if (senderMember != null) {
                val parts = senderMember.split("|||")
                Pair(parts.getOrNull(1) ?: "Pal", parts.getOrNull(2))
            } else {
                if (msg.userId == currentUserId) {
                    Pair(userFirstName, customAvatarUriString)
                } else {
                    Pair("Pal", null)
                }
            }
            Pair(senderName, senderAvatar)
        }.distinctBy { it.first }
    }

    val memberSubs = if (isActualMember) {
        filteredSubmissions.filter { sub ->
            if (memberId != null && memberId != "legacy_id") {
                sub.userId == memberId
            } else {
                if (isUser) {
                    sub.userId == currentUserId
                } else {
                    val cleanSubName = parseUserDisplayName(sub.userDisplayName).first.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                    cleanSubName.equals(memberName, ignoreCase = true)
                }
            }
        }
    } else {
        emptyList()
    }

    val sortedMemberSubs = remember(memberSubs) {
        memberSubs.mapNotNull { sub ->
            val parts = sub.imageUrl.split("|||")
            val path = parts.getOrNull(0) ?: ""
            if (path.isEmpty()) null else {
                var hour = 12
                if (!sub.createdAt.isNullOrEmpty()) {
                    try {
                        val instant = java.time.Instant.parse(sub.createdAt)
                        val localDateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        val rawHour = localDateTime.hour
                        hour = (rawHour - 4 + 24) % 24
                    } catch (e: Exception) {}
                }
                val timestamp = if (!sub.createdAt.isNullOrEmpty()) {
                    try { java.time.Instant.parse(sub.createdAt).toEpochMilli() } catch (e: Exception) { 0L }
                } else 0L
                Triple(sub, hour, timestamp)
            }
        }
        .groupBy { it.second }
        .map { entry -> entry.value.maxByOrNull { it.third }!! }
        .sortedBy { it.third }
        .map { it.first }
    }

    val hasSubmission = sortedMemberSubs.isNotEmpty()
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showEmojiOverlay by remember { mutableStateOf(false) }
    val defaultEmojis = remember { listOf("😂", "❤️", "😭", "✨", "🥺", "🔥", "🥰", "🎉", "💀", "👍", "🙏", "💯", "😎", "👀") }
    var currentEmojis by remember { mutableStateOf(defaultEmojis.take(5)) }

    if (hasSubmission) {
        // ACTIVE VIDEO CARD PLAYER FOR ANY MEMBER WITH SUBMISSION
        val firstSub = sortedMemberSubs.first()
        val videoPaths = remember(sortedMemberSubs) { sortedMemberSubs.map { it.imageUrl.split("|||").firstOrNull() ?: "" }.filter { it.isNotEmpty() } }
        val videoPath = videoPaths.firstOrNull() ?: ""
        val caption = firstSub.imageUrl.split("|||").getOrNull(1) ?: ""
        if (isUser) {
            LaunchedEffect(isEditingCaption) {
                if (isEditingCaption) {
                    onEditCaptionTextChange(
                        androidx.compose.ui.text.input.TextFieldValue(
                            text = caption,
                            selection = androidx.compose.ui.text.TextRange(caption.length)
                        )
                    )
                }
            }
        }
        val timeText = if (!firstSub.createdAt.isNullOrEmpty()) {
            try {
                val instant = java.time.Instant.parse(firstSub.createdAt)
                val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                val hr = zonedDateTime.hour
                String.format(java.util.Locale.US, "%02d:00", hr)
            } catch (e: Exception) {
                "12:00"
            }
        } else {
            "12:00"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeightDp)
                .clip(cardShape)
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA))
        ) {
            VideoPlayerItem(
                videoPaths = videoPaths,
                modifier = Modifier.fillMaxSize(),
                shape = cardShape
            )

            // Overlay 1: Avatar and Name (Top Left)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = if (isGrid) 8.dp else 12.dp, start = if (isGrid) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userAvatar = if (isUser) customAvatarUriString else memberAvatar
                if (!userAvatar.isNullOrEmpty()) {
                    UriImage(
                        uriString = userAvatar,
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.smile_medium),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(180f)
                        )
                    }
                }

                Text(
                    text = if (isUser) userFirstName else (memberName ?: ""),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 12.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }



            // Overlays 2 & 3: Inline edit layout or Centered caption/time layouts
            if (isUser && isEditingCaption) {
                val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                androidx.compose.foundation.text.BasicTextField(
                    value = editCaptionText,
                    onValueChange = onEditCaptionTextChange,
                    textStyle = TextStyle(
                        fontFamily = RobotoFontFamily,
                        fontSize = if (isGrid) 12.sp else 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 3f
                        )
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(accentColor),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = if (isGrid) 8.dp else 16.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (editCaptionText.text.isEmpty()) {
                                Text(
                                    text = "write caption...",
                                    fontFamily = RobotoFontFamily,
                                    fontSize = if (isGrid) 12.sp else 16.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Overlay 5: Top Right Checkmark Save button (only visible during caption editing mode)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (isGrid) 4.dp else 8.dp, end = if (isGrid) 6.dp else 12.dp)
                        .size(if (isGrid) 24.dp else 36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            val userSub = filteredSubmissions.firstOrNull { it.userId == currentUserId }
                            if (userSub != null) {
                                val userPath = userSub.imageUrl.split("|||").firstOrNull() ?: ""
                                if (userPath.isNotEmpty()) {
                                    onUpdateVlogCaption(userPath, editCaptionText.text.trim())
                                }
                            }
                            onIsEditingCaptionChange(false)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Caption",
                        tint = Color.White,
                        modifier = Modifier.size(if (isGrid) 14.dp else 20.dp)
                    )
                }
            } else {
                // Both user (when not editing) and other members show timeText and caption 5dp below it
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = timeText,
                        fontFamily = DelaGothicOneFontFamily,
                        fontSize = if (isGrid) 12.5.sp else 18.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (caption.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = caption,
                            fontFamily = RobotoFontFamily,
                            fontSize = if (isGrid) 11.sp else 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            style = TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 3f
                                )
                            )
                        )
                    }
                }
            }

            // Overlay 4/5: Actions for User vs Other Members
            if (isUser) {
                if (!isEditingCaption) {
                    // Triple dots at bottom right for User
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (isGrid) 8.dp else 12.dp, end = if (isGrid) 10.dp else 16.dp)
                            .clickable { showDropdownMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "•••",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF1E1D22) else Color(0xFFF5F3EB), RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            DropdownMenuItem(
                                text = { Text("edit caption", color = if (isDark) Color.White else Color.Black) },
                                onClick = {
                                    showDropdownMenu = false
                                    onEditCaptionClick(index)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("delete", color = if (isDark) Color.White else Color.Black) },
                                onClick = {
                                    showDropdownMenu = false
                                    onDeleteClick(index)
                                }
                            )
                        }
                    }
                }
            } else {
                // Stacked Reply and Love icons aligned vertically at the right end
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (isGrid) 10.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isGrid) 16.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Reply Icon
                    Box(
                        modifier = Modifier
                            .clickable { onReplyClick(videoPath) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Reply",
                            tint = Color.White,
                            modifier = Modifier.size(if (isGrid) 18.dp else 25.dp)
                        )
                    }

                    // Love Icon
                    Box(
                        modifier = Modifier
                            .clickable { showEmojiOverlay = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = "Love",
                            tint = Color.White,
                            modifier = Modifier.size(if (isGrid) 18.dp else 25.dp)
                        )
                    }
                }

                // Replies pills shown at Bottom Left corner
                if (memberReplies.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = if (isGrid) 8.dp else 12.dp, start = if (isGrid) 10.dp else 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        memberReplies.take(2).forEach { (replierName, replierAvatar) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!replierAvatar.isNullOrEmpty()) {
                                    UriImage(
                                        uriString = replierAvatar,
                                        modifier = Modifier
                                            .size(if (isGrid) 16.dp else 20.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isGrid) 16.dp else 20.dp)
                                            .clip(CircleShape)
                                            .background(accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.smile_medium),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .rotate(180f)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = replierName,
                                        color = Color.Black,
                                        fontSize = if (isGrid) 9.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transparent Emoji Selector Overlay in the exact middle of the card
            if (showEmojiOverlay) {
                // Dimmed dismiss overlay background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { showEmojiOverlay = false }
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(if (isGrid) 8.dp else 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = if (isGrid) 16.sp else 28.sp,
                            modifier = Modifier
                                .clickable {
                                    onEmojiReacted(videoPath, emoji)
                                    showEmojiOverlay = false
                                }
                        )
                    }

                    // Refresh/Smiley indicator as outline icon at the end matching image 2
                    Box(
                        modifier = Modifier
                            .size(if (isGrid) 20.dp else 32.dp)
                            .clip(CircleShape)
                            .clickable {
                                currentEmojis = defaultEmojis.shuffled().take(5)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "More",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(if (isGrid) 16.dp else 28.dp)
                        )
                    }
                }
            }
        }
    } else if (isUser && !hasSubmission) {
        // BOUNCING SCREEN SAVER CARD FOR USER (Slot 0, empty)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeightDp)
                .clip(cardShape)
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA))
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                    shape = cardShape
                )
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val smileySizePx = with(density) { (if (isGrid) 34.dp else 50.dp).toPx() }

            var groupPosX by remember { mutableStateOf(0f) }
            var groupPosY by remember { mutableStateOf(0f) }
            var groupSmileRotation by remember { mutableStateOf(0f) }
            var groupSmileColorIndex by remember { mutableStateOf(0) }

            LaunchedEffect(widthPx, heightPx) {
                if (widthPx <= 0f || heightPx <= 0f) return@LaunchedEffect
                groupPosX = (widthPx - smileySizePx) / 2f
                groupPosY = (heightPx - smileySizePx) / 2f

                val speed = with(density) { 100.dp.toPx() }
                var vx = speed * 0.76f
                var vy = speed * 0.65f

                var lastTime = androidx.compose.runtime.withFrameNanos { it }

                while (true) {
                    androidx.compose.runtime.withFrameNanos { time ->
                        val dt = (time - lastTime) / 1_000_000_000f
                        lastTime = time

                        val cappedDt = dt.coerceAtMost(0.1f)

                        var newX = groupPosX + vx * cappedDt
                        var newY = groupPosY + vy * cappedDt

                        var collided = false

                        val minX = 0f
                        val maxX = widthPx - smileySizePx
                        val minY = 0f
                        val maxY = heightPx - smileySizePx

                        if (maxX > 0f) {
                            if (newX <= minX) {
                                newX = minX
                                vx = -vx
                                collided = true
                            } else if (newX >= maxX) {
                                newX = maxX
                                vx = -vx
                                collided = true
                            }
                        }

                        if (maxY > 0f) {
                            if (newY <= minY) {
                                newY = minY
                                vy = -vy
                                collided = true
                            } else if (newY >= maxY) {
                                newY = maxY
                                vy = -vy
                                collided = true
                            }
                        }

                        groupPosX = newX
                        groupPosY = newY

                        groupSmileRotation = (groupSmileRotation + 120f * cappedDt) % 360f

                        if (collided) {
                            groupSmileColorIndex = (groupSmileColorIndex + 1) % shufflingColors.size
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(if (isGrid) 34.dp else 50.dp)
                    .offset(
                        x = with(density) { groupPosX.toDp() },
                        y = with(density) { groupPosY.toDp() }
                    )
                    .rotate(groupSmileRotation)
                    .clip(CircleShape)
                    .background(shufflingColors[groupSmileColorIndex]),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = if (isGrid) R.drawable.smile_small else R.drawable.smile_medium),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = if (isGrid) 8.dp else 12.dp, start = if (isGrid) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userAvatar = if (isUser) customAvatarUriString else memberAvatar
                if (!userAvatar.isNullOrEmpty()) {
                    UriImage(
                        uriString = userAvatar,
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.smile_medium),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(180f)
                        )
                    }
                }

                val userEmptyTextColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                val userEmptyCaptureColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                Text(
                    text = if (isUser) userFirstName else (memberName ?: ""),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 12.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = userEmptyTextColor
                )
            }

            val now = java.time.LocalTime.now()
            val roundedHourStr = String.format("%02d:00", now.hour)

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isGrid) 4.dp else 6.dp)
            ) {
                val userEmptyTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                val userEmptyCaptureColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                Text(
                    text = roundedHourStr,
                    fontFamily = DelaGothicOneFontFamily,
                    fontSize = if (isGrid) 12.5.sp else 18.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = userEmptyTextColor,
                    modifier = Modifier.offset(y = 2.5.dp)
                )

                Box(
                    modifier = Modifier
                        .offset(y = (-2.5).dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.35f)
                        )
                        .clickable { onNavigateToCamera() }
                        .padding(horizontal = if (isGrid) 8.dp else 12.dp, vertical = if (isGrid) 2.dp else 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "tap to capture",
                        fontFamily = GoogleSansFontFamily,
                        fontSize = if (isGrid) 10.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = userEmptyCaptureColor
                    )
                }
            }

            // Triple dots for User slot empty box
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isGrid) 8.dp else 12.dp, end = if (isGrid) 10.dp else 16.dp)
                    .clickable {
                        android.widget.Toast.makeText(context, "Capture a pal first!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•••",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                )
            }
        }
    } else if (isActualMember) {
        // OTHER MEMBER CARD WITHOUT SUBMISSION (bouncing smiley & synced current time)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeightDp)
                .clip(cardShape)
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA))
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                    shape = cardShape
                )
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val smileySizePx = with(density) { (if (isGrid) 34.dp else 50.dp).toPx() }

            var groupPosX by remember { mutableStateOf(0f) }
            var groupPosY by remember { mutableStateOf(0f) }
            var groupSmileRotation by remember { mutableStateOf(0f) }
            var groupSmileColorIndex by remember { mutableStateOf(0) }

            LaunchedEffect(widthPx, heightPx) {
                if (widthPx <= 0f || heightPx <= 0f) return@LaunchedEffect
                groupPosX = (widthPx - smileySizePx) / 2f
                groupPosY = (heightPx - smileySizePx) / 2f

                val speed = with(density) { 100.dp.toPx() }
                var vx = speed * 0.76f
                var vy = speed * 0.65f

                var lastTime = androidx.compose.runtime.withFrameNanos { it }

                while (true) {
                    androidx.compose.runtime.withFrameNanos { time ->
                        val dt = (time - lastTime) / 1_000_000_000f
                        lastTime = time

                        val cappedDt = dt.coerceAtMost(0.1f)

                        var newX = groupPosX + vx * cappedDt
                        var newY = groupPosY + vy * cappedDt

                        var collided = false

                        val minX = 0f
                        val maxX = widthPx - smileySizePx
                        val minY = 0f
                        val maxY = heightPx - smileySizePx

                        if (maxX > 0f) {
                            if (newX <= minX) {
                                newX = minX
                                vx = -vx
                                collided = true
                            } else if (newX >= maxX) {
                                newX = maxX
                                vx = -vx
                                collided = true
                            }
                        }

                        if (maxY > 0f) {
                            if (newY <= minY) {
                                newY = minY
                                vy = -vy
                                collided = true
                            } else if (newY >= maxY) {
                                newY = maxY
                                vy = -vy
                                collided = true
                            }
                        }

                        groupPosX = newX
                        groupPosY = newY

                        groupSmileRotation = (groupSmileRotation + 120f * cappedDt) % 360f

                        if (collided) {
                            groupSmileColorIndex = (groupSmileColorIndex + 1) % shufflingColors.size
                        }
                    }
                }
            }

            // Draw the bouncing smiley
            Box(
                modifier = Modifier
                    .size(if (isGrid) 34.dp else 50.dp)
                    .offset(
                        x = with(density) { groupPosX.toDp() },
                        y = with(density) { groupPosY.toDp() }
                    )
                    .rotate(groupSmileRotation)
                    .clip(CircleShape)
                    .background(shufflingColors[groupSmileColorIndex]),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = if (isGrid) R.drawable.smile_small else R.drawable.smile_medium),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Draw Avatar and Name in Top Left
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = if (isGrid) 8.dp else 12.dp, start = if (isGrid) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userAvatar = if (isUser) customAvatarUriString else memberAvatar
                if (!userAvatar.isNullOrEmpty()) {
                    UriImage(
                        uriString = userAvatar,
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isGrid) 18.dp else 24.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.smile_medium),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(180f)
                        )
                    }
                }

                val memberTextColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                Text(
                    text = memberName ?: "",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 12.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = memberTextColor
                )
            }

            // Synced current time text in the middle
            val now = java.time.LocalTime.now()
            val roundedHourStr = String.format("%02d:00", now.hour)
            val memberTimeColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)

            Text(
                text = roundedHourStr,
                fontFamily = DelaGothicOneFontFamily,
                fontSize = if (isGrid) 12.5.sp else 18.5.sp,
                fontWeight = FontWeight.Bold,
                color = memberTimeColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        // INVITE SLOT (totalSlots > actualMembers)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeightDp)
                .clip(cardShape)
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA))
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                    shape = cardShape
                )
                .clickable { onInviteClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Friend",
                    tint = if (isDark) textColor else Color(0xFF8E8E93),
                    modifier = Modifier.size(if (isGrid) 20.dp else 28.dp)
                )
                Text(
                    text = "invite a friend",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 11.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDark) textColor else Color(0xFF8E8E93)
                )
            }
        }
    }
}

@Composable
fun GroupScreenContent(
    params: VlogScreenContentParams,
    pal: PalItem,
    groupMembers: List<String>,
    userFirstName: String,
    filteredSubmissions: List<SubmissionDbItem>,
    currentUserId: String,
    currentDisplayName: String,
    accentColor: Color,
    isDark: Boolean,
    textColor: Color,
    customAvatarUriString: String?,
    shufflingColors: List<Color>,
    selectedMemberIndex: Int,
    onSelectedMemberIndexChange: (Int) -> Unit,
    onNavigateToCamera: () -> Unit,
    capturedVlogsPaths: List<String>,
    selectedPageIndex: Int,
    onSelectedPageIndexChange: (Int) -> Unit,
    isEditingCaption: Boolean,
    onIsEditingCaptionChange: (Boolean) -> Unit,
    showDeleteVlogConfirmation: Boolean,
    onShowDeleteVlogConfirmationChange: (Boolean) -> Unit,
    editCaptionText: androidx.compose.ui.text.input.TextFieldValue,
    onEditCaptionTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onUpdateVlogCaption: (String, String) -> Unit,
    density: androidx.compose.ui.unit.Density,
    context: android.content.Context,
    palReactions: Map<String, String> = emptyMap(),
    onEmojiReacted: (String, String) -> Unit = { _, _ -> },
    onReplyClick: (String) -> Unit = {},
    messages: List<MessageDbItem> = emptyList()
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightDp = maxHeight
        val screenWidthDp = maxWidth

        val totalSlots = maxOf(groupMembers.size, pal.size.toIntOrNull() ?: 4)
        val isGrid = totalSlots > 5
        val contentSpacingDp = 2.5.dp
        val topPaddingDp = 100.dp
        val bottomPaddingDp = 24.dp
        val rows = (totalSlots + 1) / 2

        val totalSpacing = contentSpacingDp * (if (isGrid) (rows - 1) else (totalSlots - 1))
        val availableHeight = screenHeightDp - topPaddingDp - bottomPaddingDp - totalSpacing

        val cardHeightDp = if (isGrid) {
            val cardWidthDpGrid = (screenWidthDp - contentSpacingDp) / 2
            (availableHeight / rows).coerceAtMost(cardWidthDpGrid)
        } else {
            val cardWidthDp = screenWidthDp - 16.dp
            val maxCardHeight = cardWidthDp * (9f / 16f)
            (availableHeight / totalSlots).coerceAtMost(maxCardHeight)
        }

        val cardWidthDpGrid = (screenWidthDp - contentSpacingDp) / 2
        val cardHeightDpGrid = cardHeightDp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPaddingDp, bottom = bottomPaddingDp)
                .padding(horizontal = if (isGrid) 0.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(contentSpacingDp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (totalSlots <= 5) {
                for (index in 0 until totalSlots) {
                    GroupMemberCard(
                        index = index,
                        isGrid = false,
                        cardHeightDp = cardHeightDp,
                        groupMembers = groupMembers,
                        userFirstName = userFirstName,
                        filteredSubmissions = filteredSubmissions,
                        currentUserId = currentUserId,
                        currentDisplayName = currentDisplayName,
                        accentColor = accentColor,
                        isDark = isDark,
                        textColor = textColor,
                        customAvatarUriString = customAvatarUriString,
                        shufflingColors = shufflingColors,
                        selectedMemberIndex = selectedMemberIndex,
                        onSelectedMemberIndexChange = onSelectedMemberIndexChange,
                        onNavigateToCamera = onNavigateToCamera,
                        onEditCaptionClick = {
                            val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                            if (userIndex != -1) {
                                onSelectedPageIndexChange(userIndex)
                                onIsEditingCaptionChange(true)
                            }
                        },
                        onDeleteClick = {
                            val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                            if (userIndex != -1) {
                                onSelectedPageIndexChange(userIndex)
                                onShowDeleteVlogConfirmationChange(true)
                            }
                        },
                        onInviteClick = {
                            try {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: ${pal.code}")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        density = density,
                        context = context,
                        palReactions = palReactions,
                        onEmojiReacted = onEmojiReacted,
                        onReplyClick = onReplyClick,
                        messages = messages,
                        isEditingCaption = isEditingCaption,
                        onIsEditingCaptionChange = onIsEditingCaptionChange,
                        editCaptionText = editCaptionText,
                        onEditCaptionTextChange = onEditCaptionTextChange,
                        onUpdateVlogCaption = onUpdateVlogCaption,
                        capturedVlogsPaths = capturedVlogsPaths
                    )
                }
            } else {
                val rows = (totalSlots + 1) / 2
                for (r in 0 until rows) {
                    val index1 = r * 2
                    val index2 = r * 2 + 1

                    if (index2 < totalSlots) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(contentSpacingDp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                GroupMemberCard(
                                    index = index1,
                                    isGrid = true,
                                    cardHeightDp = cardHeightDpGrid,
                                    groupMembers = groupMembers,
                                    userFirstName = userFirstName,
                                    filteredSubmissions = filteredSubmissions,
                                    currentUserId = currentUserId,
                                    currentDisplayName = currentDisplayName,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    textColor = textColor,
                                    customAvatarUriString = customAvatarUriString,
                                    shufflingColors = shufflingColors,
                                    selectedMemberIndex = selectedMemberIndex,
                                    onSelectedMemberIndexChange = onSelectedMemberIndexChange,
                                    onNavigateToCamera = onNavigateToCamera,
                                    onEditCaptionClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onIsEditingCaptionChange(true)
                                        }
                                    },
                                    onDeleteClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onShowDeleteVlogConfirmationChange(true)
                                        }
                                    },
                                    onInviteClick = {
                                        try {
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: ${pal.code}")
                                                type = "text/plain"
                                            }
                                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    density = density,
                                    context = context,
                                    palReactions = palReactions,
                                    onEmojiReacted = onEmojiReacted,
                                    onReplyClick = onReplyClick,
                                    messages = messages,
                                    isEditingCaption = isEditingCaption,
                                    onIsEditingCaptionChange = onIsEditingCaptionChange,
                                    editCaptionText = editCaptionText,
                                    onEditCaptionTextChange = onEditCaptionTextChange,
                                    onUpdateVlogCaption = onUpdateVlogCaption,
                                    capturedVlogsPaths = capturedVlogsPaths
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                GroupMemberCard(
                                    index = index2,
                                    isGrid = true,
                                    cardHeightDp = cardHeightDpGrid,
                                    groupMembers = groupMembers,
                                    userFirstName = userFirstName,
                                    filteredSubmissions = filteredSubmissions,
                                    currentUserId = currentUserId,
                                    currentDisplayName = currentDisplayName,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    textColor = textColor,
                                    customAvatarUriString = customAvatarUriString,
                                    shufflingColors = shufflingColors,
                                    selectedMemberIndex = selectedMemberIndex,
                                    onSelectedMemberIndexChange = onSelectedMemberIndexChange,
                                    onNavigateToCamera = onNavigateToCamera,
                                    onEditCaptionClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onIsEditingCaptionChange(true)
                                        }
                                    },
                                    onDeleteClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onShowDeleteVlogConfirmationChange(true)
                                        }
                                    },
                                    onInviteClick = {
                                        try {
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: ${pal.code}")
                                                type = "text/plain"
                                            }
                                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    density = density,
                                    context = context,
                                    palReactions = palReactions,
                                    onEmojiReacted = onEmojiReacted,
                                    onReplyClick = onReplyClick,
                                    messages = messages,
                                    isEditingCaption = isEditingCaption,
                                    onIsEditingCaptionChange = onIsEditingCaptionChange,
                                    editCaptionText = editCaptionText,
                                    onEditCaptionTextChange = onEditCaptionTextChange,
                                    onUpdateVlogCaption = onUpdateVlogCaption,
                                    capturedVlogsPaths = capturedVlogsPaths
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.width(cardWidthDpGrid)) {
                                GroupMemberCard(
                                    index = index1,
                                    isGrid = true,
                                    cardHeightDp = cardHeightDpGrid,
                                    groupMembers = groupMembers,
                                    userFirstName = userFirstName,
                                    filteredSubmissions = filteredSubmissions,
                                    currentUserId = currentUserId,
                                    currentDisplayName = currentDisplayName,
                                    accentColor = accentColor,
                                    isDark = isDark,
                                    textColor = textColor,
                                    customAvatarUriString = customAvatarUriString,
                                    shufflingColors = shufflingColors,
                                    selectedMemberIndex = selectedMemberIndex,
                                    onSelectedMemberIndexChange = onSelectedMemberIndexChange,
                                    onNavigateToCamera = onNavigateToCamera,
                                    onEditCaptionClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onIsEditingCaptionChange(true)
                                        }
                                    },
                                    onDeleteClick = {
                                        val userIndex = filteredSubmissions.indexOfFirst { it.userId == currentUserId }
                                        if (userIndex != -1) {
                                            onSelectedPageIndexChange(userIndex)
                                            onShowDeleteVlogConfirmationChange(true)
                                        }
                                    },
                                    onInviteClick = {
                                        try {
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: ${pal.code}")
                                                type = "text/plain"
                                            }
                                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    density = density,
                                    context = context,
                                    palReactions = palReactions,
                                    onEmojiReacted = onEmojiReacted,
                                    onReplyClick = onReplyClick,
                                    messages = messages,
                                    isEditingCaption = isEditingCaption,
                                    onIsEditingCaptionChange = onIsEditingCaptionChange,
                                    editCaptionText = editCaptionText,
                                    onEditCaptionTextChange = onEditCaptionTextChange,
                                    onUpdateVlogCaption = onUpdateVlogCaption,
                                    capturedVlogsPaths = capturedVlogsPaths
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}

data class FeedItem(
    val path: String,
    val caption: String,
    val userId: String,
    val userDisplayName: String,
    val dayDateStr: String,
    val timeStr: String,
    val rawInstant: java.time.Instant,
    val localDate: java.time.LocalDate,
    val isUser: Boolean,
    val isSound: Boolean = false,
    val soundName: String = "",
    val soundUrl: String = "",
    val isTextMessage: Boolean = false,
    val textMessageContent: String = "",
    val messageId: String? = null
)

@kotlin.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VlogScreenContent(
    params: VlogScreenContentParams
) {
    val context = LocalContext.current
    val pal = params.pal
    val currentUserId = params.currentUserId
    val onBack = params.onBack
    val isDark = params.isDark
    val accentColor = params.accentColor
    val palTextLogoColor = params.palTextLogoColor
    val textColor = params.textColor
    val mutedTextColor = params.mutedTextColor
    val activeGradientColors = params.activeGradientColors
    val rotationAngle = params.rotationAngle
    val palsCount = params.palsCount
    val onStartCapture = params.onStartCapture
    val isCapturing = params.isCapturing
    val captureProgress = params.captureProgress
    val showDropdown = params.showDropdown
    val onShowDropdownChange = params.onShowDropdownChange
    val showChat = params.showChat
    val onShowChatChange = params.onShowChatChange
    val showEdit = params.showEdit
    val onShowEditChange = params.onShowEditChange
    val showDelete = params.showDelete
    val onShowDeleteChange = params.onShowDeleteChange
    val showLeave = params.showLeave
    val onShowLeaveChange = params.onShowLeaveChange
    val showExportDialog = params.showExportDialog
    val onShowExportDialogChange = params.onShowExportDialogChange
    val expandedMembers = params.expandedMembers
    val onExpandedMembersChange = params.onExpandedMembersChange
    val expandedSettings = params.expandedSettings
    val onExpandedSettingsChange = params.onExpandedSettingsChange
    val editName = params.editName
    val onEditNameChange = params.onEditNameChange
    val editSize = params.editSize
    val onEditSizeChange = params.onEditSizeChange
    val isEditingLoading = params.isEditingLoading
    val onStartSaveEdit = params.onStartSaveEdit
    val editDots = params.editDots
    val messages = params.messages
    val onSendMessage = params.onSendMessage
    val currentDisplayName = params.currentDisplayName
    val onDeletePal = params.onDeletePal
    val onLeavePal = params.onLeavePal
    val customAvatarUriString = params.customAvatarUriString
    val capturedVlogsPaths = params.capturedVlogsPaths
    val capturedVlogsTimes = params.capturedVlogsTimes
    val capturedVlogsCaptions = params.capturedVlogsCaptions
    val currentPlayingIndex = params.currentPlayingIndex
    val vlogPlaybackProgress = params.vlogPlaybackProgress
    val vlogExoPlayer = params.vlogExoPlayer
    val onNavigateToCamera = params.onNavigateToCamera
    val onDeleteVlog = params.onDeleteVlog
    val onUpdateVlogCaption = params.onUpdateVlogCaption
    val selectedDayOffset = params.selectedDayOffset
    val onSelectedDayOffsetChange = params.onSelectedDayOffsetChange
    val allPalsSubmissions = params.allPalsSubmissions
    val allPalsMembers = params.allPalsMembers

    val palReactions = params.palReactions
    val onEmojiReacted = params.onEmojiReacted
    val activeReplyPreviewPath = params.activeReplyPreviewPath
    val onActiveReplyPreviewPathChange = params.onActiveReplyPreviewPathChange
    val activeReactionPreview = params.activeReactionPreview
    val onActiveReactionPreviewChange = params.onActiveReactionPreviewChange
    val onDeleteMessageLocal = params.onDeleteMessageLocal

    val selectedProfileColor = remember(palTextLogoColor) {
        when (palTextLogoColor) {
            Color(0xFFFFE600) -> Color(0xFF00F0FF) // Yellow logo -> Blue profile
            Color(0xFF00F0FF) -> Color(0xFFFFE600) // Blue logo -> Yellow profile
            Color(0xFFFF6700) -> Color(0xFFFF007F) // Orange logo -> Pink profile
            Color(0xFFFF007F) -> Color(0xFFFF6700) // Pink logo -> Orange profile
            Color(0xFFFF073A) -> Color(0xFFB000FF) // Red logo -> Purple profile
            Color(0xFFB000FF) -> Color(0xFFFF073A) // Purple logo -> Red profile
            else -> Color(0xFFFFE600)
        }
    }

    val userFirstName = remember(currentDisplayName) {
        currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
    }
    val cachedMembers = params.allPalsMembers[pal.code] ?: emptyList()
    var groupMembers by remember(pal.code) {
        val initialList = if (cachedMembers.isNotEmpty()) {
            cachedMembers.map { member ->
                val parts = member.split("|||")
                if (parts.size >= 2) {
                    member
                } else {
                    val clean = member.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                    if (clean == userFirstName || clean == "$userFirstName (You)") {
                        "$currentUserId|||$userFirstName (You)|||"
                    } else {
                        "legacy_id|||$clean|||"
                    }
                }
            }
        } else {
            val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
            listOf("$currentUserId|||$userFirstName (You)|||${localAvatar ?: ""}")
        }
        mutableStateOf(initialList)
    }
    LaunchedEffect(pal.code, currentDisplayName) {
        if (pal.isVlog) {
            val localAvatar = if (customAvatarUriString?.startsWith("http") == true) customAvatarUriString else null
            groupMembers = listOf("$currentUserId|||$userFirstName (You)|||${localAvatar ?: ""}")
        }
    }

    LaunchedEffect(cachedMembers, userFirstName) {
        if (cachedMembers.isNotEmpty()) {
            groupMembers = cachedMembers.map { name ->
                if (name == userFirstName || name == "$userFirstName (You)") {
                    "$userFirstName (You)"
                } else {
                    name
                }
            }
        }
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    var selectedMemberIndex by remember(pal.code) { mutableStateOf(0) }
    val activePalSubmissions = params.allPalsSubmissions[pal.code] ?: emptyList<SubmissionDbItem>()
    val targetDate = remember(selectedDayOffset) {
        val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
        val activeLocalDate = if (now.hour < 4) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        activeLocalDate.minusDays(selectedDayOffset.toLong())
    }
    val filteredSubmissions = remember(activePalSubmissions, targetDate) {
        activePalSubmissions.filter { sub ->
            getSubmissionLocalDate(sub) == targetDate
        }
    }
    val filteredPaths = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(0) ?: ""
        }
    }
    val filteredTimes = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            if (!sub.createdAt.isNullOrEmpty()) {
                try {
                    val instant = java.time.Instant.parse(sub.createdAt)
                    val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                    zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
                } catch (e: Exception) {
                    sub.createdAt.substringAfter("T").substringBefore(".").take(5)
                }
            } else {
                "12:00"
            }
        }
    }
    val filteredCaptions = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(1) ?: ""
        }
    }
    val shufflingColors = remember {
        listOf(
            Color(0xFFFFE600), // Yellow
            Color(0xFFFF6700), // Orange
            Color(0xFFFF007F), // Pink
            Color(0xFF00F0FF), // Blue
            Color(0xFFB000FF), // Purple
            Color(0xFFFF073A)  // Red
        )
    }
    var selectedPageIndex by remember(pal.code) { mutableStateOf(0) }
    var inMembersSubMenu by remember { mutableStateOf(false) }
    var capsuleLeftDp by remember { mutableStateOf(0.dp) }
    var inSettingsSubMenu by remember { mutableStateOf(false) }
    var showArchiveView by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    var showTripleDotMenu by remember { mutableStateOf(false) }
    var isEditingCaption by remember { mutableStateOf(false) }
    var showDeleteVlogConfirmation by remember { mutableStateOf(false) }
    var savedVlogPaths by remember { mutableStateOf(setOf<String>()) }
    var editCaptionText by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    var isDropdownSaving by remember { mutableStateOf(false) }
    var isDropdownSaved by remember { mutableStateOf(false) }

    LaunchedEffect(isEditingCaption) {
        if (isEditingCaption) {
            val existingCaption = capturedVlogsCaptions.getOrNull(selectedPageIndex) ?: ""
            editCaptionText = androidx.compose.ui.text.input.TextFieldValue(
                text = existingCaption,
                selection = androidx.compose.ui.text.TextRange(existingCaption.length)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedDayOffset, onSelectedDayOffsetChange) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        val threshold = 100f
                        if (totalDrag > threshold) {
                            // Swipe Right -> Older day (increment offset)
                            if (selectedDayOffset < 6) {
                                onSelectedDayOffsetChange(selectedDayOffset + 1)
                            }
                        } else if (totalDrag < -threshold) {
                            // Swipe Left -> Newer day (decrement offset)
                            if (selectedDayOffset > 0) {
                                onSelectedDayOffsetChange(selectedDayOffset - 1)
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        if (showTripleDotMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showTripleDotMenu = false
                    }
            )
        }
        // Main centered card of the same size as the vlog box
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showArchiveView) {
                VlogArchiveCard(
                    activePalSubmissions = activePalSubmissions,
                    currentUserId = currentUserId,
                    selectedDayOffset = selectedDayOffset,
                    onSelectedDayOffsetChange = onSelectedDayOffsetChange,
                    isDark = isDark,
                    accentColor = accentColor,
                    selectedProfileColor = selectedProfileColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    onDismiss = { showArchiveView = false }
                )
            } else {
                if (!pal.isVlog) {
                    GroupScreenContent(
                        params = params,
                        pal = pal,
                        groupMembers = groupMembers,
                        userFirstName = userFirstName,
                        filteredSubmissions = filteredSubmissions,
                        currentUserId = currentUserId,
                        currentDisplayName = currentDisplayName,
                        accentColor = accentColor,
                        isDark = isDark,
                        textColor = textColor,
                        customAvatarUriString = customAvatarUriString,
                        shufflingColors = shufflingColors,
                        selectedMemberIndex = selectedMemberIndex,
                        onSelectedMemberIndexChange = { selectedMemberIndex = it },
                        onNavigateToCamera = onNavigateToCamera,
                        capturedVlogsPaths = capturedVlogsPaths,
                        selectedPageIndex = selectedPageIndex,
                        onSelectedPageIndexChange = { selectedPageIndex = it },
                        isEditingCaption = isEditingCaption,
                        onIsEditingCaptionChange = { isEditingCaption = it },
                        showDeleteVlogConfirmation = showDeleteVlogConfirmation,
                        onShowDeleteVlogConfirmationChange = { showDeleteVlogConfirmation = it },
                        editCaptionText = editCaptionText,
                        onEditCaptionTextChange = { editCaptionText = it },
                        onUpdateVlogCaption = onUpdateVlogCaption,
                        density = density,
                        context = context,
                        palReactions = palReactions,
                        onEmojiReacted = onEmojiReacted,
                        onReplyClick = { path ->
                            onActiveReplyPreviewPathChange(path)
                        },
                        messages = messages
                    )
                } else {
                    // main centered vlog video card
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val screenWidthPx = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
                    val parentPaddingPx = with(density) { 24.dp.roundToPx() }
                    val targetPaddingPx = with(density) { 8.dp.roundToPx() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {


                        Box(
                            modifier = Modifier
                        .offset(y = -2.5.dp) // shift vlog box up by 3dp (from 0.5dp to -2.5dp)
                        .layout { measurable, constraints ->
                            val targetWidth = screenWidthPx - (2 * targetPaddingPx)
                            val newConstraints = constraints.copy(
                                minWidth = targetWidth,
                                maxWidth = targetWidth
                            )
                            val placeable = measurable.measure(newConstraints)
                            val shiftPx = 16.dp.roundToPx()
                            layout(placeable.width, placeable.height) {
                                placeable.place(targetPaddingPx - parentPaddingPx + shiftPx, 0)
                            }
                        }
                ) {
                    val cardWidthDp = with(density) { (screenWidthPx - (2 * targetPaddingPx)).toDp() }
                    val cardHeightDp = cardWidthDp * (9f / 16f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (capturedVlogsPaths.isNotEmpty()) {
                                    Color.Black
                                } else {
                                    if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA) // Charcoal in dark mode, grey in light mode
                                }
                            )
                    ) {
                    // If there are vlogs, play them inside a Crossfade overlay view
                    if (capturedVlogsPaths.isNotEmpty()) {
                        // Sync local state with currentPlayingIndex if changed externally
                        LaunchedEffect(currentPlayingIndex) {
                            if (currentPlayingIndex < capturedVlogsPaths.size && currentPlayingIndex >= 0) {
                                selectedPageIndex = currentPlayingIndex
                            }
                        }

                        // Sync local selection back to the ExoPlayer media item index
                        LaunchedEffect(selectedPageIndex) {
                            if (vlogExoPlayer.currentMediaItemIndex != selectedPageIndex && selectedPageIndex < capturedVlogsPaths.size) {
                                vlogExoPlayer.seekTo(selectedPageIndex, 0L)
                            }
                        }

                        androidx.compose.animation.Crossfade(
                            targetState = selectedPageIndex,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 350),
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val path = capturedVlogsPaths.getOrNull(page)
                            if (path != null) {
                                // Separate local ExoPlayer for each page to allow rendering adjacent videos during transitions (no black screens)
                                @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
                                val localPlayer = remember(path) {
                                    androidx.media3.exoplayer.ExoPlayer.Builder(context)
                                        .setRenderersFactory(
                                            androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                                                setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                                                setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                            }
                                        )
                                        .setMediaSourceFactory(
                                            androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                                                .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context))
                                        )
                                        .build().apply {
                                            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                                            volume = 0f
                                        val localPath = getVlogPrefs(context).getString("local_path_$path", null)
                                        val resolvedPath = localPath ?: path
                                        if (resolvedPath.startsWith("http")) {
                                            setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(resolvedPath)))
                                            prepare()
                                        } else {
                                            val cleanPath = when {
                                                resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                                                else -> resolvedPath
                                            }
                                            val file = java.io.File(cleanPath)
                                            if (file.exists()) {
                                                setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                                                prepare()
                                            } else if (path.startsWith("http")) {
                                                setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(path)))
                                                prepare()
                                            }
                                        }
                                    }
                                }

                                DisposableEffect(localPlayer) {
                                    onDispose {
                                        localPlayer.release()
                                    }
                                }

                                // Play the active video immediately
                                LaunchedEffect(localPlayer) {
                                    localPlayer.playWhenReady = true
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    androidx.compose.ui.viewinterop.AndroidView(
                                        factory = { ctx ->
                                            val view = android.view.LayoutInflater.from(ctx)
                                                .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                                            view.layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            view.apply {
                                                player = localPlayer
                                                useController = false
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                                                setBackgroundColor(android.graphics.Color.BLACK)
        
                                                fun applyVideoScale() {
                                                    val videoSize = localPlayer.videoSize
                                                    val videoWidth = videoSize.width
                                                    val videoHeight = videoSize.height
                                                    val textureView = getVideoSurfaceView() as? android.view.TextureView
                                                    if (textureView == null) {
                                                        postDelayed({ applyVideoScale() }, 100)
                                                        return
                                                    }
                                                    val containerWidth = width.toFloat()
                                                    val containerHeight = height.toFloat()
                                                    if (containerWidth > 0f && containerHeight > 0f && videoWidth > 0 && videoHeight > 0) {
                                                        val isPortrait = videoHeight > videoWidth
                                                        val rotatedWidth = if (isPortrait) videoHeight.toFloat() else videoWidth.toFloat()
                                                        val rotatedHeight = if (isPortrait) videoWidth.toFloat() else videoHeight.toFloat()
                                                        
                                                        val scale = java.lang.Math.max(containerWidth / rotatedWidth, containerHeight / rotatedHeight)
                                                        
                                                        val calculatedScaleX: Float
                                                        val calculatedScaleY: Float
                                                        val calculatedRotation: Float
                                                        if (isPortrait) {
                                                            calculatedScaleX = (rotatedHeight * scale) / containerWidth
                                                            calculatedScaleY = (rotatedWidth * scale) / containerHeight
                                                            calculatedRotation = 270f
                                                        } else {
                                                            calculatedScaleX = (rotatedWidth * scale) / containerWidth
                                                            calculatedScaleY = (rotatedHeight * scale) / containerHeight
                                                            calculatedRotation = 0f
                                                        }
                                                        
                                                        android.util.Log.d("PalVideoScale", "Reverted scale for localPlayer: video=${videoWidth}x${videoHeight}, container=${containerWidth}x${containerHeight}, scaleX=${calculatedScaleX}, scaleY=${calculatedScaleY}, rotation=${calculatedRotation}")
                                                        
                                                        textureView.pivotX = containerWidth / 2f
                                                        textureView.pivotY = containerHeight / 2f
                                                        textureView.scaleX = calculatedScaleX
                                                        textureView.scaleY = calculatedScaleY
                                                        textureView.rotation = calculatedRotation
                                                    } else {
                                                        postDelayed({ applyVideoScale() }, 100)
                                                    }
                                                }

                                                val layoutListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                                                    applyVideoScale()
                                                }
                                                addOnLayoutChangeListener(layoutListener)

                                                localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                                        super.onVideoSizeChanged(videoSize)
                                                        applyVideoScale()
                                                    }
                                                    override fun onPlaybackStateChanged(playbackState: Int) {
                                                        super.onPlaybackStateChanged(playbackState)
                                                        applyVideoScale()
                                                    }
                                                })

                                                applyVideoScale()
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        update = { view ->
                                            if (view.player != localPlayer) {
                                                view.player = localPlayer
                                            }
                                            if (!localPlayer.isPlaying && localPlayer.playbackState == androidx.media3.common.Player.STATE_READY) {
                                                localPlayer.play()
                                            }
                                        }
                                    )

                                    // Invisible click overlay for left/right tap navigation (occupying full size of the box)
                                    Row(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        // Left 50% Click Area -> Newer Vlog / Successive (decrease page index)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (selectedPageIndex > 0) {
                                                        selectedPageIndex -= 1
                                                    }
                                                }
                                        )
                                        // Right 50% Click Area -> Older Vlog / Preceding (increase page index)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (selectedPageIndex < capturedVlogsPaths.lastIndex) {
                                                        selectedPageIndex += 1
                                                    }
                                                }
                                        )
                                    }

                                // Overlay 1: Profile picture & user's name on top left (avatar size = 15.dp, text size = 15.sp)
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 5.5.dp, start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (customAvatarUriString != null) {
                                        UriImage(
                                            uriString = customAvatarUriString,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(accentColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.smile_medium),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .rotate(180f)
                                            )
                                        }
                                    }

                                    Text(
                                        text = currentDisplayName,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White
                                    )
                                }

                                // Overlay 2: vlog name (left center) and time text (right center), time text size to perfect 12.5sp
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showEdit && editName.isNotEmpty()) editName else pal.name,
                                        fontFamily = BricolageVariableFontFamily,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                blurRadius = 3f
                                            )
                                        )
                                    )

                                    val currentTime = capturedVlogsTimes.getOrNull(page) ?: "12:00"
                                    Text(
                                        text = currentTime,
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                blurRadius = 3f
                                            )
                                        )
                                    )
                                }

                                // Overlay 3: custom caption text (if present) at center or text field when editing
                                if (isEditingCaption && page == selectedPageIndex) {
                                    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                                    LaunchedEffect(Unit) {
                                        focusRequester.requestFocus()
                                    }

                                    androidx.compose.foundation.text.BasicTextField(
                                        value = editCaptionText,
                                        onValueChange = { editCaptionText = it },
                                        textStyle = TextStyle(
                                            fontFamily = RobotoFontFamily,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                blurRadius = 3f
                                            )
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(accentColor),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(horizontal = 48.dp)
                                            .focusRequester(focusRequester),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.Center) {
                                                if (editCaptionText.text.isEmpty()) {
                                                    Text(
                                                        text = "write caption...",
                                                        fontFamily = RobotoFontFamily,
                                                        fontSize = 22.sp,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                } else {
                                    val currentCaption = capturedVlogsCaptions.getOrNull(page) ?: ""
                                    if (currentCaption.isNotEmpty()) {
                                        Text(
                                            text = currentCaption,
                                            fontFamily = RobotoFontFamily,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(horizontal = 48.dp),
                                            textAlign = TextAlign.Center,
                                            style = TextStyle(
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                    blurRadius = 3f
                                                )
                                            )
                                        )
                                    }
                                }

                                // Overlay 4: Triple dots at bottom right (white color)
                                Box(
                                     modifier = Modifier
                                         .align(Alignment.BottomEnd)
                                         .padding(bottom = 12.dp, end = 12.dp)
                                         .clip(CircleShape)
                                         .clickable { showTripleDotMenu = true }
                                         .padding(8.dp),
                                     contentAlignment = Alignment.Center
                                 ) {
                                    Text(
                                        text = "•••",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                blurRadius = 3f
                                            )
                                        )
                                    )
                                }

                                // Overlay 5: Top Right Checkmark Save button (only visible during caption editing mode)
                                if (isEditingCaption && page == selectedPageIndex) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 8.dp, end = 12.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clickable {
                                                val targetPath = filteredPaths.getOrNull(selectedPageIndex) ?: ""
                                                if (targetPath.isNotEmpty()) {
                                                    onUpdateVlogCaption(targetPath, editCaptionText.text.trim())
                                                }
                                                isEditingCaption = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save Caption",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                    } else {
                        // Custom empty state card with DVD bounce screensaver smiley
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val widthPx = with(density) { maxWidth.toPx() }
                            val heightPx = with(density) { maxHeight.toPx() }
                            val smileySizePx = with(density) { 50.dp.toPx() }

                            val profileColors = remember {
                                listOf(
                                    Color(0xFFFFE600), // yellow
                                    Color(0xFFFF6700), // orange
                                    Color(0xFFFF007F), // pink
                                    Color(0xFF00F0FF), // blue
                                    Color(0xFFB000FF), // purple
                                    Color(0xFFFF073A)  // red
                                )
                            }

                            val currentTimeText = remember {
                                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
                            }

                            // Local animation state for the bouncing smiley inside vlog empty state
                            var localPosX by remember { mutableStateOf(0f) }
                            var localPosY by remember { mutableStateOf(0f) }
                            var localSmileRotation by remember { mutableStateOf(0f) }
                            var localSmileColorIndex by remember { mutableStateOf(0) }

                            LaunchedEffect(widthPx, heightPx) {
                                if (widthPx <= 0f || heightPx <= 0f) return@LaunchedEffect
                                // Start at the center of the box
                                localPosX = (widthPx - smileySizePx) / 2f
                                localPosY = (heightPx - smileySizePx) / 2f

                                val speed = with(density) { 100.dp.toPx() }
                                var vx = speed * 0.76f
                                var vy = speed * 0.65f

                                var lastTime = androidx.compose.runtime.withFrameNanos { it }

                                while (true) {
                                    androidx.compose.runtime.withFrameNanos { time ->
                                        val dt = (time - lastTime) / 1_000_000_000f
                                        lastTime = time

                                        val cappedDt = dt.coerceAtMost(0.1f)

                                        var newX = localPosX + vx * cappedDt
                                        var newY = localPosY + vy * cappedDt

                                        var collided = false

                                        val minX = 0f
                                        val maxX = widthPx - smileySizePx
                                        val minY = 0f
                                        val maxY = heightPx - smileySizePx

                                        if (maxX > 0f) {
                                            if (newX <= minX) {
                                                newX = minX
                                                vx = -vx
                                                collided = true
                                            } else if (newX >= maxX) {
                                                newX = maxX
                                                vx = -vx
                                                collided = true
                                            }
                                        }

                                        if (maxY > 0f) {
                                            if (newY <= minY) {
                                                newY = minY
                                                vy = -vy
                                                collided = true
                                            } else if (newY >= maxY) {
                                                newY = maxY
                                                vy = -vy
                                                collided = true
                                            }
                                        }

                                        localPosX = newX
                                        localPosY = newY

                                        localSmileRotation = (localSmileRotation + 120f * cappedDt) % 360f

                                        if (collided) {
                                            localSmileColorIndex = (localSmileColorIndex + 1) % profileColors.size
                                        }
                                    }
                                }
                            }

                            // 1. Bouncing animated rotating smiley (size 50.dp) - circular background filled with dynamic color, eyes/mouth in black (rendered first so it is at the very background of the vlog and time texts)
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .offset(
                                        x = with(density) { localPosX.toDp() },
                                        y = with(density) { localPosY.toDp() }
                                    )
                                    .rotate(localSmileRotation)
                                    .clip(CircleShape)
                                    .background(profileColors[localSmileColorIndex]),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.capture_smile),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // 2. Profile photo and user name on top left matching captured state padding and display name
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 5.5.dp, start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (customAvatarUriString != null) {
                                    UriImage(
                                        uriString = customAvatarUriString,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.smile_medium),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .rotate(180f)
                                        )
                                    }
                                }

                                val vlogEmptyTextColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                                Text(
                                    text = currentDisplayName,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = vlogEmptyTextColor
                                )
                            }

                            // 3. Middle left: pal.name, Middle right: currentTimeText in adaptive text color without shadow
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val vlogEmptyTextColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                                Text(
                                    text = if (showEdit && editName.isNotEmpty()) editName else pal.name,
                                    fontFamily = BricolageVariableFontFamily,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vlogEmptyTextColor
                                )

                                Text(
                                    text = currentTimeText,
                                    fontFamily = RobotoFontFamily,
                                    fontSize = 12.5.sp, // reduced to 12.5sp exactly
                                    fontWeight = FontWeight.Normal,
                                    color = vlogEmptyTextColor
                                )
                            }

                            // 4. Center: Pill shape button "tap to capture" with compact size, frosted glass background, no border
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.35f)
                                    )
                                    .clickable { onNavigateToCamera() }
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val vlogEmptyCaptureColor = if (isDark) Color(0xFF5C5E62) else Color(0xFF8E8E93)
                                Text(
                                    text = "tap to capture",
                                    fontFamily = GoogleSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vlogEmptyCaptureColor
                                )
                            }
                        }
                    }
                    }

                    // 2. Context Menu Dropdown Popup aligned TopEnd (right edge matching the card's right edge), offset by y = cardHeightDp
                    if (showTripleDotMenu) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(y = cardHeightDp)
                                .width(185.dp) // reduced by 5dp
                                .background(if (isDark) Color(0xFF1E1D22) else Color(0xFFF5F3EB), RoundedCornerShape(4.dp))
                                .padding(vertical = 8.dp)
                                .zIndex(10f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val currentPath = capturedVlogsPaths.getOrNull(selectedPageIndex) ?: ""
                                val isSaved = savedVlogPaths.contains(currentPath) || isDropdownSaved

                                // Option 1: edit caption
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showTripleDotMenu = false
                                            isEditingCaption = true
                                        }
                                        .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "edit caption",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }

                                // Option 2: save
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!isDropdownSaving && !isSaved && currentPath.isNotEmpty()) {
                                                isDropdownSaving = true
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    val cleanPath = when {
                                                        currentPath.startsWith("file://") -> currentPath.substring(7)
                                                        else -> currentPath
                                                    }
                                                    val tempOut = java.io.File(context.cacheDir, "temp_dropdown_save_${System.currentTimeMillis()}.mp4")
                                                    val caption = capturedVlogsCaptions.getOrNull(selectedPageIndex) ?: ""
                                                    val timeStr = capturedVlogsTimes.getOrNull(selectedPageIndex) ?: ""
                                                    
                                                    VideoProcessor.processVideo(
                                                        context = context,
                                                        inputPath = cleanPath,
                                                        outputPath = tempOut.absolutePath,
                                                        vlogText = pal.name,
                                                        timeText = timeStr,
                                                        captionText = caption,
                                                        roundedCorners = false
                                                    ) { success ->
                                                        if (success) {
                                                            val saveSuccess = saveVideoToGallery(context, tempOut.absolutePath)
                                                            if (saveSuccess) {
                                                                isDropdownSaved = true
                                                                savedVlogPaths = savedVlogPaths + currentPath
                                                                coroutineScope.launch {
                                                                    kotlinx.coroutines.delay(2000)
                                                                    isDropdownSaved = false
                                                                    showTripleDotMenu = false
                                                                }
                                                            }
                                                        }
                                                        try { tempOut.delete() } catch(e: Exception) {}
                                                        isDropdownSaving = false
                                                    }
                                                }
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isSaved) "saved" else "save",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    if (isDropdownSaving) {
                                        CircularProgressIndicator(
                                            color = if (isDark) Color.White else Color.Black,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        AnimatedSaveIcon(
                                            isSaved = isSaved,
                                            tint = if (isDark) Color.White else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Option 3: delete
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showTripleDotMenu = false
                                            showDeleteVlogConfirmation = true
                                        }
                                        .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "delete",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
                }
                }
            }
        }

        // Top Bar Layout (overlayed on top of the centered box)
        val headerButtonBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        val headerIconTint = if (isDark) Color.White else Color.Black

        // Dynamic font size calculation based on text length to fit exactly at center and not overlap
        val displayText = if (selectedDayOffset > 0) {
            val targetDate = java.time.LocalDate.now().minusDays(selectedDayOffset.toLong())
            targetDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
        } else {
            if (showEdit && editName.isNotEmpty()) editName else pal.name
        }
        val textLength = displayText.length

        // Vlog font size: base is 19.5f. We reduce it as length increases.
        val vlogFontSize = when {
            textLength <= 8 -> 19.5f
            textLength <= 14 -> 19.5f - (textLength - 8) * 0.8f
            textLength <= 20 -> 14.7f - (textLength - 14) * 0.5f
            else -> 11.7f - (textLength - 20) * 0.2f
        }.coerceAtLeast(10f).sp

        // Group font size: base is 16f. We reduce it as length increases.
        val groupFontSize = when {
            textLength <= 6 -> 16.0f
            textLength <= 10 -> 14.5f
            textLength <= 14 -> 13.0f
            textLength <= 18 -> 11.5f
            textLength <= 22 -> 10.0f
            else -> 8.5f
        }.sp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(60.dp)
        ) {
            // Left: back chevron and calendar button with dark transparent bg
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.5.dp)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(32.5.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    ChevronLeftIcon(
                        tint = headerIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Calendar Button
                Box(
                    modifier = Modifier
                        .size(32.5.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { showArchiveView = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CalendarMonthIcon,
                        contentDescription = "calendar month",
                        tint = headerIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
                   // Center: dropdown capsule & horizontal smileys row underneath (using Box to prevent shifting)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dropdown capsule button (width reduced by 5dp -> horizontal padding = 13.5.dp, height reduced by 3dp to 37.dp)
                // For Vlog, show plain text in middle, else show capsule
                if (pal.isVlog) {
                    val targetDate = remember(selectedDayOffset) {
                        java.time.LocalDate.now().minusDays(selectedDayOffset.toLong())
                    }
                    val dayName = remember(targetDate) {
                        targetDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                    }
                    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                    Row(
                        modifier = Modifier
                            .offset(y = (-5).dp) // offset so it aligns exactly at y = 2.dp (7 - 5 = 2.dp)
                            .height(32.5.dp) // background pill height matches 32.5.dp
                            .clip(RoundedCornerShape(16.25.dp))
                            .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFEBEBEB))
                            .then(
                                if (selectedDayOffset == 0) {
                                    Modifier.clickable { onShowDropdownChange(true) }
                                } else {
                                    Modifier
                                }
                            )
                            .onGloballyPositioned { coordinates ->
                                val localPos = coordinates.positionInRoot()
                                capsuleLeftDp = with(density) { localPos.x.toDp() }
                            }
                            .widthIn(max = (screenWidthDp - 225.dp).coerceAtLeast(100.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        Text(
                            text = if (selectedDayOffset > 0) dayName else (if (showEdit && editName.isNotEmpty()) editName else pal.name),
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = vlogFontSize,
                            fontWeight = FontWeight.Bold,
                            color = headerIconTint,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (selectedDayOffset == 0) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "dropdown arrow",
                                tint = headerIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (capturedVlogsPaths.isNotEmpty()) {
                        Row(
                            modifier = Modifier.offset(y = 26.dp), // offset downwards below the vlog capsule
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            capturedVlogsPaths.forEachIndexed { index, _ ->
                                val isSelected = index == selectedPageIndex
                                Box(
                                    modifier = Modifier
                                        .size(17.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(1.dp, palTextLogoColor, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(1.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(selectedProfileColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.smile_small),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .rotate(180f),
                                            colorFilter = ColorFilter.tint(Color.Black)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val targetDate = remember(selectedDayOffset) {
                        java.time.LocalDate.now().minusDays(selectedDayOffset.toLong())
                    }
                    val dayName = remember(targetDate) {
                        targetDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                    }
                    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                    Row(
                        modifier = Modifier
                            .offset(y = -5.dp) // offset so it aligns exactly at y = 2.dp (7 - 5 = 2.dp)
                            .height(32.5.dp) // background pill height matches 32.5.dp
                            .clip(RoundedCornerShape(16.25.dp))
                            .background(headerButtonBg)
                            .then(
                                if (selectedDayOffset == 0) {
                                    Modifier.clickable { onShowDropdownChange(true) }
                                } else {
                                    Modifier
                                }
                            )
                            .onGloballyPositioned { coordinates ->
                                val localPos = coordinates.positionInRoot()
                                capsuleLeftDp = with(density) { localPos.x.toDp() }
                            }
                            .widthIn(max = (screenWidthDp - 225.dp).coerceAtLeast(100.dp))
                            .padding(horizontal = 13.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        Text(
                            text = if (selectedDayOffset > 0) dayName else (if (showEdit && editName.isNotEmpty()) editName else pal.name),
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = groupFontSize,
                            fontWeight = FontWeight.Bold,
                            color = headerIconTint,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (selectedDayOffset == 0) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "dropdown arrow",
                                tint = headerIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Horizontal row of small smileys showing the pals sent to vlog menu count (spaced 12.5dp below capsule, filled with boundary/accentColor, with active ring)
                    val memberCount = groupMembers.size
                    val computedSmileySize = when {
                        memberCount <= 4 -> 22.dp
                        memberCount <= 6 -> 16.dp
                        memberCount <= 8 -> 12.dp
                        else -> 9.dp
                    }
                    val computedInnerSize = when {
                        memberCount <= 4 -> 15.dp
                        memberCount <= 6 -> 11.dp
                        memberCount <= 8 -> 8.5.dp
                        else -> 6.5.dp
                    }
                    GroupMembersSmileysRow(
                        members = groupMembers,
                        submissions = filteredSubmissions,
                        isDark = isDark,
                        accentColor = accentColor,
                        palTextLogoColor = palTextLogoColor,
                        currentUserId = currentUserId,
                        userFirstName = userFirstName,
                        smileySize = computedSmileySize,
                        innerSize = computedInnerSize,
                        showOnlyLit = true,
                        modifier = Modifier.offset(y = 27.5.dp)
                    )
                }
            }

            // Right: Share (Export) & Chat bubble buttons
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share/Export Button
                Box(
                    modifier = Modifier
                        .size(32.5.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { onShowExportDialogChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    ShareIcon(
                        tint = headerIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Chat bubble button
                Box(
                    modifier = Modifier
                        .size(32.5.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { onShowChatChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    ChatBubbleIcon(
                        tint = headerIconTint,
                        modifier = Modifier.size(18.dp),
                        filled = isDark
                    )
                }
            }
        }
        
        // --- Vlog dropdown menu overlay ---
        if (showDropdown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onShowDropdownChange(false)
                        inSettingsSubMenu = false
                        inMembersSubMenu = false
                    }
            ) {
                val isSubMenuOpen = inSettingsSubMenu || inMembersSubMenu
                val dropdownWidth = if (isSubMenuOpen) 172.5.dp else 185.dp
                val submenuHeaderTextSize = 12.5.sp

                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = capsuleLeftDp, top = 78.dp) // Aligns with the capsule's left edge
                        .width(dropdownWidth)
                        .background(if (isDark) Color(0xFF161616) else Color(0xFFF5F3EB)) // Plain square box with no border, adapts to light/dark mode
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (inSettingsSubMenu) {
                            // Settings Sub-menu (Image 4 style: settings, edit pal, delete pal)
                            
                            // 1. settings row (acts as header / back action)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inSettingsSubMenu = false }
                                    .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "settings",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = submenuHeaderTextSize,
                                    color = textColor
                                )
                            }
                            
                            // 2. edit pal option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onShowDropdownChange(false)
                                        inSettingsSubMenu = false
                                        onShowEditChange(true)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "edit pal",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                            }
                            
                            // 3. delete pal (or leave pal) option
                            if (!pal.isVlog) {
                                if (pal.isCreator) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onShowDropdownChange(false)
                                                inSettingsSubMenu = false
                                                onShowDeleteChange(true)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "delete pal",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 15.sp,
                                            color = textColor
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onShowDropdownChange(false)
                                                inSettingsSubMenu = false
                                                onShowLeaveChange(true)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "leave pal",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 15.sp,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        } else if (inMembersSubMenu) {
                            // Members Sub-menu (styled identically to settings submenu)
                            
                            // 1. members row (acts as header / back action)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inMembersSubMenu = false }
                                    .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "members",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = submenuHeaderTextSize,
                                    color = textColor
                                )
                            }
                            
                            groupMembers.forEach { memberInfo ->
                                val memberParts = memberInfo.split("|||")
                                val memberName = if (memberParts.size >= 2) memberParts[1] else memberInfo
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• $memberName",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else {
                            // Main dropdown menu list (members, settings)

                            // 1.5. code option
                            if (!pal.isVlog) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Pal Code", pal.code)
                                                clipboard.setPrimaryClip(clip)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "code: ${pal.code}",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = textColor
                                    )
                                }
                            }
                            
                            // 2. members option (with right chevron)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inMembersSubMenu = true }
                                    .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "members",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // 3. settings option (with right chevron)
                            if (!pal.isVlog) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { inSettingsSubMenu = true }
                                        .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "settings",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = textColor
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }


                        }
                    }
                }
            }
        }
        
        // --- Edit Pal Overlay (Image 5 Style) ---
        // --- Edit Pal Overlay (Image 5 Style) ---
        if (showEdit) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black else PalBackground) // full screen background
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                if (isEditingLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = accentColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "editing pal$editDots",
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 16.sp,
                            color = textColor
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                    ) {
                        // Header Row: Close Button, Title (pal), Checkmark Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .height(64.dp)
                        ) {
                            // Left Close button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(headerButtonBg)
                                    .clickable { onShowEditChange(false) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = headerIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Middle pal Title
                            Text(
                                text = "Pal",
                                fontFamily = OwnglyphFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = palTextLogoColor,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            // Right Checkmark Save button in a solid circle of boundary edge color (accentColor)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .clickable {
                                        if (editName.trim().isNotEmpty()) {
                                            onStartSaveEdit()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_checkmark_custom),
                                    contentDescription = "Save",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Body: edit pal label, custom text input field, and pal size options
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Label: → edit pal (using Monospace)
                            Text(
                                text = "→ edit pal",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                color = textColor
                            )

                            // Text field: vlog (using Monospace, clean text display)
                            androidx.compose.foundation.text.BasicTextField(
                                value = editName,
                                onValueChange = { onEditNameChange(it) },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textColor
                                ),
                                singleLine = true,
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Label: pal size (using Monospace)
                            Text(
                                text = "pal size",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                color = textColor
                            )

                            // Sizes grid: Row 1 (2..6), Row 2 (7..10)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (s in 2..6) {
                                        val isSelected = editSize == s.toString()
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) accentColor 
                                                    else (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA).copy(alpha = 0.5f))
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) accentColor 
                                                            else (if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onEditSizeChange(s.toString()) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = s.toString(),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else textColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (s in 7..10) {
                                        val isSelected = editSize == s.toString()
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) accentColor 
                                                    else (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA).copy(alpha = 0.5f))
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) accentColor 
                                                            else (if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onEditSizeChange(s.toString()) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = s.toString(),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // --- Delete Pal Dialog ---
        if (showDelete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShowDeleteChange(false) },
                contentAlignment = Alignment.Center
            ) {
                val dialogBg = if (isDark) Color(0xFF2B2930) else Color(0xFFF5F3EB)
                val titleColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
                val subtitleColor = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F)
                val buttonColor = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 320.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(dialogBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "are you sure you want to delete this pal permanently?",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = titleColor,
                            lineHeight = 22.sp
                        )
                        
                        Text(
                            text = "This removes the pal for everyone.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = subtitleColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = buttonColor,
                                modifier = Modifier
                                    .clickable { onShowDeleteChange(false) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "delete",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = buttonColor,
                                modifier = Modifier
                                    .clickable {
                                        onShowDeleteChange(false)
                                        onDeletePal()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // --- Leave Dialog ---
        if (showLeave) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShowLeaveChange(false) },
                contentAlignment = Alignment.Center
            ) {
                GlassmorphicCard(
                    modifier = Modifier
                        .width(280.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    borderRadius = 24.dp,
                    isDark = isDark,
                    gradientColors = if (isDark) activeGradientColors else listOf(Color(0xFFF5F3EB), Color(0xFFF5F3EB)),
                    borderColor = if (isDark) accentColor else Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "leave group",
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        Text(
                            text = "are you sure you want to leave this group? you will lose access to future pals.",
                            fontSize = 13.sp,
                            color = textColor
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassmorphicCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                onClick = { onShowLeaveChange(false) },
                                borderRadius = 20.dp,
                                isDark = isDark
                            ) {
                                Text(
                                    text = "cancel",
                                    fontSize = 13.sp,
                                    color = textColor
                                )
                            }
                            
                            GlassmorphicCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                onClick = {
                                    onShowLeaveChange(false)
                                    onLeavePal()
                                },
                                borderRadius = 20.dp,
                                isDark = isDark,
                                gradientColors = listOf(
                                    Color(0xFFF35F38).copy(alpha = 0.85f),
                                    Color(0xFFF35F38).copy(alpha = 0.70f)
                                ),
                                borderColor = Color(0xFFF35F38)
                            ) {
                                Text(
                                    text = "leave",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // --- Delete Vlog Confirmation Dialog ---
        DeleteVlogConfirmationDialog(
            showDeleteVlogConfirmation = showDeleteVlogConfirmation,
            isDark = isDark,
            capturedVlogsPaths = capturedVlogsPaths,
            selectedPageIndex = selectedPageIndex,
            onSelectedPageIndexChange = { selectedPageIndex = it },
            onShowDeleteVlogConfirmationChange = { showDeleteVlogConfirmation = it },
            onDeleteVlog = onDeleteVlog
        )

        // --- Pal Chat Overlay ---
        PalChatOverlay(
            showChat = showChat,
            pal = pal,
            isDark = isDark,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            accentColor = accentColor,
            headerButtonBg = headerButtonBg,
            selectedProfileColor = selectedProfileColor,
            capturedVlogsPaths = capturedVlogsPaths,
            capturedVlogsCaptions = capturedVlogsCaptions,
            allPalsSubmissions = allPalsSubmissions,
            currentUserId = currentUserId,
            currentDisplayName = currentDisplayName,
            palReactions = palReactions,
            onEmojiReacted = onEmojiReacted,
            onActiveReplyPreviewPathChange = onActiveReplyPreviewPathChange,
            onShowChatChange = onShowChatChange,
            onNavigateToCamera = onNavigateToCamera,
            onSendMessage = onSendMessage,
            onShowExportDialogChange = onShowExportDialogChange,
            customAvatarUriString = customAvatarUriString,
            allPalsMembers = allPalsMembers,
            messages = messages,
            onDeleteMessageLocal = onDeleteMessageLocal,
            onDeleteVlog = onDeleteVlog
        )

        // --- Reply Preview Overlay ---
        ReplyPreviewOverlay(
            activeReplyPreviewPath = activeReplyPreviewPath,
            palName = "vlog",
            accentColor = accentColor,
            onActiveReplyPreviewPathChange = onActiveReplyPreviewPathChange,
            onSendReply = params.onSendReply
        )

        // --- Reaction Preview Overlay ---
        ReactionPreviewOverlay(
            activeReactionPreview = activeReactionPreview,
            palName = "vlog",
            onActiveReactionPreviewChange = onActiveReactionPreviewChange
        )
        
        // --- Export Dialog Overlay ---
        if (showExportDialog) {
            var isExportSavingVideo by remember { mutableStateOf(false) }
            var isExportSharingVideo by remember { mutableStateOf(false) }
            var isExportSaved by remember { mutableStateOf(false) }
            val localCoroutineScope = rememberCoroutineScope()
            
            var showEditExportSheet by remember { mutableStateOf(false) }
            var exportBackground by remember { mutableStateOf("black") }
            var exportMissedText by remember { mutableStateOf("💤") }
            var showMissedTextDialog by remember { mutableStateOf(false) }
            var tempMissedText by remember { mutableStateOf("💤") }
            val missedTextFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            val exportKeyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
            



            androidx.activity.compose.BackHandler {
                onShowExportDialogChange(false)
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black else Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShowExportDialogChange(false) },
                contentAlignment = Alignment.Center
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val scaleHeight = maxHeight.value / 620f
                val scaleWidth = (screenWidth.value * 0.85f) / 306f
                val scale = scaleHeight.coerceAtMost(scaleWidth).coerceAtMost(1.1f)

                // Precise positioning and dimension logic matching the main camera frame exactly
                val shutterBottomMargin = 67.5.dp
                val shutterSize = 59.dp * scale
                val cardBottomPadding = shutterBottomMargin + (shutterSize / 2f)
                val isVlog = pal.isVlog
                val exportShift = if (isVlog) 25.dp else 30.dp
                val cameraFrameBottomPadding = cardBottomPadding - 2.5.dp + exportShift - 10.dp

                val cameraWidth = screenWidth - 15.dp
                val cameraHeight = cameraWidth * (16f / 9f)

                val hasVlogs = capturedVlogsPaths.isNotEmpty()

                // Camera Viewfinder Box (9:16 rounded card) aligned exactly as in the main camera menu
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = cameraFrameBottomPadding)
                        .width(cameraWidth)
                        .height(cameraHeight)
                ) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxSize(),
                        borderRadius = 28.dp * scale,
                        isDark = isDark,
                        gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else listOf(Color(0xFFEBEBEB), Color(0xFFEBEBEB)),
                        borderColor = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp * scale))
                        ) {
                            if (pal.isVlog) {
                                if (capturedVlogsPaths.isEmpty()) {
                                    Text(
                                        text = "Nothing to export for this collection.",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = (14 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isDark) Color.White else Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(horizontal = 24.dp * scale)
                                    )
                                } else {
                                    val exportLocalPlayer = remember(capturedVlogsPaths) {
                                        androidx.media3.exoplayer.ExoPlayer.Builder(context)
                                            .setRenderersFactory(
                                                androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                                                    setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                                                    setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                                }
                                            )
                                            .build().apply {
                                                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                                                volume = 0f
                                            }
                                    }

                                    DisposableEffect(exportLocalPlayer) {
                                        onDispose {
                                            exportLocalPlayer.release()
                                        }
                                    }

                                    var exportActiveIndex by remember { mutableStateOf(0) }
                                    
                                    LaunchedEffect(capturedVlogsPaths, exportLocalPlayer) {
                                        exportLocalPlayer.stop()
                                        exportLocalPlayer.clearMediaItems()
                                        val reversedPaths = capturedVlogsPaths.reversed()
                                        val resolved = reversedPaths.map { path ->
                                            ensureVideoCached(context, path)
                                        }
                                        resolved.forEach { resolvedPath ->
                                            if (resolvedPath.startsWith("http")) {
                                                exportLocalPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(resolvedPath)))
                                            } else {
                                                val cleanPath = when {
                                                    resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                                                    else -> resolvedPath
                                                }
                                                val fileTarget = java.io.File(cleanPath)
                                                if (fileTarget.exists() && fileTarget.length() > 0) {
                                                    val targetUri = android.net.Uri.fromFile(fileTarget)
                                                    exportLocalPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                                                }
                                            }
                                        }
                                        if (resolved.isNotEmpty()) {
                                            exportLocalPlayer.prepare()
                                        }
                                        exportLocalPlayer.playWhenReady = true
                                        exportLocalPlayer.play()
                                    }

                                    LaunchedEffect(exportLocalPlayer) {
                                        exportLocalPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                                                super.onMediaItemTransition(mediaItem, reason)
                                                exportActiveIndex = exportLocalPlayer.currentMediaItemIndex
                                            }
                                            override fun onPlaybackStateChanged(playbackState: Int) {
                                                super.onPlaybackStateChanged(playbackState)
                                                if (playbackState == androidx.media3.common.Player.STATE_READY) {
                                                    exportLocalPlayer.play()
                                                }
                                            }
                                        })
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                            .align(Alignment.Center)
                                            .background(Color.Black)
                                    ) {
                                        androidx.compose.ui.viewinterop.AndroidView(
                                            factory = { ctx ->
                                                val view = android.view.LayoutInflater.from(ctx)
                                                    .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                                                view.layoutParams = android.view.ViewGroup.LayoutParams(
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                                view.apply {
                                                    val localPlayer = exportLocalPlayer
                                                    player = localPlayer
                                                    useController = false
                                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                                                    setBackgroundColor(android.graphics.Color.BLACK)
                                                    
                                                    fun applyVideoScale() {
                                                        val videoSize = localPlayer.videoSize
                                                        val videoWidth = videoSize.width
                                                        val videoHeight = videoSize.height
                                                        val textureView = getVideoSurfaceView() as? android.view.TextureView
                                                        if (textureView == null) {
                                                            postDelayed({ applyVideoScale() }, 100)
                                                            return
                                                        }
                                                        val containerWidth = width.toFloat()
                                                        val containerHeight = height.toFloat()
                                                        if (containerWidth > 0f && containerHeight > 0f && videoWidth > 0 && videoHeight > 0) {
                                                            val isPortrait = videoHeight > videoWidth
                                                            val rotatedWidth = if (isPortrait) videoHeight.toFloat() else videoWidth.toFloat()
                                                            val rotatedHeight = if (isPortrait) videoWidth.toFloat() else videoHeight.toFloat()
                                                            
                                                            val scale = java.lang.Math.max(containerWidth / rotatedWidth, containerHeight / rotatedHeight)
                                                            
                                                            val calculatedScaleX: Float
                                                            val calculatedScaleY: Float
                                                            val calculatedRotation: Float
                                                            if (isPortrait) {
                                                                calculatedScaleX = (rotatedHeight * scale) / containerWidth
                                                                calculatedScaleY = (rotatedWidth * scale) / containerHeight
                                                                calculatedRotation = 270f
                                                            } else {
                                                                calculatedScaleX = (rotatedWidth * scale) / containerWidth
                                                                calculatedScaleY = (rotatedHeight * scale) / containerHeight
                                                                calculatedRotation = 0f
                                                            }
                                                            
                                                            android.util.Log.d("PalVideoScale", "Reverted scale for exportLocalPlayer: video=${videoWidth}x${videoHeight}, container=${containerWidth}x${containerHeight}, scaleX=${calculatedScaleX}, scaleY=${calculatedScaleY}, rotation=${calculatedRotation}")
                                                            
                                                            textureView.pivotX = containerWidth / 2f
                                                            textureView.pivotY = containerHeight / 2f
                                                            textureView.scaleX = calculatedScaleX
                                                            textureView.scaleY = calculatedScaleY
                                                            textureView.rotation = calculatedRotation
                                                        } else {
                                                            postDelayed({ applyVideoScale() }, 100)
                                                        }
                                                    }

                                                    val layoutListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                                                        applyVideoScale()
                                                    }
                                                    addOnLayoutChangeListener(layoutListener)

                                                    exportLocalPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                                        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                                            super.onVideoSizeChanged(videoSize)
                                                            applyVideoScale()
                                                        }
                                                        override fun onPlaybackStateChanged(playbackState: Int) {
                                                            super.onPlaybackStateChanged(playbackState)
                                                            applyVideoScale()
                                                        }
                                                    })

                                                    applyVideoScale()
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            update = { view ->
                                                if (view.player != exportLocalPlayer) {
                                                    view.player = exportLocalPlayer
                                                }
                                            }
                                        )
                                        
                                        val originalIndex = (capturedVlogsPaths.lastIndex - exportActiveIndex).coerceIn(0, capturedVlogsPaths.lastIndex.coerceAtLeast(0))
                                        val currentCaption = capturedVlogsCaptions.getOrNull(originalIndex) ?: ""
                                        val currentTime = capturedVlogsTimes.getOrNull(originalIndex) ?: ""

                                        // Left center: "vlog" text in BricolageVariableFontFamily
                                        Text(
                                            text = "vlog",
                                            fontFamily = BricolageVariableFontFamily,
                                            fontSize = (22 * scale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 5.5.dp * scale),
                                            style = TextStyle(
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset = androidx.compose.ui.geometry.Offset(1f * scale, 1f * scale),
                                                    blurRadius = 3f * scale
                                                )
                                            )
                                        )

                                        // Right center: capture time text in RobotoFontFamily
                                        Text(
                                            text = currentTime,
                                            fontFamily = RobotoFontFamily,
                                            fontSize = (17 * scale).sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 5.5.dp * scale),
                                            style = TextStyle(
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset = androidx.compose.ui.geometry.Offset(1f * scale, 1f * scale),
                                                    blurRadius = 3f * scale
                                                )
                                            )
                                        )

                                        // Center: custom caption text (if present) in FontFamily.SansSerif
                                        if (currentCaption.isNotEmpty()) {
                                            Text(
                                                text = currentCaption,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = (22 * scale).sp,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .padding(horizontal = 48.dp * scale),
                                                textAlign = TextAlign.Center,
                                                style = TextStyle(
                                                    shadow = androidx.compose.ui.graphics.Shadow(
                                                        color = Color.Black.copy(alpha = 0.5f),
                                                        offset = androidx.compose.ui.geometry.Offset(1f * scale, 1f * scale),
                                                        blurRadius = 3f * scale
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Group Export Preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA))
                                ) {
                                    val totalSlots = maxOf(groupMembers.size, pal.size.toIntOrNull() ?: 4)
                                    val isGrid = totalSlots > 5
                                    val columns = if (isGrid) 2 else 1
                                    val contentSpacingDp = 0.dp
                                    val verticalPadding = 28.dp * scale
                                    
                                    val rows = (totalSlots + columns - 1) / columns
                                    val availableHeight = cameraHeight - (verticalPadding * 2)
                                    
                                    val cardHeightDp = if (isGrid) {
                                        val cardWidthDpGrid = cameraWidth / 2
                                        (availableHeight / rows).coerceAtMost(cardWidthDpGrid)
                                    } else {
                                        val cardWidthDp = cameraWidth
                                        val maxCardHeight = cardWidthDp * (9f / 16f)
                                        (availableHeight / totalSlots).coerceAtMost(maxCardHeight)
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = verticalPadding, bottom = verticalPadding)
                                            .padding(horizontal = 0.dp),
                                        verticalArrangement = Arrangement.spacedBy(contentSpacingDp, Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val chunked = (0 until totalSlots).chunked(columns)
                                        for (rowIndices in chunked) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(contentSpacingDp)
                                            ) {
                                                for (index in rowIndices) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(cardHeightDp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        GroupExportMemberSlot(
                                                            index = index,
                                                            groupMembers = groupMembers,
                                                            userFirstName = userFirstName,
                                                            filteredSubmissions = filteredSubmissions,
                                                            currentUserId = currentUserId,
                                                            exportBackground = exportBackground,
                                                            exportMissedText = exportMissedText,
                                                            context = context,
                                                            textColor = if (exportBackground == "white") Color.Black else Color.White,
                                                            pal = pal,
                                                            cardHeightDp = cardHeightDp,
                                                            isGrid = isGrid
                                                        )
                                                    }
                                                }
                                                // If the last row is not full, fill it with empty space weights so items don't stretch
                                                val emptySlotsCount = columns - rowIndices.size
                                                if (emptySlotsCount > 0) {
                                                    for (s in 0 until emptySlotsCount) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Space below the viewfinder containing the 4 buttons exactly 2.5.dp below the frame
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(cameraFrameBottomPadding)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.5.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExportMenuButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = "discard",
                            isPrimary = false,
                            isDark = isDark,
                            onClick = { onShowExportDialogChange(false) }
                        )

                        ExportMenuButton(
                            icon = {
                                EditIcon(
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = "edit",
                            isPrimary = false,
                            isDark = isDark,
                            onClick = {
                                if (!pal.isVlog) {
                                    showEditExportSheet = true
                                }
                            }
                        )

                        ExportMenuButton(
                            icon = {
                                if (isExportSavingVideo) {
                                    CircularProgressIndicator(
                                        color = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    AnimatedSaveIcon(
                                        isSaved = isExportSaved,
                                        tint = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = if (isExportSaved) "saved" else "save",
                            isPrimary = false,
                            isDark = isDark,
                            onClick = {
                                val hasItemsToExport = if (pal.isVlog) capturedVlogsPaths.isNotEmpty() else filteredPaths.isNotEmpty()
                                if (!isExportSavingVideo && !isExportSharingVideo && !isExportSaved && hasItemsToExport) {
                                    isExportSavingVideo = true
                                    localCoroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        val tempOut = java.io.File(context.cacheDir, "temp_export_save_${System.currentTimeMillis()}.mp4")
                                        val pathsToProcess = if (pal.isVlog) capturedVlogsPaths.reversed() else filteredPaths
                                        val resolvedPaths = pathsToProcess.map { path ->
                                            ensureVideoCached(context, path)
                                        }
                                        val timesToProcess = if (pal.isVlog) capturedVlogsTimes.reversed() else filteredTimes
                                        val captionsToProcess = if (pal.isVlog) capturedVlogsCaptions.reversed() else filteredCaptions
                                        val vlogsToProcess = if (pal.isVlog) List(resolvedPaths.size) { "vlog" } else {
                                            filteredSubmissions.map { sub ->
                                                parseUserDisplayName(sub.userDisplayName).first
                                            }
                                        }

                                        VideoProcessor.processVideoList(
                                            context = context,
                                            inputPaths = resolvedPaths,
                                            outputPath = tempOut.absolutePath,
                                            vlogTexts = vlogsToProcess,
                                            timeTexts = timesToProcess,
                                            captionTexts = captionsToProcess,
                                            roundedCorners = false,
                                            exportBackground = exportBackground
                                        ) { success ->
                                            if (success) {
                                                val saveSuccess = saveVideoToGallery(context, tempOut.absolutePath)
                                                if (saveSuccess) {
                                                    isExportSaved = true
                                                    localCoroutineScope.launch {
                                                        kotlinx.coroutines.delay(3000)
                                                        isExportSaved = false
                                                    }
                                                }
                                            }
                                            try { tempOut.delete() } catch(e: Exception) {}
                                            isExportSavingVideo = false
                                        }
                                    }
                                }
                            }
                        )

                        ExportMenuButton(
                            icon = {
                                if (isExportSharingVideo) {
                                    CircularProgressIndicator(
                                        color = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    UpwardArrowIcon(
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = "share",
                            isPrimary = true,
                            isDark = isDark,
                            onClick = {
                                val hasItemsToExport = if (pal.isVlog) capturedVlogsPaths.isNotEmpty() else filteredPaths.isNotEmpty()
                                if (!isExportSavingVideo && !isExportSharingVideo && hasItemsToExport) {
                                    isExportSharingVideo = true
                                    localCoroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        val tempOut = java.io.File(context.cacheDir, "temp_export_share_${System.currentTimeMillis()}.mp4")
                                        val pathsToProcess = if (pal.isVlog) capturedVlogsPaths.reversed() else filteredPaths
                                        val resolvedPaths = pathsToProcess.map { path ->
                                            ensureVideoCached(context, path)
                                        }
                                        val timesToProcess = if (pal.isVlog) capturedVlogsTimes.reversed() else filteredTimes
                                        val captionsToProcess = if (pal.isVlog) capturedVlogsCaptions.reversed() else filteredCaptions
                                        val vlogsToProcess = if (pal.isVlog) List(resolvedPaths.size) { "vlog" } else {
                                            filteredSubmissions.map { sub ->
                                                parseUserDisplayName(sub.userDisplayName).first
                                            }
                                        }

                                        VideoProcessor.processVideoList(
                                            context = context,
                                            inputPaths = resolvedPaths,
                                            outputPath = tempOut.absolutePath,
                                            vlogTexts = vlogsToProcess,
                                            timeTexts = timesToProcess,
                                            captionTexts = captionsToProcess,
                                            roundedCorners = false,
                                            exportBackground = exportBackground
                                        ) { success ->
                                            if (success) {
                                                try {
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        "com.finrein.pals.fileprovider",
                                                        tempOut
                                                    )
                                                    val sendIntent = android.content.Intent().apply {
                                                        action = android.content.Intent.ACTION_SEND
                                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                        type = "video/mp4"
                                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share slideshow video")
                                                    context.startActivity(shareIntent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            isExportSharingVideo = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // OVERLAY DIALOGS (edit export sheet)
                if (showEditExportSheet) {
                    val dialogBg = if (isDark) Color(0xFF2C2B30) else Color(0xFFF5F3EB)
                    val dialogTextColor = if (isDark) Color.White else Color.Black
                    val buttonColor = if (isDark) Color(0xFFB39DDB) else Color(0xFF7C4DFF)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showEditExportSheet = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(dialogBg)
                                .clickable(enabled = false) {}
                                .padding(24.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "edit export",
                                    fontFamily = BricolageVariableFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dialogTextColor
                                )
                                


                                // Background Option: background · black / white
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            exportBackground = if (exportBackground == "black") "white" else "black"
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = androidx.compose.ui.text.buildAnnotatedString {
                                            append("background · ")
                                            append(exportBackground)
                                        },
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 16.sp,
                                        color = dialogTextColor
                                    )
                                }

                                // Missed Text Option: missed text · zzz
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            tempMissedText = exportMissedText
                                            showMissedTextDialog = true
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = androidx.compose.ui.text.buildAnnotatedString {
                                            append("missed text · ")
                                            pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF0F8CFF)))
                                            append(exportMissedText)
                                            pop()
                                        },
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 16.sp,
                                        color = dialogTextColor
                                    )
                                }

                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = buttonColor,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .clickable { showEditExportSheet = false }
                                )
                            }
                        }
                    }
                }

                // Missed Text Input Dialog Overlay
                if (showMissedTextDialog) {
                    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(150)
                        try {
                            missedTextFocusRequester.requestFocus()
                            keyboardController?.show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val dialogBg = if (isDark) Color(0xFF2C2B30) else Color(0xFFF5F3EB)
                    val dialogTextColor = if (isDark) Color.White else Color.Black
                    val buttonColor = if (isDark) Color(0xFFB39DDB) else Color(0xFF7C4DFF)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showMissedTextDialog = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(dialogBg)
                                .clickable(enabled = false) {}
                                .padding(24.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "missed text",
                                    fontFamily = BricolageVariableFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dialogTextColor
                                )

                                var textVal by remember {
                                    mutableStateOf(
                                        androidx.compose.ui.text.input.TextFieldValue(
                                            text = tempMissedText,
                                            selection = androidx.compose.ui.text.TextRange(tempMissedText.length)
                                        )
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            missedTextFocusRequester.requestFocus()
                                            keyboardController?.show()
                                        }
                                ) {
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = textVal,
                                        onValueChange = {
                                            textVal = it
                                            exportMissedText = it.text
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontFamily = RobotoFontFamily,
                                            fontSize = 18.sp,
                                            color = Color(0xFF0F8CFF),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(missedTextFocusRequester)
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    keyboardController?.show()
                                                }
                                            },
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(buttonColor)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(dialogTextColor.copy(alpha = 0.3f))
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "cancel",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = buttonColor,
                                        modifier = Modifier
                                            .clickable {
                                                exportMissedText = tempMissedText
                                                showMissedTextDialog = false
                                            }
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "OK",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = buttonColor,
                                        modifier = Modifier
                                            .clickable {
                                                exportMissedText = textVal.text
                                                showMissedTextDialog = false
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NameInputScreen(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    isDark: Boolean,
    textColor: Color,
    mutedTextColor: Color
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 40.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_logo_small),
                contentDescription = "Pal Small Yellow Cloud Logo",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "need help?",
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                color = textColor,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.clickable { /* No-op */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "→ welcome to pal",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "to get started we need your\nfirst and last name...",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // First Name Field
        androidx.compose.foundation.text.BasicTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = textColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            decorationBox = { innerTextField ->
                Column {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        if (firstName.isEmpty()) {
                            Text(
                                text = "First Name",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                color = mutedTextColor.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(textColor.copy(alpha = 0.3f))
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Last Name Field
        androidx.compose.foundation.text.BasicTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = textColor
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            decorationBox = { innerTextField ->
                Column {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        if (lastName.isEmpty()) {
                            Text(
                                text = "Last Name",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                color = mutedTextColor.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(textColor.copy(alpha = 0.3f))
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(36.dp))

        val isFormValid = firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()
        Text(
            text = "next →",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = if (isFormValid) textColor else textColor.copy(alpha = 0.4f),
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
            modifier = Modifier.clickable(enabled = isFormValid) { onNext() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "cancel",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
            modifier = Modifier.clickable { onCancel() }
        )
    }
}

@Composable
fun NameConfirmScreen(
    firstName: String,
    lastName: String,
    onContinue: () -> Unit,
    textColor: Color,
    mutedTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 40.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_logo_small),
                contentDescription = "Pal Small Yellow Cloud Logo",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "need help?",
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                color = textColor,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.clickable { /* No-op */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "→ welcome to pal",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "to get started we need your\nfirst and last name...",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Star, Name, Smiley
        Text(
            text = "☆ $firstName $lastName :)",
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "continue →",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
            modifier = Modifier.clickable { onContinue() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Terms of service footnote (just below the continue button)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "by continuing you agree to",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = mutedTextColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "the ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = mutedTextColor
                )
                Text(
                    text = "terms of service.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = mutedTextColor,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
fun CreatingAccountScreen(
    firstName: String,
    lastName: String,
    textColor: Color,
    mutedTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 40.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_logo_small),
                contentDescription = "Pal Small Yellow Cloud Logo",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "need help?",
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                color = textColor,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (firstName.isEmpty()) "→ restoring account" else "→ welcome to pal",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (firstName.isEmpty()) "please wait while we configure\nyour workspace..." else "to get started we need your\nfirst and last name...",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
            // Star, Name, Smiley
            Text(
                text = "☆ $firstName $lastName :)",
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(36.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (firstName.isEmpty()) "restoring account details" else "creating account",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = mutedTextColor.copy(alpha = 0.5f),
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = mutedTextColor.copy(alpha = 0.5f),
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terms of service footnote (just below creating account loader)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "by continuing you agree to",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = mutedTextColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "the ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = mutedTextColor
                )
                Text(
                    text = "terms of service.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = mutedTextColor,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}



@Composable
fun PermissionsScreen(
    onDone: () -> Unit,
    textColor: Color,
    mutedTextColor: Color
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val checkmarkColor = if (isSystemDark) Color(0xFFB57F65) else Color(0xFF879768)

    var isCameraGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var isMicrophoneGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val storagePermissionString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    var isStorageGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, storagePermissionString
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionSubStep by remember {
        mutableStateOf(
            if (!isCameraGranted) 1
            else if (!isMicrophoneGranted) 2
            else if (!isStorageGranted) 3
            else 4
        )
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isStorageGranted = true
            permissionSubStep = 4
            onDone()
        }
    }

    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isMicrophoneGranted = true
            permissionSubStep = 3
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isCameraGranted = true
            permissionSubStep = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 40.dp)
    ) {
        // Top row (logo only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_logo_small),
                contentDescription = "Pal Small Yellow Cloud Logo",
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "→ permissions",
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "first we need a few\npermissions...",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mid section with steps
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Camera Permission Status/Details
            if (isCameraGranted) {
                Text(
                    text = "✓ camera",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = checkmarkColor,
                    fontWeight = FontWeight.Medium
                )
            } else if (permissionSubStep == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "camera",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "capture photos and videos to\nshare with close friends.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "photos and videos are end-to-end\nencrypted, only you and friends\nyou share with can see them.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                }
            }

            // Microphone Permission Status/Details
            if (isMicrophoneGranted) {
                Text(
                    text = "✓ microphone",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = checkmarkColor,
                    fontWeight = FontWeight.Medium
                )
            } else if (permissionSubStep == 2) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "microphone",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "record audio while capturing\nvideos.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                }
            }

            // Storage Permission Status/Details
            if (isStorageGranted) {
                Text(
                    text = "✓ storage",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = checkmarkColor,
                    fontWeight = FontWeight.Medium
                )
            } else if (permissionSubStep == 3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "storage",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "we require this permission to store your memories and vlogs natively to your device",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Trigger
            if (isCameraGranted && isMicrophoneGranted && isStorageGranted) {
                Text(
                    text = "done →",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = textColor,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier.clickable { onDone() }
                )
            } else {
                Text(
                    text = "continue →",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = textColor,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        when (permissionSubStep) {
                            1 -> cameraLauncher.launch(android.Manifest.permission.CAMERA)
                            2 -> microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            3 -> storageLauncher.launch(storagePermissionString)
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun VideoThumbnail(videoPath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(videoPath) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(videoPath) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                val resolvedPath = ensureVideoCached(context, videoPath)
                val cleanPath = when {
                    resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                    else -> resolvedPath
                }
                
                when {
                    cleanPath.startsWith("content://") -> {
                        val pfd = context.contentResolver.openFileDescriptor(android.net.Uri.parse(cleanPath), "r")
                        if (pfd != null) {
                            retriever.setDataSource(pfd.fileDescriptor)
                            pfd.close()
                        } else {
                            retriever.setDataSource(cleanPath, java.util.HashMap<String, String>())
                        }
                    }
                    cleanPath.startsWith("http://") || cleanPath.startsWith("https://") -> {
                        retriever.setDataSource(cleanPath, java.util.HashMap<String, String>())
                    }
                    else -> {
                        val file = java.io.File(cleanPath)
                        if (file.exists()) {
                            val fis = java.io.FileInputStream(file)
                            retriever.setDataSource(fis.fd)
                            fis.close()
                        } else {
                            if (videoPath.startsWith("content://")) {
                                val pfd = context.contentResolver.openFileDescriptor(android.net.Uri.parse(videoPath), "r")
                                if (pfd != null) {
                                    retriever.setDataSource(pfd.fileDescriptor)
                                    pfd.close()
                                }
                            } else if (videoPath.startsWith("http")) {
                                retriever.setDataSource(videoPath, java.util.HashMap<String, String>())
                            } else {
                                retriever.setDataSource(cleanPath)
                            }
                        }
                    }
                }
                
                val bmp = retriever.getFrameAtTime(0)
                if (bmp != null) {
                    val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    val rotation = rotationStr?.toIntOrNull() ?: 0
                    
                    val needsManualRotation = if (rotation == 90 || rotation == 270) {
                        bmp.width > bmp.height
                    } else {
                        false
                    }
                    
                    val baseBmp = if (needsManualRotation) {
                        val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                        android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                    } else {
                        bmp
                    }

                    // Rotate the bitmap by 90 degrees anticlockwise (-90f) so it fits the landscape aspect ratio box perfectly end-to-end
                    val finalMatrix = android.graphics.Matrix().apply { postRotate(-90f) }
                    val finalBmp = android.graphics.Bitmap.createBitmap(baseBmp, 0, 0, baseBmp.width, baseBmp.height, finalMatrix, true)
                    bitmap = finalBmp
                }
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun VideoPlayerItem(
    videoPath: String,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp)
) {
    VideoPlayerItem(
        videoPaths = listOf(videoPath),
        modifier = modifier,
        shape = shape
    )
}

@Composable
fun VideoPlayerItem(
    videoPaths: List<String>,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp)
) {
    val context = LocalContext.current
    val palPrefs = remember(context) { context.getSharedPreferences("pal_prefs", android.content.Context.MODE_PRIVATE) }
    val vlogPrefs = remember(context) { context.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE) }

    var resolvedPaths by remember(videoPaths) { mutableStateOf<List<String>>(emptyList()) }
    var isVideoReady by remember(resolvedPaths) { mutableStateOf(false) }

    LaunchedEffect(videoPaths) {
        val list = mutableListOf<String>()
        videoPaths.forEach { videoPath ->
            list.add(ensureVideoCached(context, videoPath))
        }
        resolvedPaths = list
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    val localPlayer = remember(resolvedPaths) {
        if (resolvedPaths.isEmpty()) null else {
            androidx.media3.exoplayer.ExoPlayer.Builder(context)
                .setRenderersFactory(
                    androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                        setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                        setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                    }
                )
                .setMediaSourceFactory(
                    androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                        .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context))
                )
                .build().apply {
                    repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                    volume = 0f
                
                addListener(object : androidx.media3.common.Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == androidx.media3.common.Player.STATE_READY) {
                            isVideoReady = true
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            isVideoReady = true
                        }
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.e("VideoPlayerItem", "ExoPlayer Error playing: ${error.message}", error)
                        isVideoReady = true
                    }
                })

                resolvedPaths.forEach { resolvedPath ->
                    val uri = android.net.Uri.parse(resolvedPath)
                    if (resolvedPath.startsWith("http") || resolvedPath.startsWith("content://")) {
                        addMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                    } else {
                        val cleanPath = when {
                            resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                            else -> resolvedPath
                        }
                        val file = java.io.File(cleanPath)
                        if (file.exists()) {
                            addMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                        } else {
                            try {
                                addMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                prepare()
                if (playbackState == androidx.media3.common.Player.STATE_READY || isPlaying) {
                    isVideoReady = true
                }
            }
        }
    }

    DisposableEffect(localPlayer) {
        onDispose {
            localPlayer?.release()
        }
    }

    LaunchedEffect(localPlayer) {
        localPlayer?.playWhenReady = true
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (localPlayer != null) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                    view.apply {
                        player = localPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                        fun applyVideoScale() {
                            val videoSize = localPlayer.videoSize
                            val videoWidth = videoSize.width
                            val videoHeight = videoSize.height
                            val textureView = getVideoSurfaceView() as? android.view.TextureView
                            if (textureView == null) {
                                postDelayed({ applyVideoScale() }, 100)
                                return
                            }
                            val containerWidth = width.toFloat()
                            val containerHeight = height.toFloat()
                            if (containerWidth > 0f && containerHeight > 0f && videoWidth > 0 && videoHeight > 0) {
                                val isPortrait = videoHeight > videoWidth
                                val rotatedWidth = if (isPortrait) videoHeight.toFloat() else videoWidth.toFloat()
                                val rotatedHeight = if (isPortrait) videoWidth.toFloat() else videoHeight.toFloat()
                                
                                val scale = java.lang.Math.max(containerWidth / rotatedWidth, containerHeight / rotatedHeight)
                                
                                val calculatedScaleX: Float
                                val calculatedScaleY: Float
                                val calculatedRotation: Float
                                if (isPortrait) {
                                    calculatedScaleX = (rotatedHeight * scale) / containerWidth
                                    calculatedScaleY = (rotatedWidth * scale) / containerHeight
                                    calculatedRotation = 270f
                                } else {
                                    calculatedScaleX = (rotatedWidth * scale) / containerWidth
                                    calculatedScaleY = (rotatedHeight * scale) / containerHeight
                                    calculatedRotation = 0f
                                }
                                
                                android.util.Log.d("PalVideoScale", "Reverted scale for localPlayer: video=${videoWidth}x${videoHeight}, container=${containerWidth}x${containerHeight}, scaleX=${calculatedScaleX}, scaleY=${calculatedScaleY}, rotation=${calculatedRotation}")
                                
                                textureView.pivotX = containerWidth / 2f
                                textureView.pivotY = containerHeight / 2f
                                textureView.scaleX = calculatedScaleX
                                textureView.scaleY = calculatedScaleY
                                textureView.rotation = calculatedRotation
                            } else {
                                postDelayed({ applyVideoScale() }, 100)
                            }
                        }

                        val layoutListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                            applyVideoScale()
                        }
                        addOnLayoutChangeListener(layoutListener)

                        localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                super.onVideoSizeChanged(videoSize)
                                applyVideoScale()
                            }
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                super.onPlaybackStateChanged(playbackState)
                                applyVideoScale()
                            }
                        })

                        applyVideoScale()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isVideoReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun VoiceNotePlayerItem(
    audioUrl: String,
    title: String,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(1) }
    var currentPosition by remember { mutableStateOf(0) }

    val mediaPlayer = remember(audioUrl) {
        android.media.MediaPlayer().apply {
            try {
                setDataSource(audioUrl)
                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(mediaPlayer) {
        onDispose {
            try {
                mediaPlayer.stop()
            } catch (e: Exception) {}
            mediaPlayer.release()
        }
    }

    LaunchedEffect(mediaPlayer) {
        mediaPlayer.setOnPreparedListener { mp ->
            duration = mp.duration.coerceAtLeast(1)
        }
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            progress = 0f
            currentPosition = 0
            mediaPlayer.seekTo(0)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                currentPosition = mediaPlayer.currentPosition
                progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                kotlinx.coroutines.delay(50)
            }
        }
    }

    val amplitudes = remember {
        listOf(
            0.3f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.4f, 0.7f, 0.9f, 1.0f,
            0.8f, 0.5f, 0.4f, 0.6f, 0.8f, 0.7f, 0.5f, 0.3f, 0.4f, 0.6f,
            0.8f, 0.9f, 0.7f, 0.4f, 0.3f
        )
    }

    Row(
        modifier = modifier
            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable {
                    try {
                        if (isPlaying) {
                            mediaPlayer.pause()
                            isPlaying = false
                        } else {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPlaying) "⏸" else "▶",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = if (isPlaying) 0.dp else 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = if (isDark) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Wave bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                amplitudes.forEachIndexed { index, amp ->
                    val barProgress = index.toFloat() / amplitudes.size.toFloat()
                    val barColor = if (progress >= barProgress) accentColor else (if (isDark) Color(0xFF444444) else Color(0xFFCCCCCC))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((amp * 20f).dp)
                            .background(barColor, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteVlogConfirmationDialog(
    showDeleteVlogConfirmation: Boolean,
    isDark: Boolean,
    capturedVlogsPaths: List<String>,
    selectedPageIndex: Int,
    onSelectedPageIndexChange: (Int) -> Unit,
    onShowDeleteVlogConfirmationChange: (Boolean) -> Unit,
    onDeleteVlog: (Int) -> Unit
) {
    if (!showDeleteVlogConfirmation) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onShowDeleteVlogConfirmationChange(false) },
        contentAlignment = Alignment.Center
    ) {
        val dialogBg = if (isDark) Color(0xFF2B2930) else Color(0xFFF5F3EB)
        val titleColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
        val buttonColor = Color(0xFF6750A4)
        val deleteButtonColor = Color(0xFFBA1A1A)

        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(dialogBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Do you wish to delete this pal ? This action can't be undone",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = titleColor,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor,
                        modifier = Modifier
                            .clickable { onShowDeleteVlogConfirmationChange(false) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "delete",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = deleteButtonColor,
                        modifier = Modifier
                            .clickable {
                                val indexToDelete = selectedPageIndex
                                onShowDeleteVlogConfirmationChange(false)
                                val nextIndex = if (capturedVlogsPaths.size <= 1) 0 else selectedPageIndex.coerceAtMost(capturedVlogsPaths.size - 2)
                                onSelectedPageIndexChange(nextIndex)
                                onDeleteVlog(indexToDelete)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyPreviewOverlay(
    activeReplyPreviewPath: String?,
    palName: String,
    accentColor: Color,
    onActiveReplyPreviewPathChange: (String?) -> Unit,
    onSendReply: (String, String) -> Unit = { _, _ -> }
) {
    if (activeReplyPreviewPath == null) return
    val videoPath = activeReplyPreviewPath
    androidx.activity.compose.BackHandler {
        onActiveReplyPreviewPathChange(null)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        var replyInput by remember { mutableStateOf("") }
        val context = LocalContext.current

        // 1. Top Header
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp)
                .height(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onActiveReplyPreviewPathChange(null) },
                contentAlignment = Alignment.Center
            ) {
                ChevronLeftIcon(
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = palName,
                fontFamily = BricolageVariableFontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 2. Centered Video with White Border (few dp's above center)
        Column(
            modifier = Modifier
                .offset(y = (-40).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(156.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            ) {
                VideoPlayerItem(
                    videoPath = videoPath,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. Message Input Bar at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = replyInput,
                    onValueChange = { replyInput = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White
                    ),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (replyInput.isEmpty()) {
                            Text(
                                text = "message",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            val isReplyValid = replyInput.trim().isNotEmpty()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isReplyValid) accentColor else Color.White.copy(alpha = 0.1f))
                    .clickable(enabled = isReplyValid) {
                        onSendReply(videoPath, replyInput.trim())
                        onActiveReplyPreviewPathChange(null)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (isReplyValid) Color.White else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ReactionPreviewOverlay(
    activeReactionPreview: Pair<String, String>?,
    palName: String,
    onActiveReactionPreviewChange: (Pair<String, String>?) -> Unit
) {
    if (activeReactionPreview == null) return
    val (videoPath, emoji) = activeReactionPreview
    androidx.activity.compose.BackHandler {
        onActiveReactionPreviewChange(null)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Top Header
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp)
                .height(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onActiveReactionPreviewChange(null) },
                contentAlignment = Alignment.Center
            ) {
                ChevronLeftIcon(
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = palName,
                fontFamily = BricolageVariableFontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 2. Centered Video with White Border and Reaction Emoji underneath
        Column(
            modifier = Modifier
                .offset(y = (-40).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(156.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.2.dp, Color.White, RoundedCornerShape(16.dp))
            ) {
                VideoPlayerItem(
                    videoPath = videoPath,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = emoji,
                fontSize = 64.sp
            )
        }
    }
}

@Composable
fun FeedReactionsAndReplies(
    feedReactions: List<Triple<String, String?, String>>,
    feedReplies: List<Triple<String, String?, String>>,
    textColor: Color,
    isDark: Boolean,
    accentColor: Color
) {
    if (feedReactions.isEmpty() && feedReplies.isEmpty()) return
    Column(
        modifier = Modifier
            .width(210.dp)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (feedReactions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                feedReactions.forEach { (senderName, senderAvatar, emoji) ->
                    Box(
                        modifier = Modifier
                            .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (!senderAvatar.isNullOrEmpty()) {
                                UriImage(
                                    uriString = senderAvatar,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Text(text = emoji, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (feedReplies.isNotEmpty()) {
            feedReplies.forEach { (senderName, senderAvatar, replyText) ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (!senderAvatar.isNullOrEmpty()) {
                        UriImage(
                            uriString = senderAvatar,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.smile_medium),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(180f)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = senderName,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        if (replyText.startsWith("SOUND|||")) {
                            val sParts = replyText.split("|||")
                            val sName = sParts.getOrNull(1) ?: ""
                            val sUrl = sParts.getOrNull(2) ?: ""
                            
                            var isPlaying by remember { mutableStateOf(false) }
                            var player by remember { mutableStateOf<MediaPlayer?>(null) }
                            
                            DisposableEffect(sUrl) {
                                onDispose {
                                    player?.stop()
                                    player?.release()
                                }
                            }
                            
                            val togglePlay = {
                                try {
                                    val p = player
                                    if (p == null) {
                                        val newPlayer = MediaPlayer().apply {
                                            setDataSource(sUrl)
                                            setOnPreparedListener {
                                                it.start()
                                                isPlaying = true
                                            }
                                            setOnCompletionListener {
                                                isPlaying = false
                                            }
                                            setOnErrorListener { _, _, _ ->
                                                isPlaying = false
                                                true
                                            }
                                            prepareAsync()
                                        }
                                        player = newPlayer
                                    } else {
                                        if (p.isPlaying) {
                                            p.pause()
                                            isPlaying = false
                                        } else {
                                            p.start()
                                            isPlaying = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                                    .clickable { togglePlay() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlaying) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(width = 1.5.dp, height = 6.dp).background(if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White))
                                            Box(modifier = Modifier.size(width = 1.5.dp, height = 6.dp).background(if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White))
                                        }
                                    } else {
                                        PlayIcon(
                                            tint = if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White,
                                            modifier = Modifier.size(7.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = sName,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        } else {
                            Text(
                                text = replyText,
                                color = textColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableSoundMessageContainer(
    isDark: Boolean,
    isUser: Boolean,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val maxOffsetPx = remember { with(density) { 100.dp.toPx() } }
    var offsetX by remember { mutableStateOf(0f) }
    
    val buttonBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val buttonIconTint = if (isDark) Color.White else Color.Black
    
    val isSwiped = offsetX != 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isUser) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (isUser) {
                            offsetX = if (offsetX < -maxOffsetPx / 2) -maxOffsetPx else 0f
                        } else {
                            offsetX = if (offsetX > maxOffsetPx / 2) maxOffsetPx else 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (isUser) {
                            offsetX = minOf(0f, maxOf(-maxOffsetPx, offsetX + dragAmount))
                        } else {
                            offsetX = maxOf(0f, minOf(maxOffsetPx, offsetX + dragAmount))
                        }
                    }
                )
            }
    ) {
        // Background Options (Visible under shifted content)
        Row(
            modifier = Modifier
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                .width(100.dp)
                .padding(horizontal = 8.dp)
                .graphicsLayer {
                    alpha = if (offsetX != 0f) 1f else 0f
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isUser) {
                // For user: Delete button and Reply button on the right
                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(enabled = isSwiped) {
                            onDelete()
                            offsetX = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = buttonIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reply Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(enabled = isSwiped) {
                            onReply()
                            offsetX = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = buttonIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // For member: Reply button and Delete button on the left
                // Reply Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(enabled = isSwiped) {
                            onReply()
                            offsetX = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = buttonIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .clickable(enabled = isSwiped) {
                            onDelete()
                            offsetX = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = buttonIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Foreground Content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .fillMaxWidth(),
            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            content()
        }
    }
}

data class MemeSound(val name: String, val url: String)

@Composable
fun PlayIcon(tint: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.25f)
            lineTo(size.width * 0.75f, size.height * 0.5f)
            lineTo(size.width * 0.35f, size.height * 0.75f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun MemeSoundPill(
    sound: MemeSound,
    isDark: Boolean,
    accentColor: Color,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA))
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onPlayClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Selection Sphere (hollow circle filled when selected)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isSelected) accentColor else (if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)),
                    shape = CircleShape
                )
                .background(if (isSelected) accentColor else Color.Transparent)
                .clickable { onSelectClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            PlayIcon(
                tint = if (isDark) Color.White else Color.Black,
                modifier = Modifier.size(12.dp)
            )
        }

        Text(
            text = sound.name,
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.White else Color.Black,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MemeSoundWaveformCard(
    soundUrl: String,
    soundName: String,
    isDark: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                player?.let { p ->
                    if (p.isPlaying) {
                        currentPosition = p.currentPosition
                        duration = p.duration
                    }
                }
                delay(50)
            }
        }
    }
    
    DisposableEffect(soundUrl) {
        val newPlayer = MediaPlayer().apply {
            try {
                setDataSource(soundUrl)
                setOnPreparedListener {
                    duration = it.duration
                }
                setOnCompletionListener {
                    isPlaying = false
                    currentPosition = 0
                }
                setOnErrorListener { _, _, _ ->
                    isPlaying = false
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        player = newPlayer
        onDispose {
            newPlayer.stop()
            newPlayer.release()
            player = null
        }
    }
    
    val playOrPause = {
        try {
            player?.let { p ->
                if (p.isPlaying) {
                    p.pause()
                    isPlaying = false
                } else {
                    p.start()
                    isPlaying = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val textAndIconColor = if (isDark) Color.White else Color.Black
    
    Row(
        modifier = modifier
            .width(180.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f))
                .clickable { playOrPause() },
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(width = 2.dp, height = 8.dp).background(textAndIconColor))
                    Box(modifier = Modifier.size(width = 2.dp, height = 8.dp).background(textAndIconColor))
                }
            } else {
                PlayIcon(
                    tint = textAndIconColor,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        
        val barCount = 20
        val amplitudes = remember(soundName) {
            val random = java.util.Random(soundName.hashCode().toLong())
            List(barCount) { 2.dp + random.nextInt(14).dp }
        }
        
        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
        
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.5.dp, Alignment.End)
            ) {
                amplitudes.forEachIndexed { index, barHeight ->
                    val barProgressLimit = index.toFloat() / barCount
                    val isFilled = progress >= barProgressLimit
                    
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isFilled) accentColor else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f))
                            )
                            .clickable {
                                try {
                                    player?.let { p ->
                                        val newPos = (duration * (index.toFloat() / barCount)).toInt()
                                        p.seekTo(newPos)
                                        currentPosition = newPos
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    )
                }
            }
            
            val displayMs = if (isPlaying && duration > 0) currentPosition else duration
            val secs = (displayMs / 1000) % 60
            val mins = (displayMs / 60000)
            val durationStr = String.format("%d:%02d", mins, secs)
            
            Text(
                text = durationStr,
                fontFamily = FontFamily.SansSerif,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}


@Composable
fun PalChatOverlay(
    showChat: Boolean,
    pal: PalItem,
    isDark: Boolean,
    textColor: Color,
    mutedTextColor: Color,
    accentColor: Color,
    headerButtonBg: Color,
    selectedProfileColor: Color,
    capturedVlogsPaths: List<String>,
    capturedVlogsCaptions: List<String>,
    allPalsSubmissions: Map<String, List<SubmissionDbItem>>,
    currentUserId: String,
    currentDisplayName: String,
    palReactions: Map<String, String>,
    onEmojiReacted: (String, String) -> Unit,
    onActiveReplyPreviewPathChange: (String?) -> Unit,
    onShowChatChange: (Boolean) -> Unit,
    onNavigateToCamera: () -> Unit,
    onSendMessage: (String) -> Unit,
    onShowExportDialogChange: (Boolean) -> Unit,
    customAvatarUriString: String?,
    allPalsMembers: Map<String, List<String>> = emptyMap(),
    messages: List<MessageDbItem> = emptyList(),
    onDeleteMessageLocal: (String) -> Unit = {},
    onDeleteVlog: (Int) -> Unit = {}
) {
    if (!showChat) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else PalBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        var messageInput by remember { mutableStateOf("") }
        var selectedVlogPreviewItem by remember { mutableStateOf<FeedItem?>(null) }
        val context = LocalContext.current
        val defaultEmojis = remember { listOf("😂", "❤️", "😭", "✨", "🥺", "🔥", "🥰", "🎉", "💀", "👍", "🙏", "💯", "😎", "👀") }
        var currentEmojis by remember { mutableStateOf(defaultEmojis.take(5)) }

        val scope = rememberCoroutineScope()
        val client = remember { HttpClient(io.ktor.client.engine.cio.CIO) }
        var showMemeSoundsMenu by remember { mutableStateOf(false) }
        var isFetchingMemeSounds by remember { mutableStateOf(false) }
        var fetchedMemeSounds by remember { mutableStateOf<List<MemeSound>>(emptyList()) }
        var displayedMemeSounds by remember { mutableStateOf<List<MemeSound>>(emptyList()) }
        var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedMemeSound by remember { mutableStateOf<MemeSound?>(null) }
        var replyTargetFeedItem by remember { mutableStateOf<FeedItem?>(null) }
        val replyFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

        val fallbackMemeSounds = remember {
            listOf(
                MemeSound("FAHHHHHHHHHHHHHH", "https://www.myinstants.com/media/sounds/fahhhhhhhhhhhhhh.mp3"),
                MemeSound("FAAAH", "https://www.myinstants.com/media/sounds/faaah.mp3"),
                MemeSound("VINE BOOM SOUND", "https://www.myinstants.com/media/sounds/vine-boom.mp3"),
                MemeSound("Fart", "https://www.myinstants.com/media/sounds/dry-fart.mp3"),
                MemeSound("Are baap re yaad aya", "https://www.myinstants.com/media/sounds/are-baap-re-yaad-aya.mp3"),
                MemeSound("Anime Wow", "https://www.myinstants.com/media/sounds/anime-wow-sound-effect.mp3"),
                MemeSound("Chicken screaming", "https://www.myinstants.com/media/sounds/chicken-on-tree-screaming.mp3"),
                MemeSound("Among Us role reveal", "https://www.myinstants.com/media/sounds/among-us-role-reveal-sound.mp3"),
                MemeSound("Sad Violin", "https://www.myinstants.com/media/sounds/tf_nemesis.mp3"),
                MemeSound("Chalo", "https://www.myinstants.com/media/sounds/chalo.mp3"),
                MemeSound("GopGopGop", "https://www.myinstants.com/media/sounds/gopgopgop.mp3"),
                MemeSound("anime ahh", "https://www.myinstants.com/media/sounds/anime-ahh.mp3"),
                MemeSound("Punch Sound", "https://www.myinstants.com/media/sounds/punch-gaming-sound-effect-hd_RzlG1GE.mp3"),
                MemeSound("HAha funny laugh", "https://www.myinstants.com/media/sounds/ny-video-online-audio-converter.mp3"),
                MemeSound("Matlab wo alag hi level", "https://www.myinstants.com/media/sounds/matlab-wo-alag-hi-level-ka-banda-tha.mp3")
            )
        }

        val loadMemeSounds = {
            isFetchingMemeSounds = true
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val fetched = try {
                    val response = client.get("https://www.myinstants.com/en/index/in/") {
                        header(io.ktor.http.HttpHeaders.UserAgent, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    }
                    val html = response.bodyAsText()
                    val regex = """play\('([^']+)'[^)]*\).*?class="instant-link[^"]*"[^>]*>([^<]+)</a>""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                    val matches = regex.findAll(html)
                    matches.map { matchResult ->
                        val path = matchResult.groupValues[1]
                        val name = matchResult.groupValues[2].trim()
                        val url = if (path.startsWith("http")) path else "https://www.myinstants.com$path"
                        MemeSound(name = name, url = url)
                    }.toList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isFetchingMemeSounds = false
                    if (fetched.isNotEmpty()) {
                        fetchedMemeSounds = fetched
                        displayedMemeSounds = fetched.shuffled().take(10)
                    } else {
                        displayedMemeSounds = fallbackMemeSounds.shuffled().take(10)
                    }
                }
            }
            Unit
        }

        val shuffleMemeSounds = {
            val sourceList = if (fetchedMemeSounds.isNotEmpty()) fetchedMemeSounds else fallbackMemeSounds
            displayedMemeSounds = sourceList.shuffled().take(10)
        }

        LaunchedEffect(searchQuery) {
            if (searchQuery.trim().isEmpty()) {
                val sourceList = if (fetchedMemeSounds.isNotEmpty()) fetchedMemeSounds else fallbackMemeSounds
                displayedMemeSounds = sourceList.shuffled().take(10)
            } else {
                delay(500L)
                isFetchingMemeSounds = true
                val query = searchQuery.trim()
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val fetched = try {
                        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                        val response = client.get("https://www.myinstants.com/en/search/?name=$encodedQuery") {
                            header(io.ktor.http.HttpHeaders.UserAgent, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        }
                        val html = response.bodyAsText()
                        val regex = """play\('([^']+)'[^)]*\).*?class="instant-link[^"]*"[^>]*>([^<]+)</a>""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                        val matches = regex.findAll(html)
                        matches.map { matchResult ->
                            val path = matchResult.groupValues[1]
                            val name = matchResult.groupValues[2].trim()
                            val url = if (path.startsWith("http")) path else "https://www.myinstants.com$path"
                            MemeSound(name = name, url = url)
                        }.toList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList()
                    }
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isFetchingMemeSounds = false
                        displayedMemeSounds = if (fetched.isNotEmpty()) {
                            fetched.take(15)
                        } else {
                            fallbackMemeSounds.filter { it.name.contains(query, ignoreCase = true) }
                        }
                    }
                }
            }
        }

        val playMemeSound = { soundUrl: String ->
            try {
                currentMediaPlayer?.stop()
                currentMediaPlayer?.release()
                currentMediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            currentMediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(soundUrl)
                    setOnPreparedListener { start() }
                    setOnErrorListener { _, _, _ ->
                        Toast.makeText(context, "Failed to play sound", Toast.LENGTH_SHORT).show()
                        true
                    }
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error playing sound", Toast.LENGTH_SHORT).show()
                }
            }
            Unit
        }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    currentMediaPlayer?.stop()
                    currentMediaPlayer?.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (showMemeSoundsMenu) {
            androidx.activity.compose.BackHandler {
                showMemeSoundsMenu = false
            }
        }

        val feedItems = remember(pal.code, capturedVlogsPaths, allPalsSubmissions, currentUserId, messages) {
            val soundItems = messages.filter { !it.content.startsWith("REPLY|||") }.mapNotNull { msg ->
                val instant = if (!msg.createdAt.isNullOrEmpty()) {
                    try {
                        java.time.Instant.parse(msg.createdAt)
                    } catch (e: Exception) {
                        java.time.Instant.now()
                    }
                } else {
                    java.time.Instant.now()
                }
                val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                val dayDateStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US))
                val timeStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                
                val membersList = allPalsMembers[pal.code] ?: emptyList()
                val senderMember = membersList.firstOrNull { it.startsWith("${msg.userId}|||") }
                val senderName = if (senderMember != null) {
                    senderMember.split("|||").getOrNull(1) ?: "Pal"
                } else {
                    if (msg.userId == currentUserId) currentDisplayName else "Pal"
                }

                if (msg.content.startsWith("SOUND|||")) {
                    val parts = msg.content.split("|||")
                    val name = parts.getOrNull(1) ?: ""
                    val url = parts.getOrNull(2) ?: ""
                    if (name.isNotEmpty() && url.isNotEmpty()) {
                        FeedItem(
                            path = msg.id ?: url,
                            caption = "",
                            userId = msg.userId,
                            userDisplayName = senderName,
                            dayDateStr = dayDateStr,
                            timeStr = timeStr,
                            rawInstant = instant,
                            localDate = zonedDateTime.toLocalDate(),
                            isUser = (msg.userId == currentUserId),
                            isSound = true,
                            soundName = name,
                            soundUrl = url,
                            messageId = msg.id
                        )
                    } else {
                        null
                    }
                } else {
                    FeedItem(
                        path = msg.id ?: msg.content,
                        caption = "",
                        userId = msg.userId,
                        userDisplayName = senderName,
                        dayDateStr = dayDateStr,
                        timeStr = timeStr,
                        rawInstant = instant,
                        localDate = zonedDateTime.toLocalDate(),
                        isUser = (msg.userId == currentUserId),
                        isTextMessage = true,
                        textMessageContent = msg.content,
                        messageId = msg.id
                    )
                }
            }

            if (pal.isVlog) {
                val vlogItems = capturedVlogsPaths.mapIndexedNotNull { idx, path ->
                    val localPath = getVlogPrefs(context).getString("local_path_$path", null)
                    val resolvedPath = localPath ?: path
                    val cleanPath = if (resolvedPath.startsWith("file://")) resolvedPath.substring(7) else resolvedPath
                    val file = java.io.File(cleanPath)
                    if (file.exists() || path.startsWith("http")) {
                        val matchingSub = allPalsSubmissions["vlog"]?.firstOrNull { it.imageUrl.startsWith(path) }
                        val instant = if (matchingSub?.createdAt != null) {
                            try {
                                java.time.Instant.parse(matchingSub.createdAt)
                            } catch (e: Exception) {
                                if (file.exists()) java.time.Instant.ofEpochMilli(file.lastModified()) else java.time.Instant.now()
                            }
                        } else if (file.exists()) {
                            java.time.Instant.ofEpochMilli(file.lastModified())
                        } else {
                            java.time.Instant.now()
                        }
                        val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                        val dayDateStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US))
                        val timeStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                        val caption = capturedVlogsCaptions.getOrNull(idx) ?: ""
                        FeedItem(
                            path = path,
                            caption = caption,
                            userId = currentUserId,
                            userDisplayName = currentDisplayName,
                            dayDateStr = dayDateStr,
                            timeStr = timeStr,
                            rawInstant = instant,
                            localDate = zonedDateTime.toLocalDate(),
                            isUser = true
                        )
                    } else {
                        null
                    }
                }
                (vlogItems + soundItems).sortedByDescending { it.rawInstant }
            } else {
                val subs = allPalsSubmissions[pal.code] ?: emptyList()
                val subItems = subs.mapNotNull { sub ->
                    val path = sub.imageUrl.split("|||").firstOrNull() ?: ""
                    val caption = sub.imageUrl.split("|||").getOrNull(1) ?: ""
                    val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
                    val file = java.io.File(cleanPath)
                    if (file.exists() || path.isNotEmpty()) {
                        val instant = if (!sub.createdAt.isNullOrEmpty()) {
                            try {
                                java.time.Instant.parse(sub.createdAt)
                            } catch (e: Exception) {
                                java.time.Instant.now()
                            }
                        } else {
                            java.time.Instant.now()
                        }
                        val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                        val dayDateStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US))
                        val timeStr = zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                        FeedItem(
                            path = path,
                            caption = caption,
                            userId = sub.userId,
                            userDisplayName = sub.userDisplayName,
                            dayDateStr = dayDateStr,
                            timeStr = timeStr,
                            rawInstant = instant,
                            localDate = zonedDateTime.toLocalDate(),
                            isUser = (sub.userId == currentUserId)
                        )
                    } else {
                        null
                    }
                }
                (subItems + soundItems).sortedBy { it.rawInstant }
            }
        }

        val groupedByDay = remember(feedItems) {
            feedItems.groupBy { it.localDate }
        }

        val vlogGroups = remember(feedItems) {
            val sortedItems = feedItems.sortedBy { it.rawInstant }
            val groups = LinkedHashMap<String, MutableList<FeedItem>>()
            sortedItems.forEach { item ->
                val today = java.time.LocalDate.now()
                val dayLabel = when (item.localDate) {
                    today -> "Today"
                    today.minusDays(1) -> "Yesterday"
                    else -> item.localDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d", java.util.Locale.US))
                }
                val header = "$dayLabel ${item.timeStr}"
                groups.getOrPut(header) { mutableListOf() }.add(item)
            }
            groups.toList()
        }

        var showEmojiOverlayForPath by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Scrollable feed column
            if (feedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "no captured pals yet",
                        fontFamily = OwnglyphFontFamily,
                        fontSize = 24.5.sp,
                        color = mutedTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 56.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (pal.isVlog) {
                        vlogGroups.forEach { (headerText, items) ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Centered Date/Time header
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = headerText,
                                        color = textColor.copy(alpha = 0.6f),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Normal
                                    )
                                }

                                // 2. Video thumbnails stacked vertically on the right
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 17.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items.forEach { feedItem ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(28.dp))
                                                .background(Color.Black)
                                                .clickable {
                                                    selectedVlogPreviewItem = feedItem
                                                }
                                        ) {
                                            VideoThumbnail(
                                                videoPath = feedItem.path,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        vlogGroups.forEach { (headerText, items) ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 1. Centered Date/Time header (regular font, no bold)
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = headerText,
                                        color = textColor.copy(alpha = 0.6f),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Normal // Regular font, no bold
                                    )
                                }

                                // 2. Video thumbnails (aligned right for user, left for other members)
                                items.forEach { feedItem ->
                                    val feedReactions = remember(messages, feedItem.path, allPalsMembers, currentDisplayName, customAvatarUriString) {
                                        messages.filter { msg ->
                                            if (msg.content.startsWith("REACTION|||")) {
                                                val parts = msg.content.split("|||")
                                                val targetPath = parts.getOrNull(3) ?: ""
                                                targetPath == feedItem.path
                                            } else {
                                                false
                                            }
                                        }.map { msg ->
                                            val parts = msg.content.split("|||")
                                            val emoji = parts.getOrNull(4) ?: ""
                                            val membersList = allPalsMembers[pal.code] ?: emptyList()
                                            val senderMember = membersList.firstOrNull { it.startsWith("${msg.userId}|||") }
                                            val (senderName, senderAvatar) = if (senderMember != null) {
                                                val sParts = senderMember.split("|||")
                                                Pair(sParts.getOrNull(1) ?: "Pal", sParts.getOrNull(2))
                                            } else {
                                                if (msg.userId == currentUserId) {
                                                    Pair(currentDisplayName, customAvatarUriString)
                                                } else {
                                                    Pair("Pal", null)
                                                }
                                            }
                                            Triple(senderName, senderAvatar, emoji)
                                        }.distinctBy { it.first }
                                    }

                                    val feedReplies = remember(messages, feedItem.path, allPalsMembers, currentDisplayName, customAvatarUriString) {
                                        messages.filter { msg ->
                                            if (msg.content.startsWith("REPLY|||")) {
                                                val parts = msg.content.split("|||")
                                                val targetPath = parts.getOrNull(3) ?: ""
                                                targetPath == feedItem.path
                                            } else {
                                                false
                                            }
                                        }.map { msg ->
                                            val parts = msg.content.split("|||")
                                            val replyText = parts.getOrNull(4) ?: ""
                                            val membersList = allPalsMembers[pal.code] ?: emptyList()
                                            val senderMember = membersList.firstOrNull { it.startsWith("${msg.userId}|||") }
                                            val (senderName, senderAvatar) = if (senderMember != null) {
                                                val sParts = senderMember.split("|||")
                                                Pair(sParts.getOrNull(1) ?: "Pal", sParts.getOrNull(2))
                                            } else {
                                                if (msg.userId == currentUserId) {
                                                    Pair(currentDisplayName, customAvatarUriString)
                                                } else {
                                                    Pair("Pal", null)
                                                }
                                            }
                                            Triple(senderName, senderAvatar, replyText)
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (feedItem.isUser) {
                                            // USER (Right Aligned)
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 17.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                ) {
                                                    val (displayName, avatarUrl) = parseUserDisplayName(feedItem.userDisplayName)
                                                    val cleanName = displayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                                                    Text(
                                                        text = cleanName,
                                                        color = textColor,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.SansSerif
                                                    )
                                                    val finalAvatar = if (avatarUrl?.startsWith("http") == true) avatarUrl else customAvatarUriString
                                                    if (!finalAvatar.isNullOrEmpty()) {
                                                        UriImage(
                                                            uriString = finalAvatar,
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .background(accentColor),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.smile_medium),
                                                                contentDescription = null,
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .rotate(180f)
                                                            )
                                                        }
                                                    }
                                                }

                                                if (feedItem.isSound) {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = true,
                                                        onDelete = {
                                                             val msgId = feedItem.messageId
                                                             val msgPath = feedItem.path
                                                             onDeleteMessageLocal(msgPath)
                                                             scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                 try {
                                                                     com.finrein.pals.PalApplication.supabase.postgrest.from("messages").delete {
                                                                         filter {
                                                                             if (!msgId.isNullOrEmpty()) {
                                                                                 eq("id", msgId)
                                                                             } else if (msgPath.contains("-") && msgPath.length >= 32) {
                                                                                 eq("id", msgPath)
                                                                             } else {
                                                                                 if (feedItem.isSound) {
                                                                                     like("message_text", "%$msgPath%")
                                                                                 } else {
                                                                                     eq("message_text", msgPath)
                                                                                 }
                                                                             }
                                                                         }
                                                                     }
                                                                 } catch (e: Exception) {
                                                                     e.printStackTrace()
                                                                 }
                                                             }
                                                        },
                                                        onReply = {
                                                            replyTargetFeedItem = feedItem
                                                            replyFocusRequester.requestFocus()
                                                        }
                                                    ) {
                                                        MemeSoundWaveformCard(
                                                            soundUrl = feedItem.soundUrl,
                                                            soundName = feedItem.soundName,
                                                            isDark = isDark,
                                                            accentColor = accentColor
                                                        )
                                                    }
                                                } else if (feedItem.isTextMessage) {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = true,
                                                        onDelete = {
                                                             val msgId = feedItem.messageId
                                                             val msgPath = feedItem.path
                                                             onDeleteMessageLocal(msgPath)
                                                             scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                 try {
                                                                     com.finrein.pals.PalApplication.supabase.postgrest.from("messages").delete {
                                                                         filter {
                                                                             if (!msgId.isNullOrEmpty()) {
                                                                                 eq("id", msgId)
                                                                             } else if (msgPath.contains("-") && msgPath.length >= 32) {
                                                                                 eq("id", msgPath)
                                                                             } else {
                                                                                 if (feedItem.isSound) {
                                                                                     like("message_text", "%$msgPath%")
                                                                                 } else {
                                                                                     eq("message_text", msgPath)
                                                                                 }
                                                                             }
                                                                         }
                                                                     }
                                                                 } catch (e: Exception) {
                                                                     e.printStackTrace()
                                                                 }
                                                             }
                                                        },
                                                        onReply = {
                                                            replyTargetFeedItem = feedItem
                                                            replyFocusRequester.requestFocus()
                                                        }
                                                    ) {
                                                        val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White
                                                        val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(18.dp))
                                                                .background(cardBg)
                                                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                                        ) {
                                                            Text(
                                                                text = feedItem.textMessageContent,
                                                                fontFamily = FontFamily.SansSerif,
                                                                fontSize = 13.sp,
                                                                color = if (isDark) Color.White else Color.Black
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = true,
                                                        onDelete = {
                                                            val vIndex = capturedVlogsPaths.indexOf(feedItem.path)
                                                            if (vIndex != -1) {
                                                                onDeleteVlog(vIndex)
                                                            }
                                                        },
                                                        onReply = {
                                                            onActiveReplyPreviewPathChange(feedItem.path)
                                                        }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(0.5f)
                                                                .aspectRatio(16f / 9f)
                                                                .clip(RoundedCornerShape(28.dp))
                                                                .background(Color.Black)
                                                                .clickable {
                                                                    selectedVlogPreviewItem = feedItem
                                                                }
                                                        ) {
                                                            VideoThumbnail(
                                                                videoPath = feedItem.path,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    FeedReactionsAndReplies(
                                                        feedReactions = feedReactions,
                                                        feedReplies = feedReplies,
                                                        textColor = textColor,
                                                        isDark = isDark,
                                                        accentColor = accentColor
                                                    )
                                                }
                                            }
                                        } else {
                                            // OTHERS (Left Aligned)
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 24.dp),
                                                horizontalAlignment = Alignment.Start
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                ) {
                                                    val (displayName, avatarUrl) = parseUserDisplayName(feedItem.userDisplayName)
                                                    if (!avatarUrl.isNullOrEmpty()) {
                                                        UriImage(
                                                            uriString = avatarUrl,
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .background(accentColor),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.smile_medium),
                                                                contentDescription = null,
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .rotate(180f)
                                                            )
                                                        }
                                                    }
                                                    val cleanName = displayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                                                    Text(
                                                        text = cleanName,
                                                        color = textColor,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.SansSerif
                                                    )
                                                }

                                                if (feedItem.isSound) {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = false,
                                                        onDelete = {
                                                             val msgId = feedItem.messageId
                                                             val msgPath = feedItem.path
                                                             onDeleteMessageLocal(msgPath)
                                                             scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                 try {
                                                                     com.finrein.pals.PalApplication.supabase.postgrest.from("messages").delete {
                                                                         filter {
                                                                             if (!msgId.isNullOrEmpty()) {
                                                                                 eq("id", msgId)
                                                                             } else if (msgPath.contains("-") && msgPath.length >= 32) {
                                                                                 eq("id", msgPath)
                                                                             } else {
                                                                                 if (feedItem.isSound) {
                                                                                     like("message_text", "%$msgPath%")
                                                                                 } else {
                                                                                     eq("message_text", msgPath)
                                                                                 }
                                                                             }
                                                                         }
                                                                     }
                                                                 } catch (e: Exception) {
                                                                     e.printStackTrace()
                                                                 }
                                                             }
                                                        },
                                                        onReply = {
                                                            replyTargetFeedItem = feedItem
                                                            replyFocusRequester.requestFocus()
                                                        }
                                                    ) {
                                                        MemeSoundWaveformCard(
                                                            soundUrl = feedItem.soundUrl,
                                                            soundName = feedItem.soundName,
                                                            isDark = isDark,
                                                            accentColor = accentColor
                                                        )
                                                    }
                                                } else if (feedItem.isTextMessage) {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = false,
                                                        onDelete = {
                                                             val msgId = feedItem.messageId
                                                             val msgPath = feedItem.path
                                                             onDeleteMessageLocal(msgPath)
                                                             scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                                 try {
                                                                     com.finrein.pals.PalApplication.supabase.postgrest.from("messages").delete {
                                                                         filter {
                                                                             if (!msgId.isNullOrEmpty()) {
                                                                                 eq("id", msgId)
                                                                             } else if (msgPath.contains("-") && msgPath.length >= 32) {
                                                                                 eq("id", msgPath)
                                                                             } else {
                                                                                 if (feedItem.isSound) {
                                                                                     like("message_text", "%$msgPath%")
                                                                                 } else {
                                                                                     eq("message_text", msgPath)
                                                                                 }
                                                                             }
                                                                         }
                                                                     }
                                                                 } catch (e: Exception) {
                                                                     e.printStackTrace()
                                                                 }
                                                             }
                                                        },
                                                        onReply = {
                                                            replyTargetFeedItem = feedItem
                                                            replyFocusRequester.requestFocus()
                                                        }
                                                    ) {
                                                        val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White
                                                        val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(18.dp))
                                                                .background(cardBg)
                                                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                                        ) {
                                                            Text(
                                                                text = feedItem.textMessageContent,
                                                                fontFamily = FontFamily.SansSerif,
                                                                fontSize = 13.sp,
                                                                color = if (isDark) Color.White else Color.Black
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    SwipeableSoundMessageContainer(
                                                        isDark = isDark,
                                                        isUser = false,
                                                        onDelete = {
                                                            val vIndex = capturedVlogsPaths.indexOf(feedItem.path)
                                                            if (vIndex != -1) {
                                                                onDeleteVlog(vIndex)
                                                            }
                                                        },
                                                        onReply = {
                                                            onActiveReplyPreviewPathChange(feedItem.path)
                                                        }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(0.5f)
                                                                .aspectRatio(16f / 9f)
                                                                .clip(RoundedCornerShape(28.dp))
                                                                .background(Color.Black)
                                                                .clickable {
                                                                    selectedVlogPreviewItem = feedItem
                                                                }
                                                        ) {
                                                            VideoThumbnail(
                                                                videoPath = feedItem.path,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    FeedReactionsAndReplies(
                                                        feedReactions = feedReactions,
                                                        feedReplies = feedReplies,
                                                        textColor = textColor,
                                                        isDark = isDark,
                                                        accentColor = accentColor
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 3. Render the day's "view pal" compilation box at the end of the day's last post
                                    val isLastItemOfDay = remember(feedItems, feedItem.localDate, feedItem.path) {
                                        feedItems.filter { it.localDate == feedItem.localDate }.lastOrNull()?.path == feedItem.path
                                    }
                                    if (isLastItemOfDay) {
                                        val today = java.time.LocalDate.now()
                                        val dayLabel = when (feedItem.localDate) {
                                            today -> "Today"
                                            today.minusDays(1) -> "Yesterday"
                                            else -> feedItem.localDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d", java.util.Locale.US))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F3EB))
                                                .border(1.dp, selectedProfileColor, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onShowExportDialogChange(true)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = dayLabel,
                                                color = textColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.align(Alignment.CenterStart)
                                            )
                                            Text(
                                                text = "view pal",
                                                color = Color(0xFFFF007F),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.align(Alignment.CenterEnd)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Emoji Reaction Overlay
            if (showEmojiOverlayForPath != null) {
                val path = showEmojiOverlayForPath!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showEmojiOverlayForPath = null
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        currentEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .clickable {
                                        onEmojiReacted(path, emoji)
                                        showEmojiOverlayForPath = null
                                    }
                            )
                        }

                        val stroke = remember { androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        ) }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    currentEmojis = defaultEmojis.shuffled().take(5)
                                }
                                .drawBehind {
                                    drawCircle(color = Color.White, style = stroke)
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.smile_medium),
                                contentDescription = "Shuffle",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 2. Header Box
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 2.dp)
                        .size(32.5.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { onShowChatChange(false) },
                    contentAlignment = Alignment.Center
                ) {
                    ChevronLeftIcon(
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                val headerTitleText = if (pal.isVlog) "vlog" else pal.name
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 2.dp)
                        .height(32.5.dp)
                        .wrapContentWidth()
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161616) else Color(0xFFEBEBEB))
                        .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = headerTitleText,
                        fontFamily = BricolageVariableFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            // 3. Footer Column (Reply Preview + Footer Row)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .background(if (isDark) Color.Black else PalBackground)
            ) {
                // Reply Preview Bar
                if (replyTargetFeedItem != null) {
                    val replyTarget = replyTargetFeedItem!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA))
                            .border(
                                width = 1.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Replying to",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Replying to ${replyTarget.userDisplayName}'s sound: ${replyTarget.soundName}",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { replyTargetFeedItem = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Footer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isVlog = pal.isVlog
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF161616) else Color(0xFFEBEBEB))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                            .clickable {
                                if (isVlog) {
                                    onNavigateToCamera()
                                } else {
                                    loadMemeSounds()
                                    showMemeSoundsMenu = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVlog) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.capture_smile),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .graphicsLayer(rotationZ = -180f)
                                        .fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.internet_troll_meme_face),
                                    contentDescription = "Meme Sounds",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC), RoundedCornerShape(22.dp))
                            .background(Color.Transparent)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                color = textColor
                            ),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(replyFocusRequester),
                            decorationBox = { innerTextField ->
                                if (messageInput.isEmpty()) {
                                    Text(
                                        text = "message",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 14.sp,
                                        color = mutedTextColor
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    val isInputValid = messageInput.trim().isNotEmpty()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isInputValid) accentColor else headerButtonBg)
                            .clickable(enabled = isInputValid) {
                                val replyTarget = replyTargetFeedItem
                                val text = messageInput.trim()
                                if (replyTarget != null) {
                                    val replyContent = "REPLY|||${replyTarget.userId}|||${replyTarget.userDisplayName}|||${replyTarget.path}|||$text"
                                    onSendMessage(replyContent)
                                    replyTargetFeedItem = null
                                } else {
                                    onSendMessage(text)
                                }
                                messageInput = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (isInputValid) Color.White else textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 4. Vlog Fullscreen Preview Overlay
            if (selectedVlogPreviewItem != null) {
                val item = selectedVlogPreviewItem!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedVlogPreviewItem = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // X Close button inside a dark circle at top-left
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, start = 24.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                selectedVlogPreviewItem = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Vlog Card (exactly how vlog box displays with 16:9 ratio and rounded corners)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {} // Consume click inside video box
                            )
                    ) {
                        VideoPlayerItem(
                            videoPath = item.path,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top-left profile avatar and user name
                        val (displayName, avatarUrl) = parseUserDisplayName(item.userDisplayName)
                        val cleanName = displayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 5.5.dp, start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!avatarUrl.isNullOrEmpty()) {
                                UriImage(
                                    uriString = avatarUrl,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.smile_medium),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(180f)
                                    )
                                }
                            }
                            Text(
                                text = cleanName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }

                        // Center: Large bold hour-only time slot (e.g. 23:00)
                        val captureHourOnlyText = try {
                            val zonedDateTime = item.rawInstant.atZone(java.time.ZoneId.systemDefault())
                            String.format(java.util.Locale.US, "%02d:00", zonedDateTime.hour)
                        } catch (e: Exception) {
                            item.timeStr
                        }

                        Text(
                            text = captureHourOnlyText,
                            fontFamily = DelaGothicOneFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                            style = TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        // Bottom: Caption text (if present)
                        if (item.caption.isNotEmpty()) {
                            Text(
                                text = item.caption,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .padding(horizontal = 24.dp),
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // Meme Sounds Menu Overlay (Repositioned between header and footer)
            if (showMemeSoundsMenu) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 80.dp)
                        .background(if (isDark) Color.Black else PalBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDark) Color(0xFF161616) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f), CircleShape)
                                        .clickable {
                                            showMemeSoundsMenu = false
                                            selectedMemeSound = null
                                            searchQuery = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = textColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "meme sounds",
                                    fontFamily = BricolageVariableFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Refresh Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f), CircleShape)
                                            .clickable {
                                                shuffleMemeSounds()
                                                selectedMemeSound = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = textColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Send Button (Colorless/Active Send States)
                                    val isSendActive = selectedMemeSound != null
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSendActive) accentColor else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)))
                                            .clickable(enabled = isSendActive) {
                                                val sound = selectedMemeSound
                                                if (sound != null) {
                                                    val replyTarget = replyTargetFeedItem
                                                    if (replyTarget != null) {
                                                        val replyContent = "REPLY|||${replyTarget.userId}|||${replyTarget.userDisplayName}|||${replyTarget.path}|||SOUND|||${sound.name}|||${sound.url}"
                                                        onSendMessage(replyContent)
                                                        replyTargetFeedItem = null
                                                    } else {
                                                        onSendMessage("SOUND|||${sound.name}|||${sound.url}")
                                                    }
                                                    selectedMemeSound = null
                                                    showMemeSoundsMenu = false
                                                    searchQuery = ""
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        UpwardArrowIcon(
                                            tint = if (isSendActive) {
                                                if (accentColor == Color(0xFF00F0FF) || accentColor == Color(0xFFFFE600) || accentColor == Color(0xFFFBC02D)) Color.Black else Color.White
                                            } else {
                                                textColor.copy(alpha = 0.3f)
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Search bar (OutlinedTextField) with 500ms debounce
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search meme sounds...",
                                        fontSize = 13.sp,
                                        color = textColor.copy(alpha = 0.5f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { searchQuery = "" },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = textColor.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = textColor.copy(alpha = 0.15f),
                                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                singleLine = true
                            )

                            // Meme sounds list
                            if (isFetchingMemeSounds) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = accentColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    displayedMemeSounds.forEach { sound ->
                                        MemeSoundPill(
                                            sound = sound,
                                            isDark = isDark,
                                            accentColor = accentColor,
                                            isSelected = (selectedMemeSound == sound),
                                            onSelectClick = {
                                                selectedMemeSound = if (selectedMemeSound == sound) null else sound
                                            },
                                            onPlayClick = {
                                                playMemeSound(sound.url)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlusMenuOverlay(
    showPlusMenu: Boolean,
    onShowPlusMenuChange: (Boolean) -> Unit,
    overlayBackdropColor: Color,
    isDark: Boolean,
    navBarBgColor: Color,
    textColor: Color,
    onCreatePalClick: () -> Unit,
    onJoinPalClick: () -> Unit
) {
    if (showPlusMenu) {
        androidx.activity.compose.BackHandler {
            onShowPlusMenuChange(false)
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayBackdropColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onShowPlusMenuChange(false)
                    }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 65.dp, end = 35.dp)
                    .width(130.dp)
                    .background(if (isDark) navBarBgColor else Color(0xFFF5F3EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "create",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onShowPlusMenuChange(false)
                                onCreatePalClick()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    Text(
                        text = "join",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onShowPlusMenuChange(false)
                                onJoinPalClick()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityScreenOverlay(
    showActivityScreen: Boolean,
    onShowActivityScreenChange: (Boolean) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    mutedTextColor: Color
) {
    if (showActivityScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))

                Text(
                    text = "activity",
                    fontFamily = BricolageVariableFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onShowActivityScreenChange(false) }
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "no activity yet.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = mutedTextColor
                )
            }
        }
    }
}

@Composable
fun JoinPalDialogOverlay(
    showJoinPalFlow: Boolean,
    onShowJoinPalFlowChange: (Boolean) -> Unit,
    joinPalCode: String,
    onJoinPalCodeChange: (String) -> Unit,
    isDark: Boolean,
    accentColor: Color,
    currentUserId: String,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    refreshPals: () -> Unit,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    currentDisplayName: String,
    customAvatarUriString: String?
) {
    if (showJoinPalFlow) {
        androidx.activity.compose.BackHandler {
            onShowJoinPalFlowChange(false)
        }

        val localScope = rememberCoroutineScope()
        val context = LocalContext.current
        var isFocused by remember { mutableStateOf(false) }
        val verticalBias by animateFloatAsState(
            targetValue = if (isFocused) 0f else 1f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            label = "join_card_vertical_bias"
        )
        val focusRequester = remember { FocusRequester() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) Color(0xCC121212) else Color(0xCCF5F3EB)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onShowJoinPalFlowChange(false)
                }
                .navigationBarsPadding()
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = 0f, verticalBias = verticalBias))
                    .size(400.dp)
                    .background(
                        Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFFE040FB).copy(alpha = 0.22f),
                                    Color(0xFF00E676).copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0xFFFF46D8).copy(alpha = 0.14f),
                                    Color(0xFF00E676).copy(alpha = 0.04f),
                                    Color.Transparent
                                )
                            }
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = 0f, verticalBias = verticalBias))
                    .width(320.dp)
                    .padding(horizontal = 16.dp, vertical = if (isFocused) 0.dp else 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xF21C1A1E), Color(0xE6141216))
                                } else {
                                    listOf(Color(0xF2F9F7F0), Color(0xE6F5F3EB))
                                }
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.White.copy(alpha = 0.04f))
                                } else {
                                    listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent, Color.Black.copy(alpha = 0.02f))
                                }
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "join a pal",
                            fontFamily = DelaGothicOneFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color.Black
                        )

                        Text(
                            text = "ask your friends for their pin#",
                            fontFamily = OwnglyphFontFamily,
                            fontSize = 14.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(
                            2.dp,
                            if (isDark) Color.White else Color.Black,
                            CircleShape
                        )
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "#",
                            fontFamily = RobotoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                        TextField(
                            value = joinPalCode,
                            onValueChange = { onJoinPalCodeChange(it.lowercase(java.util.Locale.ROOT)) },
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = RobotoFontFamily,
                                fontSize = 16.sp,
                                color = if (isDark) Color.White else Color.Black
                            ),
                            placeholder = {
                                Text(
                                    text = "abc123",
                                    fontFamily = RobotoFontFamily,
                                    fontSize = 16.sp,
                                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f)
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = if (isDark) Color.White else Color.Black,
                                unfocusedIndicatorColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.3f),
                                cursorColor = accentColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                    if (it.isFocused) {
                                        keyboardController?.show()
                                    }
                                }
                        )

                        val isCodeValid = joinPalCode.trim().isNotEmpty()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCodeValid) accentColor else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                                )
                                .clickable(enabled = isCodeValid) {
                                    val code = joinPalCode.trim().lowercase(java.util.Locale.ROOT)
                                    localScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            // 1. Check/Insert mapping first to bypass pals RLS select policy
                                            val existingMapping = supabaseClient.postgrest.from("user_pals")
                                                .select {
                                                    filter {
                                                        eq("user_id", currentUserId)
                                                        eq("pal_code", code)
                                                    }
                                                }
                                                .decodeSingleOrNull<UserPalMapping>()

                                            if (existingMapping == null) {
                                                val newMapping = UserPalMapping(
                                                    userId = currentUserId,
                                                    palCode = code,
                                                    userDisplayName = currentDisplayName,
                                                    userAvatarUrl = customAvatarUriString
                                                )
                                                // If code is invalid, foreign key constraint will throw exception here
                                                supabaseClient.postgrest.from("user_pals").upsert(newMapping, onConflict = "pal_code,user_id")
                                            }

                                            // 2. Fetch group details with retries to account for database replication lag
                                            var matchedPalDb: PalDbItem? = null
                                            for (i in 1..4) {
                                                try {
                                                    matchedPalDb = supabaseClient.postgrest.from("pals")
                                                        .select {
                                                            filter {
                                                                eq("pal_code", code)
                                                            }
                                                        }
                                                        .decodeSingleOrNull<PalDbItem>()
                                                    if (matchedPalDb != null) break
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                                delay(500)
                                            }

                                            // 3. Fallback construct if RLS or replication delays details fetching
                                            val matchedItem = PalItem(
                                                name = matchedPalDb?.name?.removeSuffix(" ($code)") ?: "Pals Group",
                                                size = "1",
                                                code = code,
                                                isVlog = false,
                                                isCreator = false
                                            )

                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                if (!createdPals.any { it.code == code }) {
                                                    onCreatedPalsChange(createdPals + matchedItem)
                                                }
                                                refreshPals()
                                                onShowJoinPalFlowChange(false)
                                                val groupNameToShow = matchedPalDb?.name?.removeSuffix(" ($code)") ?: "Pals Group"
                                                android.widget.Toast.makeText(context, "Successfully joined $groupNameToShow!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Cleanup if insert succeeded but failed later
                                            try {
                                                supabaseClient.postgrest.from("user_pals").delete {
                                                    filter {
                                                        eq("user_id", currentUserId)
                                                        eq("pal_code", code)
                                                    }
                                                }
                                            } catch (ex: Exception) {}
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                android.widget.Toast.makeText(context, "Pal code not found or invalid", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Submit",
                                tint = if (isCodeValid) Color.White else (if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditNameDialogOverlay(
    showEditNameDialog: Boolean,
    onShowEditNameDialogChange: (Boolean) -> Unit,
    isDark: Boolean,
    accentColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    editFirstName: String,
    onEditFirstNameChange: (String) -> Unit,
    editLastName: String,
    onEditLastNameChange: (String) -> Unit,
    editNameFocusRequester: FocusRequester,
    onEditNameBoundsChange: (Rect) -> Unit,
    currentDisplayName: String,
    onCurrentDisplayNameChange: (String) -> Unit,
    useDarkTextOnAccent: Boolean
) {
    if (showEditNameDialog) {
        androidx.activity.compose.BackHandler {
            onShowEditNameDialogChange(false)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { 
                    onShowEditNameDialogChange(false) 
                }
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            val dialogBgColor = if (isDark) Color(0xFF161616) else Color(0xFFF5F3EB)
            Box(
                modifier = Modifier
                    .width(290.dp)
                    .offset(y = (-27).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(dialogBgColor)
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .onGloballyPositioned { coords ->
                        onEditNameBoundsChange(coords.boundsInRoot())
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click to avoid closing the dialog
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "edit name",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Text(
                        text = "enter your name",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = mutedTextColor,
                        fontWeight = FontWeight.Normal
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFEBEBEB))
                            .padding(vertical = 4.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = editFirstName,
                            onValueChange = onEditFirstNameChange,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = textColor
                            ),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(43.dp)
                                .focusRequester(editNameFocusRequester),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (editFirstName.isEmpty()) {
                                        Text(
                                            text = "First",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 12.sp,
                                            color = mutedTextColor
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        
                        androidx.compose.foundation.text.BasicTextField(
                            value = editLastName,
                            onValueChange = onEditLastNameChange,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = textColor
                            ),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(43.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (editLastName.isEmpty()) {
                                        Text(
                                            text = "Last",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 12.sp,
                                            color = mutedTextColor
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isDark) Color(0xFF262626) else Color(0xFFE5E5E5))
                                .clickable {
                                    onShowEditNameDialogChange(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "cancel",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(accentColor)
                                .clickable {
                                    if (editFirstName.trim().isNotEmpty()) {
                                        onCurrentDisplayNameChange("$editFirstName $editLastName".trim())
                                        onShowEditNameDialogChange(false)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "save",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                color = if (useDarkTextOnAccent && !isDark) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePalDialogOverlay(
    showCreatePalFlow: Boolean,
    onShowCreatePalFlowChange: (Boolean) -> Unit,
    isCreatingPal: Boolean,
    onIsCreatingPalChange: (Boolean) -> Unit,
    createPalStep: Int,
    onCreatePalStepChange: (Int) -> Unit,
    newPalName: String,
    onNewPalNameChange: (String) -> Unit,
    newPalSize: String,
    onNewPalSizeChange: (String) -> Unit,
    generatedPalCode: String,
    creationDots: String,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    currentUserId: String,
    groupDatabase: MutableMap<String, PalItem>,
    createPalFocusRequester: FocusRequester,
    isDark: Boolean,
    accentColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    palTextLogoColor: Color,
    backgroundColor: Color,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    currentDisplayName: String,
    customAvatarUriString: String?,
    onSaveGroupClick: (String, String) -> Unit
) {
    if (showCreatePalFlow) {
        val localScope = rememberCoroutineScope()
        val context = androidx.compose.ui.platform.LocalContext.current
        var isSaving by remember { mutableStateOf(false) }

        androidx.activity.compose.BackHandler {
            if (!isCreatingPal && !isSaving) {
                onShowCreatePalFlowChange(false)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E1D22) else Color(0xFFE5E5EA))
                            .clickable(enabled = !isSaving) {
                                onIsCreatingPalChange(false)
                                onShowCreatePalFlowChange(false)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Pal",
                        fontFamily = OwnglyphFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = palTextLogoColor,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (!isCreatingPal && createPalStep == 1) {
                        val isFormValid = newPalName.trim().isNotEmpty()
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFormValid) accentColor else accentColor.copy(alpha = 0.5f)
                                )
                                .clickable(enabled = isFormValid && !isSaving) {
                                    onSaveGroupClick(newPalName, "")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_checkmark_custom),
                                contentDescription = "Save",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isCreatingPal) {
                    Text(
                        text = "creating pal$creationDots",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = textColor,
                        modifier = Modifier.offset(y = (-25).dp)
                    )
                } else if (createPalStep == 1) {
                    Text(
                        text = "→ create a pal",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "pal name",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = mutedTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newPalName,
                        onValueChange = onNewPalNameChange,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            color = textColor
                        ),
                        placeholder = {
                            Text(
                                text = "pal name",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                color = mutedTextColor.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = textColor.copy(alpha = 0.15f),
                            cursorColor = accentColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(createPalFocusRequester)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "pal size",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = mutedTextColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val row1 = listOf("2", "3", "4", "5", "6", "7")
                    val row2 = listOf("8", "9", "10")

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row1.forEach { size ->
                                val isSelected = size == newPalSize
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) accentColor else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) accentColor else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !isSaving) { onNewPalSizeChange(size) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = size,
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else textColor
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row2.forEach { size ->
                                val isSelected = size == newPalSize
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) accentColor else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) accentColor else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !isSaving) { onNewPalSizeChange(size) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = size,
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else textColor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "$newPalName :)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "code: #$generatedPalCode",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "share code →",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my Pal group $newPalName using code #$generatedPalCode !")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                            .padding(vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "done →",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                onShowCreatePalFlowChange(false)
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TripleDotMenuOverlay(
    showTripleDotMenu: Boolean,
    onShowTripleDotMenuChange: (Boolean) -> Unit,
    tripleDotScreen: TripleDotScreen,
    onTripleDotScreenChange: (TripleDotScreen) -> Unit,
    isDark: Boolean,
    customAvatarUriString: String?,
    accentColor: Color,
    currentDisplayName: String,
    textColor: Color,
    mutedTextColor: Color,
    selectedThemeColor: String,
    onSelectedThemeColorChange: (String) -> Unit,
    notificationInterval: String,
    onNotificationIntervalChange: (String) -> Unit,
    onChoosePhotoClick: () -> Unit,
    onDeletePhotoClick: () -> Unit,
    onShowEditNameDialogChange: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onTripleDotMenuBoundsChange: (Rect) -> Unit
) {
    val context = LocalContext.current
    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    if (showTripleDotMenu) {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.activity.compose.BackHandler {
                if (tripleDotScreen == TripleDotScreen.MAIN) {
                    onShowTripleDotMenuChange(false)
                } else if (tripleDotScreen == TripleDotScreen.COLOR_SELECTION || tripleDotScreen == TripleDotScreen.PHOTO_OPTIONS) {
                    onTripleDotScreenChange(TripleDotScreen.EDIT_PROFILE)
                } else {
                    onTripleDotScreenChange(TripleDotScreen.MAIN)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onShowTripleDotMenuChange(false)
                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                    }
            )

        val cardBgColor = if (isDark) Color.Black else PalBackground

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(290.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(cardBgColor)
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .onGloballyPositioned { coords ->
                        onTripleDotMenuBoundsChange(coords.boundsInRoot())
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                ) {
                    when (tripleDotScreen) {
                        TripleDotScreen.MAIN -> {
                            // Profile Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (customAvatarUriString != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    ) {
                                        UriImage(
                                            uriString = customAvatarUriString,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.smile_medium),
                                            contentDescription = "Profile Avatar",
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = currentDisplayName,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val mainOptions = listOf(
                                Triple("edit profile", "display name, profile photo, color") {
                                    onTripleDotScreenChange(TripleDotScreen.EDIT_PROFILE)
                                },
                                Triple("pal notifications", if (notificationInterval.isEmpty()) "off" else notificationInterval) {
                                    onTripleDotScreenChange(TripleDotScreen.PAL_NOTIFICATIONS)
                                },
                                Triple("settings", "log out, terms of service, delete account") {
                                    onTripleDotScreenChange(TripleDotScreen.SETTINGS)
                                },
                                Triple("feedback", "report bugs or request features") {
                                    onShowTripleDotMenuChange(false)
                                }
                            )

                            mainOptions.forEach { (title, description, onClick) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClick() }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onShowTripleDotMenuChange(false)
                                            onTripleDotScreenChange(TripleDotScreen.MAIN)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        TripleDotScreen.EDIT_PROFILE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "edit profile",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            val editOptions = listOf(
                                "display name" to {
                                    onShowTripleDotMenuChange(false)
                                    onShowEditNameDialogChange(true)
                                },
                                "profile photo" to {
                                    onTripleDotScreenChange(TripleDotScreen.PHOTO_OPTIONS)
                                },
                                "color" to {
                                    onTripleDotScreenChange(TripleDotScreen.COLOR_SELECTION)
                                }
                            )

                            editOptions.forEach { (title, onClick) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClick() }
                                        .padding(horizontal = 20.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
                                )
                            }
                        }
                        TripleDotScreen.PHOTO_OPTIONS -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "profile photo",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val photoOptions = listOf(
                                "choose photo" to {
                                    onChoosePhotoClick()
                                },
                                "delete" to {
                                    onDeletePhotoClick()
                                }
                            )

                            photoOptions.forEach { (title, onClick) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClick() }
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (title == "delete") Color(0xFFF35F38) else textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
                                )
                            }
                        }
                        TripleDotScreen.COLOR_SELECTION -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "color",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val colorOptions = listOf(
                                "yellow" to Color(0xFFFFE600),
                                "orange" to Color(0xFFFF6700),
                                "pink" to Color(0xFFFF007F),
                                "blue" to Color(0xFF00F0FF),
                                "purple" to Color(0xFFB000FF),
                                "red" to Color(0xFFFF073A)
                            )

                            colorOptions.forEach { (colorName, colorVal) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectedThemeColorChange(colorName)
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CustomRadioButton(
                                        selected = selectedThemeColor == colorName,
                                        color = colorVal,
                                        onClick = { onSelectedThemeColorChange(colorName) }
                                    )
                                    Text(
                                        text = colorName,
                                        fontSize = 17.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = colorVal,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
                                )
                            }
                        }
                        TripleDotScreen.PAL_NOTIFICATIONS -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "pal notifications",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val notificationOptions = listOf("every 1hr", "every 3hrs", "off")
                            notificationOptions.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (option != "off" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    postNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            }
                                            onNotificationIntervalChange(option)
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CustomRadioButton(
                                        selected = notificationInterval == option,
                                        color = textColor,
                                        onClick = {
                                            if (option != "off" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    postNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            }
                                            onNotificationIntervalChange(option)
                                        }
                                    )
                                    Text(
                                        text = option,
                                        fontSize = 17.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = textColor,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
                                )
                            }
                        }
                        TripleDotScreen.SETTINGS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "settings",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "v1.0",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    color = mutedTextColor,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val settingsOptions = listOf(
                                "log out" to {
                                    onShowTripleDotMenuChange(false)
                                    onSignOut()
                                },
                                "add backup email" to {
                                    onShowTripleDotMenuChange(false)
                                },
                                "terms of service" to {
                                    onShowTripleDotMenuChange(false)
                                },
                                "delete account" to {
                                    onShowTripleDotMenuChange(false)
                                    onDeleteAccount()
                                }
                            )

                            settingsOptions.forEach { (title, onClick) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClick() }
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (title == "delete account") Color(0xFFF35F38) else textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "cancel",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 17.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun PalsTabScreenContent(
    isDark: Boolean,
    palTextLogoColor: Color,
    accentColor: Color,
    customAvatarUriString: String?,
    isLoadingPals: Boolean,
    mutedTextColor: Color,
    createdPals: List<PalItem>,
    rotationAngle: Float,
    capturedVlogsPaths: List<String>,
    currentPlayingIndex: Int,
    capturedVlogsCaptions: List<String>,
    capturedVlogsTimes: List<String>,
    vlogPlaybackProgress: Float,
    vlogExoPlayer: androidx.media3.exoplayer.ExoPlayer,
    textColor: Color,
    allPalsMembers: Map<String, List<String>>,
    firstName: String,
    allPalsSubmissions: Map<String, List<SubmissionDbItem>>,
    currentUserId: String,
    currentDisplayName: String,
    circleNumBg: Color,
    circleNumText: Color,
    onPlusClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPalClick: (PalItem) -> Unit,
    onCameraClick: () -> Unit,
    onGlobalSearchTrigger: (String) -> Unit
) {
    PalGroupGridScreen(
        isDark = isDark,
        palTextLogoColor = palTextLogoColor,
        accentColor = accentColor,
        customAvatarUriString = customAvatarUriString,
        isLoadingPals = isLoadingPals,
        mutedTextColor = mutedTextColor,
        createdPals = createdPals,
        rotationAngle = rotationAngle,
        capturedVlogsPaths = capturedVlogsPaths,
        currentPlayingIndex = currentPlayingIndex,
        capturedVlogsCaptions = capturedVlogsCaptions,
        capturedVlogsTimes = capturedVlogsTimes,
        vlogPlaybackProgress = vlogPlaybackProgress,
        vlogExoPlayer = vlogExoPlayer,
        textColor = textColor,
        allPalsMembers = allPalsMembers,
        firstName = firstName,
        allPalsSubmissions = allPalsSubmissions,
        currentUserId = currentUserId,
        currentDisplayName = currentDisplayName,
        circleNumBg = circleNumBg,
        circleNumText = circleNumText,
        onPlusClick = onPlusClick,
        onProfileClick = onProfileClick,
        onPalClick = onPalClick,
        onCameraClick = onCameraClick
    )
}

@Composable
fun VlogArchiveCard(
    activePalSubmissions: List<SubmissionDbItem>,
    currentUserId: String,
    selectedDayOffset: Int,
    onSelectedDayOffsetChange: (Int) -> Unit,
    isDark: Boolean,
    accentColor: Color,
    selectedProfileColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    onDismiss: () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val parentPaddingPx = with(density) { 24.dp.roundToPx() }
    val targetPaddingPx = with(density) { 8.dp.roundToPx() }
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.now()) }

    val activeLocalDate = remember {
        val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
        if (now.hour < 4) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
    }

    val datesWithSubmissions = remember(activePalSubmissions, currentUserId) {
        activePalSubmissions
            .filter { it.userId == currentUserId }
            .mapNotNull { sub ->
                val instant = safeParseInstant(sub.createdAt)
                if (instant != null) {
                    val zdt = instant.atZone(java.time.ZoneId.systemDefault())
                    if (zdt.hour < 4) {
                        zdt.toLocalDate().minusDays(1)
                    } else {
                        zdt.toLocalDate()
                    }
                } else {
                    null
                }
            }
            .toSet()
    }

    GlassmorphicCard(
        modifier = Modifier
            .offset(y = 0.5.dp)
            .layout { measurable, constraints ->
                val targetWidth = screenWidthPx - (2 * targetPaddingPx)
                val newConstraints = constraints.copy(
                    minWidth = targetWidth,
                    maxWidth = targetWidth
                )
                val placeable = measurable.measure(newConstraints)
                val shiftPx = 16.dp.roundToPx()
                layout(placeable.width, placeable.height) {
                    placeable.place(targetPaddingPx - parentPaddingPx + shiftPx, 0)
                }
            },
        borderRadius = 28.dp,
        isDark = isDark,
        gradientColors = if (isDark) listOf(Color(0xFF161616), Color(0xFF161616)) else listOf(Color(0xFFF5F3EB), Color(0xFFF5F3EB)),
        borderColor = if (isDark) accentColor.copy(alpha = 0.3f) else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "history",
                fontFamily = BricolageVariableFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Prev Month",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { currentMonth = currentMonth.minusMonths(1) }
                )
                Text(
                    text = "${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)} ${currentMonth.year}",
                    fontFamily = RobotoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { currentMonth = currentMonth.plusMonths(1) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = mutedTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - firstDayOfWeek + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNum)
                                    val diff = java.time.temporal.ChronoUnit.DAYS.between(date, activeLocalDate).toInt()
                                    val isClickable = diff in 0..6
                                    val isSelected = isClickable && diff == selectedDayOffset
                                    val hasUserSub = datesWithSubmissions.contains(date)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) accentColor else Color.Transparent)
                                                .clickable(enabled = isClickable) {
                                                    onSelectedDayOffsetChange(diff)
                                                    onDismiss()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> if (isDark) Color.Black else Color.White
                                                    isClickable -> textColor
                                                    else -> textColor.copy(alpha = 0.3f)
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (hasUserSub) {
                                            Image(
                                                painter = painterResource(id = R.drawable.smile_small),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .rotate(180f),
                                                colorFilter = ColorFilter.tint(accentColor)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = "live",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            onSelectedDayOffsetChange(0)
                            onDismiss()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun VlogEmptyStateContent(
    params: VlogScreenContentParams,
    selectedProfileColor: Color
) {
    val pal = params.pal
    val isDark = params.isDark
    val accentColor = params.accentColor
    val textColor = params.textColor
    val currentDisplayName = params.currentDisplayName
    val customAvatarUriString = params.customAvatarUriString
    val onNavigateToCamera = params.onNavigateToCamera

    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val groupSize = pal.size.toIntOrNull() ?: 4
        val reservedDp = 80.dp + 15.dp + 16.dp + (9.dp * (groupSize - 1))
        val availableHeight = maxHeight - reservedDp
        val itemHeight = (availableHeight.value / groupSize).dp.coerceIn(100.dp, 130.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp) // leaves space for the top header
                .padding(horizontal = 8.dp) // EXACT width alignment with vlog box
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp), // reduced spacing by 3dp (now 9dp)
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp) // Move 1st box below by 15dp
                    .height(itemHeight)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA)) // charcoal in dark mode, grey in light mode
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val smileySizePx = with(density) { 50.dp.toPx() }

                val profileColors = remember {
                    listOf(
                        Color(0xFFFFE600), // yellow
                        Color(0xFFFF6700), // orange
                        Color(0xFFFF007F), // pink
                        Color(0xFF00F0FF), // blue
                        Color(0xFFB000FF), // purple
                        Color(0xFFFF073A)  // red
                    )
                }

                var groupPosX by remember { mutableStateOf(0f) }
                var groupPosY by remember { mutableStateOf(0f) }
                var groupSmileRotation by remember { mutableStateOf(0f) }
                var groupSmileColorIndex by remember { mutableStateOf(0) }

                LaunchedEffect(widthPx, heightPx) {
                    if (widthPx <= 0f || heightPx <= 0f) return@LaunchedEffect
                    groupPosX = (widthPx - smileySizePx) / 2f
                    groupPosY = (heightPx - smileySizePx) / 2f

                    val speed = with(density) { 100.dp.toPx() }
                    var vx = speed * 0.76f
                    var vy = speed * 0.65f

                    var lastTime = androidx.compose.runtime.withFrameNanos { it }

                    while (true) {
                        androidx.compose.runtime.withFrameNanos { time ->
                            val dt = (time - lastTime) / 1_000_000_000f
                            lastTime = time

                            val cappedDt = dt.coerceAtMost(0.1f)

                            var newX = groupPosX + vx * cappedDt
                            var newY = groupPosY + vy * cappedDt

                            var collided = false

                            val minX = 0f
                            val maxX = widthPx - smileySizePx
                            val minY = 0f
                            val maxY = heightPx - smileySizePx

                            if (maxX > 0f) {
                                if (newX <= minX) {
                                    newX = minX
                                    vx = -vx
                                    collided = true
                                } else if (newX >= maxX) {
                                    newX = maxX
                                    vx = -vx
                                    collided = true
                                }
                            }

                            if (maxY > 0f) {
                                if (newY <= minY) {
                                    newY = minY
                                    vy = -vy
                                    collided = true
                                } else if (newY >= maxY) {
                                    newY = maxY
                                    vy = -vy
                                    collided = true
                                }
                            }

                            groupPosX = newX
                            groupPosY = newY

                            groupSmileRotation = (groupSmileRotation + 120f * cappedDt) % 360f

                            if (collided) {
                                groupSmileColorIndex = (groupSmileColorIndex + 1) % profileColors.size
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .offset(
                            x = with(density) { groupPosX.toDp() },
                            y = with(density) { groupPosY.toDp() }
                        )
                        .rotate(groupSmileRotation)
                        .clip(CircleShape)
                        .background(profileColors[groupSmileColorIndex]),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.capture_smile),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 12.dp, start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (customAvatarUriString != null) {
                        UriImage(
                            uriString = customAvatarUriString,
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.smile_medium),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(180f)
                            )
                        }
                    }

                    val firstName = currentDisplayName.substringBefore(" ")
                    val vlogEmptyTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                    Text(
                        text = firstName,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = vlogEmptyTextColor
                    )
                }

                val now = java.time.LocalTime.now()
                val roundedHourStr = String.format("%02d", now.hour)

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val vlogEmptyTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(y = 2.5.dp)
                    ) {
                        Text(
                            text = roundedHourStr,
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 21.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = vlogEmptyTextColor
                        )
                        Text(
                            text = ":",
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 21.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = vlogEmptyTextColor,
                            modifier = Modifier.offset(y = (-1.5).dp)
                        )
                        Text(
                            text = "00",
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 21.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = vlogEmptyTextColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.35f)
                            )
                            .clickable { onNavigateToCamera() }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val vlogEmptyCaptureColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                        Text(
                            text = "tap to capture",
                            fontFamily = GoogleSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = vlogEmptyCaptureColor
                        )
                    }
                }
            }

            for (i in 1 until groupSize) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFE5E5EA))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable {
                            try {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: ${pal.code}")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Friend",
                            tint = textColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "invite a friend",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupExportMemberSlot(
    index: Int,
    groupMembers: List<String>,
    userFirstName: String,
    filteredSubmissions: List<SubmissionDbItem>,
    currentUserId: String,
    exportBackground: String,
    exportMissedText: String,
    context: android.content.Context,
    textColor: Color,
    pal: PalItem,
    cardHeightDp: Dp,
    isGrid: Boolean
) {
    val memberInfo = groupMembers.getOrNull(index)
    val memberParts = memberInfo?.split("|||")
    val (memberId, memberNameClean, memberAvatar) = if (memberParts != null && memberParts.size >= 2) {
        Triple(memberParts[0], memberParts[1], memberParts.getOrNull(2))
    } else {
        Triple(null, memberInfo, null)
    }
    val memberName = if (memberNameClean != null) {
        if (memberNameClean.contains("(You)")) userFirstName else memberNameClean
    } else {
        null
    }
    val isUser = memberId != null && memberId == currentUserId || 
                 (memberNameClean != null && (memberNameClean.contains("(You)") || memberNameClean == userFirstName))
    
    // Find all submissions for this member on the active day
    val memberSubs = if (isUser) {
        filteredSubmissions.filter { it.userId == currentUserId }
    } else if (memberId != null && memberId != "legacy_id") {
        filteredSubmissions.filter { it.userId == memberId }
    } else if (memberName != null) {
        filteredSubmissions.filter { 
            val cleanSubName = parseUserDisplayName(it.userDisplayName).first.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
            cleanSubName == memberName
        }
    } else {
        emptyList()
    }
    
    val sortedMemberSubs = remember(memberSubs) {
        memberSubs.mapNotNull { sub ->
            val parts = sub.imageUrl.split("|||")
            val path = parts.getOrNull(0) ?: ""
            if (path.isEmpty()) null else {
                var hour = 12
                if (!sub.createdAt.isNullOrEmpty()) {
                    try {
                        val instant = java.time.Instant.parse(sub.createdAt)
                        val localDateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        val rawHour = localDateTime.hour
                        hour = (rawHour - 4 + 24) % 24
                    } catch (e: Exception) {}
                }
                val timestamp = if (!sub.createdAt.isNullOrEmpty()) {
                    try { java.time.Instant.parse(sub.createdAt).toEpochMilli() } catch (e: Exception) { 0L }
                } else 0L
                Triple(sub, hour, timestamp)
            }
        }
        .groupBy { it.second }
        .map { entry -> entry.value.maxByOrNull { it.third }!! }
        .sortedBy { it.third }
        .map { it.first }
    }
    
    val videoPaths = remember(sortedMemberSubs) { sortedMemberSubs.map { it.imageUrl.split("|||").firstOrNull() ?: "" }.filter { it.isNotEmpty() } }
    val firstSub = sortedMemberSubs.firstOrNull()
    val captureTime = if (firstSub != null && !firstSub.createdAt.isNullOrEmpty()) {
        try {
            val instant = java.time.Instant.parse(firstSub.createdAt)
            val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
            val hr = zonedDateTime.hour
            String.format(java.util.Locale.US, "%02d:00", hr)
        } catch (e: Exception) {
            ""
        }
    } else {
        ""
    }
    val displayTimeText = remember {
        val now = java.time.LocalTime.now()
        String.format(java.util.Locale.US, "%02d:00", now.hour)
    }
    
    val cardShape = androidx.compose.ui.graphics.RectangleShape
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(cardShape)
            .background(if (videoPaths.isNotEmpty()) Color.Black else (if (exportBackground == "white") Color.White else Color.Black)),
        contentAlignment = Alignment.Center
    ) {
        if (videoPaths.isNotEmpty()) {
            @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
            val localPlayer = remember(videoPaths) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context)
                    .setRenderersFactory(
                        androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
                            setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
                            setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        }
                    )
                    .setMediaSourceFactory(
                        androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                            .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context))
                    )
                    .build().apply {
                        repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                        volume = 0f
                    }
            }
            DisposableEffect(localPlayer) {
                onDispose {
                    localPlayer.release()
                }
            }
            LaunchedEffect(videoPaths, localPlayer) {
                localPlayer.stop()
                localPlayer.clearMediaItems()
                val resolved = videoPaths.map { path ->
                    ensureVideoCached(context, path)
                }
                resolved.forEach { resolvedPath ->
                    if (resolvedPath.startsWith("http")) {
                        localPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(resolvedPath)))
                    } else {
                        val cleanPath = when {
                            resolvedPath.startsWith("file://") -> resolvedPath.substring(7)
                            else -> resolvedPath
                        }
                        val file = java.io.File(cleanPath)
                        if (file.exists()) {
                            localPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                        }
                    }
                }
                if (resolved.isNotEmpty()) {
                    localPlayer.prepare()
                }
                localPlayer.playWhenReady = true
                localPlayer.play()
            }
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                    view.apply {
                        player = localPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                        setBackgroundColor(android.graphics.Color.BLACK)
                        
                        fun applyVideoScale() {
                            val videoSize = localPlayer.videoSize
                            val videoWidth = videoSize.width
                            val videoHeight = videoSize.height
                            val textureView = getVideoSurfaceView() as? android.view.TextureView
                            if (textureView == null) {
                                postDelayed({ applyVideoScale() }, 100)
                                return
                            }
                            val containerWidth = width.toFloat()
                            val containerHeight = height.toFloat()
                            if (containerWidth > 0f && containerHeight > 0f && videoWidth > 0 && videoHeight > 0) {
                                val isPortrait = videoHeight > videoWidth
                                val rotatedWidth = if (isPortrait) videoHeight.toFloat() else videoWidth.toFloat()
                                val rotatedHeight = if (isPortrait) videoWidth.toFloat() else videoHeight.toFloat()
                                
                                val scale = java.lang.Math.max(containerWidth / rotatedWidth, containerHeight / rotatedHeight)
                                
                                val calculatedScaleX: Float
                                val calculatedScaleY: Float
                                val calculatedRotation: Float
                                if (isPortrait) {
                                    calculatedScaleX = (rotatedHeight * scale) / containerWidth
                                    calculatedScaleY = (rotatedWidth * scale) / containerHeight
                                    calculatedRotation = 270f
                                } else {
                                    calculatedScaleX = (rotatedWidth * scale) / containerWidth
                                    calculatedScaleY = (rotatedHeight * scale) / containerHeight
                                    calculatedRotation = 0f
                                }
                                
                                android.util.Log.d("PalVideoScale", "Reverted scale for localPlayer: video=${videoWidth}x${videoHeight}, container=${containerWidth}x${containerHeight}, scaleX=${calculatedScaleX}, scaleY=${calculatedScaleY}, rotation=${calculatedRotation}")
                                
                                textureView.pivotX = containerWidth / 2f
                                textureView.pivotY = containerHeight / 2f
                                textureView.scaleX = calculatedScaleX
                                textureView.scaleY = calculatedScaleY
                                textureView.rotation = calculatedRotation
                            } else {
                                postDelayed({ applyVideoScale() }, 100)
                            }
                        }

                        val layoutListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                            applyVideoScale()
                        }
                        addOnLayoutChangeListener(layoutListener)

                        localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                super.onVideoSizeChanged(videoSize)
                                applyVideoScale()
                            }
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                super.onPlaybackStateChanged(playbackState)
                                applyVideoScale()
                            }
                        })

                        applyVideoScale()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    if (view.player != localPlayer) {
                        view.player = localPlayer
                    }
                    if (!localPlayer.isPlaying && localPlayer.playbackState == androidx.media3.common.Player.STATE_READY) {
                        localPlayer.play()
                    }
                }
            )
            
            // Time text centered, size 20.sp, bold white Dela Gothic One
            val showTime = if (captureTime.isNotEmpty()) captureTime else displayTimeText
            val caption = firstSub?.imageUrl?.split("|||")?.getOrNull(1) ?: ""
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = showTime,
                    fontFamily = DelaGothicOneFontFamily,
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                if (caption.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = caption,
                        fontFamily = RobotoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        } else {
            // Missed state: Center-aligned vertical stack
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Scheduled time text: Dela Gothic One (20.sp), textColor (black or white)
                Text(
                    text = displayTimeText,
                    fontFamily = DelaGothicOneFontFamily,
                    fontSize = 15.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Missed text: custom text in blue color
                Text(
                    text = exportMissedText,
                    fontSize = 22.sp,
                    fontFamily = BricolageVariableFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F8CFF)
                )
            }
        }
    }
}

@Composable
fun VideoVlogBoxItem(
    videoUrl: String, 
    capturedTimeText: String, 
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Pull or create the persistent player instance
    val cachedPlayer = remember(videoUrl) { 
        VlogPlayerManager.getOrCreatePlayer(context, videoUrl) 
    }

    androidx.compose.runtime.DisposableEffect(videoUrl) {
        onDispose {
            VlogPlayerManager.releasePlayer(videoUrl)
        }
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        // Force the underlying player implementation to use a TextureView
                        // This natively prevents the black/white empty frame recycling glitch
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        try {
                            val textureView = javaClass.getDeclaredMethod("setSurfaceType", Int::class.javaPrimitiveType)
                            textureView.invoke(this, 2) // 2 = SURFACE_TYPE_TEXTURE_VIEW
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        
                        player = cachedPlayer
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    if (view.player != cachedPlayer) {
                        view.player = cachedPlayer
                    }
                    // Force video kickstart on tab re-entry
                    if (!cachedPlayer.isPlaying) {
                        cachedPlayer.play()
                    }
                }
            )
        }
        
        Text(
            text = capturedTimeText, 
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

}

@Composable
fun HomeScreenOverlays(
    showPlusMenu: Boolean,
    onShowPlusMenuChange: (Boolean) -> Unit,
    showTripleDotMenu: Boolean,
    onShowTripleDotMenuChange: (Boolean) -> Unit,
    showActivityScreen: Boolean,
    onShowActivityScreenChange: (Boolean) -> Unit,
    showCreatePalFlow: Boolean,
    onShowCreatePalFlowChange: (Boolean) -> Unit,
    showJoinPalFlow: Boolean,
    onShowJoinPalFlowChange: (Boolean) -> Unit,
    showEditNameDialog: Boolean,
    onShowEditNameDialogChange: (Boolean) -> Unit,
    tripleDotScreen: TripleDotScreen,
    onTripleDotScreenChange: (TripleDotScreen) -> Unit,
    editFirstName: String,
    onEditFirstNameChange: (String) -> Unit,
    editLastName: String,
    onEditLastNameChange: (String) -> Unit,
    editNameFocusRequester: FocusRequester,
    tripleDotMenuBounds: Rect?,
    onTripleDotMenuBoundsChange: (Rect?) -> Unit,
    createPalStep: Int,
    onCreatePalStepChange: (Int) -> Unit,
    joinPalCode: String,
    onJoinPalCodeChange: (String) -> Unit,
    newPalName: String,
    onNewPalNameChange: (String) -> Unit,
    newPalSize: String,
    onNewPalSizeChange: (String) -> Unit,
    generatedPalCode: String,
    onGeneratedPalCodeChange: (String) -> Unit,
    creationDots: String,
    onCreationDotsChange: (String) -> Unit,
    isCreatingPal: Boolean,
    onIsCreatingPalChange: (Boolean) -> Unit,
    createPalFocusRequester: FocusRequester,
    groupDatabase: androidx.compose.runtime.snapshots.SnapshotStateMap<String, PalItem>,
    createdPals: List<PalItem>,
    onCreatedPalsChange: (List<PalItem>) -> Unit,
    activeVlogPal: PalItem?,
    onActiveVlogPalChange: (PalItem?) -> Unit,
    currentUserId: String,
    currentDisplayName: String,
    onCurrentDisplayNameChange: (String) -> Unit,
    customAvatarUriString: String?,
    onCustomAvatarUriStringChange: (String?) -> Unit,
    notificationInterval: String,
    onNotificationIntervalChange: (String) -> Unit,
    selectedThemeColor: String,
    onSelectedThemeColorChange: (String) -> Unit,
    themeConfig: PalThemeConfig,
    accentColor: Color,
    palTextLogoColor: Color,
    backgroundColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    navBarBgColor: Color,
    overlayBackdropColor: Color,
    isDark: Boolean,
    photoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    sessionManager: com.finrein.pals.data.local.SessionManager,
    refreshPals: () -> Unit,
    refreshActivePalDetails: (String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    allPalsMessages: androidx.compose.runtime.snapshots.SnapshotStateMap<String, List<MessageDbItem>>,
    editNameBounds: Rect?,
    onEditNameBoundsChange: (Rect?) -> Unit,
    onSaveGroupClick: (String, String) -> Unit
) {
    // Plus (+) Menu Overlay
    PlusMenuOverlay(
        showPlusMenu = showPlusMenu,
        onShowPlusMenuChange = onShowPlusMenuChange,
        overlayBackdropColor = overlayBackdropColor,
        isDark = isDark,
        navBarBgColor = navBarBgColor,
        textColor = textColor,
        onCreatePalClick = {
            onNewPalNameChange("")
            onNewPalSizeChange("4")
            onCreatePalStepChange(1)
            onShowCreatePalFlowChange(true)
        },
        onJoinPalClick = {
            onJoinPalCodeChange("")
            onShowJoinPalFlowChange(true)
        }
    )

    // Triple Dot (...) Menu Overlay
    TripleDotMenuOverlay(
        showTripleDotMenu = showTripleDotMenu,
        onShowTripleDotMenuChange = onShowTripleDotMenuChange,
        tripleDotScreen = tripleDotScreen,
        onTripleDotScreenChange = onTripleDotScreenChange,
        isDark = isDark,
        customAvatarUriString = customAvatarUriString,
        accentColor = accentColor,
        currentDisplayName = currentDisplayName,
        textColor = textColor,
        mutedTextColor = mutedTextColor,
        selectedThemeColor = selectedThemeColor,
        onSelectedThemeColorChange = onSelectedThemeColorChange,
        notificationInterval = notificationInterval,
        onNotificationIntervalChange = onNotificationIntervalChange,
        onChoosePhotoClick = {
            onShowTripleDotMenuChange(false)
            photoPickerLauncher.launch("image/*")
        },
        onDeletePhotoClick = {
            onShowTripleDotMenuChange(false)
            sessionManager.saveAvatarUri(null)
            onCustomAvatarUriStringChange(null)
        },
        onShowEditNameDialogChange = onShowEditNameDialogChange,
        onSignOut = { VlogPlayerManager.clearAll(); onSignOut() },
        onDeleteAccount = { VlogPlayerManager.clearAll(); onDeleteAccount() },
        onTripleDotMenuBoundsChange = onTripleDotMenuBoundsChange
    )

    // Activity Screen Overlay
    ActivityScreenOverlay(
        showActivityScreen = showActivityScreen,
        onShowActivityScreenChange = onShowActivityScreenChange,
        backgroundColor = backgroundColor,
        textColor = textColor,
        mutedTextColor = mutedTextColor
    )

    // Create Pal Flow Screen
    CreatePalDialogOverlay(
        showCreatePalFlow = showCreatePalFlow,
        onShowCreatePalFlowChange = onShowCreatePalFlowChange,
        isCreatingPal = isCreatingPal,
        onIsCreatingPalChange = onIsCreatingPalChange,
        createPalStep = createPalStep,
        onCreatePalStepChange = onCreatePalStepChange,
        newPalName = newPalName,
        onNewPalNameChange = onNewPalNameChange,
        newPalSize = newPalSize,
        onNewPalSizeChange = onNewPalSizeChange,
        generatedPalCode = generatedPalCode,
        creationDots = creationDots,
        createdPals = createdPals,
        onCreatedPalsChange = onCreatedPalsChange,
        currentUserId = currentUserId,
        groupDatabase = groupDatabase,
        createPalFocusRequester = createPalFocusRequester,
        isDark = isDark,
        accentColor = accentColor,
        textColor = textColor,
        mutedTextColor = mutedTextColor,
        palTextLogoColor = palTextLogoColor,
        backgroundColor = backgroundColor,
        supabaseClient = supabaseClient,
        currentDisplayName = currentDisplayName,
        customAvatarUriString = customAvatarUriString,
        onSaveGroupClick = onSaveGroupClick
    )

    // Join Pal Dialog Flow (Overlay Card at bottom / center based on focus)
    JoinPalDialogOverlay(
        showJoinPalFlow = showJoinPalFlow,
        onShowJoinPalFlowChange = onShowJoinPalFlowChange,
        joinPalCode = joinPalCode,
        onJoinPalCodeChange = { onJoinPalCodeChange(it.lowercase(java.util.Locale.ROOT)) },
        isDark = isDark,
        accentColor = accentColor,
        currentUserId = currentUserId,
        createdPals = createdPals,
        onCreatedPalsChange = onCreatedPalsChange,
        refreshPals = refreshPals,
        supabaseClient = supabaseClient,
        currentDisplayName = currentDisplayName,
        customAvatarUriString = customAvatarUriString
    )

    // Edit Name Dialog Flow (Centered Screen Overlay)
    EditNameDialogOverlay(
        showEditNameDialog = showEditNameDialog,
        onShowEditNameDialogChange = onShowEditNameDialogChange,
        isDark = isDark,
        accentColor = accentColor,
        textColor = textColor,
        mutedTextColor = mutedTextColor,
        editFirstName = editFirstName,
        onEditFirstNameChange = onEditFirstNameChange,
        editLastName = editLastName,
        onEditLastNameChange = onEditLastNameChange,
        editNameFocusRequester = editNameFocusRequester,
        onEditNameBoundsChange = onEditNameBoundsChange,
        currentDisplayName = currentDisplayName,
        onCurrentDisplayNameChange = onCurrentDisplayNameChange,
        useDarkTextOnAccent = themeConfig.useDarkTextOnAccent
    )
}





