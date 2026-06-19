package com.finrein.pals.presentation.home

import android.os.Build
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import java.time.LocalTime

@Serializable
data class PalDbItem(
    val code: String,
    val name: String,
    val size: String,
    @SerialName("is_vlog") val isVlog: Boolean,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserPalMapping(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("pal_code") val palCode: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SubmissionDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_display_name") val userDisplayName: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class MessageDbItem(
    val id: String? = null,
    @SerialName("pal_code") val palCode: String,
    @SerialName("sender_name") val senderName: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null
)

data class PalItem(
    val name: String,
    val size: String,
    val code: String,
    val isVlog: Boolean = false,
    val isCreator: Boolean = true
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
                val uri = android.net.Uri.parse(uriString)
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
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

fun getVlogLocalDate(path: String): java.time.LocalDate? {
    val regex = Regex("\\d{13}")
    val match = regex.find(path)
    if (match != null) {
        try {
            val millis = match.value.toLong()
            val instant = java.time.Instant.ofEpochMilli(millis)
            return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            // ignore
        }
    }
    val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
    val file = java.io.File(cleanPath)
    if (file.exists()) {
        val lastModified = file.lastModified()
        val instant = java.time.Instant.ofEpochMilli(lastModified)
        return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }
    return null
}

fun getSubmissionLocalDate(sub: SubmissionDbItem): java.time.LocalDate? {
    if (!sub.createdAt.isNullOrEmpty()) {
        try {
            val instant = java.time.Instant.parse(sub.createdAt)
            return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            // fallback
        }
    }
    val parts = sub.imageUrl.split("|||")
    val path = parts.getOrNull(0) ?: ""
    return getVlogLocalDate(path)
}


@Composable
fun HomeScreen(
    user: com.finrein.pals.domain.model.User?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("vlog_prefs", android.content.Context.MODE_PRIVATE) }

    val currentUserId = remember(user) { user?.id ?: "" }
    val sessionManager = remember { com.finrein.pals.data.local.SessionManager(context) }
    var onboardingFlowStep by rememberSaveable {
        mutableStateOf(
            if (sessionManager.isOnboardingCompleted()) 6 else 1
        )
    }
    val parsedName = remember(user) { parseName(user?.email, user?.displayName) }
    var onboardingFirstName by rememberSaveable { mutableStateOf(parsedName.first) }
    var onboardingLastName by rememberSaveable { mutableStateOf(parsedName.second) }

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
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            sessionManager.saveAvatarUri(uri.toString())
            customAvatarUriString = uri.toString()
        }
    }
    var isLoadingPals by remember { mutableStateOf(sessionManager.isFirstLogin()) }

    var selectedTab by rememberSaveable { mutableStateOf("pals") } // "camera" or "pals"
    var isRecordingCamera by remember { mutableStateOf(false) }



    LaunchedEffect(selectedTab) {
        if (selectedTab == "pals" && sessionManager.isFirstLogin()) {
            isLoadingPals = true
            delay(500)
            isLoadingPals = false
            sessionManager.setFirstLogin(false)
        } else {
            isLoadingPals = false
        }
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

    LaunchedEffect(onboardingFlowStep) {
        if (onboardingFlowStep == 3) {
            kotlinx.coroutines.delay(2000)
            onboardingFlowStep = 4
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

    LaunchedEffect(isCreatingPal) {
        if (isCreatingPal) {
            val supabaseClient = com.finrein.pals.PALApplication.supabase
            val startTime = System.currentTimeMillis()
            var uniqueCode = ""
            var isUnique = false
            try {
                var attempts = 0
                while (!isUnique && attempts < 10) {
                    val candidate = (1..5).map { ('a'..'z').random() }.joinToString("")
                    val existing = supabaseClient.postgrest.from("pals")
                        .select {
                            filter {
                                eq("code", candidate)
                            }
                        }
                        .decodeList<PalDbItem>()
                    if (existing.isEmpty()) {
                        uniqueCode = candidate
                        isUnique = true
                    }
                    attempts++
                }
                if (!isUnique) {
                    uniqueCode = (1..5).map { ('a'..'z').random() }.joinToString("")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uniqueCode = (1..5).map { ('a'..'z').random() }.joinToString("")
            }
            generatedPalCode = uniqueCode

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = 1800 - elapsed
            if (remaining > 0) {
                val animStart = System.currentTimeMillis()
                while (System.currentTimeMillis() - animStart < remaining) {
                    creationDots = ""
                    kotlinx.coroutines.delay(200)
                    creationDots = "."
                    kotlinx.coroutines.delay(200)
                    creationDots = ".."
                    kotlinx.coroutines.delay(200)
                    creationDots = "..."
                    kotlinx.coroutines.delay(200)
                }
            } else {
                kotlinx.coroutines.delay(100)
            }
            createPalStep = 2
            isCreatingPal = false
        }
    }

    var createdPals by rememberSaveable(stateSaver = PalItemListSaver) {
        val saved = sharedPrefs.getString("created_pals", "") ?: ""
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
        mutableStateOf(initialList)
    }

    LaunchedEffect(createdPals) {
        val serialized = createdPals.joinToString(";;;") { "${it.name.replace(":", "\\:")}:${it.size}:${it.code}:${it.isVlog}:${it.isCreator}" }
        sharedPrefs.edit().putString("created_pals", serialized).apply()
    }

    val groupDatabase = remember {
        mutableStateMapOf<String, PalItem>().apply {
            put("hello9", PalItem(name = "Hi", size = "2", code = "hello9", isVlog = false, isCreator = false))
            put("abcd12", PalItem(name = "Cool Group", size = "4", code = "abcd12", isVlog = false, isCreator = false))
        }
    }

    var selectedThemeColor by rememberSaveable { mutableStateOf("yellow") }
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

    var tripleDotScreen by rememberSaveable { mutableStateOf(TripleDotScreen.MAIN) }
    var showEditNameDialog by rememberSaveable { mutableStateOf(false) }
    var notificationInterval by rememberSaveable { mutableStateOf("off") }
    var userPin by rememberSaveable { mutableStateOf("4A2D8B") }

    // Vlog and Group screen states
    var activeVlogPal by rememberSaveable(stateSaver = PalItemSaverNullable) { mutableStateOf<PalItem?>(null) }
    var showingCapturedPreview by rememberSaveable { mutableStateOf(false) }
    var capturedVideoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var capturedVideoDuration by rememberSaveable { mutableStateOf(2000L) }
    var capturedCaptionText by rememberSaveable { mutableStateOf("") }
    var capturedVideoTimeText by rememberSaveable { mutableStateOf("") }
    var isMuted by rememberSaveable { mutableStateOf(false) }

    val deletedVlogsKey = "deleted_vlog_paths_$currentUserId"
    val initialDeleted = remember(deletedVlogsKey) {
        val saved = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
        if (saved.isEmpty()) emptySet<String>() else saved.split(";;;").toSet()
    }

    var capturedVlogsPaths by remember(deletedVlogsKey) {
        val savedPaths = sharedPrefs.getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredPathsList = validIndices.map { paths[it] }
        mutableStateOf(ArrayList(filteredPathsList))
    }
    var capturedVlogsTimes by remember(deletedVlogsKey) {
        val savedPaths = sharedPrefs.getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedTimes = sharedPrefs.getString("vlog_times", "") ?: ""
        val times = if (savedTimes.isEmpty()) emptyList() else savedTimes.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredTimesList = validIndices.map { times.getOrNull(it) ?: "12:00" }
        mutableStateOf(ArrayList(filteredTimesList))
    }
    var capturedVlogsCaptions by remember(deletedVlogsKey) {
        val savedPaths = sharedPrefs.getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedCaptions = sharedPrefs.getString("vlog_captions", "") ?: ""
        val captions = if (savedCaptions.isEmpty()) emptyList() else savedCaptions.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredCaptionsList = validIndices.map { captions.getOrNull(it) ?: "" }
        mutableStateOf(ArrayList(filteredCaptionsList))
    }
    var capturedVlogsDurations by remember(deletedVlogsKey) {
        val savedPaths = sharedPrefs.getString("vlog_paths", "") ?: ""
        val paths = if (savedPaths.isEmpty()) emptyList() else savedPaths.split(";;;")
        val savedDurations = sharedPrefs.getString("vlog_durations", "") ?: ""
        val durations = if (savedDurations.isEmpty()) emptyList() else savedDurations.split(";;;")
        val validIndices = paths.indices.filter { idx -> paths[idx] !in initialDeleted }
        val filteredDurationsList = validIndices.map { durations.getOrNull(it) ?: "2000" }
        mutableStateOf(ArrayList(filteredDurationsList))
    }
    var currentPlayingIndex by remember { mutableStateOf(0) }
    var vlogPlaybackProgress by remember { mutableStateOf(0f) }
    var showVlogDropdownMenu by rememberSaveable { mutableStateOf(false) }
    var showVlogChatScreen by rememberSaveable { mutableStateOf(false) }
    var showEditPalFlow by rememberSaveable { mutableStateOf(false) }
    var showDeletePalDialog by rememberSaveable { mutableStateOf(false) }
    var showLeavePalDialog by rememberSaveable { mutableStateOf(false) }
    var showVlogExportDialog by rememberSaveable { mutableStateOf(false) }
    val palPalsCount = remember { mutableStateMapOf<String, Int>() }
    val palMessages = remember { mutableStateMapOf<String, List<MessageDbItem>>() }
    val allPalsSubmissions = remember { mutableStateMapOf<String, List<SubmissionDbItem>>() }
    val allPalsMessages = remember { mutableStateMapOf<String, List<MessageDbItem>>() }
    val allPalsMembers = remember { mutableStateMapOf<String, List<String>>() }
    var selectedDayOffset by remember { mutableStateOf(0) }

    val palReactions = remember { mutableStateMapOf<String, String>() }
    var activeReplyPreviewPath by remember { mutableStateOf<String?>(null) }
    var activeReactionPreview by remember { mutableStateOf<Pair<String, String>?>(null) }

    val targetDate = remember(selectedDayOffset) {
        java.time.LocalDate.now().minusDays(selectedDayOffset.toLong())
    }

    val activePalSubmissions = remember(activeVlogPal, capturedVlogsPaths, capturedVlogsTimes, capturedVlogsCaptions, capturedVlogsDurations, allPalsSubmissions) {
        val pal = activeVlogPal
        if (pal == null || pal.code == "vlog") {
            capturedVlogsPaths.mapIndexed { idx, path ->
                val caption = capturedVlogsCaptions.getOrNull(idx) ?: ""
                val duration = capturedVlogsDurations.getOrNull(idx) ?: "2000"
                val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
                val file = java.io.File(cleanPath)
                val createdAtStr = if (file.exists()) {
                    java.time.Instant.ofEpochMilli(file.lastModified()).toString()
                } else {
                    java.time.Instant.now().toString()
                }
                SubmissionDbItem(
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
            getSubmissionLocalDate(sub) == targetDate
        }
    }

    val filteredPaths = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(0) ?: ""
        }
    }

    val todaySubmissionsMap = remember(allPalsSubmissions) {
        allPalsSubmissions.mapValues { (_, subs) ->
            subs.filter { sub -> getSubmissionLocalDate(sub) == java.time.LocalDate.now() }
        }
    }

    val todayVlogPaths = remember(capturedVlogsPaths, capturedVlogsTimes, capturedVlogsCaptions, capturedVlogsDurations) {
        val today = java.time.LocalDate.now()
        capturedVlogsPaths.mapIndexedNotNull { idx, path ->
            val caption = capturedVlogsCaptions.getOrNull(idx) ?: ""
            val duration = capturedVlogsDurations.getOrNull(idx) ?: "2000"
            val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
            val file = java.io.File(cleanPath)
            val createdAtStr = if (file.exists()) {
                java.time.Instant.ofEpochMilli(file.lastModified()).toString()
            } else {
                java.time.Instant.now().toString()
            }
            val sub = SubmissionDbItem(
                palCode = "vlog",
                userId = currentUserId,
                userDisplayName = currentDisplayName,
                imageUrl = "$path|||$caption|||$duration",
                createdAt = createdAtStr
            )
            if (getSubmissionLocalDate(sub) == today) path else null
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

    val filteredDurations = remember(filteredSubmissions) {
        filteredSubmissions.map { sub ->
            sub.imageUrl.split("|||").getOrNull(2)?.toLongOrNull() ?: 2000L
        }
    }

    val vlogExoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
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
        filteredPaths.forEach { path ->
            val cleanPath = when {
                path.startsWith("file://") -> path.substring(7)
                else -> path
            }
            val fileTarget = java.io.File(cleanPath)
            if (fileTarget.exists() && fileTarget.length() > 0) {
                val targetUri = android.net.Uri.fromFile(fileTarget)
                vlogExoPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
            }
        }
        if (filteredPaths.isNotEmpty()) {
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
        showingCapturedPreview,
        lastPhysicalIsRotated
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
                    (orientation in 45..135) || (orientation in 225..315) -> true
                    (orientation < 10) || (orientation > 350) -> false
                    else -> null
                }

                if (currentIsRotated != null && currentIsRotated != lastPhysicalIsRotated) {
                    if (lastPhysicalIsRotated != null) {
                        if (currentIsRotated && selectedTab == "pals") {
                            selectedTab = "camera"
                        } else if (!currentIsRotated && selectedTab == "camera") {
                            selectedTab = "pals"
                        }
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

    val supabaseClient = remember { com.finrein.pals.PALApplication.supabase }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshPals() {
        if (currentUserId.isEmpty()) return
        coroutineScope.launch {
            try {
                isRefreshing = true
                val mappings = supabaseClient.postgrest.from("user_pals")
                    .select {
                        filter {
                            eq("user_id", currentUserId)
                        }
                    }
                    .decodeList<UserPalMapping>()
                
                val palCodes = mappings.map { it.palCode }
                val defaultVlog = PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true)
                if (palCodes.isNotEmpty()) {
                    val palsFromDb = supabaseClient.postgrest.from("pals")
                        .select {
                            filter {
                                isIn("code", palCodes)
                            }
                        }
                        .decodeList<PalDbItem>()
                    
                    val allGroupMappings = supabaseClient.postgrest.from("user_pals")
                        .select {
                            filter {
                                isIn("pal_code", palCodes)
                            }
                        }
                        .decodeList<UserPalMapping>()
                    val counts = allGroupMappings.groupBy { it.palCode }.mapValues { it.value.size }
                    counts.forEach { (code, count) ->
                        palPalsCount[code] = count
                    }

                    val mappedPals = palsFromDb.map { dbPal ->
                        PalItem(
                            name = dbPal.name,
                            size = dbPal.size,
                            code = dbPal.code,
                            isVlog = dbPal.isVlog,
                            isCreator = dbPal.creatorId == currentUserId
                        )
                    }
                    val combinedList = (createdPals + listOf(defaultVlog) + mappedPals).distinctBy { it.code }
                    createdPals = combinedList
                } else {
                    val combinedList = (createdPals + listOf(defaultVlog)).distinctBy { it.code }
                    createdPals = combinedList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refreshVlogs() {
        if (currentUserId.isEmpty()) return
        coroutineScope.launch {
            try {
                val dbSubmissions = supabaseClient.postgrest.from("submissions")
                    .select {
                        filter {
                            eq("pal_code", "vlog")
                            eq("user_id", currentUserId)
                        }
                    }
                    .decodeList<SubmissionDbItem>()
                
                val savedDeleted = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
                val currentDeleted = if (savedDeleted.isEmpty()) emptySet<String>() else savedDeleted.split(";;;").toSet()

                val sorted = dbSubmissions
                    .filterNot { sub ->
                        val parts = sub.imageUrl.split("|||")
                        val path = parts.getOrNull(0) ?: ""
                        path in currentDeleted || sub.imageUrl in currentDeleted
                    }
                    .sortedByDescending { it.id ?: "" }
                
                val paths = ArrayList<String>()
                val times = ArrayList<String>()
                val captions = ArrayList<String>()
                val durations = ArrayList<String>()
                
                sorted.forEach { sub ->
                    val parts = sub.imageUrl.split("|||")
                    val path = parts.getOrNull(0) ?: ""
                    val caption = parts.getOrNull(1) ?: ""
                    val duration = parts.getOrNull(2) ?: "2000"
                    
                    paths.add(path)
                    val timePart = if (!sub.createdAt.isNullOrEmpty()) {
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
                    times.add(timePart)
                    captions.add(caption)
                    durations.add(duration)
                }
                
                capturedVlogsPaths = paths
                capturedVlogsTimes = times
                capturedVlogsCaptions = captions
                capturedVlogsDurations = durations
                
                sharedPrefs.edit().apply {
                    putString("vlog_paths", paths.joinToString(";;;"))
                    putString("vlog_times", times.joinToString(";;;"))
                    putString("vlog_captions", captions.joinToString(";;;"))
                    putString("vlog_durations", durations.joinToString(";;;"))
                    apply()
                }
                
                vlogExoPlayer.stop()
                vlogExoPlayer.clearMediaItems()
                paths.forEach { path ->
                    val cleanPath = when {
                        path.startsWith("file://") -> path.substring(7)
                        else -> path
                    }
                    val fileTarget = java.io.File(cleanPath)
                    if (fileTarget.exists() && fileTarget.length() > 0) {
                        val targetUri = android.net.Uri.fromFile(fileTarget)
                        vlogExoPlayer.addMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                    }
                }
                if (paths.isNotEmpty()) {
                    vlogExoPlayer.prepare()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshActivePalDetails(palCode: String) {
        if (currentUserId.isEmpty() || palCode == "vlog") return
        coroutineScope.launch {
            try {
                val dbSubmissions = supabaseClient.postgrest.from("submissions")
                    .select {
                        filter {
                            eq("pal_code", palCode)
                        }
                    }
                    .decodeList<SubmissionDbItem>()
                val savedDeleted = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
                val currentDeleted = if (savedDeleted.isEmpty()) emptySet<String>() else savedDeleted.split(";;;").toSet()
                val filteredSubmissions = dbSubmissions.filterNot { sub ->
                    val parts = sub.imageUrl.split("|||")
                    val path = parts.getOrNull(0) ?: ""
                    path in currentDeleted || sub.imageUrl in currentDeleted
                }
                allPalsSubmissions[palCode] = filteredSubmissions
                
                val dbMessages = supabaseClient.postgrest.from("messages")
                    .select {
                        filter {
                            eq("pal_code", palCode)
                        }
                    }
                    .decodeList<MessageDbItem>()
                allPalsMessages[palCode] = dbMessages
                
                val userFirstName = currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                val names = (filteredSubmissions.map { it.userDisplayName } + dbMessages.map { it.senderName } + listOf(currentDisplayName))
                    .map { name ->
                        if (name == currentDisplayName) {
                            userFirstName
                        } else {
                            name.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        }
                    }
                    .distinct()
                    .filter { it.isNotEmpty() }
                allPalsMembers[palCode] = names
                palPalsCount[palCode] = filteredSubmissions.map { it.userId }.distinct().size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshMessages(palCode: String) {
        if (currentUserId.isEmpty() || palCode == "vlog") return
        coroutineScope.launch {
            try {
                val dbMessages = supabaseClient.postgrest.from("messages")
                    .select {
                        filter {
                            eq("pal_code", palCode)
                        }
                        order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    }
                    .decodeList<MessageDbItem>()
                palMessages[palCode] = dbMessages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            refreshPals()
            refreshVlogs()
        }
    }

    LaunchedEffect(createdPals, currentDisplayName) {
        createdPals.forEach { pal ->
            if (!pal.isVlog) {
                refreshActivePalDetails(pal.code)
            }
        }
    }

    LaunchedEffect(activeVlogPal, showVlogChatScreen) {
        val pal = activeVlogPal
        if (pal != null && pal.code != "vlog") {
            refreshActivePalDetails(pal.code)
            refreshMessages(pal.code)
            
            if (showVlogChatScreen) {
                while (true) {
                    delay(3000)
                    refreshMessages(pal.code)
                }
            }
        }
    }
    var isCapturingPal by rememberSaveable { mutableStateOf(false) }
    var capturingProgress by rememberSaveable { mutableStateOf(0.0f) }
    var vlogMenuExpandedMembers by rememberSaveable { mutableStateOf(false) }
    var vlogMenuExpandedSettings by rememberSaveable { mutableStateOf(false) }
    var editPalName by rememberSaveable { mutableStateOf("") }
    var editPalSize by rememberSaveable { mutableStateOf("3") }
    var isEditingPalLoading by rememberSaveable { mutableStateOf(false) }
    var editPalDots by rememberSaveable { mutableStateOf("") }

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
                coroutineScope.launch {
                    try {
                        val delimiterString = "${capturedVideoPath ?: "dummy_image"}|||||2000"
                        val newSubmission = SubmissionDbItem(
                            palCode = pal.code,
                            userId = currentUserId,
                            userDisplayName = currentDisplayName,
                            imageUrl = delimiterString
                        )
                        supabaseClient.postgrest.from("submissions").insert(newSubmission)
                        if (pal.code != "vlog") {
                            refreshActivePalDetails(pal.code)
                        } else {
                            refreshVlogs()
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
        if (isDark) Color(0xFF181513) else Color(0xFFFCF6ED)
    } else {
        if (isDark) Color(0xFF121212) else PalBackground
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(screenCornerRadius))
            .background(currentBackgroundColor)
            .border(2.dp, currentBorderColor, RoundedCornerShape(screenCornerRadius))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val screenContent = @Composable {
            if (onboardingFlowStep < 6) {
                when (onboardingFlowStep) {
                    1 -> NameInputScreen(
                        firstName = onboardingFirstName,
                        onFirstNameChange = { onboardingFirstName = it },
                        lastName = onboardingLastName,
                        onLastNameChange = { onboardingLastName = it },
                        onNext = { onboardingFlowStep = 2 },
                        onCancel = { onSignOut() },
                        isDark = isDark,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                    2 -> NameConfirmScreen(
                        firstName = onboardingFirstName,
                        lastName = onboardingLastName,
                        onContinue = {
                            val newName = "$onboardingFirstName $onboardingLastName".trim()
                            currentDisplayName = newName
                            user?.let {
                                sessionManager.saveUser(it.copy(displayName = newName))
                            }
                            onboardingFlowStep = 3
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
                            sessionManager.setOnboardingCompleted(true)
                            onboardingFlowStep = 6
                        },
                        textColor = textColor,
                        mutedTextColor = mutedTextColor
                    )
                }
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
                            if (code != "vlog") {
                                val newMessage = MessageDbItem(
                                    palCode = code,
                                    senderName = currentDisplayName,
                                    content = msg
                                )
                                coroutineScope.launch {
                                    try {
                                        supabaseClient.postgrest.from("messages").insert(newMessage)
                                        refreshMessages(code)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        val localMsg = MessageDbItem(
                                            palCode = code,
                                            senderName = currentDisplayName,
                                            content = msg
                                        )
                                        palMessages[code] = (palMessages[code] ?: emptyList()) + localMsg
                                    }
                                }
                            } else {
                                val localMsg = MessageDbItem(
                                    palCode = code,
                                    senderName = currentDisplayName,
                                    content = msg
                                )
                                palMessages[code] = (palMessages[code] ?: emptyList()) + localMsg
                            }
                        },
                        currentDisplayName = currentDisplayName,
                        currentUserId = currentUserId,
                        onDeletePal = {
                            val p = activeVlogPal
                            if (p != null) {
                                createdPals = createdPals.filterNot { it.code == p.code }
                                if (groupDatabase.containsKey(p.code)) {
                                    groupDatabase.remove(p.code)
                                }
                                coroutineScope.launch {
                                    try {
                                        supabaseClient.postgrest.from("user_pals").delete {
                                            filter {
                                                eq("pal_code", p.code)
                                                eq("user_id", currentUserId)
                                            }
                                        }
                                        if (p.isCreator) {
                                            supabaseClient.postgrest.from("pals").delete {
                                                filter {
                                                    eq("code", p.code)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                activeVlogPal = null
                            }
                        },
                        onLeavePal = {
                            val p = activeVlogPal
                            if (p != null) {
                                createdPals = createdPals.filterNot { it.code == p.code }
                                coroutineScope.launch {
                                    try {
                                        supabaseClient.postgrest.from("user_pals").delete {
                                            filter {
                                                eq("pal_code", p.code)
                                                eq("user_id", currentUserId)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                activeVlogPal = null
                            }
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
                            if (indexToDelete in filteredPaths.indices) {
                                val deletedPath = filteredPaths[indexToDelete]
                                val palCode = activeVlogPal?.code ?: "vlog"
                                
                                if (palCode != "vlog") {
                                    // GROUP PAL DELETION
                                    // 1. Add to local deleted blacklist in sharedPrefs
                                    val savedDeleted = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
                                    val currentDeleted = if (savedDeleted.isEmpty()) mutableSetOf<String>() else savedDeleted.split(";;;").toMutableSet()
                                    
                                    val cleanPath = when {
                                        deletedPath.startsWith("file://") -> deletedPath.substring(7)
                                        else -> deletedPath
                                    }
                                    currentDeleted.add(deletedPath)
                                    currentDeleted.add(cleanPath)
                                    if (!deletedPath.startsWith("file://")) {
                                        currentDeleted.add("file://$deletedPath")
                                    }
                                    sharedPrefs.edit().putString(deletedVlogsKey, currentDeleted.joinToString(";;;")).apply()

                                    // 2. Remove from allPalsSubmissions
                                    val currentSubs = allPalsSubmissions[palCode]
                                    if (currentSubs != null) {
                                        val updatedSubs = currentSubs.filterNot { 
                                            val pathPart = it.imageUrl.split("|||").firstOrNull() ?: ""
                                            pathPart == deletedPath || it.imageUrl == deletedPath
                                        }
                                        allPalsSubmissions[palCode] = updatedSubs
                                    }
                                    
                                    // 3. Delete from Supabase
                                    coroutineScope.launch {
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
                                            refreshActivePalDetails(palCode)
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
                                        
                                        sharedPrefs.edit().apply {
                                            putString("vlog_paths", updatedPaths.joinToString(";;;"))
                                            putString("vlog_times", updatedTimes.joinToString(";;;"))
                                            putString("vlog_captions", updatedCaptions.joinToString(";;;"))
                                            putString("vlog_durations", updatedDurations.joinToString(";;;"))
                                            apply()
                                        }
                                        
                                        // Add to local deleted blacklist in sharedPrefs
                                        val savedDeleted = sharedPrefs.getString(deletedVlogsKey, "") ?: ""
                                        val currentDeleted = if (savedDeleted.isEmpty()) mutableSetOf<String>() else savedDeleted.split(";;;").toMutableSet()
                                        
                                        val cleanPath = when {
                                            deletedPath.startsWith("file://") -> deletedPath.substring(7)
                                            else -> deletedPath
                                        }
                                        currentDeleted.add(deletedPath)
                                        currentDeleted.add(cleanPath)
                                        if (!deletedPath.startsWith("file://")) {
                                            currentDeleted.add("file://$deletedPath")
                                        }
                                        sharedPrefs.edit().putString(deletedVlogsKey, currentDeleted.joinToString(";;;")).apply()

                                        capturedVlogsPaths = updatedPaths
                                        capturedVlogsTimes = updatedTimes
                                        capturedVlogsCaptions = updatedCaptions
                                        capturedVlogsDurations = updatedDurations
                                        
                                        coroutineScope.launch {
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
                                            currentPlayingIndex = nextIndex
                                        } else {
                                            currentPlayingIndex = 0
                                        }
                                    }
                                }
                            }
                        },
                        onUpdateVlogCaption = { index, newCaption ->
                            if (index in filteredPaths.indices) {
                                val targetPath = filteredPaths[index]
                                val globalIndex = capturedVlogsPaths.indexOf(targetPath)
                                if (globalIndex != -1) {
                                    val targetDuration = capturedVlogsDurations.getOrNull(globalIndex) ?: "2000"
                                    val palCode = activeVlogPal?.code ?: "vlog"
                                    
                                    val updatedCaptions = ArrayList(capturedVlogsCaptions)
                                    updatedCaptions[globalIndex] = newCaption
                                    capturedVlogsCaptions = updatedCaptions
                                    sharedPrefs.edit().putString("vlog_captions", updatedCaptions.joinToString(";;;")).apply()
                                
                                coroutineScope.launch {
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
                    },
                        selectedDayOffset = selectedDayOffset,
                        onSelectedDayOffsetChange = { selectedDayOffset = it },
                        allPalsSubmissions = allPalsSubmissions,
                        allPalsMembers = allPalsMembers,
                        palReactions = palReactions,
                        onEmojiReacted = { path, emoji ->
                            palReactions[path] = emoji
                            activeReactionPreview = Pair(path, emoji)
                        },
                        activeReplyPreviewPath = activeReplyPreviewPath,
                        onActiveReplyPreviewPathChange = { activeReplyPreviewPath = it },
                        activeReactionPreview = activeReactionPreview,
                        onActiveReactionPreviewChange = { activeReactionPreview = it }
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
                        onSend = { caption, targetPal ->
                            capturedCaptionText = caption
                            val time = java.time.LocalTime.now()
                            val formattedTime = String.format("%02d:%02d", time.hour, time.minute)
                            capturedVideoTimeText = formattedTime

                            val targetPalCode = targetPal.code
                            if (targetPalCode == "vlog") {
                                capturedVideoPath?.let { path ->
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

                                    val updatedPaths = ArrayList(capturedVlogsPaths)
                                    updatedPaths.add(0, persistentPath)
                                    capturedVlogsPaths = updatedPaths
                                    sharedPrefs.edit().putString("vlog_paths", updatedPaths.joinToString(";;;")).apply()

                                    val updatedTimes = ArrayList(capturedVlogsTimes)
                                    updatedTimes.add(0, formattedTime)
                                    capturedVlogsTimes = updatedTimes
                                    sharedPrefs.edit().putString("vlog_times", updatedTimes.joinToString(";;;")).apply()

                                    val updatedCaptions = ArrayList(capturedVlogsCaptions)
                                    updatedCaptions.add(0, caption)
                                    capturedVlogsCaptions = updatedCaptions
                                    sharedPrefs.edit().putString("vlog_captions", updatedCaptions.joinToString(";;;")).apply()

                                    val updatedDurations = ArrayList(capturedVlogsDurations)
                                    updatedDurations.add(0, capturedVideoDuration.toString())
                                    capturedVlogsDurations = updatedDurations
                                    sharedPrefs.edit().putString("vlog_durations", updatedDurations.joinToString(";;;")).apply()
                                }
                            }

                            if (targetPalCode != "vlog") {
                                palPalsCount[targetPalCode] = (palPalsCount[targetPalCode] ?: 0) + 1
                            }
                            coroutineScope.launch {
                                try {
                                    val delimiterString = "${capturedVideoPath ?: ""}|||${caption}|||${capturedVideoDuration}"
                                    val newSubmission = SubmissionDbItem(
                                        palCode = targetPalCode,
                                        userId = currentUserId,
                                        userDisplayName = currentDisplayName,
                                        imageUrl = delimiterString
                                    )
                                    supabaseClient.postgrest.from("submissions").insert(newSubmission)
                                    if (targetPalCode != "vlog") {
                                        refreshActivePalDetails(targetPalCode)
                                    } else {
                                        refreshVlogs()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showingCapturedPreview = false
                            selectedTab = "pals"
                        },
                        currentUserId = currentUserId,
                        currentDisplayName = currentDisplayName
                    )
                } else {
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
                        onCaptureSuccess = { path, duration ->
                            capturedVideoPath = path
                            capturedVideoDuration = duration
                            showingCapturedPreview = true
                        }
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
                        val code = query.trim().removePrefix("#").trim()
                        if (code.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    val matchedPalDb = supabaseClient.postgrest.from("pals")
                                        .select {
                                            filter {
                                                eq("code", code)
                                            }
                                        }
                                        .decodeSingleOrNull<PalDbItem>()

                                    if (matchedPalDb != null) {
                                        val newMapping = UserPalMapping(
                                            userId = currentUserId,
                                            palCode = code
                                        )
                                        supabaseClient.postgrest.from("user_pals").insert(newMapping)

                                        val matchedItem = PalItem(
                                            name = matchedPalDb.name,
                                            size = matchedPalDb.size,
                                            code = matchedPalDb.code,
                                            isVlog = matchedPalDb.isVlog,
                                            isCreator = matchedPalDb.creatorId == currentUserId
                                        )

                                        if (!createdPals.any { it.code == code }) {
                                            createdPals = createdPals + matchedItem
                                        }
                                        refreshPals()
                                        Toast.makeText(context, "Joined group: ${matchedPalDb.name}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No group found with code: $code", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Failed to search/join group: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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
        // 7. FLOATING MENUS & OVERLAYS
        // ----------------------------------------------------

        // Plus (+) Menu Overlay
        PlusMenuOverlay(
            showPlusMenu = showPlusMenu,
            onShowPlusMenuChange = { showPlusMenu = it },
            overlayBackdropColor = overlayBackdropColor,
            isDark = isDark,
            navBarBgColor = navBarBgColor,
            textColor = textColor,
            onCreatePalClick = {
                newPalName = ""
                newPalSize = "4"
                createPalStep = 1
                showCreatePalFlow = true
            },
            onJoinPalClick = {
                joinPalCode = ""
                showJoinPalFlow = true
            }
        )

        // Triple Dot (...) Menu Overlay
        TripleDotMenuOverlay(
            showTripleDotMenu = showTripleDotMenu,
            onShowTripleDotMenuChange = { showTripleDotMenu = it },
            tripleDotScreen = tripleDotScreen,
            onTripleDotScreenChange = { tripleDotScreen = it },
            isDark = isDark,
            customAvatarUriString = customAvatarUriString,
            accentColor = accentColor,
            currentDisplayName = currentDisplayName,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = { selectedThemeColor = it },
            notificationInterval = notificationInterval,
            onNotificationIntervalChange = { notificationInterval = it },
            onChoosePhotoClick = {
                showTripleDotMenu = false
                photoPickerLauncher.launch("image/*")
            },
            onDeletePhotoClick = {
                showTripleDotMenu = false
                sessionManager.saveAvatarUri(null)
                customAvatarUriString = null
            },
            onShowEditNameDialogChange = { showEditNameDialog = it },
            onSignOut = { onSignOut() },
            onTripleDotMenuBoundsChange = { tripleDotMenuBounds = it }
        )

        // Activity Screen Overlay
        ActivityScreenOverlay(
            showActivityScreen = showActivityScreen,
            onShowActivityScreenChange = { showActivityScreen = it },
            backgroundColor = backgroundColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor
        )

        // Create Pal Flow Screen
        CreatePalDialogOverlay(
            showCreatePalFlow = showCreatePalFlow,
            onShowCreatePalFlowChange = { showCreatePalFlow = it },
            isCreatingPal = isCreatingPal,
            onIsCreatingPalChange = { isCreatingPal = it },
            createPalStep = createPalStep,
            onCreatePalStepChange = { createPalStep = it },
            newPalName = newPalName,
            onNewPalNameChange = { newPalName = it },
            newPalSize = newPalSize,
            onNewPalSizeChange = { newPalSize = it },
            generatedPalCode = generatedPalCode,
            creationDots = creationDots,
            createdPals = createdPals,
            onCreatedPalsChange = { createdPals = it },
            currentUserId = currentUserId,
            groupDatabase = groupDatabase,
            createPalFocusRequester = createPalFocusRequester,
            isDark = isDark,
            accentColor = accentColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            palTextLogoColor = palTextLogoColor,
            backgroundColor = backgroundColor,
            supabaseClient = supabaseClient
        )

        // Join Pal Dialog Flow (Overlay Card at bottom / center based on focus)
        JoinPalDialogOverlay(
            showJoinPalFlow = showJoinPalFlow,
            onShowJoinPalFlowChange = { showJoinPalFlow = it },
            joinPalCode = joinPalCode,
            onJoinPalCodeChange = { joinPalCode = it },
            isDark = isDark,
            accentColor = accentColor,
            currentUserId = currentUserId,
            createdPals = createdPals,
            onCreatedPalsChange = { createdPals = it },
            refreshPals = { refreshPals() },
            supabaseClient = supabaseClient
        )

        // Edit Name Dialog Flow (Centered Screen Overlay)
        EditNameDialogOverlay(
            showEditNameDialog = showEditNameDialog,
            onShowEditNameDialogChange = { showEditNameDialog = it },
            isDark = isDark,
            accentColor = accentColor,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            editFirstName = editFirstName,
            onEditFirstNameChange = { editFirstName = it },
            editLastName = editLastName,
            onEditLastNameChange = { editLastName = it },
            editNameFocusRequester = editNameFocusRequester,
            onEditNameBoundsChange = { editNameBounds = it },
            currentDisplayName = currentDisplayName,
            onCurrentDisplayNameChange = { currentDisplayName = it },
            useDarkTextOnAccent = themeConfig.useDarkTextOnAccent
        )


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
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, "PAL_vlog_${System.currentTimeMillis()}.mp4")
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/PAL")
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
    zoomLevel: Int = 1,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onVideoCaptureCreated: (VideoCapture<Recorder>?) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.scaleType = scaleType
        }
    }
    LaunchedEffect(scaleType) {
        previewView.scaleType = scaleType
    }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var activeCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    
    LaunchedEffect(isCameraFlipped) {
        val cameraProvider = cameraProviderFuture.get()
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)
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

    LaunchedEffect(activeCamera, zoomLevel) {
        val camera = activeCamera ?: return@LaunchedEffect
        try {
            val zoomState = camera.cameraInfo.zoomState.value
            val minZoom = zoomState?.minZoomRatio ?: 1.0f
            val maxZoom = zoomState?.maxZoomRatio ?: 2.0f
            val targetRatio = when (zoomLevel) {
                1 -> 1.0f
                2 -> 1.25f
                3 -> 1.50f
                4 -> 1.75f
                5 -> 2.0f
                else -> 1.0f
            }
            val ratio = targetRatio.coerceIn(minZoom, maxZoom)
            camera.cameraControl.setZoomRatio(ratio)
        } catch (exc: Exception) {
            exc.printStackTrace()
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
    var activeTimerMode by remember { mutableStateOf(TimerMode.DEFAULT) }
    var flashMode by remember { mutableStateOf("off") } // "off", "on", "auto"
    var isCameraFlipped by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableStateOf(0.0f) }
    var countdownSeconds by remember { mutableStateOf(0) }
    var videoCaptureRef by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecordingSession by remember { mutableStateOf<Recording?>(null) }

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
                "PAL_REC_${System.currentTimeMillis()}.mp4"
            )
            val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()

            val mainExecutor = ContextCompat.getMainExecutor(context)
            var session: Recording? = null
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
                            is VideoRecordEvent.Finalize -> {
                                if (!recordEvent.hasError()) {
                                    android.util.Log.d("ProductionCamera", "Video encoding success: ${outputFile.absolutePath}")
                                    onCaptureSuccess(outputFile.absolutePath, durationMs)
                                } else {
                                    android.util.Log.e("ProductionCamera", "Encoder finalized with error: ${recordEvent.error}")
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

            recordingProgress = 0.0f
            val steps = 50
            val delayMs = durationMs / steps
            for (step in 1..steps) {
                delay(delayMs)
                recordingProgress = step.toFloat() / steps
            }

            session.stop()
            activeRecordingSession = null
            onRecordingChange(false)
        } else {
            activeRecordingSession?.stop()
            activeRecordingSession = null
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


        val progressWidth = 6.5.dp * scale

        // Precise positioning constants for taller layout
        val shutterBottomMargin = 100.dp
        val shutterSize = 59.dp * scale
        val cardBottomPadding = shutterBottomMargin + (shutterSize / 2f)

        // Dynamically calculate camera frame size so the right gap is exactly progressWidth
        var cameraWidth = screenWidth - 4.dp - (progressWidth * 2)
        var cameraHeight = cameraWidth * (16f / 9f)

        // Ensure height doesn't exceed screen height minus margins (bottom padding + top margin of 30.dp)
        val maxAllowedHeight = screenHeight - cardBottomPadding - 30.dp
        if (cameraHeight > maxAllowedHeight) {
            cameraHeight = maxAllowedHeight
            cameraWidth = cameraHeight * (9f / 16f)
        }

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

        // Camera Viewfinder Box (9:16 rounded card) wrapped in a glow container
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = cardBottomPadding)
                .width(cameraWidth)
                .height(cameraHeight)
                .background(selectedProfileColor.copy(alpha = 0.15f), RoundedCornerShape(29.dp * scale)) // soft glow layer
                .padding(1.dp * scale) // thin inset
        ) {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp * scale,
                        color = selectedProfileColor.copy(alpha = 0.15f), // barely visible light glow line
                        shape = RoundedCornerShape(28.dp * scale)
                    ),
                borderRadius = 28.dp * scale,
                isDark = isDark,
                gradientColors = if (isDark) listOf(Color(0xFF161616), Color(0xFF161616)) else listOf(Color(0xFFEBEBEB), Color(0xFFEBEBEB)),
                borderColor = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp * scale))
                ) {
                    // Actual Camera Preview Feed!
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        isCameraFlipped = isCameraFlipped,
                        isFlashOn = (flashMode == "on") || (flashMode == "auto" && isRecording),
                        zoomLevel = activeSlot,
                        onVideoCaptureCreated = { videoCaptureRef = it }
                    )

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

        // Capture Button (R.drawable.capture_smile) centered on the bottom border of 9:16 frame exactly half-in, half-out
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = shutterBottomMargin)
                .size(shutterSize)
                .clip(CircleShape)
                .background(currentInnerColor)
                .pointerInput(Triple(activeSlot, isRecording, countdownSeconds)) {
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
                            var dragAccumulator = 0f
                            
                            while (true) {
                                val event = awaitPointerEvent()
                                val dragEvent = event.changes.firstOrNull { it.id == dragPointerId }
                                if (dragEvent == null || !dragEvent.pressed) {
                                    break
                                }
                                
                                val diffY = dragEvent.position.y - dragEvent.previousPosition.y
                                if (diffY != 0f) {
                                    dragEvent.consume()
                                    dragAccumulator -= diffY
                                    val slotDelta = (dragAccumulator / 30f).toInt()
                                    if (slotDelta != 0) {
                                        val newSlot = (activeSlot + slotDelta).coerceIn(1, 5)
                                        if (newSlot != activeSlot) {
                                            activeSlot = newSlot
                                            dragAccumulator = 0f
                                        }
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
                    .rotate(180f - rotationAngle) // upside down baseline rotating clockwise continuously
            )
        }

        // Outer concentric ring for capture button (hollow, transparent fill, centered on the bottom border line)
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = cardBottomPadding - (67.dp * scale / 2f))
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
                .padding(bottom = cardBottomPadding - (36.dp * scale / 2f))
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
        val offsetFromCenter = (screenWidth / 4f) + (cameraWidth / 4f) - 1.dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = offsetFromCenter)
                .padding(bottom = cardBottomPadding + 28.dp * scale) // aligned with straight vertical edge of card
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
                    .rotate(if (isLit) 180f else 0f)
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
    val isCurrentUserLit = currentAlreadySent || isSelected
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until displayCount) {
            val memberName = members.getOrNull(i) ?: ""
            val isLit = if (memberName.isEmpty()) {
                false
            } else {
                if (memberName == userFirstName || memberName.contains("(You)") || memberName == "only you") {
                    submissions.any { it.userId == currentUserId }
                } else {
                    submissions.any { sub ->
                        val cleanSubName = sub.userDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        cleanSubName.equals(memberName, ignoreCase = true)
                    }
                }
            }
            
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
                        modifier = Modifier.fillMaxSize(),
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
    onSend: (String, PalItem) -> Unit,
    currentUserId: String,
    currentDisplayName: String
) {
    val context = LocalContext.current
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var captionText by rememberSaveable { mutableStateOf("") }
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
    var selectedPal by remember { mutableStateOf<PalItem?>(null) }
    val groupMembersMap = remember { mutableStateMapOf<String, List<String>>() }
    val groupSubmissionsMap = remember { mutableStateMapOf<String, List<SubmissionDbItem>>() }
    val userFirstName = remember(currentDisplayName) {
        currentDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
    }

    LaunchedEffect(createdPals, currentDisplayName) {
        createdPals.forEach { pal ->
            if (pal.isVlog) {
                groupMembersMap[pal.code] = listOf("only you")
            } else {
                coroutineScope.launch {
                    try {
                        val dbSubs = com.finrein.pals.PALApplication.supabase.postgrest.from("submissions")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<SubmissionDbItem>()
                        val dbMsgs = com.finrein.pals.PALApplication.supabase.postgrest.from("messages")
                            .select { filter { eq("pal_code", pal.code) } }
                            .decodeList<MessageDbItem>()
                        
                        val todaySubs = dbSubs.filter { sub ->
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
                        groupSubmissionsMap[pal.code] = todaySubs
                        
                        val names = (dbSubs.map { it.userDisplayName } + dbMsgs.map { it.senderName } + currentDisplayName)
                            .map { name ->
                                if (name == currentDisplayName) {
                                    userFirstName
                                } else {
                                    name.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                                }
                            }
                            .distinct()
                            .filter { it.isNotEmpty() }
                        groupMembersMap[pal.code] = names
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // 1. Initialize ExoPlayer safely
    val exoPlayer = remember(capturedVideoPath) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
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
            if (fileTarget.exists() && fileTarget.length() > 0) {
                val targetUri = android.net.Uri.fromFile(fileTarget)
                exoPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(targetUri))
                exoPlayer.prepare()
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
                        view.apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                            exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                    super.onVideoSizeChanged(videoSize)
                                    
                                    val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                    
                                    val containerWidth = width.toFloat()
                                    val containerHeight = height.toFloat()
                                    if (containerWidth > 0f && containerHeight > 0f) {
                                        if (videoSize.height > videoSize.width) {
                                            android.util.Log.d("PalMatrixFix", "Portrait Clip detected. Applying anti-compression scaling matrix transformations.")
                                            
                                            val scaleX = containerHeight / containerWidth
                                            val scaleY = containerWidth / containerHeight

                                            textureView.scaleX = scaleX
                                            textureView.scaleY = scaleY
                                            textureView.rotation = 270f
                                        } else {
                                            android.util.Log.d("PalMatrixFix", "Landscape Clip detected. Restoring clean un-modified layout boundaries.")
                                            
                                            textureView.scaleX = 1.0f
                                            textureView.scaleY = 1.0f
                                            textureView.rotation = 0f
                                        }
                                    }
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Middle Vlog Title Header Text ("vlog >" or selected group name + " >")
            val selectedPalName = selectedPal?.name?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            if (selectedPalName != null) {
                Text(
                    text = "$selectedPalName >",
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
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(palTextLogoColor), // cursor color exactly matches top-left PAL color
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
            val isButtonEnabled = selectedPal != null
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
                                selectedPal?.let { onSend(captionText, it) }
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
                                    vlogText = selectedPal?.name ?: "vlog",
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
            sortedPals.forEach { pal ->
                val isSelected = selectedPal?.code == pal.code
                val groupName = pal.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                val members = groupMembersMap[pal.code] ?: emptyList()
                val otherMembers = members.filter { it != userFirstName && !it.contains("(You)") && it != "only you" }
                
                val description = if (pal.isVlog) "only you" else {
                    if (otherMembers.isEmpty()) "only you" else {
                        otherMembers.take(3).joinToString(", ") { name ->
                            name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                    }
                }
                
                // Card Box Container (Grey in light mode, Charcoal in dark mode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA))
                        .clickable {
                            // Toggle selection (single-select style)
                            selectedPal = if (isSelected) null else pal
                        }
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
                        Text(
                            text = groupName,
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
                    if (pal.isVlog) {
                        val vlogSubmissions = if (capturedVlogsPaths.isNotEmpty()) {
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
                        // For Groups
                        val groupSubs = groupSubmissionsMap[pal.code] ?: emptyList()
                        GroupMembersSmileysRow(
                            members = members,
                            submissions = groupSubs,
                            isDark = isDark,
                            accentColor = accentColor,
                            palTextLogoColor = palTextLogoColor,
                            currentUserId = currentUserId,
                            userFirstName = userFirstName,
                            smileySize = 24.dp,
                            innerSize = 18.dp
                        )
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
    val onUpdateVlogCaption: (Int, String) -> Unit = { _, _ -> },
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
    val onActiveReactionPreviewChange: (Pair<String, String>?) -> Unit = {}
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
    onReplyClick: (String) -> Unit = {}
) {
    val isActualMember = index < groupMembers.size
    val cardShape = if (isGrid) androidx.compose.ui.graphics.RectangleShape else RoundedCornerShape(28.dp)
    val memberName = if (isActualMember) groupMembers[index] else null
    val isUser = index == 0 || (memberName != null && (memberName.contains("(You)") || memberName == userFirstName))

    val memberSub = if (isActualMember) {
        filteredSubmissions.firstOrNull { sub ->
            if (isUser) {
                sub.userId == currentUserId
            } else {
                val cleanSubName = sub.userDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                cleanSubName.equals(memberName, ignoreCase = true)
            }
        }
    } else {
        null
    }
    val hasSubmission = memberSub != null
    var showDropdownMenu by remember { mutableStateOf(false) }
    var isLoved by rememberSaveable { mutableStateOf(false) }
    var showEmojiOverlay by remember { mutableStateOf(false) }
    val defaultEmojis = remember { listOf("😂", "❤️", "😭", "✨", "🥺", "🔥", "🥰", "🎉", "💀", "👍", "🙏", "💯", "😎", "👀") }
    var currentEmojis by remember { mutableStateOf(defaultEmojis.take(5)) }

    if (index == selectedMemberIndex && hasSubmission) {
        // ACTIVE VIDEO CARD PLAYER
        val videoPath = memberSub!!.imageUrl.split("|||").firstOrNull() ?: ""
        val caption = memberSub.imageUrl.split("|||").getOrNull(1) ?: ""
        val timeText = if (!memberSub.createdAt.isNullOrEmpty()) {
            try {
                val instant = java.time.Instant.parse(memberSub.createdAt)
                val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
                zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
            } catch (e: Exception) {
                memberSub.createdAt.substringAfter("T").substringBefore(".").take(5)
            }
        } else {
            "12:00"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeightDp)
                .clip(cardShape)
                .background(Color.Black)
                .border(
                    width = 2.dp,
                    color = accentColor,
                    shape = cardShape
                )
        ) {
            val localPlayer = remember(videoPath) {
                androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                    repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                    volume = 0f
                    val cleanPath = if (videoPath.startsWith("file://")) videoPath.substring(7) else videoPath
                    val file = java.io.File(cleanPath)
                    if (file.exists()) {
                        setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                        prepare()
                    }
                }
            }

            DisposableEffect(localPlayer) {
                onDispose {
                    localPlayer.release()
                }
            }

            LaunchedEffect(localPlayer) {
                localPlayer.playWhenReady = true
            }

            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                    view.apply {
                        player = localPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                        localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                super.onVideoSizeChanged(videoSize)
                                val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                val containerWidth = width.toFloat()
                                val containerHeight = height.toFloat()
                                if (containerWidth > 0f && containerHeight > 0f) {
                                    if (videoSize.height > videoSize.width) {
                                        val scaleX = containerHeight / containerWidth
                                        val scaleY = containerWidth / containerHeight
                                        textureView.scaleX = scaleX
                                        textureView.scaleY = scaleY
                                        textureView.rotation = 270f
                                    } else {
                                        textureView.scaleX = 1.0f
                                        textureView.scaleY = 1.0f
                                        textureView.rotation = 0f
                                    }
                                }
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay 1: Avatar and Name (Top Left)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = if (isGrid) 8.dp else 12.dp, start = if (isGrid) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userAvatar = if (isUser) customAvatarUriString else null
                if (userAvatar != null) {
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
                            modifier = Modifier.fillMaxSize()
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

            // Overlay 2: Time Text (Center)
            Text(
                text = timeText,
                fontFamily = BricolageVariableFontFamily,
                fontSize = if (isGrid) 16.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            // Overlay 3: Caption Text
            if (caption.isNotEmpty()) {
                Text(
                    text = caption,
                    fontFamily = RobotoFontFamily,
                    fontSize = if (isGrid) 11.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isGrid) 12.dp else 36.dp),
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 3f
                        )
                    )
                )
            }

            // Overlay 4: Triple dots at bottom right (Only show if it is the user's card)
            if (isUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isGrid) 8.dp else 12.dp, end = if (isGrid) 10.dp else 16.dp)
                        .clickable { showDropdownMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "•••",
                        fontSize = if (isGrid) 14.sp else 18.sp,
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
            } else {
                // Reply Icon at middle right
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (isGrid) 10.dp else 16.dp)
                        .clip(CircleShape)
                        .clickable {
                            onReplyClick(videoPath)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = Color.White,
                        modifier = Modifier
                            .graphicsLayer(scaleX = -1f)
                            .size(if (isGrid) 20.dp else 28.dp)
                    )
                }

                // Love Icon at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (isGrid) 8.dp else 12.dp, end = if (isGrid) 10.dp else 16.dp)
                        .clip(CircleShape)
                        .clickable {
                            showEmojiOverlay = !showEmojiOverlay
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Love",
                        tint = Color.White,
                        modifier = Modifier.size(if (isGrid) 20.dp else 28.dp)
                    )
                }
            }

            // Reacted emoji centered near the bottom (below caption)
            val reactedEmoji = palReactions[videoPath]
            if (reactedEmoji != null) {
                Text(
                    text = reactedEmoji,
                    fontSize = if (isGrid) 24.sp else 32.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isGrid) 6.dp else 10.dp)
                )
            }

            // Centered Emoji overlay
            if (showEmojiOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showEmojiOverlay = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        currentEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .clickable {
                                        onEmojiReacted(videoPath, emoji)
                                        showEmojiOverlay = false
                                    }
                            )
                        }

                        // Dotted shuffle button
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
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), style = stroke)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Shuffle",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                val userAvatar = if (isUser) customAvatarUriString else null
                if (userAvatar != null) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                val userEmptyTextColor = if (isDark) Color(0xFF5C5E62) else textColor
                val userEmptyCaptureColor = if (isDark) Color(0xFF5C5E62) else Color.Black
                Text(
                    text = userFirstName,
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
                val userEmptyTextColor = if (isDark) Color(0xFF5C5E62) else textColor
                val userEmptyCaptureColor = if (isDark) Color(0xFF5C5E62) else Color.Black
                Text(
                    text = roundedHourStr,
                    fontFamily = BricolageVariableFontFamily,
                    fontSize = if (isGrid) 16.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = userEmptyTextColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White
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
                    fontSize = if (isGrid) 14.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF5C5E62) else Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    } else if (isActualMember) {
        // OTHER MEMBER CARD (either has submission but not active, or doesn't have submission)
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
                .then(
                    if (hasSubmission) {
                        Modifier.clickable { onSelectedMemberIndexChange(index) }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (hasSubmission) {
                val videoPath = memberSub!!.imageUrl.split("|||").firstOrNull() ?: ""
                val localPlayer = remember(videoPath) {
                    androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                        repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                        volume = 0f
                        val cleanPath = if (videoPath.startsWith("file://")) videoPath.substring(7) else videoPath
                        val file = java.io.File(cleanPath)
                        if (file.exists()) {
                            setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                            prepare()
                        }
                    }
                }

                DisposableEffect(localPlayer) {
                    onDispose {
                        localPlayer.release()
                    }
                }

                LaunchedEffect(localPlayer) {
                    localPlayer.playWhenReady = true
                }

                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                        view.apply {
                            player = localPlayer
                            useController = false
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                            localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                    super.onVideoSizeChanged(videoSize)
                                    val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                    val containerWidth = width.toFloat()
                                    val containerHeight = height.toFloat()
                                    if (containerWidth > 0f && containerHeight > 0f) {
                                        if (videoSize.height > videoSize.width) {
                                            val scaleX = containerHeight / containerWidth
                                            val scaleY = containerWidth / containerHeight
                                            textureView.scaleX = scaleX
                                            textureView.scaleY = scaleY
                                            textureView.rotation = 270f
                                        } else {
                                            textureView.scaleX = 1.0f
                                            textureView.scaleY = 1.0f
                                            textureView.rotation = 0f
                                        }
                                    }
                                }
                            })
                        }
                    },
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
                val userAvatar = if (isUser) customAvatarUriString else null
                if (userAvatar != null) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                val actualMemberTextColor = if (hasSubmission) Color.White else textColor
                Text(
                    text = memberName ?: "",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 12.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = actualMemberTextColor
                )
            }

            val displayTimeText = if (hasSubmission) {
                val sub = memberSub!!
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
            } else {
                "4:00"
            }

            val actualMemberTextColor = if (hasSubmission) Color.White else textColor
            Text(
                text = displayTimeText,
                fontFamily = BricolageVariableFontFamily,
                fontSize = if (isGrid) 16.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = actualMemberTextColor,
                modifier = Modifier.align(Alignment.Center)
            )

            val outerColor = shufflingColors[index % 6]
            val innerColor = shufflingColors[(index + 3) % 6]

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = if (isGrid) 10.dp else 24.dp)
                    .size(if (isGrid) 30.dp else 44.dp)
                    .then(
                        if (hasSubmission) {
                            Modifier.border(2.dp, outerColor, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val currentInnerSize = if (hasSubmission) (if (isGrid) 22.dp else 34.dp) else (if (isGrid) 30.dp else 44.dp)
                Box(
                    modifier = Modifier
                        .size(currentInnerSize)
                        .clip(CircleShape)
                        .background(
                            if (hasSubmission) innerColor else (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.smile_small),
                        contentDescription = "Status Smiley",
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(
                            if (hasSubmission) {
                                Color.Black
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                            }
                        )
                    )
                }
            }
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
                    tint = textColor,
                    modifier = Modifier.size(if (isGrid) 20.dp else 28.dp)
                )
                Text(
                    text = "invite a friend",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = if (isGrid) 11.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
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
    onUpdateVlogCaption: (Int, String) -> Unit,
    density: androidx.compose.ui.unit.Density,
    context: android.content.Context,
    palReactions: Map<String, String> = emptyMap(),
    onEmojiReacted: (String, String) -> Unit = { _, _ -> },
    onReplyClick: (String) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightDp = maxHeight
        val screenWidthDp = maxWidth

        val totalSlots = maxOf(groupMembers.size, pal.size.toIntOrNull() ?: 4)
        val isGrid = totalSlots > 5
        val contentSpacingDp = if (isGrid) 2.dp else 9.dp
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
            verticalArrangement = Arrangement.Center,
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
                        onReplyClick = onReplyClick
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
                                    onReplyClick = onReplyClick
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
                                    onReplyClick = onReplyClick
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
                                    onReplyClick = onReplyClick
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isEditingCaption) {
            Dialog(onDismissRequest = { onIsEditingCaptionChange(false) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isDark) Color(0xFF2B2930) else Color(0xFFF5F3EB))
                        .padding(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Edit Caption",
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )

                        val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        OutlinedTextField(
                            value = editCaptionText,
                            onValueChange = onEditCaptionTextChange,
                            placeholder = {
                                Text(
                                    text = "write caption...",
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
                            ),
                            textStyle = TextStyle(
                                fontFamily = RobotoFontFamily,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true
                        )

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
                                color = Color(0xFF6750A4),
                                modifier = Modifier
                                    .clickable { onIsEditingCaptionChange(false) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Save",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier
                                    .clickable {
                                        val userSub = filteredSubmissions.firstOrNull { it.userId == currentUserId }
                                        if (userSub != null) {
                                            val userPath = userSub.imageUrl.split("|||").firstOrNull() ?: ""
                                            val userFilteredIndex = capturedVlogsPaths.indexOf(userPath)
                                            if (userFilteredIndex != -1) {
                                                onUpdateVlogCaption(userFilteredIndex, editCaptionText.text.trim())
                                            }
                                        }
                                        onIsEditingCaptionChange(false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
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
    val isUser: Boolean
)

@kotlin.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VlogScreenContent(
    params: VlogScreenContentParams
) {
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
    var groupMembers by remember(pal.code) {
        val cached = params.allPalsMembers[pal.code]
        val initialList = if (cached != null) {
            cached.map { name ->
                if (name == userFirstName || name == "$userFirstName (You)") {
                    "$userFirstName (You)"
                } else {
                    name
                }
            }
        } else {
            listOf("$userFirstName (You)")
        }
        mutableStateOf(initialList)
    }
    LaunchedEffect(pal.code, currentDisplayName) {
        if (pal.isVlog) {
            groupMembers = listOf("$userFirstName (You)")
        } else {
            try {
                val dbSubs = com.finrein.pals.PALApplication.supabase.postgrest.from("submissions")
                    .select {
                        filter {
                            eq("pal_code", pal.code)
                        }
                    }
                    .decodeList<SubmissionDbItem>()
                val dbMsgs = com.finrein.pals.PALApplication.supabase.postgrest.from("messages")
                    .select {
                        filter {
                            eq("pal_code", pal.code)
                        }
                    }
                    .decodeList<MessageDbItem>()
                val names = (dbSubs.map { it.userDisplayName } + dbMsgs.map { it.senderName } + currentDisplayName)
                    .map { name ->
                        if (name == currentDisplayName) {
                            "$userFirstName (You)"
                        } else {
                            name.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                        }
                    }
                    .distinct()
                    .filter { it.isNotEmpty() }
                groupMembers = names
            } catch (e: Exception) {
                e.printStackTrace()
                groupMembers = listOf("$userFirstName (You)")
            }
        }
    }

    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    var selectedMemberIndex by remember(pal.code) { mutableStateOf(0) }
    val activePalSubmissions = remember(pal.code, params.allPalsSubmissions) {
        params.allPalsSubmissions[pal.code] ?: emptyList<SubmissionDbItem>()
    }
    val targetDate = remember(selectedDayOffset) {
        java.time.LocalDate.now().minusDays(selectedDayOffset.toLong())
    }
    val filteredSubmissions = remember(activePalSubmissions, targetDate) {
        activePalSubmissions.filter { sub ->
            getSubmissionLocalDate(sub) == targetDate
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
                    capturedVlogsPaths = capturedVlogsPaths,
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
                        }
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
                                val localPlayer = remember(path) {
                                    androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                                        repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                                        volume = 0f
                                        val cleanPath = when {
                                            path.startsWith("file://") -> path.substring(7)
                                            else -> path
                                        }
                                        val file = java.io.File(cleanPath)
                                        if (file.exists()) {
                                            setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                                            prepare()
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
                                            view.apply {
                                                player = localPlayer
                                                useController = false
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                                                localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                                        super.onVideoSizeChanged(videoSize)
                                                        val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                                        val containerWidth = width.toFloat()
                                                        val containerHeight = height.toFloat()
                                                        if (containerWidth > 0f && containerHeight > 0f) {
                                                            if (videoSize.height > videoSize.width) {
                                                                val scaleX = containerHeight / containerWidth
                                                                val scaleY = containerWidth / containerHeight
                                                                textureView.scaleX = scaleX
                                                                textureView.scaleY = scaleY
                                                                textureView.rotation = 270f
                                                            } else {
                                                                textureView.scaleX = 1.0f
                                                                textureView.scaleY = 1.0f
                                                                textureView.rotation = 0f
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
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
                                                modifier = Modifier.fillMaxSize()
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
                                        .padding(bottom = 16.dp, end = 16.dp)
                                        .clickable { showTripleDotMenu = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "•••",
                                        fontSize = 18.sp,
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
                                                onUpdateVlogCaption(selectedPageIndex, editCaptionText.text.trim())
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
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                val vlogEmptyTextColor = if (isDark) Color(0xFF5C5E62) else textColor
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
                                val vlogEmptyTextColor = if (isDark) Color(0xFF5C5E62) else textColor
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
                                val vlogEmptyCaptureColor = if (isDark) Color(0xFF5C5E62) else Color.Black
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(64.dp)
        ) {
            // Left: back chevron with dark transparent bg
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
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
                    Row(
                        modifier = Modifier
                            .offset(y = (-6).dp) // moved up by 6dp
                            .height(34.dp) // background pill height reduced accordingly
                            .clip(RoundedCornerShape(17.dp))
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
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (selectedDayOffset > 0) dayName else (if (showEdit && editName.isNotEmpty()) editName else pal.name),
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 19.5.sp, // reduced by 1.5sp
                            fontWeight = FontWeight.Bold,
                            color = headerIconTint
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
                                            modifier = Modifier.fillMaxSize(),
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
                    Row(
                        modifier = Modifier
                            .offset(y = -7.dp)
                            .height(37.dp)
                            .clip(RoundedCornerShape(18.5.dp))
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
                            .padding(horizontal = 13.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (selectedDayOffset > 0) dayName else (if (showEdit && editName.isNotEmpty()) editName else pal.name),
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerIconTint
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
                    Row(
                        modifier = Modifier.offset(y = 32.5.dp), // offset downwards below the non-vlog capsule (shifted up by 7dp from 42dp to 35dp)
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shufflingColors = listOf(
                            Color(0xFFFFE600), // Yellow
                            Color(0xFFFF6700), // Orange
                            Color(0xFFFF007F), // Pink
                            Color(0xFF00F0FF), // Blue
                            Color(0xFFB000FF), // Purple
                            Color(0xFFFF073A)  // Red
                        )
                        repeat(palsCount) { index ->
                            val isActive = index == selectedPageIndex
                            val outerColor = shufflingColors[index % 6]
                            val innerColor = shufflingColors[(index + 3) % 6]
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .then(
                                        if (isActive) {
                                            Modifier.border(1.5.dp, outerColor, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) innerColor else innerColor.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.smile_small),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        colorFilter = ColorFilter.tint(if (isActive) Color.Black else Color.Black.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right: Share (Export) & Chat bubble buttons (moved to right by 10dp)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share/Export Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(headerButtonBg)
                        .clickable { onShowExportDialogChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    ShareIcon(
                        tint = headerIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Chat bubble button
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                            
                            groupMembers.forEach { memberName ->
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
                            // Main dropdown menu list (archive, members, settings)
                            
                            // 1. archive option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onShowDropdownChange(false)
                                        showArchiveView = true
                                    }
                                    .padding(horizontal = 16.dp, vertical = 7.dp), // reduced by 5dp
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "archive",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
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
                                text = "PAL",
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

                        // Body: edit pal label and custom text input field
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Label: → edit log (using Monospace)
                            Text(
                                text = "→ edit log",
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
                                text = "delete log",
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
        if (showDeleteVlogConfirmation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDeleteVlogConfirmation = false },
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
                                    .clickable { showDeleteVlogConfirmation = false }
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
                                        showDeleteVlogConfirmation = false
                                        selectedPageIndex = if (capturedVlogsPaths.size <= 1) 0 else selectedPageIndex.coerceAtMost(capturedVlogsPaths.size - 2)
                                        onDeleteVlog(indexToDelete)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Pal Chat Overlay ---
        if (showChat) {
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
                val context = LocalContext.current
                val defaultEmojis = remember { listOf("😂", "❤️", "😭", "✨", "🥺", "🔥", "🥰", "🎉", "💀", "👍", "🙏", "💯", "😎", "👀") }
                var currentEmojis by remember { mutableStateOf(defaultEmojis.take(5)) }

                val feedItems = remember(pal.code, capturedVlogsPaths, allPalsSubmissions, currentUserId) {
                    if (pal.isVlog) {
                        capturedVlogsPaths.mapIndexedNotNull { idx, path ->
                            val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
                            val file = java.io.File(cleanPath)
                            if (file.exists()) {
                                val instant = java.time.Instant.ofEpochMilli(file.lastModified())
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
                        .sortedBy { it.rawInstant }
                    } else {
                        val subs = allPalsSubmissions[pal.code] ?: emptyList()
                        subs.mapNotNull { sub ->
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
                        .sortedBy { it.rawInstant }
                    }
                }

                val groupedByDay = remember(feedItems) {
                    feedItems.groupBy { it.localDate }
                }

                var showEmojiOverlayForPath by remember { mutableStateOf<String?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // 1. Scrollable feed column (drawn behind, fills parent with padding to accommodate header/footer)
                    if (feedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp, bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "no captured pals yet",
                                fontFamily = OwnglyphFontFamily,
                                fontSize = 24.5.sp, // original 12.sp + 12.5.sp
                                color = mutedTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 64.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            groupedByDay.keys.sorted().forEach { dayDate ->
                                val dayFeed = groupedByDay[dayDate] ?: emptyList()
                                val today = java.time.LocalDate.now()
                                val dayLabel = when (dayDate) {
                                    today -> "Today"
                                    today.minusDays(1) -> "Yesterday"
                                    else -> dayDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d", java.util.Locale.US))
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    dayFeed.forEach { feedItem ->
                                        val headerText = "$dayLabel ${feedItem.timeStr}"

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // 2. Aligned message body
                                            if (feedItem.isUser) {
                                                // USER (Right Aligned)
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(end = 17.dp),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    // "apple_user" text
                                                    Text(
                                                        text = "apple_user",
                                                        fontFamily = FontFamily.SansSerif,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = textColor.copy(alpha = 0.6f),
                                                        modifier = Modifier.padding(bottom = 2.dp, end = 4.dp)
                                                    )

                                                    // Video Player
                                                    VideoPlayerItem(
                                                        videoPath = feedItem.path,
                                                        modifier = Modifier
                                                            .width(210.dp)
                                                            .height(125.dp)
                                                            .clip(RoundedCornerShape(16.dp))
                                                    )

                                                    // Reacted emoji centered directly below video
                                                    val reactedEmoji = palReactions[feedItem.path]
                                                    if (reactedEmoji != null) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = reactedEmoji,
                                                            fontSize = 20.sp,
                                                            modifier = Modifier.align(Alignment.CenterHorizontally)
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
                                                    // Row with Avatar & First Name
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    ) {
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
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                        val cleanName = feedItem.userDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                                                        Text(
                                                            text = cleanName,
                                                            color = textColor,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.SansSerif
                                                        )
                                                    }

                                                    // Video card container with reply & love icons underneath
                                                    Column(
                                                        modifier = Modifier.width(210.dp)
                                                    ) {
                                                        VideoPlayerItem(
                                                            videoPath = feedItem.path,
                                                            modifier = Modifier
                                                                .width(210.dp)
                                                                .height(125.dp)
                                                                .clip(RoundedCornerShape(16.dp))
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                                                contentDescription = "Reply",
                                                                tint = textColor.copy(alpha = 0.8f),
                                                                modifier = Modifier
                                                                    .graphicsLayer(scaleX = -1f)
                                                                    .size(20.dp)
                                                                    .clickable {
                                                                        onActiveReplyPreviewPathChange(feedItem.path)
                                                                    }
                                                            )
                                                            Icon(
                                                                imageVector = Icons.Default.FavoriteBorder,
                                                                contentDescription = "Love",
                                                                tint = textColor.copy(alpha = 0.8f),
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .clickable {
                                                                        showEmojiOverlayForPath = feedItem.path
                                                                    }
                                                            )
                                                            
                                                            val reactedEmoji = palReactions[feedItem.path]
                                                            if (reactedEmoji != null) {
                                                                Text(text = reactedEmoji, fontSize = 20.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. View Log full-width card at the end of the day Date group
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F3EB))
                                            .border(1.dp, selectedProfileColor, RoundedCornerShape(12.dp))
                                            .clickable {
                                                Toast.makeText(context, "view log clicked for $dayLabel", Toast.LENGTH_SHORT).show()
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
                                            text = "view log",
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

                    // Emoji reaction overlay for feed item if visible
                    if (showEmojiOverlayForPath != null) {
                        val path = showEmojiOverlayForPath!!
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showEmojiOverlayForPath = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
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

                                // Dotted shuffle button
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
                                            drawCircle(color = Color.White.copy(alpha = 0.6f), style = stroke)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Shuffle",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Header Box aligned at top
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(60.dp)
                    ) {
                        // Left Back button
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(y = 2.dp)
                                .size(40.dp)
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

                        // Middle Title
                        Text(
                            text = pal.name,
                            fontFamily = BricolageVariableFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 2.dp)
                        )
                    }

                    // 3. Footer Row aligned at bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .imePadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF161616) else Color(0xFFEBEBEB))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                                .clickable {
                                    onNavigateToCamera()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner smiley circle
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.capture_smile),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .graphicsLayer(rotationZ = -180f)
                                        .fillMaxSize()
                                )
                            }
                        }

                        // Middle text input box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
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
                                modifier = Modifier.fillMaxWidth(),
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

                        // Right Send arrow button
                        val isInputValid = messageInput.trim().isNotEmpty()
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isInputValid) accentColor else headerButtonBg)
                                .clickable(enabled = isInputValid) {
                                    onSendMessage(messageInput.trim())
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
            }
        }

        // --- Reply Preview Overlay ---
        if (activeReplyPreviewPath != null) {
            val videoPath = activeReplyPreviewPath!!
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
                        text = pal.name,
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
                                Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
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

        // --- Reaction Preview Overlay ---
        if (activeReactionPreview != null) {
            val (videoPath, emoji) = activeReactionPreview!!
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
                        text = pal.name,
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
        
        // --- Export Dialog Overlay ---
        if (showExportDialog) {
            var isExportSaving by remember { mutableStateOf(false) }
            var isExportSaved by remember { mutableStateOf(false) }
            val localCoroutineScope = rememberCoroutineScope()
            androidx.activity.compose.BackHandler {
                onShowExportDialogChange(false)
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) Color.Black else PalBackground)
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
                val shutterBottomMargin = 100.dp
                val shutterSize = 59.dp * scale
                val cardBottomPadding = shutterBottomMargin + (shutterSize / 2f)

                val progressWidth = 6.5.dp * scale
                var cameraWidth = screenWidth - 4.dp - (progressWidth * 2)
                var cameraHeight = cameraWidth * (16f / 9f)

                val maxAllowedHeight = screenHeight - cardBottomPadding - 30.dp
                if (cameraHeight > maxAllowedHeight) {
                    cameraHeight = maxAllowedHeight
                    cameraWidth = cameraHeight * (9f / 16f)
                }

                val hasVlogs = capturedVlogsPaths.isNotEmpty()

                // Camera Viewfinder Box (9:16 rounded card) aligned exactly as in the main camera menu
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = cardBottomPadding)
                        .width(cameraWidth)
                        .height(cameraHeight)
                        .background(selectedProfileColor.copy(alpha = 0.15f), RoundedCornerShape(29.dp * scale)) // soft glow layer
                        .padding(1.dp * scale) // thin inset
                ) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp * scale,
                                color = selectedProfileColor.copy(alpha = 0.15f), // light glow line
                                shape = RoundedCornerShape(28.dp * scale)
                            ),
                        borderRadius = 28.dp * scale,
                        isDark = isDark,
                        gradientColors = if (isDark) listOf(Color(0xFF161616), Color(0xFF161616)) else listOf(Color(0xFFEBEBEB), Color(0xFFEBEBEB)),
                        borderColor = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp * scale))
                        ) {
                            if (!hasVlogs) {
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
                                            view.apply {
                                                player = vlogExoPlayer
                                                useController = false
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                                                
                                                vlogExoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                                        super.onVideoSizeChanged(videoSize)
                                                        val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                                        val containerWidth = width.toFloat()
                                                        val containerHeight = height.toFloat()
                                                        if (containerWidth > 0f && containerHeight > 0f) {
                                                            if (videoSize.height > videoSize.width) {
                                                                val scaleX = containerHeight / containerWidth
                                                                val scaleY = containerWidth / containerHeight
                                                                textureView.scaleX = scaleX
                                                                textureView.scaleY = scaleY
                                                                textureView.rotation = 270f
                                                            } else {
                                                                textureView.scaleX = 1.0f
                                                                textureView.scaleY = 1.0f
                                                                textureView.rotation = 0f
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    val activeIndex = currentPlayingIndex.coerceIn(0, capturedVlogsPaths.lastIndex.coerceAtLeast(0))
                                    val currentCaption = capturedVlogsCaptions.getOrNull(activeIndex) ?: ""
                                    val currentTime = capturedVlogsTimes.getOrNull(activeIndex) ?: ""

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
                        }
                    }
                }

                // Space below the viewfinder containing the 4 buttons exactly 40.dp below the frame
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(cardBottomPadding)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

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
                            label = "close",
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
                            onClick = {}
                        )

                        ExportMenuButton(
                            icon = {
                                if (isExportSaving) {
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
                                if (!isExportSaving && !isExportSaved && capturedVlogsPaths.isNotEmpty()) {
                                    isExportSaving = true
                                    localCoroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        val tempOut = java.io.File(context.cacheDir, "temp_export_save_${System.currentTimeMillis()}.mp4")
                                        val reversedPaths = capturedVlogsPaths.reversed()
                                        val reversedTimes = capturedVlogsTimes.reversed()
                                        val reversedCaptions = capturedVlogsCaptions.reversed()

                                        VideoProcessor.processVideoList(
                                            context = context,
                                            inputPaths = reversedPaths,
                                            outputPath = tempOut.absolutePath,
                                            vlogTexts = List(reversedPaths.size) { "vlog" },
                                            timeTexts = reversedTimes,
                                            captionTexts = reversedCaptions,
                                            roundedCorners = false
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
                                            isExportSaving = false
                                        }
                                    }
                                }
                            }
                        )

                        ExportMenuButton(
                            icon = {
                                UpwardArrowIcon(
                                    tint = if (isDark) Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = "share",
                            isPrimary = true,
                            isDark = isDark,
                            onClick = {}
                        )
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
                contentDescription = "PAL Small Yellow Cloud Logo",
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
                contentDescription = "PAL Small Yellow Cloud Logo",
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
                contentDescription = "PAL Small Yellow Cloud Logo",
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "creating account",
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

    var permissionSubStep by rememberSaveable {
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
                contentDescription = "PAL Small Yellow Cloud Logo",
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
                    color = Color(0xFF00E676),
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
                    color = Color(0xFF00E676),
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
                    color = Color(0xFF00E676),
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
                val cleanPath = when {
                    videoPath.startsWith("file://") -> videoPath.substring(7)
                    else -> videoPath
                }
                retriever.setDataSource(cleanPath)
                val bmp = retriever.getFrameAtTime(0)
                retriever.release()
                bitmap = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.DarkGray),
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
fun VideoPlayerItem(videoPath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val localPlayer = remember(videoPath) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            volume = 0f
            val cleanPath = when {
                videoPath.startsWith("file://") -> videoPath.substring(7)
                else -> videoPath
            }
            val file = java.io.File(cleanPath)
            if (file.exists()) {
                setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.fromFile(file)))
                prepare()
            }
        }
    }

    DisposableEffect(localPlayer) {
        onDispose {
            localPlayer.release()
        }
    }

    LaunchedEffect(localPlayer) {
        localPlayer.playWhenReady = true
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                val view = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                view.apply {
                    player = localPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                    localPlayer.addListener(object : androidx.media3.common.Player.Listener {
                        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                            super.onVideoSizeChanged(videoSize)
                            val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                            val containerWidth = width.toFloat()
                            val containerHeight = height.toFloat()
                            if (containerWidth > 0f && containerHeight > 0f) {
                                if (videoSize.height > videoSize.width) {
                                    val scaleX = containerHeight / containerWidth
                                    val scaleY = containerWidth / containerHeight
                                    textureView.scaleX = scaleX
                                    textureView.scaleY = scaleY
                                    textureView.rotation = 270f
                                } else {
                                    textureView.scaleX = 1.0f
                                    textureView.scaleY = 1.0f
                                    textureView.rotation = 0f
                                }
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
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
    onActiveReplyPreviewPathChange: (String?) -> Unit
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
                        Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
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
fun PalChatOverlay(
    showChat: Boolean,
    pal: PalDbItem,
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
    onSendMessage: (String) -> Unit
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
        val context = LocalContext.current
        val defaultEmojis = remember { listOf("😂", "❤️", "😭", "✨", "🥺", "🔥", "🥰", "🎉", "💀", "👍", "🙏", "💯", "😎", "👀") }
        var currentEmojis by remember { mutableStateOf(defaultEmojis.take(5)) }

        val feedItems = remember(pal.code, capturedVlogsPaths, allPalsSubmissions, currentUserId) {
            if (pal.isVlog) {
                capturedVlogsPaths.mapIndexedNotNull { idx, path ->
                    val cleanPath = if (path.startsWith("file://")) path.substring(7) else path
                    val file = java.io.File(cleanPath)
                    if (file.exists()) {
                        val instant = java.time.Instant.ofEpochMilli(file.lastModified())
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
                .sortedBy { it.rawInstant }
            } else {
                val subs = allPalsSubmissions[pal.code] ?: emptyList()
                subs.mapNotNull { sub ->
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
                .sortedBy { it.rawInstant }
            }
        }

        val groupedByDay = remember(feedItems) {
            feedItems.groupBy { it.localDate }
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
                        .padding(top = 64.dp, bottom = 80.dp),
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
                        .padding(top = 64.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    groupedByDay.keys.sorted().forEach { dayDate ->
                        val dayFeed = groupedByDay[dayDate] ?: emptyList()
                        val today = java.time.LocalDate.now()
                        val dayLabel = when (dayDate) {
                            today -> "Today"
                            today.minusDays(1) -> "Yesterday"
                            else -> dayDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d", java.util.Locale.US))
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            dayFeed.forEach { feedItem ->
                                val headerText = "$dayLabel ${feedItem.timeStr}"

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // 2. Aligned message body
                                    if (feedItem.isUser) {
                                        // USER (Right Aligned)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 17.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "apple_user",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = textColor.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(bottom = 2.dp, end = 4.dp)
                                            )

                                            VideoPlayerItem(
                                                videoPath = feedItem.path,
                                                modifier = Modifier
                                                    .width(210.dp)
                                                    .height(125.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                            )

                                            val reactedEmoji = palReactions[feedItem.path]
                                            if (reactedEmoji != null) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = reactedEmoji,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.align(Alignment.CenterHorizontally)
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
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                val cleanName = feedItem.userDisplayName.trim().substringBefore(" ").substringBefore("_").substringBefore(".")
                                                Text(
                                                    text = cleanName,
                                                    color = textColor,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.SansSerif
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.width(210.dp)
                                            ) {
                                                VideoPlayerItem(
                                                    videoPath = feedItem.path,
                                                    modifier = Modifier
                                                        .width(210.dp)
                                                        .height(125.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = "Reply",
                                                        tint = textColor.copy(alpha = 0.8f),
                                                        modifier = Modifier
                                                            .graphicsLayer(scaleX = -1f)
                                                            .size(20.dp)
                                                            .clickable {
                                                                onActiveReplyPreviewPathChange(feedItem.path)
                                                            }
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.FavoriteBorder,
                                                        contentDescription = "Love",
                                                        tint = textColor.copy(alpha = 0.8f),
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clickable {
                                                                showEmojiOverlayForPath = feedItem.path
                                                            }
                                                    )
                                                    
                                                    val reactedEmoji = palReactions[feedItem.path]
                                                    if (reactedEmoji != null) {
                                                        Text(text = reactedEmoji, fontSize = 20.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. View Log Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F3EB))
                                    .border(1.dp, selectedProfileColor, RoundedCornerShape(12.dp))
                                    .clickable {
                                        Toast.makeText(context, "view log clicked for $dayLabel", Toast.LENGTH_SHORT).show()
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
                                    text = "view log",
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

            // Emoji Reaction Overlay
            if (showEmojiOverlayForPath != null) {
                val path = showEmojiOverlayForPath!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showEmojiOverlayForPath = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                    drawCircle(color = Color.White.copy(alpha = 0.6f), style = stroke)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Shuffle",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
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
                        .size(40.dp)
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

                Text(
                    text = pal.name,
                    fontFamily = BricolageVariableFontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 2.dp)
                )
            }

            // 3. Footer Row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161616) else Color(0xFFEBEBEB))
                        .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                        .clickable {
                            onNavigateToCamera()
                        },
                    contentAlignment = Alignment.Center
                ) {
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
                        modifier = Modifier.fillMaxWidth(),
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
                            onSendMessage(messageInput.trim())
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
    supabaseClient: io.github.jan.supabase.SupabaseClient
) {
    if (showJoinPalFlow) {
        androidx.activity.compose.BackHandler {
            onShowJoinPalFlowChange(false)
        }

        val localScope = rememberCoroutineScope()
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
                            onValueChange = onJoinPalCodeChange,
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
                                    val code = joinPalCode.trim()
                                    localScope.launch {
                                        try {
                                            val matchedPalDb = supabaseClient.postgrest.from("pals")
                                                .select {
                                                    filter {
                                                        eq("code", code)
                                                    }
                                                }
                                                .decodeSingleOrNull<PalDbItem>()

                                            if (matchedPalDb != null) {
                                                val newMapping = UserPalMapping(
                                                    userId = currentUserId,
                                                    palCode = code
                                                )
                                                supabaseClient.postgrest.from("user_pals").insert(newMapping)

                                                val matchedItem = PalItem(
                                                    name = matchedPalDb.name,
                                                    size = matchedPalDb.size,
                                                    code = matchedPalDb.code,
                                                    isVlog = matchedPalDb.isVlog,
                                                    isCreator = matchedPalDb.creatorId == currentUserId
                                                )

                                                if (!createdPals.any { it.code == code }) {
                                                    onCreatedPalsChange(createdPals + matchedItem)
                                                }
                                                refreshPals()
                                                onShowJoinPalFlowChange(false)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
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
    supabaseClient: io.github.jan.supabase.SupabaseClient
) {
    if (showCreatePalFlow) {
        androidx.activity.compose.BackHandler {
            if (!isCreatingPal) {
                onShowCreatePalFlowChange(false)
            }
        }
        val localScope = rememberCoroutineScope()
        val context = androidx.compose.ui.platform.LocalContext.current

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
                // Header aligned top like Edit Pal Screen (Image 5 style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(64.dp)
                ) {
                    // Close button on the left (solid dark circle, size 40.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E1D22) else Color(0xFFE5E5EA))
                            .clickable {
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

                    // Center PAL Text logo (same color as top left on pals menu, Ownglyph font)
                    Text(
                        text = "PAL",
                        fontFamily = OwnglyphFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = palTextLogoColor,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Top right checkmark exactly as vlog menu's edit pal settings checkmark (filled with accentColor border edge color)
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
                                .clickable(enabled = isFormValid) {
                                    onIsCreatingPalChange(true)
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
                    // Monospace font style matching the mock image
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
                        // Row 1: 2 to 7
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
                                        .clickable { onNewPalSizeChange(size) },
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

                        // Row 2: 8 to 10
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
                                        .clickable { onNewPalSizeChange(size) },
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
                        text = "# $generatedPalCode",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(38.dp))

                    Text(
                        text = "invite a friend →",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my pal code: $generatedPalCode")
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
                                val newItem = PalItem(
                                    name = newPalName,
                                    size = newPalSize,
                                    code = generatedPalCode,
                                    isVlog = false,
                                    isCreator = true
                                )
                                localScope.launch {
                                    try {
                                        val newPalDb = PalDbItem(
                                            code = generatedPalCode,
                                            name = newPalName,
                                            size = newPalSize,
                                            isVlog = false,
                                            creatorId = currentUserId
                                        )
                                        supabaseClient.postgrest.from("pals").insert(newPalDb)
                                        
                                        val newMapping = UserPalMapping(
                                            userId = currentUserId,
                                            palCode = generatedPalCode
                                        )
                                        supabaseClient.postgrest.from("user_pals").insert(newMapping)
                                        
                                        onCreatedPalsChange(createdPals + newItem)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        onCreatedPalsChange(createdPals + newItem)
                                        groupDatabase[generatedPalCode] = newItem
                                    }
                                }
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
    onTripleDotMenuBoundsChange: (Rect) -> Unit
) {
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

        val isSubmenuColorOrNotifications = tripleDotScreen == TripleDotScreen.COLOR_SELECTION || tripleDotScreen == TripleDotScreen.PAL_NOTIFICATIONS
        val cardBgColor = if (isDark) {
            if (isSubmenuColorOrNotifications) Color(0xFF2E2B36) else Color(0xFF161616)
        } else {
            if (isSubmenuColorOrNotifications) Color(0xFFE5E5E5) else Color(0xFFF5F3EB)
        }

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
                                "edit profile" to { onTripleDotScreenChange(TripleDotScreen.EDIT_PROFILE) },
                                "pal notifications" to { onTripleDotScreenChange(TripleDotScreen.PAL_NOTIFICATIONS) },
                                "settings" to { onTripleDotScreenChange(TripleDotScreen.SETTINGS) },
                                "feedback" to { onShowTripleDotMenuChange(false) }
                            )

                            mainOptions.forEach { (title, onClick) ->
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
                                    modifier = Modifier.clickable {
                                        onShowTripleDotMenuChange(false)
                                        onTripleDotScreenChange(TripleDotScreen.MAIN)
                                    }
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
                                        fontWeight = FontWeight.Bold,
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
                                        fontWeight = FontWeight.Bold,
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
                                        onTripleDotScreenChange(TripleDotScreen.EDIT_PROFILE)
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
                                        onTripleDotScreenChange(TripleDotScreen.EDIT_PROFILE)
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
                                            onNotificationIntervalChange(option)
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CustomRadioButton(
                                        selected = notificationInterval == option,
                                        color = textColor,
                                        onClick = { onNotificationIntervalChange(option) }
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
                                        fontWeight = FontWeight.Bold,
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
    capturedVlogsPaths: List<String>,
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

    val capturedDates = remember(capturedVlogsPaths) {
        capturedVlogsPaths.mapNotNull { path ->
            val regex = Regex("\\d{13}")
            val match = regex.find(path)
            val dateFromTimestamp = if (match != null) {
                try {
                    val millis = match.value.toLong()
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            if (dateFromTimestamp != null) {
                dateFromTimestamp
            } else {
                val cleanPath = when {
                    path.startsWith("file://") -> path.substring(7)
                    else -> path
                }
                val file = java.io.File(cleanPath)
                if (file.exists()) {
                    val lastModified = file.lastModified()
                    val instant = java.time.Instant.ofEpochMilli(lastModified)
                    instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                } else {
                    null
                }
            }
        }.toSet()
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
        gradientColors = if (isDark) listOf(Color(0xFF161616), Color(0xFF161616)) else listOf(Color(0xFFFFF1EB), Color(0xFFFFF1EB)),
        borderColor = if (isDark) accentColor.copy(alpha = 0.3f) else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "archive",
                    fontFamily = BricolageVariableFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

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
                                    val isToday = date == java.time.LocalDate.now()
                                    val hasCaptured = capturedDates.contains(date)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isToday) accentColor else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isToday) Color.White else textColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (hasCaptured) {
                                            Box(
                                                modifier = Modifier
                                                    .requiredSize(15.dp)
                                                    .aspectRatio(1f)
                                                    .clip(CircleShape)
                                                    .background(selectedProfileColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.smile_small),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(1.5.dp)
                                                        .rotate(-180f),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.requiredSize(15.dp))
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
                        .clickable { onDismiss() }
                        .padding(8.dp)
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
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    val firstName = currentDisplayName.substringBefore(" ")
                    val vlogEmptyTextColor = if (isDark) Color(0xFF8E8E93) else textColor
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
                    val vlogEmptyTextColor = if (isDark) Color(0xFF8E8E93) else textColor
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
                        val vlogEmptyCaptureColor = if (isDark) Color(0xFF8E8E93) else Color.Black
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



