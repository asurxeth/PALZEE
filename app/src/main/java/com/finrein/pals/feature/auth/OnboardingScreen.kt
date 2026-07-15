package com.finrein.pals.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finrein.pals.R
import com.finrein.pals.core.ui.theme.*
import kotlinx.coroutines.launch
import com.finrein.pals.PalApplication
import com.finrein.pals.BuildConfig
import android.os.Build
import androidx.compose.ui.platform.LocalDensity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.ClearCredentialStateRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.gotrue.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (com.finrein.pals.core.domain.model.User) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
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

    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // Dialog control states for Email and Passkey tracks
    var showPasskeyNameDialog by remember { mutableStateOf(false) }
    var showEmailOtpDialog by remember { mutableStateOf(false) }
    
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Backup email sheet states
    var showBackupEmailSheet by remember { mutableStateOf(false) }
    var backupEmailInput by remember { mutableStateOf("") }
    var backupOtpInput by remember { mutableStateOf("") }
    var backupFlowStep by remember { mutableStateOf(1) } // 1: Email, 2: Code
    var backupErrorMessage by remember { mutableStateOf<String?>(null) }
    val backupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isTextFieldFocused by remember { mutableStateOf(false) }

    // Central theme index (0 to 4) representing the 5 off-white themes
    val currentThemeIndex by remember { mutableStateOf(0) }

    // Gradients for dark mode matching the temperature/hue of the light mode themes
    val themeDarkGradients = remember {
        listOf(
            Brush.verticalGradient(listOf(Color(0xFF1E1C15), Color(0xFF14120E))), // Dark warm cream
            Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF121212))), // Dark grey
            Brush.verticalGradient(listOf(Color(0xFF1F1B12), Color(0xFF15120B))), // Dark warm yellow
            Brush.verticalGradient(listOf(Color(0xFF221A18), Color(0xFF16100F))), // Dark warm peach
            Brush.verticalGradient(listOf(Color(0xFF24191C), Color(0xFF181012)))  // Dark warm pink
        )
    }

    val backgroundColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFFAF9F6)

    // Unpack success flow
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val success = uiState as AuthUiState.Success
            if (success.user != null) {
                showEmailOtpDialog = false
                showPasskeyNameDialog = false
                showBackupEmailSheet = false
                onAuthSuccess(success.user)
            }
        }
    }

    // Email OTP flow coordinator
    LaunchedEffect(uiState, showEmailOtpDialog) {
        if (showEmailOtpDialog) {
            if (uiState is AuthUiState.Success) {
                val success = uiState as AuthUiState.Success
                if (success.user != null) {
                    showEmailOtpDialog = false
                } else if (!isOtpSent) {
                    isOtpSent = true
                    viewModel.resetState()
                }
            }
        }
    }

    // Backup email flow coordinator
    LaunchedEffect(uiState, showBackupEmailSheet) {
        if (showBackupEmailSheet) {
            when (uiState) {
                is AuthUiState.Success -> {
                    val success = uiState as AuthUiState.Success
                    if (success.user != null) {
                        // Successfully logged in! Close sheet
                        showBackupEmailSheet = false
                    } else if (backupFlowStep == 1) {
                        // Code sent successfully, transition to step 2
                        backupFlowStep = 2
                        backupErrorMessage = null
                        viewModel.resetState() // clear loading/success to restore default visual state
                    }
                }
                is AuthUiState.Error -> {
                    val error = uiState as AuthUiState.Error
                    if (backupFlowStep == 2) {
                        // Verification failed
                        backupErrorMessage = "Wrong code or code expired."
                    } else {
                        // Sending code failed
                        backupErrorMessage = error.exceptionMessage
                    }
                }
                else -> {}
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(screenCornerRadius))
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(37.dp))

            // 1. Top Section Box: Contains Cloud, Envelope, Moon, and the 5 Stars
            // Positioned exactly as they are in the image (asymmetric hand-drawn scattered collage style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Cloud Logo in the middle
                Image(
                    painter = painterResource(id = R.drawable.onboarding_logo),
                    contentDescription = "Pal Yellow Cloud Logo",
                    modifier = Modifier.size(130.dp).offset(y = 20.dp),
                    contentScale = ContentScale.Fit
                )
                // Envelope on left
                Image(
                    painter = painterResource(id = R.drawable.dm_envalope),
                    contentDescription = "Envelope with heart",
                    modifier = Modifier.offset(x = (-100).dp, y = 35.dp).size(50.dp)
                )
                // Moon on right
                Image(
                    painter = painterResource(id = R.drawable.dm_moon),
                    contentDescription = "Crescent Moon",
                    modifier = Modifier.offset(x = 100.dp, y = 35.dp).size(55.dp)
                )
                // Stars above the cloud, hand-spaced
                Image(
                    painter = painterResource(id = R.drawable.dm_star_1),
                    contentDescription = "Star 1",
                    modifier = Modifier.offset(x = (-115).dp, y = (-40).dp).size(36.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dm_star_2),
                    contentDescription = "Star 2",
                    modifier = Modifier.offset(x = (-55).dp, y = (-65).dp).size(45.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dm_star_3),
                    contentDescription = "Star 3",
                    modifier = Modifier.offset(x = 15.dp, y = (-75).dp).size(30.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dm_star_4),
                    contentDescription = "Star 4",
                    modifier = Modifier.offset(x = 85.dp, y = (-65).dp).size(45.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dm_star_5),
                    contentDescription = "Star 5",
                    modifier = Modifier.offset(x = 135.dp, y = (-30).dp).size(34.dp)
                )
            }

            // 2. Title Section (using OwnglyphFontFamily for thin line weight)
            Text(
                text = "PAL",
                fontFamily = OwnglyphFontFamily,
                fontSize = 36.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier.offset(y = 0.dp)
            )

            // 3. Middle Section Box: Contains Pizza, Orange, and Plant
            // Positioned asymmetrically (matching the visual scatter look of the image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .offset(y = (-10).dp),
                contentAlignment = Alignment.Center
            ) {
                // Pizza slice on the left, rotated, placed higher and shifted left by 5dp
                Image(
                    painter = painterResource(id = R.drawable.dm_pizza),
                    contentDescription = "Pizza Slice",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-15).dp, y = (-5).dp)
                        .size(105.dp)
                        .rotate(-15f)
                )
                // Orange in the middle, placed lower
                Image(
                    painter = painterResource(id = R.drawable.dm_orange),
                    contentDescription = "Orange Doodle",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-25).dp, y = 25.dp)
                        .size(45.dp)
                )
                // Potted plant on the right, placed higher and pushed outside of the view to let branches overflow
                Image(
                    painter = painterResource(id = R.drawable.dm_plant),
                    contentDescription = "Potted Plant",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 34.dp, y = (-15).dp)
                        .size(160.dp)
                )
            }

            val passkeyBg = if (isDark) Color(0xFF1C1C1C) else Color(0xFFCBDCA7)
            val passkeyText = if (isDark) Color.White else Color(0xFF1E1C1A)
            val passkeyBorder = if (isDark) Color(0xFF444444) else null

            val googleBg = if (isDark) Color(0xFFCBDCA7) else Color.White
            val googleText = Color(0xFF1E1C1A)
            val googleBorder = if (isDark) Color.Transparent else Color(0xFFE2DDD5)

            val emailBg = if (isDark) Color(0xFFFCD9BE) else Color(0xFFE8B093)
            val emailText = Color(0xFF1E1C1A)

            // 4. Buttons Section (stacked vertically)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .offset(y = (-10).dp)
            ) {
                // Button 1: Continue with Passkey
                OnboardingButton(
                    text = "Continue with passkey",
                    iconResId = R.drawable.ic_passkey_custom_light,
                    backgroundColor = passkeyBg,
                    contentColor = passkeyText,
                    borderColor = passkeyBorder,
                    onClick = {
                        firstNameInput = ""
                        emailInput = ""
                        viewModel.resetState()
                        showPasskeyNameDialog = true
                    }
                )

                // Button 2: Continue with Google
                OnboardingButton(
                    text = "Continue with Google",
                    iconResId = R.drawable.ic_google_custom_light,
                    backgroundColor = googleBg,
                    contentColor = googleText,
                    borderColor = googleBorder,
                    onClick = {
                        coroutineScope.launch {
                            try {
                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            } catch (e: Exception) {
                                android.util.Log.e("Auth", "Failed to clear state: ${e.message}")
                            }

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            try {
                                 val result = credentialManager.getCredential(
                                     context = context,
                                     request = request
                                 )
                                 val credential = result.credential
                                 if (credential is CustomCredential && 
                                     credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                     
                                     val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                     val userEmail = googleIdTokenCredential.id
                                     val displayName = googleIdTokenCredential.displayName ?: "Google User"
                                     val idToken = googleIdTokenCredential.idToken
                                     
                                     viewModel.setLoading()
                                     val routeState = viewModel.authenticateAndRouteUser(idToken)
                                     val sessionManager = com.finrein.pals.core.data.local.SessionManager(context)
                                     val currentUserId = com.finrein.pals.PalApplication.supabase.auth.currentUserOrNull()?.id ?: "google_user_12345"
                                     val user = com.finrein.pals.core.domain.model.User(
                                         id = currentUserId,
                                         email = userEmail,
                                         displayName = displayName,
                                         isPasskeyRegistered = false
                                     )
                                     
                                     when (routeState) {
                                         com.finrein.pals.core.domain.repository.UserRouteState.RETURNING_USER -> {
                                            sessionManager.setHasLoggedInBefore(true)
                                            sessionManager.setFirstLogin(false)
                                            viewModel.setSuccess("Google handshake complete!", user)
                                         }
                                         com.finrein.pals.core.domain.repository.UserRouteState.NEW_USER -> {
                                            sessionManager.setHasLoggedInBefore(false)
                                            sessionManager.setFirstLogin(true)
                                            viewModel.setSuccess("Google handshake complete!", user)
                                         }
                                         com.finrein.pals.core.domain.repository.UserRouteState.ERROR -> {
                                            viewModel.setError("Google sign in or database sync failed.")
                                         }
                                     }
                                 }
                             } catch (e: GetCredentialException) {
                                android.util.Log.e("AuthError", "Sign in dialog failed: ${e.message}")
                            }
                        }
                    }
                )

                // Button 3: Continue with Email
                OnboardingButton(
                    text = "Continue with Email",
                    iconResId = R.drawable.ic_email_custom_light,
                    backgroundColor = emailBg,
                    contentColor = emailText,
                    onClick = {
                        emailInput = ""
                        otpInput = ""
                        isOtpSent = false
                        viewModel.resetState()
                        showEmailOtpDialog = true
                    }
                )
            }

            // 5. Bottom Section Box: Contains "having trouble signing in?", Tea, Fire, and Bingsu
            // Spaced and integrated naturally according to the image collage layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Help text
                Text(
                    text = "having trouble signing in?",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    textDecoration = TextDecoration.Underline,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                        .padding(top = 10.dp)
                        .clickable {
                            backupEmailInput = ""
                            backupOtpInput = ""
                            backupFlowStep = 1
                            backupErrorMessage = null
                            isTextFieldFocused = false
                            viewModel.resetState()
                            showBackupEmailSheet = true
                        }
                )

                // Tea cup on bottom left, extending upwards next to help text
                Image(
                    painter = painterResource(id = R.drawable.dm_tea),
                    contentDescription = "Tea Cup",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = (-5).dp)
                        .size(80.dp)
                )

                // Flames in bottom center, below help text
                Image(
                    painter = painterResource(id = R.drawable.dm_fire),
                    contentDescription = "Flames",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-13).dp)
                        .size(50.dp)
                )

                // Bingsu on bottom right, extending upwards next to help text
                Image(
                    painter = painterResource(id = R.drawable.dm_bingsu),
                    contentDescription = "Bingsu",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-10).dp, y = (-5).dp)
                        .size(80.dp)
                )
            }
        }

        // ----------------------------------------------------
        // 3. OVERLAYS & STATE PRESENTATION (Loading & Error)
        // ----------------------------------------------------
        AnimatedVisibility(
            visible = uiState is AuthUiState.Loading && !showEmailOtpDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {}, // Scrim block
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PalCloudYellow,
                    strokeWidth = 4.dp
                )
            }
        }

        if (uiState is AuthUiState.Error) {
            val errorMessage = (uiState as AuthUiState.Error).exceptionMessage
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text(
                            text = "OK",
                            color = if (isDark) PalWhite else PalTextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                title = { Text("Authentication Error", style = Typography.titleLarge) },
                text = { Text(errorMessage, style = Typography.bodyMedium) },
                containerColor = if (isDark) Color(0xFF1E1E1E) else PalBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(
                    width = 3.dp,
                    color = if (isDark) PalWhite else PalBlack,
                    shape = RoundedCornerShape(16.dp)
                )
            )
        }

        if (showEmailOtpDialog) {
            Dialog(
                onDismissRequest = {
                    showEmailOtpDialog = false
                    viewModel.resetState()
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight()
                        .background(backgroundColor, shape = RoundedCornerShape(24.dp))
                        .border(
                            width = 2.dp,
                            color = if (isDark) PalWhite else PalBlack,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isOtpSent) {
                                IconButton(
                                    onClick = {
                                        isOtpSent = false
                                        otpInput = ""
                                        viewModel.resetState()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = if (isDark) PalWhite else PalTextDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }

                            Text(
                                text = if (!isOtpSent) "Sign in With Email" else "Check your email",
                                fontFamily = BricolageVariableFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isDark) PalWhite else PalTextDark,
                                modifier = Modifier.offset(x = (-10).dp)
                            )

                            IconButton(
                                onClick = {
                                    showEmailOtpDialog = false
                                    viewModel.resetState()
                                },
                                modifier = Modifier
                                    .size(21.dp)
                                    .offset(x = (-7).dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDark) PalWhite else PalTextDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {


                            Text(
                                text = if (!isOtpSent) {
                                    "Enter your email to get a sign in code"
                                } else {
                                    "We sent a code to $emailInput"
                                },
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TextField(
                                value = if (!isOtpSent) emailInput else otpInput,
                                onValueChange = {
                                    if (!isOtpSent) emailInput = it else otpInput = it
                                },
                                textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(0.12f)),
                                placeholder = {
                                    Text(
                                        text = if (!isOtpSent) "Email address" else "Verification code",
                                        color = Color.Gray,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = if (isDark) PalWhite else PalTextDark,
                                    unfocusedTextColor = if (isDark) PalWhite else PalTextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (uiState is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = if (isDark) PalWhite else Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        val textVal = if (!isOtpSent) emailInput else otpInput
                                        if (textVal.isNotEmpty()) {
                                            IconButton(onClick = { if (!isOtpSent) emailInput = "" else otpInput = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val isEnabled = if (uiState is AuthUiState.Loading) {
                                 false
                             } else if (!isOtpSent) {
                                 android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                             } else {
                                 otpInput.isNotEmpty()
                             }

                            val buttonBgColor = if (isEnabled) {
                                if (isDark) PalWhite else Color.Black
                            } else {
                                if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                            }

                            val buttonContentColor = if (isEnabled) {
                                if (isDark) Color.Black else Color.White
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.3f) else Color.White
                            }

                            Button(
                                onClick = {
                                    if (!isOtpSent) {
                                        viewModel.sendEmailVerificationCode(emailInput)
                                    } else {
                                        coroutineScope.launch {
                                            viewModel.setLoading()
                                            val routeState = viewModel.verifyOtpAndRouteUser(emailInput, otpInput)
                                            val sessionManager = com.finrein.pals.core.data.local.SessionManager(context)
                                            val currentUserId = com.finrein.pals.PalApplication.supabase.auth.currentUserOrNull()?.id ?: "email_user_67890"
                                            val user = com.finrein.pals.core.domain.model.User(
                                                id = currentUserId,
                                                email = emailInput,
                                                displayName = emailInput.substringBefore("@"),
                                                isPasskeyRegistered = false
                                            )
                                            when (routeState) {
                                                com.finrein.pals.core.domain.repository.UserRouteState.RETURNING_USER -> {
                                                    sessionManager.setHasLoggedInBefore(true)
                                                    sessionManager.setFirstLogin(false)
                                                    showEmailOtpDialog = false
                                                    onAuthSuccess(user)
                                                }
                                                com.finrein.pals.core.domain.repository.UserRouteState.NEW_USER -> {
                                                    sessionManager.setHasLoggedInBefore(false)
                                                    sessionManager.setFirstLogin(true)
                                                    showEmailOtpDialog = false
                                                    onAuthSuccess(user)
                                                }
                                                com.finrein.pals.core.domain.repository.UserRouteState.ERROR -> {
                                                    viewModel.setError("Email verification failed or database is unreachable.")
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = isEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonBgColor,
                                    contentColor = buttonContentColor,
                                    disabledContainerColor = buttonBgColor,
                                    disabledContentColor = buttonContentColor
                                ),
                                shape = RoundedCornerShape(30.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (!isOtpSent) Icons.AutoMirrored.Filled.Send else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (!isOtpSent) "Send code" else "Verify",
                                        fontFamily = BricolageVariableFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPasskeyNameDialog) {
            Dialog(
                onDismissRequest = {
                    showPasskeyNameDialog = false
                    viewModel.resetState()
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight()
                        .background(backgroundColor, shape = RoundedCornerShape(24.dp))
                        .border(
                            width = 2.dp,
                            color = if (isDark) PalWhite else PalBlack,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.size(36.dp))

                            Text(
                                text = "Passkey",
                                fontFamily = BricolageVariableFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isDark) PalWhite else PalTextDark
                            )

                            IconButton(
                                onClick = {
                                    showPasskeyNameDialog = false
                                    viewModel.resetState()
                                },
                                modifier = Modifier
                                    .size(21.dp)
                                    .offset(x = (-7).dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDark) PalWhite else PalTextDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {


                            Text(
                                text = "Enter Your Name and Email",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Input 1: Enter Your name
                            TextField(
                                value = firstNameInput,
                                onValueChange = { firstNameInput = it },
                                textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(0.12f)),
                                placeholder = {
                                    Text(
                                        text = "Enter Your name",
                                        color = Color.Gray,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = if (isDark) PalWhite else PalTextDark,
                                    unfocusedTextColor = if (isDark) PalWhite else PalTextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (firstNameInput.isNotEmpty()) {
                                        IconButton(onClick = { firstNameInput = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Input 2: Enter Your Email
                            TextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(0.12f)),
                                placeholder = {
                                    Text(
                                        text = "Enter Your Email",
                                        color = Color.Gray,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = if (isDark) PalWhite else PalTextDark,
                                    unfocusedTextColor = if (isDark) PalWhite else PalTextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (emailInput.isNotEmpty()) {
                                        IconButton(onClick = { emailInput = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val isEnabled = firstNameInput.isNotBlank() && 
                                    android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()

                            val buttonBgColor = if (isEnabled) {
                                if (isDark) PalWhite else Color.Black
                            } else {
                                if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                            }

                            val buttonContentColor = if (isEnabled) {
                                if (isDark) Color.Black else Color.White
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.3f) else Color.White
                            }

                            Button(
                                onClick = {
                                    // Save the passkey binding locally
                                    val sharedPrefs = context.getSharedPreferences("pal_passkey_prefs", android.content.Context.MODE_PRIVATE)
                                    sharedPrefs.edit()
                                        .putBoolean("passkey_registered_$emailInput", true)
                                        .putString("passkey_name_$emailInput", firstNameInput)
                                        .putString("passkey_user_id_$emailInput", java.util.UUID.randomUUID().toString())
                                        .apply()

                                    showPasskeyNameDialog = false
                                    viewModel.registerTemporaryPasskeyUser(firstNameInput, emailInput)
                                },
                                enabled = isEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonBgColor,
                                    contentColor = buttonContentColor,
                                    disabledContainerColor = buttonBgColor,
                                    disabledContentColor = buttonContentColor
                                ),
                                shape = RoundedCornerShape(30.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Register Profile",
                                        fontFamily = BricolageVariableFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBackupEmailSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBackupEmailSheet = false
                    viewModel.resetState()
                },
                sheetState = backupSheetState,
                containerColor = Color.Transparent,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = null
            ) {
                val heightModifier = if (isTextFieldFocused) {
                    Modifier.fillMaxHeight(0.85f)
                } else {
                    Modifier.wrapContentHeight()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(heightModifier)
                        .background(backgroundColor, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Drag Handle inside the gradient Column
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(41.dp)
                            .offset(y = (-2).dp), // shifted below by 5px/dp (from -7dp to -2dp)
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back button (only in step 2)
                        if (backupFlowStep == 2) {
                            IconButton(
                                onClick = {
                                    backupFlowStep = 1
                                    backupOtpInput = ""
                                    backupErrorMessage = null
                                    viewModel.resetState()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) PalWhite else PalTextDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Empty space to preserve center alignment of title
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        // Title
                        Text(
                            text = "Use Backup Email",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) PalWhite else PalTextDark
                        )

                        // Close button
                        IconButton(
                            onClick = {
                                showBackupEmailSheet = false
                                viewModel.resetState()
                            },
                            modifier = Modifier
                                .size(25.dp)
                                .offset(x = (-7).dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isDark) PalWhite else PalTextDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(9.dp)) // Reduced by 15px/dp (from 24dp to 9dp) to move title and menu up by 15px

                    // Content Column (Left Aligned text)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Main Title (reduced text size by exactly 10sp/px from 30sp to 20sp)
                        Text(
                            text = if (backupFlowStep == 1) "Sign in with a\nbackup email" else "Check your email",
                            fontFamily = BricolageVariableFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            color = if (isDark) PalWhite else PalTextDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            text = if (backupFlowStep == 1) {
                                "Enter your backup email to get a sign-in code."
                            } else {
                                "We sent a code to $backupEmailInput"
                            },
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // TextField
                        TextField(
                            value = if (backupFlowStep == 1) backupEmailInput else backupOtpInput,
                            onValueChange = {
                                if (backupFlowStep == 1) {
                                    backupEmailInput = it
                                } else {
                                    backupOtpInput = it
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(baselineShift = BaselineShift(0.12f)),
                            placeholder = {
                                Text(
                                    text = if (backupFlowStep == 1) "Email address" else "Verification code",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = if (isDark) PalWhite else PalTextDark,
                                unfocusedTextColor = if (isDark) PalWhite else PalTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                val textValue = if (backupFlowStep == 1) backupEmailInput else backupOtpInput
                                if (textValue.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            if (backupFlowStep == 1) backupEmailInput = "" else backupOtpInput = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .onFocusChanged { isTextFieldFocused = it.isFocused }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Button
                        val isEnabled = if (uiState is AuthUiState.Loading) {
                            false
                        } else if (backupFlowStep == 1) {
                            android.util.Patterns.EMAIL_ADDRESS.matcher(backupEmailInput).matches()
                        } else {
                            backupOtpInput.isNotEmpty()
                        }

                        val buttonBgColor = if (isEnabled) {
                            if (isDark) PalWhite else Color.Black
                        } else {
                            if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                        }

                        val buttonContentColor = if (isEnabled) {
                            if (isDark) Color.Black else Color.White
                        } else {
                            if (isDark) Color.White.copy(alpha = 0.3f) else Color.White
                        }

                        Button(
                            onClick = {
                                if (backupFlowStep == 1) {
                                    val sharedPrefs = context.getSharedPreferences("pal_passkey_prefs", android.content.Context.MODE_PRIVATE)
                                    val isPasskeyRegistered = sharedPrefs.getBoolean("passkey_registered_$backupEmailInput", false)
                                    if (isPasskeyRegistered) {
                                        val name = sharedPrefs.getString("passkey_name_$backupEmailInput", "Passkey User") ?: "Passkey User"
                                        val userId = sharedPrefs.getString("passkey_user_id_$backupEmailInput", java.util.UUID.randomUUID().toString()) ?: java.util.UUID.randomUUID().toString()
                                        val user = com.finrein.pals.core.domain.model.User(
                                            id = userId,
                                            email = backupEmailInput,
                                            displayName = name,
                                            isPasskeyRegistered = true
                                        )
                                        viewModel.loginWithLocalPasskey(user)
                                        showBackupEmailSheet = false
                                    } else {
                                        viewModel.sendEmailVerificationCode(backupEmailInput)
                                    }
                                 } else {
                                     coroutineScope.launch {
                                         viewModel.setLoading()
                                         val routeState = viewModel.verifyOtpAndRouteUser(backupEmailInput, backupOtpInput)
                                         val sessionManager = com.finrein.pals.core.data.local.SessionManager(context)
                                         val currentUserId = com.finrein.pals.PalApplication.supabase.auth.currentUserOrNull()?.id ?: "email_user_67890"
                                         val user = com.finrein.pals.core.domain.model.User(
                                             id = currentUserId,
                                             email = backupEmailInput,
                                             displayName = backupEmailInput.substringBefore("@"),
                                             isPasskeyRegistered = false
                                         )
                                         when (routeState) {
                                             com.finrein.pals.core.domain.repository.UserRouteState.RETURNING_USER -> {
                                                 sessionManager.setHasLoggedInBefore(true)
                                                 sessionManager.setFirstLogin(false)
                                                 showBackupEmailSheet = false
                                                 onAuthSuccess(user)
                                             }
                                             com.finrein.pals.core.domain.repository.UserRouteState.NEW_USER -> {
                                                 sessionManager.setHasLoggedInBefore(false)
                                                 sessionManager.setFirstLogin(true)
                                                 showBackupEmailSheet = false
                                                 onAuthSuccess(user)
                                             }
                                             com.finrein.pals.core.domain.repository.UserRouteState.ERROR -> {
                                                 viewModel.setError("Email verification failed or database is unreachable.")
                                             }
                                         }
                                     }
                                 }
                            },
                            enabled = isEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBgColor,
                                contentColor = buttonContentColor,
                                disabledContainerColor = buttonBgColor,
                                disabledContentColor = buttonContentColor
                            ),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (backupFlowStep == 1) Icons.AutoMirrored.Filled.Send else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (backupFlowStep == 1) "Send code" else "Verify",
                                    fontFamily = BricolageVariableFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (backupErrorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = backupErrorMessage!!,
                                color = Color.Red,
                                fontSize = 14.sp,
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
fun DoodleImage(
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun LiquidGlassButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Liquid glass gradient base
    val glassBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    }

    // Border stroke with glass edge reflection
    val glassBorder = if (isDark) {
        BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    } else {
        BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.20f)
                )
            )
        )
    }

    // Custom Box implementation with height increased by 3px to 38.dp.
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(38.dp) // EXACT 38px box size
            .clip(RoundedCornerShape(30.dp))
            .background(glassBg)
            .border(glassBorder, RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon rendering exactly 20px (20.dp)
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (isDark) PalWhite else PalTextDark),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            // Button label text exactly 17px (17.sp) using Bricolage Grotesque Variable family
            Text(
                text = text,
                fontFamily = BricolageVariableFontFamily,
                fontSize = 17.sp, // EXACT 17px text size
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                color = if (isDark) PalWhite else PalTextDark,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OnboardingButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(44.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .let { 
                if (borderColor != null) {
                    it.border(BorderStroke(1.dp, borderColor), RoundedCornerShape(30.dp))
                } else {
                    it
                }
            }
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
        }
    }
}
